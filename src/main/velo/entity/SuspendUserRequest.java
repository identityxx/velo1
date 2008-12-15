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
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class SuspendUserRequest extends Request {
    private final String REQUEST_TYPE = "Suspend User";
    
    private static final long serialVersionUID = 1987302492306961413L;
    private String userName;

    /**
     * @param userName the userName to disable
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * @return the userName The user name to disable
     */
    @Column(name="USER_NAME")
    public String getUserName() {
        return userName;
    }
 
    
    
    @Transient
    public String getType() {
        return REQUEST_TYPE;
    }
}
