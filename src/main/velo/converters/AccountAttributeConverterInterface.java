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
package velo.converters;

import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.storage.Attribute;

/**
 * The Converter interface for converting account attribute
 * 
 * @author Asaf Shakarchi
 */
public interface AccountAttributeConverterInterface extends ConverterInterface {

	public boolean convert();

	/**
	 * Set the user the account attribute is related to
	 * @param user
	 */
	public void setUser(User user);

	/**
	 * Get the user object the account attribute conversion occures for
	 * @return User entity object
	 */
	public User getUser();
	
	
	public void setResource(Resource ts);
	
	public Resource getResource();
	
	public void setResourceAttribute(ResourceAttribute tsa);
	
	public ResourceAttribute getResourceAttribute();

	
	/**
	 * Set the source of the UserIdentityAttribute to be converted
	 * @param uesrAttribute UserIdentityAttribute object to convert
	 */
	public void setSourceUserAttribute(UserIdentityAttribute uesrAttribute);

	/**
	 * Get the UserIdentityAttribute source object to be converted
	 * @return UserIdentityAttribute object
	 */
	public UserIdentityAttribute getSourceUserAttribute();

	/**
	 * Set the resultAttribute after conversion occures
	 * @param resultAttribute object after conversion
	 */
	public void setResultAttribute(Attribute resultAttribute);

	/**
	 * Get the converted user identity attribute
	 * @return Attribute after conversion
	 */
	public Attribute getResultAttribute();
}