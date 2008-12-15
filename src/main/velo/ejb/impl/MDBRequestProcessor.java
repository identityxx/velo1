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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.RequestManagerLocal;
import velo.entity.Request;
import velo.exceptions.OperationException;

//JB@MessageDriven(mappedName="RequestProcessingQueue")
@MessageDriven(name="RequestExecuterMessageBean", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
		@ActivationConfigProperty(propertyName="destination", propertyValue="queue/velo/RequestsDefaultQueue")
})
public class MDBRequestProcessor implements MessageListener {
    
    
    /**
     * Inject A local Request EJB bean
     */
	@EJB
    public RequestManagerLocal requestManager;
	
	//@PersistenceContext
	//EntityManager entityManager;
    
    private static Logger log = Logger.getLogger(MDBRequestProcessor.class.getName());
    
    @Resource
    private MessageDrivenContext context;
    
    
    public void onMessage(Message msg) {
    	log.info("Recieved a request message for processing");
        BytesMessage bm = null;
        
        bm = (BytesMessage) msg;
        
        try {
            long requestId = bm.getLongProperty("requestId");
            log.trace("recieved request for processing in JMS message for request ID: '" + requestId + "', loading corresponding entity and performing request processing");

            //Load the task entity by the specified ID
            Request request = requestManager.findRequest(requestId);
            
            try {
            	requestManager.process(request);
            } //catch (OperationException e) {
            catch(Throwable e) {
            	log.error("An uncatched exception has occurd while trying to process request '" + requestId + "'");
            	e.printStackTrace();
            	
            	/*
            	log.error(e.getMessage());
            	request.setInProcess(false);
            	request.setSuccessfullyProcessed(false);
            	request.setProcessed(true);
            	request.addLog("Error", "Failed to process request", e.toString());
            	//requestManager.mergeRequestEntity(request);
            	log.trace("MERGING REQUEST!!!!!!!!!!!!!!!!!!!!!!!!(A)");
            	entityManager.merge(request);
            	log.trace("MERGING REQUEST!!!!!!!!!!!!!!!!!!!!!!!!(B)");
            	*/
            }
            
            bm = null;
        } catch (JMSException jmse) {
            bm = null;
            log.error("A JMS exception has occured while trying to perform request processing, printing stack trace!");
            jmse.printStackTrace();
            //JB - set rollback!
            context.setRollbackOnly();
            //TODO What else could be done if the task could not be fetched anyway?
        }
    }
}
