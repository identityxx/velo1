/**
 * Copyright (c) 2000-2007, Asaf Shakarchi
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

import groovy.lang.GroovyObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.OpenContentElement;
import org.openspml.v2.msg.spml.Extensible;
import org.openspml.v2.profiles.dsml.DSMLAttr;
import org.openspml.v2.profiles.dsml.DSMLProfileException;
import org.openspml.v2.profiles.dsml.DSMLValue;

import velo.actions.tools.ResourceAttributeActionRuleTools;
import velo.contexts.OperationContext;
import velo.entity.ResourceAttributeBase.SourceTypes;
import velo.exceptions.AttributeNotFound;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.EdentityException;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.ScriptInvocationException;
import velo.exceptions.ScriptLoadingException;

/**
 * @author Asaf Shakarchi
 *
 */
@Table(name = "VL_ACCOUNT")
@Entity
@SequenceGenerator(name="AccountIdSeq",sequenceName="ACCOUNT_ID_SEQ")
@NamedQueries( {
		@NamedQuery(name = "account.findByNameWithCase", query = "SELECT account FROM Account account WHERE account.name = :accountName AND account.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "account.findByNameIgnoreCase", query = "SELECT account FROM Account account WHERE upper(account.name) = upper(:accountName) AND account.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "account.findOnResourceForUser", query = "SELECT object(account) from Account account WHERE account.resource = :resource AND account.user = :user"),
		@NamedQuery(name = "account.isExistIgnoreCase", query = "SELECT count(account) FROM Account account WHERE UPPER(account.name) = :accountName AND account.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "account.isExistWithCase", query = "SELECT count(account) FROM Account account WHERE account.name = :accountName AND account.resource.uniqueName = :resourceUniqueName"),
		


		@NamedQuery(name = "isAccountExistOnTarget", query = "SELECT count(account) FROM Account AS account WHERE account.name = :accountName AND account.resource = :resource"),
		@NamedQuery(name = "isStaleAccountExistsOnTarget", query = "SELECT count(account) FROM Account AS account WHERE account.name = :accountName AND account.resource = :resource AND account.user is null"),
		
		@NamedQuery(name = "account.searchByString", query = "SELECT object(account) from Account account WHERE UPPER(account.name) like :searchString")
})
		
public class Account extends AccountSkeletal {
	private static transient Logger log = Logger.getLogger(Account.class.getName());

	public static Account factory(String accountName, Resource resource) {
		Account account = new Account();
		account.setResource(resource);
		account.setName(accountName);
		account.setCreationDate(new Date());

		return account;
	}
	
	
	public Account() {
	}
	
	public Account(String accountName, Resource resource) {
		setName(accountName);
		setResource(resource);
		setCreationDate(new Date());
	}

	//private long accountId;
	private Long accountId;

	/**
	 * The entity that the account is attached to
	 */
	private User user;

	private String status = "Not checked";

	// Transient, holds active attributes of the account, constructed from
	// activeData or fetched online from resource
	
	// Also used by building the virtual values upon UserAttributes/resourceAttributes since Velo doesn't keep -all of the- account's attributes
	private HashMap<String, Attribute> activeAttributes = new HashMap<String, Attribute>();
	//Whether the active attributes for this account were loaded or not
	private boolean activeAttributesLoaded;
	
	
	//persistence attributes per account.
	private Set<AccountAttribute> accountAttributes = new HashSet<AccountAttribute>();

	private Set<ResourceGroupMember> groupMembership = new HashSet<ResourceGroupMember>();
	
	
	/**
	 * Get the account ID
	 * 
	 * @return Account ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="AccountIdSeq")
	//@GeneratedValue
	@Column(name = "ACCOUNT_ID")
	//public long getAccountId() {
	public Long getAccountId() {
		return accountId;
	}

	/**
	 * Set account ID
	 * 
	 * @param accountId
	 *            Account ID to set
	 */
	//public void setAccountId(long accountId) {
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	/**
	 * Set user entity
	 * 
	 * @param user
	 *            User entity to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Get the user entity that is own the account
	 * 
	 * @return User entity
	 */
	// Only cascade in PERSIST, unlinking account from User -never- requires to
	// delete the User as well
	// NOTE: had CascadeType.MERGE here, since Account objects usually gets
	// SERIALIZED into TASKS, had a situation that when a 'Delete Account
	// Action'
	// Was performed, when it successfully ended, the serialized User entity got
	// UPDATED with old state (as probably the serliazed account->user entity
	// had UserRoles that had to be deleted) but when update was called
	// It re-merged all changes of the User. this is wrong. an Account should
	// not affect its User. if User needs to be updated, then the User entity
	// should be loaded and be merged by itself. and not through the Account
	// entity.
	// Removing the CascadeType.MERGE solved the problem.
	@ManyToOne(cascade = { CascadeType.PERSIST })
	// Field is nullable, because importing accounts may not have userID
	@JoinColumn(name = "USER_ID", nullable = true, unique = false)
	public User getUser() {
		return user;
	}
	
	@OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("resourceAttribute ASC")
	public Set<AccountAttribute> getAccountAttributes() {
		return accountAttributes;
	}


	public void setAccountAttributes(Set<AccountAttribute> accountAttributes) {
		this.accountAttributes = accountAttributes;
	}
	
	public void addAccountAttribute(Object singleValue, ResourceAttribute ra) throws AttributeSetValueException {
		AccountAttribute attr = new AccountAttribute();
		AccountAttributeValue val = new AccountAttributeValue();
		
		attr.getValues().add(val);
		attr.setResourceAttribute(ra);
		attr.setAccount(this);
		
		val.setValue(singleValue);
		val.setAccountAttribute(attr);
		
		getAccountAttributes().add(attr);
	}
	
	@Transient
	public AccountAttribute getAccountAttribute(String uniqueName) {
		for (AccountAttribute currAA : getAccountAttributes()) {
			if (currAA.getResourceAttribute() == null) {
				throw new RuntimeException("Could not find RA for account attribute ID '" + currAA.getAccountAttributeId() + "' for account name '" + getName() + "'");
			}
			if (currAA.getResourceAttribute().getUniqueName().equals(uniqueName)) {
				return currAA;
			}
		}
		
		return null;
	}
	

	@OneToMany(mappedBy="primaryKey.account")
	public Set<ResourceGroupMember> getGroupMembership() {
		return groupMembership;
	}


	public void setGroupMembership(Set<ResourceGroupMember> groupMembership) {
		this.groupMembership = groupMembership;
	}


	// TRANSIENT/HELPER
	/**
	 * Add an attribute to the AccountAttribute map
	 * 
	 * @param attribute
	 *            An attribute object to add
	 * @see #getAccountAttributes()
	 */
	public void addActiveAttribute(Attribute attribute) {
		// system.finest("Adding attribute name: " + attribute.getName() + ",
		// with value: " + attribute.getStringValue() + ", with type: " +
		// attribute.getType());
		
		if (getResource().isCaseSensitive()) {
			getActiveAttributes().put(attribute.getUniqueName(), attribute);
		} else {
			getActiveAttributes().put(attribute.getUniqueName().toUpperCase(), attribute);
		}
	}

	/**
	 * Get all account attributes as a MAP Note: these account attributes must
	 * be loaded by some logical method (probably written in the corresponding
	 * 'account' EJB)
	 * 
	 * @return a HashMap containing all account attributes
	 */
	@Transient
	public HashMap<String, Attribute> getActiveAttributes() {
		return activeAttributes;
	}
	
	public void setActiveAttributes(HashMap<String, Attribute> activeAttributes) {
		this.activeAttributes = activeAttributes;
	}
	
	public void dumpActiveAttributes() {
    	int i=0;
    	for (Map.Entry<String,Attribute> currAttr : getActiveAttributes().entrySet()) {
    		i++;
    		/*
    		log.trace("Start of attr #" + i + " ,unique name: '" + currAttr.getKey() + "'");
    		int j=0;
    		for (AttributeValue attrVal : currAttr.getValue().getValues()) {
    			j++;
    			log.trace("Value #" + j +" : value: '" + attrVal.getAsString() + "[" + attrVal.getValueAsObject().getClass().getName()+"]");
    		}
    		log.trace("End of attr #"+i);
    		*/
    		log.info(currAttr.getValue().getDisplayable());
    	}
    }
	
	public void dumpAccountAttributes() {
    	int i=0;
    	for (AccountAttribute currAttr : getAccountAttributes()) {
    		i++;
    		/*
    		log.trace("Start of attr #" + i + " ,unique name: '" + currAttr.getKey() + "'");
    		int j=0;
    		for (AttributeValue attrVal : currAttr.getValue().getValues()) {
    			j++;
    			log.trace("Value #" + j +" : value: '" + attrVal.getAsString() + "[" + attrVal.getValueAsObject().getClass().getName()+"]");
    		}
    		log.trace("End of attr #"+i);
    		*/
    		log.info(currAttr.getAsStandardAttribute().getDisplayable());
    	}
    }

	/**
	 * Set whether the active attributes were loaded or not
	 * 
	 * @param isAccountAttributesLoaded
	 *            true/false boolean upon loaded/not loaded
	 */
	@Transient
	public void setActiveAttributesLoaded(boolean activeAttributesLoaded) {
		this.activeAttributesLoaded = activeAttributesLoaded;
	}

	/**
	 * Return a boolean, indicate whether the active attributes were loaded
	 * or not
	 * 
	 * @return true/false upon loaded/not loaded
	 */
	@Transient
	public boolean isActiveAttributesLoaded() {
		return activeAttributesLoaded;
	}
	
	@Transient
	public boolean isActiveAttributeExists(String uniqueName) {
		for (Attribute currAttr : getActiveAttributes().values()) {
			if (currAttr.getUniqueName().equals(uniqueName)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Attribute getActiveAttribute(String uniqueName) {
		/*
		for (Attribute currAttr : getActiveAttributes().values()) {
			if (currAttr.getUniqueName().equals(uniqueName)) {
				return currAttr;
			}
		}
		
		return null;
		*/
		
		if (getResource().isCaseSensitive()) {
			return getActiveAttributes().get(uniqueName);
		} else {
			//Expecting map keys to be in uppercase here
			return getActiveAttributes().get(uniqueName.toUpperCase());
		}
	}
	
	@Override
    public boolean equals(Object obj) {
		if (!(obj != null && obj instanceof Account))
			return false;
		Account ent = (Account) obj;
		if (this.accountId.equals(ent.accountId))
			return true;
		return false;
    }
	
	
	
	
	
	/**
	 * Load a resource attribute with values virtually for this account
	 * Exceptions:
	 *   - This method only works if the account is associated 
	 *     with a User since resource attributes values are heavily based on User IdentityAttribute
	 * Algorithm:
	 *   - Verify that the account is associated with a User, otherwise through an exception
	 *   - Verify that the specified resource unique name is defined for the account's resource, otherwise through an exception
	 *   - If there is a direct mapping to an IdentityAttribute, then fetch its value for the account's user
	 *     and set the attribute's value by the User's IdentityAttribute value, otherwise continue directly to the next step
	 *   - Per attribute rule attached to the Resource Attribute, perform rule execution
	 *   - return the resulted 'ResourceAttriubute' object
	 *    
	 * @param resourceUniqueName The resource attribute unique name to load the attribute for this account
	 * @return A virtually loaded resource attribute 
	 */
	@Transient
	public ResourceAttribute getVirtualAttribute(String resourceAttrUniqueName, Set<RoleResourceAttribute> roleResAttrs) throws LoadingVirtualAccountAttributeException {
		log.debug("Calculating virtual account attribute for account name '" + getName() 
			+ "', for resource attribuute uniquely named '" + resourceAttrUniqueName + "'");
		
		if (getUser() == null) {
			throw new LoadingVirtualAccountAttributeException("Cannot perform virtual attribute loading for accounts that are not associated with a User");
		}

		ResourceAttribute ra = getResource().getResourceAttribute(resourceAttrUniqueName);
		if (ra == null) {
			throw new LoadingVirtualAccountAttributeException("Could not find resource attribute unique name '" + resourceAttrUniqueName + "' for resource '" + getResource().getDisplayName() + "'");
		}
		
		boolean userIdentityAttributeMapping;
		if (ra.getIdentityAttribute() == null) {
			userIdentityAttributeMapping = false;
		}
		else {
			userIdentityAttributeMapping = true;
		}
		
		
		log.trace("Resource Attribute source type is '" + ra.getSourceType() + "'");
		UserIdentityAttribute mappedUIA = null;
		if (userIdentityAttributeMapping) {
			mappedUIA = getUser().getUserIdentityAttribute(ra.getIdentityAttribute());
			//make sure that there is a UIA instance for the user, otherwise skip mapping
			if (mappedUIA == null) {
				//TODO: info? or debug? or warn?
				log.info("Skipping mapping, could not find a UserIdentityAttribute instance...");
				
				//factoring empty value instance so it'll be easy for the integrator to set value in rules
				ra.addValue(ra.factoryValue());
			} else {
				//make sure that the user identity attribute has a value, although always expecting one if there is a UIA instance
				if (mappedUIA.isFirstValueIsNotNull()) {
					log.trace("attribute was mapped to IdentityAttribute named '" + mappedUIA.getDisplayName() + "', importing its values...");
					ra.importValues(mappedUIA);
				} else {
					log.debug("User Identity Attribute instnace was found but has no values, factoring first empty value...");
					ra.importValues(mappedUIA);
				}
			}
		} 
		//TODO: Else? ifs?! here?! wtf!? clean it up!
		else if (ra.getSourceType() == SourceTypes.USER_NAME) {
			log.trace("Attribute source is 'USER_NAME', getting the user name value...");
			try {
				ra.addValue(getUser().getName());
			} catch (AttributeSetValueException e) {
				throw new LoadingVirtualAccountAttributeException("Could not load virtual attribute name '" + resourceAttrUniqueName + "': " + e.toString());
			}
		} else if (ra.getSourceType() == SourceTypes.USER_PASSWORD) {
			log.trace("Attribute source is 'USER_PASSWORD', getting the user password value...");
			//TODO: Support encrypted passwords will ruin this, must decrypt here
			try {
				ra.addValue(getUser().getPassword());
			}catch (AttributeSetValueException e) {
				throw new LoadingVirtualAccountAttributeException("Could not load virtual attribute name '" + resourceAttrUniqueName + "': " + e.toString());
			}
		}
		else {
			log.trace("attribute has no default mapping, factoring first null value cell...");
			//set one empty value for easy access via attribute rules
			ra.addValue(ra.factoryValue());
		}
		
		
		OperationContext context = new OperationContext();
		//set a map with all other identityAttributes of the user
		context.addVar("userIdAttrs", getUser().getUserIdentityAttributesAsMap());
		context.addVar("userName", getUser().getName());
		context.addVar("resourceUniqueName", getResource().getUniqueName());
		context.addVar("resourceDisplayName", getResource().getDisplayName());
		context.addVar("attr", ra);
		context.addVar("attrs", getActiveAttributes());
		
		ResourceAttributeActionRuleTools tools = new ResourceAttributeActionRuleTools();
		//Perform attribute action rules
		for (ResourceAttributeActionRule currActionRule : ra.getActionRules()) {
			try {
				GroovyObject go = currActionRule.getScriptedObject();
				//Set the resource attribute into the scripted object, which hopefully pass by reference thus any modification to this object should be affected already without any special clonings/data coping...
				go.setProperty("attribute", ra);
				//21-may-08: this is the old name but must be kept for backward compatability, should be deprecated and removed in version 2
				go.setProperty("context", context);
				go.setProperty("cntx", context);
				go.setProperty("tools", tools);

				invoke(go);
			} catch (ScriptInvocationException e) {
				if (currActionRule.isShowStopper()) { 
					throw new LoadingVirtualAccountAttributeException("Failed to invoke action rule expression '" + currActionRule.getDescription() + "', that is indicated as a showStopper: " + e.toString());
				}
				else {
					//TODO: Log the message into the EventLog or something
					log.warn("Failed to ivoke action rule expression '" + currActionRule.getDescription() + "', that is not indicated as a showStopper(thus not breaking the process): " + e.toString());
				}
			} catch (ScriptLoadingException e) {
				if (currActionRule.isShowStopper()) { 
					throw new LoadingVirtualAccountAttributeException("Failed to load action rule expression '" + currActionRule.getDescription() + "', that is indicated as a showStopper: " + e.toString());
				}
				else {
					//TODO: Log the message into the EventLog or something
					log.warn("Failed to load action rule expression '" + currActionRule.getDescription() + "', that is not indicated as a showStopper(thus not breaking the process): " + e.toString());
				}
			}
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Resulted virtual attribute with values amount '" + ra.getValues().size() + "'");
			int i=0;
			for (AttributeValue val : ra.getValues()) {
				if (val.getValueAsObject() != null) {
					log.trace("Value [" + i + "] is: " + val.getAsString());
				} else {
					log.trace("Value #"+i+" is null...");
				}
				i++;
			}
		}
		
		
		
		
		
		
		//TODO: Support rules priorities here?!?!?!?
		//iterate over the roles attrs and make the modifications
		for (RoleResourceAttribute currRoleResourceAttr : roleResAttrs) {
			//per rule make sure the 'attribute' var was cleaned from previous run as 'roleAttr.calculate'
			//does not override existence attribute. (roleResAttr(entity).read calculateAttributeResult() for more information)
			context.removeVar("attribute");
			ResourceAttribute resAttr = currRoleResourceAttr.calculateAttributeResult(context);
			
			if (resAttr == null) {
				//TODO: Support showStopper handling.
				log.warn("Skipping role resource attribute since a null was returned (expected a resource attribute!)");
				continue;
			}
			
			
			log.trace("Calculated (first) value(from role resource attribute!) is: '" + resAttr.getFirstValue().getAsString() + "' of resource attribute '" + resAttr.getUniqueName() + "'");
			
			
			log.trace("The ResourceAttribute has values from 'resource attribute level' with amount '" + ra.getValues().size() + "', VALUES WILL BE OVERRIDED BY CURRENT ITERATED RULE!");
			
			if (resAttr.isFirstValueIsNotNull()) {
				ra.setValues(resAttr.getValues());
			}
		}
		
		
		
		
		return ra;
	}

	
	//roles to add might be null in case delete/modify account is using this method as now 'modify account' does not support modify attributes
	@Transient
	public HashMap<String, Attribute> getVirtualAttributes(Set<Role> rolesToAdd) {
		//better to clean all resource attributes as this resource object might be in use already in the past and its attributes might already loaded with values by this method in the past
		getResource().clearActiveAttributeValues();
		
		
		log.debug("----START OF: GETVIRTUALATTRIBUTES()-----");
		if (isActiveAttributesLoaded()) {
			return getActiveAttributes();
		}
		else {
			setActiveAttributesLoaded(true);
			
			Set<RoleResourceAttribute> roleResAttrs = new HashSet<RoleResourceAttribute>();
			for (ResourceAttribute resourceAttribute : getResource().getAttributes().getOnlyActive()) {
				//clean the collection from previous runs
				roleResAttrs.clear();
				
				try {
					
					if (rolesToAdd != null) {
						//Retrieve the role resource attributes relevant for the current iterated resource attribute
						for (Role currRole : rolesToAdd) {
							roleResAttrs.addAll(currRole.getRoleResourceAttributes(resourceAttribute));
						}
					}
					
					
					resourceAttribute = getVirtualAttribute(resourceAttribute.getUniqueName(),roleResAttrs);
					getActiveAttributes().put(resourceAttribute.getUniqueName(), resourceAttribute);
				} catch (LoadingVirtualAccountAttributeException e) {
					log.warn("Failed to load virtual attribute, skipping to the next attribute: " + e.toString());
					continue;
				}
			}
		}
		
		log.debug("----END OF: GETVIRTUALATTRIBUTES()-----");
		return getActiveAttributes();
	}
	
	//just a proxy
	@Transient
	public Extensible getVirtualAttributesAsSPMLExtensible(List<Attribute> attrs) throws DSMLProfileException {
		Map<String,Attribute> attrsMap = new HashMap<String,Attribute>();
		
		for (Attribute currAttr : attrs) {
			if ( (currAttr.getUniqueName() != null) && (currAttr.getUniqueName().length() > 0) ) {
				attrsMap.put(currAttr.getUniqueName(), currAttr);
			} else {
				throw new DSMLProfileException("Could not create SPML attribute extensible, one of the specified attributes has no unique name!");
			}
		}
		
		
		if (attrsMap.size() > 0) {
			return getVirtualAttributesAsSPMLExtensible(attrsMap);
		} else {
			throw new DSMLProfileException("Could not create SPML attribute extensible, no attributes were found !");
		}
	}
	
	@Transient
	public Extensible getVirtualAttributesAsSPMLExtensible(Map<String, Attribute> attrsMap) throws DSMLProfileException {
		log.debug("----START OF: getVirtualAttributesAsSPMLExtensible()-----");
		
		if (log.isTraceEnabled()) {
			log.trace("Dumping virtual attribute map entries!");
			for (Map.Entry<String,Attribute> currMapEntry : attrsMap.entrySet()) {
				log.trace("Map key: '" + currMapEntry.getKey() + "', map(attr) values amount: '" + currMapEntry.getValue().getValues().size() + "'");
			}
		}
		
		
		log.debug("Attributes map size: " + attrsMap.size());
		Extensible ext = new Extensible();
		for(Attribute currAttr : attrsMap.values()) {
			DSMLAttr dsmlAttr = getVirtualAccountAttributeAsDSMLAttr(currAttr);
			if (dsmlAttr != null) {
				ext.addOpenContentElement(dsmlAttr);
			}
		}
		
		
		
		
		if (log.isTraceEnabled()) {
			log.trace("Dumping Extensible's DSML attributes");
			OpenContentElement[] contentElements = ext.getOpenContentElements();
			for (int i=0;i<contentElements.length;i++) {
				DSMLAttr currAttr = (DSMLAttr)contentElements[i];
				log.trace("Found DSML Attribute with name '" + currAttr.getName() + "', with values size: " + currAttr.getValues().length);
				
				DSMLValue[] currAttrValues = currAttr.getValues();
				for (int l=0; l<currAttr.getValues().length;l++) {
					log.trace("Value[" + l +"]: '" + currAttrValues[l].getValue() + "'");
				}
			}
		}
		
		log.debug("----END OF: getVirtualAttributesAsSPMLExtensible()-----");
		return ext;
	}
	
	@Transient
	public Extensible getVirtualAttributesAsSPMLExtensible(Set<Role> rolesToAdd) throws DSMLProfileException {
		Map<String, Attribute> attrsMap = getVirtualAttributes(rolesToAdd);
		return getVirtualAttributesAsSPMLExtensible(attrsMap);
	}
	
	@Transient
	public DSMLAttr getVirtualAccountAttributeAsDSMLAttr(Attribute virtualAccountAttribute) throws DSMLProfileException {
		log.trace("Constructing a DSML SPML Attribute from Velo Virtual Account Attribute named '" + virtualAccountAttribute.getUniqueName() + "', with amount of values: '" + virtualAccountAttribute.getValues().size());
		
		//warning: this.getVirtualAttribute() set a default first value element for resource attributes that has no default mappings
		//if the integrator has failed to set a value, then here a DSMLValue with null value will be constructed
		//This will result a NPE when the request gets marshalled, then such attributes must be skipped here.
		//If they are required, the request will fail via the resource XSD schema validation. 
		
		if (!virtualAccountAttribute.isFirstValueIsNotNull()) {
			log.info("Could not construct a DSML SPML attribute since the specified virtual account attribute has no values...");
			return null;
		}
		
		DSMLValue[] values = new DSMLValue[virtualAccountAttribute.getValues().size()];
		
		int i=0;
		for (AttributeValue currValue : virtualAccountAttribute.getValues()) {
			values[i] = new DSMLValue(currValue.getValue());
		}
		
		DSMLAttr attr = new DSMLAttr(virtualAccountAttribute.getUniqueName(),values);
		
		return attr;
	}
	
	
	//TODO: Put in parent
	private void invoke(GroovyObject go) throws ScriptInvocationException {
		try {
			log.trace("Invoking default method over scripted groovy object");
			Object[] args = {};
			go.invokeMethod("run", args);
			log.trace("Ended method invokation");
		}
		//Wrap any exception into one, scripts are externally supplied thus any exception might occur here since 
		catch (Exception e) {
			throw new ScriptInvocationException("Failed to execute script: " + e.toString());
		}
	}

	
	
	@Transient
	public String getNameInRightCase() {
		if (getResource().isCaseSensitive()) {
			return getName();
		} else {
			return getName().toUpperCase();
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Load current account properties by a map (Mostly used by the Reconcile
	 * process in order to load 'account attributes' per listed account on a resource)
	 * <i>note: This method supports multiple values as long as the 'values' is a collection.</i>
	 *  
	 *  Main Logic:
	 *  	- Verify that there's account ID.
	 *  	- Set the account ID attribute from the map as the account name
	 *  	- Per available RA, load the attribute:
	 *  		- If RA is persistence, load it as 'account attribute'
	 *  		- If RA is not persistence, load it as a 'active attribute'
	 *  
	 *  
	 * @param map A map contains key=unique_name of the attribute, value=the value of the attribute
	 * @throws ObjectsConstructionException
	 */
	/*
	public void loadAccountByMap(Map map) throws ObjectsConstructionException {
		ResourceAttribute accId = getResource().getAccountIdAttribute();

		if (accId == null) {
			throw new ObjectsConstructionException(
					"Couldnt find any resource attribute that is indicates as an Account ID.");
		}

		// NOTE: very problematic! DbUtil returns all keys of maps as lowercase
		// while velo maintain the unique name as uppercase!
		
		// TODO: Dbutil's lowercase issue should get solved in a lower level!
		// (probably in adapter level!)
		if (map.containsKey(accId.getUniqueName())) {
			// logger.info("Loading account by map: " +
			// (String)map.get("username"));
			// TODO: Trimming spaces (should actually trim only the spaces on
			// the end of the username since sometimes users might be for
			// example 'firstname lastname'
			// NOTE: For some reason, sometimes recieced accounts with spaces,
			// then lets trim them.
			// This only happennd in MSSQL EXpress but what if username is built
			// from 'first last' names? with space? I think we should trim only
			// spaces at the end of the line!

			// Make sure that we got a value, if a null returned for an ACCOUNT
			// ID attribute, then throw an exception
			if (map.get(accId.getUniqueName()) == null) {
				throw new ObjectsConstructionException(
						"Couldnt load account, ACCOUNT ID attribute was not found!");
			} else {
				//FIXME: Should use setNameInRightCase or not?
				this.setName((String) map.get(accId.getUniqueName()).toString().trim());
			}
		} else {
			throw new ObjectsConstructionException(
					"Cannot load an account by MAP, couldnt find '" + accId.getUniqueName() + "' field which is a MUST");
		}
		
		
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			
			ResourceAttribute currRA = getResource().getResourceAttributeIgnoreCase((String)pairs.getKey());
			if (currRA == null) {
				log.info("No resource attribute was found for recieved attribute '" + pairs.getKey() + "' while loading account by map.");
				continue;
			} 

			if (currRA.isPersistence()) {
				AccountAttribute currAA;
				try {
					currAA = AccountAttribute.factory(currRA,this,pairs.getValue());
					getAccountAttributes().add(currAA);
				} catch (AttributeSetValueException e) {
					e.printStackTrace();
					//TODO: !!!!!!!!!!!!!!!!!!!!!!CRITICAL, WHAT IS THE SAFEST THING TO DO WHEN SOMETHING LIKE THAT OCCUR?! :/
				}
			}
			else {
				//only add active if RA is not flagged as persistence
				addActiveAttribute(factoryActiveAttribute((String)pairs.getKey(), pairs.getValue()));
			}
		}
		
		
		//Indicate that the account attributes were loaded manually
		this.setActiveAttributesLoaded(true);

	}
	*/
	
	
	/**
	 * Import active attributes by map, this will create an 'active attribute' per entry in the map, with setting the value(s) too
	 * 
	 * <note>Mainly used by JDBC adapter, hopefully DBUtils keys are column's names in lowercase, better to handle names case depends whether the resource is case sensitive or not</note>
	 * <note>Ignore 
	 * @param map A map contains all the attributes->values where a value can be a collection or value or a single value
	 * 
	 * @throws ObjectsConstructionException
	 */
	public void loadActiveAttributesByMap(Map map) throws ObjectsConstructionException {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			
			//'addActiveAttribute' already take care of setting the key in the appropriate case
			addActiveAttribute(factoryActiveAttribute((String)pairs.getKey(), pairs.getValue()));
		}
		
		
		//Indicate that the active attributes were loaded
		this.setActiveAttributesLoaded(true);
	}
	
	
	/**
	 * Init the account by the loaded active attributes
	 * Expecting the map to be loaded by {@link #loadActiveAttributesByMap(Map)}, which handles key names in the right case
	 * 
	 * @throws ObjectsConstructionException 
	 */
	public void initFullAccountByLoadedActiveAttributes() throws ObjectsConstructionException {
		if (!isActiveAttributesLoaded()) {
			throw new ObjectsConstructionException("Active attributes were not loaded!");
		}
		
		if (getResource() == null) {
			throw new ObjectsConstructionException("Resource was not set for this account, must set resource entity first before invoking this method.");
		}
		
		ResourceAttribute accId = getResource().getAccountIdAttribute();

		if (accId == null) {
			throw new ObjectsConstructionException(
					"Couldnt find any resource attribute that is indicates as an Account ID.");
		}

		if (getActiveAttributes().containsKey(accId.getUniqueNameInRightCase())) {
			// logger.info("Loading account by map: " +
			// (String)map.get("username"));
			// TODO: Trimming spaces (should actually trim only the spaces on
			// the end of the username since sometimes users might be for
			// example 'firstname lastname'
			// NOTE: For some reason, sometimes recieced accounts with spaces,
			// then lets trim them.
			// This only happennd in MSSQL EXpress but what if username is built
			// from 'first last' names? with space? I think we should trim only
			// spaces at the end of the line!

			// Make sure that we got a value, if a null returned for an ACCOUNT
			// ID attribute, then throw an exception
			if (getActiveAttributes().get(accId.getUniqueNameInRightCase()) == null) {
				throw new ObjectsConstructionException(
						"Couldnt init account, ACCOUNT_ID attribute was not found!");
			} else {
				this.setName((String) getActiveAttributes().get(accId.getUniqueNameInRightCase()).getFirstValueAsString().trim());
			}
		} else {
			throw new ObjectsConstructionException(
					"Cannot load an account, couldnt find '" + accId.getUniqueNameInRightCase() + "' in active attributes which is a must!");
		}
		

		Set<String> persistenceAttrsNames = new HashSet<String>(); 
		//required for the new created accounts via reconcile/or anything else
		for (Map.Entry<String,Attribute> currAA : getActiveAttributes().entrySet()) {
			//if resource is case sensitive, will search in case sensitive, otherwise will search case ignorance in mind 
			ResourceAttribute currRA = getResource().getResourceAttribute(currAA.getValue().getUniqueName());

			if (currRA == null) {
				log.debug("No resource attribute was found for recieved attribute '" + currAA.getValue().getUniqueName() + "' while loading account by map.");
				
				//FIXME: shell we clean it from the active attributes or not?
				continue;
			} 

			//If attribute is persistence, factory a new account attribute
			if (currRA.isPersistence()) {
				AccountAttribute currAccountAttribute;
				try {
					currAccountAttribute = AccountAttribute.factory(currRA,this,currAA.getValue());
					getAccountAttributes().add(currAccountAttribute);
				} catch (AttributeSetValueException e) {
					//THROW OUT AN EXCEPTION, A VALUE COULD NOT BE SET.
					throw new ObjectsConstructionException(e.getMessage());
				}
				
				//remove it from active (case matters, set the key name, not the attr's uniqueName) 
				persistenceAttrsNames.add(currAA.getKey());
			}
			//Attribute is not persistence, keep it as active, do nothing.
			else {
				//
			}
		}
		
		for (String attrUniqueNameToRemoveFromActive : persistenceAttrsNames) {
			getActiveAttributes().remove(attrUniqueNameToRemoveFromActive);
		}
	}
	
	
	
	/**
	 * Get all attributes persistence + active
	 * 
	 * @return A set of attributes
	 */
	@Transient
	public Map<String,Attribute> getAttributes() {
		Map<String,Attribute> attrs = new HashMap<String,Attribute>();
		
		//first priority are the account attributes
		for (AccountAttribute currAA : getAccountAttributes()) {
			attrs.put(currAA.getUniqueName(),currAA.getAsStandardAttribute());
		}
		
		
		for (Attribute currActiveAttribute : getActiveAttributes().values()) {
			if (!attrs.containsKey(currActiveAttribute.getUniqueName())) {
				attrs.put(currActiveAttribute.getUniqueName(),currActiveAttribute);
			}else {
				log.warn("account.getAttributes() found an activeAttribute with the same name as an 'account attribute' that was already loaded."); 
			}
		}
		
		
		return attrs;
	}
	
	@Transient
	public boolean isAttributeExists(String attrName,boolean checkInActive) {
		return getAttribute(attrName,checkInActive) != null;
	}
	
	@Transient
	public Attribute getAttribute(String attrName, boolean checkInActive) {
		AccountAttribute accAttr =  getAccountAttribute(attrName);
		if (accAttr != null) {
			return accAttr.getAsStandardAttribute();
		}
		
		if (checkInActive) {
			Attribute transAttr = getActiveAttribute(attrName);
			if (transAttr != null) {
				return transAttr;
			}
		}
		
		return null;
	}
	
	/** 
	 * Factory a active attribute
	 * @param name The unique name of the attribute
	 * @param value The value, can be any kind of primitive/object that represents a primitive, or a collection[any type] that contains primitives
	 * @return
	 * @throws ObjectsConstructionException
	 */
	public Attribute factoryActiveAttribute(String name, Object value) throws ObjectsConstructionException {
		//validate whether there's an existence resource attribute defined for current iterated row
		
		//shouldnt be AccountAttribute at some point? (there's a lot of dependencies on 'Attribute' though)
		Attribute attr = new Attribute();
		
		//skip attribute if its name is null
		if ((name == null)) {
			log.info("Skipping current attribute, the NAME of the attribute is null!");
			throw new ObjectsConstructionException("Skipping current attribute, the NAME of the attribute is null!");
		}
		
		attr.setUniqueName(name);
		
		try {
			//sometimes in resources, columns for a certain account might be null of course
			if (value != null) {
				if (value instanceof Collection) {
					Collection valueCol = (Collection) value;
					for (Object currValue : valueCol) {
						attr.addValue(currValue);
					}
				} else {
					//'addValue' will take care of identifying the right type
					attr.addValue(value);
				}
				
				
				//FIXME: Is this is right?
				//set the attr type based on the first value type
				if (attr.getFirstValue() != null) {
					attr.setDataType(attr.getFirstValue().getDataType());
				}
			}
			else {
				log.debug("Added an attribute with unique name '" + attr.getUniqueName() + " for account '" + getName() + "' that has -no- value...'");
			}
			
		} catch (AttributeSetValueException asve) {
			log.warn("Could not set value into current iterated attribute, skipping attribute,  failure message: " + asve.getMessage());
		}
		
		
		return attr;
		
		
		
		
		//HOLD HISTORY
		
		
		// System.out.println("Mapping attribute name: '"+pairs.getKey() +
		// ", with value: '" + pairs.getValue() + "'");
		// Note: important to keep attribute names as UPPERCASE (dbUtil
		// MapListHandler returns all keys as lowercase!)
		// attr.setName(((String)pairs.getKey()).toUpperCase());

		// 24/08/06 -> Since there are JDBC based DBs that are case
		// sensitive (and other systems as well) at the end tweaked
		// DBUtils to return columns as case sensitive, so lets keep it
		// while saving the keys into the loaded attributes keys names

		
		
		

		// System.out.println(pairs.getValue());
		// !
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Load current account properties by a map (Mostly used by the Reconcile
	 * process in order to load 'virtual accounts' per listed account on a resource)
	 * <i>note: This method only supports single value per attribute</i>
	 *  
	 * @param map A map contains key=unique_name of the attribute, value=the value of the attribute
	 * @throws ObjectsConstructionException
	 */
	public void loadAccountByMapOrg(Map map) throws ObjectsConstructionException {
		ResourceAttributeBase accId = null;

		boolean accountIdAttributeFound = false;
		for (ResourceAttribute ra : getResource().getAttributes()) {
			if (ra.isAccountId()) {
				accId = ra;
				accountIdAttributeFound = true;
			}
		}

		if (!accountIdAttributeFound) {
			throw new ObjectsConstructionException(
					"Couldnt find any resource attribute that is indicates as an Account ID.");
		}

		// NOTE: very problematic! DbUtil returns all keys of maps as lowercase
		// while velo maintain the unique name as uppercase!
		
		// TODO: Dbutil's lowercase issue should get solved in a lower level!
		// (probably in adapter level!)
		if (map.containsKey(accId.getUniqueName())) {
			// logger.info("Loading account by map: " +
			// (String)map.get("username"));
			// TODO: Trimming spaces (should actually trim only the spaces on
			// the end of the username since sometimes users might be for
			// example 'firstname lastname'
			// NOTE: For some reason, sometimes recieced accounts with spaces,
			// then lets trim them.
			// This only happennd in MSSQL EXpress but what if username is built
			// from 'first last' names? with space? I think we should trim only
			// spaces at the end of the line!

			// Make sure that we got a value, if a null returned for an ACCOUNT
			// ID attribute, then throw an exception
			if (map.get(accId.getUniqueName()) == null) {
				throw new ObjectsConstructionException(
						"Couldnt load account, ACCOUNT ID attribute was not found!");
			} else {
				this.setName((String) map.get(accId.getUniqueName()).toString().trim());
			}
		} else {
			throw new ObjectsConstructionException(
					"Cannot load an account by MAP, couldnt find '" + accId.getUniqueName() + "' field which is a MUST");
		}

		//Indicate that the account attributes were loaded manually
		this.setActiveAttributesLoaded(true);

		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			//TODO: validate whether there's an existence resource attribute defined for current iterated row
			
			
			// System.out.println("Starting to iterate over MAP to virtually
			// load accounts attributes");
			Attribute attr = new Attribute();
			Map.Entry pairs = (Map.Entry) it.next();
			// System.out.println("Mapping attribute name: '"+pairs.getKey() +
			// ", with value: '" + pairs.getValue() + "'");
			// Note: important to keep attribute names as UPPERCASE (dbUtil
			// MapListHandler returns all keys as lowercase!)
			// attr.setName(((String)pairs.getKey()).toUpperCase());

			// 24/08/06 -> Since there are JDBC based DBs that are case
			// sensitive (and other systems as well) at the end tweaked
			// DBUtils to return columns as case sensitive, so lets keep it
			// while saving the keys into the loaded attributes keys names

			if ((pairs.getKey() == null)) {
				log.info("Skipping current attribute, the NAME of the attribute is null!");
				continue;
			}
			
			attr.setUniqueName(((String) pairs.getKey()));

			// System.out.println(pairs.getValue());
			// !
			try {
				//sometimes in resources, columns for a certain account might be null of course
				if (pairs.getValue() != null) {
					attr.addValue(pairs.getValue());
				}
				else {
					log.debug("Added an attribute with unique name '" + attr.getUniqueName() + " for account '" + getName() + "' that has no value...'");
				}
				
				//add the attribute
				this.addActiveAttribute(attr);
			} catch (AttributeSetValueException asve) {
				log.info("Could not set value into current iterated attribute, skipping attribute,  failure message: "
								+ asve.getMessage());
				continue;
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	// TO BE TESTED YET
	/**
	 * Load current account properties by multi valued map
	 * 
	 * @param map
	 *            A map containing fields names that correspond to the fields of
	 *            this account entity
	 * @throws EdentityException
	 */
	public void loadAccountByMultiValuedMap(Map<String, ArrayList> map)
			throws EdentityException {
		ResourceAttribute accountIdAttribute = getResource()
				.getAccountIdAttribute();

		if (accountIdAttribute == null) {
			throw new EdentityException(
					"Couldnt find any target system attribute that indicates an ACCOUNT ID.");
		}

		// NOTE: very problematic! DbUtil(JDBD Adapter) returns all keys of maps
		// as lowercase.
		// TODO: Dbutil's lowercase issue should get solved in a lower level!
		// (probably in adapter level!)
		if (map.containsKey(accountIdAttribute.getUniqueName())) {
			// logger.info("Loading account by map: " +
			// (String)map.get("username"));
			// TODO: Trimming spaces (should actually trim only the spaces on
			// the end of the username since sometimes users might be for
			// example 'firstname lastname'
			// NOTE: For some reason, sometimes recieced accounts with spaces,
			// then lets trim them.
			// This only happennd in MSSQL EXpress but what if username is built
			// from 'first last' names? with space? I think we should trim only
			// spaces at the end of the line!

			// Make sure that we got a value, if a null returned for an ACCOUNT
			// ID attribute, then throw an exception
			if (map.get(accountIdAttribute.getUniqueName()) == null) {
				throw new EdentityException(
						"Couldnt load account, ACCOUNT ID was not found....!");
			} else {
				ArrayList valuesOfAttrMapEntry = (ArrayList)map.get(accountIdAttribute.getUniqueName());
				this.setName(valuesOfAttrMapEntry.iterator().next().toString().trim());
			}
		} else {
			throw new EdentityException(
					"Cannot load an account by MAP, couldnt find 'username' field which is a MUST");
		}

		// Important - emulate that the account attributes were loaded
		this.setActiveAttributesLoaded(true);

		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			log.trace("Starting to iterate over MAP to virtually load accounts attributes");
			Attribute attr = new Attribute();
			Map.Entry<String, ArrayList> pairs = (Map.Entry) it.next();
			System.out.println("Mapping attribute name: '" + pairs.getKey()
					+ ", with values of size: '" + pairs.getValue().size()
					+ "'");
			// Note: important to keep attribute names as UPPERCASE (dbUtil
			// MapListHandler returns all keys as lowercase!)
			// attr.setName(((String)pairs.getKey()).toUpperCase());

			// 24/08/06 -> Since there are JDBC based DBs that are case
			// sensitive (and other systems as well) at the end tweaked
			// DBUtils to return columns as case sensitive, so lets keep it
			// while saving the keys into the loaded attributes keys names

			if ((pairs.getKey() == null)) {
				log.info("Skipping current attribute, the UniqueName of the attribute is null!");
				continue;
			}

			attr.setUniqueName(((String) pairs.getKey()));

			// Map values
			try {
				for (Object currValue : pairs.getValue()) {
					attr.addValue(currValue);
				}

				addActiveAttribute(attr);
			} catch (AttributeSetValueException asve) {
				log.info("Could not set value into current iterated attribute, skipping attribute,  failure message: "
								+ asve.getMessage());
				continue;
			}
		}
	}

	
	
	
	
	
	
	
	
	@Transient
	@Deprecated //DOES NOT HANDLE ROLES ASSOC TO POSITIONS!
	public boolean isAnyUserRoleProtectsAccount(
			Collection<Role> forcedUnprotectRoleList) {
		if (getUser() == null) {
			return false;
		}

		boolean isProtected = false;

		StringBuffer sb = new StringBuffer();
		sb.append("Role(s) that protects the account are: ");

		// handle null UnprotectedRoleList
		if (forcedUnprotectRoleList == null) {
			forcedUnprotectRoleList = new HashSet<Role>();
		}

		for (UserRole currUserRole : getUser().getUserRoles()) {
			// If current iterated userRole->role is one of the
			// unprotectRoleList then continue to the next role...
			boolean currRoleIsUnprotected = false;
			for (Role currUnprotectThisRole : forcedUnprotectRoleList) {
				if (currUserRole.getRole().getRoleId().equals(
						currUnprotectThisRole.getRoleId())) {
					currRoleIsUnprotected = true;
				}
				if (currRoleIsUnprotected) {
					break;
				}
			}

			// The role is forced to be unportected, continue to the next
			// UserRole
			if (currRoleIsUnprotected) {
				continue;
			}

			if (currUserRole.getRole().isResourceAssociated(getResource())) {
				sb.append(currUserRole.getRole().getName() + ", ");
				isProtected = true;
			}
		}

		if (isProtected) {
			// throw new UserRoleProtectsAccountException(sb.toString());
			return true;
		}

		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Account) {
			Account entity = (Account) obj;
			return this.accountId == entity.getAccountId();
		} else {
			return false;
		}
	}
	*/

	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Get an account attribute by the specified name
	 * 
	 * @param attrName
	 *            The name of the account attribute to get
	 * @return An attribute object from the accountList that correspond to the
	 *         specified name
	 * @throws AttributeNotFound
	 */
	@Transient
	@Deprecated
	public Attribute getAccountAttributeDUP(String attrName)
			throws AttributeNotFound {
		// System.out.println(getAccountAttributes());

		if (!isActiveAttributesLoaded()) {
			throw new AttributeNotFound(
					"Accounts Attributes were not loaded yet, cannot load account attribute name: "
							+ attrName
							+ ", call account.loadAccountAttributes() first.");
		}

		// DEBUG
		/*
		 * for (Attribute curr : getAccountAttributes().values()) {
		 * System.out.println("name: |"+curr.getName()+"|");curr.getName();
		 * System.out.println("size: |"+curr.getName().length()+"|"); }
		 */

		if (!getActiveAttributes().containsKey(attrName)) {
			throw new AttributeNotFound("Could not get attribute for name: "
					+ attrName
					+ ", this attribute was not found for account name: "
					+ this.getName());
		} else {
			return getActiveAttributes().get(attrName);
		}
	}

	

	/**
	 * Get the status of the account (Whether it's enabled/disabled/[anything
	 * else]) <i>Loaded automatically by AccountBean EJB methods</i>
	 * 
	 * @return The status of the account as a string
	 */
	@Transient
	@Deprecated
	public String getStatus() {
		return status;
	}

	/**
	 * Set the status of the account (Whether it's enabled/disabled/[anything
	 * else]) <i>Loaded automatically by AccountBean EJB methods</i>
	 * 
	 * @param status
	 *            The status of the account to set as a string
	 */
	@Transient
	@Deprecated
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	@Transient
	public Account clone() {
		try {
			Account clonedEntity = (Account) super.clone();
			clonedEntity.setUser(getUser().clone());

			// TODO: SHOULD CLONE ALL OF ITS OBJECT REFERENCES!!!
			clonedEntity.setResource(getResource().clone());

			return clonedEntity;
		} catch (CloneNotSupportedException cnfe) {
			System.out.println("Couldnt clone class: "
					+ this.getClass().getName() + ", with exception message: "
					+ cnfe.getMessage());
			return null;
		}
	}
	
	
	
	
	
	public void initNameByAttribute() {
		ResourceAttribute accId = getResource().getAccountIdAttribute();
		Attribute activeAttrCorresToAccId = getActiveAttributes().get(accId.getUniqueNameInRightCase());
		
		System.out.println("!!!!!!!!!!!!!!!!!: " + activeAttrCorresToAccId);
		
		for (Attribute currAttr : getAttributes().values()) {
			System.out.println("\t\t" + currAttr.getDisplayable());
		}

		//TODO: WHAT HAPPENS IF COULDN'T FIND ACC ID?!, currently will throw NPE somewhere coz of that! proibably reconcile!	
		setName(activeAttrCorresToAccId.getFirstValueAsString().trim());
	}
	
	
	

}