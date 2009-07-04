package velo.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "VL_RESOURCE_GROUP_MEMBER")
@AssociationOverrides({
	@AssociationOverride(name="primaryKey.account", joinColumns = @JoinColumn(name="ACCOUNT_ID")),
	@AssociationOverride(name="primaryKey.resourceGroup", joinColumns = @JoinColumn(name="RESOURCE_GROUP_ID"))
})
public class ResourceGroupMember {
	private static final long serialVersionUID = 1987302492306161413L;
	private ResourceGroupMemberPK primaryKey = new ResourceGroupMemberPK();
	
	public ResourceGroupMember() {
	}
	
	
	public ResourceGroupMember(Account account, ResourceGroup resourceGroup) {
		setAccount(account);
		setResourceGroup(resourceGroup);
	}

	@Id
	public ResourceGroupMemberPK getPrimaryKey() {
		return primaryKey;
	}


	public void setPrimaryKey(ResourceGroupMemberPK primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public void setAccount(Account account) {
		primaryKey.setAccount(account);
	}

	@Transient
	public Account getAccount() {
		return primaryKey.getAccount();
	}
	
	
	public void setResourceGroup(ResourceGroup resourceGroup) {
		primaryKey.setResourceGroup(resourceGroup);
	}
	
	@Transient
	public ResourceGroup getResourceGroup() {
		return primaryKey.getResourceGroup();
	}
	
	
	
	//Used by smooks for imports
	public void initMemberData(String accountName, String groupUniqueId, String groupDisplayName, String groupDescription, String groupType, Resource resource) {
		Account acc = Account.factory(accountName,resource);
		ResourceGroup group = ResourceGroup.factory(groupUniqueId, groupDisplayName, groupDescription, groupType, resource);
		
		
		setAccount(acc);
		setResourceGroup(group);
	}
}
