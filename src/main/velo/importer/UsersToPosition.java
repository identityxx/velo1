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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Shakarchi Asaf
 */

public class UsersToPosition {
    
    private String positionUniqueId;
    private String positionDisplayName;
    private Set<String> usersNames = new HashSet<String>();

    public UsersToPosition() {
    }
    
    public UsersToPosition(String positionUniqueId,Set<String> usersNames) {
        this.positionUniqueId = positionUniqueId;
        this.usersNames = usersNames;
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
	public Set<String> getUsersNames() {
		return usersNames;
	}

	/**
	 * @param rolesNames the rolesNames to set
	 */
	public void setRolesNames(Set<String> usersNames) {
		this.usersNames = usersNames;
	}
    

    public void addUser(String userName) {
    	getUsersNames().add(userName);
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
