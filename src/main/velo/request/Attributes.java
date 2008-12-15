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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import velo.storage.Attribute;

/**
 * @author Asaf Shakarchi
 */
public class Attributes implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;
	
	private Map<String,Attribute> attributes = new HashMap<String,Attribute>();

	public void addAttribute(Attribute attr) {
		attributes.put(attr.getName(), attr);
	}
	
	public Collection<Attribute> asCollection() {
		return attributes.values();
	}
	
	public Set<Entry<String,Attribute>> asSet() {
		return attributes.entrySet();
	}
	
	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}
	
	public int size() {
		return attributes.size();
	}
	
	public boolean isAttributeExists(String name) {
		return attributes.containsKey(name);
	}
}