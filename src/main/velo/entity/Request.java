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
//@!@not
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.jboss.seam.jsf.SetDataModel;

import velo.exceptions.AttributeNotFound;
import velo.exceptions.AttributeSetValueException;
import velo.storage.AttributeValueTypes;

/**
 An entity that represents a Request
 This is a super class for all child entities (such as CreateUserRequest/DeleteUserRequest/etc...)
 
 @author Asaf Shakarchi
 */
@Entity
@Table(name="VL_REQUEST")
//@Name("request") //Seam name
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="REQUEST_TYPE_CLASS")
@SequenceGenerator(name="RequestIdSeq",sequenceName="REQUEST_ID_SEQ")
@NamedQueries({

	
	
	
	
	
    @NamedQuery(name = "request.findById",query = "SELECT object(request) FROM Request AS request WHERE request.requestId = :requestId"),
    @NamedQuery(name = "request.findByStatus",query = "SELECT object(request) FROM Request AS request WHERE request.status = :status ORDER BY request.requestId DESC"),
    @NamedQuery(name = "request.findRequestsToProcess", query = "SELECT object(request) FROM Request AS request where request.status = 'APPROVED' AND request.processed = 0 AND request.inProcess = 0 ORDER BY request.creationDate DESC"),
    @NamedQuery(name = "request.findLastProcessedRequest", query = "SELECT object(request) FROM Request AS request where request.status = 'APPROVED' AND request.processed = 1 AND request.inProcess = 0 and request.processedTime is not null ORDER BY request.processedTime DESC"),
    @NamedQuery(name = "request.countRequestsInProcess", query = "SELECT count(request) FROM Request AS request WHERE request.inProcess = 1 AND request.processed = 0")
})
    public class Request extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public enum RequestStatus {
    	REQUEST_INITIATED, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED, SUSPENDED
	}
    
    private Long requestId;
    
    /**
     A user entity which made this request (if any, request might be inserted by automated systems)
     */
    private User requester;
    
    private Date processedTime = new Date();

    //private RequestStatus status;
    private RequestStatus status;
    //private String status = Request.PENDING_APPROVAL;
    
    /**
     The logs of the request
     */
    private Set<RequestLog> logs = new HashSet<RequestLog>();
    
    private Set<RequestUserComment> requestUserComments = new HashSet<RequestUserComment>();
    
    /**
     A list of bulk tasks that are apart of this request
     */
    private Set<BulkTask> bulkTasks = new HashSet<BulkTask>();
    
    /**
     A list of tasks that are apart of this request.
     */
    private Set<Task> tasks = new HashSet<Task>();
    
    /**
     Whether the request was executed by a superuser or not (if so, the request will skip the workflow and will be executed immediately!)
     
     private boolean isExecutedBySuperUser;*/
    
    /**
     Notes regarding this request.
     */
    private String notes;
    
    private ArrayList<String> tags;
    
    private boolean processed;
    
    private boolean inProcess;
    
    private boolean successfullyProcessed;
    
    private boolean inBusinessProcess = false;
    
    private Set<Form> forms = new HashSet<Form>();
    
    private String businessProcessName; 
    
    /**
     The request attributes.
     (This is transient and will be persisted serialized)
     @see #serializedRequestAttributes
     */
    //private TrailedRequest trailedRequest = new TrailedRequest();
    //private Attributes attributes = new Attributes();
    private List<RequestAttribute> attributes = new ArrayList<RequestAttribute>();
    
    
    
    
    
    
    
    /**
     @param requestId The requestId to set.
     */
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    
    /**
     @return Returns the requestId.
     */
    //GF@Id
    //GF@SequenceGenerator(name="IDM_REQUEST_GEN",sequenceName="IDM_REQUEST_SEQ", allocationSize=1)
    //GF@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="IDM_REQUEST_GEN")
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="RequestIdSeq")
    //@GeneratedValue //JB
    @Column(name="REQUEST_ID")
    public Long getRequestId() {
        return requestId;
    }
    
    /**
     @param requester The requester to set.
     */
    public void setRequester(User requester) {
        this.requester = requester;
    }
    
    /**
     @return Returns the requester.
     */
    //Only cascade in PERSIST, removing request never happens and should -never- delete the User
    @ManyToOne
    @JoinColumn(name="REQUESTER_USER_ID", nullable=true)
    public User getRequester() {
        return requester;
    }
    
    @Column(name="PROCESSED_TIME", nullable=true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }
    
    /**
     @param status the status to set
     */
    //public void setStatus(RequestStatus status) {
    public void setStatus(RequestStatus status) {
                /*CLEAN!
                //Before setting the status of the request, check whether there are events to fire or not
                Iterator itr = eventsPerRequestStatus.entrySet().iterator();
                while (itr.hasNext()) {
                        Map.Entry<RequestStatus,RequestEvent> event = (Map.Entry) itr.next();
                 
                        if (event.getKey() == status) {
                                velo.request.RequestEvent currReqEvent = event.getValue();
                                currReqEvent.setRequest(this);
                 
                                event.getValue().execute();
                        }
                }
                 */
        
        
        this.status = status;
    }
    
    /**
     @return the status
     */
    @Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
    public RequestStatus getStatus() {
        return status;
    }
    
    /**
     @param logs The logs to set.
     */
    public void setLogs(Set<RequestLog> logs) {
        this.logs = logs;
    }
    
    /**
     @return Returns the logs.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy="request", fetch=FetchType.LAZY)
    public Set<RequestLog> getLogs() {
        return logs;
    }
    
    /**
    @param requestUserComments The requestUserComments to set.
    */
   public void setRequestUserComments(Set<RequestUserComment> requestUserComments) {
       this.requestUserComments = requestUserComments;
   }
   
   /**
    @return Returns the requestUserComments.
    */
   @OneToMany(cascade = CascadeType.ALL, mappedBy="request", fetch=FetchType.LAZY)
   public Set<RequestUserComment> getRequestUserComments() {
       return requestUserComments;
   }
    
    
    /**
     @param bulkTasks the bulkTasks to set
     */
    public void setBulkTasks(Set<BulkTask> bulkTasks) {
        this.bulkTasks = bulkTasks;
    }
    
    /**
     @return the bulkTasks
     */
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<BulkTask> getBulkTasks() {
        return bulkTasks;
    }
    
    /**
     @param tasks the tasks to set
     */
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
    
    /**
     @return the tasks
     */
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public Set<Task> getTasks() {
        return tasks;
    }
    
    /**
     @param note the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     @return the notes
     */
    @Column(name="NOTES", nullable=true)
    public String getNotes() {
        return notes;
    }
    
    @Column(name="TAGS", nullable=true)
    @Basic
    @Lob
    public ArrayList<String> getTags() {
        return tags;
    }
    
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    
    
    
        /*
         *
         * @param attributes the attributes to set
         *
        public void setAttributes(Attributes attributes) {
                this.attributes = attributes;
        }
         
        /*
         * @return the requestAttributes
         *
        @Column(name="SERIALIZED_ATTRIBUTES", nullable=true)
        public Attributes getAttributes() {
                return attributes;
        }
         */
    
    /**
     @param attributes the attributes to set
     */
    public void setAttributes(List<RequestAttribute> attributes) {
        this.attributes = attributes;
    }
    
    /**
     @return the attributes
     */
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<RequestAttribute> getAttributes() {
        return attributes;
    }
    
    @Column(name = "PROCESSED", nullable = false)
    public boolean isProcessed() {
        return processed;
    }
    
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
    
    @Column(name = "IN_PROCESS", nullable = false)
    public boolean isInProcess() {
        return inProcess;
    }
    
    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }
    
    @Column(name = "SUCCESSFULLY_PROCESSED", nullable = false)
    public boolean isSuccessfullyProcessed() {
        return successfullyProcessed;
    }

    public void setSuccessfullyProcessed(boolean successfullyProcessed) {
        this.successfullyProcessed = successfullyProcessed;
    }
    
    /**
	 * @return the inBusinessProcess
	 */
    @Column(name = "IN_BUSINESS_PROCESS", nullable = false)
	public boolean isInBusinessProcess() {
		return inBusinessProcess;
	}

	/**
	 * @param inBusinessProcess the inBusinessProcess to set
	 */
	public void setInBusinessProcess(boolean inBusinessProcess) {
		this.inBusinessProcess = inBusinessProcess;
	}
    
	
	/**
	 * @return the forms
	 */
	@OneToMany(mappedBy = "request", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public Set<Form> getForms() {
		return forms;
	}

	/**
	 * @param forms the forms to set
	 */
	public void setForms(Set<Form> forms) {
		this.forms = forms;
	}
	
	/**
	 * @return the businessProcessName
	 */
    @Column(name = "BUSINESS_PROCESS_NAME", nullable = true)
	public String getBusinessProcessName() {
		return businessProcessName;
	}

	/**
	 * @param businessProcessName the businessProcessName to set
	 */
	public void setBusinessProcessName(String businessProcessName) {
		this.businessProcessName = businessProcessName;
	}

	
	
	
	
	
	
	
	
	
	
	
	
    
    
    
    
    //TRANSIENT/HELPER
    public void initRequest(User requester, String notes) {
        Date currDate = new Date();
        
        if (requester != null) {
            setRequester(requester);
        }
        
        setCreationDate(currDate);
        setLastUpdateDate(currDate);
        setStatus(RequestStatus.REQUEST_INITIATED);
        
        if (notes != null) {
            setNotes(notes);
        }
        
        //RequestLog rl = new RequestLog();
        //rl.setLoggedByUser(requester);
        //rl.setMessage("Request ID: " + getRequestId() + ", to perform on user: " + user.getName() + " created.");
        //getRequestLogs().add(rl);
    }
    
    public void addRequestUserComment(User user, String comment) {
    	RequestUserComment ruc = RequestUserComment.factory(user,comment);
    	ruc.setRequest(this);
    	getRequestUserComments().add(ruc);
    }
    
    
    public boolean isTagExists(String tagName) {
        if (getTags() == null) {
            return false;
        }
        
        for (String currTag : getTags()) {
            if (currTag.toUpperCase().equals(tagName.toUpperCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<String>();
        }
        
        tags.add(tag);
    }
   
    
    public void addLog(String severity, String summaryMessage, String detailedMessage) {
        RequestLog rl = new RequestLog();
        rl.setCreationDate(new Date());
        rl.setSummaryMessage(summaryMessage);
        rl.setDetailedMessage(detailedMessage);
        rl.setSeverity(severity);
        rl.setRequest(this);
        getLogs().add(rl);
    }
    
    public void addAttribute(RequestAttribute attribute) {
        getAttributes().add(attribute);
    }
    
    public void addAttribute(String uniqueName, String displayName, Object value) throws AttributeSetValueException {
    	//if there's no value then skip the insertion
    	if (value ==null) {
    		System.out.println("Cannot add attribute to request with unique name '" + uniqueName + "' since it has no value");
    		return;
    	}
    	
    	RequestAttribute attr = RequestAttribute.factoryRequestAttribute(this);
        attr.setUniqueName(uniqueName);
        attr.setDisplayName(displayName);

        attr.addValue(value);
        
        //update attr's datatype as it's important for persistence.
        if (attr.getRequestAttributeValues().size() > 0) {
        	attr.setDataType(attr.getRequestAttributeValues().iterator().next().getDataType());
        } else {
        	System.out.println("Couldnt add the value for some reason, skipping attribute...");
        	return;
        }
        
        getAttributes().add(attr);
    }
    
    public void addAttribute(String name, String value, AttributeValueTypes type) {
        //addAttribute(name,value,type.toString());
    }
    
    @Transient
    public RequestAttribute getAttributeByName(String name) throws AttributeNotFound {
        for (RequestAttribute currRA : getAttributes()) {
            if (currRA.getUniqueName().toUpperCase().equals(name.toUpperCase())) {
                return currRA;
            }
        }
        
        throw new AttributeNotFound("Could not find attribute named: '" + name + "' in request ID: " + getRequestId());
    }
    
    @Transient
    public boolean isApproved() {
        return getStatus() == RequestStatus.APPROVED;
    }
    
    public boolean isAttributeExistsByName(String name) {
        for (RequestAttribute currRA : getAttributes()) {
            if (currRA.getUniqueName().toUpperCase().equals(name.toUpperCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Transient
    public String getReadableStatus() {
    	switch(getStatus()) {
			case APPROVED: {
				return "Approved";
			} case PENDING_APPROVAL: {
				return "Pending Approval";
			} case REJECTED: {
				return "Rejected";
			} case CANCELLED: {
				return "Canceled";
			} case SUSPENDED: {
				return  "Suspended";
			} default: {
				return "INVALID REQUEST STATUS";
			}
    	}
    }
    
    @Transient
    public String getType() {
        return "Base Request";
    }
    
    @Transient
    public boolean isRequester(User r) {
    	if (getRequester() != null)
    		return getRequester().equals(r);
    		
    	
    	return false;
    }
    
    
    
    @Transient
	public boolean isCancebleByRequester() {
		return getStatus() == RequestStatus.PENDING_APPROVAL; 
	}

}
