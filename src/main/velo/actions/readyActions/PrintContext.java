package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.exceptions.action.ActionExecutionException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that removes a certain account from the repository 
 */
public class PrintContext extends ReadyAction {
	private static Logger log = Logger.getLogger(PrintContext.class.getName());
	
	public void execute() throws ActionExecutionException {
		log.debug("Printing context: " + getContext());
	}
}
