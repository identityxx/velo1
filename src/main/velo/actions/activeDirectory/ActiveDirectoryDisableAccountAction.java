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


import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.AccountTools;
import velo.common.EdmMessage.EdmMessageType;
import velo.entity.User;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * Disable account action for ActiveDirectory SSL based adapter
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryDisableAccountAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
	private static final long serialVersionUID = 198730545231111412L;
	
	private User user;
	private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
	
	
	
	
	public boolean execute() throws ActionFailureException {
		logger.fine("EXECUTE() method for disabling account action for ActiveDirectory Target Type started");
		logger.fine("Disabling account on Resource name: '" + getResource().getDisplayName() + "', for account name: '" + getAccount().getName() + "'");
		
		try {
			//Performs the connectivity
			super.execute();
			
			AccountTools atools = (AccountTools)getTools();
			//Get the CN of the Account, required to locate the account object
			//String accountCN = atools.getVirtualAccountAttribute("CN").getAsString();
			
			//Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();

		    //TODO Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
		    String usersFullDn = null; //DEP(String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    
		    //Instead seek by accountID attribute
		    String accountName = getAccount().getName();
		    
		    try {
		    	SearchResult sr = getAdapter().getAccountBySamAccountName(accountName,usersFullDn);
		    	
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl",Integer.toString(UF_ACCOUNTDISABLE)));
					
				//Perform the update
				getAdapter().replaceAttributes(sr.getName(), mods);

				getAdapter().disconnect();
				return true;
		    }
		    catch (AccountWasNotFoundOnTargetException awnfote) {
		    	getMsgs().add(EdmMessageType.WARNING,"AccountWasNotFoundOnTargetException",awnfote.getMessage());
		    	logger.warning(awnfote.getMessage());
		    	//throw new ActionFailureException(awnfote.getMessage());
		    	return false;
		    }
    	}
    	catch (AdapterException ae) {
    		throw new ActionFailureException(ae.getMessage());
    	}
    	catch (ResourceDescriptorException tsde) {
    		throw new ActionFailureException(tsde.getMessage());
    	}
    	/*
    	catch (AccountWasNotFoundOnTargetException awnfote) {
    		logger.warning("Cannot disable account, the account was not found on target!");
    		throw new ActionFailureException(awnfote.getMessage());
    	}
    	*/
    	/*Used when fetched account by CN
    	catch (LoadingVirtualAccountAttributeException lvaae) {
    		throw new ActionFailureException(lvaae.getMessage());
    	}
    	*/
	}
	
	public boolean postActionOperation() {
		//TODO Make sure that the account was really got disabled!
		return true;
	}
	
	
	public Collection getActionResult() {
		return new ArrayList();
	}

	/**
	 * @param user The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return Returns the user.
	 */
	public User getUser() {
		return user;
	}
}
