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
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ReconcileGroupsException;
import velo.exceptions.SyncListImporterException;

/**
 * @author Asaf Shakarchi
 * 
 * A class that represents a Reconcile Group Membership process for a certain resource
 */
public class ReconcileGroupMembership {

	private static Logger logger = Logger.getLogger("velo.reconcilidation");

	private HashMap<String,ResourceGroup> activeGroups = new HashMap<String,ResourceGroup>();

	/**
	 * A resourceManager EJB object (Initialized by constructor)
	 */
	public ResourceManagerLocal tsm;

	/**
	 * An AccountManager EJB object (Initialized by constructor)
	 */
	public AccountManagerLocal am;
	
	private ResourceGroupManagerLocal tsgm;

	
	/**
	 * Constructor Mainly initalize the required EJBs objects into the class
	 */
	public ReconcileGroupMembership(AccountManagerLocal am, ResourceGroupManagerLocal tsgm) {
		this.am = am;
		this.tsgm = tsgm;
	}

	/**
	 * Reconcile accounts on the specified resource
	 * 
	 * @param resource
	 *            The resource entity to reconcile accounts for
	 * @throws ReconcileAccountsException
	 */
	public void reconcileGroupsByresource(Resource resource) throws ReconcileGroupsException {
		try {
			// --Fetch the list of accounts to reconcile from the last updated sync file
			
			SyncTargetImporter syncImporter = new SyncTargetImporter(resource.factorySyncFileName());
			SyncTargetData syncData = syncImporter.getSyncedData();
			
			logger.info("Factoring the 'ActiveGroup' list by the SyncImporter which resulted: '" + syncData.getGroups().size()+"' groups, constructing a MAP of virtual Groups entities");
			//Per active account(that was set by the SyncImporter), generate an 'Account' entity loaded with the iterated activeAccount's attributes
			for (ActiveGroup currGroup : syncData.getGroups()) {
				//Add the loaded account to the 'ActiveAccount' map
				//logger.info("Adding loaded Group name: '" + currGroup.getDisplayName() + "', with description: '" + currGroup.getDescription() + "' to the map.");
				if (activeGroups.containsKey(currGroup.getUniqueId())) {
					logger.warning("Skipping group with uniqueID: '" + currGroup.getUniqueId() + "', with displyName: '" + currGroup.getDisplayName() + "', group ID already found from previous groups!");
				}
				else {
					activeGroups.put(currGroup.getUniqueId(),currGroup);
				}
			}
			
			logger.info("Resulted a list of -Active Groups- on target system name: '"
								+ resource.getDisplayName()
								+ "', resulted *"
								+ activeGroups.size()
								+ "* groups.");


			// --End of account fetching
			logger.info("Dumping Constructed ActiveGroups SET...");
			for (Map.Entry<String,ResourceGroup> currGroup : activeGroups.entrySet()) {
				logger.fine("ActiveGroup unique id: " + currGroup.getKey());
			}

			
			
			// Refresh the collection before looking at it (Seems like toplink
			// does not refresh it automatically)
			// resource = (resource)tsm.refreshEntity(resource);

			// Get a collection of all accounts IDM claims to be available
			Set<ResourceGroup> idmClaimedGroupList = resource.getGroups();
			logger.info("E-dentity claims to have *"
					+ idmClaimedGroupList.size()
					+ "* groups for target system name: '"
					+ resource.getDisplayName() + "'");

			
			// Iterate through the claimed group list for current iterated resource
			for (ResourceGroup currClaimedGroup : idmClaimedGroupList) {
				// Check if the claimed group exist within the activeSet
				// NOTE: keys are case sensitive, account names comes all as
				// uppercase!
				if (activeGroups.containsKey(currClaimedGroup.getUniqueId())) {
					
					ResourceGroup currGroupToCompare = activeGroups.get(currClaimedGroup.getUniqueId());
					
					//Perform the group membership comparation!
					//logger.info("Found group with ID: '" + currGroupToCompare.getDisplayName() + "' to compare, -Active- group contains members with amount: '" + currGroupToCompare.getAccountNames().size() + ", while claimed contains members with amount: '" + currClaimedGroup.getAccounts().size() + "'");
				/*	for (String currActiveAccountName : currGroupToCompare.getAccountNames()) {
						
					}
					*/
					
					
					//Remove the group from the active list
					activeGroups.remove(currClaimedGroup.getUniqueId());
				} else {
					//The group was not found in Active groups although expected!
					
				}
			}

			logger.info("Iterating over the left 'active groups' on target system: "
							+ resource.getDisplayName()
							+ ", left groups number to manage: "
							+ activeGroups.size());
			
			for (ResourceGroup currActiveLeftGroup : activeGroups.values()) {
				//Eh, what shell we do with the left groups here? they do not exist in claim list, how's that possible? aint the 'Group Reconciliation' process should add them?
				logger.warning("Group ID: " + currActiveLeftGroup.getDisplayName() + " had to be synced by the 'Groups Reconciliation' process but didnt.");
			}
			
			logger.info("---------------GROUP MEMBERSHIP RECONCILIATION SUMMARY---------------");
			/*
			//Persist the 'groupsToPersist' SET.
			logger.info("Resulted *" + groupsToPersist.size() + "* groups to persist!, persisting...");
			tsgm.persistGroupEntities(groupsToPersist);
			logger.info("Finished persisting *" + groupsToPersist.size() + "* groups to IDM repository!.");
			logger.info("Resulted *" + groupsToRemove.size() + "* groups to be removed!, removing...");
			tsgm.removeGroupEntities(groupsToRemove);
			logger.info("Finished removing *" + groupsToRemove.size() + "* groups from IDM repository!");
			*/
			
		}
		catch (SyncListImporterException slie) {
			//Throw an exception
			throw new ReconcileGroupsException(
					"Failed to reconcile groups on target due to error importing the sync file, failed with message: " + slie.getMessage());
		}
	}
}
