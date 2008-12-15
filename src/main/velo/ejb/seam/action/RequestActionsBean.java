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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.annotations.bpm.ResumeProcess;
import org.jboss.seam.annotations.bpm.StartTask;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import velo.ejb.interfaces.RequestManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.seam.TaskList;
import velo.entity.Request;
import velo.entity.User;
import velo.exceptions.OperationException;

@Stateful
@Name("requestActions")
public class RequestActionsBean implements RequestActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@EJB
	public RoleManagerLocal roleManager;
	
	@EJB
	public RequestManagerLocal requestManager;
	
	@In(value="#{requestHome.instance}")
	public Request request;
	
	//WARNING: never access directly, only use the accessor!
	@In(value="#{requestList.requestSelection}")
	Map<Request, Boolean> requestSelection;

	public void finallyApproveRequest() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		
		try {
			requestManager.finalApproveRequest(request, loggedUser);
			facesMessages.add("Successfully performed final approval for Request Id #0",request.getRequestId());
		} catch (OperationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR,"Could not perform final approval for request ID #0: #1", request.getRequestId(), e.toString());
		}
	}
	
	public void finallyRejectRequest() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		facesMessages.add("Successfully performed final reject for Request Id #0",request.getRequestId());
		
		requestManager.finalRejectRequest(request, loggedUser);
	}
	
	
	
	public void multipleFinallyRejectRequest() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		Set<Request> requests = getRequestSelection();
		log.debug("Selected #0 requests to reject by admin user #1, performing action...",requests.size(),loggedUser.getName());
		for (Request currReq : requests) {
			requestManager.finalRejectRequest(currReq, loggedUser);
		}
		
		facesMessages.add("Finished requesting final reject for the selected requests.");
		clearSelection();
		
	}
	
	public void multipleFinallyApproveRequest() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		Set<Request> requests = getRequestSelection();
		log.debug("Selected #0 requests to approval by admin user #1, performing action...",requests.size(),loggedUser.getName());
		for (Request currReq : requests) {
			try {
				requestManager.finalApproveRequest(currReq, loggedUser);
			} catch (OperationException e) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR,"Failed tot perform final approval for request ID #0: #1", request.getRequestId(), e.toString());
			}
		}
		
		facesMessages.add("Finished requesting final approval for the selected requests.");
		clearSelection();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//scanner
    public void changeRequestScannerMode() {
        try     {
            requestManager.changeRequestScannerMode();
            facesMessages.add("Successfully changed Request Scanner status...");
        } catch (OperationException ex) {
            facesMessages.add(FacesMessage.SEVERITY_WARN, "Cannot change Request-Scanner status due to: " + ex.getMessage());
        }
    }
    
    public boolean isRequestScannerActivate() {
        return requestManager.isRequestScannerActivate();
    }
	
	
    
    
    
    
    //Accessors
	public Set<Request> getRequestSelection() {
		Set<Request> requests = new HashSet<Request>();
		
		//should never happen, handled by seam's @In annotation
		if (requestSelection != null) {
			log.debug("Printing Map With Requests Selection #0", requestSelection);
			for (Map.Entry<Request, Boolean> requestEntry : requestSelection.entrySet()) {
				if (requestEntry.getValue()) {
					requests.add(requestEntry.getKey());
				}
			}
		}
		
		return requests;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@In(create=true, value="#{taskList}")
	TaskList failedTaskList;
	
	
	
	
	
	
	
	
	@In(required=false) 
    ProcessInstance processInstance;
	
	@CreateProcess(definition="approveSelfServiceRequest", processKey="1")
	public void submit() {
		facesMessages.add("Submitted process!");
	}
	
	@ResumeProcess(definition="approveSelfServiceRequest", processKey="1")
	public void approveRequest() {
		System.out.println("KEY IS: " + processInstance.getKey());
		processInstance.signal("reject");
	}
	
	
	public String[] getPooledActors() {
       return new String[] { "managers_approvers", "admins" };
    }
	
	@StartTask
	public void startTask() {
		
	}
	
	@EndTask
	public void endTask() {
		
	}
	
	
	@Destroy
	@Remove
	public void destroy() {
	}
	
	public void clearSelection(){
		if (requestSelection != null) {
			log.debug("Clearing the requests selection");
			for (Map.Entry<Request, Boolean> requestEntry : requestSelection.entrySet()) {
				if (requestEntry.getValue()) {
					requestEntry.setValue(false);
				}
			}
		}
	}

}
