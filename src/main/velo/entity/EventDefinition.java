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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents an event definition
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_EVENT_DEFINITION")
@SequenceGenerator(name="EventDefinitionIdSeq",sequenceName="EVENT_DEFINITION_ID_SEQ")
@Name("eventDefinition") //Seam name
@NamedQueries({
    @NamedQuery(name = "eventDefinition.findByUniqueName",query = "SELECT object(ed) FROM EventDefinition AS ed WHERE ed.uniqueName = :uniqueName")
})
public class EventDefinition extends ExtBasicEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    private static Logger logger = Logger.getLogger(EventDefinition.class.getName());
    
    /**
     * ID of the entity
     */
    private Long eventDefinitionId;
    
    private Set<EventResponse> eventResponses = new HashSet<EventResponse>();
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventDefinitionIdSeq")
    //@GeneratedValue //JB
    @Column(name="EVENT_DEFINITION_ID")
    public Long getEventDefinitionId() {
        return eventDefinitionId;
    }
    
    /**
     * Set entity ID
     * @param eventDefinitionId Entity's ID to set
     */
    public void setEventDefinitionId(Long eventDefinitionId) {
        this.eventDefinitionId = eventDefinitionId;
    }
    
    /**
     * @param tasks The eventResponses to set.
     */
    public void setEventResponses(Set<EventResponse> eventResponses) {
        this.eventResponses = eventResponses;
    }
    
    /**
     * @return Returns the event responses associated with the event.
     */
    @OneToMany(mappedBy = "eventDefinition", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Set<EventResponse> getEventResponses() {
        return eventResponses;
    }
    
    
    
    
    //-------TRANSIENTS-------
    @Transient
    public Set<EventResponse> getActiveEventResponses() {
    	Set<EventResponse> activeResponses = new HashSet<EventResponse>();
    	for (EventResponse currER : getEventResponses()) {
    		if (currER.isActive()) {
    			activeResponses.add(currER);
    		}
    	}
    	
    	return activeResponses;
    }
    
    public void addResponse(EventResponse eventResponse) {
    	eventResponse.setEventDefinition(this);
        getEventResponses().add(eventResponse);
    }
    
    /**
     * @param isInitialized the isInitialized to set
     */
        /*
        @Transient
        public void setInitialized(boolean isInitialized) {
                this.isInitialized = isInitialized;
        }
         */
    
    /**
     * @return the isInitialized
     */
        /*
        @Transient
        public boolean isInitialized() {
                return isInitialized;
        }
         */
    
    public EventResponse factoryEventResponse() {
        EventResponse erd = new EventResponse();
        erd.setCreationDate(new Date());
        erd.setEventDefinition(this);
        
        return erd;
    }
    
    /*
    @Transient
    public String getEventsFullDirPath() {
        String eventsFullDirPath = SysConf.getSysConf().getString("system.directory.user_workspace_dir")
        + "/"
            + SysConf.getSysConf().getString("system.directory.events_dir")
            + "/"
            + this.getUniqueName().toLowerCase();
        
        return eventsFullDirPath;
    }
    */
}