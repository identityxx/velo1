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

import velo.entity.Resource;
import velo.exceptions.AdapterException;
import velo.query.Query;

/**
 * A QueryBased Adapter
 * 
 * @author Asaf Shakarchi
 */
public abstract class QueryBasedAdapter extends ResourceAdapter {
	/**
	 * Hold a referance to a query object 
	 * that the action works with
	 * 
	 * @see velo.actions.Action
	 */
	private Query query;
	
	/**
	 * Query timeout in seconds
	 */
	private int queryTimeout;

	public QueryBasedAdapter() {
		
	}
	
	public QueryBasedAdapter(Resource resource) {
		super(resource);
	}

	/*
	 * Constructor
	 * @param resource The resource entity the adapter works with
	 * @throws resourceDescriptorException
	 */
	/*
	public QueryBasedAdapter(resource resource) throws resourceDescriptorException {
		super(resource);
		init();
	}
	*/

	/**
	 * Whether the connection is estlbihsed or not
	 */
	boolean bConnectionEstablished;

	/**
	 * Runs the query constructed by the 
	 * action(through the Query object) on the target system held
	 * by this connection.
	 * 
	 * @param query The query object to execute
	 * @throws AdapterException         
	 */
	abstract public void runQuery(Query query) throws AdapterException;

	/**
	 * Initialize the connection, mostly intiialize a connector by calling
	 * createConnector() local method
	 * 
	 * @return	true if the initializion was finished successfully
	 * 			false if it failed.
	 */
	public abstract boolean init();

	/**
	 * Set the status of the connectivity
	 * @param Status - The status of the connection
	 * 
	 */
	public void setConnectionEstablished(boolean Status) {
		this.bConnectionEstablished = Status;
	}

	/**
	 * @return boolean The status of the connection
	 */
	public boolean getConnectionEstablished() {
		return this.bConnectionEstablished;
	}

	/**
	 * @param queryTimeout the queryTimeout to set
	 */
	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	/**
	 * @return the queryTimeout
	 */
	public int getQueryTimeout() {
		return queryTimeout;
	}

	/*
	 public void setQuery(Query query) {
	 this.query = query;
	 }

	 public Query getQuery() {
	 return query;
	 }
	 */

}
