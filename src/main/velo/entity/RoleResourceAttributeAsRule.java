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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.exceptions.FactoryException;
import velo.scripting.ScriptingManager;

@Entity
@DiscriminatorValue(value = "RULE")
public class RoleResourceAttributeAsRule extends RoleResourceAttribute {
	private static Logger log = Logger.getLogger(RoleResourceAttributeAsRule.class.getName());
	
	private RoleResourceAttributeRule actionRule;

	public RoleResourceAttributeAsRule() {
		
	}
	
	public RoleResourceAttributeAsRule(Role role, ResourceAttribute resourceAttribute) {
		super(role,resourceAttribute);
	}
	
	/**
	 * @return the actionRule
	 */
	@ManyToOne()//cascade = { CascadeType. })
	@JoinColumn(name = "ACTION_RULE_ID")
	public RoleResourceAttributeRule getActionRule() {
		return actionRule;
	}

	/**
	 * @param actionRule the actionRule to set
	 */
	public void setActionRule(RoleResourceAttributeRule actionRule) {
		this.actionRule = actionRule;
	}
	
	
	@Transient
	public String getType() {
		return "RULE";
	}
	
	
	public ResourceAttribute calculateAttributeResult(OperationContext context) {
		log.trace("Calculating value for role resource attribute(rule) named '" + getDescription() + "'");
		

		//checking whether the context already has a resource attribute set (initialized by the resource attribute level)
		//if not, then the ra this entity references will be set instead.
		log.debug("Determining whether the context already has an 'attribute' (resource attribute) var set or not");
		if (!context.isVarExists("attribute")) {
			log.debug("context does not have an 'attribute' var, adding the one this rule references into the context.");
			context.addVar("attribute", getResourceAttribute());
		} else {
			log.debug("'attribute' variable already set within the context (from the 'resource attribute' level), skipping attribute insertion into context.");
		}
		
		
		log.trace("Dumping context before script invocation: " + context);
		//try {
			ScriptingManager sm = new ScriptingManager();
			try {
				ScriptEngine se = sm.getScriptEngine(getActionRule().getActionLanguage().getName().toLowerCase());
				
				//invoke the rule
				se.put("cntx", context);
				se.put("log", log);
				
				log.trace("Invoking default method over scripted object");
				se.eval(getActionRule().getContent());
				log.trace("Ended method invocation");
				
				
				log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + context.get("attribute"));
				//all ok, get the value
				return (ResourceAttribute)context.get("attribute");
			} catch (FactoryException e) {
				String errMsg = "Could not factory scripting manager: " + e.toString();
				log.error(errMsg);
				log.info("due to error, returning null!");
				return null;
			} catch (ScriptException e) {
				log.error("Failed to execute role resource attribute of Role: '" + getRole().getRoleId() + "', related to res attr ID: '" + getResourceAttribute().getResourceAttributeId() + "': " + e.toString());
				log.info("due to error, returning null!");
				return null;
			}
		/*}catch (AttributeSetValueException e) {
			log.error(e.toString() + ", returning null.");
			return null;
		}*/
	}
}
