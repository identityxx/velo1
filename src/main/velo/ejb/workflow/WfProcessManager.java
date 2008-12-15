package velo.ejb.workflow;

import java.util.Map;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.ResumeProcess;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.Jbpm;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;

import velo.entity.Role;
import velo.entity.User;

@Name("wfProcessManager")
@AutoCreate
public class WfProcessManager {
	@Logger
	Log log;
	
	@In
	FacesMessages facesMessages;
	
	@In
	User loggedUser;
	
	@In(value="org.jboss.seam.bpm.jbpm")
    private Jbpm jbpm;
    
    @In
    private JbpmContext jbpmContext;
	
    @In
	BusinessProcess businessProcess;
    
    @In(required=false) 
    ProcessInstance processInstance;
    
	
	public void startProcessBasedOnRole(Role role, String businessKey,Map<String,Object> processVars) {
		if (role.getWorkflowProcessDef() == null) {
			log.warn("Could not create a process based on role '#0' since role is not associated with any workflow process definition!",role.getName());
			return;
		}
		
		for (Map.Entry<String,Object> entry : processVars.entrySet()) {
			Contexts.getBusinessProcessContext().set(entry.getKey(), entry.getValue());
		}
		
		//TODO: What if a process was not associated to role?
		String processDefinitionName = role.getWorkflowProcessDef().getUniqueName();

		
		
		
		
		businessProcess.createProcess(processDefinitionName);
		
		
		
		//ProcessInstance process = jbpmContext.newProcessInstanceForUpdate(processDefinitionName);
		//process.setKey(businessKey);
		//process.signal();
	}
	
	
	public void startProcess(String processDefinitionName,Map<String,Object> processVars) {
		log.debug("Starting process of definition name '#0', with amount of vars' #1",processDefinitionName, processVars.size());
		for (Map.Entry<String,Object> entry : processVars.entrySet()) {
			Contexts.getBusinessProcessContext().set(entry.getKey(), entry.getValue());
		}
		
		businessProcess.createProcess(processDefinitionName);
		log.debug("Sucessfully created process.");
	}
	
	
	@ResumeProcess()
	public void endProcess() {
		//TODO: currently only the owner of the process can end the process.
		
		processInstance.end();
	}
}

