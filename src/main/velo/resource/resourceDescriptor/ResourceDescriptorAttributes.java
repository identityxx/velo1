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
package velo.resource.resourceDescriptor;

import java.io.Serializable;

/**
 * @author Asaf Shakarchi
 * 
 * A class that represents Attributes of all resources
 */
public class ResourceDescriptorAttributes implements Serializable {
	private String resourceName;

	private String resourceType;

	private String resourceDescription;

	/**
	 * Constructor
	 * 
	 */
	public ResourceDescriptorAttributes() {
	}

	/**
	 * @param resourceName
	 *            The resourceName to set.
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return Returns the resourceName.
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceType
	 *            The resourceType to set.
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * @return Returns the resourceType.
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceDescription
	 *            The resourceDescription to set.
	 */
	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription = resourceDescription;
	}

	/**
	 * @return Returns the resourceDescription.
	 */
	public String getResourceDescription() {
		return resourceDescription;
	}

}