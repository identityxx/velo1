package velo.actions.readyActions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.seam.Component;

import velo.ejb.workflow.WfProcessManager;
import velo.entity.Account;
import velo.entity.Attribute;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that create a workflow process from a reconcile event 
 */
public class ReconcileCreateWorkflowProcess extends ReadyAction {
	private static Logger log = Logger.getLogger(ReconcileCreateWorkflowProcess.class.getName());
	
	public void validate() throws ActionValidationException {
		log.debug("Validating required params in context (resourceUniqueName, accontName");
		//verify that in context we have the account name and resource name
		if (!getContext().isVarExists("resourceUniqueName")) {
			throw new ActionValidationException("Could not find 'resourceUniqueName' variable in context.");
		}
		
		if (!getContext().isVarExists("accountName")) {
			throw new ActionValidationException("Could not find 'accountName' variable in context.");
		}
		log.debug("Successfully validated required params!.");
	}
	
	public void execute() throws ActionExecutionException {
		String defName = (String)getContext().get("processDefinitionName");
		
		WfProcessManager processManager = (WfProcessManager)Component.getInstance("wfProcessManager");
		
		
		//prepare the process vars.
		Map<String,Object> processVars = new HashMap<String,Object>();
		
		//try to make this as generic as possible. if account exist in context, then iterate over its attributes and add them as vars
		//with accattr_ prefix
		
		if ( (getContext().isVarExists("account")) && (getContext().get("account") instanceof Account) ) {
			Account acc = (Account)getContext().get("account");
			for (Attribute currAttr : acc.getAttributes().values()) {
				processVars.put("ACCATTR_" + currAttr.getUniqueName(),currAttr.getFirstValue().getValueAsObject());
			}
			
			processVars.put("accountName", acc.getName());
			processVars.put("resourceUniqueName", acc.getResource().getUniqueName());
			processVars.put("resourceDisplayName", acc.getResource().getDisplayName());
		}
		
		processManager.startProcess(defName, processVars);
		
		log.debug("Succesfully started process definition name '" + defName + "'"); 
	}
}
