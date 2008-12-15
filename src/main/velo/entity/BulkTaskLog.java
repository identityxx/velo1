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

import javax.persistence.CascadeType;
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
 * An entity that represents a BulkTaskLog entry
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_BULK_TASK_LOG")
@SequenceGenerator(name="BulkTaskLogIdSeq",sequenceName="BULK_TASK_LOG_ID_SEQ")
public class BulkTaskLog extends EntityLog implements Serializable {
    
    private static final long serialVersionUID = 1987302492306161413L;
    
    private Long bulkTaskLogId;
    
    private BulkTask bulkTask;
    
    /**
     * @param bulkTaskLogId The bulkTaskLogId to set.
     */
    public void setBulkTaskLogId(Long bulkTaskLogId) {
        this.bulkTaskLogId = bulkTaskLogId;
    }
    
    /**
     * @return Returns the bulkTaskLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_BULK_TASK_LOG_GEN",sequenceName="IDM_BULK_TASK_LOG_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_BULK_TASK_LOG_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="BulkTaskLogIdSeq")
    //@GeneratedValue //JB
    @Column(name="BULK_TASK_LOG_ID")
    public Long getBulkTaskLogId() {
        return bulkTaskLogId;
    }
    
    /**
     * @param bulkTask The BulkTask to set.
     */
    public void setBulkTask(BulkTask bulkTask) {
        this.bulkTask = bulkTask;
    }
    
    /**
     * @return Returns the bulkTask.
     */
    @ManyToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name="BULK_TASK_ID", nullable=true)
    public BulkTask getBulkTask() {
        return bulkTask;
    }
}