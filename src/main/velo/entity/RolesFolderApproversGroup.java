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
@Table(name = "VL_RF_APPROVERS_GROUP")
@Name("rolesFolderApproversGroup")
@AssociationOverrides({
@AssociationOverride(name="primaryKey.rolesFolder", joinColumns = @JoinColumn(name="ROLESFOLDER_ID")),
@AssociationOverride(name="primaryKey.approversGroup", joinColumns = @JoinColumn(name="APPROVERS_GROUP_ID"))
})
public class RolesFolderApproversGroup extends EntityApprovers implements
		Serializable {
	private static final long serialVersionUID = 1987305452306161213L;

	private RolesFolderApproversGroupPK primaryKey = new RolesFolderApproversGroupPK();
	
	/**
	 * @return the primaryKey
	 */
	@Id
	public RolesFolderApproversGroupPK getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(RolesFolderApproversGroupPK primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
	public void setRolesFolder(RolesFolder rolesFolder) {
		primaryKey.setRolesFolder(rolesFolder);
	}

	@Transient
	public RolesFolder getRolesFolder() {
		return primaryKey.getRolesFolder();
	}

	public void setApproversGroup(ApproversGroup approversGroup) {
		primaryKey.setApproversGroup(approversGroup);
	}

	@Transient
	public ApproversGroup getApproversGroup() {
		return primaryKey.getApproversGroup();
	}
	
	
	
	
	
	public static RolesFolderApproversGroup factory(RolesFolder rolesFolder, ApproversGroup approversGroup) {
		RolesFolderApproversGroup rfag = new RolesFolderApproversGroup();
		rfag.setCreationDate(new Date());
		rfag.setRolesFolder(rolesFolder);
		rfag.setApproversGroup(approversGroup);

		Integer level=1;
		if (approversGroup.getApprovalLevel() != null)
			if (approversGroup.getApprovalLevel() > 1)
				level = approversGroup.getApprovalLevel();
			
		rfag.setApprovalLevel(level);
		
		if (approversGroup.getAssignmentDescription() != null) 
			rfag.setDescription(approversGroup.getAssignmentDescription());
		
		return rfag;
	}

}
