package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceGroupMember;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that updates the repository when a resource group member was dissociated  
 */
public class UpdateRepoWithGroupMembershipDissociation extends ReadyAction {
	private static Logger log = Logger.getLogger(UpdateRepoWithGroupMembershipDissociation.class.getName());
	
	public void validate() throws ActionValidationException {
		if (!getContext().isVarExists("memberDissociated")) {
			throw new ActionValidationException("Could not find 'memberDissociated' entity in context.");
		}
		
		//TODO: Support also resource unique name for simplicity ?
		//Expecting this to be from reconcile group membership
		if (!getContext().isVarExists("resource")) {
			throw new ActionValidationException("Could not find 'resource' entity in context.");
		}
		
		if (!getContext().isVarExists("groupFromRepo")) {
			throw new ActionValidationException("Could not find 'groupFromRepo' entity in context.");
		}
	}
	
	public void execute() throws ActionExecutionException {
		ResourceGroupMember groupMember = (ResourceGroupMember)getContext().get("memberDissociated");
		Resource resource = (Resource)getContext().get("resource");
		ResourceGroup groupFromRepo = (ResourceGroup)getContext().get("groupFromRepo");
		
		//That might not work in some circtumentences as group member can be loaded from a different resource obj
		//FIXME: Is it important at all to remove from the collection? 
		groupFromRepo.getMembers().remove(groupMember);
		getAPI().getResourceGroupManager().removeGroupMember(groupMember);
		
		log.warn("Succesfully removed member name '" + groupMember.getAccount().getName() + "' from group '" + groupFromRepo.getUniqueId() + "' in resource '" + resource.getDisplayName() + "'");
	}
}
