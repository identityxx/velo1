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
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

/**
 * @author Asaf Shakarchi
 */
@Name(value = "schedulerCronTrigger")
@DiscriminatorValue(value = "cronTrigger")
@Entity
public class SchedulerCronTrigger extends SchedulerTrigger {
    private String cronExpression;

    public SchedulerCronTrigger() {
    }

    @NotNull
    @Column(name = "CRON_EXPRESSION", nullable = false)
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
//    public String getType() {
//        return "Cron Trigger";
//    }
    
    
    public void copyValues(Object entity) {
    	//TODO: Implement
    }
}
