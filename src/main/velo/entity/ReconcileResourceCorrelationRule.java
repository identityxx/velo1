package velo.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


//@Entity
//@Table(name="VL_REC_RES_CORRELATION_RULES")


//@DiscriminatorValue("REC_RES_CORRELATION_RULE")
//public class ReconcileResourceCorrelationRule extends ActionRule {
public class ReconcileResourceCorrelationRule extends ScriptedAction {
	private static final long serialVersionUID = 1023411L;
	
	//private static transient Logger log = Logger.getLogger(ReconcileResourceCorrelationRule.class.getName());
	public Set<ReconcilePolicy> reconcilePolicies = new HashSet<ReconcilePolicy>();
	
	@OneToMany(mappedBy = "reconcileResourceCorrelationRule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public Set<ReconcilePolicy> getReconcilePolicies() {
		return reconcilePolicies;
	}

	public void setReconcilePolicies(Set<ReconcilePolicy> reconcilePolicies) {
		this.reconcilePolicies = reconcilePolicies;
	}
	
	
	@Override
	@Transient
	public String getDisplayableActionType() {
		return "Reconcile Correlation Rule";
	}
}
