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
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

import velo.common.EdmMessage;
import velo.common.EdmMessages;

/**
 * An entity that represents a serialized task instance that is executable
 * Each serialized task is documented on the table with dates with the object task serialized
 * When the task is sent into the 'queue' for execution, after execution, the executer task flags the task with its result
 * and if set to, delete/keeps the entity from the table with the new status
 *
 * @author Administrator
 */

//Seam annotations
@Name(value = "task")
@Entity
@Table(name = "VL_TASK")
@SequenceGenerator(name="TaskIdSeq",sequenceName="TASK_ID_SEQ")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CLASS_TYPE")
@NamedQueries( {
		@NamedQuery(name = "task.findById", query = "SELECT object(task) FROM Task task WHERE task.taskId = :taskId"),
		//@NamedQuery(name = "task.waitingTasksToExecuteAmount", query = "SELECT count(task) FROM Task AS task WHERE (task.status = 'PENDING' OR task.status = 'FAILURE') AND task.expectedExecutionDate < :currDate AND task.taskDefinition.failureRetries >= task.failureCounts AND task.locked = 0 AND task.deleted = 0")})
		@NamedQuery(name = "task.waitingTasksToExecuteAmount", query = "SELECT count(task) FROM Task AS task WHERE (task.status = 'PENDING' OR task.status = 'FAILURE') AND task.expectedExecutionDate < :currDate AND task.inProcess = 0 AND task.deleted = 0"),
		//@NamedQuery(name = "task.findAllTasksToQueue", query = "SELECT object(task) FROM Task AS task WHERE (task.status = 'WAITING' OR task.status = 'FAILURE') AND task.expectedExecutionDate < :currDate AND task.taskDefinition.failureRetries >= task.failureCounts AND task.locked = 0 AND task.deleted = 0"), 
		@NamedQuery(name = "task.findAllTasksToQueue", query = "SELECT object(task) FROM Task AS task WHERE (task.status = 'PENDING' OR task.status = 'FAILURE') AND task.expectedExecutionDate < :currDate AND task.inProcess = 0 AND task.deleted = 0") 
})
public class Task extends BaseEntity implements Serializable {

	public enum TaskStatus {
		PENDING, RUNNING, SUCCESS, FAILURE, FATAL_ERROR, CANCELLED, HANDLED_MANUALLY
	}
	
    private static final long serialVersionUID = 12345;

    private Set<TaskLog> taskLogs = new HashSet<TaskLog>();
    
    private Set<Task> dependencyTasks = new HashSet<Task>();

    /**
     * The bulk task entity if this task is a part of a bulk
     * (null means that this task is NOT a part of a bulk task)
     */
    private BulkTask bulkTask;
    
    private Date lastExecutionDate;
    
    private Date expectedExecutionDate;
    
    private int failureCounts;
    
    private String description;

    /**
     * Whether the task is flagged as deleted or not
     */
    private boolean deleted;

    /**
     * Whether the task is locked (in progress by some agent already or not)
     */
    private boolean inProcess;

    /**
     * The request this task is a part of (tasks that has bulk task set are not directly associated to the request but through the bulk task.);
     */
    private Request request;
    
    private String body;

    private TaskStatus status;

    // private TaskLog taskLog;
    private Long taskId;
    
    private boolean isCreationTaskCancelled = false; 

    private Long workflowProcessId;
    
    private String taskExecuterClassName;
    
    private Boolean async = true;
    
    
    
    /**
     * @return Returns the taskId.
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="TaskIdSeq")
    //@GeneratedValue //JB
    @Column(name = "TASK_ID")
    public Long getTaskId() {
        return taskId;
    }

    /**
     * @param taskId The taskId to set.
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @return Returns the lastExecutionDate.
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "EXECUTION_DATE")
    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    /**
     * @param lastExecutionDate The lastExecutionDate to set.
     */
    public void setLastExecutionDate(Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }

    /**
     * @return Returns the expectedExecutionDate.
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "EXPECTED_EXECUTION_DATE")
    public Date getExpectedExecutionDate() {
        return expectedExecutionDate;
    }

    /**
     * @param expectedExecutionDate The expectedExecutionDate to set.
     */
    public void setExpectedExecutionDate(Date expectedExecutionDate) {
        this.expectedExecutionDate = expectedExecutionDate;
    }

    /**
     * @param failureCounts The failureCounts to set.
     */
    public void setFailureCounts(int failureCounts) {
        this.failureCounts = failureCounts;
    }

    /**
     * @return Returns the failureCounts.
     */
    @Column(name = "FAILURE_COUNTS")
    public int getFailureCounts() {
        return failureCounts;
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
    @Column(name = "DESCRIPTION", nullable = true, unique = false)
    public String getDescription() {
        return description;
    }

    /**
     * @param bulkTask The bulkTask to set.
     */
    public void setBulkTask(BulkTask bulkTask) {
        this.bulkTask = bulkTask;
    }

    /**
     * @return Returns the bulkTask.
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "BULK_TASK_ID", nullable = true, unique = false)
    public BulkTask getBulkTask() {
        return bulkTask;
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
    @JoinColumn(name = "REQUEST_ID", nullable = true, unique = false)
    public Request getRequest() {
        return request;
    }

    /**
     * @param taskLogs The taskLogs to set.
     */
    public void setTaskLogs(Set<TaskLog> taskLogs) {
        this.taskLogs = taskLogs;
    }

    /**
     * @return Returns the taskLogs.
     */
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "task", fetch = FetchType.EAGER)
    @OrderBy("creationDate")
    public Set<TaskLog> getTaskLogs() {
        return taskLogs;
    }

    /**
     * @param dependencyTasks The dependencyTasks to set.
     */
    public void setDependencyTasks(Set<Task> dependencyTasks) {
        this.dependencyTasks = dependencyTasks;
    }

    /**
     * @return Returns the dependencyTasks.
     */
    //@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    @ManyToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY, targetEntity = velo.entity.Task.class)
    @JoinTable(name = "VL_TASK_DEPENDENCY", joinColumns = @JoinColumn(name = "TASK_ID"), inverseJoinColumns = @JoinColumn(name = "DEPEND_ON_TASK_ID"))
    public Set<Task> getDependencyTasks() {
        return dependencyTasks;
    }

    /**
     * @param deleted The deleted to set.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return Returns the isDeleted.
     */
    @Column(name = "DELETED")
    public boolean isDeleted() {
        return deleted;
    }
    
	/**
	 * @return the inProcess
	 */
    @Column(name = "IN_PROCESS")
	public boolean isInProcess() {
		return inProcess;
	}

	/**
	 * @param inProcess the inProcess to set
	 */
	public void setInProcess(boolean inProcess) {
		this.inProcess = inProcess;
	}

	/**
	 * @return the body
	 */
	@Column(name = "BODY")
	@Lob
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the status
	 */
	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	public TaskStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
	
	@Column(name = "WORKFLOW_PROCESS_ID", nullable=true)
	public Long getWorkflowProcessId() {
		return workflowProcessId;
	}

	public void setWorkflowProcessId(Long workflowProcessId) {
		this.workflowProcessId = workflowProcessId;
	}
	

	@Column(name = "TASK_EXECUTER_CLASS_NAME")
	//TODO: At some point should be not nullable
	public String getTaskExecuterClassName() {
		return taskExecuterClassName;
	}

	public void setTaskExecuterClassName(String taskExecuterClassName) {
		this.taskExecuterClassName = taskExecuterClassName;
	}
	
	@Transient
	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}
	
	
	
	

	//Transients/Helper
	public void addLog(EdmMessage em) {
        addLog(em.getType().toString(), em.getSummary(), em.getDescription());
    }

    public void addLogs(EdmMessages msgs) {
        for (EdmMessage currMsg : msgs.getMessages()) {
            addLog(currMsg);
        }
    }

    public void addDependentTask(Task task) {
        getDependencyTasks().add(task);
    }
    
    public void addLog(String severity, String summaryMessage, String detailedMessage) {
        TaskLog tl = new TaskLog();

        tl.setCreationDate(new Date());
        tl.setSummaryMessage(summaryMessage);
        tl.setDetailedMessage(detailedMessage);
        tl.setSeverity(severity);
        tl.setTask(this);
        getTaskLogs().add(tl);
    }

    @Transient
    public boolean isAllDependentTasksCompletedSuccessfully() {
        boolean status = true;

        //System.out.println("Found '" + getDependencyTasks().size() + "' dependecies on task ID: " + this.getTaskId());
        for (Task currDependentTask : getDependencyTasks()) {
            //System.out.println("Task Dependency ID: '" + currDependentTask.getTaskId() + "', status: " + currDependentTask.status);
            if (!currDependentTask.getStatus().equals(TaskStatus.SUCCESS)) {
                status = false;
                break;
            }
        }

        return status;
    }

    
    @Transient
    public String getLastLoggedErrorMessage() {
    	String errMsg = null;
    	for (TaskLog currTL : getTaskLogs()) {
    		if (currTL.getSeverity().equals("ERROR")) {
    			//FIXME: Summary?Detailed? maybe consolidate them both to one message and thats it?
    			errMsg = currTL.getSummaryMessage();
    		}
    	}
    	
    	return errMsg;
    }
    
    
    /**
	 * @return the isCreationTaskCancelled
	 */
    @Transient
	public boolean isCreationTaskCancelled() {
		return isCreationTaskCancelled;
	}

	/**
	 * @param isCreationTaskCancelled the isCreationTaskCancelled to set
	 */
	public void setCreationTaskCancelled(boolean isCreationTaskCancelled) {
		this.isCreationTaskCancelled = isCreationTaskCancelled;
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

	@Transient
    @Deprecated
    public boolean isApproved() {
        if ((getRequest() == null) && (getBulkTask() == null)) {
            return true;
        } else {
            if (getBulkTask() != null) {
                return getBulkTask().isApproved();
            }

            if (getRequest() != null) {
                return getRequest().isApproved();
            }

            return true;
        }
    }

    @Deprecated
    public String logsToString() {
        StringBuilder logsAsString = new StringBuilder();
        int logId = 0;

        for (TaskLog currTaskLog : getTaskLogs()) {
            logId++;
            logsAsString.append("(" + logId + ") " + ", Severity: " + currTaskLog.getSeverity() + ", Summary: " + currTaskLog.getSummaryMessage() + ", Detailed: " + currTaskLog.getDetailedMessage());
        }

        return logsAsString.toString();
    }

    
    @Transient
    @Deprecated
    public String getFirstLogDetailedMessage() {
        if (getTaskLogs().size() > 0) {
            return getTaskLogs().iterator().next().getDetailedMessage();
        } else {
            return "";
        }
    }

    @Transient
    @Deprecated
    public String getLastLogDetailedMessage() {

        // TODO: UGLY, WHY TO LOOP THROUGH THE ENTIRE LIST? CAN'T JUST ACCESS SOMEHOW LAST ROW?
        if (getTaskLogs().size() > 0) {
            Iterator<TaskLog> logsIterator = getTaskLogs().iterator();
            String detailedMessage = new String();

            while (logsIterator.hasNext()) {
                detailedMessage = logsIterator.next().getDetailedMessage();
            }

            return detailedMessage;
        } else {
            return "";
        }
    }
    
    
    @Deprecated
    public void setSerializedTask(String serializedTask) {
        
    }
    
    @Deprecated
    @Transient
    public boolean isScripted() {
    	return true;
    }
    @Deprecated
    public void setScript(String content) {
    	
    }
    
    public void serializeAsTask(Object o) {}
    
    @Transient
    @Deprecated
    public String getSerializedTask() {
        return null;
    }
    
    @Deprecated
    @Transient
    public TaskDefinition getTaskDefinition() {
    	return null;
    }
    
    @Deprecated
    public void setExecutionDate(Date d) {
    	
    }
    
    
    public static Task factory(String description) {
    	Task task = new SpmlTask();
    	task.setCreationDate(new Date());
    	task.setExpectedExecutionDate(new Date());
    	task.setDescription(description);
    	task.setStatus(TaskStatus.PENDING);
    	
    	return task;
    }
	
}
