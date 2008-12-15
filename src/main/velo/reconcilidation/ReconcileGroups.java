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
package velo.reconcilidation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.exceptions.ReconcileGroupsException;
import velo.exceptions.SyncListImporterException;
import velo.processSummary.resourceReconcileSummary.ResourceReconcileSummary;
import velo.processSummary.resourceReconcileSummary.SummaryGroup;

/**
 * A class that represents a ReconcileGroups process for a certain Resource
 * @author Asaf Shakarchi
 */
public class ReconcileGroups {
    private static Logger log = Logger.getLogger("velo.reconcilidation");
    
    private HashMap<String,ResourceGroup> activeGroups = new HashMap<String,ResourceGroup>();
    
    
    
    public ReconcileGroups() {
    	
    }
    
    
    public void performResourceGroupsReconciliation(Resource resource, ResourceReconcileSummary rrs) throws ReconcileGroupsException {
        try {
            // --Fetch the list of accounts to reconcile from the last updated sync file
            SyncTargetImporter syncImporter = new SyncTargetImporter(resource.factorySyncFileName());
            SyncTargetData syncData = syncImporter.getSyncedData();
            
            log.debug("Factoring the 'ActiveGroup' list by the SyncImporter which resulted: '" + syncData.getGroups().size()+"' groups, constructing a MAP of virtual Groups entities");
            //Per active account(that was set by the SyncImporter), generate an 'Account' entity loaded with the iterated activeAccount's attributes
            for (ActiveGroup currGroup : syncData.getGroups()) {
                //Add the loaded account to the 'ActiveAccount' map
                //logger.info("Adding loaded Group name: '" + currGroup.getDisplayName() + "', with description: '" + currGroup.getDescription() + "' to the map.");
                if (activeGroups.containsKey(currGroup.getUniqueId())) {
                    log.warn("Skipping group with uniqueID: '" + currGroup.getUniqueId() + "', with displyName: '" + currGroup.getDisplayName() + "', group ID already found from previous groups!");
                } else {
                	if (currGroup.getUniqueId().equals("") || currGroup.getDisplayName().equals("")) {
                		log.warn("Skipping group as its uniqueId or display name is empty!");
                	} else {
                		activeGroups.put(currGroup.getUniqueId(),currGroup);
                	}
                }
            }
            
            log.debug("Resulted a list of -Active Groups- on target system name: '"
                + resource.getDisplayName()
                + "', resulted *"
                + activeGroups.size()
                + "* groups.");
            
          //update the summary
           rrs.groupsAmountInResource = activeGroups.size();
            
            // --End of account fetching
            log.debug("Dumping Constructed ActiveGroups SET...");
            for (Map.Entry<String,ResourceGroup> currGroup : activeGroups.entrySet()) {
                log.trace("ActiveGroup unique id: " + currGroup.getKey());
            }
            
            
            // Refresh the collection before looking at it (Seems like toplink
            // does not refresh it automatically)
            // resource = (Resource)tsm.refreshEntity(resource);
            
            // Get a collection of all accounts IDM claims to be available
            Set<ResourceGroup> idmClaimedGroupList = resource.getGroups();
            log.debug("Velo claims to have *"
                + idmClaimedGroupList.size()
                + "* groups in repository for resource name: '"
                + resource.getDisplayName() + "'");
            Map<String,ResourceGroup> groupsOfResourceInRepository = getResourceGroupsAsMap(resource);
            
            
            
            Map<String,ResourceGroup> groupsToPersist = new HashMap<String,ResourceGroup>();
            Map<String,ResourceGroup> groupsToRemove = new HashMap<String,ResourceGroup>();
            Map<String,ResourceGroup> groupsToMerge = new HashMap<String,ResourceGroup>();
            //Set<ResourceGroup> groupsToPersist = new HashSet<ResourceGroup>();
            //Set<ResourceGroup> groupsToRemove = new HashSet<ResourceGroup>();
            
            // Iterate through the claimed group list for current iterated Resource
            for (ResourceGroup currClaimedGroup : idmClaimedGroupList) {
                // Check if the claimed group exist within the activeSet
                // NOTE: keys are of course case sensitive
                if (activeGroups.containsKey(currClaimedGroup.getUniqueId().toUpperCase())) {
                    //Make sure the current iterated claimed group is not flagged as deleted.
                    //If so, re-add it.
                    if (currClaimedGroup.isDeletedInResource()) {
                        //groupsToPersist.add(currClaimedGroup);
                    	log.debug("Claimed group that was found in resource is flagged in repostiroy as 'deleted in resource', removing 'deleted in resource' flag from group in repository");
                    	currClaimedGroup.setDeletedInResource(false);
                    	currClaimedGroup.setNumberOfReconcilesGroupKeepsBeingDeletedInResource(0);
                    	currClaimedGroup.setFirstTimeGroupBeingDeletedInResource(null);

                    	groupsToMerge.put(currClaimedGroup.getUniqueId().toUpperCase(),currClaimedGroup);
                    }
                    
                    
                    //Remove the group from the active list
                    activeGroups.remove(currClaimedGroup.getUniqueId().toUpperCase());
                } else {
                    // Claimed group was NOT found, result a DELETED group!
                    //07-02-07 -> Removal in a bulk! no more remove call per iteration, too slow!: tsgm.removeGroupEntity(currClaimedGroup);
                    groupsToRemove.put(currClaimedGroup.getUniqueId().toUpperCase(),currClaimedGroup);
                    
                    
                    //Remove the group from the active list
                    activeGroups.remove(currClaimedGroup.getUniqueId().toUpperCase());
                }
            }
            
            log.debug("Iterating over the left 'active groups' on resource: "
                + resource.getDisplayName()
                + ", left groups number to manage: "
                + activeGroups.size());
            
            for (ResourceGroup currActiveLeftGroup : activeGroups.values()) {
                //Persist the group
                //Set the Resource into the group (is not set by the importer!)
                currActiveLeftGroup.setResource(resource);
                
                groupsToPersist.put(currActiveLeftGroup.getUniqueId().toUpperCase(),currActiveLeftGroup);
            }
            
            //sometimes reconciliation takes long, then get a local manager here, otherwise sometimes got exceptions from hibernate that the connection is closed
            //TODO: Replace with remote calls if reconciliation may performed remotely
            InitialContext ic = new InitialContext();
            ResourceGroupManagerLocal tsgm = (ResourceGroupManagerLocal) ic.lookup("velo/ResourceGroupBean/local");
            
            log.debug("---------------GROUPS RECONCILIATION SUMMARY---------------");
            //Persist the 'groupsToPersist' SET.
            log.debug("Resulted *" + groupsToPersist.size() + "* groups to persist!, persisting...");
            tsgm.persistGroups(groupsToPersist.values());
            log.debug("Finished persisting *" + groupsToPersist.size() + "* groups to IDM repository!.");
            log.debug("Resulted *" + groupsToRemove.size() + "* groups to be removed!, removing...");
            tsgm.removeGroups(groupsToRemove.values());
            log.debug("Resulted * " + groupsToMerge.size() + "* groups to update, updating...");
            tsgm.mergeGroups(groupsToMerge.values());
            
            //rrs.groupsAmountInResource = groupsToPersist.size();
            //rrs.accountsAmountInResource
            
            
            buildSummaries(rrs, groupsToRemove, groupsToPersist);
            
            log.debug("Finished removing *" + groupsToRemove.size() + "* groups from IDM repository!");
        } catch (SyncListImporterException slie) {
            //Throw an exception
            throw new ReconcileGroupsException(
                "Failed to reconcile groups on target due to error importing the sync file, failed with message: "
                + slie.getMessage());
        } catch (NamingException ne) {
            throw new ReconcileGroupsException(ne.getMessage());
        }
    }
    
    private Map<String,ResourceGroup> getResourceGroupsAsMap(Resource resource) {
    	Map<String,ResourceGroup> groups = new HashMap<String,ResourceGroup>();
    	for (ResourceGroup rg : resource.getGroups()) {
    		groups.put(rg.getUniqueId().toUpperCase(), rg);
    	}
    	
    	
    	return groups;
    }
    
    
    
    
    
    private void buildSummaries(ResourceReconcileSummary rrs ,Map<String,ResourceGroup> groupsToRemove, Map<String,ResourceGroup> groupsToPersist) {
    	for (Map.Entry<String, ResourceGroup> currGrpToRemoveEntry : groupsToRemove.entrySet()) { 
    		SummaryGroup sg = new SummaryGroup();
    		sg.name = currGrpToRemoveEntry.getKey();
    		rrs.groupsRemovedFromRepository.add(sg);
    	}
    	
    	for (Map.Entry<String, ResourceGroup> currGrpToPersistEntry : groupsToPersist.entrySet()) { 
    		SummaryGroup sg = new SummaryGroup();
    		sg.name = currGrpToPersistEntry.getKey();
    		rrs.groupsInsertedToRepository.add(sg);
    	}
    }
}
