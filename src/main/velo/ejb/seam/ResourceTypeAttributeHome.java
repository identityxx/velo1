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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.ResourceTypeAttribute;

@Name("resourceTypeAttributeHome")
public class ResourceTypeAttributeHome extends EntityHome<ResourceTypeAttribute> {

	@In
	FacesMessages facesMessages;
	
	
	public void setResourceAttributeId(Long id) {
		setId(id);
	}

	public Long getResourceAttributeId() {
		return (Long) getId();
	}

	@Override
	protected ResourceTypeAttribute createInstance() {
		ResourceTypeAttribute resourceTypeAttribute = new ResourceTypeAttribute();
		return resourceTypeAttribute;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}
	
	public ResourceTypeAttribute getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	@Begin(nested=true)
	public String override() {
		if (getInstance().getResourceType().isAttributeOverriddenInTypeLevel(getInstance())) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "This attribute is already overrided!");
			return null;
		}
		
		try {
			setInstance(getInstance().override());
		} catch (CloneNotSupportedException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Cannot perform override resource type attribute: " + e.toString());
		}
		
		return "/admin/ResourceTypeAttributeEdit.xhtml";
	}
	
	@End
	public String persistOverrideResourceTypeAttribute() {
		getEntityManager().persist(getInstance());
		getEntityManager().flush();
		facesMessages.add("Successfully overrided target system type attribute");
		
		return "persisted";
	}
}
