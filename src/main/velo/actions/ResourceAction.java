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
package velo.actions;

import java.util.Collection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import velo.actions.tools.ResourceActionTools;
import velo.adapters.ResourceAdapter;
import velo.ejb.interfaces.AdapterManagerLocal;
import velo.ejb.interfaces.ResourceManagerRemote;
import velo.entity.Account;
import velo.entity.Resource;
import velo.exceptions.AdapterException;
import velo.exceptions.AdapterPoolException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * The base class of all actions that are executed over Target Systems
 * 
 * @author Asaf Shakarchi
 */
//abstract public class ResourceAction extends Action implements ResourceActionInterface {
@Deprecated
abstract public class ResourceAction extends Action {
	private static Logger log = Logger.getLogger(ResourceAction.class.getName());
	
	/** 
	 * A reference to the target system the action is execution is performed on
	 **/
	private Resource resource;
	
	/**
	 * The adapter to the target, casted to a generic typed.
	 */
	private ResourceAdapter resourceAdapter;

    //private static AdapterManagerRemote adapterManager;

    /**
     * Some tools relevant to operations related to target systems.
     */
    private ResourceActionTools resourceActionTools;
    
    /**
     * <b>Note: MOST of the target system actions are handlign account entity (maybe except createAccount)
	 * Due to multiple enheritance limitations of java, this is the best play to set the account entity
	 * So in createAccount action, this will return null.</b>
     */
    private Account account;
    
    private ResourceDescriptor resourceDescriptor;
    
    //private static Logger logger = Logger.getLogger(ResourceAction.class.getName());
    
	/**
	 * Constractur, initialize a new ResourceAction with the specified Resource entity
	 * @param resource The Resource object the action is execution is performed on
	 */
	public ResourceAction(Resource resource) {
		setResource(resource);
	}

	/**
	 * Empty Constructor
	 * <b>Must be available since class can be scripted and scripts are factored by generic factoryManager that might not expect a specific constructor when factoring a script object</b> 
	 */ 
	public ResourceAction() {
	}
	
	
	/**
	 * @param account The account to set.
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * @return Returns the account.
	 */
	public Account getAccount() {
		return account;
	}


	public boolean validate() {
		//Validate that the 'Resource' entity is set (required for all target system typed actions!)
		if (getResource() == null) {
			getMsgs().severe("Resource action validation failed, 'Resource' entity must set for any target system action typed in order to perform this action!");
			return false;
		}
		return true;
	}
	

	/**
	 * Get the resourceAdapter for this action
	 * 
	 * @return Adapter An Adapter object
	 */
	public ResourceAdapter getResourceAdapter() throws AdapterException {
		log.trace("Getting an Adapter for action...");
		if (resourceAdapter != null) {
			//logger.fine("Adapter already factored for this class, returning current adapter");
			return resourceAdapter;
		}
		else {
			//adapter = getResource().adapterFactory();
			try {
				//logger.fine("Factoring adapter...");
				//17/02/07 -> No reason to retrieve the AdapterManager remotely since each server-agent will have its
				//own adapter pools (obviously connections cannot pass between servers anyway)
				//Also, had a annoying issue when after 'JdbcAdapter().connect()' was called, retrieving the adapter remotly 
				//resulted a 'Java.Logging cannot be serialized', but couldnt find why .connect() resulted such an exception.
				//loadEjb();
				//ResourceAdapter tsa = adapterManager.getAdapter((getResource()));
				//adapter = tsm.adapterFactory((getResource()));
				
				InitialContext ic = new InitialContext();
				AdapterManagerLocal adapterManager = (AdapterManagerLocal) ic.lookup("velo/AdapterBean/local");
				ResourceAdapter tsa = adapterManager.getAdapter(getResource());
				
				
				//We'd like the adapter to log into our current logger
				//adapter.logger = logger;
				//Also after factoring the adapter, set it into the class for next use
				//setAdapter(adapter);
				log.trace("Retrieved adapter object: " + tsa);
				setResourceAdapter(tsa);
				return tsa;
			}
			catch (NamingException ne) {
				getMsgs().add(ne.getMessage());
				throw new AdapterException(ne);
			}
			catch (AdapterPoolException ape) {
				getMsgs().add(ape.getMessage());
				throw new AdapterException(ape);
			}
			//TODO: Handle exceptions correctly!!!
			/*
			catch (FactoryException fe) {
				//logger.warning("Adapter class was not found while trying to factory with message: " + fe.getMessage());
				logger.warning("wTF?????????????????????" + fe);
				throw fe;
			}
			*/
		}
	}
	
	/**
	 * Set the resourceAdapter object
	 * @param resourceAdapter A resourceAdapter object to set
	 */
	public void setResourceAdapter(ResourceAdapter resourceAdapter) {
		this.resourceAdapter = resourceAdapter;
	}
	
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		if (!resource.isLoaded()) {
			try {
    			Context ic = new InitialContext();
    			//Must be remote since it could be executed in server agents
        		//GF ResourceManagerRemote tsm = (ResourceManagerRemote) ic.lookup(ResourceManagerRemote.class.getName());
    			//Remote, since actions might be executed remotely via remote performers
    			ResourceManagerRemote tsm = (ResourceManagerRemote) ic.lookup("velo/ResourceBean/remote");
        		try {
        			Resource loadedResource = tsm.loadResourceForAction(resource.getResourceId());
        			loadedResource.setLoaded(true);
        			setResource(loadedResource);
        			return loadedResource;
        		}
        		catch (NoResultFoundException nrfe) {
        			log.warn("Cannot get target system due to: " + nrfe);
        			return null;
        		}
    		}
			//TODO: Handle exceptions! correctly!
    		catch (NamingException ne) {
    			System.out.println("Couldnt load user!: " + ne);
    			return null;
    		}
		}
		else {
			return resource;
		}
	}
	
	
	/*
	 * @deprecated
	 */
	/*
	public void loadEjb() {
		//TODO: Replace adapter retrival by sort of generic pool object per application server.
		if (adapterManager == null) {
			try {
				//Context ic = new InitialContext();
				InitialContext ic = new InitialContext();
				//TODO: Replace to local interface, no reason to use remote
				AdapterManagerRemote adapterManager = (AdapterManagerRemote) ic.lookup(AdapterManagerRemote.class.getName());
				this.adapterManager = adapterManager;
			}
			catch(NamingException ne) {
				ne.printStackTrace();
			}
		}
	}
	*/
	
	/**
	 * Accessors for the ActionTools object, short method names are used for easy access by scripts
	 * @param resourceActionTools The resourceActionTools to set.
	 */
	public void setTools(ResourceActionTools resourceActionTools) {
		this.resourceActionTools = resourceActionTools;
	}

	/**
	 * Get the tools object
	 * @return Returns the resourceActionTools.
	 */
	public ResourceActionTools getTools() {
		return resourceActionTools;
	}
	
	public Collection getActionResult() {
		try {
			return getResourceAdapter().getResult();
		}
		catch (AdapterException fe) {
			//logger.warning("Could not return any adapter result due, due to error: " + fe.getMessage());
			return null;
		}
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
	public ResourceDescriptor getResourceDescriptor() throws AdapterException {
		try {
			if (resourceDescriptor != null) {
				return resourceDescriptor;
			}
			else {
				this.resourceDescriptor = getTools().getResourceDescriptor();
			}
			return resourceDescriptor;
		}
		catch (ResourceDescriptorException tsde) {
			throw new AdapterException(tsde.getMessage()); 
		}
	}
}
