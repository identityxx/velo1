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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * An entity that represents a RequestRole entry
 *
 * @author Asaf Shakarchi
 */
@Entity 
@Table(name="VL_REQUEST_ROLE")
@SequenceGenerator(name="RequestRoleIdSeq",sequenceName="REQUEST_ROLE_ID_SEQ")
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name="REQUEST_ROLE_TYPE")
//@MappedSuperclass
@Deprecated
public class RequestRole extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    private Long requestRoleId;
    private String name;
    private Request request;
    
    /**
     * @param requestRoleId The requestRoleId to set.
     */
    public void setRequestRoleId(Long requestRoleId) {
        this.requestRoleId = requestRoleId;
    }
    
    /**
     * @return Returns the taskLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_REQUEST_ROLE_GEN",sequenceName="IDM_REQUEST_ROLE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_REQUEST_ROLE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestRoleIdSeq")
    //@GeneratedValue //JB
    @Column(name="REQUEST_ROLE_ID")
    public Long getRequestRoleId() {
        return requestRoleId;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the name
     */
    @Column(name="NAME")
    public String getName() {
        return name;
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
