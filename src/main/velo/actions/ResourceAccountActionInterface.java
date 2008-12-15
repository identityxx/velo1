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
import velo.entity.User;

/**
 * The Action interface for all account typed target system actions
 * Required by Groovy Class Loader in order to easily manage scriptable actions with one interface
 * 
 * @author Asaf Shakarchi
 */
@Deprecated
public interface ResourceAccountActionInterface extends ResourceActionInterface {
	
	/**
	 * Set the user object
	 * @param user a User object to set
	 */
	public void setUser(User user);
	
	/**
	 * Get the user object
	 * @return User object
	 */
	public User getUser();
	
	
	/**
	 * Set the account object
	 * @param account a Account object to set
	 */
	public void setAccount(Account account);
	
	/**
	 * Get the account object
	 * @return Account object
	 */
	public Account getAccount();
	
}