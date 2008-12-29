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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.framework.EntityQuery;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.ProcessDefinition;

import velo.entity.Resource;
import velo.entity.WorkflowProcessDef;
import velo.entity.Task.TaskStatus;

@Name("workflowProcessDefList")
public class WorkflowProcessDefList extends EntityQuery {
	
	@In
	JbpmContext jbpmContext;
	
	
	private static final String[] RESTRICTIONS = {
			"lower(workflowProcessDef.uniqueName) like concat(lower(#{workflowProcessDefList.workflowProcessDef.uniqueName}),'%')"};

	private WorkflowProcessDef workflowProcessDef = new WorkflowProcessDef();

	private static final String EJBQL = "select workflowProcessDef from WorkflowProcessDef workflowProcessDef";

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public WorkflowProcessDef getworkflowProcessDef() {
		return workflowProcessDef;
	}

	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
	
	
	
}
