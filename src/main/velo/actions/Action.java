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
package velo.actions;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import velo.common.EdmMessages;
import velo.exceptions.ActionFailureException;

/**
 The base class of all actions.
 
 An action is a class that makes some sort of execution. Mostly used for
 'resourceAction' childs, for executing actions on resources such as
 'create/delete/update' accounts
 
 @author Asaf Shakarchi
 */
@Deprecated
abstract public class Action implements ActionInterface, Serializable {
    private static final long serialVersionUID = 1987305452306161212L;
    
    private static Logger log = Logger.getLogger(Action.class.getName());
    
    private EdmMessages msgs = new EdmMessages();
    
    private HashMap properties = new HashMap<String,Object>();
    
    private Date expectedExecutionDate = new Date();
    
    /**
     Empty constructor, - Required by all actions for 'factoring by
     reflection' easily
     */
    public Action() {
        
    }
    
    /**
     Return the Messages utility object
     
     @return Returns the EdmMessages utility object to log messages.
     */
    public EdmMessages getMsgs() {
        return msgs;
    }
    
    /**
     @param properties the properties to set
     */
    public void setProperties(HashMap properties) {
        this.properties = properties;
    }
    
    /**
     @return the properties
     */
    public HashMap getProperties() {
        return properties;
    }
    
    /**
     Logs the validation function and execute it
     
     @see #validate()
     @return the validateAction result
     */
    public boolean __validate__() {
        /* Log validate */
        log.info("Action validation STARTED for action class name: " + this.getClass().getName());
        
        boolean bResult = validate();
        
        /* log result */
        log.info("Action validation ENDED for action class name: " + this.getClass().getName());
        
        return bResult;
    }
    
    /**
     Used to validate some data before the action if performed, either in the
     active object (The object to insert/update) etc, or in some external
     source
     
     For example, in 'createAccount' action, better to check that the
     connectivity is possible In 'updateAccount' action, better to check that
     the account for update exist
     
     @return true/false upon success/failure of the validation process
     */
    public boolean validate() {
        return true;
    }
    
    /**
     Wraps the preActionOperation method, mainly for logging porpuses
     
     @see #preActionOperation()
     @return the true/false upon success/failure
     */
    public boolean __preActionOperation__() {
        /* Log validate */
        log.info("PRE Action operation STARTED for action class name: " + this.getClass().getName());
        
        boolean bResult = preActionOperation();
        
        /* log result */
        log.info("PRE Action operation ENDED for action class name: " + this.getClass().getName());
        
        return bResult;
    }
    
    /**
     Used to run code before the action itself takes place. Will only be
     executed if validate return true.
     
     @see #validate()
     @return the true/false upon success/failure
     */
    public boolean preActionOperation() {
        return true;
    }
    
    /**
     Wraps the postActionOperation method, mainly for logging porpuses
     
     @see #postActionOperation()
     @return the true/false upon success/failure
     */
    public boolean __postActionOperation__() {
        /* Log validate */
        log.info("POST Action operation STARTED for action class name: " + this.getClass().getName());
        
        boolean bResult = postActionOperation();
        
        /* log result */
        log.info("POST Action operation ENDED for action class name: " + this.getClass().getName());
        
        return bResult;
    }
    
    /**
     Used to run code after the action itself takes place. Will only run if
     'execute' method return true.
     
     @see #execute()
     @return true/false upon action success/failure execution
     */
    public boolean postActionOperation() {
        return true;
    }
    
    
    public boolean __cleanup__() {
        /* Log validate */
        log.info("CLEANUP operation STARTED for action class name: " + this.getClass().getName());
        
        boolean bResult = cleanup();
        
        /* log result */
        log.info("CLEANUP operation ENDED for action class name: " + this.getClass().getName());
        
        return bResult;
    }
    
    /*
    public boolean __init__() {
        //Log validate
        log.info("INIT operation STARTED for action class name: " + this.getClass().getName());
        
        boolean bResult = init();
        
        // log result
        log.info("INIT operation ENDED for action class name: " + this.getClass().getName());
        
        return bResult;
    }
*/
    
    /**
     Used to cleanup the action.
     this is the last method being called as a part of the action execution
     (Usually used to cleanup some objects such as closing adapter connections/objects removals/etc...
     
     @see #execute()
     @return true/false upon action success/failure execution
     */
    public boolean cleanup() {
        return true;
    }
    
    public boolean init() {
        return true;
    }
    
    public boolean __execute__() throws ActionFailureException {
        try {
            /* Log validate */
            log.info("EXECUTE Action operation STARTED for action class name: " + this.getClass().getName());
            
            // First of execution, lets validate the action.
            if (!__validate__()) {
                throw new ActionFailureException(
                    "Failed to perform action execution while Validating action, validation returned FALSE!' ,optional message is: "
                    + getMsgs().toString());
            }
            
            /*
            if (!__init__()) {
                throw new ActionFailureException("Failed to perform INITIAL execution due to: " + getMsgs().toString());
            }
            */
            
            // If action was validated, execute the PRE action operation
            if (!__preActionOperation__()) {
                throw new ActionFailureException(
                    "Failed to perform action execution while calling 'Pre Action Operation' , optional message is: "
                    + getMsgs().toString());
            }
            
            // If PRE action operation returned success, execute the action itself
            if (!execute()) {
                throw new ActionFailureException(
                    "Failed to perform action execution while executing the action itself' , optional message is: "
                    + getMsgs().toString());
            }
            
            // If Action itself was executed and returned success, execute the POST
            // action operation
            if (!__postActionOperation__()) {
                throw new ActionFailureException(
                    "Failed to perform POST action execution' , optional -action- message(s) are: "
                    + getMsgs().toString());
            }
            
            __cleanup__();
            
            log.info("Succesfully finished executing action class name: " + this.getClass().getName());
        }
        //	If a method was not found
        catch (MissingMethodException mme) {
            throw new ActionFailureException(mme.toString());
        }
        //If a method is called over a null object
        catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            throw new ActionFailureException(npe.toString());
        }
        //When a method is called over a non existend 'object'
        catch (MissingPropertyException mpe) {
            throw new ActionFailureException(mpe.toString());
        } catch (Exception e) {
            log.error("A generic exception has occured while trying to execute actiom, failed with message: " + e);
            log.error("PRINTING STACK TRACE...");
            e.printStackTrace();
            getMsgs().add(e.getMessage());
            throw new ActionFailureException(e.toString());
        }
        return true;
    }
    
    /**
     
     Implementation of the action execution code itself
     <b>Note: Not called directly, always call __execute__() method!</b>
     
     @throws ActionFailureException Throws a failure exception if action execution was failed.
     @return true/false upon action success/failure execution
     */
    public abstract boolean execute() throws ActionFailureException;
    
    /**
     Rrturns the result of the action as a collection data object
     
     @return A containerDataObject that is the result of the last action. if
     its a list of objects, will return the first object in the list.
     If the action did not return an object, will return a null.
     */
    //public abstract Collection getActionResult();
    public Collection getActionResult() {
        return null;
    }
    
    public Date getExpectedExecutionDate() {
        return expectedExecutionDate;
    }
    
    public void setExpectedExecutionDate(Date expectedExecutionDate) {
        this.expectedExecutionDate = expectedExecutionDate;
    }
    
    public void setExecutionDate(Date date) {
        setExpectedExecutionDate(date);
    }
}
