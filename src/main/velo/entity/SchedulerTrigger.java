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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
//@!@clean
/**
 *
 * @author Asaf Shakarchi
 */
@Table(name = "VL_SCHEDULER_TRIGGER")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TRIGGER_TYPEA")
@SequenceGenerator(name="SchedulerTriggerIdSeq",sequenceName="SCHEDULER_TRIGGER_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "schedulerTrigger.readAll",query = "SELECT object(schedulerTrigger) FROM SchedulerTrigger schedulerTrigger"),
    @NamedQuery(name = "schedulerTrigger.findByUniqueName",query = "SELECT object(schedulerTrigger) FROM SchedulerTrigger schedulerTrigger WHERE UPPER(schedulerTrigger.uniqueName) = :uniqueName"),
    @NamedQuery(name = "schedulerTrigger.readAllActive", query = "SELECT object(st) FROM SchedulerTrigger st WHERE st.active = 1")
})
public abstract class SchedulerTrigger extends BaseEntity implements Serializable {

    private Long schedulerTriggerId;
    private String uniqueName;
    private String displayName;
    private String description;
    private Integer misFireInstruction;
    private boolean volatility;
    private boolean active;

    //The type of the trigger, which also the discriminator column
    private String triggerType;

    public SchedulerTrigger() {
    }

    //GF@Id
    //GF@SequenceGenerator(name="IDM_SCHEDULER_TRIGGER_GEN",sequenceName="IDM_SCHEDULER_TRIGGER_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_SCHEDULER_TRIGGER_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="SchedulerTriggerIdSeq")
    //@GeneratedValue //JB
    @Column(name="SCHEDULER_TRIGGER_ID")
    public Long getSchedulerTriggerId() {
        return schedulerTriggerId;
    }

    public void setSchedulerTriggerId(Long schedulerTriggerId) {
        this.schedulerTriggerId = schedulerTriggerId;
    }

    @Length(min=3, max=40) @NotNull //seam
    @Column(name="UNIQUE_NAME", nullable=false)
    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Column(name="DISPLAY_NAME", nullable=false, unique=true)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Column(name="DESCRIPTION", nullable=false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="MIS_FIRE_INSTRUCTION")
    public Integer getMisFireInstruction() {
        return misFireInstruction;
    }

    public void setMisFireInstruction(Integer misFireInstruction) {
        this.misFireInstruction = misFireInstruction;
    }

    @Column(name="VOLATILITY")
    public boolean isVolatility() {
        return volatility;
    }

    public void setVolatility(boolean volatility) {
        this.volatility = volatility;
    }
    
    @Column(name="TRIGGER_TYPE")
    public String getType() {
        return triggerType;
    }
    
    public void setType(String triggerType) {
        this.triggerType = triggerType;
    }
    
    @Column(name="ACTIVE", nullable=false)
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
