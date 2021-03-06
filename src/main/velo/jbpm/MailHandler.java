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
package velo.jbpm;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.jpdl.el.VariableResolver;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.jpdl.el.impl.JbpmVariableResolver;
import org.jbpm.taskmgmt.exe.TaskInstance;

import velo.ejb.interfaces.ApproversGroupManagerLocal;
import velo.ejb.interfaces.EmailManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.ApproversGroup;
import velo.entity.EmailTemplate;
import velo.entity.User;
import velo.exceptions.ExpressionCreationException;
import velo.tools.EdmEmailSender;
import velo.validators.Generic;

public class MailHandler implements ActionHandler  {
	private static Logger log = Logger.getLogger(MailHandler.class.getName());
	String template = null;
	String actors = null;
	String to = null;
	String bcc = null;
	String bccActors = null;
	String subject = null;
	String text = null;
	String addresses;
	ExecutionContext executionContext = null;
	
	
	public MailHandler() {
	}
	
	public MailHandler(String template,String actors,String to,String subject,String text) {
		//System.out.println("!!!!!!!!!!!!!!!!!(2): " + actors);
		this.template = template;
		this.actors = actors;
		this.to = to;
		this.subject = subject;
		this.text = text;
	}
	
	public MailHandler(String template,String actors,String to,String bccActors,String bcc,String subject,String text) {
		//System.out.println("!!!!!!!!!!!!!!!!!(1): " + actors);
		this.template = template;
		this.actors = actors;
		this.to = to;
		this.bccActors = bccActors;
		this.bcc = bcc;
		this.subject = subject;
		this.text = text;
	}  
	
	public void execute(ExecutionContext executionContext) {
	    this.executionContext = executionContext;
	    
	    if (log.isTraceEnabled()) {
	    	log.trace("Printing variables of JBPM MailHandler");
	    	log.trace("Template: " + template);
	    	log.trace("Actors: " + actors);
	    	log.trace("To: " + to);
	    	log.trace("Subject: " +subject);
	    	log.trace("Process ID: " + executionContext.getProcessInstance().getId());
	    	log.trace("Process Vars: " + executionContext.getContextInstance().getVariables());
	    }
	    
	    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!LALALALAOLALALAL OBJECT:" + executionContext.getNode().hashCode());
	    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!TOKENENENENE OBJECT:" + executionContext.getToken().getId());
	    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!MAILHANDLER OBJECT:" + this);
	    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!EXE CNTX OBJ:" + executionContext);
	    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!PROCESS INSTANCE OBJ:" + executionContext.getProcessInstance());
	    
	    
	    
	    //cleanups! - seems like when creating processes within a loop one by another, the same mailhandler instance is used.
	    //which resulted same class properties to be used, for saftey, lets clean up all class variables.
	    cleanups();
	    
	    send();
	}

	public void send() {
		boolean isApproversGroup = false;
		String resultedActor;
		
		
		try {
		log.debug("MailNode of a business process has just invoked.");
		
		log.debug("Sending email to the specified template: " + template);

		if (actors == null) {
			log.error("Actors was set to null, aboring sending mail process.");
			return;
		} else {
			log.debug("The specified actors is: " + actors);
		}
		
		//HANDLE APPROVERS GROUP SUROUNDED WITH []
		if ( (actors.startsWith("[")) && (actors.endsWith("]")) ) {
			log.debug("Found a string surounded with '[]' meaning the actor is an approvers group, determining whether it is an expression or not.");
			String agStr = actors.substring(1, actors.length()-1);
			isApproversGroup = true;
			if (isExpression(agStr)) {
				log.debug("ApproversGroup is an expression, evaluating expression now.");
				String result = evaluate(agStr);
				log.debug("Expression result is: '" + result + "' (overriding actors with the result)");
				
				
				/////actors = result;
				resultedActor = result;
			} else {
				log.debug("Approvers group is not expression and would be: '" + agStr + "'");
				/////actors = agStr;
				resultedActor = agStr;
			}
		//INDIVIDUAL
		} else {
			
			//if an expression of an individual user
			if ( (actors.startsWith("#{")) && (actors.endsWith("}")) ){
				log.debug("The specified actor is an expression (single user), evaluating expression now...");
				String result = evaluate(actors);
				
				log.debug("Expression evaluation result is '" + result + "', overriding 'actors' with result.");
				//////actors = result;
				resultedActor = result;
				
			//not an expression but single user
			} else {
				log.debug("The specified actor is NOT an expression(and is not an approvers group), evaluating as a context var...");
				String result = (String)executionContext.getVariable(actors);
				log.debug("Context var result for actors '" + actors + "' is: '" + result + "', overriding 'actors' with result.");
				//get the real value as the specified value is the var name
				//////actors = result;
				resultedActor = result;
			}
		}
		
		
		
		
		

		
		
		
		Context initialContext = new InitialContext();
		EmailManagerLocal emailManager = (EmailManagerLocal) initialContext.lookup("velo/EmailBean/local");
		EmailTemplate et = emailManager.findEmailTemplate(template);
		
		if (et == null) {
			log.error("Could not send mail, could not find email template named: '" + template + "' in repository!");
			return;
		}
		
		/*
		//copied from executionContext.getVariable code:)
		if (executionContext.getTaskInstance()!=null) {
			et.addContentVar("processVars", executionContext.getTaskInstance().getVariableInstances());
		} else {
			et.addContentVar("processVars", executionContext.getContextInstance().getVariables());
		}
		*/

		//adding process vars map to the template
		et.addContentVar("processVars", executionContext.getContextInstance().getVariables());
		//add the process to the context
		et.addContentVar("process", executionContext.getProcessInstance());
		
		JbpmCommentsUtils jbpmCommentsUtils = new JbpmCommentsUtils();
		
		et.addContentVar("processCommentsHtml", jbpmCommentsUtils.getProcessCommentsAsHtml(executionContext.getProcessInstance()));
		//the current node
		et.addContentVar("node", executionContext.getNode());
		et.addContentVar("currentTime",new Date());
		
		/*
		Transition t = executionContext.getNode().getDefaultLeavingTransition();
		Node nextNode = t.getTo();
		System.out.println("!!!!!!!!!!!!!!!!!" + nextNode);
		
		if (nextNode != null) {
			System.out.println("!!!!!!!!!!!!!!!!!" + nextNode.getName());
			System.out.println("!!!!!!!!!!!!!!!!!" + nextNode.getDescription());
		}
		
		TaskNode taskNode = (TaskNode)nextNode;
		
		Task ta = (Task)taskNode.getTasksMap().get("bla");
		ta.getDescription();
		*/

		EdmEmailSender sender = new EdmEmailSender();
		
		/*currently 'TO' is not supported
		if (to != null) {
			addEmailAddress(to);
		}
		*/
		
		
		
		
		
		//if AG, then handle it, otherwise handle an individual approver
		if (isApproversGroup) {
			ApproversGroupManagerLocal approversGroupManager = (ApproversGroupManagerLocal) initialContext.lookup("velo/ApproversGroupBean/local");
			//Not sure why, even though it's a local interface the session get closed and a lazy loading exception occurs
			//thus, loading entity eagerly.
			ApproversGroup ag = approversGroupManager.findApproversGroupEagerly(resultedActor);
		
		
			//make sure AG entity was found and there are associated approvers, otherwise abort.
			if (ag == null) {
				log.error("Could not find any approvers group for unique name: '" + resultedActor + "', skipping sending mail process");
				return;
			} else {
				if (ag.getApprovers().size() > 0) {
					log.debug("Found ApproversGroup with uniqueName '" + ag.getUniqueName() + "', with associated approvers number '" + ag.getApprovers().size() + "'");
				} else {
					log.info("Found ApproversGroup with uniqueName '" + ag.getUniqueName() + "', but with 0 associated approvers, skipping sending mail process...");
					return;
				}
			}
			
			for (User currApprover : ag.getApprovers()) {
				String email = currApprover.getEmail();
				//make sure email is not null
				if (email == null) {
					log.info("User '" + currApprover.getName() + "' has no email address, skipping sending mail to user.");
					continue;
				}
				
				if (Generic.isEmailValid(email)) {
					log.trace("Current iterated Approver with username '" + currApprover.getName() + "', has a valid email address and will recieve an email to address:  '" + email + "'");
					addEmailAddress(email);
				} else {
					log.warn("Current iterated Approver with username '" + currApprover.getName() + "', has an INVALID email address, skipping sending mail to this approver.");
				}
			}
		//handle an individual	
		} else {
			log.debug("Recipient is an individual, loading approver(user) entity from repository with name: '" + resultedActor + "'");
			UserManagerLocal userManager = (UserManagerLocal) initialContext.lookup("velo/UserBean/local");
		
			User user = userManager.findUser(resultedActor);
		
			if (user == null) {
				log.warn("Could not find actor(user) in repository for name: " + resultedActor + ", aboring mail.");
				return;
			} else {
				String emailAddr = user.getEmail();
				if (emailAddr != null) {
					if (Generic.isEmailValid(emailAddr)) {
						addEmailAddress(emailAddr);
					} else {
						log.warn("User '" + resultedActor + "' was found in repository but has an INVALID email address: '" + emailAddr + "'");
					}
				} else {
					log.warn("Could not find email address of user: " + user.getName());
				}
			}
		}
				
		
		
		
		if (addresses != null) {
			log.debug("Sending mail to addresses: " + addresses + ", subject: " + et.getSubject());
			sender.addHtmlEmail(addresses, et.getSubject(), et.getParsedContent());
			log.debug("Sending...");
			sender.send();
			log.debug("Successfully sent email...!");
		} else {
			log.warn("Skipping sending email since no email address was resulted(from to/actors)");
			return;
		}
		
		
		} catch (NamingException e) {
			log.error("Naming Exception error due to " + e.getMessage());
		}catch (EmailException e) {
			log.error("Error sending email due to: " + e.getMessage());
		}catch (ExpressionCreationException e) {
			log.error("Error sending email due to: " + e.getMessage());
		}
		
	}
	
	
	public void addEmailAddress(String address) {
		if (addresses != null) {
			addresses = addresses + ";" + address;
		} else {
			addresses = address;
		}
	}
	
	
	
	
	public void cleanups() {
		addresses = null;
	}
	
	
	
	
	
	String evaluate(String expression) {
	    if (expression==null) {
	      return null;
	    }
	    VariableResolver variableResolver = JbpmExpressionEvaluator.getUsedVariableResolver();
	    if (variableResolver!=null) {
	      variableResolver = new JbpmVariableResolver();
	    }
	    return (String) JbpmExpressionEvaluator.evaluate(expression, executionContext, variableResolver, null);
	  }
	
	
	
	private boolean isExpression(String str) {
		return ( (str.startsWith("#{")) && (str.endsWith("}")) );
	}

}
