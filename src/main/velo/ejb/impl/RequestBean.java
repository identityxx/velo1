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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import velo.common.SysConf;
import velo.contexts.OperationContext;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.IdentityAttributeManagerLocal;
import velo.ejb.interfaces.PositionManagerLocal;
import velo.ejb.interfaces.RequestManagerLocal;
import velo.ejb.interfaces.RequestManagerRemote;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.AccountsRequest;
import velo.entity.BulkTask;
import velo.entity.CreateUserRequest;
import velo.entity.DeleteUserRequest;
import velo.entity.IdentityAttribute;
import velo.entity.ModifyUserRolesRequest;
import velo.entity.Position;
import velo.entity.Request;
import velo.entity.RequestAccount;
import velo.entity.RequestAttribute;
import velo.entity.RequestRole;
import velo.entity.RequestedPosition;
import velo.entity.ResumeUserRequest;
import velo.entity.Role;
import velo.entity.SelfServiceAccessRequest;
import velo.entity.SpmlTask;
import velo.entity.SuspendUserRequest;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserRole;
import velo.entity.Request.RequestStatus;
import velo.entity.UserJournalingEntry.UserJournalingActionType;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.LoadingObjectsException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.ObjectNotFoundException;
import velo.exceptions.OperationException;
import velo.exceptions.PersistEntityException;
import velo.exceptions.RequestCreationException;
import velo.exceptions.RequestExecutionException;
import velo.exceptions.ScriptInvocationException;

/**
 * A Stateless EJB bean for managing a Request(s).
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
//@Name("requestAPI")
public class RequestBean implements RequestManagerLocal, RequestManagerRemote {
	// Constants
	private static final String eventRequestStatusModification = "REQUEST_STATUS_MODIFICATION";

	private static Logger log = Logger.getLogger(RequestBean.class.getName());

	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	//TODO, FIX!!! @Resource(mappedName = "MDBQueueConnectionFactory")
	private QueueConnectionFactory queueCF;

	//TODO, FIX!!!@Resource(mappedName = "RequestProcessingQueue")
	private Queue mdbQueue;

	@EJB
	private IdentityAttributeManagerLocal identityAttributeManager;

	@EJB
	private UserManagerLocal userManager;

	@EJB
	private RoleManagerLocal roleManager;
	
	@EJB
	private PositionManagerLocal positionManager;

	@EJB
	private AccountManagerLocal accountManager;
	
	@EJB
	private ResourceOperationsManagerLocal resourceOperations;

	@EJB
	private EventManagerLocal eventManager;
	
	

	@Resource
	TimerService timerService;

	@Resource
	private SessionContext sessionContext;
	
	
	public Request submitRequest(Request request) throws RequestCreationException {
		log.info("Creation of 'Basic Request' has started");
		
		em.persist(request);
		em.flush();
		
		log.info("Successfully stored request!");
		
		return request;
	}
	
	public void submitRequest(SuspendUserRequest suspendUserRequest) throws RequestCreationException {
		log.info("Creation of 'Suspend User Request' has started for request ID: " + suspendUserRequest.getRequestId());

		//make sure user exist
		if (!userManager.isUserExit(suspendUserRequest.getUserName())) {
			throw new RequestCreationException(
				"Cannot create 'Disable User Request' request, user name: '"
				+ suspendUserRequest.getUserName()
				+ "' does not exist in repository!");
		}
		
		suspendUserRequest.addLog("INFO", "Submitted Request", "Submitted Suspend User Request for user '" +  suspendUserRequest.getUserName() + "'");
		
		em.persist(suspendUserRequest);
		log.info("Successfully stored request!");
	}
	
	public void submitRequest(ResumeUserRequest resumeUserRequest) throws RequestCreationException {
		log.info("Creation of 'Resume User Request' has started for request ID: " + resumeUserRequest.getRequestId());

		//make sure user exist
		if (!userManager.isUserExit(resumeUserRequest.getUserName())) {
			throw new RequestCreationException(
				"Cannot create 'Disable User Request' request, user name: '"
				+ resumeUserRequest.getUserName()
				+ "' does not exist in repository!");
		}
		
		resumeUserRequest.addLog("INFO", "Submitted Request", "Submitted Resume User Request for user '" +  resumeUserRequest.getUserName() + "'");
		
		em.persist(resumeUserRequest);
		log.info("Successfully stored request!");
	}
	
	
	public SelfServiceAccessRequest submitRequest(SelfServiceAccessRequest selfServiceAccessRequest) throws RequestCreationException {
		log.info("Creation of 'Resume User Request' has started for request ID: " + selfServiceAccessRequest.getRequestId());
		em.persist(selfServiceAccessRequest);
		em.flush();
		log.info("Successfully stored request!");
		
		return selfServiceAccessRequest;
	}
	
	
	public void submitRequest(AccountsRequest request) throws RequestCreationException {
		log.debug("Submitting accounts request has started...");
		AccountsRequest areq = (AccountsRequest) request;
		//Iterate over all specified accounts, make sure that these
		// accounts exists in repository!
		log.debug("Verifying that the speicifed accounts in request exist in repository...");
		
		for (RequestAccount currReqAcct : areq.getAccounts()) {
			log.trace("Checking whether account to '" + currReqAcct.getAccountOperation() + "' with name: "+ currReqAcct.getAccountName()+ ", of resource: " + currReqAcct.getResourceName() + "' exists in repository or not...");
	
			if (1==1) {
//			if (!accountManager.isAccountExists(currReqAcct.getAccountName(), currReqAcct)) {
				throw new RequestCreationException("Cannot create request, account named: '"
					+ currReqAcct.getAccountName()
					+ ", on resource: '"
					+ currReqAcct.getResourceName() + "' for operation '" + currReqAcct.getAccountOperation()
					+ "' does not exist in repository!");
			}
			
			log.trace("Account exists!, continuing...");
			
		}
		
		log.debug("Successfully verified accounts existance, storing request...");
		
		em.persist(request);
		log.info("Successfully stored request!");
	}

	
	
	public CreateUserRequest submitRequest(CreateUserRequest request) throws RequestCreationException {
		log.info("Creation of 'Create User Request' has started for request ID: " + request.getRequestId());
		
		Collection<IdentityAttribute> activeIAs = identityAttributeManager.findAllActive();
		log.debug("Loaded all active Identity Attributes with amount: "+ activeIAs.size());
		log.debug("Iterating over the active Identity attributes, making sure each required IdentityAttribute is stored within the request");
		
		// Iterate over the IAs, if the iterated IA is required, then make
		// sure that the request holds that attribute, otherwise throw an exception
		for (IdentityAttribute currIA : activeIAs) {
			if (currIA.isRequiredInRequest()) {
				if (!request.isAttributeExistsByName(currIA.getUniqueName())) {
					throw new RequestCreationException(
						"A required IdentityAttribute named: '"
						+ currIA.getUniqueName().toUpperCase()
						+ "' does not exist in Request.");
				}
			}
		}
		
		log.debug("All required Identity Attributes were set in Request, validating the specified values...");
		
		
		//validate that all roles specified within the request exists in the repository
		log.debug("Validate that all requested roles exist in the repository...");
		for (RequestRole rr : request.getRequestRoles()) {
			Role loadedRole = roleManager.findRole(rr.getName());
			
			if (loadedRole == null) {
				String errMsg = "Failed to find a stored role in request with name '" + rr.getName() + "'in repository'";
				//oct-21-07(asaf): huh? request could not be created at all, no indication request process failure is needed as it tries to merge a non-existance request!
				//indicateRequestProcessFailure(request, errMsg);
				throw new RequestCreationException(errMsg);
			}
			
			log.trace("Continuing, role stored in request with name '" + rr.getName() + "' was found in repository!");
		}
		log.debug("Successfully validated all requested roles!");
		
		
		
		// Iterate over the attributes within the request and make sure it is
		// validated against the corresponding loaded IA.
		/*TODO support attribute validation
		for (RequestAttribute currRA : request.getAttributes()) {
			// Find the corresponding IA, if failed to find, skip the iterated attribute.
			for (IdentityAttribute currIA : activeIAs) {
				// Found a corresponding IA, do the validation.
				if (currRA.getUniqueName().equals(currIA.getUniqueName())) {
					
				}
			}
		}
		*/
		
		log.debug("Persisting request...");
		em.persist(request);
		em.flush();
		log.info("Successfully stored request!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!: " + request.getRequestId());
		
		return request;
	}
	
	
	
	
	
	
	
	
	
	
	
	public Request findRequest(Long requestId) {
		log.debug("Finding Request in repository forID '" + requestId + "'");

		return (Request)em.find(Request.class, requestId);
	}
	
	
	
	
	
	
	
	
	
	//PROCESS
	public void process(Request request) throws OperationException {
		if (request instanceof SuspendUserRequest) {
			SuspendUserRequest req = (SuspendUserRequest)request;
			process(req);
		} else if (request instanceof ResumeUserRequest) {
			ResumeUserRequest req = (ResumeUserRequest)request;
			process(req);
		} else if (request instanceof AccountsRequest) {
			AccountsRequest req = (AccountsRequest)request;
			process(req);
		} else if (request instanceof CreateUserRequest) {
			CreateUserRequest req = (CreateUserRequest)request;
				process(req);
		} else if (request instanceof SelfServiceAccessRequest) {
			SelfServiceAccessRequest req = (SelfServiceAccessRequest)request;
			process(req);
		} else if (request instanceof ModifyUserRolesRequest) {
			ModifyUserRolesRequest req = (ModifyUserRolesRequest)request;
			process(req);
		} else {
			//throw new OperationException("Request class '" + request.getClass().getName() + "' is not supported!");
			processGenericRequest(request);
		}
	}
	
	
	public void processGenericRequest(Request request) throws OperationException {
		indicateRequestProcessSuccess(request);
	}
	
	
	public void process(SuspendUserRequest request) throws OperationException {
		User user = userManager.findUser(request.getUserName());
		if (user == null) {
			throw new OperationException("Could not load user name '" + request.getUserName());
		}
		
		BulkTask bt = BulkTask.factory("Suspend User '" + user.getName() + "' and its association Accounts");
		
		//TODO: Support bulk suspend
		for (Account account : user.getAccounts()) {
			bt.addTask(resourceOperations.createSuspendAccountRequestTask(account));
		}

		request.getBulkTasks().add(bt);
		

		// Indicate that the user is disabled!
		user.setDisabled(true);
		em.merge(user);
		
		indicateRequestProcessSuccess(request);
	}

	
	
	public void process(ResumeUserRequest request) throws OperationException {
		User user = userManager.findUser(request.getUserName());
		if (user == null) {
			throw new OperationException("Could not load user name '" + request.getUserName());
		}
		
		BulkTask bt = BulkTask.factory("Resume User '" + user.getName() + "' and its association Accounts");
		
		//TODO: Support bulk suspend
		for (Account account : user.getAccounts()) {
			bt.addTask(resourceOperations.createResumeAccountRequestTask(account));
		}
		
		request.getBulkTasks().add(bt);
		

		// Indicate that the user is disabled!
		user.setDisabled(false);
		em.merge(user);
		
		indicateRequestProcessSuccess(request);
	}
	
	
	public void process(AccountsRequest request) throws OperationException {
		log.debug("Processing Accounts Request ID '" + request.getRequestId() + "' has started...");

		AccountsRequest accountsReq = (AccountsRequest) request;
		BulkTask bt = BulkTask.factory("Accounts Request modification");

		log.debug("Iterating over 'Accounts to Suspend' with accounts amount '"
			+ accountsReq.getAccountsToSuspend().size()
			+ "'");
	
		for (RequestAccount accountToSuspend : accountsReq.getAccountsToSuspend()) {
			Account loadedAccount = accountManager.findAccount(accountToSuspend.getAccountName(), accountToSuspend.getResourceName());
			
			/*
			if (loadedAccount == null) {
				throw new OperationException("Could not find account to suspend with name '" + accountToSuspend.getAccountName() + "', on resource '" + accountToSuspend.getResourceName() + "' in repository!");
			}
			*/
			//Make sure that the account was found!
			if (loadedAccount == null) {
				request.addLog("WARNING", "Account to suspend '" + accountToSuspend.getAccountName() + "', on resource '" +  accountToSuspend.getResourceName() + "' does not exist in repository, account deletion was skipped.","");
				continue;
			}
			
			try {
				SpmlTask task = resourceOperations.createSuspendAccountRequestTask(loadedAccount);
				task.setExpectedExecutionDate(accountToSuspend.getExpectedExecutionDate());	
				bt.addTask(task);
			} catch (OperationException ex) {
				indicateRequestProcessFailure(request, ex.getMessage());
				throw new OperationException(
					"Cannot approve request ID '"
					+ request.getRequestId()
					+ "', failed with message: " + ex);
			}
		}

		log.debug("Iterating over 'Accounts to Resume' with accounts amount '"
			+ accountsReq.getAccountsToResume().size() + "'");
	
		for (RequestAccount accountToResume : accountsReq.getAccountsToResume()) {
			Account loadedAccount = accountManager.findAccount(accountToResume.getAccountName(), accountToResume.getResourceName());

			/*
			if (loadedAccount == null) {
				throw new OperationException("Could not find account to resume with name '" + accountToResume.getAccountName() + "', on resource '" + accountToResume.getResourceName() + "' in repository!");
			}
			*/
			if (loadedAccount == null) {
				request.addLog("WARNING", "Account to resume '" + accountToResume.getAccountName() + "', on resource '" +  accountToResume.getResourceName() + "' does not exist in repository, account deletion was skipped.","");
				continue;
			}

			
			try {
				SpmlTask spmlTask = resourceOperations.createResumeAccountRequestTask(loadedAccount);
				spmlTask.setExpectedExecutionDate(accountToResume.getExpectedExecutionDate());
				bt.addTask(spmlTask);
			} catch (OperationException ex) {
				indicateRequestProcessFailure(request, ex.getMessage());
				throw new OperationException(
					"Cannot approve request ID '"
					+ request.getRequestId()
					+ "', failed with message: " + ex);
			}
		}

		log.debug("Iterating over 'Accounts to Delete' with accounts amount '"
			+ accountsReq.getAccountsToDelete().size()
			+ "'");
		
		for (RequestAccount accountToDelete : accountsReq.getAccountsToDelete()) {
			Account loadedAccount = accountManager.findAccount(
			accountToDelete.getAccountName(), accountToDelete.getResourceName());
		
			
			//Make sure that the account was found!
			if (loadedAccount == null) {
				request.addLog("WARNING", "Account to delete '" + accountToDelete.getAccountName() + "', on resource '" +  accountToDelete.getResourceName() + "' does not exist in repository, account deletion was skipped.","");
				continue;
			}
			
			log.debug("Loaded account to delete with ID: '"
				+ loadedAccount.getAccountId() + "', named '"
				+ loadedAccount.getName() + "', on resource: '"
				+ loadedAccount.getResource().getDisplayName()
				+ "'");

			try {
				//TODO: Support roles removal? is it safe just to delete accounts with roles assigned?
				if (accountsReq.isRevokeRelevantRolesIfAccountIsProtected()) {
					SpmlTask task = resourceOperations.createDeleteAccountRequestTask(loadedAccount, null,true);
					task.setExpectedExecutionDate(accountToDelete.getExpectedExecutionDate());
					bt.addTask(task);
				} else {
					SpmlTask task = resourceOperations.createDeleteAccountRequestTask(loadedAccount, null,false);
					task.setExpectedExecutionDate(accountToDelete.getExpectedExecutionDate());
					bt.addTask(task);
				}
			} catch (OperationException e) {
				indicateRequestProcessFailure(request, e.getMessage());
				throw e;
			}
		}

		// Associate bulkTask with request
		log.debug("Associating a bulk task that contains '*"+ bt.getTasks().size() + "'* tasks to the request.");
		accountsReq.getBulkTasks().add(bt);
		
		indicateRequestProcessSuccess(accountsReq);
	}
	
	
	
	public void process(CreateUserRequest request) throws OperationException {
		log.debug("Processing 'Create User' Request ID '" + request.getRequestId() + "' has started...");

		Set<Role> roles = new HashSet<Role>();
		// Make sure all specified roles exists first.
		CreateUserRequest cur = (CreateUserRequest) request;
		log.debug("Iterating over all roles assigned to request with amount '" + cur.getRequestRoles().size() + "', determining whether roles exists in repository or not...");
		for (RequestRole rr : cur.getRequestRoles()) {
			Role loadedRole = roleManager.findRole(rr.getName());
			
			if (loadedRole == null) {
				String errMsg = "Failed to find a stored role in request with name '" + rr.getName() + "'in repository'";
				indicateRequestProcessFailure(request, errMsg);
				throw new OperationException(errMsg);
			}
			
			if (request.isInheritedByPosition()) {
				loadedRole.setInherited(true);
			}
			
			roles.add(loadedRole);
			log.trace("Continuing, role stored in request with name '" + rr.getName() + "' was found in repository!");
		}

		
		try {
			User newUser = new User();
			
			log.debug("Factoring UserIdentityAttributes based on request attributes with amount '" + request.getAttributes().size() + "'");
			List<UserIdentityAttribute> uias = factoryUserIdentityAttriubtes(request.getAttributes(), newUser);
			log.debug("Factored User Identity Attributes based on request attributes with amount '" + uias.size() + "'");
			newUser.load(uias, request.getSuggestedUserName());
			newUser.setCreatedByRequest(true);
			newUser.addJournalingEntry(UserJournalingActionType.CREATED, request.getRequester(), "User is created by request", null);
			
			log.debug("Associating roles to the newly created user...");
			//request modifyRoles without persisting userRoleEntities as the user do not exist yet in DB
			//User+UserRoles will be persisted below by persist(user) (with userRoles persist cascade)
			//BulkTask bulkTask = roleManager.modifyRolesOfUserTasks(new HashSet<UserRole>(), roles, newUser, false);

			
			BulkTask btDirectRoles = roleManager.modifyRolesOfUserTasks(new HashSet<Role>(), roles, newUser);
			//BulkTask bulkTask = roleManager.modifyDirectUserRoles(new HashSet<Role>(), roles, newUser);
			
			log.debug("Factored all tasks relevant to roles associations with amount '" + btDirectRoles.getTasks().size() + "'");
			
			if (btDirectRoles.getTasks().size() > 0) {
				cur.getBulkTasks().add(btDirectRoles);
				btDirectRoles.setRequest(request);
			}
			//assign the roles to the user
			for (Role roleToAdd : roles) {
				UserRole userRole = new UserRole();
	            userRole.setCreationDate(new Date());
	            userRole.setRole(roleToAdd);
	            userRole.setUser(newUser);
	            userRole.setExpirationDate(roleToAdd.getExpirationDate());
	            userRole.setInherited(roleToAdd.isInherited());
	            userRole.setUser(newUser);
	            
	            newUser.getUserRoles().add(userRole);
			}
			
			//positions
			log.debug("Modifying user's position assignment...");
			BulkTask btPos = positionManager.modifyUserPositionsBulkTask(request.getRequestedPositions(), new HashSet<Position>(), newUser);
			btPos.setDescription("Modify user access by user position modifications...");

			if (btPos.getTasks().size() > 0) {
				btPos.setRequest(request);
				cur.getBulkTasks().add(btPos);
			}
			
			log.debug("Associating positions-> user entities in repository...");
			//assign the positions entities to the user
			//cannot be used as the user is still transient (new user is just created)
			//positionManager.associatePositionsToUserEntities(newUser, request.getRequestedPositions());
			
			for (Position currPos : request.getRequestedPositions()) {
				//currPos.getUsers().add(newUser);
				newUser.getPositions().add(currPos);
			}
			
			
			
			//everything worked so fine, then we shell persist the user in DB
			
			//make sure the user does not exist yet in repository
			//25-dec-07(god knows why, but 'catch (Exception e))' does not catch the following exception:
			//2007-12-25 22:39:22,401 DEBUG [velo.ejb.impl.EventBean] Invoking event definition responses for event name 'User Creation', with responses amount '0'
			//2007-12-25 22:39:22,571 WARN  [org.hibernate.util.JDBCExceptionReporter] SQL Error: 1062, SQLState: 23000
			//2007-12-25 22:39:22,571 ERROR [org.hibernate.util.JDBCExceptionReporter] Duplicate entry 'P03100033222' for key 2
			//2007-12-25 22:39:22,581 WARN  [org.hibernate.util.JDBCExceptionReporter] SQL Error: 0, SQLState: null
			//2007-12-25 22:39:22,581 ERROR [org.hibernate.util.JDBCExceptionReporter] Transaction is not active: tx=TransactionImple < ac, BasicAction: a0114dd:ee3:47715f3c:b43 status: ActionStatus.ABORT_ONLY >; - nested throwable: (javax.resource.ResourceException: Transaction is not active: tx=TransactionImple < ac, BasicAction: a0114dd:ee3:47715f3c:b43 status: ActionStatus.ABORT_ONLY >)
			//2007-12-25 22:39:22,581 ERROR [org.jboss.resource.adapter.jms.inflow.JmsServerSession] Unexpected error delivering message org.jboss.mq.SpyBytesMessage {
			if (userManager.isUserExit(newUser.getName())) {
				throw new OperationException("Cannot create user, user name '" + newUser.getName() + "' already exist in repository!");
			} else {
				userManager.persistUserEntity(newUser);
			}
		} catch (AttributeSetValueException e) {
			log.error("An error is occurd, dumping stacktrace...");
			e.printStackTrace();
			indicateRequestProcessFailure(request, e.getMessage());
			throw new OperationException(e.toString());
		} catch (ObjectNotFoundException e) {
			log.error("An error is occurd, dumping stacktrace...");
			e.printStackTrace();
			indicateRequestProcessFailure(request, e.getMessage());
			throw new OperationException(e.toString());
		} catch (ObjectFactoryException e) {
			log.error("An error is occurd, dumping stacktrace...");
			e.printStackTrace();
			indicateRequestProcessFailure(request, e.getMessage());
			throw new OperationException(e.toString());
		} catch (PersistEntityException e) {
			log.error("An error is occurd, dumping stacktrace...");
			e.printStackTrace();
			indicateRequestProcessFailure(request, e.getMessage());
			throw new OperationException(e.toString());
		//Loaduser is based on scripts (pluginID) that is groovy script and is not trusted
		//Move its execution through the standard ScriptEngine and not directly execute its code 
		} catch (Exception e) {
			log.error("An error is occurd, dumping stacktrace...");
			e.printStackTrace();
			indicateRequestProcessFailure(request, e.getMessage());
			throw new OperationException(e.toString());
		}
		
		
		//will persist request too
		indicateRequestProcessSuccess(cur);
	}		
		
	
	public void process(SelfServiceAccessRequest request) throws OperationException {
		log.debug("Processing 'Self Service' Request ID '" + request.getRequestId() + "' has started...");
		//SelfServiceAccessRequest ssr = (SelfServiceAccessRequest) request;
		
		
		Role role = request.getRole();
		
		//handle grant access date
		//if 'immediate' flag was set, then the user should get its access when the request is processed(now)
		if (request.isGrantAccessImmediately()) {
			log.debug("Access flagged to be granted immediately...!");
			role.setGrantAccessDate(new Date());
		} else {
			log.debug("Access flagged NOT to be granted immediately..., access will be added at: " + request.getGrantAccessDate());
			role.setGrantAccessDate(request.getGrantAccessDate());
		}
		
		
		role.setExpirationDate(request.getExpirationAccessDate());
		
		
		//perform role modification for user
		//roleManager.associateRoleToUser(role, request.getRequestedAccessForUser(), request.getGrantAccessDate());
		Set<Role> roles = new HashSet<Role>();
		roles.add(role);
		roleManager.modifyDirectUserRoles(new HashSet<Role>(),roles,request.getRequestedAccessForUser());

		//will persist request too
		indicateRequestProcessSuccess(request);
	}

	
	public void process(ModifyUserRolesRequest request) throws OperationException {
		log.debug("Processing 'Modify User Roles' Request ID '" + request.getRequestId() + "' has started...");
		
		Set<Position> posToAddSet = new HashSet<Position>();
		Set<Position> posToRevokeSet = new HashSet<Position>();
		for (RequestedPosition currRP : request.getRequestedPositionsToAssign()) {
			posToAddSet.add(currRP.getPosition());
		}
		for (RequestedPosition currRP : request.getRequestedPositionsToRevoke()) {
			posToRevokeSet.add(currRP.getPosition());
		}
		
		User user = userManager.findUser(request.getUserName());
		
		if (user == null) {
			throw new OperationException("User with name '" + request.getUserName() + "' could not be found in repository");
		}
		
		BulkTask btPos = positionManager.modifyUserPositionsBulkTask(posToAddSet, posToRevokeSet, user);
		btPos.setDescription("Modify user access by user position modifications...");
		
		//must both sides
		btPos.setRequest(request);
		request.getBulkTasks().add(btPos);
		
		
		//associate positions to user entity
		positionManager.modifyUserPositionAssignmentEntities(user, posToAddSet,posToRevokeSet);
		
		
		//success :)
		indicateRequestProcessSuccess(request);
	}
	
	

	
	
	
	
	private List<UserIdentityAttribute> factoryUserIdentityAttriubtes(List<RequestAttribute> requestAttributes, User user) throws AttributeSetValueException,ObjectNotFoundException {
		List<UserIdentityAttribute> uias = new ArrayList<UserIdentityAttribute>();
		
		for (RequestAttribute currReqAttr : requestAttributes) {
			IdentityAttribute ia = identityAttributeManager.findIdentityAttribute(currReqAttr.getUniqueName());
			
			//make sure ia was found, otherwise throw an exception
			if (ia == null) {
				//throw new ObjectNotFoundException("Could not find Identity Attribute Definition with name '" + currReqAttr.getUniqueName() + "' that was specified as a requested attribute!");
				log.warn("Could not find Identity Attribute Definition with name '" + currReqAttr.getUniqueName() + "' that was specified as a requested attribute, skipping attribute!");
				continue;
			}
			
			UserIdentityAttribute uia = UserIdentityAttribute.factory();
			uia.setIdentityAttribute(ia);
			uia.importValues(currReqAttr);
			uia.setUser(user);
			uias.add(uia);
		}
		
		return uias;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void finalApproveRequest(Request request, User approver) throws OperationException {
		// Make sure that the status is the correct typed
		if (request.getStatus() != RequestStatus.PENDING_APPROVAL) {
			throw new OperationException("Cannot approve request ID '"
			+ request.getRequestId() + "' with status '"
			+ request.getStatus() + "'");
		}
		
		if (request.isInProcess()) {
			throw new OperationException("Cannot approve request in process for request ID '"
				+ request.getRequestId() + "'");
		}
		
		if (request.isProcessed()) {
			throw new OperationException(
				"Cannot approve request that was already processed for request ID '"
				+ request.getRequestId() + "'");
		}
		
		request.addLog("INFO", "Request final approval by user name '"
				+ approver.getName() + "'", null);
		updateRequestStatus(request, RequestStatus.APPROVED);
	}

		
	public void finalRejectRequest(Request request, User rejecter) {
		request.addLog("INFO", "Request final rejection by user name '"
				+ rejecter.getName() + "'", null);
		
		updateRequestStatus(request, RequestStatus.REJECTED);
		mergeRequestEntity(request);
	}
	
	
	
	//HELPER
	private void indicateRequestProcessSuccess(Request request) {
		request.addLog("INFO", "Request succesfully processed!", null);

		// Indicate that the request was processed.
		request.setProcessed(true);
		request.setSuccessfullyProcessed(true);
		request.setProcessedTime(new Date());
		request.setInProcess(false);

		// Merge the request to the DB, with all tasks and changes!
		mergeRequestEntity(request);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Request scanner
	public void changeRequestScannerMode() throws OperationException {
		boolean isRequestScannerEnabled = SysConf.getSysConf().getBoolean(
			"requests.activate_requests_processor_scanner");

		log.info("Requested to change Request Scanner Mode...");

		if (timerService.getTimers().size() > 0) {
			log.info("Current Request Scanner mode is enabled, found '"
					+ timerService.getTimers().size()
					+ " timers..., clearing...");
			Iterator timersIt = timerService.getTimers().iterator();
			while (timersIt.hasNext()) {
				Timer currTimer = (Timer) timersIt.next();
				log.trace("Found timer with info: '" + currTimer.getInfo()
						+ "', object: '" + currTimer + "'");
				currTimer.cancel();
			}
		} else {
			log.info("Current Request Scanner mode is disabled, found '"
					+ timerService.getTimers().size()
					+ " timers..., adding timer...");
			int interval = SysConf.getSysConf().getInt(
					"requests.scanner_interval_in_seconds");
			
			//make sure it's allowed to create the scanner
			if (!isRequestScannerEnabled) {
				throw new OperationException(
						"Cannot enable the scanner since Request Scanner mode is disabled in system configuration!");
			} else {
				createTimerScanner(interval, interval);
			}
		}
	}

	public boolean isRequestScannerActivate() {
		if (timerService.getTimers().size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public void createTimerScanner(long initialDuration, long intervalDuration) {
		// Calculating miliseconds from the specified parameters as seconds
		long msInitialDuration = initialDuration * 1000;
		long msIntervalDuration = intervalDuration * 1000;

		cancelTimers();
		// Timer timer =
		// timerService.createTimer(msInitialDuration,msIntervalDuration,
		// "Created new timer with interval of: " + msIntervalDuration + "
		// seconds.");

		sessionContext.getTimerService().createTimer(msInitialDuration,
				msInitialDuration, "requests-scanner");
		System.out
				.println("Created Request-Scanner timer over a session context object: "
						+ sessionContext);
	}
	
	
	//not in interface
	@Timeout
	public synchronized void scanRequestsForExecution(Timer timer) {
		log.info("Requests scanner executed over time: '" + timer
				+ "', remaining time until next execution: "
				+ timer.getTimeRemaining());
		//TODO: JBOSS - FIX!!!!
		//int requestProcessingAmountPerScan = SysConf.getSysConf().getInt("requests.requests_processing_amount_per_scan");
		int requestProcessingAmountPerScan = 1;
		
		//TODO: JBOSS - FIX!!!!
		/*int intervalToNextPossibleRequestProcessingInSeconds = SysConf
				.getSysConf()
				.getInt(
						"requests.interval_to_next_possible_request_processing_in_seconds");*/
		
		int intervalToNextPossibleRequestProcessingInSeconds = 40;

		
		
		
		// Make sure that the maximum requests to process is not already
		// reached.
		boolean inLimitConcurrentRequestsInProcess = false;
		Long currentRequestsInProcess = getRequestsNumberInProcess();
		log.info("Allowed requests in process '"
				+ requestProcessingAmountPerScan
				+ "', Current requests in process '" + currentRequestsInProcess
				+ "'");
		if (currentRequestsInProcess >= requestProcessingAmountPerScan) {
			inLimitConcurrentRequestsInProcess = true;
			log
					.info("Nothing to do, limit of concurrent requests execution has reached!");
		}

		boolean waitingIntervalPassedToProcessRequests = false;
		// If limit of current processing requests is not reached, make sure it
		// is allowed to perform execution due to time limitations
		if (!inLimitConcurrentRequestsInProcess) {
			try {
				velo.entity.Request loadedLastProcessedRequest = findLastProcessedRequest();

				long msOfLastRequest = loadedLastProcessedRequest
						.getProcessedTime().getTime();
				msOfLastRequest += intervalToNextPossibleRequestProcessingInSeconds * 1000;
				Date allowedTimeExecution = new Date(msOfLastRequest);

				if (!allowedTimeExecution.before(new Date())) {
					log
							.info("Cannot process requests since interval to the next possible request processing is not reached!");
					waitingIntervalPassedToProcessRequests = false;
				} else {
					waitingIntervalPassedToProcessRequests = true;
				}
			} catch (NoResultFoundException ex) {
				// Could not find any request that were processed, probably a
				// clean request table, then, meaning requests processing is
				// allowed!
				waitingIntervalPassedToProcessRequests = true;
			}
		}

		if ((!inLimitConcurrentRequestsInProcess)
				&& (waitingIntervalPassedToProcessRequests)) {
			// Collection<Request> requests =
			// loadRequestsToProcess(requestProcessingAmountPerScan);
			Query requestsToProcessQuery = em
					.createNamedQuery("request.findRequestsToProcess");
			requestsToProcessQuery.setMaxResults(requestProcessingAmountPerScan
					- currentRequestsInProcess.intValue());
			// requestsToProcessQuery.setParameter("maxAmount", amount);
			Collection<Request> requests = requestsToProcessQuery
					.getResultList();

			boolean foundRequestsToProcess = false;
			if (requests.size() < 1) {
				log.info("No requests were found to process...");
			} else {
				log.info("Processing '" + requests.size()
						+ "' in current scan...");
				foundRequestsToProcess = true;
			}

			if (foundRequestsToProcess) {
				lockRequests(requests);

				for (Request currRequestToProcess : requests) {
					/* try { */
					log.debug("Sending request ID '"
							+ currRequestToProcess.getRequestId()
							+ " to the processing process...!");
					sendRequestToJmsQueueForProcessing(currRequestToProcess);
					// processRequest(currRequestToProcess);
				}
			} else {
				log.debug("No requests were found to process at this scan.");
			}
		}
	}
	
	
	
	private void sendRequestToJmsQueueForProcessing(Request requestToProcess) {
		try {
			
			Queue queue = null;
            QueueConnection connection = null;
            QueueSession session = null;
            MessageProducer messageProducer = null;
            
            
            InitialContext ctx = new InitialContext();
            queue = (Queue) ctx.lookup("queue/velo/RequestsDefaultQueue");
            QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
            connection = factory.createQueueConnection();
            session = connection.createQueueSession(false,QueueSession.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(queue);
            
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
			
			BytesMessage byteMsg = session.createBytesMessage();
			byteMsg.setLongProperty("requestId", requestToProcess.getRequestId());
			messageProducer.send(byteMsg);
			
			log.debug("Sent Request ID '" + requestToProcess.getRequestId()+ "' to JMS queue for processing!");

			
			
			
			// Close the sender/session/connection to the queue
			messageProducer.close();
            session.close();
            connection.close();
            
		} catch (Exception e) {
			// Could not send request to JMS queue, log the error.
			String errMsg = "Could not approve request ID '"
					+ requestToProcess.getRequestId() + "' due to: '"
					+ e.getMessage();
			log.error(errMsg);
			requestToProcess.addLog("ERROR",
					"Error while trying to process request", errMsg);
			mergeRequestEntity(requestToProcess);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public void mergeRequestEntity(Request request) {
		em.merge(request);
	}

	public Request createRequestEntity(Request request)
			throws RequestCreationException {
		log.info("Creating Request Entity...");
		// Make sure that all required IdentityAttributes were specified within
		// the request attributes.
		Collection<IdentityAttribute> identityAttributes = identityAttributeManager
				.findAllActiveIdentityAttributes();

		

		// --MODIFY USER ROLES REQUEST-- VALIDATE ROLES EXISTANCE!
		if (request instanceof ModifyUserRolesRequest) {
			ModifyUserRolesRequest murr = (ModifyUserRolesRequest) request;
			for (RequestRole currRR : murr.getRolesToAssign()) {
				if (!roleManager.isRoleExit(currRR.getName())) {
					throw new RequestCreationException(
							"Role named '"
									+ currRR.getName()
									+ "' flagged to be assigned but does not exist in repository!");
				}
			}

			for (RequestRole currRR : murr.getRolesToRevoke()) {
				if (!roleManager.isRoleExit(currRR.getName())) {
					throw new RequestCreationException(
							"Role named '"
									+ currRR.getName()
									+ "' flagged to be revoked but does not exist in repository!");
				}
			}
		}


		

		log.info("Validated values, persisting the request in the database");
		// Everything is okay, persist the request.
		em.persist(request);

		log.info("Successfully created request!");
		return request;
	}

	/*
	public void removeRequestEntity(Request request) {
		Request mergedEntity = em.merge(request);
		em.remove(mergedEntity);
	}
	*/




	/**
	 * Prepare the actions to perform on the trailed reuqest object Execute the
	 * actions if prepared actions successfully
	 * 
	 * @param rolem
	 *            The local RoleManager EJB interface
	 */
	@Deprecated
	public void executeRequest(Request request)
			throws RequestExecutionException {
		log.info("Executing request ID: " + request.getRequestId());

		/*
		 * //This should never occure but make sure the request was approved,
		 * otherwise through an exception if (!request.isApproved()) { throw new
		 * RequestExecutionException("Cannot execute request, the request was
		 * not approved yet!"); }
		 * 
		 * //Prepare the trailedRequest within the trailed request to perform
		 * try { request.getTrailedRequest().prepare(rolem);
		 * 
		 * //Make sure the user entity was set into request before execution if
		 * (request.getUser() == null) { throw new
		 * RequestExecutionException("User to perform the request on must be set
		 * in the request before proceeding with request execution!"); }
		 * 
		 * 
		 * Iterator<resourceActionInterface> it =
		 * request.getTrailedRequest().getAllresourceActionsForExecution().iterator();
		 * System.out.println("Executing actions in request with total number of
		 * actions: " +
		 * request.getTrailedRequest().getAllresourceActionsForExecution().size());
		 * while (it.hasNext()) { resourceActionInterface currAction =
		 * it.next(); System.out.println("Executing action on target system: " +
		 * currAction.getClass().getName()); try { currAction.__execute__(); }
		 * catch(ActionFailureException afe) { //TODO: Request must has some
		 * type of trace of what actions were failed //TODO: Failed actions
		 * should get into some 'action queue' for re-initialization
		 * System.out.println("Action was failed while executing Request,
		 * skipping action, msg: " + afe.getMessage()); } } } catch
		 * (FactoryresourceActionsForRoleException ftsafre) { throw new
		 * RequestExecutionException(ftsafre.getMessage()); }
		 */
	}

	public void processRequest(Request request) throws OperationException {
		log.info("Processing Request ID: " + request.getRequestId()
				+ "has started...");
		// request.setInProcess(true);
		// em.merge(request);
		// em.flush();
		// request.setStatus(RequestStatus.APPROVED);

		// TODO: Handle all other request types.
		if (request instanceof CreateUserRequest) {
			log
					.info("Found request of type 'Create User Request', performing request approval");
			try {
				// Make sure all specified roles exists first.
				CreateUserRequest cur = (CreateUserRequest) request;
				for (RequestRole rr : cur.getRequestRoles()) {
					try {
						roleManager.findRole(rr.getName());
					}
					// Does not work, goddamn catch
					// (javax.persistence.NoResultException nre) {
					catch (Exception e) {
						String errMsg = "Cannot process reuqest, failed to fetch role named: '"
								+ rr.getName()
								+ "' due to: '"
								+ e.getMessage()
								+ "'";
						indicateRequestProcessFailure(request, errMsg);
						throw new OperationException(errMsg);
					}
				}

				CreateUserRequest createUserReq = (CreateUserRequest) request;

				// CREATE A NEW USER.
//				User newUser = userManager.createUserFromRequest(createUserReq);

				Set<String> roleNames = new HashSet<String>();
				for (RequestRole currRR : createUserReq
						.getRequestRoles()) {
					roleNames.add(currRR.getName());
				}

				try {
					Set<Role> roles = roleManager.loadRolesByNames(roleNames);

					for (Role currRole : roles) {
						currRole.setInherited(createUserReq
								.isInheritedByPosition());
					}

					// Now fetch bulk tasks of adding roles to user, and
					// associate them to the request
					// List<BulkTask> rolesBulkTasks =
					// (List<BulkTask>)rolem.addRolesByRolesNamesToUserBulkTaskList(roleNames,
					// newUser, isDirect);
					log.debug("Adding '" + roles.size()
							+ "' roles to user...");
					//BulkTask bulkTask = roleManager.modifyRolesOfUserTasks(new HashSet<UserRole>(), roles, newUser, true);
//					BulkTask bulkTask = roleManager.modifyRolesOfUserTasks(new HashSet<Role>(), roles, newUser);
					
					
					//PERSIST THE ENTITIES!@#$!@#$!@#$!@#$!@#$!@#$!@#$
					
					
//					bulkTask.setRequest(request);
//					request.getBulkTasks().add(bulkTask);

				} catch (LoadingObjectsException loe) {
					indicateRequestProcessFailure(request, loe.getMessage());
					throw new OperationException(loe);
				}

			} catch (OperationException oe) {
				throw (oe);
			}

		} else if (request instanceof DeleteUserRequest) {
			log
					.info("Found request of type 'Delete User Request', performing request approval");
			try {
				DeleteUserRequest deleteUserReq = (DeleteUserRequest) request;
				User loadedUser = userManager.findUserByName(deleteUserReq
						.getUserName());
//				BulkTask bt = userManager.deleteUserBulkTask(loadedUser);

//				request.getBulkTasks().add(bt);

			} catch (NoResultFoundException nrfe) {
				indicateRequestProcessFailure(request, nrfe.getMessage());
				throw new OperationException(nrfe.getMessage());
			}

		} else if (request instanceof ModifyUserRolesRequest) {
			log
					.info("Found request of type 'Modify User Roles Request', performing request approval");
			try {
				ModifyUserRolesRequest murr = (ModifyUserRolesRequest) request;
				User user = userManager.findUserByName(murr.getUserName());

				Set<Role> rolesToAdd = new HashSet<Role>();
				for (RequestRole currRR : murr.getRolesToAssign()) {
					Role loadedRole = roleManager.findRole(currRR
							.getName());
					rolesToAdd.add(loadedRole);
				}

				/*
				Set<UserRole> userRolesToRemove = new HashSet<UserRole>();
				for (RequestRole currRR : murr.getRolesToRevoke()) {
					Role loadedRole = roleManager.findRole(currRR
							.getName());
					UserRole loadedUserRole = roleManager.findUserRole(user,
							loadedRole);
					userRolesToRemove.add(loadedUserRole);
				}
				*/
				Set<Role> userRolesToRemove = new HashSet<Role>();
				for (RequestRole currRR : murr.getRolesToRevoke()) {
					Role loadedRole = roleManager.findRole(currRR.getName());
					UserRole loadedUserRole = roleManager.findUserRole(user,loadedRole);
					userRolesToRemove.add(loadedUserRole.getRole());
				}
				

				//BulkTask bt = roleManager.modifyRolesOfUserTasks(userRolesToRemove, rolesToAdd, user, true);
				BulkTask bt = roleManager.modifyRolesOfUserTasks(userRolesToRemove, rolesToAdd, user);
				//PERSIST THE ENTITIES!@#$!@#$!@#$!@#$!@#$!@#$!@#$
				//HANDLE 'roleManager.findUserRole' correctly!
				
				log
						.trace("Created modify roles bulk task of 'Create User from Request' process with size of tasks: '"
								+ bt.getTasks().size() + "'");
				bt.setRequest(murr);
				murr.getBulkTasks().add(bt);

			} catch (NoResultFoundException ex) {
				indicateRequestProcessFailure(request, ex.getMessage());
				throw new OperationException(ex);
			}
		} 

		indicateRequestProcessSuccess(request);

		log.info("Successfully processed request ID '"
				+ request.getRequestId() + "' and created relevant tasks!");
	}



	/*
	 * public Request factoryRequest(User requester, String notes) { Request
	 * request = new Request();
	 * 
	 * request.initRequest(requester, notes); request.initEvents();
	 * 
	 * return request; }
	 */

	public void updateRequestStatus(Request request, RequestStatus newStatus) {
		User user;
		request.setStatus(newStatus);
		
		log.trace("Started method updateRequestStatus");
		
//		EventDefinition ed = eventManager.find(eventRequestStatusModification);
		//make sure that the event was found, otherwise throw an exception
//		if (ed == null) {
//			log.error("Could not find event definition '" + eventRequestStatusModification + "', skipping event response invocations...");
//			return;
//		}
		OperationContext context = new OperationContext();
		
		System.out.println ("The request is an instance of class " + request.getClass().toString());
		if(request instanceof AccountsRequest) {
			AccountsRequest r = (AccountsRequest)request;
			user = userManager.findUser(r.getUserName());
			context.addVar("user", user);
			context.addVar("userName", user.getName());
			context.addVar("userIdAttrs", user.getUserIdentityAttributesAsMap());
			context.addVar("request", r);
		}
		else if(request instanceof ModifyUserRolesRequest) {
			ModifyUserRolesRequest r = (ModifyUserRolesRequest)request;
			user = userManager.findUser(r.getUserName());
			context.addVar("user", user);
			context.addVar("userName", user.getName());
			context.addVar("userIdAttrs", user.getUserIdentityAttributesAsMap());
			context.addVar("request", r);
		}	
			
//		try {
//			eventManager.invokeEvent(ed, context);
//		}
//		catch(ScriptInvocationException sie){
//			log.error("The ScriptInvocationException has occured " + sie.getMessage());
//		}
		
		em.merge(request);
		
		
		
		
		
		
		
		
//		request.setStatus(newStatus);

		//try {
			// Trigger event
//			EventDefinition ed = eventManager.find(eventRequestStatusModification);
			
			//TODO: FIX!
			/*JB!!! fix!
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("request", request);
			eventManager.createEventResponsesOfEventDefinition(ed, properties);
		} catch (NoResultFoundException nrfe) {
			log
					.warn("Warning, could not create an event named: '"
							+ eventRequestStatusModification
							+ "' since the event does not exist by the specified name!");
		} catch (EventResponseException ex) {
			// TODO: Replace with throwing an exception, events integrity is
			// very important!
			log.warn("Could not fire events due to: " + ex);
		}
		*/

//		em.merge(request);
	}

	
	
	public Collection<Request> loadRequestsToProcess(int amount) {
		Query requestsToProcessQuery = em
				.createNamedQuery("request.findRequestsToProcess");
		requestsToProcessQuery.setMaxResults(amount);
		// requestsToProcessQuery.setParameter("maxAmount", amount);

		Collection<Request> requests = requestsToProcessQuery.getResultList();

		return requests;
	}

	

	public void createTimerScanner() throws OperationException {
		boolean isRequestsScannerEnabled = SysConf.getSysConf().getBoolean(
				"requests.activate_requests_processor_scanner");

		if (isRequestsScannerEnabled) {
			long intervalRequests = SysConf.getSysConf().getInt(
					"requests.scanner_interval_in_seconds");
			// System.out.println("Starting Requests-Scanner, Firing the
			// requests scanner each '" + interval + "' seconds");
			log
					.info("Starting Requests-Scanner, Firing the requests scanner each '"
							+ intervalRequests + "' seconds");
			createTimerScanner(intervalRequests, intervalRequests);
		} else {
			throw new OperationException(
					"Cannot start Request Scanner since it is disabled in system configuration!");
		}

	}


	// if (waitingIntervalPassedToProcessRequests) {
	// log.info("Found '" + requests.size() + "' to execute, processing
	// requests...");

	/*
	 * //Lock the requests, indicate them as they are currently in process...
	 * for (Request currRequest : requests) { currRequest.setInProcess(true);
	 * em.merge(currRequest); em.flush();
	 * 
	 * 
	 * em.refresh(currRequest); if (currRequest.isInProcess()) {
	 * System.out.println("GOOD!"); } else {
	 * System.out.println("BAD!!!!!!!!!!!!!!!"); }
	 * //mergeRequestEntity(currRequest); }
	 */

	// Requests processing takes some time, Make sure data is flushed to the DB
	// now,
	// so next scan won't accidently try to re-process the request.
	// em.flush();

	/*
	 * for (Request currRequestToProcess : requests) { //try {
	 * log.info("Sending request ID '" + currRequestToProcess.getRequestId() + "
	 * to the processing process...!"); //processRequest(currRequestToProcess);
	 * sendRequestToJmsQueueForProcessing(currRequestToProcess); }
	 */
	/*
	 * catch (OperationException ex) { String errMsg = "Could not approve
	 * request ID '" + currRequestToProcess.getRequestId() + "' due to: '" +
	 * ex.getMessage(); log.severe(errMsg);
	 * currRequestToProcess.addLog("ERROR", "Error while trying to process
	 * request", errMsg); mergeRequestEntity(currRequestToProcess); }
	 */
	// }
	// }
	

	// Helper Classes
	private Request findLastProcessedRequest() throws NoResultFoundException {
		try {
			Request loadedLastProcessedRequest = (Request) em.createNamedQuery(
					"request.findLastProcessedRequest").setMaxResults(1)
					.getSingleResult();
			return loadedLastProcessedRequest;
		} catch (Exception e) {
			throw new NoResultFoundException(
					"Could not find last processed request in repository!");
		}
	}

	private void lockRequests(Collection<Request> requests) {
		log.debug("Locking requests with amount '" + requests.size() + "'");
		
		for (Request currRequest : requests) {
			currRequest.setInProcess(true);
			mergeRequestEntity(currRequest);
			log.trace("Locked request ID: " + currRequest.getRequestId());
		}

		log.trace("Flushing as lock must occure now!");
		em.flush();
		log.trace("flushed!");
	}

	private Long getRequestsNumberInProcess() {
		Query q = em.createNamedQuery("request.countRequestsInProcess");
		Long num = (Long) q.getSingleResult();

		return num;
	}

	private void indicateRequestProcessFailure(Request request, String errMsg) {
		request.addLog("FAILURE", "Error while trying to process request!",errMsg);

		request.setProcessed(true);
		request.setSuccessfullyProcessed(false);
		request.setProcessedTime(new Date());
		request.setInProcess(false);

		// Merge the request to the DB, with all tasks and changes!
		mergeRequestEntity(request);
	}


	public void cancelTimers() {
		for (Object currTimerObj : sessionContext.getTimerService().getTimers()) {
			Timer currTimer = (Timer) currTimerObj;
			currTimer.cancel();
		}
	}
	
}
