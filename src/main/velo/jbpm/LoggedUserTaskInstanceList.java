package velo.jbpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Support for the pooled task list.
 * 
 * @see TaskInstanceList
 * @author Asaf Shakarchi
 */
@Name("loggedUserTaskInstanceLista")
@Scope(ScopeType.APPLICATION)
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class LoggedUserTaskInstanceList
{
   @Unwrap
   @Transactional
   public List<TaskInstance> getLoggedUserTaskInstanceList()
   {
      Actor actor = Actor.instance();
      String actorId = actor.getId();
      if ( actorId == null ) return null;
      ArrayList groupIds = new ArrayList( actor.getGroupActorIds() );
      groupIds.add(actorId);
      List<TaskInstance> objs = ManagedJbpmContext.instance().getGroupTaskList(groupIds);
      
      objs.addAll(getTaskInstanceList( Actor.instance().getId() ));
      
      return objs;
   }
   
   
   private List<TaskInstance> getTaskInstanceList(String actorId)
   {
      if ( actorId == null ) return null;

      return ManagedJbpmContext.instance().getTaskList(actorId);
   }
}
