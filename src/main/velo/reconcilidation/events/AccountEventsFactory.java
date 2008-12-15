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

import java.util.Map;

import velo.entity.Account;
import velo.entity.Resource;
import velo.entity.User;
import velo.exceptions.FactoryException;
import velo.patterns.Factory;
import velo.reconcilidation.events.reconcileAccounts.ConfirmedAccountEvent;
import velo.reconcilidation.events.reconcileAccounts.DeletedAccountEvent;
import velo.reconcilidation.events.reconcileAccounts.UnmatchedAccountEvent;
import velo.reconcilidation.events.reconcileAccounts.UnsignedAccountEvent;

/**
 * A factory class for Account Events
 * 
 *  @author Asaf Shakarchi
 */
public class AccountEventsFactory extends Factory {

	//TODO: At least move this static classpath to somewhere external
	private final static String eventClassPath = "velo.reconcilidation.events.reconcileAccounts";
	
	/**
	 * Factory an account event
	 * @param eventShortClassName The name of the event class to factore
	 * @param resource The resource the account is attached to
	 * @param account The Account to factory the event for
	 * @return A factored AccountEvent object
	 */
	//public static AccountEvent factoryAccountEvent(String eventShortClassName, resource resource, Account account) {
	public static AccountEvent factoryAccountEvent(String eventShortClassName, Resource resource) {
		try {
			AccountEvent accountEvent = (AccountEvent)AccountEventsFactory.factoryInstance(eventClassPath+"."+eventShortClassName);
			accountEvent.setResource(resource);
			//accountEvent.setAccountName(account.getName());
			//accountEvent.setAccount(account);
			return accountEvent;
		}
		catch (FactoryException fe) {
			System.out.println("Could not factor event: " + fe.getMessage());
			return null;
		}
	}
	
	
	
	
	/**
	 * Factory a CONFIRMED account event
	 * @param resource The resource the account is attached to
	 * @param account The Account to factory the event for
	 * @return A factored AccountEvent object
	 */
	public static ConfirmedAccountEvent factoryConfirmedAccountEvent(Resource resource, Account claimedAccount) {
		ConfirmedAccountEvent uae = (ConfirmedAccountEvent)factoryAccountEvent("ConfirmedAccountEvent", resource);
		uae.setClaimedAccount(claimedAccount);
		return uae;
	}
	
	
	
	
	/**
	 * Factory an account event
	 * @param eventShortClassName The name of the event class to factore
	 * @param resource The resource the account is attached to
	 * @param account The Account to factory the event for
	 * @return A factored AccountEvent object
	 */
	public static UnmatchedAccountEvent factoryUnmatchedAccountEvent(Resource resource, Account activeAccount, Map<String,Account> accountsToPersist) {
		UnmatchedAccountEvent uae = (UnmatchedAccountEvent)factoryAccountEvent("UnmatchedAccountEvent", resource);
		uae.setAccountsToPersist(accountsToPersist);
		uae.setActiveAccount(activeAccount);
		
		return uae;
	}
	
	
	
	/**
	 * Factory an 'Unasigned' Account event
	 * @param resource The resource the account is attached to
	 * @param matchedUser The matched user to assign to the account
	 * @param account The account the event should handle
	 * @param accountsToPersist A list of accounts to persist, if this event needs to persist an account object, then the event must add the account object to the list
	 * @return An 'UnasignedAccount' Event factored object
	 */
	public static UnsignedAccountEvent factoryUnsignedAccountEvent(Resource resource, User matchedUser, Account activeAccount, Map<String,Account> accountsToPersist) {
		//Cast the factored event to a DeletedAccountEvent
		UnsignedAccountEvent uae = (UnsignedAccountEvent)factoryAccountEvent("UnsignedAccountEvent",resource);
		uae.setUser(matchedUser);
		uae.setAccountsToPersist(accountsToPersist);
		uae.setActiveAccount(activeAccount);
		
		return uae;
	}
	
	/**
	 * Factory a 'delete' account event
	 * @param resource The resource the account is attached to 
	 * @param account The account the event should handle (delete)
	 * @param it A reference to the itrator containing the list of the Accounts(Must be set into the factored event)
	 * @return A factored 'DeleteAccount' event object
	 */
	public static DeletedAccountEvent factoryDeletedAccountEvent(Resource resource,Account claimedAccount,Map<String,Account> accountsToRemove) {
		//	Cast the factored event to a DeletedAccountEvent
		DeletedAccountEvent dae = (DeletedAccountEvent)factoryAccountEvent("DeletedAccountEvent",resource);
		dae.setResource(resource);
		dae.setAccountsToRemove(accountsToRemove);
		dae.setClaimedAccount(claimedAccount);
		
		return dae;
	}
}
