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
import java.util.HashMap;
import java.util.Set;
import javax.persistence.EntityManager;
import velo.converters.AccountAttributeConverterInterface;
import velo.entity.Account;
import velo.entity.AuditedAccount;
import velo.entity.BulkTask;
import velo.entity.Request;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceAttributeBase;
import velo.entity.ResourceGroup;
import velo.entity.Task;
import velo.entity.User;
import velo.exceptions.BulkActionsFactoryFailureException;
import velo.exceptions.ConverterProcessFailure;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.OperationException;
import velo.exceptions.PasswordValidationException;
import velo.exceptions.TaskCreationException;
import velo.storage.Attribute;

/**
 * An AccountManager interface for all EJB exposed methods
 *
 * @author Asaf Shakarchi
 */
public interface AccountManager {
	
	public void removeAccountEntity(String accountUniqueId, String resourceUniqueName);

	/**
     * Remove an account entity and its relations
     * @param account The account entity to remove from repository
     */
    public void removeAccountEntity(Account account);
	
	
    /**
     * Find account on resource for a certain user.
     * @param resource The resource to find the account on
     * @param user The user entity attached to the user
     * @return A found account, returns null if non-found
     */
    public Account findAccount(Resource resource, User user);
	
	
	
	public void persistAccount(String accountName, String resourceName, String userName);
	public void persistAccount(Account accountToPersist);
	public void updateAccount(Account account);
	public void persistAccountViaReconcile(Account account);
	
    //public boolean isAccountExists(String accountName,String uniqueResourceName);
	public boolean isAccountExists(String accountName,Resource resource);

    public boolean isAuditedAccountExists(String accountName,String uniqueResourceName);
    
    /**
     * Find an account by name
     * @param accountName The name of the account to find
     * @param resourceUniqueName The name of the resource the account is related to
     * @return an Account entity, or null if account was not found / multiple accounts found for resource(not supported yet)
     */
    public Account findAccount(String accountName, Resource resource);
    public Account findAccount(String accountName, String resourceUniqueName);
    
    public Account findAccountEagerly(String accountName, Resource resource);
	
    
    //used by the importer
    public void associateAccountToUser(String accountName, String targetUniqueName, String userName) throws OperationException;
	
	
    
    //used by reconcile
    /**
     * Persist a bulk of accounts in one transaction.
     * @param accountsToPersist A collection of account entities to persist.
     */
    public void persistAccountEntities(Collection<Account> accountsToPersist);
    
    /**
     * Remove a bulk of accounts in one transaction.
     * @param accountsToRemove A collection of account entities to remove.
     */
    public void removeAccountEntities(Collection<Account> accountsToRemove);
	
	
    public void setEntityManager(EntityManager entityManager);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
     * Find any accounts attached to a resourceAttribute for a certain user
     * @param user The User entity the accounts are related to
     * @param resourceAttributes A list of attributes to check whether the accounts are attached with
     * @return A Collection of found accounts
     */
	@Deprecated
    public Collection findAccounts(User user,
            Collection<ResourceAttribute> resourceAttributes);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    
    /**
     * Find an account by ID
     * @param accountId The Id of the account to load
     * @return a loaded Account entity
     */
	@Deprecated
    public Account findAccountById(long accountId) throws NoResultFoundException;
    
	@Deprecated
    public boolean isStaleAccountExists(String accountName, Resource resource);
    
	//used by pluginID!
	@Deprecated
    public Collection<Account> findAccounts(String wildCardAccountName);
	@Deprecated
	public Collection<AuditedAccount> findAuditedAccounts(String wildCardAccountName);
    
    /**
     * Whether an account exist on the specified resource or not
     * @param accountName The account name to check
     * @param resource The resource the account is related to
     * @return true/false upon existense/non-existense
     */
	@Deprecated
    public boolean isAccountExistOnTarget(String accountName, Resource resource);
    
	
    
    /**
     * Create(persist) a new account entity
     * <br><b>Note: This action will NOT create an account on the resource</b>
     * @param account An account entity to remove
     */
	//@Deprecated
    //public void createAccount(Account account);
    
    
    /**
     * Seek for a resourceAttribute with the specified name,
     * if found check whether it's one-to-one mapped to an IdentityAttribute, if so set its value into the Attribute object
     * if not, factory the corresponding converter script, and return an Attribute with the converted value
     *
     * @param account Account name to load the virtual attributes for
     * @param onlySynchronizedAttributesInReconciliationProcess Only load target attributes that flagged for reconciliation process!
     * @return The Account entity with the loaded attributes
     */
	@Deprecated
    public Account loadVirtualAccountAttributes(Account account, boolean onlySynchronizedAttributesInReconciliationProcess);
    
    
	@Deprecated
    public Attribute loadVirtualAccountAttribute(User user,ResourceAttributeBase ra, AccountAttributeConverterInterface atci) throws NoUserIdentityAttributeValueException, NoUserIdentityAttributeFoundException, ConverterProcessFailure, LoadingVirtualAccountAttributeException;
    
    /**
     * Get a specific account attribute for the specified resourceAttribute and User
     * @param user The User entity to get the virtual account attribute for
     * @param tsa The resourceAttribute to get the virtual account attribute for
     * @return An Attribute object as the VirtualAccountAttribute
     * @throws NoUserIdentityAttributeValueException
     * @throws NoUserIdentityAttributeFoundException
     * @throws LoadingVirtualAccountAttributeException
     */
	@Deprecated
    public Attribute loadVirtualAccountAttribute(User user,
            ResourceAttributeBase ra)
            throws NoUserIdentityAttributeValueException,
            NoUserIdentityAttributeFoundException,ConverterProcessFailure, LoadingVirtualAccountAttributeException;
    
    //public Attribute getVirtualAcccountAttribute(Account account, resourceAttribute tsa);
    
	@Deprecated
    public AccountAttributeConverterInterface factoryResourceAttributeConverter(ResourceAttribute tsa) throws ObjectFactoryException;
    
    
    //ACTIONS execution for accounts
    
    
    /**
     * @param user The user to create the account for
     * @param ts The resource to create the account on
     * @param bulkTask The BulkTask entity if this account creation is a part of (usually happens when creating accounts part of a role)
     * @param outputGeneratedAccountId Set into the specified StringBuffer the generatead account ID that is going to be be added by the task.
     * @return The created task entity.
     * @throws TaskCreationException An exception if there was a failure while trying to create the task.
     */
	//@Deprecated
    //public Task createAccountTask(User user, Resource ts, BulkTask bulkTask,StringBuffer outputGeneratedAccountId) throws TaskCreationException;
    
    /**
     * Create an account for a certain User and resource
     * @param user The user to create the account for
     * @param ts The resource to create the account on
     * @param bulkTask The BulkTask entity if this account creation is a part of (usually happens when creating accounts part of a role)
     * @param outputGeneratedAccountId Set into the specified StringBuffer the generatead account ID that is going to be be added by the task.
     * @return The task ID of the created task
     * @throws TaskCreationException An exception if there was a failure while trying to create the task.
     */
	//@Deprecated
    //public Long createAccount(User user, Resource ts, BulkTask bulkTask,StringBuffer outputGeneratedAccountId) throws TaskCreationException;
    
    /**
     * Disable an account
     * @param account The Account to perform the action on
     * @param bulkTask The bulkTask object to attach the 'Disable Account' task to.
     * @param request The Request to this action should be attached to.
     * @param requester The requester of the task
     * @return The task ID of the created task
     */
	//@Deprecated
    //public Long disableAccount(Account account, BulkTask bulkTask, Request request, User requester) throws TaskCreationException;
    
    
    /**
     * @param account
     * @param bulkTask
     * @param request
     * @param requester The requester of the task
     * @return
     * @throws TaskCreationException
     */
	//@Deprecated
    //public Task disableAccountTask(Account account, BulkTask bulkTask, Request request, User requester) throws TaskCreationException;
    
	//@Deprecated
    //public void disableAccounts(Set<Account> accounts, Request request, User requester) throws OperationException;
    
    /**
     * Create a task to enable an account
     * @param account The Account to perform the action on
     * @param bulkTask The BulkTask entity, if the enableAccount task is a part of a bulkTask (null means no)
     * @param request The request entity to attach the task to
     * @return true/false upon success/failure of action execution
     */
	//@Deprecated
    //public Task enableAccountTask(Account account, BulkTask bulkTask, Request request) throws TaskCreationException;
    
	//@Deprecated
    //public Long enableAccount(Account account, BulkTask bulkTask, Request request) throws TaskCreationException;
    
	//@Deprecated
    //public void enableAccounts(Set<Account> accounts, Request request, User requester) throws OperationException;
    
    /**
     * Create a task to reset password for an account
     * @param account The Account to perform the action on
     * @return true/false upon success/failure of action execution
     * @throws PasswordValidationException
     */
	//@Deprecated
    //public Task accountResetPasswordTask(Account account, String password) throws TaskCreationException, PasswordValidationException;
    
    /**
     * Perform an account reset password
     * @param account The account to perform the reset password action on
     * @param password The password to reset
     * @return The ID of the task that is responsible to perform the created action.
     * @throws TaskCreationException
     * @throws
     */
	//@Deprecated
    //public Long accountResetPassword(Account account, String password) throws TaskCreationException, PasswordValidationException;
    
    /**
     * Update an account
     * @param account The Account to perform the action on
     * @return true/false upon success/failure of action execution
     */
	//@Deprecated
    //public boolean updateAccount(Account account);
    
    /**
     * Authenticate an account
     * @param account The Account to perform the action on
     * @param password The password to authenticate
     * @return true/false upon success/failure of action execution
     */
	//@Deprecated
    //public boolean authAccount(Account account, String password);
    
    /**
     * Check account status of a certain account
     * @param account The Account to perform the action on
     * @param statusString Will set the status as a string into this parameter
     * 			(Must be a StringBuffer since String does not pass by reference)
     * @return true/false upon success/failure of action execution
     */
	//@Deprecated
    //public boolean accountStatus(Account account, StringBuffer statusString);
    
    /**
     * Update accounts status for the specified Collection of accounts
     * @param accountList A collection of accounts to update status for
     * @return true/false upon success/failure of actions execution
     * 			(will return false even if -one- account update fails)
     */
	//@Deprecated
    //public boolean updateAccountsStatus(Collection<Account> accountList);
    
	//@Deprecated
    //public Task addGroupMembershipTask(ResourceGroup tsg,String accountId,BulkTask bt) throws TaskCreationException;
	
//	@Deprecated
  //  public Long addGroupMembership(ResourceGroup tsg,String accountId,BulkTask bt) throws TaskCreationException;
    
	//@Deprecated
    //public Task removeGroupMembershipTask(ResourceGroup tsg,Account account,BulkTask bt) throws TaskCreationException;
	
	//@Deprecated
    //public Long removeGroupMembership(ResourceGroup tsg,Account account,BulkTask bt) throws TaskCreationException;
    
    
    //BULK ACCOUNT ACTIONS FACTORY
    
    /**
     * Perform update for the specified account list
     * @param accountList The Accounts to update
     * @return A list of updated accounts
     * @throws BulkActionsFactoryFailureException Threw if there was a failure to factory one or more of the update actions
     */
	/*@Deprecated
    public Collection<ResourceAccountActionInterface> getUpdateActions(
            Collection<Account> accountList) throws BulkActionsFactoryFailureException;
    */
	
	//@Deprecated
    //public Account loadAccountAttributes(Account account,HashMap<String,Attribute> attrs) throws OperationException;
    
    //Seam methods
    
    /**
     * Get account ID
     * @return JSF Navigation string
     */
    //public String getAccountId();
    
    /**
     * Set account ID
     * @param accountId
     */
    //public void setAccountId(String accountId);
    
}
