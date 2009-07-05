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
package velo.ejb.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBs;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jfree.util.Log;

import velo.common.SysConf;
import velo.converters.AccountAttributeConverterInterface;
import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.AdapterManagerLocal;
import velo.ejb.interfaces.CommonUtilsManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceGroupManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.TaskManagerLocal;
import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.Account;
import velo.entity.AccountAttribute;
import velo.entity.AuditedAccount;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceAttributeBase;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.exceptions.ConverterProcessFailure;
import velo.exceptions.LoadingVirtualAccountAttributeException;
import velo.exceptions.NoUserIdentityAttributeFoundException;
import velo.exceptions.NoUserIdentityAttributeValueException;
import velo.exceptions.ObjectFactoryException;
import velo.exceptions.OperationException;
import velo.exceptions.ScriptLoadingException;
import velo.patterns.FactoryAccountAttributeConverter;
import velo.storage.Attribute;

/**
 * A Stateless EJB bean for managing an Account
 * 
 * @author Asaf Shakarchi
 */
//	Required in order to check status of accounts
  @EJBs({ @EJB(name="resourceEjbRef",beanInterface=ResourceManagerLocal.class),  @EJB(name="adapterEjbRef",beanInterface=AdapterManagerLocal.class) })
@Stateless()
@Name("accountManager")
@AutoCreate
public class AccountBean implements AccountManagerLocal, AccountManagerRemote {
	  private final String table_accounts_to_resource_group = "VL_ACCOUNTS_TP_RESOURCE_GRPS";
	  
	/**
	 * Injected entity manager
	 */
	@PersistenceContext
	public EntityManager em;

	/**
	 * Inject the User Bean
	 */
	@org.jboss.annotation.IgnoreDependency
	@IgnoreDependency
	@EJB
	UserManagerLocal userManager;

	/**
	 * Inject the ResourceBean
	 */
	@EJB
	ResourceManagerLocal resourceManager;

	@EJB
	ResourceAttributeManagerLocal tsam;

	@EJB
	CommonUtilsManagerLocal cum;

	@EJB
	TaskManagerLocal tm;

	@EJB
	ResourceGroupManagerLocal tsgm;

	private static Logger logger = Logger.getLogger(AccountBean.class.getName());

	public void setEntityManager(EntityManager entityManager) {
		this.em = entityManager;
	}
	
	
	
	public void removeAccountEntity(String accountUniqueName, String resourceUniqueName) {
		logger.debug("Removing account named '" + accountUniqueName + "', on resource unique name: '" + resourceUniqueName + "'");
		//Account account = (Account)em.createNamedQuery("account.findByName").setParameter("accountName", accountUniqueName).setParameter("resourceUniqueName", resourceUniqueName).getSingleResult();
		Account acc = findAccount(accountUniqueName, resourceUniqueName);
		
		if (acc != null) {
			removeAccountEntity(acc);
		}
		
		logger.debug("Successfully found account in repository, performing entity removal...");
	}
	
	public void removeAccountEntity(Account account) {
		logger.info("Removing account name " + account.getName());

		/*
		if (!em.contains(account)) {
			// Merge first, cannot remove detached entity
			account = em.merge(account);
		}
		*/
		
		// Clean Orphan group2accounts assocaitions
		/*Irrelevant
		if (account.getResourceGroups().size() > 0) {
			em.createNativeQuery(
					"DELETE FROM " + table_accounts_to_resource_group + " where ACCOUNT_ID = "
							+ account.getAccountId()).executeUpdate();
		}
		*/

		em.remove(account);
	}
	
	//used by modifyRoles
	public Account findAccount(Resource resource, User user) {
		logger.debug("Verifying whether there are account(s) attached to Resource name: "
			+ resource.getDisplayName()
			+ ", for user name: "
			+ user.getName());

		try {
			Query q = em.createNamedQuery("account.findOnResourceForUser").setParameter("user",user).setParameter("resource", resource);
			return (Account) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			logger.debug("Found account did not result any account, returning null.");
			return null;
		}
	}
	
	public void persistAccount(String accountName, String resourceUniqueName, String userName) {
		logger.debug("Persisting account '" + accountName + "', of resource '" + resourceUniqueName + ", associated to user '" + userName + "'");
		
		//TODO: What if the resource was not found?
		Resource foundResource = resourceManager.findResource(resourceUniqueName);

		Account acc = Account.factory(accountName, foundResource);

		
		if (userName != null) {
			User foundUser = userManager.findUser(userName);
			if (foundUser == null) {
				Log.warn("Could not find user name '" + userName + "' when persisting account name '" + accountName + "', on resource '" + resourceUniqueName + "', skipping user association.");
			} else {
				acc.setUser(foundUser);
			}
		}
		
		
		//em.persist(acc);
		persistAccount(acc);
	}
	
	public void persistAccount(Account account) {
		logger.debug("Persisting account '" + account.getName() + "', of resource '" + account.getResource().getDisplayName() + "'");
		
		//make sure account does not exist already in repository
		if (isAccountExists(account.getName(), account.getResource())) {
			logger.warn("Won't persist, account with name '" + account.getName() + "' on resource '" + account.getResource().getDisplayName() + "' already exists in repository!");
			return;
		}
		
		em.persist(account);
		
		
		//Keep audited account if set in configuration
        boolean createAuditedAccount = SysConf.getSysConf().getBoolean("accounts.audit_accounts_permanently");
        
        if (createAuditedAccount) {
            AuditedAccount aa = new AuditedAccount(account.getName(),account.getResource());
            em.merge(aa);
        }
        
		logger.debug("Successfully persisted account!");
	}
	
	public void updateAccount(Account account) {
		em.merge(account);
	}
	
	
	//used by resourceAttributeActionTools
	//public boolean isAccountExists(String accountName,String uniqueResourceName) {
	public boolean isAccountExists(String accountName,Resource resource) {
		logger.debug("Checking whether account name: '" + accountName
				+ "' On resource name: '" + resource.getDisplayName()
				+ "' exist or not...");

		
		Query q = null;
		if (resource.isCaseSensitive()) {
			 q = em.createNamedQuery(
				"account.isExistWithCase").setParameter("accountName",
						accountName).setParameter("resourceUniqueName",resource.getUniqueName());
		} else {
			q = em.createNamedQuery(
			"account.isExistIgnoreCase").setParameter("accountName",
					accountName.toUpperCase()).setParameter("resourceUniqueName",resource.getUniqueName());
		}

		
		Long num = (Long) q.getSingleResult();

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public boolean isAuditedAccountExists(String accountName,String uniqueResourceName) {
		logger.debug("Checking whether audited account name: '" + accountName
				+ "' On resource with unique Name: '" + uniqueResourceName
				+ "' exist or not...");
		
		// TODO Only set upper case when targets are none-case sensitive!
		Query q = em.createNamedQuery(
				"auditedAccount.isExists").setParameter("accountName",
				accountName.toUpperCase()).setParameter("resourceUniqueName",
				uniqueResourceName);

		
		Long num = (Long) q.getSingleResult();

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
	//NOTE: Should never be used in processes such as reconcile as it loads the resource every invocation
	public Account findAccount(String accountName, String resourceUniqueName) {
		Resource resource = resourceManager.findResource(resourceUniqueName);
		if (resource == null) {
			return null;
		}
		
		return findAccount(accountName, resource);
	}
	
	
	public Account findAccount(String accountName, Resource resource) {
		try {
			logger.debug("Finding Account in repository for name '" + accountName + "', in resource name '" + resource.getDisplayName() + "'");
			Query q = null;
			
			if (resource.isCaseSensitive()) {
				q = em.createNamedQuery("account.findByNameWithCase").setParameter("accountName", accountName).setParameter("resourceUniqueName", resource.getUniqueName());
			} else {
				q = em.createNamedQuery("account.findByNameIgnoreCase").setParameter("accountName", accountName.toUpperCase()).setParameter("resourceUniqueName", resource.getUniqueName());
			}
			
			return (Account) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			logger.info("FindAccount did not result any account name '" + accountName + "' on resource name '" + resource.getDisplayName() + "', returning null.");
			return null;
		} catch (NonUniqueResultException nure) {
			logger.warn("FindAccount found multiple accounts with name '" + accountName + "' on resource name '" + resource.getDisplayName() + "', returning null.");
			return null;
		}
	}
	
	//just a proxy with eager support (used for tests like creating tasks tests)
	public Account findAccountEagerly(String accountName, Resource resource) {
		Account acc = findAccount(accountName,resource);
		
		if (acc == null) return null;
		
		acc.getResource().getAttributes().size();
		acc.getAccountAttributes().size();
		
		for (AccountAttribute currAA : acc.getAccountAttributes()) {
			currAA.getValues().size();
		}
		
		return acc;
	}
	
	/*
	public Account findAccount(String accountName, String resourceUniqueName, boolean isResourceCaseSensitive){
		String namedQuery = (isResourceCaseSensitive ? "account.findByName" : "account.findByNameCaseNonSensitive");
		try {
			logger.debug("Finding Account in repository for name '" + accountName + "', in resource name '" + resourceUniqueName + "', regardless of the account name case");
			Query q = em.createNamedQuery(namedQuery).setParameter("accountName", accountName).setParameter("resourceUniqueName", resourceUniqueName);
			return (Account) q.getSingleResult();
		}
		catch (javax.persistence.NoResultException e) {
			logger.info("FindAccount did not result any account name '" + accountName + "' on resource name '" + resourceUniqueName + "', returning null.");
			return null;
		} catch (NonUniqueResultException nure) {
			logger.warn("FindAccount found multiple accounts with name '" + accountName + "' on resource name '" + resourceUniqueName + "', returning null.");
			return null;
		}
	}
	*/
	
	
	//TODO: Clean the ugly debugs
	public void associateAccountToUser(String accountName,
			String resourceUniqueName, String userName) throws OperationException {
		//try {
			// Load account
		logger.info("!!!!!!!!!!!!!!!!(1)");
		boolean isResourceCaseSensitive = false;
		Resource loadedResource = resourceManager.findResource(resourceUniqueName);
		if (loadedResource == null) {
			throw new OperationException("Could not find resource with unique name '" + resourceUniqueName + "'");
		}
		else {
			 isResourceCaseSensitive = loadedResource.isCaseSensitive();
			 logger.trace("The resource " + loadedResource.getDisplayName() + " is case sensitive - " + isResourceCaseSensitive);
		}
		logger.info("!!!!!!!!!!!!!!!!(2)");
			
			Account loadedAccount = findAccount(accountName,loadedResource);
			
			logger.info("!!!!!!!!!!!!!!!!(3)");
			if (loadedAccount == null) {
				throw new OperationException(
						"Could not find account name '"
						+ accountName + "', on Resource '"
						+ resourceUniqueName + "'");
			}

			// Make sure that the account is not associated to any user.
			if (loadedAccount.getUser() != null) {
				throw new OperationException(
						"Could not associate account named '"
								+ accountName
								+ "', on Resource name '"
								+ resourceUniqueName
								+ "', to User '"
								+ userName
								+ "' since the specified account already assciated to user name '"
								+ loadedAccount.getUser().getName() + "'");
			}
			logger.info("!!!!!!!!!!!!!!!!(4)");
			// Load user
			User loadedUser = userManager.findUser(userName);
			logger.info("!!!!!!!!!!!!!!!!(5)");
			if (loadedUser == null) {
				throw new OperationException("Could not associate account named '"
						+ accountName + "', on Resource name '" + resourceUniqueName
						+ "', to User '" + userName
						+ "' since the account does not exist!");
			}
			
			logger.info("!!!!!!!!!!!!!!!!(6)");
			// Perform the assocaition!
			loadedAccount.setUser(loadedUser);
			// updateAccount(loadedAccount);
			em.merge(loadedAccount);
		/*} catch (NonUniqueResultException ex) {
			throw new OperationException("Could not associate account named '"
					+ accountName + "', on Resource name '" + resourceUniqueName
					+ "', to User '" + userName
					+ "' since more than one account exist on resource!");
		}*/
			
			
			logger.info("!!!!!!!!!!!!!!!!(7)");
	}
	
	
	
	
	//fast persis for reconciles
	//TODO: Should we check here if account already exists before persisting?
	public void persistAccountEntities(Collection<Account> accountsToPersist) {
		for (Account currAccount : accountsToPersist) {
			em.persist(currAccount);
		}
	}

	public void removeAccountEntities(Collection<Account> accountsToRemove) {
		for (Account currAccount : accountsToRemove) {
			removeAccountEntity(currAccount);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Collection<Account> findAccounts(User user,Collection<ResourceAttribute> resourceAttributes) {
		Collection<Account> accountList = new HashSet<Account>();
		for (ResourceAttribute currRA : resourceAttributes) {
			if (logger.isDebugEnabled()) {
				boolean isUserHasAccountOnResource = user.isUserHasAccount(currRA.getResource());
				logger.debug("Is user: '"+ user.getName()+ "', has accounts on resource named: "
						+ currRA.getResource().getDisplayName() + "?: "
						+ isUserHasAccountOnResource);
			}
			
			accountList.addAll(user.findAccounts(currRA.getResource()));
		}
		
		return accountList;
	}
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public Account findAccountById(long accountId) {
		try {
			return (Account) em.createNamedQuery("findAccountById").setHint(
					"toplink.refresh", "true").setParameter("accountId",
					accountId).getSingleResult();
		} catch (NoResultException e) {
			throw new NoResultException(
					"No such result exception occured: Couldnt load Account for ID number: "
							+ accountId);
		} catch (Exception e) {
			throw new EJBException(e.getMessage());
		}
	}



	@Deprecated
	public boolean isAccountExistOnTarget(String accountName,
			Resource resource) {
		logger.debug("Checking whether account name: '" + accountName
				+ "' exist on Resource Name: '"
				+ resource.getDisplayName() + "' or not...");
		Query q = em.createNamedQuery("isAccountExistOnTarget").setHint(
				"toplink.refresh", "true").setParameter("accountName",
				accountName).setParameter("resource", resource);

		Long num = (Long) q.getSingleResult();

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	}


	@Deprecated
	public boolean isStaleAccountExists(String accountName,
			Resource resource) {
		logger.debug("Checking whether account name: '" + accountName
				+ "' is stale account and exist on Resource Name: '"
				+ resource.getDisplayName() + "' or not...");
		Query q = em.createNamedQuery("isStaleAccountExistsOnTarget").setHint(
				"toplink.refresh", "true").setParameter("accountName",
				accountName).setParameter("resource", resource);

		Long num = (Long) q.getSingleResult();

		if (num == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Deprecated
	public Collection<Account> findAccounts(String wildCardAccountName) {
		// String searchPattern = wildCardAccountName == null ? "%" : '%' +
		// wildCardAccountName
		// .toLowerCase().replace('*', '%') + '%';
		String searchPattern = wildCardAccountName.toUpperCase().replace('*',
				'%');

		logger.debug("Checking whether accounts for wildCard string: '"
				+ searchPattern + "' exist on ALL targets or not!");

		Query q = em.createNamedQuery("account.searchByString").setHint(
				"toplink.refresh", "true").setParameter("searchString",
				searchPattern);

		return q.getResultList();
	}
	
	
	@Deprecated
	public Collection<AuditedAccount> findAuditedAccounts(String wildCardAccountName) {
		// String searchPattern = wildCardAccountName == null ? "%" : '%' +
		// wildCardAccountName
		// .toLowerCase().replace('*', '%') + '%';
		String searchPattern = wildCardAccountName.toUpperCase().replace('*','%');

		logger.debug("Checking whether accounts for wildCard string: '"
				+ searchPattern + "' exist on ALL targets or not!");

		Query q = em.createNamedQuery("auditedAccount.searchByString").setHint(
				"toplink.refresh", "true").setParameter("searchString",
				searchPattern);

		return q.getResultList();
	}
	
	

	@Deprecated
	public void createAccount(Account account) {
		logger.debug("Creating account for account name: " + account.getName());
		// Resource ts = em.merge(account.getResource());
		// account.setResource(ts);
		em.merge(account);

		// Keep audited account if set in configuration
		boolean createAuditedAccount = SysConf.getSysConf().getBoolean(
				"accounts.audit_accounts_permanently");

		if (createAuditedAccount) {
			AuditedAccount aa = new AuditedAccount(account.getName(), account
					.getResource());
			em.merge(aa);
		}
	}


	@Deprecated
	public Account loadVirtualAccountAttributes(Account account,
			boolean onlySynchronizedAttributesInReconciliationProcess) {
		// Get all ResourceAttributes for the corresponding target system of
		// this account
		logger.debug("Loading virtual accounts for account name: "
				+ account.getName());

		if (account.getUser() == null) {
			logger
					.info("Account is not associated with User, skipping loading...");
			return account;
		}

		// Since this method might be called externally (by actions) it is a
		// must to re-load the user since the passed object might be loaded
		// lazily.
		Account loadedAccount = findAccountById(account.getAccountId());

		// Per Resource attribute:
		// - Check if the attribute is mapped directly to an Identity Attribute
		// or not
		// - if mapped directly, get the corresponding Identity attribute value
		// - otherwise, factory the attached converter, execute it and return
		// the value after conversion
		//
		boolean isLoadAttribute = false;
		for (ResourceAttributeBase currResourceAttribute : loadedAccount
				.getResource().getResourceAttributes()) {
			if (onlySynchronizedAttributesInReconciliationProcess) {
				if (currResourceAttribute
						.isSynced()) {
					isLoadAttribute = true;
				} else {
					isLoadAttribute = false;
				}
			} else {
				isLoadAttribute = true;
			}

			if (isLoadAttribute) {
				// Add the attribute into the map, set the key of the map as the
				// name of the attribute
				try {
					Attribute attr = loadVirtualAccountAttribute(loadedAccount
							.getUser(), currResourceAttribute);

					logger
							.debug("Adding virtual account attribute for account name: "
									+ account.getName()
									+ ", attribute name: "
									+ attr.getName()
									+ ", value: "
									+ attr.getValueAsString());
					//JBloadedAccount.addAccountAttribute(attr);

				} catch (NoUserIdentityAttributeValueException nuiave) {
					logger
							.warn("Skipping target system attribute while loading virtual account attribute with message: "
									+ nuiave.getMessage());
				} catch (NoUserIdentityAttributeFoundException nuiafe) {
					logger
							.warn("Skipping target system attribute while loading virtual account attribute with message: "
									+ nuiafe.getMessage());
				} catch (LoadingVirtualAccountAttributeException lvaae) {
					logger
							.warn("Skipping target system attribute while loading virtual account attribute with message: "
									+ lvaae.getMessage());
				}
				/*
				 * catch (MultipleAttributeValueVaiolation mavv) {
				 * logger.warn(mavv.getMessage()); }
				 */
			}
		}

		logger
				.debug("-END- of loading virtual attributes process for account name: '"
						+ account.getName()
						+ "', loaded attributes with amount: '"
						+ account.getAttributes().size() + "'");
		// Indicated that the account attributes were loaded
		loadedAccount.setActiveAttributesLoaded(true);

		return loadedAccount;
	}

	@Deprecated
	public Attribute loadVirtualAccountAttribute(User user,
			ResourceAttributeBase tsa)
			throws NoUserIdentityAttributeValueException,
			NoUserIdentityAttributeFoundException, ConverterProcessFailure,
			LoadingVirtualAccountAttributeException {
		return loadVirtualAccountAttribute(user, tsa, null);
	}

	@Deprecated
	public Attribute loadVirtualAccountAttribute(User user,
			ResourceAttributeBase tsa, AccountAttributeConverterInterface atci)
			throws NoUserIdentityAttributeValueException,
			NoUserIdentityAttributeFoundException, ConverterProcessFailure,
			LoadingVirtualAccountAttributeException {

		// TODO Too much exceptions being threw here, should only throw one
		// exception which encapsulate all of these exceptions!!
		logger.debug("Getting virtual account attribute for user: "
				+ user.getName() + ", for ResourceAttribute name: "
				+ tsa.getDisplayName());

		// 30-may-07(AsaF): Loading the user per call seriously slow down the
		// method as this might be invoked MANY times
		// Canceled loading the user, the invokers of this method should supply
		// a fully updated loaded user!
		// Since this method might be called externally (by actions) it is a
		// must to re-load the user since the passed object might be loaded
		// lazily.
		// User loadedUser = userm.findUserById(user.getUserId());

		// logger.debugst("Original Method User object: " + user);
		// logger.debugst("Loaded User object: " + loadedUser);

		// Attribute attr = new Attribute();
		// attr.setValue(loadUserAttribute(user,
		// tsam.getIdentityAttribute().getName()).getValue());
		// return attr;
		Attribute attribute = new Attribute();
		boolean isUserIdentityAttributeMapping = true;

		try {
			logger
					.trace("Verifying whether the TSA is mapped to an IA or not.");
			// Verify whether the TSA was mapped to an IA or not, if not, skip
			// the search for a corresponding UserIdentityAttribute.
			if (tsa.getIdentityAttribute() == null) {
				// If mapped directly, then throw an exception.
				if (tsa.isMapDirectly()) {
					logger
							.warn("Mapped directly, but has no IA assoication, throwing an exception.");
					throw new LoadingVirtualAccountAttributeException(
							"Target System Attribute ID: '"
									+ tsa.getResourceAttributeId()
									+ "named: '"
									+ tsa.getDisplayName()
									+ "' is not mapped to any Identity Attribute but flagged as 'Mapped Directly!', please map the Target Attribute to an Identity Attribute or remove the 'Mapped Directly' flag.");
				}

				isUserIdentityAttributeMapping = false;
			}

			// If the Target Attribute was mapped to an IdentityAttribute, then
			// search a corresponding UIA object.
			UserIdentityAttribute uia = null;
			if (isUserIdentityAttributeMapping) {
				// UserIdentityAttribute uia =
				// userm.getUserIdentityAttribute(user,tsa.getIdentityAttribute());
				boolean isUiaFound = false;

				logger
						.trace("Seeking for a UserIdentityAttribute that correspond to TSA's IA assoication named: '"
								+ tsa.getIdentityAttribute().getDisplayName()
								+ "'");
				// logger.debugst("User Identity Attributes size: '" +
				// loadedUser.getUserIdentityAttributes().size() + "'");
				logger.trace("User Identity Attributes size: '"
						+ user.getUserIdentityAttributes().size() + "'");

				// //for (UserIdentityAttribute currUia :
				// loadedUser.getUserIdentityAttributes()) {
				// for (UserIdentityAttribute currUia :
				// user.getUserIdentityAttributes()) {
				// logger.info("CURR UIA id: " +
				// currUia.getUserIdentityAttributeId() + ", currUIA's IA: " +
				// currUia.getIdentityAttribute() + ", TSA's IA OBJ: " +
				// tsa.getIdentityAttribute());
				// //logger.info("Curr UIA IA ID: " +
				// currUia.getIdentityAttribute().getIdentityAttributeId() + ",
				// trying to find match to TSA's attached IA ID: " +
				// tsa.getIdentityAttribute().getIdentityAttributeId());
				//
				// //Spent a hour on this! if 'Long' is an object (Long) and not
				// a primitive datatype (long) must use 'x Long.equals(y Long)'
				// in order to do comparation! and not '=='
				// //if (
				// currUia.getIdentityAttribute().getIdentityAttributeId() ==
				// tsa.getIdentityAttribute().getIdentityAttributeId() ) {
				// if (
				// currUia.getIdentityAttribute().getIdentityAttributeId().equals(tsa.getIdentityAttribute().getIdentityAttributeId()))
				// {
				// uia = currUia;
				// isUiaFound = true;
				// break;
				// }
				// }

				uia = user.getUserIdentityAttribute(tsa.getIdentityAttribute());

				// Make sure the attribute has at least -one- values,
				// otherwise
				// throw an exception, this skips the attribute from being
				// loaded
				if (uia.getValues().size() < 1) {
					throw new NoUserIdentityAttributeValueException(uia);
				}
				
				if (uia == null) {
					logger.trace("NO UserIdentityAttribute was not found! throwing an exception!");
					throw new NoUserIdentityAttributeFoundException("No UIA was found!");
				}
			}

			// Set the value to the attribute value of the corresponding
			// UserAttribute
			// If the mapping is one to one, just set the value to the
			// attribute, otherwise call the proper converter and set the
			// converted value

			// The name of the attribute should be the corresponding
			// Resource attribute name
			if (tsa.isMapDirectly()) {
				if (isUserIdentityAttributeMapping) {
					attribute.setValueByUserIdentityAttribute(uia);
				}
				// Mistake -> fixed in 22/08/06 since 'reconcile' tried to find
				// the attribute by the TS name and couldnt ->
				// attribute.setName(uia.getName());
				attribute.setName(tsa.getUniqueName());
				// try {
				logger
						.info("Target attribute is mapped directly, retrieving value of attribute name: "
								+ attribute.getName()
								+ ", with (first) value: "
								+ attribute.getValueAsString());
				return attribute;
				/*
				 * } catch (MultipleAttributeValueVaiolation mavv) {
				 * logger.warn(mavv.getMessage()); return null; }
				 */
			} else {

				// A converter is needed, factory the converter and expect it to
				// return a value to be set
				try {
					// AccountAttributeConverterInterface aac =
					// FactoryAccountAttributeConverter.factoryConverter(tsa,
					// loadedUser, uia);

					// If not specified a converter, then create one
					if (atci == null) {
						atci = FactoryAccountAttributeConverter
								.factoryConverter(tsa, user, uia);
					} else {
						/*JB!!!!!!!!!
						atci = FactoryAccountAttributeConverter
								.initConverterReferences(atci, tsa, user, uia);
						*/
					}

					logger.trace("Executing convert() for convertor object: "
							+ atci);

					// Goddamn toplink, it auto merge the collection even
					// without calling merge explicity!
					// uia.getFirstAttributeValue().setValue("MOOOO");

					// Execute converter, expect a true to be returned
					if (!atci.convert()) {

						// clean converter references as this method might be
						// invoked million times by Reconcilliation processes
						atci = null;

						throw new ConverterProcessFailure(
								"Converter action returned a fail signal for target attribute name: "
										+ tsa.getDisplayName());
					}
					// Converted ok, check that the converter object has a
					// ResultAttribute which is not null
					else {
						logger
								.trace("Checking whether the converter has returned a resulted attribute object or not: "
										+ atci.getResultAttribute());
						if (atci.getResultAttribute() != null) {
							attribute = atci.getResultAttribute();
							attribute.setName(tsa.getUniqueName());

							// attribute.setValueByUserIdentityAttribute(resultedAttribute);
							// attribute.setName(resultedAttribute.getName());
							logger
									.trace("Attribute is NOT mapped directly, -succesfully- retrieved a converted attribute name: "
											+ tsa.getDisplayName()
											+ ", first value: "
											+ attribute.getAsString());

						} else {
							throw new ConverterProcessFailure(
									"Converter action returned a SUCCESS signal BUT did not assign a ResultAttribute for Target-Attribute name: "
											+ tsa.getDisplayName()
											+ ", must call setResultAttribute(Attribute)");
						}
					}

					// clean converter references as this method might be
					// invoked million times by Reconcilliation processes
					FactoryAccountAttributeConverter.destroyConverter(atci);
					atci = null;

					// VERY important, I assume the transaction ends here (at
					// the end of th EJB call),
					// em.clear() must be called, since cascade.MERGE is set on
					// the attributes and their values, otherwise, the
					// modification done to the attribute by the converter
					// Will be persisted when the transaction ends by the
					// persistence provider.
					// WTF? what I had in my mind when I set this?!?! ->
					// em.clear();

					return attribute;
				} catch (ScriptLoadingException sle) {
					logger
							.warn("WARNING: Couldn't load a needed converter: "
									+ sle.getMessage());
					throw new LoadingVirtualAccountAttributeException(sle
							.getMessage());
				} catch (java.lang.AbstractMethodError ame) {
					logger.warn("an AbstractMethodError has occured!!!");
					// TODO: This is totally wrong, it should throw another
					// action that there was an error to load the virtual
					// account
					// Think maybe of a way to execute scripts and catch their
					// exceptions more gently.
					throw new LoadingVirtualAccountAttributeException(ame
							.getMessage());
				} catch (groovy.lang.MissingPropertyException mpe) {
					logger.warn("MissingPropertyException has occured!!!");
					throw new LoadingVirtualAccountAttributeException(mpe
							.getMessage());
				} catch (groovy.lang.GroovyRuntimeException gre) {
					logger.warn("GroovyRunTimeException has occured!!!");
					throw new LoadingVirtualAccountAttributeException(gre
							.getMessage());
				} catch (Exception e) {
					logger.warn("A generic exception has occured!");
					logger.warn("Cause: " + e.getCause());
					logger.warn("Exception Message: " + e);
					logger.warn("Printing stack trace: ");
					e.printStackTrace(System.out);

					throw new LoadingVirtualAccountAttributeException(e
							.getMessage());
				}
			}
		} catch (NoUserIdentityAttributeValueException nuiave) {
			throw nuiave;
		}
	}

	@Deprecated
	public AccountAttributeConverterInterface factoryResourceAttributeConverter(
			ResourceAttribute tsa) throws ObjectFactoryException {
		try {
			if (tsa.isMapDirectly()) {
				throw new ObjectFactoryException(
						"Could not factor Attribute Converter since ResourceAttribute named '"
								+ tsa.getDisplayName()
								+ "', related to Target named '"
								+ tsa.getResource().getDisplayName()
								+ "' is flagged as 'mapped directly'!");
			}

			return FactoryAccountAttributeConverter.factoryConverter(tsa);
		} catch (ScriptLoadingException sle) {
			throw new ObjectFactoryException(sle);
		}
	}


	/*
	 * Moved to user public Attribute getVirtualAcccountAttribute(Account
	 * account, ResourceAttribute tsa) { //Factory a new Attribute Attribute
	 * attr = new Attribute();
	 * 
	 * //Set its name as the ResourceAttribute name
	 * attr.setName(tsa.getName());
	 * 
	 * attr.setValue(userm.getVirtualAccountAttribute(account.getUser(),tsa));
	 * 
	 * System.out.println("Factoried a virtual attribute for account name: " +
	 * account.getName() + ", attriubte name: " + attr.getName() + ", with
	 * value: " + attr.getStringValue()); return attr; }
	 */

	/*
	 * ########### ACCOUNT ACTIONS ##########
	 */

	/*
	@Deprecated
	public Task disableAccountTask(Account account, BulkTask bulkTask,
			Request request, User requester) throws TaskCreationException {
		logger
				.info("START of Factoring/Executing 'Disable Account' task for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());

		ActionManager am = new ActionManager();

		try {
			TaskDefinition td;

			if (account.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("DISABLE_ACCOUNT_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("DISABLE_ACCOUNT");
			}

			Task task = td.factoryTask();
			task.setDescription("Disable Account Task");

			if (requester != null) {
				//TODO: FIX task.setRequester(requester);
			}

			// Start a log trail for the action
			task.addLog("INFO", "Disable Account event",
					"Task to disable account for account name: '"
							+ account.getName() + "', on Target-System: "
							+ account.getResource().getDisplayName());

			// Extremely important to load a new account, since part of the
			// lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			Account loadedAccount = findAccountById(account.getAccountId());

			// /Important, clear the entityManager since actionfactory clear
			// referesnces from the managed object!
			// (This is just a safe step, as only the cloned's entity references
			// get cleared.
			em.clear();

			// Extremely important to clean references from a cloned
			// account->user, otherwise methods that make a usage of
			// account->user original entity might get unexpected data.
			Account clonedAccount = loadedAccount.clone();
			clonedAccount.getUser().cleanReferences();

			DisableAccountActionFactory daaf = new DisableAccountActionFactory(
					clonedAccount);
			ResourceAccountActionInterface disableAction = daaf.factory();

			//TODO: FIX task.serializeAsTask(disableAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				task.setScript(am
						.getDisableAccountActionScriptContent(clonedAccount));
			}

			// If this task is a part of a bulk task, then set it
			if (bulkTask != null) {
				task.setBulkTask(bulkTask);
			}

			return task;
		} catch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			throw new TaskCreationException(sle.getMessage());
		} catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			throw new TaskCreationException(cce.getMessage());
		} catch (ActionFactoryException afe) {
			logger.warn(afe.getMessage());
			throw new TaskCreationException(afe.getMessage());
		}
	}
	*/

	/*
	@Deprecated
	public Long disableAccount(Account account, BulkTask bulkTask,
			Request request, User requester) throws TaskCreationException {
		logger.debug("Disabling account named: ')" + account.getName() + "'");
		Task task = disableAccountTask(account, bulkTask, request, requester);
		// Persist the task
		tm.persistTask(task);
		em.flush();

		logger.debug("Successfully persisted task with ID: '" + task.getTaskId()
				+ "'");
		return task.getTaskId();
	}
	*/
	
	/*
	@Deprecated
	public void disableAccounts(Set<Account> accounts, Request request, User requester) throws OperationException {
		BulkTask bt = BulkTask.factory("Disable Accounts");
		for (Account acc : accounts) {
			try {
				bt.addTask(disableAccountTask(acc, bt, request, requester));
			} catch (TaskCreationException e) {
				throw new OperationException("Failed to disable accounts: " + e.toString());
			}			
		}
		
		em.persist(bt);
	}
	*/

	
/*
	@Deprecated
	public Task enableAccountTask(Account account, BulkTask bulkTask,
			Request request) throws TaskCreationException {
		logger
				.debug("START of Factoring/Executing 'Enable Account' task for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());

		ActionManager am = new ActionManager();

		try {
			TaskDefinition td;

			if (account.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("ENABLE_ACCOUNT_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("ENABLE_ACCOUNT");
			}

			Task task = td.factoryTask();
			task.setDescription("Enable Account named '" + account.getName()
					+ "', on Target System '"
					+ account.getResource().getDisplayName() + "'");

			// Start a log trail for the action
			task.addLog("INFO", "Enable Account event",
					"Task to enable account for account name: '"
							+ account.getName() + "', on Target-System: "
							+ account.getResource().getDisplayName());

			// Extremely important to load a new account, since part of the
			// lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			Account loadedAccount = findAccountById(account.getAccountId());

			// /Important, clear the entityManager since actionfactory clear
			// referesnces from the managed object!
			// (This is just a safe step, as only the cloned's entity references
			// get cleared.
			em.clear();

			// Extremely important to clean references from a cloned
			// account->user, otherwise methods that make a usage of
			// account->user original entity might get unexpected data.
			Account clonedAccount = loadedAccount.clone();
			clonedAccount.getUser().cleanReferences();

			EnableAccountActionFactory eaaf = new EnableAccountActionFactory(
					clonedAccount);
			ResourceAccountActionInterface enableAction = eaaf.factory();

			task.serializeAsTask(enableAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				task.setScript(am
						.getEnableAccountActionScriptContent(clonedAccount));
			}

			// If this task is a part of a bulk task, then set it
			if (bulkTask != null) {
				task.setBulkTask(bulkTask);
			}

			return task;
		} catch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			throw new TaskCreationException(sle.getMessage());
		} catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			throw new TaskCreationException(cce.getMessage());
		} catch (ActionFactoryException afe) {
			logger.warn(afe.getMessage());
			throw new TaskCreationException(afe.getMessage());
		}
	}

	@Deprecated
	public Long enableAccount(Account account, BulkTask bulkTask,
			Request request) throws TaskCreationException {
		logger.debug("Enabling account named: ')" + account.getName() + "'");
		Task task = enableAccountTask(account, bulkTask, request);
		// Persist the task
		tm.persistTask(task);
		em.flush();

		logger.debug("Successfully persisted task with ID: '" + task.getTaskId()
				+ "'");
		return task.getTaskId();
	}
	*/
	
	/*
	@Deprecated
	public void enableAccounts(Set<Account> accounts, Request request, User requester) throws OperationException {
		BulkTask bt = BulkTask.factory("Enable Accounts");
		
		for (Account acc : accounts) {
			try {
				bt.addTask(enableAccountTask(acc, bt, request));
			} catch (TaskCreationException e) {
				throw new OperationException("Failed to enable accounts: " + e.toString());
			}			
		}
		
		em.persist(bt);
	}
	*/
	
	/*
	@Deprecated
	public Task accountResetPasswordTask(Account account, String password)
			throws TaskCreationException, PasswordValidationException {
		logger
				.info("START of Factoring/Executing 'Account Reset Password' task for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());

		// Validate the password
		account.getResource().getPasswordPolicyContainer()
				.getPasswordPolicy().validate(password, account.getName());

		ActionManager am = new ActionManager();

		try {
			TaskDefinition td;

			if (account.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("ACCOUNT_RESET_PASSWORD_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("ACCOUNT_RESET_PASSWORD");
			}

			Task task = td.factoryTask();

			// Start a log trail for the action
			task.addLog("INFO", "Account Reset Password event",
					"Task to reset password for account name: '"
							+ account.getName() + "', on Target-System: "
							+ account.getResource().getDisplayName());

			// Extremely important to load a new account, since part of the
			// lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			Account loadedAccount = findAccountById(account.getAccountId());

			// /Important, clear the entityManager since actionfactory clear
			// referesnces from the managed object!
			// (This is just a safe step, as only the cloned's entity references
			// get cleared.
			em.clear();

			// Extremely important to clean references from a cloned
			// account->user, otherwise methods that make a usage of
			// account->user original entity might get unexpected data.
			Account clonedAccount = loadedAccount.clone();
			clonedAccount.getUser().cleanReferences();

			ResourceAccountActionInterface resetPasswordAction = am
					.factoryAccountResetPasswordAction(clonedAccount, password);

			//task.serializeAsTask(resetPasswordAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				//task.setScript(am.getResetPasswordAccountActionScriptContent(clonedAccount));
			}

			return task;
		} catch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			throw new TaskCreationException(sle.getMessage());
		} catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			throw new TaskCreationException(cce.getMessage());
		}
		/*
		 * catch (ActionFactoryException afe) {
		 * logger.warn(afe.getMessage()); throw new
		 * TaskCreationException(afe.getMessage()); }
		 */
	//}

  
	/*
	@Deprecated
	public Long accountResetPassword(Account account, String password)
			throws TaskCreationException, PasswordValidationException {
		Task task = accountResetPasswordTask(account, password);
		// Persist the task
		tm.persistTask(task);

		em.flush();

		return task.getTaskId();
	}
	*/

	/*
	@Deprecated
	public Task createAccountTask(User user, Resource ts,
			BulkTask bulkTask, StringBuffer outputGeneratedAccountId)
			throws TaskCreationException {
		try {
			logger
					.info("Started method 'createAccountTask' in Account Service...");

			// Generate a new AccountID
			String generatedAccountId = resourceManager.generateNewAccountId(ts, user);

			if ((generatedAccountId == null)
					|| (generatedAccountId.length() == 0)) {
				throw new TaskCreationException(
						"Could not create task since there was a problem to generate a new account ID for User: '"
								+ user.getName()
								+ "', on target system: '"
								+ ts.getDisplayName() + "'");
			}

			logger.debug("Generated account ID (As a part of create process): "
					+ outputGeneratedAccountId);
			// System.out.println("Generated account ID LENGTH: " +
			// outputGeneratedAccountId.length());
			// System.out.println("GENERATED ACCOUNT ID !!!!: " +
			// generatedAccountId);
			if (outputGeneratedAccountId != null) {
				outputGeneratedAccountId.replace(0, outputGeneratedAccountId
						.length(), generatedAccountId);
			}

			logger
					.debug("START of Factoring/Executing 'Create Account' task for Generated account ID: "
							+ "'"
							+ generatedAccountId
							+ "'"
							+ " for User name: "
							+ user.getName()
							+ ", on target system: " + ts.getDisplayName());

			ActionManager am = new ActionManager();

			TaskDefinition td;
			if (ts.getResourceType().isScripted()) {
				td = tm.findTaskDefinition("CREATE_ACCOUNT_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("CREATE_ACCOUNT");
			}

			Task task = td.factoryTask();
			task.setDescription("Create Account Task for User named '"
					+ user.getName() + "', on Target-System named '"
					+ ts.getDisplayName() + "'");

			// Start a log trail for the action
			task.addLog("INFO", "Create Account event",
					"Task to create account for account name: '"
							+ generatedAccountId + "', on Target-System: "
							+ ts.getDisplayName() + ", for user: "
							+ user.getName());

			// Extremely important to load a new ts/user, since part of the
			// lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			User loadedUser = userManager.findUserById(user.getUserId());
			Resource loadedResource = resourceManager.findResourceById(ts
					.getResourceId());

			// /Important, clear the entityManager since actionfactory clear
			// referesnces from the managed object!
			// (This is just a safe step, as only the cloned's entity references
			// get cleared.
			em.clear();

			// ResourceAccountActionInterface createAction =
			// am.factoryCreateAccountAction(ts,user,generatedAccountId,
			// userRole);
			ResourceAccountActionInterface createAction = am
					.factoryCreateAccountAction(loadedResource.clone(),
							loadedUser.clone(), generatedAccountId);

			//JBtask.serializeAsTask(createAction);
			task.setExpectedExecutionDate(createAction
					.getExpectedExecutionDate());
			// If this task is a part of a bulk task, then set it
			if (bulkTask != null) {
				task.setBulkTask(bulkTask);
			}

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				// System.out.println("Create action action is scripted base,
				// getting the content of the script and keep it into the
				// task");
				//JBtask.setScript(am.getCreateAccountActionScriptContent(ts));
			}

			return task;
		} catch (ActionFactoryException afe) {
			String shortErrMsg = "Could not create account on Resource: "
					+ ts.getDisplayName() + ", For user" + user.getName();
			/*cum.addEventLog("ACCOUNT", "FAILURE", "WARNING", shortErrMsg,
					shortErrMsg + ", failed with message: " + afe.getMessage());*/
	
		//	logger.warn(afe.getMessage());
			// return false;
			/*throw new TaskCreationException(afe.getMessage());
		} catch (AccountIdGenerationException aige) {
			String shortErrMsg = "Could not create account on Resource: "
					+ ts.getDisplayName() + ", For user" + user.getName();
			
			
			/*cum
					.addEventLog("ACCOUNT", "FAILURE", "WARNING", shortErrMsg,
							shortErrMsg + ", failed with message: "
									+ aige.getMessage());*/
			//logger.warn(aige.getMessage());
			/*cum.addEventLog("ACCOUNT", "FAILURE", "WARNING",
					"Could not create account on Resource: "
							+ ts.getDisplayName() + ", For user"
							+ user.getName() + ", failed with message: "
							+ aige.getMessage(), null);*/
			//throw new TaskCreationException(aige.getMessage());
		//}
		// TODO: Should not get this such an exception! find a better soltuion.
		/*JBcatch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			cum.addEventLog("ACCOUNT", "FAILURE", "WARNING",
					"Could not create account on Resource: "
							+ ts.getDisplayName() + ", For user"
							+ user.getName() + ", failed with message: "
							+ sle.getMessage(), null);
			throw new TaskCreationException(sle.getMessage());
		}*/ /*catch (NoResultFoundException nrfe) {
			throw new TaskCreationException(nrfe.getMessage());
		}
	}*/


  /*
  @Deprecated
	public Long createAccount(User user, Resource ts, BulkTask bulkTask,
			StringBuffer outputGeneratedAccountId) throws TaskCreationException {
		Task task = createAccountTask(user, ts, bulkTask,
				outputGeneratedAccountId);

		// Persist the task
		tm.persistTask(task);
		// Flush just after persisting, otherwise task.getUserId will return
		// null (since by default persistence provider flushes after the method
		// execution ends)
		em.flush();

		return task.getTaskId();
	}

	@Deprecated
	public boolean updateAccount(Account account) {
		logger
				.debug("START of Factoring/Executing 'Update Account' task for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());
		ActionManager am = new ActionManager();

		try {
			TaskDefinition td;

			if (account.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("UPDATE_ACCOUNT_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("UPDATE_ACCOUNT");
			}

			Task task = td.factoryTask();
			task.setDescription("Update Account named '" + account.getName()
					+ "', on Target System '"
					+ account.getResource().getDisplayName() + "'");

			// Start a log trail for the action
			task.addLog("INFO", "Update Account event",
					"Task to update account for account name: '"
							+ account.getName() + "', on Target-System: "
							+ account.getResource().getDisplayName());

			// Extremely important to load a new account, since part of the
			// lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			Account loadedAccount = findAccountById(account.getAccountId());

			ResourceAccountActionInterface updateAction = am
					.factoryUpdateAccountAction(loadedAccount);

			//JBtask.serializeAsTask(updateAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				//JBtask.setScript(am.getUpdateAccountActionScriptContent(account));
			}

			// Persist the task
			tm.persistTask(task);

			return true;
		} /*JBcatch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			return false;
		}*/ /*catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			return false;
		} catch (ActionFactoryException afe) {
			logger.warn(afe.getMessage());
			return false;
		}
	}*/

  /*
	@Deprecated
	public boolean accountStatus(Account account, StringBuffer statusString) {
		logger
				.info("START of Factoring/Executing 'Account Status' action for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());
		ActionManager am = new ActionManager();
		try {
			ResourceAccountActionInterface actionAccountStatus = am
					.factoryAccountStatusAction(account);
			try {
				boolean status = actionAccountStatus.__execute__();
				logger.debug("Account status result #: "
						+ actionAccountStatus.getActionResult().size());
				// Hopefully in the script the actionResult was cleaned before
				// the status string was set, otherwise it'll return here a
				// HashMap containing the reuslts from the query
				// Which wont get displayed. the script must call
				// actionResult.clear() before setting the status as a string
				// into the actionResult list.
				if (actionAccountStatus.getActionResult().size() > 0) {
					logger.debug("Account status result #: "
							+ actionAccountStatus.getActionResult().size());
					String statusFromAction = (String) actionAccountStatus
							.getActionResult().iterator().next();
					statusString.append(statusFromAction);
					System.out.println("Account status for account name ' "
							+ account.getName() + "' is: " + statusFromAction);
				} else {
					statusString.append("Not Applicable");
				}
				logger
						.debug("END of EXECUTION 'Account Status' action for account name: "
								+ account.getName()
								+ ", on target system: "
								+ account.getResource().getDisplayName());

				return status;
			} catch (ActionFailureException afe) {
				logger.warn("Action was failed, exiting with message: "
						+ afe.getMessage());
				// Set the status to the failure message, we'd like to show in
				// the user interface what went wrong
				statusString.append(afe.getMessage());

				String shortMessage = "Failed to check account status for account name: "
						+ account.getName()
						+ ", on target: "
						+ account.getResource().getDisplayName();
				String detailedMessage = shortMessage
						+ ", Action was failed with message: "
						+ afe.getMessage();
				// last place before view to log an event
				//cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMessage,detailedMessage);

				return false;
			}
		} catch (ActionFactoryException afe) {
			logger
					.warn("Could not factor account status check, failed with message: "
							+ afe.getMessage());

			String shortMessage = "Failed to get account status for account name: "
					+ account.getName()
					+ ", on target: "
					+ account.getResource().getDisplayName();
			String detailedMessage = shortMessage
					+ ", Action was failed with message: " + afe.getMessage();
			// last place before view to log an event
			//cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMessage,detailedMessage);

			return false;
		}
		// Catch any other exception (this is the last place to do so)
		catch (Exception e) {
			String shortMsg = "An exception was occured while trying to determine the status of the account";
			logger
					.warn("An exception was occured while trying to determine the status of the account, failure message: "
							+ e.getMessage());
			// last place before view to log an event
			//cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMsg, shortMsg+ ", failure message is: " + e.getMessage());
			return false;
		}
		// This is threw when the Adpater could not be factored
		/*
		 * catch (FactoryException fe) { logger .warning("Adapter class was not
		 * found while trying to factory with message: " + fe.getMessage());
		 * 
		 * String shortMessage = "Failed to get account status for account name: " +
		 * account.getName() + ", on target: " +
		 * account.getResource().getDisplayName(); String detailedMessage =
		 * shortMessage + ", Action was failed with message: " +
		 * fe.getMessage(); // last place before view to log an event
		 * cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMessage,
		 * detailedMessage);
		 * 
		 * return false; }
		 */
	//}

	/*
	@Deprecated
	public boolean authAccount(Account account, String password) {
		logger
				.debug("START of Factoring/Executing 'Account Authentication' action for account name: "
						+ account.getName()
						+ ", on target system: "
						+ account.getResource().getDisplayName());
		ActionManager am = new ActionManager();
		try {
			ResourceAccountActionInterface actionAuthAccount = am
					.factoryAccountAuthentication(account, password);
			try {
				boolean status = actionAuthAccount.__execute__();
				logger
						.debug("END of EXECUTION 'Account Authentication' action for account name: "
								+ account.getName()
								+ ", on target system: "
								+ account.getResource().getDisplayName());
				return status;
			} catch (ActionFailureException afe) {
				logger.error("Action was failed, exiting with message: "
						+ afe.getMessage());

				String shortMessage = "Failed to authenticate account name: "
						+ account.getName() + ", on target: "
						+ account.getResource().getDisplayName();
				String detailedMessage = shortMessage
						+ ", Action was failed with message: "
						+ afe.getMessage();
				// last place before view to log an event
				//cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMessage,detailedMessage);

				return false;
			}
		} catch (ScriptLoadingException sle) {
			logger
					.error("Could not load action since due to script loading problem, exiting with message: "
							+ sle.getMessage());

			String shortMessage = "Failed to authenticate account name: "
					+ account.getName() + ", on target: "
					+ account.getResource().getDisplayName();
			String detailedMessage = shortMessage
					+ ", Action was failed with message: " + sle.getMessage();
			// last place before view to log an event
			//cum.addEventLog("Accounts", "FAILURE", "MEDIUM", shortMessage,detailedMessage);

			return false;
		}
	}
	*/

	/*
	// BULK ACCOUNT ACTIONS FACTORY
	@Deprecated
	public Collection<ResourceAccountActionInterface> getUpdateActions(
			Collection<Account> accountList)
			throws BulkActionsFactoryFailureException {
		// Indicate if the whole process were successfully ended or not
		boolean status = true;
		EdmMessages ems = new EdmMessages();

		Collection<ResourceAccountActionInterface> actions = new ArrayList<ResourceAccountActionInterface>();

		ActionManager am = new ActionManager();

		// Iterate over the accounts
		Iterator<Account> accIt = accountList.iterator();
		while (accIt.hasNext()) {
			Account currAccount = accIt.next();

			try {
				ResourceAccountActionInterface actionUpdateAccount = am
						.factoryUpdateAccountAction(currAccount);
				actions.add(actionUpdateAccount);
			} catch (ActionFactoryException afe) {
				// Indicate that there was a failure in the whole process
				status = false;

				String msg = "Skipping account update for account name: "
						+ currAccount.getName()
						+ ", could not load action due to script loading problem, exiting with message: "
						+ afe.getMessage();

				// Log the error
				logger.error(msg);
				// Add it to the message queue
				ems.severe(msg);
			}
		}

		if (!status) {
			// If there was a failure, log the event to the EventLog
			/*cum.addEventLog("Accounts", "FAILURE", "MEDIUM",
					"Failed to prepare all 'Update Accounts' actions", ems
							.toString());*/

			// //If there was a failure, throw a bulk action preparation
			// exception failure with the logged messages.
			/*throw new BulkActionsFactoryFailureException(ems.toString());
		}

		return actions;
	}*/

	
	/*
	@Deprecated
	public boolean updateAccountsStatus(Collection<Account> accountList) {
		boolean funcStatus = true;
		for (Iterator<Account> iter = accountList.iterator(); iter.hasNext();) {
			Account currAccount = iter.next();
			StringBuffer currAccountStatus = new StringBuffer();
			if (!accountStatus(currAccount, currAccountStatus)) {
				funcStatus = false;
			}
			currAccount.setStatus(currAccountStatus.toString());
			// Delete the content of the stringbuffer
			currAccountStatus.delete(0, currAccountStatus.length());
		}

		return funcStatus;
	}

	@Deprecated
	public Task addGroupMembershipTask(ResourceGroup tsg, String accountId,
			BulkTask bulkTask) throws TaskCreationException {
		logger
				.debug("START of Factoring/Executing 'Add Group Membership' task for target system group name: '"
						+ tsg.getDisplayName()
						+ "', member account name: "
						+ accountId
						+ ", on target system: "
						+ tsg.getResource().getDisplayName());

		ActionManager am = new ActionManager();
		try {
			TaskDefinition td;
			if (tsg.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("ADD_GROUP_MEMBERSHIP_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("ADD_GROUP_MEMBERSHIP");
			}

			Task task = td.factoryTask();
			// task.setDescription("Account Group Membership Association Task");
			task.setDescription("Associating account named '" + accountId
					+ "', to group named '" + tsg.getDisplayName()
					+ "',  of Resource named '"
					+ tsg.getResource().getDisplayName() + "'");

			// Start a log trail for the action
			task.addLog("INFO", "Add Group Membership event",
					"Task to add group membership of member account named: '"
							+ accountId + "', on Target-System: "
							+ tsg.getResource().getDisplayName()
							+ ", for group named: '" + tsg.getDisplayName()
							+ "'");

			// Extremely important to load a new resourceGroup, since part
			// of the lazily fetched associations might already got fetched
			// Thus can grow the serialized action in task unncesserily
			ResourceGroup loadedResourceGroup = tsgm.findGroupById(tsg
					.getResourceGroupId(), false);

			// /Important, clear the entityManager since actionfactory clear
			// referesnces from the managed object!
			// (This is just a safe step, as only the cloned's entity references
			// get cleared.
			em.clear();

			ResourceAccountActionInterface addGroupMembershipAction = am
					.factoryAddGroupMembershipAction(loadedResourceGroup
							.clone(), accountId);

			//JBtask.serializeAsTask(addGroupMembershipAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				//JBtask.setScript(am.getAddGroupMembershipActionScriptContent(tsg.getResource()));
			}

			// If is a part of a bulk, then set the bulktask entity into the
			// task
			if (bulkTask != null) {
				task.setBulkTask(bulkTask);
			}

			return task;

		} catch (ActionFactoryException afe) {
			logger.warn(afe.getMessage());
			throw new TaskCreationException(afe.getMessage());
		} /*JBcatch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			throw new TaskCreationException(sle.getMessage());
		} catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			throw new TaskCreationException(cce.getMessage());*/
		//}
	//}

	/*
	@Deprecated
	public Long addGroupMembership(ResourceGroup tsg, String accountId,
			BulkTask bulkTask) throws TaskCreationException {
		Task task = addGroupMembershipTask(tsg, accountId, bulkTask);
		// Persist the task
		tm.persistTask(task);
		// Flush just after persisting, otherwise task.getUserId will return
		// null (since by default persistence provider flushes after the method
		// execution ends)
		em.flush();

		return task.getTaskId();
	}

	@Deprecated
	public Task removeGroupMembershipTask(ResourceGroup tsg,
			Account account, BulkTask bt) throws TaskCreationException {
		logger
				.debug("START of Factoring/Executing 'Remove Group Membership' task for target system group name: '"
						+ tsg.getDisplayName()
						+ "', member account name: "
						+ account.getName()
						+ ", on target system: "
						+ tsg.getResource().getDisplayName());

		ActionManager am = new ActionManager();
		try {
			TaskDefinition td;
			if (tsg.getResource().getResourceType().isScripted()) {
				td = tm.findTaskDefinition("REMOVE_GROUP_MEMBERSHIP_SCRIPTED");
			} else {
				td = tm.findTaskDefinition("REMOVE_GROUP_MEMBERSHIP");
			}

			Task task = td.factoryTask();
			task.setDescription("Unlink account named '" + account.getName()
					+ "', from group named '" + tsg.getDisplayName()
					+ "',  of Resource named '"
					+ tsg.getResource().getDisplayName() + "'");
			// Start a log trail for the action
			task.addLog("INFO", "Remove Group Membership event",
					"Task to remove group membership of member account named: '"
							+ account.getName() + "', on Target-System: "
							+ tsg.getResource().getDisplayName()
							+ ", for group named: '" + tsg.getDisplayName()
							+ "'");

			// Extremely important to load a new resourceGroup/Account,
			// since part of the lazily fetched associations might already got
			// fetched
			// Thus can grow the serialized action in task unncesserily
			ResourceGroup loadedResourceGroup = tsgm.findGroupById(tsg
					.getResourceGroupId(), false);
			Account loadedAccount = findAccountById(account.getAccountId());

			// /Important, clear the entityManager before removing references
			// from Entities as they might be in managed state by the
			// EntityManager!
			em.clear();

			// Extremely important to clean references from a cloned
			// account->user, otherwise methods that make a usage of
			// account->user original entity might get unexpected data.
			Account clonedAccount = loadedAccount.clone();
			clonedAccount.getUser().cleanReferences();

			ResourceAccountActionInterface removeGroupMembershipAction = am
					.factoryRemoveGroupMembershipAction(loadedResourceGroup
							.clone(), clonedAccount);

			//JB task.serializeAsTask(removeGroupMembershipAction);

			// If task is scripted, then we need to keep the script itself into
			// the task!
			if (td.isScripted()) {
				/*JB
				task.setScript(am
						.getRemoveGroupMembershipActionScriptContent(tsg
								.getResource()));*/
			//}

			// If is a part of a bulk, then set the bulktask entity into the
			// task
			/*if (bt != null) {
				task.setBulkTask(bt);
			}

			return task;

		} catch (ActionFactoryException afe) {
			logger.warn(afe.getMessage());
			throw new TaskCreationException(afe.getMessage());
		} /*JBcatch (ScriptLoadingException sle) {
			logger.warn(sle.getMessage());
			throw new TaskCreationException(sle.getMessage());
		} catch (ClassCastException cce) {
			logger.warn(cce.getMessage());
			throw new TaskCreationException(cce.getMessage());
		}*/
	//}

	/*
	@Deprecated
	public Long removeGroupMembership(ResourceGroup tsg, Account account,
			BulkTask bt) throws TaskCreationException {
		Task task = removeGroupMembershipTask(tsg, account, bt);
		// Persist the task
		tm.persistTask(task);
		// Flush just after persisting, otherwise task.getUserId will return
		// null (since by default persistence provider flushes after the method
		// execution ends)
		em.flush();

		return task.getTaskId();
	}
*/
	
	/*
	@Deprecated
	public Account loadAccountAttributes(Account account,
			HashMap<String, Attribute> attrs) throws OperationException {
		boolean accountIdWasFound = false;
		// logger.debug("Importing '"+attrs.size()+"' attributes for account");
		for (Map.Entry<String, Attribute> attr : attrs.entrySet()) {
			// Make sure there's a corresponding target attribute on the same
			// name
			// try {
			// Eliminate access to DB when possible this is too slow for big
			// processes such as the reconcile process!
			// ResourceAttribute tsa =
			// tsam.findResourceAttributeByName(attr.getKey(),account.getResource());

			boolean isTSAFound = false;
			ResourceAttribute tsa = new ResourceAttribute();
			for (ResourceAttribute currTSA : account.getResource().getResourceAttributes()) {
				//System.out.println("TSA!!!!!!!!!!:" + currTSA.getResourceAttributeId() + ", NAME: " + currTSA.getDisplayName() + " Is current iterated TSA named '" + currTSA.getNameForRetrieval() + "' equals to active attr named: '" + attr.getKey() + "'???");
				//System.out.println("Is current iterated TSA named '" + currTSA.getNameForRetrieval() + "' equals to active attr named: '" + attr.getKey() + "'???");
				if (attr.getKey().equalsIgnoreCase(
						currTSA.getUniqueName())) {
					tsa = currTSA;
					isTSAFound = true;
					break;
				}
			}

			if (!isTSAFound) {
				logger
						.debug("Skipping loading attribute since corresponding Resource Attribute was not found on name: "
								+ attr.getKey());
				continue;
			}

			// if target system attribute indicates an 'account-id' attribute,
			// then set the account name to the current itterated attribute
			// value
			//System.out.println("^^^^^^^: " + tsa.getNameForRetrieval());
			//System.out.println("^^^^^^^: " + tsa.isAccountId());
			if (tsa.isAccountId()) {
				// System.out.println("Found account ID while loading account
				// attributes with value:
				// '"+attr.getValue().getValueAsString());
				// account.setName(attr.getValue().getValueAsString().toUpperCase());
				account.setName(attr.getValue().getValueAsString());
				accountIdWasFound = true;
			}

			// Add the attribute to the account attribute list
			//JB!!! account.addAccountAttribute(attr.getValue());
			// }
			/*
			 * catch (NoResultFoundException nrfe) { }
			 */
		//}
/*
		if (!accountIdWasFound) {
			throw new OperationException("Could not find account ID on the whole attribute collection, returning null!");
			//return null;
		} else {
			account.setTransientAttributesLoaded(true);
			return account;
		}
	}
	*/

	// SEAM METHODS
	/*
	 * public String getAccountId() { return accountId; }
	 * 
	 * public void setAccountId(String accountId) { this.accountId = accountId; }
	 */

	
	
  }