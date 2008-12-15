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
package velo.ejb.seam.action;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.TaskManagerLocal;
import velo.entity.Task;
import velo.entity.Task.TaskStatus;
import velo.exceptions.CannotRequeueTaskException;
import velo.exceptions.OperationException;

@Stateful
@Name("taskActions")
public class TaskActionsBean implements TaskActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@PersistenceContext
    public EntityManager em;
    
	@EJB
	public TaskManagerLocal taskManager;

	@In(value = "#{taskHome.instance}")
	Task task;
	
	public void executeTask() {
		if (task.isInProcess()) {
            facesMessages.add(FacesMessage.SEVERITY_WARN, "Cannot execute task ID '" + task.getTaskId() + "', task is locked!");
        } else {
            taskManager.sendTaskToJmsQueue(task);
            facesMessages.add("Requested task execution for task ID '" + task.getTaskId() + "'");
        }
	}
	
	public void cancelTask() {
		task.setStatus(TaskStatus.CANCELLED);
		taskManager.updateTask(task);
		
		facesMessages.add("Successfully cancelled task Id: #0", task.getTaskId());
	}
	
	public void handledManuallyTask() {
		task.setStatus(TaskStatus.HANDLED_MANUALLY);
		taskManager.updateTask(task);
		
		facesMessages.add("Successfully set task status as 'handled manually' for task Id: #0", task.getTaskId());
	}
	
	public void reQueueTask() {
        log.debug("Re-queuing task ID: " + task.getTaskId());
        
        try {
            taskManager.reQueueTask(task);
        } catch(CannotRequeueTaskException crte) {
            facesMessages.add(crte.getMessage());
        }
    }
	
	
	
	//Scanner methods
    public void changeTaskScannerMode() {
        try     {
            taskManager.changeTaskScannerMode();
            facesMessages.add("Successfully changed Task Scanner status...");
        } catch (OperationException ex) {
            facesMessages.add(FacesMessage.SEVERITY_WARN, "Cannot change Task-Scanner status due to: " + ex.getMessage());
        }
    }
    
    public boolean isTaskScannerActivate() {
        return taskManager.isTaskScannerActivate();
    }
    
    public void scanTasks() {
    	taskManager.scanTasks();
        facesMessages.add("Successfully executed task scan!");
    }
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
