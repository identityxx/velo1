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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="VL_RESOURCE_ATTRIBUTE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@SequenceGenerator(name="ResourceAttributeIdSeq",sequenceName="RESOURCE_ATTRIBUTE_ID_SEQ")
@DiscriminatorColumn(name="OVERRIDE_LEVEL")
public abstract class ResourceAttributeBase extends Attribute implements Serializable {
	private Long resourceAttributeId;
	
	private boolean passwordAttribute = false;

	private boolean accountId = false;

	private boolean synced = false;

	private boolean storeLocally = false;

	//init here, otherwise 'getVirtualAttribute' will cause NPE
	private Set<ResourceAttributeActionRule> actionRules = new HashSet<ResourceAttributeActionRule>();

	private boolean overridden = false;
	
	private Integer priority = 0;
	
	private boolean persistence = false;
	
	
	
	//sources
	public enum SourceTypes { 
		IDENTITY_ATTRIBUTE,USER_NAME,USER_PASSWORD,NONE
	}

	private SourceTypes sourceType;
	private IdentityAttribute identityAttribute;
	


	public ResourceAttributeBase() {
		
	}
	
	public ResourceAttributeBase(String uniqueName, String displayName, String description, AttributeDataTypes dataType, boolean required, boolean managed, int minLength, int maxLength, int minValues, int maxValues) {
		super(uniqueName, displayName, description, dataType, required, managed, minLength, maxLength, minValues, maxValues);
	}
	
	/**
	 * @return the resourceAttributeId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceAttributeIdSeq")
	//@GeneratedValue
    @Column(name="RESOURCE_ATTRIBUTE_ID")
	public Long getResourceAttributeId() {
		return resourceAttributeId;
	}

	/**
	 * @param resourceAttributeId the resourceAttributeId to set
	 */
	public void setResourceAttributeId(Long resourceAttributeId) {
		this.resourceAttributeId = resourceAttributeId;
	}
	
	/**
	 * @return the passwordAttribute
	 */
	@Column(name = "PASSWORD_ATTRIBUTE", nullable=false)
	public boolean isPasswordAttribute() {
		return passwordAttribute;
	}

	/**
	 * @param passwordAttribute
	 *            the passwordAttribute to set
	 */
	public void setPasswordAttribute(boolean passwordAttribute) {
		this.passwordAttribute = passwordAttribute;
	}

	/**
	 * @return the accountId
	 */
	@Column(name = "ACCOUNT_ID", nullable=false)
	public boolean isAccountId() {
		return accountId;
	}

	/**
	 * @param accountId
	 *            the accountId to set
	 */
	public void setAccountId(boolean accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return the synced
	 */
	@Column(name = "SYNCED", nullable=false)
	public boolean isSynced() {
		return synced;
	}

	/**
	 * @param synced
	 *            the synced to set
	 */
	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	/**
	 * @return the storeLocally
	 */
	@Column(name = "STORE_LOCALLY", nullable=false)
	public boolean isStoreLocally() {
		return storeLocally;
	}

	/**
	 * @param storeLocally
	 *            the storeLocally to set
	 */
	public void setStoreLocally(boolean storeLocally) {
		this.storeLocally = storeLocally;
	}


	
	//sources
	
	/**
	 * @return the resourceType
	 */
    public void setIdentityAttribute(IdentityAttribute identityAttribute) {
        this.identityAttribute = identityAttribute;
    }
    
    
    @ManyToOne
    @JoinColumn(name="IDENTITY_ATTRIBUTE_ID", nullable=true, unique=false)
    public IdentityAttribute getIdentityAttribute() {
        return identityAttribute;
    }
    
    
    /**
	 * @return the actionRules
	 */
    /*
    @ManyToMany(
    	cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, targetEntity = velo.entity.AttributeActionRule.class)
    	@JoinTable(name = "VL_ATTR_ACTN_RULES_TO_RES_ATTR", joinColumns = @JoinColumn(name = "RESOURCE_ATTRIBUTE_ID"), inverseJoinColumns = @JoinColumn(name = "ACTION_ID"))
    */
    @OneToMany(mappedBy = "resourceAttribute", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public Set<ResourceAttributeActionRule> getActionRules() {
		return actionRules;
	}

	/**
	 * @param actionRules the actionRules to set
	 */
	public void setActionRules(Set<ResourceAttributeActionRule> actionRules) {
		this.actionRules = actionRules;
	}
	
	/**
	 * @return the sourceType
	 */
    @Column(name = "SOURCE_TYPE")
	@Enumerated(EnumType.STRING)
	public SourceTypes getSourceType() {
		return sourceType;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceTypes sourceType) {
		this.sourceType = sourceType;
	}

	
	
	
	
	
    
	/**
	 * @return the overridden
	 */
    @Column(name = "OVERRIDDEN", nullable=false)
	public boolean isOverridden() {
		return overridden;
	}

	/**
	 * @param overridden the overridden to set
	 */
	public void setOverridden(boolean overridden) {
		this.overridden = overridden;
	}
	
	
	/**
	 * @return the priority
	 */
	@Column(name = "PRIORITY", nullable=false)
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	
	@Column(name = "PERSISTENCE", nullable=false)
	public boolean isPersistence() {
		return persistence;
	}

	public void setPersistence(boolean persistence) {
		this.persistence = persistence;
	}

	@Transient
    @Deprecated
    public boolean isMappedDirectly() {
    	return true;
    }
    
    @Transient
    @Deprecated
    public boolean isMapDirectly() {
    	return true;
    }
    
    @Transient
    @Deprecated
    public String getConverterClassName() {
    	return "asdfsadf";
    }
    
    @Transient
    @Deprecated
    public String getConverterFileName() {
    	return "asdfsadF";
    }

}
