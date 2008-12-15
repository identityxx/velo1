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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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
public class SyncTargetDataFileGenerator {
    private List<Account> accounts = new ArrayList<Account>();
    private List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
    
    /**
     @param accounts The accounts to set.
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    
    /**
     @return Returns the accounts.
     */
    public List<Account> getAccounts() {
        return accounts;
    }
    
    
    /**
     @param groups The groups to set.
     */
    public void setGroups(List<ResourceGroup> groups) {
        this.groups = groups;
    }
    
    
    /**
     @return Returns the groups.
     */
    public List<ResourceGroup> getGroups() {
        return groups;
    }
    
    
    public void generateXmlDataSyncFile(Resource resource, String fileName) throws IOException {
        OutputStream fout= new FileOutputStream(fileName);
        OutputStream bout= new BufferedOutputStream(fout);
        OutputStreamWriter out = new OutputStreamWriter(bout, "UTF-8");
        
        out.write("<?xml version=\"1.0\" ");
        out.write("standalone=\"yes\"?>\r\n");
        out.write("<syncList>\r\n");
        out.write("<resource uniqueName='" + resource.getUniqueName() + "'>\r\n");
        out.write("<accounts>\r\n\t");
        for (Account currAccount : getAccounts()) {
            out.write("<account name='" + currAccount.getName() + "'>\r\n\t\t");
            out.write("<attributes>\r\n\t\t\t");
            for (Attribute currAttr : currAccount.getTransientAttributes().values()) {
                out.write("<attribute name='" + currAttr.getUniqueName() + "'>\r\n\t\t\t\t");
                out.write("<values>\r\n\t\t\t\t\t");
                //might throw a NPE if no values were found
                out.write("<value>" + currAttr.getFirstValue().getAsString() + "</value>\r\n\t\t\t\t");
                out.write("</values>\r\n\t\t\t");
                out.write("</attribute>\r\n\t\t");
            }
            out.write("</attributes>\r\n\t");
            out.write("</account>\r\n");
        }
        out.write("</accounts>\r\n");
        
        //Groups
        out.write("<groups>\r\n\t");
        for (ResourceGroup currGroup : getGroups()) {
            out.write("<group uniqueId='" + currGroup.getUniqueId() + "' displayName='" + currGroup.getDisplayName() + "' description='" + currGroup.getDescription() + "'/>\r\n");
        }
        out.write("</groups>\r\n");
        
        out.write("</resource>\r\n");
        out.write("</syncList>\r\n");
        
        out.flush();  // Don't forget to flush!
        out.close();
    }
}