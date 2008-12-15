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

//@!@clean
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a Password Policy Folder
 * 
 * @author Asaf Shakarchi
 */
@Entity
@Table(name = "VL_USER_CONTAINER")
@SequenceGenerator(name="UserContainerIdSeq",sequenceName="USER_CONTAINER_ID_SEQ")
@Name("userContainer")
// Seam name
public class UserContainer extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;

	/**
	 * ID of the entity
	 */
	private Long userContainerId;

	/**
	 * The unique name of the entity
	 */
	private String uniqueName;

	/**
	 * The display name of the entity
	 */
	private String displayName;

	/**
	 * The description of the entity
	 */
	private String description;

	private List<User> users = new ArrayList<User>();
	
	private boolean immuned;

	/**
	 * Get the entity ID
	 * 
	 * @return Entity ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="UserContainerIdSeq")
	//@GeneratedValue
	// JB
	@Column(name = "USER_CONTAINER_ID")
	public Long getUserContainerId() {
		return userContainerId;
	}

	/**
	 * Set entity ID
	 * 
	 * @param UserContainerId
	 *            Entity's ID to set
	 */
	public void setUserContainerId(Long userContainerId) {
		this.userContainerId = userContainerId;
	}

	/**
	 * @param uniqueName
	 *            The uniqueName to set.
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * @return Returns the name.
	 */
	@Column(name = "UNIQUE_NAME", nullable = false, unique = true)
	@Length(min = 3, max = 40)
	@NotNull(message = "Unique Name is a Must.")
	// seam
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the displayName
	 */
	@Column(name = "DISPLAY_NAME", nullable = false)
	@Length(min = 3, max = 40)
	@NotNull
	// seam
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}

	/**
	 * @return the users
	 */
	@OneToMany(mappedBy = "userContainer", fetch = FetchType.LAZY)
	public List<User> getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            The users to set.
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof UserContainer))
			return false;
		UserContainer ent = (UserContainer) obj;
		if (this.userContainerId.equals(ent.userContainerId))
			return true;
		return false;
	}
}
