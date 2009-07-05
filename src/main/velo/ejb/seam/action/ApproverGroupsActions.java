package velo.ejb.seam.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.seam.ApproversGroupHome;
import velo.ejb.seam.ApproversGroupList;
import velo.entity.ApproversGroup;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
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
	
	@In
	UserManagerLocal userManager;
	
	@In
	ResourceManagerLocal resourceManager;
	
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

	public void bla2() {
		/*
		System.out.println("!!!!!!!!!!!!!!!!!!!!");
		Map<String,String> a = new HashMap<String,String>();
		a.put("FIRST_NAME","first1");
		a.put("LAST_NAME","LOVELY");
		List<User> users = userManager.findUsers(a, false, false, 0);
		System.out.println("!!!!!!!!!!!USERS: " + users.size());
		
		for (User currUser : users) {
			System.out.println("User name: " + currUser.getName());
		}
		*/
		

		/*Works
		String a = "select user FROM User user, IN(user.localUserAttributes) uia1, IN (uia1.values) uiaVal1, IN(user.localUserAttributes) uia2, IN(uia2.values) uiaVal2 WHERE ( (uia1.identityAttribute.uniqueName = :uia1UniqueName AND UPPER(uiaVal1.valueString) = :uia1ValContent) AND (uia2.identityAttribute.uniqueName = :uia2UniqueName AND UPPER(uiaVal2.valueString) = :uia2ValContent) )";
		Query q = entityManager.createQuery(a);
		q.setParameter("uia1UniqueName","FIRST_NAME");
		q.setParameter("uia1ValContent","first1");
		q.setParameter("uia2UniqueName","LAST_NAME");
		q.setParameter("uia2ValContent","last1");
		*/
		
		String a = "select user FROM User user,Account account, IN(user.localUserAttributes) uia1," +
				" IN (uia1.values) uiaVal1, IN(account.accountAttributes) accAttr1, IN(accAttr1.values) accAttrVal1, " +
				"IN (user.accounts) userAccount " +
				"WHERE ( (uia1.identityAttribute.uniqueName = :uia1UniqueName " +
				"AND UPPER(uiaVal1.valueString) = :uia1ValContent) " +
				"AND (accAttr1.resourceAttribute = :accAttr1 " +
				"AND UPPER(accAttrVal1.valueString) = :accAttrVal1Content) ) AND userAccount = account";
		
		
		Resource r = resourceManager.findResourceEagerly("TESTAPP1");
		ResourceAttribute raLastName = r.getResourceAttribute("last_name");
		ResourceAttribute raStatus = r.getResourceAttribute("status");
		
		Query q = entityManager.createQuery(a);
		q.setParameter("uia1UniqueName","FIRST_NAME");
		q.setParameter("uia1ValContent","first1");
		
		q.setParameter("accAttr1",raLastName);
		q.setParameter("accAttrVal1Content","last1");
		
		
		List<User> users = q.getResultList();
		
		
		System.out.println("USERS!!!!!!!!!!!!!: " + users.size());
		for (User currUser : users) {
			System.out.println("LALALA USER: " + currUser.getName());
		}
	}
	
	public void bla1() {
		Map<String,String> a = new HashMap<String,String>();
		a.put("FIRST_NAME","first1");
		a.put("LAST_NAME","last1");
		List<User> users = userManager.findUsers(a, false, false, 0);
		System.out.println("!!!!!!!!!!!USERS: " + users.size());
	}

	public User getNewMember() {
		return newMember;
	}
	public void setNewMember(User newMember) {
		this.newMember = newMember;
	}
}
