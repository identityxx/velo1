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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.annotation.IgnoreDependency;

import velo.common.SysConf;
import velo.ejb.interfaces.AdapterManagerLocal;
import velo.ejb.interfaces.PositionManagerLocal;
import velo.ejb.interfaces.PositionManagerRemote;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.BulkTask;
import velo.entity.ModifyUserRolesRequest;
import velo.entity.Position;
import velo.entity.PositionRole;
import velo.entity.Role;
import velo.entity.User;
import velo.exceptions.OperationException;
import velo.reconcilidation.ReconcilePositions;

/**
 * A Stateless EJB bean for managing an Account
 * 
 * @author Asaf Shakarchi
 */
//	Required in order to check status of accounts
  @EJBs({ @EJB(name="resourceEjbRef",beanInterface=ResourceManagerLocal.class),  @EJB(name="adapterEjbRef",beanInterface=AdapterManagerLocal.class) })
@Stateless()
public class PositionBean implements PositionManagerLocal, PositionManagerRemote {
	  
	  private static Logger log = Logger.getLogger(PositionBean.class.getName());
	  
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	/**
	 * Inject the User Bean
	 */
	@IgnoreDependency 
	@EJB
	UserManagerLocal userManager;

	
	/**
	 * Inject the Role Bean
	 */
	@IgnoreDependency 
	@EJB
	RoleManagerLocal roleManager;
	
	/**
	 * Inject the ResourceBean
	 */
	
	@EJB
	TaskManagerLocal tm;


	private static Logger logger = Logger.getLogger(PositionBean.class.getName());

	public void setEntityManager(EntityManager entityManager) {
		this.em = entityManager;
	}
	
	
	public Position findPosition(String positionUniqueId) {
		try {
			logger.debug("Finding Position in repository with uniqueId '" + positionUniqueId +"'");
			Query q = em.createNamedQuery("position.findByName").setParameter("uniqueIdentifier", positionUniqueId);
			return (Position) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			logger.info("FindPosition did not result any positions named '" + positionUniqueId  + "', returning null.");
			return null;
		} catch (NonUniqueResultException nure) {
			logger.warn("FindPosition found multiple positions with name '" + positionUniqueId +  "', returning null.");
			return null;
		}
	}
	
	//touching collections will grow up based on needs.
	public Position findPositionEagerly(String positionUniqueId) {
		Position pos = findPosition(positionUniqueId);
		if (pos == null) {
			return null;
		}
		
		
		pos.getUsers().size();
		pos.getPositionRoles().size();
		
		//neded when feeder needs to invoke pos.isResourceAssociated() method.
		for (PositionRole posRole : pos.getPositionRoles()) {
			posRole.getRole().getResources().size();
		}
		
		return pos;
	}
		
	
	//for imports
	public void associatePositionToUser(String positionUniqueId, String positionDisplayName,  String userName) throws OperationException {
		//try {
			// Load account
			Position loadedPosition = findPosition(positionUniqueId);
			
			if (loadedPosition == null) {
				throw new OperationException(
						"Could not find position named '"
						+ positionDisplayName );
			}

			
			// Load user
			User loadedUser = userManager.findUser(userName);
			
			if (loadedUser == null) {
				throw new OperationException("Could not associate position named '"
						+ positionUniqueId +  "' to User '" + userName
						+ "' since the user does not exist!");
			}
			
			if (loadedUser.isPositionAssociatedToUser(loadedPosition)){
				logger.trace("The association of  user " + loadedUser.getName() + " and position " + loadedPosition.getDisplayName() + " won't be persisted since the association exists already; skipping...");
			}
				
			else {
				// Perform the association!
				loadedPosition.getUsers().add(loadedUser);
				em.merge(loadedPosition);
			}
	}
	
	
	public void associatePositionToUserEntities(User user, Position position) {
		
		log.debug("Associating position '" + position.getDisplayName() + "' to User '" + user.getName() + "'");
		if (user.isPositionAssociatedToUser(position)){
			logger.trace("The association of  user " + user.getName() + " and position " + position.getDisplayName() + " won't be persisted since the association exists already; skipping...");
			return;
		}
		
		user.getPositions().add(position);
		position.getUsers().add(user);
		em.merge(position);
	}
	
	public void removePositionFromUserAssocEntity(User user, Position position) {
		log.debug("Removing position '" + position.getDisplayName() + "' from User '" + user.getName() + "' entity");
		if (!user.isPositionAssociatedToUser(position)){
			logger.trace("The association of  user " + user.getName() + " and position " + position.getDisplayName() + " won't be removed since the association does not exist, skipping...");
			return;
		}
		
		//update user entity just in case as it passes by reference
		user.removePosition(position);
		position.removeUser(user);
		em.merge(position);
	}
	
	public void associatePositionsToUserEntities(User user, Set<Position> positions) {
		for (Position currPos : positions) {
			associatePositionToUserEntities(user,currPos);
		}
	}
	
	
	public void revokePositionsFromUserEntities(User user, Set<Position> positions) {
		for (Position currPos : positions) {
			removePositionFromUserAssocEntity(user,currPos);
		}
	}
	
	public void modifyUserPositionAssignmentEntities(User user, Set<Position> positionsToAssoc, Set<Position> positionsToRevoke) {
		associatePositionsToUserEntities(user, positionsToAssoc);
		revokePositionsFromUserEntities(user, positionsToRevoke);
	}
	
	public void associatePositionToRole(String positionUniqueId, String positionDisplayName, String roleName) throws OperationException {
		//try {
			// Load account
			Position loadedPosition = findPosition(positionUniqueId);
			
			if (loadedPosition == null) {
				throw new OperationException(
						"Could not find position named '"
						+ positionDisplayName );
			}

			
			// Load user
			Role loadedRole = roleManager.findRole(roleName);
			
			if (loadedRole == null) {
				throw new OperationException("Could not associate position named '"
						+ positionDisplayName +  "' to Role '" + roleName
						+ "' since the role does not exist!");
			}
			else
					logger.trace("Successfully loaded role "+ roleName);
			// Perform the association!
			PositionRole r = findPositionRole(positionUniqueId, roleName);
				if(r == null)
				{
					r = new PositionRole(loadedRole, loadedPosition);
					em.persist(r);
					logger.trace("Successfully added a positionRole");
				}
				else
					logger.trace("Position " + positionDisplayName + " is already associated with role named " + roleName + ", skipping");
			//loadedPosition.getPositionRoles().add(new PositionRole(loadedRole, loadedPosition));
			
			//em.merge(loadedPosition);
		/*} catch (NonUniqueResultException ex) {
			throw new OperationException("Could not associate account named '"
					+ accountName + "', on Resource name '" + resourceUniqueName
					+ "', to User '" + userName
					+ "' since more than one account exist on resource!");
		}*/
	}
	
	public PositionRole findPositionRole(String positionUniqueId, String roleName) {
			try {
				logger.debug("Finding PositionRole in repository for position with uniqueId '" + positionUniqueId + "', and with role named  '" + roleName + "'");
				Query q = em.createNamedQuery("positionRole.findByRoleNameAndPositionUniqueId").setParameter("positionUniqueId", positionUniqueId).setParameter("roleName", roleName);
				return (PositionRole) q.getSingleResult();
			}
			catch (javax.persistence.NoResultException e) {
				//
				return null;
			} 
	}
	
	public List<Position> loadAllPositions() {
		return em.createNamedQuery("position.loadAll").getResultList();
	}
	
	public void persistPosition(Position position) {
		em.persist(position);
	}
	
	public void removePosition(Position position) {
		position.setDeleted(true);
		em.merge(position);
	}
	
	public void persistPositions(List<Position> positions) {
		for (Position currPos : positions) {
			persistPosition(currPos);
		}
	}
	
	public void removePositions(List<Position> positions) {
		for (Position currPos : positions) {
			removePosition(currPos);
		}
	}
	
	public void reconcilePositions() throws OperationException {
		String workspaceDir = SysConf.getSysConf().getString("system.directory.user_workspace_dir");
		workspaceDir += "/data/positions/positions.xml";
		
		File f = new File(workspaceDir);
		
		ReconcilePositions recPos = new ReconcilePositions();
		recPos.perform(f);
	}
	
	public BulkTask prepareBulkTaskForModifyRolesInPosition(Position position,Set<PositionRole> positionRolesToAdd, Set<PositionRole> positionRolesToRevoke) throws OperationException {
		log.info("Performing roles modifications for position '" + position.getDisplayName() + "'");
		log.info("Amount of roles to revoke: '" + positionRolesToRevoke.size() + "', amount of roles to add: '" + positionRolesToAdd.size());
		
		
		BulkTask globalBT = BulkTask.factory("Modifying roles for position name '" + position.getDisplayName() + "'");
		Set<Role> rolesToRevoke = new HashSet<Role>();
		Set<Role> rolesToAdd = new HashSet<Role>();
		
		for (PositionRole currPR : positionRolesToRevoke) {
			rolesToRevoke.add(currPR.getRole());
		}
		
		for (PositionRole currPR : positionRolesToAdd) {
			rolesToAdd.add(currPR.getRole());
		}
		
		
		for (User currUserInPos : position.getUsers()) {
			globalBT.getTasks().addAll(roleManager.modifyRolesOfUserTasks(rolesToRevoke, rolesToAdd, currUserInPos).getTasks());
		}
		
		
		return globalBT;
	}
	
	
	
	public Long modifyRolesInPosition(Position position, Set<PositionRole> positionRolesToAdd, Set<PositionRole> positionRolesToRevoke, BulkTask bt) {
		em.persist(bt);
		
		//persist entities
		for (PositionRole currPosRoleToAdd : positionRolesToAdd) {
			em.persist(currPosRoleToAdd);
		}
		
		for (PositionRole currPosRoleToDelete : positionRolesToRevoke) {
			em.remove(em.find(PositionRole.class, currPosRoleToDelete.getPrimaryKey()));
		}
		
		//position = em.merge(position);
		
		//entity is not managed...
		//em.refresh(position);
		
		
		em.flush();
		
		//does not work, throws: Unable to find velo.entity.PositionRole with id velo.entity.PositionRolePK@4a65c4
		/*
		for (PositionRole currPosRoleToAdd : positionRolesToAdd) {
			position.addPositionRole(currPosRoleToAdd);
		}
		*/
		
		return bt.getBulkTaskId();
	}
	
	
	public BulkTask modifyUserPositionsBulkTask(Collection<Position> positionsToAssign, Collection<Position> positionsToRemove, User user) throws OperationException {
		
		log.debug("Modifying User Positions has started...");
		
		Map<String,Role> rolesToAssignGlobal = new HashMap<String,Role>();
		Map<String,Role> rolesToRemoveGlobal = new HashMap<String,Role>();
		
		for (Position currPosition : positionsToAssign) {
			for (PositionRole currPosRole : currPosition.getPositionRoles()) {
				if (!rolesToAssignGlobal.containsKey(currPosRole.getRole().getName())) {
					rolesToAssignGlobal.put(currPosRole.getRole().getName(),currPosRole.getRole());
				} else {
					log.debug("Cannot add role '" + currPosRole.getRole().getName() + "' to assign to the global map as role already exists in map");
				}
			}
		}
		for (Position currPosition : positionsToRemove) {
			for (PositionRole currPosRole : currPosition.getPositionRoles()) {
				if (!rolesToRemoveGlobal.containsKey(currPosRole.getRole().getName())) {
					rolesToRemoveGlobal.put(currPosRole.getRole().getName(),currPosRole.getRole());
				} else {
					log.debug("Cannot add role '" + currPosRole.getRole().getName() + "' to assign to the global map as role already exists in map");
				}
			}
		}
		
		//modify roles expecting sets :/
		Set<Role> rolesToAddGlobalSet = new HashSet<Role>();
		Set<Role> rolesToRemoveGlobalSet = new HashSet<Role>();
		rolesToAddGlobalSet.addAll(rolesToAssignGlobal.values());
		rolesToRemoveGlobalSet.addAll(rolesToRemoveGlobal.values());
		
		try {
			BulkTask bt = roleManager.modifyRolesOfUserTasks(rolesToRemoveGlobalSet, rolesToAddGlobalSet, user);
			return bt;
		}catch (OperationException e) {
			throw(e);
		}
		
		/*
		log.debug("Assigning positions with amount '" + positionsToAssign.size() + "', with amount of positions  to remove '" + positionsToRemove.size() + "' for user name '" + user.getName() + "'");
		for (Position currPosToAssign : positionsToAssign) {
			log.trace("Assigning Position with unique indentifier '" + currPosToAssign.getUniqueIdentifier() + "'");
			
			if (user.isPositionAssociatedToUser(currPos)) {
				log.info("Won't associate current iterated position to user since it is already associated!");
				continue;
			}
			
			BulkTask bt = ModifyUserRolesRequest.modifyUserPositions(user, positionsToRemove, positionsToAdd)
		}
		*/
	}
	
	
	
	
	public void replicatePositionRoles(String sourcePosUniqueId, String destPosUniqueId) throws OperationException {
		Position sourcePos = findPosition(sourcePosUniqueId);
		
		if (sourcePos == null) {
			throw new OperationException("Could not find source position with unique ID '" + sourcePosUniqueId + "'");
		}
		
		Position destPos = findPosition(destPosUniqueId);
		
		if (destPos == null) {
			throw new OperationException("Could not find destination position with unique ID '" + destPosUniqueId + "'");
		}
		
		
		for (PositionRole sourcePosRole : sourcePos.getPositionRoles()) {
			//make sure the role is not already assoc to the dest pos
			if (destPos.isRoleAssociated(sourcePosRole.getRole())) {
				continue;
			}
			
			PositionRole destPosRole = new PositionRole(sourcePosRole.getRole(), destPos);
			
			destPos.getPositionRoles().add(destPosRole);
			em.merge(destPosRole);
		}
	}
}