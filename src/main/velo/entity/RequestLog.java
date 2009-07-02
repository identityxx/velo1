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

import javax.persistence.CascadeType;
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
 * An entity that represents a Request Log entry
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_REQUEST_LOG")
@SequenceGenerator(name="RequestLogIdSeq",sequenceName="REQUEST_LOG_ID_SEQ")
@Deprecated
public class RequestLog extends EntityLog implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    private long requestLogId;
    private Request request;
    
    /**
     * @param requestLogId The requestLogId to set.
     */
    public void setRequestLogId(long requestLogId) {
        this.requestLogId = requestLogId;
    }
    
    /**
     * @return Returns the requestLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_REQUEST_LOG_GEN",sequenceName="IDM_REQUEST_LOG_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_REQUEST_LOG_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestLogIdSeq")
    //@GeneratedValue //JB
    @Column(name="REQUEST_LOG_ID")
    public long getRequestLogId() {
        return requestLogId;
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
    //TODO WTF is that cascading?!
    @ManyToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name="REQUEST_ID", nullable=true)
    public Request getRequest() {
        return request;
    }
}
