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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import velo.exceptions.AttributeSetValueException;

/**
 * An entity that represents a persistence account attribute
 *
 * @author Asaf Shakarchi
 */
@Table(name="VL_ACCOUNT_ATTRIBUTE")
@Entity
@SequenceGenerator(name="AccountAttributeIdSeq",sequenceName="ACCOUNT_ATTRIBUTE_ID_SEQ")
public class AccountAttribute extends BaseEntity implements Serializable, Cloneable {
	private static transient Logger log = Logger.getLogger(AccountAttribute.class.getName());

	private static final long serialVersionUID = 1987305452306161213L;

	private long accountAttributeId;


	/**
	 * The account owner of this attribute
	 */
	private Account account;

	/**
	 * The Resource Attribute definition
	 */
	private ResourceAttribute resourceAttribute;

	private List<AccountAttributeValue> values = new ArrayList<AccountAttributeValue>();

	
	public AccountAttribute() {
		
	}
	
	public AccountAttribute(Account account, ResourceAttribute resourceAttribute) {
		setAccount(account);
		setResourceAttribute(resourceAttribute);
	}
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO,generator="AccountAttributeIdSeq")
	@Column(name="ACCOUNT_ATTRIBUTE_ID")
	public long getAccountAttributeId() {
		return accountAttributeId;
	}


	public void setAccountAttributeId(long accountAttributeId) {
		this.accountAttributeId = accountAttributeId;
	}

	
	@ManyToOne(optional=false)
	@JoinColumn(name="ACCOUNT_ID", nullable=false)
	public Account getAccount() {
		return account;
	}


	public void setAccount(Account account) {
		this.account = account;
	}
	
	
	@ManyToOne(optional=false)
	@JoinColumn(name="RESOURCE_ATTRIBUTE_ID", nullable=false)
	public ResourceAttribute getResourceAttribute() {
		return resourceAttribute;
	}


	public void setResourceAttribute(ResourceAttribute resourceAttribute) {
		this.resourceAttribute = resourceAttribute;
	}


	public void setValues(List<AccountAttributeValue> values) {
		this.values = values;
	}


	//@OneToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE, CascadeType.REMOVE}, mappedBy="userIdentityAttribute", fetch=FetchType.EAGER)
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy="accountAttribute", fetch=FetchType.LAZY)
	public List<AccountAttributeValue> getValues() {
		return values;
	}
	
	
	
	
	
	//This method is required for attribute comparations used mostly by Reconcile. 
	@Transient
	public Attribute getAsStandardAttribute() {
		//Attribute attr = new Attribute(getUniqueName(), getDisplayName(), getDescription(), getResourceAttribute().getDataType(),getResourceAttribute().isRequired(), getResourceAttribute().isManaged(), getResourceAttribute().getMinLength(), getMaxLength(), getMinValues(), getMaxValues());
		
		//RISKY! because objects passes by reference
		//Attribute attr = getResourceAttribute();
		Attribute attr = new Attribute();
		attr.setDataType(getResourceAttribute().getDataType());
		attr.setDisplayName(getResourceAttribute().getDisplayName());
		attr.setUniqueName(getResourceAttribute().getUniqueName());
		//FINISH......
		
		for (AccountAttributeValue aav : getValues()) {
			attr.addValue(aav);
		}
		
		return attr;
	}
	
	
	
	
	
	




	//PROXY methods of RA
	@Transient
	public String getDisplayName() {
		return getResourceAttribute().getDisplayName();
	}
	
	@Transient
	public String getUniqueName() {
		return getResourceAttribute().getUniqueName();
	}

	@Transient
	public String getDescription() {
		return getResourceAttribute().getDescription();
	}
	
	
	
	

	//helper
	public void importValues(Collection<AttributeValue> values) throws AttributeSetValueException {
		for (AttributeValue currAV : values) {
			getValues().add(AccountAttributeValue.factory(this, currAV));
		}
	}
	
	@Transient
	public List<AttributeValue> getValuesAsStandardValues() {
		List<AttributeValue> standardValues = new ArrayList<AttributeValue>();
		for (AccountAttributeValue currAV : getValues()) {
			standardValues.add((AttributeValue)currAV);
		}
		
		return standardValues;
	}
	
	public static AccountAttribute factory() {
		AccountAttribute uia = new AccountAttribute();
		uia.setCreationDate(new Date());

		return uia;
	}

	public static AccountAttribute factory(ResourceAttribute ra, Account account) {
		AccountAttribute aa = factory();
		aa.setResourceAttribute(ra);
		aa.setAccount(account);
		//RISKY: WITHOUT THIS: uia.factoryValue();

		return aa;
	}
	
	
	
	//Used by 'account.initFullAccountByLoadedActiveAttributes' to create accountAttribute based on activeAttribute
	public static AccountAttribute factory(ResourceAttribute ra, Account account, Attribute attribute) throws AttributeSetValueException {
		AccountAttribute accAttr = factory(ra,account);
		
		//FIXME: Aint there a nicer way as both are 'AttributeValue' types?
		for (AttributeValue val : attribute.getValues()) {
			accAttr.factoryValue().setValue(val.getValueAsObject());
		}
		
		return accAttr;
	}
	
	
	public static AccountAttribute factory(ResourceAttribute ra, Account account, Object values) throws AttributeSetValueException {
		AccountAttribute accAttr = factory(ra,account);
		
		if (values != null) {
			if (values instanceof Collection) {
				Collection valueCol = (Collection) values;
				for (Object currValue : valueCol) {
					accAttr.factoryValue().setValue(currValue);
				}
			} else {
				//'addValue' will take care of identifying the right type
				accAttr.factoryValue().setValue(values);
			}
		}
		
		return accAttr;
	}
	

	
	public AccountAttributeValue factoryValue() {
		AccountAttributeValue aav = AccountAttributeValue.factory(getResourceAttribute().getDataType());
		aav.setAccountAttribute(this);
		this.getValues().add(aav);

		return aav;
	}
}
