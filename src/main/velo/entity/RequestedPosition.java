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
import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a RequestRole entry
 *
 * @author Asaf Shakarchi
 */

@Entity
@Table(name = "VL_REQUESTED_POSITION")
@AssociationOverrides({
@AssociationOverride(name="primaryKey.request", joinColumns = @JoinColumn(name="REQUEST_ID")),
@AssociationOverride(name="primaryKey.position", joinColumns = @JoinColumn(name="POSITION_ID"))
})
@Deprecated
public class RequestedPosition extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    public enum RequestedPositionActionType {
		ASSIGN, REVOKE
	}
    
    private RequestedPositionPK primaryKey = new RequestedPositionPK();
    private RequestedPositionActionType actionType; 

    
    public RequestedPosition() {
    	
    }
    
    
    public RequestedPosition(Position position, Request request, RequestedPositionActionType actionType) {
    	setCreationDate(new Date());
    	setPosition(position);
    	setRequest(request);
    	setActionType(actionType);
    }
    
	/**
	 * @return the primaryKey
	 */
    @Id
	public RequestedPositionPK getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(RequestedPositionPK primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
	public void setRequest(Request request) {
		primaryKey.setRequest(request);
	}

	@Transient
	public Request getRequest() {
		return primaryKey.getRequest();
	}
	
	
	public void setPosition(Position position) {
		primaryKey.setPosition(position);
	}
	
	@Transient
	public Position getPosition() {
		return primaryKey.getPosition();
	}

	/**
	 * @return the actionType
	 */
	@Column(name = "ACTION_TYPE")
	@Enumerated(EnumType.STRING)
	public RequestedPositionActionType getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(RequestedPositionActionType actionType) {
		this.actionType = actionType;
	}
}
