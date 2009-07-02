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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "VL_RESOURCE_TYPE_OPERATION")
@SequenceGenerator(name="ResourceTypeOperationIdSeq",sequenceName="RESOURCE_TYPE_OPERATION_ID_SEQ")
public class ResourceTypeOperation extends BaseEntity {
	private Long resourceTypeOperationId;
	private ResourceGlobalOperation resourceGlobalOperation;
	private ResourceType resourceType;
	
	private boolean mustAttachActionToValidatePhase;
	private boolean mustAttachActionToPrePhase;
	private boolean mustAttachActionToPostPhase;
	
	

	public ResourceTypeOperation() {
		
	}
	
	public ResourceTypeOperation(ResourceGlobalOperation resourceGlobalOperation, ResourceType resourceType, boolean mustAttachActionToValidatePhase, boolean mustAttachActionToPrePhase, boolean mustAttachActionToPostPhase) {
		setResourceGlobalOperation(resourceGlobalOperation);
		setResourceType(resourceType);
		setMustAttachActionToValidatePhase(mustAttachActionToValidatePhase);
		setMustAttachActionToPrePhase(mustAttachActionToPrePhase);
		setMustAttachActionToPostPhase(mustAttachActionToPostPhase);
	}
	
	public ResourceTypeOperation(ResourceGlobalOperation resourceGlobalOperation, ResourceType resourceType) {
		setResourceGlobalOperation(resourceGlobalOperation);
		setResourceType(resourceType);
	}
	
	/**
	 * @return the resourceTypeOperationId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceTypeOperationIdSeq")
	//@GeneratedValue
	@Column(name = "RESOURCE_TYPE_OPERATION_ID")
	public Long getResourceTypeOperationId() {
		return resourceTypeOperationId;
	}
	
	
	/**
	 * @param resourceTypeOperationId the resourceTypeOperationId to set
	 */
	public void setResourceTypeOperationId(Long resourceTypeOperationId) {
		this.resourceTypeOperationId = resourceTypeOperationId;
	}
	/**
	 * @return the resourceGlobalOperation
	 */
	@ManyToOne(optional=false)
    @JoinColumn(name="RESOURCE_GLOBAL_OPER_ID", nullable=false, unique=false)
    @OrderBy("uniqueName ASC")
	public ResourceGlobalOperation getResourceGlobalOperation() {
		return resourceGlobalOperation;
	}
	/**
	 * @param resourceGlobalOperation the resourceGlobalOperation to set
	 */
	public void setResourceGlobalOperation(
			ResourceGlobalOperation resourceGlobalOperation) {
		this.resourceGlobalOperation = resourceGlobalOperation;
	}
	/**
	 * @return the resourceType
	 */
	
	@ManyToOne()
    @JoinColumn(name="RESOURCE_TYPE_ID", nullable=false, unique=false)
	public ResourceType getResourceType() {
		return resourceType;
	}
	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	
	/**
	 * @return the mustAttachActionToValidatePhase
	 */
	@Column(name = "MUST_ATTACH_ACTN_TO_VALD_PHASE")
	public boolean isMustAttachActionToValidatePhase() {
		return mustAttachActionToValidatePhase;
	}

	/**
	 * @param mustAttachActionToValidatePhase the mustAttachActionToValidatePhase to set
	 */
	public void setMustAttachActionToValidatePhase(
			boolean mustAttachActionToValidatePhase) {
		this.mustAttachActionToValidatePhase = mustAttachActionToValidatePhase;
	}

	/**
	 * @return the mustAttachActionToPrePhase
	 */
	@Column(name = "MUST_ATTACH_ACTN_TO_PRE_PHASE")
	public boolean isMustAttachActionToPrePhase() {
		return mustAttachActionToPrePhase;
	}

	/**
	 * @param mustAttachActionToPrePhase the mustAttachActionToPrePhase to set
	 */
	public void setMustAttachActionToPrePhase(boolean mustAttachActionToPrePhase) {
		this.mustAttachActionToPrePhase = mustAttachActionToPrePhase;
	}

	/**
	 * @return the mustAttachActionToPostPhase
	 */
	@Column(name = "MUST_ATTACH_ACTN_TO_POST_PHASE")
	public boolean isMustAttachActionToPostPhase() {
		return mustAttachActionToPostPhase;
	}

	/**
	 * @param mustAttachActionToPostPhase the mustAttachActionToPostPhase to set
	 */
	public void setMustAttachActionToPostPhase(boolean mustAttachActionToPostPhase) {
		this.mustAttachActionToPostPhase = mustAttachActionToPostPhase;
	}
	
	
	
	
	
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof ResourceTypeOperation))
			return false;
		ResourceTypeOperation ent = (ResourceTypeOperation) obj;
		if (this.getResourceTypeOperationId().equals(ent.getResourceTypeOperationId()))
			return true;
		return false;
	}
	
	
	
	
	//HELPER/TRANSIENTS
	//for gui
	@Transient
	public String getComments() {
		String comments = new String();
		
		if (isMustAttachActionToValidatePhase()) {
			comments+= "Must implement action during VALIDATION phase!";
		}
		
		if (isMustAttachActionToPrePhase()) {
			if (comments.length() > 0) {
				comments+= ",";
			}
			comments+="Must implement action during PRE phase!";
		}
		
		if (isMustAttachActionToPostPhase()) {
			if (comments.length() > 0) {
				comments+= ",";
			}
			comments+="Must implement action during POST phase!";
		}
		return comments;
	}
	
	@Transient
	public String getDisplayName() {
		return getResourceGlobalOperation().getDisplayName();
	}
	
	@Transient
	public String getUniqueName() {
		return getResourceGlobalOperation().getUniqueName();
	}
}
