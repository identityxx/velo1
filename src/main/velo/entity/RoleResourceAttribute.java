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

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import velo.contexts.OperationContext;

@Entity
@Table(name="VL_ROLE_RESOURCE_ATTRIBUTE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ROLE_RESOURCE_ATTR_TYPE")
//@SequenceGenerator(name="RoleResourceAttrIdSeq",sequenceName="ROLE_RESOURCE_ATTR_ID_SEQ")
@AssociationOverrides({
@AssociationOverride(name="primaryKey.role", joinColumns = @JoinColumn(name="ROLE_ID")),
@AssociationOverride(name="primaryKey.resourceAttribute", joinColumns = @JoinColumn(name="RESOURCE_ATTRIBUTE_ID"))
})
public abstract class RoleResourceAttribute extends BaseEntity {
	public enum RoleResourceAttributeBehaviour {
		OVERRIDE_EXISTING_VALUE
	}
	
	private RoleResourceAttributePK primaryKey = new RoleResourceAttributePK();
	private String description;
	private int priority;
	
	
	public RoleResourceAttribute() {
		
	}

	public RoleResourceAttribute(Role role, ResourceAttribute resourceAttribute) {
		setCreationDate(new Date());
		setRole(role);
		setResourceAttribute(resourceAttribute);
	}
	
	
	
	/**
	 * @return the primaryKey
	 */
	@Id
	public RoleResourceAttributePK getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(RoleResourceAttributePK primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
	public void setRole(Role role) {
		primaryKey.setRole(role);
	}

	@Transient
	public Role getRole() {
		return primaryKey.getRole();
	}

	public void setResourceAttribute(ResourceAttribute resourceAttribute) {
		primaryKey.setResourceAttribute(resourceAttribute);
	}

	@Transient
	public ResourceAttribute getResourceAttribute() {
		return primaryKey.getResourceAttribute();
	}
	
	
	/**
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", nullable = true)
	//@NotNull
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the priority
	 */
	@Column(name = "PRIORITY", nullable = false)
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	
	//COMMENT: maybe there's a better way to return the @DiscriminatorValue
	@Transient
	public abstract String getType();
	
	public abstract ResourceAttribute calculateAttributeResult(OperationContext context);
	
	@Override
    public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof RoleResourceAttribute))
			return false;
		RoleResourceAttribute ent = (RoleResourceAttribute) obj;
		if (this.primaryKey.equals(ent.primaryKey))
			return true;
		return false;
    }
}
