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
package velo.actions.jdbc;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.actions.ResourceAccountActionInterface;
import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.Resource;
import velo.entity.User;

/**
 * An abstracted class for JDBC type 'CreateAccount' action
 * 
 * TODO: Currently empty, in the future might create the list query automatically
 * Currently must get implemented by child (as an Scriptable action based
 *
 * @author Asaf Shakarchi
 */
public class JdbcAccountAction extends JdbcResourceAction implements ResourceAccountActionInterface {
	
	/**
	 * Holds a refernece to the user entity the action is performed on
	 * (Assuming any account typed action need the User entity for fetching data) 
	 */
	private User user;
	
	//private static Logger logger = Logger.getLogger(JdbcAccountAction.class.getName());
	
	/**
	 * Constructor
	 * @param resource The Resource the action is executed on
	 */
	public JdbcAccountAction(Resource resource) {
		super(resource);
	}
	
	/**
	 * Constructor
	 * Empty constructor, for easier factory by reflection
	 */
	public JdbcAccountAction() {
		
	}
	
	public boolean buildQuery() {
		//logger.info("buildQuery() method must get implemented!");
		return false;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		if (!user.isLoaded()) {
			try {
    			Context ic = new InitialContext();
    			//Must be remote since it could be executed in server agents
        		UserManagerRemote userm = (UserManagerRemote) ic.lookup(UserManagerRemote.class.getName());
        		User loadedUser = userm.findUserById(user.getUserId());
        		loadedUser.setLoaded(true);
        		setUser(loadedUser);
        		return loadedUser;
    		}
			//TODO: Handle exceptions! correctly!
    		catch (NamingException ne) {
    			System.out.println("Couldnt load user!: " + ne);
    			return null;
    		}
		}
		else {
			return user;
		}
	}
}
