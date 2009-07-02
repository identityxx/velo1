package velo.reconciliation.summary;

import java.util.ArrayList;
import java.util.List;


@Deprecated
public class ReconcileIdentitiesExistenceProcessSummary extends ResourceReconcileProcess {
	private static final long serialVersionUID = 1L;
	private List<ReconcileIdentitySummary> identities = new ArrayList<ReconcileIdentitySummary>();

	public ReconcileIdentitiesExistenceProcessSummary(Long resourceId, String resourceUniqueName) {
		super(resourceId, resourceUniqueName);
	}

	public List<ReconcileIdentitySummary> getIdentities() {
		return identities;
	}

	public void setIdentities(List<ReconcileIdentitySummary> identities) {
		this.identities = identities;
	}
	
	public void addIdentity(ReconcileIdentitySummary ris) {
		getIdentities().add(ris);
	}
	
	public void addIdentity(String identityName, ReconcileEvents event) {
		getIdentities().add(new ReconcileIdentitySummary(identityName, event));
	}
}
