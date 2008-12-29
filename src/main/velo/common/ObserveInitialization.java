package velo.common;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.RequestManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.exceptions.OperationException;



@Name("observeInitialization")
public class ObserveInitialization {
	@Logger Log log;
	
	public InitialContext initialContext;
	public RequestManagerLocal requestManager;
	public TaskManagerLocal taskManager;
	
	@Observer("org.jboss.seam.postInitialization")
	public void observe() {
	 
      log.info("The ObserveInitialization class is initializing scanners.");
      
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
   } 
}