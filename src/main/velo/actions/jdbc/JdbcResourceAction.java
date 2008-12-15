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

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.logging.Logger;

import velo.actions.ResourceAction;
import velo.adapters.JdbcAdapter;
import velo.entity.Resource;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.query.Query;

/**
 * The base call of all actions that are based on a JDBC adapter type.
 *
 * @author Asaf Shakarchi
 */
abstract public class JdbcResourceAction extends ResourceAction {

	/**
	 *  A reference to a query object, used in the actions code.
	 */
	Query query = new Query();

	private transient static Logger logger = Logger.getLogger(JdbcResourceAction.class.getName());

	/**
	 * Set the Resource into the class
	 * @param resource The Resource entity to set
	 */
	public JdbcResourceAction(Resource resource) {
		super(resource);
	}

	/**
	 * Empty constructor
	 * Used for easy factory by reflection
	 */
	public JdbcResourceAction() {

	}

	/**
	 *  Replaces the old query object with a new query object.
	 *  
	 *  @param query the new query object to set inside the action.
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	/**
	 * Returns a reference to the currently used query object
	 * in the action.
	 * 
	 * @return A referance to the currently used query.
	 */
	public Query getQuery() {
		return query;
	}

	//Override parent getAdapter function to return the specific typed adapter for this action type
	//@return A JdbcAdapter factored by the corresponding Resource entity
	/*
	 public JdbcAdapter getAdapter() {
	 //Cast the adapter to a JDBC adapter
	 return (JdbcAdapter)super.getAdapter();
	 }
	 */

	/**
	 * Get the specific adapter for this adpater actions type
	 * @return JdbcAdapter a JdbcAdapter object
	 */
	public JdbcAdapter getAdapter() throws AdapterException {
		return (JdbcAdapter) super.getResourceAdapter();
	}

	/**
	 * Builds the internal query that
	 * the action will run on a traget system.
	 *
	 * TODO: Currently no automated query builder is supported
	 * This must get implemented by a script on the Resource actions level!
	 * 
	 * Scripted by user, this must return a value to indicate if everything went ok or not
	 * @return true if build was successfull, false if failed
	 */
	protected abstract boolean buildQuery();

	public boolean execute() throws ActionFailureException {
		//Important to first connect to the target system, 'buildquery()' (which is scripted) might already use the adapter to execute perliminary queries
		try {
			getAdapter().connect();
		} catch (AdapterException ae) {
			throw new ActionFailureException(ae.getMessage());
		}

		//Build the member query implemented in child
		//Note: No automated query generator, currently expected by the Scripted Resource Action to set the query into the query object!
		
		//Exepcting buildQuery to return true! otherwise fail and expect an error to be set into the error message queue
		
		
		//ANY SCRIPTED METHOD EXECUTION SHOULD BE VALIDATED VERY CAREFULLY
		try {
		if (!buildQuery()) {
			StringBuilder msg = new StringBuilder();
			msg.append("FAILED to build query for action, failure message: ");
			msg.append(getMsgs().toString());
			
			throw new ActionFailureException(msg.toString());
		}
		}
		//If a method was not found
		catch (MissingMethodException mme) {
			throw new ActionFailureException(mme.toString());
		}
		//If a method is called over a null object
		catch (NullPointerException npe) {
			throw new ActionFailureException(npe.toString());
		}
		//When a method is called over a non existend 'object'
		catch (MissingPropertyException mpe) {
			throw new ActionFailureException(mpe.toString());
		}
		catch (AbstractMethodError ame) {
			throw new ActionFailureException(ame.toString());
		}
		
		
		/*TODO: logs */
		//Execute the query by the Adapter!
		//logger.info("Executing action with the following QUERY string: " + getQuery().getQueryString());
		
		/*
		logger.info("Adapter object is: " + getAdapter()
				+ ", adapter class name is: "
				+ getAdapter().getClass().getName());
		*/
		//Connect to the TS by the adapter
		//Execute the query!
		//Expecting a result from the executed action!
		try {
			getAdapter().runQuery(getQuery());
			/*TODO : logs!*/
			return true;
		}
		catch (AdapterException fe) {
			throw new ActionFailureException(fe.getMessage());
		} 
	}
	
	
	public boolean cleanup() {
		try {
			logger.fine("Disconnecting adapter...");
			getAdapter().disconnect();
			logger.fine("Successfully disconnected adapter...");
			return true;
		}
		catch (AdapterException ae) {
			logger.warning("An adapter exception has occured while trying to disconnect adapter, failure message: " + ae);
			return false;
		}
	}
}