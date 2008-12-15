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
package velo.ejb.impl;

import groovy.lang.GroovyObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import velo.contexts.OperationContext;
import velo.ejb.interfaces.EventManagerLocal;
import velo.ejb.interfaces.EventManagerRemote;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.entity.EventDefinition;
import velo.entity.EventResponse;
import velo.entity.EventResponseTask;
import velo.exceptions.EventResponseException;
import velo.exceptions.FactoryException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.ScriptInvocationException;
import velo.scripting.GenericTools;
import velo.scripting.ScriptingManager;

/**
 * A Stateless EJB bean for managing Events
 * 
 * @author Asaf Shakarchi
 */
@Stateless()
public class EventBean implements EventManagerLocal, EventManagerRemote {
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	@EJB
	TaskManagerLocal taskManager;

	@EJB
	EventManagerLocal eventManager;

	private static Logger log = Logger.getLogger(EventBean.class.getName());

	
	
	public void invokeEvent(EventDefinition eventDef, OperationContext context) throws ScriptInvocationException {
		log.info("Invoking event definition '" + eventDef.getDisplayName() + "'");
		Set<EventResponseTask> ertList = factorEventResponseTasks(eventDef,context);
		Set<EventResponseTask> ertListToInvoke = new HashSet<EventResponseTask>();

		log.debug("Iterating over event response tasks to invoke, determining whether to invoke/persist tasks");
		for (EventResponseTask currERT : ertList) {
			if (currERT.getEventResponse().isPersistence()) {
				log.trace("event response task '" + currERT.getEventResponse().getDescription() + "' is flagged as persistence, persisting task");
				taskManager.persistTask(currERT);
			} else {
				log.trace("event response task '" + currERT.getEventResponse().getDescription() + "' was not flagged as persistence, invoking task immediately.");
				ertListToInvoke.add(currERT);
			}
		}
		
		
		if (ertListToInvoke.size() > 0) {
			for (EventResponseTask currERT : ertListToInvoke) {
				invokeEventResponseTask(currERT, context);
			}
		}
	}
	
	private Set<EventResponseTask> factorEventResponseTasks(EventDefinition eventDef, OperationContext context) {
		log.debug("Factoring active event responses tasks with amount '" + eventDef.getActiveEventResponses().size() + "'");
		log.debug("!!!!!!!!!!!!1Factoring active event responses tasks with amount '" + eventDef.getActiveEventResponses().size() + "'");
		Set<EventResponseTask> ertList = new HashSet<EventResponseTask>();
		for (EventResponse currER : eventDef.getActiveEventResponses()) {
			try {
				ertList.add(EventResponseTask.factory(currER, context));
			} catch (ObjectFactoryException e) {
				log.error("Could not factory event response task, skipping response...");
				continue; 
			}
		}
		
		log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!(7)");
		
		return ertList;
	}
	
	//public void invokeEventDefinitionResponses(EventDefinition eventDef, OperationContext context) throws ScriptInvocationException {
	public void invokeEventResponseTask(EventResponseTask eventResponseTask, OperationContext context) throws ScriptInvocationException {
		ScriptingManager sm = new ScriptingManager();
		
		log.info("Invokign event response(task) '" + eventResponseTask.getEventResponse().getDescription() + "'");
		try {
			ScriptEngine se = sm.getScriptEngine(eventResponseTask.getEventResponse().getActionLanguage().getName().toLowerCase());
			
			//meaning task is persistence, de-serialize the context
			if (context == null) {
				log.debug("Context is null, meaning event(task) is persistence, de-serializing context...");
				se.put("cntx",eventResponseTask.getDeserializedContent());
			} else {
				log.debug("Context is available, meaning event(task) is not persistence, adding context to script");
				se.put("cntx", context);				
			}
			
			se.put("log", log);
			GenericTools tools = new GenericTools();
			se.put("tools", tools);
			//entities
			if (eventResponseTask.getUserRef() != null) {
				se.put("user", eventResponseTask.getUserRef());
			}
			if (eventResponseTask.getTaskRef() != null) {
				se.put("task", eventResponseTask.getTaskRef());
			}
			if (eventResponseTask.getRequestRef() != null) {
				se.put("request", eventResponseTask.getRequestRef());
			}
			
			
			//invoke the action
			log.trace("Invoking default method over scripted object");
			se.eval(eventResponseTask.getEventResponse().getContent());
			log.trace("Ended method invocation");
			
		} catch (FactoryException e) {
			String errMsg = "Could not factory scripting manager: " + e.toString();
			log.error(errMsg);
			//setErrorMessage(errMsg);
		} catch (ScriptException e) {
			log.error("Failed to execute event response ID '" + eventResponseTask.getEventResponse().getDescription() + "': " + e.toString());
			
			log.trace("Determining whether the failed resource action is showStopper or not");
			if (eventResponseTask.getEventResponse().isShowStopper()) {
				log.trace("Resource Action is a showStopper, indicating Resource Operation as a failure and stoppoing its invocation.");
				//setErrorMessage(e.toString());
				//return false;
				throw new ScriptInvocationException("Event Response '" + eventResponseTask.getEventResponse().getDescription() + "' was failed and indicated as a showstopper: " + e.getMessage());
			}
			else {
				log.trace("Resource Action is not a showstopper, continuing Resource Operation invocation...");
				log.error(e.getMessage());
				//TODO: Log the error, so it could be then logged into the relevant task log..
			}
		}
	}
	
	
	public EventDefinition find(String uniqueName) {
		
		log.debug("Finding Event Definition in repository with uniqueName '" + uniqueName + "'");

		try {
			Query q = em.createNamedQuery("eventDefinition.findByUniqueName").setParameter("uniqueName",uniqueName);
			return (EventDefinition) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("'Find Event Definition' did not result any EventDefintion with unique name '" + uniqueName + "', returning null.");
			return null;
		}
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	
	
	//TODO: Consulidate with resourceActions/resource attribute object scripting object
	
	
	
	
	/*
	@Deprecated
	public BulkTask createEventResponsesOfEventDefinitionBulkTask(EventDefinition ed, List<Map<String, Object>> responsesProperties) throws EventResponseException {
		logger.info("Initializing event response definitions for Event Definition ID: "+ ed.getEventDefinitionId());
		BulkTask bulkTask = new BulkTask();
		bulkTask.setDescription("Event Responses for event name: '"+ ed.getDisplayName() + "'");

		Map<EventResponse, Object> eventResponses = new HashMap<EventResponse, Object>();

		for (EventResponse currERD : ed.getEventResponses()) {
			if (currERD.isActive()) {
				try {
					//TODO: Support scripting types?( not sure it's here )
					// Create the groovy object from the script
					logger.fine("Creating Response object for current name: " + currERD.getDescription());
					
					
					//execute ERD
					
					
					
					
					ScriptFactory sf = new ScriptFactory();
						GroovyObject currEventResponseGroovyObject = sf
								.factoryScriptableObject(currERD
										.retrieveScriptContent());
						eventResponses.put(currERD,
								currEventResponseGroovyObject);
					}
				} catch (ScriptLoadingException sle) {
					if (currERD.isIntegrity()) {
						throw new EventResponseException(ed, currERD, sle
								.getMessage());
					} else {
						logger
								.warning("Could not generate EventResponse object for EventResponseDefinition ID: '"
										+ currERD
												.getEventResponseDefinitionId()
										+ "', EventResponse was skipped due to: "
										+ sle.getMessage());
						continue;
					}
				} catch (FileNotFoundException fnfe) {
					if (currERD.isIntegrity()) {
						throw new EventResponseException(ed, currERD, fnfe
								.getMessage());
					} else {
						logger
								.warning("Could not generate EventResponse object for EventResponseDefinition ID: '"
										+ currERD
												.getEventResponseDefinitionId()
										+ "', EventResponse was skipped due to: "
										+ fnfe.getMessage());
						continue;
					}
				} catch (ClassCastException cce) {
					if (currERD.isIntegrity()) {
						throw new EventResponseException(ed, currERD, cce
								.getMessage());
					} else {
						logger
								.warning("There was a problem to cast the Response object to class named: '"
										+ ed.getResponseDefinitionClassName()
										+ "'");
						continue;
					}
				}
			} else {
				logger.info("Event Response ID: '"
						+ currERD.getEventResponseDefinitionId()
						+ "' is not active, skipping event response...");
			}
		}

		logger.fine("Created Event Response Definitions with amount '"
				+ eventResponses.size() + "'");

		TaskDefinition eventResponseScriptedTaskDef = taskManager
				.findTaskDefinition("EVENT_RESPONSE_SCRIPTED");

		for (Map<String, Object> currResponseProperties : responsesProperties) {
			for (Map.Entry<EventResponse, Object> currResponse : eventResponses
					.entrySet()) {
				try {
					// Factory a responseEvent class of the correct class type
					logger.fine("Factoring ResponseEvent of class named: '"
							+ ed.getResponseDefinitionClassName());
					Class responseClass = Factory.factory(ed
							.getResponseDefinitionClassName());
					logger.fine("Factored class named: "
							+ responseClass.getName());

					// Cast the groovyObject to the factored Response Class
					// above.
					logger
							.fine("Casting the scripted object to the created class above.");
					responseClass.cast(currResponse.getValue());
					logger
							.fine("Successfully casted scripted object to the class created above.");

					logger
							.finest("Iterating over the specified properties, injecting them into the response class if possible...");
					for (Map.Entry<String, Object> currProperty : currResponseProperties
							.entrySet()) {
						try {
							logger.finest("Iterating over field name: '"
									+ currProperty.getKey()
									+ "', trying to inject...");
							Field currField = responseClass
									.getField(currProperty.getKey());
							currField.set(currResponse.getValue(), currProperty
									.getValue());
						} catch (NoSuchFieldException nsfe) {
							logger
									.warning("Field named: '"
											+ currProperty.getKey()
											+ "' was not found in reasponse class named: '"
											+ ed
													.getResponseDefinitionClassName()
											+ "'");
							continue;
						}
					}

					EventResponse er = (EventResponse) currResponse.getValue();
					try {
						// Init the event, important for the
						// persistence/expectedExecutionDate properties!
						er.initEventResponse();
					} // catch (InitException ex) {
					catch (Exception ex) { // Scriptable, any exception may
											// occure here...
						if (currResponse.getKey().isIntegrity()) {
							throw new EventResponseException(ed, currResponse
									.getKey(), ex.getMessage());
						} else {
							logger
									.warning("Cannot init event response, skipping response, failure message is: "
											+ ex.getMessage());
							continue;
						}
					}

					boolean persistence = false;
					// If response is persistence, then create a corresponding
					// task, otherwise execute the response now.
					if (er.isPersistenceModified()) {
						persistence = er.isPersistence();
					} else {
						persistence = currResponse.getKey().isPersistence();
					}

					if (persistence) {
						Task task = eventResponseScriptedTaskDef.factoryTask();
						task.setDescription("Event response '"
								+ currResponse.getKey().getDescription()
								+ "', for event '" + ed.getDisplayName() + "'");
//TODO: FIX!!
//						task.serializeAsTask(er);
//						task.setScript(currResponse.getKey()
//								.retrieveScriptContent());
						task.setExpectedExecutionDate(er
								.getExpectedExecutionDate());
						task.setBulkTask(bulkTask);

						bulkTask.addTask(task);
					} else {
						// Handle execution correctly.
						try {
							er.__execute__();
						} catch (ActionFailureException afe) {
							if (currResponse.getKey().isIntegrity()) {
								throw new EventResponseException(
										"Failed to execute event Response '"
												+ currResponse.getKey()
														.getDescription()
												+ "', related to Event '"
												+ ed.getDisplayName() + "': ",
										afe);
							} else {
								logger
										.warning("Failed to execute event Response '"
												+ currResponse.getKey()
														.getDescription()
												+ "', related to Event '"
												+ ed.getDisplayName()
												+ "': "
												+ afe);
							}
						}
					}

				} catch (IllegalAccessException iae) {
					if (currResponse.getKey().isIntegrity()) {
						throw new EventResponseException(ed, currResponse
								.getKey(), iae.getMessage());
					} else {
						logger
								.warning("Illegal Access Exception, failure message is: "
										+ iae.getMessage());
						continue;
					}
				} catch (ClassNotFoundException cnfe) {
					if (currResponse.getKey().isIntegrity()) {
						throw new EventResponseException(ed, currResponse
								.getKey(), cnfe.getMessage());
					} else {
						logger
								.warning("Could not factory EventResponse class, failure message is: "
										+ cnfe.getMessage());
						continue;
					}
				} //catch (FileNotFoundException fnfe) {
				//	if (currResponse.getKey().isIntegrity()) {
				//		throw new EventResponseException(ed, currResponse
				//				.getKey(), fnfe.getMessage());
				//	} else {
				//		logger
				//				.warning("Could not generate EventResponse object for EventResponseDefinition ID: '"
				//						+ currResponse.getKey()
				//								.getEventResponseDefinitionId()
				//						+ "', EventResponse was skipped due to: "
				//						+ fnfe.getMessage());
				//		continue;
					//}
				//}
			}
		}

		logger
				.info("Initialized event response definitions for Event Definition ID: "
						+ ed.getEventDefinitionId()
						+ "with size of: '"
						+ bulkTask.getTasks().size());

		// setInitialized(true);

		return bulkTask;
	}
*/
	
	
	@Deprecated
	public void createEventResponsesOfEventDefinition(EventDefinition ed,
			Map<String, Object> responseProperties)
			throws EventResponseException {
		List<Map<String, Object>> responsesProperties = new ArrayList<Map<String, Object>>();
		responsesProperties.add(responseProperties);

		/*JB
		BulkTask bt = createEventResponsesOfEventDefinitionBulkTask(ed,
				responsesProperties);
		if (bt.getTasks().size() > 0) {
			taskManager.persistBulkTask(bt);
		}
		*/
	}

	@Deprecated
	public void createEventResponsesOfEventDefinition(EventDefinition ed,
			List<Map<String, Object>> responsesProperties)
			throws EventResponseException {
		/*JB
		BulkTask bt = createEventResponsesOfEventDefinitionBulkTask(ed,
				responsesProperties);

		if (bt.getTasks().size() > 0) {
			taskManager.persistBulkTask(bt);
		}
		*/
	}

	
	
	
	
	
	
	
	
	
	//TODO: Move to a global place, since ResourceOperationBean has such a method too
	//currently available under 'actionDefinition'
	public void invoke(GroovyObject go) throws ScriptInvocationException {
		try {
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
	
	
	
}