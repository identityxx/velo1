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
package velo.actions.edm;

import java.util.Collection;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.actions.Action;
import velo.ejb.interfaces.RoleManagerRemote;
import velo.entity.UserRole;
import velo.exceptions.PersistEntityException;

/**
 * An action to create a role entity
 * 
 * @author Asaf Shakarchi
 */
@Deprecated
public class PersistRoleEntity extends Action {
	private static final long serialVersionUID = 1987305452306161212L;
	
	private static Logger logger = Logger.getLogger(PersistRoleEntity.class.getName());

	public boolean execute() {
		logger.info("Persisting role entity via action");
		//Expecting UserRole object to exist in action's properties.
		UserRole userRole = (UserRole)getProperties().get("userRole");

		//logger.info("Persisting a new UserRole entity of Role named: '" + userRole.getRole().getName() + "', to Uesr: '" + userRole.getUser().getName() + "' into the database.");
		
		try {
			//Actions might be executed remotely, always interact remotely with EJBs
			Context ic = new InitialContext();
			RoleManagerRemote rolem = (RoleManagerRemote) ic.lookup("velo/RoleBean/remote");

			rolem.persistUserRoleEntity(userRole);
			
			return true;
			
		} catch (NamingException ne) {
			return false;
		}
		catch (PersistEntityException pee) {
			return false;
		}
	}
	
	public Collection getActionResult() {
		return null;
	}
}
