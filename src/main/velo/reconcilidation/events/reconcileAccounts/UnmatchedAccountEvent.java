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
package velo.reconcilidation.events.reconcileAccounts;

import java.util.logging.Logger;

import velo.entity.Account;
import velo.entity.User;
import velo.exceptions.NoObjectPersistentRequiredException;
import velo.exceptions.NoResultFoundException;
import velo.reconcilidation.events.AccountEvent;

/**
 An event that occures when an Unmatched account was found while Reconcile Account process executed
 
 (An 'Unmatched' account event occures when there is no User owner found for the account)
 
 @author Asaf Shakarchi
 */
public class UnmatchedAccountEvent extends AccountEvent {
    private static Logger logger = Logger.getLogger(UnmatchedAccountEvent.class.getName());
    
    private Account activeAccount;
    
    public boolean execute() {
        logger.finest("-UNMATCHED- account event has occured for account name: '" + getActiveAccount().getName() + "', in target system name: '" + getResource().getDisplayName() + "'");
        logger.finest("Action to take is: " + getReconcilePolicy().getUnmatchedAccountEventAction());
        
                /*
                switch (getReconcilePolicy().getUnmatchedAccountEventAction()) {
                        case NOTHING:
                                break;
                        case CREATE_NEW_USER_BASED_ON_RESOURCE_ACCOUNT:
                                System.out.println("Creating USER for account name: " + getAccountName());
                                createUserAndLinkAccount();
                }
                 */
        String activeAccountName = getActiveAccount().getName();
        
        if (!getResource().isCaseSensitive()) {
            activeAccountName.toUpperCase();
        }
        
        
        if (getReconcilePolicy().getUnmatchedAccountEventAction().equals("NOTHING")) {
            //logger.fine("'NOTHING' was choosed, nothing left to do for account Unmatched event for account name: " + getAccount().getName());
        } else if (getReconcilePolicy().getUnmatchedAccountEventAction().equals("CREATE_NEW_USER_BASED_ON_RESOURCE_ACCOUNT")) {
            //logger.info("Creating USER for account name: " + getAccountName());
            try {
                //TODO: Make sure that the user name that should be created does not already exist!
                
            	User user = null;
            	//Make sure that the user does not already exist before creating one
            	if (getUserManager().isUserExit(getActiveAccount().getName())) {
            		try {
						user = getUserManager().findUserByName(getActiveAccount().getName());
						//Persist the user here, although it is a performance lack, since later on when the accounts get persisted as bulk
	            		//The User object will be detached already
	            		//getUserManager().persistUserEntity(user);
            			//TODO: detached entity to persist exception occure!!! FIX!!!
            			//Currently sets the user of the account as null...
						user = null;
					} catch (NoResultFoundException e) {
						e.printStackTrace();
						return false;
					}
            	}
            	else {
            		//Need to create a new user / account, prepare the object and add it to the list!
            		user = factoryUser(getActiveAccount().getName());
            		user.setCreatedByReconcile(true);
            		user.setSourceResource(getResource());
            	}
                
                //TODO: Saved currently as uppercase, support case-sensitive targets in the future
                if (!getAccountsToPersist().containsKey(activeAccountName)) {
                    //TODO: Saved currently as uppercase, support case-sensitive targets in the future
                    getAccountsToPersist().put(activeAccountName,getAccountToPersistWithUserLink(activeAccountName,user));
                } else {
                    logger.warning("Skipping account creation based on account named: '" + getActiveAccount().getName() + "', of Target named: '" + getResource().getDisplayName() + "' since this account already flagged to be added to repository!");
                }
                
            } catch (NoObjectPersistentRequiredException nopre) {
                return true;
            }
        } else if (getReconcilePolicy().getUnmatchedAccountEventAction().equals("CREATE_ACCOUNT_IN_REPOSITORY")) {
            try {
                //Need to create a new account, prepare the object and add it to the list!
                if (!getAccountsToPersist().containsKey(activeAccountName)) {
                    //TODO: Saved currently as uppercase, support case-sensitive targets in the future
                    getAccountsToPersist().put(activeAccountName,getAccountToPersistWithoutUserLink(activeAccountName));
                } else {
                    logger.warning("Skipping account creation based on account named: '" + getActiveAccount().getName() + "', of Target named: '" + getResource().getDisplayName() + "' since this account already flagged to be added to repository!");
                }
            } catch (NoObjectPersistentRequiredException nopre) {
                return true;
            }
        }
        
        return true;
    }
    
    /**
     @param activeAccount the activeAccount to set
     */
    public void setActiveAccount(Account activeAccount) {
        this.activeAccount = activeAccount;
    }
    
    /**
     @return the activeAccount
     */
    public Account getActiveAccount() {
        return activeAccount;
    }
}
