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
package velo.actions;

import java.util.Collection;
import velo.ejb.interfaces.ResourceManager;
import velo.entity.Role;
import velo.entity.Resource;
import velo.entity.User;
import velo.exceptions.AccountIdGenerationException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.FactoryResourceActionsForRoleException;

/**
 * An action to add a role
 * 
 * @author Administrator
 * @deprecated
 */
@Deprecated
public final class AddRoleAction extends Action {
	private static final long serialVersionUID = 1987305452306161212L;
	
	/**
	 * The role entity to execute
	 */
	private Role role;
	
	/**
	 * The user to add the role to
	 */
	private User user;
	
	private ResourceManager tsm;
	
	public boolean execute() throws ActionFailureException {
		//Make sure we have the required properties set
		if ( (role == null) || (user == null)) {
			throw new ActionFailureException("Role or User is null, must set both entities before initializing an 'AddRole' action!");
		}
		
		//Create a map to hold account ID / action
		try {
			for (Resource currTs : role.getResources()) {
				String currGeneratedAccountId = tsm.generateNewAccountId(currTs,user);
				ResourceActionInterface currAction = factoryResourceCreateAccountAction(currTs,user,currGeneratedAccountId);
			}
			
			//Get 'CreateAccount' actions for the specified Role&User
			//Collection<resourceActionInterface> createAccountActionList = factoryResourceCreateAccountActionsForRoleToAdd(role,user);

			//Iterate over the account actions, execute each and keep a list of the created accounts
			return true;
		}
		catch (AccountIdGenerationException aige) {
			throw new ActionFailureException(aige.getMessage());
		}
		catch (FactoryResourceActionsForRoleException ftsafre) {
			throw new ActionFailureException(ftsafre.getMessage());
		}
	}
	
	public Collection getActionResult() {
		return null;
	}
	
	public ResourceActionInterface factoryResourceCreateAccountAction(Resource ts, User user,String generatedAccountId) throws FactoryResourceActionsForRoleException {
		ActionManager am = new ActionManager();
		return null;
	}
	
	/*
	public Collection<resourceActionInterface> factoryresourceCreateAccountActionsForRoleToAdd (
			Role role, User user) throws FactoryresourceActionsForRoleException {
		
		if (role.getResources().size() == 0) {
			throw new FactoryresourceActionsForRoleException ("Cannot factory actions for role since the role is empty, role name: " + role.getName());
		}

		ActionManager am = new ActionManager();
		Collection<resourceActionInterface> roleActions = new ArrayList<resourceActionInterface>();
		
		for (resource currresource : role.getResources()) {
			try {
				//Add the factored create account action to the role action list
				System.out.println("Adding factored action to role name: "
						+ role.getName() + ", for target system: "
						+ currresource.getDisplayName());
				roleActions.add(am.factoryCreateAccountAction(currresource,
						user));
			} catch (ScriptLoadingException sle) {
				String errMsg = "Couldnt factory 'create account' action for role name: "
					+ role.getName()
					+ ", action's script was not found for target system: "
					+ currresource.getDisplayName()
					+ ", exiting with message: " + sle.getMessage();
				throw new FactoryresourceActionsForRoleException(errMsg);
			}
		}
		
		return roleActions;
	}
	*/
}
