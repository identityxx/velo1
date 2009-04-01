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

import groovy.lang.GroovyObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
//import org.hibernate.lucene.Indexed;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.common.SysConf;
import velo.contexts.OperationContext;
import velo.encryption.EncryptionUtils;
import velo.entity.UserJournalingEntry.UserJournalingActionType;
import velo.exceptions.CollectionElementInsertionException;
import velo.exceptions.DecryptionException;
import velo.exceptions.EncryptionException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.ScriptLoadingException;
import velo.scripting.GroovyScripting;
import velo.scripting.UserPluginIdTools;
import velo.tools.FileUtils;

/**
 * An entity that represents a User
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("user")
@Entity
@SequenceGenerator(name="UserIdSeq",sequenceName="USER_ID_SEQ")
@Table(name = "VL_USER")
//@Indexed
@NamedQueries( {
	@NamedQuery(name = "user.findByName", query = "SELECT object(user) FROM User user WHERE UPPER(user.name) = :name"),
	@NamedQuery(name = "user.isExistsByName", query = "SELECT count(user) FROM User AS user WHERE UPPER(user.name) = :userName"),
	@NamedQuery(name = "user.findAll", query = "SELECT object(user) FROM User user"),
	
	@NamedQuery(name = "user.findAssignedDirectlyToRole", query = "SELECT object(user) FROM User user, IN (user.userRoles) userRoles WHERE userRoles.role.name = :roleName"),	
	@NamedQuery(name = "user.findAssignedToRoleByPositions", query = "SELECT object(user) FROM User user, IN (user.positions) pos, IN (pos.positionRoles) posRole WHERE posRole.primaryKey.role.name = :roleName"),
	
	
	
	
	//SHOUL;D BE REMOVED, SOME METHODS IN USERBEAN IS USING IT
	@NamedQuery(name = "user.findAssignedToRole", query = "SELECT object(user) FROM User user, IN (user.userRoles) userRoles WHERE userRoles.role = :role"),	
	
	
	
    @NamedQuery(name = "findUserById", query = "SELECT object(user) FROM User user WHERE user.userId = :userId"),
    @NamedQuery(name = "findUserByName", query = "SELECT object(user) FROM User user WHERE UPPER(user.name) = :userName"),
    @NamedQuery(name = "user.searchUsersByString", query = "SELECT object(user) from User user WHERE UPPER(user.name) like :searchString ORDER BY user.name"),
    @NamedQuery(name = "user.findUsersToSync", query = "SELECT user FROM User user WHERE user.deleted = 0 AND user.syncIdentityAttributes = 1"),
    @NamedQuery(name = "findAccountsForUserPerResource", query = "SELECT object(account) from Account account WHERE account.resource = :resource AND account.user = :user"),
    //'Select in collection' DOES NOT WORK ANYMORE. @NamedQuery(name = "user.findAssignedToRole", query = "SELECT object(user) from User user WHERE user.userRoles.role = :role")
    //@NamedQuery(name = "user.searchUsersInGui", query = "SELECT object(user) from User user JOIN user.userIdentityAttributes uia JOIN uia.values val WHERE ( (UPPER(user.name) like :searchString) OR (UPPER(uia.identityAttribute.uniqueName) = 'FIRST_NAME' AND val.asString like :searchString) )")
    @NamedQuery(name = "user.searchUsersInGui", query = "SELECT object(user) from User user WHERE UPPER(user.name) like :searchString ORDER BY user.name")
})
        public class User extends BaseEntity implements Serializable,Cloneable {
    
	private transient static Logger log = Logger.getLogger(User.class.getName());
	private transient static Logger groovyLog = Logger.getLogger(GroovyObject.class.getName());
    private static final long serialVersionUID = 1987305459306161213L;
    
    /**
     * The unique ID of the user
     */
    private Long userId;
    
    /**
     * The Identity User name
     */
    private String name;
    
    /**
     * Get a collection of resource Accounts entities that are assigned to the Identity User
     */
    private Set<Account> accounts = new HashSet<Account>();
    
    /**
     * Get a collection of UserIdentityAttributes related to this user
     */
    private Set<UserIdentityAttribute> userIdentityAttributes = new HashSet<UserIdentityAttribute>();
    
    //(Moved to 'UserRole' entity) private Collection<Role> roles;
    
    private boolean isDiabled;
    
    private boolean deleted;
    
    /**
     * Whether or not the user created manually
     */
    private boolean createdManually;
    
    /**
     * Whether or not the user created by reconcile process
     */
    private boolean createdByReconcile;
    
    /**
     * Whether or not the user created request
     */
    private boolean createdByRequest;
    
    /**
     * Local password for the user
     */
    private String password;
    
    
    /**
     * A property to set password confirmation before persisting the user
     * (This property is transient and is not stored into the DB but set from the view by people)
     * (UserManage check this attribute [if 'password' field was set since 'password' is not always required] before persisting)
     */
    private String passwordConfirm;
    
    /**
     * Whether the user is authenticated via a local password or not
     */
    private boolean isAuthenticatedViaLocalPassword;
    
    /**
     * Whether the user is protected or not,
     * Protected users wont get deleted by the reconcile policies or other automated mechanisms (if any)BUT through the console manually
     */
    private boolean isProtected;
    
    /**
     * Whether the user is locked or not.
     */
    private boolean locked;
    
    private boolean syncIdentityAttributes = true;
    
    /**
     * The number of authentication failures;
     */
    private int authFailureCounter;
    
    private User delegator;
    
    private List<User> delegatorOf;
    
    private ApproversGroup requestDelegatorGroup;
    
    private Set<UserRole> userRoles = new HashSet<UserRole>();
    
    private List<Position> positions = new ArrayList<Position>();
    
    private List<CapabilityFolder> capabilityFolders = new ArrayList<CapabilityFolder>();
    
    private List<Capability> capabilities = new ArrayList<Capability>();
    
    private String sourceType;
    
    private Resource sourceResource;
    
    private UserContainer userContainer;
    
    private Set<UserJournalingEntry> journaling;
    
    
    //Transients
    
    //This is a transient and is set only for the the JSF INPUT components generation purpose for the web GUI
    private Collection<IdentityAttributesGroup> userIdentityAttributesGroups;
    //private HtmlDataTable jsfUserIdentityAttributeGroupsInputFields;
    
    //Transients for roles modification process
    private Set<UserRole> userRolesToRevoke;
    private Set<Role> rolesToAssign;
    private List<ApproversGroup> approversGroups;
    
    private Date expirationDate;
    

	/**
     * Set the ID of the entity
     * @param userId The ID of the entity to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_USER_GEN",sequenceName="IDM_USER_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_USER_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="UserIdSeq")
    //@GeneratedValue //JB
    @Column(name = "USER_ID")
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Set the name of the user
     * @param name The name of the user to set
     */
    public void setName(String name) {
        this.name = name.toUpperCase();
    }
    
    /**
     * Get the name of the user
     * @return The name of the user
     */
    @Column(name = "NAME", nullable = false, unique = true)
    @Length(min = 1, max = 100)
    @NotNull
    //@Field(index=Index.TOKENIZED)
    //seam
    public String getName() {
        if (name != null) {
            return name.toUpperCase();
        }
        else {
            return null;
        }
    }
    
    /**
     * Get a list of accounts attached to the user
     * @return A list of accounts attached to the user
     */
    //  @OneToMany(cascade = CascadeType.ALL, mappedBy="user", fetch=FetchType.EAGER)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public Set<Account> getAccounts() {
        return accounts;
    }
    
    /**
     * Set a list of accounts attached to the user
     * @param accounts A list of accounts attached to the user to set
     */
    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
    
    /**
     * Set the UserIdentityAttributes list related to this user
     * @param userIdentityAttributes The User Identity Attribute list to set
     */
    public void setUserIdentityAttributes(
            Set<UserIdentityAttribute> userIdentityAttributes) {
        this.userIdentityAttributes = userIdentityAttributes;
    }
    
    
    /**
     * Get the UserIdentityAttributes list related to this user
     * @return The UserIdentityAttributes list related to this user
     */
    //@OneToMany(cascade = CascadeType.ALL, mappedBy="user", fetch=FetchType.LAZY) (WAS EAGER)
    
    //12/03/07 - Sucks, tried to load lazily, but in loadVirtualAccountAttributes once it loaded the collection once it didnt.
    //Even when touching the list after loading it! not sure, looks like a bug of toplink! :-/
    //@OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //holy crap, lets try
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<UserIdentityAttribute> getUserIdentityAttributes() {
        return userIdentityAttributes;
    }
    
    /**
     * Set the roles collection that assigned to the user
     * @param roles A collection of roles assigned to the user
     */
        /*
        public void setRoles(Collection<Role> roles) {
                this.roles = roles;
        }
         */
    
    /**
     * Get the roles collection that assigned to the user
     * @return A collection of roles assigned to the user
     */
        /*
        @ManyToMany(cascade = { CascadeType.ALL }, targetEntity = Role.class)
        @JoinTable(name = "ROLES_TO_USER", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = { @JoinColumn(name = "ROLE_ID") })
        public Collection<Role> getRoles() {
                return roles;
        }
         */
    
    /**
     * Set whether the user is disabled or not
     * @param isDiabled An indicator flag to set whether the user is disabled or not
     */
    public void setDisabled(boolean isDiabled) {
        this.isDiabled = isDiabled;
    }
    
    /**
     * Get whether the user is disabled or not
     * @return true/false upon user disabled / not disabled
     */
    @Column(name = "DISABLED", nullable = false)
    @NotNull
    public boolean isDisabled() {
        return isDiabled;
    }
    
    @Column(name = "DELETED", nullable = false)
    @NotNull
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    @Column(name = "CREATED_MANUALLY", nullable = false)
    @NotNull
    public boolean isCreatedManually() {
        return createdManually;
    }
    
    public void setCreatedManually(boolean createdManually) {
        this.createdManually = createdManually;
    }
    
    @Column(name = "CREATED_BY_RECONCILE", nullable = false)
    @NotNull
    public boolean isCreatedByReconcile() {
        return createdByReconcile;
    }
    
    public void setCreatedByReconcile(boolean createdByReconcile) {
        this.createdByReconcile = createdByReconcile;
    }
    
    @Column(name = "CREATED_BY_REQUEST", nullable = false)
    @NotNull
    public boolean isCreatedByRequest() {
        return createdByRequest;
    }
    
    public void setCreatedByRequest(boolean createdByRequest) {
        this.createdByRequest = createdByRequest;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return Returns the password.
     */
    @Column(name = "PASSWORD", nullable = true)
    public String getPassword() {
        return password;
    }
    
    /**
     * @param isAuthenticatedViaLocalPassword The isAuthenticatedViaLocalPassword to set.
     */
    public void setAuthenticatedViaLocalPassword(boolean isAuthenticatedViaLocalPassword) {
        this.isAuthenticatedViaLocalPassword = isAuthenticatedViaLocalPassword;
    }
    
    /**
     * @return Returns the isAuthenticatedViaLocalPassword.
     */
    //@Column(name = "IS_AUTHENTICATED_VIA_LOCAL_PASSWORD", nullable = false)
    @Column(name = "IS_AUTH_VIA_LOCAL_PASSWORD", nullable = false)
    public boolean isAuthenticatedViaLocalPassword() {
        return isAuthenticatedViaLocalPassword;
    }
    
    /**
     * @param isProtected The isProtected to set.
     */
    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
    
    /**
     * @return Returns the isProtected.
     */
    @Column(name = "IS_PROTECTED", nullable = false)
    public boolean isProtected() {
        return isProtected;
    }
    
    
    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    /**
     * @return the locked
     */
    @Column(name = "LOCKED", nullable = false)
    @NotNull
    public boolean isLocked() {
        return locked;
    }
    
    @Column(name = "SYNC_IDENTITY_ATTRIBUTES", nullable = false)
    @NotNull
    public boolean isSyncIdentityAttributes() {
        return syncIdentityAttributes;
    }
    
    public void setSyncIdentityAttributes(boolean syncIdentityAttributes) {
        this.syncIdentityAttributes = syncIdentityAttributes;
    }
    
    /**
     * @param authFailureCounter the authFailureCounter to set
     */
    public void setAuthFailureCounter(int authFailureCounter) {
        this.authFailureCounter = authFailureCounter;
    }
    
    /**
     * @return the authFailureCounter
     */
    @Column(name = "AUTH_FAILURE_COUNTER", nullable = false)
    @NotNull
    public int getAuthFailureCounter() {
        return authFailureCounter;
    }
    
    /**
     * @param userRoles The userRoles to set.
     */
    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
    
    /**
     * @return Returns the userRoles.
     */
    //WARN: at the moment this fetched eaglery, fucken X values are found within each element in user.userIdentityAttributes!
    //where X is the amount of roles, and all of the 'value' elements are the SAME object (same obj ID)
    //could not make 'values' as SET since it is directly set by the GUI, well, lazy fetch make it work for now
    //god knows what's happening here, I think it has something with the link below
    //http://www.jroller.com/eyallupu/entry/hibernate_exception_simultaneously_fetch_multiple
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,CascadeType.PERSIST, CascadeType.MERGE})
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
    
    /**
     * @param positions The positions to set.
     */
    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
    
    /**
     * @return Returns the positions.
     */
        @ManyToMany(
    
    fetch=FetchType.LAZY,targetEntity=velo.entity.Position.class
            )
            @JoinTable(
            name="VL_USERS_TO_POSITIONS",
            joinColumns=@JoinColumn(name="USER_ID"),
            inverseJoinColumns=@JoinColumn(name="POSITION_ID")
            )
            public List<Position> getPositions() {
        return positions;
    }
    
    /**
     * @param capabilities the capabilities to set
     */
    public void setCapabilityFolders(List<CapabilityFolder> capabilityFolders) {
        this.capabilityFolders = capabilityFolders;
    }
    
    /**
     * @return the capabilities
     */
        @ManyToMany(
    
    cascade={CascadeType.REFRESH},
            fetch=FetchType.LAZY,targetEntity=velo.entity.CapabilityFolder.class
            )
            @JoinTable(
            name="VL_USERS_TO_CAP_FOLDERS",
            joinColumns=@JoinColumn(name="USER_ID"),
            inverseJoinColumns=@JoinColumn(name="CAP_FOLDER_ID")
            )
            public List<CapabilityFolder> getCapabilityFolders() {
        return capabilityFolders;
    }
    
    /**
     * @param capabilities the capabilities to set
     */
    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }
    
    /**
     * @return the capabilities
     */
        @ManyToMany(
    
    cascade={CascadeType.PERSIST, CascadeType.MERGE},
            fetch=FetchType.LAZY,targetEntity=velo.entity.Capability.class
            )
            @JoinTable(
            name="VL_USERS_TO_CAPABILITIES",
            joinColumns=@JoinColumn(name="USER_ID"),
            inverseJoinColumns=@JoinColumn(name="CAPABILITY_ID")
            )
            public List<Capability> getCapabilities() {
        return capabilities;
    }
    
    @Column(name = "SOURCE_TYPE", nullable = true)
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
    
    @ManyToOne()
    @JoinColumn(name="SOURCE_RESOURCE_ID", nullable=true, unique=false)
    public Resource getSourceResource() {
        return sourceResource;
    }
    
    public void setSourceResource(Resource sourceResource) {
        this.sourceResource = sourceResource;
    }
    
    
    /**
	 * @return the userContainer
	 */
    @ManyToOne
    @JoinColumn(name="USER_CONTAINER_ID", nullable=true, unique=false)
	public UserContainer getUserContainer() {
		return userContainer;
	}

	/**
	 * @param userContainer the userContainer to set
	 */
	public void setUserContainer(UserContainer userContainer) {
		this.userContainer = userContainer;
	}
	
	
	
    
	@Override
    public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof User))
			return false;
		User ent = (User) obj;
		if (this.userId.equals(ent.userId))
			return true;
		return false;
    }
    
    
    
    /**
	 * @return the approversGroups
	 */
    @ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="VL_APPROVERS_TO_APPROVERS_GRPS",
		joinColumns=@JoinColumn(name="APPROVER_ID"),
		inverseJoinColumns=@JoinColumn(name="APPROVERS_GROUP_ID"))
	public List<ApproversGroup> getApproversGroups() {
		return approversGroups;
	}

	/**
	 * @param approversGroups the approversGroups to set
	 */
	public void setApproversGroups(List<ApproversGroup> approversGroups) {
		this.approversGroups = approversGroups;
	}
    
	
	/**
	 * @return the expirationDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="EXPIRATION_DATE")
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	/**
	 * @return the journaling
	 */
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("creationDate DESC")
	public Set<UserJournalingEntry> getJournaling() {
		return journaling;
	}

	/**
	 * @param journaling the journaling to set
	 */
	public void setJournaling(Set<UserJournalingEntry> journaling) {
		this.journaling = journaling;
	}
	
	@ManyToOne
	@JoinColumn(name = "DELEGATOR_USER_ID", nullable = true, unique = false)
	public User getDelegator() {
		return delegator;
	}

	public void setDelegator(User delegator) {
		this.delegator = delegator;
	}

	@OneToMany(mappedBy = "delegator", fetch = FetchType.LAZY)
	public List<User> getDelegatorOf() {
		return delegatorOf;
	}

	public void setDelegatorOf(List<User> delegatorOf) {
		this.delegatorOf = delegatorOf;
	}
	
	
	@ManyToOne
	@JoinColumn(name = "REQUEST_DELEGATOR_GROUP_ID", nullable = true, unique = false)
	public ApproversGroup getRequestDelegatorGroup() {
		return requestDelegatorGroup;
	}

	public void setRequestDelegatorGroup(ApproversGroup requestDelegatorGroup) {
		this.requestDelegatorGroup = requestDelegatorGroup;
	}
	
	

	//Transient accessors
	 /**
     * @param passwordConfirm The passwordConfirm to set.
     */
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
    
    /**
     * @return Returns the passwordConfirm.
     */
    @Transient
    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    
    
  //User Role Modifications Methods
    @Transient
    public Set<UserRole> getUserRolesToRevoke() {
    	if (userRolesToRevoke == null) {
    		userRolesToRevoke = new HashSet<UserRole>();
    	}
    	
        return userRolesToRevoke;
    }
    
    public void setUserRolesToRevoke(Set<UserRole> userRolesToRevoke) {
        this.userRolesToRevoke = userRolesToRevoke;
    }
    
    public void addUserRoleToRevoke(UserRole userRole) {
    	getUserRolesToRevoke().add(userRole);
    }
    
    public void removeUserRoleToRevoke(UserRole userRole) {
    	getUserRolesToRevoke().remove(userRole);
    }
    
    @Transient
    public Set<Role> getRolesToAssign() {
    	if (rolesToAssign == null) {
    		rolesToAssign = new HashSet<Role>();
    	}
    	
        return rolesToAssign;
    }
    
    public void setRolesToAssign(Set<Role> rolesToAssign) {
        this.rolesToAssign = rolesToAssign;
    }
    
    public void addRoleToAssign(Role role) throws CollectionElementInsertionException {
    	//Make sure that the user does not have this role already
    	if (isRoleAssociatedToUser(role)) {
    		throw new CollectionElementInsertionException("Role already associated to user");
    	}
    	
    	getRolesToAssign().add(role);
    }
    
    public void removeRoleToAssign(Role role) {
    	getRolesToAssign().remove(role);
    }
    
    public void addJournalingEntry(UserJournalingActionType actionType, User by, String summaryMessage, String detailedMessage) {
    	if (journaling == null) journaling = new HashSet<UserJournalingEntry>();
    	
    	getJournaling().add(new UserJournalingEntry(this,actionType,by,summaryMessage,detailedMessage));
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //OVERRIDES
    @Override
    public User clone() {
        try {
            User clonedEntity = (User) super.clone();
            
            //TODO: SHOULD CLONE ALL REFERENCES!
            
            return clonedEntity;
        } catch(CloneNotSupportedException cnfe) {
            System.out.println("Couldnt clone class: " + this.getClass().getName() + ", with exception message: " + cnfe.getMessage());
            return null;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
	
	
	//HELPER CLASSES
	@Transient
    public UserIdentityAttribute getUserIdentityAttribute(String uniqueName) {
		for (UserIdentityAttribute currUIA : getUserIdentityAttributes()) {
            //22-02-07 -> Happened once that there was an UIA with association of a missing IA's ID., caused a null pointer exception in scripts.
            if (currUIA.getIdentityAttribute() != null) {
                if (currUIA.getIdentityAttribute().getUniqueName().equalsIgnoreCase(uniqueName)) {
                    return currUIA;
                }
            } else {
                log.warn("Skipped User Identity Attribute with ID: '" + currUIA.getUserIdentityAttributeId() + "', it is not associated with any IdentityAttribute (or associated with an ID of missing IdentityAttribute!)");
            }
        }
        
        //throw new NoResultFoundException("Could not find Identity-Attribute name: '" + name + "', for User: '" + getName() + "'");
		return null;
    }
	@Transient
	public String getAttribute(String uniqueName) { 
		UserIdentityAttribute attr = getUserIdentityAttribute(uniqueName);
        if (attr != null)
        	if (attr.getValues().size() > 0)
        		return (String)attr.getValues().iterator().next().getAsString();
        
        return null;
	}
    
	
    @Transient
    public UserIdentityAttribute getUserIdentityAttribute(IdentityAttribute ia) {
    	log.debug("Trying to load User Identity Attribute for User '" + getName() + "', for Identity Attribute named: '" + ia.getDisplayName() + "'");

        UserIdentityAttribute foundUIA = null;
        
        for (UserIdentityAttribute currUia : getUserIdentityAttributes()) {
            if ( currUia.getIdentityAttribute().getIdentityAttributeId().equals(ia.getIdentityAttributeId())) {
                foundUIA = currUia;
                break;
            }
        }
        
        if (foundUIA != null) {
            return foundUIA;
        } else {
            //throw new NoUserIdentityAttributeFoundException("Could not find UserIdentityAttribute for User named '" + getName() + "', for IdentityAttribute named '" + ia.getDisplayName() + "'");
        	return null;
        }
    }
    
    @Transient
    public UserIdentityAttribute getUserIdentityAttributeForGui(IdentityAttribute ia) {
    	UserIdentityAttribute uia = getUserIdentityAttribute(ia);
    	if (uia == null) {
    		log.debug("User '" + getName() + "' has no Identity Attribute for IA '" + ia.getUniqueName() + "', creating a new one.");
    		uia = factoryUserIdentityAttribute(ia);
    	}
    	
    	return uia;
    }
    
    
    //used by gui, invoked: getUserIdentityAttributeForGui
    public Set<UserIdentityAttribute> getUserIdentityAttributesForIdentityAttributesGroup(
			IdentityAttributesGroup identityAttributesGroup, User user) {

    	//important to keep the order of the attributes for GUI
		Set<UserIdentityAttribute> userIdentityAttributes = new LinkedHashSet<UserIdentityAttribute>();

		log
				.debug("Loading Identity Attributes instances for Identity Attributes in Group name: "
						+ identityAttributesGroup.getName()
						+ ", for User: "
						+ getName()
						+ ", IdentityAttributes in Group number: "
						+ identityAttributesGroup.getIdentityAttributes()
								.size());

		// Per IdentityAttribute in the Group, fetch the corresponding
		// UserIdentityAttribute for the specified User entity and add it to the
		// returned collection
		for (IdentityAttribute currIA : identityAttributesGroup.getIdentityAttributes()) {
			//not good, should factory a default value if does not exist
			//UserIdentityAttribute uia = getUserIdentityAttribute(currIA);
			
			//Will add or factor a new one if there's instance already
			UserIdentityAttribute uia = getUserIdentityAttributeForGui(currIA);
			
			if (uia == null) {
				log.debug("Could not find UserIdentityAttribute for IdentityAttribute name: "
					+ currIA.getUniqueName()
					+ ", for IdentityAttributesGroup: "
					+ identityAttributesGroup.getName()
					+ ", skipping...");
				continue;
			}

			userIdentityAttributes.add(uia);
		}
			
		log.debug("returning loaded user identity attributes size: "
				+ userIdentityAttributes.size() + ", for group name: "
				+ identityAttributesGroup.getName());
		return userIdentityAttributes;
	}
    
    @Transient
	public Set<IdentityAttributesGroup> getIdentityAttributesGroupsForUser(
			Set<IdentityAttributesGroup> identityAttributesGroups) {
		for (IdentityAttributesGroup currGroup : identityAttributesGroups) {
			currGroup.getUserIdentityAttributes().addAll(
					getUserIdentityAttributesForIdentityAttributesGroup(
							currGroup, this));
		}

		return identityAttributesGroups;
	}
    
    public boolean isMemberOfApproversGroup(String approversGroupUniqueName) {
    	for (ApproversGroup currAG : getApproversGroups()) {
    		if (currAG.getUniqueName().equals(approversGroupUniqueName)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    
    
    
    @Transient
    public Account getAccountOnTarget(String uniqueResourceName) {
        for (Account currAccount : getAccounts()) {
            if (currAccount.getResource().getUniqueName().equalsIgnoreCase(uniqueResourceName)) {
                return currAccount;
            }
        }
        
        //throw new ObjectNotFoundException("Could not find account on target system with unique name '" + uniqueResourceName + "', related to User: '" + getName() + "'");
        return null;
    }
    
    @Transient
    public Account getAccountOnResource(String uniqueResourceName) {
    	return getAccountOnTarget(uniqueResourceName);
    }
    
    
    @Transient
    public boolean isUserAttributeExists(String uniqueName) {
        for (UserIdentityAttribute currUIA : getUserIdentityAttributes()) {
            if (currUIA.getIdentityAttribute().getUniqueName().equalsIgnoreCase(uniqueName)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Transient
    public boolean isUserAttributeExistsWithFirstValue(String uniqueName) {
    	log.debug("Checking whether user '" + getName() + "' has an User Identity Attribute with at least one value for IA name '" + uniqueName);
    	UserIdentityAttribute uia = getUserIdentityAttribute(uniqueName);
    	
    	log.debug("Could not find UIA instance for IA '" + uniqueName + "' for user '" + getName() + "'");
    	if (uia == null) {
    		return false;
    	}
    	
    	
    	if (uia.isFirstValueIsNotNullAndNotEmpty()) {
    		log.debug("Found UIA for IA '" + uniqueName + "' for User '" + getName() + "' and found a not null/not empty string first value!");
    		return true;
    	} else {
    		log.debug("Found UIA for IA '" + uniqueName + "' for User '" + getName() + "' but could not find a not null/not empty string first value!");
    		return false;
    	}
    }
    
    @Transient
    public boolean isRoleAssociatedToUser(Role role) {
    	if (getUserRoles() != null) {
    		for (UserRole currUserRole : getUserRoles()) {
    			if (currUserRole.getRole().getRoleId().equals(role.getRoleId())) {
    				return true;
    			}
    		}
        
    		//return false;
    	} else { 
    		//return false;
    	}
    	
    	
    	for (Position currPos : getPositions()) {
    		if (currPos.isRoleAssociated(role)) {
    			return true;
    		}
    	}
    	
    	
    	
    	return false;
    }
    
    

    @Transient
    public Set<Role> getAssociatedRolesFromAllLevels() {
    	Set<Role> roles = new HashSet<Role>();
    	
    	for (UserRole currUserRole : getUserRoles()) {
    		roles.add(currUserRole.getRole());
    	}
    	
    	for (Position currPos : getPositions()) {
    		roles.addAll(currPos.getRoles());
    	}
    	
    	
    	return roles;
    }
    
    @Transient
    public Map<String,Role> getAssociatedRolesFromAllLevelsAsMap() {
    	Map<String,Role> roles = new HashMap<String,Role>();
    	for (Role currRole : getAssociatedRolesFromAllLevels()) {
    		roles.put(currRole.getName(), currRole);
    	}
    	
    	return roles;
    }
    
    
    
    
    @Transient
    public boolean isPositionAssociatedToUser(Position position) {
    	if (getPositions() != null) {
    		for (Position currUserPosition : getPositions()) {
    			if (currUserPosition.getPositionId().equals(position.getPositionId())) {
    				return true;
    			}
    		}
        
    		return false;
    	} else { 
    		return false;
    	}
    		
    }
    
    public UserIdentityAttribute factoryUserIdentityAttribute(IdentityAttribute ia) {
    	return UserIdentityAttribute.factory(ia, this);
    }
    
    @Transient
    public String getFirstName() {
        UserIdentityAttribute attr = getUserIdentityAttribute("FIRST_NAME");
        if (attr != null)
        	if (attr.getValues().size() > 0)
        		return (String)attr.getValues().iterator().next().getAsString();
        
        return null;
    }
    
    @Transient
    public String getLastName() {
    	UserIdentityAttribute attr = getUserIdentityAttribute("LAST_NAME");
        if (attr != null)
        	if (attr.getValues().size() > 0)
        		return (String)attr.getValues().iterator().next().getAsString();
        
        return null;
    }
    
    @Transient
    public String getFullName() {
    	return getFirstName() + " " + getLastName();
    }
    
    @Transient
    public String getEmail() {
    	UserIdentityAttribute attr = getUserIdentityAttribute("EMAIL");
        if (attr != null)
        	if (attr.getValues().size() > 0)
        		return (String)attr.getValues().iterator().next().getAsString();
        
        return null;
    }
	
	
    
    
    
    
    
    
    
    
    //used by request/gui to factory a new user based on attributes
    public void load(Collection<UserIdentityAttribute> userIdentityAttributes, String userName) throws ObjectFactoryException {
    	log.debug("Factoring User entity based on the specified user identity attributes with attrs amount '" + userIdentityAttributes.size() + "'");
    	for (UserIdentityAttribute currUIA : userIdentityAttributes) {
    		//just for sanity check, make sure UIA's user is set
    		currUIA.setUser(this);
    		getUserIdentityAttributes().add(currUIA);
    	}
    	
    	log.debug("Determining user name...");
    	//might be null as generated in some later stages via plugin ID
    	if (userName != null) {
    		log.debug("User name was set with value '" + userName);
    		setName(userName);
    		//TODO Invoke scripts through global class, this is not clean enough
    	} else if (SysConf.getSysConf().getBoolean("users.plugin_id_enabled")) {
    		log.debug("The specified user name is null and plugin id is enabled!, determining user name via user plugin id");
    		//get the user name via the pluginID
    		GroovyScripting gs = new GroovyScripting();
    		
    		String pluginIdFileName = SysConf.getVeloWorkspaceDir() + "/" + SysConf.getSysConf().getString("system.directory.general_scripts") + "/" + SysConf.getSysConf().getString("system.files.user_plugin_id_script");
    		log.debug("Seeking pluginID file with name '" + pluginIdFileName);
    		File f = new File(pluginIdFileName);
    		if (!f.exists()) {
    			throw new ObjectFactoryException("Could not factory a user entity, expecting pluginID file that does not exist with name '" + pluginIdFileName);
    		}
    		
    		String fileContent = FileUtils.getContents(f);
    		try {
    			GroovyObject go = gs.factoryGroovyObject(fileContent);
    			go.setProperty("log", groovyLog);
    			
    			//Prepare operation context
    			OperationContext context = new OperationContext();
    			//add properties to context
    			UserPluginIdTools upit = new UserPluginIdTools();
    			context.set("tools", upit);
    			context.set("userIdAttrs", getUserIdentityAttributesAsMap());
    			context.set("user", this);
    			
    			go.setProperty("context", context);

    			log.trace("Invoking default method over user plugin ID");
    			Object[] args = {};
    			
    			
    			try {
    				go.invokeMethod("run", args);
    			}catch (Exception e) {
    				log.error("Could not generate user name via plugin ID, printing stack trace.");
    				e.printStackTrace();
        			throw new ObjectFactoryException(e.toString());
        		}
    			
    			String generatedUserName = (String)context.get("userName");
    			
    			log.debug("Generated user name by pluginID is: '" + generatedUserName + "'");
    			log.trace("Ended method invokation");
    			
    			setName(generatedUserName);
    			
    		} catch (ScriptLoadingException e) {
    			throw new ObjectFactoryException(e.toString());
    		}
    	} else {
    		throw new ObjectFactoryException("Could not create a user, could not determine user name!");
    	}
		
    }
    
    
    public void cleanReferences() {
        accounts = null;
        userIdentityAttributes = null;
        positions = null;
        capabilityFolders = null;
        userRoles = null;
        sourceResource = null;
        
        setLoaded(false);
    }
	
    
    /*
    public void encryptPassword() throws EncryptionException {
        String fileName = SysConf.getSysConf().getString("system.directory.system_conf") + "/" + "keys" + "/" + SysConf.getSysConf().getString("system.files.users_encryption_key");
        try {
            String key = FileUtils.readLine(fileName);
            setPassword(EncryptionUtils.encrypt(getPassword(),key));
            //TODO: EncryptionUtils does not handle exceptions so well, encapsulate all possible exception.
        } catch (Exception ex) {
            throw new EncryptionException("Could not encrypt password due to: " + ex);
        }
    }
    
    @Transient
    public String getDecryptedPassword() throws DecryptionException {
        String fileName = SysConf.getSysConf().getString("system.directory.system_conf") + "/" + "keys" + "/" + SysConf.getSysConf().getString("system.files.users_encryption_key");
        
        try {
            String key = FileUtils.readLine(fileName);
            return EncryptionUtils.decrypt(getPassword(),key);
            //TODO: EncryptionUtils does not handle exceptions so well, encapsulate all possible exception.
        } catch (Exception ex) {
            throw new DecryptionException("Could not decrypt password due to: " + ex);
        }
    }
    */
    
    @Transient
    public boolean isUserHasAccount(Resource resource) {
    	for (Account currAccount : getAccounts()) {
    		if (currAccount.getResource().equals(resource)) {
    			return true;
    		}
    	}
    	
    	
    	return false;
    }
    
    @Transient
    public Collection<Account> findAccounts(Resource resource) {
    	Set<Account> accounts = new HashSet<Account>();
    	
    	for (Account currAccount : getAccounts()) {
    		if (currAccount.getResource().equals(resource)) {
    			accounts.add(currAccount);
    		}
    	}
    	
    	return accounts;
    }
    
    
    //Used by RoleManager.ModifyRoles method when a role is detached from a user
    public void removeUserRole(UserRole userRole) {
    	UserRole toBeRemoved = null;
    	boolean found=false;
    	for (UserRole currUserRole : getUserRoles()) {
    		if (currUserRole.equals(userRole)) {
    			toBeRemoved = currUserRole;
    			found = true;
    			break;
    		}
    	}
    	
    	if (found) {
    		getUserRoles().remove(toBeRemoved);
    	}
    }
    
    //for the Attribute Rule, for easy access :)
    @Transient
    public Map<String,UserIdentityAttribute> getUserIdentityAttributesAsMap() {
    	log.debug("Getting user identity attributes as map, user attrs amount: '" + getUserIdentityAttributes().size() + "'");
    	Map<String,UserIdentityAttribute> attrs = new HashMap<String,UserIdentityAttribute>();
    	
    	for (UserIdentityAttribute currUIA : getUserIdentityAttributes()) {
    		attrs.put(currUIA.getIdentityAttribute().getUniqueName(), currUIA);
    	}
    	
    	
    	return attrs;
    }
    
    

    //Get all resource groups relevant for an account on a certain resource, excluding the specified roles
    @Transient
    public Set<ResourceGroup> getResourceGroupsForResource(Resource resource, Set<Role> excludedRoles, boolean ignoreAccountOnResourceExistance) {
    	
    	if (!ignoreAccountOnResourceExistance) {
    		Account account = getAccountOnTarget(resource.getUniqueName());
    		if (account == null) {
    			return null;
    		}
    	}
    	
    	Set<ResourceGroup> groups = new HashSet<ResourceGroup>();
    	Map<String,Role> rolesAsMap = getAssociatedRolesFromAllLevelsAsMap();
    	
    	//remove excluded roles from the rolesMap
    	if (excludedRoles != null) {
    		for (Role currExclRole : excludedRoles) {
    			if (rolesAsMap.containsKey(currExclRole.getName())) {
    				rolesAsMap.remove(currExclRole.getName());
    			} 
    		}
    	}
    	
    	
    	//iterate over the left roles in the map, add groups relevant to the specified resource to the groups set
    	for (Role currRole : rolesAsMap.values()) {
    		groups.addAll(currRole.getResourceGroups(resource));
    	}
    	
    	
    	return groups;
    }
    
    @Transient
    public Set<ResourceGroup> getResourceGroupsForResource(Resource resource) {
    	return getResourceGroupsForResource(resource,null,true);
    }
    
    @Transient
    public boolean isResourceGroupAssociatedByRoles(ResourceGroup rg, Set<Role> excludedRoles) {
    	//FIXME: Efficient enough?
    	Set<ResourceGroup> getResourceGroupsForResource = getResourceGroupsForResource(rg.getResource(), excludedRoles,false);
    	
    	for (ResourceGroup currRG : getResourceGroupsForResource) {
    		if (currRG.equals(rg)) {
    			return true;
    		}
    	}
    	
    	
    	return false;
    }
    
    
    @Transient
    public boolean isAssocRolesReferencesResource(Resource resource, Set<Role> includedRoles, Set<Role> excludedRoles) {
    	Map<String,Role> rolesFromAllLevels = getAssociatedRolesFromAllLevelsAsMap();
    	for (Role currInclRole : includedRoles) {
    		if (!rolesFromAllLevels.containsKey(currInclRole.getName())) {
    			rolesFromAllLevels.put(currInclRole.getName(), currInclRole);
    		}
    	}
    	
    	//remove excluded roles
    	for (Role currExclRole : excludedRoles) {
    		if (rolesFromAllLevels.containsKey(currExclRole.getName())) {
    			rolesFromAllLevels.remove(currExclRole.getName());
    		}
    	}
    	
    	for (Role currRole : rolesFromAllLevels.values()) {
    		if (currRole.isResourceAssociated(resource)) {
    			return true;
    		}
    	}
    	
    	
    	return false;
    }
    
    
    public void removePosition(Position position) {
    	Position p = null;
    	for (Position currPos : getPositions()) {
    		if (currPos.equals(position)) {
    			p = currPos;
    			break;
    		}
    	}
    	
    	if (p != null) {
    		getPositions().remove(p);
    	}
    }
    
    
    
    public UserRole getUserRole(String uniqueRoleName) {
    	for (UserRole currUR : getUserRoles()) {
    		if (currUR.getRole().getName().equals(uniqueRoleName)) {
    			return currUR;
    		}
    	}
    	
    	
    	return null;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //DEP
	
    
    
    
    @Transient
    @Deprecated
    public void setUserIdentityAttributesGroups(Collection<IdentityAttributesGroup> userIdentityAttributesGroups) {
        this.userIdentityAttributesGroups = userIdentityAttributesGroups;
    }

	
    /*
    @Deprecated
    public void setUserIdentityAttributeGroupsInputFields(HtmlDataTable jsfUserIdentityAttributeGroupsInputFields) {
        this.jsfUserIdentityAttributeGroupsInputFields = jsfUserIdentityAttributeGroupsInputFields;
    }
    */
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
