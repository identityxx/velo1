package velo.ejb.workflow.api;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import velo.entity.ApproversGroup;
import velo.entity.User;

@Name("wfApprovalTools")
@AutoCreate
public class WfApprovalTools {
	@Logger
	Log log;

	@In
	WfUserManager wfUserManager;
	
	@In
	EntityManager entityManager;
	
	public boolean isInApproversGroup(String userName, String approversGroupName) {
		log.debug("Is user name '#0' member of approvers group name '#1'",userName,approversGroupName);
		
		User user = wfUserManager.findUser(userName);
		if (user == null) {
			log.debug("User '#0' could not be found in repository, returning false.");
			return false;
		}
		
		ApproversGroup ag = findApproversGroup(approversGroupName);
		if (ag == null) {
			log.debug("ApproversGroup '#0' could not be found in repository, returning false.");
			return false;
		}
		
		
		if (ag.isMember(userName)) {
			log.debug("User '#0' is a member of approversGroup '#1', returning true.",userName,approversGroupName);
			return true;
		} else {
			log.debug("User '#0' is NOT a member of approversGroup '#1', returning false.",userName,approversGroupName);
			return false;
		}
	}
	
	
	
	public boolean isApproversGroupEmpty(String approversGroupName) {
		log.debug("Checking whether approvers group '#0' is empty or not...", approversGroupName);
		ApproversGroup ag = findApproversGroup(approversGroupName);
		
		if (ag == null) {
			log.debug("ApproversGroup '#0' does not exist, returning empty as it makes more sense!",approversGroupName);
			log.warn("ApproversGroup '#0' was not found while trying to determine whether approvers group is empty or not!", approversGroupName);
			return true;
		} else {
			return ag.getApprovers().size() < 1;
		}
	}
	
	//FIXME: Duplicated from ApprovalGroupBean
	private ApproversGroup findApproversGroup(String uniqueName) {
		log.debug("Finding Approvers Group in repository with unique name '" + uniqueName + "'");

		try {
			Query q = entityManager.createNamedQuery("approversGroup.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (ApproversGroup) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Could not find any Approvers Group for unique name '" + uniqueName + "', returning null.");
			return null;
		}
	}
	
	
	
}
