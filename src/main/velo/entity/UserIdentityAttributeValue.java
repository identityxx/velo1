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

import java.io.Serializable;
import java.util.Date;

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

import velo.entity.Attribute.AttributeDataTypes;
import velo.exceptions.AttributeSetValueException;
//@!@clean
/**
 * An entity that represents a User Identity attribute value
 *
 * @author Asaf Shakarchi
 */
@Table(name="VL_USER_IDENTITY_ATTR_VALUE")
@SequenceGenerator(name="UserIdentityAttrValueIdSeq",sequenceName="USER_IDENTITY_ATTR_VAL_ID_SEQ")
@Entity
public class UserIdentityAttributeValue extends AttributeValue implements Serializable,Cloneable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long userIdentityAttributeValueId;
    
    private UserIdentityAttribute userIdentityAttribute;
    
    /**
     * @param userIdentityAttributeValueId the userIdentityAttributeValueId to set
     */
    public void setUserIdentityAttributeValueId(Long userIdentityAttributeValueId) {
        this.userIdentityAttributeValueId = userIdentityAttributeValueId;
    }
    
    /**
     * @return the userIdentityAttributeValueId
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="UserIdentityAttrValueIdSeq")
    //@GeneratedValue
    @Column(name="USER_IDENTITY_ATTR_VALUE_ID")
    public Long getUserIdentityAttributeValueId() {
        return userIdentityAttributeValueId;
    }
    

    /**
     * @param userIdentityAttribute The userIdentityAttribute to set.
     */
    public void setUserIdentityAttribute(UserIdentityAttribute userIdentityAttribute) {
        this.userIdentityAttribute = userIdentityAttribute;
    }
    
    /**
     * @return Returns the userIdentityAttribute.
     * Cascade.Persist->Persist new 'UserIdentityAttribute' owner when persisting a new UserIdentityAttributeValue (see (velo.ejb.impl.UserBean.getUserIdentityAttribute())
     */
    @ManyToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name="USER_IDENTITY_ATTRIBUTE_ID", nullable=false, unique=false)
    public UserIdentityAttribute getUserIdentityAttribute() {
        return userIdentityAttribute;
    }
    
    
    
    
    //Helper
    public static UserIdentityAttributeValue factory(UserIdentityAttribute uia, AttributeValue attrValue) throws AttributeSetValueException {
    	UserIdentityAttributeValue uiav = new UserIdentityAttributeValue();
    	uiav.setCreationDate(new Date());
    	uiav.setDataType(attrValue.getDataType());
    	uiav.setValueAsObject(attrValue.getValueAsObject());
    	uiav.setUserIdentityAttribute(uia);
    	
    	return uiav;
    }
    
    //TODO: Why without UIA association?!
    public static UserIdentityAttributeValue factory(AttributeDataTypes dataType) {
    	UserIdentityAttributeValue uiav = new UserIdentityAttributeValue();
    	uiav.setCreationDate(new Date());
    	uiav.setDataType(dataType);
    	
    	
    	return uiav;
    }
    
    
    public UserIdentityAttributeValue clone() {
        try {
            UserIdentityAttributeValue clonedEntity = (UserIdentityAttributeValue) super.clone();
            //Since clone only clone the object but not all references within it, then we must clone all of its properties as well
            
            return clonedEntity;
        } catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
}
