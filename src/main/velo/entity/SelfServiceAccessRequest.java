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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;
//@!@clean
@Entity
@DiscriminatorValue(value = "SS_REQUEST_ACCESS_REQUEST")
@Name("SSAccessRequest")
public class SelfServiceAccessRequest extends Request {

    private final String REQUEST_TYPE = "Self Service Access Request";

    private static final long serialVersionUID = 1987302492306161413L;

    private User requestedAccessForUser;
    private User userThatHasMyAccess;
    private Resource resource;
    private Role role;
    private String description;
    private Date grantAccessDate;
    private boolean grantAccessImmediately;
    private Date expirationAccessDate;
    private String reason;
    
    
    //will be used to calculate the expirationAccessDate
    private int accessExpirationDateAmount;
    private String accessExpirationDateAmountType;
    
    
    //This is just a workaround until a nice calendar with time support will be available for gui
    private String grantAccessHour;
    private String grantAccessMinutes;
    




	/**
	 * @return the requestedAccessForUser
	 */
    @JoinColumn(name="REQUESTED_ACCESS_FOR_USER_ID", nullable=true, unique=false)
    @ManyToOne
	public User getRequestedAccessForUser() {
		return requestedAccessForUser;
	}


	/**
	 * @param requestedAccessForUser the requestedAccessForUser to set
	 */
	public void setRequestedAccessForUser(User requestedAccessForUser) {
		this.requestedAccessForUser = requestedAccessForUser;
	}


	/**
	 * @return the userThatHasMyAccess
	 */
	@JoinColumn(name="USER_THAT_HAS_MY_ACCESS_ID", nullable=true, unique=false)
	@ManyToOne
	public User getUserThatHasMyAccess() {
		return userThatHasMyAccess;
	}


	/**
	 * @param userThatHasMyAccess the userThatHasMyAccess to set
	 */
	public void setUserThatHasMyAccess(User userThatHasMyAccess) {
		this.userThatHasMyAccess = userThatHasMyAccess;
	}


	/**
	 * @return the resource
	 */
	@JoinColumn(name="RESOURCE_ID", nullable=true, unique=false)
	@ManyToOne
	public Resource getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	
	/**
	 * @return the role
	 */
	@JoinColumn(name="ROLE_ID", nullable=true, unique=false)
	@ManyToOne
	public Role getRole() {
		return role;
	}


	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return the description
	 */
	@Column(name="DESCRIPTION")
	public String getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * @return the grantAccessDate
	 */
	@Column(name="GRANT_ACCESS_DATE", nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
	public Date getGrantAccessDate() {
		return grantAccessDate;
	}


	/**
	 * @param grantAccessDate the grantAccessDate to set
	 */
	public void setGrantAccessDate(Date grantAccessDate) {
		this.grantAccessDate = grantAccessDate;
	}


	/**
	 * @return the expirationAccessDate
	 */
	@Column(name="EXPIRATION_ACCESS_DATE", nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
	public Date getExpirationAccessDate() {
		return expirationAccessDate;
	}


	/**
	 * @param expirationAccessDate the expirationAccessDate to set
	 */
	public void setExpirationAccessDate(Date expirationAccessDate) {
		this.expirationAccessDate = expirationAccessDate;
	}

	/**
	 * @return the reason
	 */
	@Column(name="REASON")
	public String getReason() {
		return reason;
	}


	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	/**
	 * @return the grantAccessImmediately
	 */
	@Column(name="GRANT_ACCESS_IMMEDIATELY")
	public boolean isGrantAccessImmediately() {
		return grantAccessImmediately;
	}


	/**
	 * @param grantAccessImmediately the grantAccessImmediately to set
	 */
	public void setGrantAccessImmediately(boolean grantAccessImmediately) {
		this.grantAccessImmediately = grantAccessImmediately;
	}
	
	
	
	
	
	

	@Transient
    public String getType() {
        return REQUEST_TYPE;
    }
	
	
	//used by 'this.getPoolActors' / 'this.getEmailsOfApprovers'
	public Set<ApproversGroup> getApproversGroups(Integer level) {
		Set<ApproversGroup> ags = new HashSet<ApproversGroup>();
		
		if (level != 0) {
			if (getRole() != null) {
				for (RoleApproversGroup currRAG : getRole().getRoleApproversGroups()) {
					if (currRAG.getApprovalLevel().equals(level)) {
						ags.add(currRAG.getApproversGroup());
					}
				}
			} else {
				System.out.println("Role object is null, there are no approvers groups in this level!");
			}
		} else {
			System.out.println("Level is 0!");
		}
		
		
		return ags;
	}
	
	
	public Set<User> getApprovers(Integer level) {
		Set<User> approvers = new HashSet<User>();
		
		for (ApproversGroup currAG : getApproversGroups(level)) {
			for (User currApprover : currAG.getApprovers()) {
				approvers.add(currApprover);
			}
		}
		
		return approvers;
	}
	
	
	
	//think of a better way to handle approvers groups
	public String[] getPoolActors(Integer level) {
		System.out.println("Returning pool actors of level: " + level);
		
		List<String> ragsList = new ArrayList<String>();
		
		for (ApproversGroup currAG : getApproversGroups(level)) {
			ragsList.add(currAG.getUniqueName());
		}
		 
		String[] rags = new String[ragsList.size()];
		
		int i=0;
		for (String currRagName : ragsList) {
			rags[i] = currRagName;
		}
		
		System.out.println("Approvers Groups for next level: " + rags);
		
		return rags;
	}


	public boolean isThereApproversInLevel(Integer level) {
		if (level != 0) {
			if (getRole() != null) {
				for (RoleApproversGroup currRAG : getRole().getRoleApproversGroups()) {
					if (currRAG.getApprovalLevel().equals(level)) {
						return true;
					}
				}
				
				return false;
			} else {
				System.out.println("Role object is null, there are no approvers groups in this level!");
				return false;
			}
		} else {
			System.out.println("Level is 0!");
			//TODO: What to do here?
			return false;
		}
	}
	
	
	//used to send email notifications
	public Set<String> getEmailsOfApprovers(Integer level) {
		Set<String> emailAddresses = new HashSet<String>();
		
		Set<ApproversGroup> groups = getApproversGroups(level);
		
		for (ApproversGroup currAG : groups) {
			for (User currApprover : currAG.getApprovers()) {
				String currEmail = currApprover.getEmail();
				if (currEmail != null) {
					emailAddresses.add(currApprover.getEmail());
				} else {
					System.out.println("Could get email address of approver '" + currApprover.getName() + "', approver has no email address.");
				}
			}
		}
		
		
		return emailAddresses;
	}
	
	
	
	
	
	
	
	
	//TRANSIENT
	/**
	 * @return the grantAccessHour
	 */
	@Transient
	public String getGrantAccessHour() {
		return grantAccessHour;
	}


	/**
	 * @param grantAccessHour the grantAccessHour to set
	 */
	public void setGrantAccessHour(String grantAccessHour) {
		this.grantAccessHour = grantAccessHour;
	}
	
	/**
	 * @return the grantAccessMinutes
	 */
	@Transient
	public String getGrantAccessMinutes() {
		return grantAccessMinutes;
	}


	/**
	 * @param grantAccessMinutes the grantAccessMinutes to set
	 */
	public void setGrantAccessMinutes(String grantAccessMinutes) {
		this.grantAccessMinutes = grantAccessMinutes;
	}
	
	
	
	
	
	
	
	


	/**
	 * @return the accessExpirationDateAmount
	 */
	@Column(name="ACCESS_EXP_DATE_AMOUNT")
	public int getAccessExpirationDateAmount() {
		return accessExpirationDateAmount;
	}


	/**
	 * @param accessExpirationDateAmount the accessExpirationDateAmount to set
	 */
	public void setAccessExpirationDateAmount(int accessExpirationDateAmount) {
		this.accessExpirationDateAmount = accessExpirationDateAmount;
	}


	/**
	 * @return the accessExpirationDateAmountType
	 */
	@Column(name="ACCESS_EXP_DATE_AMOUNT_TYPE")
	public String getAccessExpirationDateAmountType() {
		return accessExpirationDateAmountType;
	}


	/**
	 * @param accessExpirationDateAmountType the accessExpirationDateAmountType to set
	 */
	public void setAccessExpirationDateAmountType(
			String accessExpirationDateAmountType) {
		this.accessExpirationDateAmountType = accessExpirationDateAmountType;
	}
	

}