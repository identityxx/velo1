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

import org.apache.commons.pool.impl.GenericObjectPool;

import velo.common.UniqueIdGenerator;
import velo.exceptions.AdapterPoolException;

public class AdapterPool extends GenericObjectPool {
	private static Logger logger = Logger.getLogger(AdapterPool.class.getName());
	/**
	 * A unique ID of the pool
	 */
	private String uniqueId;
	
	/**
	 * Constructor,
	 * Initialize the pool by the specified adapterFactory and adapter pool config
	 * @param adapterFactory
	 * @param config
	 */
	public AdapterPool(AdapterFactory adapterFactory,AdapterPoolConfig apc) {
		super(adapterFactory, apc.getPoolConfig());
		setUniqueId("Adapter Pool:"+UniqueIdGenerator.generateUniqueIdByUID());
	}
	
	public AdapterPool(AdapterFactory adapterFactory) {
		super(adapterFactory,new AdapterPoolConfig().getPoolConfig());
		setUniqueId("Adapter Pool:"+UniqueIdGenerator.generateUniqueIdByUID());
	}
	
	public Object borrowObject() throws AdapterPoolException {
		try {
			logger.fine("borrowing adapter from pool..!");
			return super.borrowObject();
		} catch (Exception e) {
			throw new AdapterPoolException(e.getMessage());
		}
	}
	
	public void returnObject(Object obj) throws AdapterPoolException {
		try {
			logger.fine("returning adapter to pool.." + obj);
			super.returnObject(obj);
		}
		catch (Exception e) {
			throw new AdapterPoolException(e);
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}
}
