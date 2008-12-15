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
import java.util.HashSet;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a resource Group Container
 *
 * @author Asaf Shakarchi
 */
@Entity
//@Table(name="TARGET_SYSTEM_GROUP_CONTAINER")
@Table(name="VL_RESOURCE_GROUP_CONTAINER")
@SequenceGenerator(name="ResourceGroupContainerIdSeq",sequenceName="RESOURCE_GRP_CONTAINER_ID_SEQ")
@Name("resourceGroupContainer") //Seam name

public class ResourceGroupContainer extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long resourceGroupContainerId;
    
    /**
     * Group Container name
     */
    private String name;
    
    /**
     * Group Container description
     */
    private String description;
    
    private Set<ResourceGroup> resourceGroups = new HashSet<ResourceGroup>();
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_TS_GROUP_CONTAINER_GEN",sequenceName="IDM_TS_GROUP_CONTAINER_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_TS_GROUP_CONTAINER_GEN")
    @Id //@GeneratedValue //JB
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceGroupContainerIdSeq")
    //@Column(name="TARGET_SYSTEM_GROUP_CONTAINER_ID")
    @Column(name="RESOURCE_GROUP_CONTAINER_ID")
    public Long getResourceGroupContainerId() {
        return resourceGroupContainerId;
    }
    
    /**
     * Set entity ID
     * @param resourceGroupContainerId Entity's ID to set
     */
    public void setResourceGroupContainerId(Long resourceGroupContainerId) {
        this.resourceGroupContainerId = resourceGroupContainerId;
    }
    
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    @Column(name="NAME")
    public String getName() {
        return name;
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
     * @param resourceGroups The resourceGroups to set.
     */
    public void setResourceGroups(Set<ResourceGroup> resourceGroups) {
        this.resourceGroups = resourceGroups;
    }
    
    /**
     * @return Returns the resourceGroups.
     */
        @ManyToMany(
    
    cascade={CascadeType.PERSIST, CascadeType.MERGE},
        fetch=FetchType.LAZY,targetEntity=velo.entity.ResourceGroup.class
        )
        @JoinTable(
    //name="TARGET_SYSTEM_GROUPS_TO_TARGET_SYSTEM_GROUP_CONTAINERS",
    name="VL_GRPS_TO_RS_GRP_CONTAINERS",
        //joinColumns=@JoinColumn(name="TARGET_SYSTEM_GROUP_CONTAINER_ID"),
        joinColumns=@JoinColumn(name="RESOURCE_GROUP_CONTAINER_ID"),
        inverseJoinColumns=@JoinColumn(name="RESOURCE_GROUP_ID")
        )
        public Set<ResourceGroup> getResourceGroups() {
        return resourceGroups;
    }
}
