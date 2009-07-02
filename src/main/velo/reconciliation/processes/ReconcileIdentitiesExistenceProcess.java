package velo.reconciliation.processes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import velo.collections.Accounts;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.entity.Account;
import velo.entity.Resource;
import velo.exceptions.EventExecutionException;
import velo.exceptions.ReconcileProcessException;
import velo.reconciliation.summary.ReconcileIdentitiesExistenceProcessSummary;
import velo.reconciliation.summary.ResourceReconcileProcess.ReconcileEvents;

/**
 * @author Asaf Shakarchi
 *
 * A reconciliation process that reconciles whether identities were removed from a resource or not.
 * 
 * This process expects the entire accounts list that exists on a resource,
 * It expects the account list to be already loaded, only the primary identifier (accountId [account->name]) of each identity should exist for efficiency.
 * 
 * <i>note: this process only verifies whether an identity was removed from a resource by comparing the active identities fetched from the controller
 * to the current identities snapshot that is stored in Velo repository</i>
 */
public class ReconcileIdentitiesExistenceProcess {
	private static Logger log = Logger.getLogger(ReconcileIdentitiesExistenceProcess.class.getName());
	private static final String REMOVED_IDENTITY_EVENT = "IDENTITY_REMOVED";
	
	Resource resource = null;
	StopWatch subStopWatch = new StopWatch();

	
	/**
	 * @param allIdentities - A list of identities loaded from the resource (as late as possible).
	 * @throws ReconcileProcessException
	 */
	public void execute(Accounts allIdentities) throws ReconcileProcessException {
		try {
			log.info("Starting identities existence reconciliation process for resource '" + getResource().getDisplayName() + "'");
			log.info("Recieved active accounts from resource with amount '" + allIdentities.size() + "'");

			ReconcileIdentitiesExistenceProcessSummary summary = new ReconcileIdentitiesExistenceProcessSummary(getResource().getResourceId(),getResource().getUniqueName());
			StopWatch globalSW = new StopWatch();
			globalSW.start();
			//TODO: Loading the full accounts entities is slow?, if so, fetch only account names as we don't need anything else for this process. 
			Set<Account> knownIdentities = getKnownIdentities();
			log.info("Amount of known identities is: '" + knownIdentities.size() + "'");

			
			//Initialize a SET that will contain all accounts to be removed
			Set<Account> identitiesToRemoveFromRepository = new HashSet<Account>();

			//TODO:Collection->Map Should be done in smooks if possible
			Map<String,Account> activeIdentitiesMap = getActiveIdentities(allIdentities);
			
			//Iterate over each of the known accounts
			for (Account currKnownIdentity : knownIdentities) {
				if (activeIdentitiesMap.containsKey(currKnownIdentity.getNameInRightCase())) {
					//MATCH!
				} else {
					//REMOVED! 
					identitiesToRemoveFromRepository.add(currKnownIdentity);
					summary.addIdentity(currKnownIdentity.getName(),ReconcileEvents.IDENTITY_REMOVED);
				}
			}
			
			
			log.info("-->> Amount of known identities to be removed '" + identitiesToRemoveFromRepository.size() + "' <<--");
			log.info("Invoking IDENTITY_REMOVED per entry!");
			OperationContext context = new OperationContext();
			context.addVar("resource", getResource());
			context.addVar("resourceUniqueName", getResource().getUniqueName());
			context.addVar("resourceDisplayName", getResource().getDisplayName());
			
			
			for (Account accToRemove : identitiesToRemoveFromRepository) {
				context.addVar("account", accToRemove);
				context.addVar("accountName", accToRemove.getNameInRightCase());
				log.info("Removing account name '" + accToRemove.getName() + "' related to resource '" + resource.getDisplayName() + "'");
				
				getEventManager().raiseReconcileEvent(REMOVED_IDENTITY_EVENT, getResource().getReconcilePolicy(), context);
			}
			
			
			//TODO: Update the last time the identities existence reconciliation process has finished
			
			context.clear();
			globalSW.stop();
			
			log.info("Finished successfully the identities existence reconciliation process for resource '" + getResource().getDisplayName() + "' in '" + globalSW.getTime()/1000 + "' seconds.");
		}catch(EventExecutionException e) {
			throw new ReconcileProcessException(e.getMessage());
		}catch(NamingException e) {
			throw new ReconcileProcessException(e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	

	
	//helper
	public EventManagerLocal getEventManager() throws NamingException {
		InitialContext ic = new InitialContext();
		EventManagerLocal eventManager = (EventManagerLocal) ic.lookup("EventBean/local");
		
		return eventManager;
	}
	
	//not in use
	public AccountManagerLocal getAccountManager() throws NamingException {
		InitialContext ic = new InitialContext();
		AccountManagerLocal accountManager = (AccountManagerLocal) ic.lookup("AccountBean/local");
		
		return accountManager;
	}

	public Set<Account> getKnownIdentities() {
		subStopWatch.reset();
		subStopWatch.start();
		
		log.debug("Loading 'known accounts' of resource, please wait...");
		Set<Account> accounts = getResource().getAccounts();
		subStopWatch.stop();
		
		log.debug("Finished loading 'known accounts', size '" + accounts.size() + "', loaded in '" + subStopWatch.getTime()/1000 + "' seconds.");
		
		return accounts;
	}

	public Map<String,Account> getActiveIdentities(Accounts accounts) {
		subStopWatch.reset();
		subStopWatch.start();
		Map<String,Account> activeAccountsMap = new HashMap<String,Account>();
		for (Account currAccount : accounts) {
			activeAccountsMap.put(getActiveIdentityIdentifierInCorrectCase(currAccount),currAccount);
		}
		subStopWatch.stop();
		
		return activeAccountsMap;
	}
	
	
	public String getActiveIdentityIdentifierInCorrectCase(Account account) {
		return account.getNameInRightCase();
	}

	public Resource getResource() {return resource;}
	public void setResource(Resource resource) {this.resource = resource;}
	
	
	
}
