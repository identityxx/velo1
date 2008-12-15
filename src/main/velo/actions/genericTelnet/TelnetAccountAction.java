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
package velo.actions.genericTelnet;

import velo.actions.ResourceAccountActionInterface;
import velo.entity.User;

/**
 * An abstracted class for JDBC type 'CreateAccount' action
 * 
 * TODO: Currently empty, in the future might create the list query automatically
 * Currently must get implemented by child (as an Scriptable action based
 *
 * @author Asaf Shakarchi
 */
class TelnetAccountAction extends TelnetResourceAction implements ResourceAccountActionInterface {
	
	/**
	 * Holds a refernece to the user entity the action is performed on
	 * (Assuming any account typed action need the User entity for fetching data) 
	 */
	private User user;
	
	//private static Logger logger = Logger.getLogger(JdbcAccountAction.class.getName());
	
	/**
	 * Constructor
	 * @param resource The resource the action isTelnetAccountActionblic HttpClientAccountAction(resource resource) {
		super(resource);
	}
	
	/**
	 * Constructor
	 * Empty constructor, for easier fTelnetAccountAction */
	public TelnetAccountAction() {
		
	}
	
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
