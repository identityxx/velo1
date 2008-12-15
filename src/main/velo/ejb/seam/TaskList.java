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

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.Task;
import velo.entity.Task.TaskStatus;

@Name("taskList")
public class TaskList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
		"lower(task.taskId) = #{taskList.task.taskId}",
		"lower(task.description) like concat(lower(#{taskList.task.description}),'%')",
		"task.status = #{taskList.task.status}",
		"task.creationDate >= #{taskList.task.fromCreationDate}",
		"task.creationDate <= #{taskList.task.toCreationDate}"};

	private Task task = new Task();
	
	@Override
	public String getEjbql() {
		if (super.getEjbql() == null) {
			//return "select task from Task task ORDER BY task.taskId DESC";
			return "select task from Task task";
		}
		else {
			return super.getEjbql();
		}
	}

	@Override
	public Integer getMaxResults() {
		if (super.getMaxResults() != null) {
			return super.getMaxResults();
		}
		else {
			return 25;
		}
	}

	public Task getTask() {
		return task;
	}

	
	
	@Factory("taskStatuses")
	public TaskStatus[] getTaskStatuses() {
		return TaskStatus.values();
	}
	
	@End
	public List getResultList() {
		//Map<String,String> hints = new HashMap<String,String>();
		//hints.put("cacheMode", "REFRESH"); //true/false
		//hints.put("org.hibernate.cacheMode", "REFRESH"); //NORMAL / IGNORE / GET / PUT / REFRESH
		
		//this.setHints(hints);
		
		return super.getResultList();
	}
	
	
	public long getTasksAmountForStatus(String status) {
		getLog().debug("Getting task amount  in repository for status: #0",status);
		return (Long) getEntityManager()
			.createQuery("SELECT count(task) FROM Task task WHERE task.status = :status")
			.setParameter("status", TaskStatus.valueOf(status)).getSingleResult();
	}
	
	
	@PostConstruct
    public void initialize() {
		setOrder("taskId desc");
		
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(getEjbql());
    }
}
