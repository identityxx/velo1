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
package velo.actions.jdbc;

import velo.actions.ResourceAccountActionInterface;
import velo.adapters.JdbcAdapter;
import velo.query.Query;

/**
 * An interface of JDBC target system actions based
 * 
 * @author Asaf Shakarchi
 */
public interface ResourceAccountJdbcActionInterface extends ResourceAccountActionInterface {

	/**
	 * Unfortunately, this is required, due to the fact that 'resourceAction'(parent) has getAdapter() that returns, and Jdbcresource(child) has getAdapter() which returns -JdbcAdapter-
	 * This is supported in Java 1.5, but groovy doesnt know how to handle it, since both methods has the same signature, it doesnt know which to call
	 * When the correct type is overided in Interface, interface is used with the top priority, so this solves the issue calling getAdapter() in Groovy classes.
	 * Unfortunately, we'll have an interface per action type on each type of adapter, unless getAdapter() will return the same object as parent (currently class name Adapter)
	 **/
	public JdbcAdapter getAdapter();
	
	/**
	 * Get the query object from the class
	 * @return Query object
	 */
	public Query getQuery();

}
