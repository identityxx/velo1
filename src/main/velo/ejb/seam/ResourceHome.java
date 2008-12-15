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

import java.io.IOException;

import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.Resource;

@Name("resourceHome")
public class ResourceHome extends EntityHome<Resource> {

	@In
	FacesMessages facesMessages;

	public void setResourceId(Long id) {
		setId(id);
	}

	public Long getResourceId() {
		return (Long) getId();
	}

	@Override
	protected Resource createInstance() {
		Resource resource = new Resource();
		return resource;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}
	
	
	public String persist() {
		//copy the configuration from the resource type
		getInstance().setConfiguration(getInstance().getResourceType().getConfigurationTemplate());
		super.persist();
		
		try {
			//create a default target folder with a conf file
			getInstance().createResourceWorkspaceFolder();
		} catch (IOException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.toString());
		}
		
		return "/admin/ResourceCustomConfiguration.xhtml";
	}
	
	
	public String remove() {
		//check users dependencies
		if (getInstance().getUsers().size() > 0) {
			getFacesMessages().add(FacesMessage.SEVERITY_ERROR, "Resource could not be deleted as there are existance users the resource is a source of, please delete the users first.");
			return null;
		}
		
		//all other associates will be deleted automatically!
		return super.remove();
	}


	public Resource getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
}
