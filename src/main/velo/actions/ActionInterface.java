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

import java.util.Collection;
import java.util.HashMap;

import velo.common.EdmMessages;

import velo.exceptions.ActionFailureException;
import java.util.Date;


/**
 Action Interface,
 An interface for all actions
 
 @author Asaf Shakarchi
 @version 0.1
 */
@Deprecated
public interface ActionInterface {
    /**
     External execute method, wraps 'execute()' method in order to provide logging messages before/after the execution
     Each action execution is done by calling this method.
     Alogirhtm:
     - Call '__validate__()' method. If action validated, continue to the next step, otherwise fail.
     - Call '__preActionOperation__()' method. If preActionOperation returned true, continue to the next step, otherwise fail.
     - Call 'execute()' method. if execution returned true, continue to the next step, otherwise fail.
     - Call '__postActionOperation__()', if postAcitonOperation returned true, continue to the next step, otherwise fail.
     - Return true if action was successfully executed!
     
     @return true/false upon action execution success/failure
     
     @throws ActionFailureException Thrown when action execution fails
     */
    public boolean __execute__() throws ActionFailureException;
    
    /**
     Get the messages component used to log messages related to the action
     
     @return An EdmMessages component for logging messages
     */
    public EdmMessages getMsgs();
    
    /**
     @param properties the properties to set
     */
    public void setProperties(HashMap properties);
    
    /**
     @return the properties
     */
    public HashMap getProperties();
    
    public Collection getActionResult();
    
    public boolean init();
    
    public Date getExpectedExecutionDate();
}
