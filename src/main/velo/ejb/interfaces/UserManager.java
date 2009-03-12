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
import java.util.Map;
import java.util.Set;

import velo.entity.Account;
import velo.entity.BulkTask;
import velo.entity.Capability;
import velo.entity.CreateUserRequest;
import velo.entity.IdentityAttribute;
import velo.entity.PasswordPolicyContainer;
import velo.entity.Position;
import velo.entity.Resource;
import velo.entity.Role;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserRole;
import velo.exceptions.EntityAssociationException;
import velo.exceptions.ModifyAttributeFailureException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.OperationException;
import velo.exceptions.PasswordValidationException;
import velo.exceptions.PersistEntityException;
import velo.exceptions.UnsupportedAttributeTypeException;
import velo.exceptions.UserAuthenticationException;
import velo.request.Attributes;
import velo.storage.Attribute;

/**
 A UserManager interface for all EJB exposed methods
 
 @author Asaf Shakarchi
 */
public interface UserManager {
    
	/**
	 * Find all accounts attached to a specific UserIdentityAttribute
	 * Algorithm:
	 *   - Fetch all resourceAttributes attached to the -IdentityAttribute- assigned in the specified UserIdentityAttribute
	 *   - For each resourceAttribute, find its corresponding accounts for the -User- entity assigned in the specified UserIdentityAttribute
	 *   - Add all accounts to the collection
	 * @param uia A UserIdentityAttribute entity to retrieve the accounts for
	 * @return A collection of accounts related to the specified UserIdentityAttribute entity
	 */
	public Collection<Account> findAccountsByUserIdentityAttribute(UserIdentityAttribute uia);
	
	/**
	 * Modify a collection of UserIdentityAttributes.
	 * (Simply iterate over the list and call 'modifyUserAttribute' method)
	 * @see #modifyUserAttribute(UserIdentityAttribute)
	 * @param uiaList A collection of UserIdentityAttribute elements to modify
	 */
	public void modifyUserAttributes(Collection<UserIdentityAttribute> uiaList) throws ModifyAttributeFailureException;
	
	public User findUser(String name);
	
	public User findUserEagerly(String name);
	
	/**
	 * Whether a user exist or not
	 * @param userName The name of the user to check for existense
	 * @return true/false upon user existense / non existense
	 */
	public boolean isUserExit(String userName);
	
	public User factoryUser(String userName);
	
	 /**
	  * Load all users in the system
	  * @return A collection of all user entities
	  * */
	//used by user reconciliation
	public Collection<User> findAllUsers();
	
    /**
    Create a user based on a user entity
    @param user A user entity to persist
    */
	public void persistUserEntity(User user) throws PersistEntityException;

	
	public Long getCreatedUsersAmount(Date from, Date to);
	
	public void assignCapabilityToUser(User user, String capabilityUniqueName) throws EntityAssociationException ;
	
	public Capability findCapability(String uniqueName);
	
    public Collection<User> findUsersToSync();
	
    /**
    Find all users assigned to a certain role
    @param role The role to find the users assigned to
    @return A collection of found users attached to the specified role.
    */
   public List<User> findAllUsersAssignedToRole(Role role);
	
   public User findUser(String identityAttributeUniqueName, String value);
	
   
   public List<User> findUsers(Map<String,String> ias, boolean caseSensitive);
	
   public Map<String,User> findUsersAssignedToRole(String roleName);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    //Mostly used by action tools to reload a user eaglery/remotely
	@Deprecated
    public User reloadUser(User user, boolean eagerly);
            
	@Deprecated
    public User loadFreshedUserById(long userId);
    
    /**
     Find a user by ID
     @param id The Id of the user to load
     @return a loaded User entity
     */
	@Deprecated
    public User findUserById(long id);
    
	@Deprecated
    public Collection<User> findUsersByString(String searchString);
    
    
    
    /**
     Find all User Roles entities for a certain user
     @param User The user to find all attached user roles entities for
     @return a collection of UserRoles
     @deprecated - Who needs this? there's a relationship between user->userRoles
     */
	@Deprecated
    public Collection<UserRole> findAllUserRolesForUser(User user);
    
    
	@Deprecated
    public User loadUserByName(String userName, boolean eagerly) throws NoResultFoundException;
    
    /**
     Find a user by name
     @param userName The name of the user to find
     @return a User entity
     @throws NoResultFoundException
     */
	@Deprecated
    public User findUserByName(String userName) throws NoResultFoundException;
    
	@Deprecated
    public Collection<User> findUserByIdentityAttributes(Collection<IdentityAttribute> identityAttributes, String searchString);
    
    /**
     Persist a bulk of users.
     @param users A collection of User entity to persist.
     */
	@Deprecated
	public void persistUsers(Collection<User> users);

	/**
	 * Remove a user based on a user entity
	 * @param user A user entity to remove
	 */
	@Deprecated
	public void removeUser(User user);

	/**
	 * Remove a user by user ID
	 * @param userId The user ID to remove
	 */
	@Deprecated
	public void removeUser(long userId);

	@Deprecated
	public void removeUsers(Collection<User> users);

	/**
	 * Update a user by updating its entity on database.
	 * @param user The user entity to update
	 */
	@Deprecated
	public void updateUser(User user);
        
	@Deprecated
        public void disableUserInRepository(User user);

	@Deprecated
        public void changeUserPassword(User user, String newPassword) throws PasswordValidationException;

	/**
	 * Find users by a wildcard
	 * @param wildCardAccountName The wildcard to find users for
	 * @return A collection of User entities that matches the specified wildcard.
	 */
	@Deprecated
	public Collection<User> findUsers(String wildCardAccountName);


	/**
	 * Whether the user has a certain role assigned or not
	 * @param user The user to check the role assignment for
	 * @param role The role to check if assigned to the specified user
	 * @return true/false upon assigned/not assigned
	 */
	@Deprecated
	public boolean isRoleAssignedToUser(User user, Role role);

	/**
	 * Whether a user has an account or not on the specified Target System
	 * @param user User entity
	 * @param ts resource entity
	 * @return true/false upon success/false of finding
	 */
	@Deprecated
	public boolean isUserHasAccountOnResource(User user, Resource ts);

        

	/*
	 * Add a role to a user
	 * Algorithm:
	 * - Create a new transaction
	 * - Per target system associated with the specified role:
	 * 	 - Check if the user already have an account for that target system
	 *     - if yes, do nothing but raise a log message
	 *     - if no, create a 'create account' action for that resource
	 * - Link the role entity to the user entity
	 * - If everything is okay, commit the transaction, otherwise rollback
	 * @param user The user entity to add the role for
	 * @param role The role to add to the specified user
	 * @param requester The requested entity of the request
	 */
	//public void addRoleToUser(User user, Role role, User requester) throws RequestExecutionException;



	/**
	 * Get a specific identity attribute for a specified user
	 * @param user The user to get the identity attribute for
	 * @param attrName The name of the attribute to retrieve
	 * @return a UserIdentityAttribute entity
	 *
	 * @throws NoUserIdentityAttributeFoundException
	 */
	@Deprecated
	public UserIdentityAttribute getUserIdentityAttribute(User user,
			String attrName) throws NoUserIdentityAttributeFoundException;

	


	/**
	 * Create User Identity Attributes for a specific user.
	 * (The attribute is skipped if the user already has one of the requested attribute)
	 * @param attributes An 'Attributes' object containing a list of attributes to set
	 * @param user The user entity to create the UserIdentityAttribute(s) for
	 * @throws UnsupportedAttributeTypeException threw if tried to set an attribute that is not supported by the corresponding IdentityAttribute named (might happen if the types of the attribute/values are different)
	 */
	@Deprecated
	public void createUserIdentityAttributesForUser(Attributes attributes, User user) throws UnsupportedAttributeTypeException;


	/**
	 * Gets account list for a specific user&target system
	 * @param ts The target system to retrieve the accounts for
	 * @param user The user to retrieve the attached accounts
	 * @return a collection of accounts
	 */
	@Deprecated
	public Collection<Account> getAccountsForResourcePerUser(
			Resource ts, User user);




	/**
     * Modify user attribute
     * Algorithm:
     * - Verify that the attribute values fits the structure of the specified UserIdentityAttribute, otherwise throw an exception
     * - If validated, set the value of the secondary (source) attribute into the UserIdentityAttribute that should be updated.
     * - Perform the modification by calling modifyUserAttribute(UserIdentityAttribute)
     *
     * @see #modifyUserAttribute(UserIdentityAttribute)
     * @param uia The userIdentityAttribute to modify
	 * @param newAttribute The new attribute with the values to modify
     */
	@Deprecated
	public void modifyUserAttribute(UserIdentityAttribute uia,
			Attribute newAttribute) throws ModifyAttributeFailureException;

	/**
	 * Modify user attribute by another Attribute
	 * Algorithm:
	 * - Update the entity itself with the new value
	 * - get a list of resourceAttributes attached to the corresponding IdentityAttribute
     * - Build a collection of account entities - Per resourceAttribute: check if an account exist for its corresponding resource and add it to the collection
     * - Per account in the collection, factory an 'Update Account' action, and execute all actions which will perform the updates on the target systems
	 *
	 * @param uia The userIdentityAttribute to modify
	 */
	@Deprecated
	public void modifyUserAttribute(UserIdentityAttribute uia) throws ModifyAttributeFailureException;



	/**
	 * Create a bulktask of all disable accounts tasks.
	 * @param user The user to disable
	 * @return A bulkTask object
	 * @throws OperationException
	 */
	@Deprecated
	public BulkTask disableUserBulkTask(User user) throws OperationException;

	/**
	 * Disable a user
	 * <b>Note: Will disable all of its accounts!</b>
	 * @param user The user entity to disable
	 * @return The BulkTask ID of the created bulk task
	 * @throws OperationException threw if there was a problem to perform the operation
	 */
	@Deprecated
	public Long disableUser(User user) throws OperationException;


	/**
	 * Create a bulktask of all enable accounts tasks.
	 * @param user The user to enable
	 * @return A bulkTask object
	 * @throws OperationException
	 */
	@Deprecated
	public BulkTask enableUserBulkTask(User user) throws OperationException;

	/**
	 * Enable a user
	 * <b>Note: Will enable all of its accounts!</b>
	 * @param user The user entity to enable
	 * @return The BulkTask ID of the created bulk task
	 * @throws OperationException threw if there was a problem to perform the operation
	 */
	@Deprecated
	public Long enableUser(User user) throws OperationException;


	/**
	 * Create a bulktask of all delete accounts tasks.
	 * @param user The user to delete
	 * @return A bulkTask object
	 * @throws OperationException
	 */
	@Deprecated
	public BulkTask deleteUserBulkTask(User user) throws OperationException;

	/**
	 * Delete a user and all of its associated accounts
	 * @param user The user entity to delete
	 * @return The BulkTask ID of the generated bulk task
	 * @throws OperationException thew if there was a problem to perform the opreation
	 */
	@Deprecated
	public Long deleteUser(User user) throws OperationException;

	/**
	 * Reset password for the specified user
	 * <b>Note: will reset password for all of its accounts!</b>
	 * @param user The user entity to reset the password for
	 * @param password The password to reset
	 * @return true/false upon success/failure
	 */
	@Deprecated
	public Long userAccountsResetPassword(User user, String password) throws OperationException, PasswordValidationException ;


	/**
	 * @param user
	 * @param password
	 * @return
	 * @throws OperationException
	 * @throws PasswordValidationException
	 */
	@Deprecated
	public BulkTask userAccountsResetPasswordBulkTask(User user, String password) throws OperationException, PasswordValidationException;

	
	@Deprecated
	public BulkTask accountsResetPasswordForPasswordPolicyContainerBulkTask(User user, PasswordPolicyContainer ppc,List<Account> accounts, String password) throws OperationException, PasswordValidationException;

	@Deprecated
	public Long accountsResetPasswordForPasswordPolicyContainer(User user, PasswordPolicyContainer ppc,List<Account> accounts, String password) throws OperationException, PasswordValidationException;


	/**
     * Authenticate a user by password
     * Algorithm:
     *  - Iterate through the 'Authentication list' of the target systems
     *  - By priority, per target system, factory its corresponding auth action and authenticate
     *  - If returned true, register the user via JAAS, otherwise, indicate a failure
     *
	 * @param username The username to authenticate
	 * @param password The password to authenticate
         * @param ip The Ip address of the client which is trying to authenticate
	 * @return true/false upon success/failure of authentication process
	 */
	@Deprecated
	public boolean authenticate(String username, String password, String ip) throws UserAuthenticationException;


	//REMOVED, MOVED TO ACCOUNT ENTITY
	/*
	 * Whether or not there is any UserRoles that protects an account
	 * (Usually used before deleting an account on a target)
	 * @param account - The account entity to check if its protected or not.
	 * @param excludedRoleList - A list of Roles to be excluded while checking roles protection for the specified account
	 * @return true/false whether the account is being protected by other UserRoles or not.
	 * @throws UserRoleProtectsAccountException
	 *
	//@Deprecated
	//public boolean isAnyUserRoleProtectsAccount(Account account, Collection<Role> forcedUnprotectRoleList) throws UserRoleProtectsAccountException;


	/**
	 * Modify User Positions
	 * @param user
	 * @param positionsToRemove
	 * @param positionsToAdd
	 */
	@Deprecated
	public void modifyUserPositions(User user, Set<Position> positionsToRemove, Set<Position> positionsToAdd);

	@Deprecated
	public User createUserFromRequest(CreateUserRequest request) throws OperationException;


	@Deprecated
	public Collection<PasswordPolicyContainer> loadAccountsInPasswordPolicyContainers(Collection<PasswordPolicyContainer> passwordPolicyContainers, User user);
        
	@Deprecated
        public void associateUserToRole(String userName, String roleName, boolean isInherited) throws OperationException;
        
	@Deprecated
        public void updateUserIdentityAttributes(Collection<UserIdentityAttribute> userIdentityAttributes);
        
	@Deprecated
        public void persistUserIdentityAttributes(Collection<UserIdentityAttribute> userIdentityAttributes);
}

