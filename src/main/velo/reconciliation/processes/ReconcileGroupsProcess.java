package velo.reconciliation.processes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import velo.collections.ResourceGroups;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.utils.JndiLookup;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcileProcessSummary;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.SystemEvent;
import velo.entity.ReconcileProcessSummary.ReconcileProcesses;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventEntityType;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventSeverities;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEvents;
import velo.exceptions.EventExecutionException;
import velo.exceptions.ReconcileProcessException;

public class ReconcileGroupsProcess {
	private static Logger log = Logger.getLogger(ReconcileGroupsProcess.class.getName());

	Resource resource;
	private ReconcileEvent GROUP_CREATED_EVENT;
	private ReconcileEvent GROUP_MODIFIED_EVENT;
	private ReconcileEvent GROUP_REMOVED_EVENT;
	private ReconcileProcessSummary rps = null;
	private Map<String,ResourceGroup> allGroupsInRepositoryForResource;
	private Set<String> allGroupsNamesInRepositoryForResourceForRemoval;

	StopWatch subStopWatch = new StopWatch();


	public void executeFull(ResourceGroups allActiveGroups) throws ReconcileProcessException {
		execute(allActiveGroups, true);
	}

//	public void executeIncrementally(ResourceGroups incrementalActiveGroups) throws ReconcileProcessException {
//		execute(incrementalActiveGroups, false);
//	}



	private void execute(ResourceGroups activeResourceGroups, boolean full) throws ReconcileProcessException {
		log.info("Starting reconcile resource groups process for resource '" + getResource().getDisplayName() + "'");
		log.debug("Recieved an list of active resource groups from resource with amount '" + activeResourceGroups.size() + "'");

		StopWatch globalSW = new StopWatch();
		globalSW.start();

		
		try {
			rps = null;
			if (full) {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_GROUPS_FULL);
			} else {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_GROUPS_FULL);
			}
			rps.start();

			//GENERAL, REQUIRED FOR ALL EVENTS
			OperationContext context = new OperationContext();
			context.addVar("resource", getResource());
			context.addVar("resourceUniqueName", getResource().getUniqueName());
			context.addVar("resourceDisplayName", getResource().getDisplayName());
			context.addVar("rps", rps);

			//For efficiency, load ALL of the events once.
			GROUP_CREATED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_CREATED_EVENT_NAME); 
			GROUP_MODIFIED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_MODIFIED_EVENT_NAME);
			GROUP_REMOVED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_REMOVED_EVENT_NAME);


			//Initialize a SET that will contain all groups that were inserted
			Set<ResourceGroup> createdActiveGroups = new HashSet<ResourceGroup>();
			Set<ResourceGroup> existedActiveGroups = new HashSet<ResourceGroup>();


			if (full) {
				subStopWatch.start();
				log.debug("(START) Loading all groups from repository (in full reconcile groups process only) one time only...");
				allGroupsInRepositoryForResource = resource.getGroupsAsMap();
				allGroupsNamesInRepositoryForResourceForRemoval = new HashSet<String>();
				for (ResourceGroup currGroup : allGroupsInRepositoryForResource.values()) {
					allGroupsNamesInRepositoryForResourceForRemoval.add(currGroup.getUniqueIdInRightCase());
				}

				subStopWatch.stop();
				log.debug("(FINISHED) Loading all groups from repository (in full reconcile groups process) one time only (with amount '" + allGroupsInRepositoryForResource.size() +"') in '" + subStopWatch.getTime()/1000 + "' seconds.");
				subStopWatch.reset();
			}


			
			//Perform sanity checks for full process
			if (full) {
				int activeAmount = activeResourceGroups.size();
				int repoAmount = allGroupsInRepositoryForResource.size();
				Integer allowedDiffInPercentages = resource.getReconcilePolicy().getSanityCheckDiffPercentagesOfGroups();

				Integer diff = null;
				if (activeAmount > repoAmount) {
					diff = (activeAmount-repoAmount) / activeAmount * 100;
				} else {
					diff = (repoAmount-activeAmount) / repoAmount * 100;
				}
				
				if (diff > allowedDiffInPercentages) { 
					throw new ReconcileProcessException("Sanity check failure: Groups difference between repository and resource is higher than '" + allowedDiffInPercentages + "%, (which is '" + diff + "'%), groups in repo: '" + repoAmount + "', while active amount is: '" + activeResourceGroups.size() + "'");
				}
			} else {
				log.debug("Skipping sanity checks for incremental process.");
			}
			
			

			subStopWatch.start();
			log.debug("(START): Iterating over recieved active groups, determining whether each exists in repository or not.");
			//Iterate over each active groups, verify whether it exists on velo repo or not
			for (ResourceGroup currActiveRG : activeResourceGroups) {
				log.trace("Determining whether active group '" + currActiveRG.getUniqueIdInRightCase() + "' exists in Velo repo...");
				if (isGroupExistInRepository(currActiveRG.getUniqueIdInRightCase(), full)) {
					log.trace("Active group '" + currActiveRG.getUniqueIdInRightCase() + "' was -found- in Velo repo. (adding group to the 'verified existed' list)");
					existedActiveGroups.add(currActiveRG);

					if (full) {
						allGroupsNamesInRepositoryForResourceForRemoval.remove(currActiveRG.getUniqueIdInRightCase());
					}
				} else {
					log.trace("Active group '" + currActiveRG.getUniqueIdInRightCase() + "' was -not found- in Velo repo. (adding group to the new created list)");
					//new group was found, store the group on the appropriate list
					createdActiveGroups.add(currActiveRG);
				}
			}
			subStopWatch.stop();
			log.debug("(FINISHED): Iterating over recieved active groups, determining whether each exists in repository or not in '" + subStopWatch.getTime() / 1000 + "' seconds.");
			subStopWatch.reset();



			//Raising new created event per created group
			log.debug("START: Raising GROUP_CREATED event for '" + createdActiveGroups.size() + "' amount of newly created groups.");
			subStopWatch.start();
			for (ResourceGroup newCreatedGroup : createdActiveGroups) {
				context.addVar("group", newCreatedGroup);
				context.addVar("groupUniqueId", newCreatedGroup.getUniqueIdInRightCase());

				log.info("Raising 'GROUP_CREATED' event for group name '" + newCreatedGroup.getUniqueIdInRightCase() + "'");
				rps.addEvent(ReconcileProcessSummaryEvents.GROUP_CREATED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.GROUP, newCreatedGroup.getUniqueIdInRightCase(), "A new group named '" + newCreatedGroup.getUniqueIdInRightCase() + "' was discovered on resource.");
				getEventManager().raiseReconcileEvent(GROUP_CREATED_EVENT, getResource().getReconcilePolicy(), context);

				//not really needed, just for safety
				context.removeVar("group");
				context.removeVar("groupUniqueId");
			}
			subStopWatch.stop();
			log.debug("FINISHED: Raising CREATED_GROUP of newly created groups in '" + subStopWatch.getTime()/1000 + "' seconds.");
			subStopWatch.reset();












			//GROUP PROPERTIES SYNCHRONIZATION - Performing attributes compare per existence identity
			log.debug("(START) Comparing properties for all existence groups (with amount '" + existedActiveGroups.size() + "')");
			subStopWatch.start();
			//Map<String,String> modifiedProperties = new HashMap<String,String>();
			log.debug("Synchronizing properties for *" + existedActiveGroups.size() + "* groups.");
			for (ResourceGroup currExistGroup : existedActiveGroups) {
				boolean groupModified = false;
				context.addVar("groupUniqueId",currExistGroup.getUniqueIdInRightCase());
				context.addVar("group",existedActiveGroups);

				ResourceGroup correspondingRepoGroupToActiveGroup = findGroupInRepository(currExistGroup.getUniqueIdInRightCase(), full);


				//COMPARE DESCRIPTION
				if (currExistGroup.getDescription() != null) {
					if (!currExistGroup.getDescription().equals(correspondingRepoGroupToActiveGroup.getDescription())) {
						correspondingRepoGroupToActiveGroup.setDescription(currExistGroup.getDescription());
						groupModified = true;
					}
				}
				
				if (!currExistGroup.getDisplayName().equals(correspondingRepoGroupToActiveGroup.getDisplayName())) {
					correspondingRepoGroupToActiveGroup.setDisplayName(currExistGroup.getDisplayName());
					groupModified = true;
				}
				
				//COMPARE TYPE
				if (!currExistGroup.getType().equals(correspondingRepoGroupToActiveGroup.getType())) {
					groupModified = true;
					correspondingRepoGroupToActiveGroup.setType(currExistGroup.getType());
				}

				if (groupModified) {
					rps.addEvent(ReconcileProcessSummaryEvents.GROUP_MODIFIED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.GROUP, currExistGroup.getUniqueIdInRightCase(), "Group name '" + currExistGroup.getUniqueIdInRightCase() + "' was modified.");
					getEventManager().raiseReconcileEvent(GROUP_MODIFIED_EVENT, getResource().getReconcilePolicy(), context);
					getEntityManager().merge(correspondingRepoGroupToActiveGroup);
				}

				//CLEAN THE MODIFIED ATTRIBUTES
				//modifiedAttributes.clear();
			}
			subStopWatch.stop();
			log.debug("(FINISHED) Comparing properties for all existence groups (amount '" + existedActiveGroups.size() + "') in '" + subStopWatch.getTime()/1000 + "' seconds.");
			subStopWatch.reset();













			//--=--ONLY--=-- if full, take care of remove groups
			if (full) {
				subStopWatch.start();
				log.info("(START) -Full- reconciliation was invoked, raising GROUP_REMOVED event for all removed groups (with amount '" + allGroupsNamesInRepositoryForResourceForRemoval.size() + "')");
				for (String currAccountNameToBeRemoved : allGroupsNamesInRepositoryForResourceForRemoval) {
					ResourceGroup currGroupToBeRemoved = findGroupInRepository(currAccountNameToBeRemoved, full);

					context.addVar("group",currGroupToBeRemoved);
					context.addVar("groupUniqueId",currGroupToBeRemoved.getUniqueIdInRightCase());
					getEventManager().raiseReconcileEvent(GROUP_REMOVED_EVENT, getResource().getReconcilePolicy(), context);
					rps.addEvent(ReconcileProcessSummaryEvents.GROUP_REMOVED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.GROUP, currGroupToBeRemoved.getUniqueIdInRightCase(), "Group name '" + currGroupToBeRemoved.getUniqueIdInRightCase() + "' was removed from resource.");
				}
				subStopWatch.stop();
				log.info("(FINISHED) raising GROUP_REMOVED event for all removed groups (with amount '" + allGroupsNamesInRepositoryForResourceForRemoval.size() + "') in '" + subStopWatch.getTime()/1000 + "' seconds.");
				subStopWatch.reset();
			}


			globalSW.stop();
			rps.setSuccessfullyFinished(true);
			rps.end();
			getReconcileManager().persistReconcileProcessSummary(rps);

			//EXECUTE POST EVENT
			getEventManager().raiseSystemEvent(SystemEvent.EVENT_RESOURCE_RECONCILIATION_POST, context);

			context.clear();
			log.info("Finished successfully the resource groups reconciliation process for resource '" + getResource().getDisplayName() + "' in '" + globalSW.getTime()/1000 + "' seconds.");
		}catch(EventExecutionException e) {
			throw new ReconcileProcessException(e.getMessage());
		}catch(NamingException e) {
			throw new ReconcileProcessException(e.getMessage());
		}
	}














	public EntityManager getEntityManager() {
		try {
			InitialContext ic = new InitialContext();
			return (EntityManager)ic.lookup("java:/veloDataSourceEM");
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResourceGroup findGroupInRepository(String groupUniqueIdInRightCase, boolean full) throws NamingException {
		if (full) {
			//the map keys are the group unique id in the right case, thus check containsKey in the right case
			return allGroupsInRepositoryForResource.get(groupUniqueIdInRightCase);
		} else {
			//search in DB whether the account exist or not. (this method supports case sensitive check for resources that are case sensitive)
			return getResourceGroupManager().findGroup(groupUniqueIdInRightCase, getResource());
		}
	}
	
	public boolean isGroupExistInRepository(String groupUniqueIdInRightCase, boolean full) throws NamingException {
		if (full) {
			return allGroupsInRepositoryForResource.containsKey(groupUniqueIdInRightCase);
		} else {
			//search in DB whether the group exist or not. (this method supports case sensitive check for resources that are case sensitive)
			return getResourceGroupManager().isGroupExists(groupUniqueIdInRightCase, getResource());
		}
	}

	public ReconcileManagerLocal getReconcileManager() throws NamingException {
		InitialContext ic = new InitialContext();
		ReconcileManagerLocal reconcileManager = (ReconcileManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("ReconcileBean"));

		return reconcileManager;
	}
	
	public ResourceGroupManagerLocal getResourceGroupManager() throws NamingException {
		InitialContext ic = new InitialContext();
		ResourceGroupManagerLocal reconcileGroupManager = (ResourceGroupManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("ResourceGroupBean"));

		return reconcileGroupManager;
	}

	public EventManagerLocal getEventManager() throws NamingException {
		InitialContext ic = new InitialContext();
		EventManagerLocal eventManager = (EventManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("EventBean"));

		return eventManager;
	}



	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
}
