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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import velo.entity.Attribute.AttributeDataTypes;
import velo.exceptions.AttributeSetValueException;

/**
 * An entity that represents a Request Attribute
 *
 * @author Asaf Shakarchi
 */
@Table(name="VL_REQUEST_ATTRIBUTE")
@Entity
@SequenceGenerator(name="RequestAttributeIdSeq",sequenceName="REQUEST_ATTRIBUTE_ID_SEQ")
@Deprecated
public class RequestAttribute extends GuiAttribute implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * The ID of the entity
     */
    private long requestAttributeId;
    
    private Request request;
    

    /**
     * A collection of values for the attribute
     */
    //note: the name is long so it wont get in conflict with 'Attribute.getValues()' which is needed for proxying 'Attribute.getTransientValues()' in AttributeRules 
    private Set<RequestAttributeValue> requestAttributeValues = new HashSet<RequestAttributeValue>();
    
    
    /**
     * @param requestAttributeId the requestAttributeId to set
     */
    public void setRequestAttributeId(long requestAttributeId) {
        this.requestAttributeId = requestAttributeId;
    }
    
    
    /**
     * @return the requestAttributeId
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_REQUEST_ATTRIBUTE_GEN",sequenceName="IDM_REQUEST_ATTRIBUTE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_REQUEST_ATTRIBUTE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestAttributeIdSeq")
    //@GeneratedValue //JB
    @Column(name="REQUEST_ATTRIBUTE_ID")
    public long getRequestAttributeId() {
        return requestAttributeId;
    }
    
    /**
     * @param request the request to set
     */
    public void setRequest(Request request) {
        this.request = request;
    }
    
    
    /**
     * @return the request
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="REQUEST_ID", nullable=false, unique=false)
    public Request getRequest() {
        return request;
    }
    
    
    /**
     * @param requestAttributeValues the requestAttributeValues to set
     */
    public void setRequestAttributeValues(Set<RequestAttributeValue> requestAttributeValues) {
        this.requestAttributeValues = requestAttributeValues;
    }
    
    
    /**
     * @return the requestAttributeValues
     */
    @OneToMany(mappedBy = "requestAttribute", fetch = FetchType.EAGER, cascade={CascadeType.REMOVE, CascadeType.PERSIST})
    public Set<RequestAttributeValue> getRequestAttributeValues() {
        return requestAttributeValues;
    }
    
    
    
    
    //HELPER/TRANSIENT
    //Transient
    public void addValue(RequestAttributeValue rav) {
        getValues().add(rav);
    }
    
    @Transient
    public RequestAttributeValue getFirstValue() {
        if (getRequestAttributeValues().size() < 1) {
            //throw new NoResultFoundException("No Values found for Request Attribute ID: '" + getRequestAttributeId() + "'");
        	return null;
        } else {
            return getRequestAttributeValues().iterator().next();
        }
    }
    
    public RequestAttributeValue factoryValue() {
    	RequestAttributeValue reqVal = new RequestAttributeValue();
    	reqVal.setCreationDate(new Date());
    	reqVal.setRequestAttribute(this);
    	
    	return reqVal;
    }
    
    public void addValue(Object value) throws AttributeSetValueException {
    	RequestAttributeValue reqVal = factoryValue();
    	reqVal.setValueAsObject(value);
    	
    	getRequestAttributeValues().add(reqVal);
    }
    
    public static RequestAttribute factoryRequestAttribute(Request request) {
    	RequestAttribute reqAttr = new RequestAttribute();
    	reqAttr.setRequest(request);
    	reqAttr.setCreationDate(new Date());
    	
    	//TODO: currently defaults, should be changed someday...
    	reqAttr.setDisplayPriority(0);
    	reqAttr.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
    	
    	return reqAttr;
    }
    
    
    
    
    
    
    
    
    
    
    //CLEANED!!!!
    //TODO: ugly. fix this method, it looks terrible.
    @Deprecated
    public void addValueByObject(Object object) {
    	/*
        if (object.getClass().equals(java.lang.Integer.class)) {
            Integer i = (Integer) object;
            
            RequestAttributeValueInteger ravi = new RequestAttributeValueInteger();
            ravi.setValue(i);
            ravi.setRequestAttribute(this);
            getValues().add(ravi);
            
        } else if (object.getClass().equals(java.lang.String.class)) {
            String s = (String) object;
            
            RequestAttributeValueString ravs = new RequestAttributeValueString();
            ravs.setValue(s);
            ravs.setRequestAttribute(this);
            getValues().add(ravs);
        } else if (object.getClass().equals(Long.class)) {
            Long l = (Long) object;
            
            RequestAttributeValueLong ravl = new RequestAttributeValueLong();
            ravl.setValue(l);
            ravl.setRequestAttribute(this);
            getValues().add(ravl);
            
            //Oracle returns 'BigDecimal' typed via JDBC sometimes, added support for that too
        } else if (object.getClass().getName().equals("java.math.BigDecimal")) {
                        
                        //BigDecimal bd = (BigDecimal) object;
                         
                        //RequestAttributeValueLong ravl = new RequestAttributeValueLong();
                        //ravl.setValue(bd.longValue());
                        //ravl.setRequestAttribute(this);
                        //getValues().add(ravl);
            System.out.println("RequestAttribute.addValueByObject() BigDecimal IS UNSUPPORTED!!!!!!!!!!!!!!!");
            
        } else if (object.getClass().getName().equals("java.lang.Boolean")) {
            Boolean bool = (Boolean) object;
            System.out.println("RequestAttribute.addValueByObject() BOOLEAN IS UNSUPPORTED!!!!!!!!!!!!!!!");
        } else {
            String errMsg = "Warning, could not set value of class type: " + object.getClass().getName();
            System.out.println("RequestAttribute.addValueByObject() " + object.getClass().getName() + "' IS UNSUPPORTED!!!!!!!!!!!!!!!, SKIPPING!!!");
        }
        */
    }
    
/*
    public void modifyValuesByScriptedValidator(IdentityAttribute ia) throws AttributeValidationException {
        try {
            IdentityAttributeValidator avs = (IdentityAttributeValidator)ia.getScriptedValidator();
            avs.setIA(ia);
            
            Attribute attr = new Attribute();
            attr.setValuesByUserIdentityAttributeValues(getValues());
            
            avs.setInputAttribute(attr);
            //TODO switch to UIA, avs.setInputAttribute(uia);
            //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + uia.getValues().size() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
            //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! OUT: " + getFirstValue().getAsString() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
            avs.__modify__();
            
            if (uia.getValues().size() == 0) {
                setValuesByAV(avs.getOutputAttribute().getValues());
            }
            else {
                UserIdentityAttributeValue fetchedVal = uia.getValues().iterator().next();
                fetchedVal.setValue(avs.getOutputAttribute().getValueAsString());
            }
            
            //setValuesByAV(avs.getOutputAttribute().getValues());
            //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTR: " + uia.getValues().size() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
            //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! THIS: " + getFirstValue().getAsString() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
        } catch (ScriptLoadingException sle) {
            throw new AttributeValidationException("Script validation name: '" + getIdentityAttribute().getValidatorScriptFileName() + "' could not be loaded due to: " + sle.getMessage());
        }
        //A lot of other exceptions may occure(Such as NullPointerException/groovy.lang.MissingMethodException/etc...) -  important, we do not trust the script itself!
        catch (Exception npe) {
            throw new AttributeValidationException(npe.getMessage());
        }
    }
*/
}
