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
package velo.attributeValidators;

import velo.exceptions.ValidationException;

/**
 * The attribute validation interface for all attribute validators
 * 
 * @author Asaf Shakarchi
 */
public interface AttributeValidatorInterface {
	
	
	/**
	 * The method which execute the validation logic
	 * @return true/false upon success/failure of the validation process
	 */
	public void validate() throws ValidationException ;

	/**
	 * A method to decide whether the attribute is required or not
	 * @return true/false upon required/un-required
	 */
	boolean isRequired();
	
	//public void setValues(List<AttributeValue> values);
}