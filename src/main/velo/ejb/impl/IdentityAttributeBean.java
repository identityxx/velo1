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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;

import velo.ejb.interfaces.IdentityAttributeManagerLocal;
import velo.ejb.interfaces.IdentityAttributeManagerRemote;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.entity.IdentityAttribute;
import velo.entity.IdentityAttributeSource;
import velo.entity.IdentityAttributesGroup;
import velo.entity.ResourceAttribute;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.RemoveEntityException;

/**
 * A Stateless EJB bean for IdentityAttribute
 *
 * @author Asaf Shakarchi
 */
@Stateless()
@Name("identityAttributeManager")
public class IdentityAttributeBean implements IdentityAttributeManagerLocal,IdentityAttributeManagerRemote {
	private static Logger log = Logger.getLogger(IdentityAttributeBean.class.getName());
	
    /**
     * Injected entity manager
     */
    @PersistenceContext
    public EntityManager em;
    
    @EJB
    public ResourceAttributeManagerLocal tsam;
    
    
    /**
     *
     * A Logger object for logging messages related to this class
     */
    private static Logger logger = Logger.getLogger("velo.ejbs");
    
    
    public Collection<IdentityAttribute> findAllActive() {
    	return em.createNamedQuery("identityAttribute.findAllActive").getResultList();
    }
    
    
    public IdentityAttribute findIdentityAttribute(String uniqueName) {
    	log.debug("Finding Identity Attribute in repository with unique name '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("identityAttribute.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (IdentityAttribute) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("FindRole method did not result any identity attribute for unique name '" + uniqueName + "', returning null.");
			return null;
		}
    }
    
    
    public Collection<IdentityAttribute> loadIdentityAttributesToSync() {
        return em.createNamedQuery("identityAttribute.findAllToSync").getResultList();
    }
    
    
    public IdentityAttribute getManagerIdentityAttribute() {
    	log.debug("Finding Identity Attribute that represents a manager.");

		try {
			Query q = em.createNamedQuery("identityAttribute.findManager");
			return (IdentityAttribute) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.info("getManagerIdentityAttribute method did not result any identity attribute that reprsents a manager., returning null.");
			return null;
		}
    }
    
	public IdentityAttribute getIdentifierIdentityAttribute() {
		log.debug("Finding Identity Attribute that represents a Identifier.");

		try {
			Query q = em.createNamedQuery("identityAttribute.findIdentifier");
			return (IdentityAttribute) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.info("getManagerIdentityAttribute method did not result any identity attribute that reprsents an identifier., returning null.");
			return null;
		}
	}
	
	
	public List<IdentityAttribute> findIdentityAttribues(Set<String> identityAttributesUniqueNames) {
		StringBuilder query = new StringBuilder("select ia FROM IdentityAttribute ia WHERE ");
		
		for (int i=0; i<identityAttributesUniqueNames.size();i++) {
			if (i>0) {
				query.append(" OR ");
			}
			query.append("ia.uniqueName = :param"+i);
		}

		Query q = em.createQuery(query.toString());
		int i=0;
		for (String currIAUniqueName : identityAttributesUniqueNames) {
			q.setParameter("param"+i,currIAUniqueName);
			i++;
		}
		
		
		return q.getResultList();
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    public void updateIdentityAttribute(IdentityAttribute ia) {
        em.merge(ia);
    }
    
    @Deprecated
    public IdentityAttribute findIdentityAttributeByUniqueName(String identityAttributeUniqueName) throws NoResultFoundException {
        try {
            return (IdentityAttribute) em.createNamedQuery("findIdentityAttributeByUniqueName").setHint("toplink.refresh", "true").setParameter("identityAttributeUniqueName", identityAttributeUniqueName).getSingleResult();
        } catch (NoResultException e) {
            throw new NoResultFoundException("Couldnt find ANY identity attribute for unique name: " + identityAttributeUniqueName);
        }
    }
    
    @Deprecated
    public Collection findIdentityAttributesAttachedToresourceAttribute(ResourceAttribute tsa) {
        try {
            return em.createNamedQuery("findIdentityAttributesAttachedToresourceAttribute").setHint("toplink.refresh", "true").setParameter("resourceAttribute", tsa).getResultList();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public void createIdentityAttribute(IdentityAttribute identityAttribute) {
        em.persist(identityAttribute);
    }
    
    @Deprecated
    public void createIdentityAttributesGroup(IdentityAttributesGroup iag) {
        em.persist(iag);
    }
    
    
    @Deprecated
    public Collection<IdentityAttributesGroup> findAllIdentityAttributesGroups() {
        try {
            return em.createNamedQuery("identityAttributesGroup.findAll").setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public boolean isIdentityAttributeExistsByUniqueName(String uniqueName) {
        logger.debug("Checking whether Identity Attribute unique named: '" + uniqueName + "' exists or not");
        
        Query q = em.createNamedQuery("identityAttribute.isExistByUniqueName").setParameter("uniqueName", uniqueName);
        Long num = (Long) q.getSingleResult();
        
        
        if (num == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    @Deprecated
    public Collection<IdentityAttribute> findAllActiveIdentityAttributes() {
        try {
            return em.createNamedQuery("identityAttribute.findAllActive").getResultList();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    @Deprecated
    public void removeIdentityAttributeEntity(IdentityAttribute ia) throws RemoveEntityException {
        logger.info("Removing Identity Attribute name: " + ia.getUniqueName());
        
        //Make sure there are no TSA associated with this IA
        if (tsam.findResourceAttributesAttachedToIdentityAttribute(ia).size() > 0) {
            throw new RemoveEntityException("Cannot remove IA named: '" + ia.getUniqueName() + "' since there are Target System Attributes associated with it!");
        } else {
            IdentityAttribute mergedIA = em.merge(ia);
            em.remove(mergedIA);
        }
    }
    @Deprecated
    public void removeIdentityAttributeSource(IdentityAttributeSource ias) {
        em.remove(em.merge(ias));
    }
    
    @Deprecated
    public void updateIdentityAttributeSources(Collection<IdentityAttributeSource> sources) {
        for (IdentityAttributeSource currSource : sources) {
            em.merge(currSource);
        }
    }
}
