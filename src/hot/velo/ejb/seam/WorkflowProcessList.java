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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

@Name("workflowProcessList")
public class WorkflowProcessList extends JbpmEntityQuery<ProcessInstance> {
	
	@In
	JbpmContext jbpmContext;
	
	private Long processId;
	private boolean ended;
	private boolean suspended;
	private Date startDate;
	private Date endDate;
	private String processDefinitionName;
	private String status; 
	private String processVarNamesForCurrentSelectedProcessDef;
	private String processVarName;
	
	@Out(scope=ScopeType.CONVERSATION,required=false)
	private ProcessInstance selectedProcessInstance;
	
	
	//TODO: Search per variable.
	
	private static final String[] RESTRICTIONS = {
		"pi.id = #{workflowProcessList.processId}",
		"pi.start >= #{workflowProcessList.startDate}",
		"pi.start <= #{workflowProcessList.endDate}",
		"pi.isSuspended = #{workflowProcessList.suspended}",
		"pi.processDefinition.name = #{workflowProcessList.processDefinitionName}",
	};

	private static final String EJBQL = "select pi from org.jbpm.graph.exe.ProcessInstance pi";

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}
	

	@Override
	public Integer getMaxResults() {
		return 25;
	}
	
	public List<String> getDyamicExpressions() {
		List<String> strArr = new ArrayList<String>();
		
		if ( (status == null) || (status.equals("ALL")) ) {
			
		} else if (status.equals("FINISHED")) {
			strArr.add("pi.end is not NULL");
		} else if (status.equals("ACTIVE")) {
			strArr.add("pi.end is NULL");
		}
		
		return strArr;
	}

	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	//getRestrictionExpressionStrings().addAll(Arrays.asList(getDyamicExpressions()));

    	setEjbql(EJBQL);
    }
	

	/*
	public List<ProcessDefinition> getProcesses() {
		return jbpmContext.getGraphSession().findAllProcessDefinitions();
	}
	*/
	
	
	/*
	 * 
	
	public List<ProcessInstance> getDefinitions() {
		try {
		      Query query = jbpmContextsession.getNamedQuery("GraphSession.findAllProcessDefinitions");
		      return query.list();
		    } catch (Exception e) {
		      log.error(e);
		      jbpmSession.handleException();
		      throw new JbpmException("couldn't find all process definitions", e);
		    } 
	}
	*/
	
	
	//FIXME: Should be under WorkflowProcessDefList but it's here because it's loaded by hibernate's JBPM session
	@Factory("latestProcessDefinitions")
	@Deprecated
	public List<ProcessDefinition> getLatestProcessDefinitions() {
		return jbpmContext.getGraphSession().findLatestProcessDefinitions();
	}

	
	@Factory("latestProcessDefinitionNames")
	public Map<String,String> getLatestProcessDefinitionNames() {
		Map<String,String> defMap = new HashMap<String,String>();
		
		for (Object currPDObj : jbpmContext.getGraphSession().findLatestProcessDefinitions()) {
			ProcessDefinition currPD = (ProcessDefinition)currPDObj;
			
			defMap.put(currPD.getName(), currPD.getName());
		}
		
		return defMap;
	}
	
	/*
	public List<String> getProcessVarNamesForCurrentSelectedProcessDef() {
		List<String> vars = new ArrayList<String>();
		
		ProcessDefinition pd = jbpmContext.getGraphSession().findLatestProcessDefinition(processDefinitionName);
	}
	*/
	
	
	
	
	
	
	
	
	
	
	
	/*
	
	@Override
	@Transactional
	public List<ProcessInstance> getResultList() {
		//List<String> blah = getRestrictionExpressionStrings();
		//blah.addAll(getDyamicExpressions());
		//setRestrictionExpressionStrings(blah);
		
		
		return super.getResultList();
	}
	*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public JbpmContext getJbpmContext() {
		return jbpmContext;
	}

	public void setJbpmContext(JbpmContext jbpmContext) {
		this.jbpmContext = jbpmContext;
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getProcessDefinitionName() {
		return processDefinitionName;
	}

	public void setProcessDefinitionName(String processDefinitionName) {
		this.processDefinitionName = processDefinitionName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	public ProcessInstance getSelectedProcessInstance() {
		return selectedProcessInstance;
	}

	public void setSelectedProcessInstance(ProcessInstance selectedProcessInstance) {
		this.selectedProcessInstance = selectedProcessInstance;
	}

	@Begin
	public String select() {
		setSelectedProcessInstance(getDataModelSelection());
		return "/admin/Process.xhtml";
		
		//System.out.println("!!!!!!!!!!!!!!!!!!!!!!: " + getDataModelSelection().getId());
	}
	
}
