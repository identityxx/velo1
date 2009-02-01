package velo.entity;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;

import velo.entity.Task.TaskStatus;
import velo.exceptions.ActionFailureException;
import velo.exceptions.ExecutionException;
import velo.reconcilidation.ReconcileIdentityAttributes;

@Entity
@DiscriminatorValue("IDENTITY_ATTRIBUTES_SYNC")
public class IdentityAttributesSyncTask extends GenericTask {
	private static Logger log = Logger.getLogger(IdentityAttributesSyncTask.class.getName());
	
	public void execute() throws ExecutionException {
		log.info("Executing User Identity Attributes Reconcile process...");
		ReconcileIdentityAttributes ria = new ReconcileIdentityAttributes();
		try {
			ria.__execute__();
		}catch (ActionFailureException e) {
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
