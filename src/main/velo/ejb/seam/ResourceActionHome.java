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

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.Resource;
import velo.entity.ResourceAction;
import velo.entity.ResourceAction.InvokePhases;

@Name("resourceActionHome")
public class ResourceActionHome extends EntityHome<ResourceAction> {

	@In
	FacesMessages facesMessages;

	@In(value = "#{resourceHome.instance}")
	Resource resource;

	public void setActionDefinitionId(Long id) {
		setId(id);
	}

	public Long getActionDefinitionId() {
		return (Long) getId();
	}

	@Override
	protected ResourceAction createInstance() {
		ResourceAction resourceAction = new ResourceAction();
		return resourceAction;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ResourceAction getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public String persist() {
		// set the resource
		getInstance().setResource(resource);

		return super.persist();
	}

	@Factory("resourceActionInvokePhases")
	public InvokePhases[] getInvokePhases() {
		return InvokePhases.values();
	}
	
	
	/*
	@Factory("resourceActionInvokePhases")
	//public InvokePhases[] getInvokePhases() {
	public List<SelectItem> getInvokePhases() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		InvokePhases[] phases = InvokePhases.values();
		
		for (int i=0;i<phases.length;i++) {
			options.add(new SelectItem(phases[i].toString()));
		}
		

		//InvokePhases[] currOpt : InvokePhases.values();
		//return InvokePhases.values();
		
		return options;
	}
	*/
}
