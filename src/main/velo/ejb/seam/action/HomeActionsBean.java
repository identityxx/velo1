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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import velo.common.SysConf;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.RequestList;
import velo.ejb.seam.ResourceList;
import velo.ejb.seam.TaskList;
import velo.ejb.seam.UserList;
import velo.ejb.seam.WorkflowProcessList;
import velo.entity.Resource;
import velo.entity.ResourceTask;
import velo.entity.Task;
import velo.entity.User;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import com.generationjava.io.xml.XmlWriter;

@Stateful
@Name("homeActions")
public class HomeActionsBean implements HomeActions {

	byte[] chart;
	
	File logo;
	
	@Logger
	private Log log;
	
	@In
	EntityManager entityManager;

	@In
	FacesMessages facesMessages;

	@EJB
	public RoleManagerLocal roleManager;
	
	@EJB
	public UserManagerLocal userManager;
	

	@In(create=true, value="#{taskList}")
	TaskList failedTaskList;
	
	@In(create=true, value="#{requestList}")
	RequestList requestList;
	
	@In(create=true, value="#{workflowProcessList}")
	WorkflowProcessList workflowProcessList;
	
	@In(create=true)
	public SysConf sysConfManager;

	//@In(create=true,value="org.jboss.seam.async.dispatcher")
	//QuartzDispatcher dispatcher;
	
	
	public TaskList getFailedTaskList() {
		failedTaskList.setEjbql("select task from Task task WHERE (task.status = 'FAILURE' OR task.status = 'FATAL_ERROR') ORDER BY task.taskId DESC");
		if (failedTaskList.getMaxResults() > 20) {
			failedTaskList.setMaxResults(5);
		}
		return failedTaskList;
    }
	
	@Deprecated
	public RequestList getLastFailedRequests() {
		requestList.setEjbql("select request from Request request WHERE (request.status = 'APPROVED') and (request.processed = 1) and (request.successfullyProcessed = 0) ORDER BY request.requestId DESC");
		requestList.refresh();
		if (requestList.getMaxResults() > 20) {
			requestList.setMaxResults(5);
		}
		return requestList;
	}

	@Deprecated
	public RequestList getLastRequestsWaitingForApproval() {
		requestList.setEjbql("select request from Request request WHERE (request.status = 'PENDING_APPROVAL') ORDER BY request.requestId DESC");
		requestList.refresh();
		if (requestList.getMaxResults() > 20) {
			requestList.setMaxResults(5);
		}
		return requestList;
	}
	
	@Deprecated
	public RequestList getLastApprovedRequests() {
		requestList.setEjbql("select request from Request request WHERE (request.status = 'APPROVED') ORDER BY request.requestId DESC");
		requestList.refresh();
		if (requestList.getMaxResults() > 20) {
			requestList.setMaxResults(5);
		}
		return requestList;
	}
	
	
	public WorkflowProcessList getLastProcessList() {
		if (workflowProcessList.getMaxResults() > 20) {
			workflowProcessList.setMaxResults(5);
		}
		
		return workflowProcessList;
	}
	
	public WorkflowProcessList getLastSuspendedProcessList() {
		workflowProcessList.setEjbql(workflowProcessList.getEjbqlForLastSuspended());
		
		if (workflowProcessList.getMaxResults() > 20) {
			workflowProcessList.setMaxResults(5);
		}
		
		return workflowProcessList;
	}
	
	
	public UserList getLastCreatedUsers() {
		UserList userList = new UserList();
		userList.setEjbql("select user from User user where user.creationDate is not null ORDER BY user.creationDate DESC");
		
		if (userList.getMaxResults() > 20) {
			userList.setMaxResults(5);
		}
		return userList;
	}
	
	
	public String getArray() {
		log.debug("Get User creation per month for last year method has been invoked!");

		
		try {
		Writer writer1 = new java.io.StringWriter();
		XmlWriter xmlwriter1 = new SimpleXmlWriter(writer1);
		PrettyPrinterXmlWriter  pxr = new PrettyPrinterXmlWriter(xmlwriter1);
		pxr.setIndent("\t");
		pxr.setNewline("\n");
		pxr.writeXmlVersion("1.0", null,"yes")
			.writeEntity("items");
		
		
		
		
		Calendar c = Calendar.getInstance();
		String[] monthName = {"Jan", "Feb",
			"Mar", "Apr", "May", "Jun", "Jul",
			"Aug", "Sep", "Oct", "Nov",
			"Dec"};
		
		
		//move the calendar to 12 months back
		c.add(Calendar.MONTH, -11);

		//build dates(1st day) of the last 12 months
		List<Date> yearlyMonths = new ArrayList<Date>();
		log.debug("Building months(1 year) before current month...");
		for (int i=0;i<=11;i++) {
			log.trace("Adding monthly date obj: #0",c.getTime());
			yearlyMonths.add(c.getTime());
			c.add(Calendar.MONTH, 1);
		}
		
		
		//iterate over months, get the amount of users created per month
		for (Date currFirstDayOfMonth : yearlyMonths) {
			c.setTime(currFirstDayOfMonth);
			c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			Long amount = userManager.getCreatedUsersAmount(currFirstDayOfMonth,c.getTime());
			
			pxr.writeEntity("item").writeAttribute("month", monthName[c.get(Calendar.MONTH)]).writeAttribute("year", c.get(Calendar.YEAR)).writeAttribute("value", amount).endEntity();
		}
		
		pxr.endEntity();
		
		log.debug("Dump of xml: " + writer1.toString());
		return writer1.toString();
		
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		
		
		/*
		.writeEntity("item").writeAttribute("month", "Jan").writeAttribute("value", 10).endEntity()
		.writeEntity("item").writeAttribute("month", "Feb").writeAttribute("value", 20).endEntity()
		.writeEntity("item").writeAttribute("month", "Mar").writeAttribute("value", 30).endEntity()
		.writeEntity("item").writeAttribute("month", "Apr").writeAttribute("value", 40).endEntity()
		.writeEntity("item").writeAttribute("month", "Jun").writeAttribute("value", 50).endEntity()
		.writeEntity("item").writeAttribute("month", "Jul").writeAttribute("value", 60).endEntity()
		.writeEntity("item").writeAttribute("month", "Aug").writeAttribute("value", 70).endEntity()
		*/
	}
	
	
	
	
	
	public void createChart()
    {
		ResourceList rl = new ResourceList();
		rl.getResource().setActive(true);
		rl.initialize();
		List<Resource> resources = rl.getResultList();
		
		//Retrieve a list of tasks from last day
		TaskList tl = new TaskList();
		List<Task> tasks = tl.getResultList();
		
		//build the summaries
		Map<String,Long> rTasks = new HashMap<String,Long>();
		
		for (Task currTask : tasks) {
			if (currTask instanceof ResourceTask) {
				ResourceTask rt = (ResourceTask)currTask;
				
				if (!rTasks.containsKey(rt.getResourceUniqueName())) {
					rTasks.put(rt.getResourceUniqueName(),new Long(0));
				}
				
				rTasks.put(rt.getResourceUniqueName(),rTasks.get(rt.getResourceUniqueName())+1);
			}
		}
		
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for (Resource currResource : resources) {
			if (rTasks.containsKey(currResource.getUniqueName())) {
				dataset.setValue(currResource.getDisplayName() + "(" + rTasks.get(currResource.getUniqueName()).longValue() + ")", rTasks.get(currResource.getUniqueName()));
			} else {
				dataset.setValue(currResource.getDisplayName() + "(0)",0);
			}
			
		}
		
         final JFreeChart chart = ChartFactory.createPieChart(
                 "Tasks Amount (1 last day) per resource",  // chart title
                 dataset,             // dataset
                 true,                // include legend
                 true,
                 false
             );
         final PiePlot plot = (PiePlot) chart.getPlot();
         plot.setNoDataMessage("No data available");

       try{
            this.chart = ChartUtilities.encodeAsPNG(chart.createBufferedImage(400, 400));
       } catch (IOException e){
            e.printStackTrace();
       }

  }


	public byte[] getChart() {
		createChart();
		return chart;
	}


	public void createLogo() {
		//try {
			//int length = (int)new File("c:/logo.jpg").length();
			//logo = new byte[length];
			//new RandomAccessFile("c:/logo.jpg", "rw").readFully(logo);
			String logoFileName = sysConfManager.getGlobalConf().getString("front-end.adminstrative.logo_file_name");
			
			if (logoFileName == null) {
				return;
			}
			
			logo = new File(logoFileName);
			/*
		}catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public void setChart(byte[] chart) {
		this.chart = chart;
	}
	
	public File getLogo() {
		createLogo();
		return logo;
	}
	
	
	public void test() {
		//System.out.println("!!!!!!!!!!!" + dispatcher);
		//System.out.println("!!!!!!!!!!!" + dispatcher.getScheduler().getClass().getName());
		
		try {
			InitialContext ic = new InitialContext();
			org.quartz.impl.StdScheduler sched = (org.quartz.impl.StdScheduler) ic.lookup("Quartz");
			System.out.println("!!!!!!!!!!!!!!!!!!NUUUUU: " + sched);
		}catch (NamingException e) {
			System.out.println("WAAAAAAAAAAAA: " + e.getMessage());
		}
	}
	
	
	public List<Task> getLastCreatedUsersAsList() {
		String qs = "select task from Task task";
		List<Task> tasks = entityManager.createQuery(qs).setMaxResults(3).getResultList(); 
		
		
		System.out.println("!!!!!!!!!!!!!!!!! FETCHED AMOUNT OF TASKS: " + tasks.size());
		
		return tasks;
	}
	
	public void fun() {
		Map<String,String> ma = new HashMap<String,String>();
		ma.put("FIRST_NAME", "Admini");
		ma.put("LAST_NAME", "Strator");
		List<User> users = userManager.findUsers(ma, false);
		
		log.trace("!!!!!!!! RETURNED USERS NUM IS #0" + users.size());
		
		for (User user : users) {
			log.trace("!!!: USER FOUND: " + user.getName());
		}
	}
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
