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
package velo.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import velo.entity.RequestAccount.RequestAccountOperation;
import velo.entity.RequestedPosition.RequestedPositionActionType;

@Entity
@DiscriminatorValue("MODIFY_USER_ROLES_REQUEST")
public class ModifyUserRolesRequest extends Request {
    private final String REQUEST_TYPE = "MODIFY_USER_ROLES";
    private static final long serialVersionUID = 1L;
    
    private String userName;
    
    //private Set<RequestRoleModifyUserRolesRevoke> rolesToRevoke = new HashSet<RequestRoleModifyUserRolesRevoke>();
    //private Set<RequestRoleModifyUserRolesAssign> rolesToAssign = new HashSet<RequestRoleModifyUserRolesAssign>();
    private Set<RequestRole> rolesToRevoke = new HashSet<RequestRole>();
    private Set<RequestRole> rolesToAssign = new HashSet<RequestRole>();

    private Set<RequestedPosition> requestedPositions = new HashSet<RequestedPosition>();
    
    /**
     * @param userName the userName to perform the user roles modification
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * @return the userName The user name to perform the user roles modificaiton
     */
    @Column(name="USER_NAME")
    public String getUserName() {
        return userName;
    }
    
    //TODO: THIS IS A PROBLEM, MUST BE EAGER, FOR ALL OF THE ROWS, OTHERWISE DATA WONT BE AVAILABLE FOR THE FEEDER
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    //public Set<RequestRoleModifyUserRolesRevoke> getRolesToRevoke() {
    public Set<RequestRole> getRolesToRevoke() {
        return rolesToRevoke;
    }
    
    //public void setRolesToRevoke(Set<RequestRoleModifyUserRolesRevoke> rolesToRevoke) {
    public void setRolesToRevoke(Set<RequestRole> rolesToRevoke) {
        this.rolesToRevoke = rolesToRevoke;
    }
    
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    //public Set<RequestRoleModifyUserRolesAssign> getRolesToAssign() {
    public Set<RequestRole> getRolesToAssign() {
        return rolesToAssign;
    }
    
    //public void setRolesToAssign(Set<RequestRoleModifyUserRolesAssign> rolesToAssign) {
    public void setRolesToAssign(Set<RequestRole> rolesToAssign) {
        this.rolesToAssign = rolesToAssign;
    }
    
    
    /**
	 * @return the requestedPositions
	 */
    @OneToMany(mappedBy="primaryKey.request", cascade={CascadeType.ALL})
	public Set<RequestedPosition> getRequestedPositions() {
		return requestedPositions;
	}

	/**
	 * @param requestedPositions the requestedPositions to set
	 */
	public void setRequestedPositions(
			Set<RequestedPosition> requestedPositions) {
		this.requestedPositions = requestedPositions;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	

	public List<String> retrieveRolesToRevokeThatDoNotExistInRolesToAssign() {
        List<String> rolesToRevoke = new ArrayList<String>();
        //for (RequestRoleModifyUserRolesRevoke currRoleToRevoke : getRolesToRevoke()) {
        for (RequestRole currRoleToRevoke : getRolesToRevoke()) {
            //for (RequestRoleModifyUserRolesAssign currRoleToAssign : getRolesToAssign()) {
        	for (RequestRole currRoleToAssign : getRolesToAssign()) {
                //If the current iterated role to revoke does not exists in roles to assign, then add the role to be revoked
                if (!currRoleToAssign.getName().toUpperCase().equals(currRoleToAssign.getName().toUpperCase())) {
                    rolesToRevoke.add(currRoleToRevoke.getName());
                }
            }
        }
        return rolesToRevoke;
    }
    
    public List<String> retrieveRolesToAssignThatDoNotExistInRolesToRevoke() {
        List<String> rolesToAssign = new ArrayList<String>();
        for (RequestRole currRoleToAssign : getRolesToAssign()) {
            for (RequestRole currRoleToRevoke : getRolesToRevoke()) {
                //If the current iterated role to assign does not exists in roles to revoke, then add the role to the assigne list
                if (!currRoleToAssign.getName().toUpperCase().equals(currRoleToRevoke.getName().toUpperCase())) {
                    rolesToAssign.add(currRoleToAssign.getName());
                }
            }
        }
        
        return rolesToAssign;
    }
    
    public void addRoleToRevoke(String roleName) {
        RequestRoleModifyUserRolesRevoke rr = new RequestRoleModifyUserRolesRevoke();
        rr.setRequest(this);
        rr.setName(roleName);
        getRolesToRevoke().add(rr);
    }
    
    public void addRoleToAssign(String roleName) {
        RequestRoleModifyUserRolesAssign rr = new RequestRoleModifyUserRolesAssign();
        rr.setRequest(this);
        rr.setName(roleName);
        getRolesToAssign().add(rr);
    }
    
    
    
    
    
    
    
    
    
    public void addPositionToAssign(Position position) {
    	RequestedPosition rp = new RequestedPosition(position,this,RequestedPositionActionType.ASSIGN);
    	getRequestedPositions().add(rp);
    }
    
    public void addPositionToRevoke(Position position) {
    	RequestedPosition rp = new RequestedPosition(position,this,RequestedPositionActionType.REVOKE);
    	getRequestedPositions().add(rp);
    }
    
    @Transient
    public Set<RequestedPosition> getRequestedPositionsToAssign() {
    	Set<RequestedPosition> reqPositions = new HashSet<RequestedPosition>();
    	
    	for (RequestedPosition reqPos : getRequestedPositions()) {
    		if (reqPos.getActionType() == RequestedPositionActionType.ASSIGN) {
    			reqPositions.add(reqPos);
    		}
    	}
    	
    	return reqPositions;
    }
    
    @Transient
    public Set<RequestedPosition> getRequestedPositionsToRevoke() {
    	Set<RequestedPosition> reqPositions = new HashSet<RequestedPosition>();
    	
    	for (RequestedPosition reqPos : getRequestedPositions()) {
    		if (reqPos.getActionType() == RequestedPositionActionType.REVOKE) {
    			reqPositions.add(reqPos);
    		}
    	}
    	
    	return reqPositions;
    }
    
    
    @Transient
    public String getType() {
        return REQUEST_TYPE;
    }
}
