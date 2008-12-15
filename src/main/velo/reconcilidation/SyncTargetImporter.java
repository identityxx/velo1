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
package velo.reconcilidation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.xml.sax.SAXException;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.AttributeValue;
import velo.exceptions.SyncListImporterException;

public class SyncTargetImporter {
    File syncFile;
    
    public SyncTargetImporter(String fileName) {
        syncFile = new File(fileName);
    }
    
    public SyncTargetData getSyncedData() throws SyncListImporterException {
        Digester d = new Digester();
        //URL url = getClass().getResource("/qflow_sync.xml");
        
        //File f = new File(url.getFile());
        try {
            SyncTargetData tsList = new SyncTargetData();
            d.push(tsList);
            addRules(d);
            d.parse(syncFile);
            
                        /*
                        for (resource currTs : tsList.getResources()) {
                                System.out.println("Parsed resources short name: " + currTs.getShortName());
                         
                                for (Account currAcc : currTs.getAccounts()) {
                                        System.out.println("Found account name: " + currAcc.getName());
                         
                                        Map<String,Attribute> attrs = currAcc.getAccountAttributes();
                                        System.out.println("Size of account's attributes: " + attrs.size());
                                        for (Map.Entry pairs : attrs.entrySet()) {
                                                Attribute currAttr = (Attribute)pairs.getValue();
                                                try {
                                                        System.out.println("Attr name: " + pairs.getKey() + ", value: " + currAttr.getValueObject().getStringValue());
                                                }
                                                catch (MultipleAttributeValueVaiolation mvv) {
                                                        mvv.printStackTrace();
                                                }
                                        }
                                }
                        }
                         */
            
            return tsList;
        } catch (SAXException saxe) {
            throw new SyncListImporterException("There was a SAX exception while trying to import the sync XML file, failed with message: " + saxe.getMessage());
        } catch (IOException ioe) {
            throw new SyncListImporterException("Cannot import sync list file, an IO exception has occured, failed with message: " + ioe.getMessage());
        }
    }
    
    
    
    /**
     @param d The digester object to add the rules for
     */
    public static void addRules(Digester d) {
        d.setValidating(false);
        d.setRules(new ExtendedBaseRules());
        
        //When we encounter an "account" tag, create an instance of class
        // 'Account' and push it on the digester stack of objects. After
        // doing that, call addAccount on the second-to-top object on the
        // digester stack (a "resource" object), passing the top object on
        // the digester stack (the "Account" object). And also set things
        // up so that for each child xml element encountered between the start
        // of the account tag and the end of the account tag, the text
        // contained in that element is passed to a setXXX method on the
        // Address object where XXX is the name of the xml element found.
        d.addObjectCreate("syncList/resource/accounts/account", Account.class);
        d.addSetProperties("syncList/resource/accounts/account");
        d.addSetNext("syncList/resource/accounts/account", "addAccount");
        //d.addSetNestedProperties("syncList/resource/account");
        d.addCallMethod("syncList/resource/accounts/account/name", "setName",0);
        
        
        d.addObjectCreate("syncList/resource/accounts/account/attributes/attribute", Attribute.class);
        //set the properties automatically, which currently the only interesting part is the 'uniqueName' of the attribute
        d.addSetProperties("syncList/resource/accounts/account/attributes/attribute");
        d.addSetNext("syncList/resource/accounts/account/attributes/attribute", "addTransientAttribute");
        //d.addSetNestedProperties("syncList/resource/account");
        //d.addCallMethod("syncList/resource/accounts/account/attributes/attribute/name", "setUniqueName",0);
        
        d.addObjectCreate("syncList/resource/accounts/account/attributes/attribute/values/value", AttributeValue.class);
        d.addCallMethod("syncList/resource/accounts/account/attributes/attribute/values/value", "setValue", 0);
        //d.addSetProperties("syncList/resource/accounts/account/attributes/attribute/values/value");
        d.addSetNext("syncList/resource/accounts/account/attributes/attribute/values/value", "addValue");
        
        d.addObjectCreate("syncList/resource/groups/group", ActiveGroup.class);
        d.addSetProperties("syncList/resource/groups/group");
        d.addSetNext("syncList/resource/groups/group", "addGroup");
        //addSetProperties handles all of these :)
        //HUH?d.addCallMethod("syncList/resource/groups/group/displayName", "setDisplayName",0);
        //HUH?d.addCallMethod("syncList/resource/groups/group/uniqueId", "setUniqueId",0);
        //HUH?d.addCallMethod("syncList/resource/groups/group/description", "setDescription",0);
        
        
        d.addObjectCreate("syncList/resource/groupsMemberShip/group", ActiveGroup.class);
        d.addSetProperties("syncList/resource/groupsMemberShip/group");
        d.addSetNext("syncList/resource/groupsMemberShip/group", "addGroupForMembership");
        d.addObjectCreate("syncList/resource/groupsMemberShip/group/member", Account.class);
        d.addSetProperties("syncList/resource/groupsMemberShip/group/member", "id","name");
        d.addSetNext("syncList/resource/groupsMemberShip/group/member", "addMemberForSync");
    }
    
    
    
    
    
    public static void main(String[] args) throws SyncListImporterException{
        SyncTargetImporter syncImporter = new SyncTargetImporter("C:/temp/velo/velo_ws/targets_scripts/asdf/sync_files/asdf_sync.xml");
        SyncTargetData syncData = syncImporter.getSyncedData();
        
        System.out.println("Parsed accounts amount: " + syncData.getAccounts().size());
        System.out.println("Parsed groups amount: " + syncData.getGroups().size());
        for (Account currAccount : syncData.getAccounts()) {
        	System.out.println("Printing attributes for account name '" + currAccount.getName() + "'");
            for (Map.Entry<String,Attribute> currAttr : currAccount.getTransientAttributes().entrySet()) {
            	System.out.println(currAttr.getValue().getDisplayable());
            }
        }
    }
}
