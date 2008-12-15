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

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import velo.resource.resourceDescriptor.ResourceDescriptorAdapter;

public class AdapterPoolConfig {
	GenericObjectPool.Config config = new GenericObjectPool.Config();
	private final boolean testOnBorrow = true;
	private final byte whenExhaustedAction  = 1; //1 means BLOCK
	
	private final int defaultMaxActive = 4;
	private final int defaultMaxIdle = 2;
	private final int defaultMaxWait = 1000;
	private final long defaultMinEvictableIdleTimeMillis = 3000;
	
	public AdapterPoolConfig() {
		setDefaults();
	}
	
	
	public AdapterPoolConfig(ResourceDescriptorAdapter tsda) {
		setDefaults();
		
		if (tsda.getMaxActive() != 0) {
			setMaxActive(tsda.getMaxActive());
		}
		
		if (tsda.getMaxIdle() != 0) {
			setMaxIdle(tsda.getMaxIdle());
		}
		
		if (tsda.getMaxWait() != 0) {
			setMaxWait(tsda.getMaxWait());
		}
		
		if (tsda.getMinEvictableIdleTimeMillis() != 0) {
			setMinEvictableIdleTimeMillis(tsda.getMinEvictableIdleTimeMillis());
		}
	}
	
	
	
	public void setDefaults() {
		setMaxActive(defaultMaxActive);
		setMaxIdle(defaultMaxIdle);
		setMaxWait(defaultMaxWait);
		setMinEvictableIdleTimeMillis(defaultMinEvictableIdleTimeMillis);
		setTestOnBorrow(testOnBorrow);
		setWhenExhaustedAction(whenExhaustedAction);
	}
	
	
	/**
	 * Maximum active adapters in the pool
	 * @param num
	 */
	public void setMaxActive(int num) {
		config.maxActive = num;
	}
	
	/**
	 * Maximum idle adapters in the pool
	 * @param num Number of idle adapters in the pool
	 */
	public void setMaxIdle(int num) {
		config.maxIdle  = num;
	}
	
	/**
	 * Mminimum idle adapters in the pool
	 * @param num Number of minimum idle adapters to set
	 */
	public void setMinIdle(int num) {
		config.minIdle = num;
	}
	
	/**
	 * The pool will wait the specified time if an Adapter is not available. 
	 * The maxWait value , however, depends on the strategy we specify. The default strategy is to block. The other possible values are fail and grow which are self-explanatory
	 */
	public void setMaxWait(int num) {
		config.maxWait = num;
	}
	
	
	public void setWhenExhaustedAction(byte action) { 
		config.whenExhaustedAction = action;
	}
	
	/**
	 * Evictor runs every milSeconds as specified.
	 * @param milSeconds - The milliseconds as the interal of Evictor execution
	 */
	public void setMinEvictableIdleTimeMillis(long milSeconds) {
		config.minEvictableIdleTimeMillis = milSeconds;
	}
	
	/**
	 * Retrieve a PoolConfig required to initialize a new AdapterPool
	 * @return A Config AdapterPool object
	 */
	public Config getPoolConfig() {
		return config;
	}
	
	/**
	 * Whether or not to check if the Adapter is active 
	 */
	public void setTestOnBorrow(boolean test) {
		config.testOnBorrow = test;
	}
}
