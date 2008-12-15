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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AttributeValueBoolean extends AttributeValue {
	
	private boolean value;
	
	/**
	 * @param value the value to set
	 */
	public void setValue(Boolean value) {
		this.value = value;
	}
	/**
	 * @return the value
	 */
	@Column(name="VALUE")
	public Boolean getValue() {
		return value;
	}
	
	public String getValueAsString() {
		return getValue().toString();
	}
	
	public Object getAsObject() {
		return value;
	}
}
