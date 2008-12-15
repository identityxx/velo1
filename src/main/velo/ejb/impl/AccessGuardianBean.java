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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import velo.common.SysConf;
import velo.ejb.interfaces.AccessGuardianLocal;
import velo.ejb.interfaces.AccessGuardianRemote;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.entity.Role;
import velo.entity.UserRole;
import velo.exceptions.OperationException;

@Stateless()
//@Name("accessGuardianEjb")
public class AccessGuardianBean implements AccessGuardianLocal, AccessGuardianRemote {
	private static Logger log = Logger.getLogger(AccessGuardianBean.class
			.getName());

	
	@PersistenceContext
    public EntityManager em;
	
	@EJB
	RoleManagerLocal roleManager;

	@javax.annotation.Resource
	TimerService timerService;

	
	@javax.annotation.Resource
    private SessionContext sessionContext;
	
	
	
	
	
	
	
	
	
	 //task scanner
	@Timeout
    public void scanRolesToRevoke(Timer timer) {
		log.info("Guardian Scanner invoked, seeking for roles to reovke...");
        Date currDate = new Date();
        int loadAmountRolesPerQuery = 10;
        
        //TODO: JBOSS: FIX THIS
        //int maxTasksToHandlePerScan = SysConf.getSysConf().getInt("tasks.task_scanner_max_tasks_to_fetch_per_scan");
        int maxUserRolesToHandlePerScan = 5;
        
        int currentResultRowNumberInDb = 0;
        Long amountOfUserRolesWaitingForRovokeInRepository = waitingUserRolesToRevokeAmount();
        
        
        List<UserRole> userRolesReadyToRevoke = new ArrayList<UserRole>();
        if (amountOfUserRolesWaitingForRovokeInRepository > 0) {
            log.trace("Amount of roles waiting for Revoke in Repository is: '" + amountOfUserRolesWaitingForRovokeInRepository + "'");
            Long availableIterations = amountOfUserRolesWaitingForRovokeInRepository / loadAmountRolesPerQuery;
            
            if ( (amountOfUserRolesWaitingForRovokeInRepository % loadAmountRolesPerQuery) != 0) {
                availableIterations++;
            }
            
            log.trace("Calculated possible maximum roles iteration per scan is '" + availableIterations + "' iterations ("+ amountOfUserRolesWaitingForRovokeInRepository + "/" + loadAmountRolesPerQuery + ")");
            
            Long currIteration = new Long(0);
            while ( (userRolesReadyToRevoke.size() < maxUserRolesToHandlePerScan) && (currIteration < availableIterations) ) {
                currIteration++;
                log.trace("Iteration number '" + currIteration + "', fetching '" + loadAmountRolesPerQuery + "', current row number '" + currentResultRowNumberInDb + "'");
                Query userRolesToRevokeQuery = em.createNamedQuery("userRole.findAllUserRolesToRevoke")
                        .setFirstResult(currentResultRowNumberInDb)
                        .setMaxResults(loadAmountRolesPerQuery)
                        .setParameter("currDate",currDate);
                //Accomulate the current row in DB
                currentResultRowNumberInDb+=loadAmountRolesPerQuery + 1;
                
                Collection<UserRole> loadedUserRolesWithExpirationDateExpired = userRolesToRevokeQuery.getResultList();
                
                //Fetch the tasks that are ready to be queued
                userRolesReadyToRevoke.addAll(getUserRolesReadyToBeRevokedPerScan(loadedUserRolesWithExpirationDateExpired,maxUserRolesToHandlePerScan));
                
                log.trace("Iteration number '" + currIteration + "' resulted '" + userRolesReadyToRevoke.size() + "' roles ready to be revoked, while the maximum allowed roles per scan is '" + maxUserRolesToHandlePerScan + "'");
                
                //tasksReadyToQueueCounter += tasksToQueue.size();
            }
        } else {
            log.debug("Roles Revoker Scanner couldn't find any roles to revoke...");
        }
        
        
        if (userRolesReadyToRevoke.size() > 0) {
            //Lock the tasks first.
            //JB indicateTasksAsRunning(tasksReadyToQueue);
            
            
            //Queue tasks into the JMS queue so agents can handle them
            log.info("Revoking -" + userRolesReadyToRevoke.size() + "- user roles!");
            for (UserRole currUserRoleToRevoke : userRolesReadyToRevoke) {
                //First lock tasks
                //currTaskToQueue.setLocked(true);
                //updateTask(currTaskToQueue);
                
            	
            	//revoke role!!!!
            	log.info("Revoking user role with role name '" + currUserRoleToRevoke.getRole().getName() + "', associated to user '" + currUserRoleToRevoke.getUser().getName() + "', with expiration time '" + currUserRoleToRevoke.getExpirationDate() + "'");
            	try {
            		Set<Role> rolesToRevoke = new HashSet<Role>();
            		rolesToRevoke.add(currUserRoleToRevoke.getRole());
            		roleManager.modifyDirectUserRoles(rolesToRevoke, new HashSet<Role>(), currUserRoleToRevoke.getUser());
            	} catch (OperationException e) {
            		//TODO: Log to the event log!
            		log.error("Could not revoke role: " + e.toString());
            	}
            }
        }
    }
    
    
    public void changeAccessGuardianScannerMode() throws OperationException {
        boolean isAccessGuardianScannerEnabled = SysConf.getSysConf().getBoolean("access_control.activate_access_guardian_scanner");
        
        log.info("Requested to change Acess Guardian Scanner Mode...");
        if (!isAccessGuardianScannerEnabled) {
            throw new OperationException("Cannot change access guardian scanner mode since scanner is disabled in system configuration!");
        }
        
        if (timerService.getTimers().size() > 0) {
            log.info("Current access guardian scanner mode is enabled, found '" + timerService.getTimers().size() + " timers..., clearing...");
            Iterator timersIt = timerService.getTimers().iterator();
            while (timersIt.hasNext()) {
                Timer currTimer = (Timer)timersIt.next();
                log.trace("Found timer with info: '" + currTimer.getInfo() + "', object: '" + currTimer + "'");
                currTimer.cancel();
            }
        } else {
            log.info("Current access guardian Scanner mode is disabled, found '" + timerService.getTimers().size() + " timers..., adding timer...");
            int interval = SysConf.getSysConf().getInt("access_control.access_guardian_scanner_interval_in_seconds");
            createTimerScanner(interval, interval);
        }
    }
    
    public boolean isAccessGuardianScannerActivate() {
        if (timerService.getTimers().size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public void createTimerScanner(long initialDuration, long intervalDuration) {
        //Calculating miliseconds from the specified parameters as seconds
        long msInitialDuration = initialDuration * 1000;
        long msIntervalDuration = intervalDuration * 1000;
        
        timerService.getTimers().clear();
        //Timer timer = timerService.createTimer(msInitialDuration,msIntervalDuration,
        //  "Created new timer with interval of: " + msIntervalDuration + " seconds.");
        sessionContext.getTimerService().createTimer(msInitialDuration, msInitialDuration, "access-guardian-scanner");
        
        //System.out.println("Created Task-Scanner timer over a session context object: " + sessionContext);
        log.info("Created an Access Guardian Scanner timer with interval of '" + msInitialDuration + "' ms.");
    }
    
    
    
    
    
    
    
    
    
    
    //accessors/helper
    private Long waitingUserRolesToRevokeAmount() {
        Date currDate = new Date();
        Query q = em.createNamedQuery("userRole.userRolesRevokeAmount").setParameter("currDate",currDate);
        Long num = (Long) q.getSingleResult();
        
        log.debug("Determined amount of user roles waiting for revoke '" + num + "'");
        return num;
    }
    
    
    public Collection<UserRole> getUserRolesReadyToBeRevokedPerScan(Collection<UserRole> userRoles, int maxUserRolesToHandlePerScan) {
        
        Set<UserRole> userRolesToRevoke = new HashSet<UserRole>();
        for (UserRole currUR : userRoles) {
            if (userRolesToRevoke.size() >= maxUserRolesToHandlePerScan) {
                break;
            }
            
            userRolesToRevoke.add(currUR);
        }
        
        return userRolesToRevoke;
    }
}
