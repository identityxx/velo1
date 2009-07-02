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
package velo.ejb.seam.action;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.entity.Account;
import velo.entity.IdentityAttributesGroup;
import velo.entity.Resource;
import velo.entity.Role;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserRole;

@Local
public interface UserManageActions {
	public void prepareCreateUser();
	public String createUser();
	
	public void manageUser();
	
	//public void viewModifiedUserIdentityAttributes() throws VeloViewException;
	public String viewModifiedUserIdentityAttributes();
	public String updateModifiedUserAttributes();
	
	//public void disableUser();
	//public void enableUser();
	//public void refreshUserAccounts();
	
	public String performUserRolesModifications();
	
	//Accessors
	public Set<IdentityAttributesGroup> getUserIdentityAttributesGroups();
	public Set<UserIdentityAttribute> getModifiedUserIdentityAttributesByUpdateAction();
	
	//User role modification methods
	public void addUserRoleToRevoke(UserRole userRole);
	public void removeUserRoleToRevoke(UserRole userRole);
	public void addRoleToAssign(Role role);
	public void removeRoleToAssign(Role role);
	
	
	public List<Account> getManuallyAccountsAssociations();
	public void setManuallyAccountsAssociations(
			List<Account> manuallyAccountsAssociations);

	public void modifyManuallyAccountsAssociations();
	
	public void removeAccountAssociationFromManagedUser(Account account);
	
	//for the special UserAccountAssociationValidator
	public AccountManagerLocal getAccountManager();
	public void associateAccountToManagedUser();
	
	
	
	
	
	
	
	public Resource getSelectedResourceForAccountToUserAssociation();
	public void setSelectedResourceForAccountToUserAssociation(Resource selectedResourceForAccountToUserAssociation);
	public Account getAccountForAccountToUserAssociation();
	public void setAccountForAccountToUserAssociation(Account accountForAccountToUserAssociation);
	
	
	public void destroy();
}