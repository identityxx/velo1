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
package velo.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.ecs.StringElement;
import org.apache.ecs.filter.CharacterFilter;
import org.apache.ecs.xml.XML;
import org.apache.ecs.xml.XMLDocument;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.entity.ResourceGroup;


/**
 * Resource Active Data XML Generator
 * @author Asaf Shakarchi
 * @Deprecated
 * @see velo.resource.SyncDataXmlGEnerator
 */
public class SyncTargetGenerator {
	private static Logger logger = Logger.getLogger(SyncTargetGenerator.class.getName());
	Resource resource;
	private List<Account> accounts = new ArrayList<Account>();
	private List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
	
	
	//public SyncTargetGenerator() {
	public SyncTargetGenerator(Resource resource) {
		this.resource = resource;
		
		/*
		resource = new resource();
		resource.setShortName("QFLOW");
		Account acc = new Account();
		acc.setName("ashakarchi");
		
		Attribute attr1 = new Attribute();
		attr1.setName("firstName");
		AttributeValue av1 = new AttributeValue();
		av1.setValueAsString("Asaf");
		attr1.addValueObject(av1);
		
		Attribute attr2 = new Attribute();
		attr2.setName("lastName");
		AttributeValue av2 = new AttributeValue();
		av2.setValueAsString("Shakarchi");
		attr2.addValueObject(av2);
		
		acc.addAccountAttribute(attr1);
		acc.addAccountAttribute(attr2);
		
		accounts.mapObject(acc);
		*/
	}
	
	
	public void addAccount(Account account) {
		getAccounts().add(account);
	}
	
	public String dumpAsString() {
		logger.info("Starting to dump SYNC XML file for Target System name: '" + resource.getDisplayName() + "'");
		logger.info("XML Sync file should be created with accounts amount: '" + getAccounts().size() + ", and groups amount: '" + getGroups().size() + "'");
	//try {
		XMLDocument document = new XMLDocument();
		//document.setCodeset("UTF-8");
		//This filter escape speacial chars in XML such as *<>, etc...
		CharacterFilter cf = new CharacterFilter ();
		
		XML listElement = new XML("syncList");
		XML targetElement = new XML("resource");
		targetElement.setAttributeFilter(cf);
		targetElement.addAttribute("uniqueName",resource.getUniqueName());
		listElement.addElement(targetElement);
		
		XML accountsElement = new XML("accounts");
		logger.info("Adding Accounts elements to XML file...");
		for (Account currAccount : getAccounts()) {
			XML currAccountElement = new XML("account");
			currAccountElement.setAttributeFilter(cf);
			currAccountElement.addAttribute("name",currAccount.getName());
			XML currAccountAttributesElement = new XML("attributes");
			currAccountAttributesElement.setAttributeFilter(cf);
			
			currAccountElement.addElement(currAccountAttributesElement);
			
			for (Attribute currAttr : currAccount.getTransientAttributes().values()) {
				XML currAccountAttributeElement = new XML("attribute");
				currAccountAttributeElement.setAttributeFilter(cf);
				
				currAccountAttributesElement.addElement(currAccountAttributeElement);
				currAccountAttributeElement.addAttribute("name",currAttr.getUniqueName());
				
				XML currAccountAttributeValuesElement = new XML("values");
				currAccountAttributeValuesElement.setAttributeFilter(cf);
				
				XML currAccountAttributeValueElement = new XML("value");
				
				
				currAccountAttributeValueElement.setAttributeFilter(cf);
				currAccountAttributeValueElement.setFilter(cf);
				currAccountAttributeValueElement.setFilterState(true);
				
				//String currAttrValue = currAttr.getAsString();
				//String utfValue = new String(currAttrValue.getBytes(),"UTF-8");
				//currAccountAttributeValueElement.setTagText(utfValue);
				

				//Not anymore, needs filter, created an element. currAccountAttributeValueElement.setTagText(currAttr.getValueAsString());
				StringElement ge = new StringElement();
				ge.setTagText(currAttr.getFirstValue().getAsString());
				currAccountAttributeValueElement.addElement(ge);
				
				
				
				currAccountAttributeValuesElement.addElement(currAccountAttributeValueElement);
				
				currAccountAttributeElement.addElement(currAccountAttributeValuesElement);
				
				//currAccountAttributeValueElement.addAttribute("value")
				//accountAttributeElement.addElement()
				//currAccountElement.addElement(accountAttributeValueElement);
				
				//currAccountAttributeElement.addAttribute("")
				//currAttr.getName()
				//currAttr.getStringValue());
			}
			
			accountsElement.addElement(currAccountElement);
		}
		
		//targetElement("asdf");
		targetElement.addElement(accountsElement);
		
		logger.info("Adding Groups elements to XML file...");
		XML groupsElement = new XML("groups");
		for (ResourceGroup currGroup : getGroups()) {
			XML currGroupElement = new XML("group");
			currGroupElement.setAttributeFilter(cf);
			currGroupElement.addAttribute("displayName",currGroup.getDisplayName());
			currGroupElement.addAttribute("uniqueId",currGroup.getUniqueId());
			if (currGroup.getDescription() != null) {
				currGroupElement.addAttribute("description",currGroup.getDescription());
			}
			else {
				currGroupElement.addAttribute("description","");
			}
			groupsElement.addElement(currGroupElement);
		}
		targetElement.addElement(groupsElement);
		
		document.addElement(listElement);
		//document.output(System.out);
		//document.setCodeset("UTF-8");
		return document.toString();
		//return null;
		
		//StringBuffer sb = new StringBuffer();
		//addLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		//addLine("<list>");
		//addLine("<resource uniqueName="QFLOW">")
	/*
	}
	catch (UnsupportedEncodingException uee) {
		System.out.println("SYNC TARGET GENERATOR SHIT!!!!!!!");
		uee.printStackTrace();
		return null;
	}
	*/
	}

	/**
	 * @param accounts The accounts to set.
	 */
	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	/**
	 * @return Returns the accounts.
	 */
	public List<Account> getAccounts() {
		return accounts;
	}


	/**
	 * @param groups The groups to set.
	 */
	public void setGroups(List<ResourceGroup> groups) {
		this.groups = groups;
	}


	/**
	 * @return Returns the groups.
	 */
	public List<ResourceGroup> getGroups() {
		return groups;
	}
	
	public void addGroup(ResourceGroup group) {
		getGroups().add(group);
	}
	
	/*
	public static void main(String[] args) {
		SyncTargetGenerator sg = new SyncTargetGenerator();
		System.out.println(sg.dumpToXml());
	}
	*/
	
}
