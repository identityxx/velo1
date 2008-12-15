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

import java.util.logging.Logger;

import org.apache.commons.pool.BasePoolableObjectFactory;

import velo.entity.Resource;
import velo.exceptions.AdapterException;
import velo.patterns.Factory;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * Factory an adapter based on a specified adapter class, stored casted to a generic parent (ResourceAdapter) class.
 * @author Administrator
 *
 */
public class AdapterFactory extends BasePoolableObjectFactory {
	private static Logger logger = Logger.getLogger(AdapterFactory.class.getName());
	private Class adapterClass;
	private Resource resource;
	private ResourceDescriptor resourceDescriptor;
	
	public AdapterFactory(Class adapterClass, Resource ts, ResourceDescriptor tsd) { 
		this.adapterClass = adapterClass;
		resource = ts;
		resourceDescriptor = tsd;
	}
	
	
    // for makeObject we'll simply return a new an adapter casted to a generic Adapter. 
    public Object makeObject() throws Exception {
    	try {
    		logger.fine("Making a new adapter for adapter class: " + adapterClass.getName() + ", for Resource name: '" + resource.getDisplayName() + "'");
    		ResourceAdapter tsa = (ResourceAdapter)Factory.factoryInstance(adapterClass.getName());
    		tsa.setResource(resource);

    		try {
    			tsa.connect();
    			tsa.setPassivated(true);
    			return tsa;
    		}
    		catch (AdapterException ae) {
    			throw new Exception(ae.getMessage());
    		}
    	}
    	catch (ClassNotFoundException cnfe) {
    		throw new Exception(cnfe);
    	}
    } 
     
    //Explaination: Method passivateObject() tries to uninitialize the state of the underlying object. After an object's state is uninitialized,
    // When the adapter is returned to the pool, flag it it as passivated 
    public void passivateObject(Object obj) { 
    	if (obj instanceof Adapter) {
    		Adapter tsa = (Adapter)obj;
    		tsa.setPassivated(true);
    		logger.info("Returning adapter to the pool, of adapter class: " + tsa.getClass().getName() + ", with uniqueID: " + tsa.getUniqueId());
    	}
    	else {
    		logger.warning("Cannot passivate object of class '" + obj.getClass().getName() + "' is not an instance of 'Adapter'");
    	}
    } 
    
    //Method activateObject() tries to activate or initialize the state of the underlying object.
    /*
    public void activateObject(Object obj) {
    	if (obj instanceof Adapter) {
    		Adapter tsa = (Adapter)obj;
    		if (!tsa.isConnected()) {
    			try {
    				tsa.connect();
    				tsa.setPassivated(true);
    			}
    			catch (AdapterException ae) {
    			}
    		}
    		else {
    			tsa.setPassivated(false);
    		}
    		logger.info("Activating adapter with ID: " + tsa.getUniqueId());
    	}
    	else {
    		logger.warning("Cannot activate object of class '" + obj.getClass().getName() + "' is not an instance of 'Adapter'");
    	}
     }
     */
     
    //Disconnect the adapter and removes the Adapter object from the pool
    public void destroyObject(Object obj) {
    	if (obj instanceof Adapter) {
    		Adapter tsa = (Adapter)obj;
    		//RE-ADD tsa.disconnect();
    	}
    	else {
       		logger.warning("Cannot destroy object of class '" + obj.getClass().getName() + "' is not an instance of 'Adapter'");
    	}
    }

    //Explaination: Method validateObject() tries to validate the object it receives from the pool. The validation method returns a Boolean value indicating whether an object returned from the pool is usable.
    //Verify that the adapter is connected, otherwise return false.
    //this method is called after the activateObject() method is called, and only when the testOnBorrow, testOnReturn, or testOnIdle is set.
    /*
    public boolean validateObject(Object obj) {
    	if (obj instanceof Adapter) {
    		Adapter tsa = (Adapter)obj;
    		if (tsa.isConnected()) {
    			return true;
    		}
    		else {
    			return false;
    		}
    		return true;
    	}
    	else {
       		logger.warning("Cannot validate object of class '" + obj.getClass().getName() + "' is not an instance of 'Adapter'");
       		return false;
    	}
    }
    */


	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}


	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}


	/**
	 * @param resourceDescriptor the resourceDescriptor to set
	 */
	public void setResourceDescriptor(ResourceDescriptor resourceDescriptor) {
		this.resourceDescriptor = resourceDescriptor;
	}


	/**
	 * @return the resourceDescriptor
	 */
	public ResourceDescriptor getResourceDescriptor() {
		return resourceDescriptor;
	}
}