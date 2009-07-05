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
package velo.ejb.seam;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.PasswordHash;

import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.User;
import velo.exceptions.OperationException;

@Name("userHome")
public class UserHome extends EntityHome<User> {

	@In
	FacesMessages facesMessages;
	
	@In
	UserManagerLocal userManager;
	
	@Logger
	private Log log;
	
	public void setUserId(Long id) {
		setId(id);
	}

	public Long getUserId() {
		return (Long) getId();
	}

	@Override
	protected User createInstance() {
		User user = new User();
		user.setCreationDate(new Date());
		return user;
		//System.out.println("!!!!!!!!!!!!: " + userManager);
		//return userManager.factoryUser(null);
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public User getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public User getInstance() {
		if (!super.getInstance().getLoaded()) {
			userManager.setEntityManager(getEntityManager());
			try {
				userManager.loadUserAttributes(super.getInstance());
			} catch (OperationException e) {
				log.error(e.getMessage());
				getFacesMessages().add(e.getMessage());
				return null;
			}
		}
		
		return super.getInstance();
	}
	
	
	//TODO: User is special and is created not only through GUI, should pass through the generic User creation method. 
	public String persist() {
		getInstance().setCreatedManually(true);
		getInstance().setPassword(PasswordHash.instance().generateSaltedHash(getInstance().getPasswordConfirm(), getInstance().getName()));

		return super.persist();
	}
	
	public void refresh() {
		log.trace("Accounts amount before refresh: #0",getInstance().getAccounts().size()); 
		getEntityManager().refresh(getInstance());
		log.trace("Accounts amount after refresh: #0",getInstance().getAccounts().size());
	}
	
	//TODO: User is special and is updated not only through GUI, should pass through the generic User update method.
	public String update() {
		if ( (getInstance().getPasswordConfirm() != null) && (getInstance().getPasswordConfirm().length() > 0) ) {
			log.debug("User password was modified, encrypting before merging...");
			getInstance().setPassword(PasswordHash.instance().generateSaltedHash(getInstance().getPasswordConfirm(), getInstance().getName()));
		}
		
		return super.update();
	}
}
