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
import java.util.List;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.RemoveGroupMembershipTools;
import velo.entity.Account;
import velo.entity.User;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import edu.vt.middleware.ldap.LdapUtil;

/**
 * An action to remove an account group membership to for ActiveDirectory.
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryRemoveGroupMembershipAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
	private static final long serialVersionUID = 198730545231111412L;
	
	private User user;
	private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
	
	
	public boolean execute() throws ActionFailureException {
		logger.info("EXECUTE() method for adding account group membership action for ActiveDirectory Target Type started");
		logger.fine("Remove group membership assocaition Resource name: '" + getResource().getDisplayName() + "', for account name: '" + getAccount().getName() + "'");
		
		try {
			//Performs the connectivity
			super.execute();
			
			RemoveGroupMembershipTools atools = (RemoveGroupMembershipTools)getTools();
			Account account = atools.getAccount();
			String groupUniqueId = atools.getTsg().getUniqueId();
			logger.info("Removing account group membership on Resource name: " + getResource().getDisplayName() + ", from  account name: '" + account.getName() + "'");
			
			//Not anymore, fetch account by account name, not by cn (see 'ActiveDirectoryDelteAccountAction) for complete details 
			//Get the CN of the Account, required to locate the account object
			//String accountCN = atools.getVirtualAccountAttribute("CN").getAsString();
			
			logger.finest("Retreving ResourceDescriptor from account tools");
			//Create the user on the container set in the XML conf file
		    ResourceDescriptor tsd = atools.getResourceDescriptor();

		    //Make sure that the account was found on the namespace.
		    //TODO: Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
		    String usersFullDn = null; //DEP (String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
		    //SearchResult sr = getAdapter().getAccount(accountCN, usersFullDn);
		    SearchResult sr = getAdapter().getAccountBySamAccountName(getAccount().getName(),usersFullDn);
		    
		    Attribute memberOfAttribute = sr.getAttributes().get("memberof");

                    
                    //01-may-07: If the account is not a member of any group, a null is returned.
                    if (memberOfAttribute == null) {
                        getAdapter().disconnect();
                        return true;
                    }
                    
		    System.out.println("WTF?!?!?!: " + memberOfAttribute.getAll());
		    //Make sure that the account has the group association before removal (otherwise AD will throw an exception while trying to remove a group that is not associated!)
		    //If the group does not exist, then return true... since there is nothing to be removed...
		    //List<String> groupMembersValues = getAdapter().getAttributeValuesAsList(memberOfAttribute);
		    List<String> accountMemberOfGroupList = LdapUtil.deepCopy(memberOfAttribute.getAll());
		    /*logger.info("Dumping all groups that our account is member of (seeking for group ID: '" + groupUniqueId  +"')");
		    for (String currGroupMembership : groupMembersValues) {
		    	logger.info("Group FQDN: " + currGroupMembership);
		    }
		    */
		    
		    /*Could not use 'contains', since it is case sensitive, group names could be mistaken due to case sensitivity
			if (!groupMembersValues.contains(groupUniqueId)) {
				logger.warning("Could not remove group ID: '" + groupUniqueId + "' from ActiveDirectory target named: '" + getResource().getDisplayName() + "' since account name: '" + getAccount().getName() + "' is not a member of the group.");
				return true;
			}
			*/
		    
		    boolean foundGroupMembership = false;
		    for (String currGroupMember : accountMemberOfGroupList) {
		    	if (currGroupMember.toUpperCase().equals(groupUniqueId.toUpperCase())) {
		    		foundGroupMembership = true;
		    		break;
		    	}
		    }
		    if (!foundGroupMembership) {
				logger.warning("Could not remove group ID: '" + groupUniqueId + "' from ActiveDirectory target named: '" + getResource().getDisplayName() + "' since account name: '" + getAccount().getName() + "' is not a member of the group.");
				return true;
		    }
			
			
		    //Perform the Group membership modifications into the namespace.
		    ModificationItem member[] = new ModificationItem[1];
		    member[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("member", sr.getName()));
		    getAdapter().getLdapManager().getLdapContext().modifyAttributes(groupUniqueId, member);

			getAdapter().disconnect();
			return true;
    	}
		catch (javax.naming.NameAlreadyBoundException nabe) {
			//TODO: Add it as a task log...!
    		logger.warning("The account is already a member of the group, failure message: " + nabe.getMessage());
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
    		logger.warning("Cannot add account group membership, the account was not found on target!");
    		throw new ActionFailureException(awnfote.getMessage());
    	}
    	/*used when fetched account by CN
    	catch (LoadingVirtualAccountAttributeException lvaae) {
    		throw new ActionFailureException(lvaae.getMessage());
    	}
    	*/
	}
	
	public boolean postActionOperation() {
		//TODO: Make sure that the account was really got disabled!
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
