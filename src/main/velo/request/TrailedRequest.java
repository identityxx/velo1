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
package velo.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

//import velo.actions.ResourceActionInterface;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.entity.Request;
import velo.entity.Role;
import velo.entity.User;
import velo.exceptions.FactoryResourceActionsForRoleException;
import velo.storage.Attribute;

/**
 * A class that represents the trailing request information
 * Must be serialized, passes through RMI for user through request executor (see RequestManager EJB)
 * All actions that are related to this request must resides here since they must get persisted
 * 
 * @author Asaf Shakarchi
 */
public class TrailedRequest implements Serializable {
	private static final long serialVersionUID = 1987305452306111211L;
	
	private Request request;
	
	private Collection<Role> rolesToAdd = new ArrayList<Role>();
	
	private Collection<Role> rolesToRemove = new ArrayList<Role>();
	
//	private Collection<ResourceActionInterface> resourceActions = new ArrayList<ResourceActionInterface>();
	
	private Collection<Attribute> requestAttributes = new ArrayList<Attribute>();
	
//private Collection<ResourceActionInterface> allresourceActionsForExecution = new ArrayList<ResourceActionInterface>();
	
	private boolean isPrepared = false;
	
	private Collection<User> authorizers = new ArrayList<User>();
	
	//Very important to be local! since Request has reference to Groovy scripted actions which cannot be transported through RMI since groovy classes are dynamically compiled
	private RoleManagerLocal roleManager;
	
	/**
	 * Empty Constructor
	 */
	public TrailedRequest(Request request) {
		//Set the parent request object of this TrailedRequest
		setRequest(request);
	}
	
	/**
	 * @param rolesToAdd The rolesToAdd to set.
	 */
	public void setRolesToAdd(Collection<Role> rolesToAdd) {
		this.rolesToAdd = rolesToAdd;
	}
	
	/**
	 * @param request The request to set.
	 */
	public void setRequest(Request request) {
		this.request = request;
	}

	/**
	 * @return Returns the request.
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return Returns the rolesToAdd.
	 */
	public Collection<Role> getRolesToAdd() {
		return rolesToAdd;
	}
	
	/**
	 * @param rolesToRemove The rolesToRemove to set.
	 */
	public void setRolesToRemove(Collection<Role> rolesToRemove) {
		this.rolesToRemove = rolesToRemove;
	}
	
	/**
	 * @return Returns the rolesToRemove.
	 */
	public Collection<Role> getRolesToRemove() {
		return rolesToRemove;
	}
	
	/**
	 * @param requestAttributes The requestAttributes to set.
	 */
	public void setRequestAttributes(Collection<Attribute> requestAttributes) {
		this.requestAttributes = requestAttributes;
	}
	
	/**
	 * @return Returns the requestAttributes.
	 */
	public Collection<Attribute> getRequestAttributes() {
		return requestAttributes;
	}
	
	
	/**
	 * @param authorizers The authorizers to set.
	 */
	public void setAuthorizers(Collection<User> authorizers) {
		this.authorizers = authorizers;
	}

	/**
	 * @return Returns the authorizers.
	 */
	public Collection<User> getAuthorizers() {
		return authorizers;
	}

	/**
	 * @param isPrepared The isPrepared to set.
	 */
	public void setPrepared(boolean isPrepared) {
		this.isPrepared = isPrepared;
	}
	
	/**
	 * @return Returns the isPrepared.
	 */
	public boolean isPrepared() {
		return isPrepared;
	}
	
	/**
	 * @param resourceActions The resourceActions to set.
	 */
	/*public void setResourceActions(Collection<ResourceActionInterface> resourceActions) {
		this.resourceActions = resourceActions;
	}*/
	
	/**
	 * @return Returns the resourceActions.
	 */
	/*public Collection<ResourceActionInterface> getResourceActions() {
		return resourceActions;
	}*/
	
	/**
	 * @param allresourceActionsForExecution The allresourceActionsForExecution to set.
	 */
	/*public void setAllresourceActionsForExecution(Collection<ResourceActionInterface> allresourceActionsForExecution) {
		this.allresourceActionsForExecution = allresourceActionsForExecution;
	}*/
	
	/**
	 * @return Returns the allresourceActionsForExecution.
	 */
	/*public Collection<ResourceActionInterface> getAllresourceActionsForExecution() {
		return allresourceActionsForExecution;
	}*/
	
	/**
	 * <p>Generate the actions from the specified role list
	 * Algorithm:
	 * 	- Factory actions from the rolesToRemove list 
	 *  - Factory action sfrom the rolesToAdd list
	 */
	/*
	public void createActionsFromRoles() throws FactoryresourceActionsForRoleException {
		//try {
		//Context ic = new InitialContext();
		//roleManager = (RoleManager) ic.lookup(RoleManager.class.getName());
		
		Iterator<Role> rtri = rolesToRemove.iterator();
		while (rtri.hasNext()) {
			Role currRoleToRemove = rtri.next();
			Collection<resourceActionInterface> currActionsForCurrRoleToRemove = roleManager.factoryresourceActionsForRoleRomoval(currRoleToRemove, request.getUser());
			//Add the factored actions to the generic action list of this trailed request
			getAllresourceActionsForExecution().addAll(currActionsForCurrRoleToRemove);
		}
		
		Iterator<Role> rtai = rolesToAdd.iterator();
		while (rtai.hasNext()) {
			Role currRoleToAdd = rtai.next();
			Collection<resourceActionInterface> currActionsForCurrRoleToAdd = roleManager.factoryresourceActionsForRoleToAdd(currRoleToAdd, request.getUser());
			//Add the factored actions to the generic action list of this trailed request
			getAllresourceActionsForExecution().addAll(currActionsForCurrRoleToAdd);
		}
		//}
		//catch(NamingException ne) {
		//	ne.printStackTrace();
		//}
	}
	*/
	
	/**
	 * Add actions from the request to the GLOBAL resourceActions collection of this request
	 *//*
	public void addActionsFromRequestresourceActions() {
		getAllresourceActionsForExecution().addAll(getResourceActions());
	}*/
	
	/**
	 * Make any required preparations before creating the actions objects  
	 * @param rolem
	 */
	public void prepare(RoleManagerLocal rolem) throws FactoryResourceActionsForRoleException {
		this.roleManager = rolem;
		System.out.println("Peparing actions for request");
		//Generate the actions from the roles
//	createActionsFromRoles();
		//Indicate the the trailed request was prepared
		setPrepared(true);
		
		
		
		
		
		//Generate actions from the direct target system actions
		//addActionsFromRequestresourceActions();
		//Indicate that the actions were prepared
		//setPrepared(true);
	}
}
