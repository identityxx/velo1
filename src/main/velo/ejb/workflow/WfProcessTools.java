package velo.ejb.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

@Name("wfProecssTools")
public class WfProcessTools {
	public List<Comment> getProcessComments(ProcessInstance pi) {
		List<Comment> comments = new ArrayList<Comment>();
		Collection<TaskInstance> tasks = pi.getTaskMgmtInstance().getTaskInstances();
		
		for (TaskInstance ti : tasks) {
			comments.addAll(ti.getComments());
		}
		
		
		return comments;
	}
}
