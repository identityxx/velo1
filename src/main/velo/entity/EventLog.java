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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a global system Event-Log entry
 *
 * @author Asaf Shakarchi
 */
//Seam annotations
@Name("eventLog")

@Entity
@Table(name="VL_EVENT_LOG")
@NamedQueries({
    @NamedQuery(name = "eventLog.findAll", query = "SELECT object(eventLog) FROM EventLog eventLog ORDER BY eventLog.creationDate DESC"),
    @NamedQuery(name = "eventLog.searchByString", query = "SELECT object(eventLog) FROM EventLog eventLog WHERE eventLog.message like :searchString OR eventLog.moduleName like :searchString")
})
@SequenceGenerator(name="EventLogIdSeq",sequenceName="EVENT_LOG_ID_SEQ")
public class EventLog extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    /**
     * The ID of the entity
     */
    private Long eventLogId;
    
    /**
     * The message of the event log
     */
    private String message;
    
    /**
     * The module the event log is related to
     */
    private String moduleName;
    
    
    /**
     * The user entity which is responsible for the event creation
     * (Might be null if the event was occured by an internal system process)
     */
    private User loggedByUser;
    
    /**
     * The severity of the event
     */
    private String severity;
    
    
    public EventLog() {
        
    }
    
    /**
     * An easy way to set an EventLog constructor
     * @param moduleName The moduleName the event is related to
     * @param severity The sevirity of the event log
     * @param message The message of the event
     */
    public EventLog(String moduleName,String severity,String message) {
        setModuleName(moduleName);
        setSeverity(severity);
        setMessage(message);
        setCreationDate(new Date());
    }
    
    /**
     * @param eventLogId The ID of the entity to set.
     */
    public void setEventLogId(Long eventLogId) {
        this.eventLogId = eventLogId;
    }
    
    /**
     * @return Returns the requestLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_EVENT_LOG_GEN",sequenceName="IDM_EVENT_LOG_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_EVENT_LOG_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventLogIdSeq")
    //@GeneratedValue //JB
    @Column(name="EVENT_LOG_ID")
    public Long getEventLogId() {
        return eventLogId;
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
    @Lob
    @Column(name="MESSAGE", nullable=false, unique=false)
    public String getMessage() {
        return message;
    }
    
    
    /**
     * @param moduleName The moduleName to set.
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    /**
     * @return Returns the moduleName.
     */
    @Column(name="MODULE_NAME")
    public String getModuleName() {
        return moduleName;
    }
    
    /**
     * @param loggedByUser The logByUser to set.
     */
    public void setLoggedByUser(User loggedByUser) {
        this.loggedByUser = loggedByUser;
    }
    
    /**
     * @return Returns the logByUser.
     */
    @ManyToOne(optional=true, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name="LOGGED_BY_USER_ID", nullable=true, unique=false)
    public User getLoggedByUser() {
        return loggedByUser;
    }
    
    
    /**
     * @param severity The severity to set.
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    /**
     * @return Returns the severity.
     */
    @Column(name="SEVERITY")
    public String getSeverity() {
        return severity;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Module: " + getModuleName());
        sb.append(", ");
        sb.append("Severity: " + getSeverity());
        sb.append(", ");
        sb.append("Message: " + getMessage());
        
        return sb.toString();
    }
}
