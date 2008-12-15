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
package velo.rules;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.User;
import velo.exceptions.OperationException;

/**
 * An abstract base class that represents an Account Correlation
 * (Inherited per Target-System)
 * 
 * @author Asaf Shakarchi
 */
public abstract class AccountsCorrelationRule {
	private Account account;
	private UserManagerLocal userManager;
	
	public AccountsCorrelationRule() throws OperationException {
		//Initiate the UserManagerLocal EJB
		try {
			InitialContext ic = new InitialContext();
			//userManager = (UserManagerLocal) ic.lookup("java:comp/env/userEjbRef");
			userManager = (UserManagerLocal) ic.lookup("velo/UserBean/local");
		}
		catch (NamingException ne) {
			throw new OperationException(ne.getMessage());
		}
	}
	
	public boolean isUserExists(String userName) throws NamingException {
		return userManager.isUserExit(userName);
	}
	
	public User findUserByAttribute(String attrName, String attrValue) {
		return null;
	}
	
	
	public abstract String correlate();

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param userManager the userManager to set
	 */
	public void setUserManager(UserManagerLocal userManager) {
		this.userManager = userManager;
	}

	/**
	 * @return the userManager
	 */
	public UserManagerLocal getUserManager() {
		return userManager;
	}
	
}
