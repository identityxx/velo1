package velo.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import velo.entity.LogEntry.EventLogLevel;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventEntityType;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEventSeverities;
import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEvents;

@Entity
@Table(name="VL_REC_PROCESS_SUMMARY")
@NamedQueries({
    @NamedQuery(name = "reconcileProcessSummary.findLatestForProcessType", query = "SELECT rps FROM ReconcileProcessSummary rps WHERE rps.processType = :processType ORDER BY rps.reconcileProcessSummaryId DESC"),
    @NamedQuery(name = "reconcileProcessSummary.findLatestSuccessfullForProcessType", query = "SELECT rps FROM ReconcileProcessSummary rps WHERE rps.processType = :processType AND rps.successfullyFinished = 1  ORDER BY rps.reconcileProcessSummaryId DESC"),
    @NamedQuery(name = "reconcileProcessSummary.deleteUntil", query = "DELETE FROM ReconcileProcessSummary rps WHERE rps.startDate < :untilDate"),
    @NamedQuery(name = "reconcileProcessSummary.selectUntil", query = "SELECT rps FROM ReconcileProcessSummary rps WHERE rps.startDate < :untilDate")
})
@SequenceGenerator(name="ReconcileProcessSummaryIdSeq",sequenceName="REC_PROCESS_SUMMARY_ID_SEQ")
public class ReconcileProcessSummary implements Serializable {
	public enum ReconcileProcesses {
		RECONCILE_IDENTITIES_INCREMENTAL,RECONCILE_IDENTITIES_FULL, RECONCILE_GROUPS_FULL,RECONCILE_GROUP_MEMBERSHIP_FULL, RECONCILE_GROUP_MEMBERSHIP_INCREMENTAL
	}
	
	private Long reconcileProcessSummaryId;
	private Date startDate;
	private Date endDate;
	private String keywords;
	private Boolean successfullyFinished;
	private Set<ReconcileProcessSummaryLog> logs = new HashSet<ReconcileProcessSummaryLog>();
	private Set<ReconcileProcessSummaryEvent> events = new HashSet<ReconcileProcessSummaryEvent>();
	private ReconcileProcesses processType;
	private Resource resource;
	
	
	private Date toEndDate;
	private Date fromStartDate;
	
	
	public ReconcileProcessSummary() {
		
	}
	
	public ReconcileProcessSummary(Resource resource, ReconcileProcesses processType) {
		setProcessType(processType);
		setResource(resource);
	}
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileProcessSummaryIdSeq")
    @Column(name="REC_PROCESS_SUMMARY_ID")
	public Long getReconcileProcessSummaryId() {
		return reconcileProcessSummaryId;
	}
	
	public void setreconcileProcessSummaryId(Long reconcileProcessSummaryId) {
		this.reconcileProcessSummaryId = reconcileProcessSummaryId;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="START_DATE")
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="END_DATE")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Column(name="KEYWORDS")
	public String getKeywords() {
		return keywords;
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	@Column(name = "SUCCESSFULLY_FINISHED", nullable=false)
	public Boolean getSuccessfullyFinished() {
		return successfullyFinished;
	}

	public void setSuccessfullyFinished(Boolean successfullyFinished) {
		this.successfullyFinished = successfullyFinished;
	}
	
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "reconcileProcessSummary", fetch = FetchType.LAZY)
	public Set<ReconcileProcessSummaryLog> getLogs() {
		return logs;
	}

	public void setLogs(Set<ReconcileProcessSummaryLog> logs) {
		this.logs = logs;
	}

	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "reconcileProcessSummary", fetch = FetchType.LAZY)
	public Set<ReconcileProcessSummaryEvent> getEvents() {
		return events;
	}
	
	public void setEvents(Set<ReconcileProcessSummaryEvent> events) {
		this.events = events;
	}
	
	@Column(name="PROCESS_TYPE")
    @Enumerated(EnumType.STRING)
	public ReconcileProcesses getProcessType() {
		return processType;
	}

	public void setProcessType(ReconcileProcesses processType) {
		this.processType = processType;
	}
	
	@ManyToOne(optional=false)
    @JoinColumn(name="RESOURCE_ID", nullable=false)
	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void addEvent(ReconcileProcessSummaryEvents event,ReconcileProcessSummaryEventSeverities severity, ReconcileProcessSummaryEventEntityType entityType, String entityName, String message) {
		if (getResource().getReconcilePolicy().getReconcileAuditPolicy().isEventAudited(event)) {
			ReconcileProcessSummaryEvent rpse = new ReconcileProcessSummaryEvent(this,event,severity, entityType, entityName, message);
			getEvents().add(rpse);
		}
	}
	
	public void addLog(EventLogLevel level, String message) {
		ReconcileProcessSummaryLog rpl = new ReconcileProcessSummaryLog(this, level, message);
		getLogs().add(rpl);
	}
	
	public void start() {
		setStartDate(new Date());
		addLog(EventLogLevel.INFO, "Reconcile process has started.");
	}
	
	public void end() {
		setEndDate(new Date());
		addLog(EventLogLevel.INFO, "Reconcile process has ended.");
	}
	
	public void end(Boolean successfull) {
		setEndDate(new Date());
		setSuccessfullyFinished(successfull);
		addLog(EventLogLevel.INFO, "Reconcile process has ended.");
	}

	
	
	
	
	
	
	
	
	
	
	
	
	@Transient
	public Date getToEndDate() {
		return toEndDate;
	}

	public void setToEndDate(Date toEndDate) {
		this.toEndDate = toEndDate;
	}

	@Transient
	public Date getFromStartDate() {
		return fromStartDate;
	}

	public void setFromStartDate(Date fromStartDate) {
		this.fromStartDate = fromStartDate;
	}
	
}
