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

import velo.entity.Resource;
import velo.exceptions.FactoryException;
import velo.patterns.Factory;

/**
 * A class to create an apropriate adapter
 * if you only got the system's data object you want to 
 * create an adapter for.
 * 
 * @author Rani sharim
 * @deprecated
 */
public class AdapterFactoryabbbbb extends Factory {
	
	/**
	 * //TODO:  Subclasses should not use this logger but their own, clean and set this logger as a private 
	 * A Logger object to log messages for this class
	 */
	public static Logger logger = Logger.getLogger("velo.adapters.AdapterFactory");
	
	/**
	 * Factory an adappter by a given adapter class name and a certain resource entity
	 * 
	 * @param adapterClassName The class name of the class we want
	 *        to create an instance of.
	 *        Must be of an adapter type.
	 * @param resource The resource entity the adapter works with        
	 *                   
	 * @return A new instance of the relevant adapter 
	 * @throws FactoryException
	 */
    public static ResourceAdapter adapterFactory(String adapterClassName, Resource resource) throws FactoryException {
    	try {
    		//Object c = factory(adapterClassName);
    		logger.info("Factoring adapter for class name: " + adapterClassName + ", for target system name: " + resource.getDisplayName());
    		
    		//14/02/07 -> Why to make it so complex? and force adapters to have special constructors?
    		//Object adapter = factory(adapterClassName, new Class[] {resource.class},new Object[] {resource});
    		ResourceAdapter adapter = (ResourceAdapter)factoryInstance(adapterClassName);
    		adapter.setResource(resource);
    		
    		/*
    		 Method[] a = adapter.getClass().getMethods();
    		 for (int i=0;i<a.length;i++) {
    		 System.out.println(a[i]);
    		 }
    		 */

    		//Cast the object to AdapterInterface and return the casted object
    		return adapter;
    	}
    	catch (FactoryException cnfe) {
    		throw new FactoryException(cnfe.getMessage());
    	}
    }
    
    /**
     * Attempts to create a ConnectorWrapper for a target 
     * system, given that system data object.
     * 
     * @see Adapter
     * 
     * @param resource the data object of the target system
     *                     to create the apropriate adapter for
     * @return A new instance of the approprite adapter
     *         for the given target system, or null if cant
     *         create one. 
     *         
     */
	public Adapter createAdapterForresource(Resource resource) {
		//TODO: Must map resourceType->Adapter in order to implement this function! 
		//Adapter a = new MysqlJdbcBasedAdapter();
		return null;
	}
}
