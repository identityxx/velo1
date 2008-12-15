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
package velo.adapters;

import org.apache.log4j.Logger;

import velo.entity.Resource;
import velo.exceptions.AdapterException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

public abstract class ResourceAdapter extends Adapter {
	private transient static Logger log = Logger.getLogger(ResourceAdapter.class.getName());
	
	
	private Resource resource;
	private ResourceDescriptor resourceDescriptor;
	
	
	public ResourceAdapter() {
		
	}
	
	
	public ResourceAdapter(Resource resource) {
		setResource(resource);
	}
	
	
	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	
	public ResourceDescriptor getResourceDescriptor() throws AdapterException {
		if (this.resourceDescriptor == null) {
			try {
				this.resourceDescriptor = getResource().factoryResourceDescriptor();
			} catch (ResourceDescriptorException e) {
				log.error("Could not factore a Resource Descriptor for resource '" + getResource().getDisplayName()  +"': " + e.toString());
				throw new AdapterException("Could not generate resource descriptor");
			}
		}
		
		return this.resourceDescriptor;
	}
}
