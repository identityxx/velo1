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

import java.util.Arrays;
import java.util.List;

import javax.ejb.Remove;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.seam.RolesFolderHome;
import velo.ejb.seam.RolesFolderList;
import velo.entity.ApproversGroup;
import velo.entity.RolesFolder;
import velo.entity.RolesFolderApproversGroup;
import velo.entity.User;
import velo.exceptions.CollectionElementInsertionException;

@Name("rolesFolderActions")
public class RolesFolderActions  {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@In
    public EntityManager entityManager;
    
	@In(value = "#{rolesFolderHome}")
	RolesFolderHome rolesFolderHome;
	
	@In(value = "#{rolesFolderHome.instance}")
	RolesFolder rolesFolder;
	
	@In
	User loggedUser;
	
	
	@End
	public void modifyApproversGroupsInRolesFolder() {
		log.info("Modifying approvers groups, associating #0 approvers groups, removing association of #1 approvers groups", rolesFolder.getApproversGroupsToAssign(), rolesFolder.getRolesFolderApproversGroupsToDelete());
		
		for (RolesFolderApproversGroup currRAG : rolesFolder.getRolesFolderApproversGroupsToDelete()) {
			entityManager.remove(entityManager.find(RolesFolderApproversGroup.class, currRAG.getPrimaryKey()));
		}
		//for god sake, why @end does not kill the nested conversation that started when clicked on 'edit approvers groups' in role page?
		rolesFolder.getRolesFolderApproversGroupsToDelete().clear();
		
		
		for (ApproversGroup currAG : rolesFolder.getApproversGroupsToAssign()) {
			RolesFolderApproversGroup currRagToPersist = RolesFolderApproversGroup.factory(rolesFolder, currAG);
			entityManager.persist(currRagToPersist);
		}
		
		//damn again
		rolesFolder.getApproversGroupsToAssign().clear();
		
		rolesFolderHome.getEntityManager().refresh(rolesFolder);
		
		log.info("Successfully modified approvers groups for role!");
	}
	
	
	
	//Approvers Groups in RolesFolder modifications
	public void addRolesFolderApproversGroupToDelete(RolesFolderApproversGroup rfag) {
		rolesFolder.addRolesFolderApproversGroupToDelete(rfag);
	}
	
	public void removeRolesFolderApproversGroupToDelete(RolesFolderApproversGroup rag) {
		rolesFolder.removeRolesFolderApproversGroupToDelete(rag);
	}
	
	public void addApproversGroupToAssign(ApproversGroup ag) {
		System.out.println("Adding object: " + ag);
		try {
			rolesFolder.addApproversGroupToAssign(ag);
		} catch (CollectionElementInsertionException ex) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, ex.toString());
		}
	}

	public void removeApproversGroupToAssign(ApproversGroup ag) {
		System.out.println("Removing object: " + ag);
		
		//System.out.println("!!!!!!!!!APPROVERS GROUP TO REMOVE: " + ag.getApproversGroupId());
		
		System.out.println("!!!!!!!!!size(1): " + rolesFolder.getApproversGroupsToAssign().size());
		rolesFolder.removeApproversGroupToAssign(ag);
		System.out.println("!!!!!!!!!size(2): " + rolesFolder.getApproversGroupsToAssign().size());
	}

	
	public List<RolesFolder> getRoleFoldersByType(String type) {
		//System.out.println("!!!!!!!!!!: " + type);
		
		
		/*doesn't work (for unknown reason, verified that restriction is added, maybe wrong understanding of Seam QueryList flow)
		RolesFolderList rfl = new RolesFolderList();
		rfl.setMaxResults(500);
		//rfl.getRestrictions().add();
		
		rfl.getRolesFolder().setType(type);
		String[] restrictions = new String[1];
		restrictions[0] = "lower(rolesFolder.typezzz) = lower(#{rolesFolderList.rolesFolder.type})"; 
		rfl.setExtraRestricions(restrictions);
		
		return rfl.getResultList();
		*/
		
		

		return entityManager.createNamedQuery("rolesFolder.findByType").setParameter("type", type).getResultList();
	}
	
	
	@Destroy
	@Remove
	public void destroy() {
	}


}
