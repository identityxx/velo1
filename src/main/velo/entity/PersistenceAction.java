package velo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import velo.actions.Action;
import velo.entity.annotations.UniqueColumnForSync;

/**
 * @author Asaf Shakarchi
 *
 * A certain action that should be invoked.
 * This is the new action class that should be used instead of ActionDefinition and others!
 * 
 * Subclasses of this class can be: direct implementation of actions (Scripted,Rule based, etc...)
 * or already available implementations
 * 
 * @see SequencedAction
 */
@MappedSuperclass
@NamedQueries({
    @NamedQuery(name = "persistenceAction.findByName",query = "SELECT action FROM PersistenceAction action WHERE action.name = :name")
})

public abstract class PersistenceAction extends Action {
//	public enum ScriptedActionTypes {
//		RESOURCE_ACTION, RESOURCE_ATTRIBUTE_RULE, GENERIC_EVENT, RECONCILE_EVENT_RESPONSE, RECONCILE_CORRELATION_RULE 
//	}
	
	public enum ActionTypes {
		READY_ACTION("Ready Action"),SCRIPTED_ACTION("Scripted ACtion");
		
		final String code;
		ActionTypes(String code) {
			this.code = code;
		}
	}
	
	private String name;
	private String description;
	private Date lastExecutionDate;
	private Date creationDate;
	private Boolean active;
	//private ScriptedActionTypes type;
	
	
	public PersistenceAction(String name, String description, Boolean active) {
		setName(name);
		setDescription(description);
		setActive(active);
	}
	
	public PersistenceAction() {
		
	}
	
	@UniqueColumnForSync
	@Column(name="NAME", nullable=false, unique=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="DESCRIPTION", nullable=true)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_EXECUTION_DATE")
	public Date getLastExecutionDate() {
		return lastExecutionDate;
	}
	public void setLastExecutionDate(Date lastExecutionDate) {
		this.lastExecutionDate = lastExecutionDate;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	@Column(name="ACTIVE", nullable=false)
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	@Transient
	public abstract String getDisplayableActionType();

//	@Enumerated(EnumType.STRING)
//	@Column(name="TYPE", nullable=false)
//	public ScriptedActionTypes getType() {
//		return type;
//	}
//
//	public void setType(ScriptedActionTypes type) {
//		this.type = type;
//	}
}
