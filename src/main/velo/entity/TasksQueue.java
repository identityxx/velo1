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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * An entity that represents a Tasks Queue
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_TASKS_QUEUE")
@Name("tasksQueue") //Seam name
@SequenceGenerator(name="TasksQueueIdSeq",sequenceName="TASK_QUEUE_ID_SEQ")
@NamedQueries({
    @NamedQuery(name = "tasksQueue.findById",query = "SELECT object(tasksQueue) FROM TasksQueue tasksQueue WHERE tasksQueue.tasksQueueId = :tasksQueueId"),
    @NamedQuery(name = "tasksQueue.findByDestinationQueueName",query = "SELECT object(tasksQueue) FROM TasksQueue tasksQueue WHERE tasksQueue.destinationQueueName = :destinationQueueName")
})
public class TasksQueue extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306161213L;
    
    /**
     * ID of the entity
     */
    private Long tasksQueueId;
    
    /**
     * The name of the entity
     */
    private String displayName;
    
    /**
     * The JMS destination queue name
     */
    private String destinationQueueName;
    
    /**
     * The description of the entity
     */
    private String description;
    
    /**
     * Get a collection of resources assigned to the tasks queue
     */
    private Set<Resource> resources;
    
    /**
     * Get the entity ID
     * @return Entity ID
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="TasksQueueIdSeq")
    //@GeneratedValue //JB
    @Column(name="TASKS_QUEUE_ID")
    public Long getTasksQueueId() {
        return tasksQueueId;
    }
    
    /**
     * Set entity ID
     * @param tasksQueueId Entity's ID to set
     */
    public void setTasksQueueId(Long tasksQueueId) {
        this.tasksQueueId = tasksQueueId;
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
     * @param destinationQueueName the destinationQueueName to set
     */
    public void setDestinationQueueName(String destinationQueueName) {
        this.destinationQueueName = destinationQueueName;
    }
    
    /**
     * @return the displayName
     */
    @Column(name="DESTINATION_QUEUE_NAME", nullable=false)
    @Length(min=3, max=40) @NotNull //seam
    public String getDestinationQueueName() {
        return destinationQueueName;
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
    @OneToMany(mappedBy = "tasksQueue", fetch = FetchType.LAZY)
    public Set<Resource> getResources() {
        return resources;
    }
    
    /**
     * @return Returns the description.
     */
    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }
    
    
    public boolean equals(Object obj) {
        if (!(obj != null && obj instanceof TasksQueue))
            return false;
        TasksQueue ent = (TasksQueue) obj;
        if (this.tasksQueueId.equals(ent.tasksQueueId))
            return true;
        return false;
    }
}
