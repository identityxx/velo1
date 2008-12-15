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
package velo.actions.activeDirectory;

import org.apache.log4j.Logger;
import velo.actions.ResourceAction;
import velo.actions.ResourceActionInterface;
import velo.adapters.ActiveDirectoryAdapter;
import velo.adapters.Adapter;
import velo.entity.Resource;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;

abstract public class ActiveDirectoryResourceAction extends ResourceAction implements ResourceActionInterface {

	private static Logger log = Logger.getLogger(ActiveDirectoryResourceAction.class.getName());
	//some useful constants from lmaccess.h
	public int UF_ACCOUNTDISABLE = 0x0002;
	public int UF_PASSWD_NOTREQD = 0x0020;
	public int UF_PASSWD_CANT_CHANGE = 0x0040;
	public int UF_NORMAL_ACCOUNT = 0x0200;
	public int UF_DONT_EXPIRE_PASSWD = 0x10000;
	public int UF_PASSWORD_EXPIRED = 0x800000;
	
	
	public ActiveDirectoryResourceAction(Resource ts) {
		super(ts);
	}
	
	public ActiveDirectoryResourceAction() {
		
	}
	
	/**
	 * Override parent getAdapter function to return the specific typed adapter for this action type
	 * @return A ActiveDirectorySSLAdapter 
	 */
	public ActiveDirectoryAdapter getAdapter() throws AdapterException {
		Adapter tsa = super.getResourceAdapter();
		log.trace("Got ADAPTER FROM parent!, adapter object is: " + tsa);
		ActiveDirectoryAdapter adapter = (ActiveDirectoryAdapter)tsa;
		
		//Cast the adapter to a JDBC adapter
		//return (ActiveDirectoryAdapter)super.getAdapter();
		return adapter;
	}
	
	//Mainly connect to the AD target.
	public boolean execute() throws ActionFailureException {
		log.debug("Connecting to target...");
		try {
			//	Connect to the Resource
			getAdapter().connect();
			if (getAdapter().isConnected()) {
				log.info("Adapter connected!");
				return true;
			}
		}
		catch (AdapterException ae) {
			throw new ActionFailureException(ae.getMessage());
		}
		
		return false;
	}
	
}
