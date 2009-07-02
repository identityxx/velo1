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
package velo.reconcilidation.events;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import velo.ejb.interfaces.IdentityAttributeManagerRemote;
import velo.ejb.interfaces.ResourceAttributeManagerRemote;
import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.IdentityAttribute;
import velo.entity.ReconcilePolicy;
import velo.entity.ResourceAttribute;
import velo.exceptions.ActionFactoryException;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AttributeNotFound;

/**
 * A class that represents a Synchronization Account Attributes event
 * 
 * @author Asaf Shakarchi
 */
public class SyncAccountAttributesEvent extends AccountAnalyzerEvent {
	
	private static Logger logger = Logger.getLogger(SyncAccountAttributesEvent.class.getName());
	
	/**
	 * Constructor
	 * 
	 * Initialize the SyncAccountAttributes event and set the active & claimed accounts
	 * @param claimedAccount The 'claimed' account that resides in the IDM database
	 * @param activeAccount The 'Active' account listed from the resource itself by the Reconcile Process
	 */
	public SyncAccountAttributesEvent(Account claimedAccount, Account activeAccount) {
		super(claimedAccount,activeAccount);
	}
	
	public boolean execute() {
		ReconcilePolicy rp = getClaimedAccount().getResource().getReconcilePolicy();
		
		logger.fine("-ACCOUNT ATTRIBUTES SYNC- event has occured for account name: '" + getClaimedAccount().getName() + "', in target system name: '" + getClaimedAccount().getResource().getDisplayName() + "'");
		logger.fine("Action to take in case attributes are not equal on this TS is: " + rp.getUnmatchedAccountAttributeEventAction());
		
		
		//Get the IdentityAttributeManager, required for synchronizing User Identity Attributes
		try {
			Context ic = new InitialContext();
			IdentityAttributeManagerRemote iam = (IdentityAttributeManagerRemote) ic.lookup("velo/IdentityAttributeBean/remote");
			ResourceAttributeManagerRemote tsam = (ResourceAttributeManagerRemote) ic.lookup("velo/ResourceAttributeBean/remote");
			
			/*
			 switch (getReconcilePolicy().getConfirmedAccountEventAction()) {
			 case NOTHING:
			 break;
			 }
			 */
			
			//Iterate over all virtual attributes in 'claimed account' 
			//(these are the attributes that we really want to synchronize, activeAccount might include 
			//other attributes that were fetched during the query but might not be relevant since they were 
			//not defined as resourceAttributes)
			Collection<Attribute> attrs = getClaimedAccount().getActiveAttributes().values();
			logger.fine("Syncing  *" + getClaimedAccount().getActiveAttributes().size() + "* attributes for resource: " + getClaimedAccount().getResource().getDisplayName());
			

			//DEBUG: had a problem that there was no matching attributes in ActiveAccount, here's a dump of all attributes
			logger.info("START of Dumping ActiveAccounts attributes list");
			//try {
				for (Attribute currAttr : getActiveAccount().getActiveAttributes().values()) {
					//JB!!! logger.info("ActiveAccount Attr name: " + currAttr.getUniqueName() + ", value: " + currAttr.getValueAsString());
				}
			/*}
			catch (MultipleAttributeValueVaiolation mavv) {
				
			}*/
			logger.info("END of Dumping ActiveAccounts attributes list");
			
			
			//Iterate -ONLY- over the claimed attributes that were successfully loaded, we are not certaintly sure that all virtual attributes were loaded.
			//Also, partial of the active attributes might not be relevant since they do not have a corresponding target system attribute defined, so we ignore them.
			for (Attribute currClaimedAttr : attrs) {
				//System.out.println("Account attribute name: " + currAttr.getName() + ", value: " + currAttr.getValue());
				try {
					//Get from the ActiveAccount the current iterated attribute name fetched from the ClaimedAccount's attribute list
//					Attribute currCorrespondingActiveAccountAttr = getActiveAccount().getAccountAttribute(currClaimedAttr.getUniqueName());
					Attribute currCorrespondingActiveAccountAttr = null;
					
					//JB!!! logger.fine("Found claimed account attribute name: " + currCorrespondingActiveAccountAttr.getUniqueName() + " in ActiveAccount, for account name: " + getClaimedAccount().getName() + "Claimed value: " + currClaimedAttr.getFirstValue().getValueAsString());
					
					
					if (currCorrespondingActiveAccountAttr.getFirstValue() != null) {
						logger.fine("activeAccount value: " + currCorrespondingActiveAccountAttr.getFirstValue().getAsString());
					}
					else {
						logger.fine("ActiveAttribute has no value...");
					}
					
					if (currCorrespondingActiveAccountAttr.equalToAttribute(currClaimedAttr)) {
						logger.fine("Values are equal for attribute name: " + currCorrespondingActiveAccountAttr.getUniqueName() + ", continuing...");
					}
					else {
						if (rp.getUnmatchedAccountAttributeEventAction().equals("NOTHING")) {
							logger.fine("NOTHING will be done, although attributes are not equal due to reconcile policy decigion, skipping...");
						}
						else if (rp.getUnmatchedAccountAttributeEventAction().equals("UPDATE_ATTRIBUTE_VALUE_ON_TARGET_SYSTEM") || rp.getUnmatchedAccountAttributeEventAction().equals("UPDATE_ATTRIBUTE_VALUE_ON_TARGET_SYSTEM_AND_IN_USER_ATTRIBUTES")) {
							//JB!!! logger.info("Updating attribute value in target system for account name: " + getClaimedAccount().getName() + ", from value: " + currCorrespondingActiveAccountAttr.getFirstValue().getValueAsString() + ", to value: " + currClaimedAttr.getFirstValue().getValueAsString());


/*FUCK!!!1.4
							ActionManager am = new ActionManager();
							try {
								ResourceAccountActionInterface updateAccountAction = am.factoryUpdateAccountAction(getClaimedAccount());
								updateAccountAction.__execute__();
							}
							catch(ActionFactoryException afe) {
								logger.warning("Couldnt factory an update action for account name: " + getClaimedAccount().getName() + "SKIPPING attribute update with message: " + afe.getMessage());
							}
							catch(ActionFailureException afe) {
								logger.warning("A failure was occured while trying to update attribute name: " + currClaimedAttr.getUniqueName() + ", for account name: " + getClaimedAccount().getName() + ", failure message: " + afe.getMessage());
							}
							*/
						}
						
						//If in policy it was decided to also update the attached 'user identity attributes' then lets check whether that target system attribute is connected to any user identity attributes
						if (rp.getUnmatchedAccountAttributeEventAction().equals("UPDATE_ATTRIBUTE_VALUE_IN_TARGET_SYSTEM_AND_IN_USER_ATTRIBUTES")) {
							//Get the resourceAttribute entity for the current iterated attribute name
							ResourceAttribute ra = getClaimedAccount().getResource().getResourceAttribute(currClaimedAttr.getUniqueName());
							
							//Get a collection of ALL IdentityAttributes that are attached to the current handled resourceAttribute
							Collection<IdentityAttribute> iaList = iam.findIdentityAttributesAttachedToresourceAttribute(ra);
							
							//Iterate over the found Identity Attributes, and synchronize their values if needed
							Iterator<IdentityAttribute> iaIt = iaList.iterator();
							while (iaIt.hasNext()) {
								IdentityAttribute ia = iaIt.next();
								
								//Synchronize the values of all user Identity attributes
								//if (ia)
							}
						}
					}
				}
				//catch (AttributeNotFound anf) {
				catch (Exception anf) {
					logger.warning("Attribute in 'ActiveAccount' was not Found for the corresponding expected Attribute of the ClaimedAccount, while syncing account attributes, failed with details: " + anf.getMessage()
							+ ", dumping *ActiveAccount* attributes: " 
							+ getActiveAccount().getActiveAttributes()
					);
				}
				
			} //End of attributes while
		}
		catch(NamingException ne) {
			ne.printStackTrace();
		}
		
		
		/*
		 if (getReconcilePolicy().getUnmatchedAccountAttributeEventAction().equals("NOTHING")) {
		 logger.fine("'NOTHING' was choosed, nothing left to do for account confirm event for account name: " + getAccount().getName());
		 }
		 */
		
		return true;
	}
}
