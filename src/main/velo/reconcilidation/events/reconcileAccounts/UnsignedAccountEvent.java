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
import velo.reconcilidation.events.AccountEvent;

/**
 An event that occures when an Unasigned account was found while Reconcile Account process executed
 
 (An 'Unasigned' account event occures when there is a User owner found for the account but not yet assigned to it)
 
 @author Asaf Shakarchi
 */
public class UnsignedAccountEvent extends AccountEvent {
    private static Logger logger = Logger.getLogger(UnsignedAccountEvent.class.getName());
    
    private User user;
    private Account activeAccount;
    
    
    public boolean execute() {
        logger.finest("-UNASIGNED- account event has occured for account name: '" + getActiveAccount().getName() + "', in target system name: '" + getResource().getDisplayName() + "'");
        logger.finest("Action to take is: " + getReconcilePolicy().getUnasignedAccountEventAction());
        
        String currActiveAccountName = getActiveAccount().getName();
        if (!getResource().isCaseSensitive()) {
            currActiveAccountName.toUpperCase();
        }
        
                /*
                switch (getReconcilePolicy().getUnasignedAccountEventAction()) {
                case NOTHING:
                        break;
                }
                 */
        if (getReconcilePolicy().getUnasignedAccountEventAction().equals("CREATE_ACCOUNT_IN_REPOSITORY_AND_ASSIGN_ACCOUNT_TO_MATCHED_USER")) {
            try {
                
                //Need to create a new user / account, prepare the object and add it to the list!
                if (!getAccountsToPersist().containsKey(currActiveAccountName)) {
                    getAccountsToPersist().put(currActiveAccountName,getAccountToPersistWithUserLink(currActiveAccountName,getUser()));
                } else {
                    logger.warning("Skipping account creation based on account named: '" + getActiveAccount().getName() + "', of Target named: '" + getResource().getDisplayName() + "' since this account already flagged to be added to repository!");
                }
                
            } catch (NoObjectPersistentRequiredException nopre) {
                return true;
            }
        } else if (getReconcilePolicy().getUnasignedAccountEventAction().equals("CREATE_ACCOUNT_IN_REPOSITORY_WITHOUT_ASSIGN_ACCOUNT_TO_MATCHED_USER")) {
            try {
                if (!getAccountsToPersist().containsKey(currActiveAccountName)) {
                    getAccountsToPersist().put(currActiveAccountName,getAccountToPersistWithoutUserLink(currActiveAccountName));
                } else {
                    logger.warning("Skipping account creation based on account named: '" + getActiveAccount().getName() + "', of Target named: '" + getResource().getDisplayName() + "' since this account already flagged to be added to repository!");
                }
                
                
            } catch (NoObjectPersistentRequiredException nopre) {
                return true;
            }
        } else {
            logger.warning("Warning, could not create account name: '" + getActiveAccount().getName() + "', on target: '" + getResource().getDisplayName() + "', attached to User: '" + getUser().getName() + "', ReconcilePolicy does not have a proper type of 'UnAssigned Account Event action, current value of this property is: " + getReconcilePolicy().getUnasignedAccountEventAction());
        }
        
        return true;
    }
    /**
     @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    /**
     @return the user
     */
    public User getUser() {
        return user;
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
