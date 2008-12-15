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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.jboss.seam.annotations.Name;

/**
 * @author Asaf Shakarchi
 */
@Name(value = "schedulerSimpleTrigger")
@DiscriminatorValue(value = "simpleTrigger")
@Entity
public class SchedulerSimpleTrigger extends SchedulerTrigger {
    
    private Date startTime;
    private Date stopTime;
    private Integer repeatCount;
    private Long repeatInterval;

    public SchedulerSimpleTrigger() {
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "START_TIME", nullable = true)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "STOP_TIME", nullable = true)
    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    @Column(name = "REPEAT_COUNT", nullable = false)
    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Column(name = "REPEAT_INTERVAL", nullable = false)
    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }
    
//    public String getType() {
//        return "Simple Trigger";
//    }
    
    
    public void copyValues(Object entity) {
    	//TODO: Implement
    }
}
