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
package velo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

//@Entity
@MappedSuperclass
public class ResourceTask extends Task {
	private String resourceUniqueName;
	//private Resource resource;

	// Holds a reference to the resource type operation
	// required by the controller to determine all associated resource actions
	// related to this operation for the specified resource unique name
	private ResourceTypeOperation resourceTypeOperation;

	/**
	 * @return the resourceUniqueName
	 */
	//nullable coz all tasks types are persisted in the same table 
	@Column(name = "RESOURCE_UNIQUE_NAME", nullable = true)
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}

	/**
	 * @param resourceUniqueName
	 *            the resourceUniqueName to set
	 */
	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}

	/**
	 * @return the resourceTypeOperation
	 */
	//optional=true otherwise column is created not nullable and required for GenericTask
	@ManyToOne(optional=true)
	@JoinColumn(name = "RESOURCE_TYPE_OPERATION_ID", nullable = true, unique = false)
	public ResourceTypeOperation getResourceTypeOperation() {
		return resourceTypeOperation;
	}

	/**
	 * @param resourceTypeOperation
	 *            the resourceTypeOperation to set
	 */
	public void setResourceTypeOperation(
			ResourceTypeOperation resourceTypeOperation) {
		this.resourceTypeOperation = resourceTypeOperation;
	}
	
//	@ManyToOne(optional=false)
//	@JoinColumn(name = "RESOURCE_ID", nullable = false)
//	public Resource getResource() {
//		return resource;
//	}
//
//	public void setResource(Resource resource) {
//		this.resource = resource;
//	}
	
	
	

	public static ResourceTask factory(String resourceUniqueName, ResourceTypeOperation resourceTypeOperation, String description) {
		ResourceTask rTask = new ResourceTask();
		rTask.setCreationDate(new Date());
		rTask.setExpectedExecutionDate(new Date());
		rTask.setDescription(description);
		rTask.setResourceUniqueName(resourceUniqueName);
		rTask.setStatus(TaskStatus.PENDING);
		rTask.setResourceTypeOperation(resourceTypeOperation);
		
    	return rTask;
    }
}
