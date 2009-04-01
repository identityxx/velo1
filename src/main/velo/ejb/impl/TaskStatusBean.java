package velo.ejb.impl;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.TaskStatusManager;
import velo.entity.Task;
import velo.entity.Task.TaskStatus;

@Stateless()
@TransactionManagement(TransactionManagementType.BEAN)
public class TaskStatusBean implements TaskStatusManager {
	
	@Resource
    private UserTransaction utx;
	
	/**
     * Injected entity manager
     */
    @PersistenceContext
    public EntityManager em;
    
    private static Logger log = Logger.getLogger(TaskStatusBean.class.getName());
    
    
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void changeStatus(TaskStatus newStatus, Task task) {
		//task.setStatus(newStatus);
    	//em.merge(task);
    	
    	try {
    		utx.begin();
        	task.setStatus(newStatus);
        	em.merge(task);
        	utx.commit();
        	log.debug("Successfully changed status of task ID '" + task.getTaskId() + "' to status: " + newStatus);
    	}catch (Exception e) {
    		try {
    	    	log.error("Transaction failed when updating stat. Try rolling back.", e);
    	    	utx.rollback();
    	    	}
    	    	catch (Exception exp) {
    	    	log.error("Transaction failed roling back.", exp);
    	    	}
    	}
	}
}
