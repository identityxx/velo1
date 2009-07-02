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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import velo.entity.RequestAccount.RequestAccountOperation;

@Entity
@DiscriminatorValue(value = "ACCOUNTS_REQUEST")
@Deprecated
public class AccountsRequest extends Request {

    private final String REQUEST_TYPE = "Accounts Request";

    private static final long serialVersionUID = 1987302492306161413L;

    private boolean revokeRelevantRolesIfAccountIsProtected;

    private String userName;

    private Set<RequestAccount> accounts = new HashSet<RequestAccount>();

    
    /**
    @param userName the userName to delete
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
    @return the userName The user name to delete
     */
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    //removed 'nullable=false' due to hibernate+oracle prob where other requests that does not have this column cause an Oracle insertion error
    @Column(name = "REVOKE_ROLES_IF_ACCT_PROTECTED")
    public boolean isRevokeRelevantRolesIfAccountIsProtected() {
        return revokeRelevantRolesIfAccountIsProtected;
    }

    public void setRevokeRelevantRolesIfAccountIsProtected(boolean revokeRelevantRolesIfAccountIsProtected) {
        this.revokeRelevantRolesIfAccountIsProtected = revokeRelevantRolesIfAccountIsProtected;
    }

    
    
    /**
	 * @return the accounts
	 */
//DAMN    @OneToMany(mappedBy = "request", fetch = FetchType.EAGER, cascade={CascadeType.ALL})
  //DAMN    @OrderBy("accountName DESC")
    @Transient
	public Set<RequestAccount> getAccounts() {
		return accounts;
	}

	/**
	 * @param accounts the accounts to set
	 */
	public void setAccounts(Set<RequestAccount> accounts) {
		this.accounts = accounts;
	}

	
	public void addAccountToSuspend(String accountName, String resourceName, Date expectedExecutionDate) {
		RequestAccount requestAccount = RequestAccount.factory(this, RequestAccountOperation.SUSPEND, accountName, resourceName, expectedExecutionDate);
        getAccounts().add(requestAccount);
    }
	
	public void addAccountToDelete(String accountName, String resourceName, Date expectedExecutionDate) {
		RequestAccount requestAccount = RequestAccount.factory(this, RequestAccountOperation.DELETE, accountName, resourceName, expectedExecutionDate);
        getAccounts().add(requestAccount);
    }
	
	public void addAccountToResume(String accountName, String resourceName, Date expectedExecutionDate) {
		RequestAccount requestAccount = RequestAccount.factory(this, RequestAccountOperation.RESUME, accountName, resourceName, expectedExecutionDate);
        getAccounts().add(requestAccount);
    }
	
	
	//transients for easy access accounts per operation
	@Transient
	public Set<RequestAccount> getAccountsToSuspend() {
		HashSet<RequestAccount> accounts = new HashSet<RequestAccount>();
		
		for (RequestAccount currAccount : getAccounts()) {
			if (currAccount.getAccountOperation() == RequestAccountOperation.SUSPEND) {
				accounts.add(currAccount);
			}
		}
		return accounts;
	}
	
	@Transient
	public Set<RequestAccount> getAccountsToResume() {
		HashSet<RequestAccount> accounts = new HashSet<RequestAccount>();
		
		for (RequestAccount currAccount : getAccounts()) {
			if (currAccount.getAccountOperation() == RequestAccountOperation.RESUME) {
				accounts.add(currAccount);
			}
		}
		return accounts;
	}
	
	@Transient
	public Set<RequestAccount> getAccountsToDelete() {
		HashSet<RequestAccount> accounts = new HashSet<RequestAccount>();
		
		for (RequestAccount currAccount : getAccounts()) {
			if (currAccount.getAccountOperation() == RequestAccountOperation.DELETE) {
				accounts.add(currAccount);
			}
		}
		return accounts;
	}
	
	
	
	@Transient
    public String getType() {
        return REQUEST_TYPE;
    }
}