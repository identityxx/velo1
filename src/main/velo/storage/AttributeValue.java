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

import javax.persistence.MappedSuperclass;

/**
 * @author Asaf Shakarchi
 * 
 * A class that represents an Attribute
 *
 */
@MappedSuperclass
public abstract class AttributeValue implements Serializable, Cloneable {
	public AttributeValue() {
		
	}
	
	private static final long serialVersionUID = 197730545306161013L;
	
	public abstract String getValueAsString();

	public abstract Object getAsObject();
}