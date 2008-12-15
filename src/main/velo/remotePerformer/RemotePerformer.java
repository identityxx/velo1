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
package velo.remotePerformer;


import java.util.Hashtable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.TaskManagerRemote;
import velo.entity.Task;
import velo.exceptions.FactoryException;
import velo.exceptions.TaskExecutionException;
import velo.patterns.Factory;
import velo.tasks.TaskExecuter;


/**
 *
 * @author Asaf Shakarchi
 */
public class RemotePerformer {
    
    private final String queueConnectionFactoryResource = "ConnectionFactory";
    private final String tasksQueueName = "velo/TasksDefaultQueue";
    private boolean serverRunning;
    
    private static Logger logger = Logger.getLogger(RemotePerformer.class.getName());
    private static String defaultExecuterClass = "velo.tasks.DefaultTaskExecuter";
    private static RemotePerformer server;
    public RemotePerformer() {
        
    }
    
    //Irrelevant out of the container
    //@Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    //Irrelevant out of the container
    //@Resource(mappedName = "jms/Queue")
    private static Queue queue;
    //Irrelevant out of the container
    //@Resource(mappedName = "jms/Topic")
    //private static Topic topic;
    
    private void stopServer() {
        serverRunning = false;
    }
    
    private void startServer() throws Exception {
    	logger.info("Starting Remote Performer, please wait...");
    	logger.debug("Initializing resources...");
        InitialContext ie = null;
        String destType = null;
        Connection connection = null;
        Session session = null;
        Destination dest = null;
        MessageConsumer consumer = null;
        TextMessage message = null;
        
        
        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        environment.put(Context.PROVIDER_URL, "jnp://localhost:1099"); // remote machine IP
        ie = new InitialContext(environment);
        
        logger.debug("Creating a JMS connection factory by resource name: " + queueConnectionFactoryResource);
        connectionFactory = (ConnectionFactory)ie.lookup(queueConnectionFactoryResource);
        
        //dest = (Destination) queue;
        
        
        /*
        try {
            dest = (Destination) queue;
            //dest = (Destination) topic;
        } catch (Exception e) {
            throw new Exception("Error while trying to set destination: " + e.toString());
        }
        */
        
        /*
         * Create connection.
         * Create session from connection; false means session is
         * not transacted.
         * Create consumer, then start message delivery.
         * Receive task IDs encapsulated as bytes messages and perform execution
         */
        try {
        	logger.debug("Creating a connection...");
        	connection = connectionFactory.createConnection();
        	logger.debug("Creating a temporary QUEUE named: " + tasksQueueName);
        	
        	logger.debug("Creating a JMS session...");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            //Create a temporary queue
            logger.debug("Creating a temporary JMS Queue...");
            //dest = session.createTemporaryQueue();
            queue = session.createQueue(tasksQueueName);
            
            //queue = (Queue)ie.lookup(tasksQueueName);
            
            
            //consumer = session.createConsumer(dest);
            consumer = session.createConsumer(queue);
            connection.start();
            
            TaskManagerRemote taskManager = (TaskManagerRemote)ie.lookup("velo/TaskBean/remote");
            
            logger.debug("Initialized ConnectionFactory object: " + connectionFactory.toString());
            //System.out.println("Initialized Queue object: " + queue.toString());
            
            
            logger.info("Remote Performer Started...!");
            serverRunning = true;
            while (serverRunning) {
                Message m = consumer.receive(1);
                
                if (m != null) {
                	logger.debug("Recieved a message!, seeking for Task ID...");
                    /*if (m instanceof TextMessage) {
                        message = (TextMessage) m;
                        System.out.println(
                                "Reading message: " + message.getText());
                    } else*/if (m instanceof BytesMessage) {
                        BytesMessage bm = (BytesMessage)m;
                        long taskId = bm.getLongProperty("taskId");
                        //logger.fine("Fetched TaskID '" + taskId + "' to be executed");
                        logger.debug("Fetched TaskID '" + taskId + "' to be executed, loading task");
                        Task task = taskManager.findTaskById(taskId);
                        logger.debug("Loaded task object: " + task.toString() +", performing execution.");
                        
                        try {
                            executeTask(task);
                            
                            bm = null;
                            m = null;
                        } catch (TaskExecutionException tee) {
                            //Indicate a task failure
                            task.addLog("Error","Error occured while trying to execute task!", tee.getMessage());
                            taskManager.indicateTaskExecutionFailure(task,null);
                            bm = null;
                            m = null;
                        }
                        
                        
                        
                        
                        
                    } /*else {
                        if (connection != null) {
                            connection.close();
                        }
                        break;
                    }*/
                }
            }
            
            
            logger.info("Recieved a signal to stop the RP");
            logger.info("RP stopped!");
        } catch (JMSException e) {
        	e.printStackTrace();
        	logger.error("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                	e.printStackTrace();
                }
            }
        }
    }
    
    private void executeTask(Task task) throws TaskExecutionException {
    	logger.info("Executing task...");
        try {
            //if (task.getTaskDefinition().getClassName() == null) {
        	if (1==1) {
                logger.debug("No special executer was set, executing with default task executer");
                
                TaskExecuter dte = (TaskExecuter) Factory.factoryInstance(defaultExecuterClass);
                dte.execute(task);
            } else {
            	//JB!
            	//TODO: FIX!!!
                //logger.debug("Special executer was set, executing task with executer class name: " + task.getTaskDefinition().getClassName());
               // TaskExecuter te = (TaskExecuter) Factory.factoryInstance(task.getTaskDefinition().getClassName());
                //te.execute(task);
            }
            
            task = null;
        } catch (FactoryException fe) {
            task = null;
            throw new TaskExecutionException(fe);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public static void start() {
        if (server == null) {
            server = new RemotePerformer();
        }
        
        try {
            server.startServer();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void stop() {
        if (server == null) {
            
        }
        else {
            server.stopServer();
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        try {
            RemotePerformer rps = new RemotePerformer();
            rps.startServer();
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("An exception has occured while the Remote Performer Service is running or at startup: " + e.toString());
            //e.printStackTrace();;
        }
    }
}
