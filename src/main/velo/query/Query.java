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
package velo.query;

import java.util.LinkedList;

/**
 * @author Asaf Shakarchi
 * 
 * A class used to abstract a query.
 * 
 * NOTE: Every sql keyword should be in uppercase,
 * for cachign reasons.
 */
public class Query {

	public enum QueryType {
		SELECT, INSERT, DELETE, UPDATE, ISTRUE
	}
	
	private QueryType queryType;
	private LinkedList<SingleQuery> queries = new LinkedList<SingleQuery>();
	
	
	/**
	 * Constructor
	 */
	public Query() {
		//Set default query type option to SELECT
		this.setQueryType(QueryType.SELECT);
	}
	
	public Query(QueryType queryType) {
		//Set default query type option to SELECT
		this.setQueryType(queryType);
	}

	
	public void add(String queryString, Object... params) {
		SingleQuery q = new SingleQuery(this,queryString,params);
		getQueries().add(q);
	}
	
	public void addAll(Query queryManager) {
		getQueries().addAll(queryManager.getQueries());
	}
	
	
	public boolean isHasQueries() {
		return getQueries().size() > 0;
	}
	
	public void clear() {
		getQueries().clear();
	}
	
	public int queryAmount() {
		return getQueries().size();
	}
	
	
	
	
	
	
	
	//accessors
	/**
	 * @return the queryType
	 */
	public QueryType getQueryType() {
		return queryType;
	}


	/**
	 * @param queryType the queryType to set
	 */
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}


	/**
	 * @return the queries
	 */
	public LinkedList<SingleQuery> getQueries() {
		return queries;
	}


	/**
	 * @param queries the queries to set
	 */
	public void setQueries(LinkedList<SingleQuery> queries) {
		this.queries = queries;
	}
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int i=0;
		for (SingleQuery currSQ : getQueries()) {
			i++;
			sb.append("Query["+i+"] string: " + currSQ.getQueryString() + ", params: "); 
			Object[] params = currSQ.getParams();
			for (int l=0;l<params.length;l++) {
				sb.append("Param[" + l + "]: ");
				if (params[l] != null) {
					sb.append(params[l].toString());
				} else {
					sb.append("Is null!");
				}
				
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
}
