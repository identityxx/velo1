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

import velo.entity.Account;
import velo.entity.Resource;
import velo.exceptions.ActionFactoryException;
@Deprecated
public class AccountActionFactory extends ResourceActionFactory {
    private Account account;
    
    public AccountActionFactory() {
        
    }
    
    public AccountActionFactory(Resource ts, Account account) throws ActionFactoryException {
        super(ts);
        
        //Critical, otherwise the persisted task holds a huge user entity
        account.getUser().cleanReferences();
        
        setAccount(account);
    }
    
    
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
}
