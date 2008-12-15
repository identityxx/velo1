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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * An entity that represents a skeletal resource account
 * 
 * @author Asaf Shakarchi
 */
@MappedSuperclass
public abstract class AccountSkeletal extends BaseEntity implements
		Serializable, Cloneable {
	private static final long serialVersionUID = 1987305452306161213L;

	/**
	 * The resource the account resides in
	 */
	private Resource resource;

	private Set<ResourceGroup> resourceGroups;

	/**
	 * The account name
	 */
	private String name;

	/**
	 * Set the target system entity the account is attached to
	 * 
	 * @param resource
	 *            resource entity
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * G÷à the target system entity the account is attached to
	 * 
	 * @return resource entity
	 */
	@ManyToOne
	@JoinColumn(name = "RESOURCE_ID", nullable = true)
	public Resource getResource() {
		return resource;
	}

	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = velo.entity.ResourceGroup.class)
	@JoinTable(
	// name="ACCOUNTS_TO_TARGET_SYSTEM_GROUP",
	name = "VL_ACCOUNTS_TO_RESOURCE_GRPS", joinColumns = @JoinColumn(name = "ACCOUNT_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
	public Set<ResourceGroup> getResourceGroups() {
		return resourceGroups;
	}

	public void setResourceGroups(Set<ResourceGroup> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	/**
	 * Set the name of the account
	 * 
	 * @param name
	 *            The name of the account as a string to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the account
	 * 
	 * @return The name of the account as a string to get
	 */
	@Column(name = "NAME", nullable = false, unique = false)
	public String getName() {
		return name;
	}

	// @Transient
	/*
	 * This is confusing, since an account does not really have attributes, They
	 * are fetched from the mapped User attribute or/and by converter
	 */
	/*
	 * public void setAccountAttributes(Collection<Attribute> attributes) {
	 * this.attributes = attributes; }
	 */
}
