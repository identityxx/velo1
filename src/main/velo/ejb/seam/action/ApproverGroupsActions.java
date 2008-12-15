package velo.ejb.seam.action;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import velo.ejb.seam.ApproversGroupList;
import velo.entity.ApproversGroup;

@Name("approverGroupsActions")
public class ApproverGroupsActions {
	
	@In
	EntityManager entityManager;
	
	@Logger
	private Log log;
	
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
}
