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
//@!@clean
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * A class that represents a Reconcilidation Users policy
 * 
 * @author Asaf Shakarchi
 */

// Seam annotations
@Name("reconcileUsersPolicy")
@Table(name = "VL_RECONCILE_USERS_POLICY")
@Entity
@SequenceGenerator(name="ReconcileUsersPolicyIdSeq",sequenceName="RECONCILE_USERS_POLICY_ID_SEQ")
@NamedQueries( {
		@NamedQuery(name = "reconcileUsersPolicy.findByUniqueName", query = "SELECT object(reconcileUsersPolicy) FROM ReconcileUsersPolicy reconcileUsersPolicy WHERE reconcileUsersPolicy.uniqueName = :uniqueName"),
		@NamedQuery(name = "reconcileUsersPolicy.findAll", query = "SELECT object(reconcileUsersPolicy) FROM ReconcileUsersPolicy reconcileUsersPolicy"),
		@NamedQuery(name = "reconcileUsersPolicy.findAllActive", query = "SELECT object(reconcileUsersPolicy) FROM ReconcileUsersPolicy reconcileUsersPolicy WHERE reconcileUsersPolicy.active = 1 ORDER BY reconcileUsersPolicy.priority ASC"),
		@NamedQuery(name = "reconcileUsersPolicy.searchReconcileUsersPoliciesByString", query = "SELECT object(reconcileUsersPolicy) from ReconcileUsersPolicy reconcileUsersPolicy WHERE reconcileUsersPolicy.uniqueName like :searchString") })
public class ReconcileUsersPolicy extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1987305492306161223L;

	private Long reconcileUsersPolicyId;

	private String uniqueName;

	private String displayName;

	private String newUserInSourceEventAction;

	private String deletedUserInSourceEventAction;

	private Resource sourceResource;

	private boolean active;

	private int priority;

	/**
	 * Set an ID for the entity
	 * 
	 * @param reconcileUsersPolicyId
	 *            The ID of the entity
	 */
	public void setReconcileUsersPolicyId(Long reconcileUsersPolicyId) {
		this.reconcileUsersPolicyId = reconcileUsersPolicyId;
	}

	/**
	 * Get the ID of the entity
	 * 
	 * @return The ID of the entity
	 */
	// GF@Id
	// GF@SequenceGenerator(name="IDM_RECONCILE_USERS_POLICY_GEN",sequenceName="IDM_RECONCILE_USERS_POLICY_SEQ",
	// allocationSize=1)
	// GF@GeneratedValue(strategy = GenerationType.SEQUENCE,
	// generator="IDM_RECONCILE_USERS_POLICY_GEN")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileUsersPolicyIdSeq")
	//@GeneratedValue
	// JB
	@Column(name = "RECONCILE_USERS_POLICY_ID")
	public Long getReconcileUsersPolicyId() {
		return reconcileUsersPolicyId;
	}

	/**
	 * The uniqueName of the entity to get
	 * 
	 * @return The uniqueName of the entity
	 */
	@Column(name = "UNIQUE_NAME", nullable = false)
	@Length(min = 3, max = 40)
	@NotNull
	// seam
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * The uniqueName of the entity to set
	 * 
	 * @param uniqueName
	 *            The Unique Name of the entity
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * The displayName of the entity to get
	 * 
	 * @return The displayName of the entity
	 */
	@Column(name = "DISPLAY_NAME", nullable = false)
	@Length(min = 3, max = 40)
	@NotNull
	// seam
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * The displayName of the entity to set
	 * 
	 * @param displayName
	 *            The Display Name of the entity
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @param newUserInSourceEventAction
	 *            the newUserInSourceEventAction to set
	 */
	public void setNewUserInSourceEventAction(String newUserInSourceEventAction) {
		this.newUserInSourceEventAction = newUserInSourceEventAction;
	}

	/**
	 * @return the newUserEventAction
	 */
	@Column(name = "NEW_USER_IN_SRC_EVENT_ACTION", nullable = false)
	public String getNewUserInSourceEventAction() {
		return newUserInSourceEventAction;
	}

	/**
	 * @param deletedUserInSourceEventAction
	 *            the deletedUserInSourceEventAction to set
	 */
	public void setDeletedUserInSourceEventAction(
			String deletedUserInSourceEventAction) {
		this.deletedUserInSourceEventAction = deletedUserInSourceEventAction;
	}

	/**
	 * @return the deletedUserEventAction
	 */
	// @Column(name = "DELETED_USER_IN_SRC_EVENT_ACTION", nullable = false)
	@Column(name = "DEL_USER_IN_SRC_EVENT_ACTION", nullable = false)
	public String getDeletedUserInSourceEventAction() {
		return deletedUserInSourceEventAction;
	}

	/**
	 * @param sourceResource
	 *            the sourceResource to set
	 */
	public void setSourceResource(Resource sourceResource) {
		this.sourceResource = sourceResource;
	}

	/**
	 * @return the sourceResource
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "SOURCE_RESOURCE_ID", nullable = false, unique = false)
	public Resource getSourceResource() {
		return sourceResource;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the active
	 */
	@Column(name = "ACTIVE", nullable = false)
	public boolean isActive() {
		return active;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the priority
	 */
	@Column(name = "PRIORITY")
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof ReconcileUsersPolicy))
			return false;
		ReconcileUsersPolicy ent = (ReconcileUsersPolicy) obj;
		if (this.reconcileUsersPolicyId.equals(ent.reconcileUsersPolicyId))
			return true;
		return false;
	}

}