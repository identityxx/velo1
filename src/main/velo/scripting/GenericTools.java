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
package velo.scripting;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EmailManagerLocal;
import velo.ejb.interfaces.IdentityAttributeManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.EmailTemplate;

public class GenericTools {
	private AccountManagerLocal accountManager;
	private UserManagerLocal userManager;
	private IdentityAttributeManagerLocal identityAttributeManager;
	private EmailManagerLocal emailManager;
	private ResourceOperationsManagerLocal resourceOperationsManager;
	private TaskManagerLocal taskManager;
	private RoleManagerLocal roleManager;
	
	private Context initialContext;
	
	/**
	 * @return the accountManager
	 */
	public AccountManagerLocal getAccountManager() throws NamingException {
		if (accountManager == null) {
			accountManager = (AccountManagerLocal) getInitialContext().lookup("velo/AccountBean/local");
		}
		return accountManager;
	}

	/**
	 * @return the userManager
	 */
	public UserManagerLocal getUserManager() throws NamingException {
		if (userManager == null) {
			userManager = (UserManagerLocal) getInitialContext().lookup("velo/UserBean/local");
		}
		
		return userManager;
	}
	
	/**
	 * @return the identityAttributeManager
	 */
	public IdentityAttributeManagerLocal getIdentityAttributeManager() throws NamingException {
		if (identityAttributeManager == null) {
			identityAttributeManager = (IdentityAttributeManagerLocal) getInitialContext().lookup("velo/IdentityAttributeBean/local");
		}
		
		return identityAttributeManager;
	}
	
	
	public EmailManagerLocal getEmailManager() throws NamingException {
		if (emailManager == null) {
			emailManager = (EmailManagerLocal) getInitialContext().lookup("velo/EmailBean/local");
		}
		
		return emailManager;
	}
	
	
	/**
	 * @return the resourceOperationsManager
	 */
	public ResourceOperationsManagerLocal getResourceOperationsManager() throws NamingException {
		if (resourceOperationsManager == null) {
			resourceOperationsManager = (ResourceOperationsManagerLocal) getInitialContext().lookup("velo/ResourceOperationsBean/local");
		}
		
		return resourceOperationsManager;
	}
	

	/**
	 * @return the taskManager
	 */
	public TaskManagerLocal getTaskManager() throws NamingException {
		if (taskManager == null) {
			taskManager = (TaskManagerLocal) getInitialContext().lookup("velo/TaskBean/local");
		}
		
		return taskManager;
	}
	
	
	/**
	 * @return the accountManager
	 */
	public RoleManagerLocal getRoleManager() throws NamingException {
		if (roleManager == null) {
			roleManager = (RoleManagerLocal) getInitialContext().lookup("velo/RoleBean/local");
		}
		return roleManager;
	}
	
	

	public Context getInitialContext() throws NamingException {
		if (initialContext == null) {
			initialContext = new InitialContext();
		}
		
		return initialContext;
	}
	
	
	
	

	
	
	//accessors (currently mostly used via event responses)
	public EmailTemplate getEmailTemplate(String name) throws NamingException {
		return getEmailManager().findEmailTemplate(name);
	}
}
