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
package velo.actions.activeDirectory;

import edu.vt.middleware.ldap.LdapUtil;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.exceptions.ActionFailureException;
import velo.exceptions.AdapterException;
import velo.exceptions.EdentityException;
import velo.resource.SyncTargetGenerator;

/**
 * A sync active directory data action clsas
 * Aims to list accounts/groups/membership of a certain ActiveDirectory Resource
 *
 * TODO: Currently empty, in the future might create the list query automatically Currently must get implemented by child
 * 
 * @author Asaf Shakarchi
 */
public class ActiveDirectorySyncDataAction extends ActiveDirectoryResourceAction {
	private static Logger log = Logger.getLogger(ActiveDirectorySyncDataAction.class.getName());
	
	/**
	 * Constructor
	 * Empty constructor
	 */
	public ActiveDirectorySyncDataAction() {
		
	}
	
	/**
	 * Constructor
	 * @param resource The Resource entity retrieve the account list for
	 */
	public ActiveDirectorySyncDataAction(Resource resource) {
		super(resource);
	}
	
	public boolean execute() throws ActionFailureException {
		//Mainly connects
		super.execute();
		
		try {
			createListFile();
			return true;
		}
		catch (IOException ioe) {
			log.warn(ioe.getMessage());
			getMsgs().add(ioe.getMessage());
			return false;
		}
		
		//If a method was not found
		catch (MissingMethodException mme) {
			throw new ActionFailureException(mme.getMessage());
		}
		//If a method is called over a null object
		catch (NullPointerException npe) {
			throw new ActionFailureException(npe.getMessage());
		}
		//When a method is called over a non existend 'object'
		catch (MissingPropertyException mpe) {
			throw new ActionFailureException(mpe.getMessage());
		}
		catch (AbstractMethodError ame) {
			throw new ActionFailureException(ame.getMessage());
		}
	}
	
	
	public boolean createListFile() throws IOException,ActionFailureException {
		log.debug("Creating sync file for Resource name: '" + getResource().getDisplayName() + "'");
		
		/*
		for (int i=0;i<groupListFromQuery.size(); i++) {
			//Convert a map per object
			Map currGroup = (Map) groupListFromQuery.get(i);

			//log.info("Dumping ActiveAccount that was just read from TS: " + currUser);
			ResourceGroup group = new ResourceGroup();
			//(Resource is required by 'loadAccountByMap')
			
			try {
				group.load(currGroup,getResource());
			} catch (LoadGroupByMapException e) {
				continue;
			}
			activeGroups.add(group);
		}
		*/
		
		try {
			SyncTargetGenerator stg = new SyncTargetGenerator(getResource());
			log.debug("Setting/Creating List of Active-Accounts...");
			stg.setAccounts(getListOfActiveAccounts());
			log.debug("Fetched '" + stg.getAccounts().size() + "' accounts from target...");
			log.debug("Setting/Creating List of Active-Groups...");
			stg.setGroups(getListOfActiveGroups());
			log.debug("Fetched '" + stg.getGroups().size() + "' groups from target...");

			//Get the file name to create the list
			String fileName = getResource().factorySyncFileName();
			log.debug("Data Sync file name to be created is: " + fileName);
			//BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
			bw.write(stg.dumpAsString());
		
			bw.close();
		}
		catch (NamingException ne) {
			throw new ActionFailureException(ne.toString());
		}
		catch (AdapterException ae) {
			throw new ActionFailureException(ae.toString());
		}
		
		return true;
	}
	
	
	
	
	
	
	public List<Account> getListOfActiveAccounts() throws AdapterException,NamingException,ActionFailureException {
		log.debug("Starting to create list of active accounts for ActiveDirectory...");
		
		List<Account> activeAccounts = new ArrayList<Account>();
		//Set<ResourceGroup> activeGroups = new HashSet<ResourceGroup>();
		
		long managedAttrsLong = getResource().countManagedResourceAttributes();
		log.trace("Determined defined Target Attributes for the target # is: " + managedAttrsLong);
		
		int managedAttrsInt = (int)managedAttrsLong;
		String returnedAttrs[]=new String[managedAttrsInt];
		
		//Fill into the array the attributes to fetch in search
		int attrLoop = 0;
		for (ResourceAttribute currTSA : getResource().getResourceAttributes()) {
			if (currTSA.isManaged()) {
				returnedAttrs[attrLoop] = currTSA.getUniqueName();
				attrLoop++;
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("Logging Attributes[] ldap's cells");
			for (int i=0;i<managedAttrsInt;i++) {
				log.trace("AD attr name [" + i + "]: '" + returnedAttrs[i] + "'");
			}
		}
		
		//Make sure attrLoop equals in its size to the 'managedAttrsLong' size as expected. otherwise empty 
		//cells in 'returnedAttrs' might cause troubles when adapter.search() is invoked!
		if (attrLoop != managedAttrsInt) {
			throw new ActionFailureException("Determined attributes size is: '" + managedAttrsInt + "', while the iterator of iterated target attributes size is: '" + attrLoop + "', both must be equal!");
		}
		//Retrieve from descriptor the base dn to seek accounts!
		String accountsBaseDn = null; //DEPgetResourceDescriptor().getSpecificAttributes().get("accounts-base-dn").toString();
		
		
		String filter1 = "(objectClass=user)";
		getAdapter().setBaseDn(accountsBaseDn);
		Iterator<SearchResult> it = getAdapter().getLdapManager().search(filter1,returnedAttrs);
		
		while (it.hasNext()) {
			SearchResult sr = it.next();
			log.trace("Fetched activeAccount attributes: " + sr.getAttributes().toString());
			Map<String,ArrayList> currAccountMap = LdapUtil.parseAttributes(sr.getAttributes(), false);
			log.trace("Parsed Ldap attributes to MAP!");
			Account account = new Account();
			account.setResource(getResource());
			try {
				log.trace("Loading account by the constructed MAP");
				account.loadAccountByMultiValuedMap(currAccountMap);
				activeAccounts.add(account);
			} catch (EdentityException e) {
				log.warn("Skipping current iterated active account due to exception: " + e.getMessage());
			}
		}
		
		if (log.isTraceEnabled()) {
			for (Account currActiveAccount : activeAccounts) {
				log.trace("START to dump loaded attributes of Active-Account name: " + currActiveAccount.getName());
				for (Attribute currAttr : currActiveAccount.getTransientAttributes().values()) {
					if (currAttr.getFirstValue() == null) {
						log.debug("NOTE: Attriubte name: '" + currAttr.getUniqueName() + "' has no values!");
					}
					else {
						log.trace("Attribute name: " + currAttr.getUniqueName() + ", with first value: " + currAttr.getFirstValue());
					}
				}
				log.trace("END of attributes dump");
			}
		}
		
		return activeAccounts;
	}

	public List<ResourceGroup> getListOfActiveGroups() throws AdapterException,NamingException {
		log.debug("Getting a list of active groups from target system named: '" + getResource().getDisplayName() + "'");
		List<ResourceGroup> activeGroups = new ArrayList<ResourceGroup>();
		String filter1 = "(objectClass=group)";
		
		//Retrieve from descriptor the base dn to seek accounts!
		String groupsBaseDn = null; //DEP getResourceDescriptor().getSpecificAttributes().get("groups-base-dn").toString();
		getAdapter().setBaseDn(groupsBaseDn);
		
		Iterator<SearchResult> it = getAdapter().getLdapManager().search(filter1);
		String returnedAttrs[]=new String[2];
		returnedAttrs[0] = "description";
		returnedAttrs[1] = "name";
		
		log.debug("Iterating over search result of the ActiveDirectory Search Group query...");
		while (it.hasNext()) {
			SearchResult sr = it.next();
			Map<String,ArrayList> currGroupMap = LdapUtil.parseAttributes(sr.getAttributes(), false);

			ResourceGroup currTsg = new ResourceGroup();
			
			String uniqueId = sr.getName();
			String description = "";
			if (currGroupMap.containsKey("description")) {
				if (currGroupMap.get("description").iterator().hasNext()) {
					description = (String)currGroupMap.get("description").iterator().next();
				}
			}
			
			//Expecting a group name to be always available
			String displayName = (String)currGroupMap.get("name").iterator().next();
			
			/*JB!!!
			try {
				//currTsg.load(uniqueId, displayName, description, getResource());
				activeGroups.add(currTsg);
			}
			catch (LoadGroupByMapException lgbme) {
				log.warn("Skipping group loading due to exception while trying to load group, failure message: '" + lgbme.getMessage() + "'");
			}
			*/
		}
		
		log.info("Successfully retrieved groups with amount: '" + activeGroups.size() + "'");
		
		return activeGroups;
		
	}
	
	/*
	public List<Account> getListOfActiveAccounts(LdapContext lc) {
		//String returnedAttrs[]={"sAMAccountName"};
		
		long managedAttrsLong = getResource().countManagedResourceAttributes()-1;
		int managedAttrsInt = (int)managedAttrsLong;
		String returnedAttrs[]=new String[managedAttrsInt-1];

		Map currUser = new HashMap();
		int attrCounter = 0;
		log.info("Iterating over managed attributes for target system name: '" + getResource().getDisplayName() + "' in order to build active accounts list");
		//Fetch the attributes of current target
		for (ResourceAttribute currTsa : getResource().getResourceAttributes()) {
			if (currTsa.isManaged()) {
				returnedAttrs[attrCounter] = currTsa.getNameForRetrieval();
				attrCounter++;
				log.info("Will retrieve value of attribute named: " + currTsa.getNameForRetrieval());
			}
			else {
				log.warn("Attribute named: '" + currTsa.getNameForRetrieval() + "' is not managed, skipping.")
			}
		}
		
		
		
		//Convert a map per object
		Map currUser = (Map) accountListFromQuery.get(i);

		//log.info("Dumping ActiveAccount that was just read from TS: " + currUser);
		Account account = new Account();
		//(Resource is required by 'loadAccountByMap')
		
		account.setResource(getResource());
		try {
			account.loadAccountByMap(currUser);
			activeAccounts.add(account);
	}
		
		
		
		ResourceActionTools tsat = (ResourceActionTools)getTools();
		ResourceDescriptor tsd = tsat.getResourceDescriptor();
		String groupsBaseFullDn = (String)tsd.getSpecificAttributes().get("groups-base-full-dn");
		
		//Create the search controls 		
		SearchControls searchCtls = new SearchControls();

		//Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		//specify the LDAP search filter
		//String searchFilter = "(&(objectClass=user)(CN=Andrew Anderson))";
		String searchFilter = "(&(objectClass=user)(CN=*))";
		
		//Specify the Base for the search
		//String searchBase = "OU=edmou,DC=ADtest,DC=co,dc=il";
		String searchBase = groupsBaseFullDn;
		
		//Specify the attributes to return
		String returnedAttrs[]={"sAMAccountName"};
		searchCtls.setReturningAttributes(returnedAttrs);
		
		//Search for objects using the filter
		NamingEnumeration answer = lc.search(searchBase, searchFilter, searchCtls);

		//initialize counter to total accounts
		int totalResults = 0;

		//Loop through the search results
		while (answer.hasMoreElements()) {
			SearchResult sr = (SearchResult)answer.next();
			
			//Print out the groups
			Attributes attrs = sr.getAttributes();
			if (attrs != null) {
				for (NamingEnumeration ae = attrs.getAll();ae.hasMore();) {
					Attribute attr = (Attribute)ae.next();
					if (attr.getID().toLowerCase().equals("samaccountname")) {
						for (NamingEnumeration e = attr.getAll();e.hasMore();totalResults++) {
							log.trace(" " +  totalResults + ". " +  e.next());
						}								
					}
					else {
						log.warn("Warning!!!");
					}
				}
			}	 
		}
	}
	*/
}
