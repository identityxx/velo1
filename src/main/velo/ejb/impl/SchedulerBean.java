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
package velo.ejb.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import velo.ejb.interfaces.SchedulerManagerLocal;
import velo.ejb.interfaces.SchedulerManagerRemote;
import velo.entity.SchedulerCronTrigger;
import velo.entity.SchedulerJobDefinition;
import velo.entity.SchedulerTrigger;
import velo.exceptions.OperationException;

/**
 * A Stateless EJB bean for managing Scheduler Methods
 *
 * @author Asaf Shakarchi
 */
@Stateless
public class SchedulerBean implements SchedulerManagerLocal, SchedulerManagerRemote {

    private final String defaultJobsGroupName = Scheduler.DEFAULT_GROUP;
    private final String defaultTriggersGroupName = Scheduler.DEFAULT_GROUP;

    /**
     * Injected entity manager
     */
    @PersistenceContext
    public EntityManager em;

    private static Logger logger = Logger.getLogger(SchedulerBean.class.getName());

    //TRIGGERS
    public Collection<SchedulerTrigger> readAllTriggers() {
        return em.createNamedQuery("schedulerTrigger.readAll").getResultList();
    }

    public Collection<SchedulerTrigger> readAllActiveTriggers() {
        return em.createNamedQuery("schedulerTrigger.readAllActive").getResultList();
    }

    public boolean isTriggerExistsByUniqueName(String uniqueName) {
        //Query database to see if user already exists...
        Query query = em.createNamedQuery("schedulerJobDefinition.findByUniqueName");
        List triggers = query.setParameter("uniqueName", uniqueName).getResultList();

        if (triggers.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void persistTrigger(SchedulerTrigger schedulerTrigger) {
        em.persist(schedulerTrigger);
    }

    public void deleteTrigger(SchedulerTrigger schedulerTrigger) {
        logger.fine("Removing trigger ID: " + schedulerTrigger.getSchedulerTriggerId());

        SchedulerTrigger mergedSchedulerTrigger = em.merge(schedulerTrigger);
        em.remove(mergedSchedulerTrigger);
    }

    public void updateTrigger(SchedulerTrigger schedulerTrigger) {
        em.merge(schedulerTrigger);
    }


    //JOB DEFS
    public Collection<SchedulerJobDefinition> readAllJobDefinitions() {
        return em.createNamedQuery("schedulerJobDefinition.readAll").getResultList();
    }

    public Collection<SchedulerJobDefinition> readAllActiveJobDefinitions() {
        return em.createNamedQuery("schedulerJobDefinition.readAllActive").getResultList();
    }

    public boolean isJobDefinitionExistsByUniqueName(String uniqueName) {
        //Query database to see if user already exists...
        Query query = em.createNamedQuery("schedulerJobDefinition.findByUniqueName");
        List jobDefs = query.setParameter("uniqueName", uniqueName).getResultList();

        if (jobDefs.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void persistJobDefinition(SchedulerJobDefinition schedulerJobDefinition) {
        em.persist(schedulerJobDefinition);
    }

    public void deleteJobDefinition(SchedulerJobDefinition schedulerJobDefinition) {
        logger.fine("Removing trigger ID: " + schedulerJobDefinition.getSchedulerJobDefinitionId());

        SchedulerJobDefinition mergedSchedulerJobDefinition = em.merge(schedulerJobDefinition);
        em.remove(mergedSchedulerJobDefinition);
    }

    public void updateJobDefinition(SchedulerJobDefinition schedulerJobDefinition) {
        em.merge(schedulerJobDefinition);
    }


    public void startScheduler() throws OperationException {
        try {
            org.quartz.SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            org.quartz.Scheduler sched = schedFact.getScheduler();

            //Load all jobs and schedule them with their triggers
            Collection<SchedulerJobDefinition> jobs = readAllJobDefinitions();
            for (SchedulerJobDefinition currJob : jobs) {
                try {
                    java.lang.Class currJobClass = java.lang.Class.forName(currJob.getJobClass());
                    org.quartz.JobDetail currQuartzJobDetail = new org.quartz.JobDetail(currJob.getUniqueName(), null, currJobClass);

                    //Per job, retrueve its quartz triggers
                    Collection<Trigger> triggersForCurrJob = getAllQuartzTriggersForVeloJob(currJob);

                    //per trigger, schedule the curr job
                    for (Trigger currTriggerOfCurrJob : triggersForCurrJob) {
                        //schedule the job with the current iterated trigger
                        sched.scheduleJob(currQuartzJobDetail, currTriggerOfCurrJob);
                    }
                } catch (ClassNotFoundException cnfe) {
                    throw new OperationException("Could not find class job of Job ID " + currJob.getSchedulerJobDefinitionId() + ", with missing class name \'" + currJob.getJobClass() + "\'");
                }
            }
            
            sched.start();
        } catch (SchedulerException se) {
            throw new OperationException("A scheduler exception has occured: " + se.toString());
        }
    }


    public void shutdownScheduler() throws OperationException {
        //how? scheduler should be probably implemented as an mbean!
    }
    
    
    //HELPER
    public Collection<Trigger> getAllQuartzTriggersForVeloJob(SchedulerJobDefinition sjd) throws OperationException {
        try {
            Collection<Trigger> triggers = new ArrayList<Trigger>();

            for (velo.entity.SchedulerTrigger currTrigger : sjd.getTriggers()) {
                if (currTrigger instanceof velo.entity.SchedulerCronTrigger) {
                    SchedulerCronTrigger currSCT = (SchedulerCronTrigger) currTrigger;
                    Trigger currQuartzTrigger = new org.quartz.CronTrigger(currTrigger.getUniqueName(), defaultTriggersGroupName, sjd.getUniqueName(), defaultJobsGroupName, currSCT.getCronExpression());
                    triggers.add(currQuartzTrigger);
                } else {
                    throw new OperationException("Trigger type is not supported: " + currTrigger.getClass().getName());
                }
            }

            return triggers;
        } catch (ParseException pe) {
            throw new OperationException("Could not parse Trigger cron expression: " + pe.toString());
        }
    }
}
