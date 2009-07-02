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

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.entity.Resource;
import velo.exceptions.OperationException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.ReconcileUsersException;
import velo.exceptions.TaskCreationException;

@Stateful
@Name("reconcileActions")
public class ReconcileActionsBean implements ReconcileActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;
	
	@EJB
	public ReconcileManagerLocal reconcileManager;
	
	@Deprecated
	private boolean fetchActiveData = true;
	
	private boolean executeProcessAsync = true;

	@In(value="#{resourceHome.instance}")
	Resource resource;
	
	
	@In EntityManager entityManager;

	public void resourceReconcileFull() {
		try {
			reconcileManager.reconcileIdentitiesFull(resource.getUniqueName(),isExecuteProcessAsync());
			
			if(isExecuteProcessAsync()) {
				facesMessages.add("Created a task to fully reconcile resource #0.",resource.getDisplayName());
			} else {
				facesMessages.add("Succesfully fully reconciled resource #0", resource.getDisplayName());
			}
		} catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,e.getMessage());
		}
	}
	
	public void resourceReconcileIncrementally() {
		try {
			reconcileManager.reconcileIdentitiesIncrementally(resource.getUniqueName(),isExecuteProcessAsync());
			
			if(isExecuteProcessAsync()) {
				facesMessages.add("Created a task to reconcile resource #0 incrementally.",resource.getDisplayName());
			} else {
				facesMessages.add("Succesfully reconciled resource #0 incrementally", resource.getDisplayName());
			}
		} catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,e.getMessage());
		}
	}
	
	
	
	public void resourceReconcileGroupsFull() {
		try {
			reconcileManager.reconcileGroupsFull(resource.getUniqueName(),isExecuteProcessAsync());
			
			if(isExecuteProcessAsync()) {
				facesMessages.add("Created a task to fully reconcile resource #0's groups.",resource.getDisplayName());
			} else {
				facesMessages.add("Succesfully fully reconciled resource #0's groups",resource.getDisplayName());
			}
		} catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,e.getMessage());
		}
	}
	
	public void resourceReconcileGroupMembershipFull() {
		try {
			reconcileManager.reconcileGroupMembershipFull(resource.getUniqueName(),isExecuteProcessAsync());
			
			if(isExecuteProcessAsync()) {
				facesMessages.add("Created a task to fully reconcile resource #0's group membership.",resource.getDisplayName());
			} else {
				facesMessages.add("Succesfully fully reconciled resource #0's group membership",resource.getDisplayName());
			}
		} catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public void reconcileAllResources() {
		//TODO: should be implemented.
		//implement your business logic here
		String msg = "Successfully Requested Reconcilliation for all Active Target Systems!"; 
		log.info(msg);
		facesMessages.add(msg);
	}

	@Deprecated
	public void reconcileResource() {
        try {
            if (fetchActiveData) {
            	log.debug("Will also fetch active data as requested, creating a task to reconcile and fetch active data for resource #0",resource.getDisplayName());
                reconcileManager.createReconcileResourceOperation(resource,fetchActiveData);
            } else {
            	log.debug("Won't fetch active data as requested, creating a task to reconcile resource #0",resource.getDisplayName());
                reconcileManager.createReconcileResourceOperation(resource,fetchActiveData);
            }
            
            facesMessages.add("Successfully requested resource reconciliation for resource name: "+ resource.getDisplayName()+ ".");

        }
        catch (TaskCreationException e) {
            facesMessages.add(
                    "Error while trying to reconcile resource: '"
                    + resource.getDisplayName()
                    + "': " + e.toString());
        }
	}
	
	
	@Deprecated
	public void reconcileUsers() {
		try {
			reconcileManager.reconcileUsers(true);
			facesMessages.add("Successfully created a task for Users Reconcilliation process...");
		} catch (ReconcileUsersException rue) {
			facesMessages.add(rue.getMessage());
		}
	}
	
	
	
	public void reconcileIdentityAttributes() {
		try {
			reconcileManager.reconcileIdentityAttributes();
			facesMessages.add("Successfully requested Identity Attributes Reconcilliation process...");
		} catch (ReconcileProcessException rue) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,rue.getMessage());
		}
	}
	

	/**
	 * @return the fetchActiveData
	 */
	@Deprecated
	public boolean isFetchActiveData() {
		return fetchActiveData;
	}

	/**
	 * @param fetchActiveData the fetchActiveData to set
	 */
	@Deprecated
	public void setFetchActiveData(boolean fetchActiveData) {
		this.fetchActiveData = fetchActiveData;
	}

	
	public boolean isExecuteProcessAsync() {
		return executeProcessAsync;
	}

	public void setExecuteProcessAsync(boolean executeProcessAsync) {
		this.executeProcessAsync = executeProcessAsync;
	}

	@Destroy
	@Remove
	public void destroy() {
	}
}
