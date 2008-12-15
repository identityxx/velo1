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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.entity.RoleJournalingEntry.RoleJournalingActionType;
import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.ValidationException;

/**
 * An entity that represents a Role within the system
 * 
 * @author Asaf Shakarchi
 */

// Seam annotations
@Name("role")
@Table(name = "VL_ROLE")
@Entity
@SequenceGenerator(name="RoleIdSeq",sequenceName="ROLE_ID_SEQ")
@NamedQueries( {
	@NamedQuery(name = "role.findByName", query = "SELECT object(role) FROM Role role WHERE UPPER(role.name) = :name"),
	
	
	
	
	
	
	
		@NamedQuery(name = "role.findRoleById", query = "SELECT object(role) FROM Role role WHERE role.roleId = :roleId"),
		@NamedQuery(name = "role.findRoleByName", query = "SELECT object(role) FROM Role role WHERE UPPER(role.name) = :name"),
		@NamedQuery(name = "role.isExistsByName", query = "SELECT count(role) FROM Role AS role WHERE UPPER(role.name) = :roleName"),
		@NamedQuery(name = "role.findAll", query = "SELECT object(role) FROM Role role"),
		@NamedQuery(name = "role.findAllActive", query = "SELECT object(role) FROM Role role"),
		@NamedQuery(name = "role.searchRolesByString", query = "SELECT object(role) from Role role WHERE UPPER(role.name) like :searchString OR UPPER(role.description) like :searchString") })
public class Role extends BaseEntity implements Serializable {
	private static Logger log = Logger.getLogger(Role.class.getName());
	private static final long serialVersionUID = 1987302492306161423L;

	private Long roleId;
	private String name;
	private String description;
	private boolean disabled;
	private boolean activeAccessSync;
	private List<Resource> resources;
	private Set<PositionRole> positionRoles = new HashSet<PositionRole>() ;
	private Set<ResourceGroup> resourceGroups = new HashSet<ResourceGroup>();
	private Set<RoleResourceAttribute> roleResourceAttributes = new HashSet<RoleResourceAttribute>();
	private List<RolesFolder> rolesFolders;
	private String info1;
	private String info2;
	private String info3;
	private WorkflowProcessDef workflowProcessDef;
	private boolean exposedToSelfService;
	
	
	private List<RoleApproversGroup> roleApproversGroups = new ArrayList<RoleApproversGroup>();
		
	private Set<RoleJournalingEntry> journaling;
	

	// Transients for groups modification process
	private Set<ResourceGroup> groupsToRevoke = new HashSet<ResourceGroup>();
	private Set<ResourceGroup> groupsToAssign = new HashSet<ResourceGroup>();

	
	
	// Transients for groups modification process
	private Set<ApproversGroup> approversGroupsToAssign = new HashSet<ApproversGroup>();
	private Set<RoleApproversGroup> roleApproversGroupsToDelete = new HashSet<RoleApproversGroup>();
	
	
	//transient for role resource attributes modification process
	//private Set<ResourceAttribute> resourceAttributesToAssign = new HashSet<ResourceAttribute>();
	private Set<RoleResourceAttribute> roleResourceAttributesToAssign = new HashSet<RoleResourceAttribute>();
	private Set<RoleResourceAttribute> roleResourceAttributesToDelete = new HashSet<RoleResourceAttribute>();
	
	/**
	 * This is a transient date property which is used as a helper property when
	 * user selects a role to add, then this is copied to the 'UserRole' entity.
	 */
	private Date expirationDate;
	private Date grantAccessDate;
	private boolean inherited = false;
	
	//transient, used only to show the affected users from a role modification
	private Set<User> affectedUsers;
	private List<PositionRole> affectedPositions;
	
	
	
	
	/**
	 * Set the ID of the entity
	 * 
	 * @param roleId
	 *            The ID of the entity to set
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	/**
	 * Get the ID of the entity
	 * 
	 * @return The ID of the entity to get
	 */
	@Id
	//@GeneratedValue
	@GeneratedValue(strategy=GenerationType.AUTO,generator="RoleIdSeq")
	@Column(name = "ROLE_ID")
	public Long getRoleId() {
		return roleId;
	}

	/**
	 * Set the name of the role
	 * 
	 * @param name
	 *            The name of the role to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the role
	 * 
	 * @return The name of the role
	 */
	
	@NotNull
	@Column(name = "NAME", nullable = false, unique = true)
	public String getName() {
		return name;
	}

	/**
	 * Set the description of the entity
	 * 
	 * @param description
	 *            The description string to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description of the entity
	 * 
	 * @return The description of the entity
	 */
	@Column(name = "DESCRIPTION", nullable = false)
	@NotNull
	public String getDescription() {
		return description;
	}

	
	/**
	 * @return the disabled
	 */
	@Column(name = "DISABLED", nullable=false)
	@NotNull
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}


	/**
	 * @return the activeAccessSync
	 */
	@Column(name = "ACTIVE_ACCESS_SYNC", nullable = false)
	@NotNull
	public boolean isActiveAccessSync() {
		return activeAccessSync;
	}

	/**
	 * @param activeAccessSync the activeAccessSync to set
	 */
	public void setActiveAccessSync(boolean activeAccessSync) {
		this.activeAccessSync = activeAccessSync;
	}

	/**
	 * Set the collection of the resources assigned to the role
	 * 
	 * @param resources
	 *            A collection of resources assigned to the role
	 */
	public void setResources(List<Resource> resources) {
		this.resources = resources;
		
	}

	/**
	 * Get a collection of resources assigned to the role
	 * 
	 * @return A collection with resource elements assigned to the role
	 */
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = velo.entity.Resource.class)
	@JoinTable(name = "VL_RESOURCES_TO_ROLES", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_ID"))
	// list is a must, otherwise seam's entityConverter does not work due to
	// UIData restrictions
	public List<Resource> getResources() {
		return resources;
	}

	/**
	 * @param resourceGroups
	 *            The resourceGroups to set.
	 */
	public void setResourceGroups(Set<ResourceGroup> resourceGroups) {
		this.resourceGroups = resourceGroups;
	}

	/**
	 * @return Returns the resourceGroups.
	 */
	// Cascade refresh, when 'manage groups in role' passed in gui, its better
	// to have the most updated state of groups in role since this functionality
	// is critical.
	@ManyToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, targetEntity = velo.entity.ResourceGroup.class)
	@JoinTable(name = "VL_RESOURCE_GROUPS_TO_ROLES", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_GROUP_ID"))
	public Set<ResourceGroup> getResourceGroups() {
		return resourceGroups;
	}

	/**
	 * Set the collection of the Roles Folders assigned to the role
	 * 
	 * @param rolesFolders
	 *            A collection of RolesFolders assigned to the role
	 */
	public void setRolesFolders(List<RolesFolder> rolesFolders) {
		this.rolesFolders = rolesFolders;
	}

	/**
	 * Get a collection of RolesFolders this role assigned to.
	 * 
	 * @return A collection of RolesFolders assigned to this role.
	 */
	@ManyToMany(
	fetch = FetchType.LAZY, targetEntity = velo.entity.RolesFolder.class)
	@JoinTable(name = "VL_ROLES_TO_ROLES_FOLDERS", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "ROLES_FOLDER_ID"))
	public List<RolesFolder> getRolesFolders() {
		return rolesFolders;
	}

	
	
	/**
	 * @return the info1
	 */
	@Column(name = "INFO1", nullable = true)
	public String getInfo1() {
		return info1;
	}

	/**
	 * @param info1
	 *            the info1 to set
	 */
	public void setInfo1(String info1) {
		this.info1 = info1;
	}

	/**
	 * @return the info2
	 */
	@Column(name = "INFO2", nullable = true)
	public String getInfo2() {
		return info2;
	}

	/**
	 * @param info2
	 *            the info2 to set
	 */
	public void setInfo2(String info2) {
		this.info2 = info2;
	}

	/**
	 * @return the info3
	 */
	@Column(name = "INFO3", nullable = true)
	public String getInfo3() {
		return info3;
	}

	/**
	 * @param info3
	 *            the info3 to set
	 */
	public void setInfo3(String info3) {
		this.info3 = info3;
	}
	
	/**
	 * @return the roleApproversGroups
	 */
	//@OneToMany(mappedBy="role")
	@OneToMany(mappedBy="primaryKey.role")
	public List<RoleApproversGroup> getRoleApproversGroups() {
		return roleApproversGroups;
	}

	/**
	 * @param roleApproversGroups the roleApproversGroups to set
	 */
	public void setRoleApproversGroups(List<RoleApproversGroup> roleApproversGroups) {
		this.roleApproversGroups = roleApproversGroups;
	}
	
	
	
	/**
	 * @return the roleResourceAttributes
	 */
	@OneToMany(mappedBy="primaryKey.role", cascade = CascadeType.ALL)
	public Set<RoleResourceAttribute> getRoleResourceAttributes() {
		return roleResourceAttributes;
	}

	/**
	 * @param roleResourceAttributes the roleResourceAttributes to set
	 */
	public void setRoleResourceAttributes(
			Set<RoleResourceAttribute> roleResourceAttributes) {
		this.roleResourceAttributes = roleResourceAttributes;
	}
	
	/**
	 * @return the journaling
	 */
	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("creationDate DESC")
	public Set<RoleJournalingEntry> getJournaling() {
		return journaling;
	}

	/**
	 * @param journaling the journaling to set
	 */
	public void setJournaling(Set<RoleJournalingEntry> journaling) {
		this.journaling = journaling;
	}

	/**
	 * @return the workflowProcessDef
	 */
	//@ManyToOne(cascade = { CascadeTy })
	@ManyToOne
	@JoinColumn(name = "WORKFLOW_PROCESS_DEF_ID", nullable = true, unique = false)
	public WorkflowProcessDef getWorkflowProcessDef() {
		return workflowProcessDef;
	}

	/**
	 * @param workflowProcessDef the workflowProcessDef to set
	 */
	public void setWorkflowProcessDef(WorkflowProcessDef workflowProcessDef) {
		this.workflowProcessDef = workflowProcessDef;
	}
	
	/**
	 * @return the exposedToSelfService
	 */
	@Column(name = "EXPOSED_TO_SELF_SERVICE", nullable=false)
	@NotNull
	public boolean isExposedToSelfService() {
		return exposedToSelfService;
	}

	/**
	 * @param exposedToSelfService the exposedToSelfService to set
	 */
	public void setExposedToSelfService(boolean exposedToSelfService) {
		this.exposedToSelfService = exposedToSelfService;
	}
		
	
	
	
	
	//HELPER / TRANSIENTS
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof Role))
			return false;
		Role ent = (Role) obj;
		if (this.roleId.equals(ent.roleId))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		if (roleId == null)
			return super.hashCode();
		return roleId.hashCode();
	}

	@Override
	public String toString() {
		return name + "(" + roleId + ")";
	}

	
	//proxy method for userRole.expirationDate
	/**
	 * @param expirationDate
	 *            The expirationDate to set.
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return Returns the expirationDate.
	 */
	@Transient
	public Date getExpirationDate() {
		return expirationDate;
	}
	
	/**
	 * @return the grantAccessDate
	 */
	@Transient
	public Date getGrantAccessDate() {
		return grantAccessDate;
	}

	/**
	 * @param grantAccessDate the grantAccessDate to set
	 */
	public void setGrantAccessDate(Date grantAccessDate) {
		this.grantAccessDate = grantAccessDate;
	}
	
	
	//proxy method for userRole.inherited;
	@Transient
	@Deprecated
	public boolean isInherited() {
		return inherited;
	}

	@Deprecated
	public void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	
	
	
	
	
	
	//some accessors
	@Transient
	public boolean isResourceGroupAssociated(ResourceGroup group) {
		for (ResourceGroup currGroupInRole : getResourceGroups()) {
			if (currGroupInRole.equals(group)) {
				return true;
			}
		}

		return false;
	}

	@Transient
	public boolean isResourceAssociated(Resource resource) {
		for (Resource currResourceInRole : getResources()) {
			if (currResourceInRole.getResourceId().equals(resource.getResourceId())) {
				return true;
			}
		}

		return false;
	}
	
	@Transient
	public boolean isResourceAssociated(String resourceUniqueName) {
		for (Resource currResourceInRole : getResources()) {
			if (currResourceInRole.getUniqueName().equals(resourceUniqueName)) {
				return true;
			}
		}

		return false;
	}
	
	public ResourceGroup getResourceGroup(String uniqueId) {
		for (ResourceGroup currGroup : getResourceGroups()) {
			if (currGroup.getUniqueId().equals(uniqueId)) {
				return currGroup;
			}
		}
		
		return null;
	}
	
	@Transient
	public Resource getResource(String uniqueName) {
		for (Resource currResourceInRole : getResources()) {
			if (currResourceInRole.getUniqueName().equals(uniqueName)) {
				return currResourceInRole;
			}
		}
		
		return null;
	}
	
	public Set<RoleResourceAttribute> getRoleResourceAttributes(Resource resource) {
		log.debug("Getting a list of all role resource attributes related to resource '" + resource.getDisplayName() + "'");
		Set<RoleResourceAttribute> roleResAttrs = new HashSet<RoleResourceAttribute>();
		for (RoleResourceAttribute currRRA : getRoleResourceAttributes()) {
			if (currRRA.getResourceAttribute().getResource().equals(resource)) {
				roleResAttrs.add(currRRA);
			}
		}
		
		log.debug("Constructed a list of role resource attributes with amount '" + roleResAttrs.size() + "'");
		return roleResAttrs;
	}
	
	public Set<RoleResourceAttribute> getRoleResourceAttributes(ResourceAttribute resourceAttribute) {
		Set<RoleResourceAttribute> roleResAttrs = new HashSet<RoleResourceAttribute>();
		for (RoleResourceAttribute currRRA : getRoleResourceAttributes(resourceAttribute.getResource())) {
			if (currRRA.getResourceAttribute().equals(resourceAttribute)) {
				roleResAttrs.add(currRRA);
			}
		}
		
		return roleResAttrs;
	}
	
	@Transient
	public boolean isApproversGroupAssociated(ApproversGroup ag) {
		for (RoleApproversGroup currRAG : getRoleApproversGroups()) {
			if (currRAG.getApproversGroup().getApproversGroupId().equals(ag.getApproversGroupId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isRoleResourceAttributeAssociated(RoleResourceAttribute rra) {
		for (RoleResourceAttribute currRRA : getRoleResourceAttributes()) {
			if (currRRA.equals(rra)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	//invoked before persisting/updating role entity
	public void validateRoleEntity() throws ValidationException {
        if (getResourceGroups() != null) {
            //Make sure that the role does not include groups of an un attached target.
            for (ResourceGroup rg : getResourceGroups()) {
                if (!isResourceAssociated(rg.getResource())) {
                    throw new ValidationException("Role name: '" + getName() + "' associated with ResourceGroup named: '" + rg.getDisplayName() + "', of Target: '" + rg.getResource().getDisplayName() + "', but the target is not associated with role!");
                }
            }
        }
    }
	
	
	public void addJournalingEntry(RoleJournalingActionType actionType, User by, String summaryMessage, String detailedMessage) {
    	if (journaling == null) journaling = new HashSet<RoleJournalingEntry>();
    	
    	getJournaling().add(new RoleJournalingEntry(this,actionType,by,summaryMessage,detailedMessage));
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Transient for groups modification process
	/**
	 * @return the groupsToRevoke
	 */
	@Transient
	public Set<ResourceGroup> getGroupsToRevoke() {
		return groupsToRevoke;
	}

	/**
	 * @param groupsToRevoke
	 *            the groupsToRevoke to set
	 */
	public void setGroupsToRevoke(Set<ResourceGroup> groupsToRevoke) {
		this.groupsToRevoke = groupsToRevoke;
	}

	public void addGroupToRevoke(ResourceGroup group) {
		getGroupsToRevoke().add(group);
	}

	public void removeGroupToRevoke(ResourceGroup group) {
		getGroupsToRevoke().remove(group);
	}

	/**
	 * @return the groupsToAssign
	 */
	@Transient
	public Set<ResourceGroup> getGroupsToAssign() {
		return groupsToAssign;
	}

	/**
	 * @param groupsToAssign
	 *            the groupsToAssign to set
	 */
	public void setGroupsToAssign(Set<ResourceGroup> groupsToAssign) {
		this.groupsToAssign = groupsToAssign;
	}

	public void addGroupToAssign(ResourceGroup group)
			throws CollectionElementInsertionException {
		// Make sure that the group is not already associated in role
		if (isResourceGroupAssociated(group)) {
			throw new CollectionElementInsertionException(
					"Group already associated to the current edited role");
		}

		getGroupsToAssign().add(group);
	}

	public void removeGroupToAssign(ResourceGroup group) {
		getGroupsToAssign().remove(group);
	}
	
	
	
	
	
	
	/**
	 * @return the roleApproversGroupsToDelete
	 */
	@Transient
	public Set<RoleApproversGroup> getRoleApproversGroupsToDelete() {
		return roleApproversGroupsToDelete;
	}

	/**
	 * @param approversGroupsToDelete the approversGroupsToDelete to set
	 */
	public void setRoleApproversGroupsToDelete(
			Set<RoleApproversGroup> roleApproversGroupsToDelete) {
		this.roleApproversGroupsToDelete = roleApproversGroupsToDelete;
	}
	
	public void removeRoleApproversGroupToDelete(RoleApproversGroup rag) {
		getRoleApproversGroupsToDelete().remove(rag);
	}
	
	/**
	 * @return the approversGroupsToAssign
	 */
	@Transient
	public Set<ApproversGroup> getApproversGroupsToAssign() {
		return approversGroupsToAssign;
	}

	/**
	 * @param approversGroupsToAssign the approversGroupsToAssign to set
	 */
	public void setApproversGroupsToAssign(Set<ApproversGroup> approversGroupsToAssign) {
		this.approversGroupsToAssign = approversGroupsToAssign;
	}
	
	public void addApproversGroupToAssign(ApproversGroup ag) throws CollectionElementInsertionException {
		// Make sure that the group is not already associated in role
		if (isApproversGroupAssociated(ag)) {
			throw new CollectionElementInsertionException(
					"Approvers Group already associated to the current edited role");
		}
		getApproversGroupsToAssign().add(ag);
	}
	
	public void addRoleApproversGroupToDelete(RoleApproversGroup rag) {
		getRoleApproversGroupsToDelete().add(rag);
	}
	
	public void removeApproversGroupToAssign(ApproversGroup ag) {
		getApproversGroupsToAssign().remove(ag);
	}
	
	
	
	
	
	
	//resource attributes management
	/**
	 * @return the roleResourceAttributesToAssign
	 */
	@Transient
	public Set<RoleResourceAttribute> getRoleResourceAttributesToAssign() {
		return roleResourceAttributesToAssign;
	}

	/**
	 * @param roleResourceAttributesToAssign the roleResourceAttributesToAssign to set
	 */
	public void setRoleResourceAttributesToAssign(
			Set<RoleResourceAttribute> roleResourceAttributesToAssign) {
		this.roleResourceAttributesToAssign = roleResourceAttributesToAssign;
	}

	
	public void addResourceAttributeToAssign(RoleResourceAttribute rra) throws CollectionElementInsertionException {
		// Make sure that the ra is not already associated in role
		if (isRoleResourceAttributeAssociated(rra)) {
			throw new CollectionElementInsertionException(
				"Resource Attribute already associated to the this role");
		}
		
		//FIXME: appearantly the set does not call equal, adding validation here but this is totally wrong.
		for (RoleResourceAttribute currRRA : getRoleResourceAttributesToAssign()) {
			if (currRRA.equals(rra)) {
				return;
			}
		}
		
		getRoleResourceAttributesToAssign().add(rra);
	}
	
	public void removeRoleResourceAttributeToAssign(RoleResourceAttribute rra) {
		getRoleResourceAttributesToAssign().remove(rra);
		log.debug("Removing  from roleResourceAttributeToAssign " + rra.getResourceAttribute().getDisplayName());
		log.debug("Now roleResourceAttributeToAssign contains " + getRoleResourceAttributesToAssign().size() + " elements");
	}
	
	/**
	 * @return the roleResourceAttributesToDelete
	 */
	@Transient
	public Set<RoleResourceAttribute> getRoleResourceAttributesToDelete() {
		return roleResourceAttributesToDelete;
	}

	/**
	 * @param roleResourceAttributesToDelete the roleResourceAttributesToDelete to set
	 */
	public void setRoleResourceAttributesToDelete(
			Set<RoleResourceAttribute> roleResourceAttributesToDelete) {
		this.roleResourceAttributesToDelete = roleResourceAttributesToDelete;
	}
	
	public void removeRoleResourceAttributeToDelete(RoleResourceAttribute rra) {
		getRoleResourceAttributesToDelete().remove(rra);
	}
	
	public void addRoleResourceAttributeToDelete(RoleResourceAttribute rra) {
		getRoleResourceAttributesToDelete().add(rra);
	}
	
	
	
	
	
	
	
	
	
	
	
	//used by the edit-groups/edit-roles interface for search purposes
	@Transient
	public Map<String,String> getResourceListString() {
		Map<String,String> resMap = new HashMap<String,String>();
		for (Resource currRes : getResources()) {
			resMap.put(currRes.getUniqueName(), currRes.getDisplayName());
		}

		return resMap;
	}
	
	
	
	
	
	
	
	
	
	
	


	/**
	 * @return the positionRoles
	 */
	@OneToMany(mappedBy="primaryKey.role", cascade = CascadeType.ALL)
	public Set<PositionRole> getPositionRoles() {
		return positionRoles;
	}

	/**
	 * @param positionRoles the positionRoles to set
	 */
	public void setPositionRoles(Set<PositionRole> positionRoles) {
		this.positionRoles = positionRoles;
	}

	/**
	 * @return the affectedUsers
	 */
	@Transient
	public Set<User> getAffectedUsers() {
		if (affectedUsers == null) {
			affectedUsers = new HashSet<User>();
		}
		
		return affectedUsers;
	}

	/**
	 * @param affectedUsers the affectedUsers to set
	 */
	public void setAffectedUsers(Set<User> affectedUsers) {
		this.affectedUsers = affectedUsers;
	}

	/**
	 * @return the affectedPositions
	 */
	@Transient
	public List<PositionRole> getAffectedPositions() {
		if (affectedPositions == null) {
			affectedPositions = new ArrayList<PositionRole>();
		}
		
		return affectedPositions;
	}

	/**
	 * @param affectedPositions the affectedPositions to set
	 */
	public void setAffectedPositions(List<PositionRole> affectedPositions) {
		this.affectedPositions = affectedPositions;
	}

	
	
	
	@Transient
	public Set<ResourceGroup> getResourceGroups(Resource resource) {
		Set<ResourceGroup> groups = new HashSet<ResourceGroup>();
		
		for (ResourceGroup currRG : getResourceGroups()) {
			if (currRG.getResource().equals(resource)) {
				groups.add(currRG);
			}
		}
		
		
		return groups;
	}

	
}
