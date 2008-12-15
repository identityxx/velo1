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
package velo.actions.genericTelnet;

import java.util.logging.Logger;

import velo.actions.ResourceAction;
import velo.adapters.GenericHttpClientAdapter;
import velo.entity.Resource;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.query.Query;

/**
 * The base call of all actions that are based on a HTTP Client adapter type.
 *
 * @author Asaf Shakarchi
 */
abstract public class TelnetResourceAction extends ResourceAction {

	/**
	 *  A reference to a query object, used in the actions code.
	 */
	Query query = new Query();

	private static Logger logger = Logger.getLogger(TelnetResourceAction.class.getName());

	/**
	 * Set the resource into the class
	 * @param resource The resource entity to set
	 */
	public TelnetResourceAction(Resource resource) {
		super(resource);
	}

	/**
	 * Empty constructor
	 * Used for easy factory by reflection
	 */
	public TelnetResourceAction() {

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
	 * Get the specific adapter for this adpater actions type
	 * @return GenericHttpClientAdapter a GenericHttpClientAdapter object
	 */
	public GenericHttpClientAdapter getAdapter() throws AdapterException {
		return (GenericHttpClientAdapter) super.getResourceAdapter();
	}

	public boolean execute() throws ActionFailureException {
		try {
			getAdapter().connect();
		}
		catch (AdapterException ae) {
			throw new ActionFailureException(ae.getMessage());
		}
		
		return true;
	}
}