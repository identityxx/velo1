package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.User;
import velo.entity.User.UserSourceTypes;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that removes a certain account from the repository 
 */
public class AddAccountAndUserToRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(AddAccountAndUserToRepository.class.getName());
	
	public void validate() throws ActionValidationException {
		log.debug("Validating required params in context (resourceUniqueName, accontName");
		//verify that in context we have the account name and resource name
		if (!getContext().isVarExists("resourceUniqueName")) {
			throw new ActionValidationException("Could not find 'resourceUniqueName' variable in context.");
		}
		
		if (!getContext().isVarExists("accountName")) {
			throw new ActionValidationException("Could not find 'accountName' variable in context.");
		}
		log.debug("Successfully validated required params!.");
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
		
		User matchedUser = null;
		matchedUserName = null;
		if (getContext().isVarExists("matchedUser")) {
			if (getContext().get("matchedUser") instanceof String) {
				matchedUserName = (String)getContext().get("matchedUser");
			}
		}
		
		if (matchedUserName != null) {
			matchedUser = getAPI().getUserManager().findUser(matchedUserName);
			
			if (matchedUser == null) {
				//matchedUser = new User(matchedUserName,false,false,UserSourceTypes.RECONCILE);
				throw new ActionExecutionException("Could not add account '" + accountName + "'(" + resourceUniqueName + ")' to repository, matched user was set to '" + matchedUser + "' but was not fond in repository1");
			}
		}
		
		//HIGHER PRIORITY OF ACCOUNT OBJECT INSTEAD OF ACCOUNT NAME AS IN RECONCILE THIS ACTION READY recieves
		//an account object with attributes.
		if ( (getContext().isVarExists("account")) && getContext().get("account") instanceof Account) {
			Account acc = (Account)getContext().get("account");
			if (matchedUser != null) {
				acc.setUser(matchedUser);
			}
			
			getAPI().getAccountManager().persistAccount(acc);
		} else {
			//Account.factory(accountName, resource);
			getAPI().getAccountManager().persistAccount(accountName, resourceUniqueName,matchedUserName);
		}
		
		
		log.debug("Succesfully stored account in repository!");
	}
}
