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
package velo.ejb.interfaces;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import velo.entity.Account;
import velo.entity.BulkTask;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.Role;
import velo.entity.RolesFolder;
import velo.entity.User;
import velo.entity.UserRole;
import velo.exceptions.LoadingObjectsException;
import velo.exceptions.MergeEntityException;
import velo.exceptions.ModifyResourceGroupsInRoleException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.OperationException;
import velo.exceptions.PersistEntityException;

/**
 * A RoleManager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface RoleManager {
	
	/**
	 * Persist a new role entity
	 * 
	 * @param role The role object to persist
	 * @throws PersistEntityException
	 */
	public void createRole(Role role) throws PersistEntityException;
	
	/**
	 * Find a role by name
	 * 
	 * @param roleName The name of the role to find
	 * @return A loaded role entity, or null if role entity was not found
	 */
	public Role findRole(String roleName);
	
	public void removeUserRoleEntity(UserRole userRole);
	
	//public BulkTask modifyRolesOfUserTasks(Set<UserRole> userRolesToRemove,
			//Set<Role> rolesToAdd, User user, boolean persistEntities) throws OperationException;
	
	public BulkTask modifyRolesOfUserTasks(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException;

	//public long modifyRolesOfUser(Set<UserRole> userRolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException;
	public long modifyRolesOfUser(Set<Role> userRolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException;
	
	public void associateRoleToUser(Role role, User user) throws OperationException;
	
	public long revokeUserRole(UserRole userRole) throws OperationException;
	
	@Deprecated
	public long associateRoleToUserOld(Role role, User user, Date expectedGrantDate) throws OperationException;
	
	public Long modifyAccountsGroupsAssignment(Set<User> users, Role role, Set<ResourceGroup> groupsToAssign, Set<ResourceGroup> groupsToRevoke) throws OperationException;
	public void modifyRoleEntityGroupsAssignmentAssoc(Role role, Set<ResourceGroup> groupsToAssign, Set<ResourceGroup> groupsToRevoke) throws OperationException;
	
	public BulkTask modifyDirectUserRoles(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user, Date tasksExpectedExecutionDate, boolean persistBulkTask) throws OperationException;
	public BulkTask modifyDirectUserRoles(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException;
	public RolesFolder findRolesFolder(String uniqueName);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Find a role by ID
	 * 
	 * @param id
	 *            The ID of the role to find
	 * @return A loaded role entity
	 */
	@Deprecated
	public Role findRoleById(Long id);

	@Deprecated
	public UserRole findUserRole(User user, Role role)
			throws NoResultFoundException;

	


	@Deprecated
	public void persistRolesFolder(RolesFolder rolesFolder);

	/**
	 * Persist a new user role entity
	 * 
	 * @param userRole
	 *            The UserRole entity to persist
	 * @throws PersistEntityException
	 *             if an error was occured while trying to persist the UserRole
	 *             entity into the DB
	 */
	@Deprecated
	public void persistUserRoleEntity(UserRole userRole)
			throws PersistEntityException;

	/**
	 * Remove a role from the database
	 * 
	 * @param role
	 *            The role entity to remove from Database
	 */
	@Deprecated
	public void removeRole(Role role);

	/**
	 * Update a certain role entity to the database
	 * 
	 * @param role
	 *            The role entity to update the database with
	 * @throws MergeEntityException
	 */
	@Deprecated
	public void updateRoleEntity(Role role) throws MergeEntityException;

	@Deprecated
	public void updateRolesFolder(RolesFolder rolesFolder);

	/**
	 * Remove a user role entity from the database
	 * 
	 * @param userRole
	 *            The userRole entity to remove from Database
	 */
	@Deprecated
	public void deleteUserRoleEntity(UserRole userRole);

	/**
	 * Remove a role from the DB by ID
	 * 
	 * @param roleId
	 *            The ID of the role to remove
	 */
	@Deprecated
	public void removeRole(Long roleId);

	@Deprecated
	public void removeRolesFolder(RolesFolder rolesFolder);

	/**
	 * Update a certain UserRole entity
	 * 
	 * @param userRole
	 *            The UserRole to update(Merge) within the database
	 * @return The updated UserRole
	 */
	@Deprecated
	public UserRole updateUserRole(UserRole userRole);

	/**
	 * Check whether a role exist or not by its name
	 * 
	 * @param roleName
	 *            The name of the role to check if exist or not
	 * @return true/false upon existense / non existense
	 */
	@Deprecated
	public boolean isRoleExit(String roleName);

	@Deprecated
	public Set<Role> loadRolesByNames(Set<String> roleNames)
			throws LoadingObjectsException;

	@Deprecated
	public Collection<BulkTask> addRolesByRolesNamesToUserBulkTaskList(
			List<String> rolesToAdd, User user, boolean isDirect);

	@Deprecated
	public void modifyResourceGroupsInRole(Role role,
			Collection<ResourceGroup> groupsToAdd,
			Collection<ResourceGroup> groupsToRemove)
			throws ModifyResourceGroupsInRoleException;

	@Deprecated
	public boolean isResourceAssignedToRole(Role role, Resource resource);

	@Deprecated
	public boolean isResourceGroupAlreadyAddedByUserRole(ResourceGroup tsg,
			User user);

	@Deprecated
	public boolean isRoleUsedByUserRoles(Role role);

	// public boolean isresourceExistInRole(resource resource);

	/**
	 * Factory list of resource actions to -ADD- by specified role and user
	 * entity
	 * 
	 * @param role
	 *            The role entity as a source of all actions to factor
	 * @param user
	 *            The user entity to factor the actions for
	 * @return A collecton of 'resource Actions' casted as a
	 *         resourceActionInterface
	 */
	// public Collection<resourceActionInterface>
	// factoryresourceActionsForRoleToAdd(
	// Role role, User user) throws FactoryresourceActionsForRoleException;
	/**
	 * Factory list of resource actions to -REMOVE- by specified role and user
	 * entity
	 * 
	 * @param role
	 *            The role entity as a source of all actions to factor
	 * @param user
	 *            The user entity to factor the actions for
	 * @return A collecton of 'resource Actions' casted as a
	 *         resourceActionInterface
	 */
	// public Collection<resourceActionInterface>
	// factoryresourceActionsForRoleRomoval(
	// Role role, User user) throws FactoryresourceActionsForRoleException;
	@Deprecated
	public boolean isAccountProtectedByAnotherUserRole(UserRole userRole,
			Resource resource);

	@Deprecated
	public Collection<UserRole> getUserRolesProtectAnAccount(Account account);

	@Deprecated
	public Collection<RolesFolder> loadRolesFolders(boolean onlyActive);

	@Deprecated
	public void associateGroupToRole(String groupUniqueId,
			String groupTargetName, String roleName) throws OperationException;

	@Deprecated
	public void createRolesRevokerTimerScanner(long initialDuration,
			long intervalDuration);

	@Deprecated
	public void createRolesRevokerTimeScanner() throws OperationException;

	@Deprecated
	public boolean isRolesRevokerScannerActivate();
}
