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


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.entity.ResourceGroup;

import com.generationjava.io.xml.PrettyPrinterXmlWriter;
import com.generationjava.io.xml.SimpleXmlWriter;
import com.generationjava.io.xml.XmlWriter;

public class SyncDataXmlGenerator {
	private static Logger log = Logger.getLogger(SyncDataXmlGenerator.class.getName());
	Resource resource;
	private List<Account> accounts = new ArrayList<Account>();
	private List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
	
	
	//public SyncTargetGenerator() {
	public SyncDataXmlGenerator(Resource resource) {
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
	
	public String dumpXmlAsString() throws IOException {
		log.info("Starting to dump SYNC XML file for Target System name: '" + resource.getDisplayName() + "'");
		log.info("XML Sync file should be created with accounts amount: '" + getAccounts().size() + ", and groups amount: '" + getGroups().size() + "'");
	
                
                Writer writer1 = new java.io.StringWriter();
                XmlWriter xmlwriter1 = new SimpleXmlWriter(writer1);
                PrettyPrinterXmlWriter  pxr = new PrettyPrinterXmlWriter(xmlwriter1);
                pxr.setIndent("\t");
                pxr.setNewline("\n");
                pxr.writeXmlVersion("1.0", null,"yes")
                    .writeEntity("syncList")
                        .writeEntity("resource").writeAttribute("uniqueName",resource.getUniqueName())
                            .writeEntity("accounts");
                                for (Account currAccount : getAccounts()) {
                                pxr.writeEntity("account").writeAttribute("name", currAccount.getName())
                                    .writeEntity("attributes");
                                    for (Attribute currAttr : currAccount.getTransientAttributes().values()) {
                                    	if (currAttr.getFirstValue() == null) {
                                    		log.info("Could not find at least one value for attribute named '" + currAttr.getUniqueName() + "' for account named '" + currAccount.getName() + "', skipping xml attribute generation'");
                                    		//TODO: IS it ok? or should the attribute be dumped without values ?
                                    		continue;
                                    	}
                                        pxr.writeEntity("attribute").writeAttribute("uniqueName",currAttr.getUniqueName())
                                            .writeEntity("values")
                                                .writeEntity("value").writeText(currAttr.getFirstValue().getAsString()).endEntity()
                                            .endEntity()
                                        .endEntity();
                                    }
                                    pxr.endEntity()
                                .endEntity();
                                }
                            pxr.endEntity()
                            .writeEntity("groups");
                                for (ResourceGroup currGroup : getGroups()) {
                                pxr.writeEntity("group").writeAttribute("uniqueId", currGroup.getUniqueId()).writeAttribute("type", currGroup.getType()).writeAttribute("displayName", currGroup.getDisplayName()).writeAttribute("description",currGroup.getDescription()).endEntity();
                                }
                            pxr.endEntity()
                        .endEntity()
                    .endEntity();
                pxr.close();
                return writer1.toString();
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
