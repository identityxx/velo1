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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * A class that represents a Reconcilidation policy and rules
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("reconcilePolicy")
@Table(name = "VL_RECONCILE_POLICY")
@Entity
@SequenceGenerator(name="ReconcilePolicyIdSeq",sequenceName="RECONCILE_POLICY_ID_SEQ")
@NamedQueries( {
    @NamedQuery(name = "findAllReconcilePolicy", query = "select object(o) from ReconcilePolicy o"),
    @NamedQuery(name = "reconcilePolicy.findByName", query = "SELECT object(reconcilePolicy) FROM ReconcilePolicy reconcilePolicy WHERE reconcilePolicy.name = :reconcilePolicyName"),
    @NamedQuery(name = "reconcilePolicy.findAll", query = "SELECT object(reconcilePolicy) FROM ReconcilePolicy reconcilePolicy"),
    @NamedQuery(name = "reconcilePolicy.searchReconcilePoliciesByString", query = "SELECT object(reconcilePolicy) from ReconcilePolicy reconcilePolicy WHERE reconcilePolicy.name like :searchString") })
    public class ReconcilePolicy extends BaseEntity implements Serializable {
        
        private static final long serialVersionUID = 1987305492306161223L;
        
        //public static enum ConfirmedAccountEventOptions {NOTHING}
        //public static enum DeletedAccountEventOptions {NOTHING,UNLINK_RESOURCE_ACCOUNT_FROM_USER,CREATE_RESOURCE_ACCOUNT_FOR_USER};
        //public static enum UnasignedAccountEventOptions {NOTHING,LINK_RESOURCE_ACCOUNT_TO_USER,DELETE_RESOURCE_ACCOUNT,DISABLE_RESOURCE_ACCOUNT};
        //public static enum UnmatchedAccountEventOptions {NOTHING,CREATE_NEW_USER_BASED_ON_RESOURCE_ACCOUNT,DELETE_RESOURCE_ACCOUNT,DISABLE_RESOURCE_ACCOUNT};
        
        private Long reconcilePolicyId;
        
        private String name;
        
        //private ConfirmedAccountEventOptions confirmedAccountEventAction;
        //private DeletedAccountEventOptions deletedAccountEventAction;
        //private UnasignedAccountEventOptions unasignedAccountEventAction;
        //private UnmatchedAccountEventOptions unmatchedAccountEventAction;
        
        private String confirmedAccountEventAction;
        
        private String deletedAccountEventAction;
        
        private String unasignedAccountEventAction;
        
        private String unmatchedAccountEventAction;
        
        private String confirmedAccountAttributeEventAction;
        
        private String unmatchedAccountAttributeEventAction;
        
        private boolean isDeleteGroupAfterReconcileProcessesNumberExceeded;
        
        private int reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup = 0;
        
        private boolean isActivateCorrelationRule;
        
        private boolean reconcileAccounts;
        
        private boolean reconcileGroups;
        
        private boolean reconcileGroupMembership;
        
        private boolean autoCorrelateAccountIfMatchedToUser;
        
        private ReconcileResourceCorrelationRule reconcileResourceCorrelationRule;
        
        private boolean activateReconcileSummaries;
        
        /**
         * Set an ID for the entity
         * @param reconcilePolicyId The ID of the entity
         */
        public void setReconcilePolicyId(Long reconcilePolicyId) {
            this.reconcilePolicyId = reconcilePolicyId;
        }
        
        /**
         * Get the ID of the entity
         * @return The ID of the entity
         */
        //GF@Id
        //Gf@SequenceGenerator(name="IDM_RECONCILE_POLICY_GEN",sequenceName="IDM_RECONCILE_POLICY_SEQ", allocationSize=1)
        //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_RECONCILE_POLICY_GEN")
        @Id
        @GeneratedValue(strategy=GenerationType.AUTO,generator="ReconcilePolicyIdSeq")
        //@GeneratedValue //JB
        @Column(name = "RECONCILE_POLICY_ID")
        public Long getReconcilePolicyId() {
            return reconcilePolicyId;
        }
        
        /**
         * The name of the entity to get
         * @return The name of the entity
         */
        @Column(name = "NAME", nullable = false)
        @Length(min = 3, max = 40)
        @NotNull
        //seam
        public String getName() {
            return name;
        }
        
        /**
         * The name of the entity to set
         * @param name The name of the entity
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /*
         public void setConfirmedAccountEventAction(ConfirmedAccountEventOptions confirmedAccountEventAction) {
         this.confirmedAccountEventAction = confirmedAccountEventAction;
         }
         @Column(name="CONFIRMED_ACCOUNT_EVENT_ACTION", nullable=false)
         @Enumerated(EnumType.STRING)
         public ConfirmedAccountEventOptions getConfirmedAccountEventAction() {
         return confirmedAccountEventAction;
         }
         */
        /**
         * Set the 'Confirmed Account Event' action
         * @param confirmedAccountEventAction
         */
        public void setConfirmedAccountEventAction(
            String confirmedAccountEventAction) {
            this.confirmedAccountEventAction = confirmedAccountEventAction;
        }
        
        /**
         * Get the 'Confirmed Account Event' action
         * @return Get the action string
         */
        @Column(name = "CONFIRMED_ACCT_EVENT_ACTION", nullable = false)
        @NotNull
        //@Enumerated(EnumType.STRING)
        public String getConfirmedAccountEventAction() {
            return confirmedAccountEventAction;
        }
        
        /**
         * Set the 'Deleted Account Event' action
         * @param deletedAccountEventAction The action string
         */
        public void setDeletedAccountEventAction(String deletedAccountEventAction) {
            this.deletedAccountEventAction = deletedAccountEventAction;
        }
        
        /**
         * Get the 'Deleted Account Event' action string
         * @return The action String
         */
        @Column(name = "DELETED_ACCT_EVENT_ACTION", nullable = false)
        @NotNull
        //@Enumerated(EnumType.STRING)
        public String getDeletedAccountEventAction() {
            return deletedAccountEventAction;
        }
        
        /*
         public void setUnasignedAccountEventAction(UnasignedAccountEventOptions unasignedAccountEventAction) {
         this.unasignedAccountEventAction = unasignedAccountEventAction;
         }
         @Column(name="UNASIGNED_ACCOUNT_EVENT_ACTION", nullable=false)
         @Enumerated(EnumType.STRING)
         public UnasignedAccountEventOptions getUnasignedAccountEventAction() {
         return unasignedAccountEventAction;
         }
         */
        
        /**
         * Get the 'Unasigned Account Event' action string
         * @param unasignedAccountEventAction The action string
         */
        public void setUnasignedAccountEventAction(
            String unasignedAccountEventAction) {
            this.unasignedAccountEventAction = unasignedAccountEventAction;
        }
        
        /**
         * Set the 'Unasigned Account Event' action string
         * @return The action string
         */
        @Column(name = "UNASIGNED_ACCT_EVENT_ACTION", nullable = false)
        @NotNull
        //@Enumerated(EnumType.STRING)
        public String getUnasignedAccountEventAction() {
            return unasignedAccountEventAction;
        }
        
        /*
         public void setUnmatchedAccountEventAction(UnmatchedAccountEventOptions unmatchedAccountEventAction) {
         this.unmatchedAccountEventAction = unmatchedAccountEventAction;
         }
         @Column(name="UNMATCHED_ACCOUNT_EVENT_ACTION", nullable=false)
         @Enumerated(EnumType.STRING)
         public UnmatchedAccountEventOptions getUnmatchedAccountEventAction() {
         return unmatchedAccountEventAction;
         }
         */
        
        /**
         * Get the 'Unmatched Account Event' action string
         * @param unmatchedAccountEventAction The action string
         */
        public void setUnmatchedAccountEventAction(
            String unmatchedAccountEventAction) {
            this.unmatchedAccountEventAction = unmatchedAccountEventAction;
        }
        
        /**
         * Set the 'Unmatched Account Event' action string
         * @return The action string
         */
        @Column(name = "UNMATCHED_ACCT_EVENT_ACTION", nullable = false)
        @NotNull
        //@Enumerated(EnumType.STRING)
        public String getUnmatchedAccountEventAction() {
            return unmatchedAccountEventAction;
        }
        
        
        /**
         * Set the 'Confirmed Account Attribute Event' action string
         * @param confirmedAccountAttributeEventAction The action string
         */
        public void setConfirmedAccountAttributeEventAction(
            String confirmedAccountAttributeEventAction) {
            this.confirmedAccountAttributeEventAction = confirmedAccountAttributeEventAction;
        }
        
        /**
         * Get the 'Confirmed Account Attribute Event' action string
         * @return The action string
         */
        //@Column(name = "CONFIRMED_ACCOUNT_ATTRIBUTE_EVENT_ACTION", nullable = false)
        @Column(name = "CONFIRMED_ACCT_ATTR_EVENT", nullable = false)
        @NotNull
        public String getConfirmedAccountAttributeEventAction() {
            return confirmedAccountAttributeEventAction;
        }
        
        /**
         * Set the 'Unmatched Account Attribute Event' action string
         * @param unmatchedAccountAttributeEventAction The action string
         */
        public void setUnmatchedAccountAttributeEventAction(
            String unmatchedAccountAttributeEventAction) {
            this.unmatchedAccountAttributeEventAction = unmatchedAccountAttributeEventAction;
        }
        
        /**
         * Get the 'Unmatched Account Attribute Event' action string
         * @return Returns the unmatchedAccountAttributeEventAction action string.
         */
        //@Column(name = "UNMATCHED_ACCOUNT_ATTRIBUTE_EVENT_ACTION", nullable = false)
        @Column(name = "UNMATCHED_ACCT_ATTR_EVENT", nullable = false)
        @NotNull
        public String getUnmatchedAccountAttributeEventAction() {
            return unmatchedAccountAttributeEventAction;
        }
        
        
        
        
        
        /**
         * @param isDeleteGroupAfterReconcileProcessesNumberExceeded the isDeleteGroupAfterReconcileProcessesNumberExceeded to set
         */
        public void setDeleteGroupAfterReconcileProcessesNumberExceeded(boolean isDeleteGroupAfterReconcileProcessesNumberExceeded) {
            this.isDeleteGroupAfterReconcileProcessesNumberExceeded = isDeleteGroupAfterReconcileProcessesNumberExceeded;
        }
        
        /**
         * @return the isDeleteGroupAfterReconcileProcessesNumberExceeded
         */
        //@Column(name = "DELETE_GROUP_AFTER_RECONCILE_PROCESSES_NUMBER_EXCEEDED", nullable = false)
        @Column(name = "DEL_GRP_AFTER_RECS_EXCEEDED", nullable = false)
        public boolean isDeleteGroupAfterReconcileProcessesNumberExceeded() {
            return isDeleteGroupAfterReconcileProcessesNumberExceeded;
        }
        
        /**
         * @param reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup the reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup to set
         */
        public void setReconcilesGroupKeepsBeingDeletedBeforeRemoveGroup(int reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup) {
            this.reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup = reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup;
        }
        
        /**
         * @return the reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup
         */
        //@Column(name = "RECONCILES_GROUP_BEING_DELETED_BEFORE_REMOVE_GROUP", nullable = false)
        @Column(name = "RECS_GRP_BEING_DEL_B4_DEL_GRP", nullable = false)
        public int getReconcilesGroupKeepsBeingDeletedBeforeRemoveGroup() {
            return reconcilesGroupKeepsBeingDeletedBeforeRemoveGroup;
        }
        
        /**
         * @param isActivateCorrelationRule the isActivateCorrelationRule to set
         */
        public void setActivateCorrelationRule(boolean isActivateCorrelationRule) {
            this.isActivateCorrelationRule = isActivateCorrelationRule;
        }
        
        /**
         * @return the isActivateCorrelationRule
         */
        @Column(name = "ACTIVATE_CORRELATION_RULE", nullable = false)
        public boolean isActivateCorrelationRule() {
            return isActivateCorrelationRule;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj != null && obj instanceof ReconcilePolicy))
                return false;
            ReconcilePolicy ent = (ReconcilePolicy) obj;
            if (this.reconcilePolicyId.equals(ent.reconcilePolicyId))
                return true;
            return false;
        }

		/**
		 * @return the reconcileAccounts
		 */
        @Column(name = "RECONCILE_ACCOUNTS", nullable = false)
		public boolean isReconcileAccounts() {
			return reconcileAccounts;
		}

		/**
		 * @param reconcileAccounts the reconcileAccounts to set
		 */
		public void setReconcileAccounts(boolean reconcileAccounts) {
			this.reconcileAccounts = reconcileAccounts;
		}

		/**
		 * @return the reconcileGroups
		 */
		@Column(name = "RECONCILE_GROUPS", nullable = false)
		public boolean isReconcileGroups() {
			return reconcileGroups;
		}

		/**
		 * @param reconcileGroups the reconcileGroups to set
		 */
		public void setReconcileGroups(boolean reconcileGroups) {
			this.reconcileGroups = reconcileGroups;
		}

		/**
		 * @return the reconcileGroupMembership
		 */
		@Column(name = "RECONCILE_GROUP_MEMBERSHIP", nullable = false)
		public boolean isReconcileGroupMembership() {
			return reconcileGroupMembership;
		}

		/**
		 * @param reconcileGroupMembership the reconcileGroupMembership to set
		 */
		public void setReconcileGroupMembership(boolean reconcileGroupMembership) {
			this.reconcileGroupMembership = reconcileGroupMembership;
		}

		@Column(name = "CORRELATE_ACC_MATCHED_TO_USER", nullable = false)
		public boolean isAutoCorrelateAccountIfMatchedToUser() {
			return autoCorrelateAccountIfMatchedToUser;
		}

		public void setAutoCorrelateAccountIfMatchedToUser(
				boolean autoCorrelateAccountIfMatchedToUser) {
			this.autoCorrelateAccountIfMatchedToUser = autoCorrelateAccountIfMatchedToUser;
		}

		@ManyToOne(optional=true)
	    @JoinColumn(name="REC_RES_CORRELATION_RULE", nullable = true)
		public ReconcileResourceCorrelationRule getReconcileResourceCorrelationRule() {
			return reconcileResourceCorrelationRule;
		}

		public void setReconcileResourceCorrelationRule(
				ReconcileResourceCorrelationRule reconcileResourceCorrelationRule) {
			this.reconcileResourceCorrelationRule = reconcileResourceCorrelationRule;
		}

		@Column(name = "ACTIVATE_RECONCILE_SUMMARIES", nullable = false)
		public boolean isActivateReconcileSummaries() {
			return activateReconcileSummaries;
		}

		public void setActivateReconcileSummaries(boolean activateReconcileSummaries) {
			this.activateReconcileSummaries = activateReconcileSummaries;
		}
		
		
		
		
    }