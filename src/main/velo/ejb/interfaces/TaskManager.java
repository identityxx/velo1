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
package velo.ejb.interfaces;

import java.util.Collection;
import java.util.List;

import velo.entity.BulkTask;
import velo.entity.Task;
import velo.entity.TaskDefinition;
import velo.exceptions.CannotRequeueTaskException;
import velo.exceptions.CollectionLoadingException;
import velo.exceptions.OperationException;

/**
 * A TaskManager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface TaskManager {
	
	/**
     * Persist a task entity
     * 
     * @param task
     */
    public void persistTask(Task task);
	
    /**
     * Update a task entity
     * 
     * @param task
     */
    public void updateTask(Task task);
	
    /**
     * Indicate that that a certain task execution was successfully completed
     * @param task The Task to set the status for
     */
    public void indicateTaskExecutionSuccess(Task task);
    
    /**
     * Indicate that that a certain task execution was failed
     * @param task The Task to set the status for
     * @param errorMsg the failure message to log
     */
    public void indicateTaskExecutionFailure(Task task, String errorMsg);
	
	
    public boolean executeTask(Task task);
	
    public Task findTaskById(Long taskId);
	
    
    /**
     * Persist a bulk task entity
     * 
     * @param bulkTask
     */
    public Long persistBulkTask(BulkTask bulkTask);
    
    /**
     * Re queue a task by:
     * - reseting its status to 'WAITING'
     * - clean the failure times of the task
     */
    public void reQueueTask(Task task) throws CannotRequeueTaskException;

	
	
	
	
	
	
    
    
    //SCANNER
    public void scanTasks();
    public void changeTaskScannerMode() throws OperationException;
    public boolean isTaskScannerActivate();
    
    /**
     * Create a timer scanner with the specified interval duration in seconds.
     * @param initialDuration - The initial (start) time of the first execution in SECONDS
     * @param intervalDuration - The interval (after the initial execution ends) for the next exeucitons in SECONDS
     */
    public void createTimerScanner(long initialDuration, long intervalDuration);
    /**
     * Queue the specified task for execution
     */
    public void sendTaskToJmsQueue(Task task);
    
    
	
	
	
	
	
	
	
	
	
	
	
    
    //public boolean isTaskDefinitionExists(long taskDefinitionId);
	@Deprecated
    public TaskDefinition findTaskDefinition(String name);
	@Deprecated
    public List<Task> loadTasksByIds(List<Long> taskIds) throws CollectionLoadingException;
	@Deprecated
    public List<BulkTask> loadBulkTasksByIds(List<Long> bulkTasksIds) throws CollectionLoadingException;
    
    
    
    
    /**
     * Persist a bulk of bulk tasks.
     * @param bulkTaskList A bulk task entity list to persist
     */
	@Deprecated
    public void persistBulkTasks(Collection<BulkTask> bulkTaskList);
    
    
    /**
     * Delete a task entity
     * 
     * @param task
     */
	@Deprecated
    public void deleteTask(Task task);
    
	@Deprecated
    public void removeTasks(Collection<Task> tasks);
    
    /**
     * Delete a bulkTask entity
     * 
     * @param bulkTask
     */
	@Deprecated
    public void deleteBulkTask(BulkTask bulkTask);
    
    
    
    /**
     * Update a bulk task entity
     * 
     * @param bulkTask
     */
	@Deprecated
    public void updateBulkTask(BulkTask bulkTask);
    
    
    
    
    
	@Deprecated
    public void createTimerScanner() throws OperationException;
    
    /*
     * Execute action in the the specified task!
     * @param task The task object that holds the action to execute
     * @return true/false upon success/failure of the execution process
     */
	//@Deprecated
    //public boolean executeActionInTask(Task task);
    
    
    
    //DEP public List<Message> getMessagesInQueue() throws JMSException;
    
	@Deprecated
    public void sendTaskToJms(Task task);
}