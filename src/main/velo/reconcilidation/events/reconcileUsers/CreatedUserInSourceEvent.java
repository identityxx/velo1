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
package velo.reconcilidation.events.reconcileUsers;

import java.util.logging.Logger;

import velo.entity.Account;
import velo.reconcilidation.ReconcileUsers;


/**
 * An event that occures when a Deleted account was found while Reconcile Account process executed
 *
 * (A 'Deleted' account event occures when the account exist in the IDM database but does not exist on the resource anymore...)
 *
 * @author Asaf Shakarchi
 */
public class CreatedUserInSourceEvent extends UserEvent {
    private static Logger logger = Logger.getLogger(ReconcileUsers.class.getName());
    
    private Account activeCreatedAccount;
    
    public boolean execute() {
        logger.fine("-CREATED ACCOUNT' in source target event has occured for active account name: '" + getActiveCreatedAccount().getName() + "', on target system name: '" + getReconcileUsersPolicy().getSourceResource().getDisplayName() + "'" + ", policy action choosed is: " + getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase());
        
        if (getReconcileUsersPolicy().getNewUserInSourceEventAction().toUpperCase().equals("NOTHING")) {
            ///logger.info("Nothing was choosed to do...");
        } else if (getReconcileUsersPolicy().getNewUserInSourceEventAction().toUpperCase().equals("CREATE_CORRESPONDING_USER_IN_REPOSITORY")) {
            logger.fine("Choosed to create a corresponding user with name: '" + getActiveCreatedAccount().getName() + "' in repository!");
            //Add a user to create (only if it does not exists already in map
            if (!getUsersToPersist().containsKey(getActiveCreatedAccount().getName().toUpperCase())) {
                getUsersToPersist().put(getActiveCreatedAccount().getName().toUpperCase(),factoryUser(getActiveCreatedAccount().getName()));
            } else {
                logger.warning("Skipping user creation based on account named: '" + getActiveCreatedAccount().getName() + "', of source Target named: '" + getReconcileUsersPolicy().getSourceResource().getDisplayName() + "' since this account name already added to the users that should be persisted!");
            }
        } else {
            logger.warning("Skipping event, Unknown option for -CREATED ACCOUNT- in source target has occured for target: " + getReconcileUsersPolicy().getSourceResource().getDisplayName() + ", for Active User named: '" + getActiveCreatedAccount().getName() + "', selected UNKNOWN policy option is: '" + getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase() + "'");
            return false;
        }
        
        return true;
    }
    
    /**
     * @param activeCreatedAccount the activeCreatedAccount to set
     */
    public void setActiveCreatedAccount(Account activeCreatedAccount) {
        this.activeCreatedAccount = activeCreatedAccount;
    }
    
    /**
     * @return the activeCreatedAccount
     */
    public Account getActiveCreatedAccount() {
        return activeCreatedAccount;
    }
}
