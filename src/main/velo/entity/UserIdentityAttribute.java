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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.exceptions.AttributeSetValueException;
import velo.exceptions.AttributeValidationException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.UnsupportedAttributeTypeException;

/**
 * An entity that represents a User Identity-Attribute
 *
 * @author Asaf Shakarchi
 */
@Table(name="VL_USER_IDENTITY_ATTRIBUTE")
@Entity
@SequenceGenerator(name="UserIdentityAttributeIdSeq",sequenceName="USER_IDENTITY_ATTRIBUTE_ID_SEQ")
@NamedQueries({
    //@NamedQuery(name = "findUserIdentityAttributeByName",query = "SELECT object(uia) FROM UserIdentityAttribute AS uia WHERE uia.identityAttribute  = identityAttribute AND uia.user = :user")
    @NamedQuery(name = "uia.findByIdentityAttributes",query = "SELECT object(uia) FROM UserIdentityAttribute AS uia WHERE uia.identityAttribute = :identityAttribute")
})
        public class UserIdentityAttribute extends BaseEntity implements Serializable, Cloneable {
	private static transient Logger log = Logger.getLogger(UserIdentityAttribute.class.getName());
    
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * The ID of the User Identity Attribute
     */
    private long userIdentityAttributeId;
    
    
    /**
     * The user object this identity attribute is related to
     */
    private User user;
    
    /**
     * The IdentityAttribute entity of this UserIdentityAttribute
     */
    private IdentityAttribute identityAttribute;
    
    private List<UserIdentityAttributeValue> values = new ArrayList<UserIdentityAttributeValue>();
    

    
    /**
     * Set the ID of the entity
     * @param userIdentityAttributeId The ID of the entity to set
     */
    public void setUserIdentityAttributeId(long userIdentityAttributeId) {
        this.userIdentityAttributeId = userIdentityAttributeId;
    }
    
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="UserIdentityAttributeIdSeq")
    //@GeneratedValue //JB
    @Column(name="USER_IDENTITY_ATTRIBUTE_ID")
    public long getUserIdentityAttributeId() {
        return userIdentityAttributeId;
    }
    
    
    /**
     * Set the user entity that the UserIdentityAttribute is related to
     * @param user The user entity to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Get the user entity that the UserIdentityAttribute is related to
     * @return The user entity to get
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="USER_ID", nullable=false, unique=false)
    public User getUser() {
        return user;
    }
    
    
    /**
     * Set the IdentityAttribute entity that the UserIdentityAttribute entity is based on
     * @param identityAttribute The IdentityAttribute entity to set
     */
    public void setIdentityAttribute(IdentityAttribute identityAttribute) {
        this.identityAttribute = identityAttribute;
    }
    
    
    /**
     * Get the IdentityAttribute entity that the UserIdentityAttribute entity is based on
     * @return The IdentityAttribute entity to get
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="IDENTITY_ATTRIBUTE_ID", nullable=false, unique=false)
    public IdentityAttribute getIdentityAttribute() {
        return identityAttribute;
    }
    
    
    /**
     * Set a list of values to the UserIdentityAttribute
     * @param values The userIdentityAttribute Values list to set.
     */
    public void setValues(List<UserIdentityAttributeValue> values) {
        this.values = values;
    }
    
    
    /**
     * Get the list of values to the UserIdentityAttribute
     * @return Returns the userIdentityAttributeValue.
     */
    //@OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE, CascadeType.REMOVE}, mappedBy="userIdentityAttribute", fetch=FetchType.EAGER)
    //12-03-09 - crap, changed from EAGER to LAZY, hopefully that is not going to do any harm
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy="userIdentityAttribute", fetch=FetchType.LAZY)
    public List<UserIdentityAttributeValue> getValues() {
        return values;
    }
    

    
    
    
    
    
    
    //Helper
    //TODO: Maybe move this to some child component, since all attributes should have value comparation techniqies
    public boolean compareValues(UserIdentityAttribute uia) {
        //TODO: Why clone?
    	UserIdentityAttribute comparedUIA = uia.clone();
    	
        //Iterate over the values of this instance
        for (UserIdentityAttributeValue uiav : getValues()) {

        	//If original value is null then continue to the next value
            //Check whether there's a corresponding UIAV in the specified compared UIA.
            boolean foundEqualValueObjInComparedAttribute = false;
            
            //Iterate over the 2nd attribute's values, seek for an equal value, if not found break and return false
            for (UserIdentityAttributeValue comparedUIAV : comparedUIA.getValues()) {
                //If found a compared value with the same class then check their values
            	//TODO: Is comparing classes that way is legal?
                //if (comparedUIAV.getClass() == uiav.getClass()) {
                    if (uiav.compareValue(comparedUIAV)) {
                        foundEqualValueObjInComparedAttribute = true;
                        //Remove the found object from the compared attribute !
                        comparedUIA.getValues().remove(comparedUIAV);
                        break;
                    }
                //} else {
                  //  System.out.println("Warning, cannot compare UIA's values of UIA ID: " + getUserIdentityAttributeId() + ", value class: '" + uiav.getClass().getName() + " is not the same as compared value class which is: '" + comparedUIAV.getClass() + "'");
                //}
            }
            //If found same value then continue to the next IAV comparation, otherwise return failure
            if (!foundEqualValueObjInComparedAttribute) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean compareTransientValues(Attribute attr) {
    	log.trace("Comparing values of UIA (user: '" + getUser().getName() + "', IA: '" + getIdentityAttribute().getDisplayName() + "')");
    	if (attr.getValues().size() != getValues().size()) {
    		log.trace("Amount of values are different, thus values are not equal!");
    		return false;
    	}
    	
    	
    	for (UserIdentityAttributeValue uiav : getValues()) {
    		
    		boolean foundEqualValueObjInComparedAttribute = false;
    		for (AttributeValue comparedAV : attr.getTransientValues()) {
    			//TODO: Is comparing classes that way is legal?
//    			if (comparedAV.getClass() == uiav.getClass()) {
    				if (uiav.compareValue(comparedAV)) {
    					foundEqualValueObjInComparedAttribute = true;
    					//Remove the found object from the compared attribute !
    					//?!?!?!?!?! comparedAV.getValues().remove(comparedAV);
    					break;
    				}
//    			} else {
//    				System.out.println("Warning, cannot compare UIA's values of UIA ID: " + getUserIdentityAttributeId() + ", value class: '" + uiav.getClass().getName() + " is not the same as compared value class which is: '" + comparedAV.getClass() + "'");
//    				return false;
//    			}
    		}
    		
    		if (!foundEqualValueObjInComparedAttribute) {
                return false;
            }
    	}
    	
    	return true;
    }
    
    
    
    
    
    //TRANSIENT PROPERTIES
    
    /**
     * A collection of old values (before user modification)
     * This is transient, only kept when the user modifies values in order to show in the view
     * What were the old values before modifications
     */
    private List<UserIdentityAttributeValue> oldValues;
    
    /**
     * Set a collection of OLD values to the UserIdentityAttribute
     * @param oldValues The userIdentityAttribute Values collection to set.
     */
    @Transient
    public void setOldValues(List<UserIdentityAttributeValue> oldValues) {
        this.oldValues = oldValues;
    }
    
    
    /**
     * Get the collection of OLD values to the UserIdentityAttribute
     * @return Returns the oldValues.
     */
    @Transient
    public Collection<UserIdentityAttributeValue> getOldValues() {
        return oldValues;
    }
    
    
    @Transient
	public boolean isFirstValueIsNotNull() {
		if (getValues().size()!=0) {
			return getValues().iterator().next().getValue() != null;
		} else {
			return false;
		}
	}
    
    @Transient
    public boolean isFirstValueIsNotNullAndNotEmpty() {
    	//also check that it is not an empty string for strings
		//if it's a number, then a number will be represented a non-empty string (including zeros)
    	if (getValues().size()!=0) {
			String value = getValues().iterator().next().getValue();
			if (value == null) {
				return false;
			} else {
				if (value.length() < 1) {
					return false;
				} else {
					return true;
				}
			}
		} else {
			return false;
		}
    }

    //For 'integration operation action scripts', otherwise integrator access 'values[cell]' where it can through nulls
    //and is very hard to track.
    @Transient
    public UserIdentityAttributeValue getFirstValue() throws NoResultFoundException {
    	log.trace("Getting first value of User Identity Attribute '" + getDisplayName() + "'");
        if (getValues().size() < 1) {
            throw new NoResultFoundException("Could not find any values for User: '" + getUser().getName() + "', for IdentityAttribute: '" + getIdentityAttribute().getUniqueName() + "'");
        } else {
            return getValues().iterator().next();
        }
    }
    
    
    //just easy way to access the first value, if there's no first value a null will be returned
    @Transient
    public String getFirstValueAsString() {
    	try {
    		UserIdentityAttributeValue firstValue = getFirstValue();
    		return firstValue.getAsString();
    	} catch (NoResultFoundException e) {
    		return null;
    	}
    }
    
    public void importValues(RequestAttribute reqAttr) throws AttributeSetValueException {
    	for (RequestAttributeValue currValue : reqAttr.getRequestAttributeValues()) {
    		
    		try {
    			UserIdentityAttributeValue uiaValue = UserIdentityAttributeValue.factory(this, currValue);
    			getValues().add(uiaValue);
    		} catch (AttributeSetValueException e) {
                throw new AttributeSetValueException("Error while trying to add value into UserIdentityAttribute: " + e.toString());
            }
    	}
    }
    
    public void importValues(List<AttributeValue> attributeValues) throws AttributeSetValueException {
    	for (AttributeValue currAV : attributeValues) {
    		try {
    			UserIdentityAttributeValue uiaValue = UserIdentityAttributeValue.factory(this, currAV);
    			getValues().add(uiaValue);
    		} catch (AttributeSetValueException e) {
                throw new AttributeSetValueException("Error while trying to add value into UserIdentityAttribute: " + e.toString());
            }
    	}
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //TRANSIENTS
    /**
     * Get the name of the IdentityAttribute this UserAttribute is attached to
     * Overides default parent getDisplayName() which assumes for a local 'displayName' existense
     * @return The name of the IdentityAttribute the UserIdentityAttribute is based on
     */
    @Transient
    public String getDisplayName() {
        return getIdentityAttribute().getDisplayName();
    }
    
    
    @Transient
    public UserIdentityAttribute clone() {
        try {
            UserIdentityAttribute clonedUIA = (UserIdentityAttribute) super.clone();
            //Since clone only clone the object but not all references within it, then we must clone all of its properties as well
            
            //Clone values
            //Clear the set from the references values of the original UIA and add cloned values objects
            //TODO: (99% sure that when cloning an object references objects still pass by references from the original cloned object, but this maybe requires better investigation, thus this code may not be necessary at all)
            //Clear was not enough, returned a 'Concurrency exception', so initialized a new SET before cloning.
            clonedUIA.setValues(new ArrayList<UserIdentityAttributeValue>());
            for (UserIdentityAttributeValue currUIAV : getValues()) {
                clonedUIA.getValues().add(currUIAV.clone());
            }
            
            //TODO: Clone user, identityAttribute, etc...
            clonedUIA.setUser(getUser().clone());
            clonedUIA.setIdentityAttribute(getIdentityAttribute().clone());
            
            return clonedUIA;
            
        }
        
        catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
    //helper
    public static UserIdentityAttribute factory() {
    	UserIdentityAttribute uia = new UserIdentityAttribute();
    	uia.setCreationDate(new Date());
    	
    	return uia;
    }
    
    //also factory a new value 
    public static UserIdentityAttribute factory(IdentityAttribute ia, User user) {
    	UserIdentityAttribute uia = factory();
    	uia.setIdentityAttribute(ia);
    	uia.setUser(user);
    	uia.factoryValue();
    	
    	return uia;
    }
    
    public UserIdentityAttributeValue factoryValue() {
    	UserIdentityAttributeValue uiav = UserIdentityAttributeValue.factory(getIdentityAttribute().getDataType());
    	uiav.setUserIdentityAttribute(this);
    	this.getValues().add(uiav);
    	
    	return uiav;
    }
    
    /**
     * Add values by collection of -generic- AttributeValues objects
     * @param attributeValues The collection of attribute values to add to this UserIdentityAttribute entity.
     */
    //@Transient
    public void setValuesByAV(List<AttributeValue> attributeValues) throws AttributeSetValueException {
    	if (getValues().size() == attributeValues.size()) {
    		for (int i=0;i<getValues().size();i++) {
    			UserIdentityAttributeValue uiav = getValues().get(i);
    			uiav.setValueAsObject(attributeValues.get(i).getValueAsObject());
    		}
    	} else {
    		getValues().clear();
    		importValues(attributeValues);
    	}
    	
    	
    	//getValues().clear();
    	//importValues(attributeValues);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //JUNK SHOULD BE CLEANED!!!!!!!!!!!!!!!
    
    
    /**
     * Add values by collection of -edm's generic- AttributeValue object
     * @param values The collection of attribute values to add to this UserIdentityAttribute entity.
     */
    @Transient
    @Deprecated
    public void setValuesByRequestValues(Collection<RequestAttributeValue> values) throws UnsupportedAttributeTypeException {
    	/*JB!!!
        //TODO: Support others but string type, consolidate with setValuesByAV, too much duplications!!!!!!!!!
        for (RequestAttributeValue currValue : values) {
            try {
                //no prints without logger! System.out.println("Factoring new Value for UserIdentityAttribute ID: '" + this.getUserIdentityAttributeId() + "', of IA name: '" + getIdentityAttribute().getUniqueName() + "', for User: '" + getUser().getName() + "'");
                UserIdentityAttributeValue valueToAdd = factoryValue();
                //no prints without logger! System.out.println("Factored new value of class type: '" + valueToAdd.getClass().getName() + "'");
                valueToAdd.setValue(currValue.getValueAsString());
                getValues().add(valueToAdd);
            } catch (UnsupportedAttributeTypeException uat) {
                throw new UnsupportedAttributeTypeException("Error while trying to add value into UserIdentityAttribute, message is: " + uat.getMessage());
            }
        }
        */
    }
    
    
    //Modify values by AV, only supports single value
    @Transient
    @Deprecated
    public void modifyValuesByAV(Collection<velo.storage.AttributeValue> values) throws UnsupportedAttributeTypeException {
    	/*JB
        if (this.getValues().size() == 0) {
            setValuesByAV(values);
        } else {
            UserIdentityAttributeValue fetchedVal = getValues().iterator().next();
            if (values.size() > 0) {
                fetchedVal.setValue(values.iterator().next().getValueAsString());
            }
        }
        */
    }
    
    
    @Deprecated
    public void modifyValuesByScriptedValidator(UserIdentityAttribute uia) throws AttributeValidationException {
    	/*
		 * JB try { IdentityAttributeValidator avs =
		 * (IdentityAttributeValidator)getIdentityAttribute().getScriptedValidator();
		 * avs.setIA(this.getIdentityAttribute());
		 * 
		 * Attribute attr = new Attribute();
		 * attr.setValuesByUserIdentityAttributeValues(uia.getValues());
		 * 
		 * avs.setInputAttribute(attr); //TODO switch to UIA,
		 * avs.setInputAttribute(uia);
		 * //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " +
		 * uia.getValues().size() +
		 * "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
		 * //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * OUT: " + getFirstValue().getAsString() +
		 * "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
		 * avs.__modify__();
		 * 
		 * if (uia.getValues().size() == 0) {
		 * setValuesByAV(avs.getOutputAttribute().getValues()); } else {
		 * UserIdentityAttributeValue fetchedVal =
		 * uia.getValues().iterator().next();
		 * fetchedVal.setValue(avs.getOutputAttribute().getValueAsString()); }
		 * 
		 * //setValuesByAV(avs.getOutputAttribute().getValues());
		 * //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * ATTR: " + uia.getValues().size() +
		 * "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------");
		 * //System.out.println("--------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * THIS: " + getFirstValue().getAsString() +
		 * "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_---------"); } catch
		 * (ScriptLoadingException sle) { //JB throw new
		 * AttributeValidationException("Script validation name: '" +
		 * getIdentityAttribute().getValidatorScriptFileName() + "' could not be
		 * loaded due to: " + sle.getMessage()); } //A lot of other exceptions
		 * may occure(Such as
		 * NullPointerException/groovy.lang.MissingMethodException/etc...) -
		 * important, we do not trust the script itself! catch (Exception npe) {
		 * throw new AttributeValidationException(npe.getMessage()); }
		 */
    }
    
}
