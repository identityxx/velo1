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

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.RoleHome;
import velo.entity.ApproversGroup;
import velo.entity.PositionRole;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.Role;
import velo.entity.RoleApproversGroup;
import velo.entity.RoleResourceAttribute;
import velo.entity.RoleResourceAttributeAsRule;
import velo.entity.RoleResourceAttributeAsTextual;
import velo.entity.User;
import velo.entity.RoleJournalingEntry.RoleJournalingActionType;
import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.OperationException;

@Stateful
@Name("roleActions")
public class RoleActionsBean implements RoleActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@PersistenceContext
    public EntityManager em;
    
	@EJB
	public RoleManagerLocal roleManager;
	
	@EJB
	public UserManagerLocal userManager;
	
	@EJB
	public ResourceOperationsManagerLocal resourceOperationsManager;

	@In(value = "#{roleHome}")
	RoleHome roleHome;
	
	@In(value = "#{roleHome.instance}")
	Role role;
	
	@In
	User loggedUser;
	
	Resource selectedResource;

	@In
	Conversation conversation;
	
	//this method should be invoked! not 'modifyGroupsInRole' which is invoked by this method!
	//@End
	public String performModifyGroupsInRole() {
		//from previous calls (if cancel button was pressed as it's a link)
		role.getAffectedUsers().clear();
		
		if (role.isActiveAccessSync()) {
			//get the affected users
			role.getAffectedUsers().addAll(userManager.findAllUsersAssignedToRole(role));
			
			//get the effected position roles
			//role.getAffectedPositions().addAll(role.getPositionRoles());
			
			return "/admin/RoleResourceGroupsModifyAffectedUsers.xhtml";
		} else {
			//return "/admin/Role.xhtml";
			
			String ret = modifyGroupsInRole();
			conversation.end();
			return ret;
		}
		
	}
	
	@End
	public String modifyGroupsInRole() {
        //try {
        	log.info("Requesting Access Groups modification in role ID #0, with name #1", role.getRoleId(), role.getName());
        	
        	//roleManager.modifyResourceGroupsInRole(role, role.getGroupsToAssign(), role.getGroupsToRevoke());
        	
        	if (role.isActiveAccessSync()) {
        		Set<User> users = new HashSet<User>();
        		users.addAll(role.getAffectedUsers());
        		
        		//add all roles from all users inherited from positions
        		for (PositionRole currPR : role.getPositionRoles()) {
        			users.addAll(currPR.getPosition().getUsers());
        		}
        		
        		
        		log.info("Will create and persist tasks for affected users amount '#0'",users.size());
        		
        		
        		//add a group membership to each of the users
        		try {
        			Long btId = roleManager.modifyAccountsGroupsAssignment(users,role,role.getGroupsToAssign(), role.getGroupsToRevoke());
        		}catch (OperationException e) {
        			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
        			return null;
        		}
        	} else {
        		try {
        			roleManager.modifyRoleEntityGroupsAssignmentAssoc(role,role.getGroupsToAssign(), role.getGroupsToRevoke());
        		}catch (OperationException e) {
        			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
        			return null;
        		}
        	}
        	
        	//since the modification is done via roleBean, which loads different role object, the data must be synced.
        	//not working, creating a 'stack trace overflow'
        	//roleHome.getEntityManager().refresh(role);
        	
        	
        	//done by?
        	role.addJournalingEntry(RoleJournalingActionType.MODIFIED_GROUPS, loggedUser, "Resource groups assocaition were modified in role", "");
        	role.getGroupsToAssign().clear();
        	role.getGroupsToRevoke().clear();
        	roleHome.update();
        	
        	//FIXME cleanups, better to do it through conversations if possible
    		role.getAffectedUsers().clear();
            facesMessages.add("Sucessfully requested Roles Access Groups modifications for role: '" + role.getName() + "'");
            
            //return "/admin/Role.xhtml";
            
            
            //since refresh does not work, a workaround is done by redirecting to the RoleList
            return "/admin/RoleList.xhtml";
            
        /*}
        catch (ModifyResourceGroupsInRoleException mtsgire) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR,"Failed to modify Access Groups in Role: '" + role.getName() + "' due to: " + mtsgire.toString());
            return "/admin/Role.xhtml";
        }*/
    }
	
	
	
	
	
	
	
	
	//should end the nested conversation (but it doesnt work!)
	//@End()
	@End
	public void modifyApproversGroupsInRole() {
		log.info("Modifying approvers groups, associating #0 approvers groups, removing association of #1 approvers groups", role.getApproversGroupsToAssign(), role.getRoleApproversGroupsToDelete());
		
		for (RoleApproversGroup currRAG : role.getRoleApproversGroupsToDelete()) {
			em.remove(em.find(RoleApproversGroup.class, currRAG.getPrimaryKey()));
		}
		//for god sake, why @end does not kill the nested conversation that started when clicked on 'edit approvers groups' in role page?
		role.getRoleApproversGroupsToDelete().clear();
		
		
		for (ApproversGroup currAG : role.getApproversGroupsToAssign()) {
			RoleApproversGroup currRagToPersist = RoleApproversGroup.factory(role, currAG);
			em.persist(currRagToPersist);
		}
		
		//damn again
		role.getApproversGroupsToAssign().clear();
		
		roleHome.getEntityManager().refresh(role);
		
		facesMessages.add("Successfully modified groups associations.");
		log.info("Successfully modified approvers groups for role!");
	}
	
	
	
	
	
	
	
	//Groups in Role modifications
	public void addGroupToRevoke(ResourceGroup group) {
		role.addGroupToRevoke(group);
	}
	
	public void removeGroupToRevoke(ResourceGroup group) {
		role.removeGroupToRevoke(group);
	}
	
	public void addGroupToAssign(ResourceGroup group) {
		try {
			role.addGroupToAssign(group);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}

	public void removeGroupToAssign(ResourceGroup group) {
		role.removeGroupToAssign(group);
	}
	
	
	
	
	
	
	
	//Approvers Groups in Role modifications
	public void addRoleApproversGroupToDelete(RoleApproversGroup rag) {
		role.addRoleApproversGroupToDelete(rag);
	}
	
	public void removeRoleApproversGroupToDelete(RoleApproversGroup rag) {
		role.removeRoleApproversGroupToDelete(rag);
	}
	
	public void addApproversGroupToAssign(ApproversGroup ag) {
		try {
			role.addApproversGroupToAssign(ag);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}

	public void removeApproversGroupToAssign(ApproversGroup ag) {
		role.removeApproversGroupToAssign(ag);
	}
	
	
	
	
	
	//Role resource attributes in role modifications
	public void addRoleResourceAttributesToDelete(RoleResourceAttribute rra) {
		role.addRoleResourceAttributeToDelete(rra);
	}
	public void removeRoleResourceAttributesToDelete(RoleResourceAttribute rra) {
		role.removeRoleResourceAttributeToDelete(rra);
	}
	
	public void addTextualResourceAttributeToAssign(ResourceAttribute ra) {
		RoleResourceAttributeAsTextual rrat = new RoleResourceAttributeAsTextual(role,ra);
		
		try {
			role.addResourceAttributeToAssign(rrat);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}
	
	public void addRuleResourceAttributeToAssign(ResourceAttribute ra) {
		RoleResourceAttributeAsRule rrar = new RoleResourceAttributeAsRule(role,ra); 
		
		try {
			role.addResourceAttributeToAssign(rrar);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}
	
	public void removeResourceAttributeToAssign(RoleResourceAttribute rra) {
		role.removeRoleResourceAttributeToAssign(rra);
	}

	
	
	@End
	public void modifyResourceAttributesInRole() {
		log.info("Modifying resource attributes, associating #0 res attrs, removing association of #1 res attrs", role.getRoleResourceAttributesToAssign().size(), role.getRoleResourceAttributesToDelete().size());
		
		for (RoleResourceAttribute currRRAToDel : role.getRoleResourceAttributesToDelete()) {
			em.remove(em.find(RoleResourceAttribute.class, currRRAToDel.getPrimaryKey()));
		}
		
		//FIXME: for god sake, why @end does not kill the nested conversation that started when clicked on 'edit res attrs' in role page?
		role.getRoleResourceAttributesToDelete().clear();
		
		
		for (RoleResourceAttribute currAGToAdd : role.getRoleResourceAttributesToAssign()) {
			if (currAGToAdd instanceof RoleResourceAttributeAsRule) {
				RoleResourceAttributeAsRule currAGToAddRule = (RoleResourceAttributeAsRule)currAGToAdd;
				currAGToAddRule.setActionRule(em.merge(currAGToAddRule.getActionRule()));
			}
			em.persist(currAGToAdd);
		}
		
		//damn again
		role.getRoleResourceAttributesToAssign().clear();
		
		role.addJournalingEntry(RoleJournalingActionType.MODIFIED_ATTRIBUTES, loggedUser, "Resource-Attributes associations were modified in role", "");
		roleHome.update();
		
		
//		roleHome.getEntityManager().refresh(role);
		log.info("Successfully modified resource attributes for role!");
	}
	
	
	public String changeRoleStatus() {
		log.trace("Modifying the role's status.");
			if(role.isDisabled()){
				log.trace("Since role is disabled, enabling it");
				role.setDisabled(false);
			}
			else {
				if(role.getPositionRoles().size()>0 || userManager.findAllUsersAssignedToRole(role).size()>0) {
					log.info("The role has positions or users associated with it; cannot disable, skipping...");
					facesMessages.add(FacesMessage.SEVERITY_WARN, "Cannot update role since it has positions and/or users associated to it");
					return null;
				}
				else {
					log.trace("Since role is enabled, disabling it");
					role.setDisabled(true);
				}
			}
		roleHome.update();	
		return null;	
				
	}
	
	//accessors (used by the 'show attrs by selected resource' in role resource attrs edit page
	/**
	 * @return the selectedResource
	 */
	public Resource getSelectedResource() {
		return selectedResource;
	}

	/**
	 * @param selectedResource the selectedResource to set
	 */
	public void setSelectedResource(Resource selectedResource) {
		this.selectedResource = selectedResource;
	}
	
	
	
	@Destroy
	@Remove
	public void destroy() {
	}


}
