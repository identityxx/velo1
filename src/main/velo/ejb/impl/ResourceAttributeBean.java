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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerRemote;
import velo.entity.IdentityAttribute;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceType;
import velo.entity.ResourceTypeAttribute;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ValidationException;
import velo.tools.FileUtils;

/**
 * A Stateless EJB bean for resourceAttribute
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
public class ResourceAttributeBean implements ResourceAttributeManagerLocal, ResourceAttributeManagerRemote {
	private static Logger log = Logger.getLogger(ResourceAttributeBean.class.getName());
	
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;
	
	
	
	
	
	// Exposed
	
	//resourceLevel = false means resrouceTypeLevel
	public boolean isResourceTypeAttributeOverridden(ResourceTypeAttribute rta) throws ValidationException {
		if (rta.isOverridden()) {
			throw new ValidationException(
				"Canno determine, passed a resource attribute type that is already overrid!");
		}

		return findResourceAttributeType(rta.getUniqueName(), rta.getResourceType()).size() > 1;
	}
		
	
	public boolean isResourceAttributeExistsByName(String uniqueName, Resource resource) {
		List<ResourceAttribute> results = em.createNamedQuery("resrouceAttribute.findByUniqueName").setParameter("uniqueName", uniqueName).setParameter("resource",resource).getResultList();
        log.trace("Method isResourceAttributeExistsByName : the results are " + results.toString());
		return results.size() != 0;
	}
	
	
	
	
	
	public Collection findResourceAttributeType(String uniqueName, ResourceType resourceType) {
		return em.createNamedQuery("resourceTypeAttribute.findByUniqueName").setParameter("uniqueName", uniqueName).setParameter("resourceType",resourceType).getResultList();
	}
	
	
	
	
	//TODO: CRITICAL FOR GUI UPDATES! Implement!
	public Collection findResourceAttributesAttachedToIdentityAttribute(IdentityAttribute ia) {
		/*Cannot, since there are attributes inherited from higher levels
		try {
			return em.createNamedQuery("resourceAttribute.findBuIdentityAttribute").setParameter("identityAttribute", ia).getResultList(); 
		}
		catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
		*/
		return null;
		 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    
	
	/*
	public resourceAttribute findresourceAttributeByName(String resourceAttributeName, resource resource) {
		try {
			return (resourceAttribute) em.createNamedQuery("findresourceAttributeByName").setParameter("name", resourceAttributeName).setParameter("resourceId",resource.getResourceId()).getSingleResult(); 
		}
		catch (NoResultException e) {
			throw new NoResultException("Couldnt find User for User Name: " + resourceAttributeName);
		}
		catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
	}
	*/
	
	/*
	public Collection<resourceAttribute> findAllresourceAttributes() {
		try {
			return em.createNamedQuery("findAllresourceAttributes").getResultList(); 
		}
		catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
	}
	*/
	
	
	public ResourceAttribute findResourceAttributeByName(
			String resourceAttributeName, Resource resource) throws NoResultFoundException {
		try {
			return (ResourceAttribute) em.createNamedQuery(
					"resrouceAttribute.findByUniqueName").setParameter("uniqueName", resourceAttributeName).setParameter("resource",resource).getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultFoundException(
					"Couldnt find resourceAttribute for attribute name: "
							+ resourceAttributeName
							+ ", for Target System name: "
							+ resource.getDisplayName());
		} catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
	}
	
	public ResourceAttribute factoryAccountIdResourceAttribute(Resource ts) throws NoResultFoundException {
		try {
			ResourceAttribute tsa = (ResourceAttribute)em.createNamedQuery("tsa.findAccountIdAttribute").setParameter("resource", ts).getSingleResult();
			
			return tsa;
		}
		catch (javax.persistence.NoResultException nre) {
			throw new NoResultFoundException(nre.getMessage());
		}
		catch (NonUniqueResultException nure) {
			throw new NoResultFoundException(nure.getMessage());
		}
	}
	
	
	public Collection<ResourceAttribute> findByResource(Resource ts) {
		return em.createNamedQuery("tsa.findByTarget").setParameter("resource", ts).getResultList(); 
	}
	
	
	public File getConverterFile(ResourceAttribute tsa) {
		File f = new File(tsa.getConverterFileName());
		
		return f;
	}
	
	public void writeConverterContentToFile(ResourceAttribute tsa, String content) throws IOException {
		File f = new File(tsa.getConverterFileName());
		FileUtils.setContents(f, content);
	}
	
	public String readConverterFileContent(ResourceAttribute tsa) throws IOException {
		File f = getConverterFile(tsa);
		
		//If file does not exist then create one
		if (!f.isFile()) {
			f.createNewFile();
		}

		return FileUtils.getContents(f);			
	}
}
