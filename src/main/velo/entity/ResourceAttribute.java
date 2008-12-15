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
//@!@clean
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;
import velo.exceptions.AttributeSetValueException;

/**
 * An entity that represents a target system attribute.
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("resourceAttribute")
@Entity
@DiscriminatorValue("RESOURCE_INSTANCE")

@NamedQueries({
	@NamedQuery(name = "resrouceAttribute.findByUniqueName", query = "SELECT ra FROM ResourceAttribute AS ra WHERE ra.uniqueName = :uniqueName AND ra.resource = :resource")
})
public class ResourceAttribute extends ResourceAttributeBase implements Serializable {
	private static transient Logger log = Logger.getLogger(ResourceAttribute.class.getName());
    private static final long serialVersionUID = 1L;
    private Set<RoleResourceAttribute> roleResourceAttributes = new HashSet<RoleResourceAttribute>();
    
    public ResourceAttribute() {
    	
    }
    
    public ResourceAttribute(Resource resource, String uniqueName, String displayName, String description, AttributeDataTypes dataType, boolean required, boolean managed, int minLength, int maxLength, int minValues, int maxValues) {
		super(uniqueName, displayName, description, dataType, required, managed, minLength, maxLength, minValues, maxValues);
		setResource(resource);
	}
    
    
    /**
     * The resource owned of this target system attribute
     */
    private Resource resource;
    

    /**
     * Set the resource entity the resourceAttribute is related to
     * @param resource A resource entity to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    /**
     * Get the resource entity the resourceAttribute is related to
     * @return A resource entity
     */
    //@ManyToOne(optional=false)
    @ManyToOne
    @JoinColumn(name="RESOURCE_ID")
    public Resource getResource() {
        return resource;
    }
    
    /**
	 * @return the roleResourceAttributes
	 */
    @OneToMany(mappedBy="primaryKey.resourceAttribute")
	public Set<RoleResourceAttribute> getRoleResourceAttributes() {
		return roleResourceAttributes;
	}

	/**
	 * @param roleResourceAttributes the roleResourceAttributes to set
	 */
	public void setRoleResourceAttributes(
			Set<RoleResourceAttribute> roleResourceAttributes) {
		this.roleResourceAttributes = roleResourceAttributes;
	}
    
    
    
    
    
    
    
    
    
    
    public static ResourceAttribute factory(Resource resource) {
    	ResourceAttribute ra = new ResourceAttribute();
    	ra.setCreationDate(new Date());
    	ra.setResource(resource);
    	
    	return ra;
    }
    
    public static ResourceAttribute factoryVirtually(ResourceAttributeBase rab, Resource resource) {
    	//TODO CLONE!!! FUCK IT
    	//TODO Might be better way? maybe with casts?
    	//Begin copy
    	ResourceAttribute ra = new ResourceAttribute(resource, rab.getUniqueName(), rab.getDisplayName(), rab.getDescription(), rab.getDataType(), rab.isRequired(), rab.isManaged(), rab.getMinLength(), rab.getMaxLength(), rab.getMinValues(), rab.getMaxValues());
    	ra.setAccountId(rab.isAccountId());
    	ra.setVirtual(true);
    	ra.setUniqueName(rab.getUniqueName());
    	
    	//TODO but why? OVERRIDE SHOULD BE OK HERE GOD HEAVEN
    	ra.setSourceType(rab.getSourceType());
    	
    	return ra;
    }

    
    //Used by the account.getVirtualAttribute method
    public void importValues(UserIdentityAttribute uia) {
    	//for (UserIdentityAttributeValue currUIAV : uia.getValues()) {
    	for (UserIdentityAttributeValue currUIAV : uia.getValues()) {
    		try {
				addValue(currUIAV.getValue());
			} catch (AttributeSetValueException e) {
				log.warn("Skipping importing value from UserIdentityAttribute of user '" + uia.getUser().getName() + "' associated to IdentityAttribute named '" + uia.getIdentityAttribute().getDisplayName() + "': " + e.toString());
				continue;
			}
        }
    }
    
    
    
    //Transient
    private boolean virtual = false;

	/**
	 * @return the virtual
	 */
    @Transient
	public boolean isVirtual() {
		return virtual;
	}

	/**
	 * @param virtual the virtual to set
	 */
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	
	
	@Transient
	public String getUniqueNameInRightCase() {
		if (getResource().isCaseSensitive()) {
			return getUniqueName();
		} else {
			return getUniqueName().toUpperCase();
		}
	}
	
	
	@Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof ResourceAttribute))
            return false;
        ResourceAttribute ent = (ResourceAttribute) obj;
        if (this.getResourceAttributeId().equals(ent.getResourceAttributeId()))
            return true;
        return false;
    }
    
}