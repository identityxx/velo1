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
package velo.ejb.interfaces;

import java.util.Collection;
import velo.entity.SchedulerJobDefinition;
import velo.entity.SchedulerTrigger;

/**
 * An AccountManager interface for all EJB exposed methods
 *
 * @author Asaf Shakarchi
 */
public interface SchedulerManager {

    //TRIGGERS
	@Deprecated
    public Collection<SchedulerTrigger> readAllTriggers();

	@Deprecated
    public boolean isTriggerExistsByUniqueName(String uniqueName);

	@Deprecated
    public void persistTrigger(SchedulerTrigger schedulerTrigger);

	@Deprecated
    public void deleteTrigger(SchedulerTrigger schedulerTrigger);

	@Deprecated
    public void updateTrigger(SchedulerTrigger schedulerTrigger);


    //JOB DEFS
	@Deprecated
    public Collection<SchedulerJobDefinition> readAllJobDefinitions();

	@Deprecated
    public boolean isJobDefinitionExistsByUniqueName(String uniqueName);

	@Deprecated
    public void persistJobDefinition(SchedulerJobDefinition schedulerJobDefinition);

	@Deprecated
    public void deleteJobDefinition(SchedulerJobDefinition schedulerJobDefinition);

	@Deprecated
    public void updateJobDefinition(SchedulerJobDefinition schedulerJobDefinition);
}
