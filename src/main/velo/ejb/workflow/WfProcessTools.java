package velo.ejb.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import velo.contexts.OperationContext;
import velo.ejb.interfaces.ActionManagerLocal;
import velo.entity.PersistenceAction;
import velo.entity.WorkflowScriptedAction;
import velo.exceptions.action.ActionExecutionException;

@Name("wfProecssTools")
public class WfProcessTools {
	@In
	ActionManagerLocal actionManager;
	
	@In
	ProcessInstance processInstance;
	
	public List<Comment> getProcessComments(ProcessInstance pi) {
		List<Comment> comments = new ArrayList<Comment>();
		Collection<TaskInstance> tasks = pi.getTaskMgmtInstance().getTaskInstances();
		
		for (TaskInstance ti : tasks) {
			comments.addAll(ti.getComments());
		}
		
		
		return comments;
	}
	
	public String invokeAction(String actionUniqueName) throws ActionExecutionException {
		//find the action in the DB
		PersistenceAction action = actionManager.findPersistenceAction(actionUniqueName);
		
		if (action == null) {
			throw new ActionExecutionException("Could not find action name '" + actionUniqueName + "' in repository");
		}
		//FIXME: Currently limited to workflow actions only, what about Ready Actions?
		if (!(action instanceof WorkflowScriptedAction)) {
			throw new ActionExecutionException("Action name '" + actionUniqueName + "' was found but is not a Workflow Scripted Action!");
		}
		
		
		OperationContext context = new OperationContext();
		context.addVar("pi",processInstance);
		context.addVar("piContext",processInstance.getContextInstance());
		
		//set the context to the action
		action.setContext(context);
		//invoke the action
		action._execute();
		
		if ( (action.getContext().isVarExists("outcome")) && ((String)action.getContext().getVars().get("outcome").getValue()).length() >0) {
			return (String)action.getContext().getVars().get("outcome").getValue();
		} else {
			return null;
		}
	}
}
