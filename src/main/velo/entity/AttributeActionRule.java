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

import groovy.lang.GroovyObject;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;

//WHAT THE HELL IS THAT CLASS?
@Entity 
@DiscriminatorValue("ATTRIBUTE_ACTION_RULE")
public abstract class AttributeActionRule extends ActionRule {
	private static final long serialVersionUID = 1L;
	private static transient Logger log = Logger.getLogger(AttributeActionRule.class.getName());
	
	@Override
	public void updateScriptedObject(GroovyObject scriptedObject) {
		scriptedObject.setProperty("log", log);
	}
}
