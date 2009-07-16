package velo.reconciliation.processes;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import velo.actions.readyActions.ReadyActionAPI;
import velo.collections.ResourceGroups;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.utils.JndiLookup;
import velo.entity.Account;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcileProcessSummary;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceGroupMember;
import velo.entity.SystemEvent;
import velo.entity.LogEntry.EventLogLevel;
import velo.entity.ReconcileProcessSummary.ReconcileProcesses;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventEntityType;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventSeverities;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEvents;
import velo.exceptions.EventExecutionException;
import velo.exceptions.ReconcileProcessException;

public class ReconcileGroupMembershipProcess {
	private static Logger log = Logger.getLogger(ReconcileGroupMembershipProcess.class.getName());

	Resource resource;
	private ReconcileEvent GROUP_MEMBER_ASSOCIATED_EVENT;
	private ReconcileEvent GROUP_MEMBER_DISSOCIATED_EVENT;
	private ReconcileEvent GROUP_MEMBERSHIP_MODIFY_EVENT;
	private ReconcileProcessSummary rps = null;
	private Map<String,ResourceGroup> allGroupsInRepositoryForResource;

	StopWatch subStopWatch = new StopWatch();


	public void executeFull(ResourceGroups allActiveGroups) throws ReconcileProcessException {
		execute(allActiveGroups, true);
	}

	public void executeIncrementally(ResourceGroups incrementalActiveGroups) throws ReconcileProcessException {
		execute(incrementalActiveGroups, false);
	}


	private void execute(ResourceGroups activeResourceGroups, boolean full) throws ReconcileProcessException {
		StopWatch globalSW = new StopWatch();
		globalSW.start();

		try {
			rps = null;
			if (full) {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_GROUP_MEMBERSHIP_FULL);
			} else {
				rps = new ReconcileProcessSummary(resource, ReconcileProcesses.RECONCILE_GROUP_MEMBERSHIP_INCREMENTAL);
			}
			rps.start();

			//GENERAL, REQUIRED FOR ALL EVENTS
			OperationContext context = new OperationContext();
			context.addVar("resource", getResource());
			context.addVar("resourceUniqueName", getResource().getUniqueName());
			context.addVar("resourceDisplayName", getResource().getDisplayName());
			context.addVar("rps", rps);
			context.addVar("veloAPI",ReadyActionAPI.getInstance());

			//For efficiency, load ALL of the events once.
			GROUP_MEMBER_ASSOCIATED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_MEMBER_ASSOCIATED_EVENT_NAME);
			GROUP_MEMBER_DISSOCIATED_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_MEMBER_DISSOCIATED_EVENT_NAME);
			GROUP_MEMBERSHIP_MODIFY_EVENT = getEventManager().findReconcileEvent(ReconcileEvent.GROUP_MEMBERSHIP_MODIFY_EVENT_NAME);

			log.info("Starting reconcile resource group membership process for resource '" + getResource().getDisplayName() + "'");
			log.debug("Recieved an list of active resource groups (with members) from resource with amount '" + activeResourceGroups.size() + "'");

			//Initialize a SET that will contain all groups that were inserted
			//Set<ResourceGroup> createdActiveGroups = new HashSet<ResourceGroup>();
			//Set<ResourceGroup> existedActiveGroups = new HashSet<ResourceGroup>();


			if (full) {
				subStopWatch.start();
				log.debug("(START) Loading all groups from repository (in full reconcile group membership process only) one time only...");
				allGroupsInRepositoryForResource = resource.getGroupsAsMap();
//				allGroupsNamesInRepositoryForResourceForRemoval = new HashSet<String>();
//				for (ResourceGroup currGroup : allGroupsInRepositoryForResource.values()) {
//					allGroupsNamesInRepositoryForResourceForRemoval.add(currGroup.getUniqueIdInRightCase());
//				}

				subStopWatch.stop();
				log.debug("(FINISHED) Loading all groups from repository (in full reconcile group membership process) one time only (with amount '" + allGroupsInRepositoryForResource.size() +"') in '" + subStopWatch.getTime()/1000 + "' seconds.");
				subStopWatch.reset();
			}



			subStopWatch.start();
			log.debug("(START): Iterating over recieved active groups, determining groupmembership for each existence group in repository...");
			//Iterate over each active groups, verify whether it exists on velo repo or not
			for (ResourceGroup currActiveRG : activeResourceGroups) {
				boolean wasRepoGroupModified = false;
				log.trace("Determining whether active group '" + currActiveRG.getUniqueIdInRightCase() + "' exists in Velo repo...");
				if (isGroupExistInRepository(currActiveRG.getUniqueIdInRightCase(), full)) {
					log.trace("Active group '" + currActiveRG.getUniqueIdInRightCase() + "' was -found- in Velo repo. (reconciling group membership...)");
					
					ResourceGroup currRepoRG = findGroupInRepository(currActiveRG.getUniqueIdInRightCase(), full);
					
					//Verify whether group exist in resource
					if (currRepoRG == null) {
						log.info("Active group name '" + currActiveRG.getDisplayName() + "' does not exist in repository, skipping membership reconciliation for this group.");
						continue;
					}
					
					//add both groups (active/repo) to context
					context.addVar("group",currActiveRG);
					context.addVar("groupFromRepo",currRepoRG);
					
					//Reconcile group's members
					Map<String,ResourceGroupMember> currActiveRGMembers = currActiveRG.getMembersAsMap();
					Map<String,ResourceGroupMember> currRepoRGMembers = currRepoRG.getMembersAsMap();
					
					for (Map.Entry<String,ResourceGroupMember> currActiveMember : currActiveRGMembers.entrySet()) {
						if (!currRepoRGMembers.containsKey(currActiveMember.getKey())) {
							//NEW MEMBERSHIP!
							
							
							/*Managed by ready action
							//add the group to the entity
							//find the corresponding account on repo
							Account accToBeMember = getAccountManager().findAccount(currActiveMember.getKey(),resource);
							if (accToBeMember == null) {
								rps.addLog(EventLogLevel.ERROR,"Could not add member name '" + currActiveMember.getKey() + "' as account in corresponding name was not found in repository!");
							} else {
								//handeled in ready action
								//ResourceGroupMember newMemberEntity = new ResourceGroupMember(accToBeMember,currRepoRG);
								//currRepoRG.getMembers().add(newMemberEntity);
								//getEntityManager().persist(newMemberEntity);
								
								wasRepoGroupModified = true;
								context.addVar("accountToBeMember",accToBeMember);
								//log.info("Raising 'GROUP_MEMBER_ASSOCIATED' event for resource group unique id '" + newCreatedAccount.getNameInRightCase() + "'");
								rps.addEvent(ReconcileProcessSummaryEvents.GROUP_MEMBER_ASSOCIATED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currActiveMember.getKey(), "A new identity named '" + currActiveMember.getKey() + "' was associated to group '" + currActiveRG.getUniqueId() + "'");
								getEventManager().raiseReconcileEvent(GROUP_MEMBER_ASSOCIATED_EVENT, getResource().getReconcilePolicy(), context);
							}
							*/
							
							wasRepoGroupModified = true;
							context.addVar("accountNameAssociated",currActiveMember.getKey());

							Account accountInRepo = getAccountManager().findAccount(currActiveMember.getKey(), getResource());
							context.addVar("account", accountInRepo);
							
							rps.addEvent(ReconcileProcessSummaryEvents.GROUP_MEMBER_ASSOCIATED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currActiveMember.getKey(), "A new identity named '" + currActiveMember.getKey() + "' was associated to group name '" + currRepoRG.getDisplayName() + "'");
							getEventManager().raiseReconcileEvent(GROUP_MEMBER_ASSOCIATED_EVENT, getResource().getReconcilePolicy(), context);
							//cleanup context
							context.removeVar("accountNameAssociated");
							context.removeVar("account");
						}
					}
					
					for (Map.Entry<String,ResourceGroupMember> currRepoMember : currRepoRGMembers.entrySet()) {
						if (!currActiveRGMembers.containsKey(currRepoMember.getKey())) {
							//REMOVED MEMBERSHIP!
							//log.info("Raising 'GROUP_MEMBER_DISSOCIATE' event for account name '" + newCreatedAccount.getNameInRightCase() + "'");
							context.addVar("memberDissociated",currRepoMember.getValue());
							rps.addEvent(ReconcileProcessSummaryEvents.GROUP_MEMBER_DISSOCIATED, ReconcileProcessSummaryEventSeverities.INFO, ReconcileProcessSummaryEventEntityType.IDENTITY, currRepoMember.getKey(), "An identity named '" + currRepoMember.getKey() + "' was dissociated from group name '" + currRepoRG.getDisplayName() + "'");
							getEventManager().raiseReconcileEvent(GROUP_MEMBER_DISSOCIATED_EVENT, getResource().getReconcilePolicy(), context);
							wasRepoGroupModified = true;
							//Managed by ready action
							//currRepoRG.getMembers().remove(currRepoMember.getValue());
							//getEntityManager().remove(currRepoMember.getValue());
							
							context.removeVar("memberDissociated");
						}
					}
					
					
					if (wasRepoGroupModified) {
						//getEntityManager().merge(currRepoRG);
						//generic event!
						//set new / removed members into context
						
						//TODO: Raise generic event, should support all members removed/new assigned
						wasRepoGroupModified = false;
					}
				} else {
					log.debug("Active group '" + currActiveRG.getUniqueIdInRightCase() + "' was -not found- in Velo repo. (skipping group...[will be reconciled after group reconciliation])");
					rps.addLog(EventLogLevel.INFO, "Group '" + currActiveRG.getUniqueIdInRightCase() + "' ("+currActiveRG.getDisplayName() +") was not found in Velo repo.");
				}
			}
			
			subStopWatch.stop();
			log.debug("(FINISHED): Iterating over recieved active groups, determining groupmembership for each existence group in repository in '" + subStopWatch.getTime() / 1000 + "' seconds.");
			subStopWatch.reset();



			globalSW.stop();
			rps.setSuccessfullyFinished(true);
			rps.end();
			getReconcileManager().persistReconcileProcessSummary(rps);

			//EXECUTE POST EVENT
			getEventManager().raiseSystemEvent(SystemEvent.EVENT_RESOURCE_RECONCILIATION_POST, context);

			context.clear();
			log.info("Finished successfully the resource group membership reconciliation process for resource '" + getResource().getDisplayName() + "' in '" + globalSW.getTime()/1000 + "' seconds.");
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
	
	public AccountManagerLocal getAccountManager() throws NamingException {
		InitialContext ic = new InitialContext();
		AccountManagerLocal accManager = (AccountManagerLocal) ic.lookup(JndiLookup.getJNDILocalBeanName("AccountBean"));

		return accManager;
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
