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
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a Password Policy Container
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_PASSWORD_POLICY_CONTAINER")
@Name("passwordPolicyContainer") //Seam name
@SequenceGenerator(name="PasswordPolicyContainerIdSeq",sequenceName="PASSWD_POLICY_CONTAINER_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "passwordPolicyContainer.findById",query = "SELECT object(passwordPolicyContainer) FROM PasswordPolicyContainer passwordPolicyContainer WHERE passwordPolicyContainer.passwordPolicyContainerId = :passwordPolicyContainerId"),
    @NamedQuery(name = "passwordPolicyContainer.findByUniqueName",query = "SELECT object(passwordPolicyContainer) FROM PasswordPolicyContainer passwordPolicyContainer WHERE passwordPolicyContainer.uniqueName = :uniqueName"),
    @NamedQuery(name = "passwordPolicyContainer.findAll", query = "SELECT object(passwordPolicyContainer) FROM PasswordPolicyContainer passwordPolicyContainer")
})
public class PasswordPolicyContainer extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long passwordPolicyContainerId;
    
    /**
     * The unique name of the entity
     */
    private String uniqueName;
    
    /**
     * The display name of the entity
     */
    private String displayName;
    
    /**
     * The description of the entity
     */
    private String description;
    
    private Set<Resource> resources;
    
    private PasswordPolicy passwordPolicy;
    
    
    
    //Transient
    private Set<Account> loadedAccounts = new HashSet<Account>();
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_PASSWD_POLICY_CNTR_GEN",sequenceName="IDM_PASSWD_POLICY_CNTR_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_PASSWD_POLICY_CNTR_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="PasswordPolicyContainerIdSeq")
    //@GeneratedValue //JB
    @Column(name="PASSWORD_POLICY_CONTAINER_ID")
    public Long getPasswordPolicyContainerId() {
        return passwordPolicyContainerId;
    }
    
    /**
     * Set entity ID
     * @param passwordPolicyContainerId Entity's ID to set
     */
    public void setPasswordPolicyContainerId(Long passwordPolicyContainerId) {
        this.passwordPolicyContainerId = passwordPolicyContainerId;
    }
    
    
    /**
     * @param uniqueName The uniqueName to set.
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    /**
     * @return Returns the name.
     */
    @Column(name="UNIQUE_NAME", nullable=false, unique=true)
    @Length(min=3, max=40) @NotNull //seam
    public String getUniqueName() {
        return uniqueName;
    }
    
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @return the displayName
     */
    @Column(name="DISPLAY_NAME", nullable=false)
    @Length(min=3, max=40) @NotNull //seam
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return Returns the description.
     */
    @Column(name="DESCRIPTION")
    public String getDescription() {
        return description;
    }
    
    /**
     * @param resources The resources to set.
     */
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }
    
    /**
     * @return Returns the resourceGroups.
     */
    @OneToMany(mappedBy = "passwordPolicyContainer", fetch = FetchType.LAZY)
    public Set<Resource> getResources() {
        return resources;
    }
    
    /**
     * @param passwordPolicy the passwordPolicy to set
     */
    public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }
    
    /**
     * @return the passwordPolicy
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "PASSWORD_POLICY_ID", nullable = false, unique = false)
    @NotNull
    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicy;
    }
    
    
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof PasswordPolicyContainer))
            return false;
        PasswordPolicyContainer ent = (PasswordPolicyContainer) obj;
        if (this.passwordPolicyContainerId.equals(ent.passwordPolicyContainerId))
            return true;
        return false;
    }
    
    /**
     * @param loadedAccounts the loadedAccounts to set
     */
    @Transient
    public void setLoadedAccounts(Set<Account> loadedAccounts) {
        this.loadedAccounts = loadedAccounts;
    }
    
    /**
     * @return the loadedAccounts
     */
    @Transient
    public Set<Account> getLoadedAccounts() {
        return loadedAccounts;
    }
}
