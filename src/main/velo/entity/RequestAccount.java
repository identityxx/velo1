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
//@!@clean
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An entity that represents a Requested Account
 *
 * @author Asaf Shakarchi
 */
@Entity
@Table(name = "VL_REQUEST_ACCOUNT")
@SequenceGenerator(name="RequestAccountIdSeq",sequenceName="REQUEST_ACCOUNT_ID_SEQ")
@Deprecated
public class RequestAccount extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1987302492306161413L;

    public enum RequestAccountOperation {
		SUSPEND, RESUME, DELETE
	}
    
    private Long requestedAccountId;
    private Request request;
    private String accountName;
    private String resourceName;
    private Date expectedExecutionDate = new Date();
    //private boolean calculateExpectedExecutionDateFromRequestApprovalTime;
    private RequestAccountOperation accountOperation;
    

	/**
     * @param requestedAccountId The requestedAccountId to set.
     */
    public void setRequestedAccountId(Long requestedAccountId) {
        this.requestedAccountId = requestedAccountId;
    }

    /**
     * @return Returns the taskLogId.
     */
    //GF@Id
    //GF@SequenceGenerator(name = "IDM_REQUESTED_ACCOUNT_GEN", sequenceName = "IDM_REQUESTED_ACCOUNT_SEQ", allocationSize = 1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IDM_REQUESTED_ACCOUNT_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestAccountIdSeq")
    //@GeneratedValue //JB
    @Column(name = "REQUESTED_ACCOUNT_ID")
    public Long getRequestedAccountId() {
        return requestedAccountId;
    }


    @ManyToOne(optional=false)
    @JoinColumn(name="REQUEST_ID", nullable=false)
    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
    
    
    /**
     * @param accountName the accountName to set
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * @return the accountName
     */
    @Column(name = "ACCOUNT_NAME")
    public String getAccountName() {
        return accountName;
    }

    @Column(name = "RESOURCE_NAME")
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    @Temporal(TemporalType.TIMESTAMP) @Column(name = "EXPECTED_EXECUTION_DATE")
    public Date getExpectedExecutionDate() {
        return expectedExecutionDate;
    }

    public void setExpectedExecutionDate(Date expectedExecutionDate) {
        this.expectedExecutionDate = expectedExecutionDate;
    }
    
    /**
	 * @return the accountOperation
	 */
    @Column(name = "ACCOUNT_OPERATION")
	@Enumerated(EnumType.STRING)
	public RequestAccountOperation getAccountOperation() {
		return accountOperation;
	}

	/**
	 * @param accountOperation the accountOperation to set
	 */
	public void setAccountOperation(RequestAccountOperation accountOperation) {
		this.accountOperation = accountOperation;
	}
	
	/**
	 * @return the calculateExpectedExecutionDateFromRequestApprovalTime
	 *
	@Column(name = "CALC_EXE_CDATE_FRM_REQ_APRVL")
	public boolean isCalculateExpectedExecutionDateFromRequestApprovalTime() {
		return calculateExpectedExecutionDateFromRequestApprovalTime;
	}
	*/
	
	/**
	 * @param calculateExpectedExecutionDateFromRequestApprovalTime the calculateExpectedExecutionDateFromRequestApprovalTime to set
	 *
	public void setCalculateExpectedExecutionDateFromRequestApprovalTime(
			boolean calculateExpectedExecutionDateFromRequestApprovalTime) {
		this.calculateExpectedExecutionDateFromRequestApprovalTime = calculateExpectedExecutionDateFromRequestApprovalTime;
	}
	*/
	
	
	
	
	
	
	
	
	//HELPER/TRANSEINTS
	public static RequestAccount factory(Request accountRequest, RequestAccountOperation accountOperation, String accountName, String resourceName, Date expectedExecutionDate) {
		RequestAccount req = new RequestAccount();
		req.setCreationDate(new Date());
		req.setRequest(accountRequest);
		req.setAccountName(accountName);
		req.setResourceName(resourceName);
		req.setAccountOperation(accountOperation);
		
		if (expectedExecutionDate == null) {
			req.setExpectedExecutionDate(new Date());
		} else {
			req.setExpectedExecutionDate(expectedExecutionDate);
		}
		
		return req;
	}

	
}
