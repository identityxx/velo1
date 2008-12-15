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

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.EmailManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.interfaces.UserManagerRemote;
import velo.exceptions.OperationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;


/**
 *
 * @author Shakarchi Asaf
 */
public class CoreActionTools {
    UserManagerRemote userManagerOld;
    AccountManagerRemote accountManagerOld;
    
    
    
    
    UserManagerLocal userManager;
    AccountManagerLocal accountManager;
    EmailManagerLocal emailManager;
    
    
    public CoreActionTools() {
    }
    
    
    public AccountManagerLocal getAccountTools() throws NamingException {
		try {
			if (accountManager == null) {
				Context ic = new InitialContext();
				accountManager = (AccountManagerLocal) ic.lookup("velo/AccountBean/local");
				
				return accountManager;
			}
			else {
				return accountManager;
			}
		} catch (NamingException e) {
			throw e;
		}
	}
    
    
    public EmailManagerLocal getEmailTools() {
    	/*
    	try {
    		System.out.println("!!!!!!WAAAAAAA");
    		UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
    		ut.begin();
    	}catch(NotSupportedException e) {
    		//throw new NamingException(e.getMessage());
    		return null;
    	}catch (SystemException e) {
    		//throw new NamingException(e.getMessage());
    		return null;
    	}catch(NamingException e) {
    		return null;
    	}
    	*/
    	
    	
		try {
			if (emailManager == null) {
				Context ic = new InitialContext();
				emailManager = (EmailManagerLocal) ic.lookup("velo/EmailBean/local");
				
				return emailManager;
			}
			else {
				return emailManager;
			}
		} catch (NamingException e) {
			//throw e;
			return null;
		}
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //SUPPORTING OLD NAMES
    @Deprecated
    public UserManagerRemote getUserManager() throws OperationException {
        try {
            if (userManagerOld == null) {
                Context ic = new InitialContext();
                userManagerOld = (UserManagerRemote) ic.lookup(UserManagerRemote.class.getName());
            }
            
            return userManagerOld;
        } catch (NamingException ne) {
            throw new OperationException(ne);
        }
    }
    
    @Deprecated
    public AccountManagerRemote getAccountManager() throws OperationException {
        try {
            if (accountManagerOld == null) {
                Context ic = new InitialContext();
                accountManagerOld = (AccountManagerRemote) ic.lookup(AccountManagerRemote.class.getName());
            }
            
            return accountManagerOld;
        } catch (NamingException ne) {
            throw new OperationException(ne);
        }
    }
    
}
