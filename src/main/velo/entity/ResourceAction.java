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

import groovy.lang.GroovyObject;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.validator.NotNull;

/**
 * A resource action that is being executed within a resource operation on a certain InvokePhase
 * 
 * @author Asaf Shakarchi
 */
@Entity 
@DiscriminatorValue("RESOURCE_ACTION")
public class ResourceAction extends ActionRule {
	private static transient Logger log = Logger.getLogger(ResourceAction.class.getName());
	
	/**
	 * Set an ENUM with the phases this action can be performed in the opreation execution
	 */
	public enum InvokePhases {
		CREATION, VALIDATE, PRE, POST
	}
	
	private Resource resource;
	
	private InvokePhases invokePhase;
	

	private ResourceTypeOperation resourceTypeOperation;

	public ResourceAction() {
		
	}
	
	public ResourceAction(Resource resource, String description, InvokePhases phase, boolean active, ResourceTypeOperation rto) {
		setResource(resource);
		setDescription(description);
		setInvokePhase(phase);
		setActive(active);
		setResourceTypeOperation(rto);
	}

	/**
	 * @return the resource
	 */
	//optional=true otherwise (in mysql at least) the column is created as NOT NULLABLE
	//this is critical as 'vl_action' table is in use for many type of actions (event responses/attribute rules/etc...)
	@ManyToOne(optional=true)
	@JoinColumn(name="RESOURCE_ID", nullable=true)
	public Resource getResource() {
		return resource;
	}

	
	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	
	/**
	 * @return the invokePhase
	 */
	@Column(name="INVOKE_PHASE")
	@Enumerated(EnumType.STRING)
	@NotNull //this is the best thing I came up with as 'nullable=false' is not an option
	//with oracle db as a repository, it is set as not_null for any actions, while resource_attributes has no invoke_phase at all
	public InvokePhases getInvokePhase() {
		return invokePhase;
	}


	/**
	 * @param invokePhase the invokePhase to set
	 */
	public void setInvokePhase(InvokePhases invokePhase) {
		this.invokePhase = invokePhase;
	}
	
	
	
	/**
	 * @return the resourceTypeOperation
	 */
	//see explaination of optional=true/nullable=true in ResourceAction->getResource()
	@ManyToOne(optional=true)
    @JoinColumn(name="RESOURCE_TYPE_OPERATION_ID", nullable=true)
	public ResourceTypeOperation getResourceTypeOperation() {
		return resourceTypeOperation;
	}


	/**
	 * @param resourceTypeOperation the resourceTypeOperation to set
	 */
	public void setResourceTypeOperation(ResourceTypeOperation resourceTypeOperation) {
		this.resourceTypeOperation = resourceTypeOperation;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof ResourceAction))
			return false;
		ResourceAction ent = (ResourceAction) obj;
		if (this.getActionDefinitionId().equals(ent.getActionDefinitionId()))
			return true;
		return false;
	}
	
	
	@Override
	public void updateScriptedObject(GroovyObject scriptedObject) {
		scriptedObject.setProperty("log", log);
	}
	

}
