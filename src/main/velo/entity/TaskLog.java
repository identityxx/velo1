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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * An entity that represents a TaskLog entry
 *
 * @author Asaf Shakarchi
 */
@Entity @Table(name="VL_TASK_LOG")
@SequenceGenerator(name="TaskLogIdSeq",sequenceName="TASK_LOG_ID_SEQ")
public class TaskLog extends EntityLog implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    private Long taskLogId;
    private Task task;
    
    /**
     * @param taskLogId The taskLogId to set.
     */
    public void setTaskLogId(Long taskLogId) {
        this.taskLogId = taskLogId;
    }
    
    /**
     * @return Returns the taskLogId.
     */
    //Gf@Id
    //GF@SequenceGenerator(name="IDM_TASK_LOG_GEN",sequenceName="IDM_TASK_LOG_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_TASK_LOG_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="TaskLogIdSeq")
    //@GeneratedValue //JB
    @Column(name="TASK_LOG_ID")
    public Long getTaskLogId() {
        return taskLogId;
    }
    
    /**
     * @param task The task to set.
     */
    public void setTask(Task task) {
        this.task = task;
    }
    
    /**
     * @return Returns the task.
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="TASK_ID", nullable=false)
    public Task getTask() {
        return task;
    }
}
