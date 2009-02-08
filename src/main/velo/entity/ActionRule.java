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
import javax.persistence.MappedSuperclass;
//@!@clean
@MappedSuperclass
public class ActionRule extends ActionDefinition {
	private static final long serialVersionUID = 1L;
	
	private int sequence;
	
	private boolean showStopper;
	
	private boolean active;

	/**
	 * @return the sequence
	 */
	@Column(name="SEQUENCE")
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * @return the showStopper
	 */
	@Column(name="SHOW_STOPPER", nullable=false)
	public boolean isShowStopper() {
		return showStopper;
	}


	/**
	 * @param showStopper the showStopper to set
	 */
	public void setShowStopper(boolean showStopper) {
		this.showStopper = showStopper;
	}
	
	
	/**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * @return the active
     */
    @Column(name="ACTIVE", nullable=false)
    public boolean isActive() {
        return active;
    }
    
    
    //just for not having this class abstract coz we need to initiate the generic gui rules
    public void updateScriptedObject(GroovyObject scriptedObject) {
    	//TODO: how to force overriden without having this abstract? maybe throw exception
    }
    
}
