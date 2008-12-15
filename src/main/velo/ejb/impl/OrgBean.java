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

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.OrgManagerLocal;
import velo.ejb.interfaces.OrgManagerRemote;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Position;
import velo.exceptions.CannotFindOrgUnitException;
import velo.organizational.OrgUnit;
import velo.organizational.TopOrgUnit;

/**
 * A Stateless EJB bean for managing Organizational Units and Positions
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
public class OrgBean implements OrgManagerLocal, OrgManagerRemote {

	private static Logger logger = Logger.getLogger(OrgBean.class.getName());
	
	/**
	 * Injected entity manager
	 */
	//@PersistenceContext
	@PersistenceContext
	public EntityManager em;

	/**
	 * Inject a local UserManager EJB  
	 */
	@EJB
	public UserManagerLocal um;

	@EJB
	public ResourceManagerLocal tsm;
	
	@EJB
	public AccountManagerLocal am;
	
	@EJB
	CommonUtilsManagerLocal cum;
	
	@EJB
	TaskManagerLocal tm;
	

	@Deprecated
	public Position findPositionById(Long positionId) {
		try {
			return (Position) em.createNamedQuery("position.findById")
					.setParameter("positionId", positionId).getSingleResult();
			//return (Position) em.createQuery("SELECT object(position) FROM Position position WHERE position.name = qflowPosition").getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultException(
					"No such result exception occured: Couldnt load Position for ID number: "
							+ positionId);
		} catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
	}
	
	@Deprecated
	public Position findPositionByUniqueId(String uniqueId) {
		try {
			return (Position) em.createNamedQuery("position.findByUniqueId")
					.setParameter("uniqueId", uniqueId).getSingleResult();
			//return (Position) em.createQuery("SELECT object(position) FROM Position position WHERE position.name = qflowPosition").getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultException(
					"No such result exception occured: Couldnt load Position for UNIQUE ID number: "
							+ uniqueId);
		} catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
	}

	@Deprecated
	public Position findPositionByName(String positionName) {
		try {
			return (Position) em.createNamedQuery("position.findPositionByName")
					.setParameter("positionName", positionName).getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultException("Couldnt find Position for Position Name: "
					+ positionName);
		} catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
	}

	@Deprecated
	public void createPositionEntity(Position position) {
		logger.info("Creating Position Entity with unique ID: '" + position.getUniqueIdentifier() + "', with name: '" + position.getDisplayName() + "'");
		em.persist(position);
	}

	@Deprecated
	public void removePositionEntity(Position position) {
		logger.info("Removing Position Entity with unique ID: '" + position.getUniqueIdentifier() + "', with name: '" + position.getDisplayName() + "'");
		Position managedPosition = em.merge(position);
		em.remove(managedPosition);
	}
	

	@Deprecated
	public void updatePositionEntity(Position position) {
		logger.info("Updating Position Entity with unique ID: '" + position.getUniqueIdentifier() + "', with name: '" + position.getDisplayName() + "'");
		em.merge(position);
	}
	
	@Deprecated
	public TopOrgUnit getOrganizationaChart() {
		TopOrgUnit tou = new TopOrgUnit("e-dentity");
		tou.construct();
		
		return tou;
	}
	
	@Deprecated
	public OrgUnit getOrgUnitOfPosition(Position position) throws CannotFindOrgUnitException {
		try {
			return getOrganizationaChart().findOrgUnitByUniqueId(position.getUniqueOrgUnitId());
		}
		catch (CannotFindOrgUnitException cfoue) {
			throw (cfoue);
		}
	}
}
