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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import velo.ejb.interfaces.ApproversGroupManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.ApproversGroup;
import velo.entity.User;
import velo.exceptions.OperationException;

@Stateless()
@Name("approversGroupManager")
@AutoCreate
public class ApproversGroupBean implements ApproversGroupManagerLocal {
	
	private static Logger log = Logger.getLogger(ApproversGroupBean.class.getName());
	
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;
	
	@EJB
	UserManagerLocal userManager;
	
	public ApproversGroup findApproversGroup(String uniqueName) {
		log.debug("Finding Approvers Group in repository with unique name '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("approversGroup.findByUniqueName").setParameter("uniqueName",uniqueName);
			ApproversGroup ag = (ApproversGroup) q.getSingleResult();
			
			for (User currUser : ag.getApprovers()) {
				try {
					userManager.loadUserAttributes(currUser);
				} catch (OperationException e) {
					log.error("Failed to load attributes for user '" + currUser.getName() + "', while loading attributes for all users in approvers group '" + ag.getDisplayName() + "', returning null");
					return null;
				}
			}
			
			
			return ag;
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Could not find any Approvers Group for unique name '" + uniqueName + "', returning null.");
			return null;
		}
	}
	
	public ApproversGroup findApproversGroupEagerly(String uniqueName) {
		ApproversGroup ag = findApproversGroup(uniqueName);
		
		if (ag != null) {
			ag.getApprovers().size();
			for (User currApprover : ag.getApprovers()) {
				currApprover.touchCollections();
			}
			
			ag.getRoleApproversGroups().size();
		}
		
		return ag;
	}
}
