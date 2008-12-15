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
import java.util.List;

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
 * An entity that represents a Password Policy Folder
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_CAPABILITY_FOLDER")
@SequenceGenerator(name="CapabilityFolderIdSeq",sequenceName="CAPABILITY_FOLDER_ID_SEQ")
@Name("capabilityFolder") //Seam name
@NamedQueries({
    @NamedQuery(name = "capabilityFolder.findById",query = "SELECT object(capabilityFolder) FROM CapabilityFolder capabilityFolder WHERE capabilityFolder.capabilityFolderId = :capabilityFolderId"),
    @NamedQuery(name = "capabilityFolder.findByUniqueName",query = "SELECT object(capabilityFolder) FROM CapabilityFolder capabilityFolder WHERE capabilityFolder.uniqueName = :uniqueName"),
    @NamedQuery(name = "capabilityFolder.findAll", query = "SELECT object(capabilityFolder) FROM CapabilityFolder capabilityFolder"),
    @NamedQuery(name = "capabilityFolder.searchByString", query = "SELECT object(capabilityFolder) from CapabilityFolder capabilityFolder WHERE UPPER(capabilityFolder.uniqueName) like :searchString OR UPPER(capabilityFolder.displayName) like :searchString OR UPPER(capabilityFolder.description) like :searchString")
})
public class CapabilityFolder extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long capabilityFolderId;
    
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
    
    //Better as 'SET', but due to JSF limitations, mutipleSelectbox does not work with SETs.
    private List<Capability> capabilities = new ArrayList<Capability>();
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_CAPABILITY_FOLDER_GEN",sequenceName="IDM_CAPABILITY_FOLDER_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_CAPABILITY_FOLDER_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="CapabilityFolderIdSeq")
    //@GeneratedValue //JB
    @Column(name="CAPABILITY_FOLDER_ID")
    public Long getCapabilityFolderId() {
        return capabilityFolderId;
    }
    
    /**
     * Set entity ID
     * @param capabilityFolderId Entity's ID to set
     */
    public void setCapabilityFolderId(Long capabilityFolderId) {
        this.capabilityFolderId = capabilityFolderId;
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
    @Length(min=3, max=40) @NotNull(message="Unique Name is a Must.") //seam
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
     * @return the capabilities
     */
        @ManyToMany(
    
    cascade={CascadeType.REFRESH, CascadeType.PERSIST},
        fetch=FetchType.EAGER,targetEntity=velo.entity.Capability.class
        )
        @JoinTable(
    name="VL_CAPS_TO_CAPS_FOLDERS",
        joinColumns=@JoinColumn(name="CAPABILITY_FOLDER_ID"),
        inverseJoinColumns=@JoinColumn(name="CAPABILITY_ID")
        )
        public List<Capability> getCapabilities() {
        return capabilities;
    }
    
    
    /**
     * @param capabilities The capabilities to set.
     */
    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof CapabilityFolder))
            return false;
        CapabilityFolder ent = (CapabilityFolder) obj;
        if (this.capabilityFolderId.equals(ent.capabilityFolderId))
            return true;
        return false;
    }
}
