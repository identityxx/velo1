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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.validator.NotNull;

import velo.contexts.OperationContext;
import velo.exceptions.AttributeSetValueException;

@Entity
@DiscriminatorValue(value = "TEXTUAL")
public class RoleResourceAttributeAsTextual extends RoleResourceAttribute {
	private static Logger log = Logger.getLogger(RoleResourceAttributeAsTextual.class.getName());
	
	private String value;

	public RoleResourceAttributeAsTextual() {
		
	}
	
	public RoleResourceAttributeAsTextual(Role role, ResourceAttribute resourceAttribute) {
		super(role,resourceAttribute);
	}
	
	/**
	 * @return the value
	 */
	//damn, must be null otherwise oracle creates this attr as must
	@Column(name = "VALUE")
	@NotNull
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
	@Transient
	public String getType() {
		return "TEXTUAL";
	}
	
	
	public ResourceAttribute calculateAttributeResult(OperationContext context) {
		log.trace("Calculating value for role resource attribute(textual) named '" + getDescription() + "'");
		try {
			//TODO: HOLY CRAP!
			//holy crap, how this attr has the value of the resource attr by 'resource attribute' level already?
			//it wasn't set by reference as we use 'getResourceAttribute()' which has its own instance!!@#$! wtf?
			//probably everything here is passing by reference from the same 'resource' entity :)
			//gee, this is very scary...
			//TODO: Support behaviors!
			getResourceAttribute().setValue(getValue());
			return getResourceAttribute();
		}catch (AttributeSetValueException e) {
			log.error(e.toString() + ", returning null.");
			return null;
		}
	}
	
	@Override
    public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof RoleResourceAttribute))
			return false;
		RoleResourceAttribute ent = (RoleResourceAttribute) obj;
		if (this.getPrimaryKey().equals(ent.getPrimaryKey()))
			return true;
		return false;
    }
}
