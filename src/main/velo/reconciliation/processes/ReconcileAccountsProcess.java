package velo.reconciliation.processes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import velo.actions.readyActions.ReadyActionAPI;
import velo.collections.Accounts;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.utils.JndiLookup;
import velo.entity.Account;
import velo.entity.AccountAttribute;
import velo.entity.AccountAttributeValue;
import velo.entity.Attribute;
import velo.entity.AttributeValue;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcileProcessSummary;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.SequencedAction;
import velo.entity.SystemEvent;
import velo.entity.User;
import velo.entity.LogEntry.EventLogLevel;
import velo.entity.ReconcileProcessSummary.ReconcileProcesses;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventEntityType;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventSeverities;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEvents;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.EventExecutionException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.action.ActionExecutionException;

/**
 * @author Asaf Shakarchi
 * 
 * This process compares whether each of the identity(account) exists in Velo repo or not, if not, it recognizes the
 * entry as a new identity and triggers the IDENTITY_CREATED event, in case where the identity already exists,
 * The process will compare each of the identity's attributes (only for those that flagged as 'synchronized' 
 * to the local repo's (USER/ACCOUNT ATTRS?) In case where an attribute was modified, it'll raise the 'IDENTITY_ATTRIBUTE_MODIFIED' 
 * event per modified attribute.
 *  
 * It will also raise an 'IDENTITY_MODIFIED' event for the entire account modifications 
 * with the old/new values of each modified attribute.
 * 
 * This process expects a list of identities (accounts) that were modified/inserted from the last previous execution of this process.
 * 
 * <i>Note: This process expects each account to be fully loaded with all of its relevant attributes that flagged to be synchronized</i>
 */
public class ReconcileAccountsProcess {
	private static Logger log = Logger.getLogger(ReconcileAccountsProcess.class.getName());
	//private static final String IDENTITY_CREATED_UNASSIGNED_EVENT = "IDENTITY_UNASSIGNED";
	//private static final String IDENTITY_CREATED_UNMATCHED_EVENT = "IDENTITY_UNMATCHED";
	//private static final String IDENTITY_MODIFIED_EVENT = "IDENTITY_MODIFIED";
	//private static final String IDENTITY_ATTRIBUTE_MODIFIED_EVENT = "IDENTITY_ATTRIBUTE_MODIFIED";
	//private static final String IDENTITY_REMOVED_EVENT = "IDENTITY_REMOVED";
	
	private ReconcileEvent IDENTITY_CREATED_UNASSIGNED_EVENT;
	private ReconcileEvent IDENTITY_CREATED_UNMATCHED_EVENT;
	private ReconcileEvent IDENTITY_MODIFIED_EVENT;
	private ReconcileEvent IDENTITY_ATTRIBUTE_MODIFIED_EVENT;
	private ReconcileEvent IDENTITY_REMOVED_EVENT;
	
	//Used by 'isAccountExistsInRepository'/'findAccountInRepository'
	private Map<String,Account> allAccountsInRepositoryForResource;
	//required for full process to determine which accounts names were removed
	private Set<String> allAccountsNamesInRepositoryForResourceForRemoval;
	
	
	Resource resource = null;
	StopWatch subStopWatch = new StopWatch();
	ReconcileProcessSummary rps = null;

	
	
	public void executeFull(Accounts activeIdentities) throws ReconcileProcessException {
		execute(activeIdentities, true);
	}
	
	public void executeIncrementally(Accounts incrementalActiveIdentities) throws ReconcileProcessException {
		execute(incrementalActiveIdentities, false);
	}
	
	
	private void execute(Accounts activeIdentities, boolean full) throws ReconcileProcessException {
		log.info("Starting reconcile accounts process for resource '" + getResource().getDisplayName() + "'");
		log.debug("Recieved an list of active accounts from resource with amount '" + activeIdentities.size() + "'");

		StopWatch globalSW = new StopWatch();
		globalSW.start();
		
		
		try {
			rps = null;
			if (full) {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_IDENTITIES_FULL);
			} else {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_IDENTITIES_INCREMENTAL);
			}
			rps.start();
			
			//GENERAL, REQUIRED FOR ALL EVENTS
			OperationContext context = new OperationContext();
			context.addVar("resource", getResource());
			context.addVar("resourceUniqueName", getResource().getUniqueName());
			context.addVar("resourceDisplayName", getResource().getDisplayName());
			context.addVar("rps", rps);
			//expose the ReadyActions APIs to the context for advanced features
			context.addVar("veloAPI",ReadyActionAPI.getInstance());

			
			//For efficiency, load ALL of the events once.
			IDENTITY_CREATED_UNASSIGNED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.IDENTITY_UNASSIGNED_EVENT_NAME); 
			IDENTITY_CREATED_UNMATCHED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.IDENTITY_UNMATCHED_EVENT_NAME);
			IDENTITY_MODIFIED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.IDENTITY_MODIFIED_EVENT_NAME);
			IDENTITY_ATTRIBUTE_MODIFIED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.IDENTITY_ATTRIBUTE_MODIFIED_EVENT_NAME);
			IDENTITY_REMOVED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.IDENTITY_REMOVED_EVENT_NAME);
			
			//Initialize a SET that will contain all accounts that were inserted
			Set<Account> createdActiveIdentities = new HashSet<Account>();
			Set<Account> existedActiveIdentities = new HashSet<Account>();
			
			if (full) {
				subStopWatch.start();
				log.debug("(START) Loading all accounts from repository (in full reconcile identities process only) one time only...");
				allAccountsInRepositoryForResource = resource.getAccountsAsMap();
				
				for (Map.Entry<String,Account> currAccEntryMap : allAccountsInRepositoryForResource.entrySet()) {
					//System.out.println(currAccEntryMap.getValue().getAccountAttributes().size());
					for (AccountAttribute currAA : currAccEntryMap.getValue().getAccountAttributes()) {
						currAA.getValues();
						//System.out.println(currAA.getValues().size());
					}
				}
				
				
				allAccountsNamesInRepositoryForResourceForRemoval = new HashSet<String>();
				for (Account currAccount : allAccountsInRepositoryForResource.values()) {
					allAccountsNamesInRepositoryForResourceForRemoval.add(currAccount.getNameInRightCase());
				}
				
				subStopWatch.stop();
				log.debug("(FINISHED) Loading all accounts from repository (in full reconcile identities process) one time only (with amount '" + allAccountsInRepositoryForResource.size() +"') in '" + subStopWatch.getTime()/1000 + "' seconds.");
				subStopWatch.reset();
			}
			
			
			//Perform sanity checks of the amounts for full only
			if (full) {
				int activeAmount = activeIdentities.size();
				int repoAmount = allAccountsInRepositoryForResource.size();
				Integer allowedDiffInPercentages = resource.getReconcilePolicy().getSanityCheckDiffPercentagesOfIdentities();

				Integer diff = null;
				if (activeAmount > repoAmount) {
					diff = (activeAmount-repoAmount) / activeAmount * 100;
				} else {
					diff = (repoAmount-activeAmount) / repoAmount * 100;
				}
				
				if (diff > allowedDiffInPercentages) { 
					throw new ReconcileProcessException("Sanity check failure: Accounts difference between repository and resource is higher than '" + allowedDiffInPercentages + "%, (which is '" + diff + "'%), accounts in repo: '" + repoAmount + "', while active amount is: '" + activeAmount + "'");
				}
			} else {
				log.debug("Skipping sanity checks for incremental process.");
			}
			
			
			
			SequencedAction correlationRule = resource.getReconcilePolicy().getCorrelationRule();
			SequencedAction confirmationRule = resource.getReconcilePolicy().getConfirmationRule();

			
			subStopWatch.start();
			log.debug("(START): Iterating over recieved active identities, determining whether each exists in repository or not.");
			//Iterate over each active account, verify whether it exists on velo repo or not
			for (Account currActiveAccount : activeIdentities) {
				log.trace("Determining whether active account '" + currActiveAccount.getNameInRightCase() + "' exists in Velo repo...");
				if (isAccountExistOnRepository(currActiveAccount.getNameInRightCase(), full)) {
					log.trace("Active account '" + currActiveAccount.getNameInRightCase() + "' was -found- in Velo repo. (adding account to the 'verified existed' attributes list)");
					existedActiveIdentities.add(currActiveAccount);

					if (full) {
						allAccountsNamesInRepositoryForResourceForRemoval.remove(currActiveAccount.getNameInRightCase());
					}
				} else {
					log.trace("Active account '" + currActiveAccount.getNameInRightCase() + "' was -not found- in Velo repo. (adding account to the new created list)");
					//new identity was found, store the account on the appropriate list
					createdActiveIdentities.add(currActiveAccount);
				}
			}
			subStopWatch.stop();
			log.debug("(FINISHED): Iterating over recieved active identities, determining whether each exists in repository or not in '" + subStopWatch.getTime() / 1000 + "' seconds.");
			subStopWatch.reset();
			
			
			//Raising new created event per created account
			log.debug("START: Raising UNASSIGNED/UNMATCHED event for '" + createdActiveIdentities.size() + "' amount of newly created identities.");
			subStopWatch.start();
			for (Account newCreatedAccount : createdActiveIdentities) {
				context.addVar("account", newCreatedAccount);
				context.addVar("accountName", newCreatedAccount.getNameInRightCase());
				
				User matchedUserEntity = null;
				//perform account->user correlation
				if (correlationRule != null) {
					matchedUserEntity = getMatchedUserViaCorrelation(newCreatedAccount, correlationRule, confirmationRule);
				}
				
				if (matchedUserEntity != null) {
					log.info("Raising 'IDENTITY_UNASSIGNED' event for account name '" + newCreatedAccount.getNameInRightCase() + "', matched user name is '" + matchedUserEntity.getName() + "'");
					rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_UNASSIGNED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, newCreatedAccount.getName(), "A new identity named '" + newCreatedAccount.getName() + "' was discovered on resource.");
					context.addVar("matchedUser", matchedUserEntity);
					context.addVar("matchedUserName", matchedUserEntity.getName());
					
					newCreatedAccount.setUser(matchedUserEntity);
					getEventManager().raiseReconcileEvent(IDENTITY_CREATED_UNASSIGNED_EVENT, getResource().getReconcilePolicy(), context);
					
					//cleanup the matchedUser
					context.removeVar("matchedUser");
					context.removeVar("matchedUserName");
				} else {
					log.info("Raising 'IDENTITY_UNMATCHED' event for account name '" + newCreatedAccount.getNameInRightCase() + "'");
					rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_UNMATCHED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, newCreatedAccount.getName(), "A new identity named '" + newCreatedAccount.getName() + "' was discovered on resource.");
					getEventManager().raiseReconcileEvent(IDENTITY_CREATED_UNMATCHED_EVENT, getResource().getReconcilePolicy(), context);
				}
				
				
				//ITERATION CLEANUPS
				matchedUserEntity = null;
				context.removeVar("account");
				context.removeVar("accountName");
			}
			subStopWatch.stop();
			log.debug("FINISHED: Raising UNASSIGNED/UNMATCHED of newly created identities in '" + subStopWatch.getTime()/1000 + "' seconds.");
			subStopWatch.reset();
			
			
			//At this point, context must be cleaned, except the initialized vars before the last iteration.
			
			
			//ATTRIBUTE SYNCHRONIZATION - Performing attributes compare per existence identity
			log.debug("(START) Comparing attributes for all existence accounts (with amount '" + existedActiveIdentities.size() + "')");
			subStopWatch.start();
			Map<String,Attribute> modifiedAttributes = new HashMap<String,Attribute>();
			log.debug("Synchronizing attributes for *" + existedActiveIdentities.size() + "* accounts.");
			for (Account currExistedActiveAccount : existedActiveIdentities) {
				context.addVar("accountName",currExistedActiveAccount.getNameInRightCase());
				context.addVar("account",currExistedActiveAccount);
				
				//TODO: Not efficient enough
				//Account correspondingRepoAccountToActiveAccount = getAccountManager().findAccount(currExistedActiveAccount.getName(), resource.getUniqueName());
				Account correspondingRepoAccountToActiveAccount = findAccountInRepository(currExistedActiveAccount.getNameInRightCase(), full);
				context.addVar("accountFromRepo",correspondingRepoAccountToActiveAccount);
				
				
				//Iterate over attributes(PERSISTENCE KIND/TRANSIENT) that flagged as synchronized, seek for changes.
				//Its important to run on getAttributes() that is persistence/active, because we sync not only persistence!
				for (Attribute currAttribute : currExistedActiveAccount.getAttributes().values()) {
					ResourceAttribute currRA = currExistedActiveAccount.getResource().getResourceAttribute(currAttribute.getUniqueName());
					if (currRA == null) {
						//should never happen as attribute had not to be loaded at all.
						String err = "Could not find corresponding Resource Attribute (definition!) to compare active attribute named '" + currAttribute.getUniqueName() + "' for account name '" + currExistedActiveAccount.getName() + "(" + currExistedActiveAccount.getResource().getDisplayName() + ")'";
						log.trace(err);
						continue;
					}
					
					if (!currRA.isSynced()) {
						continue;
					}
					
					
					if (currRA.isPersistence()) {
						AccountAttribute currAccAttr = correspondingRepoAccountToActiveAccount.getAccountAttribute(currRA.getUniqueName());
						
						if (currAccAttr == null) {
							//Persist a new one (will never understand why transient account is not enough to persist an attribute)
							//currAccAttr = AccountAttribute.factory(currRA,getEntityManager().merge(currExistedActiveAccount));
							currAccAttr = AccountAttribute.factory(currRA,correspondingRepoAccountToActiveAccount);
							
							
							try {
								currAccAttr.importValues(currAttribute.getValues());
								context.addVar("attribute",currAttribute);
								modifiedAttributes.put(currAttribute.getUniqueName(),currAttribute);
								rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_ATTRIBUTE_MODIFIED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.ACCOUNT_ATTRIBUTE, currAttribute.getUniqueName(), "An account attribute '" + currAttribute.getUniqueName() + "' was modified(created in repository!) for account name '" +  correspondingRepoAccountToActiveAccount.getNameInRightCase() + "'");
								
								context.addVar("sync", new Boolean(true));
								getEventManager().raiseReconcileEvent(IDENTITY_ATTRIBUTE_MODIFIED_EVENT, getResource().getReconcilePolicy(), context);
								
								//Persist only if synchronization flag was not set to false.
								if ((Boolean)context.get("sync")) {
									getEntityManager().persist(currAccAttr);
								}
							} catch (AttributeSetValueException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							continue;
						} else {
							if (currAttribute.equalToAttribute(currAccAttr.getAsStandardAttribute())) {
								//Equal, do nothing
							} else {
								//Trigger IDENTITY_ATTRIBUTE_MODIFIED
								
								//set the persisted old account's attr into the old values of the current new active attriubte
								currAttribute.setOldValues(currAccAttr.getValuesAsStandardValues());
								context.addVar("attribute",currAttribute);
								modifiedAttributes.put(currAttribute.getUniqueName(),currAttribute);
								rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_ATTRIBUTE_MODIFIED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.ACCOUNT_ATTRIBUTE, currAttribute.getUniqueName(), "An account attribute '" + currAttribute.getUniqueName() + "' was modified for account name '" +  correspondingRepoAccountToActiveAccount.getNameInRightCase() + "'");
								
								context.addVar("sync", new Boolean(true));
								getEventManager().raiseReconcileEvent(IDENTITY_ATTRIBUTE_MODIFIED_EVENT, getResource().getReconcilePolicy(), context);
								
								
								//Update only if synchronization flag was not set to false.
								if ((Boolean)context.get("sync")) {
									//UPDATE THE VALUES
									int looper=0;
									int numOfOldValues = currAccAttr.getValues().size();
									for (AttributeValue currAV : currAttribute.getValues()) {
										looper++;
										if (looper <= currAccAttr.getValues().size()) {
											try {
												currAccAttr.setLastUpdateDate(new Date());
												currAccAttr.getValues().get(looper-1).setLastUpdateDate(new Date());
												currAccAttr.getValues().get(looper-1).setValue(currAV.getValueAsObject());
											} catch (AttributeSetValueException e) {
												e.printStackTrace();
											}
										} else {
											try {
												currAccAttr.factoryValue().setValue(currAV.getValueAsObject());
											} catch (AttributeSetValueException e) {
												e.printStackTrace();
											}
										}
									}
									
									
									if (looper < numOfOldValues) {
										for (int i=looper;i<numOfOldValues;i++) {
											AccountAttributeValue currAAVToRemove = currAccAttr.getValues().get(i-1);
											currAccAttr.getValues().remove(currAAVToRemove);
										}
									}
									
									
									getEntityManager().merge(currAccAttr);
								}
							}
						}
					} else {
						//Attribute is not persistence, should be loaded virtually.
						log.trace("ONLY PERSISTENCE RA ARE SUPPORTED RIGHT NOW");
					}
				}
				
				if (modifiedAttributes.size() > 0) {
					context.addVar("attributes", modifiedAttributes);
					rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_MODIFIED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currExistedActiveAccount.getNameInRightCase(), "Account name '" + currExistedActiveAccount.getNameInRightCase() + "' was modified.");
					getEventManager().raiseReconcileEvent(IDENTITY_MODIFIED_EVENT, getResource().getReconcilePolicy(), context);
				} else {
					rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_CONFIRMED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currExistedActiveAccount.getNameInRightCase(), "Account name '" + currExistedActiveAccount.getNameInRightCase() + "' exists and confirmed (wasnt changed at all).");
				}
				
				
				//Recorrelate existence orphan accounts if neccessary 
				if ( (resource.getReconcilePolicy().isReCorrelateOrphanIdentities()) && (correlationRule != null) ) {
					if (correspondingRepoAccountToActiveAccount.getUser() == null) {
						
						
						//recorrelate account (passing the active account as its active attributes are required
						User matchedUserEntity = getMatchedUserViaCorrelation(currExistedActiveAccount,correlationRule, confirmationRule);
						
						if (matchedUserEntity != null) {
							correspondingRepoAccountToActiveAccount.setUser(matchedUserEntity);
							getAccountManager().updateAccount(correspondingRepoAccountToActiveAccount);
						}
					}
				}
				
				
				//CLEAN THE MODIFIED ATTRIBUTES
				modifiedAttributes.clear();
			}
			//Make sure sync is back as true by default
			context.set("sync", true);
			context.removeVar("attributes");
			context.removeVar("attribute");
			context.removeVar("accountFromRepo");
			context.removeVar("account");
			context.removeVar("accountName");
			
			
			subStopWatch.stop();
			log.debug("(FINISHED) Comparing attributes for all existence accounts (amount '" + existedActiveIdentities.size() + "') in '" + subStopWatch.getTime()/1000 + "' seconds.");
			subStopWatch.reset();

			
			
			
			//--=--ONLY--=-- if full, take care of remove identities
			if (full) {
				subStopWatch.start();
				log.info("(START) -Full- reconciliation was invoked, raising IDENTITY_REMOVED event for all removed identities (with amount '" + allAccountsNamesInRepositoryForResourceForRemoval.size() + "')");
				for (String currAccountNameToBeRemoved : allAccountsNamesInRepositoryForResourceForRemoval) {
					Account currAccountToBeRemoved = findAccountInRepository(currAccountNameToBeRemoved, full);
					
					context.addVar("account",currAccountToBeRemoved);
					context.addVar("accountName",currAccountToBeRemoved.getNameInRightCase());
					getEventManager().raiseReconcileEvent(IDENTITY_REMOVED_EVENT, getResource().getReconcilePolicy(), context);
					rps.addEvent(ReconcileProcessSummaryEvents.IDENTITY_REMOVED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currAccountToBeRemoved.getNameInRightCase(), "Account name '" + currAccountToBeRemoved.getNameInRightCase() + "' was removed from resource.");
				}
				subStopWatch.stop();
				log.info("(FINISHED) raising IDENTITY_REMOVED event for all removed identities (with amount '" + allAccountsNamesInRepositoryForResourceForRemoval.size() + "') in '" + subStopWatch.getTime()/1000 + "' seconds.");
				subStopWatch.reset();
			}
			
			//clean context from last iteration
			context.removeVar("account");
			context.removeVar("accountName");
			
			
			globalSW.stop();
			rps.setSuccessfullyFinished(true);
			rps.end();
			getReconcileManager().persistReconcileProcessSummary(rps);
			
			//EXECUTE POST EVENT
			getEventManager().raiseSystemEvent(SystemEvent.EVENT_RESOURCE_RECONCILIATION_POST, context);

			
			context.clear();
			log.info("Finished successfully the identities reconciliation process for resource '" + getResource().getDisplayName() + "' in '" + globalSW.getTime()/1000 + "' seconds.");
		}catch(EventExecutionException e) {
			throw new ReconcileProcessException(e.getMessage());
		}catch(NamingException e) {
			throw new ReconcileProcessException(e.getMessage());
		}
	}
	
	
	

	
	//helper
	public User getMatchedUserViaCorrelation(Account account, SequencedAction correlationRule, SequencedAction confirmationRule) throws NamingException {
		OperationContext correlationRuleContext = new OperationContext();
		
		correlationRule.setContext(correlationRuleContext);
		//TODO: Shell we pass the exact account name or in the right case to the correlation rule? 
		correlationRuleContext.addVar("accountName", account.getNameInRightCase());
		correlationRuleContext.addVar("account", account);
		correlationRuleContext.addVar("matchedUsers", new ArrayList<String>());
		
		String matchedUser = null;
		User matchedUserEntity = null;
		
		try {
			correlationRule.execute();
			ArrayList<String> matchedUsers = (ArrayList<String>)correlationRule.getContext().get("matchedUsers");
			
			if (matchedUsers.size() == 1) {
				matchedUser = matchedUsers.iterator().next();
			} else if (matchedUsers.size() > 1) {
				try {
					//Requires confirmation rule
					confirmationRule.setContext(correlationRuleContext);
					confirmationRule.execute();
					
					matchedUser = (String)correlationRuleContext.get("matchedUser");
				}catch(ActionExecutionException e) {
					rps.addLog(EventLogLevel.ERROR, "Account name '" + account.getNameInRightCase() + "' could not be correlated due to confimration rule execution error: " + e.getMessage());
					return null;
				}
			} else {
				//no owner found
			}
			
			
			
			if (matchedUser != null) {
				matchedUserEntity = getUserManager().findUser(matchedUser);
				
				if (matchedUserEntity != null) {
					return matchedUserEntity;
				} else {
					log.info("Correlation rule was returning a matched user '" + matchedUser + "' but it does not exist in repository.");
					return null;
				}
			} else {
				return null;
			}
			
		}catch(ActionExecutionException e) {
			rps.addLog(EventLogLevel.ERROR, "Account name '" + account.getNameInRightCase() + "' could not be correlated: " + e.getMessage());
			return null;
		}
	}
	
	
	
	
	
	public Account findAccountInRepository(String accountNameInRightCase, boolean full) throws NamingException {
		//For efficiency, if the reconciliatin is full, first load all accounts, then always search on the account map locally
		//Otherwise, in incremental, can look directly in DB.
		if (full) {
			if (allAccountsInRepositoryForResource == null) {
			}
		
			//the map keys are the account names in the right case, thus check containsKey in the right case
			return allAccountsInRepositoryForResource.get(accountNameInRightCase);
		} else {
			//search in DB whether the account exist or not. (this method supports case sensitive check for resources that are case sensitive)
			return getAccountManager().findAccount(accountNameInRightCase, getResource());
		}
	}
	
	
	
	/**
	 * Find whether an account exist on repo or not
	 * 
	 * @param accountNameInRightCase
	 * @param full
	 * @return
	 * @throws NamingException
	 */
	public boolean isAccountExistOnRepository(String accountNameInRightCase, boolean full) throws NamingException {
		//For efficiency, if the reconciliatin is full, first load all accounts, then always search on the account map locally
		//Otherwise, in incremental, can look directly in DB.
		if (full) {
			if (allAccountsInRepositoryForResource == null) {
				StopWatch sw = new StopWatch();
				sw.start();
				log.debug("(START) Loading all accounts from repository (in full reconcile identities process) one time only...");
				allAccountsInRepositoryForResource = resource.getAccountsAsMap();
				log.debug("(FINISHED) Loading all accounts from repository (in full reconcile identities process) one time only (with amount '" + allAccountsInRepositoryForResource.size() +"') in '" + sw.getTime()/1000 + "' seconds.");
				sw.stop();
			}
		
			//the map keys are the account names in the right case, thus check containsKey in the right case
			return allAccountsInRepositoryForResource.containsKey(accountNameInRightCase);
		} else {
			//search in DB whether the account exist or not. (this method supports case sensitive check for resources that are case sensitive)
			return getAccountManager().isAccountExists(accountNameInRightCase, getResource());
		}
	}
	
	
	public EventManagerLocal getEventManager() throws NamingException {
		InitialContext ic = new InitialContext();
		EventManagerLocal eventManager = (EventManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("EventBean"));
		
		return eventManager;
	}
	
	public ReconcileManagerLocal getReconcileManager() throws NamingException {
		InitialContext ic = new InitialContext();
		ReconcileManagerLocal reconcileManager = (ReconcileManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("ReconcileBean"));
		
		return reconcileManager;
	}
	
	public UserManagerLocal getUserManager() throws NamingException {
		InitialContext ic = new InitialContext();
		UserManagerLocal reconcileManager = (UserManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("UserBean"));
		
		return reconcileManager;
	}
	
	public EntityManager getEntityManager() {
		try {
			InitialContext ic = new InitialContext();
			return (EntityManager)ic.lookup("java:/veloDataSourceEM");
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//not in use
	public AccountManagerLocal getAccountManager() throws NamingException {
		InitialContext ic = new InitialContext();
		AccountManagerLocal accountManager = (AccountManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("AccountBean"));
		
		return accountManager;
	}
	
	public Resource getResource() {return resource;}
	public void setResource(Resource resource) {this.resource = resource;}
	
	
	
}
