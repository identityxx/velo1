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
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
import velo.exceptions.OperationException;
import velo.exceptions.ScriptInvocationException;
import velo.exceptions.ScriptLoadingException;

/**
 * @author Asaf Shakarchi
 *
 */
@Table(name = "VL_ACCOUNT")
@Entity
@SequenceGenerator(name="AccountIdSeq",sequenceName="ACCOUNT_ID_SEQ")
//todo: support case senstivie/non case-sensitive in finding accounts
@NamedQueries( {
		@NamedQuery(name = "account.findByName", query = "SELECT object(account) FROM Account account WHERE account.name = :accountName AND account.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "account.findByNameCaseNonSensitive", query = "SELECT object(account) FROM Account account WHERE upper(account.name) = upper(:accountName) AND account.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "account.findOnResourceForUser", query = "SELECT object(account) from Account account WHERE account.resource = :resource AND account.user = :user"),
		@NamedQuery(name = "account.isAccountExistOnResourceByResourceUniqueName", query = "SELECT count(account) FROM Account account WHERE UPPER(account.name) = :accountName AND account.resource.uniqueName = :resourceUniqueName"),
		
		
		
		
		
		
		
		@NamedQuery(name = "isAccountExistOnTarget", query = "SELECT count(account) FROM Account AS account WHERE account.name = :accountName AND account.resource = :resource"),
		@NamedQuery(name = "isStaleAccountExistsOnTarget", query = "SELECT count(account) FROM Account AS account WHERE account.name = :accountName AND account.resource = :resource AND account.user is null"),
		
		@NamedQuery(name = "account.searchByString", query = "SELECT object(account) from Account account WHERE UPPER(account.name) like :searchString")
})
		
public class Account extends AccountSkeletal {
	private static transient Logger log = Logger.getLogger(Account.class.getName());

	public static Account factory(String accountName, Resource resource) {
		
		Account account = new Account();
		account.setName(accountName);
		account.setResource(resource);
		account.setCreationDate(new Date());

		return account;
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
	// Builds virtual values upon UserAttributes/resourceAttributes since Velo
	// doesn't keep the account's attributes
	private HashMap<String, Attribute> transientAttributes = new HashMap<String, Attribute>();
	// Transient, whether the attributes for this account were loaded or not
	private boolean transientAttributesLoaded;

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
	
	
	
	
	
	
	
	
	
	
	
	

	// TRANSIENT/HELPER
	/**
	 * Add an attribute to the AccountAttribute map
	 * 
	 * @param attribute
	 *            An attribute object to add
	 * @see #getAccountAttributes()
	 */
	public void addTransientAttribute(Attribute attribute) {
		// system.finest("Adding attribute name: " + attribute.getName() + ",
		// with value: " + attribute.getStringValue() + ", with type: " +
		// attribute.getType());
		getTransientAttributes().put(attribute.getUniqueName(), attribute);
	}

	/**
	 * Get all account attributes as a MAP Note: these account attributes must
	 * be loaded by some logical method (probably written in the corresponding
	 * 'account' EJB)
	 * 
	 * @return a HashMap containing all account attributes
	 */
	@Transient
	public HashMap<String, Attribute> getTransientAttributes() {
		return transientAttributes;
	}
	
	public void setTransientAttributes(HashMap<String, Attribute> transientAttributes) {
		this.transientAttributes = transientAttributes;
	}

	/**
	 * Set whether the transient attributes were loaded or not
	 * 
	 * @param isAccountAttributesLoaded
	 *            true/false boolean upon loaded/not loaded
	 */
	@Transient
	public void setTransientAttributesLoaded(boolean transientAttributesLoaded) {
		this.transientAttributesLoaded = transientAttributesLoaded;
	}

	/**
	 * Return a boolean, indicate whether the transient attributes were loaded
	 * or not
	 * 
	 * @return true/false upon loaded/not loaded
	 */
	@Transient
	public boolean isTransientAttributesLoaded() {
		return transientAttributesLoaded;
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
		this.setTransientAttributesLoaded(true);

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

				addTransientAttribute(attr);
			} catch (AttributeSetValueException asve) {
				log.info("Could not set value into current iterated attribute, skipping attribute,  failure message: "
								+ asve.getMessage());
				continue;
			}
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

		ResourceAttribute ra = getResource().findAttributeByName(resourceAttrUniqueName);
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
		context.addVar("attrs", getTransientAttributes());
		
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
			for (AttributeValue val : ra.getTransientValues()) {
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
			
			
			log.trace("The ResourceAttribute has values from 'resource attribute level' with amount '" + ra.getTransientValues().size() + "', VALUES WILL BE OVERRIDED BY CURRENT ITERATED RULE!");
			
			if (resAttr.isFirstValueIsNotNull()) {
				ra.setTransientValues(resAttr.getTransientValues());
			}
		}
		
		
		
		
		return ra;
	}

	
	//roles to add might be null in case delete/modify account is using this method as now 'modify account' does not support modify attributes
	@Transient
	public HashMap<String, Attribute> getVirtualAttributes(Set<Role> rolesToAdd) {
		//better to clean all resource attributes as this resource object might be in use already in the past and its attributes might already loaded with values by this method in the past
		getResource().clearTransientAttributeValues();
		
		
		log.debug("----START OF: GETVIRTUALATTRIBUTES()-----");
		if (isTransientAttributesLoaded()) {
			return getTransientAttributes();
		}
		else {
			setTransientAttributesLoaded(true);
			
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
					getTransientAttributes().put(resourceAttribute.getUniqueName(), resourceAttribute);
				} catch (LoadingVirtualAccountAttributeException e) {
					log.warn("Failed to load virtual attribute, skipping to the next attribute: " + e.toString());
					continue;
				}
			}
		}
		
		log.debug("----END OF: GETVIRTUALATTRIBUTES()-----");
		return getTransientAttributes();
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
		log.trace("Constructing a DSML SPML Attribute from Velo Virtual Account Attribute named '" + virtualAccountAttribute.getUniqueName() + "', with amount of values: '" + virtualAccountAttribute.getTransientValues().size());
		
		//warning: this.getVirtualAttribute() set a default first value element for resource attributes that has no default mappings
		//if the integrator has failed to set a value, then here a DSMLValue with null value will be constructed
		//This will result a NPE when the request gets marshalled, then such attributes must be skipped here.
		//If they are required, the request will fail via the resource XSD schema validation. 
		
		if (!virtualAccountAttribute.isFirstValueIsNotNull()) {
			log.info("Could not construct a DSML SPML attribute since the specified virtual account attribute has no values...");
			return null;
		}
		
		DSMLValue[] values = new DSMLValue[virtualAccountAttribute.getTransientValues().size()];
		
		int i=0;
		for (AttributeValue currValue : virtualAccountAttribute.getTransientValues()) {
			values[i] = new DSMLValue(currValue.getValue());
		}
		
		DSMLAttr attr = new DSMLAttr(virtualAccountAttribute.getUniqueName(),values);
		
		return attr;
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
	public Attribute getAccountAttribute(String attrName)
			throws AttributeNotFound {
		// System.out.println(getAccountAttributes());

		if (!isTransientAttributesLoaded()) {
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

		if (!getTransientAttributes().containsKey(attrName)) {
			throw new AttributeNotFound("Could not get attribute for name: "
					+ attrName
					+ ", this attribute was not found for account name: "
					+ this.getName());
		} else {
			return getTransientAttributes().get(attrName);
		}
	}

	/**
	 * Load current account properties by a map (Mostly used by the Reconcile
	 * process in order to load 'virtual accounts' per listed account on the
	 * system) (USE 'Account EJB -> loadAccountAttributes' method instead!)
	 * 
	 * @param map
	 *            A map containing fields names that correspond to the fields of
	 *            this account entity
	 * @throws EdentityException
	 * @deprecated
	 */
	@Deprecated
	//clean and remove deprecated!
	//used by 'ActiveaccountsConstruction' to build an account entity based on the attriutes recieved by query/xml/ldap/etc...
	public void loadAccountByMap(Map map) throws ObjectsConstructionException {
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
					"Couldnt find any target system attribute that indicates an ACCOUNT ID.");
		}

		// NOTE: very problematic! DbUtil returns all keys of maps as lowercase
		// while we always work with uppercase!
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
						"Couldnt load account, ACCOUNT ID was not found....!");
			} else {
				this.setName((String) map.get(accId.getUniqueName()).toString().trim());
			}
		} else {
			throw new ObjectsConstructionException(
					"Cannot load an account by MAP, couldnt find '" + accId.getUniqueName() + "' field which is a MUST");
		}

		// Important - emulate that the account attributes were loaded
		this.setTransientAttributesLoaded(true);

		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
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
				this.addTransientAttribute(attr);
			} catch (AttributeSetValueException asve) {
				log.info("Could not set value into current iterated attribute, skipping attribute,  failure message: "
								+ asve.getMessage());
				continue;
			}
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


}