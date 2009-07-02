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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.account.ActiveAccountsConstructor;
import velo.actions.Action;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.IdentityAttributeManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.IdentityAttribute;
import velo.entity.IdentityAttributeSource;
import velo.entity.IdentityAttributeSourceByResourceAttribute;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.IdentityAttributeSourceByResourceAttribute.SyncByResourceAttributePolicy;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ObjectsConstructionException;
import velo.utils.Stopwatch;

/**
 * A process to reconcile Identity Attributes instances
 *
 * @author Asaf Shakarchi
 */
public class ReconcileIdentityAttributes extends Action {
    //Source types
    private final int targetAttributeSource = 1;
    private final int scriptSource = 2;
    private final int constantSource = 3;
    
    private static Logger log = Logger.getLogger(ReconcileIdentityAttributes.class.getName());
    //private AccountManagerLocal accountManager;
    //private ResourceAttributeManagerLocal targetSystemAttributeManager;
    //private UserManagerLocal userManager;
    
    /*
    public ReconcileIdentityAttributes(ResourceAttributeManagerLocal targetSystemAttributeManager, AccountManagerLocal accountManager, UserManagerLocal userManager) {
        this.accountManager = accountManager;
        this.targetSystemAttributeManager = targetSystemAttributeManager;
        this.userManager = userManager;
    }
     */
    
    
    public void execute() {
        long startExecutionTime = System.currentTimeMillis();
        
        try {
            //Load relevant data
            Context ic = new InitialContext();
            AccountManagerLocal accountManager = (AccountManagerLocal) ic.lookup("velo/AccountBean/local");
            ResourceAttributeManagerLocal targetSystemAttributeManager = (ResourceAttributeManagerLocal) ic.lookup("velo/ResourceAttributeBean/local");
            UserManagerLocal userManager = (UserManagerLocal) ic.lookup("velo/UserBean/local");
            IdentityAttributeManagerLocal identityAttributeManager = (IdentityAttributeManagerLocal) ic.lookup("velo/IdentityAttributeBean/local");
            
            Stopwatch stopwatch = new Stopwatch();
            log.debug("Loading Users and Identity Attributes to synchronize, please wait...");
            stopwatch.start();
            Collection<IdentityAttribute> identityAttributesToSync = identityAttributeManager.loadIdentityAttributesToSync();
            
            
            Collection<User> usersToSync = userManager.findUsersToSync();
            for (User currUserToSync : usersToSync) {
            	currUserToSync.getUserIdentityAttributes().size();
            }
            
            stopwatch.stop();
            log.debug("END: Successfully Loaded all users and Identity Attributes to synchronize: " + stopwatch); 
            
            
            log.info("Successfully loaded amount of '" + identityAttributesToSync.size() + "' Identity Attributes to sync, for each User in repository, Users loaded to be synced with amount '" + usersToSync.size() + "'");
            //(1) Build a MAP containing all Resources which are sources of all IAs to sync
            HashMap<String,Resource> resourceSources = new HashMap<String,Resource>();
            
            log.debug("START of constructing Resource map of all ResourceAttribute sources of all IdentityAttributes flagged to be synchronized");
            for (IdentityAttribute currIA : identityAttributesToSync) {
                for (IdentityAttributeSource currIASource : currIA.getSources()) {
                    if (currIASource instanceof IdentityAttributeSourceByResourceAttribute) {
                        IdentityAttributeSourceByResourceAttribute iasbtsa = (IdentityAttributeSourceByResourceAttribute) currIASource;
                        if (!resourceSources.containsKey(iasbtsa.getResourceAttribute().getResource().getUniqueName())) {
                            log.trace("Adding resource named '" + iasbtsa.getResourceAttribute().getResource().getUniqueName() + "' to the resourceSources MAP'");
                            resourceSources.put(iasbtsa.getResourceAttribute().getResource().getUniqueName(), iasbtsa.getResourceAttribute().getResource());
                        } else {
                            log.trace("Cannot add resource named '" + iasbtsa.getResourceAttribute().getResource().getUniqueName() + "' to the resourceSources MAP since it already exist!'");
                        }
                    }
                }
            }
            log.debug("END of Resource map construction with Resources amount '" + resourceSources.size() + "'");
            
            
            log.debug("START: Make sure there are ActiveData XML files for each relevant Resource...");
            //(2) Make sure that the ActiveData of each Resource in map exists, otherwise request by tasks to fetch them all.
            if (resourceSources.size() > 0) {
                log.trace("Found '" + resourceSources.size() + "' target systems that are sources of IdentityAttributes to be synced, making sure that there are valid ActiveData XMLs for all found target systems!");
                
                for (Resource currTs : resourceSources.values()) {
                    //TODO Support if XML is valid by date, currently fetch all XMLs anyway
                    //Task taskToRetrieveActiveDataFile =
                }
            } else {
                log.debug("No target systems found as sources of any IdentityAttributes to be synced...");
            }
            log.debug("END of preparing ActiveData XML files for each relevant Resource");
            

            stopwatch.start();
            log.debug("START: Consturcting ActiveAccounts from ActiveData(XML Files) for all relevant Targets, this will take a while... ");
            HashMap<String,Map<String,Account>> accountsInTargets = new HashMap<String,Map<String,Account>>();
            //(3) Create MAPS of all employees from all Resources Sources MAP
            ActiveAccountsConstructor aac = new ActiveAccountsConstructor();
            for (Resource currTs : resourceSources.values()) {
                try {
                    //Force accountID as uppercase as case-sensitive Users in Repository are not supported.
                    //(Will be easy to compare the ActiveAccounts to the Users in rep)
                    accountsInTargets.put(currTs.getUniqueName(), aac.constructByXml(currTs, true));
                } catch (ObjectsConstructionException ex) {
                    String errMsg = "Failed to perofrm Reconcile Identity Attributes due to: '" + ex.getMessage() + "')";
                    log.error(errMsg);
                    //throw new ReconcileProcessException(errMsg);
//                    getMsgs().add(errMsg);
//                    return false;
                }
            }
            
            
            stopwatch.stop();
            log.debug("END: Successfully constructed ActiveAccounts per relevant Resource, process execution time: " + stopwatch);
            
            if (log.isTraceEnabled()) {
            	for (Map.Entry<String,Map<String,Account>> entry : accountsInTargets.entrySet()) {
            		log.trace("---START OF DUMP OF ACTIVE ACCOUNTS OF RESOURCE '" + entry.getKey() + "'---");
            		for (Account currAccount : entry.getValue().values()) {
            			log.trace("\t--Start of dump of active account named '" + currAccount.getName() + "'");
            			for (Map.Entry<String, Attribute> currAttr : currAccount.getActiveAttributes().entrySet()) {
            				log.trace("\t\tAttribute unique name '" + currAttr.getValue().getUniqueName() + "(" + currAttr.getKey() + ") " + "', first value: '" + currAttr.getValue().getFirstValue().getAsString() + "'");
            			}
            			log.trace("\t--End of dump of active account named '" + currAccount.getName() + "'");
            		}
            		
            		log.trace("---END OF DUMP OF ACTIVE ACCOUNTS OF RESOURCE '" + entry.getKey() + "'---");
            	}
            }
            
            log.debug("START: Constructing a MAP per relevant Resource which contains 'Resource|Resource Account Id Attribute");
            stopwatch.start();
            //(4) Create a map containing all RA that represents an 'Account ID' for the resource sources
            HashMap<String,ResourceAttribute> tsaAccountIds = new HashMap<String,ResourceAttribute>();
            for (Resource currTs : resourceSources.values()) {
            	ResourceAttribute currAccountIdAttribute = currTs.getAccountIdAttribute();
                if (currAccountIdAttribute != null) {
                	tsaAccountIds.put(currTs.getUniqueName(), currTs.getAccountIdAttribute());
                } else {
                	String errMsg = "Cannot perform Reconcile Identity Attributes since a Resource Attribute flagged as account ID does not exist for Resource named '" + currTs.getDisplayName() + "'";
                	log.error(errMsg);
                	//throw new ReconcileProcessException(errMsg);
//                	getMsgs().add(errMsg);
//                	return false;
                }
            }
            stopwatch.stop();
            log.debug("END: Constructed a MAP per relevant Resource with its 'AccountID ResourceAttribute', successfully found all Resource Attributes that flagged as 'AccountID' for all relevant targets!, process execution time: " + stopwatch);
            
            
            
            
            
            
            //(5) Algorithm:
            //    - Find a valid source:
            //      - If not found, skip that IA sync and log an error message, and continue to the next IA
            //      - If the source type is 'TSA', then get the 'TSA Account ID' for the iterated TSA->TS before looping through users
            //        - If not found -> SANITY CHECK -> throw an exception as this should never happen (step 4 should take care of validating that this map constructed successfully!)
            //        - If found, iterate over all Users in repository, per user do:
            //          - Load the value of the 'TSA's AccountID' for the current iterated User
            //          - If the current iterated user is not in the 'Black User List Of Current Iterated Target' then seek and try to load the corresponding employee row in the 'Active Accounts' map imported from the 'Active Data'
            //            - If not found -> add the user to the 'Black User List Of Current Iterated Target' and skip the user.
            //            - If found -> Fetch the corresponding User's attribute by the Source's TSA->retrieveName
            //              - If not found, skip the compare of this attribute and log the error
            //              - If found, compare the values:
            //                - If not equal, update the User IdentityAttribute
            //                - If equal, then continue to the next User iteration
            //
            //Prepare a UserBlackList per target MAP
            HashMap<String,ArrayList<String>> userBlackListPerTarget = new HashMap<String,ArrayList<String>>();
            for (Resource relevantTarget : resourceSources.values()) {
            	userBlackListPerTarget.put(relevantTarget.getUniqueName(), new ArrayList<String>());
            }
            
            stopwatch.start();
            ArrayList<UserIdentityAttribute> affectedUserIdentityAttributes = new ArrayList<UserIdentityAttribute>();
            ArrayList<UserIdentityAttribute> newUserIdentityAttributesToPersist = new ArrayList<UserIdentityAttribute>();
            int amountOfIaToSync = identityAttributesToSync.size();
            int iaSyncCounter = 0;
            log.debug("START of IdentityAttribute synchronizations, will synchronize '-" + identityAttributesToSync.size() + "- Identity Attributes for each User in repository!");

            //iterate through the IAs to sync
            for (IdentityAttribute currIAToSync : identityAttributesToSync) {
                iaSyncCounter++;
                
                log.debug("Currently syncing IdentityAttribute named '" + currIAToSync.getDisplayName() + "', synced '" + iaSyncCounter + " out of '" + identityAttributesToSync.size() + "' IAs...");
                boolean foundValidSource = false;
                int sourceType = 0;
                IdentityAttributeSource foundValidIdentityAttributeSource = null;
                for (IdentityAttributeSource currSource : currIAToSync.getSources()) {
                    //Expecting sources to be organized by priority
                    //TODO Support 'constant/scripted' sources
                    if (currSource instanceof IdentityAttributeSourceByResourceAttribute) {
                        //IdentityAttributeSourceByResourceAttribute iasbtsa = (IdentityAttributeSourceByResourceAttribute) currSource;
                        
                        foundValidSource = true;
                        sourceType = this.targetAttributeSource;
                        foundValidIdentityAttributeSource = currSource;
                        break; //End the loop to seek for a valid Source!
                    } else {
                        log.warn("Source of class '" + currSource.getClass().getName() + "is currently not supported!");
                    }
                }
                //If Not found a valid source, continue to the next IA
                if (!foundValidSource) {
                    log.warn("Could not synchronize Identity Attribute with name '" + currIAToSync.getDisplayName() + "' since it has no valid sources, skipping...");
                    continue;
                }
                //Found a valid source, continue to the next step
                else {
                    //Handle RA Source
                    if (sourceType == this.targetAttributeSource) {

                        ResourceAttribute currRAAccountId = null;
//                        AccountAttributeConverterInterface atci = null;
                        //By policy, decide which key type was specified
                        IdentityAttributeSourceByResourceAttribute iasbtsa = (IdentityAttributeSourceByResourceAttribute) foundValidIdentityAttributeSource;
                        if (iasbtsa.getSyncByResourceAttributePolicy() == SyncByResourceAttributePolicy.SYNC_BY_TARGET_SYSTEM_ACCOUNT_ID_ATTRIBUTE) {
                        	currRAAccountId = tsaAccountIds.get(iasbtsa.getResourceAttribute().getResource().getUniqueName());
                            
                        	/*
                            try {
                                if ( (!currRAAccountId.isMapDirectly()) ) {
                                    atci = accountManager.factoryResourceAttributeConverter(currRAAccountId);
                                }
                            } catch (ObjectFactoryException ex) {
                                String errMsg = "Could not generate converter for ResourceAttribute named '" + currTSAAccountId.getDisplayName() + "', due to: '" + ex.getMessage() + "'";
                                log.warn(errMsg);
                                getMsgs().add(errMsg);
                                return false;
                            }
                            */
                        } else if (iasbtsa.getSyncByResourceAttributePolicy() == SyncByResourceAttributePolicy.SYNC_BY_ACCOUNT_ASSOCIATED_TO_USER) {
                            //none to do here
                        }
                        
                        
                        //Get the TSA AccountID
                        
                        //This is just a sanity check, it should never return false, since there are already validations when constructing the 'tsaAccountIds' MAP!
                        //if (tsaAccountIds.containsKey(iasbtsa.getResourceAttribute().getResource().getShortName())) {
                        
                        
                        for (User currUser : usersToSync) {
                            //try {
                                String employeeKeyName = null;
                                
                                if (iasbtsa.getSyncByResourceAttributePolicy() == SyncByResourceAttributePolicy.SYNC_BY_TARGET_SYSTEM_ACCOUNT_ID_ATTRIBUTE) {
//                                    velo.storage.Attribute employeeKeyValueForCurrentUser = accountManager.loadVirtualAccountAttribute(currUser, currTSAAccountId, atci);
//                                    employeeKeyName = employeeKeyValueForCurrentUser.getFirstValue().getValueAsString();
                                }
                                else {
                                     Account userAccountInRelevantResource = currUser.getAccountOnTarget(iasbtsa.getResourceAttribute().getResource().getUniqueName());
                                     if (userAccountInRelevantResource == null) {
                                        log.debug("Cannot synchronize IdentityAttribute named '" + currIAToSync.getDisplayName() + "', for User named '" + currUser.getName() + "', since account on resource named '" + iasbtsa.getResourceAttribute().getResource().getDisplayName() + "' could not be found!, skipping user...");
                                        continue;
                                     }
                                     
                                     //get the employee account name in the right case
                                     employeeKeyName = userAccountInRelevantResource.getNameInRightCase();
                                }
                                
                                
                                //Make sure that the user is not in the black list per target
                                if (!userBlackListPerTarget.get(iasbtsa.getResourceAttribute().getResource().getUniqueName()).contains(currUser.getName())) {
                                    //Retrieve the ACTIVE attribute from the importer Active Data that correspond to the 'employee Key!'
                                    log.trace("Employee KEY generated is '" + employeeKeyName + "', seeking for a corresponding entry in the 'Active Accounts' map for RA source");
                                    //Seeking in the right case the account from the active accounts map
                                    if (accountsInTargets.get(iasbtsa.getResourceAttribute().getResource().getUniqueName()).containsKey(employeeKeyName)) {
                                        
                                        //If ActiveAccount does not exists, then continue to the next user
                                        if (!accountsInTargets.get(iasbtsa.getResourceAttribute().getResource().getUniqueName()).containsKey(employeeKeyName)) {
                                            log.info("Could not find corresponding Active Account with name '" + employeeKeyName + "', continuing to the next user");
                                            continue;
                                        }
                                        
                                        Account foundActiveAccount = accountsInTargets.get(iasbtsa.getResourceAttribute().getResource().getUniqueName()).get(employeeKeyName);
                                        
                                        
                                        //Fetch the ACTIVE attribute / current User's UIA
                                        //try {
                                            log.trace("Active account was found with name '" + foundActiveAccount.getNameInRightCase() + "', seeking for the corresponding Resource Attribute to sync with name '" + iasbtsa.getResourceAttribute().getUniqueName() + "'");
                                            
                                            
                                            //get the unique name of the RA that indicates the account ID in the right case
                                            //(needed in order to fetch the relevant attribute that need to be compared to the current synced IA)
                                            String raUniqueName = iasbtsa.getResourceAttribute().getUniqueNameInRightCase();
                                            
                                            
                                            Attribute currActiveAttribute = foundActiveAccount.getActiveAttributes().get(raUniqueName);
                                            UserIdentityAttribute correspondingUserIdentityAttribute = currUser.getUserIdentityAttribute(currIAToSync.getUniqueName());
                                            
                                            
                                            for (Map.Entry<String, Attribute> currAttr : foundActiveAccount.getActiveAttributes().entrySet()) {
//                                            	System.out.println("!!!!!!!!!!!!!!!!!!!!!!: '" + currAttr.getKey() +"', " + currAttr.getValue());
//                                            	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!" + foundActiveAccount.getTransientAttributes().get("first_name"));
                                            }
                                            
                                            if (currActiveAttribute == null) {
                                            	log.info("Could not find active attribute ('" + iasbtsa.getResourceAttribute().getUniqueName() + "') for current iterated user '" + currUser.getName() + "', skipping user '" + currUser.getName() + "' while syncing IA '" + currIAToSync.getDisplayName() + "', skipping current user");
                                            	continue;
                                            }
                                                
                                            //there's no IA available for the user, then comparation is not needed, lets create one
                                            if (correspondingUserIdentityAttribute == null) {
                                            	//try {
                                            	correspondingUserIdentityAttribute = currIAToSync.factoryUserIdentityAttribute(currUser);
                                            	
                                            	try {
                                            		correspondingUserIdentityAttribute.importValues(currActiveAttribute.getValues());
                                            		newUserIdentityAttributesToPersist.add(correspondingUserIdentityAttribute);
                                            	} catch (AttributeSetValueException e) {
                                            		//TODO: nicer message!
                                            		log.warn("Could not perform value modification due to: '" + e.getMessage() + "'");
                                            	}
                                            //user identity attribute was found, compare the values, if values are not equal then modify the UIA
                                           	//with the new value from the corresponding active account attribute
                                            } else {
                                            	log.trace("Found the Active Attribute object: '" + currActiveAttribute + "' with value size: " + currActiveAttribute.getTransientValues().size() + "'");
                                            	log.trace("Successfully fetch both UserIdentityAttribute & Attribute from corresponding 'Active Account' entry, comparing values...");                                            	
                                            	
                                            	
                                            	if (correspondingUserIdentityAttribute.compareTransientValues(currActiveAttribute)) {
                                                    log.trace("Values are equal!, continuing to the next User...");
                                                    
                                                    //Clean references!
                                                    //attrFromUserIdentityAttribute = null;
                                                } else {
                                                    //Clean references!
                                                    //attrFromUserIdentityAttribute = null;
                                                    log.trace("Values are NOT equal!, updating User's Identity Attribute value with the Active value!");
                                                    
                                                    try {
                                                        //currUserIdentityAttribute.setValuesByAV(currActiveAttribute.getValues());
                                                    	correspondingUserIdentityAttribute.setValuesByAV(currActiveAttribute.getValues());
                                                        affectedUserIdentityAttributes.add(correspondingUserIdentityAttribute);
                                                    } catch (AttributeSetValueException ex) {
                                                        //TODO Dump a better warning message it is not certaintly not clear enough!
                                                        log.warn("Could not perform value modification, skipping to the next User in the list... due to: " + ex.getMessage());
                                                        continue;
                                                    }
                                                }
                                            	
                                            }
                                                
                                        	//}
                                        //If active ATTR not found
//                                        catch (AttributeNotFound anf) {
//                                            log.warn("Could not find Attribute for Active Account named '" + foundActiveAccount.getName() + "', failed with detailed message: '" + anf.getMessage() + "'");
//                                            continue;
//                                        }
                                    } else {
                                        log.warn("Could not find a corresponding entry in 'Active Accounts' MAP for TSA Source for generated employee KEY '" + employeeKeyName + "', skipping synchronization of IdentityAttribute named '" + currIAToSync.getDisplayName() + "' for User named '" + currUser.getName() + "'!");
                                        continue;
                                    }
                                } else {
                                    log.debug("Skipping synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' for User named '" + currUser.getName() + "', since user is in the black list(had error before) on resource named '" + iasbtsa.getResourceAttribute().getResource().getDisplayName() + "'");
                                }
                                
                                
                                //These exceptions are by the 'loadVirtualAccountAttribute' to load the AccountID value for the current iterated user,
                                //If there was an exception, the whole user should be skipped.
                            /*} catch (NoResultFoundException ex) {
                                log.warn("Could not fetch the KEY to seek the ActiveUser for User '" + currUser.getName() + "' while synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' due to: " + ex.getMessage());
                                continue; //Continue to the next user.
                            } catch (NoUserIdentityAttributeValueException ex) {
                                log.warn("Could not fetch the KEY to seek the ActiveUser for User '" + currUser.getName() + "' while synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' due to: " + ex.getMessage());
                                continue; //Continue to the next user.
                            } catch (NoUserIdentityAttributeFoundException ex) {
                                log.warn("Could not fetch the KEY to seek the ActiveUser for User '" + currUser.getName() + "' while synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' due to: " + ex.getMessage());
                                continue; //Continue to the next user.
                            } catch (ConverterProcessFailure ex) {
                                log.warn("Could not fetch the KEY to seek the ActiveUser for User '" + currUser.getName() + "' while synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' due to: " + ex.getMessage());
                                continue; //Continue to the next user.
                            } catch (LoadingVirtualAccountAttributeException ex) {
                                log.warn("Could not fetch the KEY to seek the ActiveUser for User '" + currUser.getName() + "' while synchronizing IdentityAttribute named '" + currIAToSync.getDisplayName() + "' due to: " + ex.getMessage());
                                continue; //Continue to the next user.
                            }*/
                            
                        }
                        /*
                        } else {
                            String errMsg = "Could not find a ResourceAttribute flagged as 'ACCOUNT ID' for Target named '" + iasbtsa.getResourceAttribute().getResource().getDisplayName() + "', this was just a sanity check, this exception should never happen as there are validations when constructing the 'tsaAccountIds' MAP that should take care of this exception!!!";
                            //throw new ReconcileProcessException(errMsg);
                            getMsgs().add(errMsg);
                         
                            //Clean references
                         
                            return false;
                        }
                         **/
                    }
                }
            }
            
            stopwatch.stop();
            log.debug("Finished Synchronization phase for all IdentityAttributes / Users to be synced, phase execution time: '" + stopwatch + "'");
            
            
            //PRINT SUMMARY TO LOG
            log.info("---RECONCILE IDENTITY ATTRIBUTES SUMMARY---");
            log.info("---Amount of User Identity Attributes to MODIFY: *" + affectedUserIdentityAttributes.size() + "* ---");
            log.info("---Amount of User Identity Attributes to CREATE: *" + newUserIdentityAttributesToPersist.size() + "* ---");
            
            if (log.isTraceEnabled()) {
                log.trace("START: Dumping all User-Identity-Attributes that should be affected before modfiying data in repository");
                for (UserIdentityAttribute currUserIdentityAttribute : affectedUserIdentityAttributes) {
                    try {
                        log.trace("Modifying User Identity Attribute of User named \'" + currUserIdentityAttribute.getUser().getName() + "\', with new (dumping only first value) value: \'" + currUserIdentityAttribute.getFirstValue().getAsString());
                    } catch (NoResultFoundException ex) {
                        log.warn("Could not dump the value of UserIdentityAttribute ID " + currUserIdentityAttribute.getUserIdentityAttributeId() + "', of User '" + currUserIdentityAttribute.getUser().getName() + "' since no values were found!");
                    }
                }
                log.trace("END: Finished dumping User-Identity-Attributes that should be affected in repository.");
            }
            
            
            if (log.isTraceEnabled()) {
                log.trace("START: Dumping all User-Identity-Attributes that should be ADDED before persisting data in repository");
                for (UserIdentityAttribute currUserIdentityAttributeToPersist : newUserIdentityAttributesToPersist) {
                    try {
                        log.trace("Modifying User Identity Attribute of User named \'" + currUserIdentityAttributeToPersist.getUser().getName() + "\', with new (dumping only first value) value: \'" + currUserIdentityAttributeToPersist.getFirstValue().getAsString());
                    } catch (NoResultFoundException ex) {
                        log.warn("Could not dump the value of UserIdentityAttribute ID " + currUserIdentityAttributeToPersist.getUserIdentityAttributeId() + "' to persist, of User '" + currUserIdentityAttributeToPersist.getUser().getName() + "' since no values were found!");
                    }
                }
                log.trace("END: Finished dumping User-Identity-Attributes that should be PERSISTED in repository.");
            }
            
            
            //Perform the updates
//fuck1.4            userManager.updateUserIdentityAttributes(affectedUserIdentityAttributes);
//fucl1.4            userManager.persistUserIdentityAttributes(newUserIdentityAttributesToPersist);
            
//            return true;
            
        } catch (NamingException ne) {
//            getMsgs().add(ne.getMessage());
//            return false;
        }
    }
}