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

import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 *
 * @author Asaf Shakarchi
 */
@Name(value = "schedulerJobDefinition")
@Table(name = "VL_SCHEDULER_JOB_DEF")
@Entity
@SequenceGenerator(name="SchedulerJobDefinitionIdSeq",sequenceName="SCHEDULER_JOB_DEF_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "schedulerJobDefinition.readAll",query = "SELECT object(schedulerJobDefinition) FROM SchedulerJobDefinition schedulerJobDefinition"),
    @NamedQuery(name = "schedulerJobDefinition.findByUniqueName",query = "SELECT object(schedulerJobDefinition) FROM SchedulerJobDefinition schedulerJobDefinition WHERE UPPER(schedulerJobDefinition.uniqueName) = :uniqueName")
})
public class SchedulerJobDefinition extends BaseEntity implements Serializable {

    private Long schedulerJobDefinitionId;
    private String uniqueName;
    private String displayName;
    private String description;
    private String jobClass;
    private boolean volatility;
    private boolean durable = true;
    private boolean stateful;
    private boolean recoveryRequesting;
    private boolean active;
    private Set<SchedulerTrigger> triggers;

    public SchedulerJobDefinition() {
    }

    //GF@Id
    //GF@SequenceGenerator(name = "IDM_SCHEDULER_JOB_DEF_GEN", sequenceName = "IDM_SCHEDULER_JOB_DEF_SEQ", allocationSize = 1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IDM_SCHEDULER_JOB_DEF_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="SchedulerJobDefinitionIdSeq")
    //@GeneratedValue //JB
    @Column(name = "SCHEDULER_JOB_DEFINITION_ID")
    public Long getSchedulerJobDefinitionId() {
        return schedulerJobDefinitionId;
    }

    public void setSchedulerJobDefinitionId(Long schedulerJobDefinitionId) {
        this.schedulerJobDefinitionId = schedulerJobDefinitionId;
    }

    @NotNull
    @Column(name = "UNIQUE_NAME", nullable = false)
    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @NotNull
    @Column(name = "DISPLAY_NAME", nullable = false)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    @Column(name = "DESCRIPTION", nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "JOB_CLASS", nullable = false)
    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    @Column(name = "VOLATILITY", nullable = false)
    public boolean isVolatility() {
        return volatility;
    }

    public void setVolatility(boolean volatility) {
        this.volatility = volatility;
    }

    @Column(name = "DURABLE", nullable = false)
    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @Column(name = "STATEFUL", nullable = false)
    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    @Column(name = "RECOVERY_REQUESTING", nullable = false)
    public boolean isRecoveryRequesting() {
        return recoveryRequesting;
    }

    public void setRecoveryRequesting(boolean recoveryRequesting) {
        this.recoveryRequesting = recoveryRequesting;
    }


    @ManyToMany(fetch = FetchType.EAGER, targetEntity = velo.entity.SchedulerTrigger.class)
    @JoinTable(name = "VL_SCHED_TRIGGERS_TO_JOBDEFS", joinColumns = @JoinColumn(name = "SCHEDULER_JOB_DEFINITION_ID"), inverseJoinColumns = @JoinColumn(name = "SCHEDULER_TRIGGER_ID"))
    public Set<SchedulerTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(Set<SchedulerTrigger> triggers) {
        this.triggers = triggers;
    }
    
    @Column(name="ACTIVE", nullable=false)
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
