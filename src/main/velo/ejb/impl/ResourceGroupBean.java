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
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerRemote;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceGroupMember;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;

/**
 A Stateless EJB bean for ResourceGroupManager
 
 @author Asaf Shakarchi
 */
//@EJB(name="resourceGroupBean", beanInterface=ResourceGroupManagerLocal.class)
@Stateless
public class ResourceGroupBean implements ResourceGroupManagerLocal, ResourceGroupManagerRemote {
    
    /**
     Injected entity manager
     */
    @PersistenceContext
    public EntityManager em;
    
    private static Logger log = Logger.getLogger(ResourceGroupBean.class.getName());
    
    
    public boolean isGroupExists(String groupName,Resource resource) {
		log.debug("Checking whether group name: '" + groupName
				+ "' On resource name: '" + resource.getDisplayName()
				+ "' exist or not...");

		
		Query q = null;
		if (resource.isCaseSensitive()) {
			 q = em.createNamedQuery(
				"resourceGroup.isExistWithCase").setParameter("uniqueId",
						groupName).setParameter("resourceUniqueName",resource.getUniqueName());
		} else {
			q = em.createNamedQuery(
			"resourceGroup.isExistIgnoreCase").setParameter("uniqueId",
					groupName.toUpperCase()).setParameter("resourceUniqueName",resource.getUniqueName());
		}

		
		Long num = (Long) q.getSingleResult();

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	} 
    
    public ResourceGroup findGroup(String groupUniqueId, Resource resource) {
		try {
			log.trace("Finding Group in repository for name '" + groupUniqueId + "', in resource name '" + resource.getDisplayName() + "'");
			Query q = null;
			
			if (resource.isCaseSensitive()) {
				q = em.createNamedQuery("resourceGroup.findByUniqueIdWithCase").setParameter("uniqueId", groupUniqueId).setParameter("resourceUniqueName", resource.getUniqueName());
			} else {
				q = em.createNamedQuery("resourceGroup.findByUniqueIdIgnoreCase").setParameter("uniqueId", groupUniqueId.toUpperCase()).setParameter("resourceUniqueName", resource.getUniqueName());
			}
			
			return (ResourceGroup) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.info("FindAccount did not result any account name '" + groupUniqueId + "' on resource name '" + resource.getDisplayName() + "', returning null.");
			return null;
		} catch (NonUniqueResultException nure) {
			log.warn("FindAccount found multiple accounts with name '" + groupUniqueId + "' on resource name '" + resource.getDisplayName() + "', returning null.");
			return null;
		}
	}
    
    public void persistGroup(ResourceGroup group) {
		log.debug("Persisting group '" + group.getUniqueId() + "', of resource '" + group.getResource().getDisplayName() + "'");
		
		//make sure account does not exist already in repository
		if (isGroupExists(group.getUniqueId(), group.getResource())) {
			log.warn("Won't persist, group with uniqueId '" + group.getUniqueId() + "' on resource '" + group.getResource().getDisplayName() + "' already exists in repository!");
			return;
		}
		
		em.persist(group);
		
		
		log.debug("Successfully persisted group!");
	}
    
    public void removeGroup(ResourceGroup rg) {
        log.info("Removing group name '" + rg.getDisplayName() + "', from resource: '" + rg.getResource().getDisplayName() + "' started, determining what to do...");
        
        //If group was not marked yet as deleted, then add the first time group was deleted to current date.
        if (rg.isDeletedInResource()) {
        	rg.setFirstTimeGroupBeingDeletedInResource(new Date());
        }
        
        rg.setNumberOfReconcilesGroupKeepsBeingDeletedInResource(rg.getNumberOfReconcilesGroupKeepsBeingDeletedInResource()+1);
        rg.setDeletedInResource(true);
        
        //TODO: SHOULD NOT BE IN THE RECONCILE POLICY as this is a general resource group configuration
        if (rg.getResource().getReconcilePolicy().isDeleteGroupAfterReconcileProcessesNumberExceeded()) {
            //Check if group exceeded its limit
            if (rg.getNumberOfReconcilesGroupKeepsBeingDeletedInResource() >= rg.getResource().getReconcilePolicy().getReconcilesGroupKeepsBeingDeletedBeforeRemoveGroup()) {
                log.info("Group exceeded the limit of reconcile processes group flagged as deleted in target which is '" + rg.getResource().getReconcilePolicy().getReconcilesGroupKeepsBeingDeletedBeforeRemoveGroup() + "' times, deleting group from repository!");
                removeGroupEntityFromRepository(rg);
            } else {
            	updateGroup(rg);
            }
        } else {
            log.info("Not deleting group entity, only flagging group as deleted and incrementing reconcile group being deleted counter");
            updateGroup(rg);
        }
    }
   
    public void persistMember(ResourceGroupMember groupMember) {
    	log.trace("Persisting group member for group '" + groupMember.getResourceGroup().getUniqueId() + "', of resource '" + groupMember.getResourceGroup().getResource().getDisplayName() + "'");
		em.persist(groupMember);
    }
    
    public void removeGroupMember(ResourceGroupMember groupMember) {
        log.info("Removing member name '" + groupMember.getAccount().getName() + "', from group: '" + groupMember.getResourceGroup().getUniqueId() + "'");
        
        em.remove(groupMember);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //used by reconcile
    @Deprecated
    public void persistGroups(Collection<ResourceGroup> groupsToPersist) {
        for (ResourceGroup currTsg : groupsToPersist) {
            persistGroup(currTsg);
        }
        
        em.flush();
    }
    
    //used by reconcile
    @Deprecated
    public void removeGroups(Collection<ResourceGroup> groupsToRemove) {
        for (ResourceGroup currTsg : groupsToRemove) {
            removeGroup(currTsg);
        }
        
        em.flush();
    }
    
    @Deprecated
    public void mergeGroups(Collection<ResourceGroup> groupsToMerge) {
    	for (ResourceGroup currRG : groupsToMerge) {
    		em.merge(currRG);
    	}
    }
    
    public List<ResourceGroup> findResourceGroupsInRepository(List<String> groupsUniqueId, Resource resource) {
    	log.trace("loading relevant groups from repository based on the specified list of groups unique ids for a certain resource with amount: '" + groupsUniqueId.size() + "'");
    	
    	
    	//if the specified group list is empty, then return an empty list
    	if (groupsUniqueId.size() == 0) {
    		return new ArrayList<ResourceGroup>();
    	}
    	
    	StringBuffer query = new StringBuffer();
    	query.append("SELECT rg from ResourceGroup rg WHERE rg.resource = :resource AND ");
    	
    	int looper=0;
    	for (String currGroupUniqueId : groupsUniqueId) {
    		if ( (looper > 0) && (looper < groupsUniqueId.size()) )
    		//if (looper < groupsUniqueId.size())
    				query.append(" OR ");
    		
    		query.append("(rg.uniqueId = '" + currGroupUniqueId + "')");
    		looper++;
    	}
    	
    	log.trace("Generated query to fetch the specified groups: " + query);
    	
    	return em.createQuery(query.toString()).setParameter("resource", resource).getResultList();
    }






    
    
    //private, internal
    private void removeGroupEntityFromRepository(ResourceGroup rg) {
        log.debug("Removing group entity with name " + rg.getDisplayName() + ", on resource: " + rg.getResource().getDisplayName() + " from repository");
        
        // Merge first, cannot remove detached entity
        //ResourceGroup mergedTsg = em.merge(rg);
        //long groupId = mergedTsg.getResourceGroupId();
        //TODO: Remove association from roles/accounts too!
        em.remove(rg);
        
        //TODO: Specify some other better places constants such as table names
        //Remove MANY2MANY associations!
        log.debug("Deleting resource group associations...");
        em.createNativeQuery("DELETE FROM VL_RESOURCE_GROUPS_TO_ROLES where RESOURCE_GROUP_ID = " + rg.getResourceGroupId()).executeUpdate();
        //em.createNativeQuery("DELETE FROM VL_ACCOUNTS_TO_TS_GRP where GROUP_ID = " + groupId).executeUpdate();
    }
    
    public void updateGroup(ResourceGroup group) {
        log.debug("Updating group unique id " + group.getUniqueId());
        
        em.merge(group);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    public ResourceGroup findGroupById(long groupId, boolean eagerly) {
        try {
            ResourceGroup tsg = (ResourceGroup) em.createNamedQuery("resourceGroup.findById")
                .setHint("toplink.refresh", "true")
                .setParameter("resourceGroupId", groupId).getSingleResult();
            
            if (eagerly) {
                tsg.getMembers().size();
            }
            
            return tsg;
        } catch (NoResultException e) {
            throw new NoResultException(
                "No such result exception occured: Couldnt load Group for ID number: "
                + groupId);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public ResourceGroup findGroupByUniqueId(String groupUniqueId, Resource resource) throws NoResultFoundException {
        try {
            return (ResourceGroup) em.createNamedQuery("resourceGroup.findByUniqueId")
                .setHint("toplink.refresh", "true")
                .setParameter("uniqueId", groupUniqueId)
                .setParameter("resource", resource)
                .getSingleResult();
        } catch (NoResultException e) {
            throw new NoResultFoundException(
                "No such result exception occured: Couldnt load Group for Unique Identifier: "
                + groupUniqueId + ", on target '" + resource.getDisplayName() + "'");
        }
    }
    
    @Deprecated
    public Set<ResourceGroup> findGroupsOnResourceForUser(Resource ts, User user) {
        return null;
    }
    
    @Deprecated
    public List<ResourceGroup> loadAllGroups(Resource ts) {
        return em.createNamedQuery("resourceGroup.findAllByResource")
            .setParameter("resource", ts)
            .getResultList();
    }
    
    @Deprecated
    public ResourceGroup findGroupByDisplayName(String displayName, Resource resource) throws NoResultFoundException {
        try {
            return (ResourceGroup) em.createNamedQuery(
                "resourceGroup.findByDisplayName")
                .setHint("toplink.refresh", "true")
                .setParameter("displayName",
                displayName).setParameter("resource",
                resource).getSingleResult();
        } catch (NoResultException e) {
            throw new NoResultFoundException(
                "Couldnt find ResourceGroup for group name: "
                + displayName
                + ", for Target System name: "
                + resource.getDisplayName());
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public Collection<ResourceGroup> findGroupsByStringForCertainTarget(String searchString, Resource resource) {
        return em.createNamedQuery("resourceGroup.findByString")
            .setHint("toplink.refresh", "true")
            .setParameter("searchString",searchString)
            .setParameter("resource",resource)
            .getResultList();
    }
    
    
    
    
//    public void addMemberToGroup(ResourceGroup tsg, Account account) {
//        ResourceGroup tsgLoaded = em.merge(tsg);
//        
//        if (tsgLoaded.isAccountMember(account)) {
//            log.warn("Could not add account membership of account named: '" + account.getName() +"' to group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "', account is already a member of that group!");
//        } else {
//            log.warn("Successfully added add account membership of account named: '" + account.getName() +"' to group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "'");
//            tsgLoaded.getMembers().add(account);
//            updateGroupEntity(tsg);
//        }
//    }
    
//    public void removeMemberFromGroup(ResourceGroup tsg, Account account) {
//        ResourceGroup tsgLoaded = em.merge(tsg);
//        
//        boolean isFound = false;
//        for (Account currAccount : tsgLoaded.getMembers()) {
//            if (currAccount.equals(account)) {
//                tsgLoaded.getMembers().remove(currAccount);
//                isFound = true;
//                break;
//            }
//        }
//        
//        if (!isFound) {
//            log.warn("Could not remove account membership of account named: '" + account.getName() +"' from group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "', account was not found as a member of this group!");
//        } else {
//            updateGroupEntity(tsg);
//            log.debug("Succesfully removed account membership of account named: '" + account.getName() +"' from group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "'");
//        }
//    }
    
    

    @Deprecated
    public void setGroupAsActive(ResourceGroup tsg) {
        if (tsg.isDeletedInResource()) {
            tsg.setDeletedInResource(false);
        }
        updateGroup(tsg);
    }
    
}
