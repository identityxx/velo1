package velo.jbpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import java.util.ArrayList;
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
@Name("loggedUserAllRequestedProcessList")
@Scope(ScopeType.APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class LoggedUserAllRequestedProcessList
{
	@In
	User loggedUser;
	
   @Unwrap
   @Transactional
   public List<ProcessInstance> getLoggedUserAllRequestedProcessList()
   {
	   if ( Actor.instance().getId() == null ) return null;

	   List<ProcessInstance> piList = new ArrayList<ProcessInstance>();
	   
	   //ManagedJbpmContext.instance().getSession().createQuery("SELECT pi from ProcessInstance pi, IN(pi.)
	   List<StringInstance> l = ManagedJbpmContext.instance().getSession().createQuery("select vi from org.jbpm.context.exe.variableinstance.StringInstance vi WHERE vi.name = :varName AND vi.value = :varValue ORDER BY vi.processInstance.end,vi.processInstance.id DESC").setParameter("varName","requesterUserName").setParameter("varValue", loggedUser.getName()).list();
	   
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
   
   
   private List<TaskInstance> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      return ManagedJbpmContext.instance().getTaskList(actorId);
   }
}
