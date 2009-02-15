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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;

import velo.exceptions.CollectionElementInsertionException;

/**
 * An entity that represents a Roles Group
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("rolesFolder")
@Table(name="VL_ROLES_FOLDER")
@SequenceGenerator(name="RolesFolderIdSeq",sequenceName="ROLE_FOLDER_ID_SEQ")
@Entity
@NamedQueries({
	@NamedQuery(name = "rolesFolder.findByType", query = "select rolesFolder from RolesFolder rolesFolder where rolesFolder.type = :type"),
	
	
	
	
	
	@NamedQuery(name = "rolesFolder.findAll", query = "SELECT object(rolesFolder) FROM RolesFolder rolesFolder"),
    @NamedQuery(name = "rolesFolder.findAllActive", query = "SELECT object(rolesFolder) FROM RolesFolder rolesFolder"),
    @NamedQuery(name = "rolesFolder.searchByString", query = "SELECT object(rolesFolder) from RolesFolder rolesFolder WHERE ( (UPPER(rolesFolder.uniqueName) like :searchString) OR (UPPER(rolesFolder.description) like :searchString) )")
})
public class RolesFolder extends ExtBasicEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    /**
     * A unique ID of the folder.
     */
    private Long rolesFolderId;
    
    private String type;
    
    /**
     * The roles entities associated with the folder
     */
    private List<Role> roles;
    
    private RolesFolder parentFolder;
    private List<RolesFolder> childFolders;
    
    private List<RolesFolderApproversGroup> rolesFolderApproversGroups = new ArrayList<RolesFolderApproversGroup>();
    
    private String info1;
	private String info2;
	private String info3;
    
    // Transients for groups modification process
	private Set<ApproversGroup> approversGroupsToAssign = new HashSet<ApproversGroup>();
	private Set<RolesFolderApproversGroup> rolesFolderApproversGroupsToDelete = new HashSet<RolesFolderApproversGroup>();
	
	
    
    /**
     * Set the ID of the entity
     * @param rolesFolderId The ID of the entity to set
     */
    public void setRolesFolderId(Long rolesFolderId) {
        this.rolesFolderId = rolesFolderId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity to get
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_ROLE_FOLDER_GEN",sequenceName="IDM_ROLE_FOLDER_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_ROLE_FOLDER_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RolesFolderIdSeq")
    //@GeneratedValue //JB
    @Column(name="ROLES_FOLDER_ID")
    public Long getRolesFolderId() {
        return rolesFolderId;
    }
    
    @Column(name="TYPE", nullable=true)
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
     Set the list of the Roles assigned to the RolesFolder
     @param roles A list of Roles assigned to the RolesFolder
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
    
    /**
     Get a list of Roles assigned to the Role
     @return A list with Roles elements asigned to the Role
     */
        @ManyToMany(cascade = CascadeType.REFRESH,
    fetch=FetchType.LAZY,targetEntity=velo.entity.Role.class
        )
        @JoinTable(
        name="VL_ROLES_TO_ROLES_FOLDERS",
        joinColumns=@JoinColumn(name="ROLES_FOLDER_ID"),
        inverseJoinColumns=@JoinColumn(name="ROLE_ID")
        )
        public List<Role> getRoles() {
        return roles;
    }
    
    @ManyToOne(optional=true)
    @JoinColumn(name="PARENT_ROLES_FOLDER_ID", nullable=true, unique=false)
    public RolesFolder getParentFolder() {
        return parentFolder;
    }
    
    public void setParentFolder(RolesFolder parentFolder) {
        this.parentFolder = parentFolder;
    }
    
    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.LAZY)    
    public List<RolesFolder> getChildFolders() {
		return childFolders;
	}

	public void setChildFolders(List<RolesFolder> childFolders) {
		this.childFolders = childFolders;
	}

	@OneToMany(mappedBy="primaryKey.rolesFolder")
    public List<RolesFolderApproversGroup> getRolesFolderApproversGroups() {
		return rolesFolderApproversGroups;
	}

	public void setRolesFolderApproversGroups(
			List<RolesFolderApproversGroup> rolesFolderApproversGroups) {
		this.rolesFolderApproversGroups = rolesFolderApproversGroups;
	}
    
    public String getInfo1() {
		return info1;
	}

	public void setInfo1(String info1) {
		this.info1 = info1;
	}

	public String getInfo2() {
		return info2;
	}

	public void setInfo2(String info2) {
		this.info2 = info2;
	}

	public String getInfo3() {
		return info3;
	}

	public void setInfo3(String info3) {
		this.info3 = info3;
	}

	//APPROVERS GROUPS STUFF
	@Transient
	public boolean isApproversGroupAssociated(ApproversGroup ag) {
		for (RolesFolderApproversGroup currRFAG : getRolesFolderApproversGroups()) {
			if (currRFAG.getApproversGroup().getApproversGroupId().equals(ag.getApproversGroupId())) {
				return true;
			}
		}
		
		return false;
	}
	
	@Transient
	public Set<ApproversGroup> getApproversGroupsToAssign() {
		return approversGroupsToAssign;
	}

	public void setApproversGroupsToAssign(
			Set<ApproversGroup> approversGroupsToAssign) {
		this.approversGroupsToAssign = approversGroupsToAssign;
	}

	@Transient
	public Set<RolesFolderApproversGroup> getRolesFolderApproversGroupsToDelete() {
		return rolesFolderApproversGroupsToDelete;
	}

	public void setRolesFolderApproversGroupsToDelete(
			Set<RolesFolderApproversGroup> rolesFolderApproversGroupsToDelete) {
		this.rolesFolderApproversGroupsToDelete = rolesFolderApproversGroupsToDelete;
	}

	public void removeRolesFolderApproversGroupToDelete(RolesFolderApproversGroup rfag) {
		getRolesFolderApproversGroupsToDelete().remove(rfag);
	}
	
	public void addRolesFolderApproversGroupToDelete(RolesFolderApproversGroup rag) {
		getRolesFolderApproversGroupsToDelete().add(rag);
	}
	
	public void addApproversGroupToAssign(ApproversGroup ag) throws CollectionElementInsertionException {
		// Make sure that the group is not already associated in role
		if (isApproversGroupAssociated(ag)) {
			throw new CollectionElementInsertionException(
					"Approvers Group already associated to the current edited role");
		}
		getApproversGroupsToAssign().add(ag);
	}
	
	public void removeApproversGroupToAssign(ApproversGroup ag) {
		getApproversGroupsToAssign().remove(ag);
	}
	
	
	@Transient
	public List<Role> getRolesExposedToSelfService() {
		List<Role> roles = new ArrayList<Role>();
		
		for (Role currRole : getRoles()) {
			if (currRole.isExposedToSelfService()) {
				roles.add(currRole);
			}
		}
		
		
		return roles;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof RolesFolder))
			return false;
		RolesFolder ent = (RolesFolder) obj;
		if (this.rolesFolderId.equals(ent.rolesFolderId))
			return true;
		return false;
	}
    
    
    
    //helper
    @Transient
    public boolean isHasParentFolder() {
        if (getParentFolder() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    
    
    
    
    
    
}
