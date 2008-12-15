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
package velo.ejb.impl;

import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.Jbpm;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.jpdl.JpdlException;

import velo.entity.WorkflowProcessDef;
import velo.exceptions.OperationException;

//@Stateless()
@Name("workflowManager")
public class WorkflowBean {//implements WorkflowManagerLocal {
	@Logger
	private Log log;
	
	@In(value="org.jboss.seam.bpm.jbpm")
    private Jbpm jbpm;
    
    @In
    private JbpmContext jbpmContext;
    

    @Transactional(TransactionPropagationType.SUPPORTS)
    public void createJbpmSchema() throws Exception {
    	System.out.println("!!!!!!!!!!!!!!!!STARTTTTTT: " + Transaction.instance().isActive());
    	Transaction.instance().commit();
    	System.out.println("!!!!!!!AFER COMMIT!!!!!!!!!: " + Transaction.instance().isActive());
    	
    	JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
    	System.out.println("!!!!!!!!!!!!!!!! after getting JBPM CONF: " + Transaction.instance().isActive());
    	jbpmConfiguration.createSchema();
    	System.out.println("!!!!!!!!!!!!!!!! AFTER CREATING SCHEMA: " + Transaction.instance().isActive());
    	//jbpmConfiguration.createSchema();
    	//jbpmContext.getJbpmConfiguration().createSchema();
    	
    }
    
	public void deployProcessDefinition(String xmlProcessDefinition, WorkflowProcessDef wfProcessDefEntity) throws OperationException {
		try {
			ProcessDefinition pd = jbpm.getProcessDefinitionFromXml(xmlProcessDefinition);
			wfProcessDefEntity.setUniqueName(pd.getName());
			wfProcessDefEntity.setVersion(pd.getVersion());
			wfProcessDefEntity.setDescription(pd.getDescription());
			log.trace("Deploying workflow process definition name #0",pd.getName());
			jbpmContext.deployProcessDefinition(pd);
			wfProcessDefEntity.setProcessDefEngineKey(String.valueOf(pd.getId()));
		//}catch (JpdlException e) {
		}catch (Exception e) {
			throw new OperationException("Could not deploy process definition: " + e.getMessage());
		}
	}
	
	public void undeployProcessDefinition(WorkflowProcessDef wfProcessDefEntity) throws OperationException {
		Long l = Long.parseLong(wfProcessDefEntity.getProcessDefEngineKey());
		GraphSession graphSession=jbpmContext.getGraphSession();
		ProcessDefinition processDefinition=graphSession.getProcessDefinition(l);
		if (processDefinition != null) {
			graphSession.deleteProcessDefinition(processDefinition);
		} else {
			throw new OperationException("Could not find process definition with (engine) key ID: " + l);
		}
	}
	
	public ProcessDefinition getProcessDefinition(WorkflowProcessDef wfProcessDefEntity) {
		Long l = Long.parseLong(wfProcessDefEntity.getProcessDefEngineKey());
		GraphSession graphSession=jbpmContext.getGraphSession();
		ProcessDefinition processDefinition=graphSession.getProcessDefinition(l);
		
		return processDefinition;
	}
	
	public void updateProcessDefinition(String xmlProcessDefinition, WorkflowProcessDef wfProcessDefEntity) throws OperationException {
		Long l = Long.parseLong(wfProcessDefEntity.getProcessDefEngineKey());
		GraphSession graphSession=jbpmContext.getGraphSession();
		ProcessDefinition processDefinition=graphSession.getProcessDefinition(l);
		if (processDefinition != null) {
			ProcessDefinition newPD = processDefinition.parseXmlString(xmlProcessDefinition);
			processDefinition.setProcessDefinition(newPD);
			
			jbpmContext.getSession().merge(processDefinition);
		} else {
			throw new OperationException("Could not find process definition with (engine) key ID: " + l);
		}
	}
	
	
	public ProcessInstance getProcessInstance(Long id) {
		System.out.println("!!!!!!!!!!");
		JbpmContext context = Jbpm.instance().getJbpmConfiguration().createJbpmContext();
		log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!: " + context);
		try {
			log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!: " + context.getConnection().isClosed());
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return context.getProcessInstance(id);
	}
}
