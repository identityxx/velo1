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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents an IdentityAttributes Group
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("identityAttributesGroup")

@Table(name="VL_IDENTITY_ATTRIBUTES_GROUP")
@Entity
@SequenceGenerator(name="IdentityAttributesGroupIdSeq",sequenceName="IDENTITY_ATTRS_GROUP_ID_SEQ")
@NamedQueries({
	@NamedQuery(name = "identityAttributesGroup.findAllVisible", query = "SELECT object(identityAttributesGroup) FROM IdentityAttributesGroup identityAttributesGroup WHERE identityAttributesGroup.visible = 1 ORDER BY identityAttributesGroup.displayPriority"),
	
	
	
	
    @NamedQuery(name = "identityAttributesGroup.findById",query = "SELECT object(identityAttributesGroup) FROM IdentityAttributesGroup identityAttributesGroup WHERE identityAttributesGroup.identityAttributesGroupId = :id"),
    @NamedQuery(name = "identityAttributesGroup.findByName",query = "SELECT object(identityAttributesGroup) FROM IdentityAttributesGroup identityAttributesGroup WHERE identityAttributesGroup.name = :name"),
    @NamedQuery(name = "identityAttributesGroup.findAll", query = "SELECT object(identityAttributesGroup) FROM IdentityAttributesGroup identityAttributesGroup")
    //@NamedQuery(name = "role.searchRolesByString", query = "SELECT object(role) from Role role WHERE role.name like :searchString")
})
        public class IdentityAttributesGroup extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long identityAttributesGroupId;
    private String name;
    private String description;
    private boolean isVisible;
    private Integer displayPriority;
    private Set<IdentityAttribute> identityAttributes;
    
    
    //private Collection<UserIdentityAttribute> userIdentityAttributes = new HashSet<UserIdentityAttribute>();
    //private Collection<UserIdentityAttribute> userIdentityAttributes = new ArrayList<UserIdentityAttribute>();
    private Collection<UserIdentityAttribute> userIdentityAttributes = new LinkedList<UserIdentityAttribute>();
    
    
    /**
     * Set the ID of the entity
     * @param identityAttributesGroupId The ID of the entity to set
     */
    public void setIdentityAttributesGroupId(Long identityAttributesGroupId) {
        this.identityAttributesGroupId = identityAttributesGroupId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity to get
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_IAS_GRP_GEN",sequenceName="IDM_IAS_GRP_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_IAS_GRP_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="IdentityAttributesGroupIdSeq")
    //@GeneratedValue //JB
    @Column(name="IDENTITY_ATTRIBUTES_GROUP_ID")
    public Long getIdentityAttributesGroupId() {
        return identityAttributesGroupId;
    }
    
    /**
     * Set the name of the Identity Attributes Group
     * @param name The name of the Identity Attributes Group to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the name of the Identity Attributes Group
     * @return The name of the Identity Attributes Group
     */
    @Length(min=3, max=40) @NotNull //seam
    @Column(name="NAME", nullable=false)
    public String getName() {
        return name;
    }
    
    /**
     * Set the description of the Identity Attributes Group
     * @param description The description string to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the description of the Identity Attributes Group
     * @return The description of the Identity Attributes Group
     */
    @Column(name="DESCRIPTION", nullable=false)
    @NotNull //seam
    public String getDescription() {
        return description;
    }
    
    /**
     * @param isVisible the isVisible to set
     */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    /**
     * @return the isVisible
     */
    @Column(name="VISIBLE", nullable=false)
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * @param displayPriority the displayPriority to set
     */
    public void setDisplayPriority(Integer displayPriority) {
        this.displayPriority = displayPriority;
    }
    
    /**
     * @return the displayPriority
     */
    @Column(name="DISPLAY_PRIORITY", nullable=false)
    public Integer getDisplayPriority() {
        return displayPriority;
    }
    
    /**
     * Set the collection of the Identity Attributes assigned to this group
     * @param identityAttributes A collection of Identity Attributes assigned to the group
     */
    public void setIdentityAttributes(Set<IdentityAttribute> identityAttributes) {
        this.identityAttributes = identityAttributes;
    }
    
    /**
     * Get a collection of Identity Attributes assigned to the group
     * @return A collection with Identity Attributes elements asigned to the group
     */
    /*Currently disabled manyToMany here, each IA will be assigned only to one IA Group
    @ManyToMany(
            fetch=FetchType.EAGER,
            targetEntity=velo.entity.IdentityAttribute.class
            )
            @JoinTable(
                name="IDENTITY_ATTRIBUTES_TO_IDENTITY_ATTRIBUTES_GROUP",
            joinColumns=@JoinColumn(name="IDENTITY_ATTRIBUTES_GROUP_ID"),
            inverseJoinColumns=@JoinColumn(name="IDENTITY_ATTRIBUTE_ID")
            )
     */
    @OneToMany(mappedBy = "identityAttributesGroup", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @OrderBy("displayPriority ASC")
    public Set<IdentityAttribute> getIdentityAttributes() {
        return identityAttributes;
    }
    
    
    /**
     * Set the UserIdentityAttributes for a specific user
     * (This is a transient method(Not apart of the entity table) and is usually set by UserBean
     * @param userIdentityAttributes A collection of userIdentityAttributes that corresponds to the IdentityAttributes attached to this group
     */
    public void setUserIdentityAttributes(Set<UserIdentityAttribute> userIdentityAttributes) {
        this.userIdentityAttributes = userIdentityAttributes;
    }
    
    /**
     * Get the UserIdentityAttributes for a specific user
     * (This is a transient method(Not apart of the entity table) and is usually set by UserBean
     * @return A collection of userIdentityAttributes that corresponds to the IdentityAttributes attached to this group
     */
    @Transient
    public Collection<UserIdentityAttribute> getUserIdentityAttributes() {
        return userIdentityAttributes;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof IdentityAttributesGroup))
            return false;
        IdentityAttributesGroup ent = (IdentityAttributesGroup) obj;
        if (this.identityAttributesGroupId.equals(ent.identityAttributesGroupId))
            return true;
        return false;
    }
    
    
    
    
}
