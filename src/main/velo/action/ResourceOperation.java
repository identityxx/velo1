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
package velo.action;

import groovy.lang.GroovyObject;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import velo.actions.tools.FacadeScriptingTools;
import velo.contexts.OperationContext;
import velo.entity.Resource;
import velo.entity.ResourceAction;
import velo.entity.ResourceTypeOperation;
import velo.exceptions.ActionFailureException;
import velo.exceptions.FactoryException;
import velo.exceptions.ScriptInvocationException;
import velo.scripting.ScriptingManager;

/**
 * A resource operation
 * 
 * Holds a reference to all interative objects plus the execution body itself
 * 
 * @author Asaf Shakarchi
 */
public class ResourceOperation {
	
	private static final long serialVersionUID = 1987305452306161212L;
	private static Logger log = Logger.getLogger(ResourceOperation.class.getName());
	private String errorMessage;
	private Resource resource;

	private Set<ResourceAction> creationPhaseResourceActions = new HashSet<ResourceAction>(); 
	private Set<ResourceAction> validateResourceActions = new HashSet<ResourceAction>();
	private Set<ResourceAction> preResourceActions = new HashSet<ResourceAction>();
	private Set<ResourceAction> postResourceActions = new HashSet<ResourceAction>();

	
	private OperationContext context;
	
	private ResourceTypeOperation resourceTypeOperation;

	/**
	 * Empty constructor, - Required by all actions for 'factoring by
	 * reflection' easily
	 */
	public ResourceOperation() {

	}
	
	public ResourceOperation(Resource resource, OperationContext context) {
		setResource(resource);
		setContext(context);
	}


	/**
	 * Used to validate some data before the action if performed
	 * 
	 * For example, in 'createAccount' action, better to check that the
	 * connectivity is possible In 'updateAccount' action, better to check that
	 * the account for update exist
	 * 
	 * @return true/false upon success/failure of the validation process
	 */
	public boolean validate() {
		return true;
	}
	
	
	/**
	 * Being execution in task creation phase 
	 */
	public boolean creationActionOperation() {
		/* Log validate */
		log.debug("CREATION Phase Action operation STARTED for action class name: "
				+ this.getClass().getName());

		log.trace("Found '" + getCreationPhaseResourceActions().size() + "' resource actions (in CREATION phase) to be executed, performing execution...");
		
		for (ResourceAction creationPhaseAction : getCreationPhaseResourceActions()) {
			ScriptingManager sm = new ScriptingManager();
			try {
				ScriptEngine se = sm.getScriptEngine(creationPhaseAction.getActionLanguage().getName().toLowerCase());
				//se.eval(preAction.getContent());
				
				//invoke the action
				invokeAction(se, creationPhaseAction);
			} catch (FactoryException e) {
				String errMsg = "Could not factory scripting manager: " + e.toString();
				log.error(errMsg);
				setErrorMessage(errMsg);
				return false;
			} catch (ScriptException e) {
				log.error("Failed to execute resource action ID '" + creationPhaseAction.getActionDefinitionId() + "': " + e.toString());
				
				log.trace("Determining whether the failed resource action is showStopper or not");
				if (creationPhaseAction.isShowStopper()) {
					log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
					setErrorMessage(e.toString());
					return false;
				}
				else {
					log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
					//TODO: Log the error, so it could be then logged into the relevant task log..
				}
			}
		}
		
		/* log result */
		log.debug("CREATION phase Action operation ENDED for action class name: "
				+ this.getClass().getName());

		return true;
	}
	
	
	/**
	 * Being execution of the validate phase 
	 */
	public boolean validateActionOperation() {
		/* Log validate */
		log.debug("VALIDATION Phase Action operation STARTED for action class name: "
				+ this.getClass().getName());

		log.trace("Found '" + getCreationPhaseResourceActions().size() + "' resource actions (in VALIDATION phase) to be executed, performing execution...");
		
		for (ResourceAction valPhaseAction : getValidateResourceActions()) {
			ScriptingManager sm = new ScriptingManager();
			try {
				ScriptEngine se = sm.getScriptEngine(valPhaseAction.getActionLanguage().getName().toLowerCase());
				//se.eval(preAction.getContent());
				
				//invoke the action
				invokeAction(se, valPhaseAction);
			} catch (FactoryException e) {
				String errMsg = "Could not factory scripting manager: " + e.toString();
				log.error(errMsg);
				setErrorMessage(errMsg);
				return false;
			} catch (ScriptException e) {
				log.error("Failed to execute resource action ID '" + valPhaseAction.getActionDefinitionId() + "': " + e.toString());
				
				log.trace("Determining whether the failed resource action is showStopper or not");
				if (valPhaseAction.isShowStopper()) {
					log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
					setErrorMessage(e.toString());
					return false;
				}
				else {
					log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
					//TODO: Log the error, so it could be then logged into the relevant task log..
				}
			}
		}
		
		/* log result */
		log.debug("VALIDATION phase Action operation ENDED for action class name: "
				+ this.getClass().getName());

		return true;
	}

	/**
	 * Used to run code before the action itself takes place. Will only be
	 * executed if validate return true.
	 * 
	 * @see #validate()
	 * @return the true/false upon success/failure
	 */
	public boolean preActionOperation() {
		/* Log validate */
		log.debug("PRE Action operation STARTED for action class name: "
				+ this.getClass().getName());

		log.trace("Found '" + getPreResourceActions().size() + "' resource actions (in PRE phase) to be executed, performing execution...");
		for (ResourceAction preAction : getPreResourceActions()) {
			/*
			try {
				if (preAction.getActionLanguage().getName().equalsIgnoreCase("GROOVY")) {
					GroovyObject go = preAction.getScriptedObject();
				
					try {
						invoke(go);
					} catch (ScriptInvocationException e) {
						log.error("Failed to execute resource action ID '" + preAction.getActionDefinitionId() + "': " + e.toString());
					
						log.trace("Determining whether the failed resource action is showStopper or not");
						if (preAction.isShowStopper()) {
							log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
							setErrorMessage(e.toString());
							return false;
						}
						else {
							log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
							//TODO: Log the error, so it could be then logged into the relevant task log..
						}
					}
				} else if (preAction.getActionLanguage().getName().equalsIgnoreCase("XML")) {
					
				}
			} catch (ScriptLoadingException e) {
				String errMsg = "Failed to invoke script during preActionOPeration phase: " + e.toString();
				log.warn(errMsg);
				setErrorMessage(errMsg);
				return false;
			}
			*/
			
			
			
			ScriptingManager sm = new ScriptingManager();
			try {
				ScriptEngine se = sm.getScriptEngine(preAction.getActionLanguage().getName().toLowerCase());
				//se.eval(preAction.getContent());
				
				//invoke the action
				invokeAction(se, preAction);
			} catch (FactoryException e) {
				String errMsg = "Could not factory scripting manager: " + e.toString();
				log.error(errMsg);
				setErrorMessage(errMsg);
				return false;
			} catch (ScriptException e) {
				log.error("Failed to execute resource action ID '" + preAction.getActionDefinitionId() + "': " + e.toString());
				
				log.trace("Determining whether the failed resource action is showStopper or not");
				if (preAction.isShowStopper()) {
					log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
					setErrorMessage(e.toString());
					return false;
				}
				else {
					log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
					//TODO: Log the error, so it could be then logged into the relevant task log..
				}
			}
		}

		/* log result */
		log.debug("PRE Action operation ENDED for action class name: "
				+ this.getClass().getName());

		return true;
	}

	/**
	 * Used to run code after the action itself takes place. Will only run if
	 * 'execute' method return true.
	 * 
	 * @see #execute()
	 * @return true/false upon action success/failure execution
	 */
	public boolean postActionOperation() {
		/* Log validate */
		log.debug("POST Action operation STARTED for action class name: "
				+ this.getClass().getName());

		
		/*
		for (ResourceAction postAction : getPostResourceActions()) {
			try {
				GroovyObject go = postAction.getScriptedObject();
				try {
					invoke(go);
				} catch (ScriptInvocationException e) {
					log.error("Failed to execute resource action ID '" + postAction.getActionDefinitionId() + "': " + e.toString());
					
					log.trace("Determining whether the failed resource action is showStopper or not");
					if (postAction.isShowStopper()) {
						log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
						setErrorMessage(e.toString());
						return false;
					}
					else {
						log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
						//TODO: Log the error, so it could be then logged into the relevant task log..
					}
				}
			} catch (ScriptLoadingException e) {
				String errMsg = "Failed to invoke script during postActionOPeration phase: " + e.toString();
				log.warn(errMsg);
				setErrorMessage(errMsg);
				return false;
			}
		}
		*/
		
		
		
		
		
		log.trace("Found '" + getPostResourceActions().size() + "' resource actions (in POST phase) to be executed, performing execution...");
		for (ResourceAction postAction : getPostResourceActions()) {
			ScriptingManager sm = new ScriptingManager();
			try {
				ScriptEngine se = sm.getScriptEngine(postAction.getActionLanguage().getName().toLowerCase());
				//se.eval(preAction.getContent());
				
				//invoke the action
				invokeAction(se, postAction);
			} catch (FactoryException e) {
				String errMsg = "Could not factory scripting manager: " + e.toString();
				log.error(errMsg);
				setErrorMessage(errMsg);
				return false;
			} catch (ScriptException e) {
				log.error("Failed to execute resource action ID '" + postAction.getActionDefinitionId() + "': " + e.toString());
				
				log.trace("Determining whether the failed resource action is showStopper or not");
				if (postAction.isShowStopper()) {
					log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
					setErrorMessage(e.toString());
					return false;
				}
				else {
					log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
					//TODO: Log the error, so it could be then logged into the relevant task log..
				}
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/* log result */
		log.debug("POST Action operation ENDED for action class name: "
				+ this.getClass().getName());

		return true;
	}
	

	/**
	 * Used to cleanup the action. this is the last method being called as a
	 * part of the action execution (Usually used to cleanup some objects such
	 * as closing adapter connections/objects removals/etc...
	 * 
	 * @see #execute()
	 * @return true/false upon action success/failure execution
	 */
	public boolean cleanup() {
		/* Log validate */
		log.info("CLEANUP operation STARTED for action class name: "
				+ this.getClass().getName());

		boolean bResult = cleanup();

		/* log result */
		log.info("CLEANUP operation ENDED for action class name: "
				+ this.getClass().getName());

		return true;
	}

	/**
	 * 
	 * Implementation of the action execution code itself <b>Note: Not called
	 * directly, always call __execute__() method!</b>
	 * 
	 * @throws ActionFailureException
	 *             Throws a failure exception if action execution was failed.
	 * @return true/false upon action success/failure execution
	 */
	public boolean executePre() {
		try {
			/* Log validate */
			log.info("EXECUTE Action operation STARTED.");

			// First of execution, lets validate the action.
			if (!validate()) {
				setErrorMessage("Failed to validate action");
				return false;
			}

			if (!preActionOperation()) {
				setErrorMessage("Failed to perform pre action operations");
				return false;
			}
		}
		catch (Exception e) {
			setErrorMessage(e.toString());
			return false;
		}
		
		return true;
	}
	
	public boolean executePost() {
		try {
			if (!postActionOperation()) {
				setErrorMessage("Failed to perform post action operations");
				return false;
			}

			cleanup();
		}
		catch (Exception e) {
			setErrorMessage(e.toString());
			return false;
		}
		
		log.info("Succesfully finished executing action!");
		return true;
	}	
	
	
	
	
	
	
	
	
	public static ResourceOperation Factory(Resource resource, OperationContext context, ResourceTypeOperation resourceTypeOperation) {
		ResourceOperation ro = new ResourceOperation(resource, context);
		
		ro.setCreationPhaseResourceActions(resource.getCreationPhaseActions(resourceTypeOperation));
		ro.setValidateResourceActions(resource.getValidatePhaseActions(resourceTypeOperation));
		ro.setPreResourceActions(resource.getPrePhaseActions(resourceTypeOperation));
		ro.setPostResourceActions(resource.getPostPhaseActions(resourceTypeOperation));
		ro.setResourceTypeOperation(resourceTypeOperation);
		
		return ro;
	}
	
	
	
	//TODO: Move to a global place, since ResourceOperationBean has such a method too
	//currently available under 'actionDefinition'
	@Deprecated
	public void invoke(GroovyObject go) throws ScriptInvocationException {
		try {
			//set the context
			go.setProperty("context", context);
		
			log.trace("Invoking default method over scripted groovy object");
			Object[] args = {};
			go.invokeMethod("run", args);
			log.trace("Ended method invokation");
		}
		//Wrap any exception into one, scripts are externally supplied thus any exception might occur here since 
		catch (Exception e) {
			throw new ScriptInvocationException("Failed to execute script: " + e.toString());
		}
	}
	
	

	
	

	public void invokeAction(ScriptEngine se, ResourceAction ra) throws ScriptException {
		//EXPERIMENTAL
		FacadeScriptingTools facadeScriptingTools = new FacadeScriptingTools();
		se.put("tools", facadeScriptingTools);
		se.put("cntx", context);
		se.put("log", log);
		Boolean execFailure = false;
		//an indicator whether the resource operation has failed or not. (default must be false!)
		se.put("execFailure",execFailure);
		se.put("lastErrorMessage",new String());
		
		log.trace("Invoking default method over scripted object");
		se.eval(ra.getContent());
		
		
		Boolean execFailureResult = (Boolean)se.get("execFailure");
		
		if (execFailureResult) {
			String lastErrorMessage = (String)se.get("lastErrorMessage");
			throw new ScriptException("opreation was failed (the execution failure indicator was set): " + lastErrorMessage);
		}
		
		log.trace("Ended method invocation");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	// accessors

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	
	/**
	 * @return the creationPhaseResourceActions
	 */
	public Set<ResourceAction> getCreationPhaseResourceActions() {
		return creationPhaseResourceActions;
	}

	/**
	 * @param creationPhaseResourceActions the creationPhaseResourceActions to set
	 */
	public void setCreationPhaseResourceActions(
			Set<ResourceAction> creationPhaseResourceActions) {
		this.creationPhaseResourceActions = creationPhaseResourceActions;
	}
	
	/**
	 * @return the validateResourceActions
	 */
	public Set<ResourceAction> getValidateResourceActions() {
		return validateResourceActions;
	}


	/**
	 * @param validateResourceActions the validateResourceActions to set
	 */
	public void setValidateResourceActions(
			Set<ResourceAction> validateResourceActions) {
		this.validateResourceActions = validateResourceActions;
	}


	/**
	 * @return the preResourceActions
	 */
	public Set<ResourceAction> getPreResourceActions() {
		return preResourceActions;
	}


	/**
	 * @param preResourceActions the preResourceActions to set
	 */
	public void setPreResourceActions(Set<ResourceAction> preResourceActions) {
		this.preResourceActions = preResourceActions;
	}


	/**
	 * @return the postResourceActions
	 */
	public Set<ResourceAction> getPostResourceActions() {
		return postResourceActions;
	}


	/**
	 * @param postResourceActions the postResourceActions to set
	 */
	public void setPostResourceActions(Set<ResourceAction> postResourceActions) {
		this.postResourceActions = postResourceActions;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the context
	 */
	public OperationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(OperationContext context) {
		this.context = context;
	}
	
	
	/**
	 * @return the resourceTypeOperation
	 */
	public ResourceTypeOperation getResourceTypeOperation() {
		return resourceTypeOperation;
	}

	/**
	 * @param resourceTypeOperation the resourceTypeOperation to set
	 */
	public void setResourceTypeOperation(ResourceTypeOperation resourceTypeOperation) {
		this.resourceTypeOperation = resourceTypeOperation;
	}

	
	
}
