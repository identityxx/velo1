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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.entity.Attribute.AttributeDataTypes;
import velo.exceptions.AttributeSetValueException;

/**
 * An entity that represents a Value of an Attribute
 * 
 * @author Asaf Shakarchi
 */
@MappedSuperclass
public class AttributeValue extends BaseEntity implements Serializable { 

	private static final long serialVersionUID = 1987302492306161413L;
	private static transient Logger log = Logger.getLogger(AttributeValue.class.getName());
	
	private Integer valueInt;
	private Long valueLong;
	private Date valueDate;
	private String valueString;
	private Boolean valueBoolean;
	private AttributeDataTypes dataType;

	public AttributeValue() {
		
	}
	
	public AttributeValue(AttributeDataTypes dataType) {
		setDataType(dataType);
	}
	
	public AttributeValue(Object value) throws AttributeSetValueException {
		setValue(value);
	}
	
	
	/**
	 * @return the valueInt
	 */
	@Column(name="VALUE_INT", nullable=true)
	public Integer getValueInt() {
		return valueInt;
	}




	/**
	 * @param valueInt the valueInt to set
	 */
	public void setValueInt(Integer valueInt) {
		if (getDataType() == null) setDataType(AttributeDataTypes.INTEGER);
		this.valueInt = valueInt;
	}




	/**
	 * @return the valueLong
	 */
	@Column(name="VALUE_LONG", nullable=true)
	public Long getValueLong() {
		return valueLong;
	}




	/**
	 * @param valueLong the valueLong to set
	 */
	public void setValueLong(Long valueLong) {
		if (getDataType() == null) setDataType(AttributeDataTypes.LONG);
		this.valueLong = valueLong;
	}




	/**
	 * @return the valueDate
	 */
	@Column(name="VALUE_DATA", nullable=true)
	public Date getValueDate() {
		return valueDate;
	}




	/**
	 * @param valueDate the valueDate to set
	 */
	public void setValueDate(Date valueDate) {
		if (getDataType() == null) setDataType(AttributeDataTypes.DATE);
		this.valueDate = valueDate;
	}




	/**
	 * @return the valueString
	 */
	@Column(name="VALUE_STRING", nullable=true)
	public String getValueString() {
		return valueString;
	}




	/**
	 * @param valueString the valueString to set
	 */
	public void setValueString(String valueString) {
		if (getDataType() == null) setDataType(AttributeDataTypes.STRING);
		this.valueString = valueString;
	}


	/**
	 * @return the valueBoolean
	 */
	@Column(name="VALUE_BOOLEAN", nullable=true)
	public Boolean getValueBoolean() {
		return valueBoolean;
	}



	/**
	 * @param valueBoolean the valueBoolean to set
	 */
	public void setValueBoolean(Boolean valueBoolean) {
		if (getDataType() == null) setDataType(AttributeDataTypes.BOOLEAN);
		this.valueBoolean = valueBoolean;
	}


	/**
	 * @return the dataType
	 */
	@Column(name="DATA_TYPE", nullable=false)
	@Enumerated(EnumType.STRING)
	public AttributeDataTypes getDataType() {
		return dataType;
	}



	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(AttributeDataTypes dataType) {
		this.dataType = dataType;
	}

	@Transient
    public String getDisplayableDataType() {
        return getDataType().toString();
    }
	
	
	//value utilities - mostly used by the Attribute Rules
	@Transient
    public String getAsString() {
		log.trace("Invoked 'asString' over value...");
		if (getValueAsObject() != null) {
			return getValueAsObject().toString();
		}
		else {
			log.warn("'asString' invoked over a null object, returning null");
			return null;
		}
	}
	
	@Transient
	public Integer getAsInt() {
		log.trace("Getting value as 'INT' invoked, parsing string value as int...");
		String strValue = getAsString();
		if (strValue == null) {
			log.trace("Could not return value as INT since it doesnt have a value (getAsString() returned null!)");
			return null;
		} else { 
			return Integer.parseInt(getAsString());
		}
	}
	
	@Transient
	public Boolean getAsBoolean() {
		return Boolean.parseBoolean(getAsString());
	}
	
	@Transient
	public Long getAsLong() {
		return Long.parseLong(getAsString());
	}
	
	

	public void setValue(Object value) throws AttributeSetValueException {
		if (value == null) {
			log.info("Could not set value for attribute, value is null...");
			return;
		}
		
		if (getDataType() != null) {
			log.trace("Setting value '" + value + "' with dataType : " + getDataType());
			
			switch(getDataType()) {
				case INTEGER: {
					setValueInt(Integer.parseInt(value.toString())); 
					break;
				}
				case STRING: {
					setValueString(value.toString());
					break;
				}
				case BOOLEAN: {
					setValueBoolean(Boolean.parseBoolean(value.toString()));
					break;
				}
				case LONG: {
					setValueLong(Long.parseLong(value.toString()));
					break;
				}
				case DATE: {
					if (value instanceof Date) {
						setValueDate((Date)value);
					}
					else {
					//TODO: LOG? WARNING? EXCPETION?
					}
					break;
				}
				default: {
					log.error("This should never happen, specified a dataType '" + getDataType() + "' which is not supported!");
				}
			}
		}
		else {
			if (value != null) {
				log.trace("no DataType set, trying to determine what datatype the object class '" + value.getClass().getName() + "' is");
			}
			if (value instanceof String) {
				log.trace("String type!");
				setValueString((String)value);
			}
			else if (value instanceof Long) {
				log.trace("Long type!");
				setValueLong((Long)value);
			}
			else if (value instanceof Integer) {
				log.trace("Integer type!");
				setValueInt((Integer)value);
			}
			else if (value instanceof Date) {
				log.trace("Date type!");
				setValueDate((Date)value);
			}
			else if (value instanceof Timestamp) {
				log.trace("Timestamp(Date) type!");
				setValueDate((Date)value);
			}
			else if (value instanceof Boolean) {
				log.trace("Boolean type!");
				setValueBoolean((Boolean)value);
			}
			else {
				log.warn("Could not set the value, the specified object of class type '" + value.getClass().getName() + "' is not supported!");
				//throw new AttributeSetValueException("Cannot set value, specified object of class type '" + value.getClass().getName() + " is not supported.");
			}
		}
	}
	public void setValueAsObject(Object value) throws AttributeSetValueException {
		setValue(value);
	}
	
	
	@Transient
	public Object getValueAsObject() {
		log.trace("Getting value as object for dataType '" + getDataType() + "'");
		switch(getDataType()) {
			case INTEGER: return getValueInt(); 
			case STRING: return getValueString();
			case BOOLEAN: return getValueBoolean();
			case LONG: return getValueLong();
			case DATE: return getValueDate();
			default: {
				System.out.println("DATA TYPE IS UNKNOWN, RETURNING NULL"); 
				return null;
			}
		}
	}
	
	//Important for JSF
	@Transient
	public String getValue() {
		return getAsString();
	}
	
	
	@Transient 
	public AttributeValue factoryFactory(AttributeDataTypes dataType) {
		AttributeValue attrValue = new AttributeValue();
		attrValue.setDataType(dataType);
		
		return attrValue;
	}
	
	
	@Transient
	//TODO: Is string comparation safe enough?
	public boolean compareValue(AttributeValue attrValue) {
		//Handle null values
		if ( (isNull()) || (attrValue.isNull()) ) {
			return (attrValue.getValue() == getValue());
		}
		
		log.trace("Is value '" + getValue().toString() + "' is equal to specified attr value '" + attrValue.getValue().toString() + "'?");
		if (getValue().toString().equals(attrValue.getValue().toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Transient
	public boolean isNull() {
		return getValue() == null;
	}
	
	
	
	//==Mainly for gui, set by the UIInput with converter to the right data-type attached
	public void setValue(String value) {
		log.trace("Invoked setValue(STRING) with value '" + value + "'");
		//a workaround !!!!!!!!!!!!!!!!!!!
		try {
			setValueAsObject(value);
		} catch (AttributeSetValueException e) {
			log.warn("Could not set value '" + value + "'!!!");
		}
	}
	
	public void setValue(Long value) {
		log.trace("Invoked setValue(LONG) with value '" + value + "'");
		setValueLong(value);
	}
	
	public void setValue(Integer value) {
		log.trace("Invoked setValue(INT) with value '" + value + "'");
		setValueInt(value);
	}
	
	public void setValue(Date date) {
		log.trace("Invoked setValue(DATE) with value '" + date.toString() + "'");
		setValueDate(date);
	}
	
	public void setValue(Boolean bool) {
		log.trace("Invoked setValue(BOOLEAN) with value '" + bool + "'");
		setValueBoolean(bool);
	}
	
	
	
	
	
	
	
	
	
	//used by integration rule scripts
	public void toProperCase() throws IOException{
		if (getDataType() != AttributeDataTypes.STRING) {
			log.warn("Could not invoke 'toProperCase' over attribue value since its type is not a STRING");
		} else {
		
			StringReader in = new StringReader(getAsString().toLowerCase());
			boolean precededBySpace = true;
			StringBuffer properCase = new StringBuffer();    
			while(true) {      
				int i = in.read();
				if (i == -1)  break;      
				char c = (char)i;      
				if (c == ' ') {
					properCase.append(c);
					precededBySpace = true;
				}
				else {
					if (precededBySpace) { 
						properCase.append(Character.toUpperCase(c));
					}
					else { 
						properCase.append(c); 
					}
					precededBySpace = false;
				}
			}
		
			setValue(properCase.toString());
		}
	}
	
}




	
