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

import velo.entity.AccountsRequest;
import velo.entity.CreateUserRequest;
import velo.entity.Request;
import velo.entity.ResumeUserRequest;
import velo.entity.SelfServiceAccessRequest;
import velo.entity.SuspendUserRequest;
import velo.entity.User;
import velo.exceptions.OperationException;
import velo.exceptions.RequestAttributeValidationException;
import velo.exceptions.RequestCreationException;
import velo.exceptions.RequestExecutionException;

/**
 A RequestManager interface for all EJB exposed methods
 
 @author Asaf Shakarchi
 */
public interface RequestManager {
    
	public Request submitRequest(Request request) throws RequestCreationException;
	
	public void submitRequest(SuspendUserRequest suspendUserRequest) throws RequestCreationException;
	
	public void submitRequest(ResumeUserRequest resumeUserRequest) throws RequestCreationException;
	
	public SelfServiceAccessRequest submitRequest(SelfServiceAccessRequest selfServiceAccessRequest) throws RequestCreationException;
	
	public void submitRequest(AccountsRequest request) throws RequestCreationException;
	
	public CreateUserRequest submitRequest(CreateUserRequest request) throws RequestCreationException;
	
	public Request findRequest(Long requestId);
	
	public void process(Request request) throws OperationException;
	
	public void process(SuspendUserRequest request) throws OperationException;
	
	public void process(ResumeUserRequest request) throws OperationException;
	
	public void process(AccountsRequest request) throws OperationException;
	
	public void process(CreateUserRequest request) throws OperationException;
	
	
	
	
	
	
	public void finalApproveRequest(Request request, User approver) throws OperationException;
	public void finalRejectRequest(Request request, User rejecter);
	
	
	
	
	
	
	
	
	//request scanner methods
    public void changeRequestScannerMode() throws OperationException;
    public boolean isRequestScannerActivate();
	
	
	
	
	
	
	
	
	
	
	

	
	
	@Deprecated
    public void mergeRequestEntity(Request request);
    
    
    /**
     Create a new request entity
     @param request A request entity to persist
     @throws RequestAttributeValidationException
     */
	@Deprecated
    public Request createRequestEntity(Request request) throws RequestCreationException;
    
    
    /**
     Execute a request
     @param request The request entity to execute
     */
	@Deprecated
    public void executeRequest(Request request) throws RequestExecutionException;
    
    
    /**
     Approve the specified request entity
     @param requrest The request entity to approve
     @throws OperationException
     */
	@Deprecated
    public void processRequest(Request request) throws OperationException;
    
    
    
	@Deprecated
    public void createTimerScanner(long initialDuration, long intervalDuration);
    
	@Deprecated
    public void createTimerScanner() throws OperationException;
}
