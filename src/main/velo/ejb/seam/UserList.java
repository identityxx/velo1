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
package velo.ejb.seam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.User;
import velo.validators.Generic;

@Name("userList")
public class UserList extends EntityQuery {
	
	private static final String[] RESTRICTIONS = {
			"lower(user.name) like concat(trim(lower(#{userList.userName})),'%')",
			"uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')",
			"uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')"
	};

	private User user = new User();
	private String firstName;
	private String lastName;
	//due to this: https://jira.jboss.org/jira/browse/JBSEAM-1587
	//http://seamframework.org/Community/BypassingValidationsWhenUsingEntityQuery
	private String userName;

	@Override
	public String getEjbql() {
		setOrder("name");
		
		return "SELECT DISTINCT user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		
		/*
		if ( (Generic.isNotEmptyAndNotNull(getFirstName())) && (!Generic.isNotEmptyAndNotNull(getLastName())) ) {
			return "select user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN";
		} else if ( (!Generic.isNotEmptyAndNotNull(getFirstName())) && (Generic.isNotEmptyAndNotNull(getLastName())) ) {
			return "select user from User user, IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		} else if ( (Generic.isNotEmptyAndNotNull(getFirstName())) && (Generic.isNotEmptyAndNotNull(getLastName())) ) {
			return "select user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		}
		else {
			return "select user from User user";
		}
		*/
		
		
		
		
		
		/*
		if (super.getEjbql() == null) {

			
			
			//return "select user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		} else {
			return super.getEjbql();
		}
		*/
	}
	
	
	public List<String> getRestrictionStrings() {
		List<String> arr = Arrays.asList(RESTRICTIONS);
		
		ArrayList<String> a = new ArrayList<String>();
		a.addAll(arr);
		
		//System.out.print("!!!!!!!!! IS FIRST NAME NULL? ");
		if (Generic.isNotEmptyAndNotNull(getFirstName())) {
			//System.out.println("!!!!!!!!! NO!!!! ");
			a.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')");
		} else {
			//System.out.println("!!!!!!!!! YES!!!! ");
		}
		
		//System.out.print("!!!!!!!!! IS LAST NAME NULL? ");
		if (Generic.isNotEmptyAndNotNull(getLastName())) {
			//System.out.println("!!!!!!!!! NO!!!! ");
			a.add("uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')");
		} else {
			//System.out.println("!!!!!!!!! YES!!!! ");
		}
		/*
		if ( (Generic.isNotEmptyAndNotNull(getFirstName())) ) {
			if (getFirstName().length() > 0) {
				a.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) = #{userList.firstName}");
			}
		}
		*/
		/*
		return a; 
		*/
		
		return a;
	}
	
	
	
	@Override
	public Integer getMaxResults() {
		if (super.getMaxResults() != null) {
			return super.getMaxResults();
		}
		else {
			return 10;
		}
	}

	public User getUser() {
		return user;
	}

	
	/*WHEN MOVED TO SEAM 2.1
	@Override
	public List<String> getRestrictions() {
		List<String> arr = Arrays.asList(RESTRICTIONS);
		
		ArrayList<String> a = new ArrayList<String>();
		a.addAll(arr);
		
		//System.out.print("!!!!!!!!! IS FIRST NAME NULL? ");
		if (Generic.isNotEmptyAndNotNull(getFirstName())) {
			//System.out.println("!!!!!!!!! NO!!!! ");
			a.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')");
		} else {
			//System.out.println("!!!!!!!!! YES!!!! ");
		}
		
		//System.out.print("!!!!!!!!! IS LAST NAME NULL? ");
		if (Generic.isNotEmptyAndNotNull(getLastName())) {
			//System.out.println("!!!!!!!!! NO!!!! ");
			a.add("uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')");
		} else {
			//System.out.println("!!!!!!!!! YES!!!! ");
		}
		/*
		if ( (Generic.isNotEmptyAndNotNull(getFirstName())) ) {
			if (getFirstName().length() > 0) {
				a.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) = #{userList.firstName}");
			}
		}
		*/
		/*
		return a; 
	}
	*/
	
	
	public List<SelectItem> userSearchByNameAutoCompleteForSelectItem(Object suggest) {
		SelectItem si = new SelectItem();
		si.setLabel("moo1");
		si.setValue("value1");
		
		SelectItem si1 = new SelectItem();
		si1.setLabel("moo2");
		si.setValue("value2");
		
		List<SelectItem> sil = new ArrayList<SelectItem>();
		
		System.out.println("!!Bla: " + sil.size());
		return sil;
	}
	
	
	public List<User> userSearchByNameAutoComplete(Object suggest) {
		getLog().debug("!!!Suggested: " + suggest);
		
		String pref = (String)suggest;
        //getUser().setName(pref);
		userName = pref;
		
		initialize();
        
        List<User> users = getResultList();
        
        getLog().debug("!!Returned: " + users.size());
        
        return users;
    }

	public String getFirstName() {
		
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		
		return lastName;
		
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getUserName() {
		/*
		if (!Generic.isNotEmptyAndNotNull(userName)) {
			userName = "%";
		}
		*/
		
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	
	
	/*TEST
	public List<User> getLol() {
		String query = "select user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN WHERE ((uiaFN.identityAttribute.uniqueName = :fnIAName) AND (lower(uiaValFN.valueString) like :fnUiaValue)) AND ((uiaLN.identityAttribute.uniqueName like :lnIAName) AND (lower(uiaValLN.valueString) like :lnUiaValue))";
		
		List<User> a = getEntityManager().createQuery(query).setParameter("fnIAName","FIRST_NAME").setParameter("fnUiaValue", "asaf").setParameter("lnIAName","LAST_NAME").setParameter("lnUiaValue", "shakarchi").getResultList();
		
		
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + a.size());
		for (User currUser : a) {
			System.out.println("!!!!!!!!!!!!: " + currUser.getName());
		}
		
		return a;
	}
	*/
	
	
	@PostConstruct
    public void initialize() {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!: " + getEjbql());
    	setRestrictionExpressionStrings(getRestrictionStrings());
    	setEjbql(getEjbql());
    }
}
