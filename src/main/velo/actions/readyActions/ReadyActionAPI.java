package velo.actions.readyActions;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.ejb.interfaces.WorkflowManagerLocal;
import velo.ejb.utils.JndiLookup;

/**
 * @author Asaf Shakarchi
 * 
 * The APIs that are exposed to ready actions.
 */
public class ReadyActionAPI {
	InitialContext ic = null;
	//private AccountManagerLocal accountManagerLocal = null;
	public static ReadyActionAPI instance = null;
	boolean boostrapped = false;
	
	protected ReadyActionAPI() {
		// Exists only to defeat instantiation.
	}
	
	public static ReadyActionAPI getInstance() {
	      if(instance == null) {
	         instance = new ReadyActionAPI();
	      }
	      return instance;
	}

	
	public AccountManagerLocal getAccountManager() {
		try {
//			if (accountManagerLocal == null) {
//				accountManagerLocal = (AccountManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("AccountBean"));
//			}
//			
//			return accountManagerLocal;
			
			return (AccountManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("AccountBean"));
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public ResourceManagerLocal getResourceManager() {
		try {
			ResourceManagerLocal resourceManager = (ResourceManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("ResourceBean"));
			return resourceManager;
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public ResourceGroupManagerLocal getResourceGroupManager() {
		try {
			ResourceGroupManagerLocal resourceGroupManager = (ResourceGroupManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("ResourceGroupBean"));
			return resourceGroupManager;
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public WorkflowManagerLocal getWorkflowManager() {
		try {
			WorkflowManagerLocal workflowManager = (WorkflowManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("WorkflowBean"));
			return workflowManager;
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public ReconcileManagerLocal getReconcileManager() {
		try {
			ReconcileManagerLocal manager = (ReconcileManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("ReconcileBean"));
			return manager;
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public UserManagerLocal getUserManager() {
		try {
			UserManagerLocal manager = (UserManagerLocal) getInitialContext().lookup(JndiLookup.getJNDILocalBeanName("UserBean"));
			return manager;
		} catch (NamingException e) {
			//TODO: What to do here?
			return null;			
		}
	}
	
	public InitialContext getInitialContext() throws NamingException {
		if (ic == null) {
			ic = new InitialContext();
		}
		
		return ic;
	}
}
