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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.actions.ActionManager;
import velo.actions.ResourceActionInterface;
import velo.actions.factory.DeleteAccountActionFactory;
import velo.common.EdmMessages;
import velo.common.SysConf;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.RoleManagerRemote;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.BulkTask;
import velo.entity.EventDefinition;
import velo.entity.Position;
import velo.entity.PositionRole;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.Role;
import velo.entity.RolesFolder;
import velo.entity.SpmlTask;
import velo.entity.Task;
import velo.entity.User;
import velo.entity.UserRole;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.AssigningRoleToUserException;
import velo.exceptions.FactoryResourceActionsForRoleException;
import velo.exceptions.LoadingObjectsException;
import velo.exceptions.MergeEntityException;
import velo.exceptions.ModifyResourceGroupsInRoleException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.OperationException;
import velo.exceptions.PersistEntityException;
import velo.exceptions.ScriptInvocationException;
import velo.exceptions.TaskCreationException;
import velo.exceptions.ValidationException;
import velo.scripting.GenericTools;

/**
A Stateless EJB bean for managing Roles
@author Asaf Shakarchi
 */
@Stateless
public class RoleBean implements RoleManagerLocal, RoleManagerRemote {

    private static Logger log = Logger.getLogger(RoleBean.class.getName());
    private static final String EVENT_PRE_MODIFY_USER_ROLES = "MODIFY_USER_ROLES_ASSIGNMENT";

    /**
    Injected entity manager
     */
    //@PersistenceContext
    @PersistenceContext
    public EntityManager em;

    /**
    Inject a local UserManager EJB
     */
    @EJB
    public UserManagerLocal um;

    @EJB
    public ResourceManagerLocal tsm;

    @EJB
    public ResourceGroupManagerLocal tsgm;

    @EJB
    public AccountManagerLocal am;

    @EJB
    CommonUtilsManagerLocal cum;
    
    @EJB
	EventManagerLocal eventManager;

    @EJB
    TaskManagerLocal tm;
    
    @EJB
    ResourceOperationsManagerLocal resourceOperationManager;

    @javax.annotation.Resource
    TimerService timerService;

    @javax.annotation.Resource
    private SessionContext sessionContext;

    //@EJB
    //;;	public ResourceAttributeManager tsam;

    
    public void createRole(Role role) throws PersistEntityException {
        try {
            //Validate role before persisting
            role.validateRoleEntity();
            em.persist(role);
        } catch (ValidationException ve) {
            throw new PersistEntityException("Role name: '" + role.getName() + "' validation process was failed with message: " + ve.getMessage());
        }
    }
    
    public Role findRole(String name) {
    	log.debug("Looking for  Role in repository with name '" + name + "'");
    
		try {
			Query q = em.createNamedQuery("role.findByName").setParameter("name",name);
			return (Role) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("FindRole method did not result any role for name '" + name + "', returning null.");
			return null;
		}
		catch (Exception ex){
			log.debug("The generic exception was caught : " + ex.toString());
			return null;
		}
		
    }
    
    
    public void removeUserRoleEntity(UserRole userRole) {
    	log.info("Removing UserRole entity of user: " + userRole.getUser().getName() + ", of role: " + userRole.getRole().getName());
        em.remove(userRole);
    }
    
    
    @Deprecated
    public long modifyRolesOfUser(Set<Role> userRolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException {
        //public long modifyRolesOfUser(Set<UserRole> userRolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException {
    	//BulkTask bt = modifyRolesOfUserTasks(userRolesToRemove, rolesToAdd, user, true);
    	BulkTask bt = modifyRolesOfUserTasks(userRolesToRemove, rolesToAdd, user);

    	if (bt.getTasks().size() > 0) {
    		return tm.persistBulkTask(bt);
    	} else {
    		return 0;
    	}
    }

    @Deprecated
    public void associateRoleToUser(Role role, User user) throws OperationException {
    	Set<Role> rolesToAdd = new HashSet<Role>();
    	rolesToAdd.add(role);
    	//modifyRolesOfUser(new HashSet<UserRole>(), rolesToAdd, user);
    	modifyRolesOfUser(new HashSet<Role>(), rolesToAdd, user);
    }
        
    
    public BulkTask modifyDirectUserRoles(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException {
    	return modifyDirectUserRoles(rolesToRemove, rolesToAdd, user, null,true);
    }
    
    public BulkTask modifyDirectUserRoles(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user, Date tasksExpectedExecutionDate, boolean persistBulkTask) throws OperationException {
    	Long id;
    	BulkTask bt = modifyRolesOfUserTasks(rolesToRemove, rolesToAdd, user);
    	
    	if (persistBulkTask) {
    		if (bt.getTasks().size() > 0) {
    			id = tm.persistBulkTask(bt);
    		} else {
    			id = new Long(0);
    		}
    	}
    	
    	if (tasksExpectedExecutionDate != null) {
    		for (Task task : bt.getTasks()) {
    			task.setExpectedExecutionDate(tasksExpectedExecutionDate);
    		}
    	}
    	
    	//all were good, lets associate the entities
    	//bt->tasks were persisted, assoc/remove assoc entities
		//WTF?!?! getRolesToAssign?!?! who's calling this method?!?!
    	//for (Role currRoleToAdd : user.getRolesToAssign()) {
    	for (Role currRoleToAdd : rolesToAdd) {
    		if (!user.isRoleAssociatedToUser(currRoleToAdd)) {
    			UserRole userRole = new UserRole();
    			userRole.setCreationDate(new Date());
    			userRole.setRole(currRoleToAdd);
    			userRole.setUser(user);
    			userRole.setExpirationDate(currRoleToAdd.getExpirationDate());
    			userRole.setInherited(currRoleToAdd.isInherited());
    			userRole.setUser(user);
            
    			em.persist(userRole);
    		} else {
    			log.debug("Role to add '" + currRoleToAdd.getName() + "' is already associated to user '" + user.getName() + "', skipping user->role association");
    		}
		}
    	
    	//FIXME: Why user getUserRolesToRevoke instead of the 'rolesToRemove' param specified in this method signature?
		//revoke roles
		for (UserRole currUserRoleToRemove : user.getUserRolesToRevoke()) {
            boolean foundRoleInRolesToAdd = false;
            //If role was flagged to be added, then skip it.
            for (Role currRoleToAdd : user.getRolesToAssign()) {
                if (currUserRoleToRemove.getRole().getName().equals(currRoleToAdd.getName())) {
                    foundRoleInRolesToAdd = true;
                    break;
                }
            }

            if (!foundRoleInRolesToAdd) {
            	user.removeUserRole(currUserRoleToRemove);
            	em.remove(em.find(UserRole.class, currUserRoleToRemove.getUserRoleId()));
            	//what for? em.flush();
            }
		}
		for (Role currRoleToRemove : rolesToRemove) {
			/*
            boolean foundRoleInRolesToAdd = false;
            //If role was flagged to be added, then skip it.
            for (Role currRoleToAdd : user.getRolesToAssign()) {
                if (currUserRoleToRemove.getRole().getName().equals(currRoleToAdd.getName())) {
                    foundRoleInRolesToAdd = true;
                    break;
                }
            }

            if (!foundRoleInRolesToAdd) {
            	user.removeUserRole(currUserRoleToRemove);
            	em.remove(em.find(UserRole.class, currUserRoleToRemove.getUserRoleId()));
            	//what for? em.flush();
            }
            */
			
			UserRole currURToDelete = user.getUserRole(currRoleToRemove.getName());
			if (currURToDelete != null) {
				user.getUserRoles().remove(currURToDelete);
				em.remove(em.find(UserRole.class, currURToDelete.getUserRoleId()));
			}
		}
		
		
		return bt;
    }
    
    
    /**
    - Perform roles modification for a certain user
     */
    //public BulkTask modifyRolesOfUserTasks(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user, boolean modifyEntities) throws OperationException {
    public BulkTask modifyRolesOfUserTasksAA(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException {
    	return null;
    	
    	/*
		try {
			Query q = em.createNamedQuery("role.findByName").setParameter("name","ROLE-0");
			
			//return (Role) q.getSingleResult();
			
			log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + q.getSingleResult());
			return null;
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("FindRole method did not result any role for name '" + "AA" + "', returning null.");
			return null;
		}
		catch (Exception ex){
			log.debug("The generic exception was caught : " + ex.toString());
			return null;
		}
		*/
		
		/*
    	log.info("!!!!!!!!!!!!!!!!! LOG (A)");
    	if (!em.isOpen()) {
    		log.info("!!!!!!!!!!!!!!!!!!!!!! EM IS CLOSED!!!!!!");
    	} else {
    		log.info("!!!!!!!!!!!!!!!!!!!!!! EM IS OPEN!!!!!!");
    	}
    	user = em.find(User.class, user.getUserId());
    	log.info("!!!!!!!!!!!!!!!!! LOG (B)");
    	BulkTask bulkTask = BulkTask.factory("User roles modification for User '" + user.getName() + "'");
    	
    	return bulkTask;
    	*/
    }
    
    
    //The working one :)
    public BulkTask modifyRolesOfUserTasks(Set<Role> rolesToRemove, Set<Role> rolesToAdd, User user) throws OperationException {
    	//trigger the event, let it have a chance to modify anything related to access...
    	try {
			invokePreModifyUserRolesEvent(user, rolesToAdd, rolesToRemove);
		} catch (ScriptInvocationException e) {
			log.error(e.toString());
		}
    	
    	//This is critical, the user is obviously not in managed state, when invoked user.getUserRoles.add(role) it got persisted
    	//when invoked user.getUserRoles().remove(userRole) it didn't... not sure how Hibernate acts here but reloading the user and having it in managed state should hopefully solve these problems
        log.debug("Modifying User Roles action has started...");
        log.debug("Removing '" + rolesToRemove.size() + "' roles, adding '" + rolesToAdd.size() + "' roles to user name: '" + user.getName() + "'");
        //log.debug("User has UserRoles in repository with amount: '" + user.getUserRoles().size() + "'");
        
        //boolean isDeleteAccountIfRoleHasLastResourceReference = SysConf.getSysConf().getBoolean("roles.last_user_role_revoke_with_reference_to_resource_deletes_resource_account");
        //boolean isDeleteAccountIfRoleHasLastResourceReference = true;
        //log.trace("Delete account if role has last resource ref?: " + isDeleteAccountIfRoleHasLastResourceReference);
        boolean isDeleteAccountIfRoleHasLastResourceReference = true;
        
        //Make sure we have the required properties set
        BulkTask bulkTask = BulkTask.factory("User roles modification for User '" + user.getName() + "'");
        EdmMessages ems = new EdmMessages();
        
        
        //A 'cannot open connection' / ' Transaction is not active' errors occur while accessing user object
        //then we reload the user from DB with current EM.
        //user = em.find(User.class, user.getUserId());

        
//PREPARE DATA CONTAINERS FOR ROLES/GROUPS/ETC INSERTIONS.        
        //Create a GLOBAL groups map containing all groups to add from all resources
        Map<Long, ResourceGroup> allGroupsToAddGlobally = new HashMap<Long, ResourceGroup>();
        //Create a GLOBAL resources map to add
        Map<Long, Resource> allResourcesToAdd = new HashMap<Long, Resource>();

        log.debug("Iterating over all ROLES TO ASSOCIATE, preparing 'Resource Groups / Resourcess' TO ADD to global lists");
        for (Role currRoleToAdd : rolesToAdd) {
            //Make sure that the user does not have this role already, if so, skip the role association.
            //if (um.isRoleAssignedToUser(user, currRoleToAdd)) {
        	log.trace("Checking whether role to add with name '" + currRoleToAdd.getName() + "' is already associated to user...");
        	if (user.isRoleAssociatedToUser(currRoleToAdd)) {
                String msg = "Could not assign role name: '" + currRoleToAdd.getName() + "', to user: '" + user.getName() + "' since this orle is already associated to user.";
                //Log the message to the event log
                //cum.addEventLog("ROLES","FAILURE","WARNING","Failed to assign role to user",msg);
                log.warn(msg);
                ems.warning(msg);
                continue;
            }

        	
            //If the role to add was specified in the remove list, then skip the role insertion.
        	//same should be happen regarding the revoke roles list.
        	log.trace("Checking whether role to add with name '" + currRoleToAdd.getName() + "' is flagged to be removed...");
            for (Role userRoleToRemove : rolesToRemove) {
                if (userRoleToRemove.getName().toUpperCase().equals(currRoleToAdd.getName().toUpperCase())) {
                	log.debug("Skipping role association for role named '" + currRoleToAdd.getName() + "' since this role was also requested to be revoked!");
                    continue;
                }
            }

            String shortMsg = "Adding role '" + currRoleToAdd.getName() + "'";
            ems.info(shortMsg);

            
            
            //Collect all 'resources to add' into a global array
            log.debug("Iterating over resources associated with role name: '" + currRoleToAdd.getName() + "', with resources amount: '" + currRoleToAdd.getResources().size() + "', adding all resources to the 'global Resource TO ADD map'.");
            for (Resource currResource : currRoleToAdd.getResources()) {
                if (!allResourcesToAdd.containsKey(currResource.getResourceId())) {
                    log.debug("Adding resource named '" + currResource.getDisplayName() + "' to 'global resource to add' map");
                    allResourcesToAdd.put(currResource.getResourceId(), currResource);
                } else {
                    log.debug("Resource name: '" + currResource.getDisplayName() + "' already flagged to be added...");
                }
            }

            
            //Collect 'all groups to add' in a global array
            log.debug("Iterating over resource groups associated with role name: '" + currRoleToAdd.getName() + "', with resource groups amount: '" + currRoleToAdd.getResourceGroups().size() + "', adding all groups to the 'Global Resource Groups TO ADD map'.");
            for (ResourceGroup currRG : currRoleToAdd.getResourceGroups()) {
                if (!allGroupsToAddGlobally.containsKey(currRG.getResourceGroupId())) {
                    log.debug("Adding group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' to 'global groups to add' map");
                    allGroupsToAddGlobally.put(currRG.getResourceGroupId(), currRG);
                } else {
                    log.debug("Group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' already flagged to be added...");
                }
            }
            
        }
        

      //Create a map in map of groups to add per resource
      //map structure: Map<String=resource unique name,MAP<Long=resourceGroupId,ResourceGroup>>
      Map<String, Map<Long,ResourceGroup>> allGroupsToAddPerResource = new HashMap<String, Map<Long,ResourceGroup>>();

      //fill the map with elements...
      for (Map.Entry<Long,ResourceGroup> currResourceGroupEntry : allGroupsToAddGlobally.entrySet()) {
    	  Map<Long,ResourceGroup> groupsToAddPerResource = null;
    	  //Make sure there is a map already for the current group's resource, otherwise create one
    	  if (allGroupsToAddPerResource.containsKey(currResourceGroupEntry.getValue().getResource().getUniqueName())) {
    		  groupsToAddPerResource = allGroupsToAddPerResource.get(currResourceGroupEntry.getValue().getResource().getUniqueName());
    	  } else {
    		  groupsToAddPerResource = new HashMap<Long,ResourceGroup>();
    		  //add the create map to its father
    		  allGroupsToAddPerResource.put(currResourceGroupEntry.getValue().getResource().getUniqueName(), groupsToAddPerResource);;
    	  }
        	
    	  groupsToAddPerResource.put(currResourceGroupEntry.getValue().getResourceGroupId(), currResourceGroupEntry.getValue());
      }
      
      //in case there are resources without groups, still there's a usage of the map per resource for these groups then we'll create an empty one
      //only needed when a modify account happens for a resource that has no groups
      for (Resource currRes : allResourcesToAdd.values()) {
    	  if ( !allGroupsToAddPerResource.containsKey(currRes.getUniqueName()) ) {
    		  allGroupsToAddPerResource.put(currRes.getUniqueName(), new HashMap<Long,ResourceGroup>());
    	  }
      }
        
        

        
      //nov-20-07 needed by 'create account' method  
      //?Create a MAP of all roles to add per resource
      Map<Long, Set<Role>> rolesToAddPerResource = new HashMap<Long, Set<Role>>();
      for (Role currRoleToAdd : rolesToAdd) {
    	  for (Resource currResourceInCurrRole : currRoleToAdd.getResources()) {
    		  if (rolesToAddPerResource.containsKey(currResourceInCurrRole.getResourceId())) {
    			  rolesToAddPerResource.get(currResourceInCurrRole.getResourceId()).add(currRoleToAdd);
    		  } else {
    			  Set<Role> rolesOfCurrResourceSet = new HashSet<Role>();
    			  rolesOfCurrResourceSet.add(currRoleToAdd);
    			  rolesToAddPerResource.put(currResourceInCurrRole.getResourceId(), rolesOfCurrResourceSet);
    		  }
    	  }
      }
        
        
        
        
        
        
        
      
      
      
      
      
      
      
        
        
        
        
//PREPARE DATA CONTAINERS FOR ROLES/GROUPS/ETC REMOVAL.       
        //Create a GLOBAL groups map containing all groups to remove from all resources
        Map<Long, ResourceGroup> allGroupsToRemoveGlobally = new HashMap<Long, ResourceGroup>();
        //Create a GLOBAL resources map to add
        Map<Long, Resource> allResourcesToRemove = new HashMap<Long, Resource>();

        log.debug("Iterating over all roles to -remove-, preparing 'Resource Groups / Resources' to remove global lists");
        for (Role currUserRoleToRemove : rolesToRemove) {
        	//If the role to remove was specified in the add list, then skip the role removal.
        	//same should be happen regarding the roles to insert list.
        	
        	boolean isRoleToRemoveFlaggedToBeAdded = false;
            for (Role currRoleToAdd : rolesToAdd) {
                if (currUserRoleToRemove.getName().toUpperCase().equals(currRoleToAdd.getName().toUpperCase())) {
                	log.debug("Skipping role removal for role named '" + currUserRoleToRemove.getName() + "' since this role was also requested to be inserted!");
                	isRoleToRemoveFlaggedToBeAdded = true;
                    break;
                }
            }
            if (isRoleToRemoveFlaggedToBeAdded) {
            	continue;
            }
        	
            
            
        	log.debug("Iterating over resources associated with role name: '" + currUserRoleToRemove.getName() + "', with resources amount: '" + currUserRoleToRemove.getResources().size() + "', appending all resources to the 'global Resource TO REMOVE map'.");
        	for (Resource currResource : currUserRoleToRemove.getResources()) {
        		if (!allResourcesToRemove.containsKey(currResource.getResourceId())) {
        			log.debug("Adding resource with name '" + currResource.getDisplayName() + "' to 'global resource to remove' map");
        			allResourcesToRemove.put(currResource.getResourceId(), currResource);
        		} else {
        			log.debug("Resource name: '" + currResource.getDisplayName() + "' already flagged to be removed...");
        		}
        	}

        	log.debug("Iterating over resource groups associated with role name: '" + currUserRoleToRemove.getName() + "', with groups amount: '" + currUserRoleToRemove.getResourceGroups().size() + "', adding all groups to the 'global Resource Groups TO REMOVE map'.");
        	for (ResourceGroup currRG : currUserRoleToRemove.getResourceGroups()) {
        		if (!allGroupsToRemoveGlobally.containsKey(currRG.getResourceGroupId())) {
        			log.debug("Adding group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' to 'global groups to remove' map");
        			allGroupsToRemoveGlobally.put(currRG.getResourceGroupId(), currRG);
        		} else {
        			log.debug("Group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' already flagged to be removed...");
        		}
        	}
        }
              
              

        //Create a map in map of groups to remove per resource
        //map structure: Map<String=resource unique name,MAP<Long=resourceGroupId,ResourceGroup>>
        Map<String, Map<Long,ResourceGroup>> allGroupsToRemovePerResource = new HashMap<String, Map<Long,ResourceGroup>>();

        //fill the map with elements...
        Map<Long,ResourceGroup> groupsToRemovePerResource = null;
        for (Map.Entry<Long,ResourceGroup> currResourceGroupEntry : allGroupsToRemoveGlobally.entrySet()) {
        	//Make sure there is a map already for the current group's resource, otherwise create one
          	if (allGroupsToRemovePerResource.containsKey(currResourceGroupEntry.getValue().getResource().getUniqueName())) {
          		groupsToRemovePerResource = allGroupsToRemovePerResource.get(currResourceGroupEntry.getValue().getResource().getUniqueName());
          	} else {
          		groupsToRemovePerResource = new HashMap<Long,ResourceGroup>();
          		//add the create map to its father
          		allGroupsToRemovePerResource.put(currResourceGroupEntry.getValue().getResource().getUniqueName(), groupsToRemovePerResource);;
          	}
          	
          	groupsToRemovePerResource.put(currResourceGroupEntry.getValue().getResourceGroupId(), currResourceGroupEntry.getValue());
          }

              
              
              
              

        
              
              
              
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        
        
        
        
        
        
        
        Set<Long> resourceAccountWasModifiedAlready = new HashSet<Long>();
       // try {
        	boolean wasAccountFoundOnCurrResource;
        	log.debug("Iterating over the 'Global Resources to ADD' MAP with amount '" + allResourcesToAdd.size() + "', deciding whether to add SPML 'AddRequests' or 'ModifyRequests'");
        
        	for (Resource currResource : allResourcesToAdd.values()) {
        		wasAccountFoundOnCurrResource = false;
        		log.info("Current Resource to add is: '" + currResource.getDisplayName() + "', checking whether the user already has an account on this resource or not...");
        		
        		SpmlTask accountSpmlTaskPerResource = null;
        		//Prepare a list of resource groups to add as SPML references
    			Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForInsertion = new HashSet<ResourceGroup>();
    			
    			
    			//Account does not exist, then create an 'Add' SPML task
        		if (!user.isUserHasAccount(currResource)) {
        			log.debug("User does not have an account, performing 'Add Account' operation for resource: '" + currResource.getDisplayName() + "'");
        			
        			Map<Long,ResourceGroup> groupsPerResourceForInsertion = allGroupsToAddPerResource.get(currResource.getUniqueName());
        			
        			//we also would like to add all groups relevant to current resource that were not flagged to be removed
        			Set<ResourceGroup> groupsFromOtherRoles = user.getResourceGroupsForResource(currResource,rolesToRemove,true);
        			//Get all groups that were not flagged to be removed!!!!!!!!!!!!!!!
        			
        			//make sure 'groupsPerResourceForInsertion' is not null (can happen if there are no groups specified in any role to add)
        			if (groupsPerResourceForInsertion == null) {
        				groupsPerResourceForInsertion = new HashMap<Long,ResourceGroup>();
        			}
        			
        			if (groupsFromOtherRoles != null) {
        				for (ResourceGroup currRG : groupsFromOtherRoles) {
            				if (!groupsPerResourceForInsertion.containsKey(currRG.getResourceGroupId())) {
            					groupsPerResourceForInsertion.put(currRG.getResourceGroupId(), currRG);
            				}
            			}
        			}
        			
        			
        			if (groupsPerResourceForInsertion.size() < 1) {
        				log.debug("Current resource has no access groups to associate, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML AddRequest method 
        			else {
        				resourceGroupsOfCurrentResourceToPassToRequestForInsertion.addAll(groupsPerResourceForInsertion.values());
        			}
        			
        			
        			//create the ADD task
        			accountSpmlTaskPerResource = resourceOperationManager.createAddAccountRequestForUserTask(currResource, user, resourceGroupsOfCurrentResourceToPassToRequestForInsertion, rolesToAddPerResource.get(currResource.getResourceId()));
        			accountSpmlTaskPerResource.setBulkTask(bulkTask);
            		bulkTask.addTask(accountSpmlTaskPerResource);
        		}
        		
        		//Otherwise account exists, then create a 'Modify' SPML Task
        		else {
        			Account accountEntity = user.getAccountOnTarget(currResource.getUniqueName());
        			
        			//just a sanity check
        			if (accountEntity == null) {
        				throw new OperationException("Couldnt find account on target system: " + currResource.getDisplayName() + ", for user: " + user.getName() + ", although expected one...(user.isUserHasAccount returned true! what is wrong here?");
        			}
        			
        			//If there are any new groups to reference to the modified account, then get them here
        			//TODO: Support verification whether account already has these groups associated or not
        			log.debug("User HAS an account, performing 'Modify Account' operation for account name '" + accountEntity.getName() + ", resource: '" + currResource.getDisplayName() + "'");
        			
        			Map<Long,ResourceGroup> groupsPerResourceForInsertion = allGroupsToAddPerResource.get(currResource.getUniqueName());
        			
        			if (groupsPerResourceForInsertion == null) {
        				log.debug("Current resource has no access groups to associate, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML modify method 
        			else {
        				//VALIDATE: that each of the specified groups were not already assoc by other already assoc roles
        				for (ResourceGroup currRGToAdd : groupsPerResourceForInsertion.values()) {
        					//also validate that it is not available in roles to be removed, no reason to add the groups as the user already has them
        					//also make sure they are not flagged to be revoked!
        					//WRONG: if (user.isResourceGroupAssociatedByRoles(currRGToAdd, rolesToRemove)) {
        					if (user.isResourceGroupAssociatedByRoles(currRGToAdd, null)) {
        						continue;
        					} else {
        						resourceGroupsOfCurrentResourceToPassToRequestForInsertion.add(currRGToAdd);
        					}
        				}
        				
        				//resourceGroupsOfCurrentResourceToPassToRequestForInsertion.addAll(groupsPerResourceForInsertion.values());
        			}
        			
        			
        			
        			
        			//If there are any groups references to remove from modified account, then get them here
        			Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
        			Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());
        			
        			if (groupsPerResourceForRemoval == null) {
        				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML modify method 
        			else {
        				//VALIDATE: Make sure that the group was not already assigned by other assoc roles (except the roles that were flagged to be removed) // 
        					//also nmake sure the group is not flagged to be added, if so no, no revoke shell occur
        				for (ResourceGroup currRGToRemove : groupsPerResourceForRemoval.values()) {
        					if ( (user.isResourceGroupAssociatedByRoles(currRGToRemove, rolesToRemove)) || (groupsPerResourceForInsertion.containsKey(currRGToRemove.getResourceGroupId())) ) {
        						continue;
        					} else {
        						resourceGroupsOfCurrentResourceToPassToRequestForRemoval.add(currRGToRemove);
        					}
        				}
        				
        				//resourceGroupsOfCurrentResourceToPassToRequestForRemoval.addAll(groupsPerResourceForRemoval.values());
        			}

        			//only create the task if there are any modifications
        			//Create an SPML task to perform account modifications
        			if (resourceGroupsOfCurrentResourceToPassToRequestForRemoval.isEmpty() &&  (resourceGroupsOfCurrentResourceToPassToRequestForInsertion.isEmpty())) {
        				log.debug("Skipping Modify account task creation for account name '" + accountEntity.getName() + "', of resource name '" + currResource.getDisplayName() + " since there were no access groups references modifications to perform...");
        			}
        			else {
        				accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(accountEntity, resourceGroupsOfCurrentResourceToPassToRequestForInsertion, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
        				accountSpmlTaskPerResource.setBulkTask(bulkTask);
                		bulkTask.addTask(accountSpmlTaskPerResource);
                		
                		resourceAccountWasModifiedAlready.add(currResource.getResourceId());
        			}
        		}
        	}
        	
          
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	//Manage all tasks as a part of the REMOVE 'groups/resources' process
            log.debug("Iterating over the 'Global Resources to REMOVE' MAP with amount '" + allResourcesToRemove.size() + "', seeking accounts to delete");
            for (Resource currResource : allResourcesToRemove.values()) {
                log.info("Currently iterating over Resource with name: '" + currResource.getDisplayName() + "'");

                //try {
                    log.debug("Trying to find account on resource for user.");
                    Account account = user.getAccountOnTarget(currResource.getUniqueName());
                    
                    if (account == null) {
                    	log.warn("Could not find account for User '" + user.getName() + "', on Resource: '" + currResource.getDisplayName() + "', skipping resource...");
                        continue;
                    }

                    
                    log.info("Found account with name: '" + account.getName() + "', associated to user.");
                    //TODO: Should we support references removal when performing account delete or we can trust resources to do this job?


                    log.debug("Checking whether the account on the current iterated resource can be deleted or not...");
                    //Check whether the account can be deleted or not by checking whether it is protected by other user roles or not
                    //if (!(account, rolesToAdd, userRolesToRemove)) {
                    if (!user.isAssocRolesReferencesResource(account.getResource(),rolesToAdd, rolesToRemove)) {
                        log.debug("The account named: '" + account.getName() + "' on current iterated resource named: '" + currResource.getDisplayName() + "' is not protected by other roles, determining whether to delete account or not according to system configuration");
                        //ems.info("Account name: '" + account.getName()+"', on target system: '" + currTs.getDisplayName() +"' is not protected by any other roles attached to user, adding a task to delete the account");

                        
                        
                        
                        
                        
                        //not protected, add a task to delete the account
                        Set<Role> excludeProtectedRoleList = new HashSet<Role>();
/*WAA                        for (UserRole currExcludedUserRole : userRolesToRemove) {
                            excludeProtectedRoleList.add(currExcludedUserRole.getRole());
                        }
*/
                        
                        isDeleteAccountIfRoleHasLastResourceReference = account.getResource().isDelAccountIfLastRoleRefToThisResourceIsRevoked();
                        if (isDeleteAccountIfRoleHasLastResourceReference) {
                        	log.debug("configuration set to delete account, adding a task to delete the account!");
                        	
                        	
                        	//03-aprl-08
                        	//There was an issue where a modify request included roles to remove: 'role-ad1,role-ad2', roles to add 'role-ad1'
                        	//role-ad1 was removed from both add/remove lists (deltas calculation above) and the result was to remove 'role-ad2'
                        	//which obviously resulted a 'delete account' task, but this is not the expected result.
                        	//there was still a role to be added, meaning the account should not be deleted and a modify should occur for the 'role-ad2' that should be removed.
                        	//FIXME: this might look like a patch and can be probably done on the top somewhere instead of here
                        	boolean isAccountShouldBeDeleted = true;
                        	for (Role currRoleToAdd : rolesToAdd) {
                        		if (currRoleToAdd.isResourceAssociated(account.getResource())) {
                        			isAccountShouldBeDeleted = false;
                        			break;
                        		}
                        	}
                        	
                        	if (isAccountShouldBeDeleted) {
                        		//force, roles protection already verified here
                        		SpmlTask deleteAccountTask = resourceOperationManager.createDeleteAccountRequestTask(account, excludeProtectedRoleList, true);
                        		deleteAccountTask.setBulkTask(bulkTask);
                        		bulkTask.addTask(deleteAccountTask);
                        	} else {
                        		log.debug("Account won't be deleted! the original 'roles to add' list includes a role to resource '" + account.getResource().getDisplayName() + "'");
                        		
                        		
                        		//DUPLICATED!@#$!@#$!@#$
                        		Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
                        		Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());

                        		if (groupsPerResourceForRemoval == null) {
                    				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
                    			}
                    			//Otherwise add all groups to the list that is passed to the SPML modify method 
                    			else {
                    				//DUPLICATED FROM BELOW, THIS CODE SHOULD BE AGGREGATED.
                    				//VALIDATE that each group is not assoc by other roles available to user (except the roles flagged to be revoked) including the roles flagged to be inserted
                    				for (ResourceGroup currRGToRevoke : groupsPerResourceForRemoval.values()) {
                    					if (user.isResourceGroupAssociatedByRoles(currRGToRevoke, rolesToRemove)) {
                    						continue;
                    					} else {
                    						resourceGroupsOfCurrentResourceToPassToRequestForRemoval.add(currRGToRevoke);
                    					}
                    				}
                    				
                    				log.debug("Modifying account...");
                    				SpmlTask accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(account, null, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
                    				accountSpmlTaskPerResource.setBulkTask(bulkTask);
                                    bulkTask.addTask(accountSpmlTaskPerResource);
                    			}
                        	}
                        	
                        	
                        } else {
                        	log.debug("Nothing to do, configuration set not to delete account.");
                        	
                        	
                        	//DUPLICATED!@#$!@#$!@#$
                    		Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
                    		Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());

                    		
                    		
                    		
                    		if (groupsPerResourceForRemoval == null) {
                				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
                			}
                			//Otherwise add all groups to the list that is passed to the SPML modify method 
                			else {
                				//DUPLICATED FROM BELOW, THIS CODE SHOULD BE AGGREGATED.
                				//VALIDATE that each group is not assoc by other roles available to user (except the roles flagged to be revoked) including the roles flagged to be inserted
                				for (ResourceGroup currRGToRevoke : groupsPerResourceForRemoval.values()) {
                					if (user.isResourceGroupAssociatedByRoles(currRGToRevoke, rolesToRemove)) {
                						continue;
                					} else {
                						resourceGroupsOfCurrentResourceToPassToRequestForRemoval.add(currRGToRevoke);
                					}
                				}
                				
                				log.debug("Modifying account...");
                				SpmlTask accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(account, null, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
                				accountSpmlTaskPerResource.setBulkTask(bulkTask);
                                bulkTask.addTask(accountSpmlTaskPerResource);
                			}

                        
                    		
                    		
                        	//what nothing to do? lets revoke the access at least...
                        	SpmlTask accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(account, null, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
            				accountSpmlTaskPerResource.setBulkTask(bulkTask);
                    		bulkTask.addTask(accountSpmlTaskPerResource);
                    		resourceAccountWasModifiedAlready.add(currResource.getResourceId());
                        	
                        }
                    } else {
                        log.debug("The account name: '" + account.getName() + "' on current iterated resource named: '" + currResource.getDisplayName() + "' is PROTECTED by other roles, skipping account removal!");
                        //perform the modify if not already done by 'ADD' above.
                        if (!resourceAccountWasModifiedAlready.contains(currResource.getResourceId())) {
                        	Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
                			Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());
                			
                			
                			if (groupsPerResourceForRemoval == null) {
                				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
                			}
                			//Otherwise add all groups to the list that is passed to the SPML modify method 
                			else {
                				Map<Long,ResourceGroup> groupsPerResourceForInsertion = allGroupsToAddPerResource.get(currResource.getUniqueName());
                				//VALIDATE that each group is not assoc by other roles available to user (except the roles flagged to be revoked) including the roles flagged to be inserted
                				for (ResourceGroup currRGToRevoke : groupsPerResourceForRemoval.values()) {
                					if (user.isResourceGroupAssociatedByRoles(currRGToRevoke, rolesToRemove)) {
                						if (groupsPerResourceForInsertion != null) {
                							if (groupsPerResourceForInsertion.containsKey(currRGToRevoke.getResourceGroupId())) {
                								continue;
                							}
                						}
                						
                						continue;
                					} else {
                						resourceGroupsOfCurrentResourceToPassToRequestForRemoval.add(currRGToRevoke);
                					}
                				}
                				
                				
                				//resourceGroupsOfCurrentResourceToPassToRequestForRemoval.addAll(groupsPerResourceForRemoval.values());
                			}
                			
                			
                            SpmlTask accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(account, null, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
                            accountSpmlTaskPerResource.setBulkTask(bulkTask);
                            bulkTask.addTask(accountSpmlTaskPerResource);
                            
                            
                            //ems.info("Account name: '" + account.getName()+"', on target system: '" + currTs.getDisplayName() +"' is protected by other roles attached to user, won't delete the account.");
                            //Add the tasks to the bulk task
                            //for (Task currTask : groupsRemoveMembershipTasks.values()) {
                        } else {
                        	log.debug("Account on current iterated resource was already modified above...(by the 'add' part), skipping modify account");
                        }
                    }
                    /*
                } catch (TaskCreationException tce) {
                    throw new OperationException(tce.getMessage());
                }
                */
            }

            
            
            
            
            StringBuffer journalingDetails = new StringBuffer();
            journalingDetails.append("Added Roles: ");
            for (Role currRoleToAdd : rolesToAdd) {
            	/*
            	//all tasks were successfully created, perform repository modifications
            	log.debug("Associating roles to user in repository...");
                UserRole userRole = new UserRole();
                userRole.setCreationDate(new Date());
                userRole.setRole(currRoleToAdd);
                userRole.setUser(user);
                userRole.setExpirationDate(currRoleToAdd.getExpirationDate());
                userRole.setInherited(currRoleToAdd.isInherited());
                userRole.setUser(user);
                
                //Instead of task, just persist the association
                //object references an unsaved transient instance - save the transient instance before flushing: velo.entity.UserRole
                //when used CASCADE.ALL over user.getUserRoles() it did not happen, but ofr some reason TWO userRoles were inserted into the DB
                */
            	
                journalingDetails.append(currRoleToAdd.getName() + ",");
                /*
                if (modifyEntities) {
                	log.debug("Persisting UserRole in repository...");
                	em.persist(userRole);
                } else {
                	log.debug("Won't persisting UserRole in repository, adding UserRole to User entity...");
                	//problematic, userRole will be detached
                	user.getUserRoles().add(userRole);
                }
                */
                
                
                //user.getUserRoles().add(userRole);
            }
            

            log.debug("Remove Roles association from User in repository...");
            journalingDetails.append("Removed Roles: ");
            
            
            
/*WAAAA, CLEAN CLEAN CLEAN
for (int i=0;i<1;i++) {
UserRole currUserRoleToRemove = new UserRole(); 
//            for (UserRole currUserRoleToRemove : userRolesToRemove) {
                boolean foundRoleInRolesToAdd = false;
                //If role was flagged to be added, then skip it.
                for (Role currRoleToAdd : rolesToAdd) {
                    if (currUserRoleToRemove.getRole().getName().toUpperCase().equals(currRoleToAdd.getName().toUpperCase())) {
                        foundRoleInRolesToAdd = true;
                        break;
                    }
                }

                if (!foundRoleInRolesToAdd) {
                	//Remove the userRole entity
                	//user.removeUserRole(currUserRoleToRemove);
                	//god knows why, but when removed, still the entity exists in DB
                	//When adding roles above this way, they get persisted.
                	
                	/*20-aug-07(asaf): Sucks does not work, god knows why!!!
                	 * WTF? REALLY, THE USER IS MANAGED, user.getUserRole() cascaded with 'REMOVE', but still hibernate does not invoke DELETE query on this object!
                	log.debug("CONTAINERS???????????" + em.contains(user));
                	log.debug("SIZE BEFORE: " + user.getUserRoles().size());
                	user.removeUserRole(currUserRoleToRemove);
                	log.debug("SIZE AFTER: " + user.getUserRoles().size());
                	em.merge(user);
                	log.debug("FLUSHING!!!");
                	em.flush();
                	log.debug("EO FLUSHING!!!");
                	*/
/*
                	if (modifyEntities) {
                		journalingDetails.append(currUserRoleToRemove.getRole().getName() + ",");
                		user.removeUserRole(currUserRoleToRemove);
                		em.remove(em.find(UserRole.class, currUserRoleToRemove.getUserRoleId()));
                		em.flush();
                	}
                	
                	//UserRole userRoleToBeRemoved = em.find(UserRole.class, currUserRoleToRemove.getUserRoleId());
                	//em.remove(userRoleToBeRemoved);
                }
            }
            log.debug("ENDED Preparing tasks to remove the User Roles association in repository...");
            
            if (modifyEntities) {
            	em.merge(user);
            }
*/
            
            log.debug("Returning bulk tasks with task amount: '" + bulkTask.getTasks().size() + "'");

            return bulkTask;
    }
    
    
    
    @Transient
    public boolean isResourceGroupAssignedByOtherRoles(ResourceGroup rg, Set<Role> excludedRoles, User user) {
        log.debug("Checking whether group with unique ID '" + rg.getUniqueId() + "', on Resource: '" + rg.getResource().getDisplayName() + "' for User: '" + user.getName() + "' is protected by user roles (except the specified excluded User Roles)");

        for (UserRole currUserRole : user.getUserRoles()) {
            boolean isRoleExcluded = false;
            //Make sure the current user role is not in the exclude list
            for (Role currExcludedRole : excludedRoles) {
                //Found role as excluded, set the flag and break the loop
                if (currUserRole.getRole().equals(excludedRoles)) {
                    log.debug("UserRole ID: '" + currExcludedRole.getRoleId() + "', of Role named: '" + currExcludedRole.getName() + "' is excluded, skipping current iterated user role...");
                    isRoleExcluded = true;
                }
            }

            //It is not an excluded role, perform the group verification
            if (!isRoleExcluded) {
                log.debug("UserRole ID: '" + currUserRole.getUserRoleId() + "', of Role named: '" + currUserRole.getRole().getName() + "' is not excluded, validating whether the RG is associated to this role or not.");
                if (currUserRole.getRole().isResourceGroupAssociated(rg)) {
                    log.debug("Group is associated!, returning true!");
                    return true;
                } else {
                    log.debug("Group is not associated, continuing to the next User Role validation...");
                }
            }
        }
        
        log.debug("Validating RG assignment to roles associated to user's positions as well...");
        //if no true was returned, keep checking with the roles inherited from positions
        for (Position currPos : user.getPositions()) {
        	for (PositionRole currPosRoleInCurrPos : currPos.getPositionRoles()) {
        		boolean isRoleExcluded = false;
                //Make sure the current user role is not in the exclude list
                for (Role currExcludedRole : excludedRoles) {
                    //Found role as excluded, set the flag and break the loop
                    if (currPosRoleInCurrPos.getRole().equals(excludedRoles)) {
                        log.debug("PositionRole ID: '" + currExcludedRole.getRoleId() + "', of Role named: '" + currExcludedRole.getName() + "' is excluded, skipping current iterated user role...");
                        isRoleExcluded = true;
                    }
                }

                //It is not an excluded role, perform the group verification
                if (!isRoleExcluded) {
                    log.debug("PositionRole (role ID): '" + currPosRoleInCurrPos.getRole().getRoleId() + ", of pos ID '" + currPosRoleInCurrPos.getPosition().getPositionId() + "', of Role named: '" + currPosRoleInCurrPos.getRole().getName() + "' is not excluded, validating whether the RG is associated to this role or not.");
                    if (currPosRoleInCurrPos.getRole().isResourceGroupAssociated(rg)) {
                        log.debug("Group is associated to role, returning true!");
                        return true;
                    } else {
                        log.debug("Group is not associated, continuing to the next Role validation...");
                    }
                }
        	}
        }

        return false;
    }
    
    
    
    
    
    public Long modifyAccountsGroupsAssignment(Set<User> users, Role role, Set<ResourceGroup> groupsToAssign, Set<ResourceGroup> groupsToRevoke) throws OperationException {
    	log.debug("Modify Resource Group Assignment for Role '" + role.getName() + "' has started");
    	log.debug("Amount of affected users is '" + users.size() + "', with groups amount to revoke '" + groupsToRevoke.size() + "', groups to assign '" + groupsToAssign.size() + "'");
		BulkTask bulkTask = BulkTask.factory("Resource Groups in Role(" + role.getName() + ") modifications");
		
		//create a set of groups to remove/add per resource as ('resource'/list of groups)
		Map<String,Set<ResourceGroup>> groupsToAssignMap = new HashMap<String,Set<ResourceGroup>>();
		Map<String,Set<ResourceGroup>> groupsToRevokeMap = new HashMap<String,Set<ResourceGroup>>();
		
		Set<Resource> resources = new HashSet<Resource>();
		for (ResourceGroup currRG : groupsToRevoke) {
			if (!resources.contains(currRG.getResource())) {
				resources.add(currRG.getResource());
			}
			if (!groupsToRevokeMap.containsKey(currRG.getResource().getUniqueName())) {
				groupsToRevokeMap.put(currRG.getResource().getUniqueName(), new HashSet<ResourceGroup>());
			}
		}
		for (ResourceGroup currRG : groupsToAssign) {
			if (!resources.contains(currRG.getResource().getUniqueName())) {
				resources.add(currRG.getResource());
			}
			if (!groupsToAssignMap.containsKey(currRG.getResource().getUniqueName())) {
				groupsToAssignMap.put(currRG.getResource().getUniqueName(), new HashSet<ResourceGroup>());
			}
		}
		
		
		for (ResourceGroup currRG : groupsToRevoke) {
			groupsToRevokeMap.get(currRG.getResource().getUniqueName()).add(currRG);
		}
		for (ResourceGroup currRG : groupsToAssign) {
			groupsToAssignMap.get(currRG.getResource().getUniqueName()).add(currRG);
		}
		
		
		
		Set<Role> excludedRoles = new HashSet<Role>();
		excludedRoles.add(role);
		Set<ResourceGroup> userGroupsToAssign = new HashSet<ResourceGroup>();
		Set<ResourceGroup> userGroupsToRevoke = new HashSet<ResourceGroup>();
		
		
		//reload the role entity, just in case...
		role = em.find(Role.class,role.getRoleId());
		for (User currUser : users) {
			//TODO is this ok? can't there be something more efficient here?
			//reload the user from DB as we can't trust the user is eagerly loaded nor the entityManager is still active
			currUser = em.find(User.class, currUser.getUserId());
			
			
			log.trace("Modifying resource group assignment to current iterated user '" + currUser.getName() +"'");
			 for (Resource currR : resources) {
				 userGroupsToAssign.clear();
				 userGroupsToRevoke.clear();
				 
				 Account acc = currUser.getAccountOnTarget(currR.getUniqueName());
				 if (acc == null) {
					 log.info("Cannot modify resource group assignment for user '" + currUser.getName() + "' since the user has no account on resource '" + currR.getDisplayName() + "'");
					 continue;
				 }
				 
				 try {
					 //per group to revoke for the current resource, make sure the groups is 'revokable'
					 if (groupsToRevokeMap.containsKey(currR.getUniqueName())) {
					 for (ResourceGroup currRG : groupsToRevokeMap.get(currR.getUniqueName())) {
						 if (isResourceGroupAssignedByOtherRoles(currRG, excludedRoles, currUser)) {
							 log.debug("Cannot revoke resource group '" + currRG.getDisplayName() + "' (in resource '" + currRG.getResource().getDisplayName() + "') from user '" + currUser.getName() + "' since it was assigned by other roles assigned to user");
						 } else {
							 userGroupsToRevoke.add(currRG);
						 }
					 }
					 }
					 
					//per group to assign for the current resource, make sure the groups was not already assigned by other roles
					 if (groupsToAssignMap.containsKey(currR.getUniqueName())) {
					 for (ResourceGroup currRG : groupsToAssignMap.get(currR.getUniqueName())) {
						 if (!isResourceGroupAssignedByOtherRoles(currRG, excludedRoles, currUser)) {
							 //make sure the group is not flagged as 'deleted' in resource.
							 if (!currRG.isDeletedInResource()) {
								 userGroupsToAssign.add(currRG);
							 } else {
								 log.debug("Cannot assign resource group '" + currRG.getDisplayName() + "' (in resource '" + currRG.getResource().getDisplayName() + "') from user '" + currUser.getName() + "' since the group was flagged as 'deleted' in resource.");
							 }
							 
						 } else {
							 log.debug("Cannot assign resource group '" + currRG.getDisplayName() + "' (in resource '" + currRG.getResource().getDisplayName() + "') from user '" + currUser.getName() + "' since it was already assigned by other roles assigned to user");
						 }
					 }
					 }
					 
					 
					 if ( (userGroupsToAssign.size() > 0) || (userGroupsToRevoke.size() > 0) ) {
						 //make sure we got the lists for the current iterated resource
						 SpmlTask currTask = resourceOperationManager.createModifyAccountRequestForUserTask(acc,userGroupsToAssign, userGroupsToRevoke,null);
						 currTask.setBulkTask(bulkTask);
						 bulkTask.addTask(currTask);
					 } else {
						 log.trace("Modify task was skipped since there are no groups to ASSIGN or REOVOKE");
					 }
				 }catch (OperationException e) {
					 throw (e);
				 }
			 }
		}
		
		log.debug("Finished iterating over users, performing entities(roles/groups) associations...");
		modifyRoleEntityGroupsAssignmentAssoc(role, groupsToAssign, groupsToRevoke);
		if (bulkTask.getTasks().size() > 0) {
			log.debug("Bulk task has '" + bulkTask.getTasks().size() + "', persisting bulk task with amount of tasks '" + bulkTask.getTasks().size() + "'");
			em.persist(bulkTask);
			
			em.flush();
			return bulkTask.getBulkTaskId();
		} else {
			return null;
		}
	}
    
    public void modifyRoleEntityGroupsAssignmentAssoc(Role role, Set<ResourceGroup> groupsToAssign, Set<ResourceGroup> groupsToRevoke) throws OperationException {
    	//Handle Add/Remove group assocaition from Role Entity
        for (ResourceGroup currGroupToRemove : groupsToRevoke) {
            //Make sure that the role contains the Group Object.
        	ResourceGroup groupObjectToBeRemoved = role.getResourceGroup(currGroupToRemove.getUniqueId());
        	if (groupObjectToBeRemoved == null) {
        		throw new OperationException("Cannot remove association of group '" + currGroupToRemove.getDisplayName() + "' since the ResourceGroup was not assigned to Role.getResourceGroup() List.");
        	}
        	role.getResourceGroups().remove(groupObjectToBeRemoved);
        }

        for (ResourceGroup currGroupToAdd : groupsToAssign) {
        	//this already associate in DB, although role is not in 'managed' state, not sure why hibernate do that :-q
        	role.getResourceGroups().add(currGroupToAdd);
        }
        
        
        //FIXME: 15-jul-08: Ok, this is very weird, at the moment I have added the merge here
        //I got errors when this method was called from 'RoleActionsBean', the reason was that for each group to be associated
        //TWO inserts were invoked and SQL threw a duplicated entry exception.
        //as a thought, the reason might be that roleHome invokes its own merge while here we do another merge
        //and somehow the different entity managers(PE/Seam EM) are not synchronized.
        //NOTE: this is just a workaround and should be taken care ASAP
        //em.merge(role);
    }
    
    
    
    
    
    
    
    
    
    
    //SCANNER
    public void createRolesRevokerTimerScanner(long initialDuration, long intervalDuration) {
        //Calculating miliseconds from the specified parameters as seconds
        long msInitialDuration = initialDuration * 1000;

        timerService.getTimers().clear();
        sessionContext.getTimerService().createTimer(msInitialDuration, msInitialDuration, "roles-revoker-scanner");
        log.info("Created a Roles-Revoker Scanner timer with interval of '" + msInitialDuration + "' ms.");
    }

    public void createRolesRevokerTimeScanner() throws OperationException {
        boolean isRolesRevokerScannerEnabled = SysConf.getSysConf().getBoolean("access_control.activate_roles_revoker_scanner");

        if (isRolesRevokerScannerEnabled) {
            int interval = SysConf.getSysConf().getInt("access_control.roles_revoker_scanner_interval_in_seconds");
            log.info("Starting Roles-Revoker Scanner, Firing the roles revoker scanner each '" + interval + "' seconds");
            createRolesRevokerTimerScanner(interval, interval);
        } else {
            throw new OperationException("Cannot start Roles Revoker Scanner since it is disabled in system configuration!");
        }
    }

    public boolean isRolesRevokerScannerActivate() {
        if (timerService.getTimers().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void changeRolesRevokerScannerMode() throws OperationException {
        boolean isRolesRevokerScannerEnabled = SysConf.getSysConf().getBoolean("access_control.activate_roles_revoker_scanner");

        log.info("Requested to change Roles Revoker Scanner Mode...");
        if (!isRolesRevokerScannerEnabled) {
            throw new OperationException("Cannot change roles revoker scanner mode since scanner is disabled in system configuration!");
        }

        if (timerService.getTimers().size() > 0) {
            log.info("Current task Scanner mode is enabled, found '" + timerService.getTimers().size() + " timers..., clearing...");
            Iterator timersIt = timerService.getTimers().iterator();
            while (timersIt.hasNext()) {
                Timer currTimer = (Timer) timersIt.next();
                log.debug("Found timer with info: '" + currTimer.getInfo() + "', object: '" + currTimer + "'");
                currTimer.cancel();
            }
        } else {
            log.info("Current Roles Revoker Scanner mode is disabled, found '" + timerService.getTimers().size() + " timers..., adding timer...");
            int interval = SysConf.getSysConf().getInt("access_control.roles_revoker_scanner_interval_in_seconds");
            createRolesRevokerTimerScanner(interval, interval);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
    - Make sure there is no conflict and the same role flagged to be added and removed together - if so throw an exception
    - Iterate over the roles to associate to user
     */
    @Deprecated
    public BulkTask modifyRolesOfUserTasksOld(Set<UserRole> userRolesToRemove, Set<Role> rolesToAdd, User user, boolean modifyEntities) throws OperationException {
    	//This is critical, the user is obviously not in managed state, when invoked user.getUserRoles.add(role) it got persisted
    	//when invoked user.getUserRoles().remove(userRole) it didn't... not sure how Hibernate acts here but reloading the user and having it in managed state should hopefully solve these problems
        log.debug("Modifying User Roles action has started...");
        log.debug("Removing '" + userRolesToRemove.size() + "' roles, adding '" + rolesToAdd.size() + "' roles to user name: '" + user.getName() + "'");
        log.debug("User has UserRoles in repository with amount: '" + user.getUserRoles().size() + "'");
        
        boolean isDeleteAccountIfRoleHasLastResourceReference = SysConf.getSysConf().getBoolean("roles.last_user_role_revoke_with_reference_to_resource_deletes_resource_account");
        log.trace("Delete account if role has last resource ref?: " + isDeleteAccountIfRoleHasLastResourceReference);
        
        
        //Make sure we have the required properties set
        BulkTask bulkTask = BulkTask.factory("User roles modification for User '" + user.getName() + "'");
        EdmMessages ems = new EdmMessages();
        
        
        
        //A 'cannot open connection' / ' Transaction is not active' errors occur while accessing user object
        //then we reload the user from DB with current EM.
        //user = em.find(User.class, user.getUserId());
        
        //Create a GLOBAL groups map containing all groups to add from all resources
        Map<Long, ResourceGroup> allGroupsToAddGlobally = new HashMap<Long, ResourceGroup>();
        //Create a GLOBAL resources map to add
        Map<Long, Resource> allResourcesToAdd = new HashMap<Long, Resource>();

        log.debug("Iterating over all ROLES TO ASSOCAITE, preparing 'Resource Groups / Resourcess' TO ADD to global lists");
        
        
        for (Role currRoleToAdd : rolesToAdd) {
            //Make sure that the user does not have this role already, if so, skip the role
            //if (um.isRoleAssignedToUser(user, currRoleToAdd)) {
        	
        	log.trace("Checking whether user is already associated to role to be added with name '" + currRoleToAdd.getName() + "'");
        	if (user.isRoleAssociatedToUser(currRoleToAdd)) {
                String msg = "Could not assign role name: '" + currRoleToAdd.getName() + "', to user: '" + user.getName() + "' since the user already has this role!";
                //Log the message to the event log
                //cum.addEventLog("ROLES","FAILURE","WARNING","Failed to assign role to user",msg);
                log.warn(msg);
                ems.warning(msg);
                continue;
            }

            //If the role to add was specified in the remove list, then skip the role insertion.
            for (UserRole userRoleToRemove : userRolesToRemove) {
                if (userRoleToRemove.getRole().getName().toUpperCase().equals(currRoleToAdd.getName().toUpperCase())) {
                	log.debug("Skipping role association for role named '" + currRoleToAdd.getName() + "' since this role was also requested to be revoked!");
                    continue;
                }
            }

            String shortMsg = "Adding role '" + currRoleToAdd.getName() + "'";
            ems.info(shortMsg);

            //Collect all targets into a global array
            log.debug("Iterating over resources associated with role name: '" + currRoleToAdd.getName() + "', with resources amount: '" + currRoleToAdd.getResources().size() + "', adding all resources to the 'global Resource TO ADD map'.");
            for (Resource currResource : currRoleToAdd.getResources()) {
                if (!allResourcesToAdd.containsKey(currResource.getResourceId())) {
                    log.debug("Adding resource named '" + currResource.getDisplayName() + "' to 'global resource to add' map");
                    allResourcesToAdd.put(currResource.getResourceId(), currResource);
                } else {
                    log.debug("Resource name: '" + currResource.getDisplayName() + "' already flagged to be added...");
                }
            }

            //Collect all groups in a global array
            log.debug("Iterating over resource groups associated with role name: '" + currRoleToAdd.getName() + "', with resource groups amount: '" + currRoleToAdd.getResourceGroups().size() + "', adding all groups to the 'Global Resource Groups TO ADD map'.");
            for (ResourceGroup currTSG : currRoleToAdd.getResourceGroups()) {
                if (!allGroupsToAddGlobally.containsKey(currTSG.getResourceGroupId())) {
                    log.debug("Adding group with unique ID: '" + currTSG.getUniqueId() + "' on Target: '" + currTSG.getResource().getDisplayName() + "' to 'global groups to add' map");
                    allGroupsToAddGlobally.put(currTSG.getResourceGroupId(), currTSG);
                } else {
                    log.debug("Group with unique ID: '" + currTSG.getUniqueId() + "' on Target: '" + currTSG.getResource().getDisplayName() + "' already flagged to be added...");
                }
            }
            
        }
        
        
        
        
      //Create a map in map of groups to add per resource
      //map structure: Map<String=resource unique name,MAP<Long=resourceGroupId,ResourceGroup>>
      Map<String, Map<Long,ResourceGroup>> allGroupsToAddPerResource = new HashMap<String, Map<Long,ResourceGroup>>();
        
        //fill the map with elements...
        for (Map.Entry<Long,ResourceGroup> currResourceGroupEntry : allGroupsToAddGlobally.entrySet()) {
        	Map<Long,ResourceGroup> groupsToAddPerResource = null;
        	//Make sure there is a map already for the current group's resource, otherwise create one
        	if (allGroupsToAddPerResource.containsKey(currResourceGroupEntry.getValue().getResource().getUniqueName())) {
        		groupsToAddPerResource = allGroupsToAddPerResource.get(currResourceGroupEntry.getValue().getResource().getUniqueName());
        	} else {
        		groupsToAddPerResource = new HashMap<Long,ResourceGroup>();
        		//add the create map to its father
        		allGroupsToAddPerResource.put(currResourceGroupEntry.getValue().getResource().getUniqueName(), groupsToAddPerResource);;
        	}
        	
        	groupsToAddPerResource.put(currResourceGroupEntry.getValue().getResourceGroupId(), currResourceGroupEntry.getValue());
        }
        
        
        
      //nov-20-07 needed by 'create account' method  
      //?Create a MAP of all roles to add per resource
      Map<Long, Set<Role>> rolesToAddPerResource = new HashMap<Long, Set<Role>>();
      for (Role currRoleToAdd : rolesToAdd) {
    	  for (Resource currResourceInCurrRole : currRoleToAdd.getResources()) {
    		  if (rolesToAddPerResource.containsKey(currResourceInCurrRole.getResourceId())) {
    			  rolesToAddPerResource.get(currResourceInCurrRole.getResourceId()).add(currRoleToAdd);
    		  } else {
    			  Set<Role> rolesOfCurrResourceSet = new HashSet<Role>();
    			  rolesOfCurrResourceSet.add(currRoleToAdd);
    			  rolesToAddPerResource.put(currResourceInCurrRole.getResourceId(), rolesOfCurrResourceSet);
    		  }
    	  }
      }
        
        
        
        
        
        
        
        
        
        
        
        //PREPARE 'REMOVE' RESOURCES/GROUPS DATA

        //Create a GLOBAL groups map containing all groups to remove from all resources
        Map<Long, ResourceGroup> allGroupsToRemoveGlobally = new HashMap<Long, ResourceGroup>();
        //Create a GLOBAL resources map to add
        Map<Long, Resource> allResourcesToRemove = new HashMap<Long, Resource>();

        log.debug("Iterating over all ROLES TO REMOVE, preparing 'Target Groups / Target Systems' TO REMOVE global lists");
        for (UserRole currUserRoleToRemove : userRolesToRemove) {
        	log.debug("Iterating over targets associated with role name: '" + currUserRoleToRemove.getRole().getName() + "', with targets amount: '" + currUserRoleToRemove.getRole().getResources().size() + "', appending all targets to the 'global Resource TO REMOVE map'.");
        	for (Resource currResource : currUserRoleToRemove.getRole().getResources()) {
        		if (!allResourcesToRemove.containsKey(currResource.getResourceId())) {
        			log.debug("Adding resource with name '" + currResource.getDisplayName() + "' to 'global resource to remove' map");
        			allResourcesToRemove.put(currResource.getResourceId(), currResource);
        		} else {
        			log.debug("Resource name: '" + currResource.getDisplayName() + "' already flagged to be removed...");
        		}
        	}

        	log.debug("Iterating over resource groups associated with role name: '" + currUserRoleToRemove.getRole().getName() + "', with groups amount: '" + currUserRoleToRemove.getRole().getResourceGroups().size() + "', adding all groups to the 'global Resource Groups TO REMOVE map'.");
        	for (ResourceGroup currRG : currUserRoleToRemove.getRole().getResourceGroups()) {
        		if (!allGroupsToRemoveGlobally.containsKey(currRG.getResourceGroupId())) {
        			log.debug("Adding group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' to 'global groups to remove' map");
        			allGroupsToRemoveGlobally.put(currRG.getResourceGroupId(), currRG);
        		} else {
        			log.debug("Group with unique ID: '" + currRG.getUniqueId() + "' on Resource: '" + currRG.getResource().getDisplayName() + "' already flagged to be removed...");
        		}
        	}
        }
              
              
              
        
        //Create a map in map of groups to add per resource
        //map structure: Map<String=resource unique name,MAP<Long=resourceGroupId,ResourceGroup>>
        Map<String, Map<Long,ResourceGroup>> allGroupsToRemovePerResource = new HashMap<String, Map<Long,ResourceGroup>>();
          
          //fill the map with elements...
        Map<Long,ResourceGroup> groupsToRemovePerResource = null;
        for (Map.Entry<Long,ResourceGroup> currResourceGroupEntry : allGroupsToRemoveGlobally.entrySet()) {
        	//Make sure there is a map already for the current group's resource, otherwise create one
          	if (allGroupsToRemovePerResource.containsKey(currResourceGroupEntry.getValue().getResource().getUniqueName())) {
          		groupsToRemovePerResource = allGroupsToRemovePerResource.get(currResourceGroupEntry.getValue().getResource().getUniqueName());
          	} else {
          		groupsToRemovePerResource = new HashMap<Long,ResourceGroup>();
          		//add the create map to its father
          		allGroupsToRemovePerResource.put(currResourceGroupEntry.getValue().getResource().getUniqueName(), groupsToRemovePerResource);;
          	}
          	
          	groupsToRemovePerResource.put(currResourceGroupEntry.getValue().getResourceGroupId(), currResourceGroupEntry.getValue());
          }

              
              
              
              
              
              
              
              
        
        
        
        
        
        
        
        
        
        

        
        
        
        
        
        
        
        
       // try {
        	boolean wasAccountFoundOnCurrResource;
        	log.debug("Iterating over the 'Global Resources to ADD' MAP with amount '" + allResourcesToAdd.size() + "', deciding whether to add SPML 'AddRequests' or 'ModifyRequests'");
        
        	for (Resource currResource : allResourcesToAdd.values()) {
        		wasAccountFoundOnCurrResource = false;
        		log.info("Current Resource to add is: '" + currResource.getDisplayName() + "', checking whether the user already has an account on this resource or not...");
        		
        		SpmlTask accountSpmlTaskPerResource = null;
        		//Prepare a list of resource groups to add as SPML references
    			Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForInsertion = new HashSet<ResourceGroup>();
    			
    			
    			//Account does not exist, then create an 'Add' SPML task
        		if (!user.isUserHasAccount(currResource)) {
        			log.debug("User does not have an account, performing 'Add Account' operation for resource: '" + currResource.getDisplayName() + "'");
        			
        			Map<Long,ResourceGroup> groupsPerResourceForInsertion = allGroupsToAddPerResource.get(currResource.getUniqueName());
        			
        			if (groupsPerResourceForInsertion == null) {
        				log.debug("Current resource has no access groups to associate, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML AddRequest method 
        			else {
        				resourceGroupsOfCurrentResourceToPassToRequestForInsertion.addAll(groupsPerResourceForInsertion.values());
        			}
        			
        			
        			//create the ADD task
        			accountSpmlTaskPerResource = resourceOperationManager.createAddAccountRequestForUserTask(currResource, user, resourceGroupsOfCurrentResourceToPassToRequestForInsertion, rolesToAddPerResource.get(currResource.getResourceId()));
        			accountSpmlTaskPerResource.setBulkTask(bulkTask);
            		bulkTask.addTask(accountSpmlTaskPerResource);
        		}
        		//Otherwise account exists, then create a 'Modify' SPML Task
        		else {
        			Account accountEntity = user.getAccountOnTarget(currResource.getUniqueName());
        			
        			//just a sanity check
        			if (accountEntity == null) {
        				throw new OperationException("Couldnt find account on target system: " + currResource.getDisplayName() + ", for user: " + user.getName() + ", although expected one...(user.isUserHasAccount returned true! what is wrong here?");
        			}
        			
        			//If there are any new groups to reference to the modified account, then get them here
        			//TODO: Support verification whether account already has these groups assoicated or not
        			log.debug("User HAS an account, performing 'Modify Account' operation for account name '" + accountEntity.getName() + ", resource: '" + currResource.getDisplayName() + "'");
        			
        			Map<Long,ResourceGroup> groupsPerResourceForInsertion = allGroupsToAddPerResource.get(currResource.getUniqueName());
        			
        			if (groupsPerResourceForInsertion == null) {
        				log.debug("Current resource has no access groups to associate, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML modify method 
        			else {
        				resourceGroupsOfCurrentResourceToPassToRequestForInsertion.addAll(groupsPerResourceForInsertion.values());
        			}
        			
        			//If there are any groups references to remove from modified account, then get them here
        			Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
        			Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());
        			
        			if (groupsPerResourceForRemoval == null) {
        				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
        			}
        			//Otherwise add all groups to the list that is passed to the SPML modify method 
        			else {
        				resourceGroupsOfCurrentResourceToPassToRequestForRemoval.addAll(groupsPerResourceForRemoval.values());
        			}

        			//only create the task if there are any modifications
        			//Create an SPML task to perform account modifications
        			if (resourceGroupsOfCurrentResourceToPassToRequestForRemoval.isEmpty() &&  (resourceGroupsOfCurrentResourceToPassToRequestForInsertion.isEmpty())) {
        				log.debug("Skipping Modify account task creation for account name '" + accountEntity.getName() + "', of resource name '" + currResource.getDisplayName() + " since there were no access groups references modifications to perform...");
        			}
        			else {
        				accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(accountEntity, resourceGroupsOfCurrentResourceToPassToRequestForInsertion, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
        				accountSpmlTaskPerResource.setBulkTask(bulkTask);
                		bulkTask.addTask(accountSpmlTaskPerResource);
        			}
        		}
        	}
        	
          
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	
        	//Manage all tasks as a part of the REMOVE 'groups/targets' process
            log.debug("Iterating over the 'Global Resources to REMOVE' MAP with amount '" + allResourcesToRemove.size() + "', seeking accounts to delete");
            for (Resource currResource : allResourcesToRemove.values()) {
                log.info("Currently iterating over Resource with name: '" + currResource.getDisplayName() + "'");

                //try {
                    log.debug("Trying to find account on resource for user.");
                    Account account = user.getAccountOnTarget(currResource.getUniqueName());
                    
                    if (account == null) {
                    	log.warn("Could not find account for User '" + user.getName() + "', on Resource: '" + currResource.getDisplayName() + "', skipping resource...");
                        continue;
                    }

                    
                    log.info("Found account with name: '" + account.getName() + "', associated to user.");
                    //TODO: Should we support references removal when performing account delete or we can trust resources to do this job?


                    log.debug("Checking whether the account on the current iterated resource can be deleted or not...");
                    //Check whether the account can be deleted or not by checking whether it is protected by other user roles or not
                    if (!isAccountProtectedByUserRoles(account, rolesToAdd, userRolesToRemove)) {
                        log.debug("The account named: '" + account.getName() + "' on current iterated resource named: '" + currResource.getDisplayName() + "' is not protected by other roles, determining whether to delete account or not according to system configuration");
                        //ems.info("Account name: '" + account.getName()+"', on target system: '" + currTs.getDisplayName() +"' is not protected by any other roles attached to user, adding a task to delete the account");

                        //not protected, add a task to delete the account
                        Set<Role> excludeProtectedRoleList = new HashSet<Role>();
                        for (UserRole currExcludedUserRole : userRolesToRemove) {
                            excludeProtectedRoleList.add(currExcludedUserRole.getRole());
                        }
                        
                        
                        if (isDeleteAccountIfRoleHasLastResourceReference) {
                        	log.debug("configuration set to delete account, adding a task to delete the account!");
                        	//force, roles protection already verified here
                        	SpmlTask deleteAccountTask = resourceOperationManager.createDeleteAccountRequestTask(account, excludeProtectedRoleList, true);
                        	deleteAccountTask.setBulkTask(bulkTask);
                        	bulkTask.addTask(deleteAccountTask);
                        } else {
                        	log.debug("Nothing to do, configuration set not to delete account.");
                        }
                    } else {
                        log.debug("The account name: '" + account.getName() + "' on current iterated resource named: '" + currResource.getDisplayName() + "' is PROTECTED by other roles, skipping account removal!");
                        //perform the modify
                        Set<ResourceGroup> resourceGroupsOfCurrentResourceToPassToRequestForRemoval = new HashSet<ResourceGroup>();
            			Map<Long,ResourceGroup> groupsPerResourceForRemoval = allGroupsToRemovePerResource.get(currResource.getUniqueName());
            			
            			
            			if (groupsPerResourceForRemoval == null) {
            				log.debug("Current resource has no access groups to removal memberships from, creating an SPML 'Add Request' without references to access groups...");
            			}
            			//Otherwise add all groups to the list that is passed to the SPML modify method 
            			else {
            				resourceGroupsOfCurrentResourceToPassToRequestForRemoval.addAll(groupsPerResourceForRemoval.values());
            			}
            			
            			
                        SpmlTask accountSpmlTaskPerResource = resourceOperationManager.createModifyAccountRequestForUserTask(account, null, resourceGroupsOfCurrentResourceToPassToRequestForRemoval,null);
                        bulkTask.addTask(accountSpmlTaskPerResource);
                        
                        
                        //ems.info("Account name: '" + account.getName()+"', on target system: '" + currTs.getDisplayName() +"' is protected by other roles attached to user, won't delete the account.");
                        //Add the tasks to the bulk task
                        //for (Task currTask : groupsRemoveMembershipTasks.values()) {
                    }
                    /*
                } catch (TaskCreationException tce) {
                    throw new OperationException(tce.getMessage());
                }
                */
            }

            
            
            
            
            StringBuffer journalingDetails = new StringBuffer();
            journalingDetails.append("Added Roles: ");
            for (Role currRoleToAdd : rolesToAdd) {
            	//all tasks were successfully created, perform repository modifications
            	log.debug("Associating roles to user in repository...");
                UserRole userRole = new UserRole();
                userRole.setCreationDate(new Date());
                userRole.setRole(currRoleToAdd);
                userRole.setUser(user);
                userRole.setExpirationDate(currRoleToAdd.getExpirationDate());
                userRole.setInherited(currRoleToAdd.isInherited());
                userRole.setUser(user);
                
                //Instead of task, just persist the association
                //object references an unsaved transient instance - save the transient instance before flushing: velo.entity.UserRole
                //when used CASCADE.ALL over user.getUserRoles() it did not happen, but ofr some reason TWO userRoles were inserted into the DB
                
                journalingDetails.append(currRoleToAdd.getName() + ",");
                if (modifyEntities) {
                	log.debug("Persisting UserRole in repository...");
                	em.persist(userRole);
                } else {
                	log.debug("Won't persisting UserRole in repository, adding UserRole to User entity...");
                	//problematic, userRole will be detached
                	user.getUserRoles().add(userRole);
                }
                
                
                //user.getUserRoles().add(userRole);
            }
            
            
             
            log.debug("Remove Roles association from User in repository...");
            journalingDetails.append("Removed Roles: ");
            for (UserRole currUserRoleToRemove : userRolesToRemove) {
                boolean foundRoleInRolesToAdd = false;
                //If role was flagged to be added, then skip it.
                for (Role currRoleToAdd : rolesToAdd) {
                    if (currUserRoleToRemove.getRole().getName().toUpperCase().equals(currRoleToAdd.getName().toUpperCase())) {
                        foundRoleInRolesToAdd = true;
                        break;
                    }
                }

                if (!foundRoleInRolesToAdd) {
                	//Remove the userRole entity
                	//user.removeUserRole(currUserRoleToRemove);
                	//god knows why, but when removed, still the entity exists in DB
                	//When adding roles above this way, they get persisted.
                	
                	/*20-aug-07(asaf): Sucks does not work, god knows why!!!
                	 * WTF? REALLY, THE USER IS MANAGED, user.getUserRole() cascaded with 'REMOVE', but still hibernate does not invoke DELETE query on this object!
                	log.debug("CONTAINERS???????????" + em.contains(user));
                	log.debug("SIZE BEFORE: " + user.getUserRoles().size());
                	user.removeUserRole(currUserRoleToRemove);
                	log.debug("SIZE AFTER: " + user.getUserRoles().size());
                	em.merge(user);
                	log.debug("FLUSHING!!!");
                	em.flush();
                	log.debug("EO FLUSHING!!!");
                	*/
                	
                	if (modifyEntities) {
                		journalingDetails.append(currUserRoleToRemove.getRole().getName() + ",");
                		user.removeUserRole(currUserRoleToRemove);
                		em.remove(em.find(UserRole.class, currUserRoleToRemove.getUserRoleId()));
                		em.flush();
                	}
                	
                	//UserRole userRoleToBeRemoved = em.find(UserRole.class, currUserRoleToRemove.getUserRoleId());
                	//em.remove(userRoleToBeRemoved);
                }
            }
            log.debug("ENDED Preparing tasks to remove the User Roles association in repository...");
            
            if (modifyEntities) {
            	log.debug("A moment before merging user entity");
            	em.merge(user);
            }
            
            
            log.debug("Returning bulk tasks with task amount: '" + bulkTask.getTasks().size() + "'");

            return bulkTask;
    }
    
    
    @Deprecated
    public long revokeUserRole(UserRole userRole) throws OperationException {
    	HashSet<Role> userRolesToRevoke = new HashSet<Role>();
//    	userRolesToRevoke.add(userRole);
    	HashSet<Role> rolesToAdd = new HashSet<Role>();
    	return modifyRolesOfUser(userRolesToRevoke, rolesToAdd, userRole.getUser());
    }
    
    @Deprecated
	public long associateRoleToUserOld(Role role, User user, Date grantDate) throws OperationException {
		HashSet<Role> userRolesToRevoke = new HashSet<Role>();
		HashSet<Role> rolesToAdd = new HashSet<Role>();
		rolesToAdd.add(role);
		//BulkTask bt = modifyRolesOfUserTasks(userRolesToRevoke, rolesToAdd, user,true);
		BulkTask bt = modifyRolesOfUserTasks(userRolesToRevoke, rolesToAdd, user);
		
		
		if (bt.getTasks().size() > 0) {
			for (Task currTask : bt.getTasks()) {
				currTask.setExpectedExecutionDate(grantDate);
			}
			
    		return tm.persistBulkTask(bt);
    	}
    	else {
    		return 0;
    	}
	}
    
    
    @Deprecated
    public Role findRoleById(Long roleId) {
        try {
            System.out.println("Entity manager is: " + em);
            return (Role) em.createNamedQuery("role.findRoleById")
                .setHint("toplink.refresh", "true")
                .setParameter("roleId", roleId).getSingleResult();
            //return (Role) em.createQuery("SELECT object(role) FROM Role role WHERE role.name = qflowRole").getSingleResult();
        } catch (NoResultException e) {
            throw new NoResultException("No such result exception occured: Couldnt load Role for ID number: " + roleId);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    @Deprecated
    public UserRole findUserRole(User user, Role role) throws NoResultFoundException {
        try {
            return (UserRole) em.createNamedQuery("userRole.findByRoleAndUser")
                .setHint("toplink.refresh", "true")
                .setParameter("user", user)
                .setParameter("role", role)
                .getSingleResult();
        } catch (NoResultException e) {
            throw new NoResultException("No such result exception occured: Couldnt load UserRole for Role name: " + role.getName() + ", and User: " + user.getName());
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    @Deprecated
    public Role findRoleByName(String roleName) throws NoResultFoundException {
        try {
            return (Role) em.createNamedQuery("role.findRoleByName")
                .setHint("toplink.refresh", "true")
                .setParameter("roleName", roleName.trim().toUpperCase()).getSingleResult();
        } catch (Exception e) {
            throw new NoResultFoundException("Couldnt find Role for Role Name: " + roleName + " due to: " + e.getMessage());
        }
    }

    @Deprecated
    public void persistRolesFolder(RolesFolder rolesFolder) {
        em.persist(rolesFolder);
    }

    @Deprecated
    public void removeRole(Role role) {
        Role managedRole = em.merge(role);
        em.remove(managedRole);
    }


    @Deprecated
    public void deleteUserRoleEntity(UserRole userRole) {
    	log.info("Deleting UserRole entity of user: " + userRole.getUser().getName() + ", of role: " + userRole.getRole().getName());
        UserRole managedUserRole = em.merge(userRole);
        em.remove(managedUserRole);
    }

    @Deprecated
    public void removeRolesFolder(RolesFolder rolesFolder) {
    	log.info("Deleting RolesFolder named: '" + rolesFolder.getUniqueName() + "'");
        RolesFolder managedRolesFolder = em.merge(rolesFolder);
        em.remove(managedRolesFolder);
    }


    @Deprecated
    public void persistUserRoleEntity(UserRole userRole) throws PersistEntityException {
        //Sometimes this function is used through tasks asynchrony, make sure before persisting that another UserRole does not exist already
        if (um.isRoleAssignedToUser(userRole.getUser(), userRole.getRole())) {
            String msg = "Couldn't assign UserRole of role name: '" + userRole.getRole().getName() + "', to user: '" + userRole.getUser().getName() + "' since the user already has this role!";
            //Log the message to the event log
            //cum.addEventLog("ROLES", "FAILURE", "WARNING", "Failed to assign role to user", msg);
            log.warn(msg);
            throw new PersistEntityException(msg);
        }

        em.persist(userRole);
    }

    @Deprecated
    public void removeRole(Long roleId) {
        try {
            Role role = em.find(Role.class, roleId);
            em.remove(role);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public void updateRoleEntity(Role role) throws MergeEntityException {
        try {
            //Validate role before persisting
            role.validateRoleEntity();
            em.merge(role);
        } catch (ValidationException ve) {
            throw new MergeEntityException("Role name: '" + role.getName() + "' validation process was failed with message: " + ve.getMessage());
        }
    }

    @Deprecated
    public void updateRolesFolder(RolesFolder rolesFolder) {
        em.merge(rolesFolder);
    }

    @Deprecated
    public UserRole updateUserRole(UserRole userRole) {
        return em.merge(userRole);
    }

    @Deprecated
    public boolean isRoleExit(String roleName) {
        Query q = em.createNamedQuery("role.isExistsByName").setParameter("roleName", roleName.trim().toUpperCase());
        Long num = (Long) q.getSingleResult();

        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Deprecated
    public Set<Role> loadRolesByNames(Set<String> roleNames) throws LoadingObjectsException {
        Set<Role> roles = new HashSet<Role>();
        for (String currRoleName : roleNames) {
            Role currLoadedRole;
            try {
                currLoadedRole = findRoleByName(currRoleName);
                roles.add(currLoadedRole);
            } catch (NoResultFoundException nrfe) {
                throw new LoadingObjectsException(nrfe);
            }
        }

        return roles;
    }

    @Deprecated
    public Collection<BulkTask> addRolesByRolesNamesToUserBulkTaskList(List<String> rolesToAdd, User user, boolean isDirect) {
        List<BulkTask> bulkTaskList = new ArrayList<BulkTask>();
        for (String currRoleToAdd : rolesToAdd) {
            Role currRole;
            try {
                currRole = findRoleByName(currRoleToAdd);
//JB WTF?!                BulkTask currBulkTask = addRoleToUserBulkTask(currRole, user, isDirect);
//JB                bulkTaskList.add(currBulkTask);
            } /*JBcatch (AssigningRoleToUserException astue) {
                log.warn("Skipping role insertion named: '" + currRoleToAdd + "' to User: '" + user.getName() + "', due to: " + astue.getMessage());*/
            //} 
            catch (NoResultFoundException nrfe) {
                log.warn("Could not find role named: '" + currRoleToAdd + "', skipping role...");
            }
        }

        return bulkTaskList;
        //tm.persistBulkTasks(bulkTaskList);
    }



    @Deprecated
    public void addRoleToUser(Role roleToAdd, User user, boolean isDirect) throws AssigningRoleToUserException {
//JB        BulkTask bt = addRoleToUserBulkTask(roleToAdd, user, isDirect);
//JB        em.persist(bt);
    }


    


    
    
    
    
    
    
    
    //AAAAAAAAAAAAAAAAAAAAAAAAA
    /**
    Modify ResourceGroups associated with a certain Role.
    Algorithm:
    - Fetch all users that assigned to the modified role, per user do:
    - Fetch the corresponding UserRole entity for the modified Role for the iterated user.
    - Iterate over -Groups to be Removed-, per group do:
    - Make sure that the group really associated with the role
    - If yes, continue, otherwise throw an exception that the group is not associated with the role!
    - Make sure that the group is not added by other user roles.
    - If yes, then do not remove the group (add a message that it could not be deleted since other roles protects it)
    - IF no, then add a task to remove the group membership.
    - Iterate over -Groups to be added-, per group do:
    - Make sure that the group does not already exist in role, if so, skip it.
    - Make sure that the group is not added by other roles:
    - if it does, skip it. (Add a message that the group was not added since its already been added by another role)
    - If not, add a task to add the group membership to the corresponding User's account.
    @param role The role to be modified.
    @param groupsToAdd A collection of ResourceGroup entities to add.
    @param groupsToRemove A collection of ResourceGroup entities to remove.
     */
    @Deprecated
    public void modifyResourceGroupsInRole(Role role, Collection<ResourceGroup> groupsToAdd, Collection<ResourceGroup> groupsToRemove) throws ModifyResourceGroupsInRoleException {
        log.info("Starting Modify Groups in Role process, modifying role '" + role.getName() + "', size of groups to remove: *" + groupsToRemove.size() + "*, size of groups to add: *" + groupsToAdd.size() + "*");

        try {
            //Make sure the role is validated, otherwise throw an exception.
            role.validateRoleEntity();
        } catch (ValidationException ve) {
            //throw new ModifyResourceGroupsInRoleException("Cannot modify Target System Groups in Role name: '" + role.getName() + "', The role could not be validated, failure message: " + )
        }


        //Validate that the groups to be removed are really associated with the role.
        for (ResourceGroup currGroupToRemove : groupsToRemove) {
            //Throw an exception if the group to be removed is not associated with the role.
            if (!role.isResourceGroupAssociated(currGroupToRemove)) {
                String errMsg = "Group named '" + currGroupToRemove.getDisplayName() + "', of Target System: '" + currGroupToRemove.getResource().getDisplayName() + "' is not associated with the role '" + role.getName() + "'";
                log.warn(errMsg);
                throw new ModifyResourceGroupsInRoleException(errMsg);
            }
        }

        //Validate that the groups to be added are not associated already with the role entity.
        for (ResourceGroup currGroupToAdd : groupsToAdd) {
            if (role.isResourceGroupAssociated(currGroupToAdd)) {
                String msgAlreadyExistInRole = "Group name: '" + currGroupToAdd.getDisplayName() + "', of Target System: '" + currGroupToAdd.getResource().getDisplayName() + "' wont be added since it is already associated with the current modified role";
                log.warn(msgAlreadyExistInRole);
                throw new ModifyResourceGroupsInRoleException(msgAlreadyExistInRole);
            }
        }


        Collection<User> affectedUserList = um.findAllUsersAssignedToRole(role);
        //Make sure we have the required properties set
        BulkTask bulkTask = BulkTask.factory("Modifying access groups in Role named '" + role.getName() + "'");
        //TODO: Add a detailed message with all groups to remove/add names and their target system.
        bulkTask.addLog("INFO", "Modifying Groups in Role named: '" + role.getName() + "'", null);
        EdmMessages ems = new EdmMessages();

        
        
        //JB!!!
        affectedUserList.clear();
        
        log.info("Fetched affected users that are associated with this role #: *" + affectedUserList.size() + "*");
        //List<Task> groupsRemoveMembershipTasks = new ArrayList<Task>();
        for (User currUser : affectedUserList) {
            UserRole userRoleOfCurrentUser;

            try {
                userRoleOfCurrentUser = findUserRole(currUser, role);
            } catch (NoResultFoundException nrfe) {
                throw new ModifyResourceGroupsInRoleException(nrfe);
            }

            //REMOVE GROUPS
            for (ResourceGroup currGroupToRemove : groupsToRemove) {
                if (isResourceGroupProtectedByAnotherUserRole(currGroupToRemove, userRoleOfCurrentUser)) {
                    String msg = "Cannot remove group '" + currGroupToRemove.getDisplayName() + "' from User: '" + currUser.getName() + "' since the group was added by other Role(s) the User is associated with.";
                    log.info(msg);
                    ems.info(msg);
                } else {
                    try {
                        Account accountOfCurrentUser = am.findAccount(currGroupToRemove.getResource(), currUser);
                        log.info("Removing Group named '" + currGroupToRemove.getDisplayName() + "', of Target System: '" + currGroupToRemove.getResource().getDisplayName() + "' from Account named: '" + accountOfCurrentUser.getName() + "', of User: '" + currUser.getName() + "'");
                        //groupsRemoveMembershipTasks.add(am.removeGroupMembershipTask(currGroupToRemove,accountOfCurrentUser,bulkTask));
                        if (!currGroupToRemove.isDeletedInResource()) {
                            bulkTask.addTask(am.removeGroupMembershipTask(currGroupToRemove, accountOfCurrentUser, bulkTask));
                        } else {
                            log.info("Cannot remove Group named '" + currGroupToRemove.getDisplayName() + "', of Target System: '" + currGroupToRemove.getResource().getDisplayName() + "' from Account named: '" + accountOfCurrentUser.getName() + "', of User: '" + currUser.getName() + "', group is marked as deleted");
                        }
                        
                        if (accountOfCurrentUser == null) {
                        	//throw new ModifyResourceGroupsInRoleException(nrfe.getMessage());
                            //04-02-07-> If the user does not have an account for the Resource contains the current iterated group, then just drop a warning
                            String detailedMessage = "No account found for User: '" + currUser.getName() + "' on Resource: '" + currGroupToRemove.getResource().getDisplayName() + "' while trying to associate the group with the User's account.";
                            String summaryMessage = "No account found for User: '" + currUser.getName() + "' on Resource: '" + currGroupToRemove.getResource().getDisplayName();
                            bulkTask.addLog("WARNING", summaryMessage, detailedMessage);
                            continue;
                        }
                    } catch (TaskCreationException tce) {
                        throw new ModifyResourceGroupsInRoleException(tce.getMessage());
                    }
                }
            }



            //ADD GROUPS
            //List<Task> groupsAddMembershipTasks = new ArrayList<Task>();
            //Iterate over groups, per group make sure that it does not already exist in role, if its not, add a task to add it.
            for (ResourceGroup currGroupToAdd : groupsToAdd) {

                if (isResourceGroupAlreadyAddedByUserRole(currGroupToAdd, currUser)) {
                    String msgAlreadyAddedByUserRole = "Group name: '" + currGroupToAdd.getDisplayName() + "', of Target System: '" + currGroupToAdd.getResource().getDisplayName() + "' wont be added since it is already associated with other roles assigned to User: '" + currUser.getName() + "'";
                    ems.info(msgAlreadyAddedByUserRole);
                    log.info(msgAlreadyAddedByUserRole);
                } else {
                    try {
                        Account accountOfCurrentUser = am.findAccount(currGroupToAdd.getResource(), currUser);
                        log.info("Adding Group named '" + currGroupToAdd.getDisplayName() + "', of Target System: '" + currGroupToAdd.getResource().getDisplayName() + "' from Account named: '" + accountOfCurrentUser.getName() + "', of User: '" + currUser.getName() + "'");
                        //groupsAddMembershipTasks.add(am.addGroupMembershipTask(currGroupToAdd,accountOfCurrentUser.getName(),bulkTask));
                        if (currGroupToAdd.isDeletedInResource()) {
                            log.warn("Cannot add Group named '" + currGroupToAdd.getDisplayName() + "', of Target System: '" + currGroupToAdd.getResource().getDisplayName() + "' from Account named: '" + accountOfCurrentUser.getName() + "', of User: '" + currUser.getName() + "', group marked as deleted!");
                        } else {
                            bulkTask.addTask(am.addGroupMembershipTask(currGroupToAdd, accountOfCurrentUser.getName(), bulkTask));
                        }
                        
                        if (accountOfCurrentUser == null) {
                        	//throw new ModifyResourceGroupsInRoleException(nrfe.getMessage());
                            //04-02-07-> If the user does not have an account for the Resource contains the current iterated group, then just drop a warning
                            String detailedMessage = "No account found for User: '" + currUser.getName() + "' on Resource: '" + currGroupToAdd.getResource().getDisplayName() + "' while trying to associate the group with the User's account.";
                            String summaryMessage = "No account found for User: '" + currUser.getName() + "' on Resource: '" + currGroupToAdd.getResource().getDisplayName();
                            bulkTask.addLog("WARNING", summaryMessage, detailedMessage);
                            continue;
                        }
                    } catch (TaskCreationException tce) {
                        throw new ModifyResourceGroupsInRoleException(tce.getMessage());
                    }
                }
            }
        }




        //Handle Add/Remove group assocaition from Role Entity
        for (ResourceGroup currGroupToRemove : groupsToRemove) {
            //Make sure that the role contains the Group Object.
            //try {
                ResourceGroup groupObjectToBeRemoved = role.getResourceGroup(currGroupToRemove.getUniqueId());
                role.getResourceGroups().remove(groupObjectToBeRemoved);
            //} catch (NoResultFoundException nrfe) {
              //  throw new ModifyResourceGroupsInRoleException("Cannot remove association of group '" + currGroupToRemove.getDisplayName() + "' since the ResourceGroup entity is not exists in Role.getResourceGroup() List.");
            //}
        }

        
        for (ResourceGroup currGroupToAdd : groupsToAdd) {
        	//this already associate in DB, although role is not in 'managed' state, not sure why hibernate do that :-q
        	role.getResourceGroups().add(currGroupToAdd);
        }

        
        
//commented out since when moved to JB+HB role's associations gets updated automatically and
//re-calling em.merge(role) creates duplications, not sure why although role is not in 'managed' state
//invoking role.getResourceGroups().add(currGroupToAdd) persist the association in the DB
  //      try {
            //Everything successfully passed, merge the Role with the new associations.
            log.info("Updating role entity with new associations...");
//            updateRoleEntity(role);

            //If there were tasks added to bulk task, then persist the bulk task to the DB.
            if (bulkTask.getTasks().size() > 0) {
                log.info("Persisting bulkTask ID: " + bulkTask.getBulkTaskId());
                tm.persistBulkTask(bulkTask);
            }
/*        } catch (MergeEntityException mee) {
            em.clear();
            throw new ModifyResourceGroupsInRoleException("Role could not be validated during ResourceGroups in Role modifications for Role named: '" + role.getName() + "', request was cancelled!");
        }*/
    }


    /*
    public Collection<ResourceActionInterface> factoryResourceActionsForRoleToAdd (
    Role role, User user) throws FactoryResourceActionsForRoleException {
    if (role.getResources().size() == 0) {
    throw new FactoryResourceActionsForRoleException ("Cannot factory actions for role since the role is empty, role name: " + role.getName());
    }
    ActionManager am = new ActionManager();
    Iterator<Resource> tsi = role.getResources().iterator();
    Collection<ResourceActionInterface> roleActions = new ArrayList();
    while (tsi.hasNext()) {
    Resource currResource = tsi.next();
    try {
    //Add the factored create account action to the role action list
    System.out.println("Adding factored action to role name: "
    + role.getName() + ", for target system: "
    + currResource.getDisplayName());
    roleActions.add(am.factoryCreateAccountAction(currResource,
    user));
    } catch (ScriptLoadingException sle) {
    System.out
    .println("Couldnt factory 'create account' action for role name: "
    + role.getName()
    + ", action's script was not found for target system: "
    + currResource.getDisplayName()
    + ", exiting with message: " + sle.getMessage());
    }
    }
    return roleActions;
    /*
    String scriptResourceName = "d:/velo_workspace/targets_scripts/qflow/actions/qflow_create_account.groovy";
    ScriptFactory sf = new ScriptFactory();
    try {
    Object scriptObj = sf.factoryScriptableObjectByResourceName(scriptResourceName);
    System.out.println("haha: " + scriptObj);
    Collection<ResourceActionInterface> roleActions = new ArrayList();
    roleActions.add((ResourceActionInterface)scriptObj);
    return roleActions;
    }
    catch (ScriptLoadingException sle) {
    sle.printStackTrace();
    }
    return null;
     */
    //	}


    @Deprecated
    public Collection<ResourceActionInterface> factoryResourceActionsForRoleRomoval(Role role, User user) throws FactoryResourceActionsForRoleException {
        if (role.getResources().size() == 0) {
            System.out.println("Warning, cannot factory actions for role since the role is empty, role name: " + role.getName());
        }

        ActionManager am = new ActionManager();
        Iterator<Resource> tsi = role.getResources().iterator();

        Collection<ResourceActionInterface> roleActions = new ArrayList();
        //Iterate over the target systems set within the role
        while (tsi.hasNext()) {
            Resource currResource = tsi.next();
            try {
                //Get a list of accounts for this target system relevant to the specified user
                Iterator<Account> accountsPerTsIterator = um.getAccountsForResourcePerUser(currResource, user).iterator();

                while (accountsPerTsIterator.hasNext()) {
                    Account currAcc = accountsPerTsIterator.next();

                    DeleteAccountActionFactory daaf = new DeleteAccountActionFactory(currAcc);
                    //Add the factored delete account action to the role action list
                    roleActions.add(daaf.factory());
                }
            } catch (ActionFactoryException afe) {
                System.out.println("Couldnt factory 'remove account' action for role name: " + role.getName() + ", action for target system: " + currResource.getDisplayName() + " failed, failure message: " + afe.getMessage());
            }
        }

        return roleActions;
    }


    //should be available in the role entity itself
    @Deprecated
    public boolean isResourceAssignedToRole(Role role, Resource resource) {
        for (Resource currTs : role.getResources()) {
            if (currTs.getResourceId() == resource.getResourceId()) {
                return true;
            }
        }

        return false;
    }


    @Deprecated
    public boolean isResourceGroupAlreadyAddedByUserRole(ResourceGroup tsg, User user) {
        log.debug("Checking whether target system group name: " + tsg.getDescription() + " already added by another user role or not...");
        Query q = em.createNamedQuery("userRole.isResourceGroupWasAddedByAnotherUserRole").setParameter("user", user).setParameter("tsg", tsg);

        Long num = (Long) q.getSingleResult();
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Deprecated
    public boolean isResourceGroupAlreadyAddedByUserRoles(Set<UserRole> userRoles, ResourceGroup tsg, User user) {
        /*
        if (isResourceGroupAlreadyAddedByUserRole(tsg, user)) {
        return true;
        } else {
        /*
        for (UserRole currUserRole : userRoles) {
        if (currUserRole.getRole().isGroupExistsInRole(tsg)) {
        return true;
        }
        }
        return false;
        }
         */

        for (UserRole currUserRole : user.getUserRoles()) {
            if (currUserRole.getRole().isResourceGroupAssociated(tsg)) {
                return true;
            }
        }

        return false;
    }


    //NOT NEEDED ANYMORE, ALSO, QUERIES THROUGH DB IN LOOPS ARE VERY SLOW.
    //BELOW IS THE ALTERNATIVE
    @Deprecated
    public boolean isResourceGroupProtectedByAnotherUserRole(ResourceGroup tsg, UserRole userRole) {
        log.info("Checking whether target system group name: '" + tsg.getDescription() + "', on Resource '" + tsg.getResource().getDisplayName() + "'" + " is protected by other User Roles except: User Role ID: '" + userRole.getUserRoleId() + "', that represents Role: '" + userRole.getRole().getName() + "', that was added to User: '" + userRole.getUser().getName() + "'");

        Query q = em.createNamedQuery("userRole.isResourceGroupProtectedByAnotherUserRole").setParameter("userRole", userRole).setParameter("tsg", tsg).setParameter("user", userRole.getUser());

        Long num = (Long) q.getSingleResult();

        log.info("Resulted: '" + num + "' of UserRole(s) that protect the group.");

        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }

    
    //see below the one that supports positions as well
    @Deprecated
    public boolean isResourceGroupProtectedByOtherUserRoles(ResourceGroup rg, Set<UserRole> excludedUserRoles, User user) {
        log.debug("Checking whether group with unique ID '" + rg.getUniqueId() + "', on Resource: '" + rg.getResource().getDisplayName() + "' for User: '" + user.getName() + "' is protected by user roles (except the specified excluded User Roles)");

        for (UserRole currUserRole : user.getUserRoles()) {
            boolean isRoleExcluded = false;
            //Make sure the current user role is not in the exclude list
            for (UserRole currExcludedUserRole : excludedUserRoles) {
                //Found role as excluded, set the flag and break the loop
                if (currUserRole.equals(currExcludedUserRole)) {
                    log.debug("UserRole ID: '" + currExcludedUserRole.getUserRoleId() + "', of Role named: '" + currExcludedUserRole.getRole().getName() + "' is excluded, skipping current iterated user role...");
                    isRoleExcluded = true;
                    break;
                }
            }

            //It is not an excluded role, perform the group verification
            if (!isRoleExcluded) {
                log.debug("UserRole ID: '" + currUserRole.getUserRoleId() + "', of Role named: '" + currUserRole.getRole().getName() + "' is not excluded, validating whether the TSG is protected by this role or not.");
                if (currUserRole.getRole().isResourceGroupAssociated(rg)) {
                    log.debug("Group is protected, returning true!");
                    return true;
                } else {
                    log.debug("Group is not protected, continuing to the next User Role validation...");
                }
            }
        }

        return false;
    }
    
    

    //see instead "user.isRolesReferencesResource" 
    @Deprecated
    public boolean isAccountProtectedByUserRoles(Account account, Set<Role> includedRoles, Set<UserRole> excludedUserRoles) {
        Map<Long, Resource> allTargetsInAllUserRoles = getAllTargetsInUserRoles(account.getUser().getUserRoles(), includedRoles, excludedUserRoles);

        if (allTargetsInAllUserRoles.containsKey(account.getResource().getResourceId())) {
            return true;
        } else {
            return false;
        }
    }

    //OLd, remove.
    @Deprecated
    public boolean isAccountProtectedByAnotherUserRole(UserRole userRole, Resource resource) {
        log.debug("Checking whether account is protected by another user role or not...");
        Query q = em.createNamedQuery("userRole.isAccountProtectedByAnotherUserRole").setParameter("userRole", userRole).setParameter("resource", resource).setParameter("user", userRole.getUser());

        Long num = (Long) q.getSingleResult();
        log.debug("The number of UserRoles that protects the specified User/Target are: '" + num + "'");
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Deprecated
    public Collection<UserRole> getUserRolesProtectAnAccount(Account account) {
        log.debug("Retrieving user roles that protects the specified account...");

        Query q = em.createNamedQuery("userRole.findUserRolesThatProtectAccount").setParameter("resource", account.getResource()).setParameter("user", account.getUser());

        Collection<UserRole> userRoles = q.getResultList();
        log.debug("The number of User Roles that protects account named '" + account.getName() + "', on Target: '" + account.getResource().getDisplayName() + "', of User: '" + account.getUser().getName() + "' is: '" + userRoles.size() + "'");

        return userRoles;
    }

    @Deprecated
    public boolean isRoleUsedByUserRoles(Role role) {
        Query q = em.createNamedQuery("userRole.isRoleInUserByUserRoles").setParameter("role", role);

        Long num = (Long) q.getSingleResult();
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }


    /*
    public Collection<Account> getAccountsToRovokeByUserRole(UserRole userRole) {
    Collection<Account> accounts = new HashSet<Account>();
    for (Resource ts : userRole.getRole().getResources()) {
    try {
    Account acc = am.findAccountOnResourceForUser(ts,userRole.getUser());
    accounts.add(acc);
    }
    catch (NoResultFoundException nrfe) {
    continue;
    }
    }
    return accounts;
    }
    public Collection<ResourceGroup> getResourceGroupsToRevokeByUserRole(UserRole userRole) {
    Collection<ResourceGroup> groups = new HashSet<ResourceGroup>();
    for (ResourceGroup currTsg : userRole.getRole().getResourceGroups()) {
    //Make sure that the current group is not used by any other UserRoles, if not, add it to the 'delete list', skip it otherwise.
    if (!isResourceGroupAlreadyAddedByAnotherUserRole(currTsg,userRole.getUser())) {
    groups.add(currTsg);
    }
    else {
    log.info("Skipping deleting ResourceGroup: " + currTsg.getDisplayName() + ", while revoking Role for User: " + userRole.getUser().getName() + ", role Name: " + userRole.getRole().getName() + ", since the group it is used by other UserRole");
    }
    }
    return groups;
    }
     */

    @Deprecated
    public Map<Long, Resource> getAllTargetsInUserRoles(Set<UserRole> includedUserRoles, Set<Role> includedRoles, Set<UserRole> excludedUserRoles) {
        HashMap<Long, Resource> allTargetsInAllRoles = new HashMap<Long, Resource>();
        HashMap<Long, Role> allIncludedRoles = new HashMap<Long, Role>();

        for (Role currRole : includedRoles) {
            if (!allIncludedRoles.containsKey(currRole.getRoleId())) {
                allIncludedRoles.put(currRole.getRoleId(), currRole);
            }
        }

        for (UserRole currUserRole : includedUserRoles) {
            boolean isUserRoleExcluded = false;
            for (UserRole currExcludedUserRole : excludedUserRoles) {
                if (currUserRole.equals(currExcludedUserRole)) {
                    isUserRoleExcluded = true;
                    break;
                }
            }

            if (!isUserRoleExcluded) {
                if (!allIncludedRoles.containsKey(currUserRole.getRole().getRoleId())) {
                    allIncludedRoles.put(currUserRole.getRole().getRoleId(), currUserRole.getRole());
                }
            }
        }


        for (Role currRole : allIncludedRoles.values()) {
            for (Resource currTSInRole : currRole.getResources()) {
                if (!allTargetsInAllRoles.containsKey(currTSInRole.getResourceId())) {
                    allTargetsInAllRoles.put(currTSInRole.getResourceId(), currTSInRole);
                }
            }
        }

        return allTargetsInAllRoles;
    }


    @Deprecated
    public Collection<RolesFolder> loadRolesFolders(boolean onlyActive) {
        if (onlyActive) {
            return em.createNamedQuery("rolesFolder.findAllActive").setHint("toplink.refresh", "true").getResultList();
        } else {
            return em.createNamedQuery("rolesFolder.findAll").setHint("toplink.refresh", "true").getResultList();
        }
    }

    @Deprecated
    public void associateGroupToRole(String groupUniqueId, String groupTargetName, String roleName) throws OperationException {
        java.lang.String errMsg = "Could not associate group with Unique ID \'" + groupUniqueId + "\', on Target: \'" + groupTargetName + "\', to Role: \'" + roleName + "\' due to: ";

        try {
            velo.entity.Resource loadedTs = tsm.findResourceByName(groupTargetName);

            if (!tsgm.isGroupExistOnTarget(groupUniqueId, loadedTs)) {
                throw new velo.exceptions.OperationException(errMsg + " The specified group does not exist!");
            }
            if (!isRoleExit(roleName)) {
                throw new velo.exceptions.OperationException(errMsg + " The specified role does not exist!");
            }
            velo.entity.Role loadedRole = findRoleByName(roleName);
            velo.entity.ResourceGroup loadedTsg = tsgm.findGroupByUniqueId(groupUniqueId, loadedTs);

            if (loadedRole.isResourceGroupAssociated(loadedTsg)) {
                throw new velo.exceptions.OperationException(errMsg + " The specified group already associated to role!");
            } else {
                loadedRole.getResourceGroups().add(loadedTsg);
                updateRoleEntity(loadedRole);
            }
        } catch (MergeEntityException ex) {
            throw new OperationException(errMsg + ex.getMessage());
        } catch (NoResultFoundException ex) {
            throw new OperationException(errMsg + ex.getMessage());
        }
/*
        catch (NonUniqueResultException ex) {
        throw new OperationException("Could not associate account named '" + accountName + "', on Target name '" + targetUniqueName + "', to User '" + userName + "' since more than one account exist on target!");
        }
        catch (NoResultFoundException ex) {
        throw new OperationException("Could not associate account named '" + accountName + "', on Target name '" + targetUniqueName + "', to User '" + userName + "' since the account does not exist!");
        }
         */
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
  //helper
	private void invokePreModifyUserRolesEvent(User user, Set<Role> rolesToAdd, Set<Role> rolesToRemove) throws ScriptInvocationException {
		EventDefinition ed = eventManager.find(EVENT_PRE_MODIFY_USER_ROLES);
		//make sure that the event was found, otherwise throw an exception
		if (ed == null) {
			log.error("Could not find event definition '" + EVENT_PRE_MODIFY_USER_ROLES + "', skipping event response invocations...");
			return;
		}
		OperationContext context = new OperationContext();
		context.addVar("user", user);
		context.addVar("userName", user.getName());
		context.addVar("rolesToAdd", rolesToAdd);
		context.addVar("rolesToRemove", rolesToRemove);
		context.addVar("tools", new GenericTools());
		
		//eventManager.invokeEventDefinitionResponses(ed, context);
		eventManager.invokeEvent(ed, context);
	}
    
    
    
    
    
}
