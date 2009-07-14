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
package velo.ejb.impl;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.TransactionTimeout;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import velo.common.EdmMessages;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.IdentityAttributeManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.ReconcileManagerRemote;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.BulkTask;
import velo.entity.IdentityAttributesSyncTask;
import velo.entity.ReconcilePolicy;
import velo.entity.ReconcileProcessSummary;
import velo.entity.ReconcileUsersPolicy;
import velo.entity.Resource;
import velo.entity.ResourceReconcileTask;
import velo.entity.ResourceTask;
import velo.entity.ResourceTypeOperation;
import velo.entity.Task;
import velo.entity.TaskDefinition;
import velo.exceptions.OperationException;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ReconcileGroupsException;
import velo.exceptions.ReconcileProcessException;
import velo.exceptions.ReconcileUsersException;
import velo.exceptions.TaskCreationException;
import velo.exceptions.TaskExecutionException;
import velo.reconcilidation.ReconcileAccounts;
import velo.reconcilidation.ReconcileGroups;
import velo.reconcilidation.ReconcileIdentityAttributes;
import velo.reconcilidation.ReconcileUsers;

/**
 * A Stateless EJB bean for managing Reconcile processes and policies
 * 
 * @author Asaf Shakarchi
 */

@EJBs( { // Required by Reconcile Identity Attributes action (NOT HERE,
			// SHOULD BE MOVED TO TASK BEAN!)
		@EJB(name = "accountEjbRef", beanInterface = AccountManagerLocal.class),
		@EJB(name = "resourceAttributeEjbRef", beanInterface = ResourceAttributeManagerLocal.class),
		@EJB(name = "identityAttributeEjbRef", beanInterface = IdentityAttributeManagerLocal.class),

		@EJB(name = "tsgEjbRef", beanInterface = ResourceGroupManagerLocal.class),
		@EJB(name = "userEjbRef", beanInterface = UserManagerLocal.class),
		@EJB(name = "resourceEjbRef", beanInterface = ResourceManagerLocal.class) })

		@Stateless()
@Name("reconcileManager")
@AutoCreate
public class ReconcileBean implements ReconcileManagerLocal,
		ReconcileManagerRemote {

	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	/**
	 * Inject A local ResourceManager EJB
	 */
	@EJB
	public ResourceManagerLocal resourceManager;

	@EJB
	public ResourceAttributeManagerLocal tsam;

	@EJB
	public ResourceGroupManagerLocal tsgm;

	@EJB
	public AccountManagerLocal am;

	@EJB
	public UserManagerLocal userm;
	
	@EJB
	public TaskManagerLocal taskManager;

	/**
	 * Inject A local CommonUtilsManager EJB
	 */
	@EJB
	public CommonUtilsManagerLocal cum;

	@EJB
	public TaskManagerLocal tm;

	@EJB
	public IdentityAttributeManagerLocal iam;
	

	private static Logger log = Logger.getLogger(ReconcileBean.class.getName());

	
	public ReconcilePolicy findReconcilePolicy(String name) {
    	log.debug("Finding Reconcile Policy in repository with  name '" + name + "'");

		try {
			Query q = em.createNamedQuery("reconcilePolicy.findByName").setParameter("name",name);
			return (ReconcilePolicy) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Could not find any Reconcile Policy for name '" + name + "', returning null.");
			return null;
		}
    }
	
	public void reconcileAllIdentities(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling all identities has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileAllIdentitiesTask(resource);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	
	public void reconcileIdentitiesIncrementally(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling identities incrementally process has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		

		
		String operationUniqueName = "RESOURCE_IDENTITIES_RECONCILIATION_INCREMENTAL";
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		if (rto == null) {
			throw new OperationException("Resource Reconciliation operation is not supported(or does not exist) by resouce '" + resource.getDisplayName() + "'");
		}
		
		
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileIdentitiesIncrementally(resource, rto);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	public void reconcileIdentitiesFull(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling identities FULL process has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		

		
		String operationUniqueName = "RESOURCE_IDENTITIES_RECONCILIATION_FULL";
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		if (rto == null) {
			throw new OperationException("Resource Reconciliation operation is not supported(or does not exist) by resouce '" + resource.getDisplayName() + "'");
		}
		
		
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileIdentitiesFull(resource, rto);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	public void reconcileGroupsFull(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling groups FULL process has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		
		String operationUniqueName = "RESOURCE_GROUPS_RECONCILIATION_FULL";
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		if (rto == null) {
			throw new OperationException("Resource Reconciliation operation is not supported(or does not exist) by resouce '" + resource.getDisplayName() + "'");
		}
		
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileGroupsFull(resource, rto);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	
	public void reconcileGroupMembershipFull(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling group membership FULL process has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		
		String operationUniqueName = "RESOURCE_GROUP_MEMBERSHIP_RECONCILIATION_FULL";
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		if (rto == null) {
			throw new OperationException("Resource Reconciliation operation is not supported(or does not exist) by resouce '" + resource.getDisplayName() + "'");
		}
		
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileGroupMembershipFull(resource, rto);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	public void reconcileGroupMembershipIncremental(String resourceUniqueName, boolean async) throws OperationException {
		log.debug("Reconciling group membership INCREMENTAL process has requested.");
		
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			throw new OperationException("Full reconcile group membership operation is not supported(or does not exist) for resouce '" + resource.getDisplayName() + "'");
		}
		
		//TODO: Remove this when offline fetch will be supported
		resource.setAutoFetch(true);
		
		String operationUniqueName = "RESOURCE_GROUP_MEMBERSHIP_RECONCILIATION_INCREMENTAL";
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		if (rto == null) {
			throw new OperationException("Incremental reconcile group membership operation is not supported(or does not exist) for resouce '" + resource.getDisplayName() + "'");
		}
		ResourceReconcileTask task = ResourceReconcileTask.factoryReconcileGroupMembershipIncrementally(resource, rto);
		task.setAsync(async);
		
		try {
			taskManager.executeTask(task);
		} catch (TaskExecutionException e) {
			throw new OperationException(e);
		}
	}
	
	
	public void persistReconcileProcessSummary(ReconcileProcessSummary reconcileProcessSummary) {
		em.persist(reconcileProcessSummary);
	}
	
	public int deleteAllReconcileProcessSummaries(Date untilDate) {
		//return em.createNamedQuery("reconcileProcessSummary.deleteUntil").setParameter("untilDate", untilDate).executeUpdate();
		List<ReconcileProcessSummary> list = em.createNamedQuery("reconcileProcessSummary.selectUntil").setParameter("untilDate", untilDate).getResultList();
		int amount = list.size();
		
		for (ReconcileProcessSummary currRPS : list) {
			em.remove(currRPS);
		}
		
		return amount;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public BulkTask createReconcileResourceBulkTask(Resource resource, boolean fetchActiveData) throws TaskCreationException {
		try {
			log.info("Requested a reconcile process for target: "+ resource.getDisplayName());
			BulkTask bulkTask = BulkTask.factory("Reconcile Process for Resource name: "+ resource.getDisplayName());
			
			EdmMessages ems = new EdmMessages();
			String shortMsg = "Reconciling resource name: "+ resource.getDisplayName();
			ems.info(shortMsg);
			
			ResourceTask syncDataTask = null;
			if (fetchActiveData) {
				log.debug("Requested to fetch active data before reconciliation, adding a task to fetch data first...");
				syncDataTask = resourceFetchActiveDataOfflineTask(resource);
				bulkTask.addTask(syncDataTask);				
			}
			
			
			ResourceTask recTask = reconcileResourceTask(resource);
			if ( (fetchActiveData) && (syncDataTask!=null) ) {
				recTask.addDependentTask(syncDataTask);
			}
			bulkTask.addTask(recTask);
			
			
			
			
			return bulkTask;
		}
		catch (TaskCreationException e) {
			throw e;
		}
	}
	public void createReconcileResourceOperation(Resource resource, boolean fetchActiveData) throws TaskCreationException {
		tm.persistBulkTask(createReconcileResourceBulkTask(resource,fetchActiveData));
	}

	public ResourceTask reconcileResourceTask(Resource resource) throws TaskCreationException {
		String operationUniqueName = "RESOURCE_RECONCILIATION";
		
		ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new TaskCreationException("Resource Reconciliation operation is not supported by resouce '" + resource.getDisplayName() + "'");
		}
		
		
		ResourceTask resourceTask = ResourceTask.factory(resource.getUniqueName(), rto, "Resource reconciliation process for resource '" + resource.getDisplayName() + "'");

		
		// Start a log trail for the task
		resourceTask.addLog("INFO", "Reconcile Process event","Task to reconcile process for resource name: " + resource.getDisplayName());

		return resourceTask;
	}
		
	
	
	
	
	
	
	//FETCH ACTIVE DATA TASK CREATION
	public ResourceTask resourceFetchActiveDataOfflineTask(Resource resource) throws TaskCreationException {
        log.info("Factoring a 'Sync Resource Active Data Offline' task for resource: " + resource.getDisplayName());
        String operationUniqueName = "RESOURCE_FETCH_ACTIVE_DATA_OFFLINE";

        ResourceTypeOperation rto = resource.getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new TaskCreationException("Resource fetch active data offline operation is not supported by resouce '" + resource.getDisplayName() + "'");
		}
		
		ResourceTask resourceTask = ResourceTask.factory(resource.getUniqueName(), rto, "Fetch active data for resource '" + resource.getDisplayName() + "'");

		
		// Start a log trail for the task
		resourceTask.addLog("INFO", "Resource Fetch Active Data Process","Task to fetch active data offline for resource name: " + resource.getDisplayName());
        
		return resourceTask;
	}
	
        
    public Long resourceFetchActiveDataOffline(Resource resource) throws TaskCreationException {
        ResourceTask resourceTask = resourceFetchActiveDataOfflineTask(resource);
        
        //Persist the task
        tm.persistTask(resourceTask);
        em.flush();
        
        return resourceTask.getTaskId();
    }
    
    
    //in seconds
    @TransactionTimeout(value=3600)
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Transactional
    public void reconcileIdentityAttributes() throws ReconcileProcessException {
		log.info("Requested a User Identity Attributes Reconcile process, constructing a task...");
		
		/*REPLACING WITH TASK
		ReconcileIdentityAttributes ria = new ReconcileIdentityAttributes();
		try {
			ria.__execute__();
		}catch (ActionFailureException e) {
			throw new ReconcileProcessException(e.getMessage());
		}
		*/
		
		
		IdentityAttributesSyncTask iast = IdentityAttributesSyncTask.factory();
		em.persist(iast);
		
		
		
		
		
		//VERY OLD
		/*BulkTask bulkTask = BulkTask.factory("User Identity Attributes Reconcile process");
		bulkTask.setDescription("Reconcile Identity Attributes");

		Task recTask = reconcileUserIdentityAttributesTask();
		recTask.setBulkTask(bulkTask);
		bulkTask.addTask(recTask);

		// Everything was okay, persist the bulk task
		tm.persistBulkTask(bulkTask);
		log
				.info("Successfully created task of Reconcile Identity Attributes process.");
				*/

		// SYNC method, now encapsulated as a task and persisted in DB.
		// Collection<IdentityAttribute> identityAttributesToSync =
		// iam.loadIdentityAttributesToSync();
		// Collection<User> usersToSync =
		// userm.findUsersToSyncIdentityAttributes();

		/*
		 * //Perform data validation before proceeding... if
		 * (identityAttributesToSync.size() == 0) { throw new
		 * ReconcileProcessException("Cannot perform Identity Attributes
		 * Reconciliation, there are no Identity Attributes flagged to be
		 * synced!"); }
		 * 
		 * if (usersToSync.size() == 0) { throw new
		 * ReconcileProcessException("Cannot perform Identity Attributes
		 * Reconciliation, there are no Users flagged to be synced!"); }
		 */

		// log.info("Performing Identity Attributes Reconciliation, will sync
		// '" + identityAttributesToSync.size() + "' to sync, for each user,
		// Users to sync amount is '" + usersToSync.size() + "'");
		// ReconcileIdentityAttributes ria = new ReconcileIdentityAttributes();
		// ria.perform(identityAttributesToSync, usersToSync);
	}
    
    
    
    
    
    
    
	
	
	
	
	
	
	
	
	
	
    
    
    
    
    
    
    
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Deprecated
	public boolean isReconcilePolicyExitsByName(String name) {
		// Query database to see if user already exists...
		Query query = em.createNamedQuery("reconcilePolicy.findByName");
		List entities = query.setParameter("name", name).getResultList();

		if (entities.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Deprecated
	public boolean isReconcileUsersPolicyExitsByUniqueName(String uniqueName) {
		// Query database to see if user already exists...
		Query query = em
				.createNamedQuery("reconcileUsersPolicy.findByUniqueName");
		List entities = query.setParameter("uniqueName", uniqueName)
				.getResultList();

		if (entities.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Deprecated
	public List<ReconcileUsersPolicy> findAllActiveReconcileUsersPoliciesOrderedByPriority() {
		List<ReconcileUsersPolicy> policies = (List) em.createNamedQuery(
				"reconcileUsersPolicy.findAllActive").getResultList();

		return policies;
	}

	@Deprecated
	public void reconcileUsers(boolean withSyncTarget)
			throws ReconcileUsersException {
		try {
			log.info("Requested a reconcile users process");
			BulkTask bulkTask = BulkTask.factory("Reconcile Users Process");
			EdmMessages ems = new EdmMessages();
			String shortMsg = "Reconciling Users";
			ems.info(shortMsg);

			// Find a list of active reconcile users policies ordered by
			// priority
			List<ReconcileUsersPolicy> policies = findAllActiveReconcileUsersPoliciesOrderedByPriority();

			if (policies.size() < 1) {
				throw new TaskCreationException(
						"Could not find any active reconcile users policy!");
			}

			// IMPORTAMT: Clear the entityManager before creating actions, since
			// action factories usually modify the entities (remove references
			// due to size of entities) which will cause an update
			// Of the entity in the DB if the entity is managed!
			em.clear();

			if (withSyncTarget) {
				log
						.debug("Requested to sync the target's data as a part of the reconcile process..");

				for (ReconcileUsersPolicy currRUP : policies) {
					log
							.debug("Creating SYNC DATA task for ReconcileUsersPolicy named: '"
									+ currRUP.getDisplayName()
									+ "', for Target source named: '"
									+ currRUP.getSourceResource()
											.getDisplayName() + "'");
					// Generate the xml syncing file task
					//JB?!?! Task syncFileTask = tsm.syncListActionTask(currRUP.getSourceResource(), bulkTask);
					//JB?!?! bulkTask.addTask(syncFileTask);
				}
			}

			log
					.debug("Adding dependencies between tasks - ReconcileUsers task depends on all Sync targets tasks...");
			// Add dependencies between tasks - ReconcileUsers task depends on
			// all Sync targets
			Task recTask = reconcileUsersTask(policies);
			for (Task currSyncTask : bulkTask.getTasks()) {
				log.trace("ReconcileUsers task depends on task name: '"
						+ currSyncTask);
				recTask.addDependentTask(currSyncTask);
			}

			log.trace("Adding ReconcileUsers task into BulkTask");
			bulkTask.addTask(recTask);

			log.debug("Persisting bulk task of Reconcile Users Process...");
			// Everything was okay, persist the bulk task
			tm.persistBulkTask(bulkTask);
			log
					.debug("Successfully created task of reconciliation users process.");
		} catch (TaskCreationException tce) {
			throw new ReconcileUsersException(tce.getMessage());
		}
	}

	@Deprecated
	public Task reconcileUsersTask(
			List<ReconcileUsersPolicy> reconcileUsersPolicies)
			throws TaskCreationException {
		log.debug("Creating a task of reconcile users process");

		TaskDefinition td;
		td = tm.findTaskDefinition("RECONCILE_USERS");
		Task task = td.factoryTask();
		log.trace("Loading task definition with description: '"
				+ td.getDescription() + "'");

		// Start a log trail for the task
		task.addLog("INFO", "Reconcile Process event",
				"Task to reconcile users");

		// Create the reconcile process action and set the target system object
		// into it
		ReconcileUsers ru = new ReconcileUsers(reconcileUsersPolicies);

		log
				.debug("Created ReconcileUsers object, serializing object into task...");
		task.serializeAsTask(ru);
		log.debug("task successfully created, returning created task...");

		return task;
	}


	@Deprecated
	public void reconcileAccountsByResource(Resource resource)
			throws ReconcileAccountsException, NoResultException {
		log.info("-START- of accounts reconciliation process.");
		ReconcileAccounts ra = new ReconcileAccounts();
		try {
			Resource managedTs = em.find(Resource.class, resource
					.getResourceId());
			em.refresh(managedTs);
			ra.performResourceAccountsReconciliation(managedTs);

		} catch (ReconcileAccountsException rae) {

			// Last place to log the event
			String summaryMessage = "Failed to perform reconcile accounts process for target system name: "
					+ resource.getDisplayName();
			String detailedMessage = "Failed not perform reconcile accounts process for target system name: "
					+ resource.getDisplayName() + ", " + rae.getMessage();

			// TODO Replace!
			//EventLog el = new EventLog("Reconcile", "FAILURE", detailedMessage);
			//cum.addEventLog(el);

			throw new ReconcileAccountsException(detailedMessage);
		}
		log.info("-END- of accounts reconciliation process.");
	}

	@Deprecated
	public void reconcileGroupsByResource(Resource resource)
			throws ReconcileGroupsException {
		// ReconcileGroups rg = new ReconcileGroups(am, tsgm);
		ReconcileGroups rg = new ReconcileGroups();

//		try {
			// Keep execution time in milliseconds
			long startExecutionTime = System.currentTimeMillis();
			log
					.info("Reconcile Groups process has just STARTED for Resource name: "
							+ resource.getDisplayName());

//			rg.performResourceGroupsReconciliation(resource, rrs);

			// Close execution time
			float executionTime;
			executionTime = System.currentTimeMillis() - startExecutionTime;
			executionTime = executionTime / 1000;
			log
					.info("Reconcile Groups process has just ENDED for Resource name: "
							+ resource.getDisplayName()
							+ ", execution time is: "
							+ executionTime
							+ "seconds.");
//		} catch (ReconcileGroupsException rae) {

			// Last place to log the event
			String summaryMessage = "Could not perform reconcile groups process for target system name: "
					+ resource.getDisplayName()
					+ ", please see EventLog for detailed information";

//			String detailedMessage = "Could not perform reconcile groups process for target system name: "
//					+ resource.getDisplayName() + ", " + rae.getMessage();

			// TODO Replace!
//			EventLog el = new EventLog("Reconcile", "FAILURE", detailedMessage);
//			cum.addEventLog(el);

//			log.warn(detailedMessage);
			throw new ReconcileGroupsException(summaryMessage);
//		}
	}

	/*
	 * public void reconcileAccountsByResourceName(String tsName) throws
	 * NoResultException, ReconcileAccountsException { try { Resource ts =
	 * tsm.findResourceByName(tsName); em.refresh(ts);
	 * reconcileAccountsByResource(ts); } catch (NoResultException nre) { throw
	 * nre; } catch (ReconcileAccountsException rae) { throw rae; } }
	 */

	@Deprecated
	public void createReconcilePolicy(ReconcilePolicy rp) {
		em.persist(rp);
	}

	@Deprecated
	public void createReconcileUsersPolicy(ReconcileUsersPolicy rup) {
		em.persist(rup);
	}

	@Deprecated
	public void removeReconcilePolicy(ReconcilePolicy rp) {
		log.debug("Removing Reconcile Policy ID: " + rp.getReconcilePolicyId());

		ReconcilePolicy mergedEntity = em.merge(rp);
		em.remove(mergedEntity);
	}

	@Deprecated
	public void removeReconcileUsersPolicy(ReconcileUsersPolicy rup) {
		log.debug("Removing Reconcile Users Policy ID: "
				+ rup.getReconcileUsersPolicyId());

		ReconcileUsersPolicy mergedEntity = em.merge(rup);
		em.remove(mergedEntity);
	}

	@Deprecated
	public void updateRconcilePolicy(ReconcilePolicy rp) {
		em.merge(rp);
	}

	@Deprecated
	public void updateRconcileUsersPolicy(ReconcileUsersPolicy rup) {
		em.merge(rup);
	}

	@Deprecated
	public List<ReconcilePolicy> findAllReconcilePolicy() {
		return em.createNamedQuery("findAllReconcilePolicy").getResultList();
	}

	@Deprecated
	public List<ReconcileUsersPolicy> findAllReconcileUsersPolicy() {
		return em.createNamedQuery("reconcileUsersPolicy.findAll")
				.getResultList();
	}


	@Deprecated
	public Task reconcileUserIdentityAttributesTask() {
		TaskDefinition td;
		td = tm.findTaskDefinition("RECONCILE_USER_IDENTITY_ATTRIBUTES");
		Task task = td.factoryTask();

		// Start a log trail for the task
		task.addLog("INFO", "Reconcile User Identity Attributes Task",
				"Task to Reconcile User Identity Attributes Process");

		// Before serializing the resource, the lazily fetched data that is
		// used after deserialization process must be touched!
		// ts.getGroups().size();

		// IMPORTAMT: Clear the entityManager before creating actions, since
		// action factories usually modify the entities (remove references due
		// to size of entities) which will cause an update
		// Of the entity in the DB if the entity is managed!
		em.clear();

		// Create the reconcile process action and set the target system object
		// into it
		ReconcileIdentityAttributes ria = new ReconcileIdentityAttributes();
		task.serializeAsTask(ria);

		return task;
	}

}