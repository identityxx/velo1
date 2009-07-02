/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class ResourceGroupMemberPK implements Serializable {
    private Account account;
    private ResourceGroup resourceGroup;

    
    
	/**
	 * @return the Account
	 */
    @ManyToOne
    @JoinColumn(name="ACCOUNT_ID")
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	
	/**
	 * @return the resourceGroup
	 */
	@ManyToOne
	@JoinColumn(name="RESOURCE_GROUP_ID")
	public ResourceGroup getResourceGroup() {
		return resourceGroup;
	}

	/**
	 * @param resourceGroup the resourceGroup to set
	 */
	public void setResourceGroup(ResourceGroup resourceGroup) {
		this.resourceGroup = resourceGroup;
	}
}
