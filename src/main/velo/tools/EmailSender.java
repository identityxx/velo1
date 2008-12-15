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
package velo.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import velo.validators.Generic;


/**
 * An email sender class
 * 
 * @author Administrator
 */
public abstract class EmailSender {
    
    private static Logger log = Logger.getLogger(EmailSender.class.getName());
    private List<Email> emails = new ArrayList<Email>();
    private String hostName;
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    /**
     * Constructor
     */
    public EmailSender() {
        init();
    }
    
    public abstract void init();
    
    public void addHtmlEmail(String fromAddress, String recipient, String subject, String body) throws EmailException {
    	log.trace("Sending email method has started.");
    	
    	log.trace("Sending from: " + fromAddress + "', to: '" + recipient + "', subject: '" + subject + "'");
    	
    	List<String> recps = new ArrayList<String>();

    	StringTokenizer st = new StringTokenizer(recipient, ";");
    	while(st.hasMoreTokens()){
    		String currRec=st.nextToken();
    		if (validateEmailAddress(currRec)) {
    			log.info("Email address is not valid, skipping sending mail to address: " + currRec);
    			continue;
    		}
    		
    		recps.add(currRec);
    	}
    	
    	log.trace("Parsed email recipients with amount: '" + recps.size());
    	addHtmlEmail(fromAddress,recps,subject,body);
    }
    
    
    public void addHtmlEmail(String fromAddress, Collection<String> recipient, String subject, String body) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(getHostName());
        //email.addTo("jdoe@somewhere.org", "John Doe");
        //email.addTo(recipient,recipient);
        email.setFrom(fromAddress,fromAddress);
        email.setSubject(subject);
        email.setHtmlMsg(body);
        for (String currRec : recipient) {
        	email.addTo(currRec);
        }
        
        email.setCharset("UTF-8");
        
        log.debug("Adding a new message with subject '" + subject + "', from: '" + fromAddress + "', to: '" + recipient + "' to the queue...");
        log.debug("Email server parameters - SMTP host: " + getHostName());
        emails.add(email);
    }
    
    public void addEmail(Email email) {
        emails.add(email);
    }
    
    public void send() {//throws MessagingException {
        log.debug("Sending messages in queue, list now contains: '" + emails.size() + "' emails");
        
        int failures = 0;
        //List<String> errorMessages = new ArrayList<String>();
        StringBuffer errorMsg = new StringBuffer();
        
        for (Email currEmail : emails) {
        	log.trace("Currently Sending email with subject '" + currEmail.getSubject() + "', please wait...");
            try {
                currEmail.send();
                //Transport.send(currMsg);
            } catch (EmailException ee) {
                failures++;
                //errorMessages.add(me.getMessage());
                errorMsg.append(ee.getMessage() + " | ");
            }
            
            if (failures > 0) {
                //throw new MessagingException("Failed to send '" + failures + "' messages out of '" + emails.size() + "', dumping failure messages: " + errorMsg);
            	//TODO: Handle! (Jboss does not know what is MesagingException)
            }
            break;
        }
        
        if (errorMsg.length() > 0) {
        	log.error("An error has occured while trying to send one or more emails: " + errorMsg.toString());
        }
    }
    
    
    public boolean validateEmailAddress(String emailAddress) {
    	return !Generic.isEmailValid(emailAddress); 
    }
}
