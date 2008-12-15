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

import velo.entity.IdentityAttribute;
import velo.entity.IdentityAttributeSource;
import velo.entity.IdentityAttributesGroup;
import velo.entity.ResourceAttribute;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.RemoveEntityException;

/**
 * An IdentityAttributeManager interface for all EJB exposed methods
 *
 *  @author Asaf Shakarchi
 */
public interface IdentityAttributeManager {
	public Collection<IdentityAttribute> findAllActive();
	public IdentityAttribute findIdentityAttribute(String uniqueName);
	public Collection<IdentityAttribute> loadIdentityAttributesToSync();
	
	
	public IdentityAttribute getManagerIdentityAttribute();
	public IdentityAttribute getIdentifierIdentityAttribute();
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
    public void updateIdentityAttribute(IdentityAttribute ia);
    
    /**
     * Find an Identity Attribute by name
     * @param identityAttributeUniqueName The unique name of the Identity Attribute to find
     * @return An IdentityAttribute entity
     * @throws NoResultFoundException
     */
	@Deprecated
    public IdentityAttribute findIdentityAttributeByUniqueName(
            String identityAttributeUniqueName) throws NoResultFoundException;
    
        /*
         * Find an Identity Attributes Group by name
         * @param identityAttributesGroupName The name of the Identity Attributes Group to find
         * @return An IdentityAttributesGroup entity
         */
    //	public IdentityAttributesGroup findIdentityAttributesGroupByName(
    //		String identityAttributesGroupName);
    
    
    /**
     * Find all IdentityAttributesGroups within the system
     * @return A collection of IdentityAttributesGroup entities
     */
	@Deprecated
    public Collection<IdentityAttributesGroup> findAllIdentityAttributesGroups();
    
    /**
     * Find all IdentityAttributes connected to the specified resourceAttribute
     * @param tsa The resourceAttribute entity to fetch all connected IdentityAttributes for
     * @return A collection of IdentityAttribute entities
     */
	@Deprecated
    public Collection findIdentityAttributesAttachedToresourceAttribute(
            ResourceAttribute tsa);
    
    
    
        /*
         * Find all IdentityAttributesGroups
         * @return A collection of IdentityAttributesGroups entities
         */
    //public Collection findAllIdentityAttributesGroups();
    
    
    /**
     * Persist a new IdentityAttribute entity into the DB
     * @param identityAttribute The IdentityAttribute entity object to persist
     */
	@Deprecated
    public void createIdentityAttribute(IdentityAttribute identityAttribute);
    
    /**
     * Persist a new IdentityAttributesGroup entity into the DB
     * @param iag The IdentityAttributesGroup entity object to persist
     */
	@Deprecated
    public void createIdentityAttributesGroup(IdentityAttributesGroup iag);
    
        /*
         * Persist a new IdentityAttributesGroup entity into the DB
         * @param identityAttributesGroup The IdentityAttributesGroup entity object to persist
         */
    //public void createIdentityAttributesGroup(IdentityAttributesGroup identityAttributesGroup);
    
    
    /**
     * Whether an Identity Attribute exists by name or not
     * @param uniqueName The name of the attribute to check for existance
     * @return true/false upon existance / non-existance.
     */
	@Deprecated
    public boolean isIdentityAttributeExistsByUniqueName(String uniqueName);
    
    /**
     * Find all active Identity Attributes
     * @return A collection of loaded IdentityAttributes from the Database.
     */
	@Deprecated
    public Collection<IdentityAttribute> findAllActiveIdentityAttributes();
    
    
    /**
     * Remove an Identity Attribute entity from the respository
     * @param ia The Identity Attribute entity to remove
     * @throws RemoveEntityException
     */
	@Deprecated
    public void removeIdentityAttributeEntity(IdentityAttribute ia) throws RemoveEntityException;
    
	@Deprecated
    public void removeIdentityAttributeSource(IdentityAttributeSource ias);
    
	@Deprecated
    public void updateIdentityAttributeSources(Collection<IdentityAttributeSource> sources);
}
