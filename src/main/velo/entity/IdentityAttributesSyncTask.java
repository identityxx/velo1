package velo.entity;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;

import velo.exceptions.ExecutionException;
import velo.exceptions.action.ActionExecutionException;
import velo.reconcilidation.ReconcileIdentityAttributes;

@Entity
@DiscriminatorValue("IDENTITY_ATTRIBUTES_SYNC")
public class IdentityAttributesSyncTask extends GenericTask {
	private static Logger log = Logger.getLogger(IdentityAttributesSyncTask.class.getName());
	
	public void execute() throws ExecutionException {
		log.info("Executing User Identity Attributes Reconcile process...");
		ReconcileIdentityAttributes ria = new ReconcileIdentityAttributes();
		try {
			ria._execute();
		}catch (ActionExecutionException e) {
			throw new ExecutionException(e.getMessage());
		}
	}
	
	
    public static IdentityAttributesSyncTask factory() {
    	IdentityAttributesSyncTask rTask = new IdentityAttributesSyncTask();
    	rTask.setDescription("Synchronize Identity Attributes");
    	rTask.setCreationDate(new Date());
    	rTask.setExpectedExecutionDate(new Date());
    	rTask.setStatus(TaskStatus.PENDING);
        	
        return rTask;
      }
}
