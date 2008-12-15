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
package velo.converters;

import java.io.IOException;
import java.io.StringReader;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerRemote;
import velo.entity.Resource;

public class AccountConverterTools {
	
	
	public String toProperCase(String theString) throws IOException{
		StringReader in = new StringReader(theString.toLowerCase());
		boolean precededBySpace = true;
		StringBuffer properCase = new StringBuffer();    
		while(true) {      
			int i = in.read();
			if (i == -1)  break;      
			char c = (char)i;      
			if (c == ' ') {
				properCase.append(c);
				precededBySpace = true;
			}
			else {
				if (precededBySpace) { 
					properCase.append(Character.toUpperCase(c));
				}
				else { 
					properCase.append(c); 
				}
				precededBySpace = false;
			}
		}
		
		return properCase.toString();
	}
	
	public boolean isAccountExistsOnTarget(String accountName, Resource resource) throws NamingException {
		//TODO: Replace to local..
   		Context ic = new InitialContext();
   		AccountManagerRemote accountManager = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
   		return accountManager.isAccountExistOnTarget(accountName, resource);
	}
}
