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

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.EventLogEntry;

@Name("eventLogEntryList")
public class EventLogEntryList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(eventLogEntry.module) like concat(lower(#{eventLogList.eventLogEntry.module}),'%')",
			"lower(eventLogEntry.level) like concat(lower(#{eventLogList.eventLogEntry.level}),'%')",};

	private EventLogEntry eventLogEntry = new EventLogEntry();
	private static final String EJBQL = "select eventLogEntry from EventLogEntry eventLogEntry";
	
	
	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public EventLogEntry getEventLogEntry() {
		return eventLogEntry;
	}

	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
}
