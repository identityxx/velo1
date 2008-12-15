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
package velo.storage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import velo.entity.RequestAttribute;
import velo.entity.RequestAttributeValue;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserIdentityAttributeValue;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.NoResultFoundException;

/**
 @author Asaf Shakarchi
 
 A class that represents an Attribute
 
 */
@Deprecated
public class Attribute implements Serializable, Cloneable {
    private static final long serialVersionUID = 197730545306161013L;
    
    /**
     Set an enum with the options of the identity attribute type
     */
    //public enum attributeDataTypeOptions {BOOLEAN,STRING,INTEGER};
    private String name;
    
    //The values of the attribute
    private Collection<AttributeValue> values = new ArrayList<AttributeValue>();
    
    //Doesnt work, must be serializeable, but Logger is not
    //Huh? I think it is ok when it is static.
    private static Logger logger = Logger.getLogger(Attribute.class.getName());
    
    
    
    /**
     Add a ValueObject to the attribute
     @param av the AttributeValue object to add
     */
    public void addValueObject(AttributeValue av) {
        if (isMultiple()) {
            values.add(av);
        } else {
            setValueObject(av);
        }
    }
    
    /**
     Set a ValueObject to the attribute
     (Clear all previous AttributeValue objects)
     
     @param value The value to set.
     */
    public void setValueObject(AttributeValue value) {
        getValues().clear();
        
        getValues().add(value);
    }
    
    
    
    
    /**
     Sets the value of the attribute from a given string
     Clean all previous values and set a new AttributeValueString object
     
     @param value The string value to set
     */
    public void setValueAsString(String value) {
        getValues().clear();
        
        getValues().add(getStringValue(value));
    }
    
    /**
     A proxy short method name to 'setValueAsString'
     @param value The value to set
     @see #setValueAsString(String)
     */
    public void setAsString(String value) {
        setValueAsString(value);
    }
    
    public AttributeValueString getStringValue(String value) {
        AttributeValueString avs = new AttributeValueString();
        avs.setValue(value);
        
        return avs;
    }
    
    public void addValueAsString(String value) {
        AttributeValueString avs = new AttributeValueString();
        avs.setValue(value);
        getValues().add(avs);
    }
    
    
    /**
     Sets the values of the attribute
     from a given long type (integer) value.
     
     Clean all previous values and set a new AttributeValueString object
     
     @param value A LONG value to set as string
     
     */
    public void setValueAsString(long value) {
        setValueAsString(String.valueOf(value));
    }
    
    
    
    
    
    /**
     Sets the value of the attribute from a given float parameter
     Clean all previous values and set a new AttributeValueFloat object
     
     @param value The FLOAT value to set
     */
    public void setValueAsFloat(float value) {
        getValues().clear();
        
        AttributeValueFloat avf = new AttributeValueFloat();
        avf.setValue(value);
        getValues().add(avf);
    }
    
    
    
        /*
         * Sets the value of the attribute
         * from a givem Date object values.
         *
         * @param value The value to set the attribute to
         *
        public void setValueAsDate(Date value) {
                setValueAsString(String.valueOf(value));
        }
         */
    
    
    /**
     Sets the value of the attribute from a given integer parameter
     Clean all previous values and set a new AttributeValueInt object
     
     Set the value of the attribute as an int
     @param value the Integer value to set
     */
    public void setValueAsInt(Integer value) {
        getValues().clear();
        getValues().add(getValueAsInt(value));
    }
    
    
    public AttributeValueInteger getValueAsInt(Integer value) {
        AttributeValueInteger avi = new AttributeValueInteger();
        avi.setValue(value);
        
        return avi;
    }
    
    public AttributeValueLong getValueAsLong(Long value) {
        AttributeValueLong avl = new AttributeValueLong();
        avl.setValue(value);
        
        return avl;
    }
    
    public void addValueAsInt(Integer value) {
        getValues().add(getValueAsInt(value));
    }
    
    public void addValueAsLong(Long value) {
        getValues().add(getValueAsLong(value));
    }
    
    /**
     Sets the value of the attribute from a given integer parameter
     Clean all previous values and set a new AttributeValueBigDecimal object
     
     @param value The value to set as BigDecimal
     */
    public void setValueAsBigDecimal(BigDecimal value) {
        getValues().clear();
        getValues().add(getValueAsBigDecimal(value));
    }
    
    public AttributeValueBigDecimal getValueAsBigDecimal(BigDecimal value) {
        AttributeValueBigDecimal avbd = new AttributeValueBigDecimal();
        avbd.setValue(value);
        
        return avbd;
    }
    
    public void addValueAsBigDecimal(BigDecimal value) {
        getValues().add(getValueAsBigDecimal(value));
    }
    
    
    public AttributeValueDate getValueAsDate(Date value) {
        AttributeValueDate avd = new AttributeValueDate();
        avd.setValue(value);
        
        return avd;
    }
    
    public void addValueAsDate(Date value) {
        getValues().add(getValueAsDate(value));
    }
    
    /**
     Sets the value of the attribute from a given boolean parameter
     Clean all previous values and set a new AttributeValueBoolean object
     
     @param value The value to set as Boolean
     */
    public void setValueAsBoolean(Boolean value) {
        getValues().clear();
        getValues().add(getValueAsBoolean(value));
    }
    
    public AttributeValueBoolean getValueAsBoolean(Boolean value) {
        AttributeValueBoolean avb = new AttributeValueBoolean();
        avb.setValue(value);
        
        return avb;
    }
    
    public void addValueAsBoolean(Boolean bool) {
        getValues().add(getValueAsBoolean(bool));
    }
    
    
    
    /**
     Set the value of the attribute as an int
     @param object the value to set as an object
     */
    public void addValueByObj(Object object) throws AttributeSetValueException {
        //Determine the type of the object
        if (object != null) {
            if (object.getClass().getName().equals("java.lang.Integer")) {
                Integer i = (Integer) object;
                addValueAsInt(i);
            } else if (object.getClass().getName().equals("java.lang.String")) {
                String s = (String) object;
                addValueAsString(s);
            } else if (object.getClass().equals(Long.class)) {
                Long l = (Long) object;
                addValueAsLong(l);
                //Oracle returned 'BigDecimal' typed, added support for that too
            } else if (object.getClass().getName().equals("java.math.BigDecimal")) {
                BigDecimal bd = (BigDecimal) object;
                addValueAsBigDecimal(bd);
            } else if (object.getClass().getName().equals("java.lang.Boolean")) {
                Boolean bool = (Boolean) object;
                addValueAsBoolean(bool);
            } else if (object instanceof Date) {
                Date val = (Date) object;
                addValueAsDate(val);
            } else {
                String errMsg = "Warning, could not set value of class type: " + object.getClass().getName();
                logger.warning(errMsg);
                throw new AttributeSetValueException(errMsg);
            }
        } else {
            throw new AttributeSetValueException("Warning, could not set value of NULL for attribute name: "
                + this.getName());
        }
    }
    
    
    
    public String getAsString() {
        if (getValues().size() > 0) {
            return getValues().iterator().next().getValueAsString();
        } else {
            return "";
        }
    }
    
    public String getValueAsString() {
        return getAsString();
    }
    
    
    /**
     Get indicator whether the attribute is multiple value or not
     @return true/false upon multiple / not multiple
     */
    public boolean isMultiple() {
        if (getValues().size() > 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public AttributeValue getFirstValue() throws NoResultFoundException {
        if (getValues().size() < 1) {
            throw new NoResultFoundException("No Values found for Attribute: '" + getName() + "'");
        } else {
            return getValues().iterator().next();
        }
    }
    
    
    
    /**
     Set the name of the attribute
     @param name The name of the attribute to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     Get the name of the attribute
     @return The name of the attribute
     */
    public String getName() {
        return name;
    }
    
    
    /**
     @return Returns the value.
     */
    public AttributeValue getFirstValueObject() {
        if (getValues().size() > 0) {
            return getValues().iterator().next();
        } else {
            //TODO: Never return nulls!
            return null;
        }
    }
    
    /**
     Set a collection of values to this attribute
     @param values The values to set.
     */
    public void setValues(Collection<AttributeValue> values) {
        this.values = values;
    }
    
    /**
     Get the collection of values of this Attribute
     @return Returns the values.
     */
    public Collection<AttributeValue> getValues() {
        return values;
    }
    
    /**
     Set the value of this attribute by a UserIdentityAttribute entity
     @param attr The UserIdentityAttribute entity to set the value from
     */
    public void setValueByUserIdentityAttribute(UserIdentityAttribute uia) {
        setValuesByUserIdentityAttributeValues(uia.getValues());
    }
    
    public void setValuesByUserIdentityAttributeValues(Collection<UserIdentityAttributeValue> values) {
        /*
                //If user identity attribute has multiple values then indicate that the created attribute is multiple and set the collecton of values
                getValues().addAll(attr.getUserIdentityAttributeValues());
                } else if (attr.getUserIdentityAttributeValues().size() == 1) {
                        setValueAsString(attr.getUserIdentityAttributeValues().iterator().next()
                                        .getValue());
                } else {
                        System.out
                                        .println("Couldnt set value! UserIdentityAttribute name: "
                                                        + attr.getName() + ", for user: "
                                                        + attr.getUser().getName()
                                                        + ", has no even -one- value !");
                }
         */
        
        for (velo.entity.UserIdentityAttributeValue currUIAV : values) {
            try {
                addValueByObj(currUIAV.getValue());
            } catch (AttributeSetValueException asve) {
                logger.warning("A Set Value vaiolation has occured for one of the values, skipping value, detailed message is: " + asve.getMessage());
                continue;
            }
        }
        //Get only one value for now :-/
        //setValueAsString(attr.getValues().iterator().next().getAsString());
        
    }
    
    
    /**
     Compares the value of the attribute
     to another attribute object
     
     Comparing is done on the STRING level.
     
     @param attr The attribute to compare this attribute with
     @return True if the values are equale,
     false if not
     */
    public boolean compareValues(Attribute attr) {
        //If sizes are not equal, then there are differences between values.
        if (getValues().size() != attr.getValues().size()) {
            return false;
        }
        
        //Per value of this object, compare to the value specified within the attr's values parameters.
        for (AttributeValue currValueOfThisAttribute : getValues()) {
            boolean foundValueForCurrentIteration = false;
            
            //TODO Comparing only supported by strings for now!
            for (AttributeValue currValueOfTargetAttribute : attr.getValues()) {
                if (currValueOfThisAttribute.getValueAsString().equalsIgnoreCase(currValueOfTargetAttribute.getValueAsString())) {
                    foundValueForCurrentIteration = true;
                    break;
                }
            }
            
            if (!foundValueForCurrentIteration) {
                return false;
            }
        }
        
        return true;
    }
    
    
    
        /*
         * Returns the value of the attribute as an int.
         *
         * @return An int value if its an int or float attribute
         *         ,or a string attribute holding a number.
         *         (rounded to nearest int if its a float)
         *         Or zero if its not.
         *
         * @throws MultipleAttributeValueVaiolation
         *
        public int getIntValue() throws MultipleAttributeValueVaiolation {
                //TODO: should throw an exception if the value cannot get converted to this type
                if (isMultiple()) {
                        throw new MultipleAttributeValueVaiolation(
                                        "Vaiolation occured for attribute name: " + getName()
                                                        + ", this attribute has: " + getValues().size()
                                                        + " values.");
                }
                try {
                        return Integer.parseInt(getValueAsString());
                } catch (NumberFormatException nfe) {
                        System.out.println("Cannot convert attribute name: " + getName()
                                        + ", with value: " + getValueAsString() + ", Exception message: "
                                        + nfe.getMessage());
                        return 0;
                }
        }
         */
    
        /*
         * Returns the value of the attribute as a float.
         * Returns 0.0 if value is not of the right type.
         *
         * @return A float value if this is a float or int
         *         ,or a string attribute holding a number.
         *         , 0 if its not.
         * @throws MultipleAttributeValueVaiolation
         *
        public float getFloatValue() throws MultipleAttributeValueVaiolation {
                if (isMultiple()) {
                        throw new MultipleAttributeValueVaiolation(
                                        "Vaiolation occured for attribute name: " + getName()
                                                        + ", this attribute has: " + getValues().size()
                                                        + " values.");
                }
                return Float.parseFloat(getValueAsString());
        }
         */
    
    
        /*
         * Returns the value of the attribute as a long.
         *
         * @return A long value if this is a long or int
         *         ,or a string attribute holding a number.
         *         , 0 if its not.
         * @throws MultipleAttributeValueVaiolation
         *
        public long getLongValue() throws MultipleAttributeValueVaiolation {
                if (isMultiple()) {
                        throw new MultipleAttributeValueVaiolation(
                                        "Vaiolation occured for attribute name: " + getName()
                                                        + ", this attribute has: " + getValues().size()
                                                        + " values.");
                }
                return Long.parseLong(getValueAsString());
        }
         */
    
    
    
        /*
         * Returns the value of the attribute
         * as a string.
         * If its a number value, will return a string
         * representation of the number.
         *
         * If its a date, will return it in a unix format date.
         *
         * @return A string contaning the value of the attribute
         *         in the right format.
         *
         * @throws MultipleAttributeValueVaiolation
         *
        public String getStringValue() throws MultipleAttributeValueVaiolation {
                if (isMultiple()) {
                        throw new MultipleAttributeValueVaiolation(
                                        "Vaiolation occured for attribute name: " + getName()
                                                        + ", this attribute has: " + getValues().size()
                                                        + " values.");
                }
         
                return getValueAsString();
        }
         */
    
    
    
    
    
    
        /*
         * Returns the values of the attribute as date.
         *
         * @return A date object if the atribute is of
         *         date type, null if not.
         */
    //public Date getDateValue();
    
    
    
    
        /*
         * Clone was added for the purpose when a scripted converter is needed,
         * the userIdentityAttribute(which is a subclass of this class) object is set as 'source'
         * Since it set as a reference, modifying the userIdentityAttribute
         * entity in script causes DB modifications
         * (even when merge is not in use! not sure why, so we'll use clone() before setting the source userIdentityAttribute in scripted converter!)
         */
        /*
        public Attribute clone() {
                try {
                        return (Attribute) super.clone();
                } catch (CloneNotSupportedException e) {
                        return null;
                }
        }
         */
    
    public boolean isEmpty() {
        for (AttributeValue currValue : getValues()) {
            if (currValue.getValueAsString().length() > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    
    public static Attribute factoryByRequestAttribute(RequestAttribute ra) throws AttributeSetValueException {
        Attribute attribute = new Attribute();
        
        logger.finest("Factoring Attribute by RequestAttribute with values amount: '" + ra.getValues().size() + "'");
        attribute.setName(ra.getUniqueName());
        for (RequestAttributeValue currRAV : ra.getRequestAttributeValues()) {
            attribute.addValueByObj(currRAV.getValueAsObject());
        }
        
        return attribute;
    }
}