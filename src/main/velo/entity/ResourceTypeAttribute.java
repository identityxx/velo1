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

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
//@!@clean
@Entity
@DiscriminatorValue("RESOURCE_TYPE")
@NamedQueries( 
	{
		@NamedQuery(name = "resourceTypeAttribute.findByUniqueName", query = "SELECT object(attr) FROM ResourceTypeAttribute AS attr WHERE attr.uniqueName = :uniqueName AND attr.resourceType = :resourceType")
	}
)
public class ResourceTypeAttribute extends ResourceAttributeBase implements Serializable,Cloneable {
	private ResourceType resourceType;
	
	public ResourceTypeAttribute() {
		
	}

	public ResourceTypeAttribute(String uniqueName, String displayName, String description, AttributeDataTypes dataType, boolean required, boolean managed, int minLength, int maxLength, int minValues, int maxValues) {
		super(uniqueName, displayName, description, dataType, required, managed, minLength, maxLength, minValues, maxValues);
	}
	
	/**
	 * @return the resourceType
	 */
	//@ManyToOne(optional = false)
	@ManyToOne(optional = true) //must be true otherwise mysql set it as 'not null' which is not true.
	//removed the 'nullable = false' because of oracle DB
	@JoinColumn(name = "RESOURCE_TYPE_ID")
	public ResourceType getResourceType() {
		return resourceType;
	}

	
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	
	public ResourceTypeAttribute clone() throws CloneNotSupportedException {
		ResourceTypeAttribute clonedEntity = (ResourceTypeAttribute) super.clone();
		return clonedEntity;
	}
	
	public ResourceTypeAttribute override() throws CloneNotSupportedException {
		ResourceTypeAttribute clonedEntity = (ResourceTypeAttribute) super.clone();
		clonedEntity.setResourceAttributeId(null);
		//Clean collections, otherwise Hibernate throws in persist time 
		//Caused by: org.hibernate.HibernateException: Found shared references to a collection: velo.entity.ResourceAttribute.attributeActionRules 
		clonedEntity.setActionRules(null);
		clonedEntity.setOverridden(true);
		
		return clonedEntity;
	}
}
