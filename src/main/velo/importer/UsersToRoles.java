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

import velo.ejb.interfaces.UserManagerRemote;
import velo.exceptions.OperationException;


public class UsersToRoles {
    private static final long serialVersionUID = 1987305452306161213L;
    private static UserManagerRemote userManager;
    
    public UsersToRoles(UserManagerRemote userManager) {
        this.userManager = userManager;
    }
    
    public final void execute(String fileName) throws OperationException {
        try {
            java.lang.System.out.println("Importing Users to Roles assoication, import data from XML file named: '" + fileName + "'");
            
            java.io.File f = new java.io.File(fileName);
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(f);
            
            NodeList nodes = doc.getElementsByTagName("user");
            for (int i = 0; i < nodes.getLength(); i++) {
                try {
                    Element element = (Element) nodes.item(i);
                    
                    String userName = element.getAttribute("name");
                    String roleName = element.getAttribute("roleName");
                    String isInherited = element.getAttribute("isRoleInherited");
                    
                    if (userName.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since userName in XML row is empty");
                        continue;
                    }
                    if (roleName.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since Role Name in XML row is empty");
                        continue;
                    }
                    if (isInherited.length() < 1) {
                        System.err.println("Skipping row ID (" + i+1 + "), since 'Is Inhreited?' in XML row is empty");
                        
                        continue;
                    }
                    else {
                        if ( (!isInherited.equalsIgnoreCase("true")) && (!isInherited.equalsIgnoreCase("false")) ) {
                            System.err.println("Skipping row ID (" + i+1 + "), since 'isRoleInherited' attribute in XML row must be equal to 'true/false' but its value is '" + isInherited + "'");
                            continue;
                        }
                    }
                    
                    boolean isInheritedBool;
                    if (isInherited.equalsIgnoreCase("true")) {
                        isInheritedBool = true;
                    }
                    else {
                        isInheritedBool = false;
                    }
                    
                    userManager.associateUserToRole(userName, roleName, isInheritedBool);
                    
                    System.out.println("Successfully associated user named '" + userName + "', to role named '" + roleName + "'");
                } catch (Exception oe) {
                    System.err.println("Could not perform User To Role assoication: '" + oe.getMessage() + "'");
                    continue;
                }
            }
        } catch (Exception e) {
            throw new OperationException(e);
        }
    }
}
