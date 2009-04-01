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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a Role within the system
 *
 * @author Asaf Shakarchi
 */

@Name("capability")
@Table(name="VL_CAPABILITY")
@Entity
@SequenceGenerator(name="CapabilityIdSeq",sequenceName="CAPABILITY_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "capability.findByName",query = "SELECT object(capability) FROM Capability capability WHERE capability.name = :name"),
    @NamedQuery(name = "capability.findAll", query = "SELECT object(capability) FROM Capability capability"),
    @NamedQuery(name = "capability.searchByString", query = "SELECT object(capability) from Capability capability WHERE capability.name like :searchString OR capability.description like :searchString")
})
public class Capability extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161423L;
    
    private Long capabilityId;
    private String name;
    private String description;
    private Set<User> users;
    
    public Capability() {
    }
    
    public Capability(Long capabilityId, String name, String description) {
        //setCapabilityId(capabilityId);
        setName(name);
        setDescription(description);
    }
    
    
    /**
     * Set the ID of the entity
     * @param capabilityId The ID of the entity to set
     */
    public void setCapabilityId(Long capabilityId) {
        this.capabilityId = capabilityId;
    }
    
    /**
     * Get the ID of the entity
     * @return The ID of the entity to get
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_CAPABILITY_GEN",sequenceName="IDM_CAPABILITY_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_CAPABILITY_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="CapabilityIdSeq")
    //@GeneratedValue //JB
    @Column(name="CAPABILITY_ID")
    public Long getCapabilityId() {
        return capabilityId;
    }
    
    /**
     * Set the name of the role
     * @param name The name of the role to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the name of the role
     * @return The name of the role
     */
//fucks the searches even though there's no 'validateAll' tag:(    @Length(min=3, max=40) @NotNull //seam
    @Column(name="NAME", nullable=false)
    public String getName() {
        return name;
    }
    
    /**
     * Set the description of the entity
     * @param description The description string to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the description of the entity
     * @return The description of the entity
     */
    @NotNull //seam
    @Column(name="DESCRIPTION", nullable=false)
    public String getDescription() {
        return description;
    }
    
    
    
    /**
     * @param users The users to set.
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    /**
     * @return Returns the users.
     */
        @ManyToMany(
    
    cascade={CascadeType.PERSIST, CascadeType.MERGE},
        fetch=FetchType.LAZY,targetEntity=velo.entity.User.class
        )
        @JoinTable(
    name="VL_USERS_TO_CAPABILITIES",
        joinColumns=@JoinColumn(name="CAPABILITY_ID"),
        inverseJoinColumns=@JoinColumn(name="USER_ID")
        )
        public Set<User> getUsers() {
        return users;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof Capability))
            return false;
        Capability ent = (Capability) obj;
        if (this.capabilityId.equals(ent.capabilityId))
            return true;
        return false;
    }
    
    @Override
    public int hashCode() {
        if (capabilityId == null) return super.hashCode();
        return capabilityId.hashCode();
    }
    
    @Override
    public String toString() {
        return name + "(" + capabilityId + ")";
    }
}
