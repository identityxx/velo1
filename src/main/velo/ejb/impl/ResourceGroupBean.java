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
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Destroy;

import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerRemote;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;

/**
 A Stateless EJB bean for ResourceGroupManager
 
 @author Asaf Shakarchi
 */
@EJB(name="resourceGroupBean", beanInterface=ResourceGroupManagerLocal.class)
@Stateless
public class ResourceGroupBean implements ResourceGroupManagerLocal, ResourceGroupManagerRemote {
    
    /**
     Injected entity manager
     */
    @PersistenceContext
    public EntityManager em;
    
    private static Logger log = Logger.getLogger(ResourceGroupBean.class.getName());
    
    
    
    public void removeGroup(ResourceGroup rg) {
        log.info("Removing group name '" + rg.getDisplayName() + "', on target: '" + rg.getResource().getDisplayName() + "' started, determining what to do...");
        
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
                updateGroupEntity(rg);
            }
        } else {
            log.info("Not deleting group entity, only flagging group as deleted and incrementing reconcile group being deleted counter");
            updateGroupEntity(rg);
        }
    }
    
    public void persistGroup(ResourceGroup rg) {
        log.info("Persisting a group name '" + rg.getDisplayName() + "', on target: '" + rg.getResource() + "' started, determining what to do...");
        
        /*
        if (isGroupExistOnTarget(rg.getUniqueId(), rg.getResource())) {
            log.info("Group already exists!, making sure group is not flagged as deleted, and reset reconciles group being deleted counter");
            if (rg.isDeletedInResource()) {
            	rg.setDeletedInResource(false);
            	rg.setNumberOfReconcilesGroupKeepsBeingDeletedInResource(0);
            	rg.setFirstTimeGroupBeingDeletedInResource(null);
            }
        } else {
            log.info("Group does not exists in repository, persisting group...");
            persistGroupEntityInRepository(rg);
        }
        */
        persistGroupEntityInRepository(rg);
    }
    
    
    //used by reconcile
    public void persistGroups(Collection<ResourceGroup> groupsToPersist) {
        for (ResourceGroup currTsg : groupsToPersist) {
            persistGroup(currTsg);
        }
        
        em.flush();
    }
    
    //used by reconcile
    public void removeGroups(Collection<ResourceGroup> groupsToRemove) {
        for (ResourceGroup currTsg : groupsToRemove) {
            removeGroup(currTsg);
        }
        
        em.flush();
    }
    
    
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
        log.info("Removing group entity with name " + rg.getDisplayName() + ", on resource: " + rg.getResource().getDisplayName() + " from repository");
        
        // Merge first, cannot remove detached entity
        ResourceGroup mergedTsg = em.merge(rg);
        long groupId = mergedTsg.getResourceGroupId();
        //TODO: Remove association from roles/accounts too!
        em.remove(mergedTsg);
        
        //TODO: Specify some other better places constants such as table names
        //Remove MANY2MANY associations!
        log.debug("Deleting resource group associations...");
        em.createNativeQuery("DELETE FROM VL_RESOURCE_GROUPS_TO_ROLES where RESOURCE_GROUP_ID = " + groupId).executeUpdate();
        //em.createNativeQuery("DELETE FROM VL_ACCOUNTS_TO_TS_GRP where GROUP_ID = " + groupId).executeUpdate();
    }
    
    
    private void persistGroupEntityInRepository(ResourceGroup rg) {
    	em.persist(rg);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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
    
    public Set<ResourceGroup> findGroupsOnResourceForUser(Resource ts, User user) {
        return null;
    }
    
    public List<ResourceGroup> loadAllGroups(Resource ts) {
        return em.createNamedQuery("resourceGroup.findAllByResource")
            .setParameter("resource", ts)
            .getResultList();
    }
    
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
    
    
    public Collection<ResourceGroup> findGroupsByStringForCertainTarget(String searchString, Resource resource) {
        return em.createNamedQuery("resourceGroup.findByString")
            .setHint("toplink.refresh", "true")
            .setParameter("searchString",searchString)
            .setParameter("resource",resource)
            .getResultList();
    }
    
    
    public boolean isGroupExistOnTarget(String uniqueId, Resource resource) {
        log.debug("Checking whether group unique id: '" + uniqueId
            + "' exist on Resource Name: '"
            + resource.getDisplayName() + "' or not...");
        Query q = em.createNamedQuery("resourceGroup.isGroupExistOnTarget")
            .setParameter("uniqueId", uniqueId).setParameter("resource",
            resource);
        
        Long num = (Long) q.getSingleResult();
        
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    
    
    public void updateGroupEntity(ResourceGroup tsg) {
        log.info("Updating group name " + tsg.getDisplayName());
        
        em.merge(tsg);
    }
    
    
    
    public void addMemberToGroup(ResourceGroup tsg, Account account) {
        ResourceGroup tsgLoaded = em.merge(tsg);
        
        if (tsgLoaded.isAccountMember(account)) {
            log.warn("Could not add account membership of account named: '" + account.getName() +"' to group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "', account is already a member of that group!");
        } else {
            log.warn("Successfully added add account membership of account named: '" + account.getName() +"' to group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "'");
            tsgLoaded.getMembers().add(account);
            updateGroupEntity(tsg);
        }
    }
    
    public void removeMemberFromGroup(ResourceGroup tsg, Account account) {
        ResourceGroup tsgLoaded = em.merge(tsg);
        
        boolean isFound = false;
        for (Account currAccount : tsgLoaded.getMembers()) {
            if (currAccount.equals(account)) {
                tsgLoaded.getMembers().remove(currAccount);
                isFound = true;
                break;
            }
        }
        
        if (!isFound) {
            log.warn("Could not remove account membership of account named: '" + account.getName() +"' from group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "', account was not found as a member of this group!");
        } else {
            updateGroupEntity(tsg);
            log.debug("Succesfully removed account membership of account named: '" + account.getName() +"' from group named: '" + tsg.getDisplayName() + "' of target name: '" + tsg.getResource().getDisplayName() + "'");
        }
    }
    
    
    
    public void setGroupAsActive(ResourceGroup tsg) {
        if (tsg.isDeletedInResource()) {
            tsg.setDeletedInResource(false);
        }
        updateGroupEntity(tsg);
    }
    
    
    
    @Destroy @Remove
    public void destroy() {
        System.out.println("zZZZZZZZZZZZZZZZZ***************** DESTROYED!!!! " + this.toString());
        
    }
}
