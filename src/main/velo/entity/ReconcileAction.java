package velo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;

//@SequenceGenerator(name="ReconcileActionIdSeq",sequenceName="RECONCILE_ACTION_ID_SEQ")
//@Table(name="VL_RECONCILE_ACTION")
//@Entity
public class ReconcileAction extends ExtBasicEntity {
	private String uniqueName;
	private String displayName;
	private String description;
	private Date creationDate;
	private Date lastUpdateDate;
	private String actionClassName;
	
	
	public ReconcileAction() {
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Column(name="ACTION_CLASS_NAME", nullable=false, unique=false)
    @NotNull
	public String getActionClassName() {
		return actionClassName;
	}

	public void setActionClassName(String actionClassName) {
		this.actionClassName = actionClassName;
	}

	/**
	 * @return the uniqueName
	 */
	@Column(name="UNIQUE_NAME", nullable=false, unique=true)
    @NotNull
	public String getUniqueName() {
		return uniqueName;
	}
	/**
	 * @param uniqueName the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	/**
	 * @return the displayName
	 */
	@Column(name="DISPLAY_NAME", nullable=false)
	@NotNull
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION")
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return Returns the creationDate.
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * @return the lastUpdateDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_UPDATE_DATE")
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate the lastUpdateDate to set
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	
}
