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

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.AccountTools;
import velo.common.EdmMessage.EdmMessageType;
import velo.entity.User;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.OperationException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * Delete account action for ActiveDirectory SSL based adapter
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryDeleteAccountAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
	private static final long serialVersionUID = 198730545231111412L;
	
	private User user;
	private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
	
	public boolean execute() throws ActionFailureException {
		logger.info("EXECUTE() method for deleting account action for ActiveDirectory Target Type started");
		logger.fine("Deleting account on Resource name: '" + getResource().getDisplayName() + "', for account name: '" + getAccount().getName() + "'");
		
		try {
			//Performs the connectivity
			super.execute();
			
			
			AccountTools atools = (AccountTools)getTools();
			
			//13-02-07: Recognizing by CN is not so good, since CN might be created by converters with dynamic fields(incremental), lets seek accounts by their original names as stored in repository
		    //Best way is to seek by SAMAccountName
			//Get the CN of the Account, required to locate the account object
			//String accountCN = atools.getVirtualAccountAttribute("CN").getAsString();
			
			
			
			//Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();

		    
		    
		    //TODO Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
		    String usersFullDn = null; //DEP (String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    
		    //no seek via CN anymore: String accountDNToDelete = "CN="+accountCN+ ","+usersFullDn;
		    //logger.info("Deleting account full DN: " + accountDNToDelete);
		    //Delete the sub context
			//getAdapter().getLcx().destroySubcontext(accountDNToDelete);
			
			
		    
		    //Instead seek by accountID attribute
		    String accountName = getAccount().getName();
		    try {
		    	//Map accountMapFromAD = getAdapter().parse(getAdapter().getAccountBySamAccountName(accountName,usersFullDn).getAttributes());
		    	String accountFQDN = getAdapter().getAccountBySamAccountName(accountName,usersFullDn).getName();
		    	getAdapter().getLdapManager().delete(accountFQDN);
		    }
		    catch (AccountWasNotFoundOnTargetException awnfote) {
		    	getMsgs().add(EdmMessageType.WARNING,"Account was not found on target",awnfote.getMessage());
		    	//throw new ActionFailureException(awnfote.getMessage());
		    	return true;
		    }
		    catch (NamingException ne) {
		    	getMsgs().add(EdmMessageType.WARNING,"NamingException", ne.getMessage());
		    	logger.warning("Could not delete account name: '" + getAccount().getName() + "' due to: " + ne);
		    	throw new ActionFailureException(ne.getMessage());
		    }
		    
			return true;
    		
    	}
    	catch (AdapterException ae) {
    		throw new ActionFailureException(ae.getMessage());
    	}
    	catch (ResourceDescriptorException tsde) {
    		throw new ActionFailureException(tsde.getMessage());
    	}
	}
	
	public boolean postActionOperation() {
		try {
			AccountTools atools = (AccountTools)getTools();
			//	Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();
			//String accountDNToDelete = "CN="+getAccount().getName() + ","+(String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
			//getAdapter().getLcx().lookup(accountDNToDelete);
		    
		    /*DESCRIPTOR IS DEP
		    if (getAdapter().isUserExistsBySamAccountName(getAccount().getName(),tsd.getSpecificAttributes().get("accounts-default-context-dn").toString())) {
		    	getMsgs().add(EdmMessageType.SEVERE, "Could not verify that the account was really deleted!","Failed to post validate delete account action for account named: '" + getAccount().getName() + "', on target system name: '" + getResource().getDisplayName() + "', seems like account still exists in target!");
		    	return false;
		    }
		    else {
		    	logger.info("Successfully validated delete operation of account named: '" + getAccount().getName() + "', on target system name: '" + getResource().getDisplayName() + "' !");
		    	return true;
		    }
		    */
		    //coz of dep
		    return true;
		}
		//If the entry was not found, then it got deleted by previous execute() method, so we'r ok. 
		/*DESC IS DEP catch (AdapterException ae) {
			String errMsg = "Factory Exception was occured while tyring to verify that the account was deleted, message: " + ae.getMessage();
			getMsgs().add(EdmMessageType.SEVERE,"Adapter Exception",errMsg);
			logger.warning(errMsg);
			return false;
		}*/
		catch (ResourceDescriptorException tsde) {
			String errMsg = "ResourceDescriptor Exception was occured while tyring to verify that the account was deleted, message: " + tsde.getMessage();
			getMsgs().add(EdmMessageType.SEVERE, "Resource Descriptor Exception",errMsg);
			logger.warning(errMsg);
			return false;
		}
		/*DESC IS DEP catch (OperationException oe) {
			logger.warning(oe.getMessage());
			getMsgs().add(EdmMessageType.SEVERE,"Operation Exception", oe.getMessage());
			return false;
		}*/
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
