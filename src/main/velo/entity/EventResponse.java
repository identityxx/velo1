package velo.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import velo.exceptions.action.ActionExecutionException;

/**
 * @author Asaf Shakarchi
 *
 * A parent of all type of event responses (scripted, action, interactive events, etc...)
 */
@Entity
@Table(name = "VL_EVENT_RESPONSE")
@SequenceGenerator(name="EventResponseIdSeq",sequenceName="EVENT_RESPONSE_ID_SEQ")
@Inheritance(strategy = InheritanceType.JOINED)
//@DiscriminatorColumn(name = "RESPONSE_TYPE")
public class EventResponse implements Comparable<EventResponse> {//extends SequencedAction {
	private Long eventResponseId;
	//maintain a reference to the event
	private Event event;
	private boolean asyncExecution = true;
	private SequencedAction action;
	private Date creationDate;
	
	public EventResponse() {
		
	}
	
	
	public EventResponse(SequencedAction action) {
		setAction(action);
		setCreationDate(new Date());
	}
	
	
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventResponseIdSeq")
    @Column(name="EVENT_RESPONSE_ID")
	public Long getEventResponseId() {
		return eventResponseId;
	}

	public void setEventResponseId(Long eventResponseId) {
		this.eventResponseId = eventResponseId;
	}

	
	/**
	 * Delegate the execution to the action,
	 * @throws ActionExecutionException
	 */
	public void execute() throws ActionExecutionException {
		//delegate the event context
		action.setContext(getEvent().getContext());
		
		action._execute();
	}
	
	//see explaination of optional=true/nullable=true in ResourceAction->getResource()
    @ManyToOne(optional=true)
    @JoinColumn(name="EVENT_ID", nullable = true)
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	 
	@Column(name="ASYNC_EXECUTION", nullable=false)
	public boolean isAsyncExecution() {
		return asyncExecution;
	}

	public void setAsyncExecution(boolean asyncExecution) {
		this.asyncExecution = asyncExecution;
	}

	//@ManyToOne(cascade = { CascadeType. })
	@ManyToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "ACTION_ID", nullable = false, unique = false)
	public SequencedAction getAction() {
		return action;
	}

	public void setAction(SequencedAction action) {
		this.action = action;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	
	
	
	
	
	//delegation to action (due to limit of multiple inheritence in java) :/
	@Transient
	public Boolean getShowStopper() {
		return getAction().getShowStopper();
	}
	
	public void setShowStopper(Boolean showStopper) {
		getAction().setShowStopper(showStopper);
	}
	
	@Transient
	public Boolean getActive() {
		return getAction().getActive();
	}
	
	public void setActive(Boolean active) {
		getAction().setActive(active);
	}
	
	@Transient
	public String getName() {
		return getAction().getName();
	}
	
	public void setName(String name) {
		getAction().setName(name);
	}
	
	@Transient
	public Integer getSequence() {
		return getAction().getSequence();
	}
	
	public void setSequence(Integer sequence) {
		getAction().setSequence(sequence);
	}
	
	
	public int compareTo(EventResponse er) {
		 if (this.getSequence() == er.getSequence())
	            return 0;
	        else if ((this.getSequence() > er.getSequence()))
	            return 1;
	        else
	            return -1;
	}
}
