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
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.AddGroupMembershipTools;
import velo.entity.User;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

/**
 * An action to add an account group membership to for ActiveDirectory.
 *
 * @author Asaf Shakarchi
 */
public class ActiveDirectoryAddGroupMembershipAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
    private static final long serialVersionUID = 198730545231111412L;
    
    private User user;
    private static Logger logger = Logger.getLogger(ActiveDirectoryDeleteAccountAction.class.getName());
    
    
    public boolean execute() throws ActionFailureException {
        logger.fine("EXECUTE() method for adding account group membership action for ActiveDirectory Target Type started");
        
        try {
            //Performs the connectivity
            super.execute();
            
            AddGroupMembershipTools atools = (AddGroupMembershipTools)getTools();
            String accountName = atools.getAccountName();
            String groupName = atools.getTsg().getUniqueId();
            logger.fine("Adding account group membership on Resource name: '" + getResource().getDisplayName() + "', for account name: '" + accountName + "'");
            
            //Not anymore, fetch account by account name, not by cn (see 'ActiveDirectoryDelteAccountAction) for complete details
            //Get the CN of the Account, required to locate the account object
            //String accountCN = atools.getVirtualAccountAttribute(account,"CN").getAsString();
            
            
            //Create the user on the container set in the XML conf file
            ResourceDescriptor tsd = atools.getResourceDescriptor();
            
            
            //Make sure that the account was found on the namespace.
            //TODO Construct the DN, should be really improved and not statically defined in TS XML Descriptior but in User Fields.
            String usersFullDn = null; //DEP(String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
            //SearchResult sr = getAdapter().getAccount(accountCN, usersFullDn);
            SearchResult sr = getAdapter().getAccountBySamAccountName(accountName,usersFullDn);
            
            //Perform the Group membership modifications into the namespace.
            ModificationItem member[] = new ModificationItem[1];
            member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", sr.getName()));
            getAdapter().getLdapManager().getLdapContext().modifyAttributes(groupName, member);
            
            getAdapter().disconnect();
            return true;
        } catch (javax.naming.NameAlreadyBoundException nabe) {
            //TODO Add it as a task log...!
            logger.warning("The account is already a member of the group, failure message: " + nabe.getMessage());
            return true;
        } catch(NamingException ne) {
            ne.printStackTrace();
            return false;
        } catch (AdapterException ae) {
            throw new ActionFailureException(ae.getMessage());
        } catch (ResourceDescriptorException tsde) {
            throw new ActionFailureException(tsde.getMessage());
        } catch (AccountWasNotFoundOnTargetException awnfote) {
            logger.warning("Cannot add account group membership, the account was not found on target!");
            throw new ActionFailureException(awnfote.getMessage());
        }
        /*
        catch (NoResultFoundException nrfe) {
                throw new ActionFailureException(nrfe.getMessage());
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
