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

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.ResourceList;
import velo.ejb.seam.UserHome;
import velo.ejb.seam.UserList;
import velo.entity.Account;
import velo.entity.IdentityAttributesGroup;
import velo.entity.Resource;
import velo.entity.Role;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserRole;
import velo.entity.UserJournalingEntry.UserJournalingActionType;
import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.ModifyAttributeFailureException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.OperationException;
import velo.storage.UserIdentityAttributeTreeMappedList;

@Scope(CONVERSATION)
@Stateful
@Name("userManageActions")
public class UserManageActionsBean implements UserManageActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@PersistenceContext
	//@In
	public EntityManager entityManager;

	// Inject the user, as it should be already in the conversation context set
	// by userHome
	//@In(value = "#{userHome.instance}")
	//User user;
	@In(value="#{userHome}") @Out
	UserHome userHome;
	
	@In
	User loggedUser;
	
	@In(value="#{userList}")
	UserList userList;
	
	@In(value="#{userHome.instance}")
	User user;

	@EJB
	public UserManagerLocal userManager;

	@EJB
	public AccountManagerLocal accountManager;
	
	@EJB
	public RoleManagerLocal roleManager;

	
	@In(create=true)
	ResourceList resourceList;
	
	
	Set<UserIdentityAttribute> modifiedUserIdentityAttributesByUpdateAction = new HashSet<UserIdentityAttribute>();
	List<Account> affectedAccountsByUserIdentityAttributeUpdateAction = new ArrayList<Account>();
	Set<IdentityAttributesGroup> userIdentityAttributesGroups = new LinkedHashSet<IdentityAttributesGroup>();
	// Keep a clone of all User Identity Attributes before modifications for
	// compare purposes
	// (We need such a special collection in order to fetch the
	// corresponding original userIdentityAttribute per the corresponding
	// attributes recieved from the view
	UserIdentityAttributeTreeMappedList orgUserAttrs = new UserIdentityAttributeTreeMappedList();
	
	private Set<Role> rolesToAddToUser;
	
	
	private String rofl;
	
	
	//manage acocunts associations manually
	private List<Account> manuallyAccountsAssociations;
	
	public void prepareCreateUser() {
		if (userIdentityAttributesGroups == null) {
			userIdentityAttributesGroups = new LinkedHashSet<IdentityAttributesGroup>();
		}
		log.trace("Invoked Create User method...");
		userIdentityAttributesGroups.clear();
		
		Query groupsQuery = entityManager.createNamedQuery("identityAttributesGroup.findAllVisible");
		userIdentityAttributesGroups.addAll(groupsQuery.getResultList());
		userIdentityAttributesGroups = user.getIdentityAttributesGroupsForUser(userIdentityAttributesGroups);
		
		
		/*HOLY SHIT, DATA DISAPPEAR WHEN USING SEAM'S MANAGED PERSISTENCE CONTEXT ANNOTATED WITH @IN!
		 * DATA DISAPPEAR WHEN CREATEUSER() is being invoked!
		Query groupsQuery = entityManager.createNamedQuery("identityAttributesGroup.findAllVisible");
		userIdentityAttributesGroups.addAll(groupsQuery.getResultList());
		log.trace("LOADED FUCKEN GROUPS: " + userIdentityAttributesGroups.size());
		*/
	}
	
	public String createUser() {
		Set<UserIdentityAttribute> uiaList = new HashSet<UserIdentityAttribute>();
		
		for (IdentityAttributesGroup currIAG : userIdentityAttributesGroups) {
			log.debug("Iterate over all UIA Groups recieved from the VIEW, for user name #0 ",user.getName());
			
			for (UserIdentityAttribute currUIA : currIAG.getUserIdentityAttributes()) {
				log.trace("Current UIA group: '#0',UIA name '#1', first value: '#2'",currIAG.getName(),currUIA.getDisplayName(),currUIA.getFirstValueAsString());
				uiaList.add(currUIA);
			}
		}
		
		try {
			userHome.getInstance().load(uiaList, userHome.getInstance().getName());
		} catch (ObjectFactoryException e) {
			facesMessages.add("Could not create user: " + e.getMessage());
			return null;
		}
		
		userHome.getInstance().addJournalingEntry(UserJournalingActionType.CREATED, loggedUser, "User is created manually", "");
		
		if (!userManager.isUserExit(userHome.getInstance().getName())) {
			return userHome.persist();
		} else {
			facesMessages.add("User name '#0' already exist in repository!", userHome.getInstance().getName());
			return null;
		}
	}
	
	@Begin(join=true)
	public void manageUser() {
		log.trace("Invoked Manage User method...");
		userIdentityAttributesGroups.clear();
		
		
		if (log.isTraceEnabled()) {
			log.trace("Dumping User Identity Attributes for User '#0'",userHome.getInstance().getName());
			for (UserIdentityAttribute currUIA : userHome.getInstance().getUserIdentityAttributes()) {
				log.trace("Identity Attribute '#0', with values amount '#1'", currUIA.getDisplayName(),currUIA.getValues().size());
			}
		}
		
		//important no? userHome.getEntityManager().refresh(userHome.getInstance());
		
		Query groupsQuery = entityManager.createNamedQuery("identityAttributesGroup.findAllVisible");
		List<IdentityAttributesGroup> rr = groupsQuery.getResultList();
		for (IdentityAttributesGroup currG : rr) {
			currG.getUserIdentityAttributes().clear();
		}
		
		
		//Otherwise user stays managed and attribute modifications get updated before approval
		userHome.getInstance().getUserRoles().size();
		userHome.getInstance().getAccounts().size();
		userHome.getInstance().getCapabilities().size();
		userHome.getInstance().getCapabilityFolders().size();
		//userHome.getEntityManager().clear();

		
		log.debug("Load all groups, needed for display");
		userIdentityAttributesGroups.addAll(groupsQuery.getResultList());

		if (log.isTraceEnabled()) {
			for (IdentityAttributesGroup currGroup : userIdentityAttributesGroups) {
				log.trace("group name #0, with priority: #1, with amount of attrs #2",currGroup.getName(),currGroup.getDisplayPriority(),currGroup.getIdentityAttributes().size());
				log.trace("Identity Attributes in current group: " + currGroup.getUserIdentityAttributes());
			}
		}
		
		log.debug("Found #0 groups that are visible, loading user attributes per group.", userIdentityAttributesGroups.size());
		
		orgUserAttrs = new UserIdentityAttributeTreeMappedList();

		// Load the User entity with all the attributes per group
		userIdentityAttributesGroups = user.getIdentityAttributesGroupsForUser(userIdentityAttributesGroups);

		// Clone the current values of all attributes so they can be compared
		// after modification
		for (IdentityAttributesGroup currIAG : userIdentityAttributesGroups) {
			for (UserIdentityAttribute currUIA : currIAG.getUserIdentityAttributes()) {
				orgUserAttrs.mapObject(currUIA.clone());
			}
		}
		
		log.trace("END of User Manage!");
	}

	//public String viewModifiedUserIdentityAttributes() throws VeloViewException {
	//@Begin(nested=true)
	public String viewModifiedUserIdentityAttributes() {
		for (IdentityAttributesGroup currIAG : userIdentityAttributesGroups) {
			log.debug("Iterate over all UIA Groups recieved from the VIEW, for user name #0 ",user.getName());
			for (UserIdentityAttribute currUIA : currIAG.getUserIdentityAttributes()) {
				// Get the corresponding original attribute
				UserIdentityAttribute currOrigUIA = orgUserAttrs.getByName(currUIA.getIdentityAttribute().getUniqueName());

				
				//if the new value is empty string then skip the attribute
				try {
					if (currUIA.getFirstValue().isNull()) {
						continue;
					}
				}catch (NoResultFoundException e) {
					continue;
				}

				
				//TODO: JSF Inputs should perform validation for REGEXP + Scriptable Validation
				
				// Compare attributes values:
				// If values are equal then do nothing
				//note: always compare userInput's attr -> original as original might be null if there was no UIA instance when 'this.manage()' was invoked., thus compareValues will throw a NPE exception
				if (currUIA.compareValues(currOrigUIA)) {
						// nothing to do, values are equal
						log.trace("Values of IA '#0' for user '#1' are equal.", currUIA.getDisplayName(), user.getName());
				}
				// If values are not equal then add the affected user
				// identity attribute to the collection
				else {
					log.debug("A UserIdentityAttribute name: #0 was modified!", currUIA.getIdentityAttribute().getUniqueName());

					// Set the old values into the attribute (mostly used to
					// display the old value)
					currUIA.setOldValues(currOrigUIA.getValues());

					// Duplicates may happen if the UIA was attached to more
					// than one IAG
					if (!modifiedUserIdentityAttributesByUpdateAction.contains(currUIA)) {
						modifiedUserIdentityAttributesByUpdateAction.add(currUIA);

						//TODO: Not implemented yet, 'userm.findAccountsByUserIdentityAttribute' should be implemented first
						//affectedAccountsByUserIdentityAttributeUpdateAction.addAll(userManager.findAccountsByUserIdentityAttribute(currUIA));
					}
				}
			}
		}
		
		if (modifiedUserIdentityAttributesByUpdateAction.size() < 1) {
            facesMessages.add("No Attributes were modified.");
            return null;
        }
		else {
			entityManager.clear();
			return "/admin/UserShowModifiedAttributes.xhtml";
		}
	}

	
	
	
	
	
	
	
	
	
	
	/*
	@Deprecated
	public String OLDviewModifiedUserIdentityAttributes() {
		for (IdentityAttributesGroup currIAG : userIdentityAttributesGroups) {
			log.debug("Iterate over all UIA Groups recieved from the VIEW, for user name #0 ",user.getName());
			for (UserIdentityAttribute currUIA : currIAG.getUserIdentityAttributes()) {
				// Get the corresponding original attribute
				UserIdentityAttribute currOrigUIA = orgUserAttrs.getByName(currUIA.getIdentityAttribute().getUniqueName());

//				log.info("!!!!!!ORG" + currOrigUIA);
//				log.info("!!!!!!ORG" + currOrigUIA.getDisplayName());
//				log.info("!!!!!!ORG" + currOrigUIA.getIdentityAttribute().getUniqueName());
//				
//				log.info("!!!!!!MODIFIED" + currUIA);
//				log.info("!!!!!!MODIFIED" + currUIA.getDisplayName());
//				log.info("!!!!!!MODIFIED" + currUIA.getIdentityAttribute().getUniqueName());
				
				
				
				
				// TODO: Move validators to JSF level, so it can be handled
				// nicely per attribute in the view.
				// ModifyAttributes will perform another validation, but if
				// validation fails, we'd like to show an error on current page
				// So lets validate again
				try {
					currOrigUIA.getIdentityAttribute()
							.validateByUserIdentityAttributeValues(
									currUIA.getValues());
				} catch (AttributeValidationException ave) {
					String msg = "Error occured while trying to validate attribute: '"
						+ currUIA.getDisplayName()
						+ "' due to: '"
						+ ave.getMessage() + "'";
					facesMessages.add(FacesMessage.SEVERITY_WARN, msg);
					//throw new VeloViewException(msg);
					return null;
				}

				// Update the values by script if required, again
				// 'modifyAttributes' does this, but it is required for the next
				// GUI page
				// To show attribute values differences...
				if (currUIA.getIdentityAttribute().isHasScriptableValidator()) {
					try {
						currUIA.modifyValuesByScriptedValidator(currUIA);
					} catch (AttributeValidationException ave) {
						String msg = "Could not modify values by the Scripted validator due to: '"
								+ ave.getMessage() + "'";
						FacesMessages.instance().add(
								FacesMessage.SEVERITY_WARN, msg);
						//throw new VeloViewException(msg);
						return null;
					}
				}

				if (currOrigUIA == null) {
					// TODO: This should -NEVER- happen, better that 'getByName'
					// will throw a null exception instead of this IF.
					// TODO: replace with null exception or something better..
					log.error("Warning! this should NEVER happen");
					continue;
				} else {
					// Compare attributes values:
					// If values are equal then do nothing
					if (currOrigUIA.compareValues(currUIA)) {
						// nothing to do, values are equal
					}
					// If values are not equal then add the affected user
					// identity attribute to the collection
					else {
						log.debug("A UserIdentityAttribute name: #0 was modified!",
								currUIA.getIdentityAttribute().getUniqueName());

						// Set the old values into the attribute (mostly used to
						// display the old value)
						currUIA.setOldValues(currOrigUIA.getValues());

						// Duplicates may happen if the UIA was attached to more
						// than one IAG
						if (!modifiedUserIdentityAttributesByUpdateAction.contains(currUIA)) {
							modifiedUserIdentityAttributesByUpdateAction.add(currUIA);

							// For each UserIdentityAttribute that needs to get
							// updated, fetch its corresponding account list
							// that should get affected and add it to the
							// 'effected accounts' collection
							affectedAccountsByUserIdentityAttributeUpdateAction.addAll(userManager
								.findAccountsByUserIdentityAttribute(currUIA));
						}
					}
				}
			}
		}
		
		if (modifiedUserIdentityAttributesByUpdateAction.size() < 1) {
            facesMessages.add("No Attributes were modified.");
            return null;
        }
		else {
			return "/admin/UserShowModifiedAttributes.xhtml";
		}
	}
	*/

	
	public String updateModifiedUserAttributes() {
        if (modifiedUserIdentityAttributesByUpdateAction.size() > 0) {
            log.debug("Updating user identity attributes with amount #0, for user: #1", modifiedUserIdentityAttributesByUpdateAction.size(),user.getName());

            
            // Perform the update
            try {
                userManager.modifyUserAttributes(modifiedUserIdentityAttributesByUpdateAction);
            } catch (ModifyAttributeFailureException mafe) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,mafe.toString());
                return null;
            }

            // Clean the affected arrays since the user still managed...
            modifiedUserIdentityAttributesByUpdateAction.clear();
            
    		//recal 'this.manage()' as old userAttrs in groups must get refreshed

            
            //update user journaling with the change
            userHome.getInstance().addJournalingEntry(UserJournalingActionType.MODIFIED_ATTRIBUTES, loggedUser, "User attributes were modified.", "");
            
            facesMessages.add("Successfully updated all affected User Identity Attributes");
        }

        entityManager.flush();
        
        
        //reload the user as it's not managed anymore by the EM
        userHome.setInstance(userHome.getEntityManager().merge(userHome.getInstance()));
        
        
        //refresh the user
		//userHome.getEntityManager().refresh(user);
		
		//25-nov-07 doubles the attrs! whatt the fuck? aint UserManager.xhtml invoke this?!
		//manageUser();
		
		
		return "/admin/UserManage.xhtml?userId=" + user.getUserId();
        //return "/admin/UserList.xhtml";
	}
	
	public void disableUser() {
		try {
			userManager.disableUser(user);
			facesMessages
					.add("Successfully requested Disable User operation for user: "
							+ user.getName());
		} catch (OperationException oe) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to request Disable User operation for user '"
							+ user.getName() + "': " + oe.toString());
		}
	}

	public void enableUser() {
		try {
			userManager.enableUser(user);
			facesMessages
					.add("Successfully requested Enable User operation for user: "
							+ user.getName());
		} catch (OperationException oe) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to request Enable User operation for user '"
							+ user.getName() + "': " + oe.toString());
		}
	}

	public void refreshUserAccounts() {
		if (!accountManager.updateAccountsStatus(user.getAccounts())) {
			facesMessages
					.add(
							FacesMessage.SEVERITY_ERROR,
							"Failed to refresh accounts status: please read event log for more information.");
		} else {
			facesMessages.add("Successfully refreshed accounts status");
		}
	}
	
	//@End
	public String performUserRolesModifications() {
		User user = userHome.getInstance();

		
		//Make sure that the lists are not null
		if (user.getUserRolesToRevoke() == null) {
			user.setUserRolesToRevoke(new HashSet<UserRole>());
		}
		if (user.getRolesToAssign() == null) {
			user.setRolesToAssign(new HashSet<Role>());
		}

		if ( (user.getRolesToAssign().size() < 1) && (user.getUserRolesToRevoke().size() < 1) ) {
            facesMessages.add("Cannnot perform update, no changes were made.");
            return null;
        } else {
        	log.info("User Roles modiifcation process has just started...");
            log.info("Adding '" + user.getRolesToAssign().size() +
            	"' roles to add, and '" +
            	user.getUserRolesToRevoke().size() + "' to revoke...");
           
            //journaling change
//            userHome.getInstance().addJournalingEntry(UserJournalingActionType.MODIFIED_ROLES, loggedUser, "User roles were modified.", "");

            
            try {
            	Set<Role> rolesToRevoke = new HashSet<Role>();
            	for (UserRole currUserRole : user.getUserRolesToRevoke()) {
            		rolesToRevoke.add(currUserRole.getRole());
            	}
            	
            	
            	//roleManager.modifyRolesOfUser(rolesToRevoke,user.getRolesToAssign(), user);
            	roleManager.modifyDirectUserRoles(rolesToRevoke, user.getRolesToAssign(), user);
            } catch (OperationException ex) {
                log.warn(ex.toString());
                facesMessages.add(FacesMessage.SEVERITY_WARN,"Failed to modify User Roles for user '#{user.name}' due to: " + ex.toString());
                return null;
            }
        }
		

		
		//TODO: Although it is expected that @END will destroy the nested context, data still not gets cleaned
		user.getRolesToAssign().clear();
		user.getUserRolesToRevoke().clear();
		
		
		facesMessages.add("Successfully requested roles modifications for #0", user.getName());
		
		//refresh the user
		//for some reason, started to create an error
		//13:01:59,140 ERROR [AssertionFailure] an assertion failure occured (this may indicate a bug in Hibernate, but is more li
		//kely due to unsafe use of the session)
		//org.hibernate.AssertionFailure: null identifier
		//userHome.getEntityManager().refresh(user);
		System.out.println("System is preparing to refresh the user");
		userHome.refresh();
		
		//reload instead :-/
		//userHome.setId(user.getUserId());
		//userHome.setInstance(null);
		//userHome.getInstance();
		
		
		
		return "/admin/UserManage.xhtml?userId=" + user.getUserId();
		//manageUser();
	}
	
	
	//User role modification methods
	/*Remove
	public void addRoleToAssignList(Role role) {
		user.addRoleToAssign(role);
	}
	*/

	// Accessors
	public Set<IdentityAttributesGroup> getUserIdentityAttributesGroups() {
		return userIdentityAttributesGroups;
	}
	
	public Set<UserIdentityAttribute> getModifiedUserIdentityAttributesByUpdateAction() {
		return modifiedUserIdentityAttributesByUpdateAction;
	}


	//User roles modifications
	public void addUserRoleToRevoke(UserRole userRole) {
		user.addUserRoleToRevoke(userRole);
	}
	
	public void removeUserRoleToRevoke(UserRole userRole) {
		user.removeUserRoleToRevoke(userRole);
	}
	
	public void addRoleToAssign(Role role) {
		try {
			user.addRoleToAssign(role);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}

	public void removeRoleToAssign(Role role) {
		user.removeRoleToAssign(role);
	}
	
	
	
	
	/**
	 * @return the manuallyAccountsAssociations
	 */
	public List<Account> getManuallyAccountsAssociations() {
		log.trace("Building manually assinged accounts...");
		if (manuallyAccountsAssociations == null) {
			log.trace("Constructing...");
			manuallyAccountsAssociations = new ArrayList<Account>();
			
			//cannot this way coz of @postContruct ResourceList resourceList = new ResourceList();
			resourceList.getResource().setActive(true);
			
			List<Resource> resources = resourceList.getResultList();
			log.trace("Resulted active resources with amount '" + resources.size() + "'");
			for (Resource currRes : resources) {
				//check whether user has an account on this resource
				Account acc = user.getAccountOnTarget(currRes.getUniqueName());
				if (acc != null) {
					manuallyAccountsAssociations.add(acc.clone());
				} else {
					Account newDummyAcc = new Account();
					newDummyAcc.setResource(currRes);
					manuallyAccountsAssociations.add(newDummyAcc);
				}
			}
		}
		

		return manuallyAccountsAssociations;
	}

	/**
	 * @param manuallyAccountsAssociations the manuallyAccountsAssociations to set
	 */
	public void setManuallyAccountsAssociations(
			List<Account> manuallyAccountsAssociations) {
		this.manuallyAccountsAssociations = manuallyAccountsAssociations;
	}
	
	public void modifyManuallyAccountsAssociations() {
		for (Account currDummyAcc : getManuallyAccountsAssociations()) {
			
					
			//load the corresponding account from DB
			//(if its a new account assoc then validator already checked the account is not associated to any user)
			//(if the account is an old assoication, then nothing should occur, only a unneccessary update call
			accountManager.setEntityManager(entityManager);
			Account loadedAccount = accountManager.findAccount(currDummyAcc.getName(), currDummyAcc.getResource().getUniqueName());
			
			//System.out.println("Loaded account : " + loadedAccount.toString() + ", named "+ loadedAccount.getName() );
			
			Account accountAssocToUser = user.getAccountOnTarget(currDummyAcc.getResource().getUniqueName());
			
			//if no account name was specified, then check whether the user had an account on the current iterated resource
			//if so clean the assoication as it got cleaned.
			
			if (currDummyAcc.getName().length() < 1) {
				//account is already assoc to user, clean the user from the loaded account 
				if (accountAssocToUser != null) {
					accountAssocToUser.setUser(null);
					entityManager.merge(accountAssocToUser);
				}
				continue;
			}
			
			
			//there's a value, lets check if the loaded acc is equal to the original user assoc
			
			//was there an account associated before with different value?
			//no, then just associate the loaded account to the user
			if (accountAssocToUser == null) {
				
			} else {
				//yes there was, if values are different, then remove the assoc from the old account!
				if (!loadedAccount.equals(accountAssocToUser)) {
					accountAssocToUser.setUser(null);
					entityManager.merge(accountAssocToUser);
				}
			}
		
			//the new acc should be assoc to user
			
			loadedAccount.setUser(user);
			entityManager.merge(loadedAccount);
			
			}
			//facesMessages.add("Successfully modified accounts associations");
		
		
	}
	public AccountManagerLocal getAccountManager() {
		accountManager.setEntityManager(entityManager);
		return accountManager;
	}
	
	
	
	
	@Destroy
	@Remove
	public void destroy() {
	}

	
}
