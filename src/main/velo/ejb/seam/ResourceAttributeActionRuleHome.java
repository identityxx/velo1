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

import velo.entity.ResourceAttributeActionRule;
import velo.entity.ResourceAttributeBase;

@Name("resourceAttributeActionRuleHome")
public class ResourceAttributeActionRuleHome extends EntityHome<ResourceAttributeActionRule> {

	@In
	FacesMessages facesMessages;

	@In(value = "#{resourceAttributeHome.instance}")
	ResourceAttributeBase resourceAttribute;

	public void setActionDefinitionId(Long id) {
		setId(id);
	}

	public Long getActionDefinitionId() {
		return (Long) getId();
	}

	@Override
	protected ResourceAttributeActionRule createInstance() {
		ResourceAttributeActionRule resourceAttributeActionRule = new ResourceAttributeActionRule();
		return resourceAttributeActionRule;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public ResourceAttributeActionRule getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public String persist() {
		// set the resource attribute before persisting
		System.out.println(resourceAttribute.getResourceAttributeId());
		getInstance().setResourceAttribute(resourceAttribute);

		return super.persist();
	}
}
