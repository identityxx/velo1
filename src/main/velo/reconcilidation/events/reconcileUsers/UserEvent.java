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

import java.util.Map;

import org.jboss.seam.annotations.Transactional;

import velo.entity.ReconcileUsersPolicy;
import velo.entity.User;
import velo.reconcilidation.events.Event;

/**
 * An abstract class that represents a User Event class for all Recomcile Users Events
 *
 *  @author Asaf Shakarchi
 */
@Deprecated
public abstract class UserEvent extends Event {
    
    /**
     * A list of users to persist in bulk when the reconcile users processes ends
     * This list is passed by referenced between all events.
     */
    private Map<String,User> usersToPersist;
    
    /**
     * A list of users to remove in bulk when the reconcile users processes ends
     * This list is passed by referenced between all events.
     */
    private Map<String,User> usersToRemove;
    
    private ReconcileUsersPolicy reconcileUsersPolicy;
    
    /**
     * Constructor
     *
     * Initialie the relevant EJBs required for the events
     */
    public UserEvent() {
    }
    
    
    //FUCKED UP, USER RECONCILIATION SHOULD NOT WORK ANYWAY RIGHT?
    public User factoryUser(String userName) {
    	//TODO:Very bad, should go through factory of userManager.factoryUser!
        //User user = User.factoryUser(userName);
    	User user = null;
        user.setCreatedByReconcile(true);
        
        return user;
    }
    
    
    /**
     * @param usersToPersist the usersToPersist to set
     */
    public void setUsersToPersist(Map<String,User> usersToPersist) {
        this.usersToPersist = usersToPersist;
    }
    
    /**
     * @return the usersToPersist
     */
    public Map<String,User> getUsersToPersist() {
        return usersToPersist;
    }
    
    /**
     * @param usersToRemove the usersToRemove to set
     */
    public void setUsersToRemove(Map<String,User> usersToRemove) {
        this.usersToRemove = usersToRemove;
    }
    
    /**
     * @return the usersToRemove
     */
    public Map<String,User> getUsersToRemove() {
        return usersToRemove;
    }
    
    /**
     * @param reconcileUsersPolicy the reconcileUsersPolicy to set
     */
    public void setReconcileUsersPolicy(ReconcileUsersPolicy reconcileUsersPolicy) {
        this.reconcileUsersPolicy = reconcileUsersPolicy;
    }
    
    /**
     * @return the reconcileUsersPolicy
     */
    public ReconcileUsersPolicy getReconcileUsersPolicy() {
        return reconcileUsersPolicy;
    }
}
