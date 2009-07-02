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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("CREATE_USER_REQUEST")
@Deprecated
public class CreateUserRequest extends Request {
    private final String REQUEST_TYPE = "CREATE_USER";
    
    private static final long serialVersionUID = 1987302492306161413L;
    

    private String suggestedUserName;
    private boolean inheritedByPosition;
    private Set<RequestRole> requestRoles = new HashSet<RequestRole>();
    private Set<Position> requestedPositions = new HashSet<Position>();
    
    /**
     * @param suggestedUserName the suggestedUserName to set
     */
    public void setSuggestedUserName(String suggestedUserName) {
        this.suggestedUserName = suggestedUserName;
    }
    
    /**
     * @return the suggestedUserName
     */
    @Column(name="SUGGESTED_USER_NAME")
    public String getSuggestedUserName() {
        return suggestedUserName;
    }
    
    /**
     * @param inheritedByPosition the inheritedByPosition to set
     */
    public void setInheritedByPosition(boolean inheritedByPosition) {
        this.inheritedByPosition = inheritedByPosition;
    }
    
    /**
     * @return the inheritedByPosition
     */
    @Deprecated
    @Column(name="INHERITED_BY_POSITION")
    public boolean isInheritedByPosition() {
        return inheritedByPosition;
    }
    
    /**
     * @param requestRoles the requestRoles to set
     */
    //public void setRequestRoles(Set<RequestRoleCreateUser> requestRoles) {
    public void setRequestRoles(Set<RequestRole> requestRoles) {
        this.requestRoles = requestRoles;
    }
    
    /**
     * @return the rolesNames
     */
//DAMN    @OneToMany(mappedBy = "request", fetch = FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    @Transient
    //public Set<RequestRoleCreateUser> getRequestRoles() {
    public Set<RequestRole> getRequestRoles() {
        return requestRoles;
    }
    
    
    //Helper Methods
    public void addRoleName(String roleName) {
        RequestRoleCreateUser rr = new RequestRoleCreateUser();
        rr.setRequest(this);
        rr.setName(roleName);
        getRequestRoles().add(rr);
    }
    
    
    /**
	 * @return the requestedPositions
	 */
    //@OneToMany(mappedBy = "request", fetch = FetchType.EAGER)
    @ManyToMany(
    	cascade={CascadeType.REFRESH},
    	fetch=FetchType.LAZY,targetEntity=velo.entity.Position.class
    	)
    	@JoinTable(
    			name="VL_POS_TO_REQUESTS",
    			joinColumns=@JoinColumn(name="REQUEST_ID"),
    			inverseJoinColumns=@JoinColumn(name="POSITION_ID")
    	)
	public Set<Position> getRequestedPositions() {
		return requestedPositions;
	}

	/**
	 * @param requestedPositions the requestedPositions to set
	 */
	public void setRequestedPositions(Set<Position> requestedPositions) {
		this.requestedPositions = requestedPositions;
	}

	
	public void addRequestedPosition(Position requestedPositions) {
		getRequestedPositions().add(requestedPositions);
	}

	@Transient
    public String getType() {
        return REQUEST_TYPE;
    }
}
