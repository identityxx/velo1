/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.reconcilidation;

import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.account.ActiveAccountsConstructor;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ScriptLoadingException;
import velo.processSummary.resourceReconcileSummary.ResourceReconcileSummary;
import velo.processSummary.resourceReconcileSummary.SummaryAccount;
import velo.reconcilidation.events.AccountEvent;
import velo.reconcilidation.events.AccountEventsFactory;
import velo.reconcilidation.events.reconcileAccounts.UnmatchedAccountEvent;
import velo.rules.AccountsCorrelationRule;
import velo.utils.Stopwatch;

/**
 * @author Asaf Shakarchi
 *
 * A class that represents a ReconcileAccounts process for a certain
 * resource
 */
public class ReconcileAccounts {
    
	private ResourceReconcileSummary rrs;
    private static Logger log = Logger.getLogger(ReconcileAccounts.class.getName());
    private static Logger logForCorelRule = Logger.getLogger("velo.scrpits.reconcile.correlationRule");
    
    /**
     * Create an AccountTreeMappedList to hold all accounts fetched from the resource,
     * Virtually load account entities from fetched active data
     */
    private HashMap<String,Account> activeAccounts;
    
    
    public ReconcileAccounts(ResourceReconcileSummary rrs) {
    	this.rrs = rrs;
    }
    
    /**
     * Constructor Mainly initalize the required EJBs objects into the class
     */
    public ReconcileAccounts() {
    	
    }
    
    
    /**
     * Reconcile accounts on the specified resource
     *
     * @param resource
     * The resource entity to reconcile accounts for
     * @throws ReconcileAccountsException
     */
    public void performResourceAccountsReconciliation(Resource resource) throws ReconcileAccountsException {
        try {
            long startExecutionTime = System.currentTimeMillis();
            
            //TODO: Replace with remote calls if reconciliation may performed remotely
            InitialContext ic = new InitialContext();
            AccountManagerLocal accountManager = (AccountManagerLocal) ic.lookup("velo/AccountBean/local");
            ResourceManagerLocal resourceManager = (ResourceManagerLocal) ic.lookup("velo/ResourceBean/local");
            UserManagerLocal userManager = (UserManagerLocal) ic.lookup("velo/UserBean/local");
            
            
            Stopwatch stopWatch = new Stopwatch(false);
            
            stopWatch.start();
            ActiveAccountsConstructor aac = new ActiveAccountsConstructor();
            activeAccounts = aac.constructByXml(resource, false);
            stopWatch.stop();
            
            log.debug("List of ActiveAccounts on resource name: '"
                    + resource.getDisplayName()
                    + "', resulted *"
                    + activeAccounts.size()
                    + "* accounts, loaded in '" + stopWatch.asSeconds() + "' seconds.");
            
            //update the summary
            rrs.accountsAmountInResource = activeAccounts.size();
            
            
            stopWatch.start();
            log.debug("Getting all resource accounts from repository, please wait...");
            // Get a set of all claimed accounts in repository
            Set<Account> veloClaimedAccountList = resource.getAccounts();
            log.debug("Finished loading all resource accounts from repository...");
            
            stopWatch.stop();
            //debug
            System.out.println("Velo claims to have *"
                    + veloClaimedAccountList.size()
                    + "* accounts for target system name: '"
                    + resource.getDisplayName() + "', loaded in '" + stopWatch.asSeconds() + " seconds.");
            

            
            
            // Iterate through the claimed account
            log.debug("Iterating over claimed accounts, comparing each claimed account to the corresponding expected active accounts, this will take a while...");
            int thousandBulkCounter = 0;
            
            Map<String,Account> accountsToRemoveFromRepository = new HashMap<String,Account>();
            
            for (Account currClaimedAccount : veloClaimedAccountList) {
                thousandBulkCounter ++;
                
                if (thousandBulkCounter > 1000) {
                    log.trace("Finished handling another bulk of 1000 accounts...");
                    thousandBulkCounter = 0;
                }
                
                // Check if the claimed account exist within the activeSet
                String currClaimedAccountName = currClaimedAccount.getName();
                
                //Note: In non-casesensitive resources, Each key's mapEntry in ActiveAccounts MAP is built as lowercase
                //to compare safely, so it is a MUST to set claimedAccountAccountName as uppercase too here!
                if (!resource.isCaseSensitive()) {
                    currClaimedAccountName = currClaimedAccountName.toUpperCase();
                }
                
                log.trace("Checking whether ActiveAccounts map contains the claimed account named '" + currClaimedAccountName + "'");

                if (activeAccounts.containsKey(currClaimedAccountName)) {
                    // Claimed account found, result a CONFRIMED event!
                    AccountEvent event = AccountEventsFactory.factoryConfirmedAccountEvent(resource, currClaimedAccount);
                    event.execute();
                    
                    // ###ATTRIBUTE SYNCHRONIZATION PER CONFIRMED ACCOUNT###
                    // For each account that is CONFIRMED, syncrhonize
                    // attributes too
                    // Algorithm:
                    // - Load the "virtual" attributes of each IDM claimed
                    // account
                    // - Expect per loaded "virtual" attribute to have a
                    // corresponding attriubte on the corresponding
                    // activeAccount
                    // - Call each registered event and pass the 'claimed loaded
                    // virtual account' and the 'active account' from the
                    // activeSet
                    
                    
                    // Perform the virtual account attributes loading
//                    currClaimedAccount = am
//                            .loadVirtualAccountAttributes(currClaimedAccount,true);
                    
                                        /*
                                        // Factory the syncing attributes event and Execute it.
                                        AccountAnalyzerEvent syncAttrsEvent = AccountAnalyzerEventFactory
                                                        .factorySynchAccountAttributesEvent(
                                                                        currClaimedAccount, activeAccounts
                                                                                        .get(currClaimedAccount
                                                                                                        .getName()));
                                        syncAttrsEvent.execute();
                                         */
                    
                    log.trace("Remove Active Account with Claimed account's name '" + currClaimedAccountName + "' from ActiveAccounts map");
                    // Remove the account from the activeAccounts
                    activeAccounts.remove(currClaimedAccountName);
                } else {
                    // Claimed account was NOT found, result a DELETED event!
                    AccountEvent event = AccountEventsFactory.factoryDeletedAccountEvent(resource,currClaimedAccount,accountsToRemoveFromRepository);
                    event.execute();
                }
            }
            
            
            log.debug("Iterating over the left 'active accounts' on target system: "
                    + resource.getDisplayName()
                    + ", left accounts number to manage: "
                    + activeAccounts.size());
            
            
            int counter = 0;
            
            //Moved to MAP since duplications may occure (XML may have two users with the same name)
            //A set of accounts that should get persisted at the end of this iteration
            //Set<Account> accountsToPersist = new HashSet<Account>();
            Map<String,Account> accountsToPersist = new HashMap<String,Account>();
            
            AccountsCorrelationRule acr = null;
            boolean isActivateAccountCorrelationRule = resource.getReconcilePolicy().isActivateCorrelationRule();
            boolean isAutoCorrelateAccountIfMatchedToUser = resource.getReconcilePolicy().isAutoCorrelateAccountIfMatchedToUser();
            
            /*TODO: add support for correlation rule (OLD SHIT!)
            if (!isActivateAccountCorrelationRule) {
            	//debug
            	System.out.println("Not activating usage of correlation rule according to Reoncile Policy");
            } else {
                acr = resource.correlationRuleFactory();
            }
            */
            
            try {
            	GroovyObject go = null;
            	
            	//compile correlation rule before iteration
            	if (isActivateAccountCorrelationRule) {
            		if (resource.getReconcilePolicy().getReconcileResourceCorrelationRule() != null) {
            			log.debug("Found reconcile correlation rule with description '" + resource.getReconcilePolicy().getReconcileResourceCorrelationRule().getDescription());
            			
            			//FACTORING RULE
            			go = resource.getReconcilePolicy().getReconcileResourceCorrelationRule().getScriptedObject();
            			go.setProperty("resource", resource);
            			//TODO: Replace with generic API that is exposed to all scripts
            			go.setProperty("userTools", userManager);
            			
            		} else {
            			throw new ReconcileAccountsException("Correlation rule was activated but was not found, aboring action.");
            		}
            	}
            	
                for (Account currActiveLeftAccount : activeAccounts.values()) {
                	log.trace("Found an active account named '" + currActiveLeftAccount.getName() + "' to be added, determining corresponding event...");
                    //Handle empty account names
                    if (currActiveLeftAccount.getName().length() < 1) {
                    	//warn
                    	System.out.println("While iterating over left active accounts in Reconcile Accounts, an account witout a name was found for resource '" + resource.getDisplayName() + "', skipping account.");
                        continue;
                    }
                    
                    counter++;
                    
                    User user = new User();
                    boolean matchedUserFound = false;
                    if (isActivateAccountCorrelationRule) {
                        // If needed, Execute the correlation rule, if the correlation rule return true then raise an UNASSIGNED event
                    	go.setProperty("log", logForCorelRule);
                    	go.setProperty("matchedUserName", new String());
                    	go.setProperty("activeAccount", currActiveLeftAccount);

                    	try {
                    		Object[] args = {};
                    		go.invokeMethod("run", args);
                    	}catch(Exception e) {
                    		log.error("Exception has occurd while trying to execute correlation rule for active account '" + currActiveLeftAccount.getName() + "': " + e.getMessage());
                    		continue;
                    	}
                    	
                        //boolean matchedUserFound = acr.execute();

                    	//this is just the name of the user that is returned from the correlation rule
                    	//it doesn't mean the user was found on the repository.
                        String matchedUserName = (String)go.getProperty("matchedUserName");
                        log.trace("Matched user resulted from correlation rule for active account '" + currActiveLeftAccount.getName() + "' is: '" + matchedUserName + "'");
                        
                        
                        //matchedUserFound = false;
                        //user = new User();
                        ///acr.setAccount(currActiveLeftAccount);
                        ///? matchedUserName = acr.correlate();
                        
                        //if null was returned from script or length<1 then flag user as not found
                        if ( ( matchedUserName == null) || (matchedUserName.length() < 1) ) {
                            matchedUserFound = false;
                        } else {
                           	//find the user, if not found, flag as matched user not found, otherwise flag true
                        	user = userManager.findUser(matchedUserName);
                                
                        	if (user!=null) {
                        		matchedUserFound = true;
                        	} else {
                        		log.warn("Correlation rule returned Username '" + matchedUserName + "' that matches account, but this user does not exist in repository!");
                        		matchedUserFound = false;
                        	}
                        }
                    } else if (isAutoCorrelateAccountIfMatchedToUser) {
                    	//auto correlate if user matched to account name

                    	//find user by the account name
                    	user = userManager.findUser(currActiveLeftAccount.getName());
                    	
                    	if (user == null) {
                    		UnmatchedAccountEvent event = AccountEventsFactory.factoryUnmatchedAccountEvent(resource, currActiveLeftAccount, accountsToPersist);
                            event.execute();
                    	} else {
                    		AccountEvent event = AccountEventsFactory.factoryUnsignedAccountEvent(resource,user, currActiveLeftAccount, accountsToPersist);
                    		event.execute();
                    	}
                    }
                    //No correlation rule. automatically set as UNMATCHED.
                    else {
                    	//debug
                    	log.trace("Correlation rule not activated, automatically defined event as UNMATCHED USER");
                        matchedUserFound = false;
                    }
                    
                    if (matchedUserFound) {
                        AccountEvent event = AccountEventsFactory
                                .factoryUnsignedAccountEvent(resource,
                                user, currActiveLeftAccount, accountsToPersist);
                        event.execute();
                    }
                    // Otherwise, an account still exist within the SET without a
                    // match, raise an "UNMATCHED" event
                    else {
                        UnmatchedAccountEvent event = AccountEventsFactory.factoryUnmatchedAccountEvent(resource, currActiveLeftAccount, accountsToPersist);
                        event.execute();
                    }
                    
                    //Is not allowed. cannot iterate over a collection and remove elements, what is it for anyway?
                    //activeAccounts.remove(currActiveLeftAccount.getName().toUpperCase());
                }
            } catch (Exception e) {
                throw new ReconcileAccountsException("An exception has occured while trying to reconcile accounts, failed with exception: " + e);
            }
            
            log.info("---------------ACCOUNTS RECONCILIATION SUMMARY---------------");
            //Persist the 'accountsToPersist' SET.
            log.info("Resulted *" + accountsToPersist.size() + "* accounts to persist!, persisting...");
            accountManager.persistAccountEntities(accountsToPersist.values());
            log.info("Finished persisting *" + accountsToPersist.size() + "* accounts to IDM repository!.");
            log.info("Resulted *" + accountsToRemoveFromRepository.size() + "* accounts to be removed!, removing...");
            accountManager.removeAccountEntities(accountsToRemoveFromRepository.values());
            
            log.debug("Finished removing *" + accountsToRemoveFromRepository.size() + "* accounts from IDM repository!");
            
            buildSummaries(accountsToRemoveFromRepository, accountsToPersist);
            
        } catch (ObjectsConstructionException slie) {
            //Throw an exception
            throw new ReconcileAccountsException(
                    "Failed to reconcile target due to error constructing active accounts from the 'acitve sync data' due to: '" + slie.getMessage());
        } /*catch (ScriptLoadingException sle) {
            String errMsg = "An exception has occured while trying to factory the correlationRule for resource: '" + resource.getDisplayName() + "', failure message: " + sle.getMessage();
            log.error(errMsg);
            throw new ReconcileAccountsException(errMsg);
        }*/
        catch (NamingException e) {
        	throw new ReconcileAccountsException("Could not perform resource accounts reconciliation process due to an error while loading EJBs: " + e.toString());
        }
    }
    
    
    private void buildSummaries(Map<String,Account> accountsToRemoveFromRepository, Map<String,Account> accountsToPersist) {
    	for (Map.Entry<String, Account> currAccToRemoveEntry : accountsToRemoveFromRepository.entrySet()) { 
    		SummaryAccount sa = new SummaryAccount();
    		sa.name = currAccToRemoveEntry.getKey();
    		rrs.accountsRemovedFromRepository.add(sa);
    	}
    	
    	for (Map.Entry<String, Account> currAccToPersistEntry : accountsToPersist.entrySet()) { 
    		SummaryAccount sa = new SummaryAccount();
    		sa.name = currAccToPersistEntry.getKey();
    		rrs.accountsInsertedToRepository.add(sa);
    	}
    }
}
