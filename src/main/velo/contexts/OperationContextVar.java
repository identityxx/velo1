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

import java.util.ArrayList;
import java.util.List;

public class OperationContextVar {
	public enum PhaseRelevance {
		VALIDATE, PRE, POST, ALL
	}
	
	private String name;
	private Object value;
	private List<PhaseRelevance> phases = new ArrayList<PhaseRelevance>();
	
	public OperationContextVar() {
		
	}
	
	public OperationContextVar(String name, Object value, PhaseRelevance phase) {
		setName(name);
		setValue(value);
		getPhases().add(phase);
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * @return the phases
	 */
	public List<PhaseRelevance> getPhases() {
		return phases;
	}

	/**
	 * @param phases the phases to set
	 */
	public void setPhases(List<PhaseRelevance> phases) {
		this.phases = phases;
	}
	
	public boolean isValueNull() {
		return getValue() == null;
	}
}
