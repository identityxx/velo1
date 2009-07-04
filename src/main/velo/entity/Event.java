package velo.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Synchronized;

import velo.actions.Action;
import velo.entity.LogEntry.EventLogLevel;
import velo.entity.annotations.UniqueColumnForSync;
import velo.exceptions.EventResponseException;
import velo.exceptions.action.ActionExecutionException;

/**
 * @author Asaf Shakarchi
 * A persistence event, 
 */
//cannot be extended from BaseEntity / ExtBaseEntity as it should be extended from Action
//An extend to the base event but with persistence properties


@Entity
@Table(name="VL_EVENTA")
@SequenceGenerator(name="EventIdSeq",sequenceName="EVENT_ID_SEQ")
@Inheritance(strategy = InheritanceType.JOINED)

//NEVER USE DISCRIMNATOR WITH INHERITANCE.JOINED, it cause two table creation for some reason (at least with SQL), in Mysql it seems to be ok
//@DiscriminatorColumn(name = "EVENT_TYPE")
//@MappedSuperclass
public class Event extends Action implements Serializable {
	private long eventId;
	private String uniqueName;
	private String displayName;
	private String description;
	private Date creationDate;
	private Date lastUpdateDate;
	
	//A reference to the registered persistence responses
	private Set<EventResponse> eventResponses = new TreeSet<EventResponse>();
	
	private static Logger log = Logger.getLogger(Event.class.getName());

	
	public Event() {
		setCreationDate(new Date());
	}
	
	public Event(String uniqueName, String displayName, String description) {
		setUniqueName(uniqueName);
		setDisplayName(displayName);
		setDescription(description);
		setCreationDate(new Date());
	}
	
	
	
	public void execute() throws ActionExecutionException {
		//Executing responses!
		TreeSet<EventResponse> activeResponses =  getActiveEventResponses();
		log.debug("Executing event, performing execution of all -active- event responses with amount '" + activeResponses.size() + "'"); 
			
		for (EventResponse rer : getActiveEventResponses()) {
			try {
				executeEventResponse(rer);
			} catch (EventResponseException e) {
				throw new ActionExecutionException(e.getMessage());
			}
		}
	}
	
	/**
	 * Invokes any kind(including sub-types) of event response
	 * 
	 * @param rer The EventResponse to be executed
	 * @throws EventResponseException in case where an exception occurs while invoking the response (only when isShowStopper is set to true)
	 */
	public void executeEventResponse(EventResponse rer) throws EventResponseException {
		try {
			rer.execute();
		}catch(ActionExecutionException e) {
			if (rer.getShowStopper()) {
				throw new EventResponseException("Could not execute event response ID '" + rer.getEventResponseId() + "' [" + rer.getName() + "] " + e.getMessage());
			} else {
				//Expecting reconcile process policy to be available here
				ReconcileProcessSummary rps = (ReconcileProcessSummary)getContext().get("rps");
				rps.addLog(EventLogLevel.ERROR, "Could not execute event response ID '" + rer.getEventResponseId() + "' [" + rer.getName() + "]" + e.getMessage());
				log.error("Could not execute event response ID '" + rer.getEventResponseId() + "' [" + rer.getName() + "]" + e.getMessage());
			}
		}
	}
	
	
	//helper, transient
	@Transient
    public TreeSet<EventResponse> getActiveEventResponses() {
		TreeSet<EventResponse> activeResponses = new TreeSet<EventResponse>();
		
    	for (EventResponse currER : getEventResponses()) {
    		if (currER.getActive()) {
    			activeResponses.add(currER);
    		}
    	}
    	
    	return activeResponses;
    }
    
    public void addResponse(EventResponse eventResponse) {
    	eventResponse.setEvent(this);
        getEventResponses().add(eventResponse);
    }
	
    
    public EventResponse factoryEventResponse() {
        EventResponse erd = new EventResponse();
        erd.setCreationDate(new Date());
        erd.setEvent(this);
        
        return erd;
    }
    
    
    //transition set/get
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventIdSeq")
    @Column(name="EVENT_ID")
	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	@OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
	//@OrderBy("sequence") //(can't use TreeSet with hibernate :(  required at all? order is maintained by the TreeSet
	public Set<EventResponse> getEventResponses() {
		return eventResponses;
	}
	public void setEventResponses(Set<EventResponse> eventResponses) {
		this.eventResponses = eventResponses;
	}

	@UniqueColumnForSync
	@Column(name="UNIQUE_NAME", nullable=false, unique=true)
	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
	@Column(name="DISPLAY_NAME", nullable=false, unique=false)
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Column(name="DESCRIPTION", nullable=false, unique=false)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_UPDATE_DATE")
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
}
