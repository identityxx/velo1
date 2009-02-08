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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.exe.ProcessInstance;

import velo.security.Authenticator;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.SelfServiceMyRequestedRequestList;
import velo.entity.User;

@Name("selfServiceHomePageActions")
public class SelfServiceHomePageActionsBean {
	
	@In(value="#{identity.username}")
	String loggedUserName;
	
	@Logger
	private Log log;
	
	@In
	FacesMessages facesMessages;
	
	//@In(create=true) //not sure, this cause errors of getEjbl in the list object, inited in the 'get' method in this class
	SelfServiceMyRequestedRequestList selfServiceMyRequestedRequestList;

	@In
	Identity identity;
	
	@In(create=true)
	Authenticator authenticator;
	
	/*
	public SelfServiceMyRequestedRequestList getMyLastRequestedRequestsList() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		selfServiceMyRequestedRequestList = new SelfServiceMyRequestedRequestList();
		selfServiceMyRequestedRequestList.setMaxResults(5);
		selfServiceMyRequestedRequestList.setRequester(loggedUser);
		
		return selfServiceMyRequestedRequestList; 
	}
	*/
	
	
	public void jbpmCancelProcess(ProcessInstance pi) {
		//cancel the process that is
		facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO, "Successfully canceled process id #0", pi.getId());
		pi.end();
	}
	
	public String logout() {
		identity.logout();
		
		return "/SSGoodBye.xhtml";
	}
	
	
	@Destroy
	@Remove
	public void destroy() {
	}
}
