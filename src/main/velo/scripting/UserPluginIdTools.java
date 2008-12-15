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
package velo.scripting;

import java.util.Collection;

import javax.naming.NamingException;

import velo.entity.Account;
import velo.entity.AuditedAccount;
import velo.entity.IdentityAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.entity.UserIdentityAttributeValue;
import velo.entity.Attribute.AttributeDataTypes;
import velo.exceptions.AttributeSetValueException;
import velo.exceptions.EdentityException;
import velo.exceptions.NoResultFoundException;

public class UserPluginIdTools extends GenericTools {

	public Collection<Account> findAccounts(String wildCard) throws NamingException {
        return getAccountManager().findAccounts(wildCard);
    }
    
    public Collection<User> findUsers(String wildCard) throws NamingException {
        return getUserManager().findUsers(wildCard);
    }
    
    public Collection<AuditedAccount> findAuditedAccounts(String wildCard) throws NamingException {
    	return getAccountManager().findAuditedAccounts(wildCard);
    }
    
    
	
	//used to generate new UIAs
    public void addAttribute(User user, String uniqueName, Object value, String type) throws EdentityException, NamingException, AttributeSetValueException {
    	IdentityAttribute ia = getIdentityAttributeManager().findIdentityAttribute(uniqueName);
    	
    	if (ia == null) {
    		throw new EdentityException("Could not add attribute, could not find Identity Attribute '" + uniqueName + "' definition in repository!");
    	}
    	
    	try {
    		//also factory a first value
    		UserIdentityAttribute uia = UserIdentityAttribute.factory(ia, user);
    		UserIdentityAttributeValue firstValue = uia.getFirstValue();
    		//valueOf will throw an exception if specified a wrong (string) type
    		firstValue.setDataType(AttributeDataTypes.valueOf(type));
    		firstValue.setValueAsObject(value);
    		user.getUserIdentityAttributes().add(uia);
    		//should never happen as factory always factor first value
    	} catch (NoResultFoundException e) {
    		//TODO never! but do a log anyway
    	}
    }
}
