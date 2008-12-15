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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.ValidationException;
/**
 * An entity that represents a Postion
 * 
 * @author Asaf Shakarchi
 */

// Seam annotations
@Name("position")
@Table(name = "VL_POSITION")
@Entity
@SequenceGenerator(name = "PositionIdSeq", sequenceName = "POSITION_ID_SEQ")
@NamedQueries
	( {
		@NamedQuery(name = "position.findByName", query = "SELECT object(position) FROM Position position WHERE position.uniqueIdentifier = :uniqueIdentifier"),
		@NamedQuery(name = "position.loadAll", query = "SELECT object(position) FROM Position position")
	})

public class Position extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1987302492306161423L;

	private Long positionId;
	private Set<PositionRole> positionRoles;
	private Set<User> users = new HashSet<User>();
	private String uniqueOrgUnitId;
	private String uniqueIdentifier;
	private String displayName;
	private boolean deleted;
	private boolean disabled;
	
	
	private Set<PositionRole> positionRolesToRevoke = new HashSet<PositionRole>();
	private Set<PositionRole> positionRolesToAssign = new HashSet<PositionRole>();
	private BulkTask bulkTaskOfRolesModification;
	private Set<PositionJournalingEntry> journaling;
	


	/**
	 * @param positionId
	 *            The positionId to set.
	 */
	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}

	/**
	 * @return Returns the positionId.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "PositionIdSeq")
	@Column(name = "POSITION_ID")
	public Long getPositionId() {
		return positionId;
	}

	/**
	 * @param positionRoles
	 *            The positionRoles to set.
	 */
	public void setPositionRoles(Set<PositionRole> positionRoles) {
		this.positionRoles = positionRoles;
		
	}

	/**
	 * @return Returns the roles.
	 */
	//@ManyToMany(
	//fetch = FetchType.LAZY, targetEntity = velo.entity.Role.class)
	//@JoinTable(name = "VL_ROLES_TO_POSITIONS", joinColumns = @JoinColumn(name = "POSITION_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
	@OneToMany(mappedBy="primaryKey.position", fetch = FetchType.LAZY)
	public Set<PositionRole> getPositionRoles() {
		return positionRoles;
	}

	/**
	 * @param uniqueOrgUnitId
	 *            The uniqueOrgUnitId to set.
	 */
	public void setUniqueOrgUnitId(String uniqueOrgUnitId) {
		this.uniqueOrgUnitId = uniqueOrgUnitId;
	}

	/**
	 * @return Returns the uniqueOrgUnitId.
	 */
	@Column(name = "UNIQUE_ORG_UNIT_ID", nullable = true)
	public String getUniqueOrgUnitId() {
		return uniqueOrgUnitId;
	}

	/**
	 * @param users
	 *            The users to set.
	 */
	public void setUsers(Set<User> users) {
		this.users = users;
	}

	/**
	 * @return Returns the users.
	 */
	@ManyToMany(
			fetch = FetchType.LAZY, targetEntity = velo.entity.User.class)
	@JoinTable(name = "VL_USERS_TO_POSITIONS", joinColumns = @JoinColumn(name = "POSITION_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
	public Set<User> getUsers() {
		return users;
	}
	
	/**
	 * @return the uniqueIdentifier
	 */
	@Column(name="UNIQUE_IDENTIFIER", nullable=false)
	@NotNull
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	/**
	 * @param uniqueIdentifier the uniqueIdentifier to set
	 */
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
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
	
	/**
	 * @return the deleted
	 */
	@Column(name="DELETED", nullable=false)
    @NotNull
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the disabled
	 */
	@Column(name="DISABLED", nullable=false)
    @NotNull
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	/**
	 * @return the journaling
	 */
	@OneToMany(mappedBy = "position", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("creationDate DESC")
	public Set<PositionJournalingEntry> getJournaling() {
		return journaling;
	}

	/**
	 * @param journaling the journaling to set
	 */
	public void setJournaling(Set<PositionJournalingEntry> journaling) {
		this.journaling = journaling;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	

	public void removePositionRole(Role role) {
		PositionRole posRole = getAssignedPositionRole(role);
		
		if (posRole != null) {
			getPositionRoles().remove(posRole);
		}
	}
	
	public void addPositionRole(PositionRole positionRole) {
		if (getPositionRoles() == null) {
			setPositionRoles(new HashSet<PositionRole>());
		}
		
		getPositionRoles().add(positionRole);
	}

	
	@Transient
	public PositionRole getAssignedPositionRole(Role role) {
		for (PositionRole currPR : getPositionRoles()) {
			if (currPR.getRole().equals(role)) {
				return currPR;
			}
		}
		
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof Position))
			return false;
		Position ent = (Position) obj;
		if (this.positionId.equals(ent.positionId))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		if (positionId == null)
			return super.hashCode();
		return positionId.hashCode();
	}

	/*
	 * @Transient public OrgUnit getOrgUnit() {
	 *  }
	 */
	
	
	// Transient for roles modification process
	/**
	 * @return the positionRolesToRevoke
	 */
	
	@Transient
	public Set<PositionRole> getPositionRolesToRevoke() {
		return positionRolesToRevoke;
	}

	/**
	 * @param positionRolesToRevoke
	 *            the positionRolesToRevoke to set
	 */
	public void setPositionRolesToRevoke(Set<PositionRole> positionRolesToRevoke) {
		this.positionRolesToRevoke = positionRolesToRevoke;
	}

	public void addPositionRoleToRevoke(PositionRole positionRole) {
		getPositionRolesToRevoke().add(positionRole);
	}

	public void removePositionRoleToRevoke(PositionRole positionRole) {
		getPositionRolesToRevoke().remove(positionRole);
	}

	/**
	 * @return the positionRolesToAssign
	 */
	@Transient
	public Set<PositionRole> getPositionRolesToAssign() {
		return positionRolesToAssign;
	}

	/**
	 * @param positionRolesToAssign
	 *            the positionRolesToAssign to set
	 */
	public void setPositionRolesToAssign(Set<PositionRole> positionRolesToAssign) {
		this.positionRolesToAssign = positionRolesToAssign;
	}

	public void addPositionRoleToAssign(PositionRole positionRole)
			throws CollectionElementInsertionException {
		// Make sure that the position role is not already associated in position
		if (isPositionRoleAssociated(positionRole)) {
			throw new CollectionElementInsertionException(
					"Role already associated to the current edited position");
		}
		if(!isPositionRoleInPRToAssignList(positionRole))
				getPositionRolesToAssign().add(positionRole);
	}

	public void removePositionRoleToAssign(PositionRole position) {
		getPositionRolesToAssign().remove(position);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * @return the bulkTaskOfRolesModification
	 */
	@Transient
	public BulkTask getBulkTaskOfRolesModification() {
		return bulkTaskOfRolesModification;
	}

	/**
	 * @param bulkTaskOfRolesModification the bulkTaskOfRolesModification to set
	 */
	public void setBulkTaskOfRolesModification(BulkTask bulkTaskOfRolesModification) {
		this.bulkTaskOfRolesModification = bulkTaskOfRolesModification;
	}

	@Transient
	public boolean isPositionRoleAssociated(PositionRole positionRole) {
		for (PositionRole currPositionRoleInPosition : getPositionRoles()) {
			if (currPositionRoleInPosition.equals(positionRole)) {
				return true;
			}
		}

		return false;
	}
	
	
	//Checks for duplicates in temporary list of PositionRoles to assign
	
	@Transient
	public boolean isPositionRoleInPRToAssignList(PositionRole positionRole){
		for (PositionRole currPositionRoleInPositionRolesToAssignList : getPositionRolesToAssign()) {
			if (currPositionRoleInPositionRolesToAssignList.equals(positionRole)) {
				return true;
			}
		}

		return false;
			
	}
	
	
	//make sure the required fields are set
	public void isCorrectlyImported() throws ValidationException {
		if (getUniqueIdentifier() == null) {
			throw new ValidationException("Unique Identifier was not set.");
		} else {
			if (getUniqueIdentifier().length() < 1) {
				throw new ValidationException("Unique Identifier was not to empty string.");
			}
		}
		
		if (getDisplayName() == null) {
			throw new ValidationException("Display Name was not set.");
		} else {
			if (getDisplayName().length() < 1) {
				throw new ValidationException("Display Name was not to empty string.");
			}
		}
		
	}
	
	@Transient
	public Set<Role> getRoles() {
		Set<Role> roles = new HashSet<Role>();
		
		for (PositionRole currPR : getPositionRoles()) {
			roles.add(currPR.getRole());
		}
		
		
		return roles;
	}
	
	@Transient
	public boolean isRoleAssociated(Role role) {
		for (PositionRole currPR : getPositionRoles()) {
			if (currPR.getRole().equals(role)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Transient
	public void removeUser(User user) {
		User u = null;
		
		for (User currUser : getUsers()) {
			if (currUser.equals(user)) {
				u = currUser;
				break;
			}
		}
		
		
		if (u != null) {
			getUsers().remove(u);
		}
	}
	
	@Transient
	public boolean isResourceAssociated(String resourceUniqueName) {
		for (PositionRole posRole : getPositionRoles()) {
			if (posRole.getRole().isResourceAssociated(resourceUniqueName)) {
				return true;
			}
		}
		
		return false;
	}
	
}
