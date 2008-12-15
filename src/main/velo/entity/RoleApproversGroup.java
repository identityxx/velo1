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
import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

@Entity
@Table(name = "VL_ROLE_APPROVERS_GROUP")
@Name("roleApproversGroup")
@AssociationOverrides({
@AssociationOverride(name="primaryKey.role", joinColumns = @JoinColumn(name="ROLE_ID")),
@AssociationOverride(name="primaryKey.approversGroup", joinColumns = @JoinColumn(name="APPROVERS_GROUP_ID"))
})
public class RoleApproversGroup extends EntityApprovers implements
		Serializable {
	private static final long serialVersionUID = 1987305452306161213L;

	private RoleApproversGroupPK primaryKey = new RoleApproversGroupPK();
	
	/**
	 * @return the primaryKey
	 */
	@Id
	public RoleApproversGroupPK getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(RoleApproversGroupPK primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
	public void setRole(Role role) {
		primaryKey.setRole(role);
	}

	@Transient
	public Role getRole() {
		return primaryKey.getRole();
	}

	public void setApproversGroup(ApproversGroup approversGroup) {
		primaryKey.setApproversGroup(approversGroup);
	}

	@Transient
	public ApproversGroup getApproversGroup() {
		return primaryKey.getApproversGroup();
	}
	
	
	
	
	
	public static RoleApproversGroup factory(Role role, ApproversGroup approversGroup) {
		RoleApproversGroup rag = new RoleApproversGroup();
		rag.setCreationDate(new Date());
		rag.setRole(role);
		rag.setApproversGroup(approversGroup);

		Integer level=1;
		if (approversGroup.getApprovalLevel() != null)
			if (approversGroup.getApprovalLevel() > 1)
				level = approversGroup.getApprovalLevel();
			
		rag.setApprovalLevel(level);
		
		if (approversGroup.getAssignmentDescription() != null) 
			rag.setDescription(approversGroup.getAssignmentDescription());
		
		return rag;
	}

}
