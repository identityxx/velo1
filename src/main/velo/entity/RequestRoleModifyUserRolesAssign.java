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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Shakarchi Asaf
 */
//@Entity
//@DiscriminatorValue("MODIFY_USER_ROLES_ASSIGN")
@Deprecated
public class RequestRoleModifyUserRolesAssign extends RequestRole {
    private static final long serialVersionUID = 1987302492306161429L;
    
    private Request request;
    
    /** Creates a new instance of RequestRoleCreateUser */
    public RequestRoleModifyUserRolesAssign() {
    }
    
    /**
     * @param request The request to set.
     */
    public void setRequest(Request request) {
        this.request = request;
    }
    
    /**
     * @return Returns the request.
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="REQUEST_ID", nullable=false)
    public Request getRequest() {
        return request;
    }
}
