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
package velo.reconcilidation.events;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.Account;
import velo.entity.ReconcilePolicy;
import velo.entity.Resource;
import velo.entity.User;
import velo.exceptions.NoObjectPersistentRequiredException;

/**
 * An abstract class that represents an Account Event class for all Account events types
 * 
 *  @author Asaf Shakarchi
 */
public abstract class AccountEvent extends Event {
	/**
	 * A list of accounts to persist in bulk when the reconcile accounts processes ends
	 * This list is passed by referenced between all events.
	 */
	private Map<String,Account> accountsToPersist;
	
	/**
	 * A list of accounts to remove in bulk when the reconcile accounts processes ends
	 * This list is passed by referenced between all events.
	 */
	private Map<String,Account> accountsToRemove;
	
	/**
	 * The target system the reconcile processes executed over
	 */
	private Resource resource;
	
	/*
	 * The account name to persist (usually used by events that handles new account creations such as 'unasigned/unmatched' events)
	 */
	//private String accountName;
	
	
	
	//EJBs required by the events - usually only to check for account existance, persistance/removal of objects are done in bulk!
	//private AccountManagerRemote am;
	private UserManagerRemote userManager;
	
	//Used by the 'Unasigned account event', recieved by the reconcile accounts processes by the correlation rule
	//private User user;
	
	//private static Logger logger = Logger.getLogger(AccountEvent.clsas.getName());

	
	
	
	/*WHAT FOR?!
	/**
	 * Constructor
	 * 
	 * Initialie the relevant EJBs required for the events 
	 *
	public AccountEvent() {
		try {
    		//Context ic = new InitialContext();
			InitialContext ic = new InitialContext();
			//TODO: Replace to local interface, no reason to use remote
    		AccountManagerRemote amLocal = (AccountManagerRemote) ic.lookup(AccountManagerRemote.class.getName());
    		//am = amLocal;
    		//TODO: Replace to local interface, no reason to use remote
    		UserManagerRemote umLocal = (UserManagerRemote) ic.lookup(UserManagerRemote.class.getName());
    		//um = umLocal;
    	}
    	catch(NamingException ne) {
    		ne.printStackTrace();
    	}
	}
	*/

	/**
	 * Set the resource entity the account is related to
	 * @param resource
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Get the resource entity the account is related to
	 * @return A resource entity the account is related to
	 */
	public Resource getResource() {
		return resource;
	}


	/**
	 * Get the reconcile policy assigned to the the resource the account is related to
	 * @return A ReconcilePolicy entity asigned to the resource
	 */
	public ReconcilePolicy getReconcilePolicy() {
		return getResource().getReconcilePolicy();
	}
	
	
	/**
	 * Create the user entity and link the account to the created user 
	 */
	/*
	public Account getAccountToPersistWithUserLink() throws NoObjectPersistentRequiredException {
		//	Create a new USER entity
		User user = new User();

		//logger.info("Creating user and linking account for name: " + getAccountName());
		System.out.println("Creating user and linking account for name: " + getAccountName());
		//Before inserting a username, make sure there is no existing user with that name
		if (!um.isUserExit(getAccountName())) {
			//Set the User name as the account name of this event 
			user.setName(getAccountName());
			//user.setEnabled(true);
			
			//Link an account to the user and persist the account
			if (!am.isAccountExistOnTarget(getAccountName(),getResource())) {
				Account account = new Account();
				account.setName(getAccountName());
				account.setResource(getResource());
				account.setUser(user);
				
				//10/12/06 -> No more persisting via events, persisting will be in one transaction for all events instead !
				//Insert the account
				//am.createAccount(account);
				return account;
			}
			else {
				//logger.warning("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
				throw new NoObjectPersistentRequiredException("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
			}
		}
		else {
			try {
				//logger.info("Cannot create USER named: " + getAccountName() + ", user already exist!, only linking the account");
				System.out.println("Cannot create USER named: " + getAccountName() + ", user already exist!, only linking the account");
				//Load the existing user
				user = um.findUserByName(getAccountName());
				//logger.info("Linking user ID: " + user.getUserId() + " to account name: " + getAccountName());
				System.out.println("Linking user ID: " + user.getUserId() + " to account name: " + getAccountName());
			
				//Link a new account to the user
			
				if (!am.isAccountExistOnTarget(getAccountName(),getResource())) {
					Account account = new Account();
					account.setName(getAccountName());
					account.setResource(getResource());
					account.setUser(user);
					//am.createAccount(account);
				
					return account;
				}
				else {
					//logger.warning("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
					throw new NoObjectPersistentRequiredException("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
				}
			}
			//TODO: never return anything, must be removed and handled correctly...!
			catch (NoResultFoundException nrfe) {
				nrfe.printStackTrace();
				return null;
			}
		}
	}
	*/
	
	
	
	
	public Account getAccountToPersistWithUserLink(String accountName, User user) throws NoObjectPersistentRequiredException {
		//Link an account to the user and persist the account
		//Lets hope account does not exists already, hopefully we can trust the reconcile accounts to determine that the account does not exist yet!
		//if (!am.isAccountExistOnTarget(getAccountName(),getResource())) {
		Account account = Account.factory(accountName, getResource());
		account.setUser(user);
				
		//10/12/06 -> No more persisting via events, persisting will be in one transaction for all events instead !
		//Insert the account
		//am.createAccount(account);
		return account;
		
		//logger.warning("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
		//throw new NoObjectPersistentRequiredException("Account name: '" + getAccountName() + "' already exist in TargetSytem: " + getresource().getDisplayName() + ", That means account was not recognized in 'reconcile accounts' process, something went wrong!!!");
	}
	
	
	
	public Account getAccountToPersistWithoutUserLink(String accountName) throws NoObjectPersistentRequiredException {
		Account account = Account.factory(accountName, getResource());
		
		return account;
	}
	
	
	public User factoryUser(String userName) {	
		//return User.factoryUser(userName);
		return userManager.factoryUser(userName);
	}
	
	
	
	
	/**
	 * @param accountsToPersist the accountsToPersist to set
	 */
	public void setAccountsToPersist(Map<String,Account> accountsToPersist) {
		this.accountsToPersist = accountsToPersist;
	}

	/**
	 * @return the accountsToPersist
	 */
	public Map<String,Account> getAccountsToPersist() {
		return accountsToPersist;
	}

	/**
	 * @param accountsToRemove the accountsToRemove to set
	 */
	public void setAccountsToRemove(Map<String,Account> accountsToRemove) {
		this.accountsToRemove = accountsToRemove;
	}

	/**
	 * @return the accountsToRemove
	 */
	public Map<String,Account> getAccountsToRemove() {
		return accountsToRemove;
	}
	
	public UserManagerRemote getUserManager() {
		if (userManager == null) {
			try {
				InitialContext ic = new InitialContext();
				//TODO: Replace to local interface, no reason to use remote, unless remote performers can execute reconciles
				userManager = (UserManagerRemote) ic.lookup("velo/UserBean/remote");
				
				return userManager;
	    	}
	    	catch(NamingException ne) {
	    		ne.printStackTrace();
	    		return null;
	    	}
		}
		else {
			return userManager;
		}
	}
}
