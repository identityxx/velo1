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

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.EventResponse;

@Name("eventResponseHome")
public class EventResponseHome extends EntityHome<EventResponse> {

	@In
	FacesMessages facesMessages;
	
	@In(value="#{eventDefinitionHome}")
	EventDefinitionHome eventDefinitionHome;
	
	public void setEventResponseId(Long id) {
		setId(id);
	}

	public Long getEventResponseId() {
		return (Long) getId();
	}

	@Override
	protected EventResponse createInstance() {
		EventResponse eventResponse = new EventResponse();
		return eventResponse;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public EventResponse getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	
	
	public String persist() {
		// set the resource
		getInstance().setEventDefinition(eventDefinitionHome.getInstance());
		//or refresh
		eventDefinitionHome.getInstance().addResponse(getInstance());
		
		return super.persist();
	}
	
	public String remove() {
		//or refresh
		eventDefinitionHome.getInstance().getEventResponses().remove(getInstance());
		
		return super.remove();
	}
}
