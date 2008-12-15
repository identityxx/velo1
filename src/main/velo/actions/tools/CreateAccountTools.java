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

import velo.entity.Resource;
import velo.entity.User;
import velo.entity.UserRole;

public class CreateAccountTools extends AccountTools {
	
	/**
	 * The generated NEW account ID
	 */
	private String accountId;
	
	/**
	 * The UserRole the account should be created for
	 */
	private UserRole userRole;
	
	
	
	public CreateAccountTools(User user, Resource resource) {
		super(user,resource);
	}

	/**
	 * @param accountId The accountId to set.
	 */
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return Returns the accountId.
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * @param userRole The userRole to set.
	 */
	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	/**
	 * @return Returns the userRole.
	 */
	public UserRole getUserRole() {
		return userRole;
	} 
}
