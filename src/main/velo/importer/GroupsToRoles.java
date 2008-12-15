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
package velo.importer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import velo.ejb.interfaces.RoleManagerRemote;
import velo.exceptions.OperationException;


public class GroupsToRoles {
    private static final long serialVersionUID = 1987305452306161213L;
    private static RoleManagerRemote roleManager;
    
    public GroupsToRoles(RoleManagerRemote roleManager) {
        this.roleManager = roleManager;
    }
    
    public final void execute(String fileName) throws OperationException {
        try {
            java.lang.System.out.println("Importing Groups to Roles assoication, import data from XML file named: '" + fileName + "'");
            
            java.io.File f = new java.io.File(fileName);
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(f);
            
            NodeList nodes = doc.getElementsByTagName("group");
            for (int i = 0; i < nodes.getLength(); i++) {
                try {
                    Element element = (Element) nodes.item(i);
                    
                    String uniqueId = element.getAttribute("uniqueId");
                    String targetName = element.getAttribute("target");
                    String roleName = element.getAttribute("role");
                    
                    if (uniqueId.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since uniqueID in XML row is empty");
                        continue;
                    }
                    if (roleName.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since Role Name in XML row is empty");
                        continue;
                    }
                    if (targetName.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since target name in XML row is empty");
                        continue;
                    }
                    
                    roleManager.associateGroupToRole(uniqueId, targetName, roleName);
                    
                    System.out.println("Successfully associated group ID '" + uniqueId + "', related to target '" + targetName + "', to role: '" + roleName + "'");
                } catch (Exception oe) {
                    System.err.println("Could not perform Group To Role assoication: '" + oe.getMessage() + "'");
                    continue;
                }
            }
        } catch (Exception e) {
            throw new OperationException(e);
        }
    }
}
