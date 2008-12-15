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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;


/**
 * A parent entity of all Identity-Attribute Sources
 *
 *
 *   @author Asaf Shakarchi
 */
@Table(name="VL_IDENTITY_ATTRIBUTE_SOURCE")
@SequenceGenerator(name="IdentityAttributeSourceIdSeq",sequenceName="IDENTITY_ATTR_SOURCE_ID_SEQ")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE")
@Entity
public class IdentityAttributeSource extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long identityAttributeSourceId;
    
    private IdentityAttribute identityAttribute;
    
    private int sequence;
    
    private String description;
    
    /**
     * @param identityAttributeSourceId The identityAttributeSourceId to set.
     */
    public void setIdentityAttributeSourceId(long identityAttributeSourceId) {
        this.identityAttributeSourceId = identityAttributeSourceId;
    }
    
    /**
     * @return Returns the identityAttributeSourceId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_IDENTITY_ATTRIBUTE_SRC_GEN",sequenceName="IDM_IDENTITY_ATTRIBUTE_SRC_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_IDENTITY_ATTRIBUTE_SRC_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="IdentityAttributeSourceIdSeq")
    //@GeneratedValue //JB
    //@Column(name="IDENTITY_ATTRIBUTE_SOURCE_BY_TARGET_SYSTEM_ATTRIBUTE_ID")
    @Column(name="IDENTITY_ATTRIBUTE_SOURCE_ID")
    public long getIdentityAttributeSourceId() {
        return identityAttributeSourceId;
    }
    
    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    /**
     * @return Returns the sequence.
     */
    @Column(name="SEQUENCE", nullable=false)
    public int getSequence() {
        return sequence;
    }
    
    /**
     * @param identityAttribute The identityAttribute to set.
     */
    public void setIdentityAttribute(IdentityAttribute identityAttribute) {
        this.identityAttribute = identityAttribute;
    }
    
    /**
     * @return Returns the identityAttribute.
     */
    @ManyToOne(optional=false)
    //Field is nullable, because importing accounts may not have userID
    @JoinColumn(name="IDENTITY_ATTRIBUTE_ID", nullable=false, unique=false)
    public IdentityAttribute getIdentityAttribute() {
        return identityAttribute;
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
    @Column(name="DESCRIPTION", nullable=false)
    @Length(min=3, max=250) @NotNull //seam
    public String getDescription() {
        return description;
    }
    
    
    @Transient
    public String getType() {
    	return "IMPLEMENT!";
    }
    
    @Transient
    public String getDisplayableType() {
    	return "Implement!";
    }
}
