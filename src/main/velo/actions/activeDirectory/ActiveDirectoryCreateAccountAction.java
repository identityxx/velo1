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


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;

import velo.actions.ResourceAccountActionInterface;
import velo.actions.tools.CreateAccountTools;
import velo.common.EdmMessage.EdmMessageType;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import velo.storage.Attribute;

/**
 Create account action for ActiveDirectory SSL based adapter
 
 @author Asaf Shakarchi
 */
public class ActiveDirectoryCreateAccountAction extends ActiveDirectoryResourceAction implements ResourceAccountActionInterface {
    private static final long serialVersionUID = 198730545231111412L;
    
    private User user;
    private static Logger logger = Logger.getLogger(ActiveDirectoryCreateAccountAction.class.getName());
    
    
    public boolean buildQuery() {
        return false;
    }
    
    public boolean execute() throws ActionFailureException {
        logger.fine("EXECUTE() method for creating account action for ActiveDirectory Target Type started");
        logger.fine("Creating account on Resource name: " + getResource().getDisplayName());
        
        try {
            //Performs the connectivity
            super.execute();
            
            //TODO I dont like that it's casted here, any better solution?
            CreateAccountTools cat = (CreateAccountTools)getTools();
            
            //Retrieve a SET of allowed attributes for the 'User' objectClass.
            //Set<String> allowedAttrs = getAdapter().getAttributeListOfObjectClass("user");
            Set<String> allowedAttrs = getAdapter().getEntireUserSchema();
            logger.finest("Dumping a list of allowed attributes in User Schema...:");
            logger.finest(allowedAttrs.toString());
            
            Attributes accAttrs = new BasicAttributes(true);
            boolean passwordAttrFound = false;
            Attribute passwordAttr = new Attribute();
            //Fetch all target system attributes of current target system
            logger.fine("Found -" + getResource().getResourceAttributes().size() + "- ResourceAttributes for Target System name: " + getResource().getDisplayName());
            
            //Create a variable for 'pwdLastSet'
            String pwdLastSet = new String();
            boolean isPwdLastSetFound = false;
            for (ResourceAttribute currTsa : getResource().getResourceAttributes()) {
                if (currTsa.isManaged()) {
                    try {
                        if (currTsa.isPasswordAttribute()) {
                            passwordAttr = cat.getVirtualAccountAttribute(currTsa);
                            passwordAttrFound = true;
                        } else {
                            Attribute attr = cat.getVirtualAccountAttribute(currTsa);
                            logger.fine("Loaded virtual account attribute name: " + attr.getName() + ", value: " + attr.getAsString());
                            logger.fine("Checking whether attribute named: '" + attr.getName() + "' is in the User schema or not...");
                            if (allowedAttrs.contains(attr.getName().toUpperCase())) {
                                logger.finest("Attribute named: '" + attr.getName() + "' is in schema, adding...");
                                // Add the attribute name/value to the AccountAttributes Map
                                
                                //29-01-07 -> Disabled attribute skipping if attribute was not found in schema due to SchemaAttributeListing of User ClassDefinition is not accurate enough.
                                //Maximum if the attribute was really not found in the schema, then AD will throw an exception of "NoSuchAttributeException"
                                //accAttrs.put(attr.getName().toUpperCase(),attr.getAsString());
                            } else {
                                logger.warning("Attribute named: '" + attr.getName() + "' was NOT found in schema !!");
                            }
                            
                            if (currTsa.getDataType().equals("BOOLEAN")) {
                                
                            }
                            logger.finest("Adding attribute name: '" + attr.getName().toUpperCase() + "', with value: '" + attr.getFirstValue().getAsObject() + "', of type: '" + attr.getFirstValue().getAsObject().getClass().getName() + "'");
                            
                            
                            
                            //06/mars/07 - Adding an attribute with NO VALUE is equivalent to deleting the valu efrom the attribute
                            // because the object hasn't been created yet, and consequently does not have a value for that attribute
                            //its impossible to delete a value for that attribute.
                            // Might be an LDAP protocol issue or AD implementation
                            
                            //If the attribute was flagged as required, make sure it has a value otherwise throw an exception
                            if (currTsa.isRequired()) {
                                if (attr.isEmpty()) {
                                    throw new ActionFailureException("Attribute named '" + attr.getName() + "' is required but has no value!");
                                }
                            }
                            
                            //If attribute is NOT empty, then add it.
                            if (!attr.isEmpty()) {
                                accAttrs.put(attr.getName().toUpperCase(),attr.getFirstValue().getValueAsString());
                            } else {
                                logger.fine("Attribute named: '" + attr.getName() + "' is empty, not adding attribute to user context.");
                            }
                            
                            //If the attribute is named 'pwdLastSet', then keep it for next phase
                            if (currTsa.getUniqueName().equalsIgnoreCase("pwdLastSet")) {
                                pwdLastSet = cat.getVirtualAccountAttribute(currTsa).getFirstValue().getValueAsString();
                                logger.fine("Found PwdLastSet attribute with value '" + pwdLastSet + "', keep its value to be modified after password change...");
                                isPwdLastSetFound = true;
                            }
                        }
                    } catch (NoResultFoundException nrfe) {
                        logger.warning("Skipping attribute name: " + currTsa.getUniqueName() + ", No result attribute was found, failure message: " + nrfe.getMessage());
                    } catch (LoadingVirtualAccountAttributeException lvaae) {
                        logger.warning("Skipping attribute name: " + currTsa.getUniqueName() + ", failure message: " + lvaae.getMessage());
                    }
                }
            }
            
            
            //Appearntly, the only attribute that is a MUST is the CN. make sure that there is a CN attribute set for the target we handle
            javax.naming.directory.Attribute cnAttr = accAttrs.get("CN");
            if (cnAttr == null) {
                throw new ActionFailureException("Cannot find or load the CN attribute, which is a must for target system type '" + getResource().getResourceType().getUniqueName() + "'");
            }
            
            //Set the ObjectClass of a user.
            accAttrs.put("objectClass","user");
            
            
            //Note that you need to create the user object before you can
            //set the password. Therefore as the user is created with no
            //password, user AccountControl must be set to the following
            //otherwise the Win2K3 password filter will return error 53
            //unwilling to perform.
            accAttrs.put("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED+ UF_ACCOUNTDISABLE));
            
            //Create the user on the container set in the XML conf file
            ResourceDescriptor tsd = cat.getResourceDescriptor();
            //TODO Replace the DN from XML descriptor based to somehting more dynamic, otherwise it wont be possible to move the user from one OU to another
            String accountDNToCreate = null;
//DEP            String accountDNToCreate = "CN=" + accAttrs.get("CN").get() + ","+(String)tsd.getSpecificAttributes().get("accounts-default-context-dn");
            
            //Add the 'CN' attribute
            //accAttrs.put("CN",accAttrs.get("CN"));
            
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("-START- of dumping Ldap Context account's attributes...");
                logger.finest("Account FQDN: " + accountDNToCreate);
                final List<javax.naming.directory.Attribute> ae = Collections.list((NamingEnumeration<javax.naming.directory.Attribute>) accAttrs.getAll());
                for (Iterator<javax.naming.directory.Attribute> it = ae.iterator(); it.hasNext(); ) {
                    javax.naming.directory.Attribute attr = it.next();
                    logger.finest("Attribute: " + attr.toString());
                }
            }
            
            
            logger.fine("Creating account with full DN: " + accountDNToCreate);
            
            try {
                //Create the context
                //Old way: Context result = getAdapter().getLcx().createSubcontext(accountDNToCreate, accAttrs);
                getAdapter().getLdapManager().create(accountDNToCreate, accAttrs);
            } catch (NoSuchAttributeException nsae) {
                String errMsg = "Could not create Account: '" + cat.getAccountId() + "', on Target: '" + getResource().getDisplayName() + "', set attribute(s) that are not available in User Schema, detailed failure message: " + nsae.getMessage();
                getMsgs().add(EdmMessageType.CRITICAL, "No Such Attribute Exception", errMsg);
                throw new ActionFailureException(errMsg);
            } catch (NameAlreadyBoundException nabe) {
                String errMsg  = "Could not create account named: '" + cat.getAccountId() + "', on Target: '" + getResource().getDisplayName() + "', account with the same name already exist!, detailed failure message: " + nabe.getMessage();
                logger.warning(errMsg);
                getMsgs().add(EdmMessageType.CRITICAL,"Name Already Bound Exception", errMsg);
                throw new ActionFailureException(errMsg);
            } catch (InvalidAttributeValueException iave) {
                String errMsg  = "Could not create account named: '" + cat.getAccountId() + "', on Target: '" + getResource().getDisplayName() + "', An Invalid attribute value has occured, detailed failure message is: " + iave.getMessage();
                logger.warning(errMsg);
                getMsgs().add(EdmMessageType.CRITICAL,"Invalid Attribute Value Exception", errMsg);
                throw new ActionFailureException(errMsg);
            } catch (Exception e) {
                String errMsg  = "Could not create account named: '" + cat.getAccountId() + "', on Target: '" + getResource().getDisplayName() + "', detailed failure message is: " + e.getMessage();
                logger.warning(errMsg);
                getMsgs().add(EdmMessageType.CRITICAL,"Exception", errMsg);
                throw new ActionFailureException(errMsg);
            }
            
            
            String pass;
            if (!passwordAttrFound) {
                //TODO WTF? replace this static attribute with better mechamism
                pass = "zozoZoiyA!o";
            } else {
                pass = passwordAttr.getAsString();
            }
            
            
            //Init the size of the array
            int arrayModsSize;
            if (isPwdLastSetFound) {
                arrayModsSize = 3;
            } else {
                arrayModsSize = 2;
            }
            
            
            try {
                //Modify the user password
                ModificationItem[] mods = new ModificationItem[arrayModsSize];
                //String newQuotedPassword = "\"Password2000\"";
                String newQuotedPassword = "\"" + pass + "\"";
                byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
                
                //If pwdLastSet was set, then it should be modified here, after the password modification (since password modification changes its value!)
                if (isPwdLastSetFound) {
                    mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("pwdLastSet", pwdLastSet));
                }
                
                //Perform the update
                getAdapter().replaceAttributes(accountDNToCreate, mods);
            } catch (UnsupportedEncodingException uee) {
                String errMsg  = "Could not modify password of created Account: '" + cat.getAccountId() + "', on Target: '" + getResource().getDisplayName() + "', failure message: " + uee.getMessage();
                logger.warning(errMsg);
                getMsgs().add(EdmMessageType.CRITICAL,"UnsupportedEncodingException", errMsg);
            }
            
            
            logger.info("Successfully modified password of created Account: '" + cat.getAccountId() +"', on Target: '" + getResource().getDisplayName() + "'");
            
            
            
            return true;
            
        /*} catch(NamingException ne) {
            ne.printStackTrace();
            return false;*/
        } catch (AdapterException ae) {
            throw new ActionFailureException(ae.getMessage());
        } catch (ResourceDescriptorException tsde) {
            throw new ActionFailureException(tsde.getMessage());
        }
        //catch(AdapterCommandExecutionException acee) {
        //throw new ActionFailureException("FAILED to execute the action itself, due to: " + acee.getMessage());
        //}
        /*
        catch (AdapterException ae) {
                ae.printStackTrace();
                return false;
        }
         */
    }
    
    
    public Collection getActionResult() {
        return new ArrayList();
    }
    
    /**
     @param user The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     @return Returns the user.
     */
    public User getUser() {
        return user;
    }
}
