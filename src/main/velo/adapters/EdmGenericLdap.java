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
package velo.adapters;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.entity.ResourceAdmin;
import velo.exceptions.AdapterException;
import velo.exceptions.DecryptionException;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.LdapConstants;

public class EdmGenericLdap extends ResourceAdapter {
    private static final long serialVersionUID = 1987305452306161213L;
    
    private EdmLdapManager ldapManager = new EdmLdapManager();
    private static Logger log = Logger.getLogger(EdmGenericLdap.class.getName());
    
    //TODO: USE getLdapConfig!
    public boolean connect() throws AdapterException {
        String host = (String)getResourceDescriptor().getString("specific.host");
        String port = (String)getResourceDescriptor().getString("specific.port");
        String protocol = (String)getResourceDescriptor().getString("specific.protocol");
        String baseDn = (String)getResourceDescriptor().getString("specific.base-dn");
        
        if (log.isDebugEnabled()) {
        	log.debug("Connecting to host: " + host);
        	log.debug("Connecting with host: " + port);
        	log.debug("Connecting to protocol: " + protocol);
        	log.debug("Base DN: " + baseDn);
        }
        
        LdapConfig lc = new LdapConfig();
        lc.setHost(host);
        //Critical, without this 'search' method throws a NullPointerException!
        lc.setBase(baseDn);
        lc.setAuthtype(LdapConstants.SIMPLE_AUTHTYPE);
        lc.setPort(port);
        
        
        if (protocol.toLowerCase().equals("ssl")) {
            lc.setSslSocketFactory(BlindSSLSocketFactory.class.getName());
            lc.useSsl(true);
        }
        
        //Make sure we got admins!
        if (getResource().getResourceAdmins().size() < 1) {
            throw new AdapterException("Cannot connect to Target name: '"+getResource().getDisplayName() + "', couldnt find any administrators for this target.");
        }
        
        
        //Before loop, set the LdapConfig object into the LdapManager
        getLdapManager().setLdapConfig(lc);
        
        //Iterate over the Admins, per admin try to connect, if failed then continue to the next one
        for (ResourceAdmin currTsAdmin : getResource().getResourceAdmins()) {
            log.debug("Trying to connect with Admin name: '" + currTsAdmin.getUserName() + "', with password *******");
            lc.setServiceUser(currTsAdmin.getUserName());
            try {
                lc.setServiceCredential(currTsAdmin.getDecryptedPassword());
            } catch (DecryptionException ex) {
                throw new AdapterException("Could not connect to target due to: " + ex);
            }
            
            //	Create the initial directory context
            try {
                if (getLdapManager().connect()) {
                    setConnected(true);
                    return true;
                } else {
                    continue;
                }
            } catch(NamingException ne) {
                String exceptionMsg = ne.getMessage();
                String errMsg;
                if (exceptionMsg.contains("error code 49")) {
                    errMsg = "Authentication failure was occured while trying to authenticate with user: " + currTsAdmin.getUserName();
                    continue;
                } else {
                    if (ne.getRootCause() != null) {
                        errMsg = "Exception occured while trying to connect to target system, failed with message: " + ne.getRootCause().getMessage();
                    } else {
                        errMsg = "Exception occured while trying to connect to target system, failed with message: " + ne.getMessage();
                    }
                }
                log.warn(errMsg);
                throw new AdapterException(errMsg);
            }
        }
        
        //Means that we'r out of admins to connect, throw an exception
        throw new AdapterException("Could not connect to Target name: '"+getResource().getDisplayName() + "', could not authenticate any of the specified credentials!");
    }
    
    
    public boolean disconnect() {
        getLdapManager().close();
        setConnected(false);
        return true;
    }
    
    
    /**
     * @param ldapManager the ldapManager to set
     */
    public void setLdapManager(EdmLdapManager ldapManager) {
        this.ldapManager = ldapManager;
    }
    
    /**
     * @return the ldapManager
     */
    public EdmLdapManager getLdapManager() {
        return ldapManager;
    }
    
}
