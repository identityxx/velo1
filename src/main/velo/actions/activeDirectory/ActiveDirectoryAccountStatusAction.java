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

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.AccountTools;
import velo.entity.User;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * Check status of an account action for ActiveDirectory based adapter
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryAccountStatusAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
	private static final long serialVersionUID = 198730545231111412L;
	
	private User user;
	private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
	Collection actionResult;
	
	
	public boolean execute() throws ActionFailureException {
		logger.info("EXECUTE() method for checking action's status for ActiveDirectory Target Type started");
		logger.info("Checking account status on Resource name: " + getResource().getDisplayName() +", for account name: " + getAccount().getName());
		
		try {
			//Performs the connectivity
			super.execute();
			
			AccountTools atools = (AccountTools)getTools();
			
			//Not anymore, fetch account by account name, not by cn (see 'ActiveDirectoryDelteAccountAction) for complete details
			//Get the CN of the Account, required to locate the account object
			//String accountCN = atools.getVirtualAccountAttribute("CN").getAsString();
			
			//Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();

		    //TODO Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
		    String usersFullDn = null; //DEP (String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    //SearchResult sr = getAdapter().getAccount(accountCN, usersFullDn);
		    String accountName = getAccount().getName();
	    	SearchResult sr = getAdapter().getAccountBySamAccountName(accountName,usersFullDn);
		    	

		    Long userAccountControl;
			userAccountControl = Long.parseLong((String)sr.getAttributes().get("userAccountControl").get());

			Collection<String> result = new ArrayList<String>();
			if ((userAccountControl & UF_ACCOUNTDISABLE) == UF_ACCOUNTDISABLE) {
 	    		result.add("Disabled");
 	    	}
 	    	else {
 	    		result.add("Enabled");
 	    	}
			setActionResult(result);
 	    	
			
		    //String accountDNToDelete = "CN="+getAccount().getName() + ","+(String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    //logger.info("Deleting account full DN: " + accountDNToDelete);
		    
		    //Delete the sub context
			//getAdapter().getLcx().destroySubcontext(accountDNToDelete);
			
			
			return true;
    		
    	}
    	catch(NamingException ne) {
    		ne.printStackTrace();
    		return false;
    	}
    	catch (AdapterException ae) {
    		throw new ActionFailureException(ae.getMessage());
    	}
    	catch (ResourceDescriptorException tsde) {
    		throw new ActionFailureException(tsde.getMessage());
    	}
    	catch (AccountWasNotFoundOnTargetException awnfote) {
    		throw new ActionFailureException(awnfote.getMessage());
    	}
    	/*Used when fetched account by CN
    	catch (LoadingVirtualAccountAttributeException lvaae) {
    		throw new ActionFailureException(lvaae.getMessage());
    	}
    	*/
	}
	
	
	//TODO Like JDBC should be replaced with the values recieved by the adapter? are they fit a collection or should be converter to a collection..?
	public Collection getActionResult() {
		return actionResult;
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
	
	public void setActionResult(Collection ar) {
		actionResult = ar;
	}
}
