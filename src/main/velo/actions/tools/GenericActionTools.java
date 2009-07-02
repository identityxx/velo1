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

import javax.persistence.NonUniqueResultException;

import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.Account;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.OperationException;
import velo.exceptions.TaskCreationException;

/**
 *  
 * @author Shakarchi Asaf
 */
public class GenericActionTools extends CoreActionTools {
    
    public GenericActionTools() {
    }
    
    public void disableUserInRepository(String userName) throws OperationException {
        UserManagerRemote userManager = getUserManager();
        User loadedUser;
        try {
            loadedUser = userManager.findUserByName(userName);
            userManager.disableUserInRepository(loadedUser);
        } catch (NoResultFoundException ex) {
            throw new OperationException(ex);
        }
        
    }
    /*
    public void disableAccount(String accountName, String targetName) throws OperationException {
        AccountManagerRemote accountManager = getAccountManager();
        try {
            //Try to find the account
            Account account = accountManager.findAccount(accountName, targetName);
            try {
                accountManager.disableAccount(account, null, null, null);
            } catch (TaskCreationException ex) {
                throw new OperationException(ex);
            }
        } catch (NonUniqueResultException ex) {
            throw new OperationException(ex);
        }
    }*/
    
    public User loadUser(String userName) throws OperationException {
        try     {
            velo.ejb.interfaces.UserManagerRemote userManager = getUserManager();
            velo.entity.User loadedUser = userManager.findUserByName(userName);
            
            return loadedUser;
        }
        catch (NoResultFoundException ex) {
            throw new OperationException(ex);
        }
}
}
