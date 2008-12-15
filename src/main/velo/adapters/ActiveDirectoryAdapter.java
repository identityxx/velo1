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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.AdapterException;
import velo.exceptions.AuthenticationFailureException;
import velo.exceptions.OperationException;
import edu.vt.middleware.ldap.Authenticator;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.LdapUtil;

public class ActiveDirectoryAdapter extends EdmGenericLdap {
	private static final long serialVersionUID = 1987305452306161213L;
	
	private static Logger log = Logger.getLogger(ActiveDirectoryAdapter.class.getName());
	
	public boolean isUserExistsByCN(String accountName, String baseDn) throws OperationException {
		try {
			if (!isConnected()) {
				connect();
			}
		
			String filter = "(&(cn=" + accountName + ")(objectClass=user))";
			Iterator a = getLdapManager().search(baseDn,filter);
		
			return a.hasNext();
		}
		catch (NamingException ne) {
			throw new OperationException("Cannot check if account exists or not due to: " + ne);
		}
		catch (AdapterException ae) {
			throw new OperationException("Cannot check if account exists or not due to: " + ae);
		}
	}
	
	public boolean isUserExistsBySamAccountName(String accountName, String baseDn) throws OperationException {
		try {
			if (!isConnected()) {
				connect();
			}
		
			String filter = "(&(sAMAccountName=" + accountName + ")(objectClass=user))";
			Iterator a = getLdapManager().search(baseDn,filter);
		
			return a.hasNext();
		}
		catch (NamingException ne) {
			throw new OperationException("Cannot check if account exists or not due to: " + ne);
		}
		catch (AdapterException ae) {
			throw new OperationException("Cannot check if account exists or not due to: " + ae);
		}
	}
	
	public SearchResult findBySamAccountName(String accountName, String baseDn) {
		try {
			if (!isConnected()) {
				connect();
			}

			String filter = "(&(sAMAccountName=" + accountName + ")(objectClass=user))";
			Iterator<SearchResult> a = getLdapManager().search(baseDn,filter);
		
			if (a.hasNext()) {
				return a.next();
			} else {
				return null;
			}
		}
		catch (NamingException e) {
			System.out.println("Could not get account by SamAccountName: " + e.toString());
			return null;
		}
		catch (AdapterException e) {
			System.out.println("Could not get account by SamAccountName: " + e.toString());
			return null;
		}
	}
	
	
	
	public SearchResult getAccountByCN(String accountName, String baseDn) throws AccountWasNotFoundOnTargetException{
		try {
			if (!isConnected()) {
				connect();
			}
		
			String filter = "(&(CN=" + accountName + ")(objectClass=user))";
		
			Iterator<SearchResult> a = getLdapManager().search(baseDn,filter);
		
			if (a.hasNext()) {
				//Attributes attrs = a.next().getAttributes();
				//return LdapUtil.parseAttributes(attrs, false);
				return a.next();
			}
			else {
				throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", account was not found!");
			}
		}
		catch (NamingException ne) {
			throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", due to: " + ne);
		}
		catch (AdapterException ae) {
			throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", due to: " + ae);
		}
	}
	
	public SearchResult getAccountBySamAccountName(String accountName, String baseDn) throws AccountWasNotFoundOnTargetException {
		try {
			if (!isConnected()) {
				connect();
			}
		
			String filter = "(&(sAMAccountName=" + accountName + ")(objectClass=user))";
		
			Iterator<SearchResult> a = getLdapManager().search(baseDn,filter);
		
			if (a.hasNext()) {
				//Attributes attrs = a.next().getAttributes();
				//return LdapUtil.parseAttributes(attrs, false);
				return a.next();
			}
			else {
				throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", account was not found!");
			}
		}
		catch (NamingException ne) {
			throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", due to: " + ne);
		}
		catch (AdapterException ae) {
			throw new AccountWasNotFoundOnTargetException("Could not find account named: '" + accountName + ", for base DN: '" + baseDn + ", on Resource: '" + getResource().getDisplayName() + ", due to: " + ae);
		}
	}
	
	
	public Map parse(Attributes attrs) throws OperationException {
		try {
			return LdapUtil.parseAttributes(attrs, false);
		}
		catch (NamingException ne) {
			throw new OperationException("Could not parse attributes to a MAP, failure message is: " + ne.getMessage());
		}
	}
	
	public void replaceAttributes(String dn, Attributes attrs) throws OperationException {
		try {
			getLdapManager().replaceAttributes(dn,attrs);
		}
		catch (NamingException ne) {
			throw new OperationException("Could not replace attributes, failure message is: " + ne.getMessage());
		}
	}
	
	public void replaceAttributes(String dn, ModificationItem[] mods) throws AdapterException {
		try {
			getLdapManager().getLdapContext().modifyAttributes(dn,mods);
		}
		catch (NamingException ne) {
			throw new AdapterException("Could not replace attributes, failure message is: " + ne.getMessage());
		}
	}
	
	
	//MANAGE SCHEMAS, SUCKS
	//TODO: Improve schema allowed attributes mechanism, static classDefinitions sucks.
	public Set<String> getEntireUserSchema() {
		Set<String> allowedAttrs = new HashSet<String>();
		
		ArrayList<String> classDefinitions = new ArrayList<String>();
		classDefinitions.add("top");
		classDefinitions.add("organizationalPerson");
		classDefinitions.add("person");
		classDefinitions.add("User");
		classDefinitions.add("securityPrincipal");
		classDefinitions.add("siteConnector");
		classDefinitions.add("msExchConnector");
		classDefinitions.add("msExchBaseClass");
		classDefinitions.add("msExchMailStorage");
		classDefinitions.add("mailRecipient");
		
		for (String currClassDefinition : classDefinitions) {
			//Might be null if one of the class definitions does not exists
			Set<String> allowedAttrsInSchema = getAttributeListOfClassName(currClassDefinition);
			
			if (allowedAttrsInSchema != null) {
				allowedAttrs.addAll(allowedAttrsInSchema);
			}
		}
		
		return allowedAttrs;
	}
	
	
	public Set<String> getAttributeListOfClassName(String className) {
		try {
			System.out.println(className);
			LdapContext lc = getLdapManager().getLdapContext();
			DirContext s = lc.getSchema("");
			
			Attributes ocAttrs = s.getAttributes("ClassDefinition/" + className);
			Set<String> attrs = new HashSet<String>();
			
			Attribute must = ocAttrs.get("MUST");
		    Attribute may = ocAttrs.get("MAY");
		    if (must != null) {
		    	addAttributeNameToList(attrs, must.getAll());
		    }
		    if (may != null) {
		    	addAttributeNameToList(attrs, may.getAll());
		    }
		    
			return attrs;
			
		}
		catch(NamingException ne) {
			//TODO NEVER RETURN NULL!
			return null;
		}
	}
	
	static void addAttributeNameToList(Set list, NamingEnumeration attrs) throws NamingException {
		while (attrs.hasMore()) {
			String val = (String)attrs.next();
			list.add(val.toUpperCase());
		 }
	}
	
	public void setBaseDn(String baseDn) {
		getLdapManager().getLdapConfig().setBase(baseDn);
	}
	
	
	public boolean simpleAuthenticate(String userName, String password) throws AuthenticationFailureException {
		try {
			if (!isConnected()) {
	    		connect();
	    	}
			
			log.debug("Finding user by samAccountName in repository...");
			SearchResult user = findBySamAccountName(userName, getLdapManager().getLdapConfig().getBase());
			
			if (user == null) {
				throw new AuthenticationFailureException("User with SamAccountName '" + userName + "' does not exist!");
			}
	    	
			String userDN = user.getName();
			log.debug("User was found (user DN: '" + userDN + "')");
			
			
			LdapConfig lc = getLdapManager().getLdapConfig();
			Authenticator auth = new Authenticator(lc);
			
			auth.setConstructDn(false);
			//AD is CN
			auth.setUserField("CN");
			//subtree search is a must as baseDN is not always the OU the user is associated with
			auth.setSubtreeSearch(true);
			//currently no TLS is supported
			auth.useTls(false);
			
			
			log.debug("Authenticating user, will seek user for base DN: '" + lc.getBase()+"'");
			
		
			if (auth.authenticateDn(userDN, password)) {
				log.debug("Successfully authenticated user...");
				return true;
			} else {
				log.debug("Failed to authenticated user...");
				return false;
			}
		}catch (NamingException e) {
			throw new AuthenticationFailureException(e.toString());
		} catch (AdapterException e) {
			throw new AuthenticationFailureException(e.toString());
		}
    }
}
