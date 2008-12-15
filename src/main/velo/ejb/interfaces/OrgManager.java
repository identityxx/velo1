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

import velo.entity.Position;
import velo.exceptions.CannotFindOrgUnitException;
import velo.organizational.OrgUnit;
import velo.organizational.TopOrgUnit;

/**
 * An OrgManager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface OrgManager {

	/**
	 * Find a position by ID
	 * @param positionId The ID of the position to find
	 * @return A loaded position entity
	 */
	@Deprecated
	public Position findPositionById(Long positionId);
	
	/**
	 * Find a position by Unique Identifier
	 * @param uniqueId The Unique ID of the position to find
	 * @return A loaded position entity
	 */
	@Deprecated
	public Position findPositionByUniqueId(String uniqueId);
	
	
	/**
	 * Find a position by name
	 * @param positionName The name of the position to find
	 * @return A loaded position entity
	 */
	@Deprecated
	public Position findPositionByName(String positionName);

	
	/**
	 * Create a new position entity (persist it in the DB)
	 * @param position The position object to persist
	 */
	@Deprecated
	public void createPositionEntity(Position position);

	/**
	 * Remove a position from the database
	 * @param position The position entity to remove from Database
	 */
	@Deprecated
	public void removePositionEntity(Position position);
	
	/**
	 * Update a certain position entity to the database
	 * @param position The position entity to update the database with
	 */
	@Deprecated
	public void updatePositionEntity(Position position);

	/**
	 * Get the organizational chart by retrieving a loaded 'Top Organizational Unit'
	 * With the entire OUs hirarchy beneath it.
	 * 
	 * @return A TopOrgUnit loaded object
	 */
	@Deprecated
	public TopOrgUnit getOrganizationaChart();
	
	/**
	 * Get an OU of a certain position
	 * @param position The position to get the OU the position is related to
	 * @return OrgUnit object which the specified position is related to
	 * @throws CannotFindOrgUnitException
	 */
	@Deprecated
	public OrgUnit getOrgUnitOfPosition(Position position) throws CannotFindOrgUnitException;
	
}
