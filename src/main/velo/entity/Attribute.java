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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.exceptions.AttributeSetValueException;

/**
 * A class that represents an attribute
 * 
 * @author Asaf Shakarchi
 */

@NamedQueries( 
	{
		@NamedQuery(name = "attribute.findByUniqueName", query = "SELECT object(attr) FROM Attribute AS attr WHERE attr.uniqueName = :uniqueName"),
		@NamedQuery(name = "attribute.findByDisplayName", query = "SELECT object(attr) FROM Attribute AS attr WHERE attr.displayName = :displayName") 
	}
)
@MappedSuperclass
public class Attribute extends BaseEntity implements Serializable {
	private static transient Logger log = Logger.getLogger(Attribute.class.getName());
	
	public Attribute() {
		
	}
	
	public Attribute(String uniqueName, String displayName, String description, AttributeDataTypes dataType, boolean required, boolean managed, int minLength, int maxLength, int minValues, int maxValues) {
		setUniqueName(uniqueName);
		setDisplayName(displayName);
		setDescription(description);
		setDataType(dataType);
		setRequired(required);
		setManaged(managed);
		setMinLength(minLength);
		setMaxLength(maxLength);
		setMinValues(minValues);
		setMaxValues(maxValues);
	}
	
	private static final long serialVersionUID = 1L;

	/**
	 * Set an ENUM with the attribute data types
	 */
	public enum AttributeDataTypes {
		BOOLEAN, STRING, INTEGER, LONG, DATE
	}

	private String uniqueName;

	private String displayName;

	private String description;

	private AttributeDataTypes dataType;

	private boolean required = false;
	
	private boolean managed = true;

	private int minLength = 1;
	
	private int maxLength = 255;
	
	private int minValues = 1;

	private int maxValues = 1;
	
	private String regexpValidationString;
	
	
	//Transient
	List<AttributeValue> transientValues = new ArrayList<AttributeValue>();

	/**
	 * @return the uniqueName
	 */
	@Column(name="UNIQUE_NAME", nullable=false)
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param uniqueName the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * @return the displayName
	 */
	@Column(name="DISPLAY_NAME", nullable=false)
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION", nullable=true)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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

	/**
	 * @return the required
	 */
	@Column(name="REQUIRED", nullable=false)
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return the managed
	 */
	@Column(name="MANAGED", nullable=false)
	public boolean isManaged() {
		return managed;
	}

	/**
	 * @param managed the managed to set
	 */
	public void setManaged(boolean managed) {
		this.managed = managed;
	}

	/**
	 * @return the minLength
	 */
	@Column(name="MIN_LENGTH", nullable=false)
	public int getMinLength() {
		return minLength;
	}

	/**
	 * @param minLength the minLength to set
	 */
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	/**
	 * @return the maxLength
	 */
	@Column(name="MAX_LENGTH", nullable=false)
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return the minValues
	 */
	@Column(name="MIN_VALUES", nullable=false)
	public int getMinValues() {
		return minValues;
	}

	/**
	 * @param minValues the minValues to set
	 */
	public void setMinValues(int minValues) {
		this.minValues = minValues;
	}

	/**
	 * @return the maxValues
	 */
	@Column(name="MAX_VALUES", nullable=false)
	public int getMaxValues() {
		return maxValues;
	}

	/**
	 * @param maxValues the maxValues to set
	 */
	public void setMaxValues(int maxValues) {
		this.maxValues = maxValues;
	}

	/**
	 * @return the regexpValidationString
	 */
	@Column(name="REGEXP_VALIDATION_STRING")
	public String getRegexpValidationString() {
		return regexpValidationString;
	}

	/**
	 * @param regexpValidationString the regexpValidationString to set
	 */
	public void setRegexpValidationString(String regexpValidationString) {
		this.regexpValidationString = regexpValidationString;
	}
	
	
	public void copyValues(Object entity) {
		//TODO: Implement
	}
	
	
	
	//Helper
	public AttributeValue factoryValue() {
		
		AttributeValue attrValue = new AttributeValue(getDataType());
		return attrValue;
	}
	
	public void addValue(Object value) throws AttributeSetValueException {
		if (value != null) {
			AttributeValue attrValue = new AttributeValue(value);
			getTransientValues().add(attrValue);
		} else {
			log.info("Recieved a null value to set, skipping value insertion to attribute");
		}
	}
	
	public void addValue(Object value, AttributeDataTypes dataType) throws AttributeSetValueException {
		AttributeValue attrValue = new AttributeValue(value);
		attrValue.setDataType(dataType);
		getTransientValues().add(attrValue);
	}
	
	//used by active data importer(digester) 
	public void addValue(AttributeValue value) {
		getTransientValues().add(value);
	}
	

	/**
	 * @return the values
	 */
	@Transient
	public List<AttributeValue> getTransientValues() {
		return transientValues;
	}

	/**
	 * @param values the values to set
	 */
	public void setTransientValues(List<AttributeValue> transientValues) {
		this.transientValues = transientValues;
	}
	
	
	
	
	
	
	//a proxy method for 'getTransientValues', should be only used by groovy when the Integrator writes AttributeRules
	@Transient
	public List<AttributeValue> getValues() {
		return transientValues;
	}
	
	//for easier value set, without manging multiple values stuff (for AttributeRule expressions)
	//hopefully exception will be catched by groovy expression
	@Transient
	public void setValue(Object value) throws AttributeSetValueException {
		getValues().clear();
		addValue(value);
	}
	
	
	@Transient
	public AttributeValue getFirstValue() {
		if (getTransientValues().size() > 0) {
			return getTransientValues().iterator().next();
		}
		else {
			return null;
		}
	}
	
	@Transient
	public String getFirstValueAsString() {
		if (getFirstValue() != null) {
			return getFirstValue().getAsString();
		}
		
		return null;
	}
	
	@Transient
	public boolean isHasValues() {
		return getTransientValues().size()!=0;
	}
	@Transient
	public boolean isFirstValueIsNotNull() {
		if (getTransientValues().size()!=0) {
			return getTransientValues().iterator().next().getValue() != null;
		} else {
			return false;
		}
	}
	
	@Transient
	public String getDisplayable() {
		StringBuffer sb = new StringBuffer();
		sb.append("Attribute uniqueName: '" + getUniqueName() + "'");
		
		if (!isHasValues()) {
			sb.append(", VALUES: attribute has no values!");
		}
		else {
			for (AttributeValue currValue : getTransientValues()) {
				sb.append(", VALUE: [" + currValue.getAsString() + ":" + currValue.getDataType().toString() + "],");
			}
		}
		
		return sb.toString();
	}
	
	
	
	
	
	
	
	
	
	
	public boolean compareValues(Attribute attr) {
		//TODO: SHOULD IMPLEMENTED, NEEDED BY SYNCACCOUNTATTRIUTESEVENT of REconcile process!
		return true;
	}
}