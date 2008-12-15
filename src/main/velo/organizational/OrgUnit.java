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
package velo.organizational;

import java.util.Map;

/**
 * A class that represents an Organizational Unit
 * 
 * @author Asaf Shakarchi
 */
public abstract class OrgUnit {
	
	private String uniqueId;
	private String name;
	private String description;
	private Map<String,OrgUnit> childOrgUnits;
	private OrgUnit parent;
	private boolean isLoaded;
	
	public abstract void construct();

	/**
	 * @return Returns the childOrgUnits.
	 */
	public Map<String,OrgUnit> getChildOrgUnits() {
		return childOrgUnits;
	}

	/**
	 * @param childOrgUnits The childOrgUnits to set.
	 */
	public void setChildOrgUnits(Map<String,OrgUnit> childOrgUnits) {
		this.childOrgUnits = childOrgUnits;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the parent.
	 */
	public OrgUnit getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(OrgUnit parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the uniqueId.
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId The uniqueId to set.
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @param isLoaded The isLoaded to set.
	 */
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	/**
	 * @return Returns the isLoaded.
	 */
	public boolean isLoaded() {
		return isLoaded;
	}
	
}
