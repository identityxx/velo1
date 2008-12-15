/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.entity;

import groovy.lang.GroovyObject;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.exceptions.ScriptInvocationException;
import velo.exceptions.ScriptLoadingException;
import velo.scripting.GroovyScripting;
//@!@clean
//@Name("actionDefinition")
@Table(name = "VL_ACTION")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="CLASS_TYPE")
@SequenceGenerator(name="ActionIdSeq",sequenceName="ACTION_ID_SEQ")
@Entity
public abstract class ActionDefinition extends BaseEntity {
	private static Logger log = Logger.getLogger(ActionDefinition.class.getName());
	
	private static final long serialVersionUID = 1L;
	private Long actionDefinitionId;
	private ActionLanguage actionLanguage;
	private String content;
	private String description;
	
	
	/**
	 * @return the actionId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ActionIdSeq")
	//@GeneratedValue
	@Column(name = "ACTION_DEFINITION_ID")
	public Long getActionDefinitionId() {
		return actionDefinitionId;
	}
	
	/**
	 * @param actionId the actionId to set
	 */
	public void setActionDefinitionId(Long actionDefinitionId) {
		this.actionDefinitionId = actionDefinitionId;
	}
	
	/**
	 * @return the actionLanguage
	 */
	@ManyToOne(optional=false)
    @JoinColumn(name="ACTION_LANGUAGE_ID", nullable=false, unique=false)
	public ActionLanguage getActionLanguage() {
		return actionLanguage;
	}
	
	/**
	 * @param actionLanguage the actionLanguage to set
	 */
	public void setActionLanguage(ActionLanguage actionLanguage) {
		this.actionLanguage = actionLanguage;
	}
	
	/**
	 * @return the content
	 */
	@Column(name = "CONTENT")
	@Lob
	public String getContent() {
		return content;
	}
	
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	
	/**
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", nullable=false)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
	@Transient
	public GroovyObject getScriptedObject() throws ScriptLoadingException {
		GroovyScripting gs = new GroovyScripting(); 
		if (getActionLanguage().getName().equalsIgnoreCase("GROOVY")) {
			GroovyObject go = gs.factoryGroovyObject(getContent());

			updateScriptedObject(go);
			return go;
		}
		else {
			throw new ScriptLoadingException("The specified scripting language is not supported!");
		}
	}
	
	@Deprecated
	public abstract void updateScriptedObject(GroovyObject scriptedObject);
	
	
	public void invoke(GroovyObject go, OperationContext context) throws ScriptInvocationException {
		try {
			log.trace("Invoking default method over scripted groovy object");
			
			//set the context
			go.setProperty("context", context);
			
			log.trace("Dumping context before script invocation: " + context);
			
			Object[] args = {};
			go.invokeMethod("run", args);
			log.trace("Ended method invokation");
		}
		//Wrap any exception into one, scripts are externally supplied thus any exception might occur here since 
		catch (Exception e) {
			throw new ScriptInvocationException("Failed to execute script: " + e.toString());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	//transient/helper
	public void copyValues(Object entity) {
    	//TODO: Implement
    }
}
