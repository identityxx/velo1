/**
 * Copyright (c) 2000-2007, Asaf Shakarchi
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
package velo.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import velo.query.Query.QueryType;

public class SingleQuery {
	private Query queryManager;
	private String queryString;
	private Object[] params;
	private QueryType queryType;
	
	
	public SingleQuery(Query queryManager, String queryString, Object... params) {
		setQueryManager(queryManager);
		setQueryString(queryString);
		setParams(params);
	}
	
	public void setQuery(String queryString, Object... params) {
		setQueryString(queryString);
		setParams(params);
	}

	
	public PreparedStatement factoryPreparedStatement(Connection c) throws SQLException { 
		PreparedStatement ps = c.prepareStatement(getQueryString());
		
		Object[] params = getParams();
		for (int i=0;i<params.length;i++) {
			ps.setObject(i+1,params[i]);
		}
		
		return ps;
	}
	
	
	
	/**
	 * @return the queryManager
	 */
	public Query getQueryManager() {
		return queryManager;
	}

	/**
	 * @param queryManager the queryManager to set
	 */
	public void setQueryManager(Query queryManager) {
		this.queryManager = queryManager;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString the queryString to set
	 */
	private void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	/**
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	private void setParams(Object[] params) {
		this.params = params;
	}

	/**
	 * @return the queryType
	 */
	public QueryType getQueryType() {
		if (queryType == null) {
			return queryManager.getQueryType();
		} else {
			return queryType;
		}
	}

	/**
	 * @param queryType the queryType to set
	 */
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}
}
