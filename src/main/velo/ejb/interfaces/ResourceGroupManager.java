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
import java.util.List;
import java.util.Set;

import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;

/**
 * An resourceGroupManager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface ResourceGroupManager {

	public void removeGroup(ResourceGroup rg);
	
	public void persistGroup(ResourceGroup rg);
	
	public void persistGroups(Collection<ResourceGroup> groupsToPersist);
	
	public void removeGroups(Collection<ResourceGroup> groupsToRemove);
	
	public void mergeGroups(Collection<ResourceGroup> groupsToMerge);
	
	public List<ResourceGroup> findResourceGroupsInRepository(List<String> groupsUniqueId, Resource resource);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Find a group by ID
	 * @param groupId The Id of the group to load
	 * @param eagerly Whether to load the entity eagerly or not (lazily)
	 * @return a loaded resourceGroup entity
	 */
	@Deprecated
	public ResourceGroup findGroupById(long groupId, boolean eagerly);
	
	/**
	 * Find a group by Unique Identifier
	 * @param groupUniqueId The Unique Id of the group to load
         * @param resource The resource entity the group is related to
	 * @return a loaded resourceGroup entity
	 */
	@Deprecated
	public ResourceGroup findGroupByUniqueId(String groupUniqueId, Resource resource) throws NoResultFoundException;

	
	/**
	 * Find groups on resource for a certian user.
	 * @param ts The target system to find the groups for
	 * @param user The user entity attached to the user
	 * @return A set of found groups
	 */
	@Deprecated
	public Set<ResourceGroup> findGroupsOnResourceForUser(Resource ts, User user);
	
	/**
	 * Find a group by name
	 * @param displayName The display name of the group to find
	 * @return an Account entity
	 */
	@Deprecated
	public ResourceGroup findGroupByDisplayName(String displayName, Resource resource) throws NoResultFoundException;

	/**
	 * Find all groups that fits a certain string of a certain target system
	 * @param searchString The search string to seek groups that matches.
	 * @param resource The resource entity to seek the groups for
	 * @return A collection of resourceGroup entities that matches the specified Search String on the specified resource
	 */
	@Deprecated
	public Collection<ResourceGroup> findGroupsByStringForCertainTarget(String searchString, Resource resource);
	
	
	/**
	 * Load all grops for a certain target system
	 * @param ts The resource entity to load the groups
	 * @return A collection of resourceGroup entities
	 */
	@Deprecated
	public List<ResourceGroup> loadAllGroups(Resource ts);
	
	/**
	 * Whether a group exist on the specified resource or not
	 * @param uniqueId The group's unique ID to check
	 * @param resource The resource the account is related to
	 * @return true/false upon existense/non-existense
	 */
	@Deprecated
	public boolean isGroupExistOnTarget(String uniqueId, Resource resource);
	
	
	/**
	 * Update a group entity into the database
	 * @param tsg The group entity to update
	 */
	@Deprecated
	public void updateGroupEntity(ResourceGroup tsg);
	
	
	@Deprecated
	public void addMemberToGroup(ResourceGroup tsg, Account account);
	
	@Deprecated
	public void removeMemberFromGroup(ResourceGroup tsg, Account account);
	
	
	@Deprecated
	public void setGroupAsActive(ResourceGroup tsg);
	
	
	@Deprecated
	public void destroy();
}
