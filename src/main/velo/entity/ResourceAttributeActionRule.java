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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;

@Entity 
@DiscriminatorValue("RESOURCE_ATTRIBUTE_ACTION_RULE")
public class ResourceAttributeActionRule extends ActionRule {
	private static final long serialVersionUID = 1L;
	
	private static transient Logger log = Logger.getLogger(ResourceAttributeActionRule.class.getName());
	private ResourceAttributeBase resourceAttribute;

	/**
	 * @return the resourceAttribute
	 */
	//see explaination of optional=true/nullable=true in ResourceAction->getResource()
	@ManyToOne(optional=true)
	//removed 'set nullable=true' bcz oracle (<--- WHY? HAD TO MAKE IT TRUE OTHERWISE COLUMN IS CREATED AS NOT NULL)
    @JoinColumn(name="RESOURCE_ATTRIBUTE_ID", nullable=true)
	public ResourceAttributeBase getResourceAttribute() {
		return resourceAttribute;
	}

	/**
	 * @param resourceAttribute the resourceAttribute to set
	 */
	public void setResourceAttribute(ResourceAttributeBase resourceAttribute) {
		this.resourceAttribute = resourceAttribute;
	}
	
	
	@Override
	public void updateScriptedObject(GroovyObject scriptedObject) {
		scriptedObject.setProperty("log", log);
	}
}
