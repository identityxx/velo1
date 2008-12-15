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


import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.TaskManagerLocal;
import velo.entity.Task;
import velo.exceptions.TaskExecutionException;

@MessageDriven(name="TaskExecuterMessageBean", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
		@ActivationConfigProperty(propertyName="destination", propertyValue="queue/velo/TasksDefaultQueue")
})
//GF@MessageDriven(mappedName="TaskExecutionQueue")
public class MDBTaskExecuter implements MessageListener {
//public class MDBTaskExecuter {
    /**
     * Inject A local Request EJB bean
     */
	@EJB
    public TaskManagerLocal taskManager;
    
    //Default task executer
    private static String defaultExecuterClass = "velo.tasks.DefaultTaskExecuter";
    
    
    private static Logger logger = Logger.getLogger(MDBTaskExecuter.class.getName());
    
    //JB
    @Resource
    private MessageDrivenContext context;
    
    
    
    public void onMessage(Message msg) {
        logger.debug("Received a message for TaskExecution! executing task!");
        logger.trace("JMS Message: " + msg);
        
        if (msg instanceof BytesMessage) {
            logger.trace("Recieved a bytes message(the taskID to be executed)...");
            BytesMessage bm = null;
            
            //Cast the message to an ObjectMessage
            bm = (BytesMessage) msg;
            
            try {
                long taskId = bm.getLongProperty("taskId");
                logger.trace("recieved task in JMS message with task ID: '" + taskId + "', loading entity and performing task execution");

                //Load the task entity by the specified ID
                Task task = taskManager.findTaskById(taskId);
                taskManager.executeTask(task);
                
                bm = null;
            //appearntly other exceptions are thrown such as 'JmsServerSession', better to catch them all
            //} catch (JMSException jmse) {
            } catch (Exception e) {
                bm = null;
                logger.error("A JMS EXCEPTION HAS OCCURED, PRINTING STACKTRACE............!");
                
                e.printStackTrace();
                //JB - set rollback!
                context.setRollbackOnly();
                //TODO What else could be done if the task could not be fetched anyway?
            }
        }
        
        /*
        if (msg instanceof ObjectMessage) {
            logger.trace("Recieved an object message (The whole task)!");
            ObjectMessage om = null;
            
            //Cast the message to an ObjectMessage
            om = (ObjectMessage) msg;
            
            //logger.finest("Recieved message: " + om);
            try {
                Task task = (Task)om.getObject();
                logger.info("Fetched task from JMS message, executing task ID '" + task.getTaskId() + "'");
                
                try {
                    executeTask(task);
                    msg = null;
                } catch (TaskExecutionException tee) {
                    //Indicate a task failure
                    task.addLog("Error","Error occured while trying to execute task!", tee.getMessage());
                    taskManager.indicateTaskExecutionFailure(task);
                    msg = null;
                    //JB - set rollback!
                    context.setRollbackOnly();
                }
                
                msg = null;
            } catch (JMSException jme) {
                logger.error("A JMS EXCEPTION HAS OCCURED, PRINTING STACKTRACE............!");
                jme.printStackTrace();
                //TODO What else could be done if the task could not be fetched anyway?
                msg = null;
                //JB - set rollback!
                context.setRollbackOnly();
            }
        }
        */
    }
    
    
    
    private void executeTask(Task task) throws TaskExecutionException {
    	//TODO: fix
        //try {
        	/*
            if (task.getTaskDefinition().getClassName() == null) {
                logger.debug("No special executer was set, executing with default task executer");
                
                TaskExecuter dte = (TaskExecuter) Factory.factoryInstance(defaultExecuterClass);
                dte.execute(task);
            } else {
                logger.debug("Special executer was set, executing task with executer class name: " + task.getTaskDefinition().getClassName());
                TaskExecuter te = (TaskExecuter) Factory.factoryInstance(task.getTaskDefinition().getClassName());
                te.execute(task);
            }
            
            task = null;
        } catch (FactoryException fe) {
            task = null;
            throw new TaskExecutionException(fe);
        }
        */
    }
}
