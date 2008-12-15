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
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;



/**
 * An entity that represents an Identity-Attribute Source by resourceAttribute
 *
 *   @author Asaf Shakarchi
 */
@Entity
@DiscriminatorValue("RESOURCE_ATTRIBUTE")
public class IdentityAttributeSourceByResourceAttribute extends IdentityAttributeSource implements Serializable {
    
    private ResourceAttribute resourceAttribute;
    private SyncByResourceAttributePolicy syncByResourceAttributePolicy;

    public void setSyncByResourceAttributePolicy(SyncByResourceAttributePolicy syncByResourceAttributePolicy) {
        this.syncByResourceAttributePolicy = syncByResourceAttributePolicy;
    }

    public enum SyncByResourceAttributePolicy{ SYNC_BY_ACCOUNT_ASSOCIATED_TO_USER,SYNC_BY_TARGET_SYSTEM_ACCOUNT_ID_ATTRIBUTE};
    
    /**
     * @param resourceAttribute The resourceAttribute to set.
     */
    public void setResourceAttribute(ResourceAttribute resourceAttribute) {
        this.resourceAttribute = resourceAttribute;
    }
    
    
    /**
     * @return Returns the resourceAttribute.
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="TS_ATTRIBUTE_ID", nullable=false)
    public ResourceAttribute getResourceAttribute() {
        
        return resourceAttribute;
    }
    
    @Column(name="SYNC_BY_TSA_POLICY", nullable=true)
    @Enumerated(EnumType.STRING)
    public SyncByResourceAttributePolicy getSyncByResourceAttributePolicy() {
        return syncByResourceAttributePolicy;
    }
    
    @Transient
    public String getType() {
    	return "RESOURCE_ATTRIBUTE";
    }
    
    @Transient
    public String getDisplayableType() {
    	return "Resource Attribute";
    }
}
