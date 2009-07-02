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
package velo.ejb.seam;

import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceTypeAttribute;
import velo.entity.Attribute.AttributeDataTypes;
import velo.entity.ResourceAttributeBase.SourceTypes;

@Name("resourceAttributeHome")
public class ResourceAttributeHome extends EntityHome<ResourceAttribute> {

	@In
	FacesMessages facesMessages;
	
	//For override
	@In(value="#{resourceTypeAttributeHome.instance}", required=false)
	ResourceTypeAttribute resourceTypeAttribute;
	@In(value="#{resourceHome.instance}", required=false)
	Resource resource;

	public void setResourceAttributeId(Long id) {
		setId(id);
	}

	public Long getResourceAttributeId() {
		return (Long) getId();
	}

	@Override
	protected ResourceAttribute createInstance() {
		ResourceAttribute resourceAttribute = new ResourceAttribute();
		return resourceAttribute;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ResourceAttribute getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	
	
	
	@Begin(nested=true)
	public String override() {
		
		//Make sure we got the resource/resourceTypeAttirubte to be overridden
		if ( (resourceTypeAttribute.getResourceAttributeId() == null) || (resource.getResourceId() == null) ) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Resource or ResourceTypeAttribute instances are not bound to session.");
			return null;
		}

		
		if (resource.isResourceAttributeExistsInResourceLevel(resourceTypeAttribute.getUniqueName())) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "This attribute is already overrided!");
			return null;
		}
		else {
			ResourceAttribute ra = ResourceAttribute.factoryVirtually(resourceTypeAttribute, resource); 
			ra.setOverridden(true);
		
			setInstance(ra);
		
			return "/admin/ResourceAttributeEdit.xhtml";
		}
	}
	
	@End
	public String persistOverrideResourceAttribute() {
		getEntityManager().persist(getInstance());
		getEntityManager().flush();
		facesMessages.add("Successfully overrided resource type attribute");
		
		return "persisted";
	}
	
	
	public String persist() {
		//if the attribute was flagged as 'accountId', make sure there are no other attributes that were flagged already as accountId
		if (getInstance().isAccountId()) {
			boolean found = false;
			ResourceAttribute accIdRA = null;
			
			for (ResourceAttribute ra : getInstance().getResource().getAttributes()) {
				if (ra.isAccountId()) {
					accIdRA = ra;
					found = true;
					break;
				}
			}
			
			
			if (found) {
				getFacesMessages().add("Resource Attribute named '#0' was already flagged as an Account ID",accIdRA.getDisplayName());
				return null;
			} 
		}
		
		return super.persist();
	}
	
	@Factory("resourceAttributeSourceTypes")
	public SourceTypes[] getSourceTypes() {
		return SourceTypes.values();
	}
	
	@Factory("resourceAttributeDataTypes")
	public AttributeDataTypes[] getResourceattributeDataTypes() {
		return AttributeDataTypes.values();
	}	
}