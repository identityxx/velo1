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
package velo.utils;

import java.beans.IntrospectionException;
import java.io.IOException;

import org.xml.sax.SAXException;

import velo.ejb.interfaces.RoleManagerRemote;
import velo.exceptions.OperationException;
import velo.exceptions.PersistEntityException;

public class DataImporter {
    private static RoleManagerRemote roleManager;
    
    public DataImporter(RoleManagerRemote roleManager) {
        this.roleManager = roleManager;
    }
    
    public final void execute(String fileName) throws OperationException {
        try     {
            java.lang.System.out.println("Executing Data Importer with file: \'" + fileName + "\'");
            //java.lang.Thread.currentThread().getContextClassLoader().setDefaultAssertionStatus(false);
            //java.lang.System.setProperty("org.omg.CORBA.ORBInitialHost","localhost");
            //javax.naming.InitialContext ic = new javax.naming.InitialContext();
            java.io.File f = new java.io.File(fileName);
            java.lang.String xmlReader = velo.tools.FileUtils.getContents(f);
            
            java.lang.System.out.println(xmlReader);
            org.apache.commons.betwixt.io.BeanReader reader = new org.apache.commons.betwixt.io.BeanReader();
            
            reader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(true);
            reader.getBindingConfiguration().setMapIDs(false);
            reader.registerBeanClass(velo.utils.RolesKeeper.class);
            velo.utils.RolesKeeper bean = (velo.utils.RolesKeeper) reader.parse(new java.io.StringReader(xmlReader));
            velo.entity.Role[] roles = bean.getRoles();
            
            java.lang.System.out.println("Roles size: " + roles.length);
            for (int i = 0; i < roles.length; i++) {
                roleManager.createRole(roles[i]);
            }
        } catch (PersistEntityException ex) {
            throw new OperationException(ex);
        } catch (IOException ex) {
            throw new OperationException(ex);
        } catch (SAXException ex) {
            throw new OperationException(ex);
        } catch (IntrospectionException ex) {
            throw new OperationException(ex);
        } //catch (NamingException ex) {
            //throw new OperationException(ex);
        //}
        
    }
    
}