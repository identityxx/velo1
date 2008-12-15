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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.NotNull;

@MappedSuperclass
public class ExtBasicEntity extends BaseEntity {
	private String uniqueName;
	private String displayName;
	private String description;
	
	
	
	public ExtBasicEntity() {
		
	}
	
	public ExtBasicEntity(String uniqueName, String displayName, String description) {
		setUniqueName(uniqueName);
		setDisplayName(displayName);
		setDescription(description);
	}
	
	
	/**
	 * @return the uniqueName
	 */
	@Column(name="UNIQUE_NAME", nullable=false, unique=true)
    @NotNull
	public String getUniqueName() {
		return uniqueName;
	}
	/**
	 * @param uniqueName the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	/**
	 * @return the displayName
	 */
	@Column(name="DISPLAY_NAME", nullable=false)
	@NotNull
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION")
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
