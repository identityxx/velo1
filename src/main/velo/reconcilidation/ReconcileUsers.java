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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.ReconcileUsersPolicy;
import velo.entity.Resource;
import velo.entity.User;
import velo.exceptions.ReconcileUsersException;
import velo.exceptions.SyncListImporterException;
import velo.reconcilidation.events.reconcileUsers.CreatedUserInSourceEvent;
import velo.reconcilidation.events.reconcileUsers.DeletedUserInSourceEvent;

/**
 * @author Asaf Shakarchi
 * 
 * A users reconciliation action. Reconcile Users based on the defined 'Users
 * Reconcile' policies.
 */
@Deprecated
public class ReconcileUsers {
	private static Logger logger = Logger.getLogger(ReconcileUsers.class
			.getName());

	/**
	 * Reconcile Users policy list Expecting a list of only Active policies
	 * ordered by priority!
	 */
	private List<ReconcileUsersPolicy> reconcileUsersPolicyList;

	/**
	 * Create an AccountTreeMappedList to hold all accounts fetched from the
	 * iterated resource (the set will be cleaned per target system iteration)
	 * Emulate an account entity for each fetched account by calling
	 * account.loadAccountByMap(map) method
	 */
	private HashMap<String, Account> activeAccounts = new HashMap<String, Account>();


	
	public ReconcileUsers(List<ReconcileUsersPolicy> reconcileUsersPolicyList) {
		this.reconcileUsersPolicyList = reconcileUsersPolicyList;
	}

	public boolean execute() throws ReconcileUsersException {
		// Start time indicator
		long startExecutionTime = System.currentTimeMillis();

		try {
			//TODO: Is reconciliation can be performed remotely? if so replace all ejb calls remotely
            Context ic = new InitialContext();
            AccountManagerLocal accountManager = (AccountManagerLocal) ic.lookup("velo/AccountBean/local");
            UserManagerLocal userManager = (UserManagerLocal) ic.lookup("velo/UserBean/local");
            ResourceManagerLocal resourceManager = (ResourceManagerLocal) ic.lookup("velo/ResourceBean/local"); 
            
            //JB!!! IdentityAttributeManagerRemote identityAttributeManager = (IdentityAttributeManagerRemote) ic.lookup("velo/IdentityAttributeBean/remote");

			// Create a map to hold Users to be removed.
			Map<String, User> usersToRemove = new HashMap<String, User>();

			// Load all active Identity Attributes
			// Collection<IdentityAttribute> activeIAs =
			// identityAttributeManager.findAllActiveIdentityAttributes();

			// Load users from repository
			logger.debug("Loading all users from repository, please wait...");
			// Find all users in repository
			Collection<User> userList = userManager.findAllUsers();

			
			// A Global MAP to hold all users to persist(MAP is required, XML may have two users with the same name)
			Map<String, User> usersToPersist = new HashMap<String, User>();

			// Iterate over reconcile users policies, expected them to be active
			// and ordered by priority
			// Per policy, perform the reconciliation.
			for (ReconcileUsersPolicy currRUP : reconcileUsersPolicyList) {
				logger.debug("Reconciling Users for User Reconcile Policy named: "
						+ currRUP.getDisplayName());

				// Build claimed accounts objects based on Target XML
				SyncTargetImporter syncImporter = new SyncTargetImporter(currRUP.getSourceResource().factorySyncFileName());
				SyncTargetData syncData = syncImporter.getSyncedData();
				Resource resourceSource = currRUP.getSourceResource();

				logger.info("Factored 'Active Users' list from resource XML which resulted: '" + syncData.getAccounts().size() + "' users");
				logger.info("Building virtual active accounts to be synchronized, please wait...");
				
				
				// Per active account(that was set by the SyncImporter),
				// generate an 'Account' entity loaded with the iterated
				// activeAccount's attributes

				for (Account currAA : syncData.getAccounts()) {
					Account activeAccount = new Account();
					activeAccount.setResource(resourceSource);
					
					
					//WHAT FOR? WHY TO FETCH HERE VIRTUAL ATTRIBUTES?!
					// JB!!! try { activeAccount =
					// am.loadTransientAttributes(activeAccount,currAA.getTransientAttributes()); }
					// catch (OperationException oe) { throw new
					// ActionFailureException(oe.toString()); }
					//
					// Might happen if there was a failure to load account
					// TODO Throw an exception! null is a mass!
					if (activeAccount == null) {
						continue;
					}

					// Add the loaded account to the 'ActiveAccount' map
					logger.trace("Adding loaded Active-User name: '" + activeAccount.getName() + "' to the map.");
					// TODO: Only support upper case accounts
					activeAccounts.put(activeAccount.getName().toUpperCase(),activeAccount);
				}

				
				
				
				logger
						.info("List if -Active Users- on resource name: '"
								+ resourceSource.getDisplayName()
								+ "', resulted *"
								+ activeAccounts.size()
								+ "* users.");

				logger.info("Loaded *" + userList.size()
						+ "* users in repository!");

				logger
						.info("Scanning Users in repository, comparing each user to the corresponding expected active account...");
				// Moved to MAP in order to have same way as usersToPersists
				// Set<User> usersToRemove = new HashSet<User>();
				for (User currUser : userList) {
					// TODO: Currently only support no case-sensitive users
					// reconcilliation
					if (activeAccounts.containsKey(currUser.getName().toUpperCase())) {
						// Confirmed. nothing to be done

						// Remove the account from active accounts
						activeAccounts.remove(currUser.getName().toUpperCase());
					} else {
						// Remove the account from active accounts
						activeAccounts.remove(currUser.getName().toUpperCase());

						// Expected Account does not exist meaning account
						// deleted in source, creating corresponding event
						DeletedUserInSourceEvent duise = new DeletedUserInSourceEvent();
						duise.setReconcileUsersPolicy(currRUP);
						duise.setUsersToRemove(usersToRemove);
						duise.setUser(currUser);
						duise.execute();
					}
				}

				logger
						.info("Iterating over the left active accounts on target system: "
								+ resourceSource.getDisplayName()
								+ ", left accounts number to manage: "
								+ activeAccounts.size());

				// Per left account, create the corresponding event
				for (Account currActiveAccount : activeAccounts.values()) {
					// New Account was found on source target, creating
					// corresponding event
					CreatedUserInSourceEvent cuise = new CreatedUserInSourceEvent();
					cuise.setReconcileUsersPolicy(currRUP);
					cuise.setUsersToPersist(usersToPersist);
					cuise.setActiveCreatedAccount(currActiveAccount);

					cuise.execute();
				}

			}

			// TODO: SYNC ATTRIBUTES!!!
			// Finished synchronizing users, now sync Identity Attributes
			// NOTE: This process assume that the process creation (I.e the
			// reconcile EJB) already prepared all active sync XML files of the
			// relevant Resources which the relevant IA's sources are based on.
			for (User currUser : userList) {
				// Make sure the user is not flagged to be removed, if so,
				// there's no reason to sync its attributes
				if (usersToRemove.containsKey(currUser.getUserId())) {
					continue;
				}

				/*
				 * //Per user, iterate over the active IA and try to synchronize
				 * each of them. for (IdentityAttribute currIA : activeIAs) {
				 * //Get its source //TODO Support more than one source! if
				 * (currIA.get)
				 */
			}

			// Persist data...
			logger.info("--------------- USERS RECONCILIATION SUMMARY ---------------");
			// Persist the 'accountsToPersist' SET.
			logger.info("Resulted *" + usersToPersist.size() + "* users to persist!, persisting...");
			userManager.persistUsers(usersToPersist.values());
			logger.info("Finished persisting *" + usersToPersist.size()+ "* users to IDM repository!.");
			logger.info("Resulted *" + usersToRemove.size()+ "* users to be removed!, removing...");
			userManager.removeUsers(usersToRemove.values());
			logger.info("Finished removing *" + usersToRemove.size() + "* users from IDM repository!");

			return true;
		} catch (SyncListImporterException e) {
			throw new ReconcileUsersException(e.toString());
		} catch (NamingException e) {
			throw new ReconcileUsersException(e.toString());
		}
	}

}
