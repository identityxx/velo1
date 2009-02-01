package velo.jbpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

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
   @Unwrap
   @Transactional
   public List<ProcessInstance> getLoggedUserAllRequestedProcessList()
   {
	   if ( Actor.instance().getId() == null ) return null;

	   //ManagedJbpmContext.instance().getSession().createQuery("SELECT pi from ProcessInstance pi, IN(pi.)
	      return ManagedJbpmContext.instance().getSession()
	         .createCriteria(ProcessInstance.class)
	         .add( Restrictions.eq("actorId", Actor.instance().getId()) )
	         .add( Restrictions.eq("isOpen", true) )
	         .add( Restrictions.ne("isSuspended", true) )
	         .addOrder( Order.desc("id") )
	         .setCacheable(true)
	         .list();
	      
	      
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
