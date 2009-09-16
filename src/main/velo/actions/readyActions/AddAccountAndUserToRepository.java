package velo.actions.readyActions;

import org.apache.log4j.Logger;
import org.jgroups.SetStateEvent;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.User;
import velo.entity.User.UserSourceTypes;
import velo.exceptions.PersistEntityException;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that adds a new account & matched user to the repository
 * 
 *  TODO: This class is very expensive, too many invocations, for this action being involved for reconcile 
 *  //when executed more than 1000-2000 times.
 */
public class AddAccountAndUserToRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(AddAccountAndUserToRepository.class.getName());
	private UserManagerLocal userManager = null;
	private AccountManagerLocal accountManager = null;
	
	public void validate() throws ActionValidationException {
		/*
		log.debug("Validating required params in context (resourceUniqueName, accontName");
		//verify that in context we have the account name and resource name
		if (!getContext().isVarExists("resourceUniqueName")) {
			throw new ActionValidationException("Could not find 'resourceUniqueName' variable in context.");
		}
		
		if (!getContext().isVarExists("accountName")) {
			throw new ActionValidationException("Could not find 'accountName' variable in context.");
		}
		log.debug("Successfully validated required params!.");
		*/
		
		if (!getContext().isVarExists("account")) {
			throw new ActionValidationException("Could not find 'account' entity in context.");
		}
		
		//its important to fetch managers once, as an object of this class will be available for the whole reconciliation execution
		//thus saving a lot of factoring per reconcile iteration.
		
		if (userManager == null) {
			userManager = getAPI().getUserManager();
		}
		
		if (accountManager == null) {
			accountManager = getAPI().getAccountManager();
		}
	}
	
	public void execute() throws ActionExecutionException {
		String resourceUniqueName = (String)getContext().get("resourceUniqueName");
		String accountName = (String)getContext().get("accountName");
		String matchedUserName = (String)getContext().get("matchedUserName");
		
		
		log.debug("Adding account '" + accountName + "[" + resourceUniqueName + "] to repository!");
		Resource resource = null;
		
		//Reconcile provides us in context a resource object.
		if (getContext().isVarExists("resource")) {
			resource = (Resource)getContext().get("resource");
		}
		
		if (resource == null) {
			getAPI().getResourceManager().findResource(resourceUniqueName);
		}
		if (resource == null) {
			throw new ActionExecutionException("Could not find resource with unique name '" + accountName + "' for resource unique name '" + resourceUniqueName + "'");
		}
		
		
		if (getContext().isVarExists("matchedUser")) {
			//Skip user creation as there's a matched user? this should never happen here
			throw new ActionExecutionException("Could not add account&user to the repository, matched user was found in context! expected no matched user!");
		}
		
		//HIGHER PRIORITY OF ACCOUNT OBJECT INSTEAD OF ACCOUNT NAME AS IN RECONCILE THIS ACTION READY recieves
		//an account object with attributes.
		if ( (getContext().isVarExists("account")) && getContext().get("account") instanceof Account) {
			Account acc = (Account)getContext().get("account");

			//Query per iteration is very expensive, but no other option :/
			//User matchedUser = getAPI().getUserManager().findUser(acc.getName());
			Boolean matchedUser = getAPI().getUserManager().isUserExit(acc.getName());

			if (!matchedUser) {
				User user = userManager.factoryUser(acc.getName());
				user.setSourceType(UserSourceTypes.RECONCILE);
				
				try {
					userManager.persistUserEntity(user);
					
					acc.setUser(user);
				} catch (PersistEntityException e) {
					throw new ActionExecutionException("Could not persist user due to: " + e.getMessage());
				}
			} else {
				
			}
			
			accountManager.persistAccountViaReconcile(acc);
		//add by 
		} else {
			//Account.factory(accountName, resource);
			accountManager.persistAccount(accountName, resourceUniqueName,matchedUserName);
		}
		
		
		log.debug("Succesfully stored account in repository!");
	}
}