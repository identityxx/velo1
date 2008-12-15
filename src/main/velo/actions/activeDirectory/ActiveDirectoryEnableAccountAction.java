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
 * Enable account action for ActiveDirectory SSL based adapter
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryEnableAccountAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
	private static final long serialVersionUID = 198730545231111412L;
	
	private User user;
	private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
	
	public boolean buildQuery() {
		return false;
	}
	
	public boolean execute() throws ActionFailureException {
		logger.info("EXECUTE() method for enabling account action for ActiveDirectory Target Type started");
		
		logger.fine("Enabling account on Resource name: '" + getResource().getDisplayName() + "', for account name: '" + getAccount().getName() + "'");
		
		try {
			//Performs the connectivity
			super.execute();
			
			
			AccountTools atools = (AccountTools)getTools();
			//Get the CN of the Account, required to locate the account object
			//String accountCN = atools.getVirtualAccountAttribute("CN").getAsString();
			
			//Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();

		    
		    //Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
		    String usersFullDn = null; //DEP (String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    //SearchResult sr = getAdapter().getAccount(accountCN, usersFullDn);
		    String accountName = getAccount().getName();
		    SearchResult sr = getAdapter().getAccountBySamAccountName(accountName,usersFullDn);
		    
		    
			
			
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT)));
			
			//Perform the update
			getAdapter().replaceAttributes(sr.getName(), mods);

			getAdapter().disconnect();
			return true;
    	}
    	catch (AdapterException ae) {
    		getMsgs().add(EdmMessageType.SEVERE,"AdapterException",ae.getMessage());
    		throw new ActionFailureException(ae.getMessage());
    	}
    	catch (ResourceDescriptorException tsde) {
    		getMsgs().add(EdmMessageType.SEVERE,"ResourceDescriptorException",tsde.getMessage());
    		throw new ActionFailureException(tsde.getMessage());
    	}
    	catch (AccountWasNotFoundOnTargetException awnfote) {
    		String errMsg = "Cannot enable account, the account was not found on target,with detailed message: " + awnfote.getMessage();
    		getMsgs().add(EdmMessageType.SEVERE,"AccountWasNotFoundOnTargetException", errMsg);
    		logger.warning(errMsg);
    		throw new ActionFailureException(errMsg);
    	}
    	/*Used when account was fetched by CN
    	catch (LoadingVirtualAccountAttributeException lvaae) {
    		throw new ActionFailureException(lvaae.getMessage());
    	}
    	*/
	}
	
	public boolean postActionOperation() {
		//TODO: Make sure that the account was really got enabled!
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
