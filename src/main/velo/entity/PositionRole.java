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
import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "VL_POSITION_ROLE")
//@Name("roleApproversGroup")
@AssociationOverrides({
@AssociationOverride(name="primaryKey.role", joinColumns = @JoinColumn(name="ROLE_ID")),
@AssociationOverride(name="primaryKey.position", joinColumns = @JoinColumn(name="POSITION_ID"))
})
@NamedQueries( {		
		@NamedQuery(name = "positionRole.findByRoleNameAndPositionUniqueId", query = "SELECT object(pr) FROM PositionRole AS pr WHERE pr.primaryKey.position.uniqueIdentifier = :positionUniqueId and pr.primaryKey.role.name = :roleName")
})
public class PositionRole extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1987305452306161213L;

	private PositionRolePK primaryKey = new PositionRolePK();
	
	private String description;
	private Date expirationTime;
	
	public PositionRole() {
		
	}
	
	public PositionRole (Role role, Position position) {
		setCreationDate(new Date());
		setRole(role);
		setPosition(position);
		System.out.println("New PositionRole object is created");
	}
	
	
	/**
	 * @return the primaryKey
	 */
	@Id
	public PositionRolePK getPrimaryKey() {
		return primaryKey;
	}
	
	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(PositionRolePK primaryKey) {
		this.primaryKey = primaryKey;
	}

	
	public void setRole(Role role) {
		primaryKey.setRole(role);
	}

	@Transient
	public Role getRole() {
		return primaryKey.getRole();
	}

	public void setPosition(Position position) {
		primaryKey.setPosition(position);
	}

	@Transient
	public Position getPosition() {
		return primaryKey.getPosition();
	}
	
	
	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION",nullable=true)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the expirationTime
	 */
	@Column(name="EXPIRATION_TIME",nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getExpirationTime() {
		return expirationTime;
	}

	/**
	 * @param expirationTime the expirationTime to set
	 */
	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof PositionRole))
			return false;
		PositionRole ent = (PositionRole) obj;
		//System.out.println("!!!!!!!!!!!!!!: CURRENT POS ID '" + getPosition().getPositionId() + "', Curr role '" + getRole().getRoleId() + "', compare to pos: '" + ent.getPosition().getPositionId() + ", role: '" + ent.getRole().getRoleId() + "'");
		if ( (this.getPosition().getPositionId().equals(ent.getPosition().getPositionId())) && (this.getRole().getRoleId().equals(ent.getRole().getRoleId())) )
			return true;
		return false;
	}
	
	
	

}
