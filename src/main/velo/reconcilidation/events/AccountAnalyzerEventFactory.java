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

import java.util.logging.Logger;

import velo.entity.Account;
import velo.exceptions.FactoryException;
import velo.patterns.Factory;

/**
 * A class that represents Account Analyzer event factory
 * 
 * @author Asaf Shakarchi
 */
public class AccountAnalyzerEventFactory extends Factory {
	
	private final static String eventClassPath = "velo.reconcilidation.events";
	private static Logger logger = Logger.getLogger(AccountAnalyzerEventFactory.class.getName());
	
	/**
	 * Factory an Account Analyzer event based on a specific class name and relevant accounts(Active/Claimed)
	 * @param shortClassName The full class name(with package path) of the event to factore 
	 * @param claimedAccount The 'claimed' account that resides in the IDM database
	 * @param activeAccount The 'Active' account listed from the resource itself by the Reconcile Process
	 * @return A factored AccountAnalyzerEvent object
	 */
	public static AccountAnalyzerEvent factoryAccountAnalyzerEvent(String shortClassName, Account claimedAccount,Account activeAccount) {
		try {
			AccountAnalyzerEvent event = (AccountAnalyzerEvent)AccountAnalyzerEventFactory.factory(eventClassPath+"."+shortClassName,new Class[] {Account.class,Account.class},new Object[] {claimedAccount,activeAccount});
			return event;
		}
		catch (FactoryException fe) {
			logger.warning("Could not factor event: " + fe.getMessage());
			return null;
		}
	}
	
	/**
	 * Factory synchronize account attributes event
	 * @param claimedAccount The 'claimed' account that resides in the IDM database
	 * @param activeAccount The 'Active' account listed from the resource itself by the Reconcile Process
	 * @return A SyncAccountAttributesEvent factored object
	 */
	public static SyncAccountAttributesEvent factorySynchAccountAttributesEvent(Account claimedAccount, Account activeAccount) {
		//	Cast the factored event to a AccountAttributesSyncEvent
		SyncAccountAttributesEvent saae = (SyncAccountAttributesEvent)factoryAccountAnalyzerEvent("SyncAccountAttributesEvent",claimedAccount,activeAccount);
		return saae;
	}
	
}