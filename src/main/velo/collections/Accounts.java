package velo.collections;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import velo.entity.Account;
import velo.entity.Resource;
import velo.exceptions.ObjectsConstructionException;

public class Accounts extends HashSet<Account> implements Cloneable, Serializable {
	private String resourceUniqueName;
	private String fetchType;
	private Date creationDate;
	
	/*
	public void generateAccount(String accountName) {
		Account account = new Account();
		account.setName(accountName);
		
		//TODO: Primary key only!
	}
	*/
	
	/*
	public void addAccount(String accountName, Resource resource) {
		Account account = new Account();
		account.setName(accountName);
		account.setResource(resource);
		add(account);
	}
	*/
	
	/*
	public void addAccount(Map accAttrs,Resource resource) {
		Account account = new Account();
		account.setResource(resource);
		
		try {
			account.loadAccountByMap(accAttrs);

			add(account);
		}catch(ObjectsConstructionException e) {
			e.printStackTrace();
		}
	}
	*/
	
	
	
	/*
	public void loadAccounts(List<Map> attrsMapEntries, Resource resource) {
		for (Map currMap : attrsMapEntries) {
			addAccount(currMap, resource);
		}
	}
	*/
	
	
	
	
	
	
	
	
	
	
	
	

	public String getResourceUniqueName() {
		return resourceUniqueName;
	}

	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
	}

	public String getFetchType() {
		return fetchType;
	}

	public void setFetchType(String fetchType) {
		this.fetchType = fetchType;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
