package velo.common;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.log.Log;
import org.jboss.seam.transaction.Transaction;

import velo.ejb.impl.WorkflowBean;
import velo.ejb.interfaces.RequestManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.exceptions.OperationException;



@Name("observeInitialization")
public class ObserveInitialization {
	@Logger Log log;
	
	@In
	WorkflowBean workflowManager;
	
	public InitialContext initialContext;
	public RequestManagerLocal requestManager;
	public TaskManagerLocal taskManager;
	
	@Observer("org.jboss.seam.postInitialization")
	public void observe() {
	 
      log.debug("The Observe Initialization has started.");
      
      
      log.debug("Initializing scanners...");
      try {
    	  initialContext = new InitialContext();
    	  requestManager = (RequestManagerLocal) initialContext.lookup("velo/RequestBean/local");
    	  taskManager = (TaskManagerLocal) initialContext.lookup("velo/TaskBean/local");
      
    	  requestManager.changeRequestScannerMode();
    	  if(!requestManager.isRequestScannerActivate()) requestManager.changeRequestScannerMode();
	      
    	  taskManager.changeTaskScannerMode();
    	  if(!taskManager.isTaskScannerActivate())taskManager.changeTaskScannerMode();
    	  
    	  
    	  //if required, start the JBPM scheduler
    	  //SysConf.getConfProperty(propertyName)
      }
      catch(OperationException oe){
    	  log.warn("Cannot change Scanner status due to: " + oe.getMessage());
      }
      catch (NamingException ne) {
    	  log.error("An error has occured while trying retrieve Manager instance");
      }
      
      
      
      
      log.debug("Initializing workflow job executer...");
      
      boolean isStartJobExecuterOnStartup = SysConf.getSysConf().getBoolean("workflow.job_executer.start_job_executer_on_startup");
      if (isStartJobExecuterOnStartup) {
    	  log.debug("Start job executer on startup is -true-, starting job executer.");
    	  try {
    		  //seems like Jboss does not start transaction automatically at this stage.
    		  Transaction.instance().begin();
    		  workflowManager.startJobExecuter();
    		  Transaction.instance().commit();
    	  }catch (Exception e) {
    		  log.error("Could not start workflow job executer due to: #0",e.getMessage());
    	  }
      } else {
    	  log.debug("Start job executer on startup is -false-, won't start job executer.");
      }
      
      log.debug("The Observe Initialization has ended.");
      
   } 
}