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

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.PositionManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.seam.PositionHome;
import velo.entity.BulkTask;
import velo.entity.Position;
import velo.entity.PositionRole;
import velo.entity.Role;
import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.OperationException;
import velo.reconcilidation.ReconcilePositions;


@Stateful
@Name("positionActions")
public class PositionActionsBean implements PositionActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@In(value = "#{positionHome}")
	PositionHome positionHome;
	
	@In(value = "#{positionHome.instance}")
	Position position;
	
	@EJB
	PositionManagerLocal positionManager;
	
	public PositionActionsBean() {
	
	}
	
	//@End
	public String showModifyRolesInPosition() {
			if ( (position.getPositionRolesToAssign().size() == 0) && (position.getPositionRolesToRevoke().size() == 0) ) {
				facesMessages.add(FacesMessage.SEVERITY_INFO,"No roles modifications were requested.");
				return null;
			}
			
		  	if(!position.getPositionRolesToAssign().isEmpty())
		  		log.debug("Roles to be added: ");
		  			for(PositionRole pr : position.getPositionRolesToAssign()){
		  				log.debug(pr.getRole().getName() + ";");
      		}
		  	if(!position.getPositionRolesToAssign().isEmpty())
				 log.debug("Roles to be revoked: ");
				  	for(PositionRole pr : position.getPositionRolesToRevoke()){
				  		log.debug(pr.getRole().getName() + ";");
		    }		
				  	
		
			if(!position.getPositionRoles().isEmpty()){
				log.info("Currently the position contains  roles : ");
				for(PositionRole pr : position.getPositionRoles()){
					log.info(pr.getRole().getName() + ";");
				}
			} else{
				log.info("Currently the position doesn't contain any roles");
			}

			//calculate the affected users
			facesMessages.add(FacesMessage.SEVERITY_INFO,"Please review the affected users from this change and submit/cancel the request when you are ready.");
			return "/admin/PositionEditRolesAffectedUsers.xhtml";
    }
	
	//@End
	public String showTasksOfModifyRolesInPosition() {
		facesMessages.add(FacesMessage.SEVERITY_INFO,"Please modify the tasks that are going to be executed from your requester operation.");
		
		try {
			BulkTask bt = positionManager.prepareBulkTaskForModifyRolesInPosition(position,position.getPositionRolesToAssign(), position.getPositionRolesToRevoke());
			position.setBulkTaskOfRolesModification(bt);
			
			return "/admin/PositionEditRolesResultedTasks.xhtml";
			
			//positionHome.getEntityManager().refresh(position);
		}catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,"Failed to perform roles modification to position due to: #0",e.getMessage());
			return "/admin/PositionList.xhtml";
		}
		
        //log.info("The content of position "+ position.getUniqueIdentifier() + "was successfully modified");
        //return "/admin/PositionList.xhtml";
	}
	
	@End
	public void performModifyRolesInPosition() {
		positionManager.modifyRolesInPosition(position,position.getPositionRolesToAssign(), position.getPositionRolesToRevoke(), position.getBulkTaskOfRolesModification());
		
		positionHome.getEntityManager().refresh(position);
		facesMessages.add(FacesMessage.SEVERITY_INFO,"Successfully modified roles association for position : '" + position.getDisplayName());
	}
	
	//Roles in Position modifications
	public void addPositionRoleToRevoke(PositionRole positionRole) {
		position.addPositionRoleToRevoke(positionRole);
	}
	
	public void removePositionRoleToRevoke(PositionRole positionRole) {
		position.removePositionRoleToRevoke(positionRole);
	}
	
	public void addRoleToAssign(Role role) {
		try {
			PositionRole pr = new PositionRole(role,position);
			position.addPositionRoleToAssign(pr);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}

	public void removePositionRoleToAssign(PositionRole positionRole) {
		position.removePositionRoleToAssign(positionRole);
	}
	
	
	public void syncPositions() {
		try {
			positionManager.reconcilePositions();
			facesMessages.add("Successfully synced positions!");
		}catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.toString());
		}
	}
	
	
	@Destroy
	@Remove
	public void destroy() {
	}


}
