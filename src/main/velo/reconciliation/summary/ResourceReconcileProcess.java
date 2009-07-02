package velo.reconciliation.summary;

import java.io.Serializable;
import java.util.Date;

@Deprecated
public class ResourceReconcileProcess implements Serializable{
	private static final long serialVersionUID = 1L;
	private String resourceUniqueName;
	private Long resourceId;
	private Date date;
	
	
	public enum ReconcileEvents {
		IDENTITY_REMOVED, IDENTITY_CREATED, IDENTITY_MODIFIED, IDENTITY_ATTRIBUTE_MODIFIED, 
		GROUP_CREATED, GROUP_MODIFIED, GROUP_REMOVED,
		GROUP_MEMBERSHIP_ASSOCIATED, GROUP_MEMBERSHIP_REMOVED
	}
	
	public ResourceReconcileProcess(Long resourceId, String resourceUniqueName) {
		super();
		this.resourceId = resourceId;
		this.resourceUniqueName = resourceUniqueName;
	}
	
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}
	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}
	
	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
