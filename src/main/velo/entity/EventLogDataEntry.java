package velo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;

@Entity
@Table(name = "VL_EVENT_LOG_DATA_ENTRY")
@SequenceGenerator(name="EventLogDataEntryIdSeq",sequenceName="EVENT_LOG_DATA_ENTRY_ID_SEQ")
public class EventLogDataEntry {
	private Long eventLogDataEntryId;
	private String name;
	private String value;
	private EventLogEntry eventLogEntry;
	
	
	public EventLogDataEntry(EventLogEntry eventLogEntry, String name, String value) {
		setEventLogEntry(eventLogEntry);
		setName(name);
		setValue(value);
	}
	
	public EventLogDataEntry() {
		
	}
	
	/**
     * @return Returns the taskLogId.
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="EventLogDataEntryIdSeq")
    @Column(name="EVENT_LOG_DATA_ENTRY_ID")
    public Long getEventLogDataEntryId() {
        return eventLogDataEntryId;
    }
    
    public void setEventLogDataEntryId(Long eventLogDataEntryId) {
		this.eventLogDataEntryId = eventLogDataEntryId;
	}


	@Column(name="NAME") @Length(min=0,max=50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="VALUE") @Length(min=0,max=255)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@ManyToOne(optional=false)
    @JoinColumn(name="EVENT_LOG_ENTRY_ID", nullable=false)
	public EventLogEntry getEventLogEntry() {
		return eventLogEntry;
	}

	public void setEventLogEntry(EventLogEntry eventLogEntry) {
		this.eventLogEntry = eventLogEntry;
	}
	
	
}
