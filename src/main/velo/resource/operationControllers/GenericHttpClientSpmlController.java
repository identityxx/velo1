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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;

import velo.action.ResourceOperation;
import velo.adapters.GenericHttpClientAdapter;
import velo.contexts.OperationContext;
import velo.entity.ResourceTask;
import velo.entity.ResourceTypeOperation;
import velo.entity.SpmlTask;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.AdapterException;
import velo.exceptions.OperationException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.general.HttpMethods;
import velo.resource.general.Response;
import velo.resource.resourceDescriptor.ResourceDescriptor;

public class GenericHttpClientSpmlController extends SpmlResourceOperationController {
	private static Logger log = Logger.getLogger(GenericHttpClientSpmlController.class.getName());
	//TODO: Move to somewhere better, in one place with all others located at 'ResourceOperationsBean'.
	private final String ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME = "ADD_GROUP_MEMBERSHIP";
	private final String DELETE_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME = "DELETE_GROUP_MEMBERSHIP";
	
	private GenericHttpClientAdapter adapter;
	
	
	public GenericHttpClientSpmlController() {
		
	}
	
	public void init(OperationContext context) {
		//needed by all methods
		HttpMethods<HttpMethod> httpMethods = new HttpMethods<HttpMethod>();
		context.addVar("httpMethods", httpMethods);
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, SuspendRequest request) throws OperationException {
		log.debug("Performing Suspend Request operation has started");

		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		OperationContext c = ro.getContext();
		HttpMethods<HttpMethod> httpMethods = new HttpMethods<HttpMethod>();
		c.addVar("httpMethods", httpMethods);

		log.trace("Determining whether there are httpMethods List in context or not...");

		if (httpMethods.size() > 0) {
			log.trace("HttpMethods found, executing http methods with amount '" + httpMethods.size() + "'");
			try {
				invokeHttpMethods(httpMethods);
			} catch (AdapterException e) {
				throw new OperationException(e.toString());
			}
		} else {
			log.info("Nothing to do for Suspend request for account '" + accountName + "' on resource '" + resourceName + "', no http methods were set in context variable [httpMethods]");
		}

		log.debug("Finished suspend request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ResumeRequest request) throws OperationException {
		log.debug("Performing Resume Request operation has started");


		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		OperationContext c = ro.getContext();
		HttpMethods<HttpMethod> httpMethods = new HttpMethods<HttpMethod>();
		c.addVar("httpMethods", httpMethods);
		
		log.trace("Determining whether there are httpMethods in context or not...");
		
		if (httpMethods.size() > 0) {
			log.trace("HttpMethods found, executing http methods with amount '" + httpMethods.size() + "'");
			try {
				invokeHttpMethods(httpMethods);
			} catch (AdapterException e) {
				throw new OperationException(e.toString());
			}
		} else {
			log.info("Nothing to do for Suspend request for account '" + accountName + "' on resource '" + resourceName + "', no http methods were set in context variable [httpMethods]");
		}
		
		log.debug("Finished Resume request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, DeleteRequest request) throws OperationException {
		log.debug("Performing Delete Request operation has started");


		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		OperationContext c = ro.getContext();
		//TODO: Should be copied to all operations
		HttpMethods<HttpMethod> httpMethods = (HttpMethods<HttpMethod>)c.get("httpMethods");

		
		
		log.trace("Determining whether there are httpMethods[] in context or not...");
		if (httpMethods.size() > 0) {
			log.trace("HttpMethods found, executing http methods with amount '" + httpMethods.size() + "'");
			try {
				invokeHttpMethods(httpMethods);
			} catch (AdapterException e) {
				throw new OperationException(e.toString());
			}
		} else {
			log.info("Nothing to do for Delete request for account '" + accountName + "' on resource '" + resourceName + "', no http methods were set in context variable [httpMethods]");
		}


		log.debug("Finished Delete request operation invocation");
	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		log.debug("Performing Add Request operation has started");

		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		OperationContext c = ro.getContext();
		HttpMethods<HttpMethod> httpMethods = new HttpMethods<HttpMethod>();
		c.addVar("httpMethods", httpMethods);
		List<Response> responses = new ArrayList<Response>();
		
		log.trace("Determining whether there are httpMethods[] in context or not...");
		if (httpMethods.size() >0 ) {
			log.trace("HttpMethods found, executing http methods with amount '" + httpMethods.size() + "'");
			try {
				responses = invokeHttpMethods(httpMethods);
			} catch (AdapterException e) {
				throw new OperationException(e.toString());
			}
		} else {
			log.info("Nothing to do for Add Request for account '" + accountName + "' on resource '" + resourceName + "', no http methods were set in context variable [httpMethods]");
		}
		
		
		log.debug("Successfully created account phase, moving to group membership performing phase...");
		
		//GROUP MEMBERSHIP ASSIGNMENT
		httpMethods.clear();
		List<String> groups = getGroupsToAssign(request, ro.getResource().getUniqueName());
		log.debug("Account should be a memmber of groups amount '" + groups.size() + "', constructing group membership http methods...");
		
		ResourceTypeOperation rto = ro.getResource().getResourceType().findResourceTypeOperation(ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
		//iterate over groups, per group fetch the query and add it to the list
		ResourceOperation groupMembershipResourceOperation = ResourceOperation.Factory(ro.getResource(), c, rto);
		
		//TODO: Only requests prepared by Velo are currently supported, so expecting the request to have a PSOID already set
		groupMembershipResourceOperation.getContext().set("accountName", request.getPsoID().getID());
		
		log.debug("Getting http methods per group membership...");
		//Iterate over all groups, per group, perform the whole life cycle of the group membership operation
		for (String currGroupUniqueId : groups) {
			//override the groupID within the context
			groupMembershipResourceOperation.getContext().set("groupUniqueId", currGroupUniqueId);
			
			//PRE
			boolean invocationStatus = groupMembershipResourceOperation.preActionOperation();
			if (!invocationStatus) {
				log.error("PreExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
			}
		
			//invocation will add a new http method within the PRE action, pass by reference, no need to do anything
			log.trace("Checking whether there are any HTTP methods available in context for 'Add Group Membership!' of group named '" + currGroupUniqueId + "'.");
			//log.trace("Current amount of http methods in context array: '" +  globalGroupMembershipQueries.queryAmount() + "'");

			//Perform POST operation actions
			invocationStatus = groupMembershipResourceOperation.postActionOperation();
			if (!invocationStatus) {
				log.error("PostExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
			}
		}
		
		//Invoking queries
		if (log.isTraceEnabled()) {
			log.trace("Recieved http methods from group membership phase with amount '" + httpMethods.size() + "'");
		}
		log.debug("Invoking the 'Add Group Membership' http methods now!");
		try {
			responses.addAll(invokeHttpMethods(httpMethods));
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
		

		//Add the responses to the context as they should be accessible via the post phase
		c.addVar("responses", responses);
		

		log.debug("Finished Add request operation invocation");

	}


	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		log.debug("Performing Modify Request operation has started");
		
		String accountName = request.getPsoID().getID();
		log.info("Performing modify request for account '" + accountName + "', on resource '" + ro.getResource().getDisplayName() + "'");
		
		
		Map<String,List<String>> groupsToManage = getGroupMembershipToModify(request,ro.getResource().getUniqueName());
		
		if (groupsToManage == null) {
			throw new OperationException("Could not load groups to assign/revoke!");
		}
    
		List<String> groupsToAssign = groupsToManage.get("groupsToAssign");
		List<String> groupsRevoke = groupsToManage.get("groupsToRevoke");
		
		log.debug("Found groups to ASSIGN with amount: '" + groupsToAssign.size() + "'");
		log.debug("Found groups to REMOVE with amount: '" + groupsRevoke.size() + "'");
		
		
		//handle groups to assign
		OperationContext c = ro.getContext();
		HttpMethods<HttpMethod> httpMethods = new HttpMethods<HttpMethod>();
		c.addVar("httpMethods", httpMethods);
		List<Response> responses = new ArrayList<Response>();
		
		
		ResourceTypeOperation addGrpTypeOper = ro.getResource().getResourceType().findResourceTypeOperation(ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
		ResourceTypeOperation removeGrpTypeOper = ro.getResource().getResourceType().findResourceTypeOperation(DELETE_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
		ResourceOperation addGrpOper = ResourceOperation.Factory(ro.getResource(), c, addGrpTypeOper);
		ResourceOperation removeGrpOper = ResourceOperation.Factory(ro.getResource(), c, removeGrpTypeOper);
		
		//log.trace("Factored AddGroupMemberShipOperation in order to perform 'account modify'")
		
		
		
		
		//handle groups to add
		for (String currGroupUniqueId : groupsToAssign) {
			log.trace("Checking whether there are any HTTP methods available in context for 'Add Group Membership!' of group named '" + currGroupUniqueId + "'.");
			addGrpOper.getContext().set("groupUniqueId", currGroupUniqueId);
			
			//PRE
			boolean invocationStatus = addGrpOper.preActionOperation();
			
			if (!invocationStatus) {
				log.error("PreExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PreAction invocation has failed: " + addGrpOper.getErrorMessage());
			}
			
			//invocation will add a new http method within the PRE action, pass by reference, no need to do anything
			//log.trace("Current amount of http methods in context array: '" +  globalGroupMembershipQueries.queryAmount() + "'");

			//Perform POST operation actions
			invocationStatus = addGrpOper.postActionOperation();
			if (!invocationStatus) {
				log.error("PostExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PostAction invocation has failed: " + addGrpOper.getErrorMessage());
			}
		}
		
		//handle groups to remove
		for (String currGroupUniqueId : groupsRevoke) {
			log.trace("Checking whether there are any HTTP methods available in context for 'Remove Group Membership!' of group named '" + currGroupUniqueId + "'.");
			
			removeGrpOper.getContext().set("groupUniqueId", currGroupUniqueId);
			
			//PRE
			boolean invocationStatus = removeGrpOper.preActionOperation();
			
			if (!invocationStatus) {
				log.error("PreExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PreAction invocation has failed: " + removeGrpOper.getErrorMessage());
			}
			
			//invocation will add a new http method within the PRE action, pass by reference, no need to do anything
			//log.trace("Current amount of http methods in context array: '" +  globalGroupMembershipQueries.queryAmount() + "'");

			//Perform POST operation actions
			invocationStatus = removeGrpOper.postActionOperation();
			if (!invocationStatus) {
				log.error("PostExecution invocation failed, indication the whole process as a failure...");
				throw new OperationException("PostAction invocation has failed: " + removeGrpOper.getErrorMessage());
			}
		}
		
		
		
		//Invoking queries
		if (log.isTraceEnabled()) {
			log.trace("Recieved http methods from group membership phase with amount '" + httpMethods.size() + "'");
		}
		log.debug("Invoking the 'Add/Remove Group Membership' http methods now!");
		try {
			responses.addAll(invokeHttpMethods(httpMethods));
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
		

		//Add the responses to the context as they should be accessible via the post phase
		c.addVar("responses", responses);
		

		log.debug("Finished Modify request operation invocation");
	}
	

	
	
	public void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("This operation is currently not supported for this resource type!");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private List<Response> invokeHttpMethods(HttpMethods<HttpMethod> hmList) throws AdapterException {
		List<Response> responses = new ArrayList<Response>();
		for (HttpMethod currMethod : hmList) {
			Response r = new Response();
			try {
				r.setDescription(currMethod.getResponseBodyAsString());
				String responseCode = String.valueOf(getAdapter().executeMethod(currMethod));
				String responseText = currMethod.getResponseBodyAsString();
				r.setCode(responseCode);
				r.setText(responseText);
				
				responses.add(r);
				log.trace("Printing response per HTTP METHOD execution: " + currMethod.getResponseBodyAsString());
			} catch (AdapterException e) {
				log.error("An error has occured while trying to invoke an HttpMethod: " + e.toString());
				throw e;
			}
			catch (IOException ie) {
				log.error("An IO Exeption has occured: " + ie.toString());
			}
		}
		
		return responses;
	}
	
	
	//TODO: Support adapters via pools	
	private GenericHttpClientAdapter getAdapter() throws AdapterException {
		if (this.adapter == null) {
			adapter = new GenericHttpClientAdapter();
			adapter.setResource(getResource());
		}
		
		if (!adapter.isConnected()) {
			adapter.connect();
		}
		return this.adapter;
	}
}