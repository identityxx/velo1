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
package velo.resource.operationControllers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;

import velo.action.ResourceOperation;
import velo.adapters.ActiveDirectoryAdapter;
import velo.collections.Accounts;
import velo.collections.ResourceGroups;
import velo.contexts.OperationContext;
import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.ResourceTask;
import velo.entity.SpmlTask;
import velo.exceptions.AccountWasNotFoundOnTargetException;
import velo.exceptions.AdapterException;
import velo.exceptions.AuthenticationFailureException;
import velo.exceptions.EdentityException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.ObjectsConstructionException;
import velo.exceptions.OperationException;
import velo.exceptions.ResourceDescriptorException;
import velo.resource.SyncDataXmlGenerator;
import velo.resource.resourceDescriptor.ResourceDescriptor;
import edu.vt.middleware.ldap.LdapUtil;

//All methods in this class are invoked synchrony against the resources 
public class ActiveDirecotryRemoteSpmlResourceOperationController extends
		SpmlResourceOperationController {
	private static Logger log = Logger
			.getLogger(ActiveDirecotryRemoteSpmlResourceOperationController.class
					.getName());

	ActiveDirectoryAdapter adapter;

	public int UF_ACCOUNTDISABLE = 0x0002;
	public int UF_PASSWD_NOTREQD = 0x0020;
	public int UF_PASSWD_CANT_CHANGE = 0x0040;
	public int UF_NORMAL_ACCOUNT = 0x0200;
	public int UF_DONT_EXPIRE_PASSWD = 0x10000;
	public int UF_PASSWORD_EXPIRED = 0x800000;

	
	public Accounts listAllIdentities(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		return null;
	}
	
	public ActiveDirecotryRemoteSpmlResourceOperationController() {

	}

	public ActiveDirecotryRemoteSpmlResourceOperationController(
			Resource resource) {
		super(resource);
	}
	
	public void init(OperationContext context) {
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,
			SuspendRequest request) throws OperationException {
		String accountName = request.getPsoID().getID();

		try {
			ResourceDescriptor rd = ro.getResource()
					.factoryResourceDescriptor();

			String usersFullDn = rd.getString("specific.accounts-base-dn");
			SearchResult sr = getAdapter(true).getAccountBySamAccountName(
					accountName, usersFullDn);
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("userAccountControl", Integer
							.toString(UF_ACCOUNTDISABLE)));

			// perform the change
			getAdapter(true).replaceAttributes(sr.getName(), mods);

			getAdapter(true).disconnect();
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.toString());
		} catch (AccountWasNotFoundOnTargetException e) {
			throw new OperationException(e.toString());
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}

		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,
			ResumeRequest request) throws OperationException {
		String accountName = request.getPsoID().getID();

		try {
			ResourceDescriptor rd = ro.getResource()
					.factoryResourceDescriptor();

			String usersFullDn = rd.getString("specific.accounts-base-dn");
			SearchResult sr = getAdapter(true).getAccountBySamAccountName(
					accountName, usersFullDn);
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("userAccountControl", Integer
							.toString(UF_NORMAL_ACCOUNT)));

			// perform the change
			getAdapter(true).replaceAttributes(sr.getName(), mods);

			getAdapter(true).disconnect();
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.toString());
		} catch (AccountWasNotFoundOnTargetException e) {
			throw new OperationException(e.toString());
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}

		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,
			DeleteRequest request) throws OperationException {
		String accountName = request.getPsoID().getID();

		try {
			ResourceDescriptor rd = ro.getResource()
					.factoryResourceDescriptor();

			String usersFullDn = rd.getString("accounts-base-dn");
			// Map accountMapFromAD =
			// getAdapter().parse(getAdapter().getAccountBySamAccountName(accountName,usersFullDn).getAttributes());
			String accountFQDN = getAdapter(true).getAccountBySamAccountName(
					accountName, usersFullDn).getName();
			getAdapter(true).getLdapManager().delete(accountFQDN);
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.toString());
		} catch (AccountWasNotFoundOnTargetException e) {
			throw new OperationException(e.toString());
		} catch (NamingException e) {
			throw new OperationException(e.toString());
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}

		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		// expecting an accountID to create already within the PSO as the
		// request currently posted by Velo
		// TODO: Support none-set PSOID within request (in high levels of
		// course)
		String accountName = request.getPsoID().getID();

		// Get pwd resource attribute
		ResourceAttribute passwdResourceAttribute = ro.getResource()
				.getPasswordAttribute();
		if (passwdResourceAttribute == null) {
			log
					.warn("Could not find a resource attribute indicated as a password attribute for resource '"
							+ ro.getResource().getDisplayName()
							+ "', will generate a random complex password for user upon creation");
		}

		// fetch the virtual attributes from the context, expecting only managed
		// attributes in map
		Map<String, ResourceAttribute> attrs = (Map<String, ResourceAttribute>) ro
				.getContext().get("attrs");
		//remove the password attribute fro map, this is important, otherwise AD fails
		//might not exist at all
		if (passwdResourceAttribute != null) {
			attrs.remove(passwdResourceAttribute.getUniqueName());
		}
		

		Attributes ldapAttrs = getLdapAttributes(attrs);
		// Appearntly, the only attribute that is a MUST is the CN. make sure
		// that there is a CN attribute set for the target we handle
		javax.naming.directory.Attribute cnAttr = ldapAttrs.get("CN");
		if (cnAttr == null) {
			throw new OperationException(
					"Could not find virtual account attribute named 'CN', that is a must for resource type '"
							+ getResource().getResourceType().getUniqueName()
							+ "'");
		}

		// Set the ObjectClass of a user.
		ldapAttrs.put("objectClass", "user");

		// Note that you need to create the user object before you can
		// set the password. Therefore as the user is created with no
		// password, user AccountControl must be set to the following
		// otherwise the Win2K3 password filter will return error 53
		// unwilling to perform.
		ldapAttrs.put("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT
				+ UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE));
		
		
		ResourceDescriptor rd = null;
		String accountDNToCreate = null;
		try {
			//TODO: Support more dynamic containers where user is created, maybe via rules
			rd = ro.getResource().factoryResourceDescriptor();
			accountDNToCreate = "CN=" + cnAttr.get() + ","+(String)rd.getString("specific.accounts-default-context-dn");
		} catch (NamingException ne) {
			throw new OperationException(ne.toString());
		} catch (ResourceDescriptorException ne) {
			throw new OperationException(ne.toString());
		}
		
		
		if (log.isTraceEnabled()) {
            log.trace("-START- of dumping Ldap Context account's attributes...");
            log.trace("Account FQDN: " + accountDNToCreate);
            final List<javax.naming.directory.Attribute> ae = Collections.list((NamingEnumeration<javax.naming.directory.Attribute>) ldapAttrs.getAll());
            for (Iterator<javax.naming.directory.Attribute> it = ae.iterator(); it.hasNext(); ) {
                javax.naming.directory.Attribute attr = it.next();
                log.trace("Attribute: " + attr.toString());
            }
        }
		
		
		log.debug("Creating account with full DN: " + accountDNToCreate);
		
		try {
            //Create the context
            //Old way: Context result = getAdapter().getLcx().createSubcontext(accountDNToCreate, accAttrs);
            getAdapter(true).getLdapManager().create(accountDNToCreate, ldapAttrs);
        } catch (NoSuchAttributeException nsae) {
            String errMsg = "Could not create account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', set attribute(s) that are not available in User Schema, detailed failure message: " + nsae.getMessage();
            log.error(errMsg);
            throw new OperationException(errMsg);
        } catch (NameAlreadyBoundException nabe) {
            String errMsg  = "Could not create account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', account with the same name already exist!, detailed failure message: " + nabe.getMessage();
            log.error(errMsg);
            throw new OperationException(errMsg);
        } catch (InvalidAttributeValueException iave) {
            String errMsg  = "Could not create account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', An Invalid attribute value has occured, detailed failure message is: " + iave.getMessage();
            log.error(errMsg);
            throw new OperationException(errMsg);
        } catch (Exception e) {
            String errMsg  = "Could not create account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', detailed failure message is: " + e.getMessage();
            log.error(errMsg);
            throw new OperationException(errMsg);
        }
        
        
        
        String pass;
        if (passwdResourceAttribute == null) {
            //TODO Replace with random password!
            pass = "z3FgKZOz1oZzyA!o1212";
        } else {
        	if (passwdResourceAttribute.isFirstValueIsNotNull()) {
        		pass = passwdResourceAttribute.getFirstValue().getAsString();
        	} else {
        		pass = "z3FgKZOz1oZzyA!o1212";;
        	}
        }
        
        
        javax.naming.directory.Attribute pwdLdapAttr = ldapAttrs.get("pwdLastSet");

        //Init the size of the array
        int arrayModsSize;
        if (pwdLdapAttr != null) {
            arrayModsSize = 3;
        } else {
            arrayModsSize = 2;
        }
        
        
        try {
            //Modify the user password
            ModificationItem[] mods = new ModificationItem[arrayModsSize];
            //String newQuotedPassword = "\"Password2000\"";
            String newQuotedPassword = "\"" + pass + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
            
            //If pwdLastSet was set, then it should be modified here, after the password modification (since password modification changes its value!)
            if (pwdLdapAttr != null) {
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, pwdLdapAttr);
            }
            
            //Perform the update
            getAdapter(true).replaceAttributes(accountDNToCreate, mods);
        } catch (UnsupportedEncodingException e) {
            String errMsg  = "Could not modify password of created account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', failure message: " + e.getMessage();
            log.error(errMsg);
            //TODO: Log this to the task somehow
            //getMsgs().add(EdmMessageType.CRITICAL,"UnsupportedEncodingException", errMsg);
        } catch (AdapterException e) {
        	String errMsg  = "Could not modify password of created account '" + accountName + "', on Resource: '" + getResource().getDisplayName() + "', failure message: " + e.getMessage();
            log.error(errMsg);
        }
        
        log.debug("Successfully modified password of created account '" + accountName +"', on Resource: '" + getResource().getDisplayName() + "'");

        
        log.debug("adding group membership...");
		//get the groups from the request as a nice readable list
		List<String> groups = getGroupsToAssign(request, ro.getResource().getUniqueName());
		log.debug("Account should be a memmber of groups amount '" + groups.size() + "', performing group membership queries...");

		//could be done in one transaction but we do not want a whole failure in case a single group is a trouble
        //ModificationItem member[] = new ModificationItem[groups.size()];
		ModificationItem member[] = new ModificationItem[1];
        for (String currGroupUniqueId : groups) {
        	member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", accountDNToCreate));
        	try {
        		getAdapter(true).getLdapManager().getLdapContext().modifyAttributes(currGroupUniqueId, member);
        		//TODO: Log the errors to the task! this is very important
        	} catch (NamingException e) {
        		log.error("Could not add group membership for account '" + accountDNToCreate + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
        	} catch (AdapterException e) {
        		log.error("Could not add group membership for account '" + accountDNToCreate + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
        	}
        }
        
        try {
        	getAdapter(true).disconnect();
        } catch (AdapterException e) {
        	log.error("Could not disconnect from resource '" + resource.getDisplayName() + "': " + e.toString());
        }
        
        log.debug("Succesfully added group membership!");
        
		
		log.debug("Sucessfully ended invocation of ResourceOperation...");
	}

	private Attributes getLdapAttributes(Map<String, ResourceAttribute> attrs) {
		// LDAP Attributes container
		Attributes accAttrs = new BasicAttributes(true);

		for (ResourceAttribute currRA : attrs.values()) {
			// AD is not case sensitive, then set all attributes as UpperCase
			// Appearntly, AD expecting all
			// attributes(numbers/strings/booleans/etc..) as string
			if (currRA.isFirstValueIsNotNull()) {
				accAttrs.put(currRA.getUniqueName().toUpperCase(), currRA
					.getFirstValue().getAsString());
			} else {
				log.info("Skipping adding resource attribute '" + currRA.getDisplayName() + "' to account attribute ldap context since it has no values...");
			}
		}

		return accAttrs;
	}
	
	
	
	
	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		String accountName = request.getPsoID().getID();
		
		log.info("Performing modify request for account '" + accountName + "', on resource '" + ro.getResource().getDisplayName() + "'");
		
		
		try {
			ResourceDescriptor rd = ro.getResource().factoryResourceDescriptor();
			String usersFullDn = rd.getString("accounts-default-context-dn");
			//TODO: Support searching by the resource attribute that is indicated as the accountID and not statically by SamAccountName!!!
			SearchResult sr = getAdapter(true).getAccountBySamAccountName(accountName,usersFullDn);
        
			//get the groups from the request as a nice readable list
			Map<String,List<String>> groupsToManage = getGroupMembershipToModify(request,ro.getResource().getUniqueName());
			if (groupsToManage == null) {
				throw new OperationException("Could not load groups to assign/revoke!");
			}
        
			List<String> groupsToAssign = groupsToManage.get("groupsToAssign");
			List<String> groupsRevoke = groupsToManage.get("groupsToRevoke");

			String accountFullDN = sr.getName();

			
			//groups to assign
			ModificationItem member[] = new ModificationItem[1];
			for (String currGroupUniqueId : groupsToAssign) {
				member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", accountFullDN));
				try {
					getAdapter(true).getLdapManager().getLdapContext().modifyAttributes(currGroupUniqueId, member);
					//TODO: Log the errors to the task! this is very important
				} catch (NamingException e) {
					log.error("Could not add group membership for account '" + accountFullDN + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
				} catch (AdapterException e) {
					log.error("Could not add group membership for account '" + accountFullDN + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
				}
			}
			
			//groups to revoke
			for (String currGroupUniqueId : groupsRevoke) {
				member[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("member", accountFullDN));
				try {
					getAdapter(true).getLdapManager().getLdapContext().modifyAttributes(currGroupUniqueId, member);
					//TODO: Log the errors to the task! this is very important
				} catch (NamingException e) {
					log.error("Could not add group membership for account '" + accountFullDN + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
				} catch (AdapterException e) {
					log.error("Could not add group membership for account '" + accountFullDN + "', on resource '" + resource.getDisplayName() + "': " + e.toString());
				}
			}
			
        
			try {
				getAdapter(true).disconnect();
			} catch (AdapterException e) {
				log.error("Could not disconnect from resource '" + resource.getDisplayName() + "': " + e.toString());
			}
        
			log.debug("Succesfully added group membership!");
		} catch (ResourceDescriptorException e) {
			throw new OperationException(e.toString());
		} catch (AccountWasNotFoundOnTargetException e) {
			throw new OperationException(e.toString());
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
	}
	
	
	@Override
	//TODO: Implement!
	public Accounts listIdentitiesIncrementally(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		return null;
	}

	@Override
	//TODO: Implement!
	public Accounts listIdentitiesFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	@Override
	//TODO: Implement!
	public ResourceGroups listGroupsFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	@Override
	//TODO: Implement!
	public ResourceGroups listGroupMembershipFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public void resourceFetchActiveDataOffline(ResourceOperation ro,
			ResourceTask resourceTask) throws OperationException {
		try {
			SyncDataXmlGenerator sdxg = new SyncDataXmlGenerator(resource);
			log.debug("Setting/Creating List of Active-Accounts...");
			sdxg.setAccounts(loadActiveAccounts(getResource()));
			log.debug("Fetched '" + sdxg.getAccounts().size()
					+ "' accounts from target...");
			log.debug("Setting/Creating List of Active-Groups...");
			sdxg.setGroups(loadActiveGroups(getResource()));
			log.debug("Fetched '" + sdxg.getGroups().size()
					+ "' groups from target...");

			// Get the file name to create the list
			String fileName = getResource().factorySyncFileName();
			log.debug("Data Sync file name to be created is: " + fileName);
			// BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "UTF-8"));
			bw.write(sdxg.dumpXmlAsString());

			bw.close();
		} catch (ObjectsConstructionException e) {
			throw new OperationException(e.toString());
		} catch (IOException e) {
			throw new OperationException(e.toString());
		}
	}

	private List<Account> loadActiveAccounts(Resource resource)
			throws ObjectsConstructionException {
		List<Account> activeAccounts = new ArrayList<Account>();
		Set<ResourceAttribute> resourceAttributes = resource.getAttributes()
				.getOnlyActive();

		int managedAttrsInt = resourceAttributes.size();
		String returnedAttrs[] = new String[managedAttrsInt];

		// Fill into the array the attributes to fetch in search
		int attrLoop = 0;
		for (ResourceAttribute currRA : resourceAttributes) {
			returnedAttrs[attrLoop] = currRA.getUniqueName();
			attrLoop++;
		}

		if (log.isTraceEnabled()) {
			log.trace("Logging Attributes[] ldap's cells");
			for (int i = 0; i < managedAttrsInt; i++) {
				log.trace("AD attr name [" + i + "]: '" + returnedAttrs[i]
						+ "'");
			}
		}

		// Make sure attrLoop equals in its size to the 'managedAttrsLong' size
		// as expected. otherwise empty
		// cells in 'returnedAttrs' might cause troubles when adapter.search()
		// is invoked!
		if (attrLoop != managedAttrsInt) {
			throw new ObjectsConstructionException(
					"Determined attributes size is: '"
							+ managedAttrsInt
							+ "', while the iterator of iterated target attributes size is: '"
							+ attrLoop + "', both must be equal!");
		}

		try {
			ResourceDescriptor rd = resource.factoryResourceDescriptor();
			String accountsBaseDn = rd.getString("specific.accounts-base-dn");
			String filter1 = "(objectClass=user)";
			getAdapter(true).setBaseDn(accountsBaseDn);
			Iterator<SearchResult> it = getAdapter(true).getLdapManager().search(
					filter1, returnedAttrs);

			while (it.hasNext()) {
				SearchResult sr = it.next();
				log.trace("Fetched activeAccount attributes: "
						+ sr.getAttributes().toString());
				Map<String, ArrayList> currAccountMap = LdapUtil
						.parseAttributes(sr.getAttributes(), false);
				log.trace("Parsed Ldap attributes to MAP!: " + currAccountMap);
				Account account = new Account();
				account.setResource(getResource());
				try {
					log.trace("Loading account by the constructed MAP");
					account.loadAccountByMultiValuedMap(currAccountMap);
					activeAccounts.add(account);
				} catch (EdentityException e) {
					log
							.warn("Skipping current iterated active account due to exception: "
									+ e.getMessage());
				}
			}

			if (log.isTraceEnabled()) {
				for (Account currActiveAccount : activeAccounts) {
					log
							.trace("START to dump loaded attributes of Active-Account name: "
									+ currActiveAccount.getName());
					for (Attribute currAttr : currActiveAccount
							.getActiveAttributes().values()) {
						if (currAttr.getFirstValue() == null) {
							log.debug("NOTE: Attriubte name: '"
									+ currAttr.getUniqueName()
									+ "' has no values!");
						} else {
							log.trace("Attribute name: "
									+ currAttr.getUniqueName()
									+ ", with first value: "
									+ currAttr.getFirstValue());
						}
					}
					log.trace("END of attributes dump");
				}
			}

			return activeAccounts;

		} catch (ResourceDescriptorException e) {
			throw new ObjectsConstructionException(e.toString());
		} catch (AdapterException e) {
			throw new ObjectsConstructionException(e.toString());
		} catch (NamingException e) {
			throw new ObjectsConstructionException(e.toString());
		}
	}

	private List<ResourceGroup> loadActiveGroups(Resource resource)
			throws ObjectsConstructionException {
		log.debug("Getting a list of active groups from target system named: '"
				+ getResource().getDisplayName() + "'");
		List<ResourceGroup> activeGroups = new ArrayList<ResourceGroup>();
		String filter1 = "(objectClass=group)";

		try {
			// Retrieve from descriptor the base dn to seek accounts!
			ResourceDescriptor rd = resource.factoryResourceDescriptor();
			String groupsBaseDn = rd.getString("specific.groups-base-dn").toString();
			getAdapter(true).setBaseDn(groupsBaseDn);

			Iterator<SearchResult> it = getAdapter(true).getLdapManager().search(
					filter1);
			String returnedAttrs[] = new String[2];
			returnedAttrs[0] = "description";
			returnedAttrs[1] = "name";

			log
					.debug("Iterating over search result of the ActiveDirectory Search Group query...");
			while (it.hasNext()) {
				SearchResult sr = it.next();
				Map<String, ArrayList> currGroupMap = LdapUtil.parseAttributes(
						sr.getAttributes(), false);

				String uniqueId = sr.getName();
				String description = "";
				if (currGroupMap.containsKey("description")) {
					if (currGroupMap.get("description").iterator().hasNext()) {
						description = (String) currGroupMap.get("description")
								.iterator().next();
					}
				}

				// Expecting a group name to be always available
				String displayName = (String) currGroupMap.get("name")
						.iterator().next();

//				try {
					ResourceGroup currRG = ResourceGroup.factory(uniqueId,
							displayName, description, "SECURITY", getResource());
					activeGroups.add(currRG);
				//} catch (ObjectFactoryException e) {
//					log
//							.warn("Skipping group loading due to exception while trying to load group, failure message: '"
//									+ e.toString() + "'");
//				}
			}

			log.info("Successfully retrieved groups with amount: '"
					+ activeGroups.size() + "'");

			return activeGroups;
		} catch (ResourceDescriptorException e) {
			throw new ObjectsConstructionException(e.toString());
		} catch (NamingException e) {
			throw new ObjectsConstructionException(e.toString());
		} catch (AdapterException e) {
			throw new ObjectsConstructionException(e.toString());
		}
	}

	// TODO: Support adapters via pools
	private ActiveDirectoryAdapter getAdapter(boolean connect) throws AdapterException {
		if (this.adapter == null) {
			adapter = new ActiveDirectoryAdapter();
			adapter.setResource(getResource());
		}

		if (connect) {
			if (!adapter.isConnected()) {
				adapter.connect();
			}
		}
		
		return this.adapter;
	}
	
	
	public boolean authenticate(String userName, String password) throws AuthenticationFailureException {
		try {
			return getAdapter(true).simpleAuthenticate(userName,password);
		} catch (AdapterException e) {
			throw new AuthenticationFailureException(e.toString());
		}
	}
}
