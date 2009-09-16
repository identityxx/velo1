package velo.actions.readyActions;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceGroupMember;
import velo.exceptions.action.ActionExecutionException;
import velo.exceptions.action.ActionValidationException;

/**
 * @author Asaf Shakarchi
 *
 * A ready action that updates the repository with a new group membership association 
 */
public class UpdateRepoWithGroupMembershipAssociation extends ReadyAction {
	private static Logger log = Logger.getLogger(UpdateRepoWithGroupMembershipAssociation.class.getName());
	
	public void validate() throws ActionValidationException {
		if (!getContext().isVarExists("accountNameAssociated")) {
			throw new ActionValidationException("Could not find 'accountNameAssociated' variable in context.");
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
		String accNameToAssoc = (String)getContext().get("accountNameAssociated");
		Resource resource = (Resource)getContext().get("resource");
		ResourceGroup groupFromRepo = (ResourceGroup)getContext().get("groupFromRepo");
		
		
		Account accToBeMember = getAPI().getAccountManager().findAccount(accNameToAssoc,resource);
		if (accToBeMember == null) {
			log.warn("Could not create a new member for group '" + groupFromRepo.getUniqueId() + "' in resource '" + resource.getDisplayName() + "' as account name '" + accNameToAssoc + "' was not found in repository");
			return;
		}
		
		
		ResourceGroupMember newMemberEntity = new ResourceGroupMember(accToBeMember,groupFromRepo);
		groupFromRepo.getMembers().add(newMemberEntity);
		getAPI().getResourceGroupManager().persistMember(newMemberEntity);
		
		log.warn("Succesfully stored new member name '" + accNameToAssoc + "' for group '" + groupFromRepo.getUniqueId() + "' in resource '" + resource.getDisplayName() + "' as account name '" + accNameToAssoc + "' was not found in repository");
	}
}
