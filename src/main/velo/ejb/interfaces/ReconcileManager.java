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

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import velo.entity.BulkTask;
import velo.entity.ReconcilePolicy;
import velo.entity.ReconcileProcessSummary;
import velo.entity.ReconcileUsersPolicy;
import velo.entity.Resource;
import velo.entity.Task;
import velo.exceptions.OperationException;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ReconcileGroupsException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.ReconcileUsersException;
import velo.exceptions.TaskCreationException;

/**
 * A ReconcileManager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface ReconcileManager {
	public ReconcilePolicy findReconcilePolicy(String name);
	public void reconcileAllIdentities(String resourceUniqueName, boolean async) throws OperationException;
	public void reconcileIdentitiesIncrementally(String resourceUniqueName, boolean async) throws OperationException;
	public void reconcileIdentitiesFull(String resourceUniqueName, boolean async) throws OperationException;
	public void reconcileGroupsFull(String resourceUniqueName, boolean async) throws OperationException;
	public void reconcileGroupMembershipFull(String resourceUniqueName, boolean async) throws OperationException;
	public void reconcileGroupMembershipIncremental(String resourceUniqueName, boolean async) throws OperationException;
	public void persistReconcileProcessSummary(ReconcileProcessSummary reconcileProcessSummary);
	public int deleteAllReconcileProcessSummaries(Date untilDate);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Create reconciliation bulkTask for a certain resource
	 * @param resource The resource to perform the reconcile process
	 * @param fetchActiveData Whether to fetch active data before reconciliation process
	 * @throws ReconcileProcessException
	 */
	public BulkTask createReconcileResourceBulkTask(Resource resource, boolean fetchActiveData) throws TaskCreationException;
	
	public void createReconcileResourceOperation(Resource resource, boolean fetchActiveData) throws TaskCreationException;
	
	/**
	 * Create a task to reconcile a resource
	 * @param resource The resource to create the reconcile process task for
	 * @return The generated reconciliation bulkTask(except reconcile task may also contain a task to fetch active data)
	 * @throws ReconcileProcessException
	 */
	public Task reconcileResourceTask(Resource resource) throws TaskCreationException;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public boolean isReconcilePolicyExitsByName(String name);
	
	@Deprecated
	public boolean isReconcileUsersPolicyExitsByUniqueName(String uniqueName);
	
	@Deprecated
	public List<ReconcileUsersPolicy> findAllActiveReconcileUsersPoliciesOrderedByPriority();
	
	@Deprecated
	public void reconcileUsers(boolean withSyncTarget) throws ReconcileUsersException;
	
	@Deprecated
	public Task reconcileUsersTask(List<ReconcileUsersPolicy> reconcileUsersPolicies) throws TaskCreationException;
	
	
	
	
	/**
     * Reconcile accounts for the specified resource entity
     * @param resource A resource entity to reconcile
     * @throws ReconcileAccountsException
     * @throws NoResultException
     */
	@Deprecated
    public void reconcileAccountsByResource(Resource resource) throws ReconcileAccountsException,NoResultException;
    
	@Deprecated
    public void reconcileGroupsByResource(Resource resource) throws ReconcileGroupsException;
    
    
    /**
     * Reconcile all active resources within the system
     * @return true/false upon full success/failure of the whole reconcile execution
     */
    //public boolean reconcileAccountsOnAllActiveresources();

    /**
     * Reconcile accounts for the specified resource name
     * @param tsName The name of the resource to reconcile
     * @throws NoResultException
     * @throws ReconcileAccountsException
     */
    //public void reconcileAccountsByresourceName(String tsName) throws NoResultException,ReconcileAccountsException;
    
    /**
     * Create a new ReconcilePolicy in the DB
     * @param rp The ReconcilePolicy entity to persist
     */
	@Deprecated
    public void createReconcilePolicy(ReconcilePolicy rp);
    
    /**
     * Create a new ReconcileUsersPolicy in the DB
     * @param rup The ReconcileUsersPolicy entity to persist
     */
	@Deprecated
    public void createReconcileUsersPolicy(ReconcileUsersPolicy rup);
    
	@Deprecated
    public void removeReconcilePolicy(ReconcilePolicy rp);
    
	@Deprecated
    public void removeReconcileUsersPolicy(ReconcileUsersPolicy rup);
    
	@Deprecated
    public void updateRconcilePolicy(ReconcilePolicy rp);
	
	@Deprecated
	public void updateRconcileUsersPolicy(ReconcileUsersPolicy rup);
	
    
    /**
     * Find all reconcile policies within the DB
     * @return A List of ReconcilePolicies available in the DB
     */
	@Deprecated
    public List<ReconcilePolicy> findAllReconcilePolicy();
    
	@Deprecated
    public List<ReconcileUsersPolicy> findAllReconcileUsersPolicy();
    
	@Deprecated
    public void reconcileIdentityAttributes() throws ReconcileProcessException;
}
