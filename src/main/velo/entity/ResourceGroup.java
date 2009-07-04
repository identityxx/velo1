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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Name;

import velo.exceptions.LoadGroupByMapException;

/**
 * An entity that represents an resource Group within a Target System
 * 
 * @author Asaf Shakarchi
 */
@Entity
@Table(name = "VL_RESOURCE_GROUP")
@SequenceGenerator(name="ResourceGroupIdSeq",sequenceName="RESOURCE_GROUP_ID_SEQ")
@Name("resourceGroup")
// Seam name
@NamedQueries( {
	@NamedQuery(name = "resourceGroup.isExistIgnoreCase", query = "SELECT count(rg) FROM ResourceGroup rg WHERE UPPER(rg.uniqueId) = :uniqueId AND rg.resource.uniqueName = :resourceUniqueName"),
	@NamedQuery(name = "resourceGroup.isExistWithCase", query = "SELECT count(rg) FROM ResourceGroup rg WHERE rg.uniqueId = :uniqueId AND rg.resource.uniqueName = :resourceUniqueName"),
	@NamedQuery(name = "resourceGroup.findByUniqueIdWithCase", query = "SELECT rg FROM ResourceGroup rg WHERE rg.uniqueId = :uniqueId AND rg.resource.uniqueName = :resourceUniqueName"),
	@NamedQuery(name = "resourceGroup.findByUniqueIdIgnoreCase", query = "SELECT rg FROM ResourceGroup rg WHERE upper(rg.uniqueId) = upper(:uniqueId) AND rg.resource.uniqueName = :resourceUniqueName"),

	

	@NamedQuery(name = "resourceGroup.findByDisplayName", query = "SELECT object(tsg) FROM ResourceGroup AS tsg WHERE tsg.displayName = :displayName"),
	// With Description @NamedQuery(name =
	// "resourceGroup.findByString",query = "SELECT object(tsg) FROM
	// resourceGroup AS tsg WHERE ( ( (UPPER(tsg.displayName) like
	// :searchString) OR ( UPPER(tsg.uniqueId) like :searchString ) OR (
	// UPPER(tsg.description) like :searchString ) ) AND (tsg.managed = 1)
	// AND (tsg.resource = :resource) )"),
	@NamedQuery(name = "resourceGroup.findByString", query = "SELECT object(tsg) FROM ResourceGroup AS tsg WHERE ( ( (UPPER(tsg.displayName) like :searchString) OR ( UPPER(tsg.uniqueId) like :searchString ) ) AND (tsg.managed = 1) AND (tsg.resource = :resource) )"),
	@NamedQuery(name = "resourceGroup.isGroupExistOnTarget", query = "SELECT count(tsg) FROM ResourceGroup AS tsg WHERE tsg.resource = :resource AND tsg.uniqueId = :uniqueId") })
	public class ResourceGroup extends BaseEntity implements Serializable, Cloneable {
	private static final long serialVersionUID = 1987305452306161213L;

	private static Logger log = Logger.getLogger(ResourceGroup.class.getName());

	/**
	 * ID of the entity
	 */
	private Long resourceGroupId;

	/**
	 * Group's unique id as represented by the target system
	 */
	private String uniqueId;

	/**
	 * Group's display name
	 */
	private String displayName;

	/**
	 * Description
	 */
	private String description;

	/**
	 * Group's type
	 */
	//private String type = "generic";
	private String type;

	/**
	 * The Target System the group is related to
	 */
	private Resource resource;

	//private Set<Account> members;
	private Set<ResourceGroupMember> members = new HashSet<ResourceGroupMember>();

	private Boolean isManaged;

	private Boolean isDeletedInResource;

	private Date firstTimeGroupBeingDeletedInResource;

	private int numberOfReconcilesGroupKeepsBeingDeletedInResource = 0;

	private Set<Role> roles;

	
	public ResourceGroup() {
		setManaged(true);
		setDeletedInResource(false);
	}
	
	public ResourceGroup(String uniqueId, String displayName, String description, Resource resource) {
		setUniqueId(uniqueId);
		setDisplayName(displayName);
		setDescription(description);
		setResource(resource);
		setManaged(true);
		setDeletedInResource(false);
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceGroupIdSeq")
	//@GeneratedValue
	// JB
	@Column(name = "RESOURCE_GROUP_ID")
	public Long getResourceGroupId() {
		return resourceGroupId;
	}

	public void setResourceGroupId(Long resourceGroupId) {
		this.resourceGroupId = resourceGroupId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Column(name = "UNIQUE_ID")
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param displayName
	 *            The display name to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return Returns the display name.
	 */
	@Column(name = "DISPLAY_NAME", nullable = false)
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	@Lob
	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return Returns the type.
	 */
	@Column(name = "TYPE", nullable = false)
	public String getType() {
		return type;
	}

	/**
	 * @param resource
	 *            The resource to set.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return Returns the resource.
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "RESOURCE_ID", nullable = false)
	public Resource getResource() {
		return resource;
	}






	/**
	 * @param accounts
	 *            The member accounts to set.
	 */
	//	public void setMembers(Set<Account> members) {
	//		this.members = members;
	//	}

	//	/**
	//	 * @return Returns the accounts that are member of this group.
	//	 */
	//	@ManyToMany(
	//	// cascade={CascadeType.PERSIST, CascadeType.MERGE},
	//
	//	// cascade={CascadeType.PERSIST, CascadeType.MERGE},
	//	fetch = FetchType.LAZY, targetEntity = velo.entity.Account.class)
	//	@JoinTable(
	//	// name="ACCOUNTS_TO_TARGET_SYSTEM_GROUP",
	//	name = "VL_ACCOUNTS_TO_RESOURCE_GRPS", joinColumns = @JoinColumn(name = "GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "ACCOUNT_ID"))
	//	public Set<Account> getMembers() {
	//		return members;
	//	}

	@OneToMany(mappedBy="primaryKey.resourceGroup")
	public Set<ResourceGroupMember> getMembers() {
		return members;
	}

	public void setMembers(Set<ResourceGroupMember> members) {
		this.members = members;
	}

	/**
	 * @param isManaged
	 *            The isManaged to set.
	 */
	public void setManaged(boolean isManaged) {
		this.isManaged = isManaged;
	}

	/**
	 * @return Returns the isManaged.
	 */
	@Column(name = "MANAGED", nullable = false)
	public boolean isManaged() {
		return isManaged;
	}

	/**
	 * @param isDeletedInResource
	 *            the isDeletedInResource to set
	 */
	public void setDeletedInResource(boolean isDeletedInResource) {
		this.isDeletedInResource = isDeletedInResource;
	}

	/**
	 * @return the isDeletedInTarget
	 */
	@Column(name = "DELETED_IN_RESOURCE", nullable = false)
	public boolean isDeletedInResource() {
		return isDeletedInResource;
	}

	/**
	 * @param firstTimeGroupBeingDeletedInResource
	 *            the firstTimeGroupBeingDeletedInResource to set
	 */
	public void setFirstTimeGroupBeingDeletedInResource(
			Date firstTimeGroupBeingDeletedInResource) {
		this.firstTimeGroupBeingDeletedInResource = firstTimeGroupBeingDeletedInResource;
	}

	/**
	 * @return the firstTimeGroupBeingDeletedInTarget
	 */
	@Temporal(TemporalType.TIMESTAMP)
	// @Column(name="FIRST_TIME_GROUP_BEING_DELETED_IN_TARGET")
	@Column(name = "FIRST_TIME_GRP_BEING_DEL_IN_RS")
	public Date getFirstTimeGroupBeingDeletedInResource() {
		return firstTimeGroupBeingDeletedInResource;
	}

	/**
	 * @param numberOfReconcilesGroupKeepsBeingDeletedInResource
	 *            the numberOfReconcilesGroupKeepsBeingDeletedInResource to set
	 */
	public void setNumberOfReconcilesGroupKeepsBeingDeletedInResource(
			int numberOfReconcilesGroupKeepsBeingDeletedInResource) {
		this.numberOfReconcilesGroupKeepsBeingDeletedInResource = numberOfReconcilesGroupKeepsBeingDeletedInResource;
	}

	/**
	 * @return the numberOfReconcilesGroupKeepsBeingDeletedInTarget
	 */
	// @Column(name="NUMBER_OF_RECONCILES_GROUP_KEEPS_BEING_DELETED_IN_TARGET",
	// nullable=false)
	@Column(name = "NUM_OF_REC_GRP_KEEPS_DEL_IN_TS", nullable = false)
	public int getNumberOfReconcilesGroupKeepsBeingDeletedInResource() {
		return numberOfReconcilesGroupKeepsBeingDeletedInResource;
	}



	/**
	 * Check whether an account is a member of the group
	 * 
	 * @param account
	 *            The account name to check membership for
	 * @return true/false upon member/not a member
	@Transient
	public boolean isAccountMember(Account account) {
		for (Account currMember : getMembers()) {
			if (currMember.getAccountId() == account.getAccountId()) {
				return true;
			}
		}

		return false;
	}
	 */



	/**
	 * @return the roles
	 */
	@ManyToMany(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY, targetEntity = velo.entity.Role.class)
	@JoinTable(name = "VL_RESOURCE_GROUPS_TO_ROLES", joinColumns = @JoinColumn(name = "RESOURCE_GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
	public Set<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}



	public static ResourceGroup factory(String uniqueId, String displayName, String description, String type, Resource resource) {
		ResourceGroup rg = new ResourceGroup();

		rg.setDisplayName(displayName);
		rg.setUniqueId(uniqueId);
		rg.setDescription(description);
		rg.setResource(resource);
		rg.setType(type);
		rg.setCreationDate(new Date());

		return rg;
	}


	@Transient
	public String getUniqueIdInRightCase() {
		if (getResource().isCaseSensitive()) {
			return getUniqueId();
		} else {
			return getUniqueId().toUpperCase();
		}
	}

	
	//mainly used by the JDBC adapter
	public void load(Map map, Resource resource) throws LoadGroupByMapException {
		try {
			if (((String) map.get("display_name") == null)) {
				throw new LoadGroupByMapException(
				"'display_name' is a must and does not exist in MAP!");
			}

			if (((String) map.get("unique_id").toString() == null)) {
				throw new LoadGroupByMapException(
				"'uniqueId' is a must and does not exist in MAP!");
			}
			
			if (((String) map.get("unique_id").toString() == null)) {
				throw new LoadGroupByMapException(
				"'type' is a must and does not exist in MAP!");
			}

//			if (map.containsKey("type")) {
//				this.setType((String) map.get("type"));
//			}

			this.setType((String) map.get("type"));
			this.setDisplayName((String) map.get("display_name"));
			this.setUniqueId((String) map.get("unique_id").toString());
			this.setDescription((String) map.get("description"));
			this.setResource(resource);

			log.trace("Loaded group: uniqueId '" + getUniqueId() + ", displayName: '" + getDisplayName() + "', type '" + getType() + "', description: '" + getDescription());
		} catch (NullPointerException npe) {
			throw new LoadGroupByMapException(npe);
		}
	}


	//Compare by name, this is mainly used for reconcile import process as IDs do not exist in active groups imports
	public boolean isMemberExistByName(Account account) {
		for (ResourceGroupMember currRGM : getMembers()) {
			if (currRGM.getAccount().getNameInRightCase().equals(account.getNameInRightCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	//mainly for group membership reconciliation
	@Transient
	public Map<String,ResourceGroupMember> getMembersAsMap() {
		Map<String,ResourceGroupMember> map = new HashMap<String,ResourceGroupMember>();
		for (ResourceGroupMember currMember : getMembers()) {
			map.put(currMember.getAccount().getNameInRightCase(),currMember);
		}
		
		return map;
	}
















	


	public ResourceGroup clone() {
		try {
			ResourceGroup clonedTSG = (ResourceGroup) super.clone();
			// Since clone only clone the object but not all references within
			// it, then we must clone all of its properties as well

			return clonedTSG;

		}

		catch (CloneNotSupportedException cnfe) {
			System.out.println("Couldnt clone class: "
					+ this.getClass().getName() + ", with exception message: "
					+ cnfe.getMessage());
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof ResourceGroup))
			return false;
		ResourceGroup ent = (ResourceGroup) obj;
		if (this.resourceGroupId.equals(ent.resourceGroupId))
			return true;
		return false;
	}
}
