package velo.entity;

import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.exceptions.EventResponseException;
import velo.exceptions.action.ActionExecutionException;


@Entity
/*@Table(name="VL_RECONCILE_EVENT")
@SequenceGenerator(name="ReconcileEventIdSeq",sequenceName="RECONCILE_EVENT_ID_SEQ")*/


//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name = "EVENT_TYPE")

@Table(name="VL_RECONCILE_EVENT")
//@DiscriminatorValue("RECONCILE_EVENT")

@NamedQueries( {
		@NamedQuery(name = "reconcileEvent.findByUniqueName", query = "SELECT re FROM ReconcileEvent re WHERE re.uniqueName = :uniqueName")
} )
public class ReconcileEvent extends Event {
	private static Logger log = Logger.getLogger(ReconcileEvent.class.getName());
	//private Long reconcileEventId;
	
	//event names
	public final static String IDENTITY_CONFIRMED_EVENT_NAME = "IDENTITY_CONFIRMED";
	public final static String IDENTITY_REMOVED_EVENT_NAME = "IDENTITY_REMOVED";
	public final static String IDENTITY_UNMATCHED_EVENT_NAME = "IDENTITY_UNMATCHED";
	public final static String IDENTITY_UNASSIGNED_EVENT_NAME = "IDENTITY_UNASSIGNED";
	public final static String IDENTITY_MODIFIED_EVENT_NAME = "IDENTITY_MODIFIED";
	public final static String IDENTITY_ATTRIBUTE_MODIFIED_EVENT_NAME = "IDENTITY_ATTRIBUTE_MODIFIED";
	
	public final static String GROUP_CREATED_EVENT_NAME = "GROUP_CREATED";
	public final static String GROUP_MODIFIED_EVENT_NAME = "GROUP_MODIFIED";
	public final static String GROUP_REMOVED_EVENT_NAME = "GROUP_REMOVED";
	
	
	public final static String GROUP_MEMBER_ASSOCIATED_EVENT_NAME = "GROUP_MEMBER_ASSOCIATED";
	public final static String GROUP_MEMBER_DISSOCIATED_EVENT_NAME = "GROUP_MEMBER_DISSOCIATED";
	public final static String GROUP_MEMBERSHIP_MODIFY_EVENT_NAME = "GROUP_MEMBERSHIP_MODIFY";
	
	public ReconcileEvent() {
		
	}
	
	public ReconcileEvent(String uniqueName, String displayName, String description) {
		super(uniqueName,displayName,description);
	}
	
	
	public void execute() throws ActionExecutionException {
		throw new ActionExecutionException("Never use this method, use execute(ReconcilePolicy rp) instead!");
	}
	
	public void execute(ReconcilePolicy rp) throws ActionExecutionException {
		log.info("Starting execution of event '" + getDisplayName() + "' for reconcile policy '" + rp.getName() + "'"); 
		//Executing responses!
		TreeSet<ReconcileEventResponse> activeResponsesForPolicy =  getEventResponsesForPolicy(rp);
			
		for (ReconcileEventResponse rer : activeResponsesForPolicy) {
			try {
				executeEventResponse(rer);
			} catch (EventResponseException e) {
				throw new ActionExecutionException(e.getMessage());
			}
		}

		log.info("Successfully ended execution of event '" + getDisplayName() + "' for reconcile policy '" + rp.getName() + "'");
	}
	
	
	@Transient
	private TreeSet<ReconcileEventResponse> getEventResponsesForPolicy(ReconcilePolicy rp) {
		TreeSet<ReconcileEventResponse> list = new TreeSet<ReconcileEventResponse>();
		
		for (EventResponse currER : getActiveEventResponses()) {
			//expecting each response to be of ReconcileEventResponse type
			ReconcileEventResponse currRER = (ReconcileEventResponse)currER;
			
			if (currRER.getReconcilePolicy().equals(rp)) {
				list.add(currRER);
			}
		}
		
		return list;
	}
	
	/*
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileEventIdSeq")
	public Long getReconcileEventId() {
		return reconcileEventId;
	}

	public void setReconcileEventId(Long reconcileEventId) {
		this.reconcileEventId = reconcileEventId;
	}
	*/
	
}
