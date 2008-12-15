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
package velo.converters;

import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.storage.Attribute;

/**
 * Account Converter
 * A class for converting account attributes
 * 
 * @author Asaf Shakarchi
 */
public abstract class AccountConverter extends Converter implements
		AccountAttributeConverterInterface {
	
	private User user;
	private Resource resource;
	private ResourceAttribute resourceAttribute;
	
	protected AccountConverterTools tools = new AccountConverterTools();

	/** 
	 * Holds a referance to the source object 
	 * (the object we want to convert FROM)
	 */
	private UserIdentityAttribute sourceUserAttribute;

	/** Holds the result of the conversion process*/
	//Initializing here so attribute will be ready for use in scripted classes
	private Attribute resultAttribute = new Attribute();

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
	public void setResource(Resource ts) {
		resource = ts;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResourceAttribute(ResourceAttribute tsa) {
		resourceAttribute = tsa;
	}
	
	public ResourceAttribute getResourceAttribute() {
		return resourceAttribute;
	}
	

	public void setSourceUserAttribute(UserIdentityAttribute sourceUserAttribute) {
		this.sourceUserAttribute = sourceUserAttribute;
	}

	public UserIdentityAttribute getSourceUserAttribute() {
		return sourceUserAttribute;
	}

	
	public void setResultAttribute(Attribute resultAttribute) {
		this.resultAttribute = resultAttribute;
	}

	public Attribute getResultAttribute() {
		return resultAttribute;
	}

	/**
	 * logs the preConvertValidate function
	 * and runs it
	 * 
	 * @see #preConvertValidate()
	 * @return the preConvertValidate result
	 */
	/*
	 public boolean __preConvertValidate__()
	 {
	 //log validate call
	 boolean bResult=preConvertValidate();
	 //log validate result
	 return bResult;
	 }
	 */

	/**
	 * Makes sure the gives source object is in the 
	 * right form and contains valid data.
	 * 
	 * Uses the source object's validate function to test it.
	 * 
	 * @return boolean if the source object validate passed ok
	 *         false if the object has wrong values.
	 */
	/*
	 public boolean preConvertValidate() {
	 if (getSourceAttribute()==null) return false;
	 return  getSourceAttribute().validate();
	 }
	 */

	/**
	 * logs the postConvertValidate function
	 * and runs it.
	 * 
	 * @see #postConvertValidate()
	 * @return the postConvertValidate result
	 */
	/*
	 public boolean __postConvertValidate__()
	 {
	 //log validate call
	 boolean bResult=postConvertValidate();
	 //log validate result
	 return bResult;
	 }
	 */

	/**
	 * Validates that the resulte field 
	 * has the right type of value in it,
	 * and that value is valid for the field.
	 * 
	 * Uses the fields validate function.
	 * 
	 * @return true if the value is ok, false if its not.
	 */
	/*
	 public boolean postConvertValidate()
	 {
	 return this.resultAttribute.validate();
	 }
	 */

	/**
	 * logs the convert function
	 * and runs it.
	 * 
	 * @see #convert()
	 * @return an attribute which is the result of the conversion,
	 *         or null if it failed.
	 */
	/*
	 public Attribute __convert__()
	 {
	 //log convert start
	 resultAttribute=createResultAttribute();
	 convert(sourceObject);
	 //log convert result
	 return resultAttribute;
	 }
	 */

	/**
	 * Hold the conversion code that extracts the
	 * target attribute from teh source object
	 * 
	 * @param sourceObject a referance to the localy 
	 *                     stored source object. 
	 * @return true if the conversion was a success, false if it failed
	 */
	//public abstract boolean convert(CollectionDataObject sourceObject);

}
