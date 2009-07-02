package velo.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="VL_RECONCILE_EVENT_RESPONSE")
@Entity
//@DiscriminatorValue("RECONCILE")
public class ReconcileEventResponse extends EventResponse {
	private ReconcilePolicy reconcilePolicy;
	
	
	public ReconcileEventResponse() {
		
	}
	
	public ReconcileEventResponse(SequencedAction action, ReconcilePolicy reconcilePolicy) {
		super(action);
		setReconcilePolicy(reconcilePolicy);
	}
	
	
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "RECONCILE_POLICY_ID", nullable = true, unique = false)
	public ReconcilePolicy getReconcilePolicy() {
		return reconcilePolicy;
	}

	public void setReconcilePolicy(ReconcilePolicy reconcilePolicy) {
		this.reconcilePolicy = reconcilePolicy;
	}
}
