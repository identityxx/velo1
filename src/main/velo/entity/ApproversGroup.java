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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;
//@!@clean
@Entity
@Table(name="VL_APPROVERS_GROUP")
@Name("approversGroup")
@SequenceGenerator(name="ApproversGroupIdSeq",sequenceName="APPRVOERS_GROUP_ID_SEQ")

@NamedQueries( {
		@NamedQuery(name = "approversGroup.findByUniqueName", query = "SELECT ag FROM ApproversGroup ag where ag.uniqueName = :uniqueName")
} )
public class ApproversGroup extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;
	
	private Long approversGroupId;
	
	private String displayName;
	
	private String uniqueName;
	
	private String description;
	
	private String type;
	
	private List<User> approvers;
	
	private List<RoleApproversGroup> roleApproversGroups = new ArrayList<RoleApproversGroup>();
	
	private List<User> requestDelegatorOf;
	
	//transient, just for gui, just a wrapper for the entityApproversGroup assignment's level/desc
	private Integer approvalLevel;
	private String assignmentDescription;
	private String assignmentType;
	

	/**
	 * @return the approversGroupId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="ApproversGroupIdSeq")
	//@GeneratedValue
    @Column(name="APPROVERS_GROUP_ID")
	public Long getApproversGroupId() {
		return approversGroupId;
	}

	/**
	 * @param approversGroupId the approversGroupId to set
	 */
	public void setApproversGroupId(Long approversGroupId) {
		this.approversGroupId = approversGroupId;
	}

	/**
	 * @return the displayName
	 */
	@Column(name="DISPLAY_NAME", nullable=false, unique=true, length=50)
    @NotNull //fucks the searches: @Length(min=3, max=40)
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the uniqueName
	 */
	@Column(name="UNIQUE_NAME", nullable=false, unique=true, length=50)
    @NotNull //fucks the searches @Length(min=3, max=40)
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param uniqueName the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION", nullable=false, length=255)
    @NotNull //seam //fucks the searches @Length(min=1, max=255)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the approvers
	 */
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="VL_APPROVERS_TO_APPROVERS_GRPS",
		joinColumns=@JoinColumn(name="APPROVERS_GROUP_ID"),
		inverseJoinColumns=@JoinColumn(name="APPROVER_ID"))
	public List<User> getApprovers() {
		return approvers;
	}

	/**
	 * @param approvers the approvers to set
	 */
	public void setApprovers(List<User> approvers) {
		this.approvers = approvers;
	}

	/**
	 * @return the roleApproversGroups
	 */
    //@OneToMany(mappedBy="approversGroup")
	@OneToMany(mappedBy="primaryKey.approversGroup")
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
	 * @return the type
	 */
	@Column(name="TYPE")
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	
	@OneToMany(mappedBy = "requestDelegatorGroup", fetch = FetchType.LAZY)
	public List<User> getRequestDelegatorOf() {
		return requestDelegatorOf;
	}

	public void setRequestDelegatorOf(List<User> requestDelegatorOf) {
		this.requestDelegatorOf = requestDelegatorOf;
	}
	
	
	
	

	/**
	 * @return the approvalLevel
	 */
	@Transient
	public Integer getApprovalLevel() {
		return approvalLevel;
	}

	/**
	 * @param approvalLevel the approvalLevel to set
	 */
	public void setApprovalLevel(Integer approvalLevel) {
		this.approvalLevel = approvalLevel;
	}
	
	
	/**
	 * @return the assignmentDescription
	 */
	@Transient
	public String getAssignmentDescription() {
		return assignmentDescription;
	}

	/**
	 * @param assignmentDescription the assignmentDescription to set
	 */
	public void setAssignmentDescription(String assignmentDescription) {
		this.assignmentDescription = assignmentDescription;
	}
	
	public String getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(String assignmentType) {
		this.assignmentType = assignmentType;
	}
	
	
	

	public boolean isMember(String userName) {
		for (User currApprover : getApprovers()) { 
			if (currApprover.getName().equals(userName)) {
				return true;
			}
		}
		
		
		return false;
			
	}
}
