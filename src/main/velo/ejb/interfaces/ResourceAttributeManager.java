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
package velo.ejb.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import velo.entity.IdentityAttribute;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.exceptions.NoResultFoundException;

/**
 * A resourceAttribute Manager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface ResourceAttributeManager {
//	public boolean isResourceAttributeOverrided(ResourceAttributeBase rab, boolean resourceLevel) throws ValidationException;
	
	
	public boolean isResourceAttributeExistsByName(String uniqueName, Resource resource);
	public ResourceAttribute findResourceAttributeByName(String resourceAttributeName, Resource resource) throws NoResultFoundException;
	/**
	 * Find Resource Attribute Type by unique name
	 * @param uniqueName The unique name to find attribute for
	 * @param resourceType resource type to find attribute for
	 * @return collection of attributes (1 or two if overridden)
	 */
	//public Collection findResourceAttributeType(String uniqueName, ResourceType resourceType);
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Find a Target System Attribute that represents the 'Account ID' attribute on a certain Target System
	 * @param ts The resource to find the 'Account ID' attribute for
	 * @return resourceAttribute The found resourceAttribute 
	 * @throws NoResultFoundException Threw if there was no AccountId attribute set (should not happen since there -must- be exaclty -one- accountID attributed per resource) 
	 */
	@Deprecated
	public ResourceAttribute factoryAccountIdResourceAttribute(Resource ts) throws NoResultFoundException;
	
	/**
	 * Find all resourceAttributes by the specified IdentityAttribute entity
	 * @param ia The IdentityAttribute entity to get all atached resourceAttributes entities for 
	 * @return A collection of resourceAttribute entities
	 */
	//public Collection<resourceAttribute> findAllresourceAttributes(long resourceId);
	@Deprecated
	public Collection findResourceAttributesAttachedToIdentityAttribute(IdentityAttribute ia);
	
	@Deprecated
	public Collection<ResourceAttribute> findByResource(Resource ts);
	
	
	@Deprecated
	public File getConverterFile(ResourceAttribute tsa);
	
	@Deprecated
	public void writeConverterContentToFile(ResourceAttribute tsa, String content) throws IOException;
	
	@Deprecated
	public String readConverterFileContent(ResourceAttribute tsa) throws IOException;
}
