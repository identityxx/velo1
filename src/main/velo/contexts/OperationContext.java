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
package velo.contexts;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import velo.contexts.OperationContextVar.PhaseRelevance;

public class OperationContext {
	private static Logger log = Logger.getLogger(OperationContext.class.getName());
	
	public Map<String,OperationContextVar> vars = new HashMap<String,OperationContextVar>();

	/**
	 * @return the vars
	 */
	public Map<String, OperationContextVar> getVars() {
		return vars;
	}

	/**
	 * @param vars the vars to set
	 */
	public void setVars(Map<String, OperationContextVar> vars) {
		this.vars = vars;
	}
	
	
	public Object get(String varName) {
		OperationContextVar variable = getVars().get(varName);
		if (variable != null) {
			return variable.getValue();
		}
		else {
			log.warn("Variable name '" + varName + "' does not exist in context");
			return null;
		}
	}
	
	public void set(String varName, Object value) {
		addVar(varName, value, PhaseRelevance.ALL);
	}
	
	
	public void setVar(OperationContextVar var) {
		if (getVars().containsKey(var.getName())) {
			OperationContextVar existance = getVars().get(var.getName());
			existance.setValue(var.getValue());
		}
		else {
			getVars().put(var.getName(), var);
		}
	}
	
	public void addVar(String name, Object value, PhaseRelevance phaseRelevant) {
		setVar(new OperationContextVar(name, value, phaseRelevant)); 
	}
	
	public void addVar(String name, Object value) {
		setVar(new OperationContextVar(name, value, PhaseRelevance.ALL)); 
	}
	
	
	public boolean isVarExists(String varName) {
		return getVars().containsKey(varName);
	}
	
	public void removeVar(String varName) {
		if (isVarExists(varName)) {
			getVars().remove(varName);
		}
	}
	
	
	@Override
	public String toString() {
		String str = "Context(";
		
		for (OperationContextVar currVar : getVars().values()) {
			//resource operation controller might inject null values
			if (!currVar.isValueNull()) {
				str+="[" + currVar.getName() + "," + currVar.getValue().toString() + "]";
			}
		}
		
		return str+=")";
	}
	
	public void clear() {
		getVars().clear();
	}
	
	//Acessors for easy work
	public void log(String log) {
		this.log.info(log);
	}
}
