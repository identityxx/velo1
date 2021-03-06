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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.ejb.interfaces.UserManagerLocal;
import velo.entity.User;
import velo.exceptions.OperationException;
import velo.validators.Generic;

/**
 * Search users by criterias,
 * The standard EntityQuery only supports user->fields but does not support search by Identity Attributes.
 * 'Identity Attribute's searches delegates to UserManager.findUsers method.  
 * @author asaf
 *
 */
@Name("userList")
public class UserList extends EntityQuery {
	@In
	UserManagerLocal userManager;
	
	/*
	private String[] RESTRICTIONS = {
		"lower(user.name) like concat(trim(lower(#{userList.userName})),'%')"
	};
	*/
	
	private List<String> RESTRICTIONS = new ArrayList<String>();
	
	
	//"uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')",
	//"uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')"

	private User user = new User();
	private String firstName;
	private String lastName;
	//due to this: https://jira.jboss.org/jira/browse/JBSEAM-1587
	//http://seamframework.org/Community/BypassingValidationsWhenUsingEntityQuery
	private String userName;

	@Override
	public String getEjbql() {
		//setOrder("name");
		
		String query = null;

		RESTRICTIONS.clear();
		RESTRICTIONS.add("lower(user.name) like concat(trim(lower(#{userList.userName})),'%')");

		/*Was working when IA were not having sources (such as Resource Attribute)
		if ( (Generic.isNotEmptyAndNotNull(getFirstName())) && (!Generic.isNotEmptyAndNotNull(getLastName())) ) {
			query = "select user from User user, IN(user.localUserAttributes) uiaFN, IN(uiaFN.values) uiaValFN";
			RESTRICTIONS.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')");
		} else if ( (!Generic.isNotEmptyAndNotNull(getFirstName())) && (Generic.isNotEmptyAndNotNull(getLastName())) ) {
			query = "select user from User user, IN(user.localUserAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
			RESTRICTIONS.add("uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')");
		} else if ( (Generic.isNotEmptyAndNotNull(getFirstName())) && (Generic.isNotEmptyAndNotNull(getLastName())) ) {
			query = "select user from User user, IN(user.localUserAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.localUserAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
			
			
			RESTRICTIONS.add("uiaFN.identityAttribute.uniqueName = 'FIRST_NAME' AND lower(uiaValFN.valueString) like concat('%',trim(lower(#{userList.firstName})),'%')");
			RESTRICTIONS.add("uiaLN.identityAttribute.uniqueName = 'LAST_NAME' AND lower(uiaValLN.valueString) like concat('%',trim(lower(#{userList.lastName})),'%')");
		}
		else {
			query = "select user from User user";
		}
		*/
		query = "select user from User user";
		
		setRestrictionExpressionStrings(RESTRICTIONS);
		return query;
		
		
		
		
		//return "SELECT DISTINCT user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		/*
		if (super.getEjbql() == null) {
			//return "select user from User user, IN(user.userIdentityAttributes) uiaFN, IN(uiaFN.values) uiaValFN,IN(user.userIdentityAttributes) uiaLN, IN(uiaLN.values) uiaValLN";
		} else {
			return super.getEjbql();
		}
		*/
	}
	
	
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
		getLog().trace("Input text for finding user auto complete: " + suggest);
		
		String pref = (String)suggest;
        //getUser().setName(pref);
		userName = pref;
		//initialize();
        
        List<User> users = getResultList();
        getLog().debug("Returned amount of users for userName auto complete : " + users.size());
        
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
	
	
	@Override
	public List<User> getResultList() {
		List<User> users = super.getResultList();
		for (User currUser : users) {
			try {
				UserManagerLocal userManager1 = (UserManagerLocal)Component.getInstance("userManager");
				userManager1.loadUserAttributes(currUser);
			} catch (OperationException e) {
				getFacesMessages().add(e.getMessage());
			}
		}
		
		return users;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
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
		
	/*	return a;
	}

*/
	
	
	@PostConstruct
    public void initialize() {
		//setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
    	//setRestrictionExpressionStrings(getRestrictionStrings());
    	//setEjbql(getEjbql());
    }
}
