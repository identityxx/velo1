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
import java.util.Set;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jboss.seam.annotations.Name;
//@!@clean
/**
 * An entity that represents a Role that is added to a User
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_USER_ROLE")
@SequenceGenerator(name="UserRoleIdSeq",sequenceName="USER_ROLE_ID_SEQ")
@Name("userRole") //Seam name


@NamedQueries({
	@NamedQuery(name = "userRole.isResourceGroupProtectedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN(userRole.role.resourceGroups) tsgInUserRole WHERE userRole <> :userRole AND tsgInUserRole = :tsg AND userRole.user = :user"),
	@NamedQuery(name = "userRole.userRolesRevokeAmount", query = "SELECT count(userRole) FROM UserRole AS userRole WHERE (userRole.inherited = 0) AND (userRole.expirationDate < :currDate) AND (userRole.expirationDate is not null)"),
	@NamedQuery(name = "userRole.findAllUserRolesToRevoke", query = "SELECT object(userRole) FROM UserRole AS userRole WHERE (userRole.inherited = 0) AND (userRole.expirationDate < :currDate) AND (userRole.expirationDate is not null)"),
	@NamedQuery(name = "userRole.isResourceGroupIsAssociatedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN(userRole.role.resourceGroups) rgInUserRole WHERE userRole.user = :user AND tsgInUserRole = :tsg"),
	
	
	
	
    @NamedQuery(name = "userRole.findById",query = "SELECT object(userRole) FROM UserRole userRole WHERE userRole.userRoleId = :id"),
    @NamedQuery(name = "userRole.findByRoleAndUser",query = "SELECT object(userRole) FROM UserRole userRole WHERE userRole.user = :user AND userRole.role = :role"),
    @NamedQuery(name = "userRole.findByUser",query = "SELECT object(userRole) FROM UserRole userRole WHERE userRole.user = :user"),
//added already on top @NamedQuery(name = "userRole.isresourceGroupWasAddedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN(userRole.role.resourceGroups) tsgInUserRole WHERE userRole.user = :user AND tsgInUserRole = :tsg"),
    //(doesnt work, delete!)@NamedQuery(name = "userRole.isAccountProtectedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN(userRole.role.resourceGroups.resource) resourceOwnerOfGroups, IN(userRole.role.resources) resourceInRole WHERE userRole <> :userRole AND resourceOwnerOfGroups = :resource AND resourceInRole = :resource")
    //'Select in collection' DOES NOT WORK ANYMORE. @NamedQuery(name = "userRole.isAccountProtectedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN (userRole.role.resources) resourceInRole WHERE resourceInRole = :resource AND userRole.role.resourceGroups.resource = :resource AND userRole <> :userRole"),
    //NUUUUUUUUUUUUU@NamedQuery(name = "userRole.isAccountProtectedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN (userRole.role.resources) resourceInRole, IN (userRole.role.resourceGroups) groupsInRole WHERE resourceInRole = :resource AND groupsInRole.resource = :resource AND userRole <> :userRole"),
    @NamedQuery(name = "userRole.isAccountProtectedByAnotherUserRole", query = "select count(userRole) FROM UserRole userRole, IN (userRole.role.resources) resourceInRole WHERE resourceInRole = :resource AND userRole.user = :user AND userRole <> :userRole"),
    
    @NamedQuery(name = "userRole.isRoleInUserByUserRoles", query = "SELECT count(userRole) from UserRole userRole where userRole.role = :role"),
    @NamedQuery(name = "userRole.findUserRolesThatProtectAccount", query = "SELECT object(userRole) FROM UserRole userRole JOIN userRole.role role JOIN role.resources resourceInRole WHERE resourceInRole = :resource AND userRole.user = :user")
})
public class UserRole extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long userRoleId;
    
    /**
     * The role entity that was added to the user
     */
    private Role role;
    
    /**
     * To which user the role was added?
     */
    private User user;
    
    /**
     * A set of accounts that were added through the role
     */
    private Set<Account> accounts;
    
    private Date expirationDate;
    
    private boolean inherited;
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_USER_ROLE_GEN",sequenceName="IDM_USER_ROLE_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_USER_ROLE_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="UserRoleIdSeq")
    //@GeneratedValue //JB
    @Column(name="USER_ROLE_ID")
    public Long getUserRoleId() {
        return userRoleId;
    }
    
    /**
     * Set entity ID
     * @param userRoleId Entity's ID to set
     */
    public void setUserRoleId(Long userRoleId) {
        this.userRoleId = userRoleId;
    }
    
    /**
     * @param role The role to set.
     */
    public void setRole(Role role) {
        this.role = role;
    }
    
    /**
     * @return Returns the role.
     */
    @ManyToOne(optional=true)
    @JoinColumn(name="ROLE_ID", nullable=false, unique=false)
    public Role getRole() {
        return role;
    }
    
    /**
     * @param user The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * @return Returns the user.
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="USER_ID", nullable=false, unique=false)
    public User getUser() {
        return user;
    }
    
    /**
     * @param accounts The accounts to set.
     */
    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
    
    
    /**
     * @return Returns the accounts.
     */
        @ManyToMany(
        fetch=FetchType.LAZY,targetEntity=velo.entity.Account.class
        )
        @JoinTable(
    name="VL_ACCOUNTS_TO_USER_ROLES",
        joinColumns=@JoinColumn(name="USER_ROLE_ID"),
        inverseJoinColumns=@JoinColumn(name="ACCOUNT_ID")
        )
        public Set<Account> getAccounts() {
        return accounts;
    }
    
    /**
     * @param expirationDate The expirationDate to set.
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    /**
     * @return Returns the expirationDate.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="EXPIRATION_DATE", nullable=true)
    public Date getExpirationDate() {
        return expirationDate;
    }
    
    /*
     * Set the comment for adding the role to the user
     * @param comment The comment to set.
     *
    public void setComment(String comment) {
        this.comment = comment;
    }
    */
    
    /*
     * Get the comment for adding the role to the user
     * @return Returns the comment.
     *
    @Length(max=255)
    //NOTE: ORACLE DOES NOT ALLOW A COLUMN NAMED 'COMMENT', IT IS A SAVED WORD!
    @Column(name="COMMENT", nullable=true)
    public String getComment() {
        return comment;
    }
    */
    
    /**
     * Set whether the role is a direct access or not
     * (Direct access is access that was added especially for this user and is not inherited from the user's profile)
     * @param inherited The isInherited to set.
     */
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
    
    /**
     * Indicates whether the role is inherited from positions or not
     * (Not Inherited access is access that was added especially for this user and is not inherited from the user's profile)
     * @return Returns Whether the role is inherited from position or not.
     */
    @Column(name="INHERITED", nullable=true)
    public boolean isInherited() {
        return inherited;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof UserRole))
            return false;
        UserRole ent = (UserRole) obj;
        if (this.userRoleId.equals(ent.userRoleId))
            return true;
        return false;
    }
}
