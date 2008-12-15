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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.annotations.bpm.ResumeProcess;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EmailManagerLocal;
import velo.ejb.interfaces.RequestManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.RequestHome;
import velo.entity.EmailTemplate;
import velo.entity.Request;
import velo.entity.RequestUserComment;
import velo.entity.SelfServiceAccessRequest;
import velo.entity.User;
import velo.entity.Request.RequestStatus;
import velo.exceptions.ExpressionCreationException;
import velo.exceptions.RequestCreationException;
import velo.tools.EdmEmailSender;

@Stateful
@Name("selfServiceAccessRequestActions")
public class SelfServiceAccessRequestActionsBean implements SelfServiceAccessRequestActions {
	//constants
	private final String requesterFinalRejectEmailTemplateName = "REQUESTER_FINAL_REQUEST_REJECT";
	private final String requesterFinalApproveEmailTemplateName = "REQUESTER_FINAL_REQUEST_APPROVAL";
	private final String approverNewApprovalTaskEmailTemplateName = "APPROVER_NEW_APPROVAL_TASK";
	private final String requesterSuccessRequestCreationEmailTemplateName = "REQUESTER_SUCCESS_REQUEST_CREATION"; 
	
	
	//@In(value="#{identity.username}")
	//String loggedUserName;
	
	@In
	User loggedUser;
	
	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	//@PersistenceContext
	@In
    public EntityManager entityManager;
    
	@EJB
    public UserManagerLocal userManager;
	
	@EJB
	public AccountManagerLocal accountManager;
	
	@EJB
	public RequestManagerLocal requestManager;
	
	@EJB
	public EmailManagerLocal emailManager;

	@Out(scope=ScopeType.CONVERSATION,required=false)
	private SelfServiceAccessRequest selfServiceRequest = new SelfServiceAccessRequest(); 
	
	@In(required=false) 
    ProcessInstance processInstance;
	
	@Out(scope=ScopeType.BUSINESS_PROCESS, required=false)
    long requestId;
	
	@In(create=true)
	RequestHome requestHome;
	
	@In(required=false, create=true, value="#{selfServiceHomePageActions}")
	SelfServiceHomePageActions selfServiceHomePageActions;

	@DataModel(value="ssMyLastRequestedRequests")
	List<Request> ssMyLastRequestedRequests;
	
	@DataModelSelection(value="ssMyLastRequestedRequests")
	Request selectedRequest;
	
	private String approvalTaskComment;
	
	/**
	 * @return the request
	 */
	public SelfServiceAccessRequest getRequest() {
		return selfServiceRequest;
	}


	/**
	 * @param request the request to set
	 */
	public void setRequest(SelfServiceAccessRequest selfServiceRequest) {
		this.selfServiceRequest = selfServiceRequest;
	}

	public String accessIsRequestedForMyself() {
		//try {
			//User myself = userManager.findUserByName(loggedUserName);
			//getRequest().setRequestedAccessForUser(myself);
			getRequest().setRequestedAccessForUser(loggedUser);
		//} catch (NoResultFoundException e) {
			//facesMessages.add(FacesMessage.SEVERITY_ERROR,"Cannot request access for myself, an internal error has occured while trying to load my user named: " + loggedUser.getName() + ", error message: " + e.toString());
			//return null;
		//}
		
		return "myself";
	}
	
	public String accessIsRequestedForOtherUser() {
		return "anotherUser";
	}
	
	public String setUserThatHasMyAccess(User user) {
		//Make sure that the selected user is not the same as the logged user
		if (loggedUser.getName().equalsIgnoreCase(user.getName())) {
			facesMessages.add(FacesMessage.SEVERITY_WARN, "Cannot request access as youself...");
			
			//there's no such a transition, so the current page will re-render.
			return "stay";
		}
		
		getRequest().setUserThatHasMyAccess(user);
		
		return null;
	}
	
	
	
	
	@CreateProcess(definition="approveSelfServiceRequest", processKey="#{selfServiceRequest.requestId}")
	@End
	public String submitRequest() {
		log.debug("Submitting self service reuqest...");
		try {
			//validate request
			if (! selfServiceRequest.isGrantAccessImmediately() ) {
				if ( (selfServiceRequest.getGrantAccessDate() == null) || (selfServiceRequest.getGrantAccessHour() == null) ) {
					facesMessages.add(FacesMessage.SEVERITY_WARN, "Please select 'immediate access' or select a required grant access date");
					return null;
				}
			}
			
			
			//User loggedUserFromSession = (User)Contexts.getSessionContext().get("loggedUser");
			//must to be managed, notifyEmail/Event Responses uses lazily collections (such as attrs)
			//User loggedUser = entityManager.find(User.class,loggedUserFromSession.getUserId());
			User loggedUserManaged = entityManager.find(User.class,this.loggedUser.getUserId());
			
			selfServiceRequest.setCreationDate(new Date());
			selfServiceRequest.setRequester(loggedUserManaged);
			log.debug("Requester is '#0'",loggedUserManaged.getName());
			selfServiceRequest.setNotes("Self Service access request");
			selfServiceRequest.setStatus(RequestStatus.PENDING_APPROVAL);
			

			Calendar c = Calendar.getInstance();
			if (!selfServiceRequest.isGrantAccessImmediately()) {
				log.trace("Grant access is not flagged immediately, calculating grant access date via specified date/time");
				//YUCK
				//TODO: This is a workaround, until a nice calendar gui component with timeselection will be available
				//calculate 'access grant from' date
				c.setTime(selfServiceRequest.getGrantAccessDate());
			
				log.trace("Grant access hour: #0, minutes: #1", selfServiceRequest.getGrantAccessHour(),selfServiceRequest.getGrantAccessMinutes()); 
				c.set(Calendar.HOUR, Integer.parseInt(selfServiceRequest.getGrantAccessHour()));
				c.set(Calendar.MINUTE, Integer.parseInt(selfServiceRequest.getGrantAccessMinutes()));
			} else {
				log.debug("No grant access date calculation is required, since grant access immediately was flagged");
			}
			
			selfServiceRequest.setGrantAccessDate(c.getTime());
			
			
			//replace with submitRequest method
			selfServiceRequest = requestManager.submitRequest(selfServiceRequest);
			facesMessages.add("Request successfully submitted!");
			
			selfServiceRequest.setInBusinessProcess(true);
			
			//outject the requestId to the business context
			requestId = selfServiceRequest.getRequestId();
			log.debug("Successfully submitted request ID '#0'", requestId);
			
			
			//send email
			notifyRequesterUponSuccessRequestCreation(selfServiceRequest);
			
			return "submitRequest";
		} catch (RequestCreationException e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Cannot create request: " + e.toString());
			
			//there's no such a transition, so the current page will re-render. 
			return "ERROR";
		}
	}
	
	
	
	//task workflow methods
	@BeginTask
	public void reviewWorkflowTask() {
		log.debug("Reviewing current task...");
		//@In annotation does not work, it cause an IllegalArgumentException when this bean is first accessed for some reason
		requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
		log.info("Injected request ID form business process: " + requestId);
	}
	
	@EndTask(transition="approve")
	public void approveWorkflowTask() {
		if (getApprovalTaskComment() != null) {
			if (getApprovalTaskComment().length() > 0) {
				User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
				requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
				requestHome = new RequestHome();
				requestHome.setRequestId(requestId);
				SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
				RequestUserComment ruc = RequestUserComment.factory(loggedUser, getApprovalTaskComment());
				ruc.setRequest(req);
				entityManager.persist(ruc);
				
				//fucken create two log instances, why? is something wrong with the cascading/relationships here?
				//but it is the same as request.logs, what's wrong?
				//req.addRequestUserComment(loggedUser, getApprovalTaskComment());
				//requestManager.mergeRequestEntity(req);
			}
		}
		
		log.info("Task approved!");
	}
	
	//@EndTask(transition="reject")
	public String rejectWorkflowTask() {
		log.warn("!!!!!!!!!!!!!!!: " + getApprovalTaskComment().length());
		if (getApprovalTaskComment().length() > 1) {
			User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
			requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
			requestHome = new RequestHome();
			requestHome.setRequestId(requestId);
			SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
			RequestUserComment ruc = RequestUserComment.factory(loggedUser, getApprovalTaskComment());
			ruc.setRequest(req);
			entityManager.persist(ruc);
			processInstance.signal("reject");
			facesMessages.add("Successfully rejected request!");
			log.info("Task rejected!");
			return "/ss/Home.seam";
		} else {
			facesMessages.add("Please specify reject reason.");
			return null;
		}
	}
	
	public void finalRequestApproval() {
		requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
		requestHome = new RequestHome();
		requestHome.setRequestId(requestId);
		SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
		req.setStatus(RequestStatus.APPROVED);
		
		
		
		//TODO: Move to some more dynamic code (logic here is too strict)
		//calculate expiration date (should be moved to somewhere else, maybe into scripts? so it can be calculated more dynamically?)
		//handle expiration access date
		Calendar expDateCal = Calendar.getInstance();
		if (req.isGrantAccessImmediately()) {
			if (req.getAccessExpirationDateAmountType().equals("HOUR")) {
				expDateCal.add(Calendar.HOUR, req.getAccessExpirationDateAmount());
			}
		} else {
			expDateCal.setTime(req.getGrantAccessDate());
			if (req.getAccessExpirationDateAmountType().equals("HOUR")) {
				expDateCal.add(Calendar.HOUR, req.getAccessExpirationDateAmount());
			}
		}
		
		req.setExpirationAccessDate(expDateCal.getTime());
		log.debug("Access will be expired at: " + req.getExpirationAccessDate());
		
		
		requestManager.mergeRequestEntity(req);
		
		
		//send notifications
		notifyRequesterUponFinalRequestApproval(req);
		
		
		log.info("Successfully performed final approval of request ID #0", requestId);
	}
	
	public void finalRequestReject() {
		requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
		requestHome = new RequestHome();
		requestHome.setRequestId(requestId);
		SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
		req.setStatus(RequestStatus.REJECTED);
		requestManager.mergeRequestEntity(req);
		
		//send notifications
		notifyRequesterUponFinalRequestReject(req);
		
		log.info("Successfully performed final reject of request ID #0", requestId);
	}
	
	
	//exposed for the workflow!
	public String[] assignPoolActors(Integer level) {
		log.debug("Determining request's pool actors for level: " + level);
		requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
		
		requestHome = new RequestHome();
		requestHome.setRequestId(requestId);
		SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
		
		
		//notify by email
		notifyApproverUponNewApprovalTask(req, level);
		
		return req.getPoolActors(level);
	}
	
	//exposed for the workflow!
	public String isThereApproversInLevel(Integer level) {
		log.debug("Determining whether there are approvers for approvers level #0",level);
		
		requestId = (Long)Contexts.getBusinessProcessContext().get("requestId");
		requestHome = new RequestHome();
		requestHome.setRequestId(requestId);
		
		SelfServiceAccessRequest req = (SelfServiceAccessRequest)requestHome.getInstance();
		
		if (req.isThereApproversInLevel(level)) {
			log.debug("There are approvers in level #0, returning 'yes'", level);
			return "yes";
		} else {
			log.debug("There are NO approvers in level #0, returning 'no'", level);
			return "no";
		}
	}
	
	
	
	
	
	
	
	//important, cannot pass only IDs as it is not secured
	@ResumeProcess(definition="approveSelfServiceRequest", processKey="#{ssMyLastRequestedRequests.rowData.requestId}")
	public void cancelRequest() {
		User loggedUser = (User)Contexts.getSessionContext().get("loggedUser");
		log.info("Logged in user obj: " + loggedUser);
		log.info("Request injected obj: " + selectedRequest);
		if (selectedRequest != null)
			log.info("Selected request ID: #0",selectedRequest.getRequestId());

		//security check: after reloading, make sure the requester of the request is really the logged in person
		Request r = entityManager.find(Request.class, selectedRequest.getRequestId());
		if (!r.getRequester().equals(loggedUser)) {
			//error!
			return;
		}
		
		r.setStatus(RequestStatus.CANCELLED);
		r.addLog("INFO", "Request was cancelled", "The request was cancelled by the requester!");
        processInstance.end();
        
        entityManager.merge(r);
        readMyLastRequestedRequests();
	}
	

	@Factory("ssMyLastRequestedRequests")
    public void readMyLastRequestedRequests() {
		//log.info("!!!!!!!!!!!!!!(1): " + selfServiceHomePageActions);
		//log.info("!!!!!!!!!!!!!!(2): " + selfServiceHomePageActions.getMyLastRequestedRequestsList());
		selfServiceHomePageActions.getMyLastRequestedRequestsList().refresh();
		ssMyLastRequestedRequests = selfServiceHomePageActions.getMyLastRequestedRequestsList().getResultList();
    }
	
	

	/**
	 * @return the approvalTaskComment
	 */
	public String getApprovalTaskComment() {
		return approvalTaskComment;
	}


	/**
	 * @param approvalTaskComment the approvalTaskComment to set
	 */
	public void setApprovalTaskComment(String approvalTaskComment) {
		this.approvalTaskComment = approvalTaskComment;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void notifyRequesterUponSuccessRequestCreation(SelfServiceAccessRequest request) {
		notifyRequester(requesterSuccessRequestCreationEmailTemplateName, request);
	}
	
	private void notifyRequesterUponFinalRequestApproval(SelfServiceAccessRequest request) {
		notifyRequester(requesterFinalApproveEmailTemplateName, request);
	}
	
	private void notifyRequesterUponFinalRequestReject(SelfServiceAccessRequest request) {
		notifyRequester(requesterFinalRejectEmailTemplateName, request);
	}
	
	private void notifyApproverUponNewApprovalTask(SelfServiceAccessRequest request, int level) {
		EmailTemplate et = emailManager.findEmailTemplate(approverNewApprovalTaskEmailTemplateName);
		
		if (et == null) {
			log.warn("Could not send mail, expected email template named #0 does not exist", approverNewApprovalTaskEmailTemplateName);
			return;
		}
		EdmEmailSender ees = new EdmEmailSender();
		
		
		et.addContentVar("requester", request.getRequester());
		et.addContentVar("request", request);
		
		String parsedContent = null;
		for (User currApprover : request.getApprovers(level)) {
			et.addContentVar("approver",currApprover);
			
			try {
				parsedContent = et.getParsedContent();
			}catch (ExpressionCreationException e) {
				log.warn("Could not parse expressions in email template #0: #1", et.getName(), e.toString());
				log.warn("Continuing to notify the next approver...");
				continue;
			}
			
			try {
				Email email = ees.factoryEmail(et.getSubject(), parsedContent);
				
				String currApproversEmailAddress = currApprover.getEmail();
				if (currApproversEmailAddress != null) {
					email.addTo(currApproversEmailAddress);
					ees.addEmail(email);
				} else {
					log.info("Approver #0 has no email address, skipping approver notification...", currApprover.getName());
				}
			} catch (EmailException e) {
				log.info("Skipping approver '#0' notification: #1", currApprover.getName(), e.toString());
			}
		}
		
		log.debug("Prepared #0 amount of emails to send, sending mails...");
		ees.send();
	}
	
	
	
	
	
	private void notifyRequester(String emailTemplateName, SelfServiceAccessRequest request) {
		EmailTemplate et = emailManager.findEmailTemplate(emailTemplateName);
		if (et == null) {
			log.warn("Could not send mail, expected email template named #0 does not exist", emailTemplateName);
			return;
		}
		
		EdmEmailSender ees = new EdmEmailSender();
		String emailAddress = request.getRequester().getEmail();

		//set email vars
		et.addContentVar("requester", request.getRequester());
		et.addContentVar("request", request);
		
		String parsedContent = null;
		try {
			parsedContent = et.getParsedContent();
		}catch (ExpressionCreationException e) {
			log.warn("Could not parse expressions in email template #0: #1", et.getName(), e.toString());
		}
		
		if (emailAddress == null) {
			log.warn("Could not send mail, user #0 does not have an email address!",request.getRequester().getName());
			return;
		}
		try {
			ees.addHtmlEmail(emailAddress, et.getSubject(), parsedContent);
			ees.send();
		} catch (EmailException e) {
			log.error("Could not send email: #0",e.toString());
		}
	}
	
	
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
