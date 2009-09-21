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

import javax.ejb.Local;

import velo.importer.AccountsList;
import velo.importer.AccountsToUsersList;
import velo.importer.RoleCreationUnitList;
import velo.importer.UsersToPositionList;
import velo.importer.RolesToPositionList;

@Local
public interface ImportActions {

	public byte[] getUploadedFile();
	public void setUploadedFile(byte[] uploadedFile);
	public String getContentType();
	public void setContentType(String contentType);
	public String getFileName();
	public void setFileName(String fileName);
	public String getSpreadSheetName();
	public void setSpreadSheetName(String spreadSheetName);
	public String showAccountsToUsersAssoications();
	public String showUsersToPositionAssociations();
	public String showRolesToPositionAssociations();
	public String showNewRolesAssociations();
	public String showAccounts();


	//accessors
	public AccountsToUsersList getImportAccountsToUsersList();
	public String performImportAccountsToUsers();
	
	public UsersToPositionList getImportUsersToPositionList();
	public String  performImportUsersToPosition();
	
	public RolesToPositionList getImportRolesToPositionList();
	public String  performImportRolesToPosition();
	
	public RoleCreationUnitList getImportNewRolesList();
	public String performImportNewRoles();

	public AccountsList getImportAccountsList();
	public String performImportAccounts();
	
	public boolean isCreateAccounts();
	public void setCreateAccounts(boolean createAccounts);	
	
	public void destroy();
}