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
package velo.ejb.impl;


import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.velo_wingw.VeloDataContainer;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.ws.WSException;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.CapabilityData;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ExecutionMode;
import org.openspml.v2.msg.spml.Modification;
import org.openspml.v2.msg.spml.ModificationMode;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spmlref.Reference;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;
import org.openspml.v2.profiles.DSMLProfileRegistrar;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.util.Spml2Exception;
import org.openspml.v2.util.xml.ObjectFactory;
import org.openspml.v2.util.xml.ReflectiveXMLMarshaller;
import org.openspml.v2.util.xml.UnknownSpml2TypeException;
import org.tempuri.IWindowsGatewayApi;
import org.tempuri.WindowsGatewayApi;

import sun.misc.BASE64Decoder;
import velo.action.ResourceOperation;
import velo.adapters.Adapter;
import velo.common.UniqueIdGenerator;
import velo.contexts.OperationContext;
import velo.contexts.OperationContextVar.PhaseRelevance;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerRemote;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.EventDefinition;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGlobalOperation;
import velo.entity.ResourceGroup;
import velo.entity.ResourceReconcileSummaryEntity;
import velo.entity.ResourceTask;
import velo.entity.ResourceTypeOperation;
import velo.entity.Role;
import velo.entity.RoleResourceAttribute;
import velo.entity.SpmlTask;
import velo.entity.Task;
import velo.entity.User;
import velo.entity.ResourceType.ResourceControllerType;
import velo.exceptions.AdapterException;
import velo.exceptions.AuthenticationFailureException;
import velo.exceptions.FactoryException;
import velo.exceptions.FactoryNativeResourceControllerException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.OperationException;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ReconcileGroupsException;
import velo.exceptions.ResourceDescriptorException;
import velo.exceptions.ScriptInvocationException;
import velo.exceptions.UserAuthenticationException;
import velo.processSummary.resourceReconcileSummary.ResourceReconcileSummary;
import velo.query.Query;
import velo.reconcilidation.ReconcileAccounts;
import velo.reconcilidation.ReconcileGroups;
import velo.resource.operationControllers.GroupMembershipSpmlResourceOpreationController;
import velo.resource.operationControllers.ResourceOperationController;
import velo.resource.operationControllers.SpmlResourceOperationController;
import velo.spml2.SpmlAddRequest;
import velo.spml2.SpmlUtils;
import velo.utils.Stopwatch;
import velo.windowsGateway.VeloDataContainerProxy;

//@Name("resourceOperationsBean")
@Stateless()
@Name("resourceOperationsManager")
@AutoCreate
public class ResourceOperationsBean implements ResourceOperationsManagerLocal,ResourceOperationsManagerRemote {
	private static Logger log = Logger.getLogger(ResourceOperationsBean.class.getName());
	
	// Constants
	private static final String resourceReconcilationEvent = "RESOURCE_RECONCILIATION";
	
	
	private final String ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME = "ADD_GROUP_MEMBERSHIP";
	private final String DELETE_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME = "DELETE_GROUP_MEMBERSHIP";

	private final String SPML_USER_IN_REPOSITORY_REFERENCE_TYPE = "VELO_REPOSITORY"; 
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;
	
	@EJB
	TaskManagerLocal taskManager;
	
	@EJB
	ResourceManagerLocal resourceManager;
	
	@EJB
	ResourceGroupManagerLocal resourceGroupManager;
	
	@EJB
	AccountManagerLocal accountManager;
	
	@EJB
	EventManagerLocal eventManager;
	

	
	//damn, nothing works but this
	//@In
    //private JbpmContext jbpmContext;
	//private JbpmContext jbpmContext;
	
	//@In
	//private Jbpm jbpm;
	
	//Invoke any supported resource actions where PSO is always an account
	public void addAccountAction(Resource resource, SpmlAddRequest spmlRequest) throws OperationException {
		//Make sure the action is supported by the specified resource
		
		//Load the corresponding resource action
		
		//Send the action and the request to the controller
		
		//return a serialized action object
		
		//persist object
	}
	
	
	//create spml requests
	public SpmlTask createSuspendAccountRequestTask(Account account) throws OperationException {
		String operationUniqueName = "SUSPEND_ACCOUNT";
		
		ResourceTypeOperation rto = account.getResource().getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new OperationException("This operation is not supported by resouce '" + account.getResource().getDisplayName() + "'");
		}
		
		SuspendRequest req = new SuspendRequest(generateUniqueId(), ExecutionMode.ASYNCHRONOUS, new PSOIdentifier(account.getName(),null,account.getResource().getUniqueName()));
		SpmlTask task = SpmlTask.factory(account.getResource().getUniqueName(), "Disable account name " + account.getName() + " on resource '" + account.getResource().getUniqueName() + "'");
		task.setResourceTypeOperation(rto);
		
		try {
			String xml = getMarshaller().marshall(req);
			log.trace("Marshalled suspend request on pso ID '" + account.getName() + "', on resource '" + account.getResource().getUniqueName());
			log.trace("Marshalled SPML code: " + xml);
			task.setBody(xml);

			
			//perform any action rule in creation phase
			try {
				performTaskCreationPhaseActions(account.getResource(),task, rto);
			}catch(OperationException e) {
				throw new OperationException("Failed to create task due to action(s) execution exception: " + e.toString());
			}
			
			return task;
		}
		catch (Spml2Exception e) {
			log.warn(e);
			throw new OperationException(e);
		}
	}
	
	public void createSuspendAccountRequest(Account account) throws OperationException {
		SpmlTask task = createSuspendAccountRequestTask(account);
		taskManager.persistTask(task);
	}
	
	
	
	public SpmlTask createResumeAccountRequestTask(Account account) throws OperationException {
		String operationUniqueName = "RESUME_ACCOUNT";
		
		ResourceTypeOperation rto = account.getResource().getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new OperationException("This operation is not supported by resouce '" + account.getResource().getDisplayName() + "'");
		}
		
		ResumeRequest req = new ResumeRequest(generateUniqueId(), ExecutionMode.ASYNCHRONOUS, new PSOIdentifier(account.getName(),null,account.getResource().getUniqueName()));
		SpmlTask task = SpmlTask.factory(account.getResource().getUniqueName(), "Enable account name " + account.getName() + " on resource '" + account.getResource().getUniqueName() + "'");
		task.setResourceTypeOperation(rto);
		
		try {
			String xml = getMarshaller().marshall(req);
			log.trace("Marshalled resume request on pso ID '" + account.getName() + "', on resource '" + account.getResource().getUniqueName());
			log.trace("Marshalled SPML code: " + xml);
			task.setBody(xml);
			
			
			//perform any action rule in creation phase
			try {
				performTaskCreationPhaseActions(account.getResource(),task, rto);
			}catch(OperationException e) {
				throw new OperationException("Failed to create task due to action(s) execution exception: " + e.toString());
			}
			
			
			return task;
		}
		catch (Spml2Exception e) {
			log.warn(e);
			throw new OperationException(e);
		}
	}
	
	public void createResumeAccountRequest(Account account) throws OperationException {
		SpmlTask task = createResumeAccountRequestTask(account);
		taskManager.persistTask(task);
	}
	
	
	public SpmlTask createDeleteAccountRequestTask(Account account, Set<Role> excludedProtectedRoleList, boolean forceDelete) throws OperationException {
		String operationUniqueName = "DELETE_ACCOUNT";
		
		ResourceTypeOperation rto = account.getResource().getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new OperationException("This operation is not supported by resource '" + account.getResource().getDisplayName() + "'");
		}
		
		
		/*Already handeled by the ModifyRole method
		if ( (excludedProtectedRoleList != null) ) {
			//Before performing any delete account steps, make sure that there is NO role that protects the account
			//except the excluded list
			if (account.isAnyUserRoleProtectsAccount(excludedProtectedRoleList)) {
				throw new OperationException("Could not delete account '" + account.getName() + "', on resource '" + account.getResource().getDisplayName() + "' since it is protected by associated roles.");
			}
		}
		*/
		
		
		//recursive is set as last parameter in constructor???
		DeleteRequest req = new DeleteRequest(generateUniqueId(), ExecutionMode.ASYNCHRONOUS, new PSOIdentifier(account.getName(),null,account.getResource().getUniqueName()),true);
		SpmlTask task = SpmlTask.factory(account.getResource().getUniqueName(), "Delete account name " + account.getName() + " on resource '" + account.getResource().getUniqueName() + "'");
		task.setResourceTypeOperation(rto);
		
		try {
			String xml = getMarshaller().marshall(req);
			log.trace("Marshalled delete request on pso ID '" + account.getName() + "', on resource '" + account.getResource().getUniqueName());
			log.trace("Marshalled SPML code: " + xml);
			task.setBody(xml);
			
			
			//perform any action rule in creation phase
			try {
				performTaskCreationPhaseActions(account.getResource(),task, rto);
			}catch(OperationException e) {
				throw new OperationException("Failed to create task due to action(s) execution exception: " + e.toString());
			}
			
			
			//taskManager.persistTask(task);
			return task;
		}
		catch (Spml2Exception e) {
			log.warn(e);
			throw new OperationException(e);
		}
	}
	
	public void createDeleteAccountRequest(Account account, Set<Role> excludedProtectedRoleList, boolean forceDelete) throws OperationException {
		SpmlTask task = createDeleteAccountRequestTask(account, excludedProtectedRoleList, forceDelete);
		taskManager.persistTask(task);
	}
	
	
	public void createAddAccountRequestForUser(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Account virtualAccount) throws OperationException {
		SpmlTask task = createAddAccountRequestForUserTask(resource, user, memberOfGroups, virtualAccount);
		taskManager.persistTask(task);
	}
	
	public void createAddAccountRequestForUser(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Set<Role> roles) throws OperationException {
		SpmlTask task = createAddAccountRequestForUserTask(resource, user, memberOfGroups, roles);
		taskManager.persistTask(task);
	}
	
	public SpmlTask createAddAccountRequestForUserTask(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Account virtualAccount) throws OperationException {
		String operationUniqueName = "ADD_ACCOUNT";
		
		try {
			//Account account = buildVirtualAccount(resource,user);
			Map<String,Attribute> accAttrs = virtualAccount.getTransientAttributes();
			
			ResourceTypeOperation rto = virtualAccount.getResource().getResourceType().findResourceTypeOperation(operationUniqueName);
			if (rto == null) {
				throw new OperationException("This operation is not supported by resource '" + virtualAccount.getResource().getDisplayName() + "'");
			}
			
			//This is trustable as 'buildVirtualAccount' validate for its existance.
			ResourceAttribute ra = virtualAccount.getResource().getAccountIdAttribute();
			
			//Map<String,Attribute> attrsMap = account.getTransientAttributes(); 
			
			
			
			
			
			
			//expecting(validated above) the virtual attributes map to contain at least one value
			String accName = accAttrs.get(ra.getUniqueName()).getFirstValue().getAsString();

			AddRequest addRequest = new AddRequest();
			addRequest.setExecutionMode(ExecutionMode.ASYNCHRONOUS);
			addRequest.setTargetId(virtualAccount.getResource().getUniqueName());
			addRequest.setData(virtualAccount.getVirtualAttributesAsSPMLExtensible(accAttrs));
			//set the PSO, as we already know it(used by the perform method of this request type)
			addRequest.setPsoID(new PSOIdentifier(accName,null,virtualAccount.getResource().getUniqueName()));
			//addRequest.setPsoID(new PSOIdentifier(accName,null,account.getResource().getUniqueName()));
			SpmlTask task = SpmlTask.factory(virtualAccount.getResource().getUniqueName(), "Add account name " + accName + " on resource '" + virtualAccount.getResource().getUniqueName() + "'");
			//where is that constructor came from? :)
			//recursive is set as last parameter in constructor???
			//AddRequest req = new AddRequest(generateUniqueId(), ExecutionMode.ASYNCHRONOUS, new PSOIdentifier(account.getName(),null,account.getResource().getUniqueName()),null,extensible);
			task.setResourceTypeOperation(rto);
			
			
			//handle groups assignment
			CapabilityData cd = new CapabilityData();
			cd.setCapabilityURI(new URI("urn:oasis:names:tc:SPML:2.0:reference"));
			if (memberOfGroups != null) {
				for (ResourceGroup currRG : memberOfGroups) { 
					Reference ref = new Reference();
					ref.setTypeOfReference("memberOf");
					ref.setToPsoID(new PSOIdentifier(currRG.getUniqueId(),null,currRG.getResource().getUniqueName()));
				
					cd.addReference(ref);
				}
			}
			
			
			//handle internal user repository assignment
			if (user != null) {
				Reference ref = new Reference();
				ref.setTypeOfReference(SPML_USER_IN_REPOSITORY_REFERENCE_TYPE);
				ref.setToPsoID(new PSOIdentifier(user.getName(),null,SPML_USER_IN_REPOSITORY_REFERENCE_TYPE));
				cd.addReference(ref);
			}
			
			addRequest.addCapabilityData(cd);
			String xml = getMarshaller().marshall(addRequest);
			log.trace("Marshalled add request on pso ID '" + virtualAccount.getName() + "', on resource '" + virtualAccount.getResource().getUniqueName());
			log.trace("Marshalled SPML code: " + xml);
			task.setBody(xml);
			
			
			
			//perform any action rule in creation phase
			try {
				performTaskCreationPhaseActions(resource,task, rto);
			}catch(OperationException e) {
				throw new OperationException("Failed to create task due to action(s) execution exception: " + e.toString());
			}
			
			
			return task;
			
		}
		catch (DSMLProfileException e) {
			throw new OperationException (e.toString());
		}
		catch (Spml2Exception e) {
			throw new OperationException (e.toString());
		} catch (URISyntaxException e) {
			throw new OperationException (e.toString());
		} /*catch (ObjectsConstructionException e) {
			throw new OperationException (e.toString());
		}*/
	}
	
	
	//rolesToRemove can be null
	private Account buildVirtualAccount(Resource resource, User user, Set<Role> rolesToAdd, List<Role> rolesToRemove) throws ObjectsConstructionException {
		Account account = new Account();
		account.setResource(resource);
		account.setUser(user);
		
		
		/*WTF IS THAT?!
		//before proceeding, make sure that the account is not protected by any roles assigned to its parent user
		if (account.isAnyUserRoleProtectsAccount(null)) {
			throw new OperationException("Cannot add account '" + account.getName() + "' on resource '" + account.getResource().getDisplayName() + "' since it is protected by roles.");
		}
		*/

		//try {
			////get the account name to be created
			//Get the accountID attr
			ResourceAttribute ra = account.getResource().getAccountIdAttribute();
			if (ra == null) {
				throw new ObjectsConstructionException("Could not find an attribute that was indicated as an account ID");
			}
			HashMap<String, Attribute> attrsMap = account.getVirtualAttributes(rolesToAdd);
			//Make sure there is a RA indicated as ID within the virtual attributes map
			if (!attrsMap.containsKey(ra.getUniqueName())) {
				throw new ObjectsConstructionException("Could not find a value for resource attribute '" + ra.getUniqueName() + "' that is indicated as an account ID");
			}
			
			//for now, make sure that the virtual attributes loading resulted a value for the accountID attribute
			//TODO: Support full resource SPML XSD scheme
			if (!attrsMap.get(ra.getUniqueName()).isFirstValueIsNotNull()) {
				throw new ObjectsConstructionException("Could not find a value for attribute '" + attrsMap.get(ra.getUniqueName()).getUniqueName() + "' that is indicated as the Account ID for resource.");
			}
			
			account.setTransientAttributes(attrsMap);
			
			return account;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public SpmlTask createAddAccountRequestForUserTask(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Set<Role> roles) throws OperationException {
		//iterate over all roles, fetch all relevant role resource attributes
		Set<RoleResourceAttribute> roleResouceAttrs = new HashSet<RoleResourceAttribute>();
		
		log.debug("Iterating over all roles that has reference to the resource("+resource.getDisplayName()+") the account should be created on, getting a global list of all role attribute rules from those roles");
		for (Role currRole : roles) {
			roleResouceAttrs.addAll(currRole.getRoleResourceAttributes(resource));
		}
		log.debug("Constructed roles resource attributes with amount: '" + roleResouceAttrs.size() + "'");

		
		try {
			//construct a virtual account here
			Account account = buildVirtualAccount(resource, user, roles,null);
		
			//get the constructed virtual resource attributes for the account
			HashMap<String, Attribute> accAttrs = account.getTransientAttributes();
		
			//build the context
			OperationContext cntx = new OperationContext();
			cntx.addVar("attrs", accAttrs);
			cntx.addVar("userIdAttrs", user.getUserIdentityAttributesAsMap());
			cntx.addVar("userName", user.getName());
			cntx.addVar("resourceUniqueName", resource.getUniqueName());
			cntx.addVar("resourceDisplayName", resource.getDisplayName());
			
			//iterate over the roles attrs and make the modifications
			for (RoleResourceAttribute currRoleResourceAttr : roleResouceAttrs) {
				//per rule make sure the 'attribute' var was cleaned from previous run as 'roleAttr.calculate'
				//does not override existence attribute. (roleResAttr(entity).read calculateAttributeResult() for more information)
				cntx.removeVar("attribute");
				
				ResourceAttribute resAttr = currRoleResourceAttr.calculateAttributeResult(cntx);
				
				if (resAttr == null) {
					//TODO: Support showStopper handling.
					log.warn("Skipping role resource attribute since a null was returned (expected a resource attribute!)");
					continue;
				}
				
				
				log.trace("Calculated (first) value is: '" + resAttr.getFirstValue().getAsString() + "' of resource attribute '" + resAttr.getUniqueName() + "'");
				
				log.trace("Checking whether the account attributes already contains resource attribute(inherited by 'resource attribute' level) with unique name '" + resAttr.getUniqueName() + "'");
				if (accAttrs.containsKey(resAttr.getUniqueName())) {
					log.trace("Attribute was found in resource attribute, overriding it!");
					//actually nothing to do here as this RA was set in context and pass by reference, but just in case :-)
					accAttrs.remove(resAttr.getUniqueName());
					accAttrs.put(resAttr.getUniqueName(), resAttr);
				} else{
					log.trace("Attribute was NOT found in resource attribute, adding it!");
					accAttrs.put(resAttr.getUniqueName(), resAttr);
				}
			}
			
			log.trace("Dumping entire account attribute to be created");
			log.trace(accAttrs);
			
			//?not needed, passed by reference
			account.setTransientAttributes(accAttrs);
			
			
			return createAddAccountRequestForUserTask(resource,user,memberOfGroups,account);
		} catch (ObjectsConstructionException e) {
			throw new OperationException(e.toString());
		}
		
	}
	
	
	public SpmlTask createModifyAccountRequestForUserTask(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException {
		String operationUniqueName = "MODIFY_ACCOUNT";
		
		ResourceTypeOperation rto = account.getResource().getResourceType().findResourceTypeOperation(operationUniqueName);
		
		if (rto == null) {
			throw new OperationException("This operation is not supported by resource '" + account.getResource().getDisplayName() + "'");
		}
		
		log.debug("Modifying account request has started for account name '" + account.getName() + "', on resource '" + account.getResource().getDisplayName() + "'");
		
		
		try {
			////get the account name to be created
			//Get the accountID attr
			ResourceAttribute ra = account.getResource().getAccountIdAttribute();
			if (ra == null) {
				throw new OperationException("Could not find an attribute that was indicated as an account ID");
			}
			
//			Map<String, Attribute> attrsMap = account.getVirtualAttributes(null);
			ModifyRequest modifyRequest = new ModifyRequest();
			modifyRequest.setExecutionMode(ExecutionMode.ASYNCHRONOUS);
			modifyRequest.setPsoID(new PSOIdentifier(account.getName(),null,account.getResource().getUniqueName()));
			
			
			
			
			//ATTRIBUTES TO MODIFY
			if ( (attrsToModify != null) && (attrsToModify.size() > 0) ) {
				log.debug("Found attributes to modify with amount '" + attrsToModify.size());
				
				Modification spmlAttrsMod = new Modification();
				spmlAttrsMod.setModificationMode(ModificationMode.REPLACE);
				spmlAttrsMod.setData(account.getVirtualAttributesAsSPMLExtensible(attrsToModify));
				
				modifyRequest.addModification(spmlAttrsMod);
			}
			
			
			//GROUP MEMBERSHIP
			if (memberOfGroupsToAdd != null) {
			if (memberOfGroupsToAdd.size() > 0) {
				log.debug("group membership to add amount '" + memberOfGroupsToAdd.size() + "'");
				//--References to ASSIGN -
				Modification modAddRefs = new Modification();
				CapabilityData cdAddRefs = new CapabilityData();
				cdAddRefs.setCapabilityURI(new URI("urn:oasis:names:tc:SPML:2.0:reference"));
				modAddRefs.addCapabilityData(cdAddRefs);
				modAddRefs.setModificationMode(ModificationMode.ADD);
			
				for (ResourceGroup currRG : memberOfGroupsToAdd) { 
					Reference ref = new Reference();
					ref.setTypeOfReference("memberOf");
					ref.setToPsoID(new PSOIdentifier(currRG.getUniqueId(),null,currRG.getResource().getUniqueName()));
				
					cdAddRefs.addReference(ref);
				}
			
				modifyRequest.addModification(modAddRefs);
			}
			}
			
			
			if (memberOfGroupsToRevoke != null) {
				log.debug("Group membership to delete amount '" +  memberOfGroupsToRevoke.size() + "'");
			if (memberOfGroupsToRevoke.size() > 0) {
				//--References to DELETE--
				Modification modDelRefs = new Modification();
				CapabilityData cdDelRefs = new CapabilityData();
				cdDelRefs.setCapabilityURI(new URI("urn:oasis:names:tc:SPML:2.0:reference"));
				modDelRefs.addCapabilityData(cdDelRefs);
				modDelRefs.setModificationMode(ModificationMode.DELETE);
			
				for (ResourceGroup currRG : memberOfGroupsToRevoke) { 
					Reference ref = new Reference();
					ref.setTypeOfReference("memberOf");
					ref.setToPsoID(new PSOIdentifier(currRG.getUniqueId(),null,currRG.getResource().getUniqueName()));
				
					cdDelRefs.addReference(ref);
				}
			
				modifyRequest.addModification(modDelRefs);
			}
			}
			
			
			
			//factory the task
			SpmlTask task = SpmlTask.factory(account.getResource().getUniqueName(), "Modify access of account name " + account.getName() + " on resource '" + account.getResource().getUniqueName() + "'");
			task.setResourceTypeOperation(rto);
			
			//Marshall the task to an request code
			String xml = getMarshaller().marshall(modifyRequest);
			log.trace("Marshalled modify request on pso ID '" + account.getName() + "', on resource '" + account.getResource().getUniqueName());
			log.trace("Marshalled SPML code: " + xml);
			task.setBody(xml);
			
			
			
			//perform any action rule in creation phase
			try {
				performTaskCreationPhaseActions(account.getResource(),task, rto);
			}catch(OperationException e) {
				throw new OperationException("Failed to create task due to action(s) execution exception: " + e.toString());
			}
			
			
			
			return task;
			
		}
		catch (DSMLProfileException e) {
			throw new OperationException (e.toString());
		}
		catch (Spml2Exception e) {
			throw new OperationException (e.toString());
		} catch (URISyntaxException e) {
			throw new OperationException (e.toString());
		}
	}
	
	public void createModifyAccountRequestForUser(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException {
		SpmlTask task = createModifyAccountRequestForUserTask(account, memberOfGroupsToAdd, memberOfGroupsToRevoke, attrsToModify);
		taskManager.persistTask(task);
	}
	
	//nicer proxy for outside calls
	public void createModifyAccountRequestForUser(String accountName, String resourceUniqueName, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException {
		Account account = accountManager.findAccount(accountName, resourceUniqueName);
		if (account == null) throw new OperationException("Could not find account name '" + accountName + "', on resource '" + resourceUniqueName + "'");
		
		SpmlTask task = createModifyAccountRequestForUserTask(account, memberOfGroupsToAdd, memberOfGroupsToRevoke, attrsToModify);
		taskManager.persistTask(task);
	}
	
	public void createModifyAccountRequestForUser(String accountName, String resourceUniqueName, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify, String taskDescription) throws OperationException {
		Account account = accountManager.findAccount(accountName, resourceUniqueName);
		if (account == null) throw new OperationException("Could not find account name '" + accountName + "', on resource '" + resourceUniqueName + "'");
		
		SpmlTask task = createModifyAccountRequestForUserTask(account, memberOfGroupsToAdd, memberOfGroupsToRevoke, attrsToModify);
		task.setDescription(taskDescription);
		taskManager.persistTask(task);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//perform operations
	public void performResourceTask(ResourceTask resourceTask) throws OperationException {
		log.trace("Invocation of perform action has started for Resource Task id'" + resourceTask.getTaskId());
		
		
		log.trace("Task is a RESOURCE TYPE, trying to load corresponding resource entity for resource name: " + resourceTask.getResourceUniqueName());
		Resource resource = resourceManager.findResource(resourceTask.getResourceUniqueName());
		log.trace("Successfully loaded resource entity.");
		
		
		if (resourceTask.getResourceTypeOperation().getUniqueName().equals("RESOURCE_RECONCILIATION")) {
			performResourceReconciliation(resource);
		}
		else if(resourceTask.getResourceTypeOperation().getUniqueName().equals("RESOURCE_FETCH_ACTIVE_DATA_OFFLINE")) {
			performResourceFetchActiveDataOffline(resourceTask, resource);
		}
		else {
			throw new OperationException("Resource operation is not supported!");
		}
	}
	
	
	public void performSpmlTask(SpmlTask spmlTask) throws OperationException {
		log.trace("Task is an Spml type, trying to load corresponding resource entity for resource name: " + spmlTask.getResourceUniqueName());
		Resource resource = resourceManager.findResource(spmlTask.getResourceUniqueName());
		log.trace("Successfully loaded resource entity.");
		
		
		//make sure this resource type supports the specified action to be performed
		//TODO!
		
		log.trace("Factoring corresponding resource operation controller for class name '" + resource.getResourceType().getResourceControllerClassName() + "'");
		//Get the operation controller for the resource type
//		SpmlResourceOperationController roc = (SpmlResourceOperationController)resource.getResourceType().factoryResourceOperationsController();
		//Make sure that a controller was successfully factored
/*		if (roc == null) {
			throw new OperationException("Could not factor resource operation controller for class '" + resource.getResourceType().getResourceControllerClassName() + "'");
		}
		
		roc.setResource(resource);
*/
		
		log.trace("Successflly initialized a new resource operations controller.");
		
		//let the controller handle execution and retrieve execution status
		org.openspml.v2.msg.spml.Request spmlRequest = null;
		
		//Unmarshall SPML request
		try {
			log.trace("Unmarshalling SPML xml data to an spml request object...");
			
			//NOTE: this is extremely important for 'AddRequest', otherwise addRequest.getDate() returns 0 results,
			//should get deeper and learn how DSMLProfileRegistrar works 
			ObjectFactory.getInstance().register(new DSMLProfileRegistrar());
			spmlRequest = spmlTask.getSpmlRequest();
			log.trace("Successfully unmarshalled, performing execution via resource operations controller...");
		} catch (UnknownSpml2TypeException e) {
			//TODO Handle exception correctly
			log.error("An SPML exception has occured: " + e);
			throw new OperationException(e.toString());
		}
		
		
		//redirect the SPML request to the correct method depends on the operation
		if (spmlRequest instanceof SuspendRequest) {
			SuspendRequest suspendRequest = (SuspendRequest)spmlRequest; 
			perform(resource, spmlTask, suspendRequest);
		}
		else if (spmlRequest instanceof ResumeRequest) {
			ResumeRequest resumeRequest = (ResumeRequest)spmlRequest; 
			perform(resource, spmlTask, resumeRequest);
		}
		
		else if (spmlRequest instanceof DeleteRequest) {
			DeleteRequest deleteRequest = (DeleteRequest)spmlRequest; 
			perform(resource, spmlTask, deleteRequest);
		}
		else if (spmlRequest instanceof AddRequest) {
			AddRequest addRequest = (AddRequest)spmlRequest;
			perform(resource, spmlTask, addRequest);
		}
		else if (spmlRequest instanceof ModifyRequest) {
			ModifyRequest modifyRequest = (ModifyRequest)spmlRequest;
			perform(resource, spmlTask, modifyRequest);
		}
		else {
			throw new OperationException("The specified operation is not supported by Velo!");
		}
	}
	
	
	
	
	
	public void performResourceFetchActiveDataOffline(ResourceTask resourceTask, Resource resource) throws OperationException {
		//in case the resource was renamed/deleted/etc...
		if (resource == null) {
			throw new OperationException("Resource was not found!");
		}
		
		if (resource.getResourceType().isGatewayRequired()) {
			VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
			
			try {
				log.debug("Performing operation through the gateway, please wait...");
				
				
				//add attrs to sync to the vdc
				for (ResourceAttribute currRA : resource.getAttributesToSync()) {
					vdcp.addAttribute(currRA.getUniqueName(), "");
				}
				
				//iface.performOperation(rto.getResourceGlobalOperation().getUniqueName(), resource.getResourceType().getResourceControllerClassName(), vdcProxy.factoryVeloDataContainer());
				
				//vdcp.importResourceConfParams(resource.factoryResourceDescriptor());
				String gzippedBase64Data = iface.performOperation("BUILD_ACTIVE_DATA", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
				
				log.trace("Size of gzipped base64 data: " + gzippedBase64Data.length());
				log.trace("---START OF GZIPPED BASE64 DUMP---");
				//log.trace(gzippedBase64Data);
				log.trace("---END OF GZIPPED BASE64 DUMP---");
				
				byte[] gzippedBase64Bytes = new byte[gzippedBase64Data.length()];
				gzippedBase64Bytes = new BASE64Decoder().decodeBuffer(gzippedBase64Data);
				
				//String strZZZ = new String(gzippedBase64Bytes);
				log.trace("Size of gzipped decoded(bytes): " + gzippedBase64Bytes.length);
				
				//log.trace("DUMPING STRING!");
				//log.trace(strZZZ);
				
				String activeDataFileName = resource.factorySyncFileName();
				ByteArrayInputStream bais = new ByteArrayInputStream(gzippedBase64Bytes);
				GZIPInputStream gis = new GZIPInputStream(bais);
				OutputStream out = new FileOutputStream(activeDataFileName);
				byte[] buf = new byte[2051648];  //size can be changed according to programmer's need.
		        int len;
		        while ((len = gis.read(buf)) > 0) {
		          out.write(buf, 0, len);
		        }
		        gis.close();
		        out.close();
		        
				
				/*
				log.trace("Starting Uncompressing the GZIP bytes");
				
				ByteArrayInputStream bais = new ByteArrayInputStream(arg);
				GZIPInputStream gis = new GZIPInputStream(bais);
				
		        OutputStream out = new FileOutputStream("c:/1.xml");
				byte[] buf = new byte[2051648];  //size can be changed according to programmer's need.
		        int len;
		        while ((len = gis.read(buf)) > 0) {
		          out.write(buf, 0, len);
		        }
		        System.out.println("The file and stream is ......closing.......... : closed"); 
		        gis.close();
		        out.close();
		        */
		        
				log.debug("Successfully performed operation through gateway!");
				return;
			} catch (SOAPFaultException e) {
				log.error("Could not perform operation via windows gateway: " + e.getMessage());
				throw new OperationException(e.getMessage());
			} /*catch (ResourceDescriptorException e) {
				throw new OperationException(e.getMessage());
			}*/ catch (IOException ioe) {
				throw new OperationException ("IO Exception: " + ioe.getMessage());
			}
			
			//return;
		}
		
		
		//make sure that the resource has an attribute that indicated as an accountId
		if (resource.getAccountIdAttribute() == null) {
			throw new OperationException("Could not fetch active data from resource, there is no resource attribute indicated as an AccountID");
		}
		
		//Prepare operation context
		OperationContext context = new OperationContext();
		context.addVar("accountsQueryManager", new Query());
		context.addVar("groupsQueryManager", new Query());
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, resourceTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		boolean invocationStatus;
		//perform execution
		log.trace("Started invocation of ResourceOperation...");
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		//PERFORM THE 'EXECUTION' PHASE VIA THE CONTROLLER
		//Found a generic resource task, executing...
		SpmlResourceOperationController roc = (SpmlResourceOperationController)ro.getResource().getResourceType().factoryResourceOperationsController();
		//Make sure that a controller was successfully factored
		if (roc == null) {
			throw new OperationException("Could not factor resource operation controller for class '" + ro.getResource().getResourceType().getResourceControllerClassName() + "'");
		}
		roc.setResource(resource);

		try {
			roc.resourceFetchActiveDataOffline(ro, resourceTask);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER
		
		
		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
	}
	
	/*
	@TransactionTimeout(value=1200)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Transactional
	*/
	public void performResourceReconciliation(Resource resource) throws OperationException {
		//in case the resource was renamed/deleted/etc...
		if (resource == null) {
			throw new OperationException("Resource not found!");
		}
		
		log.info("Performing resource reconciliation process for resource '" + resource.getDisplayName() + "'");
		Stopwatch wholeProcessWatch = new Stopwatch(true);
		ResourceReconcileSummary rrs = new ResourceReconcileSummary(true);
		rrs.times.startTime = new Date();
		rrs.description = "Resource reconcile process for resource: " + resource.getDisplayName(); 
		
		if (resource.getReconcilePolicy().isReconcileGroups()) {
			try {
				log.debug("Reconciliation of resource groups process has started...");
				Stopwatch groupsWatch = new Stopwatch(true);
			
				ReconcileGroups rg = new ReconcileGroups();
				rg.performResourceGroupsReconciliation(resource, rrs);
				groupsWatch.stop();
				log.debug("Reconciliation of resource groups process has finished within '" + groupsWatch.asSeconds() + "' seconds");
			} catch (ReconcileGroupsException e) {
				throw new OperationException(e.toString());
			}
		}
		else {
			log.info("Reconcile Policy was set not to reconcile groups, skipping process...");
		}
		
		
		if (resource.getReconcilePolicy().isReconcileAccounts()) {
			try {
				log.debug("Reconciliation of resource accounts process has started...");
				Stopwatch accountsWatch = new Stopwatch(true);
				
				ReconcileAccounts ra = new ReconcileAccounts(rrs);
				ra.performResourceAccountsReconciliation(resource);

				accountsWatch.stop();
				log.debug("Reconciliation of resource accounts process has finished within '" + accountsWatch.asSeconds() + "' seconds");
			} catch (ReconcileAccountsException e) {
				throw new OperationException(e.toString());
			}
		}
		else {
			log.info("Reconcile Policy was set not to reconcile accounts, skipping process...");
		}
		
		
		
		//TODO: reconcile group membership
		
		
		
		
		//trigger events
		OperationContext context = new OperationContext();
		
		if (resource.getReconcilePolicy().isActivateReconcileSummaries()) {
			log.debug("Reconcile summary was activated, creating reconcile summary.");
			wholeProcessWatch.stop();
			rrs.times.executionDuration = wholeProcessWatch.asMS();
			rrs.times.endTime = new Date();
		
			log.info("Amount of accounts in summary: " + rrs.getAccountsAmountInResource());
			log.info("Amount of groups in summary: " + rrs.getGroupsInsertedToRepository().size());
			ResourceReconcileSummaryEntity rrse = new ResourceReconcileSummaryEntity(rrs);
			rrse.update();
		
			context.addVar("objectId", rrse.getXmlObjectId());
			em.persist(rrse);
		} else {
			log.debug("Reconcile summary was not activated, skipping generation of reconcile summary.");
		}
		//needed for the ID sent to event context
		
		
		//make the TaskStatusBean stuck when commiting then new status (SUCCSS) (before utx.commit())
		//em.flush();
		
		
		context.addVar("resource", resource);
		context.addVar("reconcileSummary", rrs);
		
		
		EventDefinition ed = eventManager.find(resourceReconcilationEvent);

		try {
			//eventManager.invokeEventDefinitionResponses(ed, context);
			eventManager.invokeEvent(ed, context);
		} catch (ScriptInvocationException e) {
			log.error(e.toString());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public void perform(Resource resource, SpmlTask spmlTask, SuspendRequest request) throws OperationException {
		log.trace("Invocation of perform action has started for SPML request id'" + request.getRequestID());
		
		
		
		//Extract data from the request
		String accountName = request.getPsoID().getID();
		String targetName = request.getPsoID().getTargetID();
		
		//Prepare operation context
		OperationContext context = new OperationContext();
		context.addVar("accountName", accountName, PhaseRelevance.ALL);
		context.addVar("targetName", targetName, PhaseRelevance.ALL);
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, spmlTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		
		
		
		//delegate the operation invocation to the gateway
		//TODO: Support Linux gateway as well
		if (resource.getResourceType().isGatewayRequired()) {
			executeOperationExecutionPhaseViaWindowsGateway(resource,spmlTask.getResourceTypeOperation(),request, ro);
			
			return;
		}
		
		
		SpmlResourceOperationController roc = null;
		try {
			roc = factoryNativeController(spmlTask, context, resource, ro);
		} catch (FactoryNativeResourceControllerException e) {
			throw new OperationException(e.toString());
		}
		
		log.trace("Initialized operation context variables, dump of context: " + context.toString());
		
		
		boolean invocationStatus;

		
		//perform PRE execution
		log.trace("Started invocation of ResourceOperation...");
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		//EXECUTION
		try {
			roc.performOperation(spmlTask, ro, request);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER
		

		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
	}
	
	
	
	//IMPLEMENT
	public void perform(Resource resource, SpmlTask spmlTask, ResumeRequest request) throws OperationException {
		log.trace("Invocation of perform action has started for SPML request id'" + request.getRequestID());
		
		
		//Extract data from the request
		String accountName = request.getPsoID().getID();
		String targetName = request.getPsoID().getTargetID();
		
		//Prepare operation context
		OperationContext context = new OperationContext();
		context.addVar("accountName", accountName, PhaseRelevance.ALL);
		context.addVar("targetName", targetName, PhaseRelevance.ALL);
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, spmlTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		
		if (resource.getResourceType().isGatewayRequired()) {
			//delegate the operation invocation to the gateway
			//TODO: Support Linux gateway as well
			executeOperationExecutionPhaseViaWindowsGateway(resource,spmlTask.getResourceTypeOperation(),request, ro);
			
			return;
		}
		
		
		
		SpmlResourceOperationController roc = null;
		try {
			roc = factoryNativeController(spmlTask, context, resource, ro);
		} catch (FactoryNativeResourceControllerException e) {
			throw new OperationException(e.toString());
		}
		
		log.trace("Initialized operation context variables, dump of context: " + context.toString());
		
		
		boolean invocationStatus;

		
		//perform PRE execution
		log.trace("Started invocation of ResourceOperation...");
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		//EXECUTION
		try {
			roc.performOperation(spmlTask, ro, request);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER
		

		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
	}
	
	public void perform(Resource resource, SpmlTask spmlTask, DeleteRequest request) throws OperationException {
		log.trace("Invocation of perform action has started for SPML request id'" + request.getRequestID());
		
		//Extract data from the request
		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		
		//Prepare operation context
		
		//make sure the account is loadable...
		Account accountToDelete = accountManager.findAccount(accountName, resourceName);
		if (accountToDelete == null) {
			throw new OperationException("Cannot perform account delete operation, account was not found in repository!");
		}
		//apparently, there is a need for virtual account attributes accessible via delete actions
		Map<String,Attribute> attrsMap = accountToDelete.getVirtualAttributes(null);
		
		OperationContext context = new OperationContext();
		context.addVar("accountName", accountName, PhaseRelevance.ALL);
		context.addVar("resourceName", resourceName, PhaseRelevance.ALL);
		context.addVar("attrs", attrsMap);
		context.addVar("account", accountToDelete);
		
		
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, spmlTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		
		
		if (resource.getResourceType().isGatewayRequired()) {
			//delegate the operation invocation to the gateway
			//TODO: Support Linux gateway as well
			executeOperationExecutionPhaseViaWindowsGateway(resource,spmlTask.getResourceTypeOperation(),request, ro);
			
			
			//---Everything passed ok, perform repository modifications---
			//Delete the account from the repository
			accountManager.removeAccountEntity(accountName, resource.getUniqueName());
			
			return;
		}
		
		
		
		SpmlResourceOperationController roc = null;
		try {
			roc = factoryNativeController(spmlTask, context, resource, ro);
		} catch (FactoryNativeResourceControllerException e) {
			throw new OperationException(e.toString());
		}
		
		
		log.trace("Initialized operation context variables, dump of context: " + context.toString());
		
		boolean invocationStatus;

		
		//perform PRE execution
		log.trace("Started invocation of ResourceOperation...");
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		//EXECUTION
		try {
			roc.performOperation(spmlTask, ro, request);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER
		

		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
	
		
		
		//perform delete account from repository
		accountManager.removeAccountEntity(accountName, resource.getUniqueName());
	}
	
	
	
	
	
	
	
	public void perform(Resource resource, SpmlTask spmlTask, AddRequest request) throws OperationException {
		log.trace("Invocation of perform action has started for SPML request id'" + request.getRequestID());
		
		//Build a map with all virtual account attributes
		Map<String,ResourceAttribute> attrsMap = resource.getManagedAttributesAsMap(request.getData().getOpenContentElements(DSMLAttr.class));
		//Extract data from the request
		String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		//Prepare operation context
		OperationContext context = new OperationContext();
		context.addVar("accountName", accountName, PhaseRelevance.ALL);
		context.addVar("resourceName", resourceName, PhaseRelevance.ALL);
		context.addVar("attrs", attrsMap, PhaseRelevance.ALL);
		
		
		//----EXPERIMENTAL----
		if (spmlTask.getWorkflowProcessId() != null) {
			//load process ID and set into the context.
			//WorkflowBean wfBean = (WorkflowBean)Component.getInstance("workflowManager");
			//JbpmContext jbpmContext = Jbpm.instance().getJbpmConfiguration().createJbpmContext();
			
			//UserTransaction tx = null;
			try {
				//WORKS WITH SEAM managed jbpm context
				//System.out.println("!!!!!!!!!!!!!!!WAAAAAAAAAAAAAAAAAAAA:" + jbpmContext);
				
				
				
				
				InitialContext ctx = null;
		    	TransactionManager tm = null;
		        try {
		          ctx = new InitialContext();
		          tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
		        }catch(Exception e) {
		        	e.printStackTrace();
		        }
		        finally {
		           if(ctx!=null) {
		              try { ctx.close(); } catch (NamingException e) {}
		           }
		        }
				
				
				
				
				
				
				
				
				
				
				
				
				//Session session = (Session) em.getDelegate();
				//FullTextHibernateSessionProxy hem = (FullTextHibernateSessionProxy)em.getDelegate();
				//jbpmContext.setSession(hem.getSessionFactory().getCurrentSession());
				
				
				//ProcessInstance pi = ManagedJbpmContext.instance().loadProcessInstance(spmlTask.getWorkflowProcessId());
				
				//UserTransaction transaction = Transaction.instance();
				//transaction.registerSynchronization(this);
				//tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
				//tx.begin();
				//JbpmContext jbpmContext = Jbpm.instance().getJbpmConfiguration().createJbpmContext();
				
				JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
				JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
				
				
				try {
		    		System.out.println("!!!!!!!!!!!!!!!Z1: " + tm.getStatus());
		    		System.out.println("!!!!!!!!!!!!!!!Z1: " + tm.getTransaction().getStatus());
		    	}catch(SystemException e) {
		    		e.printStackTrace();
		    	}
				//DbPersistenceService persServ=(DbPersistenceService)jbpmContext.getServices().getPersistenceService();
				//jbpmContext.setSession(persServ.getSessionFactory().getCurrentSession());
				ProcessInstance pi = jbpmContext.loadProcessInstance(spmlTask.getWorkflowProcessId());
				//ProcessInstance pi = wfBean.getProcessInstance(spmlTask.getWorkflowProcessId());
				
				try {
		    		System.out.println("!!!!!!!!!!!!!!!Z2: " + tm.getStatus());
		    		System.out.println("!!!!!!!!!!!!!!!Z2: " + tm.getTransaction().getStatus());
		    	}catch(SystemException e) {
		    		e.printStackTrace();
		    	}
		    	
				pi.getContextInstance();
				pi.getVersion();
				context.addVar("process",pi);
				
				
		        
		        try {
		    		System.out.println("!!!!!!!!!!!!!!!Z3: " + tm.getStatus());
		    		System.out.println("!!!!!!!!!!!!!!!Z3: " + tm.getTransaction().getStatus());
		    	}catch(SystemException e) {
		    		e.printStackTrace();
		    	}
				
			}catch(Exception e) {
				System.out.println("!!!!!!!!!!!!!: " + e.getMessage());
			}finally {
				//jbpmContext.close();
			}
		}
		//ResourceOperationActionTools tools = new ResourceOperationActionTools();
		//context.addVar("tools", tools);
		//----EXPERIMENTAL----
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, spmlTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		
		//gateway
		if (resource.getResourceType().isGatewayRequired()) {
			//delegate the operation invocation to the gateway
			//TODO: Support Linux gateway as well
			executeOperationExecutionPhaseViaWindowsGateway(resource,spmlTask.getResourceTypeOperation(),request, ro);
			
			
			//TODO: Support Group membership persistance in the repository
			//---Everything passed ok, perform repository modifications---
			//Persist the account in repository
			try {
				accountManager.persistAccount(request.getPsoID().getID(), resource.getUniqueName(), getRepositoryUserNameReferenceFromAddRequest(request));
			} catch (NoResultFoundException e) {
				throw new OperationException("Could not persist the account entity in the repository: " + e.toString());
			}
			
			return;
		}
		
		
		SpmlResourceOperationController roc = null;
		try {
			roc = factoryNativeController(spmlTask, context, resource, ro);
		} catch (FactoryNativeResourceControllerException e) {
			throw new OperationException(e.toString());
		}
		
		
		log.trace("Initialized operation context variables, dump of context: " + context.toString());
		
		boolean invocationStatus;

		
		//TODO: Add validation for all operations! not just add!
		//perform VALIDATION execution
		invocationStatus = ro.validateActionOperation();
		if (!invocationStatus) {
			log.error("VALIDATE phase invocation has failed, indication the whole process as a failure...");
			throw new OperationException("VALIDATE invocation has failed: " + ro.getErrorMessage());
		}
		
		//perform PRE execution
		log.trace("Started invocation of ResourceOperation...");
		
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		//EXECUTION
		try {
			roc.performOperation(spmlTask, ro, request);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER
		

		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
		
		
		//---Everything passed ok, perform repository modifications---
		//Persist the account in repository
		try {
			accountManager.persistAccount(request.getPsoID().getID(), resource.getUniqueName(), getRepositoryUserNameReferenceFromAddRequest(request));
		} catch (NoResultFoundException e) {
			throw new OperationException("Could not persist the account entity in the repository: " + e.toString());
		}
		
		
		
		
		//If this is an SPML GROUP controller type then handle group membership here
		if (resource.getResourceType().getResourceControllerType() == ResourceControllerType.SPML_ACCESS_GROUPS) {
			if (!resource.getResourceType().isGatewayRequired()) {
				GroupMembershipSpmlResourceOpreationController groupableROC = (GroupMembershipSpmlResourceOpreationController)roc;
				
				
				//Now handle group membership insertion
				List<String> groupsStringsToAdd = roc.getGroupsToAssign(request,resource.getUniqueName());
				
				
				//load the groups from the repository as the 'type' of the group is needed in the context
				//(sucks: could not find a way to store the group type within the standard SPML V2 spec)
				List<ResourceGroup> groupEntitiesToAdd = resourceGroupManager.findResourceGroupsInRepository(groupsStringsToAdd, resource); 
				
				
				log.debug("Account should be a memmber of groups amount '" + groupsStringsToAdd.size() + "', performing add group membership operation");
				ResourceTypeOperation rto = ro.getResource().getResourceType().findResourceTypeOperation(ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
				ResourceOperation groupMembershipResourceOperation = ResourceOperation.Factory(ro.getResource(), context, rto);
				
				try {
					groupableROC.performOperationAddGroupMembership(spmlTask, groupMembershipResourceOperation, request, groupEntitiesToAdd);
					
					//success, persist the group membership in repository
					//TODO: persist the group membership in repository
				}
				catch (OperationException e) {
					throw e;
				}
			}
		} else if (resource.getResourceType().getResourceControllerType() == ResourceControllerType.SPML_GENERIC) {
			//TODO: PERSIST GROUP MEMBERSHIP IN REPOSITORY!
		}
	}
	
	
	
	public void perform(Resource resource, SpmlTask spmlTask, ModifyRequest request) throws OperationException {
		log.trace("Invocation of perform action has started for SPML request id'" + request.getRequestID());
		//String accountName = request.getPsoID().getID();
		String resourceName = request.getPsoID().getTargetID();
		
		//make sure the account is loadable...
		Account accountToModify = accountManager.findAccount(request.getPsoID().getID(), resourceName);
		if (accountToModify == null) {
			throw new OperationException("Cannot perform account modify operation, account was not found in repository!");
		}

		Map<String,Attribute> attrsMap = accountToModify.getVirtualAttributes(null); 
		//Prepare operation context
		OperationContext context = new OperationContext();
		//context.addVar("accountName", accountName, PhaseRelevance.ALL);
		context.addVar("resourceName", resourceName, PhaseRelevance.ALL);
		context.addVar("accountName", request.getPsoID().getID(), PhaseRelevance.ALL);
		context.addVar("attrs", attrsMap, PhaseRelevance.ALL);
		log.trace("Initialized operation context variables, dump of context: " + context.toString());
		
		log.trace("Factoring ResourceOperation object...");
		ResourceOperation ro = factoryResourceOperation(resource, context, spmlTask.getResourceTypeOperation());
		ro.setResource(resource);
		
		
		//gateway
		if (resource.getResourceType().isGatewayRequired()) {
			//delegate the operation invocation to the gateway
			//TODO: Support Linux gateway as well
			executeOperationExecutionPhaseViaWindowsGateway(resource,spmlTask.getResourceTypeOperation(),request, ro);
			
			return;
		}
		
		
		
		
		SpmlResourceOperationController roc = null;
		try {
			roc = factoryNativeController(spmlTask, context, resource, ro);
		} catch (FactoryNativeResourceControllerException e) {
			throw new OperationException(e.toString());
		}
		
		
		boolean invocationStatus;
		//perform execution
		log.trace("Started invocation of ResourceOperation...");
		
		
		invocationStatus = ro.preActionOperation();
		if (!invocationStatus) {
			log.error("PreExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PreAction invocation has failed: " + ro.getErrorMessage());
		}
		
		
		//EXECUTION
		try {
			roc.performOperation(spmlTask, ro, request);
		}
		catch (OperationException e) {
			throw e;
		}
		//END OF EXECUTION PHASE VIA CONTROLLER

		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}
		
		
		//Perform POST operation actions
		invocationStatus = ro.postActionOperation();
		if (!invocationStatus) {
			log.error("PostExecution invocation failed, indication the whole process as a failure...");
			throw new OperationException("PostAction invocation has failed: " + ro.getErrorMessage());
		}

		//---Everything passed ok, perform repository modifications---
		//?? nothing to do here currently.
		
		
		
		
		
		
		
		
		//If this is an SPML GROUP controller type then handle group membership here
		if (resource.getResourceType().getResourceControllerType() == ResourceControllerType.SPML_ACCESS_GROUPS) {
			if (!resource.getResourceType().isGatewayRequired()) {
				GroupMembershipSpmlResourceOpreationController groupableROC = (GroupMembershipSpmlResourceOpreationController)roc;
						
				Map<String,List<String>> groups = SpmlUtils.getGroupMembershipToModify(request, resource.getUniqueName());
				//Now handle group membership insertion
				List<String> groupsStringsToAdd = groups.get("groupsToAssign");
				List<String> groupsStringsToRemove = groups.get("groupsToRevoke");
				log.debug("Account should BE A memmber of groups amount '" + groupsStringsToAdd.size() + "', membership should be removed from groups amount '" + groupsStringsToRemove.size() + "' performing add group membership operation");

				
				
				//load the groups from the repository as the 'type' of the group is needed in the context
				//(sucks: could not find a way to store the group type within the standard SPML V2 spec)
				List<ResourceGroup> groupEntitiesToAdd = resourceGroupManager.findResourceGroupsInRepository(groupsStringsToAdd, resource); 
				List<ResourceGroup> groupEntitiesToRemove = resourceGroupManager.findResourceGroupsInRepository(groupsStringsToRemove, resource);
						
				
				
				
				ResourceTypeOperation rtoAddGrps = ro.getResource().getResourceType().findResourceTypeOperation(ADD_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
				ResourceOperation addGroupMembershipResourceOperation = ResourceOperation.Factory(ro.getResource(), context, rtoAddGrps);
				ResourceTypeOperation rtoRemoveGrps = ro.getResource().getResourceType().findResourceTypeOperation(DELETE_GROUP_MEMBERSHIP_OPERATION_UNIQUE_NAME);
				ResourceOperation removeGroupMembershipResourceOperation = ResourceOperation.Factory(ro.getResource(), context, rtoRemoveGrps);
						
				try {
					groupableROC.performOperationAddGroupMembership(spmlTask, addGroupMembershipResourceOperation, (Request)request, groupEntitiesToAdd);
					groupableROC.performOperationRemoveGroupMembership(spmlTask, removeGroupMembershipResourceOperation, request, groupEntitiesToRemove);
				
					//success, persist the group membership in repository
					//TODO: persist the group membership in repository
				}
				catch (OperationException e) {
					throw e;
				}
			}
		} else if (resource.getResourceType().getResourceControllerType() == ResourceControllerType.SPML_GENERIC) {
			//TODO: PERSIST GROUP MEMBERSHIP IN REPOSITORY!
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//helper
	private ResourceOperation factoryResourceOperation(Resource resource, OperationContext context, ResourceTypeOperation resourceTypeOperation) {
		//return ResourceOperation.Factory(resource, context, resourceTypeOperation);
		ResourceOperation ro = new ResourceOperation(resource,context);
		
		ro.setCreationPhaseResourceActions(resource.getCreationPhaseActions(resourceTypeOperation));
		ro.setValidateResourceActions(resource.getValidatePhaseActions(resourceTypeOperation));
		ro.setPreResourceActions(resource.getPrePhaseActions(resourceTypeOperation));
		ro.setPostResourceActions(resource.getPostPhaseActions(resourceTypeOperation));
		ro.setResourceTypeOperation(resourceTypeOperation);
		
		return ro;
	}

	
	
	private String getRepositoryUserNameReferenceFromAddRequest(AddRequest request) throws NoResultFoundException {
		CapabilityData[] cDatas = request.getCapabilityData();
		if (cDatas.length < 1) {
			throw new NoResultFoundException("No capability datas were found in request!");
		}
		
		//expecting one capability data...
		CapabilityData firstCapData = cDatas[0];
		Reference[] refsInFirstCapData = firstCapData.getReference();
		
		String userName = null;
		boolean refFound = false;
		for (int i=0;i<refsInFirstCapData.length;i++) {
			if (refsInFirstCapData[i].getTypeOfReference().equals(SPML_USER_IN_REPOSITORY_REFERENCE_TYPE)) {
				refFound=true;
				userName = refsInFirstCapData[i].getToPsoID().getID();
			}
		}
		
		if (!refFound) {
			throw new NoResultFoundException("Could not find expected user in repository reference!");
		}
		
		return userName;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	//PRIVATE Helper methods
	private ResourceGlobalOperation findResourceGlobalOperation(String uniqueName) {
		return (ResourceGlobalOperation)em.createNamedQuery("resourceGlobalOperation.findByUniqueName").setParameter("uniqueName", uniqueName).getSingleResult();
	}
	
	private String generateUniqueId() {
		return UniqueIdGenerator.generateUniqueIdByUID();
	}
	
	
	private XMLMarshaller getMarshaller() throws Spml2Exception {
		XMLMarshaller marshaller = new ReflectiveXMLMarshaller();
		marshaller.setIndent(4);
		return marshaller;
	} 
	
	
	//generic task modifications relevant for all operations
	private void performTaskCreationPhaseActions(Resource resource, Task task, ResourceTypeOperation rto) throws OperationException {
		OperationContext context = new OperationContext();
		context.addVar("task", task);
		ResourceOperation ro = factoryResourceOperation(resource, context, rto);
		boolean indicator = ro.creationActionOperation();
		
		if (!indicator) {
			throw new OperationException("Failed to execute resource action: " + ro.getErrorMessage());
		}
	}
	
	
	private SpmlResourceOperationController factoryNativeController(SpmlTask spmlTask, OperationContext context, Resource resource, ResourceOperation ro) throws FactoryNativeResourceControllerException {
		SpmlResourceOperationController roc = (SpmlResourceOperationController)ro.getResource().getResourceType().factoryResourceOperationsController();
		
		
		//Make sure that a controller was successfully factored
		if (roc == null) {
			throw new FactoryNativeResourceControllerException("Could not factor resource operation controller for class '" + ro.getResource().getResourceType().getResourceControllerClassName() + "'");
		}
		
		roc.setResource(resource);
		roc.init(context);
		
		return roc;
	}
	
	private IWindowsGatewayApi factoryWindowsGateway(Resource resource, ResourceTypeOperation rto, Request request, ResourceOperation ro, VeloDataContainerProxy vdcp) throws OperationException {
		if (resource.getGateway() == null) {
			throw new OperationException("Gateway must be specified for resource '" + resource.getDisplayName() + "'");
		}
		
		
		String wsdlUrl = "http://" + resource.getGateway().getHostName() + ":" + resource.getGateway().getPort() + "/ServiceModelSamples/service?wsdl";
		String serviceName = "WindowsGatewayApi";
		String namespace = "http://tempuri.org/";
		
		if (log.isTraceEnabled()) {
			log.trace("Gateway display name: " + resource.getGateway().getDisplayName());
			log.trace("Gateway WSDL Url: " + wsdlUrl);
			log.trace("Gateway serviceName: " + serviceName);
			log.trace("Gateway namespace: " + namespace);
			
			if (rto != null) {
				log.trace("Operation Unique Name:" + rto.getResourceGlobalOperation().getUniqueName());
			}
			log.trace("Resource Type Controller Class Name: " + resource.getResourceType().getResourceControllerClassName());
		}

		QName serviceQN = new QName(namespace,serviceName);
		
		//throws a WebServiceException if URL does not exist
		WindowsGatewayApi wgi = null;
		IWindowsGatewayApi iface = null;
		
		try {
			log.trace("Factoring a windows gateway service manager...");
			wgi = new WindowsGatewayApi(new URL(wsdlUrl), serviceQN);
			//TODO: Replace basic http binding to a more secured one!
			log.trace("Binding Windows Gateway via Basic Http Binding...");
			iface = wgi.getBasicHttpBindingIWindowsGatewayApi();
		} catch (WebServiceException e) {
			log.trace("Performing execution via Windows Gateway, please wait...");
			throw new OperationException(e.getMessage());
		} catch (MalformedURLException e) {
			throw new OperationException(e.getMessage());
			//Hopefully encapsulate all exceptions
			/*Already catched by WebServiceException
		} catch (SOAPFaultException e) {
			throw new OperationException(e.getMessage());*/
		}
		
		
		
		//Prepare the DateContainer for the windows gateway
		//pass by reference from outside: vdcp = new VeloDataContainerProxy();
		
		try {
			vdcp.importResourceConfParams(resource.factoryResourceDescriptor());
			if (request != null) {
				vdcp.importDataFromRequest(resource, request);
			}
			if (ro != null) {
				vdcp.importDataFromResourceOperation(ro);
			}
		}catch (ResourceDescriptorException e) {
			throw new OperationException(e.toString());
		}catch (ObjectsConstructionException e) {
			throw new OperationException(e.toString());
		}

		try {
			iface = wgi.getBasicHttpBindingIWindowsGatewayApi();
			return iface;
		} catch (SOAPFaultException e) {
			log.error("Could not perform operation via windows gateway: " + e.getMessage());
			throw new OperationException(e.getMessage());
		}
	}
	
	
	
	private void executeOperationExecutionPhaseViaWindowsGateway(Resource resource, ResourceTypeOperation rto, Request request, ResourceOperation ro) throws OperationException {
		VeloDataContainerProxy vdcp = new VeloDataContainerProxy(); 
		IWindowsGatewayApi iface = factoryWindowsGateway(resource, rto, request, ro, vdcp);
	
		VeloDataContainer vdc = vdcp.factoryVeloDataContainer();
		System.out.println("!!!!!" + vdc.getResourceConfParams().getValue().getKeyValueOfstringstring().size());
		
		try {
			log.debug("Performing operation through the gateway, please wait...");
			iface.performOperation(rto.getResourceGlobalOperation().getUniqueName(), resource.getResourceType().getResourceControllerClassName(), vdc);
			log.debug("Successfully performed operation through gateway!");
		} catch (SOAPFaultException e) {
			log.error("Could not perform operation via windows gateway: " + e.getMessage());
			throw new OperationException(e.getMessage());
		} catch (WSException e) {
			throw new OperationException("Could not perform operation through gateway(make sure the gateway is up and running!): " + e.getMessage());
		}
	}

	
	
	
	
	public void authenticate(Resource resource,String userName, String password) throws UserAuthenticationException {
		//gateway
		if (resource.getResourceType().isGatewayRequired()) {
			try {
			//delegate the operation invocation to the gateway
			
			VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
			vdcp.setAccountName(userName);
			vdcp.setPassword(password);
			
			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
			
			
			try {
				iface.performOperation("AUTHENTICATE", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
			} catch (SOAPFaultException e) {
				log.error("Could not perform operation via windows gateway: " + e.getMessage());
				throw new UserAuthenticationException(e.getMessage());
			} catch (WSException e) {
				throw new UserAuthenticationException("Could not perform operation through gateway(make sure the gateway is up and running!): " + e.getMessage());
			} 
			}
			catch (OperationException e) {
				throw new UserAuthenticationException(e.getMessage());
			}
			
			return;
		}

		ResourceOperationController roc = (ResourceOperationController)resource.getResourceType().factoryResourceOperationsController();
		roc.setResource(resource);
		
		try {
			//return the authentication result
			roc.authenticate(userName, password);
		}
		catch (AuthenticationFailureException e) {
			log.info("Failed to authenticate user '" + userName + "' against resource '" + resource.getDisplayName() + "': " + e.toString());
			throw new UserAuthenticationException(e.toString());
		}
	}
	
	
	public void testConnectivity(Resource resource) throws OperationException {
		log.info("Testing connecvitiy for resource: '" + resource.getDisplayName() + "'");
		
		String errMsg = "Failed to connect resource '" + resource.getDisplayName() + "': ";
		
		if (resource.getResourceType().isGatewayRequired()) {
			VeloDataContainerProxy vdcp = new VeloDataContainerProxy();
			IWindowsGatewayApi iface = factoryWindowsGateway(resource, null, null, null, vdcp);
			
			try {
				iface.performOperation("TEST_CONNECTIVITY", resource.getResourceType().getResourceControllerClassName(), vdcp.factoryVeloDataContainer());
			} catch (SOAPFaultException e) {
				log.error("Could not test connectivity via windows gateway: " + e.getMessage());
				throw new OperationException(errMsg + e.getMessage());
			} catch (WSException e) {
				throw new OperationException(errMsg + e.getMessage());
			} 
			
			log.debug("Resource is configured with gateway, determining connctivity via gateway.");
		} else {
			log.debug("Resource is configured without gateway, determining connctivity via local adapter.");
			try {
				Adapter adapter = resourceManager.adapterFactory(resource);
				adapter.connect();
				if (adapter.isConnected()) {
					log.debug("Connectivity is ok :)");
				}
			} catch (FactoryException e) {
				throw new OperationException(errMsg + e.getMessage());
			} catch (AdapterException e) {
				throw new OperationException(errMsg + e.getMessage());
			}
		}
	}
	
	
	
	/*
	private void performOperationExecutionPhaseViaNativeController(SpmlTask spmlTask, OperationContext context, Resource resource, ResourceOperation ro, Request request) throws OperationException {
		log.debug("Performing operation execution phase via resource controller has started.");
		
		log.debug("Determining whether execution should be done via native controller or via remote gateway...");
		if (resource.getResourceType().isGatewayRequired()) {
			log.debug("Gateway is required, executing via remote(Windows) gateway!");
			executeOperationExecutionPhaseViaWindowsGateway(resource, ro, request);
		} else {
			log.debug("Gateway is not reqired, executing via native local controller...");
			SpmlResourceOperationController roc = factoryNativeController(spmlTask, context, resource, ro);

			try {
				Class partypes[] = new Class[5];
				partypes[0] = spmlTask.getClass();
				partypes[1] = context.getClass();
				partypes[2] = resource.getClass();
				partypes[3] = ro.getClass();
				partypes[4] = request.getClass();
				
				Method meth = roc.getClass().getMethod("performOperation", partypes);
				
				Object argList[] = new Object[5];
	            argList[0] = spmlTask;
	            argList[1] = context;
	            argList[2] = resource;
	            argList[3] = ro;
	            argList[4] = request;
	            		
				meth.invoke(roc, argList);
			}
			catch (Throwable e) {
				throw new OperationException(e.getMessage());
			}
		}
	}
	*/
}
