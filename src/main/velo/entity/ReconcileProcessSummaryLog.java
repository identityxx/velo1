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

@Entity
@Table(name="VL_REC_PROCESS_SUMMARY_LOG")
@SequenceGenerator(name="ReconcileProcessSummaryLogIdSeq",sequenceName="REC_PROCESS_SUMMARY_LOG_ID_SEQ")
public class ReconcileProcessSummaryLog extends LogEntry {
	private Long reconcileProcessSummaryLogId;
	private ReconcileProcessSummary reconcileProcessSummary;
	
	public ReconcileProcessSummaryLog() {
		super();
	}

	public ReconcileProcessSummaryLog(ReconcileProcessSummary reconcileProcessSummary, EventLogLevel level, String message) {
		super(level, message);
		setReconcileProcessSummary(reconcileProcessSummary);
	}

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileProcessSummaryLogIdSeq")
    @Column(name="REC_PROCESS_SUMMARY_LOG_ID")
	public Long getReconcileProcessSummaryLogId() {
		return reconcileProcessSummaryLogId;
	}

	public void setReconcileProcessSummaryLogId(Long reconcileProcessSummaryLogId) {
		this.reconcileProcessSummaryLogId = reconcileProcessSummaryLogId;
	}

	@ManyToOne(optional=false)
    @JoinColumn(name="REC_PROCESS_SUMMARY_ID", nullable=false)
	public ReconcileProcessSummary getReconcileProcessSummary() {
		return reconcileProcessSummary;
	}

	public void setReconcileProcessSummary(
			ReconcileProcessSummary reconcileProcessSummary) {
		this.reconcileProcessSummary = reconcileProcessSummary;
	}
	
	public String toString() {
		return "[Log ID '" + getReconcileProcessSummaryLogId() + "'], [Server: '" + getServer() +  "'], [Level: '" + getLevel() + "'], [Message: '" + getMessage() + "']";
	}
}
