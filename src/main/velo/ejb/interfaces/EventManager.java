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
package velo.ejb.interfaces;

import velo.contexts.OperationContext;
import velo.entity.ReconcileEvent;
import velo.entity.ReconcilePolicy;
import velo.exceptions.EventExecutionException;

/**
 * An Event Manager interface for all EJB exposed methods
 *
 * @author Asaf Shakarchi
 */
public interface EventManager {
	public void raiseSystemEvent(String eventUniqueName, OperationContext context) throws EventExecutionException;
	public void raiseReconcileEvent(ReconcileEvent event, ReconcilePolicy rp, OperationContext context) throws EventExecutionException;
	//VERY VERY NOT EFFCIENT
	public void raiseReconcileEvent(String eventUniqueName, ReconcilePolicy rp, OperationContext context) throws EventExecutionException;
	public ReconcileEvent findReconcileEvent(String uniqueName);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    /**
     * Find an EventDefinition by unique name
     * @param uniqueName The unique name of the event definition to find
     * @return A loaded EventDefinition entity
     */
    //public EventDefinition find(String uniqueName);

    
	//public void invokeEventDefinitionResponses(EventDefinition eventDef, OperationContext context) throws ScriptInvocationException;
    //public void invokeEvent(EventDefinition eventDef, OperationContext context) throws ScriptInvocationException;
    
    //public void invokeEventResponseTask(EventResponseTask eventResponseTask, OperationContext context) throws ScriptInvocationException;
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
     * Find an EventDefinition entity by ID
     * @param eventDefinitionId The ID of the event definition to find
     * @return A loaded EventDefinition entity
     * @throws NoResultFoundException If no result was found
     */
    //@Deprecated
    //public EventDefinition findEventDefinitionById(Long eventDefinitionId)throws NoResultFoundException;
    
    /*
     * Find the entire list of event definitions
     * @return A collection of EventDefinitionse entities fetched from the database
     */
    //@Deprecated
    //public Collection<EventDefinition> findAllEventDefinitions();
    
    /**
     * Whether an EventDefinition entity exists or not
     * @param uniqueName The unique name of the EventDefinition to check for existance
     * @return true/false upon existance/non-existance
     */
    //@Deprecated
    //public boolean isEventDefinitionExistsByUniqueName(String uniqueName);
    
    /*
     * Persist an EventDefinition entity in the database
     * @param eventDefinition The EventDefinition entity to persist
     *
    //@Deprecated
    //public void persistEventDefinition(EventDefinition eventDefinition);
    
    /*
     * Delete an EventDefinition entity from the database
     * @param eventDefinition The EventDefinition entity to delete
     */
    //@Deprecated
    //public void deleteEventDefinition(EventDefinition eventDefinition);
    
    /*
     * Update an existed EventDefinition entity in the database
     * @param eventDefinition The EventDefinition entity to update(merge)
     */
    //@Deprecated
    //public void updateEventDefinition(EventDefinition eventDefinition);
    
    
    
    
    
    
    
//	--START OF *EVENT RESPONSE DEFINITIONS* METHODS--
    
    /*
     * Find an EventResponseDefinition by unique name
     * @param eventResponseDefinitionId The ID of the EventResponseDefinition to find
     * @return A loaded EventResponseDefinition entity
     * @throws NoResultFoundException If no result was found
     */
    //@Deprecated
    //public EventResponse findEventResponseDefinitionById(Long eventResponseDefinitionId)throws NoResultFoundException;
    
    /*
     * Find the entire list of EventResponseDefinitions
     * @return A collection of EventResponseDefinition entities fetched from the database
     */
    //@Deprecated
    //public Collection<EventResponse> findAllEventResponseDefinitions();
    
    /*
     * Persist an EventResponseDefinition entity in the database
     * @param eventResponseDefinition The EventResponseDefinition entity to persist
     */
    //@Deprecated
    //public void persistEventResponseDefinition(EventResponse eventResponseDefinition);
    
    /*
     * Delete an EventResponseDefinition entity from the database
     * @param eventResponseDefinition The EventResponseDefinition entity to delete
     */
    //@Deprecated
    //public void deleteEventResponseDefinition(EventResponse eventResponseDefinition);
    
    /*
     * Update an existed EventResponseDefinition entity in the database
     * @param eventResponseDefinition The EventResponseDefinition entity to update(merge)
     */
    //@Deprecated
    //public void updateEventResponseDefinition(EventResponse eventResponseDefinition);
    
    //@Deprecated
    //public BulkTask createEventResponsesOfEventDefinitionBulkTask(EventDefinition ed, List<Map<String,Object>>responsesProperties) throws EventResponseException;
    
    //@Deprecated
    //public void createEventResponsesOfEventDefinition(EventDefinition ed, Map<String,Object>responseProperties) throws EventResponseException;

    //@Deprecated
    //public void createEventResponsesOfEventDefinition(EventDefinition ed, List<Map<String,Object>> responsesProperties) throws EventResponseException;
}
