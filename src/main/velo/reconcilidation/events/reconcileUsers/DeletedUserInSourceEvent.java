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

import velo.entity.User;
import velo.reconcilidation.ReconcileUsers;


/**
 An event that occures when a Deleted account was found while Reconcile Account process executed
 
 (A 'Deleted' account event occures when the account exist in the IDM database but does not exist on the resource anymore...)
 
 @author Asaf Shakarchi
 */
public class DeletedUserInSourceEvent extends UserEvent {
    private static Logger logger = Logger.getLogger(ReconcileUsers.class.getName());
    
    /**
     The user in IDM repository which the corresponding account was deleted
     */
    private User user;
    
    public boolean execute() {
        logger.fine("-DELETED ACCOUNT' in source target event has occured for corresponding user name: '" + getUser().getName() + "', in target system name: '" + getReconcileUsersPolicy().getSourceResource().getDisplayName() + "'" + ", policy action choosed is: " + getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase());
        
        if (getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase().equals("NOTHING")) {
            ///logger.info("Nothing was choosed to do...");
        } else if (getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase().equals("REMOVE_CORRESPONDING_USER_FROM_REPOSITORY")) {
            logger.fine("Choosed to remove the corresponding user with name: '" + getUser().getName() + "' from repository!");
            //Remove the user!
            if (!getUsersToRemove().containsKey(getUser().getName().toUpperCase())) {
                getUsersToRemove().put(getUser().getName().toUpperCase(),getUser());
            } else {
                logger.warning("Skipping user removal named: '" + getUser().getName() + "', based on target named: " + getReconcileUsersPolicy().getSourceResource().getDisplayName() + "' since this account name already flagged to be removed from user repository!");
            }
            
        } else {
            logger.warning("Skipping event, Unknown option for -DELETE ACCOUNT- in source target has occured for target: " + getReconcileUsersPolicy().getSourceResource().getDisplayName() + ", for corresponding User in repository named: '" + getUser().getName() + "', selected UNKNOWN policy option is: '" + getReconcileUsersPolicy().getDeletedUserInSourceEventAction().toUpperCase() + "'");
            return false;
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
    
    
}
