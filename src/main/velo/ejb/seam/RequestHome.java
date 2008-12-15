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
package velo.ejb.seam;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;

import velo.entity.Request;

@Name("requestHome")
public class RequestHome extends EntityHome<Request> {
	private static final String LOGGED_USER = "loggedUser";
	
	@In
	FacesMessages facesMessages;

	public void setRequestId(Long id) {
		setId(id);
	}

	public Long getRequestId() {
		return (Long) getId();
	}

	@Override
	protected Request createInstance() {
		Request request = new Request();
		return request;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public Request getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	
	
	
	/*
	public String approveRequests() {
        int requstsToApproveSize = 0;
        for (Request currRequest : requestList) {
            Boolean selected = requestSelection.get(currRequest);
            if (selected != null && selected) {
                // getCustomerSelectionBool().put(currCust, false);
                logger.fine("Requested to approve request ID: '" + currRequest.getRequestId() + "'");
                try {
                    approveRequest(currRequest);
                    requstsToApproveSize++;
                } catch (OperationException oe) {
                    //A message already thrown by 'approveRequest' method
                    //FacesMessages.instance().add("Error occured while trying to approv request ID: '" + selectedRequest.getRequestId() + "', reason: " + oe);
                    //continue;
                    continue;
                }
            }
        }
        
        if (requstsToApproveSize < 1) {
            FacesMessages.instance().add(FacesMessage.SEVERITY_WARN, "No requests were successfully approved.");
        } else {
            FacesMessages.instance().add(FacesMessage.SEVERITY_INFO, "Successfully approved '" + requstsToApproveSize + "' requests!");
        }
        
        return null;
    }
    */
	
	/*
	public String rejectRequests() {
        int requstsToRejectSize = 0;
        for (Request currRequest : requestList) {
            Boolean selected = requestSelection.get(currRequest);
            if (selected != null && selected) {
                requstsToRejectSize++;
                // getCustomerSelectionBool().put(currCust, false);
                logger.fine("Requested to reject request ID: '" + currRequest.getRequestId() + "'");
                rejectRequest(currRequest);;
            }
        }
        
        if (requstsToRejectSize < 1) {
            FacesMessages.instance().add(FacesMessage.SEVERITY_WARN,"No requests were flagged to be rejected.");
        } else {
            FacesMessages.instance().add(FacesMessage.SEVERITY_INFO,"Successfully rejected '" + requstsToRejectSize + "' requests!");
        }
        
        return null;
    }
    */
}
