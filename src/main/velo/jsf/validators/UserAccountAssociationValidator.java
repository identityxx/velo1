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
package velo.jsf.validators;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Validator;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import velo.ejb.seam.action.UserManageActions;
import velo.entity.Account;
import velo.entity.User;

@Name("velo.ui.UserAccountAssociationValidator")
//@Scope(Se)
//@Install(precedence = Install.BUILT_IN)
@Validator
@BypassInterceptors
public class UserAccountAssociationValidator implements javax.faces.validator.Validator,Serializable {
	private static Logger log = Logger.getLogger(UserAccountAssociationValidator.class.getName());
	private User user;
	private Account account;
	//private ValueExpression<EntityManager> entityManager;

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
		String strValue = (String)value;
		//EntityManager em = (EntityManager)Component.getInstance("entityManager");
		
		
		if (strValue.length() < 1) {
			return;
		}
		
		UserManageActions userManageActions = (UserManageActions)Component.getInstance("userManageActions");
		log.debug("Validating whether account: '" + account.getName() + "' can be associated to user '" + user.getName() + "'");
		
		// check if account is already associated with another user
		// check the DB...
		Account getAccountFromDb = userManageActions.getAccountManager().findAccount(strValue,account.getResource().getUniqueName());
		
		if (getAccountFromDb == null) {
			throw new ValidatorException(createMessage("The specified account name does not exist in repository."));
		}
		
		if ( (getAccountFromDb.getUser() != null) && (!getAccountFromDb.getUser().equals(user)) ) {
			throw new ValidatorException(createMessage("The account already associated with user '" + getAccountFromDb.getUser().getName() + "'")); 
		}
	}

	private FacesMessage createMessage(String msg) {
		return new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"" + msg, null);
	}

	
	/*
	public void setEntityManager(ValueExpression<EntityManager> entityManager)
	   {
	      this.entityManager = entityManager;
	   }
	   
	   private EntityManager getEntityManager() 
	   {
	      return entityManager == null ? 
	            null : entityManager.getValue();
	   }
	  */
}
