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
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.entity.Task.TaskStatus;

/**
 * An entity that represents a Task Definition
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("taskDefinition")
@Table(name="VL_TASK_DEFINITION")
@SequenceGenerator(name="TaskDefinitionIdSeq",sequenceName="TASK_DEFINITION_ID_SEQ")
@Entity
@NamedQueries({
    @NamedQuery(name = "taskDefinition.findByName",query = "SELECT object(td) FROM TaskDefinition AS td WHERE td.name = :name")
})
public class TaskDefinition extends BaseEntity implements Serializable {
	//public enum ActionTypes {JAVA,GROOVY,XML,BEANSHELL};
	
    private static final long serialVersionUID = 1987302491306161423L;
    
    private Long taskDefinitionId;
    
    private String name;
    
    private String description;
    
    /**
     * Identifies the name of the class that implements the task
     */
    private String className;
    
    /**
     * Identifies the name of the class that executes this task
     */
    //private String executor;
    
    /**
     * Whether the task is suspendable or not
     */
    private boolean isSuspendable;
    
    /**
     * The execution mode of this task definition, can be 'ASYNC/SYNC'
     */
    private String execMode;
    
    /**
     * Limit in seconds that the task is allowed to execute. if time is ecceeded the schduler is allowed to terminate the task.
     * 0 means no limit.
     */
    private long executionLimit;
    
    private boolean isScripted;
    
    private int failureRetries;
    
    private List<ActionLanguage> actionLanguages;
    
    /**
     * Whether to delete the task after it finished its execution process or not.
     * (Task will only get deleted if it finished successfully!)
     */
    private boolean isDeleteTaskAfterExecution;
    
    
    public TaskDefinition() {
    }
    
    public TaskDefinition(Long taskDefinitionId, String name, String description, String className, boolean isSuspendable, String execMode, long executionLimit, boolean isScripted, int failureRetries, boolean isDeletedTaskAfterExecution) {
        //setTaskDefinitionId(taskDefinitionId);
        setName(name);
        setDescription(description);
        setClassName(className);
        setSuspendable(isSuspendable);
        setExecMode(execMode);
        setExecutionLimit(executionLimit);
        setScripted(isScripted);
        setFailureRetries(failureRetries);
        setDeleteTaskAfterExecution(isDeletedTaskAfterExecution);
    }
    
    
    
    /**
     * Set the ID of the entity
     * @param taskDefinitionId The ID of the entity to set
     */
    public void setTaskDefinitionId(Long taskDefinitionId) {
        this.taskDefinitionId = taskDefinitionId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity to get
     */
    //Gf@Id
    //Gf@SequenceGenerator(name="IDM_TASK_DEFINITION_GEN",sequenceName="IDM_TASK_DEFINITION_SEQ", allocationSize=1)
    //Gf@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_TASK_DEFINITION_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="TaskDefinitionIdSeq")
    //@GeneratedValue //JB
    @Column(name="TASK_DEFINITION_ID")
    public Long getTaskDefinitionId() {
        return taskDefinitionId;
    }
    
    /**
     * Set the name of the entity
     * @param name The name of the entity to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the name of the entity
     * @return The name of the entity
     */
    @Length(min=3, max=40) @NotNull //seam
    @Column(name="NAME", nullable=false)
    public String getName() {
        return name;
    }
    
    /**
     * Set the description of the entity
     * @param description The description string to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the description of the entity
     * @return The description of the entity
     */
    @Column(name="DESCRIPTION", nullable=false)
    public String getDescription() {
        return description;
    }
    
    
    
    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * @return Returns the className.
     */
    @Column(name="CLASS_NAME", nullable=true)
    public String getClassName() {
        return className;
    }
    
    /**
     * @param isSuspendable The isSuspendable to set.
     */
    public void setSuspendable(boolean isSuspendable) {
        this.isSuspendable = isSuspendable;
    }
    
    /**
     * @return Returns the isSuspendable.
     */
    @Column(name="IS_SUSPENDABLE", nullable=false)
    public boolean isSuspendable() {
        return isSuspendable;
    }
    
    /**
     * @param execMode The execMode to set.
     */
    public void setExecMode(String execMode) {
        this.execMode = execMode;
    }
    
    /**
     * @return Returns the execMode.
     */
    @Column(name="EXEC_MODE", nullable=false)
    public String getExecMode() {
        return execMode;
    }
    
    /**
     * @param executionLimit The executionLimit to set.
     */
    public void setExecutionLimit(long executionLimit) {
        this.executionLimit = executionLimit;
    }
    
    /**
     * @return Returns the executionLimit.
     */
    @Column(name="EXECUTION_LIMIT", nullable=false)
    public long getExecutionLimit() {
        return executionLimit;
    }
    
    /**
     * @param isDeleteTaskAfterExecution The isDeleteTaskAfterExecution to set.
     */
    public void setDeleteTaskAfterExecution(boolean isDeleteTaskAfterExecution) {
        this.isDeleteTaskAfterExecution = isDeleteTaskAfterExecution;
    }
    
    /**
     * @return Returns the isDeleteTaskAfterExecution.
     */
    @Column(name="DELETE_TASK_AFTER_EXECUTION", nullable=false)
    public boolean isDeleteTaskAfterExecution() {
        return isDeleteTaskAfterExecution;
    }
    
    /**
     * @param isScripted The isScripted to set.
     */
    public void setScripted(boolean isScripted) {
        this.isScripted = isScripted;
    }
    
    /**
     * @return Returns the isScripted.
     */
    @Column(name="IS_SCRIPTED", nullable=false)
    public boolean isScripted() {
        return isScripted;
    }
    
    
    
    /**
     * @param failureRetries The failureRetries to set.
     */
    public void setFailureRetries(int failureRetries) {
        this.failureRetries = failureRetries;
    }
    
    /**
     * @return Returns the failureRetries.
     */
    @Column(name="FAILURE_RETRIES", nullable=false)
    public int getFailureRetries() {
        return failureRetries;
    }
    
    
    /**
	 * @return the actionTypes
	 */
    @ManyToMany(fetch=FetchType.LAZY,targetEntity=velo.entity.ActionLanguage.class)
    @JoinTable(
    	name="VL_ACTION_LANGS_TO_TASK_DEFS",
    	joinColumns=@JoinColumn(name="TASK_DEFINITION_ID"),
    	inverseJoinColumns=@JoinColumn(name="ACTION_LANGUAGE_ID")
    )
	public List<ActionLanguage> getActionLanguages() {
		return actionLanguages;
	}

	/**
	 * @param actionLanguages the actionLanguages to set
	 */
	public void setActionLanguages(List<ActionLanguage> actionLanguages) {
		this.actionLanguages = actionLanguages;
	}
	
	
    @Override
    public boolean equals(Object other) {
        if (other instanceof TaskDefinition) {
            TaskDefinition that = (TaskDefinition) other;
            return this.taskDefinitionId.longValue() == that.taskDefinitionId.longValue();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        if (taskDefinitionId == null) return super.hashCode();
        return taskDefinitionId.hashCode();
    }
    
    @Transient
    //TODO Make static
    @Deprecated
    public Task factoryTask() {
        Task task = new Task();
        Date currDate = new Date();
        task.setCreationDate(currDate);
        task.setExpectedExecutionDate(currDate);
        //task.setTaskDefinition(this);
        task.setStatus(TaskStatus.PENDING);
        task.setDescription(getDescription());
        
        return task;
    }
}
