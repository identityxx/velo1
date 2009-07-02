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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import velo.ejb.interfaces.ReconcileManagerLocal;
import velo.entity.Resource;
import velo.exceptions.ReconcileAccountsException;
import velo.exceptions.ReconcileGroupsException;

@Deprecated
public class FullReconcileProcessAction {
    private static final long serialVersionUID = 1987305452306161213L;
    private static Logger logger = Logger.getLogger(FullReconcileProcessAction.class.getName());
    Resource resource;
    

	public boolean execute() {
        try {
        	//TODO: Is reconciliation can be performed remotely? if so replace all ejb calls remotely
            Context ic = new InitialContext();
            ReconcileManagerLocal rm = (ReconcileManagerLocal) ic.lookup("velo/ReconcileBean/local");
            
            //Keep execution time in milliseconds
            long startExecutionTime = System.currentTimeMillis();
            logger.info("Reconcile process has just STARTED for Resource name: " + getResource().getDisplayName());
            
            //Reconcile the groups
            if (getResource().getReconcilePolicy().isReconcileGroups()) {
            	rm.reconcileGroupsByResource(getResource());
            }
            
            if (getResource().getReconcilePolicy().isReconcileAccounts()) {
            	//Reconcile the accounts
            	rm.reconcileAccountsByResource(getResource());
            }
            
            //Remove the sync file since(tasks might got generated already, if so the file is out of state)
            String fileName = getResource().factorySyncFileName();
            File file = new File(fileName);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy_hh-mm");
            String dateStr = sdf.format(new Date());
            
            String newFileName = fileName+"_"+dateStr;
            file.renameTo(new File(newFileName));
            logger.info("Renaming file name: " + fileName + ", to file name: " + newFileName);
            
            //Close execution time
            float executionTime;
            executionTime = System.currentTimeMillis() - startExecutionTime;
            executionTime = executionTime / 1000;
            logger.info("Reconcile process has just ENDED for Resource name: " + getResource().getDisplayName() + ", execution time is: "
                + executionTime + "seconds.");
            
            return true;
        } catch (NamingException ne) {
            logger.error(ne.getMessage());
            //getMsgs().add(EdmMessageType.SEVERE,"NamingException",ne.getMessage());
            return false;
        } catch (ReconcileGroupsException rge) {
            logger.error(rge.getMessage());
            //getMsgs().add(EdmMessageType.SEVERE,"ReconcileGroupsException",rge.getMessage());
            return false;
        } catch (ReconcileAccountsException rae) {
            logger.error(rae.getMessage());
            //getMsgs().add(EdmMessageType.SEVERE,"ReconcileAccountsException", rae.getMessage());
            return false;
        }
    }
    
    
    
    
    
    /**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}
    
}