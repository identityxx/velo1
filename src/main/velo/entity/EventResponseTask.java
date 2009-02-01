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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.ejb.impl.EventBean;
import velo.exceptions.ObjectFactoryException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@Entity
@DiscriminatorValue("EVENT_RESPONSE")
public class EventResponseTask extends Task implements Serializable {
	private static Logger log = Logger.getLogger(EventResponseTask.class.getName());
	private Request requestRef;
	private Task taskRef;
	private User userRef;
	private EventResponse eventResponse;
	private String context;
	
	
	/**
	 * @return the requestRef
	 */
	@ManyToOne(optional=true)
    @JoinColumn(name = "REQUEST_REF_ID", nullable = true)
	public Request getRequestRef() {
		return requestRef;
	}
	
	/**
	 * @param requestRef the requestRef to set
	 */
	public void setRequestRef(Request requestRef) {
		this.requestRef = requestRef;
	}
	
	/**
	 * @return the taskRef
	 */
	@ManyToOne(optional=true)
    @JoinColumn(name = "TASK_REF_ID", nullable = true)
	public Task getTaskRef() {
		return taskRef;
	}
	
	/**
	 * @param taskRef the taskRef to set
	 */
	public void setTaskRef(Task taskRef) {
		this.taskRef = taskRef;
	}
	
	/**
	 * @return the userRef
	 */
	@ManyToOne(optional=true)
    @JoinColumn(name = "USER_REF_ID", nullable = true)
	public User getUserRef() {
		return userRef;
	}
	
	/**
	 * @param userRef the userRef to set
	 */
	public void setUserRef(User userRef) {
		this.userRef = userRef;
	}

	
	
	/**
	 * @return the eventResponse
	 */
	@ManyToOne(optional=true)
    @JoinColumn(name = "EVENT_RESPONSE_ID", nullable = true)
	public EventResponse getEventResponse() {
		return eventResponse;
	}

	/**
	 * @param eventResponse the eventResponse to set
	 */
	public void setEventResponse(EventResponse eventResponse) {
		this.eventResponse = eventResponse;
	}
	
	

	/**
	 * @return the context
	 */
	@Column(name="SERIALIZED_CONTEXT")
	@Lob
	public String getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	public static EventResponseTask factory(EventResponse eventRes, OperationContext context) throws ObjectFactoryException {
		log.debug("(1)Factoring event response task '" + eventRes.getDescription() + "', of event: '" + eventRes.getEventDefinition().getDisplayName() + "'");
		EventResponseTask ert = new EventResponseTask();
		ert.setStatus(Task.TaskStatus.PENDING);
		ert.setDescription(eventRes.getDescription());
		ert.setCreationDate(new Date());
		ert.setEventResponse(eventRes);
		//log.debug("!!!!2");
		
		if (eventRes.isPersistence()) {
		try {
			//log.debug("!!!!3");
			XStream xstream = new XStream(new DomDriver());
			//log.debug("!!!!4");
			ert.setContext(xstream.toXML(context));
		}catch (Exception e) {
			log.error("Could not serialize context for event response '" + eventRes.getDescription() + "': " + e.getMessage());
			throw new ObjectFactoryException(e);
		}
		}
		//log.debug("!!!!5");
		
		if (eventRes.getExecutionTimeDifference() != null) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, eventRes.getExecutionTimeDifference());
			ert.setExpectedExecutionDate(c.getTime());
		}
		
		//log.debug("!!!!6");
		return ert;
	}
	
	@Transient
	public OperationContext getDeserializedContent() {
		XStream xstream = new XStream(new DomDriver());
		
		return (OperationContext)xstream.fromXML(getContext());
	}
	
}
