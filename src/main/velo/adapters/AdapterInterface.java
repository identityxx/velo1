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
package velo.adapters;

import java.io.Serializable;

import velo.exceptions.AdapterException;

/**
 * An Adapter interface for all Adapters
 * 
 * @author Asaf Shakarchi
 */
public interface AdapterInterface extends Serializable {
	
	/**
	 * Connect method to establish communication with the System
	 * @return true/false upon success/failure
	 * @throws AdapterException
	 */
	public abstract boolean connect() throws AdapterException;
	
	/**
	 * Disconnect from the system
	 * @return true/false upon success/failure
	 */
	public boolean disconnect();
	
	/*
	 * Get the adapter Result(from the last command execution) as a LIST
	 * 
	 * @return The result from the last command exectuion as a LIST
	 */
	//public Map getAdapterResult();
}
