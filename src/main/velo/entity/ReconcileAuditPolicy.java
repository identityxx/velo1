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
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import velo.entity.ReconcileProcessSummaryEvent.ReconcileProcessSummaryEvents;


/**
 * A class that represents a Reconcilidation audit policy
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Table(name = "VL_RECONCILE_AUDIT_POLICY")
@Entity
@SequenceGenerator(name="ReconcileAuditPolicyIdSeq",sequenceName="RECONCILE_AUDIT_POLICY_ID_SEQ")
public class ReconcileAuditPolicy extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1987305492306161223L;

	private Long reconcileAuditPolicyId;
	private String name;
	private boolean auditIdentityConfirmedEvent = false;
	private boolean auditIdentityUnmatchedEvent = true;
	private boolean auditIdentityUnassignedEvent = true;
	private boolean auditIdentityRemovedEvent = true;
	private boolean auditIdentityModifiedEvent = true;
	private boolean auditIdentityAttributeModifiedEvent = true;
	private boolean auditGroupModifiedEvent = true;
	private boolean auditGroupCreatedEvent = true;
	private boolean auditGroupRemovedEvent = true;
	private boolean auditGroupMembershipAssocEvent = true;
	private boolean auditGroupMembershipDessocEvent = true;

	public ReconcileAuditPolicy() {
		
	}
	
	public ReconcileAuditPolicy(String name) {
		setName(name);
	}

	public void setReconcileAuditPolicyId(Long reconcileAuditPolicyId) {
		this.reconcileAuditPolicyId = reconcileAuditPolicyId;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcileAuditPolicyIdSeq")
	@Column(name = "RECONCILE_AUDIT_POLICY_ID")
	public Long getReconcileAuditPolicyId() {
		return reconcileAuditPolicyId;
	}

	@Column(name="NAME", nullable=false, unique=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name="IDENTITY_CONFIRMED_EVENT",nullable=false)
	public boolean isAuditIdentityConfirmedEvent() {
		return auditIdentityConfirmedEvent;
	}

	public void setAuditIdentityConfirmedEvent(boolean auditIdentityConfirmedEvent) {
		this.auditIdentityConfirmedEvent = auditIdentityConfirmedEvent;
	}

	@Column(name="IDENTITY_UNMATCHED_EVENT",nullable=false)
	public boolean isAuditIdentityUnmatchedEvent() {
		return auditIdentityUnmatchedEvent;
	}

	public void setAuditIdentityUnmatchedEvent(boolean auditIdentityUnmatchedEvent) {
		this.auditIdentityUnmatchedEvent = auditIdentityUnmatchedEvent;
	}

	@Column(name="IDENTITY_UNASSIGNED_EVENT",nullable=false)
	public boolean isAuditIdentityUnassignedEvent() {
		return auditIdentityUnassignedEvent;
	}

	public void setAuditIdentityUnassignedEvent(boolean auditIdentityUnassignedEvent) {
		this.auditIdentityUnassignedEvent = auditIdentityUnassignedEvent;
	}

	@Column(name="IDENTITY_REMOVED_EVENT",nullable=false)
	public boolean isAuditIdentityRemovedEvent() {
		return auditIdentityRemovedEvent;
	}

	public void setAuditIdentityRemovedEvent(boolean auditIdentityRemovedEvent) {
		this.auditIdentityRemovedEvent = auditIdentityRemovedEvent;
	}

	@Column(name="IDENTITY_MODIFIED_EVENT",nullable=false)
	public boolean isAuditIdentityModifiedEvent() {
		return auditIdentityModifiedEvent;
	}

	public void setAuditIdentityModifiedEvent(boolean auditIdentityModifiedEvent) {
		this.auditIdentityModifiedEvent = auditIdentityModifiedEvent;
	}

	@Column(name="IDENTITY_ATTR_MODIFIED_EVENT",nullable=false)
	public boolean isAuditIdentityAttributeModifiedEvent() {
		return auditIdentityAttributeModifiedEvent;
	}

	public void setAuditIdentityAttributeModifiedEvent(
			boolean auditIdentityAttributeModifiedEvent) {
		this.auditIdentityAttributeModifiedEvent = auditIdentityAttributeModifiedEvent;
	}

	@Column(name="GROUP_MODIFIED_EVENT",nullable=false)
	public boolean isAuditGroupModifiedEvent() {
		return auditGroupModifiedEvent;
	}

	public void setAuditGroupModifiedEvent(boolean auditGroupModifiedEvent) {
		this.auditGroupModifiedEvent = auditGroupModifiedEvent;
	}

	@Column(name="GROUP_CREATED_EVENT",nullable=false)
	public boolean isAuditGroupCreatedEvent() {
		return auditGroupCreatedEvent;
	}

	public void setAuditGroupCreatedEvent(boolean auditGroupCreatedEvent) {
		this.auditGroupCreatedEvent = auditGroupCreatedEvent;
	}

	@Column(name="GROUP_REMOVED_EVENT",nullable=false)
	public boolean isAuditGroupRemovedEvent() {
		return auditGroupRemovedEvent;
	}

	public void setAuditGroupRemovedEvent(boolean auditGroupRemovedEvent) {
		this.auditGroupRemovedEvent = auditGroupRemovedEvent;
	}
	
	
	@Column(name="GROUP_MEMBER_ASSOC_EVENT",nullable=false)
	public boolean isAuditGroupMembershipAssocEvent() {
		return auditGroupMembershipAssocEvent;
	}

	public void setAuditGroupMembershipAssocEvent(
			boolean auditGroupMembershipAssocEvent) {
		this.auditGroupMembershipAssocEvent = auditGroupMembershipAssocEvent;
	}

	@Column(name="GROUP_MEMBER_DESSOC_EVENT",nullable=false)
	public boolean isAuditGroupMembershipDessocEvent() {
		return auditGroupMembershipDessocEvent;
	}

	public void setAuditGroupMembershipDessocEvent(
			boolean auditGroupMembershipDessocEvent) {
		this.auditGroupMembershipDessocEvent = auditGroupMembershipDessocEvent;
	}

	public boolean isEventAudited(ReconcileProcessSummaryEvents reconcileEvent) {
		switch(reconcileEvent) {
			case IDENTITY_CONFIRMED: return isAuditIdentityConfirmedEvent();
			case IDENTITY_UNASSIGNED: return isAuditIdentityUnassignedEvent();
			case IDENTITY_UNMATCHED: return isAuditIdentityUnmatchedEvent();
			case IDENTITY_REMOVED: return isAuditIdentityRemovedEvent();
			case IDENTITY_MODIFIED: return isAuditIdentityModifiedEvent();
			case IDENTITY_ATTRIBUTE_MODIFIED: return isAuditIdentityAttributeModifiedEvent();
			case GROUP_CREATED: return isAuditGroupCreatedEvent();
			case GROUP_REMOVED: return isAuditGroupRemovedEvent();
			case GROUP_MODIFIED: return isAuditGroupModifiedEvent();
			case GROUP_MEMBER_ASSOCIATED: return isAuditGroupMembershipAssocEvent();
			case GROUP_MEMBER_DISSOCIATED: return isAuditGroupMembershipDessocEvent();
			default: return false;
		}
	}
}