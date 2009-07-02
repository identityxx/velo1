package velo.reconciliation.summary;

import java.io.Serializable;

import velo.reconciliation.summary.ResourceReconcileProcess.ReconcileEvents;

@Deprecated
public class ReconcileIdentitySummary implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private ReconcileEvents event;
	
	public ReconcileIdentitySummary(String name, ReconcileEvents event) {
		super();
		this.name = name;
		this.event = event;
	}
	
	public ReconcileIdentitySummary() {
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ReconcileEvents getEvent() {
		return event;
	}

	public void setEvent(ReconcileEvents event) {
		this.event = event;
	}
}
