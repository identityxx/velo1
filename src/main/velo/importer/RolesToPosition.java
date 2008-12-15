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
package velo.importer;

/**
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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Shakarchi Asaf
 */

public class RolesToPosition {
    
    private String positionUniqueId;
    
    private String positionDisplayName;
    
    private Set<String> rolesNames = new HashSet<String>();

    public RolesToPosition() {
    }
    
    public RolesToPosition(String positionUniqueId,Set<String> rolesNames) {
        this.positionUniqueId = positionUniqueId;
        this.rolesNames = rolesNames;
    }

	/**
	 * @return the positionUniqueId
	 */
	public String getPositionUniqueId() {
		return positionUniqueId;
	}

	/**
	 * @param positionUniqueId the positionUniqueId to set
	 */
	public void setPositionUniqueId(String positionUniqueId) {
		this.positionUniqueId = positionUniqueId;
	}

	/**
	 * @return the rolesNames
	 */
	public Set<String> getRolesNames() {
		return rolesNames;
	}

	/**
	 * @param rolesNames the rolesNames to set
	 */
	public void setRolesNames(Set<String> rolesNames) {
		this.rolesNames = rolesNames;
	}
    

    public void addRole(String roleName) {
    	getRolesNames().add(roleName);
    }

	/**
	 * @return the positionDisplayName
	 */
	public String getPositionDisplayName() {
		return positionDisplayName;
	}

	/**
	 * @param positionDisplayName the positionDisplayName to set
	 */
	public void setPositionDisplayName(String positionDisplayName) {
		this.positionDisplayName = positionDisplayName;
	}
    
}
