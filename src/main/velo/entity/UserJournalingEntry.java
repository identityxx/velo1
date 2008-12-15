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
@DiscriminatorValue(value = "USER")
public class UserJournalingEntry extends EntityJournalingEntry {
	
	public enum UserJournalingActionType {
    	CREATED,DISABLED,ENABLED,MODIFIED_ROLES,MODIFIED_ACCOUNTS,MODIFIED_CAPABILITIES,MODIFIED_ATTRIBUTES,MODIFIED_GENERAL_DETAILS
	}
	
	private User user;
	private UserJournalingActionType actionType;
	
	
	public UserJournalingEntry() { 
		
	}
	
	public UserJournalingEntry(User user, UserJournalingActionType actionType, User performedBy, String summaryMessage, String detailedMessage) {
		super(performedBy,summaryMessage,detailedMessage);
		setUser(user);
		setActionType(actionType);
	}
	
	/**
	 * @return the user
	 */
	//sucks, if opetional=false in mysql the col is created as nullable=false
	@ManyToOne(optional=true)
	@JoinColumn(name = "ENTITY_USER_ID", nullable = true)
	public User getUser() {
		return user;
	}
	
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the actionType
	 */
	@Column(name="ACTION_TYPE")
	@Enumerated(EnumType.STRING)
	public UserJournalingActionType getActionType() {
		return actionType;
	}
	
	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(UserJournalingActionType actionType) {
		this.actionType = actionType;
	}
}
