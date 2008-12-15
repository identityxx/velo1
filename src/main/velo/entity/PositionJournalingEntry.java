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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(value = "POSITION")
public class PositionJournalingEntry extends EntityJournalingEntry {
	
	public enum PositionJournalingActionType {
    	CREATED,DISABLED,MODIFIED_ROLES,SYNCED
	}
	
	private Position position;
	private PositionJournalingActionType actionType;
	
	
	public PositionJournalingEntry() { 
		
	}
	
	public PositionJournalingEntry(Position position, PositionJournalingActionType actionType, User performedBy, String summaryMessage, String detailedMessage) {
		super(performedBy,summaryMessage,detailedMessage);
		setPosition(position);
		setActionType(actionType);
	}
	
	/**
	 * @return the role
	 */
	//sucks, if opetional=false in mysql the col is created as nullable=false
	@ManyToOne(optional=true)
	@JoinColumn(name = "ENTITY_POSITION_ID", nullable = true)
	public Position getPosition() {
		return position;
	}
	
	/**
	 * @param role the role to set
	 */
	public void setPosition(Position position) {
		this.position = position;
	}
	
	/**
	 * @return the actionType
	 */
	@Column(name="ACTION_TYPE")
	@Enumerated(EnumType.STRING)
	public PositionJournalingActionType getActionType() {
		return actionType;
	}
	
	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(PositionJournalingActionType actionType) {
		this.actionType = actionType;
	}
}
