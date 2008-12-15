package velo.ejb.workflow;

import java.util.Date;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.PooledActor;
import org.jbpm.taskmgmt.exe.TaskInstance;

import velo.entity.User;
import velo.wf.WfComment;
import velo.wf.WfComments;

@Name("wfNodesManager")
public class ApprovalNodesManager {
	@In(required=false) 
    ProcessInstance processInstance;
	
	@Logger
	Log log;
	
	//expecting a task instance to be already available in the context
	@In(required=false)
	TaskInstance taskInstance;
	
	@In(required=false)
	String wfActiveApprovalComment;
	
	@In(scope=ScopeType.BUSINESS_PROCESS, required=false)
	WfComments wfApprovalComments;
	
	//@RequestParameter
	String transition;
	//@RequestParameter
	String nextPage;
	
	@In
	FacesMessages facesMessages;
	
	@In
	User loggedUser;
	
	//public void reviewApprovalNode()
	@BeginTask()
	//wtf?!?! with 'String' as a return result of this method causes the instance of TaskInstance & processInstnace to be null when a method later on is invoked (such as approve/reject/etc...) 
	//public String loadTaskNode() {
	public void loadTaskNode() {
		log.info("Loading selected task (ID: '#0', of process ID '#1', process def description: '#2') to the conversation scope and starting a conversation now...!", taskInstance.getId(), processInstance.getId(), processInstance.getProcessDefinition().getDescription());
		
		
		//SECURITY CHECK - make sure that the user can load the task, otherwise throw an exception and log the incident. 
		//check whether the actor ID equals to the user name
		boolean securityVerification = false;
		if ( (taskInstance.getActorId() != null) && (taskInstance.getActorId().length() > 0) ) {
			if (taskInstance.getActorId().equals(loggedUser.getName())) {
				log.debug("Actor ID of task instance ID #0 is associated to actor id that equals to the logged user name (#1)", taskInstance.getId(), loggedUser.getName());
				securityVerification = true;
			}
		}

		
		for (Object pooledActorName : taskInstance.getPooledActors()) {
			PooledActor currPooledActor = (PooledActor)pooledActorName;
			if (loggedUser.isMemberOfApproversGroup(currPooledActor.getActorId())) {
				log.debug("Logged User (#0) is associated with pooled actor group (#1) that is assigned to the current task instance to load with ID #2",loggedUser.getName(), currPooledActor.getActorId(), taskInstance.getId());
				securityVerification = true;
				break;
			}
		}
		
		if (securityVerification) {
			log.debug("User #0 is verified to load/approve task instance ID #1...!",loggedUser.getName(),taskInstance.getId());
		} else {
			log.warn("User #0 has tried to load task instance ID #1 but is not allowed to do so!",loggedUser.getName(),taskInstance.getId());
			facesMessages.add(FacesMessage.SEVERITY_WARN,"You are not allowed to load this task, the incident has been reported!");
			
			if (Contexts.isConversationContextActive()) {
				Contexts.getConversationContext().set("processInstance",null);
				Contexts.getConversationContext().set("taskInstance",null);
			} else {
				log.error("Should never happen!!!!");
			}
		}
		
		//return nextPage;
	}
	
	@EndTask(transition="#{wfNodesManager.transition}")
	public String moveNext() {
		log.debug("moveNext() was invoked for current task, moving to transition #{wfApprovalNodesManager.transition}");
		return redirect();
	}
	
	@EndTask(transition="approved")
	public String approve() {
		log.info("User '#0' is approving task ID '#1', of process ID: '#2'", loggedUser.getName(), taskInstance.getId(), processInstance.getId());
		facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,"ss.workflow.task.message.successfulyApprovedTask",loggedUser.getFullName());
		
		addComment(wfActiveApprovalComment);
		//return redirect();
		return "/ss/Home.xhtml";
	}
	
	@EndTask(transition="rejected")
	public String reject() {
		facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_INFO,"ss.workflow.task.message.successfulyRejectedTask",loggedUser.getFullName());
		log.info("User '#0' is rejecting task ID '#1', of process ID: '#2'", loggedUser.getName(), taskInstance.getId(), processInstance.getId());
		//return redirect();
		//return null;
		
		addComment(wfActiveApprovalComment);
		return "/ss/Home.xhtml";
	}

	
	public String redirect() {
		log.debug("Redirecting to next page which is: '#0'",nextPage);
		if (nextPage != null) {
			return nextPage;
		} else {
			facesMessages.add(FacesMessage.SEVERITY_WARN, "Next page was not set.");
			return null;
		}
	}

	
	
	
	public void addComment(String comment) {
		if (log.isTraceEnabled()) {
			dumpCurrentApprovalComments();
		}

		log.debug("Determining whether an approval comment was set or not.");
		
		boolean commentSpecified = false;
		
		if (wfActiveApprovalComment != null) {
			if (wfActiveApprovalComment.length() > 1) {
				commentSpecified = true;
			}
			
			if (!commentSpecified) {
				log.debug("No approval comment was specified");
				return;
			}
		}
		
		
		log.debug("An approval comment was set with value: '#0'",wfActiveApprovalComment);
			
		if (wfApprovalComments == null) {
			log.trace("Creating an approval comments list as it doesnt exist yet in process.");
			wfApprovalComments = new WfComments();
		}
		
		wfApprovalComments.add(new WfComment(new Date(),wfActiveApprovalComment,loggedUser.getFullName(),loggedUser.getName()));
		Contexts.getBusinessProcessContext().set("wfApprovalComments",wfApprovalComments);
		wfActiveApprovalComment = null;
	}
	
	public void dumpCurrentApprovalComments() {
		log.trace("Dumping current approval comments in process ID '#0'",processInstance.getId());
		if (wfApprovalComments != null) {
			log.trace("Amount of approval comments in process '#0'",wfApprovalComments.size());
			int i=0;
			for (WfComment currComment : wfApprovalComments) {
				i++;
				log.trace("Comment[#0]: #1",i,currComment);
			}
		} else {
			log.trace("No approval comments exist in WF!");
		}
		
		log.trace("Finished dumping current approval comments in process ID '#0'",processInstance.getId());
	}
	
	/**
	 * @return the nextPage
	 */
	public String getNextPage() {
		return nextPage;
	}

	/**
	 * @param nextPage the nextPage to set
	 */
	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}
}

