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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "ROLE")
public class RoleJournalingEntry extends EntityJournalingEntry {
	
	public enum RoleJournalingActionType {
    	CREATED,ACTIVATED,DEACTIVATED,MODIFIED_GROUPS,MODIFIED_ATTRIBUTES
	}
	
	private Role role;
	private RoleJournalingActionType actionType;
	
	
	public RoleJournalingEntry() { 
		
	}
	
	public RoleJournalingEntry(Role role, RoleJournalingActionType actionType, User performedBy, String summaryMessage, String detailedMessage) {
		super(performedBy,summaryMessage,detailedMessage);
		setRole(role);
		setActionType(actionType);
	}
	
	/**
	 * @return the role
	 */
	//sucks, if opetional=false in mysql the col is created as nullable=false
	@ManyToOne(optional=true)
	@JoinColumn(name = "ENTITY_ROLE_ID", nullable = true)
	public Role getRole() {
		return role;
	}
	
	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}
	
	/**
	 * @return the actionType
	 */
	@Column(name="ACTION_TYPE")
	@Enumerated(EnumType.STRING)
	public RoleJournalingActionType getActionType() {
		return actionType;
	}
	
	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(RoleJournalingActionType actionType) {
		this.actionType = actionType;
	}
}
