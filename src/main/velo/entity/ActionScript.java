package velo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import velo.exceptions.FactoryException;
import velo.scripting.ScriptingManager;

@Entity
@Table(name="VL_ACTION_SCRIPT")
@SequenceGenerator(name="ActionScriptIdSeq",sequenceName="ACTION_SCRIPT_ID_SEQ")
public class ActionScript implements Comparable<ActionScript>{
	public enum InvokePhases {
		CREATION, VALIDATE, PRE, EXECUTE, POST
	}

	private Long id;
	private InvokePhases invokePhase;
	private String title;
	private String content;
	private ActionLanguage actionLanguage;
	private Boolean active;
	private ScriptedAction action;
	private Integer sequence;
	private Boolean showStopper;

	//compiled code
	private CompiledScript compiledScript = null;
	private boolean compiled;

	public ActionScript(ActionLanguage actionLanguage, String content, ScriptedAction action, Boolean active, InvokePhases invokePhase, Integer sequence, Boolean showStopper) {
		setActionLanguage(actionLanguage);
		setInvokePhase(invokePhase);
		setContent(content);
		setActive(active);
		setContent(content);
		setAction(action);
		setSequence(sequence);
		setShowStopper(showStopper);
	}

	public ActionScript() {

	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ActionScriptIdSeq")
	@Column(name="ID")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	public InvokePhases getInvokePhase() {
		return invokePhase;
	}
	public void setInvokePhase(InvokePhases invokePhase) {
		this.invokePhase = invokePhase;
	}

	@Lob
	@Column(name="CONTENT")
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	@ManyToOne(optional=false)
	@JoinColumn(name="ACTION_LANGUAGE_ID", nullable=false, unique=false)
	public ActionLanguage getActionLanguage() {
		return actionLanguage;
	}
	public void setActionLanguage(ActionLanguage actionLanguage) {
		this.actionLanguage = actionLanguage;
	}

	@Column(name="ACTIVE")
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}

	@ManyToOne(optional=false)
	@JoinColumn(name="SCRIPTED_ACTION_ID", nullable=false, unique=false)
	public ScriptedAction getAction() {
		return action;
	}
	public void setAction(ScriptedAction action) {
		this.action = action;
	}

	@Column(name="SEQUENCE")
	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	@Column(name="SHOW_STOPPER")
	public Boolean getShowStopper() {
		return showStopper;
	}

	public void setShowStopper(Boolean showStopper) {
		this.showStopper = showStopper;
	}

	@Column(name="TITLE")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void init() {
		if (getId() == null) {
			setShowStopper(true);
			setActive(true);
			setSequence(1);
			setInvokePhase(InvokePhases.EXECUTE);
		}
	}

	public int compareTo(ActionScript as) {
		if (this.sequence == as.getSequence())
			return 0;
		else if ((this.getSequence() > as.getSequence()))
			return 1;
		else
			return -1;
	}

	public void compileScript() throws ScriptException {
		if (!isCompiled()) {
			try {
				ScriptEngine se = ScriptingManager.getScriptEngine(getActionLanguage().getName());
				compiledScript = ((Compilable)se).compile(getContent());
				setCompiled(true);
			} catch (FactoryException e) {
				throw new ScriptException("Could not compile script(could not factor a script engine): " + e.getMessage());
			} catch (ScriptException e) {
				throw new ScriptException("Could not compile script: " + e.getMessage());
			}
		}
	}

	@Transient
	public CompiledScript getCompiledScript() {
		return compiledScript;
	}

	public void setCompiledScript(CompiledScript compiledScript) {
		this.compiledScript = compiledScript;
	}

	@Transient
	public boolean isCompiled() {
		return compiled;
	}

	public void setCompiled(boolean compiled) {
		this.compiled = compiled;
	}


}
