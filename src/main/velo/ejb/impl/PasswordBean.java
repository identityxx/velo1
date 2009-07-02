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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.PasswordManagerLocal;
import velo.ejb.interfaces.PasswordManagerRemote;
import velo.entity.PasswordPolicy;
import velo.entity.PasswordPolicyContainer;
import velo.entity.Resource;
import velo.exceptions.NoResultFoundException;

/**
 * A Stateless EJB bean for managing Password processes and policies
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
public class PasswordBean implements PasswordManagerLocal, PasswordManagerRemote {
	private static Logger log = Logger.getLogger(PasswordBean.class.getName());
	
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	
	public PasswordPolicyContainer findPasswordPolicyContainer(String uniqueName) {
		log.debug("Finding Password Policy Container in repository with unique name '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("passwordPolicyContainer.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (PasswordPolicyContainer) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Could not find any Password Policy Container for unique name '" + uniqueName + "', returning null.");
			return null;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//MANAGE PASSWORD POLICIES
	@Deprecated
	public void persistPasswordPolicyEntity(PasswordPolicy pp) {
		em.persist(pp);
	}

	@Deprecated
	public List<PasswordPolicy> loadAllPasswordPolicies() {
		return em.createNamedQuery("passwordPolicy.findAll").getResultList();
	}

	@Deprecated
	public PasswordPolicy findPasswordPolicyByUniqueName(String uniqueName) throws NoResultFoundException {
		try {
			return (PasswordPolicy) em.createNamedQuery("passwordPolicy.findByUniqueName")
				.setParameter("uniqueName", uniqueName).getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultFoundException("Couldnt find Password Policy for unique name: "
					+ uniqueName + ", detailed message is: " + e.getMessage());
		}
	}
	
	@Deprecated
	public boolean isPasswordPolicyExistsByUniqueName(String uniqueName) {
		//Query database to see if user already exists...
		Query query = em.createNamedQuery("passwordPolicy.findByUniqueName");
		List mailTemplates = query.setParameter("uniqueName", uniqueName).getResultList();

		//If no entry found, persist the entity, send a message to the view and clean the entity from the session by initializing a new one and refresh the DataModel list
		if (mailTemplates.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
	@Deprecated
	public void deletePasswordPolicy(PasswordPolicy pp) {
		log.info("Removing Password Policy with Unique Name '" + pp.getUniqueName());

		PasswordPolicy mergedPasswordPolicy = em.merge(pp);
		em.remove(pp);
		
	}
	
	@Deprecated
	public void mergePasswordPolicy(PasswordPolicy pp) {
		em.merge(pp);
	}
	
	
	
	//MANAGE PASSWORD POLICY CONTAINERS
	@Deprecated
	public void persistPasswordPolicyContainerEntity(PasswordPolicyContainer pp) {
		em.persist(pp);
	}

	@Deprecated
	public List<PasswordPolicyContainer> loadAllPasswordPolicyContainers() {
		return em.createNamedQuery("passwordPolicyContainer.findAll").getResultList();
	}

	
	
	@Deprecated
	public boolean isPasswordPolicyContainerExistsByUniqueName(String uniqueName) {
		//Query database to see if user already exists...
		Query query = em.createNamedQuery("passwordPolicyContainer.findByUniqueName");
		List mailTemplates = query.setParameter("uniqueName", uniqueName).getResultList();

		//If no entry found, persist the entity, send a message to the view and clean the entity from the session by initializing a new one and refresh the DataModel list
		if (mailTemplates.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
	@Deprecated
	public void deletePasswordPolicyContainer(PasswordPolicyContainer pp) {
		log.info("Removing Password PolicyContainer with Unique Name '" + pp.getUniqueName());

		PasswordPolicyContainer mergedPasswordPolicyContainer = em.merge(pp);
		em.remove(pp);
		
	}
	
	@Deprecated
	public void mergePasswordPolicyContainer(PasswordPolicyContainer pp) {
		em.merge(pp);
	}
}