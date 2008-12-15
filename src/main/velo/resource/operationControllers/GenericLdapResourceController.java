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
package velo.resource.operationControllers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;

import velo.action.ResourceOperation;
import velo.adapters.EdmGenericLdap;
import velo.contexts.OperationContext;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.ResourceTask;
import velo.entity.SpmlTask;
import velo.exceptions.AdapterException;
import velo.exceptions.OperationException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.resourceDescriptor.ResourceDescriptor;

public class GenericLdapResourceController extends GroupMembershipSpmlResourceOpreationController {
	private static Logger log = Logger.getLogger(GenericLdapResourceController.class.getName());
	EdmGenericLdap adapter;

	public GenericLdapResourceController() {
	}

	public void init(OperationContext context) {
		try {
			context.addVar("adapter", getAdapter(true));
		}catch (AdapterException e) {
			log.error("Could not set 'adapter' variable into context" + e.getMessage());
		}
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, SuspendRequest request) throws OperationException {
		log.debug("Performing Suspend Request operation has started");


		//Currently must be handled via pre/post actions


		log.debug("Finished suspend request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ResumeRequest request) throws OperationException {
		log.debug("Performing Resume Request operation has started");


		//Currently must be handled via pre/post actions


		log.debug("Finished Resume request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, DeleteRequest request) throws OperationException {
		log.debug("Performing Delete Request operation has started");

		try {
			ResourceDescriptor rd = ro.getResource().factoryResourceDescriptor();
			//Get some required data
			String accountName = request.getPsoID().getID();
			ResourceAttribute accountIdRA = ro.getResource().getAccountIdAttribute();
			String baseDn = rd.getString("specific.base-dn");
			
			String accountsFullDn = baseDn;
			String accountsPrefDn = rd.getString("specific.account.accounts-base-dn");
			if (accountsPrefDn != null) {
				accountsFullDn = accountsPrefDn + "," +  accountsFullDn;
			}
			
		
			if (accountIdRA == null) {
				throw new OperationException("No attribute was indicated as an account id attribute!");
			}
		
			log.debug("Constructed the following DN to seek account in: '" + accountsFullDn + "', searching account in directory...");
			
			/*This shitty search type based on Attrs is not working, it always returns nothing, at least this was tested against SUN Directory
			//search a specific user
			Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
			matchAttrs.put(new BasicAttribute(accountIdRA.getUniqueName(), accountName));
			// Search for objects that have those matching attributes
			Iterator<SearchResult> sr = getAdapter(true).getLdapManager().searchAttributes(accountsFullDn, matchAttrs);
			*/
			
			String attrFilter = accountIdRA.getUniqueName() + "=" + accountName;
			Iterator<SearchResult> sr = getAdapter(true).getLdapManager().search(accountsFullDn,attrFilter);
			
			
			if (!sr.hasNext()) {
				throw new OperationException("Account named '" + accountName + "' does not exist within the specified DN (" + accountsFullDn + ")");
			}
		
			SearchResult ldapAccountResult = sr.next();
			String nameInNamespace = ldapAccountResult.getName();
			log.debug("Found account in repostiroy with name: '" + nameInNamespace + "', deleting account...");
		
			getAdapter(true).getLdapManager().delete(nameInNamespace);
			
		} catch (NamingException e) {
			throw new OperationException(e.getMessage());
		} catch (AdapterException e) {
			throw new OperationException(e.getMessage());
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.getMessage());
		}

		log.debug("Finished Delete request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		log.debug("Performing Add Request operation has started");
		
		try {
			Map<String, ResourceAttribute> attrs = (Map<String, ResourceAttribute>) ro.getContext().get("attrs");
			ResourceDescriptor rd = ro.getResource().factoryResourceDescriptor();
			
			String objectClassStr = rd.getString("specific.account.account-object-class");
			//make sure its loaded
			if (objectClassStr == null) {
				throw new OperationException("ObjectClass(es) was not specified!");
			}
			String[] objectClasses = null;
			
			
			
			//if objectClass has ';' char in it then it means we have to handle a list here
			if (objectClassStr.contains(";")) {
				objectClasses = objectClassStr.split(";");
			} else {
				objectClasses = new String[0];
				objectClasses[0] = objectClassStr;
			}
			
			String baseDN = rd.getString("specific.base-dn");
			String accountDefaultCreationDn = rd.getString("specific.account.account-default-creation-dn");
			if (accountDefaultCreationDn != null) {
				baseDN = accountDefaultCreationDn + "," + baseDN;
			}
		
			log.trace("Dumping attrs map from context: " + attrs);
			//build the user DN
			String userDN;
			ResourceAttribute accountIdRAOrg = ro.getResource().getAccountIdAttribute();
			ResourceAttribute accountIdRA = attrs.get(accountIdRAOrg.getUniqueName());
			
			if (accountIdRA == null) {
				throw new OperationException("No attribute was indicated as an account id attribute!");
			} else if (!accountIdRA.isFirstValueIsNotNull()) {
				throw new OperationException("An attribute was indicated as an account id was found but has no values!");
			}
		
			ResourceAttribute userDNRA = attrs.get("DN");
			if (userDNRA.isFirstValueIsNotNull()) {
				userDN = userDNRA.getFirstValue().getAsString();
				//remove the special DN attribute as it's not really exist.
				attrs.remove("DN");
			} else {
				userDN = accountIdRA.getUniqueName() + "=" + accountIdRA.getFirstValue().getAsString() + "," + baseDN;
			}
		
			Attributes ldapAttrs = getLdapAttributes(attrs);
			
			for (int i=0;i<objectClasses.length;i++) {
				ldapAttrs.put("objectClass",objectClasses[i]);
			}
			
			
			//create the user
			log.debug("Dumping attributes before aaccount creation: " + ldapAttrs.toString());
			log.info("Creating account in directory, name in directory would be: " + userDN);
			getAdapter(true).getLdapManager().create(userDN,ldapAttrs);
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.getMessage());
		} catch (NamingException e) {
			throw new OperationException(e.getMessage());
		} catch (AdapterException e) {
			throw new OperationException(e.getMessage());
		}
		

		log.debug("Finished Add request operation invocation");

	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		log.debug("Performing Modify Request operation has started");


		//TODO: Implement!


		log.debug("Finished Modify request operation invocation");

	}	
	
	
	public void performOperationAddGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToAdd) throws OperationException {
		log.debug("Getting a query per group membership to assign...");
	}
	
	public void performOperationRemoveGroupMembership(SpmlTask spmlTask, ResourceOperation ro, Request request, List<ResourceGroup> groupsToRemove) throws OperationException {
		log.debug("Getting a query per group membership to remove...");
	}
	
	public void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("This operation is currently not supported for this resource type!");
	}
	
	
	

	
	
	private Attributes getLdapAttributes(Map<String, ResourceAttribute> attrs) {
		// LDAP Attributes container
		Attributes accAttrs = new BasicAttributes(true);

		for (ResourceAttribute currRA : attrs.values()) {
			if (currRA.isFirstValueIsNotNull()) {
				accAttrs.put(currRA.getUniqueName(), currRA.getFirstValue().getValueAsObject());
			} else {
				log.info("Skipping adding resource attribute '" + currRA.getDisplayName() + "' to account attribute ldap context since it has no values...");
			}
		}

		return accAttrs;
	}
	
	// TODO: Support adapters via pools
	private EdmGenericLdap getAdapter(boolean connect) throws AdapterException {
		if (this.adapter == null) {
			adapter = new EdmGenericLdap();
			adapter.setResource(getResource());
		}

		if (connect) {
			if (!adapter.isConnected()) {
				adapter.connect();
			}
		}
		
		return this.adapter;
	}
}