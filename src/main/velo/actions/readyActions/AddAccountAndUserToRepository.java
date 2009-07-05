package velo.actions.readyActions;

import org.apache.log4j.Logger;

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
 */
public class AddAccountAndUserToRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(AddAccountAndUserToRepository.class.getName());
	
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
	}
	
	public void execute() throws ActionExecutionException {
		String resourceUniqueName = (String)getContext().get("resourceUniqueName");
		String accountName = (String)getContext().get("accountName");
		String matchedUserName = (String)getContext().get("matchedUserName");
		
		
		log.debug("Adding account '" + accountName + "[" + resourceUniqueName + "] to repository!");
		Resource resource = getAPI().getResourceManager().findResource(resourceUniqueName);
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

			User matchedUser = getAPI().getUserManager().findUser(acc.getName());

			if (matchedUser == null) {
				User user = User.factory(acc.getName(), false, false,UserSourceTypes.RECONCILE);
				try {
					getAPI().getUserManager().persistUserEntity(user);
					
					acc.setUser(user);
				} catch (PersistEntityException e) {
					throw new ActionExecutionException("Could not persist user due to: " + e.getMessage());
				}
			}
			
			getAPI().getAccountManager().persistAccount(acc);
		//add by 
		} else {
			//Account.factory(accountName, resource);
			getAPI().getAccountManager().persistAccount(accountName, resourceUniqueName,matchedUserName);
		}
		
		
		log.debug("Succesfully stored account in repository!");
	}
}