package velo.entity;

import groovy.lang.GroovyObject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;

@Entity 
@DiscriminatorValue("REC_RES_CORRELATION_RULE")
public class ReconcileResourceCorrelationRule extends ActionRule {
	private static final long serialVersionUID = 1023411L;
	
	private static transient Logger log = Logger.getLogger(ReconcileResourceCorrelationRule.class.getName());
	public Set<ReconcilePolicy> reconcilePolicies = new HashSet<ReconcilePolicy>();
	
	@OneToMany(mappedBy = "reconcileResourceCorrelationRule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public Set<ReconcilePolicy> getReconcilePolicies() {
		return reconcilePolicies;
	}

	public void setReconcilePolicies(Set<ReconcilePolicy> reconcilePolicies) {
		this.reconcilePolicies = reconcilePolicies;
	}


	@Override
	public void updateScriptedObject(GroovyObject scriptedObject) {
		//scriptedObject.setProperty("log", log);
	}
}
