package velo.collections;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceGroupMember;
import velo.exceptions.LoadGroupByMapException;

public class ResourceGroups extends HashSet<ResourceGroup> implements Cloneable, Serializable {
	private String resourceUniqueName;
	private String fetchType;
	private Date creationDate;
	
	
	public void addGroup(String uniqueId, String displayName, String description, Resource resource) {
		ResourceGroup rg = ResourceGroup.factory(uniqueId, displayName, description, null, resource);
		add(rg);
	}

	
	public void addGroup(Map map, Resource resource) throws LoadGroupByMapException {
		ResourceGroup rg = ResourceGroup.factory(null, null, null,null,resource);
		rg.load(map, resource);
		add(rg);
	}
	
	
	//used for reconcile group membership to import group membership, mainly used by the controllers
	public void addGroupMembership(Map map, Resource resource) throws LoadGroupByMapException {
		//Expecting at least to have the group_unique_id and account_name available here
		
		String groupUniqueIdFieldName = "group_unique_id";
		String groupTypeFieldName = "group_type";
		String accountNameFieldName = "account_name";
			
			
		if (!map.containsKey(groupUniqueIdFieldName)) {
			throw new LoadGroupByMapException("Could not find '" + groupUniqueIdFieldName + "' for group to be loaded for the following map entry: ' " + map + "'");
		}
		
		if (!map.containsKey(accountNameFieldName)) {
			throw new LoadGroupByMapException("Could not find '" + accountNameFieldName + "' for group to be loaded for the following map entry: ' " + map + "'");
		}
		
		if (!map.containsKey(groupTypeFieldName)) {
			throw new LoadGroupByMapException("Could not find '" + groupTypeFieldName + "' for group member be loaded for the following map entry: ' " + map + "'");
		}
		
		/*Has to be taken care in reconcile to simplify the loading data from resource/files
		ResourceGroup rg = resource.findGroup((String)map.get("group_unique_id"));
		if (rg == null) {
			//resource group does not exist, skipping...
			return;
		}
		*/
		
		
		Account acc = Account.factory((String)map.get(accountNameFieldName), resource);
		ResourceGroup group = getByUniqueId((String)(map.get(groupUniqueIdFieldName)));
		
		//If group already in the list, just add a new member, otherwise create a new group
		if (group != null) {
			if (!group.isMemberExistByName(acc)) {
				group.getMembers().add(new ResourceGroupMember(acc,group));
			}
		} else {
			group = ResourceGroup.factory((String)(map.get(groupUniqueIdFieldName)), null, null,(String)(map.get(groupTypeFieldName)),resource);
			group.getMembers().add(new ResourceGroupMember(acc,group));
			
			add(group);
		}
		
		
	}
	
	
	
	
	
	
	public ResourceGroup getByUniqueId(String uniqueId) {
		for (ResourceGroup currRG : this) {
			if (currRG.getUniqueIdInRightCase().equals(uniqueId)) {
				return currRG;
			}
		}
		
		return null;
	}

	public String getResourceUniqueName() {
		return resourceUniqueName;
	}

	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}

	public String getFetchType() {
		return fetchType;
	}

	public void setFetchType(String fetchType) {
		this.fetchType = fetchType;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
