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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.ResourceManagerRemote;
import velo.entity.Resource;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * Target System Action tools
 * 
 * @author Asaf Shakarchi
 */
public abstract class ResourceActionTools {
	/**
	 * A reference to the target system the account is created on 
	 */
	private Resource resource;
	
	/**
	 * Set the Resource entity object to the class
	 * @param resource The resource to set.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Get the Resource entity object from the class
	 * @return Returns the resource.
	 */
	public Resource getResource() {
		return resource;
	}
	
	public ResourceActionTools(Resource ts) {
		setResource(ts);
	}
	public ResourceDescriptor getResourceDescriptor() throws ResourceDescriptorException {
		try {
			//TODO: Replace to local..
    		Context ic = new InitialContext();
    		ResourceManagerRemote tsm = (ResourceManagerRemote) ic.lookup("velo/ResourceBean/remote");
    		try {
    			return tsm.factoryResourceDescriptor(getResource());
    		}
    		catch (ResourceDescriptorException tsde) {
    			throw tsde;
    		}
    	}
    	catch(NamingException ne) {
    		ne.printStackTrace();
    		return null;
    	}
	}
}
