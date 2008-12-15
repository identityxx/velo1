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

import velo.entity.Attribute.AttributeDataTypes;

/**
 * An entity that represents a Request Attribute Value
 *
 * @author Asaf Shakarchi
 */
@Table(name="VL_REQUEST_ATTRIBUTE_VALUE")
@Entity
@SequenceGenerator(name="RequestAttributeValueIdSeq",sequenceName="REQUEST_ATTRIBUTE_VALUE_ID_SEQ")
public class RequestAttributeValue extends AttributeValue implements Serializable,Cloneable {
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long requestAttributeValueId;
    
    /**
     * The RequestAttribute this value is related to
     */
    private RequestAttribute requestAttribute;
    
    
    public RequestAttributeValue() {
    }
    
    public RequestAttributeValue(AttributeDataTypes dataType) {
    	super(dataType);
    }
    
    
    
    
    
    
    
    
    /**
     * @param requestAttributeValueId the requestAttributeValueId to set
     */
    public void setRequestAttributeValueId(Long requestAttributeValueId) {
        this.requestAttributeValueId = requestAttributeValueId;
    }
    
    /**
     * @return the requestAttributeValueId
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_REQUEST_ATTR_VALUE_GEN",sequenceName="IDM_REQUEST_ATTR_VALUE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_REQUEST_ATTR_VALUE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestAttributeValueIdSeq")
    //@GeneratedValue //JB
    @Column(name="REQUEST_ATTRIBUTE_VALUE_ID")
    public Long getRequestAttributeValueId() {
        return requestAttributeValueId;
    }
    
    
    /**
     * @param requestAttribute the requestAttribute to set
     */
    public void setRequestAttribute(RequestAttribute requestAttribute) {
        this.requestAttribute = requestAttribute;
    }
    
    /**
     * @return the requestAttribute
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="REQUEST_ATTRIBUTE_ID", nullable=false, unique=false)
    public RequestAttribute getRequestAttribute() {
        return requestAttribute;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //what for?
    @Deprecated
    public RequestAttributeValue clone() {
        try {
            RequestAttributeValue clonedEntity = (RequestAttributeValue) super.clone();
            //Since clone only clone the object but not all references within it, then we must clone all of its properties as well
            
            return clonedEntity;
        } catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
    //@Transient
    //public abstract String getValueAsString();
    
    //@Transient
    //public abstract Object getValueAsObject();
}
