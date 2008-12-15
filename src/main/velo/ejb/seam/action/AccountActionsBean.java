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
package velo.ejb.seam.action;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.ResourceOperationsManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.User;
import velo.exceptions.OperationException;

@Stateful
@Name("accountActions")
public class AccountActionsBean implements AccountActions {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;

	@PersistenceContext
    public EntityManager em;
    
	//Inject the user, as it should be already in the conversation context set by userHome
	@In(value="#{userHome.instance}")
	User user;
	
	@EJB
    public UserManagerLocal userManager;
	
	@EJB
	public AccountManagerLocal accountManager;
	
	@EJB
	public ResourceOperationsManagerLocal resourceOperationsManager;

	//WARNING: never access directly, only use the accessor!
	@In(value="#{accountList.accountSelection}")
	Map<Account, Boolean> accountSelection;

	
	public void disableAccounts() {
		Set<Account> accounts = getAccountSelection();
		if (accounts.size() < 1) {
			facesMessages.add("No accounts selected to disable.");
		}
		
		log.info("Requesting a task to disable selected accounts amount #0",accounts.size());
	
		if (accounts.size() > 0) {
			try {
				//accountManager.disableAccounts(accounts,null,null);
				for (Account currAcc : accounts) {
					resourceOperationsManager.createSuspendAccountRequest(currAcc);
				}
				facesMessages.add("Successfully requested disable operation on the selected accounts");
			}
			catch (OperationException ex) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Could not perform disable operation on the selected accounts: " + ex.toString());
			}
		}
	}
	
	
	public void enableAccounts() {
		Set<Account> accounts = getAccountSelection();
		if (accounts.size() < 1) {
			facesMessages.add("No accounts selected to enable.");
		}
		
		log.info("Requesting a task to enable selected accounts amount #0",accounts.size());
	
		if (accounts.size() > 0) {
			try {
				for (Account currAcc : accounts) {
					resourceOperationsManager.createResumeAccountRequest(currAcc);
				}
				facesMessages.add("Successfully requested enable operation on the selected accounts");
			}
			catch (OperationException ex) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Could not perform enable operation on the selected accounts: " + ex.toString());
			}
		}
	}
	
	public void deleteAccounts() {
		Set<Account> accounts = getAccountSelection();
		if (accounts.size() < 1) {
			facesMessages.add("No accounts selected to delete.");
		}
		
		log.info("Requesting a task to delete selected accounts amount #0",accounts.size());
	
		if (accounts.size() > 0) {
			try {
				for (Account currAcc : accounts) {
					resourceOperationsManager.createDeleteAccountRequest(currAcc,null,false);
				}
				facesMessages.add("Successfully requested delete operation on the selected accounts");
			}
			catch (OperationException ex) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Could not perform delete operation on the selected accounts: " + ex.toString());
			}
		}
	}
	
	public void deleteAccountsInRepository() {
		Set<Account> accounts = getAccountSelection();
		if (accounts.size() < 1) {
			facesMessages.add("No accounts selected to delete.");
		}
		
		log.info("Requesting a task to delete selected accounts amount #0",accounts.size());
	
		if (accounts.size() > 0) {
			for (Account currAccount : accounts) {
				user.getAccounts().remove(currAccount);
				accountManager.removeAccountEntity(currAccount);
			}

			facesMessages.add("Successfully deleted selected accounts in repository!");
		}
	}
	
	
	//Accessors
	public Set<Account> getAccountSelection() {
		Set<Account> accounts = new HashSet<Account>();
		
		//should never happen, handled by seam's @In annotation
		if (accountSelection != null) {
			log.debug("Printing Map With Account Selection #0", accountSelection);
			for (Map.Entry<Account, Boolean> accountEntry : accountSelection.entrySet()) {
				if (accountEntry.getValue()) {
					accounts.add(accountEntry.getKey());
				}
			}
		}
		
		return accounts;
	}
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
