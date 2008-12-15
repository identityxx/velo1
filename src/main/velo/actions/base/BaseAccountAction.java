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
package velo.actions.base;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.ResourceAction;
import velo.entity.Resource;
import velo.entity.User;

/**
 * An abstracted class for all resourceAccount actions
 * 
 * @author Asaf Shakarchi
 */
abstract public class BaseAccountAction extends ResourceAction implements ResourceAccountActionInterface {
	
	/**
	 * Interface requires this, in order to set the user for actions such as 'CreateAccount' (The owner of the account)
	 */
	private User user;
	
	/**
	 * Constructor
	 * @param resource The resource the action is executed on
	 */
	public BaseAccountAction(Resource resource) {
		super(resource);
	}
	
	/**
	 * Constructor
	 * Empty constructor, for easier factory by reflection
	 */
	public BaseAccountAction() {
		
	}
	
	
	
	
	
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
}
