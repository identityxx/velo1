package velo.ejb.seam.action;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.seam.ApproversGroupHome;
import velo.ejb.seam.ApproversGroupList;
import velo.entity.ApproversGroup;
import velo.entity.User;

@Name("approverGroupsActions")
public class ApproverGroupsActions {
	
	@In
	EntityManager entityManager;
	
	@Logger
	private Log log;
	
	@In(required=false)
	ApproversGroupHome approversGroupHome;
	
	@In
	FacesMessages facesMessages;
	
	private User newMember;
	
	public List<String> getApproverGroupsNamesByType(String type) {
		log.debug("Seeking for all Approver Groups unique names for type '#0'", type);
		
		List<String> agListAsString = new ArrayList<String>();
		
		ApproversGroupList agl = new ApproversGroupList();
		agl.setMaxResults(5000);
		
		agl.setEjbql("select ag from ApproversGroup ag where ag.type = '" + type + "'");
		//agl.getRestrictions().add("approversGroup.type = '" + type + "'");
		
		List<ApproversGroup> agList = agl.getResultList();
		for (ApproversGroup currAG :  agList) {
			agListAsString.add(currAG.getUniqueName());
		}
		
		
		return agListAsString;
	}
	
	
	public void removeUserFromGroup(User user) {
		approversGroupHome.getInstance().getApprovers().remove(user);
		user.getApproversGroups().remove(approversGroupHome.getInstance());
		facesMessages.add("Successfully removed selected user from current group.");
	}
	
	public void addMemberToGroup() {
		approversGroupHome.getInstance().getApprovers().add(newMember);
		facesMessages.add("Successfully added selected user to current group.");
		
		newMember = null;
	}
	
	public void bla() {
		Query q = entityManager.createQuery("select user from User as user join user.userIdentityAttributes as uia left join uia.values as uiavalue WHERE (uia.identityAttribute.uniqueName = :iaName AND uiavalue.valueString = :iaValue) AND (uia.identityAttribute.uniqueName = :iaName1 AND uiavalue.valueString = :iaValue1)").setParameter("iaName", "FIRST_NAME").setParameter("iaValue", "Cow").setParameter("iaName1","LAST_NAME").setParameter("iaValue1","Moo");
		List<User> users = q.getResultList();
		
		System.out.println(users.size());
		
		int i=0;
		for (User currUser : users) {
			i++;
			System.out.println("["+i+"]: " + currUser.getName());
		}
	}


	public User getNewMember() {
		return newMember;
	}
	public void setNewMember(User newMember) {
		this.newMember = newMember;
	}
}
