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
package velo.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asaf Shakarchi
 */
public class RolesNames implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;
	private List<String> rolesNames = new ArrayList<String>();
	
	public void addRoleName(String roleName) {
		rolesNames.add(roleName);
	}
	
	public List<String> getAsList() {
		return rolesNames;
	}
}