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
package velo.ejb.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import velo.adapters.AdapterFactory;
import velo.adapters.AdapterPool;
import velo.adapters.ResourceAdapter;
import velo.ejb.interfaces.AdapterManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.entity.Resource;
import velo.exceptions.AdapterPoolException;
import velo.exceptions.ResourceDescriptorException;
import velo.patterns.Factory;
import velo.resource.resourceDescriptor.ResourceDescriptor;


/**
 * A Stateful EJB bean for managing Adapters
 * Should be deployed in Application server per task-performer
 * 
 * @author Asaf Shakarchi
 */
@Stateful()
public class AdapterBean implements AdapterManagerLocal {
	
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;
	
	@EJB
	public ResourceManagerLocal tsm;

	private static Logger logger = Logger.getLogger(AdapterBean.class.getName());
	
	/**
	 * A map of pools per target system
	 */
	private Map<Class,AdapterPool> pools = new HashMap<Class,AdapterPool>();
	
	@Deprecated
	public ResourceAdapter getAdapter(Resource resource) throws AdapterPoolException {
		logger.entering(this.getClass().getName(), "getAdapter",new Object[]{(resource)});
		logger.finest("Getting adapter for Resource: '" + resource.getDisplayName() + "'");

		logger.finest("Getting Adapter Pool for the specified target");
		AdapterPool tsp = getPoolByResource(resource);
		
		logger.finest("Borrow an adapter from pool ID: " + tsp.getUniqueId());
		ResourceAdapter tsa = (ResourceAdapter)tsp.borrowObject();
		logger.finest("Borrowed ResourceAdapter of class '" + tsa.getClass().getName()+ "'");
		logger.exiting(this.getClass().getName(), "getAdapter", tsa);
		
		return tsa;
	}
	
	
	//Private methods - not exposed in EJB interfaces
	@Deprecated
	public AdapterPool getPoolByResource(Resource resource) throws AdapterPoolException {
		try {
			ResourceDescriptor tsd =  tsm.factoryResourceDescriptor(resource);
			//String adapterClassName = tsd.getResourceDescriptorAdapter().getClassName();
//JB			String adapterClassName = resource.getResourceType().getAdapterClassName();
			String adapterClassName = null;
			logger.finest("Adapter class name set in Resource type is: " + adapterClassName);
			
			//Cant trust that we got a value from target config file
			if (adapterClassName == null) {
				throw new AdapterPoolException(("Could not get adapter class name for Resource name: '" + resource.getDisplayName() + "', null recieved as an adapter class name."));
			}
			else {
				logger.finest("Retrieved adapter class name: '" + adapterClassName + "' from target, checking whether an adapter with such class name exists or not...");
			}
	
			//Factory the adapter class for the specified adapter class name
			Class adapterClass = Factory.factory(adapterClassName);
			logger.finest("Found a class that correspond to the specified adapter name, checking whether the found class is inherited from ResourceAdapter class..");
			//Make sure factored class's superclass is the correct type
			if (ResourceAdapter.class.isAssignableFrom(adapterClass)) {
				logger.finest("The found adapter is inherited from ResourceAdapter class, trying to find a pool of adapters for the specified target system");
				//Check if the MAP of pools already has a pool for the adapter
				if (pools.containsKey(resource.getClass())) {
					AdapterPool ap = pools.get(adapterClass);
					logger.finest("Found pool of adapters for the specified Resource, pool unique ID is: '" + ap.getUniqueId() + "'");
					return ap;
				}
				//Pool does not exist yet, create a new pool for the specified adapter
				else {
					logger.finest("Could not find a pool of adapters for the specified Resource, factoring a new adapter pool...");
					AdapterPool ap = new AdapterPool(new AdapterFactory(adapterClass,resource,tsd));
					pools.put(adapterClass, ap);
					logger.finest("Returning factored adapter pool for the specified Resource with uniqueID: '" + ap.getUniqueId() + "'");
					return ap;
				}
			}
			else {
				throw new AdapterPoolException("The specified adapter class named: '" + adapterClassName + "' must be inherited from class: " + ResourceAdapter.class.getName());
			}
		}
		catch (ClassNotFoundException cnfe) {
			throw new AdapterPoolException("Cannot initiate a new pool for target system name '" + resource.getDisplayName() + "'",cnfe);
		}
		catch (ResourceDescriptorException tsde) {
			throw new AdapterPoolException("Cannot initiate a new pool for target system name '" + resource.getDisplayName() + "'",tsde);
		}
	}
}
