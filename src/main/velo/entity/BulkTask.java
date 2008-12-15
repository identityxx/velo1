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
//@!@not
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a Bulk Task
 * (Groups several tasks together, usually used to create a big task execution such as 'adding role to user' or 'adding group container' to user
 * that is built from a lot of tasks)
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_BULK_TASK")
@Name("bulkTask") //Seam name
@SequenceGenerator(name="BulkTaskIdSeq",sequenceName="BULK_TASK_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "bulkTask.findById",query = "SELECT object(bulkTask) FROM BulkTask AS bulkTask WHERE bulkTask.bulkTaskId = :bulkTaskId"),
    @NamedQuery(name = "bulkTask.findAll",query = "SELECT object(bulkTask) FROM BulkTask AS bulkTask ORDER BY bulkTask.bulkTaskId DESC")
})
public class BulkTask extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long bulkTaskId;
    
    private String description;
    
    /**
     * The request this bulk task is a part of
     */
    private Request request;
    
    /**
     * The tasks related to this bulk
     */
    private Set<Task> tasks = new HashSet<Task>();
    
    private Set<BulkTaskLog> bulkTaskLogs = new HashSet<BulkTaskLog>();
    
    private Long workflowProcessId;
    
    
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_BULK_TASK_GEN",sequenceName="IDM_BULK_TASK_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_BULK_TASK_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="BulkTaskIdSeq")
    //@GeneratedValue //JB
    @Column(name="BULK_TASK_ID")
    public Long getBulkTaskId() {
        return bulkTaskId;
    }
    
    /**
     * Set entity ID
     * @param bulkTaskId Entity's ID to set
     */
    public void setBulkTaskId(Long bulkTaskId) {
        this.bulkTaskId = bulkTaskId;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return the description
     */
    @Column(name="DESCRIPTION", nullable=true, unique=false)
    public String getDescription() {
        return description;
    }
    
    /**
     * @param request the request to set
     */
    public void setRequest(Request request) {
        this.request = request;
    }
    
    /**
     * @return the request
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="REQUEST_ID", nullable=true, unique=false)
    public Request getRequest() {
        return request;
    }
    
    /**
     * @param tasks The tasks to set.
     */
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
    
    /**
     * @return Returns the tasks.
     */
    @OneToMany(mappedBy = "bulkTask", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<Task> getTasks() {
        return tasks;
    }
    
    public void addTask(Task task) {
        getTasks().add(task);
    }
    
    
    /**
     * @param bulkTaskLogs The bulkTaskLogs to set.
     */
    public void setBulkTaskLogs(Set<BulkTaskLog> bulkTaskLogs) {
        this.bulkTaskLogs = bulkTaskLogs;
    }
    
    /**
     * @return Returns the bulkTaskLogs.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy="bulkTask", fetch=FetchType.LAZY)
    @OrderBy("creationDate")
    public Set<BulkTaskLog> getBulkTaskLogs() {
        return bulkTaskLogs;
    }
    
    
    @Column(name = "WORKFLOW_PROCESS_ID", nullable=true)
	public Long getWorkflowProcessId() {
		return workflowProcessId;
	}

	public void setWorkflowProcessId(Long workflowProcessId) {
		this.workflowProcessId = workflowProcessId;
	}
	
	
	
    
    public void addLog(String severity, String summaryMessage, String detailedMessage) {
        BulkTaskLog btl = new BulkTaskLog();
        btl.setCreationDate(new Date());
        btl.setSummaryMessage(summaryMessage);
        btl.setDetailedMessage(detailedMessage);
        btl.setSeverity(severity);
        btl.setBulkTask(this);
        getBulkTaskLogs().add(btl);
    }
    
    public static BulkTask factory(String description) {
    	BulkTask bt = new BulkTask();
    	bt.setDescription(description);
    	bt.setCreationDate(new Date());
    	
    	return bt;
    }
    
    public void modifyWorkflowProcessIdOfTasks(Long id) {
    	for (Task currTask : getTasks()) {
    		currTask.setWorkflowProcessId(id);
    	}
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    @Transient
    public boolean isApproved() {
        if (getRequest() != null) {
            return getRequest().isApproved();
        } else {
            return true;
        }
    }
    
    @Deprecated
    @Transient
    public String getFirstLogSummaryMessage() {
        if (getBulkTaskLogs().size() > 0) {
            return getBulkTaskLogs().iterator().next().getSummaryMessage();
        } else {
            return "";
        }
    }
    
}