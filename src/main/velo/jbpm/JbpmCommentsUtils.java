package velo.jbpm;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

@AutoCreate
@Name("jbpmCommentsUtils")

public class JbpmCommentsUtils {

	public String getProcessCommentsAsHtml(ProcessInstance pi){

		String commentsAsHtml = new String("<table border=\"1\">");
		
		List<Comment> comments = getProcessComments(pi);
		
		if (comments == null)
			return null;
		
		for (Comment cmt : comments) {
			if (!(cmt.getMessage() == null) && !cmt.getMessage().isEmpty()) {
				commentsAsHtml +=  "<tr>";

				commentsAsHtml += "<td>" + cmt.getActorId() + "</td>";
				commentsAsHtml += "<td>" + DateFormat.getDateTimeInstance().format(cmt.getTime()) + "</td>";
				commentsAsHtml += "<td>" + cmt.getMessage() + "</td>";
				
				commentsAsHtml += "</tr>";
			}
		}

		commentsAsHtml += "</table>";
		
		return commentsAsHtml;
	}
	
	public List<Comment> getProcessComments(ProcessInstance processInstance) {
		if (processInstance == null)
			return null;

		List<Comment> comments = new ArrayList<Comment>();
		Collection<TaskInstance> tasks = processInstance.getTaskMgmtInstance().getTaskInstances();

		if (tasks == null)
			return null;
		
		for (TaskInstance ti : tasks) {
			comments.addAll(ti.getComments());
		}
		
		return sortCommentsList(comments);
	}	
	
	private List<Comment> sortCommentsList(List<Comment> comments) {

		Comment tempComment;

		for (int i=0; i<comments.size()-1; i++)
			for (int j=0; j<comments.size()-1; j++)
				if (comments.get(j).getTime().after(comments.get(j+1).getTime())) {
					tempComment = comments.get(j);
					comments.set(j, comments.get(j+1));
					comments.set(j+1, tempComment);
				}
		
		return comments;
	}	
}
