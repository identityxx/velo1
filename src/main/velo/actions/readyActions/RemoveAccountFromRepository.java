package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that removes a certain account from the repository 
 */
public class RemoveAccountFromRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(RemoveAccountFromRepository.class.getName());
	
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
		
		log.debug("Removing account '" + accountName + "[" + resourceUniqueName + "] from repository!");
		Account account = getAPI().getAccountManager().findAccount(accountName, resourceUniqueName);
		if (account == null) {
			throw new ActionExecutionException("Could not find account name '" + accountName + "' for resource unique name '" + resourceUniqueName + "' to remove.");
		}
		
		getAPI().getAccountManager().removeAccountEntity(account);
		
		log.debug("Succesfully removed account!");
	}
}
