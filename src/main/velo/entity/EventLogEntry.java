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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a global system Event-Log entry
 *
 * @author Asaf Shakarchi
 */
//Seam annotations
@Name("eventLogEntry")

@Entity
@Table(name="VL_EVENT_LOG_ENTRY")
@NamedQueries({
    @NamedQuery(name = "eventLogEntry.findAll", query = "SELECT object(eventLogEntry) FROM EventLogEntry eventLogEntry ORDER BY eventLogEntry.creationDate DESC"),
    @NamedQuery(name = "eventLogEntry.searchByString", query = "SELECT object(eventLogEntry) FROM EventLogEntry eventLogEntry WHERE eventLogEntry.message like :searchString OR eventLogEntry.module like :module")
})
@SequenceGenerator(name="EventLogEntryIdSeq",sequenceName="EVENT_LOG_ENTRY_ID_SEQ")
public class EventLogEntry  implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    public enum EventLogModule {
		SECURITY,WORKFLOW
	}
    
    /*//we cannot support hirarchial enums
    public enum EventLogTaskCategory {
    	LOGIN,LOGOUT,AUTH_FAILURE
    }
    */
    
    public enum EventLogLevel {
    	DEBUG,INFO,WARN,ERROR,CRITICAL
    }
    
    /**
     * The ID of the entity
     */
    private Long eventLogEntryId;
    
    /**
     * The message of the event log
     */
    private String message;
    
    /**
     * The module the event log is related to
     */
    private EventLogModule module;
    
    private EventLogLevel level;
    
    private String keywords;
    
    private Date creationDate;
    
    private String server;
    
    private String category;
    
    
    private Set<EventLogDataEntry> dataEntries = new HashSet<EventLogDataEntry>();
    
    public EventLogEntry() {
        
    }
    
    /**
     * An easy way to set an EventLog constructor
     * @param moduleName The moduleName the event is related to
     * @param severity The sevirity of the event log
     * @param message The message of the event
     */
    public EventLogEntry(EventLogModule module,String category,EventLogLevel eventLogLevel, String message) {
        setModule(module);
        setMessage(message);
        setLevel(eventLogLevel);
        setCreationDate(new Date());
        setCategory(category);
    }
    
    
    /**
     * @param eventLogId The ID of the entity to set.
     */
    public void setEventLogEntryId(Long eventLogEntryId) {
        this.eventLogEntryId = eventLogEntryId;
    }
    
    /**
     * @return Returns the requestLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_EVENT_LOG_GEN",sequenceName="IDM_EVENT_LOG_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_EVENT_LOG_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventLogEntryIdSeq")
    //@GeneratedValue //JB
    @Column(name="EVENT_LOG_ENTRY_ID")
    public Long getEventLogEntryId() {
        return eventLogEntryId;
    }
    
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * @return Returns the detailed message.
     */
    @Column(name="MESSAGE", nullable=false, unique=false)
    public String getMessage() {
        return message;
    }
    
    @Column(name="MODULE_NAME")
    @Enumerated(EnumType.STRING)
    public EventLogModule getModule() {
		return module;
	}

	public void setModule(EventLogModule module) {
		this.module = module;
	}

	
	//'LEVEL' IS RESERVED WORD IN ORACLE
	@Column(name="EVENT_LEVEL")
    @Enumerated(EnumType.STRING)
    public EventLogLevel getLevel() {
		return level;
	}

	public void setLevel(EventLogLevel level) {
		this.level = level;
	}

	@Column(name="KEYWORDS")
    public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Column(name="SERVER")
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	@Column(name="CATEGORY")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "eventLogEntry", fetch = FetchType.LAZY)
    @OrderBy("name")
	public Set<EventLogDataEntry> getDataEntries() {
		return dataEntries;
	}

	public void setDataEntries(Set<EventLogDataEntry> dataEntries) {
		this.dataEntries = dataEntries;
	}

	public void addData(String name, String value) {
		EventLogDataEntry evde = new EventLogDataEntry(this,name,value);
        getDataEntries().add(evde);
	}
	
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Module: " + getModule());
        sb.append(", ");
        sb.append("Severity: " + getLevel());
        sb.append(", ");
        sb.append("Message: " + getMessage());
        
        return sb.toString();
    }
}
