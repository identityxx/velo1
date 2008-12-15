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
package velo.actions.tools;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerRemote;


//EXPOSED IN ACTION SCRIPTS FOR ATTRIBUTE RULES
public class ResourceAttributeActionRuleTools {
	AccountManagerRemote accountManager;
	
	public boolean isAccountExists(String accountName, String resourceUniqueName, boolean checkInAuditedAccount) throws NamingException {
		boolean exist = getAccountManager().isAccountExists(accountName, resourceUniqueName);
		
		if (exist) {
			return true;
		}
		
		if (checkInAuditedAccount) {
			return getAccountManager().isAuditedAccountExists(accountName, resourceUniqueName);
		} else {
			return exist;
		}
	}
	
	
	public AccountManagerRemote getAccountManager() throws NamingException {
		try {
			if (accountManager == null) {
				Context ic = new InitialContext();
				accountManager = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
				
				return accountManager;
			}
			else {
				return accountManager;
			}
		} catch (NamingException e) {
			throw e;
		}
	}
}
