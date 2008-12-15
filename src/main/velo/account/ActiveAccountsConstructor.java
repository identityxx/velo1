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

package velo.account;

import java.util.HashMap;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.SyncListImporterException;
import velo.utils.Stopwatch;

/*
 * ActiveAccountsConstructor.java
 *
 * Created on 24/05/2007, 18:38:27
 *
 * @author Shakarchi Asaf
 */
public class ActiveAccountsConstructor {
    private static Logger log = Logger.getLogger(ActiveAccountsConstructor.class.getName());
    
    public ActiveAccountsConstructor() {
    }
    
    
    public HashMap<String, Account> constructByXml(Resource resource, boolean forceAccountIdAsUppercase) throws ObjectsConstructionException {
        try {
            Stopwatch stopWatch = new Stopwatch(true);
            
            java.util.HashMap<java.lang.String,velo.entity.Account> activeAccounts = new java.util.HashMap<java.lang.String,velo.entity.Account>();
            log.debug("Importing accounts data from XML file, please wait...");
            velo.reconcilidation.SyncTargetImporter syncImporter = new velo.reconcilidation.SyncTargetImporter(resource.factorySyncFileName());
            velo.reconcilidation.SyncTargetData syncData = syncImporter.getSyncedData();
            log.debug("Import active data from XML resulted: \'" + syncData.getAccounts().size() + "' accounts");
            
            
            log.debug("Creating a MAP with all constructed ActiveAccounts has started...");
            for (Account currAA : syncData.getAccounts()) {
            	currAA.setResource(resource);
                
                if (log.isTraceEnabled()) {
                	for (Attribute currAttr : currAA.getTransientAttributes().values()) {
               			log.trace(currAttr.getDisplayable());
                	}
                }
                
                
                //what for? digester take care of this shit: 
                //activeAccount.loadAccountByMap(currAA.getTransientAttributes());
                /*
                if (activeAccount != null) {
                    java.lang.String currActiveAccountName = activeAccount.getName();
                    
                    if ( (forceAccountIdAsUppercase) || (!resource.isCaseSensitive()) ) {
                        currActiveAccountName = currActiveAccountName.toUpperCase();
                    }
                    
                    
                    if (activeAccounts.containsKey(currActiveAccountName)) {
                        log.warn("Cannot add Active-Account named \'" + currActiveAccountName + "\' to the map containing all active accounts, account with an equal name already exists in map!");
                    } else {
                        log.trace("Adding account named \'" + currActiveAccountName + "\' to the ActiveAccounts map.");
                        activeAccounts.put(currActiveAccountName, activeAccount);
                    }
                }
                */
                
                //Note: Non-case sensitive systems key will be stored in MAP as UPPERCASE
                //So reconcile accounts can compare thisAccountMap.containsKey(accountInRepository.toUpperCase()) to compare safely
                if (!resource.isCaseSensitive()) {
                	activeAccounts.put(currAA.getName().toUpperCase(), currAA);
                }
                else {
                	activeAccounts.put(currAA.getName(), currAA);
                }
            }
            
            stopWatch.stop();
            log.debug("FINISHED Factoring ActiveAccounts SET with all attributes, took: " + stopWatch.asSeconds() + " seconds!");
            
            
            return activeAccounts;
        } catch (SyncListImporterException ex) {
            String errMsg = "Could not constrcut ActiveAccounts by XML Active Data: " + ex.toString();
            log.error(errMsg);
            throw new ObjectsConstructionException(errMsg);
        }
    }
}
