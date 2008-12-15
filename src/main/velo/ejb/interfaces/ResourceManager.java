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

import velo.actions.ResourceActionInterface;
import velo.adapters.ResourceAdapter;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceType;
import velo.entity.User;
import velo.exceptions.AccountIdGenerationException;
import velo.exceptions.DeleteObjectViolation;
import velo.exceptions.FactoryException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ResourceDescriptorException;
import velo.exceptions.ResourceTypeDescriptorException;
import velo.exceptions.ScriptLoadingException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import velo.resource.resourceTypeDescriptor.ResourceTypeDescriptor;
import velo.rules.AccountsCorrelationRule;

/**
 A Resource Manager interface for all EJB exposed methods
 
 @author Asaf Shakarchi
 */
public interface ResourceManager {
    
	/**
     * Find a target system by name
     * @param resourceName The name of the target system to find
     * @return A Resource entity or null if non existence
     */
	public Resource findResource(String uniqueName);
	
	public ResourceType findResourceType(String uniqueName);
	
	/**
    Find all target systems
    @return A Collection of Resource entities
    */
   public List<ResourceAttribute> findAllActiveResourceAttributesToSync();
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public Resource findResourceByName(String uniqueName);
	
    /**
     Add a new Resource to the system by:
     - Persisting the specified Resource entity
     - Adding an XML file by coping/modifying the correct XML template (based on ResourceType)
     @param ts The Resource entity to persist
     @return true/false upon success/failure
     TODO: Take care of adding the XML file
     @deprecated
     */
    public boolean addResource(Resource ts);
    
    /**
     Remove a Resource from the system by:
     - Removing the specified entity from the database.
     - Put the Resource Configuration directory into the recycle bin folder
     @param ts The Resource entity to remove from database
     @return true/false upon success/failure
     */
    public boolean removeResource(Resource ts) throws DeleteObjectViolation;
    
    public void persistResourceEntity(Resource resource);
    public void removeResourceEntity(Resource resource);
    public void persistResourceTypeEntity(ResourceType resourceType);
    public void removeResourceTypeEntity(ResourceType resourceType);
    public ResourceType findResourceTypeByUniqueName(String uniqueName) throws NoResultFoundException;
    
    /**
     Update the specified Resource including:
     - Update the entity in the database
     - Update the configuration XML descriptor file.
     @param ts
     @return ture/false unpon success/failure of update process
     */
    public boolean updateResource(Resource ts);
    
    /**
     Find a target system by ID
     @param id The ID of the Target System to find
     @return A Resource entity
     @throws NoResultFoundException
     */
    public Resource findResourceById(long id) throws NoResultFoundException;
    
    public Resource loadResourceByIdEagrly(long id) throws NoResultFoundException;
    
    public Resource loadResourceForAction(long id) throws NoResultFoundException;
    
    
    
    /**
     Find all Active target systems
     @return A Collection of active Resource entities
     */
    public Collection findAllActiveResources();
    
    
    /**
     Check whether an account exist on a certain Resource or not
     @param ts The Resource entity to check whether the specified account name exist or not
     @param accountName The account name to check if exist on the specified Resource or not
     @return true/false upon exist/not exist
     */
    public boolean isAccountExistOnResource(Resource ts,
        String accountName);
    
    /**
     Factory a ResourceDescriptor for a certain Target-System
     @param resource The Resource entity to factory the Descriptor for
     @return A ResourceDescriptor object for the specified Resource
     @throws ResourceDescriptorException
     */
    public ResourceDescriptor factoryResourceDescriptor(
        Resource resource) throws ResourceDescriptorException;
    
    /**
     Factory a ResourceTypeDescriptor for a certain Target System Type
     @param tst The ResourceType entity to factory the Descriptor for
     @return A ResourceTypeDescriptor object for the specified Resource Type
     @throws ResourceTypeDescriptorException
     */
    public ResourceTypeDescriptor factoryResourceTypeDescriptor(ResourceType tst) throws ResourceTypeDescriptorException;
    
    /**
     Factory an ResourceAdapter for a certain Resource
     Will factor the correct type of adapter based on the confiugration of the target casted to ResourceAdapter.
     @param resource The Resource entity to factory the adapter for
     @return a ResourceAdapter object for the specified Resource
     @throws FactoryException
     */
    public ResourceAdapter adapterFactory(Resource resource) throws FactoryException;
    
    /**
     Factory an Action object for a certain Resource by action name
     (Use action manager instead!)
     @param resource The Resource entity to factory an action for
     @param actionName The action name to factory
     @return A factored action casted as a ResourceActionInterface
     @throws ScriptLoadingException
     @deprecated
     */
    public ResourceActionInterface factoryActionScriptByActionName(
        Resource resource, String actionName)
        throws ScriptLoadingException;
    
    /**
     Factory an account correlation rule for the specified Resource
     @param resource The Resource entity to factory the account correlation rule for
     @return The factored account correlation rule object
     */
    public AccountsCorrelationRule correlationRuleFactory(
        Resource resource);
    
    
    /**
     Generate a new account ID for a certain Resource/User
     @param resource The Resource to generate the account ID for.
     @param user The User entity to generate the account ID for.
     @return The name of the new generated account ID
     @throws AccountIdGenerationException Threw if there was a failure to generate a new account id.
     */
    public String generateNewAccountId(Resource resource, User user) throws AccountIdGenerationException;
    
    /**
     Refresh the specified entity
     @param entity The entity to refresh
     @return A refreshed object
     */
    public Object refreshEntity(Object entity);
    
    /**
     Flush tne entity
     @deprecated
     */
    public void flushEntityManager();
    
    //moved to reconcileBean public Task syncListActionTask(Resource ts, BulkTask bulkTask) throws TaskCreationException;
    
    //moved to reconcileBean public Long syncListAction(Resource ts, BulkTask bulkTask) throws TaskCreationException ;
    
    //moved to reconcileBeanpublic Long syncActiveData(String uniqueName) throws TaskCreationException;
    
    public boolean isResourceAssignedToRoles(Resource ts);
}