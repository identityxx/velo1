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
package velo.actions.tools;

//TODO: Throw better exceptions!
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.User;
import velo.entity.UserRole;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.storage.Attribute;

public class AddGroupMembershipTools extends ResourceActionTools {
    
    /**
     * The generated NEW account ID
     */
    private String accountName;
    
    /**
     * The UserRole the account should be created for
     */
    private UserRole userRole;
    
    private ResourceGroup tsg;
    
    
    public AddGroupMembershipTools(Resource resource, ResourceGroup tsg) {
        super(resource);
        setTsg(tsg);
    }
    
    /**
     * @param accountName The accountName to set.
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    /**
     * @return Returns the accountName.
     */
    public String getAccountName() {
        return accountName;
    }
    
    /**
     * @param userRole The userRole to set.
     */
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
    
    /**
     * @return Returns the userRole.
     */
    public UserRole getUserRole() {
        return userRole;
    }
    
    /**
     * @param tsg The tsg to set.
     */
    public void setTsg(ResourceGroup tsg) {
        this.tsg = tsg;
    }
    
    /**
     * @return Returns the tsg.
     */
    public ResourceGroup getTsg() {
        return tsg;
    }
    
    
    
    //TODO Why this tools are not inherited from account's tools...? (Make sure it is possible since in addGroupMembership account does not exist yet!
    
    /**
     * Get a virtual account attribute for the specified attribute name for the SET user entity
     * @param tsAttrName The name of the target system attribute to fetch the virtual attribute object for
     * @return a VirtualAccountAttribute entity
     */
    @Deprecated
    public Attribute getVirtualAccountAttribute(Account account,String tsAttrName) throws NoResultFoundException {
        try {
            ResourceAttribute tsa = getTargetAttribute(tsAttrName);
            
            //TODO: Replace to local..
            Context ic = new InitialContext();
            AccountManagerRemote am = (AccountManagerRemote) ic.lookup(AccountManagerRemote.class.getName());
            UserManagerRemote userManager = (UserManagerRemote) ic.lookup(UserManagerRemote.class.getName());
            
            //User might be encapsulated in task references are cleared, reload the user before trying to load its attributes
            User loadedUser = userManager.reloadUser(account.getUser(), true);
            
            try {
                return am.loadVirtualAccountAttribute(loadedUser,tsa);
            } catch (NoUserIdentityAttributeValueException nuiave) {
                System.err.println(nuiave.getMessage());
                return null;
            } catch(NoUserIdentityAttributeFoundException nuiafe) {
                System.err.println(nuiafe.getMessage());
                return null;
            } catch (LoadingVirtualAccountAttributeException lvaae) {
                System.err.println(lvaae.getMessage());
                return null;
            }
        } catch(NamingException ne) {
            ne.printStackTrace();
            return null;
        }
        /*
        catch (NoResultFoundException nrfe) {
                System.out.println(nrfe.getMessage());
                return null;
        }
         */
    }
    
    
    
    
    
    
    /**
     *
     * Get a target system attribute for the specified attribute name for the SET Resource entity
     * @param attrName The name of the Resource attribute to get
     * @return an ResourceAttribute object
     */
    public ResourceAttribute getTargetAttribute(String uniqueName) throws NoResultFoundException {
        ResourceAttribute ra = getResource().getResourceAttribute(uniqueName);
        	
        if (ra == null) {
        	throw new NoResultFoundException("No result was found for Attribute name: " + uniqueName + "' on resource '" + getResource().getDisplayName());
        }
        else {
        	return ra;
        }
    }

    @Deprecated
    public Account loadAccountEntityByName(String accountName, String resourceName) {
        try {
            Context ic = new InitialContext();
            AccountManagerRemote am = (AccountManagerRemote) ic.lookup(AccountManagerRemote.class.getName());
            //return am.findAccount(accountName, resourceName);
            return null;
        } catch(NamingException ne) {
            ne.printStackTrace();
            //TODO: Naming exception has occured, should be handled correctly!
            return null;
        }
    }
}
