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
import velo.ejb.interfaces.ResourceAttributeManagerRemote;
import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.storage.Attribute;

/**
 * Account action tools, used by user scripts for easy access of required objects
 *
 * @author Asaf Shakarchi
 */
public class AccountTools extends ResourceActionTools {
    
    /**
     * A reference to the user the account is attached to
     */
    private User user;
    
    /**
     * If its a password reset action, this is the password to reset the account to
     */
    private String password;
    
    
    /**
     * Constructor, initialize references to User/Resource entities
     * @param user
     * @param resource
     */
    public AccountTools(User user, Resource resource) {
        super(resource);
        setUser(user);
    }
    
    /**
     * Set the User entity object to the class
     * @param user The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Get the User entity object to the class
     * @return Returns the user entity object.
     */
    public User getUser() {
        if (!user.getLoaded()) {
            try {
                Context ic = new InitialContext();
                //Must be remote since it could be executed in server agents
                UserManagerRemote userManager = (UserManagerRemote) ic.lookup("velo/UserBean/remote");
                
                return userManager.findUser(user.getName());
            } catch (NamingException ne) {
                System.out.println("Couldnt load user!: " + ne);
                return null;
            }
        } else {
            return user;
        }
    }
    
    
    /**
     * Get a user attribute for the user the account that will be created will attach to
     * @param attrName
     * @return a UserIdentityAttribute object or NULL if no attribute was found!
     */
    public UserIdentityAttribute getUserAttribute(String attrName) {
        try {
            //TODO: Replace to local..
            Context ic = new InitialContext();
            UserManagerRemote userm = (UserManagerRemote) ic.lookup(UserManagerRemote.class.getName());
            try {
                return userm.getUserIdentityAttribute(getUser(),attrName);
            } catch (NoUserIdentityAttributeFoundException nuiafe) {
                System.out.println("Couldnt load user identity attribute, exiting with message: " + nuiafe.getMessage());
                return null;
            }
        } catch(NamingException ne) {
            ne.printStackTrace();
            return null;
        }
    }
    
    
    /**
     *
     * Get a target system attribute for the specified attribute name for the SET Resource entity
     * @param attrName The name of the Resource attribute to get
     * @return an ResourceAttribute object
     */
    @Deprecated //wtf?! for which resrouce!?
    public ResourceAttribute getTargetAttribute(String uniqueName) throws NoResultFoundException {
        try {
            //TODO: Replace to local..
            Context ic = new InitialContext();
            ResourceAttributeManagerRemote ram = (ResourceAttributeManagerRemote) ic.lookup("velo/ResourceAttributeBean/remote");
            //return ram.findResourceAttribute(uniqueName, getResource());
            //JB
            return null;
        } catch(NamingException ne) {
            ne.printStackTrace();
            return null;
        }
        /*JB
        } catch (NoResultFoundException nrfe) {
            //System.out.println("No result was found for Attribute name: " + attrName + ", failed with message: " + nrfe.getMessage());
            throw new NoResultFoundException("No result was found for Attribute name: " + uniqueName + ", failed with message: " + nrfe.getMessage());
        }
        */
    }
    
    /**
     * Get a virtual account attribute for the specified attribute name for the SET user entity
     * @param tsAttrName The name of the target system attribute to fetch the virtual attribute object for
     * @return a VirtualAccountAttribute entity
     */
    public Attribute getVirtualAccountAttribute(String tsAttrName) throws LoadingVirtualAccountAttributeException {
        try {
            ResourceAttribute tsa = getTargetAttribute(tsAttrName);
            
            //TODO: Replace to local..
            Context ic = new InitialContext();
            AccountManagerRemote am = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
            UserManagerRemote userManager = (UserManagerRemote) ic.lookup("velo/UserBean/remote");
            
            User loadedUser = userManager.reloadUser(getUser(),true);
            
            try {
                return am.loadVirtualAccountAttribute(loadedUser,tsa);
            } catch (NoUserIdentityAttributeValueException nuiave) {
                throw new LoadingVirtualAccountAttributeException(nuiave.getMessage());
                //System.err.println(nuiave.getMessage());
                //return null;
            } catch(NoUserIdentityAttributeFoundException nuiafe) {
                throw new LoadingVirtualAccountAttributeException(nuiafe.getMessage());
                //System.err.println(nuiafe.getMessage());
                //return null;
            } catch (LoadingVirtualAccountAttributeException lvaae) {
                //System.err.println(lvaae.getMessage());
                //return null;
                throw new LoadingVirtualAccountAttributeException(lvaae.getMessage());
            }
        } catch(NamingException ne) {
            //ne.printStackTrace();
            //return null;
            throw new LoadingVirtualAccountAttributeException(ne.getMessage());
        } catch (NoResultFoundException nrfe) {
            throw new LoadingVirtualAccountAttributeException(nrfe.getMessage());
        }
        /*
        catch (NoResultFoundException nrfe) {
                System.out.println(nrfe.getMessage());
                return null;
        }
         */
    }
    
    //WHAT FOR? not enough by name?
    public Attribute getVirtualAccountAttribute(ResourceAttribute tsa) throws LoadingVirtualAccountAttributeException {
        try {
            //TODO: Replace to local..
            //(might be executed remotely, maybe no local replacement is needed?
            Context ic = new InitialContext();
            AccountManagerRemote am = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
            UserManagerRemote userManager = (UserManagerRemote) ic.lookup("velo/UserBean/remote");
            
            //Must re-load the user as it serialized in Task without references
            User loadedUser = userManager.reloadUser(getUser(), true);
            
            try {
                return am.loadVirtualAccountAttribute(loadedUser,tsa);
            } catch (NoUserIdentityAttributeValueException nuiave) {
                //return null;
                throw new LoadingVirtualAccountAttributeException(nuiave.getMessage());
            } catch(NoUserIdentityAttributeFoundException nuiafe) {
                //System.out.println(nuiafe.getMessage());
                //return null;
                throw new LoadingVirtualAccountAttributeException(nuiafe.getMessage());
            } catch (LoadingVirtualAccountAttributeException lvaae) {
                throw new LoadingVirtualAccountAttributeException(lvaae.getMessage());
            }
        } catch(NamingException ne) {
            ne.printStackTrace();
            return null;
        }
    }
    
    
    public ResourceAttribute getVirtualAccountIdAttribute() {
        try {
            Context ic = new InitialContext();
            ResourceAttributeManagerRemote tsam = (ResourceAttributeManagerRemote) ic.lookup("velo/ResourceAttributeBean/remote");
            try {
                ResourceAttribute accountIdAttr = tsam.factoryAccountIdResourceAttribute(getResource());
                return accountIdAttr;
                
            } catch (NoResultFoundException nrfe) {
                return null;
            }
        } catch (NamingException ne) {
            //TODO: Improve handling these errors in tools
            return null;
        }
    }
    
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
