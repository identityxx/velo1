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

// @!@clean
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.exceptions.FactoryException;
import velo.resource.operationControllers.ResourceOperationController;

/**
 * An entity that represents a resource type
 * 
 * @author Asaf Shakarchi
 */
@Table(name = "VL_RESOURCE_TYPE")
@Entity
@SequenceGenerator(name="ResourceTypeIdSeq",sequenceName="RESOURCE_TYPE_ID_SEQ")
@NamedQueries( {
		@NamedQuery(name = "resourceType.findById", query = "SELECT object(resourceType) FROM ResourceType resourceType WHERE resourceType.resourceTypeId = :id"),
		@NamedQuery(name = "resourceType.findByUniqueName", query = "SELECT object(resourceType) FROM ResourceType resourceType WHERE resourceType.uniqueName = :uniqueName"),
		@NamedQuery(name = "resourceType.findAll", query = "SELECT rt FROM ResourceType rt") })
public class ResourceType extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;

	private static Logger log = Logger.getLogger(ResourceType.class.getName());

	public enum ResourceControllerType {
    	SPML_GENERIC, SPML_ACCESS_GROUPS
	}
	
	private Long resourceTypeId;
	private String uniqueName;
	private boolean isScripted;
	private String configurationTemplate;
	private String resourceControllerClassName;
	private ResourceControllerType resourceControllerType;
	private Set<ResourceTypeAttribute> resourceTypeAttributes = new HashSet<ResourceTypeAttribute>();
	private Set<ResourceTypeOperation> supportedOperations = new HashSet<ResourceTypeOperation>();
	private boolean gatewayRequired;

	
	
	
	public void setResourceTypeId(Long resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	// GF@Id
	// @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
	// "TARGET_SYSTEM_TYPE_SEQ")
	// GF@SequenceGenerator(name="IDM_TARGET_SYSTEM_TYPE_GEN",sequenceName="IDM_TARGET_SYSTEM_TYPE_SEQ",
	// allocationSize=1)
	// GF@GeneratedValue(strategy = GenerationType.SEQUENCE,
	// generator="IDM_TARGET_SYSTEM_TYPE_GEN")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceTypeIdSeq")
	//@GeneratedValue
	// JB
	@Column(name = "RESOURCE_TYPE_ID")
	public Long getResourceTypeId() {
		return resourceTypeId;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Column(name = "UNIQUE_NAME", nullable = false, unique = true)
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param isScripted
	 *            The isScripted to set.
	 */
	public void setScripted(boolean isScripted) {
		this.isScripted = isScripted;
	}


	/**
	 * @return Returns the isScripted.
	 */
	@Column(name = "IS_SCRIPTED", nullable = false)
	public boolean isScripted() {
		return isScripted;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof ResourceType))
			return false;
		ResourceType ent = (ResourceType) obj;
		if (this.resourceTypeId.equals(ent.resourceTypeId))
			return true;
		return false;
	}

	/**
	 * @return the configurationTemplate
	 */
	@Column(name = "CONFIGURATION_TEMPLATE", nullable = false)
	@Lob
	public String getConfigurationTemplate() {
		return configurationTemplate;
	}

	/**
	 * @param configurationTemplate
	 *            the configurationTemplate to set
	 */
	public void setConfigurationTemplate(String configurationTemplate) {
		this.configurationTemplate = configurationTemplate;
	}

	/**
	 * @return the resourceTypeAttributes
	 */
	@OneToMany(mappedBy = "resourceType", fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.REFRESH })
	@OrderBy("displayName")
	public Set<ResourceTypeAttribute> getResourceTypeAttributes() {
		return resourceTypeAttributes;
	}

	/**
	 * @param resourceTypeAttributes
	 *            the resourceTypeAttributes to set
	 */
	public void setResourceTypeAttributes(
			Set<ResourceTypeAttribute> resourceTypeAttributes) {
		this.resourceTypeAttributes = resourceTypeAttributes;
	}

	/**
	 * @return the supportedOperations
	 */
	//@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, targetEntity = velo.entity.ResourceTypeOperation.class)
	//@JoinTable(name = "VL_RES_TYPES_TO_RES_OPRTN_DEFS", joinColumns = @JoinColumn(name = "RESOURCE_TYPE_ID"), inverseJoinColumns = @JoinColumn(name = "RES_TYOE_OPERATION_ID"))
	@OneToMany(mappedBy = "resourceType", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public Set<ResourceTypeOperation> getSupportedOperations() {
		return supportedOperations;
	}

	/**
	 * @param supportedOperations
	 *            the supportedOperations to set
	 */
	public void setSupportedOperations(
			Set<ResourceTypeOperation> supportedOperations) {
		this.supportedOperations = supportedOperations;
	}
	
	/**
	 * @return the resourceControllerClassName
	 */
	@Column(name = "RESOURCE_CONTROLLER_CLASS_NAME", nullable = false)
	public String getResourceControllerClassName() {
		return resourceControllerClassName;
	}

	/**
	 * @param resourceControllerClassName the resourceControllerClassName to set
	 */
	public void setResourceControllerClassName(String resourceControllerClassName) {
		this.resourceControllerClassName = resourceControllerClassName;
	}
	
	
	/**
	 * @return the gatewayRequired
	 */
	@Column(name = "GATEWAY_REQUIRED", nullable = false)
	public boolean isGatewayRequired() {
		return gatewayRequired;
	}

	/**
	 * @param gatewayRequired the gatewayRequired to set
	 */
	public void setGatewayRequired(boolean gatewayRequired) {
		this.gatewayRequired = gatewayRequired;
	}
	
	/**
	 * @return the resourceControllerType
	 */
	@Column(name = "RESOURCE_CONTROLLER_TYPE")
	@Enumerated(EnumType.STRING)
	public ResourceControllerType getResourceControllerType() {
		return resourceControllerType;
	}

	/**
	 * @param resourceControllerType the resourceControllerType to set
	 */
	public void setResourceControllerType(
			ResourceControllerType resourceControllerType) {
		this.resourceControllerType = resourceControllerType;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Transient
	public Set<ResourceTypeAttribute> getOverriddenResourceTypeAttributes() {
		Set<ResourceTypeAttribute> attrs = new HashSet<ResourceTypeAttribute>();
		for (ResourceTypeAttribute currAttr : getResourceTypeAttributes()) {
			if (currAttr.isOverridden()) {
				attrs.add(currAttr);
			}
		}

		return attrs;
	}

	@Transient
	public Set<ResourceTypeAttribute> getNotOverriddenResourceTypeAttributes() {
		log.trace("Getting all resoruce type attributes that were not overridden yet for resource type '" + getUniqueName() + "'");
		Set<ResourceTypeAttribute> attrs = new HashSet<ResourceTypeAttribute>();
		for (ResourceTypeAttribute currAttr : getResourceTypeAttributes()) {
			if (!currAttr.isOverridden()) {
				//found a default shipped attribute, make sure it did not get overridden
				if (!isAttributeOverriddenInTypeLevel(currAttr)) {
					log.trace("Default shipped attribute named '" + currAttr.getDisplayName() + "' is NOT overridden, adding...");
					attrs.add(currAttr);
				} else {
					log.trace("Default shipped attribute named '" + currAttr.getDisplayName() + "' IS overridden, skipping....");
				}
			}
		}

		return attrs;
	}

	@Transient
	public boolean isAttributeOverriddenInTypeLevel(ResourceTypeAttribute rta) {
		// Make sure the specified rta's resourceType is the same, otherwise
		// return false as there's no reason to compare
		if (!rta.getResourceType().equals(rta.getResourceType())) {
			log
					.warn("Cannot determine whether RTA named '"
							+ rta.getDisplayName()
							+ "' is overridden since the attribute is associated with another resource type!");
			return false;
		}

		for (ResourceTypeAttribute currRTA : getOverriddenResourceTypeAttributes()) {
			if (currRTA.getUniqueName().equals(rta.getUniqueName())) {
				return true;
			}
		}

		return false;
	}
	
	@Transient
	public boolean isOperationSupported(String uniqueName) {
		for (ResourceTypeOperation operation : getSupportedOperations()) {
			if (operation.getResourceGlobalOperation().getUniqueName().equals(uniqueName)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Transient
	public ResourceOperationController factoryResourceOperationsController() {
		try {
			log.trace("Factoring resource operation controller...");
			return (ResourceOperationController)velo.patterns.Factory.factoryInstance(getResourceControllerClassName());
		} catch (FactoryException e) {
			log.error("Failed to factory a resource operation controller: " + e.toString());
			return null;
		}
	}
	
	public ResourceTypeOperation findResourceTypeOperation(String uniqueName) {
		for (ResourceTypeOperation currRTO : getSupportedOperations()) {
			if (currRTO.getResourceGlobalOperation().getUniqueName().equals(uniqueName)) {
				return currRTO;
			}
		}
		
		return null;
	}

	
}
