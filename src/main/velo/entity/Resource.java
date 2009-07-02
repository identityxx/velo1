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
package velo.entity;
//@!@not
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLValue;

import velo.action.ResourceOperation;
import velo.common.SysConf;
import velo.contexts.OperationContext;
import velo.entity.ResourceAction.InvokePhases;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.ResourceDescriptorException;
import velo.exceptions.ScriptLoadingException;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import velo.rules.AccountsCorrelationRule;
import velo.scripting.ScriptFactory;
import velo.storage.ResourceAttributeSet;

/**
 * An entity that represents a resource within the system
 * 
 * @author Asaf Shakarchi
 */
@Name("resource")
// Seam annotations
@Entity
@Table(name = "VL_RESOURCE")
@SequenceGenerator(name="ResourceIdSeq",sequenceName="RESOURCE_ID_SEQ") 
@NamedQueries( {
	@NamedQuery(name = "resource.findByUniqueName", query = "SELECT object(r) FROM Resource r WHERE r.uniqueName = :uniqueName"),
	@NamedQuery(name = "resource.findAllActive", query = "SELECT res FROM Resource res WHERE res.active = 1")
})
/*
@NamedQueries( {
		@NamedQuery(name = "findResourceById", query = "SELECT object(ts) FROM Resource ts WHERE ts.resourceId = :id"),
		@NamedQuery(name = "findAllResources", query = "SELECT ts FROM Resource ts"),
		@NamedQuery(name = "resource.findAllActive", query = "SELECT object(ts) FROM Resource ts WHERE ts.active = 1"),
		@NamedQuery(name = "isAccountByNameExistOnResource", query = "SELECT count(account) FROM Account AS account WHERE account.name = :accountName AND account.resource = :resource"),
		@NamedQuery(name = "resource.searchByName", query = "SELECT object(resource) from Resource resource WHERE resource.uniqueName like :searchString"),
		@NamedQuery(name = "resource.isAssignedToRoles", query = "SELECT count(ts) FROM Resource as ts, Role AS role, IN (role.resources) resourceInRole WHERE resourceInRole = :resource") })
*/
public class Resource extends BaseEntity implements Serializable, Cloneable {
	private static final long serialVersionUID = 198735545231161223L;
	private static transient Logger log = Logger.getLogger(Resource.class.getName());
	
	private static Configuration sysConf;

	private Long resourceId;

	private String uniqueName;

	private String description;

	private String displayName;

	// private String hostName;
	//private String confFileName;

	private Set<ResourceAdmin> resourceAdmins = new HashSet<ResourceAdmin>();

	// private Set<Account> accounts = new HashSet<Account>();
	private Set<Account> accounts;

	private Set<AuditedAccount> auditedAccounts;

	private Set<ResourceGroup> groups;

	private Set<ResourceAttribute> resourceAttributes = new LinkedHashSet<ResourceAttribute>();
	
	private Set<ReconcileProcessSummary> ReconcileProcessSummaries;

	private ReconcilePolicy reconcilePolicy;

	private PasswordPolicyContainer passwordPolicyContainer;
	
	private Gateway gateway;

	private boolean isActive;

	private boolean caseSensitive;

	private boolean isScripted;

	private boolean isRequestableBySelfService;
	
	private boolean delAccountIfLastRoleRefToThisResourceIsRevoked = true; 
	
	private String configuration;
	
	private int daysToRevokeAccount;

	/**
	 * The corresponding resource type entity
	 */
	private ResourceType resourceType;

	/**
	 * The maximum allowed accounts per user
	 */
	private int maxAllowedAccountsPerUser = 1;
	
	private TasksQueue tasksQueue;

	private Set<ResourceAction> resourceActions;
	
	private ApproversGroup approversGroupOwner;
	
	private List<User> users;
	
	private boolean autoFetch;
	
	
	
	/**
	 * Set the ID of the entity
	 * 
	 * @param resourceId
	 *            The ID of the entity to set
	 */
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Get the ID of the entity
	 * 
	 * @return The ID of the entity to get
	 */
	@Id
	//@GeneratedValue
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceIdSeq") 
	@Column(name = "RESOURCE_ID")
	public Long getResourceId() {
		return resourceId;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Column(name = "UNIQUE_NAME", nullable = false, unique = true)
//makes troubles with seam search	@Length(min = 3, max = 50)
	@NotNull
	// seam
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Set the description of the Resource
	 * 
	 * @param description
	 *            The Description string to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description of the Resource
	 * 
	 * @return The Resource Description
	 */
	@Column(name = "DESCRIPTION", nullable = true)
//makes troubles with seam search	@Length(min = 5, max = 200)
	@NotNull
	// seam
	public String getDescription() {
		return description;
	}

	/**
	 * Set the display name of the Resource
	 * 
	 * @param displayName
	 *            The Resource DisplayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Get the display name of the Resource
	 * 
	 * @return The displayName of the Resource as a string
	 */
	@Column(name = "DISPLAY_NAME", nullable = false, unique = true)
//makes troubles with seam search	@Length(min = 3, max = 50)
	@NotNull
	// seam
	public String getDisplayName() {
		return displayName;
	}


	/*
	 * Set the Configuration file name of the Resource <b>(Only file name
	 * should be set, without the path to the target folder!)</b>
	 * 
	 * @param confFileName
	 *            The conf file name to set
	 *
	public void setConfFileName(String confFileName) {
		this.confFileName = confFileName;
	}
	*/

	/*
	 * Get the Configuration file name of the Resource
	 * 
	 * @return The configuration file name
	 *
	@Column(name = "CONF_FILE_NAME", nullable = false, unique = true)
	@Length(min = 4, max = 200)
	@NotNull
	// seam
	public String getConfFileName() {
		return confFileName;
	}
	*/

	/**
	 * Get a list of Resource Admin entities assigned to the Resource
	 * 
	 * @return A list of ResourceAdmin entities
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.EAGER)
	@OrderBy("priority")
	public Set<ResourceAdmin> getResourceAdmins() {
		return resourceAdmins;
	}

	/**
	 * Set a list of Resource Admin entities assigned to the Resource
	 * 
	 * @param resourceAdmins
	 *            A list of ResourceAdmin entities
	 */
	public void setResourceAdmins(Set<ResourceAdmin> resourceAdmins) {
		this.resourceAdmins = resourceAdmins;
	}

	/**
	 * Get a list of accounts entities related to the Resource
	 * 
	 * @return A list of accounts
	 */
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "resource", fetch = FetchType.LAZY)
	@OrderBy("accountId")
	// public Set<Account> getAccounts() {
	public Set<Account> getAccounts() {
		return accounts;
	}

	/**
	 * Set a list of accounts entities related to the Resource
	 * 
	 * @param accounts
	 *            A list of accounts
	 */
	// public void setAccounts(Set<Account> accounts) {
	public void setAccounts(Set<Account> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Get a list of accounts entities related to the Resource
	 * 
	 * @return A list of accounts
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY)
	public Set<AuditedAccount> getAuditedAccounts() {
		return auditedAccounts;
	}

	/**
	 * Set a list of accounts entities related to the Resource
	 * 
	 * @param accounts
	 *            A list of accounts
	 */
	public void setAuditedAccounts(Set<AuditedAccount> auditedAccounts) {
		this.auditedAccounts = auditedAccounts;
	}
	

	/**
	 * @return the approversGroupOwner
	 */
	@ManyToOne(optional = true)
	@JoinColumn(name = "APPROVERS_GROUP_OWNER_ID", nullable = true)
	public ApproversGroup getApproversGroupOwner() {
		return approversGroupOwner;
	}

	/**
	 * @param approversGroupOwner the approversGroupOwner to set
	 */
	public void setApproversGroupOwner(ApproversGroup approversGroupOwner) {
		this.approversGroupOwner = approversGroupOwner;
	}

	/**
	 * Transient, used by Digester to push the account found in the XML for
	 * dynamically loading accounts by XML file
	 * 
	 * @param acc
	 *            The account to add to the account list of this entity
	 */
	@Transient
	public void addAccount(Account acc) {
		if (getAccounts() == null) {
			accounts = new HashSet<Account>();
		}

		getAccounts().add(acc);
	}

	/**
	 * @param groups
	 *            The groups to set.
	 */
	public void setGroups(Set<ResourceGroup> groups) {
		this.groups = groups;
	}

	/**
	 * @return Returns the groups.
	 */
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "resource", fetch = FetchType.LAZY)
	public Set<ResourceGroup> getGroups() {
		return groups;
	}

	/**
	 * Get the ResourceType entity of the Resource
	 * 
	 * @return The ResourceType entity of the Resource
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "RESOURCE_TYPE_ID", nullable = true)
	public ResourceType getResourceType() {
		return resourceType;
	}

	/**
	 * Set the resourceType entity of the resource
	 * 
	 * @param resourceType
	 *            The resourceType entity of the resource
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * Set a list of ResourceAttribute entities related to this Resource
	 * 
	 * @param resourceAttributes
	 *            A list of ResourceAttributes to set
	 */
	public void setResourceAttributes(
			Set<ResourceAttribute> resourceAttributes) {
		this.resourceAttributes = resourceAttributes;
	}

	/**
	 * Get a list of ResourceAttribute entities related to this Resource
	 * 
	 * @return The list of ResourceAttributes
	 */
	@OneToMany(mappedBy = "resource", fetch = FetchType.LAZY, cascade = {
			CascadeType.PERSIST, CascadeType.REMOVE })
	@OrderBy("priority DESC")
	@Deprecated //never use this directly externally, use getAttributes() instead!
	public Set<ResourceAttribute> getResourceAttributes() {
		return resourceAttributes;
	}
	
	
	@OneToMany(mappedBy = "resource", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@OrderBy("startDate DESC")
	public Set<ReconcileProcessSummary> getReconcileProcessSummaries() {
		return ReconcileProcessSummaries;
	}

	public void setReconcileProcessSummaries(
			Set<ReconcileProcessSummary> reconcileProcessSummaries) {
		ReconcileProcessSummaries = reconcileProcessSummaries;
	}

	/**
	 * Set the ReconcilePolicy entity for the Resource
	 * 
	 * @param reconcilePolicy
	 *            A ReconcilePlicy entity to set
	 */
	public void setReconcilePolicy(ReconcilePolicy reconcilePolicy) {
		this.reconcilePolicy = reconcilePolicy;
	}

	/**
	 * Get a ReconcilePolicy entity attached to the Resource
	 * 
	 * @return A ReconcilePolicy entity
	 */
	// @ManyToOne(optional=false, cascade = CascadeType.ALL)
	@ManyToOne(optional = false)
	// Field is nullable, because Resource may not have an attached sync
	// policy (coulld get inherited from ResourceType / General Default
	// Synch Policy)
	@JoinColumn(name = "RECONCILE_POLICY_ID", nullable = true, unique = false)
	public ReconcilePolicy getReconcilePolicy() {
		return reconcilePolicy;
	}

	/**
	 * @param passwordPolicyContainer
	 *            the passwordPolicyContainer to set
	 */
	public void setPasswordPolicyContainer(
			PasswordPolicyContainer passwordPolicyContainer) {
		this.passwordPolicyContainer = passwordPolicyContainer;
	}

	/**
	 * @return the passwordPolicyContainer
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "PASSWORD_POLICY_CONTAINER_ID", nullable = false, unique = false)
	public PasswordPolicyContainer getPasswordPolicyContainer() {
		return passwordPolicyContainer;
	}

	/**
	 * Set whether the Resource is active or not
	 * 
	 * @param isActive
	 *            Set an indicator of whether the Resource is active or not
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Whether a Resource is active or not
	 * 
	 * @return true/false upon active / not active
	 */
	@Column(name = "IS_ACTIVE", nullable = false)
	@NotNull
	// seam
	public boolean isActive() {
		return isActive;
	}

	@Column(name = "CASE_SENSITIVE", nullable = false)
	@NotNull
	// seam
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Set whether the Resource is scripted or not
	 * 
	 * @param isScripted
	 *            The isScriptable to set.
	 */
	public void setScripted(boolean isScripted) {
		this.isScripted = isScripted;
	}

	/**
	 * Whether a Resource is scripted or not
	 * 
	 * @return true/false upon scripted / not scripted
	 */
	@Column(name = "IS_SCRIPTED", nullable = false)
	@NotNull
	// seam
	public boolean isScripted() {
		return isScripted;
	}

	/**
	 * @param isRequestableBySelfService
	 *            The isRequestableBySelfService to set.
	 */
	public void setRequestableBySelfService(boolean isRequestableBySelfService) {
		this.isRequestableBySelfService = isRequestableBySelfService;
	}

	/**
	 * @return Returns the isRequestableBySelfService.
	 */
	@Column(name = "REQUESTABLE_BY_SELF_SERVICE", nullable = false)
	public boolean isRequestableBySelfService() {
		return isRequestableBySelfService;
	}
	
	
	/**
	 * @return the delAccountIfLastRoleRefToThisResourceIsRevoked
	 */
	@Column(name = "DEL_ACC_WHEN_LAST_ROLE_REVOKED", nullable = false)
	public boolean isDelAccountIfLastRoleRefToThisResourceIsRevoked() {
		return delAccountIfLastRoleRefToThisResourceIsRevoked;
	}

	/**
	 * @param delAccountIfLastRoleRefToThisResourceIsRevoked the delAccountIfLastRoleRefToThisResourceIsRevoked to set
	 */
	public void setDelAccountIfLastRoleRefToThisResourceIsRevoked(
			boolean delAccountIfLastRoleRefToThisResourceIsRevoked) {
		this.delAccountIfLastRoleRefToThisResourceIsRevoked = delAccountIfLastRoleRefToThisResourceIsRevoked;
	}

	/**
	 * @param maxAllowedAccountsPerUser
	 *            The maxAllowedAccountsPerUser to set.
	 */
	public void setMaxAllowedAccountsPerUser(int maxAllowedAccountsPerUser) {
		this.maxAllowedAccountsPerUser = maxAllowedAccountsPerUser;
	}

	/**
	 * @return Returns the maxAllowedAccountsPerUser.
	 */
	@Column(name = "MAX_ALLOWED_ACCTS_PER_USER", nullable = false)
	//@Length(min = 1, max = 100)
	//@NotNull
	public int getMaxAllowedAccountsPerUser() {
		return maxAllowedAccountsPerUser;
	}
	
	
	/**
	 * Set the TasksQueue
	 * 
	 * @param tasksQueue a taskQueue entity to set
	 */
	public void setTasksQueue(TasksQueue tasksQueue) {
		this.tasksQueue = tasksQueue;
	}

	/**
	 * Get a TasksQueue entity attached to the Resource
	 * 
	 * @return A TasksQueue entity
	 */
	@ManyToOne
	@JoinColumn(name = "TASKS_QUEUE_ID", nullable = true, unique = false)
	public TasksQueue getTasksQueue() {
		return tasksQueue;
	}
	
	
	/**
	 * @return the resourceActions
	 */
	@OneToMany(mappedBy = "resource", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@OrderBy("description")
	public Set<ResourceAction> getResourceActions() {
		return resourceActions;
	}

	/**
	 * @param resourceActions the resourceActions to set
	 */
	public void setResourceActions(Set<ResourceAction> resourceActions) {
		this.resourceActions = resourceActions;
	}
	
	/**
	 * @return the configuration
	 */
	@Column(name = "CONFIGURATION", nullable = false)
	@Lob
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	@Column(name = "DAYS_TO_REVOKE_ACCOUNT", nullable = false)
	public int getDaysToRevokeAccount() {
		return daysToRevokeAccount;
	}

	public void setDaysToRevokeAccount(int daysToRevokeAccount) {
		this.daysToRevokeAccount = daysToRevokeAccount;
	}

	/**
	 * @return the gateway
	 */
	@ManyToOne
	@JoinColumn(name = "GATEWAY_ID", nullable = true, unique = false)
	public Gateway getGateway() {
		return gateway;
	}

	/**
	 * @param gateway the gateway to set
	 */
	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}
	
	@OneToMany(mappedBy = "sourceResource", fetch = FetchType.LAZY)
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	@Column(name = "AUTO_FETCH", nullable = false)
	public boolean isAutoFetch() {
		return autoFetch;
	}

	public void setAutoFetch(boolean autoFetch) {
		this.autoFetch = autoFetch;
	}
	
	
	
	
	
	

	

	//helper/transients
	@Transient
	public ResourceAttributeSet<ResourceAttribute> getAttributes() {
		log.trace("Getting resource attributes(calculated from all overridings)");
		//Map<String,Object> attrs = new HashMap<String,Object>(); 
		Map<String,Object> attrs = new LinkedHashMap<String,Object>();
		
		//Iterate over the resource level, which is the strongest and add all attributes
		log.trace("Adding 'resource level' attributes with amount '" + getResourceAttributes().size() + "'");
		for (ResourceAttribute ra : getResourceAttributes()) {
			attrs.put(ra.getUniqueName(), ra);
		}
		
		//Iterate over the resource type overridden level and add them all
		//Conflicts will be handled by the map itself which is unique
		Set<ResourceTypeAttribute> rtaOverriddenList = getResourceType().getOverriddenResourceTypeAttributes();
		log.trace("Adding 'resource type overridden level' attributes with amount '" + rtaOverriddenList.size() + "'");
		for (ResourceAttributeBase ra : rtaOverriddenList) {
			if (!attrs.containsKey(ra.getUniqueName())) {
				attrs.put(ra.getUniqueName(),ra);
			} else {
				log.trace("Skipping insertion of resource type overridden attribute named '" + ra.getUniqueName() + "', already added by 'Resource-Level'");
			}
		}
		
		//Iterate over the resource type default shipped attributes
		Set<ResourceTypeAttribute> rtaNotOverriddenList = getResourceType().getNotOverriddenResourceTypeAttributes();
		log.trace("Adding 'resource type NOT overridden level' attributes with amount '" + rtaNotOverriddenList.size() + "'");
		for (ResourceAttributeBase ra : rtaNotOverriddenList) {
			if (!attrs.containsKey(ra.getUniqueName())) {
				attrs.put(ra.getUniqueName(), ra);
			} else {
				log.trace("Skipping insertion of 'resource type NOT overridden attribute' named '" + ra.getUniqueName() + "', already added by 'Resource-Level/Resource-Type overridden level'");
			}
		}
		
		
		
		
		ResourceAttributeSet<ResourceAttribute> attrsSet = new ResourceAttributeSet<ResourceAttribute>();
		for (Object currObj : attrs.values()) {
			if (currObj instanceof ResourceAttribute) {
				ResourceAttribute ra = (ResourceAttribute)currObj;
				attrsSet.add(ra);
			}
			else {
				//Factory a virtual resource attribute based on the ResourceTypeAttribute
				ResourceAttributeBase rab = (ResourceAttributeBase)currObj;
				ResourceAttribute ra = ResourceAttribute.factoryVirtually(rab, this);

				attrsSet.add(ra);
			}
		}
		
		return attrsSet;
	}
	
	@Transient
	public List<ResourceAttribute> getAttributesToSync() {
		log.debug("Getting attributes to synchronize for resource '" + getDisplayName() + "'");
		
		List<ResourceAttribute> list = new ArrayList<ResourceAttribute>();
		
		for (ResourceAttribute currRA : getAttributes()) {
			if (currRA.isSynced()) {
				list.add(currRA);
			}
		}
		
		log.debug("Returning a list of attributes to synchronize with amount '" + list.size() + "'");
		
		return list;
	}
	
	public ResourceAttribute getResourceAttribute(String uniqueName) {
		if (isCaseSensitive()) {
			return findAttributeByName(uniqueName, false);
		} else {
			return findAttributeByName(uniqueName, true);
		}
	}
	
	private ResourceAttribute getResourceAttributeIgnoreCase(String uniqueName) {
		return findAttributeByName(uniqueName, true);
	}
	
	public ResourceAttribute findAttributeByName(String uniqueName, boolean ignoreCase) {
		for (ResourceAttribute currRA : getAttributes()) {
			if (ignoreCase) {
				if (currRA.getUniqueName().toUpperCase().equals(uniqueName.toUpperCase())) {
					return currRA;
				}
			} else {
				if (currRA.getUniqueName().equals(uniqueName)) {
					return currRA;
				}
			}
		}
		
		return null;
	}
	
	@Transient
	public boolean isResourceAttributeExistsInResourceLevel(String uniqueName) {
		//note: iteration only occurs on the -local- attributes
		for (ResourceAttribute currAttr : getResourceAttributes()) {
			if (currAttr.getUniqueName().equals(uniqueName)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Transient
	public ResourceAttribute getAccountIdAttribute() {
		//System.out.println("^^^^^^^^^^^^^^^^^ ATTRS SIZE: " + getAttributes().size());
		for (ResourceAttribute currAttr : getAttributes()) {
			//System.out.println("^^^^^^^^^^^^^^ IS CURR ATTR NAMED '" + currAttr.getUniqueName() + "' is account ID? '" + currAttr.isAccountId());
			if (currAttr.isAccountId()) {
				return currAttr;
			}
		}
		
		return null;
	}
	
	@Transient
	public ResourceAttribute getPasswordAttribute() {
		for (ResourceAttribute currAttr : getAttributes()) {
			if (currAttr.isPasswordAttribute()) {
				return currAttr;
			}
		}
		
		return null;
	}
	
	
	
	@Transient
	public Set<ResourceAction> getPrePhaseActions(ResourceTypeOperation resourceTypeOperation) {
		Set<ResourceAction> actions = new HashSet<ResourceAction>();
		for (ResourceAction currRA : getResourceActions()) {
			if ( (currRA.getInvokePhase() == InvokePhases.PRE) && currRA.getResourceTypeOperation().equals(resourceTypeOperation)) {
				actions.add(currRA);
			}
		}
		
		return actions;
	}
	
	
	@Transient
	public Set<ResourceAction> getCreationPhaseActions(ResourceTypeOperation resourceTypeOperation) {
		Set<ResourceAction> actions = new HashSet<ResourceAction>();
		for (ResourceAction currRA : getResourceActions()) {
			if ( (currRA.getInvokePhase() == InvokePhases.CREATION) && currRA.getResourceTypeOperation().equals(resourceTypeOperation)) {
				actions.add(currRA);
			}
		}
		
		return actions;
	}
	
	
	@Transient
	public Set<ResourceAction> getValidatePhaseActions(ResourceTypeOperation resourceTypeOperation) {
		Set<ResourceAction> actions = new HashSet<ResourceAction>();
		for (ResourceAction currRA : getResourceActions()) {
			if ( (currRA.getInvokePhase() == InvokePhases.VALIDATE) && currRA.getResourceTypeOperation().equals(resourceTypeOperation)) {
				actions.add(currRA);
			}
		}
		
		return actions;
	}
	
	
	@Transient
	public Set<ResourceAction> getPostPhaseActions(ResourceTypeOperation resourceTypeOperation) {
		Set<ResourceAction> actions = new HashSet<ResourceAction>();
		for (ResourceAction currRA : getResourceActions()) {
			if ( (currRA.getInvokePhase() == InvokePhases.POST) && currRA.getResourceTypeOperation().equals(resourceTypeOperation)) {
				actions.add(currRA);
			}
		}
		
		return actions;
	}
	

	//public ResourceDescriptor factoryResourceDescriptor() throws ResourceDescriptorException {
	public ResourceDescriptor factoryResourceDescriptor() throws ResourceDescriptorException {
		
        try {
        	ResourceDescriptor rd = new ResourceDescriptor();
        	rd.loadResourceXmlConfiguration(getConfiguration());

        	return rd;
        }catch (ConfigurationException e) {
        	throw new ResourceDescriptorException(e.getMessage());
        }
        
        
        
        /* OLD VIA DIGESTER
        //ResourceDescriptorReader tsDescriptorReader = new ResourceDescriptorReader();
        try {
            //	Parse the configuration
        	tsDescriptorReader.parse(getConfiguration());
        	//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^CONF: " + getConfiguration());
        	//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^DESCR: " + tsDescriptorReader.getResourceDescriptor());
        	//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ADAPTER: " + tsDescriptorReader.getResourceDescriptor().getResourceDescriptorAdapter());
            return tsDescriptorReader.getResourceDescriptor();
        } catch (IOException ioe) {
            throw new ResourceDescriptorException(ioe.getMessage());
        } catch(SAXException saxe) {
            throw new ResourceDescriptorException(saxe.getMessage());
        }
        */
    }
	
	
	@Transient
	public boolean isOperationSupported(String uniqueName) {
		return getResourceType().isOperationSupported(uniqueName);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof Resource))
			return false;
		Resource ent = (Resource) obj;
		if (this.resourceId.equals(ent.resourceId))
			return true;
		return false;
	}
	
	
	//for the 'add request' performing method, construct a map with all attributes and their values from the
	//SPML DSML attributes, this map will be used by the actions in the 'Add Account' operation by the Integrator
	//i.e: attrs['firstname'].values[0].asString
	@Transient
	public Map<String,ResourceAttribute> getManagedAttributesAsMap(List<DSMLAttr> attrs) {
		log.debug("Constructing a MAP with managed Resource Attributes based on the specified SPML DSMLAttribute List for resource '" + getDisplayName() + "'");
		Map<String,ResourceAttribute> veloAttrs = new HashMap<String,ResourceAttribute>();
		Map<String,DSMLAttr> dsmlAttrs = getDSMLAttrListAsMap(attrs);

		
		//FIXME: What the fuck? these validations should be done when the spml is created, why here?
		for (ResourceAttribute currResAttr : getAttributes()) {
			log.trace("Iterating over resource attribute with unique name '" + currResAttr.getUniqueName() + "', verifying whether it is managed or not...");
			if (!currResAttr.isManaged()) {
				log.trace("Resource Attribute is NOT managed, continuing to the next one...");
				continue;
			}
			else {
				log.trace("Resource Attribute is managed, loading its values by the correpsonding DSMLAttribute...");
			}
			
			log.trace("Trying to find corresponding DSMLAttribute for current iterated managed Resource Attribute...");
			//get the DSML attr for the current iterated Managed Resource-Attribute
			DSMLAttr correspondingDSMLAttr = dsmlAttrs.get(currResAttr.getUniqueName());
			if (correspondingDSMLAttr != null) {
				log.trace("Corresponding DSMLAttribute was found with values amount '-"+correspondingDSMLAttr.getValues().length + "-', copying its values into the resource attribute entity!");
				//TODO: Support DSML Schema, for integrity by SPML level
				for (DSMLValue currDSMLValue : correspondingDSMLAttr.getValues()) {
					try {
						//TODO: is a '<value>' tag in SPML means null or empty string???
						if (currDSMLValue.getValue() != null) {
							currResAttr.addValue(currDSMLValue.getValue(), currResAttr.getDataType());
						} else {
							log.warn("Skipping DSML Value as its value is null");
						}
					} catch (AttributeSetValueException e) {
						log.warn("Could not add DSMLValue: " + e.toString() + ", how come? dsmlValue.getValue() always return a string datatype");
						continue;
					}
				}
			}
			else {
				log.debug("Could not find corresponding DSMLAttribute for current iterated Resource-Attribute!, continuing to the next managed Resource Attribute...");
			}
			
			veloAttrs.put(currResAttr.getUniqueName(), currResAttr);
		}
		
		
		return veloAttrs;
	}

	//helper method for the above method
	@Transient
	private Map<String,DSMLAttr> getDSMLAttrListAsMap(List<DSMLAttr> attrs) {
		log.debug("constructing a MAP of DSML Attributes where map.entry<key=DSML Attribute Name,value=DSMLAttr object>...");
		
		log.debug("Iterating over '" + attrs.size() + "' DSML Attributes");
		Map<String,DSMLAttr> attrMap = new HashMap<String,DSMLAttr>();
		for (DSMLAttr dsmlAttr : attrs) {
			log.trace("Putting DSML Attribute named '" + dsmlAttr.getName() + "' into the map...");
			attrMap.put(dsmlAttr.getName(), dsmlAttr);
		}
		
		log.debug("Finished DSML Map construction...");
		return attrMap;
	}
	
	
	
	public void clearActiveAttributeValues() {
		log.debug("Clearning attributes's active values of resource '" + getDisplayName() + "'");
		for (ResourceAttribute currAttr : getAttributes()) {
			currAttr.getValues().clear();
		}
	}
	
	@Transient
	public ResourceAdmin getFirstResourceAdmin() {
		if (getResourceAdmins().size() > 0) {
			return getResourceAdmins().iterator().next();
		}
		
		return null;
	}
	
	
	
	
	public ResourceOperation factoryResourceOperation(OperationContext context, ResourceTypeOperation resourceTypeOperation) {
		//return ResourceOperation.Factory(resource, context, resourceTypeOperation);
		ResourceOperation ro = new ResourceOperation(this,context);
		
		ro.setCreationPhaseResourceActions(this.getCreationPhaseActions(resourceTypeOperation));
		ro.setValidateResourceActions(this.getValidatePhaseActions(resourceTypeOperation));
		ro.setPreResourceActions(this.getPrePhaseActions(resourceTypeOperation));
		ro.setPostResourceActions(this.getPostPhaseActions(resourceTypeOperation));
		ro.setResourceTypeOperation(resourceTypeOperation);
		
		return ro;
	}
	
	
	/**
	 * Creates a map of accounts, the key is the account name in the right case.
	 * @return
	 */
	@Transient
	public Map<String,Account> getAccountsAsMap() {
		Map<String,Account> map = new HashMap<String,Account>();
		for (Account currAccount : getAccounts()) {
			map.put(currAccount.getNameInRightCase(),currAccount);
		}
		
		return map;
	}
	
	
	@Transient
	public Map<String,ResourceGroup> getGroupsAsMap() {
		Map<String,ResourceGroup> map = new HashMap<String,ResourceGroup>();
		for (ResourceGroup currRG : getGroups()) {
			map.put(currRG.getUniqueIdInRightCase(),currRG);
		}
		
		return map;
	}
	

	//not the best idea to use this as there might be a lot of groups
	@Transient
	public ResourceGroup findGroup(String uniqueId) {
		for (ResourceGroup currRG : getGroups()) {
			if (currRG.getUniqueId().equals(uniqueId)) {
				return currRG;
			}
		}
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	//UNCLEANED

	/**
	 * Factory a correlation rule for the Resource
	 * 
	 * @return A AccountsCorrelationRule object
	 * @throws ScriptLoadingException
	 */
	@Transient
	public AccountsCorrelationRule correlationRuleFactory()
			throws ScriptLoadingException {
		if (sysConf == null) {
			sysConf = SysConf.getSysConf();
		}

		String scriptResourceName = sysConf
				.getString("system.directory.user_workspace_dir")
				+ "/targets_scripts/"
				+ this.getUniqueName().toLowerCase()
				+ "/"
				+ "rules"
				+ "/"
				+ "accounts_correlation_rule"
				+ SysConf.getSysConf().getString("scripts.file_extension");
		try {
			//System.outprintln("Factoring account correlation rule for resource name: "+ scriptResourceName);
			ScriptFactory sf = new ScriptFactory();
			return (AccountsCorrelationRule) sf
					.factoryScriptableObjectByResourceName(scriptResourceName);
		} catch (ScriptLoadingException sle) {
			// System.out.println("Cannot factory a correlation rule for target
			// system name: " + this.getDisplayName() + ", message: " +
			// sle.getMessage());
			throw new ScriptLoadingException(
					"Cannot factory a correlation rule for target system name: "
							+ this.getDisplayName() + ", message: "
							+ sle.getMessage());
		}
	}

	/*
	 * @Override public boolean equals(Object obj) { //System.out.println("Obj
	 * in entity's equals() is: " + obj); //System.out.println("TS EQUALS()
	 * ?!?!"); if (!(obj != null && obj instanceof Resource)) return false;
	 * Resource ent = (Resource) obj; if
	 * (this.resourceId.equals(ent.resourceId)) return true; return
	 * false; }
	 */


	@Transient
	public String factorySyncFileName() {
		if (sysConf == null) {
			sysConf = SysConf.getSysConf();
		}

		String scriptResourceName = SysConf.getVeloWorkspaceDir()
				+ "/"
				+ sysConf.getString("system.directory.targets_files_dir")
				+ "/"
				+ this.getUniqueName().toLowerCase()
				+ "/"
				+ sysConf.getString("system.directory.sync_files_dir_per_ts")
				+ "/" + this.getUniqueName().toLowerCase() + "_sync" + ".xml";

		return scriptResourceName;
	}

	@Transient
	public long countManagedResourceAttributes() {
		long counter = 0;
		for (ResourceAttributeBase currAttr : getAttributes()) {
			if (currAttr.isManaged()) {
				counter++;
			}
		}

		return counter;
	}

	public void clearReferences() {
		// Not cleaning admins since they are required for the adapter in
		// actions
		accounts = null;
		groups = null;
		passwordPolicyContainer = null;

		setLoaded(false);
	}

	@Override
	public Resource clone() {
		try {
			Resource clonedEntity = (Resource) super.clone();

			// TODO: SHOULD CLONE ALL REFERENCES!

			return clonedEntity;
		} catch (CloneNotSupportedException cnfe) {
			System.out.println("Couldnt clone class: "
					+ this.getClass().getName() + ", with exception message: "
					+ cnfe.getMessage());
			return null;
		}
	}
	
	//not needed anymore, kept in DB
	@Deprecated
	@Transient
	public String getConfigurationFilename() {
		//String descriptorFileName = getResourceWorkspaceFolder() + "/" + SysConf.getSysConf().getString("system.directory.conf_dir_per_ts") + "/" + getConfFileName();
		
		//return descriptorFileName
		return null;
	}
	
	@Transient
	public String getResourceWorkspaceFolder() {
		String wsFolder = SysConf.getVeloWorkspaceDir() + "/" +
		SysConf.getSysConf().getString("system.directory.targets_files_dir") + "/"
		+ getUniqueName().toLowerCase();
		
		return wsFolder;
	}
	
	public void createResourceWorkspaceFolder() throws IOException {
		String wsDirString = getResourceWorkspaceFolder();
		File tsDir = new File(wsDirString);
		
		if (tsDir.isDirectory()) {
			throw new IOException("Resource Directory '" + getResourceWorkspaceFolder() + "' already exists!");
		}
		else {
			if (!tsDir.mkdir()) {
				throw new IOException("Could not create Resource folder for unknown reason.");
			}
		}
		
		/*
		//create a templated conf file
		File confFile = new File(getConfigurationFilename());
		if (confFile.createNewFile()) {
			FileUtils.setContents(confFile, getResourceType().getConfFileTemplate());
		}
		else {
			throw new IOException("Could not create Target-System templated conf file for unknown reason.");
		}
		*/
		
		String syncedFileName = wsDirString + "/" + "sync_files";
		File syncFiles = new File(syncedFileName);
		syncFiles.mkdir();
	}
	
	public void copyValues(Object entity) {
    	//TODO: Implement
    }

	
	
}
