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
//@!@clean
import groovy.lang.GroovyObject;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents an Event Response for a certain event
 *
 * @author Asaf Shakarchi
 */
//Seam annotations
@Name("eventResponse")

@Entity
@DiscriminatorValue("EVENT_RESPONSE")
public class EventResponse extends ActionRule implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    private EventDefinition eventDefinition;
    private boolean persistence;
    private Integer executionTimeDifference;
    
    
    //transient, can be modified by integration rules
    private Date expectedExecutionDate;
    
    

	/**
     * @return the eventDefinition
     */
    //see explaination of optional=true/nullable=true in ResourceAction->getResource()
    @ManyToOne(optional=true)
    @JoinColumn(name="EVENT_DEFINITION_ID", nullable = true)
    public EventDefinition getEventDefinition() {
        return eventDefinition;
    }
    
    /**
     * @param eventDefinition the eventDefinition to set
     */
    public void setEventDefinition(EventDefinition eventDefinition) {
        this.eventDefinition = eventDefinition;
    }
    
    //FIXME: the best way was to set a default value instead of nullable=true as this column must have a value but irrelevant for other type of actions
    @Column(name="PERSISTENCE", nullable=true)
    //@Column(name="PERSISTENCE", nullable=false)
    public boolean isPersistence() {
        return persistence;
    }
    
    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }
    

	public void updateScriptedObject(GroovyObject scriptedObject) {
    	
    }
 
    
	/**
	 * @return the executionTimeDifference
	 */
	@Column(name="EXECUTION_TIME_DIFFERENCE")
	public Integer getExecutionTimeDifference() {
		return executionTimeDifference;
	}

	/**
	 * @param executionTimeDifference the executionTimeDifference to set
	 */
	public void setExecutionTimeDifference(Integer executionTimeDifference) {
		this.executionTimeDifference = executionTimeDifference;
	}
    
    
    
    
    /**
	 * @return the expectedExecutionDate
	 */
    @Transient
	public Date getExpectedExecutionDate() {
		return expectedExecutionDate;
	}

	/**
	 * @param expectedExecutionDate the expectedExecutionDate to set
	 */
	public void setExpectedExecutionDate(Date expectedExecutionDate) {
		this.expectedExecutionDate = expectedExecutionDate;
	}
}
