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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.management.PasswordHash;

import velo.common.SysConf;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.ConfManagerLocal;
import velo.ejb.interfaces.ConfManagerRemote;
import velo.ejb.interfaces.PasswordManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.encryption.EncryptionUtils;
import velo.entity.ActionLanguage;
import velo.entity.BaseEntity;
import velo.entity.Capability;
import velo.entity.CapabilityFolder;
import velo.entity.IdentityAttribute;
import velo.entity.IdentityAttributesGroup;
import velo.entity.PasswordPolicy;
import velo.entity.PasswordPolicyContainer;
import velo.entity.ReadyAction;
import velo.entity.ReconcileAuditPolicy;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcilePolicy;
import velo.entity.Resource;
import velo.entity.ResourceAdmin;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGlobalOperation;
import velo.entity.ResourceType;
import velo.entity.ResourceTypeAttribute;
import velo.entity.ResourceTypeOperation;
import velo.entity.SystemEvent;
import velo.entity.User;
import velo.entity.UserContainer;
import velo.entity.Attribute.AttributeDataTypes;
import velo.entity.GuiAttribute.AttributeVisualRenderingType;
import velo.entity.IdentityAttribute.IdentityAttributeSources;
import velo.entity.ResourceAttributeBase.SourceTypes;
import velo.entity.ResourceType.ResourceControllerType;
import velo.entity.annotations.UniqueColumnForSync;
import velo.exceptions.OperationException;
import velo.exceptions.SynchronizationException;
import velo.tools.FileUtils;

/**
 * A Stateless EJB bean for managing general configuration stuff
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
@Name("confBean")
public class ConfBean implements ConfManagerLocal, ConfManagerRemote {
	private static Logger log = Logger.getLogger(ConfBean.class.getName());

	public enum objectType {
		RESOURCE_TYPE
	}

	Map<String,ResourceGlobalOperation> loadedResourceGlobalOperations;


	private Map<Class,String> entityUniqueKeyVars = new HashMap<Class,String>();
	public Map<String,ResourceType> resourceTypes = new LinkedHashMap<String,ResourceType>();

	public Set<Object> capabilities = new LinkedHashSet<Object>();
	public Set<Object> capabilityFolders = new LinkedHashSet<Object>();
	public Set<Object> identityAttributes = new LinkedHashSet<Object>();
	public Set<Object> identityAttributesGroups = new LinkedHashSet<Object>();
	public Set<Object> taskDefinitions = new LinkedHashSet<Object>();
	public Set<Object> passwordPolicies = new LinkedHashSet<Object>();
	public Set<Object> passwordPolicyContainers = new LinkedHashSet<Object>();
	public Set<Object> systemEvents = new LinkedHashSet<Object>();
	public Set<Object> users = new LinkedHashSet<Object>();
	public Set<Object> actionLanguages = new HashSet<Object>();
	public Set<Object> userContainers = new HashSet<Object>();
	public Set<Object> resourceTypeAttributes = new HashSet<Object>();
	public Set<Object> resourceGlobalOperations = new HashSet<Object>();
	//reconcile related
	public Set<Object> reconcileTargetPolicies = new LinkedHashSet<Object>();	
	public Set<Object> reconcileEvents = new LinkedHashSet<Object>();
	//actions
	public Set<Object> readyActions = new LinkedHashSet<Object>();
	
	

	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	/**
	 * Inject the User Bean
	 */
	@EJB
	UserManagerLocal userm;

	/**
	 * Inject the resourceBean
	 */
	@EJB
	ResourceManagerLocal resourceManager;

	@EJB
	ResourceAttributeManagerLocal tsam;

	@EJB
	TaskManagerLocal taskManager;

	@EJB
	CommonUtilsManagerLocal cum;

	@EJB
	TaskManagerLocal tm;

	@EJB
	ResourceGroupManagerLocal tsgm;
	
	@EJB
	PasswordManagerLocal passwordManager;
	
	@EJB
	ReconcileManagerLocal reconcileManager;

	/**
	 * 
	 * A Logger object for logging messages related to this class
	 */
	private static Logger logger = Logger.getLogger(ConfBean.class.getName());


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	







	/*
	public void generateUsersLocalPasswordsEncryptionKey() throws OperationException {
		String fileName = SysConf.getSysConf().getString(
		"system.directory.system_conf")
		+ "/"
		+ keyDir
		+ "/"
		+ SysConf.getSysConf().getString(
		"system.files.users_encryption_key");
		
		log.debug("Key file name to be generated: " + fileName);
		String keyString = EncryptionUtils.generateKey();

		File f = new File(fileName);
		if (!f.isFile()) {
			try {
				f.createNewFile();
				FileUtils.setContents(f, keyString);
			} catch (IOException ex) {
				throw new OperationException(
						"Could not create key generation file for file name: '"
						+ fileName + "', due to: " + ex);
			}
		} else {
			throw new OperationException("Could not create key generation file for file name: '" + fileName + "', file already exists.");
		}
		
		log.info("Successfully generated users local passwords encryption key.");
	}
	*/









	//TODO This is a bad way :/
	public boolean isInitialDataImported() {
		Query q = em.createNamedQuery("resourceType.findAll");

		return q.getResultList().size() > 0;
	}


	public void persistInitialtData() {
		//TODO: Generic initInitialData, Should splitted to inidividual initals
		initInitialData();

		/*
		 * Unneccessary, persisted automatically by the admin folder that
		 * contains all capabilities //Create capabilities for (Capability
		 * currCap : capabilities) { em.persist(currCap); }
		 */

		//also persist all identity attributes
		persistEntities(identityAttributesGroups);
		em.flush();
		// Create capability folders(persist all capabilities too)
		persistEntities(resourceGlobalOperations);
		em.flush();
		persistEntities(resourceTypes.values());
		em.flush();
		persistEntities(capabilityFolders);
		em.flush();
		persistEntities(reconcileTargetPolicies);
		em.flush();
		persistEntities(taskDefinitions);
		em.flush();
		persistEntities(passwordPolicies);
		em.flush();
		persistEntities(passwordPolicyContainers);
		em.flush();
		persistEntities(users);
		em.flush();
		persistEntities(userContainers);
		em.flush();
		persistEntities(actionLanguages);
		em.flush();
		persistEntities(reconcileEvents);
		em.flush();
		
		
		
		//--------------------------------------//
		//This is the new method for synchronizing amount of entities that can be changed often
		syncReconcileEvents();
		em.flush();
		syncSystemEvents();
		em.flush();
		syncReadyActions();
		em.flush();
		
		
		
		//TODO: generate keys
	}

	public Map<String,ResourceGlobalOperation> getLoadedResourceGlobalOperations() {
		if (loadedResourceGlobalOperations == null) {
			List<ResourceGlobalOperation> list = em.createQuery("SELECT rgo FROM ResourceGlobalOperation rgo").getResultList();

			loadedResourceGlobalOperations = new HashMap<String,ResourceGlobalOperation>();
			for (ResourceGlobalOperation rgo : list) {
				loadedResourceGlobalOperations.put(rgo.getUniqueName(), rgo);
			}

			return loadedResourceGlobalOperations;
		} else {
			return loadedResourceGlobalOperations;
		}
	}



	/*
	 * public void syncCapabilities() { initInitialData(); /* for (Capability
	 * currCap : capabilities) { if
	 * (capabilityManager.isCapabilityExists(currCap.getCapabilityId())) {
	 * System.out.println("Capability named '" + currCap.getName() + "' already
	 * exists, skipping..."); } else { System.out.println("Adding capability
	 * named '" + currCap.getName() + "'"); em.persist(currCap); } }
	 */
	// }
	/*
	 * public void syncTaskDefinitions() { initInitialData(); /* for
	 * (TaskDefinition currTaskDef : taskDefinitions) { if
	 * (taskManager.isTaskDefinitionExists(currTaskDef.getTaskDefinitionId())) {
	 * System.out.println("Task Definition named '" + currTaskDef.getName() + "'
	 * already exists, skipping..."); } else { System.out.println("Adding
	 * capability named '" + currTaskDef.getName() + "'");
	 * em.persist(currTaskDef); } }
	 */
	// }
	// PRIVATE
	//existence
	public void initInitialData() {
		//ACCOUNTS & ACCESS
		//GLOBAL LIST OF resource operation definitions (not related to any resource type or resource!)
		ResourceGlobalOperation rodAddAccount = new ResourceGlobalOperation("ADD_ACCOUNT","Add Account","Add a new account",true);
		resourceGlobalOperations.add(rodAddAccount);
		ResourceGlobalOperation rodDelAccount = new ResourceGlobalOperation("DELETE_ACCOUNT","Delete an account","Delete an existence account",true);
		resourceGlobalOperations.add(rodDelAccount);
		ResourceGlobalOperation rodSuspendAccount = new ResourceGlobalOperation("SUSPEND_ACCOUNT","Suspend Account","Suspend an existence account",true);
		resourceGlobalOperations.add(rodSuspendAccount);
		ResourceGlobalOperation rodResumeAccount = new ResourceGlobalOperation("RESUME_ACCOUNT","Resume Account","Resume an existence account",true);
		resourceGlobalOperations.add(rodResumeAccount);
		ResourceGlobalOperation rodAddGroup = new ResourceGlobalOperation("ADD_GROUP","Add Group","Add a new access group",false);
		resourceGlobalOperations.add(rodAddGroup);
		ResourceGlobalOperation rodDeleteGroup = new ResourceGlobalOperation("DELETE_GROUP","Delete Group","Delete an existence access group",false);
		resourceGlobalOperations.add(rodDeleteGroup);
		ResourceGlobalOperation rodAddGroupMembership = new ResourceGlobalOperation("ADD_GROUP_MEMBERSHIP","Add Group Membership","Associate an account to an access group",false);
		resourceGlobalOperations.add(rodAddGroupMembership);
		ResourceGlobalOperation rodDeleteGroupMembership = new ResourceGlobalOperation("DELETE_GROUP_MEMBERSHIP","Delete Group Membership","Remove account association from an access group", false);
		resourceGlobalOperations.add(rodDeleteGroupMembership);
		ResourceGlobalOperation rodModifyAccount =  new ResourceGlobalOperation("MODIFY_ACCOUNT","Modify Account","Modify access and attributes of an existence account", false);
		resourceGlobalOperations.add(rodModifyAccount);

		
		
		
		//RECONCILIATION
		ResourceGlobalOperation rodResourceIdentitiesReconciliationFull = new ResourceGlobalOperation("RESOURCE_IDENTITIES_RECONCILIATION_FULL","Full Resource Identities Reconciliation","A full resource identities reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceIdentitiesReconciliationFull);
		ResourceGlobalOperation rodResourceIdentitiesReconciliationIncremental = new ResourceGlobalOperation("RESOURCE_IDENTITIES_RECONCILIATION_INCREMENTAL","Incremental Resource Identities Reconciliation","An incremental resource identities reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceIdentitiesReconciliationIncremental);
		ResourceGlobalOperation rodResourceGroupsReconciliation = new ResourceGlobalOperation("RESOURCE_GROUPS_RECONCILIATION_FULL","Full Resource Groups Reconciliation","A full resource groups reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceGroupsReconciliation);
		ResourceGlobalOperation rodResourceGroupMembershipReconciliation = new ResourceGlobalOperation("RESOURCE_GROUP_MEMBERSHIP_RECONCILIATION_FULL","Full Resource Group Membership Reconciliation","A full resource group membership reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceGroupMembershipReconciliation);
		ResourceGlobalOperation rodResourceGroupMembershipReconciliationIncr = new ResourceGlobalOperation("RESOURCE_GROUP_MEMBERSHIP_RECONCILIATION_INCREMENTAL","Incremental Resource Group Membership Reconciliation","An incremental resource group membership reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceGroupMembershipReconciliation);
		resourceGlobalOperations.add(rodResourceGroupMembershipReconciliationIncr);
		
		
		
		//TODO: RECONCILIATION - OLD SHOULD BE DEPRECATED!!!!!!!!
		ResourceGlobalOperation rodResourceReconciliation = new ResourceGlobalOperation("RESOURCE_RECONCILIATION","Resource Reconciliation","A resource reconciliation process.", false);
		resourceGlobalOperations.add(rodResourceReconciliation);
		ResourceGlobalOperation rodResourceFetchActiveDataOffline = new ResourceGlobalOperation("RESOURCE_FETCH_ACTIVE_DATA_OFFLINE","Resource Fetch Active Data Offline", "Fetch Active Data from resource offline", false);
		resourceGlobalOperations.add(rodResourceFetchActiveDataOffline);



		//PASSWORD OPERATIONS

















		capabilities.add(new Capability(new Long(1), "super_user",
		"admin super user capability"));
		capabilities.add(new Capability(new Long(2), "basic_auth",
		"Basic authentication capability"));
		capabilities.add(new Capability(new Long(3), "manage_users",
		"Manage Users"));
		capabilities.add(new Capability(new Long(4), "modify_user_attributes",
		"Manage User Attributes"));
		capabilities.add(new Capability(new Long(5), "disable_users",
		"Enable Users"));
		capabilities.add(new Capability(new Long(6), "delete_users",
		"Delete Users"));
		capabilities.add(new Capability(new Long(7), "enable_accounts",
		"Enable Accounts"));
		capabilities.add(new Capability(new Long(8), "disable_accounts",
		"Disable Accounts"));
		capabilities.add(new Capability(new Long(9), "modify_user_roles",
		"Modify User Roles"));
		capabilities.add(new Capability(new Long(12), "view_requests",
		"View Requests"));
		capabilities
		.add(new Capability(new Long(13),
				"assign_users_to_capabilities",
		"Assign users to capabilities"));
		capabilities.add(new Capability(new Long(15), "manage_resources",
		"Manage Resources"));
		capabilities.add(new Capability(new Long(18),
				"manage_resource_attributes",
		"Manage Resource Attributes"));
		capabilities.add(new Capability(new Long(19),
				"manage_identity_attributes", "Manage Identity Attributes"));
		capabilities.add(new Capability(new Long(20),
				"manage_identity_attribute_groups",
		"Manage Identity Attribute Groups"));
		capabilities.add(new Capability(new Long(21), "manage_capabilities",
		"Manage Capabilities"));
		capabilities.add(new Capability(new Long(23),
				"manage_resource_reconcile_policies", "Manage Resource Reconcile Policies"));
		capabilities.add(new Capability(new Long(24), "manage_email_templates",
		"Manage Email Templates"));
		capabilities.add(new Capability(new Long(27), "manage_tasks",
		"Manage Tasks"));
		capabilities.add(new Capability(new Long(30), "manage_bulk_tasks",
		"Manage Bulk Tasks"));
		capabilities.add(new Capability(new Long(32),
				"manage_event_responses", "Manage Event Responses"));
		capabilities.add(new Capability(new Long(34), "requeue_tasks",
		"Re-Queue Tasks"));
		capabilities.add(new Capability(new Long(36), "manage_password_sync",
		"Manage Password Synchronization Objects"));
		capabilities.add(new Capability(new Long(37), "approver", "Approver"));

		// CAPABILITY FOLDERS
		CapabilityFolder adminCapabilityFolder = new CapabilityFolder();
		adminCapabilityFolder.setDescription("Administrator Capabilities Folder");
		adminCapabilityFolder.setUniqueName("ADMINISTRATOR");
		//adminCapabilityFolder.setCapabilityFolderId(new Long(1));
		adminCapabilityFolder.setDisplayName("Administrator");
		
		CapabilityFolder standardUserCapabilityFolder = new CapabilityFolder();
		standardUserCapabilityFolder.setDescription("Standard User Capabilities Folder, any new user will be assigned to this capability folder automatically.");
		standardUserCapabilityFolder.setUniqueName("STANDARD_USER");
		standardUserCapabilityFolder.setDisplayName("Standard User");
		
		//TODO: Add basic_auth/other caps to standarDUser Cap folder?
		
		
		for (Object currCap : capabilities) {
			Capability cap = (Capability)currCap;
			adminCapabilityFolder.getCapabilities().add(cap);
		}

		capabilityFolders.add(adminCapabilityFolder);
		capabilityFolders.add(standardUserCapabilityFolder);

		/*
		// TASK DEFINITIONS
		taskDefinitions.add(new TaskDefinition(new Long(1), "CREATE_ACCOUNT",
				"A task to create an account",
				"velo.tasks.CreateAccountTaskExecuter", true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(2),
				"CREATE_ACCOUNT_SCRIPTED", "A task to create an account",
				"velo.tasks.CreateAccountTaskExecuter", true, "ASYNC", 0, true,
				2, true));
		taskDefinitions.add(new TaskDefinition(new Long(3), "ENABLE_ACCOUNT",
				"A task to enable an account", null, true, "ASYNC", 0, false,
				2, true));
		taskDefinitions.add(new TaskDefinition(new Long(4),
				"ENABLE_ACCOUNT_SCRIPTED", "A task to enable an account", null,
				true, "ASYNC", 0, true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(5), "DISABLE_ACCOUNT",
				"A Task to disable an account", null, true, "ASYNC", 0, false,
				2, true));
		taskDefinitions.add(new TaskDefinition(new Long(6),
				"DISABLE_ACCOUNT_SCRIPTED", "A Task to disable an account",
				null, true, "ASYNC", 0, true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(7), "DELETE_ACCOUNT",
				"A Task to delete an Account",
				"velo.tasks.DeleteAccountTaskExecuter", true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(8),
				"DELETE_ACCOUNT_SCRIPTED", "A Task to delete an account",
				"velo.tasks.DeleteAccountTaskExecuter", true, "ASYNC", 0, true,
				2, true));
		taskDefinitions.add(new TaskDefinition(new Long(9), "UPDATE_ACCOUNT",
				"A task for Updating an Account", null, true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(10),
				"UPDATE_ACCOUNT_SCRIPTED", "A task for Updating an Account",
				null, true, "ASYNC", 0, true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(11),
				"SYNC_TARGET_SYSTEM", "A task for sync a Target System", null,
				true, "ASYNC", 0, false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(12),
				"SYNC_TARGET_SYSTEM_SCRIPTED",
				"A task for sync a Target System", null, true, "ASYNC", 0,
				true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(13),
				"ADD_GROUP_MEMBERSHIP", "A task for Adding Group membership",
				"velo.tasks.AddGroupMembershipTaskExecuter", true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(14),
				"ADD_GROUP_MEMBERSHIP_SCRIPTED",
				"A task for Adding Group membership",
				"velo.tasks.AddGroupMembershipTaskExecuter", true, "ASYNC", 0,
				true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(15),
				"REMOVE_GROUP_MEMBERSHIP",
				"A task for Removing Group membership",
				"velo.tasks.RemoveGroupMembershipTaskExecuter", true, "ASYNC",
				0, false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(16),
				"REMOVE_GROUP_MEMBERSHIP_SCRIPTED",
				"A task for Removing Group membership",
				"velo.tasks.RemoveGroupMembershipTaskExecuter", true, "ASYNC",
				0, true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(17),
				"RECONCILE_TARGET_SYSTEM",
				"A task to reconcile a target system", null, true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(18),
				"PERSIST_USER_ROLE_ENTITY",
				"A task to associate a Role for a User in the repository",
				null, true, "ASYNC", 0, false, 2, true));
		taskDefinitions
				.add(new TaskDefinition(
						new Long(19),
						"REMOVE_USER_ROLE_ENTITY",
						"A task to remove Role assocaition from User in the repository",
						null, true, "ASYNC", 0, false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(20),
				"ACCOUNT_RESET_PASSWORD", "A task to reset account password",
				null, true, "ASYNC", 0, false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(21),
				"ACCOUNT_RESET_PASSWORD_SCRIPTED",
				"A task to reset account password", null, true, "ASYNC", 0,
				true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(22), "RECONCILE_USERS",
				"A task to reconcile users", null, true, "ASYNC", 0, false, 2,
				true));
		taskDefinitions.add(new TaskDefinition(new Long(23),
				"EVENT_RESPONSE_SCRIPTED", "A scripted event response task",
				null, true, "ASYNC", 0, true, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(24), "EVENT_RESPONSE",
				"A none scripted event response task", null, true, "ASYNC", 0,
				false, 2, true));
		taskDefinitions.add(new TaskDefinition(new Long(25),
				"RECONCILE_USER_IDENTITY_ATTRIBUTES",
				"A task to reconcile User Identity Attributes", null, true,
				"ASYNC", 0, false, 2, true));
		 */









		// RESOURCE Types
		ResourceType jdbcType = new ResourceType();
		jdbcType.setUniqueName("JDBC");
		jdbcType.setScripted(true);
		jdbcType.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<resource-descriptor>\n	<!--Adapter configuration, any adapter has these configuration parameters-->\n	<adapter>\n		<className desc=\"Adapter Class Name\">velo.adapters.JdbcAdapter</className>\n		<maxActive desc=\"Max Active Workers\">4</maxActive>\n		<maxIdle desc=\"Max Idle Workers\">2</maxIdle>\n		<maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait>\n		<minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis>\n	</adapter>\n	\n	<!-- Specific attributes relevant to the specified adapter-->\n	<specific>\n		<host desc=\"Host Name\"></host>\n		<port desc=\"Port\"></port>\n		<dbName desc=\"Database Name\"></dbName>\n		<driverName desc=\"Driver name\"></driverName>\n		<urlTemplate desc=\"Url Template\"></urlTemplate>\n		<query-timeout desc=\"Query timeout\">30</query-timeout>\n	</specific>\n</resource-descriptor>\n");
		jdbcType.setResourceControllerClassName("velo.resource.operationControllers.JdbcSpmlResourceOperationController");


		//Create ResourceOperationDefinitions for current resource type
		//jdbc is open and supports all global actions
		ResourceTypeOperation rtodAddAccount = new ResourceTypeOperation(rodAddAccount, jdbcType, false,true,false);
		ResourceTypeOperation rtodDelAccount = new ResourceTypeOperation(rodDelAccount, jdbcType, false,true,false);
		ResourceTypeOperation rtodSuspend = new ResourceTypeOperation(rodSuspendAccount, jdbcType, false,true,false);
		ResourceTypeOperation rtodResume = new ResourceTypeOperation(rodResumeAccount, jdbcType, false,true,false);
		ResourceTypeOperation rtodAddGroup = new ResourceTypeOperation(rodAddGroup, jdbcType, false,true,false);
		ResourceTypeOperation rtodRemoveGroup = new ResourceTypeOperation(rodDeleteGroup, jdbcType, false,true,false);
		ResourceTypeOperation rtodAddGroupMembership = new ResourceTypeOperation(rodAddGroupMembership, jdbcType, false,true,false);
		ResourceTypeOperation rtodDelGroupMembership = new ResourceTypeOperation(rodDeleteGroupMembership, jdbcType, false,true,false);
		ResourceTypeOperation rtodModifyAccount = new ResourceTypeOperation(rodModifyAccount, jdbcType, false,false,false);
		
		//Reconcile
		ResourceTypeOperation rtodResourceIdentitiesReconcileFull = new ResourceTypeOperation(rodResourceIdentitiesReconciliationFull, jdbcType, false,false,false);
		ResourceTypeOperation rtodResourceIdentitiesReconcileIncremental = new ResourceTypeOperation(rodResourceIdentitiesReconciliationIncremental, jdbcType, false,false,false);
		ResourceTypeOperation rtodResourceGroupsReconcileFull = new ResourceTypeOperation(rodResourceGroupsReconciliation, jdbcType, false,false,false);
		ResourceTypeOperation rtodResourceGroupMembershipReconcileFull = new ResourceTypeOperation(rodResourceGroupMembershipReconciliation, jdbcType, false,false,false);
		
		//ResourceTypeOperation rtodResourceReconciliation = new ResourceTypeOperation(rodResourceReconciliation, jdbcType, false,false,false);
		//ResourceTypeOperation rtodResourceFetchActiveDataOffline = new ResourceTypeOperation(rodResourceFetchActiveDataOffline, jdbcType, false,true,false);
		
		
		jdbcType.getSupportedOperations().add(rtodAddAccount);
		jdbcType.getSupportedOperations().add(rtodDelAccount);
		jdbcType.getSupportedOperations().add(rtodSuspend);
		jdbcType.getSupportedOperations().add(rtodResume);
		jdbcType.getSupportedOperations().add(rtodAddGroup);
		jdbcType.getSupportedOperations().add(rtodRemoveGroup);
		jdbcType.getSupportedOperations().add(rtodAddGroupMembership);
		jdbcType.getSupportedOperations().add(rtodDelGroupMembership);
		jdbcType.getSupportedOperations().add(rtodModifyAccount);
		//jdbcType.getSupportedOperations().add(rtodResourceReconciliation);
		//jdbcType.getSupportedOperations().add(rtodResourceFetchActiveDataOffline);

		jdbcType.getSupportedOperations().add(rtodResourceIdentitiesReconcileFull);
		jdbcType.getSupportedOperations().add(rtodResourceIdentitiesReconcileIncremental);
		jdbcType.getSupportedOperations().add(rtodResourceGroupsReconcileFull);
		jdbcType.getSupportedOperations().add(rtodResourceGroupMembershipReconcileFull);
		
		

		// jdbcType.setConfFileTemplate(FileUtils.getContents(confFileTemplate)



		
		
		
		

		//REMOTE AD
		ResourceType activeDirectoryType = new ResourceType();
		activeDirectoryType.setUniqueName("Remote Active-Directory");
		activeDirectoryType.setScripted(false);
		activeDirectoryType.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<resource-descriptor>\n	<adapter>\n		<className desc=\"Adapter Class Name\" disabled=\"true\">velo.adapters.ActiveDirectoryAdapter</className>\n        <maxActive desc=\"Max Active Workers\">4</maxActive>\n        <maxIdle desc=\"Max Idle Workers\">2</maxIdle>\n        <maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait>\n        <minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis>\n	</adapter>\n	\n	<!-- Specific attributes relevant to this target system descriptor type-->\n	<specific>\n		<host desc=\"Host Name\"></host>\n		<port desc=\"Port\">636</port>\n		<protocol desc=\"Protocol\">ssl</protocol>\n		<base-dn desc=\"Base DN (for all operations)\"></base-dn>\n		<groups-base-dn desc=\"Groups base DN (for searches/default creation path)\"></groups-base-dn>\n		<accounts-base-dn desc=\"Accounts Base DN (for sync porpuses)\"></accounts-base-dn>\n		<accounts-default-context-dn desc=\"Accounts default creation context\"></accounts-default-context-dn>\n		<ssl-key-store desc=\"SSL Key Store path\"></ssl-key-store>\n		<ssl-trust-store desc=\"SSL Trust store path\"></ssl-trust-store>\n		<ssl-key-store-password desc=\"SSL Keystore Password\"></ssl-key-store-password>\n		<authentication desc=\"Authentication scheme\">simple</authentication>\n	</specific>\n</resource-descriptor>\n");
		activeDirectoryType.setResourceControllerClassName("velo.resource.operationControllers.ActiveDirecotryRemoteSpmlResourceOperationController");

		//Create ResourceOperationDefinitions for current resource type
		//jdbc is open and supports all global actions
		ResourceTypeOperation RADrtodAddAccount = new ResourceTypeOperation(rodAddAccount, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodDelAccount = new ResourceTypeOperation(rodDelAccount, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodSuspend = new ResourceTypeOperation(rodSuspendAccount, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodResume = new ResourceTypeOperation(rodResumeAccount, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodAddGroup = new ResourceTypeOperation(rodAddGroup, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodRemoveGroup = new ResourceTypeOperation(rodDeleteGroup, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodAddGroupMembership = new ResourceTypeOperation(rodAddGroupMembership, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodDelGroupMembership = new ResourceTypeOperation(rodDeleteGroupMembership, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodModifyAccount = new ResourceTypeOperation(rodModifyAccount, activeDirectoryType, false,false,false);
		//ResourceTypeOperation RADresourceReconciliation = new ResourceTypeOperation(rodResourceReconciliation, activeDirectoryType, false,false,false);
		//ResourceTypeOperation RADresourceFetchActiveDataOffline = new ResourceTypeOperation(rodResourceFetchActiveDataOffline, activeDirectoryType, false,false,false);
		
		ResourceTypeOperation RADrtodResourceIdentitiesReconcileFull = new ResourceTypeOperation(rodResourceIdentitiesReconciliationFull, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodResourceIdentitiesReconcileIncremental = new ResourceTypeOperation(rodResourceIdentitiesReconciliationIncremental, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodResourceGroupsReconcileFull = new ResourceTypeOperation(rodResourceGroupsReconciliation, activeDirectoryType, false,false,false);
		ResourceTypeOperation RADrtodResourceGroupMembershipReconcileFull = new ResourceTypeOperation(rodResourceGroupMembershipReconciliation, activeDirectoryType, false,false,false);
		
		
		activeDirectoryType.getSupportedOperations().add(RADrtodAddAccount);
		activeDirectoryType.getSupportedOperations().add(RADrtodDelAccount);
		activeDirectoryType.getSupportedOperations().add(RADrtodSuspend);
		activeDirectoryType.getSupportedOperations().add(RADrtodResume);
		activeDirectoryType.getSupportedOperations().add(RADrtodAddGroup);
		activeDirectoryType.getSupportedOperations().add(RADrtodRemoveGroup);
		activeDirectoryType.getSupportedOperations().add(RADrtodAddGroupMembership);
		activeDirectoryType.getSupportedOperations().add(RADrtodDelGroupMembership);
		activeDirectoryType.getSupportedOperations().add(RADrtodModifyAccount);
		//activeDirectoryType.getSupportedOperations().add(RADresourceReconciliation);
		//activeDirectoryType.getSupportedOperations().add(RADresourceFetchActiveDataOffline);
		activeDirectoryType.getSupportedOperations().add(RADrtodResourceIdentitiesReconcileFull);
		activeDirectoryType.getSupportedOperations().add(RADrtodResourceIdentitiesReconcileIncremental);
		activeDirectoryType.getSupportedOperations().add(RADrtodResourceGroupsReconcileFull);
		activeDirectoryType.getSupportedOperations().add(RADrtodResourceGroupMembershipReconcileFull);












		//NATIVE AD
		ResourceType activeDirectoryDotNetType = new ResourceType();
		activeDirectoryDotNetType.setUniqueName("NATIVE_ACTIVE_DIRECTORY");
		activeDirectoryDotNetType.setScripted(false);
		activeDirectoryDotNetType.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><resource-descriptor><adapter><className desc=\"Adapter Class Name\" disabled=\"true\">velo.adapters.ActiveDirectoryAdapter</className><maxActive desc=\"Max Active Workers\">4</maxActive><maxIdle desc=\"Max Idle Workers\">2</maxIdle><maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait><minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis></adapter><!-- Specific attributes relevant to this target system descriptor type--><specific><domain desc=\"Domain Name\"></domain><base-dn desc=\"Base DN (for all operations)\"></base-dn><groups-base-dn desc=\"Groups base DN (for searches/default creation path)\"></groups-base-dn><accounts-base-dn desc=\"Accounts Base DN (for sync porpuses)\"></accounts-base-dn><accounts-default-context-dn desc=\"Accounts default creation context\"></accounts-default-context-dn><authenticationType desc=\"Authentication Type\">simple</authenticationType></specific></resource-descriptor>");
		activeDirectoryDotNetType.setResourceControllerClassName("velo.wingw.WindowsGateway.controllers.NativeActiveDirectoryController");
		activeDirectoryDotNetType.setGatewayRequired(true);
		activeDirectoryDotNetType.setResourceControllerType(ResourceControllerType.SPML_GENERIC);

		//Create ResourceOperationDefinitions for current resource type
		//jdbc is open and supports all global actions
		ResourceTypeOperation NADrtodAddAccount = new ResourceTypeOperation(rodAddAccount, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodDelAccount = new ResourceTypeOperation(rodDelAccount, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodSuspend = new ResourceTypeOperation(rodSuspendAccount, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodResume = new ResourceTypeOperation(rodResumeAccount, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodAddGroup = new ResourceTypeOperation(rodAddGroup, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodRemoveGroup = new ResourceTypeOperation(rodDeleteGroup, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodAddGroupMembership = new ResourceTypeOperation(rodAddGroupMembership, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodDelGroupMembership = new ResourceTypeOperation(rodDeleteGroupMembership, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodModifyAccount = new ResourceTypeOperation(rodModifyAccount, activeDirectoryDotNetType, false,false,false);
		//ResourceTypeOperation NADresourceReconciliation = new ResourceTypeOperation(rodResourceReconciliation, activeDirectoryDotNetType, false,false,false);
		//ResourceTypeOperation NADresourceFetchActiveDataOffline = new ResourceTypeOperation(rodResourceFetchActiveDataOffline, activeDirectoryDotNetType, false,false,false);
		
		ResourceTypeOperation NADrtodResourceIdentitiesReconcileFull = new ResourceTypeOperation(rodResourceIdentitiesReconciliationFull, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodResourceIdentitiesReconcileIncremental = new ResourceTypeOperation(rodResourceIdentitiesReconciliationIncremental, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodResourceGroupsReconcileFull = new ResourceTypeOperation(rodResourceGroupsReconciliation, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodResourceGroupMembershipReconcileFull = new ResourceTypeOperation(rodResourceGroupMembershipReconciliation, activeDirectoryDotNetType, false,false,false);
		ResourceTypeOperation NADrtodResourceGroupMembershipReconcileIncr = new ResourceTypeOperation(rodResourceGroupMembershipReconciliationIncr, activeDirectoryDotNetType, false,false,false);
		
		
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodAddAccount);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodDelAccount);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodSuspend);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResume);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodAddGroup);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodRemoveGroup);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodAddGroupMembership);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodDelGroupMembership);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodModifyAccount);
		//activeDirectoryDotNetType.getSupportedOperations().add(NADresourceReconciliation);
		//activeDirectoryDotNetType.getSupportedOperations().add(NADresourceFetchActiveDataOffline);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResourceIdentitiesReconcileFull);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResourceIdentitiesReconcileIncremental);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResourceGroupsReconcileFull);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResourceGroupMembershipReconcileFull);
		activeDirectoryDotNetType.getSupportedOperations().add(NADrtodResourceGroupMembershipReconcileIncr);
		











		
		
		
		
		
		
		//ORACLE SERVER
		ResourceType oracleServerType = new ResourceType();
		oracleServerType.setUniqueName("Oracle Server");
		oracleServerType.setScripted(false);
		oracleServerType.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<resource-descriptor>\n<!--Adapter configuration, any adapter has these configuration parameters-->\n<adapter>\n<className desc=\"Adapter Class Name\">velo.adapters.JdbcAdapter</className>\n<maxActive desc=\"Max Active Workers\">4</maxActive>\n<maxIdle desc=\"Max Idle Workers\">2</maxIdle>\n<maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait>\n<minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis>\n</adapter>\n<!-- Specific attributes relevant to the specified adapter-->\n<specific>\n<host desc=\"Host Name\"></host>\n<port desc=\"Port\"></port>\n<dbName desc=\"Database Name\"></dbName>\n<driverName desc=\"Driver name\">driver:oracle.jdbc.driver.OracleDriver</driverName>\n<urlTemplate desc=\"Url Template\">jdbc:oracle:thin:@%h:%p:%d</urlTemplate>\n<query-timeout desc=\"Query timeout\">30</query-timeout>\n</specific>\n</resource-descriptor>");
		oracleServerType.setResourceControllerClassName("velo.resource.operationControllers.OracleServerSpmlController");
		oracleServerType.setGatewayRequired(false);
		oracleServerType.setResourceControllerType(ResourceControllerType.SPML_GENERIC);

		//Create ResourceOperationDefinitions for current resource type
		//oracle is open and supports all global actions
		ResourceTypeOperation ORArtodAddAccount = new ResourceTypeOperation(rodAddAccount, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodDelAccount = new ResourceTypeOperation(rodDelAccount, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodSuspend = new ResourceTypeOperation(rodSuspendAccount, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodResume = new ResourceTypeOperation(rodResumeAccount, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodAddGroup = new ResourceTypeOperation(rodAddGroup, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodRemoveGroup = new ResourceTypeOperation(rodDeleteGroup, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodAddGroupMembership = new ResourceTypeOperation(rodAddGroupMembership, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodDelGroupMembership = new ResourceTypeOperation(rodDeleteGroupMembership, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodModifyAccount = new ResourceTypeOperation(rodModifyAccount, oracleServerType, false,false,false);
		//ResourceTypeOperation ORAresourceReconciliation = new ResourceTypeOperation(rodResourceReconciliation, oracleServerType, false,false,false);
		//ResourceTypeOperation ORAresourceFetchActiveDataOffline = new ResourceTypeOperation(rodResourceFetchActiveDataOffline, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodResourceIdentitiesReconcileFull = new ResourceTypeOperation(rodResourceIdentitiesReconciliationFull, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodResourceIdentitiesReconcileIncremental = new ResourceTypeOperation(rodResourceIdentitiesReconciliationIncremental, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodResourceGroupsReconcileFull = new ResourceTypeOperation(rodResourceGroupsReconciliation, oracleServerType, false,false,false);
		ResourceTypeOperation ORArtodResourceGroupMembershipReconcileFull = new ResourceTypeOperation(rodResourceGroupMembershipReconciliation, oracleServerType, false,false,false);
		
		
		
		oracleServerType.getSupportedOperations().add(ORArtodAddAccount);
		oracleServerType.getSupportedOperations().add(ORArtodDelAccount);
		oracleServerType.getSupportedOperations().add(ORArtodSuspend);
		oracleServerType.getSupportedOperations().add(ORArtodResume);
		oracleServerType.getSupportedOperations().add(ORArtodAddGroup);
		oracleServerType.getSupportedOperations().add(ORArtodRemoveGroup);
		oracleServerType.getSupportedOperations().add(ORArtodAddGroupMembership);
		oracleServerType.getSupportedOperations().add(ORArtodDelGroupMembership);
		oracleServerType.getSupportedOperations().add(ORArtodModifyAccount);
		//oracleServerType.getSupportedOperations().add(ORAresourceReconciliation);
		//oracleServerType.getSupportedOperations().add(ORAresourceFetchActiveDataOffline);
		oracleServerType.getSupportedOperations().add(ORArtodResourceIdentitiesReconcileFull);		
		oracleServerType.getSupportedOperations().add(ORArtodResourceIdentitiesReconcileIncremental);
		oracleServerType.getSupportedOperations().add(ORArtodResourceGroupsReconcileFull);
		oracleServerType.getSupportedOperations().add(ORArtodResourceGroupMembershipReconcileFull);
		
		
		
		
		




		
		
		
		//LDAP v3 SERVER
		ResourceType ldapV3Type = new ResourceType();
		ldapV3Type.setUniqueName("Generic LDAP");
		ldapV3Type.setScripted(false);
		ldapV3Type.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<resource-descriptor>\n<adapter>\n<className desc=\"Adapter Class Name\" disabled=\"true\">velo.adapters.ActiveDirectoryAdapter</className>\n<maxActive desc=\"Max Active Workers\">4</maxActive>\n<maxIdle desc=\"Max Idle Workers\">2</maxIdle>\n<maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait>\n<minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis>\n</adapter>\n<specific>\n<host desc=\"Host Name\">\n</host>\n<port desc=\"Port\">636</port>\n<protocol desc=\"The protocol used to connect to the directory (ssl/clear)\">ssl</protocol>\n<ssl-key-store desc=\"SSL Key Store path\">\n</ssl-key-store>\n<ssl-trust-store desc=\"SSL Trust store path\">\n</ssl-trust-store>\n<ssl-key-store-password desc=\"SSL Keystore Password\">\n</ssl-key-store-password>\n<group>\n<group-dn desc=\"This value is used in addition to the base DN when searching and loading groups, an example is ou=Groups. If no value is supplied, the subtree search will start from the base DN.\">\n</group-dn>\n<group-object-class desc=\"The LDAP user object class typeto use when loading groups.\">groupOfUniqueNames</group-object-class>\n<group-object-filter desc=\"The filter to use when searching group objects.\">(objectclass=groupOfUniqueNames)</group-object-filter>\n<group-unique-name-attribute desc=\"The attribute field to use when loading the group unique name.\">cn</group-unique-name-attribute>\n<group-display-name-attribute desc=\"The attribute field to use when loading the group display name.\">cn</group-display-name-attribute>\n<group-description-attribute desc=\"The attribute field to use when loading the group description\">description</group-description-attribute>\n<group-members-attribute desc=\"The attribute field to use when loading the group members.\">uniqueMember</group-members-attribute>\n</group>\n<account>\n<account-dn desc=\"This value is used in addition to the base DN when searching and loading accounts, an example is ou=Users. If no value is supplied, the subtree search will start from the base DN.\">\n</account-dn>\n<account-object-class desc=\"The LDAP account object class type o use when loading principals.\">inetorgperson</account-object-class>\n<account-object-filter desc=\"The filter to use when searching user objects.\">(objectclass=inetorgperson)</account-object-filter>\n<account-default-creation-dn desc=\"The value is used in addition to the base DN, this is the default account creation container. a value must be supplied.\">\n</account-default-creation-dn>\n</account>\n</specific>\n</resource-descriptor>");
		ldapV3Type.setResourceControllerClassName("velo.resource.operationControllers.GenericLdapResourceController");
		ldapV3Type.setGatewayRequired(false);
		ldapV3Type.setResourceControllerType(ResourceControllerType.SPML_GENERIC);

		//Create ResourceOperationDefinitions for current resource type
		//oracle is open and supports all global actions
		ResourceTypeOperation ldapV3rtodAddAccount = new ResourceTypeOperation(rodAddAccount, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodDelAccount = new ResourceTypeOperation(rodDelAccount, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodSuspend = new ResourceTypeOperation(rodSuspendAccount, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodResume = new ResourceTypeOperation(rodResumeAccount, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodAddGroup = new ResourceTypeOperation(rodAddGroup, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodRemoveGroup = new ResourceTypeOperation(rodDeleteGroup, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodAddGroupMembership = new ResourceTypeOperation(rodAddGroupMembership, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodDelGroupMembership = new ResourceTypeOperation(rodDeleteGroupMembership, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodModifyAccount = new ResourceTypeOperation(rodModifyAccount, ldapV3Type, false,false,false);
		//ResourceTypeOperation ldapV3resourceReconciliation = new ResourceTypeOperation(rodResourceReconciliation, ldapV3Type, false,false,false);
		//ResourceTypeOperation ldapV3resourceFetchActiveDataOffline = new ResourceTypeOperation(rodResourceFetchActiveDataOffline, ldapV3Type, false,false,false);
		
		
		ResourceTypeOperation ldapV3rtodResourceIdentitiesReconcileFull = new ResourceTypeOperation(rodResourceIdentitiesReconciliationFull, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodResourceIdentitiesReconcileIncremental = new ResourceTypeOperation(rodResourceIdentitiesReconciliationIncremental, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodResourceGroupsReconcileFull = new ResourceTypeOperation(rodResourceGroupsReconciliation, ldapV3Type, false,false,false);
		ResourceTypeOperation ldapV3rtodResourceGroupMembershipReconcileFull = new ResourceTypeOperation(rodResourceGroupMembershipReconciliation, ldapV3Type, false,false,false);
		
		ldapV3Type.getSupportedOperations().add(ldapV3rtodAddAccount);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodDelAccount);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodSuspend);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodResume);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodAddGroup);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodRemoveGroup);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodAddGroupMembership);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodDelGroupMembership);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodModifyAccount);
		//ldapV3Type.getSupportedOperations().add(ldapV3resourceReconciliation);
		//ldapV3Type.getSupportedOperations().add(ldapV3resourceFetchActiveDataOffline);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodResourceIdentitiesReconcileFull);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodResourceIdentitiesReconcileIncremental);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodResourceGroupsReconcileFull);
		ldapV3Type.getSupportedOperations().add(ldapV3rtodResourceGroupMembershipReconcileFull);



















		ResourceType httpClient = new ResourceType();
		httpClient.setUniqueName("HTTP-Client");
//		JB		httpClient.setAdapterClassName("velo.adapters.GenericHttpClientAdapter");
		httpClient.setScripted(true);
		httpClient.setConfigurationTemplate("<XML>");
		httpClient.setResourceControllerClassName("velo.resource.operationControllers.HttpClientSpmlResourceOperationController");

		ResourceType genericScriptedTelnetType = new ResourceType();
		genericScriptedTelnetType.setUniqueName("Generic Scripted Telnet");
//		JB		genericScriptedTelnetType.setAdapterClassName("velo.adapters.GenericTelnetAdapter");
		genericScriptedTelnetType.setConfigurationTemplate("<XML>");
		genericScriptedTelnetType.setScripted(true);
		genericScriptedTelnetType.setResourceControllerClassName("velo.resource.operationControllers.TelnetSpmlResourceOperationController");















		resourceTypes.put(jdbcType.getUniqueName(),jdbcType);
		resourceTypes.put(activeDirectoryType.getUniqueName(),activeDirectoryType);
		resourceTypes.put(httpClient.getUniqueName(),httpClient);
		resourceTypes.put(genericScriptedTelnetType.getUniqueName(),genericScriptedTelnetType);
		resourceTypes.put(activeDirectoryDotNetType.getUniqueName(),activeDirectoryDotNetType);
		resourceTypes.put(oracleServerType.getUniqueName(),oracleServerType);
		resourceTypes.put(ldapV3Type.getUniqueName(),ldapV3Type);
		











































		// Password Policies
		PasswordPolicy ppDefault = new PasswordPolicy();
		ppDefault.setUniqueName("DEFAULT");
		ppDefault.setDisplayName("Default Password Policy");
		ppDefault.setDescription("Default Password Policy");
		passwordPolicies.add(ppDefault);

		// Password Policy Containers
		PasswordPolicyContainer ppcDefault = new PasswordPolicyContainer();
		ppcDefault.setUniqueName("DEFAULT");
		ppcDefault.setDisplayName("Default Password Policy Container");
		ppcDefault.setDescription("Default Password Policy Container");
		ppcDefault.setPasswordPolicy(ppDefault);
		passwordPolicyContainers.add(ppcDefault);

		
		
		
		// Event Definitions
		/*
		EventDefinition edUserCreateSuccess = new EventDefinition();
		edUserCreateSuccess.setUniqueName("USER_CREATION");
		edUserCreateSuccess.setDisplayName("Successfully user creation event");
		edUserCreateSuccess
		.setDescription("An event that occures after a user was successfully created in the repository");
		eventDefinitions.add(edUserCreateSuccess);

		EventDefinition edTaskStatusModification = new EventDefinition();
		edTaskStatusModification.setUniqueName("TASK_FAILURE");
		edTaskStatusModification.setDisplayName("Task Failure");
		edTaskStatusModification
		.setDescription("An event that is triggered when a task execution fails");
		eventDefinitions.add(edTaskStatusModification);

		EventDefinition edRequestStatusModification = new EventDefinition();
		edRequestStatusModification
		.setUniqueName("REQUEST_STATUS_MODIFICATION");
		edRequestStatusModification
		.setDisplayName("Request Status Modification");
		edRequestStatusModification
		.setDescription("An event that occures when a status of a certain request successfully modified.");
		eventDefinitions.add(edRequestStatusModification);

		EventDefinition edResourceReconciliation = new EventDefinition();
		edResourceReconciliation
		.setUniqueName("RESOURCE_RECONCILIATION");
		edResourceReconciliation
		.setDisplayName("Resource Reconciliation");
		edResourceReconciliation
		.setDescription("An event that is triggered when a resoruce reconciliation process finishes");
		eventDefinitions.add(edResourceReconciliation);
		 */
		
		
		

		// IdentityAttribute Group
		IdentityAttributesGroup iagGeneric = new IdentityAttributesGroup();
		iagGeneric.setDescription("Generic Attributes");
		iagGeneric.setDisplayPriority(0);
		iagGeneric.setName("Generic");
		iagGeneric.setVisible(true);
		identityAttributesGroups.add(iagGeneric);


		// IDENTITY ATTRIBUTES
		IdentityAttribute firstName = new IdentityAttribute();
		//firstName.setIdentityAttributeId(new Long(1));
		firstName.setDisplayName("First Name");
		firstName.setDataType(AttributeDataTypes.STRING);
		firstName.setDescription("Employee English First Name");
		firstName.setDisplayPriority(1);
		firstName.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		firstName.setMaxLength(255);
		firstName.setMaxValues(1);
		firstName.setMinValues(1);
		firstName.setRequired(true);
		firstName.setRequiredInRequest(true);
		firstName.setUniqueName("FIRST_NAME");
		firstName.setVisibleInRequest(true);
		firstName.setVisibleInUserList(true);
		firstName.setIdentityAttributesGroup(iagGeneric);
		firstName.setSource(IdentityAttributeSources.LOCAL);

		IdentityAttribute lastName = new IdentityAttribute();
		//lastName.setIdentityAttributeId(new Long(1));
		lastName.setDisplayName("Last Name");
		lastName.setDataType(AttributeDataTypes.STRING);
		lastName.setDescription("Employee English Last Name");
		lastName.setDisplayPriority(1);
		lastName.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		lastName.setMaxLength(255);
		lastName.setMaxValues(1);
		lastName.setMinValues(1);
		lastName.setRequired(true);
		lastName.setRequiredInRequest(true);
		lastName.setUniqueName("LAST_NAME");
		lastName.setVisibleInRequest(true);
		lastName.setVisibleInUserList(true);
		lastName.setIdentityAttributesGroup(iagGeneric);
		lastName.setSource(IdentityAttributeSources.LOCAL);

		IdentityAttribute title = new IdentityAttribute();
		//lastName.setIdentityAttributeId(new Long(1));
		title.setDisplayName("Title");
		title.setDataType(AttributeDataTypes.STRING);
		title.setDescription("Employee Title");
		title.setDisplayPriority(5);
		title.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		title.setMaxLength(255);
		title.setMaxValues(1);
		title.setMinValues(1);
		title.setRequired(false);
		title.setRequiredInRequest(false);
		title.setUniqueName("TITLE");
		title.setVisibleInRequest(true);
		title.setVisibleInUserList(false);
		title.setIdentityAttributesGroup(iagGeneric);
		title.setSource(IdentityAttributeSources.LOCAL);

		IdentityAttribute emailAddressIA = new IdentityAttribute();
		//lastName.setIdentityAttributeId(new Long(1));
		emailAddressIA.setDisplayName("Email Address");
		emailAddressIA.setDataType(AttributeDataTypes.STRING);
		emailAddressIA.setDescription("Email Address");
		emailAddressIA.setDisplayPriority(10);
		emailAddressIA.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		emailAddressIA.setMaxLength(255);
		emailAddressIA.setMaxValues(1);
		emailAddressIA.setMinValues(1);
		emailAddressIA.setRequired(false);
		emailAddressIA.setRequiredInRequest(false);
		emailAddressIA.setUniqueName("EMAIL");
		emailAddressIA.setVisibleInRequest(true);
		emailAddressIA.setVisibleInUserList(true);
		emailAddressIA.setIdentityAttributesGroup(iagGeneric);
		emailAddressIA.setSource(IdentityAttributeSources.LOCAL);
		
		
		IdentityAttribute departmentIA = new IdentityAttribute();
		departmentIA.setDisplayName("Department");
		departmentIA.setDataType(AttributeDataTypes.STRING);
		departmentIA.setDescription("User Department");
		departmentIA.setDisplayPriority(15);
		departmentIA.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		departmentIA.setMaxLength(255);
		departmentIA.setMaxValues(1);
		departmentIA.setMinValues(1);
		departmentIA.setRequired(false);
		departmentIA.setRequiredInRequest(false);
		departmentIA.setUniqueName("DEPARTMENT");
		departmentIA.setVisibleInRequest(true);
		departmentIA.setVisibleInUserList(true);
		departmentIA.setIdentityAttributesGroup(iagGeneric);
		departmentIA.setSource(IdentityAttributeSources.LOCAL);
		
		
		IdentityAttribute mobileIA = new IdentityAttribute();
		mobileIA.setDisplayName("Mobile");
		mobileIA.setDataType(AttributeDataTypes.STRING);
		mobileIA.setDescription("User Mobile Number");
		mobileIA.setDisplayPriority(20);
		mobileIA.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		mobileIA.setMaxLength(30);
		mobileIA.setMaxValues(1);
		mobileIA.setMinValues(1);
		mobileIA.setRequired(false);
		mobileIA.setRequiredInRequest(false);
		mobileIA.setUniqueName("MOBILE");
		mobileIA.setVisibleInRequest(true);
		mobileIA.setVisibleInUserList(true);
		mobileIA.setIdentityAttributesGroup(iagGeneric);
		mobileIA.setSource(IdentityAttributeSources.LOCAL);


		IdentityAttribute companyIA = new IdentityAttribute();
		companyIA.setDisplayName("Company");
		companyIA.setDataType(AttributeDataTypes.STRING);
		companyIA.setDescription("The company of the user");
		companyIA.setDisplayPriority(20);
		companyIA.setVisualRenderingType(AttributeVisualRenderingType.INPUT);
		companyIA.setMaxLength(255);
		companyIA.setMaxValues(1);
		companyIA.setMinValues(1);
		companyIA.setRequired(false);
		companyIA.setRequiredInRequest(false);
		companyIA.setUniqueName("COMPANY");
		companyIA.setVisibleInRequest(true);
		companyIA.setVisibleInUserList(true);
		companyIA.setIdentityAttributesGroup(iagGeneric);
		companyIA.setSource(IdentityAttributeSources.LOCAL);
		
		identityAttributes.add(firstName);
		identityAttributes.add(lastName);
		identityAttributes.add(title);
		identityAttributes.add(emailAddressIA);
		identityAttributes.add(departmentIA);
		identityAttributes.add(mobileIA);
		identityAttributes.add(companyIA);




		Set<IdentityAttribute> allDefaultIdentityAttributes = new HashSet<IdentityAttribute>();
		for (Object currObject : identityAttributes) {
			IdentityAttribute currIA = (IdentityAttribute)currObject;
			allDefaultIdentityAttributes.add(currIA);
		}


		// Associate IdentityAttributes to their group
		iagGeneric.setIdentityAttributes(allDefaultIdentityAttributes);
		identityAttributesGroups.add(iagGeneric);

		
		
		//RECONCILE POLICY & RECONCILE AUDIT POLICY
		ReconcileAuditPolicy rap = new ReconcileAuditPolicy();
		rap.setName("DEFAULT");
		
		ReconcilePolicy reconcilePolicy = new ReconcilePolicy();
		reconcilePolicy.setActivateCorrelationRule(false);
		reconcilePolicy.setConfirmedAccountAttributeEventAction("NOTHING");
		reconcilePolicy.setConfirmedAccountEventAction("NOTHING");
		reconcilePolicy
		.setDeleteGroupAfterReconcileProcessesNumberExceeded(true);
		reconcilePolicy
		.setDeletedAccountEventAction("REMOVE_ACCOUNT_FROM_IDM_REPOSITORY");
		reconcilePolicy.setName("Default");
		reconcilePolicy
		.setReconcilesGroupKeepsBeingDeletedBeforeRemoveGroup(10);
		reconcilePolicy
		.setUnasignedAccountEventAction("CREATE_ACCOUNT_WITHOUT_ASSIGN_ACCOUNT_TO_MATCHED_USER");
		reconcilePolicy.setUnmatchedAccountAttributeEventAction("NOTHING");
		reconcilePolicy
		.setUnmatchedAccountEventAction("PERSIST_ACCOUNT_IN_IDM_REPOSITORY");
		reconcilePolicy.setReconcileAuditPolicy(rap);
		reconcileTargetPolicies.add(reconcilePolicy);

		
		
		
		
		

		// Create an admin user
		User adminUser = new User();
		//adminUser.setUserId(new Long(1));
		//adminUser.setUserId(Long.valueOf(1));
		adminUser.setCreatedManually(true);
		adminUser.setProtected(true);
		adminUser.setAuthenticatedViaLocalPassword(true);
		adminUser.setName("admin");
		
		if (Contexts.isApplicationContextActive()) {
			adminUser.setPassword(PasswordHash.instance().generateSaltedHash("admin", adminUser.getName()));
		}
		
		
		// Admin CapFolder
		adminUser.getCapabilityFolders().add(adminCapabilityFolder);
		users.add(adminUser);





		// default action languages
		ActionLanguage alGroovy = new ActionLanguage();
		//alGroovy.setActionLanguageId(new Long(1));
		alGroovy.setName("groovy");
		alGroovy.setDescription("Groovy - An agile scripting language");
		actionLanguages.add(alGroovy);

		ActionLanguage alXml = new ActionLanguage();
		//alXml.setActionLanguageId(new Long(2));
		alXml.setName("XML");
		alXml.setDescription("A simple XML action");
		actionLanguages.add(alXml);




		//User Containers
		UserContainer duc = new UserContainer();
		duc.setCreationDate(new Date());
		duc.setDisplayName("Default Container");
		duc.setDescription("All users associated to this container by default.");
		duc.setUniqueName("Default");
		userContainers.add(duc);















		//--ACTIVE DIRECTORY DEFAULT ATTRIBUTES!
		//resource type default shipped attributes
		ResourceTypeAttribute attr1 = new ResourceTypeAttribute(
				"sAMAccountName", "SAM Account Name", "SAM Account Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr1.setResourceAttributeId(new Long(1));
		attr1.setAccountId(true);
		attr1.setResourceType(activeDirectoryDotNetType);
		attr1.setIdentityAttribute(firstName);
		attr1.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attr1);
		resourceTypeAttributes.add(attr1);


		//CN
		ResourceTypeAttribute attr2 = new ResourceTypeAttribute(
				"cn", "Common Name", "Common Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		attr2.setResourceType(activeDirectoryDotNetType);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attr2);
		attr2.setIdentityAttribute(firstName);
		attr2.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		resourceTypeAttributes.add(attr2);

		//mobile
		ResourceTypeAttribute attrMobile = new ResourceTypeAttribute(
				"mobile", "Mobile Number", "Mobile",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		attrMobile.setResourceType(activeDirectoryDotNetType);
		attrMobile.setIdentityAttribute(mobileIA);
		attrMobile.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);

		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrMobile);
		resourceTypeAttributes.add(attrMobile);


		//displayName
		ResourceTypeAttribute attrDisplayName = new ResourceTypeAttribute(
				"displayname", "Display Name", "Display Name",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		attrDisplayName.setResourceType(activeDirectoryDotNetType);
		attrDisplayName.setSourceType(SourceTypes.NONE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrDisplayName);
		resourceTypeAttributes.add(attrDisplayName);



		//sn
		ResourceTypeAttribute attrSN = new ResourceTypeAttribute(
				"sn", "Surname", "Surname",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		attrSN.setResourceType(activeDirectoryDotNetType);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrSN);
		attrSN.setIdentityAttribute(lastName);
		attrSN.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		resourceTypeAttributes.add(attrSN);



		//givenName
		ResourceTypeAttribute attrGN = new ResourceTypeAttribute(
				"givenname", "Given Name", "Given Name",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		attrGN.setResourceType(activeDirectoryDotNetType);
		attrGN.setIdentityAttribute(firstName);
		attrGN.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrGN);
		resourceTypeAttributes.add(attrGN);


		//description
		ResourceTypeAttribute attrDescription = new ResourceTypeAttribute(
				"description", "Description", "Description",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		attrDescription.setResourceType(activeDirectoryDotNetType);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrDescription);
		attrDescription.setSourceType(SourceTypes.NONE);
		resourceTypeAttributes.add(attrDescription);


		//title
		ResourceTypeAttribute attrTitle = new ResourceTypeAttribute(
				"title", "Title", "Title",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		attrTitle.setResourceType(activeDirectoryDotNetType);
		attrTitle.setIdentityAttribute(title);
		attrTitle.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(attrTitle);
		resourceTypeAttributes.add(attrTitle);


		//company
		ResourceTypeAttribute ADCompanyAttr = new ResourceTypeAttribute(
				"company", "Comapny", "Company",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		ADCompanyAttr.setResourceType(activeDirectoryDotNetType);
		ADCompanyAttr.setIdentityAttribute(companyIA);
		ADCompanyAttr.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(ADCompanyAttr);
		resourceTypeAttributes.add(ADCompanyAttr);

		//department
		ResourceTypeAttribute ADDepartmentAttr = new ResourceTypeAttribute(
				"department", "Department", "Department",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		ADDepartmentAttr.setResourceType(activeDirectoryDotNetType);
		ADDepartmentAttr.setIdentityAttribute(departmentIA);
		ADDepartmentAttr.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(ADDepartmentAttr);
		resourceTypeAttributes.add(ADDepartmentAttr);

		//UPN
		ResourceTypeAttribute ADUserPrincipalNameAttr = new ResourceTypeAttribute(
				"userPrincipalName", "User Principal Name", "User Principal Name",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		ADUserPrincipalNameAttr.setResourceType(activeDirectoryDotNetType);
		ADUserPrincipalNameAttr.setSourceType(SourceTypes.NONE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(ADUserPrincipalNameAttr);
		resourceTypeAttributes.add(ADUserPrincipalNameAttr);


		//ProxyAddresses
		ResourceTypeAttribute ADProxyAddressesAttr = new ResourceTypeAttribute(
				"proxyAddresses", "Proxy Addresses", "The Proxy Addresses of the mailbox",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		ADProxyAddressesAttr.setResourceType(activeDirectoryDotNetType);
		ADProxyAddressesAttr.setSourceType(SourceTypes.NONE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(ADProxyAddressesAttr);
		resourceTypeAttributes.add(ADProxyAddressesAttr);

		//homeMDB
		ResourceTypeAttribute ADHomeMDBAttr = new ResourceTypeAttribute(
				"homeMDB", "Home Mdb", "The Exchange HomeMDB that will store the mailbox",
				AttributeDataTypes.STRING, false, true, 1, 255, 1, 1);
		ADHomeMDBAttr.setResourceType(activeDirectoryDotNetType);
		ADHomeMDBAttr.setSourceType(SourceTypes.NONE);
		activeDirectoryDotNetType.getResourceTypeAttributes().add(ADHomeMDBAttr);
		resourceTypeAttributes.add(ADHomeMDBAttr);






		//--NATIVE(DOT NET) ACTIVE DIRECTORY DEFAULT ATTRIBUTES!
		//resource type default shipped attributes
		ResourceTypeAttribute ADDNattr1 = new ResourceTypeAttribute(
				"sAMAccountName", "SAM Account Name", "SAM Account Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr1.setResourceAttributeId(new Long(1));
		ADDNattr1.setAccountId(true);
		ADDNattr1.setResourceType(activeDirectoryType);
		ADDNattr1.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		ADDNattr1.setIdentityAttribute(firstName);
		activeDirectoryType.getResourceTypeAttributes().add(ADDNattr1);
		resourceTypeAttributes.add(ADDNattr1);


		//CN
		ResourceTypeAttribute ADDNattr2 = new ResourceTypeAttribute(
				"cn", "Common Name", "Common Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		ADDNattr2.setResourceType(activeDirectoryType);
		activeDirectoryType.getResourceTypeAttributes().add(ADDNattr2);
		ADDNattr2.setSourceType(SourceTypes.IDENTITY_ATTRIBUTE);
		ADDNattr2.setIdentityAttribute(firstName);
		resourceTypeAttributes.add(ADDNattr2);


		
		
		//call inits
		initReconcileEvents();
		initSystemEvents();
		//Sync method takes care of this initReadyActions();
	}

	
	
	
	//--==Individual inits==--
	public void initReconcileEvents() {
		//reconcile events
		ReconcileEvent reIdentityConfirmed = new ReconcileEvent("IDENTITY_CONFIRMED", "Confirmed Identity", "An event that occurs whenever the identity was confirmed to be existed on the resource");
		ReconcileEvent reIdentityRemoved = new ReconcileEvent("IDENTITY_REMOVED", "Removed Identity", "An event that occures whenever an identity was removed on the resource");
		ReconcileEvent reIdentityUnmatched = new ReconcileEvent("IDENTITY_UNMATCHED", "Unmatched Identity", "An event that occures whenever a new identity is discovered on the resource but has no matching owner.");
		ReconcileEvent reIdentityUnasigned = new ReconcileEvent("IDENTITY_UNASSIGNED", "Unassigned Identity", "An event that occures whenever a new identity is discovered on the resource with a matching owner.");
		ReconcileEvent reIdentityModified = new ReconcileEvent("IDENTITY_MODIFIED", "Modified Identity", "An event that occures whenever attributes of current existence identity were modified.");
		ReconcileEvent reIdentityAttributeModified = new ReconcileEvent("IDENTITY_ATTRIBUTE_MODIFIED", "Modified Identity Attribute", "An event that occures whenever a certain attribute of current existence identity was modified.");
		
		ReconcileEvent reGroupModified = new ReconcileEvent("GROUP_MODIFIED", "Modified Group", "An event that occures whenever a group was modified on the resource.");
		ReconcileEvent reGroupCreated = new ReconcileEvent("GROUP_CREATED", "Created Group", "An event that occures whenever a new group was discovered on the resource.");
		ReconcileEvent reGroupRemoved = new ReconcileEvent("GROUP_REMOVED", "Removed Group", "An event that occures whenever a group was removed from the resource.");
		
		
		ReconcileEvent reGroupMemberAssoc = new ReconcileEvent(ReconcileEvent.GROUP_MEMBER_ASSOCIATED_EVENT_NAME, "Group Member Associated", "An event that occures whenever a new member was associated to a resource group.");
		ReconcileEvent reGroupMemberDisso = new ReconcileEvent(ReconcileEvent.GROUP_MEMBER_DISSOCIATED_EVENT_NAME, "Group Member Dissociated", "An event that occures whenever a member was dissociated from a resource group.");
		ReconcileEvent reGroupMemberModify = new ReconcileEvent(ReconcileEvent.GROUP_MEMBERSHIP_MODIFY_EVENT_NAME, "Group Membership Modify", "An event that occures whenever a member(s) was/were associated/disocciated from a resource group.");
		
		
		reconcileEvents.add(reIdentityConfirmed);
		reconcileEvents.add(reIdentityRemoved);
		reconcileEvents.add(reIdentityUnmatched);
		reconcileEvents.add(reIdentityUnasigned);
		reconcileEvents.add(reIdentityModified);
		reconcileEvents.add(reIdentityAttributeModified);
		reconcileEvents.add(reGroupModified);
		reconcileEvents.add(reGroupCreated);
		reconcileEvents.add(reGroupRemoved);
		reconcileEvents.add(reGroupMemberAssoc);
		reconcileEvents.add(reGroupMemberDisso);
		reconcileEvents.add(reGroupMemberModify);
	}
	
	public void initSystemEvents() {
		SystemEvent systemEventUserCreationPre = new SystemEvent("USER_CREATION_PRE","User Creation - Pre", "An event that occurs before a user was successfully created");
		SystemEvent systemEventUserCreationPost = new SystemEvent("USER_CREATION_POST","User Creation - Post", "An event that occurs after a user was successfully created");
		SystemEvent systemEventTaskFailure = new SystemEvent("TASK_FAILURE","Task Failure", "An event that occurs when task execution fails");
		SystemEvent systemEventResourceReconciliationPost = new SystemEvent("RESOURCE_RECONCILIATION_POST","Resource Reconciliation - Post", "An event that occurs after resource reconciliation process is executed");
		
		systemEvents.add(systemEventUserCreationPre);
		systemEvents.add(systemEventUserCreationPost);
		systemEvents.add(systemEventTaskFailure);
		systemEvents.add(systemEventResourceReconciliationPost);
	}
	
	
	public void initReadyActions() {
		ReadyAction removeAccountReadyAction = new ReadyAction("REMOVE_ACCOUNT_FROM_REPOSITORY", "Remove an account from Velo repository", "velo.actions.readyActions.RemoveAccountFromRepository");
		ReadyAction addAccountToRepositoryReadyAction = new ReadyAction("ADD_ACCOUNT_TO_REPOSITORY", "Add an account to Velo repository", "velo.actions.readyActions.AddAccountToRepository");
		ReadyAction addResourceGroupToRepositoryReadyAction = new ReadyAction("ADD_RESOURCE_GROUP_TO_REPOSITORY", "Add a resource group to Velo repository", "velo.actions.readyActions.AddResourceGroupToRepository");
		ReadyAction removeResourceGroupFromRepositoryReadyAction = new ReadyAction("REMOVE_RESOURCE_GROUP_FROM_REPOSITORY", "Remove a resource group from Velo repository", "velo.actions.readyActions.RemoveResourceGroupFromRepository");
		ReadyAction updateRepoWithGroupMembershipAssociationReadyAction = new ReadyAction("UPDATE_REPO_WITH_GROUP_MEMBERSHIP_ASSOCIATION", "Update Repo With Grop Membership Association", "velo.actions.readyActions.UpdateRepoWithGroupMembershipAssociation");
		ReadyAction updateRepoWithGroupMembershipDissociationReadyAction = new ReadyAction("UPDATE_REPO_WITH_GROUP_MEMBERSHIP_DISSOCIATION", "Update Repo With Grop Membership Dissociation", "velo.actions.readyActions.UpdateRepoWithGroupMembershipDissociation");
		ReadyAction addAccountAndUserToRepositoryReadyAction = new ReadyAction("ADD_ACCOUNT_AND_USER_TO_REPOSITORY", "Add an Account & User to Repository", "velo.actions.readyActions.AddAccountAndUserToRepository");

		
		readyActions.add(removeAccountReadyAction);
		readyActions.add(addAccountToRepositoryReadyAction);
		readyActions.add(addResourceGroupToRepositoryReadyAction);
		readyActions.add(removeResourceGroupFromRepositoryReadyAction);
		readyActions.add(updateRepoWithGroupMembershipAssociationReadyAction);
		readyActions.add(updateRepoWithGroupMembershipDissociationReadyAction);
		readyActions.add(addAccountAndUserToRepositoryReadyAction);
	}
	
	
	
	private void buildResourceTypes() {
		ResourceType ldapV3RT = new ResourceType();
		ldapV3RT.setUniqueName("GENERIC_LDAP_V3");
		ldapV3RT.setScripted(true);
		ldapV3RT.setConfigurationTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><resource-descriptor><adapter><className desc=\"Adapter Class Name\" disabled=\"true\">velo.adapters.ActiveDirectoryAdapter</className><maxActive desc=\"Max Active Workers\">4</maxActive><maxIdle desc=\"Max Idle Workers\">2</maxIdle><maxWait desc=\"Max Worker Waiting Time in ms\">1000</maxWait><minEvictableIdleTimeMillis desc=\"Minimum Evictable Idle time in MS\">30000</minEvictableIdleTimeMillis></adapter><specific><host desc=\"Host Name\"></host><port desc=\"Port\">636</port><protocol desc=\"The protocol used to connect to the directory (ssl/clear)\">ssl</protocol><ssl-key-store desc=\"SSL Key Store path\"></ssl-key-store><ssl-trust-store desc=\"SSL Trust store path\"></ssl-trust-store><ssl-key-store-password desc=\"SSL Keystore Password\"></ssl-key-store-password><group><group-dn desc=\"This value is used in addition to the base DN when searching and loading groups, an example is ou=Groups. If no value is supplied, the subtree search will start from the base DN.\"></group-dn><group-object-class desc=\"The LDAP user object class typeto use when loading groups.\">groupOfUniqueNames</group-object-class><group-object-filter desc=\"The filter to use when searching group objects.\">(objectclass=groupOfUniqueNames)</group-object-filter><group-unique-name-attribute desc=\"The attribute field to use when loading the group unique name.\">cn</group-unique-name-attribute><group-display-name-attribute desc=\"The attribute field to use when loading the group display name.\">cn</group-display-name-attribute><group-description-attribute desc=\"The attribute field to use when loading the group description\">description</group-description-attribute><group-members-attribute desc=\"The attribute field to use when loading the group members.\">uniqueMember</group-members-attribute></group><account><account-dn desc=\"This value is used in addition to the base DN when searching and loading accounts, an example is ou=Users. If no value is supplied, the subtree search will start from the base DN.\"></account-dn><account-object-class desc=\"The LDAP account object class type o use when loading principals.\">inetorgperson</account-object-class><account-object-filter desc=\"The filter to use when searching user objects.\">(objectclass=inetorgperson)</account-object-filter><account-default-creation-dn desc=\"The value is used in addition to the base DN, this is the default account creation container. a value must be supplied.\"></account-default-creation-dn></account></specific></resource-descriptor>");
		ldapV3RT.setResourceControllerClassName("velo.resource.operationControllers.GenericLdapResourceController");
		ldapV3RT.setResourceControllerType(ResourceControllerType.SPML_GENERIC);


		//Create ResourceOperationDefinitions for current resource type
		//jdbc is open and supports all global actions
		ResourceTypeOperation ldapv3rtAddAccount = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("ADD_ACCOUNT"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtDelAccount = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("DELETE_ACCOUNT"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtSuspend = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("SUSPEND_ACCOUNT"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtResume = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("RESUME_ACCOUNT"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtAddGroup = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("ADD_GROUP"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtRemoveGroup = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("DELETE_GROUP"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtAddGroupMembership = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("ADD_GROUP_MEMBERSHIP"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtDelGroupMembership = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("DELETE_GROUP_MEMBERSHIP"), ldapV3RT, false,true,false);
		ResourceTypeOperation ldapv3rtModifyAccount = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("MODIFY_ACCOUNT"), ldapV3RT, false,false,false);
		ResourceTypeOperation ldapv3rtResourceReconciliation = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("RESOURCE_RECONCILIATION"), ldapV3RT, false,false,false);
		ResourceTypeOperation ldapv3rtResourceFetchActiveDataOffline = new ResourceTypeOperation(getLoadedResourceGlobalOperations().get("RESOURCE_FETCH_ACTIVE_DATA_OFFLINE"), ldapV3RT, false,true,false);
		ldapV3RT.getSupportedOperations().add(ldapv3rtAddAccount);
		ldapV3RT.getSupportedOperations().add(ldapv3rtDelAccount);
		ldapV3RT.getSupportedOperations().add(ldapv3rtSuspend);
		ldapV3RT.getSupportedOperations().add(ldapv3rtResume);
		ldapV3RT.getSupportedOperations().add(ldapv3rtAddGroup);
		ldapV3RT.getSupportedOperations().add(ldapv3rtRemoveGroup);
		ldapV3RT.getSupportedOperations().add(ldapv3rtAddGroupMembership);
		ldapV3RT.getSupportedOperations().add(ldapv3rtDelGroupMembership);
		ldapV3RT.getSupportedOperations().add(ldapv3rtModifyAccount);
		ldapV3RT.getSupportedOperations().add(ldapv3rtResourceReconciliation);
		ldapV3RT.getSupportedOperations().add(ldapv3rtResourceFetchActiveDataOffline);


		ResourceTypeAttribute ldapV3_DN = new ResourceTypeAttribute(
				"DN", "Distinguished Name","The context of the user",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr1.setResourceAttributeId(new Long(1));
		ldapV3_DN.setAccountId(true);
		ldapV3_DN.setResourceType(ldapV3RT);
		ldapV3RT.getResourceTypeAttributes().add(ldapV3_DN);


		resourceTypes.put(ldapV3RT.getUniqueName(),ldapV3RT);
	}



	
	public void persistTestData() {
		ResourceType jdbcResType = resourceManager.findResourceType("JDBC");
		PasswordPolicyContainer ppc = passwordManager.findPasswordPolicyContainer("DEFAULT");
		ReconcilePolicy rp = reconcileManager.findReconcilePolicy("Default");
		
		
		Resource resource = new Resource();
		resource.setAutoFetch(true);
		resource.setResourceType(jdbcResType);
		resource.setReconcilePolicy(rp);
		resource.setPasswordPolicyContainer(ppc);
		resource.setUniqueName("TESTAPP1");
		resource.setDisplayName("test application 1");
		resource.setDescription("A simple mysql test application 1");
		resource.setConfiguration(jdbcResType.getConfigurationTemplate());
		resource.setConfiguration(resource.getConfiguration().replace("></host>", ">localhost</host>"));
		resource.setConfiguration(resource.getConfiguration().replace("></port>", ">3306</port>"));
		resource.setConfiguration(resource.getConfiguration().replace("></dbName>", ">testapp1</dbName>"));
		resource.setConfiguration(resource.getConfiguration().replace("></driverName>", ">org.gjt.mm.mysql.Driver</driverName>"));
		resource.setConfiguration(resource.getConfiguration().replace("></urlTemplate>", ">jdbc:mysql://%h/%d</urlTemplate>"));
		ResourceAttribute ra1 = new ResourceAttribute(resource, "login_name", "Login Name", "Login Name!",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra1.setAccountId(true);
		ResourceAttribute ra2 = new ResourceAttribute(resource, "first_name", "First Name", "First Name!",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra2.setPersistence(true); ra2.setSynced(true);
		ResourceAttribute ra3 = new ResourceAttribute(resource, "last_name", "Last Name", "Last!",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra3.setPersistence(true); ra3.setSynced(true);
		ResourceAttribute ra4 = new ResourceAttribute(resource, "email", "Email", "Email!",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra4.setPersistence(true); ra4.setSynced(true);
		ResourceAttribute ra5 = new ResourceAttribute(resource, "status", "Status", "Status!",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra5.setPersistence(true); ra5.setSynced(true);
		ResourceAttribute ra6 = new ResourceAttribute(resource, "children_number", "Children Number", "Children Number!",AttributeDataTypes.INTEGER,true,true,0,3,1,1);
		ra6.setPersistence(true); ra6.setSynced(true);
		resource.getResourceAttributes().add(ra1); resource.getResourceAttributes().add(ra2); resource.getResourceAttributes().add(ra3); resource.getResourceAttributes().add(ra4);
		resource.getResourceAttributes().add(ra5); resource.getResourceAttributes().add(ra6);
		ResourceAdmin ra = new ResourceAdmin();
		ra.setUserName("testapp1");
		ra.setPassword("252-120-0-170-195-49-100-85-192-87-223-195-176-96-83-223");
		ra.setResource(resource);
		resource.getResourceAdmins().add(ra);
		
		
		em.persist(resource);
	}

























	public void generateResourcePrincipalsEncryptionKey() throws OperationException {
		String fileName = SysConf.getSysConf().getString(
		"system.directory.system_conf")
		+ "/"
		+ SysConf.VELO_KEYS_DIR
		+ "/"
		+ SysConf.getSysConf().getString(
		"system.files.targets_principals_encryption_key");
		
		
		
		log.debug("Key file name to be generated: " + fileName);
		String keyString = EncryptionUtils.generateKey();

		File f = new File(fileName);
		if (!f.isFile()) {
			try {
				f.createNewFile();
				FileUtils.setContents(f, keyString);
			} catch (IOException ex) {
				throw new OperationException(
						"Could not create key generation file for file name: '"
						+ fileName + "', due to: " + ex);
			}
		} else {
			throw new OperationException("Could not create key generation file for file name: '" + fileName + "', file already exists.");
		}
		
		log.info("Successfully generated resource principals encryption key.");
	}



	

	// HELPER
	public Configuration getConf() {
		return SysConf.getSysConf();

	}
	
	
	public Resource getMockedJdbcResource() {
		initInitialData();
		
		ResourceType jdbcResourceType = resourceTypes.get("JDBC");
		Resource resource = new Resource();
		resource.setResourceType(jdbcResourceType);
		resource.setUniqueName("TESTAPP1");
		resource.setDisplayName("test application 1");
		resource.setDescription("A simple mysql test application 1");
		resource.setConfiguration(jdbcResourceType.getConfigurationTemplate());
		
		resource.setConfiguration(resource.getConfiguration().replace("></host>", ">localhost</host>"));
		resource.setConfiguration(resource.getConfiguration().replace("></port>", ">3306</port>"));
		resource.setConfiguration(resource.getConfiguration().replace("></dbName>", ">testapp1</dbName>"));
		resource.setConfiguration(resource.getConfiguration().replace("></driverName>", ">org.gjt.mm.mysql.Driver</driverName>"));
		resource.setConfiguration(resource.getConfiguration().replace("></urlTemplate>", ">jdbc:mysql://%h/%d</urlTemplate>"));
		
		ResourceAttribute ra1 = new ResourceAttribute(resource, "login_name", "Login Name", "Login Name",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ra1.setAccountId(true);
		ResourceAttribute ra2 = new ResourceAttribute(resource, "first_name", "First Name", "First Name",AttributeDataTypes.STRING,true,true,0,255,1,1);
		ResourceAttribute ra3 = new ResourceAttribute(resource, "last_name", "Last Name", "Last",AttributeDataTypes.STRING,true,true,0,255,1,1);
		resource.getResourceAttributes().add(ra1); resource.getResourceAttributes().add(ra2); resource.getResourceAttributes().add(ra3);

		ResourceAdmin ra = new ResourceAdmin();
		ra.setUserName("testapp1");
		ra.setPassword("252-120-0-170-195-49-100-85-192-87-223-195-176-96-83-223");
		
		resource.getResourceAdmins().add(ra);
		return resource;
	}
	
	
	
	
	
	
	private void syncEntities(Set<Object> entities,Class clazzType) {
		Method uniqueColumnForSyncMethod = getMethodAnnotatedWithUniqueColumnForSync(clazzType);
		String uniqueColName = getBeanPropertyOfGetter(uniqueColumnForSyncMethod.getName());
		
		Iterator iter = entities.iterator();
		while (iter.hasNext()) {
			try {
				Object currEntity = (Object)iter.next();
				Object value = uniqueColumnForSyncMethod.invoke(currEntity);
				
				
				Query q = generateCountQueryForSyncEntities(uniqueColName, clazzType);
				q.setParameter(uniqueColName,value);
				
				Long amount = (Long)q.getSingleResult();
				if (amount < 1) {
					em.persist(currEntity);
				}
			} catch (Exception e) {
				throw new SynchronizationException(e);
			}
		}
	}
	
	private Query generateCountQueryForSyncEntities(String uniqueColumnName, Class clazzType) {
		String NL = System.getProperty("line.separator");
		
		//Generate the alias
		StringBuilder aliasBuilder = new StringBuilder();
        for (char c : clazzType.getName().toCharArray()) {
            if (Character.isUpperCase(c)) {
                aliasBuilder.append(Character.toLowerCase(c));
            }
        }
        String alias = aliasBuilder.toString();
        
     
        //Generate the query
        //Check whether an object with the same unique value exists
		StringBuilder results = new StringBuilder();
        results.append("SELECT ");
        results.append("COUNT(").append(alias).append(") ").append(NL);;
        results.append("FROM ").append(clazzType.getName()).append(' ').append(alias).append(NL);
        results.append("WHERE ").append(alias).append(".");
        results.append(uniqueColumnName).append(" =:").append(uniqueColumnName);
        
        
        Query q = em.createQuery(results.toString());
        
        
        return q;
	}
	
	
	private Method getMethodAnnotatedWithUniqueColumnForSync(Class clazz) {
		for (Method currMethod : clazz.getMethods()) {
			if (currMethod.isAnnotationPresent(UniqueColumnForSync.class)) {
				return currMethod;
			}
		}
		
		return null;
	}
	
	
	
	public void syncReconcileEvents() {
		initReconcileEvents();
		syncEntities(reconcileEvents,ReconcileEvent.class);
	}
	
	public void syncSystemEvents() {
		initSystemEvents();
		syncEntities(systemEvents,SystemEvent.class);
	}
	
	
	public void syncReadyActions() {
		initReadyActions();
		syncEntities(readyActions,ReadyAction.class);
	}
	
	private String getBeanPropertyOfGetter(String property) {
        return property.substring(3, 4).toLowerCase() + property.substring(4, property.length());
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// DEPENDENT ON RESOURCE TYPES !
	@Deprecated
	public void syncResourceTypeAttributes() throws OperationException {
		log.info("Syncing Resource Type Attributes...");
		Set<Object> resourceTypeAttributes = new HashSet<Object>();

		ResourceType adType = em.find(ResourceType.class, new Long(3));


		// AD action languages
		// sAMAccountName
		ResourceTypeAttribute attr1 = new ResourceTypeAttribute(
				"sAMAccountName", "SAM Account Name", "SAM Account Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr1.setResourceAttributeId(new Long(1));
		attr1.setAccountId(true);
		attr1.setResourceType(adType);
		resourceTypeAttributes.add(attr1);


		//CN
		ResourceTypeAttribute attr2 = new ResourceTypeAttribute(
				"cn", "Common Name", "Common Name",
				AttributeDataTypes.STRING, true, true, 1, 255, 1, 1);
		//attr2.setResourceAttributeId(new Long(2));
		attr2.setResourceType(adType);
		resourceTypeAttributes.add(attr2);


		try {
			mergeEntities(resourceTypeAttributes);
		}
		catch (Exception e) {
			throw new OperationException(e.toString());
		}
	}
	
	@Deprecated
	public void syncActionLanguages() throws OperationException {
		log.info("Syncing Default Action Languages...");

		initInitialData();

		try {
			mergeEntities(actionLanguages);
		} catch (Exception e) {
			throw new OperationException(e.toString());
		}
	}

	@Deprecated
	public void syncProductData(objectType objType) {
		if (objType == objectType.RESOURCE_TYPE) {
			//sync data
		}
	}


	
	@Deprecated
	public void syncResourceTypes() {
		initInitialData();
		buildResourceTypes();

		for (ResourceType currRT : resourceTypes.values()) {
			//try to load the entity from the DB
			ResourceType loadedRT = resourceManager.findResourceType(currRT.getUniqueName());

			//if no RT was found in repository then persist it.
			if (loadedRT == null) {
				log.info("Persisting resource type: " + currRT.getUniqueName() + ", attrs attached amount: " + currRT.getResourceTypeAttributes().size());
				em.persist(currRT);
			} else {
				//sync it.
			}
		}
	}

	@Deprecated
	public void mergeEntities(Set<Object> entities) throws Exception {
		log.debug("Syncing '" + entities.size() + "' entities...");
		for (Object entity : entities) {
			Long currEntityId = getIdAsLong(entity);

			log.trace("Trying to load entity ID '" + currEntityId
					+ "' of entity class type '" + entity.getClass().getName()
					+ "')");
			// Hibernate returns null if object was not found
			Object o = em.find(entity.getClass(), currEntityId);
			if (o == null) {
				log.trace("Entity was not found, persisting a new entity...");
				em.merge(entity);
			} else {
				log.trace("Entity was found, merging entity...");
				BaseEntity currEntity = (BaseEntity) o;
				currEntity.copyValues(entity);
				em.merge(currEntity);
			}
		}
	}

	// HELPER
	@Deprecated
	protected Long getIdAsLong(Object entity) throws Exception {
		Method idMethod = findIdProperty(entity);

		if (idMethod != null) {
			Object[] params = {};
			try {
				Object id = idMethod.invoke(entity, params);
				if (id == null) {
					throw new Exception("ID is null");
				} else {
					return Long.valueOf(id.toString());
				}
			} catch (Exception e) {
				throw new Exception(e.toString());
			}
		} else {
			throw new Exception(
					"Could not find any method annotated as ID on Entity class: "
					+ entity.getClass().getName());
		}
	}

	@Deprecated
	protected Method findIdProperty(Object entity) {
		return findIdMethod(entity.getClass());
	}

	@Deprecated
	protected Method findIdMethod(Class clazz) {
		for (Class currClazz = clazz; !currClazz.equals(Object.class); currClazz = currClazz
		.getSuperclass()) {
			// Iterate over the fields and find the Id field
			for (Method m : currClazz.getDeclaredMethods()) {
				if (!(m.isAccessible()))
					m.setAccessible(true);
				if (m.isAnnotationPresent(Id.class)) {
					return m;
				}
			}
		}
		return null;
	}

	private void persistEntities(Collection entities) {
		for (Object entity : entities) {
			log.info("PERSISTING obj: '" + entity + "' of CLASS: '" + entity.getClass().getName());
			em.persist(entity);
		}

		em.flush();
	}

	@Deprecated
	public void syncResourceActionDefinitions() {
		em.createNativeQuery("TRUNCATE VL_RESOURCE_OPERATION_DEF").executeUpdate();

		persistEntities(resourceGlobalOperations);
	}
}