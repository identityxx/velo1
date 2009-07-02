package velo.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name="VL_SYSTEM_EVENT_RESPONSE")
@Entity
//@DiscriminatorValue("SYSTEM")
public class SystemEventResponse extends EventResponse {
	public SystemEventResponse() {
		
	}
	
	public SystemEventResponse(SequencedAction action, ReconcilePolicy reconcilePolicy) {
		super(action);
	}
}
