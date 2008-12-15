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

/**
 * A tool class required when authenticating a user
 *  
 * @author Asaf Shakarchi
 */
public class AccountAuthTools extends AccountTools {
	
	private String password;
	
	/**
	 * Constructor
	 * @param user The user entity to authorize
	 * @param resource The target system to check authorization against
	 * @param password The password to authorize
	 */
	public AccountAuthTools(User user, Resource resource, String password) {
		super(user,resource);
		setPassword(password);
	}
	
	/**
	 * Get the password string
	 * @return Password string
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Set the password string
	 * @param password The password string to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
}
