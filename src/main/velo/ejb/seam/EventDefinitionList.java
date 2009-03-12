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
package velo.ejb.seam;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.EventDefinition;

@Name("eventDefinitionList")
public class EventDefinitionList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(eventDefinition.eventDefinitionId) = #{eventDefinitionList.eventDefinition.eventDefinitionId}",
			"lower(eventDefinition.uniqueName) like concat(lower(#{eventDefinitionList.eventDefinition.uniqueName}),'%')",
			"lower(eventDefinition.displayName) like concat(lower(#{eventDefinitionList.eventDefinition.displayName}),'%')",
			"lower(eventDefinition.description) like concat(lower(#{eventDefinitionList.eventDefinition.description}),'%')"};

	private EventDefinition eventDefinition = new EventDefinition();

	private static final String EJBQL = "select eventDefinition from EventDefinition eventDefinition";
	
	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public EventDefinition getEventDefinition() {
		return eventDefinition;
	}

	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
}
