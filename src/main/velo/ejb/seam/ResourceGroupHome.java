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

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.ResourceGroup;

@Name("resourceGroupHome")
public class ResourceGroupHome extends EntityHome<ResourceGroup> {

	@In
	FacesMessages facesMessages;
	
	public void setResourceGroupId(Long id) {
		setId(id);
	}

	public Long getResourceGroupId() {
		return (Long) getId();
	}

	@Override
	protected ResourceGroup createInstance() {
		ResourceGroup resourceGroup = new ResourceGroup();
		return resourceGroup;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ResourceGroup getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
}
