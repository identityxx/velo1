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

import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import velo.ejb.interfaces.ApproversGroupManagerLocal;
import velo.ejb.interfaces.EmailManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.ApproversGroup;
import velo.entity.EmailTemplate;
import velo.entity.User;
import velo.exceptions.EmailNotificationException;
import velo.exceptions.ExpressionCreationException;
import velo.tools.EdmEmailSender;
import velo.validators.Generic;

@Stateless()
@Name("emailBean")
@AutoCreate
public class EmailBean implements EmailManagerLocal {
	
	private static Logger log = Logger.getLogger(EmailBean.class.getName());
	
	
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;
	
	@EJB
	public ApproversGroupManagerLocal approversGroupManager;
	
	@EJB
	public UserManagerLocal userManager;
	
	public EmailTemplate findEmailTemplate(String name) {
		log.debug("Finding Email Template in repository for name '" + name + "'");

		try {
			Query q = em.createNamedQuery("emailTemplate.findByName").setParameter("name",name);
			return (EmailTemplate) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			log.debug("Found user did not result any email template for name '" + name + "', returning null.");
			return null;
		}
	}
	
	
	public void sendEmailsToApproversGroup(String agUniqueName, String emailTemplateName, Map<String,Object> varsMap) throws EmailNotificationException {
		ApproversGroup ag = approversGroupManager.findApproversGroup(agUniqueName);
		
		
		if (ag == null) {
			throw new EmailNotificationException("Could not find Approvers Group with name '" + agUniqueName + "'");
		}
		
		EmailTemplate et = findEmailTemplate(emailTemplateName);
		if (et == null) {
			throw new EmailNotificationException("Could not find Email Template with name '" + emailTemplateName + "'");
		}
		
		InitialContext ctx = null;
    	TransactionManager tm = null;
        try {
          ctx = new InitialContext();
          tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
        }catch(Exception e) {
        	e.printStackTrace();
        }
        finally {
           if(ctx!=null) {
              try { ctx.close(); } catch (NamingException e) {}
           }
        }
        
        /*
        try {
    		System.out.println("!!!!!!!!!!!!!!!zzzzzzzzzzZ2: " + tm.getStatus());
    		System.out.println("!!!!!!!!!!!!!!!Zzzzzzzzzz2: " + tm.getTransaction().getStatus());
    	}catch(SystemException e) {
    		e.printStackTrace();
    	}
    	*/
    	
		//throw new EmailNotificationException("ERROR ERROR ERROR!");
    	
		et.setContentVars(varsMap);
		
		EdmEmailSender es = new EdmEmailSender();
		
		Email email = null;
		try {
			email = es.factoryEmail(et.getSubject(),et.getParsedContent());
			//email.addTo("asaf@mydomain.com");
			//email.send();
		}catch (EmailException e) {
			log.error("Could not factory email objecT: " + e.getMessage());
			throw new EmailNotificationException(e);
		}catch (ExpressionCreationException e) {
			log.error("Could parsing email content: " + e.getMessage());
			throw new EmailNotificationException(e);
		}

		for (User currUser : ag.getApprovers()) {
			try {
				if (Generic.isEmailValid(currUser.getEmail())) {
					email.addTo(currUser.getEmail());
				} else {
					log.warn("The specified email address is not valid, skipping email: " + currUser.getEmail());
				}
			}catch(EmailException e) {
				log.error("Could not send email to user: " + currUser.getName() + " (email address: '" + currUser.getEmail() + "'), part of ApproversGroup '" + ag.getDescription() + "' due to: " + e.getMessage());
			}
		}
		
		
		try {
			email.send();
		}catch(EmailException e) {
			throw new EmailNotificationException(e);
		}
	}
	
	public void sendEmailToUser(String userName, String emailTemplateName, Map<String,Object> varsMap) throws EmailNotificationException {
		EmailTemplate et = findEmailTemplate(emailTemplateName);
		if (et == null) {
			throw new EmailNotificationException("Could not find Email Template with name '" + emailTemplateName + "'");
		}
		
		et.setContentVars(varsMap);
		
		User user = userManager.findUser(userName);
		
		if (user == null) {
			throw new EmailNotificationException("Could not find User name '" + emailTemplateName + "'");
		}
		
		
		EdmEmailSender es = new EdmEmailSender();
		Email email = null;
		try {
			email = es.factoryEmail(et.getSubject(),et.getParsedContent());
		}catch (EmailException e) {
			log.error("Could not factory email objecT: " + e.getMessage());
			throw new EmailNotificationException(e);
		}catch (ExpressionCreationException e) {
			log.error("Could parsing email content: " + e.getMessage());
			throw new EmailNotificationException(e);
		}
		
		try {
			email.addTo(user.getEmail());
		}catch(EmailException e) {
				log.error("Could not send email to user: " + user.getName() + " (email address: '" + user.getEmail() + "')" + "' due to: " + e.getMessage());
		}
		
		try {
			email.send();
		}catch(EmailException e) {
			throw new EmailNotificationException(e);
		}
	}
}
