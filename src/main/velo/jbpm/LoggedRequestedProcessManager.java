package velo.jbpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.context.exe.variableinstance.StringInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import velo.entity.User;

/**
 * Support for the pooled task list.
 * 
 * @see LoggedUserTaskInstanceList
 * @author Asaf Shakarchi
 */
@Name("loggedRequestedProcessManager")
@Scope(ScopeType.APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class LoggedRequestedProcessManager
{
	//@In
	//User loggedUser;
	
	
	
	
	
   //@Unwrap
   @Transactional
   public List<ProcessInstance> getLoggedUserAllRequestedProcessList()
   {
	   if ( Actor.instance().getId() == null ) return null;

	   List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
	   
	   //ManagedJbpmContext.instance().getSession().createQuery("SELECT pi from ProcessInstance pi, IN(pi.)
	   
	   //not allowed to do loggedUser for Application scope
	   //List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND vi.processInstance.isSuspended=0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", loggedUser.getName()).list();
	   List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND vi.processInstance.isSuspended=0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", Actor.instance().getId()).list();
	   
	   
	   
	   for (StringInstance si : l) {
		   piList.add(si.getProcessInstance());
	   }
	   
	   
	   return piList;
	      
	      /*
      Actor actor = Actor.instance();
      String actorId = actor.getId();
      if ( actorId == null ) return null;
      ArrayList groupIds = new ArrayList( actor.getGroupActorIds() );
      groupIds.add(actorId);
      List<TaskInstance> objs = ManagedJbpmContext.instance().getGroupTaskList(groupIds);
      
      //bjs.addAll(getTaskInstanceList( Actor.instance().getId() ));
      
      //return objs;
      return null;
      */
   }
   
   
   @Transactional
   public List<ProcessInstance> getLoggedUserNotFinishedAndFinishedRequestedProcessList(Integer previousDays)
   {
	   if ( Actor.instance().getId() == null ) return null;

	   List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
	   
	   
	   previousDays = previousDays * -1;
	   Calendar c = Calendar.getInstance();
	   c.add(Calendar.DAY_OF_YEAR, previousDays);
	   
	   
	   //not allowed to do loggedUser for Application scope
	   //List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND (vi.processInstance.end = null OR vi.processInstance.end >= :endDate) AND vi.processInstance.isSuspended = 0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", loggedUser.getName()).setParameter("endDate", c.getTime()).list();
	   List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND (vi.processInstance.end = null OR vi.processInstance.end >= :endDate) AND vi.processInstance.isSuspended = 0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", Actor.instance().getId()).setParameter("endDate", c.getTime()).list();
	   
	   for (StringInstance si : l) {
		   piList.add(si.getProcessInstance());
	   }
	   
	   
	   return piList;
   }
   
   @Transactional
   public List<ProcessInstance> getLoggedUserNotFinishedAndFinishedRequestedProcessListAsRequestDelegator(Integer previousDays)
   {
	   if ( Actor.instance().getId() == null ) return null;

	   List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
	   
	   
	   previousDays = previousDays * -1;
	   Calendar c = Calendar.getInstance();
	   c.add(Calendar.DAY_OF_YEAR, previousDays);
	   
	   
	   //not allowed to do loggedUser for Application scope
	   //List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND (vi.processInstance.end = null OR vi.processInstance.end >= :endDate) AND vi.processInstance.isSuspended = 0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", loggedUser.getName()).setParameter("endDate", c.getTime()).list();
	   List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue AND (vi.processInstance.end = null OR vi.processInstance.end >= :endDate) AND vi.processInstance.isSuspended = 0 ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requestDelegatorUserName").setParameter("varValue", Actor.instance().getId()).setParameter("endDate", c.getTime()).list();
	   
	   for (StringInstance si : l) {
		   piList.add(si.getProcessInstance());
	   }
	   
	   
	   return piList;
   }   
   
   @Transactional
   public List<ProcessInstance> getLoggedUserNotFinishedAndFinishedRequestedProcessListAsRequesterAndAsRequestDelegator(Integer previousDays)
   {
	   if ( Actor.instance().getId() == null ) return null;

	   List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
	   
	   piList.addAll(getLoggedUserNotFinishedAndFinishedRequestedProcessList(previousDays));	   
	   piList.addAll(getLoggedUserNotFinishedAndFinishedRequestedProcessListAsRequestDelegator(previousDays));	   
	   
	   return piList;
   }   

   
   private List<TaskInstance> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      return ManagedJbpmContext.instance().getTaskList(actorId);
   }
}
