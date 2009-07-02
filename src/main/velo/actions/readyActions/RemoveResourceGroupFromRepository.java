package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.ResourceGroup;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that removes a certain resource group from the repository 
 */
public class RemoveResourceGroupFromRepository extends ReadyAction {
	private static Logger log = Logger.getLogger(RemoveResourceGroupFromRepository.class.getName());
	
	public void validate() throws ActionValidationException {
		if (!getContext().isVarExists("group")) {
			throw new ActionValidationException("Could not find 'group' entity in context.");
		}
	}
	
	public void execute() throws ActionExecutionException {
		ResourceGroup group = (ResourceGroup)getContext().get("group");
		getAPI().getResourceGroupManager().removeGroup(group);
		
		log.debug("Succesfully invoked removeGroup method!");
	}
}
