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
package velo.storage;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import velo.entity.ResourceAttribute;

//public class ResourceAttributeSet<E> extends HashSet<ResourceAttribute> implements Cloneable, Serializable {
public class ResourceAttributeSet<E> extends LinkedHashSet<ResourceAttribute> implements Cloneable, Serializable {

	public Set<ResourceAttribute> getOnlyActive() {
		Set<ResourceAttribute> set = new LinkedHashSet<ResourceAttribute>();
		
		for (ResourceAttribute currRA : this) {
			if (currRA.isManaged()) {
				set.add(currRA);
			}
		}
		
		return set;
	}
	
	public Set<ResourceAttribute> getNotActive() {
		Set<ResourceAttribute> set = new LinkedHashSet<ResourceAttribute>();
		
		for (ResourceAttribute currRA : this) {
			if (currRA.isManaged()) {
				set.add(currRA);
			}
		}
		
		return set;
	}
}
