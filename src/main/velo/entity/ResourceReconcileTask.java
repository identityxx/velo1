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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A reconciliation persistence task
 *
 * @author Asaf Shakarchi
 */
@Entity
@DiscriminatorValue("RESOURCE_RECONCILE")
public class ResourceReconcileTask extends ResourceTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public ResourceReconcileTask() {
    	
    }

    public static ResourceReconcileTask factory(Resource resource, String description) {
    	ResourceReconcileTask task = new ResourceReconcileTask();
    	task.setCreationDate(new Date());
    	task.setExpectedExecutionDate(new Date());
    	task.setDescription(description);
    	//stop using 'resource unique name'
    	task.setResourceUniqueName(resource.getUniqueName());
    	task.setStatus(TaskStatus.PENDING);
    	
    	return task;
    }
    
    public static ResourceReconcileTask factoryReconcileAllIdentitiesTask(Resource resource){
    	ResourceReconcileTask task = factory(resource, "All Identities reconciliation, makes sure no accounts were removed from resource.");
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileAllIdentitiesTaskExecuter");
    	
    	return task;
    }
	
    public static ResourceReconcileTask factoryReconcileIdentitiesIncrementally(Resource resource, ResourceTypeOperation rto){
    	ResourceReconcileTask task = factory(resource, "Reconcile Identities incrementally reconciliation for resource '" + resource.getDisplayName() + "'.");
    	task.setResourceTypeOperation(rto);
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileIdentitiesIncrementallyTaskExecuter");
    	
    	return task;
    }
    
    public static ResourceReconcileTask factoryReconcileIdentitiesFull(Resource resource, ResourceTypeOperation rto){
    	ResourceReconcileTask task = factory(resource, "Full Reconcile Identities Reconciliation for resource '" + resource.getDisplayName() + "'.");
    	task.setResourceTypeOperation(rto);
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileIdentitiesFullTaskExecuter");
    	
    	return task;
    }
    
    public static ResourceReconcileTask factoryReconcileGroupsFull(Resource resource, ResourceTypeOperation rto){
    	ResourceReconcileTask task = factory(resource, "Full Reconcile Groups Reconciliation for resource '" + resource.getDisplayName() + "'.");
    	task.setResourceTypeOperation(rto);
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileGroupsFullTaskExecuter");
    	
    	return task;
    }
    
    
    public static ResourceReconcileTask factoryReconcileGroupMembershipFull(Resource resource, ResourceTypeOperation rto){
    	ResourceReconcileTask task = factory(resource, "Full Reconcile Group Membership Reconciliation for resource '" + resource.getDisplayName() + "'.");
    	task.setResourceTypeOperation(rto);
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileGroupMembershipFullTaskExecuter");
    	
    	return task;
    }
    
    public static ResourceReconcileTask factoryReconcileGroupMembershipIncrementally(Resource resource, ResourceTypeOperation rto){
    	ResourceReconcileTask task = factory(resource, "Incremental Reconcile Group Membership Reconciliation for resource '" + resource.getDisplayName() + "'.");
    	task.setResourceTypeOperation(rto);
    	task.setTaskExecuterClassName("velo.tasks.taskExecuters.ReconcileGroupMembershipIncrementalTaskExecuter");
    	
    	return task;
    }
	
}