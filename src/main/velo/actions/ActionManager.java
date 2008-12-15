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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;

import velo.actions.tools.AccountAuthTools;
import velo.actions.tools.AccountTools;
import velo.actions.tools.AddGroupMembershipTools;
import velo.actions.tools.CreateAccountTools;
import velo.actions.tools.RemoveGroupMembershipTools;
import velo.common.SysConf;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.User;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.FactoryException;
import velo.exceptions.ScriptLoadingException;
import velo.exceptions.ResourceTypeDescriptorException;
import velo.patterns.Factory;
import velo.scripting.ScriptFactory;
import velo.resource.resourceTypeDescriptor.ResourceTypeDescriptor;

/**
 * An Action Manager class Used to factory scripted/non-scripted actions easily.
 *
 * @author Asaf Shakarchi
 */
@Deprecated
public class ActionManager {
	private static Configuration sysConf = SysConf.getSysConf();

	private final static String createAccountActionScriptPartialFileName = "create_account"; 

	/**
	 * A ResourceManager EJB object (Initialized by constructor)
	 */
	public ResourceManagerLocal tsm;
	
	public AccountManagerLocal am;
	
	public ActionManager() {
		try {
			Context ic = new InitialContext();
			//ResourceManagerRemote tsmLocal = (ResourceManagerRemote) ic
				//	.lookup(ResourceManagerRemote.class.getName());
			ResourceManagerLocal tsmLocal = (ResourceManagerLocal) ic.lookup("velo/ResourceBean/local");
			tsm = tsmLocal;
			
			
			AccountManagerLocal amLocal = (AccountManagerLocal) ic.lookup("velo/AccountBean/local"); 
			am = amLocal;
			
			
		} catch (NamingException ne) {
			ne.printStackTrace();
		}
	}
	
	/**
	 * Factory a Resource scriptable action
	 * 
	 * @param scriptResourceName
	 *            The resource (class) name of the scripted action class
	 * @param resource
	 *            The Resource object the action referes to
	 * @return A scriptable action object returned casted as
	 *         ResourceActionInterface
	 * @throws ScriptLoadingException
	 */
	public ResourceActionInterface factoryScriptedResourceAction(
			String scriptResourceName, Resource resource)
			throws ScriptLoadingException {

		ScriptFactory sf = new ScriptFactory();
		try {
			Object scriptObj = sf.factoryScriptableObjectByResourceName(scriptResourceName);
			
			ResourceActionInterface scriptedAction = (ResourceActionInterface) scriptObj;
			// Set the target system into the scriptedAction
			scriptedAction.setResource(resource);

			// ResourceActionInterface cc = (ResourceActionInterface)
			// new QflowListAccounts();

			return scriptedAction;
			// return cc;
		} catch (ScriptLoadingException sle) {
			throw sle;
		}
	}
	
	
	/**
	 * @param actionClassName
	 *            The className of the action
	 * @param resource
	 *            The Resource object the action referes to
	 * @return An action object returned casted as a ResourceActionInterface
	 * @throws ClassNotFoundException
	 *             thrown when the specified class name is not found.
	 */
	public ResourceActionInterface factoryResourceJavaAction(
			String actionClassName, Resource resource)
			throws FactoryException {
		System.out.println("Factoring 'Java Action' for class name: "
				+ actionClassName);
		
		if (actionClassName == null) {
			throw new FactoryException("Class recieved is null!");
		}
		
		Object javaActionObj = Factory.factoryInstance(actionClassName);
		ResourceActionInterface tsai = (ResourceActionInterface) javaActionObj;
		// Set the target system into the scriptedAction
		tsai.setResource(resource);

		return tsai;
	}

	
	
	/**
	 * Factory a 'create account' action
	 * 
	 * @param resource
	 *            The target system the action refres to
	 * @param user
	 *            The user entity object the account is related to
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ScriptLoadingException
	 *             threw when impossible to load a script
	 */
	//public ResourceAccountActionInterface factoryCreateAccountAction(Resource resource, User user, String generatedAccountId, UserRole userRole) throws ActionFactoryException {
	public ResourceAccountActionInterface factoryCreateAccountAction(Resource resource, User user, String generatedAccountId) throws ActionFactoryException {
		//Important, otherwise serialized task is huge.
		resource.clearReferences();
		user.cleanReferences();
		
		
		if (resource.getResourceType().isScripted()) {
			try {
				String scriptResourceName = getResourceName(createAccountActionScriptPartialFileName,resource);
				System.out.println("Target system '"+resource.getDisplayName()+"' is scripted, seeking for script name: " + scriptResourceName);
				ResourceActionInterface tsai = factoryScriptedResourceAction(scriptResourceName, resource);

				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;
				//initCreateAccountAction(tsaai,user,userRole,resource,generatedAccountId);
				initCreateAccountAction(tsaai,user,resource,generatedAccountId);
                                
				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				ResourceTypeDescriptor tstd = tsm.factoryResourceTypeDescriptor(resource.getResourceType());
				String className = (String)tstd.getActionsClasses().get("create_account");
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,resource);
				//initCreateAccountAction(tsaai,user,userRole,resource,generatedAccountId);
				initCreateAccountAction(tsaai,user,resource,generatedAccountId);
                                
				return tsaai;
			}
			catch (FactoryException cnfe) {
				throw new ActionFactoryException(cnfe.getMessage());
			}
			catch (ResourceTypeDescriptorException tstde) {
				throw new ActionFactoryException(tstde.getMessage());
			}
		}
	}
	//public void initCreateAccountAction(ResourceAccountActionInterface tsaai, User user, UserRole userRole,Resource resource,String generatedAccountId) {
	public void initCreateAccountAction(ResourceAccountActionInterface tsaai, User user, Resource resource,String generatedAccountId) {
		//	Set the user into the object
		tsaai.setUser(user);
		CreateAccountTools accountTools =  new CreateAccountTools(user, resource);
		accountTools.setAccountId(generatedAccountId);
		//not needed anymore -> accountTools.setUserRole(userRole);
		tsaai.setTools(accountTools);
                //This is scriptable, might throw exceptions
                try {
                    tsaai.init();
                }
                catch (Exception ex) {
                    System.err.println("Could not init action of class named '" + tsaai.getClass().getName() + "', due to: " + ex.getMessage());
                }
	}
	
	

	
	
	/**
	 * Factory a 'enable account' action
	 * 
	 * @param account The account entity to ENABLE
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ScriptLoadingException threw when impossible to load a script
	 */
	public ResourceAccountActionInterface factoryUpdateAccountAction(Account account) throws ActionFactoryException {
		if (account.getResource().getResourceType().isScripted()) {
			try {
				String actionPartialFileName = "update_account";

				String scriptResourceName = getResourceName(actionPartialFileName,account.getResource());
				ResourceActionInterface tsai = factoryScriptedResourceAction(scriptResourceName, account.getResource());

				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;

				// Set the user into the object
				tsaai.setUser(account.getUser());
				tsaai.setAccount(account);

				// TODO CreateAccontTools might fit but not a good class name, inherit
				// from one generic class and do subclass for tools per action type
				// Set an 'CreateAccountActionTools' object for the CreateAccountAction
				// object we factore
				tsaai.setTools(new AccountTools(account.getUser(), account.getResource()));

				// Indicate that the query action is UPDATE (Jdbc adapter needs to know
				// that :-/)
				// tsaai.getQuery().setActionType(ActionOptions.UPDATE);

				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				ResourceTypeDescriptor tstd = tsm.factoryResourceTypeDescriptor(account.getResource().getResourceType());
				String className = (String)tstd.getActionsClasses().get("update_account");
				System.out.println("Target system '"+account.getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,account.getResource());
				//Set the user into the object
				tsaai.setUser(account.getUser());
				tsaai.setAccount(account);
				AccountTools atools = new AccountTools(account.getUser(),account.getResource());
				tsaai.setTools(atools);
				return tsaai;
			}
			catch (FactoryException cnfe) {
				throw new ActionFactoryException(cnfe.getMessage());
			}
			catch (ResourceTypeDescriptorException tstde) {
				throw new ActionFactoryException(tstde.getMessage());
			}
		}
	}
	
	
	
	
	public String getCreateAccountActionScriptContent(Resource ts) throws ScriptLoadingException {
		String actionPartialFileName = "create_account";
		String scriptResourceName = getResourceName(actionPartialFileName,
				ts);
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getDisableAccountActionScriptContent(Account account) throws ScriptLoadingException {
		String actionPartialFileName = "disable_account";
		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getDeleteAccountActionScriptContent(Account account) throws ScriptLoadingException {
		String actionPartialFileName = "delete_account";
		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getEnableAccountActionScriptContent(Account account) throws ScriptLoadingException {
		String actionPartialFileName = "enable_account";
		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getUpdateAccountActionScriptContent(Account account) throws ScriptLoadingException {
		String actionPartialFileName = "update_account";
		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getResetPasswordAccountActionScriptContent(Account account) throws ScriptLoadingException {
		String actionPartialFileName = "reset_pass";
		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getAddGroupMembershipActionScriptContent(Resource ts) throws ScriptLoadingException {
		String actionPartialFileName = "add_group_membership";
		String scriptResourceName = getResourceName(actionPartialFileName,ts);
		return ScriptFactory.getScriptContent(scriptResourceName);
	}
	
	public String getRemoveGroupMembershipActionScriptContent(Resource ts) throws ScriptLoadingException {
		String actionPartialFileName = "remove_group_membership";
		String scriptResourceName = getResourceName(actionPartialFileName,ts);
		return ScriptFactory.getScriptContent(scriptResourceName);
	}

	
	/**
	 * Factory a "Status Action" action for a certain account
	 * 
	 * @param account The account entity to check the status for
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ScriptLoadingException threw when impossible to load a script
	 */
	public ResourceAccountActionInterface factoryAccountStatusAction(Account account) throws ActionFactoryException {
		if (account.getResource().getResourceType().isScripted()) {
			try {
				String actionPartialFileName = "account_status";

				String scriptResourceName = getResourceName(actionPartialFileName,account.getResource());
				ResourceActionInterface tsai = factoryScriptedResourceAction(scriptResourceName, account.getResource());

				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;

				// Set the user into the object
				tsaai.setUser(account.getUser());
				tsaai.setAccount(account);
				AccountTools atools = new AccountTools(account.getUser(),account.getResource());
				tsaai.setTools(atools);
				
				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				ResourceTypeDescriptor tstd = tsm.factoryResourceTypeDescriptor(account.getResource().getResourceType());
				String className = (String)tstd.getActionsClasses().get("account_status");
				System.out.println("Target system '"+account.getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,account.getResource());
				//Set the user into the object
				tsaai.setUser(account.getUser());
				tsaai.setAccount(account);
				AccountTools atools = new AccountTools(account.getUser(),account.getResource());
				tsaai.setTools(atools);
				return tsaai;
			}
			catch (FactoryException cnfe) {
				throw new ActionFactoryException(cnfe.getMessage());
			}
			catch (ResourceTypeDescriptorException tstde) {
				throw new ActionFactoryException(tstde.getMessage());
			}
		}
	}
	
	
	public ResourceAccountActionInterface factoryAddGroupMembershipAction(ResourceGroup tsg,String accountId) throws ActionFactoryException {
		
		if (tsg.getResource().getResourceType().isScripted()) {
			try {
				String actionPartialFileName = "add_group_membership";
				String scriptResourceName = getResourceName(actionPartialFileName,tsg.getResource());
				ResourceActionInterface tsai = factoryScriptedResourceAction(scriptResourceName, tsg.getResource());

				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;

				AddGroupMembershipTools gmTools =  new AddGroupMembershipTools(tsg.getResource(),tsg);
				// Set the user into the object
				gmTools.setAccountName(accountId);
				tsaai.setTools(gmTools);

				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				ResourceTypeDescriptor tstd = tsm.factoryResourceTypeDescriptor(tsg.getResource().getResourceType());
				String className = (String)tstd.getActionsClasses().get("add_group_membership");
				System.out.println("Target system '"+tsg.getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,tsg.getResource());
				//Set the user into the object
				//tsaai.setUser(acc.getUser());
				//tsaai.setAccount(acc);
				//AccountTools atools = new AccountTools(acc.getUser(),tsg.getResource());
				//tsaai.setTools(atools);
				
				
				AddGroupMembershipTools gmTools =  new AddGroupMembershipTools(tsg.getResource(),tsg);
				// Set the user into the object
				gmTools.setAccountName(accountId);
				tsaai.setTools(gmTools);
				
				return tsaai;
			}
			catch (FactoryException cnfe) {
				throw new ActionFactoryException(cnfe.getMessage());
			}
			catch (ResourceTypeDescriptorException tstde) {
				throw new ActionFactoryException(tstde.getMessage());
			}
		}
		
	}
	
	
	public ResourceAccountActionInterface factoryRemoveGroupMembershipAction(ResourceGroup tsg,Account account) throws ActionFactoryException {
		if (tsg.getResource().getResourceType().isScripted()) {
			try {
				String actionPartialFileName = "remove_group_membership";
				String scriptResourceName = getResourceName(actionPartialFileName,tsg.getResource());
				ResourceActionInterface tsai = factoryScriptedResourceAction(scriptResourceName, tsg.getResource());

				// Cast it to the proper interface
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;
				tsaai.setAccount(account);
				
				RemoveGroupMembershipTools rgmTools =  new RemoveGroupMembershipTools(account,tsg);
				tsaai.setTools(rgmTools);

				return tsaai;
			}
			catch (ScriptLoadingException sle) {
				throw new ActionFactoryException(sle.getMessage());
			}
		}
		else {
			try {
				ResourceTypeDescriptor tstd = tsm.factoryResourceTypeDescriptor(tsg.getResource().getResourceType());
				String className = (String)tstd.getActionsClasses().get("remove_group_membership");
				System.out.println("Target system '"+tsg.getResource().getDisplayName()+"' is NOT scripted, creating new instance of class name: " + className);
				ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface)factoryResourceJavaAction(className,tsg.getResource());
				//Set the user into the object
				//tsaai.setUser(acc.getUser());
				//tsaai.setAccount(acc);
				//AccountTools atools = new AccountTools(acc.getUser(),tsg.getResource());
				//tsaai.setTools(atools);
				
				tsaai.setAccount(account);
				RemoveGroupMembershipTools rgmTools =  new RemoveGroupMembershipTools(account,tsg);
				tsaai.setTools(rgmTools);
				
				return tsaai;
			}
			catch (FactoryException cnfe) {
				throw new ActionFactoryException(cnfe.getMessage());
			}
			catch (ResourceTypeDescriptorException tstde) {
				throw new ActionFactoryException(tstde.getMessage());
			}
		}
		
	}
	
	
	
	
	
	/**
	 * Factory a "Reset Password" action for a certain account
	 * 
	 * @param account The account entity to reset the password for
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ScriptLoadingException threw when impossible to load a script
	 */
	public ResourceAccountActionInterface factoryAccountResetPasswordAction(Account account, String password) throws ScriptLoadingException {
		String actionPartialFileName = "reset_pass";

		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		ResourceActionInterface tsai = factoryScriptedResourceAction(
				scriptResourceName, account.getResource());

		// Cast it to the proper interface
		ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;

		// Set the user into the object
		tsaai.setUser(account.getUser());
		tsaai.setAccount(account);
		
		AccountTools tools = new AccountTools(account.getUser(), account.getResource());
		tools.setPassword(password);
		tsaai.setTools(tools);
				
				
		

		// Indicate that the query action is UPDATE (Jdbc adapter needs to know
		// that :-/)
		// tsaai.getQuery().setActionType(ActionOptions.UPDATE);

		return tsaai;
	}

	
	/**
	 * Factory a "Account Authentication" action for a certain account
	 * 
	 * @param account The account entity to check the status for
	 * @param password The new password to set
	 * @return a 'ResourceAccountActionInterface' object
	 * @throws ScriptLoadingException threw when impossible to load a script
	 */
	public ResourceAccountActionInterface factoryAccountAuthentication(Account account, String password) throws ScriptLoadingException {
		String actionPartialFileName = "auth_account";

		String scriptResourceName = getResourceName(actionPartialFileName,
				account.getResource());
		ResourceActionInterface tsai = factoryScriptedResourceAction(
				scriptResourceName, account.getResource());

		// Cast it to the proper interface
		ResourceAccountActionInterface tsaai = (ResourceAccountActionInterface) tsai;

		// Set the user into the object
		tsaai.setUser(account.getUser());
		tsaai.setAccount(account);

		// Set an 'AccountAuthTools' object for the CreateAccountAction object
		// we factore
		tsaai.setTools(new AccountAuthTools(account.getUser(), account
				.getResource(), password));

		// Indicate that the query action is SELECT (Jdbc adapter needs to know
		// that :-/)
		// tsaai.getQuery().setActionType(ActionOptions.SELECT);

		return tsaai;
	}

	
	
	/**
	 * Get a resource name constructed from the action name and the target
	 * system to perform the action on
	 * 
	 * @param actionPartialFileName The partial name of the action
	 * @param resource The target-system to perform the action on
	 * @return A full resource(file) name of the action
	 */
	public String getResourceName(String actionPartialFileName, Resource resource) {
		// Get a propercase of the short target system name
		// String scriptName =
		// StringUtils.capitalize(this.getShortName().toLowerCase());
		// Decided at the end to go all for lowercase
		String scriptName = resource.getUniqueName().toLowerCase() + "_";
		// Now add the actionName to the scriptName
		scriptName += actionPartialFileName.toLowerCase();

		String resourceScriptsRootDirectory = sysConf.getString("system.directory.user_workspace_dir");
		String scriptResourceName = resourceScriptsRootDirectory
				+ "/targets_scripts/"
				+ resource.getUniqueName().toLowerCase() + "/" + "actions"
				+ "/" + scriptName
				+ sysConf.getString("scripts.file_extension");

		return scriptResourceName;
	}
}
