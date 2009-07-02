package velo.actions;

import java.io.Serializable;
import java.util.HashMap;
import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.exceptions.action.ActionConstructException;
import velo.exceptions.action.ActionDestructException;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionPostException;
import velo.exceptions.action.ActionPreException;
import velo.exceptions.action.ActionValidationException;


/**
 * @author Asaf Shakarchi
 * 
 * A base class for any mechanism that requires action invocation,
 * The action class implements a full lifecycle of an action execution with several phases:
 * - Validate
 * - Pre
 * - Execute
 * - Post
 * 
 * (Borrowed from verti'x scheduler with permission) 
 */
abstract public class Action implements Serializable {//implements ActionInterface, Serializable {

	private static Logger log = Logger.getLogger(Action.class.getName());

	//private EdmMessages msgs = new EdmMessages();

	//Maintain a map of properties with key (a string name), every kind of object as value is legal
	//private HashMap<String,Object> properties; //this is sucks! use context instead
	
	private OperationContext context = new OperationContext();

	//Maintain a map of objects that are resulted from the action execution
	private HashMap<String,Object> actionResults;

	public Action() {

	}



	/**
	 * Used to initialize the action.
	 * This method is invoked at the very beginning of the action creation
	 * 
	 */
	public void construct() {

	}

	public void _construct() throws ActionConstructException {
		log.debug("Action construct has -started- for class: " + this.getClass().getName());
		construct();
		log.debug("Action construct has -ended- for class: " + this.getClass().getName());
	}

	/**
	 * Used to cleanup the action.
	 * This is the last method being called as a part of the action execution 
	 * (Usually used to cleanup some objects such as closing adapter connections/objects removals/etc...
	 */
	public void destruct() throws ActionDestructException {

	}

	/**
	 * Logs and execute distruct
	 * @throws ActionDestructException
	 */
	public void _destruct() throws ActionDestructException {
		log.debug("Action distruct has -started- for class: " + this.getClass().getName());
		destruct();
		log.debug("Action distruct has -ended- for class: " + this.getClass().getName());
	}


	/**
	 * Logs the validation phase and execute it
	 * 
	 */
	public void _validate() throws ActionValidationException {
		log.debug("Action Validation phase has -started- for class: " + this.getClass().getName());
		validate();
		log.debug("Action Validation phase has successfully -ended- for class: " + this.getClass().getName());
	}



	/**
	 * Used for validation (can be used for example for data validation)before the pre phase is performed
	 * 
	 */
	public void validate() throws ActionValidationException {

	}

	/**
	 * Logs the pre phase and execute it
	 * 
	 */
	public void _pre() throws ActionPreException {
		log.debug("Action PRE phase has -started- for class: " + this.getClass().getName());
		pre();
		log.debug("Action PRE phase has -started- for class: " + this.getClass().getName());
	}

	/**


    /**
	 * Used to run code before the action itself takes place. Will only be executed if validate return true.
	 */
	public void pre() throws ActionPreException {
	}



	/**
	 * Logs the post phase and execute it
	 * 
	 */
	public void _post() throws ActionPostException {
		log.debug("Action POST phase has -started- for class: " + this.getClass().getName());
		post();
		log.debug("Action POST phase has -started- for class: " + this.getClass().getName());
	}


	/**
	 * Used to run code after the action itself takes place. Will only be executed if the execution phase ends successfully
	 */
	public void post() throws ActionPostException {

	}


	public void _execute() throws ActionExecutionException {
		log.info("Action is executed for action class name: " + this.getClass().getName());

		//execute validate phase 
		try {
			_validate();
		}catch(ActionValidationException e) {
			throw new ActionExecutionException("Failed to execute action in 'validation phase': " + e.getMessage());
		}


		//execute the PRE phase
		try {
			_pre();
		}catch(ActionPreException e) {
			throw new ActionExecutionException("Failed to execute action in 'pre phase': " + e.getMessage());
		}

		//execute execution phase
		try {
			execute();
		}catch(ActionExecutionException e) {
			throw new ActionExecutionException("Failed to execute action in 'execution phase': " + e.getMessage());
		}

		//execute the POST phase
		try {
			_post();
		}catch(ActionPostException e) {
			throw new ActionExecutionException("Failed to execute action in 'post phase': " + e.getMessage());
		}


		//execute the action distruction
		try {
			_destruct();
		} catch(ActionDestructException e) {
			throw new ActionExecutionException("Failed to execute the action distruction method : " + e.getMessage());
		}

		log.info("Succesfully finished executing action class name: " + this.getClass().getName());
	}


	/**
	 * An implementation of the action execution.
	 * <b>Note: Never invoked directly, always invoke _execute() method!</b>
	 * 
	 * @throws ActionExecutionException
	 */
	public abstract void execute() throws ActionExecutionException;









	public void addActionResult(String string, Object obj) {
		if (actionResults == null) {
			actionResults = new HashMap<String,Object>();
		}

		actionResults.put(string,obj);
	}

	/*
	public void addProperty(String string, Object obj) {
		if (properties == null) {
			properties = new HashMap<String,Object>();
		}

		properties.put(string,obj);
	}
	*/

	

	public OperationContext getContext() {
		return context;
	}

	public void setContext(OperationContext context) {
		this.context = context;
	}
	
	public void addVar(String name, String value) {
		getContext().addVar(name, value);
	}

	public OperationContext getCntx() {
		return getContext();
	}


	/**
	 * Returns the result of the action as a collection data object,
	 * Can be used in case where the action has to return something back.
	 * 
	 * It is safe to store any kind of object on any phase in this map.
	 * @return A map with action results from the execution, null if no results were pushed. 
	 */
	public HashMap<String,Object> getActionResults() {return actionResults;}
	public void setActionResults(HashMap<String,Object> actionResults) {this.actionResults = actionResults;}

	//public void setProperties(HashMap<String,Object> properties) {this.properties = properties;}
	//public HashMap<String,Object> getProperties() {return properties;}
}
