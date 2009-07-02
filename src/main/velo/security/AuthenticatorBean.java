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
package velo.security;

import static org.jboss.seam.ScopeType.SESSION;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import jcifs.smb.NtlmPasswordAuthentication;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import velo.common.SysConf;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.ApproversGroup;
import velo.entity.Capability;
import velo.entity.CapabilityFolder;
import velo.entity.EventLogEntry;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.EventLogEntry.EventLogLevel;
import velo.entity.EventLogEntry.EventLogModule;
import velo.exceptions.UserAuthenticationException;

@Name("authenticator")
@Stateless
public class AuthenticatorBean implements Authenticator {
	@In
	FacesMessages facesMessages;

	@Logger
	Log log;

	@In
	Identity identity;

	@In
	Credentials credentials;

	@EJB
	UserManagerLocal userManager;

	@In
	Actor actor;

	@Out(required=false, scope = SESSION)
	User loggedUser;

	@In(create=true)
	Context sessionContext;

	@In(create=true)
	public SysConf sysConfManager;

	@EJB
	public CommonUtilsManagerLocal cum;

	private boolean isAuthenticated = false;

	
	String ntlmUserName;
	String ntlmDomainName;
	
	
	//invoked via 'org.jboss.seam.security.notLoggedIn [in components.xml] event'.
	public boolean autoLogin() {
		log.debug("Silently authenticating via NTLM...");
		
		//trying auto-login
		Object autoLogin = sessionContext.get("NtlmHttpAuth");
		isAuthenticated = false;

		if ( autoLogin != null && (autoLogin instanceof NtlmPasswordAuthentication)  ) {
			log.debug("NtlmHttpAuth object in session context was successfuly set by JCIFS!");

			NtlmPasswordAuthentication ntlm = (NtlmPasswordAuthentication) autoLogin;
			ntlmUserName = ntlm.getUsername();
			ntlmDomainName = ntlm.getDomain();

			log.debug("Found username '#0' (domain: '#1') within JCIFS NTLM auth object...", ntlmUserName, ntlmDomainName);

			log.debug("Checking whether the found user name exist in repository or not...");
			//find the user in the repository
			if (!userManager.isUserExit(ntlmUserName)) {
				log.debug("User '#0' does not exist in repository!", ntlmUserName);
				return false;
			}

			log.debug("User '#0' was found in repository, authenticating user...");


			//FIXME: Make sure the user is in the trusted domain.


			//TODO: Verify that execution will never get in here if user was not authenticated via NTLM. 
			identity.setUsername( ntlmUserName );
			identity.setPassword("jibberish"); // trusting NTLM - not setting real password and even better if we don't
			isAuthenticated = true; // user is authenticated successfully via NTLM
			 
		}

		
		
		return isAuthenticated;
	}


	public boolean emptyAuth() {
		
		log.debug("Empty Auth has invoked(Risky! should be -only- used when NTLM is on!");
		return true;
	}


	public boolean authenticate() {
		log.debug("Authenticating user...");

		//NTLM
		if (sysConfManager.getGlobalConf().getBoolean("security.authentication.ntlm_silent_authentication.active")) {
			log.debug("Silence NTLM login is on, autenticating user via NTLM...");
			boolean autoLogin = autoLogin();

			log.debug("NTLM authenticatin resulted: #0",autoLogin);

			//SUCCESS NTLM AUTH
			if (autoLogin) {
				log.debug("Successfully authenticated user via NTLM, registering user in session.");

				registerUserInSession(ntlmUserName);
				credentials.setUsername(ntlmUserName);
				credentials.setPassword("NTLM_TRUST");

				return true;
				//IF NTLM failed, just throw a log message and fail.
			} else {
				log.debug("Failed to authenticate user via NTLM.");
				return false;
			}
			//return autoLogin;
		}







		//NO NTLM, continue the standard way

		//String userName = Identity.instance().getUsername();
		//String password = Identity.instance().getPassword();
		String userName = credentials.getUsername();
		String password = credentials.getPassword();


		//TODO: move out of here. IP address should be logged for NTLM too!
		HttpServletRequest rr = (HttpServletRequest) FacesContext
		.getCurrentInstance().getExternalContext().getRequest();
		String ip = rr.getRemoteAddr();

		boolean genericMessage = SysConf.getSysConf().getBoolean("security.generic-security-messages");

		//try {
		//TODO: What this boolean for? if authentciate anyway throw an exception on failure?
		boolean isAuthenticated;

		try {
			log.debug("Recieved user name '" + userName + "' from form.");
			isAuthenticated = userManager.authenticate(userName, password, ip);

			if (isAuthenticated) {
				log.debug("Successfully authenticated user '" + userName + "'");

				registerUserInSession(userName);

				//WTF?!?!? actor.getGroupActorIds().add("managers_approvers");

				return true;
			}
			else {
				log.debug("Failed to authenticated user '" + userName + "'");
				return false;
			}
		} catch (UserAuthenticationException e) {
			//In case failed to authenticate
			if (genericMessage) {
				facesMessages.add(FacesMessage.SEVERITY_WARN,"Failed to authenticate user...");
			} else {
				facesMessages.add(FacesMessage.SEVERITY_WARN, e.toString());
			}

			return false;
		}
		/*} catch (NoResultFoundException ex) {
			log.warn("Could not find user named '#0' in repository", userName);
			return false;
		}*/
	}


	//invoked in 'login.page.xml', if user is already authenticated (currently used by NTLM's 'autoLogin' method) , if so it redirects the user to the original captured view. 
	public void ssoRedirect() throws Exception {
		//doesn't work for some reason :/
		//if(identity.isLoggedIn() == true) {
		if (credentials.isSet()) {
			log.debug("SSORedirect: User #0 is already logged in, redirecting to the original captured view.", identity.getPrincipal());
			FacesMessages.instance().clear(); //clear the regular Seam messages
			// FacesMessages.instance().add(new FacesMessage("SSO login successful"));
			Redirect.instance().returnToCapturedView(); //return to the captured view
		} else {
			log.debug("SSORedirect: User is not logged in, doing nothing...");
		}
	}



	public void registerUserInSession(String userName) {
		loggedUser = userManager.findUser(userName);

		if (!loggedUser.getCapabilityFolders().isEmpty()) {
			for (CapabilityFolder currCF : loggedUser
					.getCapabilityFolders()) {
				for (Capability currCap : currCF.getCapabilities()) {
					Identity.instance().addRole(currCap.getName());
				}
			}
		}

		if (!loggedUser.getCapabilities().isEmpty()) {
			for (Capability currSpecificCap : loggedUser
					.getCapabilities()) {
				Identity.instance().addRole(
						currSpecificCap.getName());
			}
		}


		//so these details will be accessilble later on
		loggedUser.getUserIdentityAttributes().size();
		for (UserIdentityAttribute uia : loggedUser.getUserIdentityAttributes()) {
			uia.getValues().size();
		}
		loggedUser.getApproversGroups().size();





		// JBPM
		actor.setId(userName);

		log.debug("Registring user with approver groups amount #0", loggedUser.getApproversGroups().size());
		if (!loggedUser.getApproversGroups().isEmpty()) {
			for (ApproversGroup currApproverGroup : loggedUser.getApproversGroups()) {
				log.trace("Registering user with approvers group '#0'", currApproverGroup.getUniqueName());
				actor.getGroupActorIds().add(currApproverGroup.getUniqueName());
			}
		}

		actor.getGroupActorIds().add("managers_approvers");


		HttpServletRequest rr = (HttpServletRequest) FacesContext
		.getCurrentInstance().getExternalContext().getRequest();
		String ip = rr.getRemoteAddr();

		//Audit the login
		EventLogEntry eve = new EventLogEntry(EventLogModule.SECURITY,"SUCCESS_LOGIN",EventLogLevel.INFO,"User '" + loggedUser.getName() + "' has logged in.");
		eve.addData("User", loggedUser.getName());
		eve.addData("FullName", loggedUser.getFullName());
		eve.addData("IP", ip);

		cum.addEventLogEntry(eve);
	}

	/*
	@Deprecated
	public User getLoggedUser() {
		return (User)Contexts.getSessionContext().get("loggedUser");

		//return null;
	}
	 */
}
