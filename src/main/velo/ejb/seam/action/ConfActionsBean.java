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
package velo.ejb.seam.action;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.quartz.Scheduler;

import velo.common.SysConf;
import velo.ejb.interfaces.ConfManagerLocal;
import velo.exceptions.OperationException;
import velo.exceptions.SynchronizationException;

@Stateful
@Name("confActions")
public class ConfActionsBean implements ConfActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@PersistenceContext
	public EntityManager em;
	
	@EJB
	public ConfManagerLocal confManager;

	
	public void refreshSysConf() {
		SysConf.refresh();
		
		facesMessages.add("Successfully refreshed system configuration");
	}
	
	
	public void saveSysConf() {
		try {
			log.debug("START: SAVE XML METHOD");
			javax.faces.component.UIViewRoot viewRoot = javax.faces.context.FacesContext
					.getCurrentInstance().getViewRoot();
			java.util.List<javax.faces.component.UIComponent> components = viewRoot
					.getChildren();

			javax.faces.component.UIComponent form = viewRoot.findComponent("xmlSysConfForm");

			javax.faces.component.UIComponent mainXmlConf = form.findComponent("mainXmlConf");
			velo.uiComponents.XMLManager xmlManager = (velo.uiComponents.XMLManager) mainXmlConf;
			xmlManager.saveDataByFile();
			
			log.debug("END: SAVE XML METHOD");
			facesMessages.add("Successfully updated system configuration");
		} catch (ConfigurationException ex) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,ex.toString());
		}
	}
	
	public String importInitialData() {
		if (!confManager.isInitialDataImported()) {
			confManager.persistInitialtData();
			facesMessages.add("Successfully imported initial data!");
			facesMessages.add("Please login with credentials: admin/admin");
			return "/admin/Home.xhtml";
		} else {
			facesMessages.add("Initial data is already imported!");
			return null;
		}
	}

	public String importTestData() {
		confManager.persistTestData();
		facesMessages.add("Successfully imported test data!");
		return "/admin/Home.xhtml";
	}
	
	
	public void generateResourcePrincipalsEncryptionKey() {
		try {
			confManager.generateResourcePrincipalsEncryptionKey();
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Successfully enrolled resource principals encryption key");
		}catch(OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	/*Irrelevant as user passwords are hashed
	public void generateUsersLocalPasswordsEncryptionKey() {
		try {
			confManager.generateUsersLocalPasswordsEncryptionKey();
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Successfully enrolled users local passwords encryption key");
		}catch(OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	*/
	
	
	
	
	
	
	
	
	
	
	public void syncReconcileEvents() {
		try {
			confManager.syncReconcileEvents();
			facesMessages.add("Successfully synchronized reconcile events!");
		} catch (SynchronizationException e) {
			facesMessages.add("Could not synchronize reconcile events: " + e.getMessage());
		}
	}
	
	
	public void syncSystemEvents() {
		try {
			confManager.syncSystemEvents();
			facesMessages.add("Successfully synchronized system events!");
		} catch (SynchronizationException e) {
			facesMessages.add("Could not synchronize system events: " + e.getMessage());
		}
	}
	
	public void syncReadyActions() {
		try {
			confManager.syncReadyActions();
			facesMessages.add("Successfully synchronized ready actions!");
		} catch (SynchronizationException e) {
			facesMessages.add("Could not synchronize ready actions: " + e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void test() {
		try {
			InitialContext ctx = new InitialContext();
			Scheduler sched = (Scheduler) ctx.lookup("Quartz");
			
			System.out.println("SCHEDULER IS: " + sched);
			System.out.println("SCHEDULER NAME IS: " + sched.getSchedulerName());
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Configuration getVeloConfig() {
		return SysConf.getSysConf();
	}
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
