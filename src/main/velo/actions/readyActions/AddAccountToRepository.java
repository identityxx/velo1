package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.AccountAttribute;
import velo.entity.User;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that removes a certain account from the repository 
 */
public class AddAccountToRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(AddAccountToRepository.class.getName());
	
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
		Account account = (Account)getContext().get("account");
		
		
		if (log.isTraceEnabled()) {
			log.trace("Account to add to repository: " + account.getNameInRightCase());
			for (AccountAttribute currAtt : account.getAccountAttributes()) {
				log.trace("\tAttr of account: '" + currAtt.getAsStandardAttribute().getDisplayable());
			}
		}
		
		if (getContext().isVarExists("matchedUserEntity")) {
			User matchedUser = (User)getContext().get("matchedUserEntity");
			
			account.setUser(matchedUser);
		}
		
		getAPI().getAccountManager().persistAccountViaReconcile(account);
		
		log.debug("Succesfully stored account in repository!");
	}
}
