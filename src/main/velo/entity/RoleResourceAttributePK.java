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

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class RoleResourceAttributePK implements Serializable {
    private ResourceAttribute resourceAttribute;
    private Role role;

	/**
	 * @return the resourceAttribute
	 */
    @ManyToOne
	public ResourceAttribute getResourceAttribute() {
		return resourceAttribute;
	}

	/**
	 * @param resourceAttribute the resourceAttribute to set
	 */
	public void setResourceAttribute(ResourceAttribute resourceAttribute) {
		this.resourceAttribute = resourceAttribute;
	}

	/**
	 * @return the role
	 */
	@ManyToOne
	public Role getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof RoleResourceAttributePK))
			return false;
		RoleResourceAttributePK ent = (RoleResourceAttributePK) obj;
		if ( (this.getRole().equals(ent.getRole())) && (this.getResourceAttribute().equals(ent.getResourceAttribute()))) 
			return true;
		return false;
	}
}
