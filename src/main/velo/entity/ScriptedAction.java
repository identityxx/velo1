package velo.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.entity.ActionScript.InvokePhases;
import velo.entity.LogEntry.EventLogLevel;
import velo.exceptions.FactoryException;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionPostException;
import velo.exceptions.action.ActionPreException;
import velo.exceptions.action.ActionValidationException;
import velo.scripting.ScriptingManager;

@Table(name="VL_SCRIPTED_ACTION")
@Entity
//NEVER USE DISCRIMNATOR WITH INHERITANCE.JOINED, it cause two table creation for some reason (at least with SQL), in Mysql it seems to be ok
//@DiscriminatorColumn(name = "CLASS_TYPE")
@NamedQueries( {
	@NamedQuery(name = "scriptedAction.findByName", query = "SELECT action FROM ScriptedAction action WHERE action.name = :name"),
	@NamedQuery(name = "scriptedAction.findAllActive", query = "SELECT action FROM ScriptedAction action WHERE action.active = 1"),
	@NamedQuery(name = "scriptedAction.findAllActiveByType", query = "SELECT action FROM ScriptedAction action WHERE action.active = 1 AND action.scriptedActionType = :scriptedActionType")
})
public class ScriptedAction extends SequencedAction {
	
	//this is what we use for now instead of subclassing scriptedaction
	public enum ScriptedActionTypes {
		RECONCILE_CORRELATION_RULE,RECONCILE_CONFIRMATION_RULE,RECONCILE_RESPONSE_ACTION, WORKFLOW_ACTION, 
	}
	
	
	private static Logger log = Logger.getLogger(ScriptedAction.class.getName());
	private Set<ActionScript> scripts = new HashSet<ActionScript>();
	private ScriptingManager sm = new ScriptingManager();
	private Map<ActionScript, String> failedScripts = new HashMap<ActionScript,String>();
	private ScriptedActionTypes scriptedActionType;
	
	public ScriptedAction(String name, String description, Boolean active, Boolean showStopper, Integer sequence) {
		super(name,description,active,showStopper,sequence);
	}
	
	public ScriptedAction() {
		
	}
	
	
	@OneToMany(mappedBy = "action", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
	public Set<ActionScript> getScripts() {
		return scripts;
	}

	public void setScripts(Set<ActionScript> scripts) {
		this.scripts = scripts;
	}

	
	@Column(name="SCRIPTED_ACTION_TYPE", nullable=false)
	@Enumerated(EnumType.STRING)
	public ScriptedActionTypes getScriptedActionType() {
		return scriptedActionType;
	}

	public void setScriptedActionType(ScriptedActionTypes scriptedActionType) {
		this.scriptedActionType = scriptedActionType;
	}

	public void validate() throws ActionValidationException {
		try {
			scriptsExecution(getScriptsForValidationPhase());
		} catch (ActionExecutionException e) {
			throw new ActionValidationException(e);
		}
	}
	
	public void pre() throws ActionPreException {
		try {
			scriptsExecution(getScriptsForPrePhase());
		} catch (ActionExecutionException e) {
			throw new ActionPreException(e);
		}
	}
	
	
	public void execute() throws ActionExecutionException {
		scriptsExecution(getScriptsForExecutionPhase());
	}

	public void post() throws ActionPostException {
		try {
			scriptsExecution(getScriptsForPostPhase());
		} catch (ActionExecutionException e) {
			throw new ActionPostException(e);
		}

	}
	
	
	public void scriptsExecution(Set<ActionScript> scripts) throws ActionExecutionException {
		for (ActionScript currAS : scripts) {
			try {
				invokeScript(currAS);
			} catch (ActionExecutionException e) {
				if (currAS.getShowStopper()) {
					throw e;
				} else {
					//Expecting reconcile process policy to be available here
					ReconcileProcessSummary rps = (ReconcileProcessSummary)getContext().get("rps");
					rps.addLog(EventLogLevel.ERROR, "Could not execute script of action ID '" + getId() + "', error: '" + e.getMessage() + "'");
					log.error("Could not execute script of action ID '" + getId() + "', error: '" + e.getMessage() + "'");
					failedScripts.put(currAS, e.getMessage());
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	public void addScript(ActionLanguage actionLanguage, String content, InvokePhases invokePhase, Integer sequence, Boolean showStopper) {
		ActionScript as = new ActionScript(actionLanguage, content, this, getActive(), invokePhase, sequence, showStopper);
		getScripts().add(as);
	}
	
	
	@Transient
	public Set<ActionScript> getActiveScripts() {
		Set<ActionScript> scripts = new TreeSet<ActionScript>();
		
		for (ActionScript currAS : getScripts()) {
			if (currAS.getActive()) {
				scripts.add(currAS);
			}
		}
		
		return scripts;
	}
	
	@Transient
	public Set<ActionScript> getScriptsForValidationPhase() {
		return getScriptsForPhase(InvokePhases.VALIDATE);
	}
	
	@Transient
	public Set<ActionScript> getScriptsForPrePhase() {
		return getScriptsForPhase(InvokePhases.PRE);
	}
	
	@Transient
	public Set<ActionScript> getScriptsForExecutionPhase() {
		return getScriptsForPhase(InvokePhases.EXECUTE);
	}
	
	@Transient
	public Set<ActionScript> getScriptsForPostPhase() {
		return getScriptsForPhase(InvokePhases.POST);
	}
	
	
	
	@Transient
	public Set<ActionScript> getScriptsForPhase(velo.entity.ActionScript.InvokePhases invokePhase) {
		Set<ActionScript> scripts = new TreeSet<ActionScript>();
		
		for (ActionScript currAS : getActiveScripts()) {
			//System.out.println("REQUESTED: " + invokePhase + ", SCRIPT IS: " + currAS.getInvokePhase());
			if (currAS.getInvokePhase().equals(invokePhase)) {
				scripts.add(currAS);
			}
		}
		
		
		return scripts;
	}
	
	@Transient
	public void invokeScript(ActionScript as) throws ActionExecutionException {
		//Based on the scripted language, get the invoker
		try {
			//Push relevant content into the context
			ScriptEngine se = getApproporiateScriptEngine(as);
			se.put("cntx", getContext());
			//se.eval(as.getContent());

			as.compileScript();
			as.getCompiledScript().eval();
		}catch (ScriptException e) {
			throw new ActionExecutionException("Could not execute script for action '" + getName() + "': " + e.getMessage());
		} catch (FactoryException e) {
			throw new ActionExecutionException("Could not factory script engine while running script for action '" + getName() + "': " + e.getMessage());
		}
	}
	
	@Transient
	public ScriptEngine getApproporiateScriptEngine(ActionScript as) throws FactoryException {
		return ScriptingManager.getScriptEngine(as.getActionLanguage().getName());
	}
	
	@Override
	@Transient
	public String getDisplayableActionType() {
		return getScriptedActionType().toString();
	}
}
