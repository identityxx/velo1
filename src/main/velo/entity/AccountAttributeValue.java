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
@Table(name="VL_ACCOUNT_ATTRIBUTE_VALUE")
@SequenceGenerator(name="AccountAttrValueIdSeq",sequenceName="ACCOUNT_ATTR_VAL_ID_SEQ")
@Entity
public class AccountAttributeValue extends AttributeValue implements Serializable,Cloneable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long accountAttributeValueId;
    
    private AccountAttribute accountAttribute;
    
    
    
    
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="AccountAttrValueIdSeq")
    @Column(name="ACCOUNT_ATTRIBUTE_VALUE_ID")
    public Long getAccountAttributeValueId() {
		return accountAttributeValueId;
	}

	public void setAccountAttributeValueId(Long accountAttributeValueId) {
		this.accountAttributeValueId = accountAttributeValueId;
	}

	@ManyToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name="ACCOUNT_ATTRIBUTE_ID", nullable=false)
	public AccountAttribute getAccountAttribute() {
		return accountAttribute;
	}

	public void setAccountAttribute(AccountAttribute accountAttribute) {
		this.accountAttribute = accountAttribute;
	}

	
    
    
    //Helper
    public static AccountAttributeValue factory(AccountAttribute aa, AttributeValue attrValue) throws AttributeSetValueException {
    	AccountAttributeValue uiav = new AccountAttributeValue();
    	uiav.setCreationDate(new Date());
    	uiav.setDataType(attrValue.getDataType());
    	uiav.setValueAsObject(attrValue.getValueAsObject());
    	uiav.setAccountAttribute(aa);
    	
    	return uiav;
    }
    
    //TODO: Why without UIA association?!
    public static AccountAttributeValue factory(AttributeDataTypes dataType) {
    	AccountAttributeValue uiav = new AccountAttributeValue();
    	uiav.setCreationDate(new Date());
    	uiav.setDataType(dataType);
    	
    	
    	return uiav;
    }
    
    
    public AccountAttributeValue clone() {
        try {
            AccountAttributeValue clonedEntity = (AccountAttributeValue) super.clone();
            //Since clone only clone the object but not all references within it, then we must clone all of its properties as well
            
            return clonedEntity;
        } catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
}
