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
package velo.ejb.interfaces;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;


import velo.entity.Position;
import velo.entity.BulkTask;
import velo.entity.PositionRole;
import velo.entity.Request;
import velo.entity.Resource;
import velo.entity.Task;
import velo.entity.User;
import velo.exceptions.BulkActionsFactoryFailureException;
import velo.exceptions.ConverterProcessFailure;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.OperationException;
import velo.exceptions.PasswordValidationException;
import velo.exceptions.TaskCreationException;
import velo.storage.Attribute;

/**
 * An AccountManager interface for all EJB exposed methods
 *
 * @author Asaf Shakarchi
 */
public interface PositionManager {
	
	//public void removePosition(Position p);
	
	//public void persistPosition(Position p);
	
	//public void persistPositions(Collection<Position> positionsToPersist);
	
	//public void removeGroups(Collection<Position> positionsToRemove);
	
	//public void mergeGroups(Collection<Position> positionsToMerge);
	
	//public List<Position> findPositionsInRepository(List<String> positionUniqueId);
	
	//used by the importer
	
	public Position findPosition(String positionUniqueId);
	
	public Position findPositionEagerly(String positionUniqueId);
	
	public void persistPosition(Position position);
	
	public void removePosition(Position position);
	
	public void persistPositions(List<Position> positions);
	
	public void removePositions(List<Position> positions);
	
    public void associatePositionToUser(String positionUniqueId, String positionDisplayName,  String userName) throws OperationException;
    
    public void associatePositionToUserEntities(User user, Position position);
    
    public void associatePositionsToUserEntities(User user, Set<Position> positions);
    
    public void associatePositionToRole(String positionUniqueId, String positionDisplayName, String roleName) throws OperationException;
    
    public void revokePositionsFromUserEntities(User user, Set<Position> positions);
    
    public void modifyUserPositionAssignmentEntities(User user, Set<Position> positionsToAssoc, Set<Position> positionToRevoke);
    
    public List<Position> loadAllPositions();
    
    public void reconcilePositions() throws OperationException;

    public BulkTask prepareBulkTaskForModifyRolesInPosition(Position position,Set<PositionRole> positionRolesToAdd, Set<PositionRole> positionRolesToRevoke) throws OperationException;
    public Long modifyRolesInPosition(Position position, Set<PositionRole> positionRolesToAdd, Set<PositionRole> positionRolesToRevoke, BulkTask bt);
    public BulkTask modifyUserPositionsBulkTask(Collection<Position> positionsToAssign, Collection<Position> positionsToRemove, User user) throws OperationException ;
    //public void modifyRolesInPosition(Position position,Set<PositionRole> positionRolesToAdd, Set<PositionRole> positionRolesToRevoke) throws OperationException;
    
    public void replicatePositionRoles(String sourcePosUniqueId, String destPosUniqueId) throws OperationException ;
} 
	
