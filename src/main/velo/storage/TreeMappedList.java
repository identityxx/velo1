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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * A list class that also maps objects
 * added to it by thier name, allowing ot get
 * them back by thier name.
 *
 * @author Rani Sharim
 */
abstract public class TreeMappedList<String,Object> extends TreeMap<String,Object> {
	
	/**
	 * Maps a single object to this list.
	 * The mapped name is expected by getObjectName() method.
	 * Does not adds the object, mearly maps it.
	 * 
	 * @param obj The object to be mapped
	 */
	public void mapObject(Object obj) {
		put(getObjectName(obj),obj);
	}
	
	/**
	 * Maps a collection of objects to this list
	 * @param objs
	 */
	public void mapAllObjects(Collection<Object> objs) {
		for (Iterator<Object> iter = objs.iterator(); iter.hasNext();) {
			Object currObj = iter.next();
			
			put(getObjectName(currObj),currObj);
		}
	}
	
	
	/**
	 * Given an object of this list, returns
	 * a string containing the name to map it
	 * for.
	 * 
	 * @param obj The object we want to get the name for
	 * @return A string contaning its name, or NULL if cant get name.
	 */
	abstract protected String getObjectName(Object obj);
	
	
	/**
	 * Maps a whole List object , object
	 * by object, to the internal map.
	 * 
	 * @param objectList The list we want to map
	 */
	protected void mapObjectList(List objectList) {
		Iterator it = objectList.iterator();
		
		//While not the end of the list, map the element to the tree list
		while (it.hasNext()) {
			//mapObject(it.next());
		}
	}
	
	/**
	 * Returns a mapped object by its name
	 * 
	 * @param sName The name of the object we want to retrive
	 * @return A Object that is the object mapped to teh 
	 *         name we gave, or null if there's no object 
	 *         mapped to this name.
	 */
	public Object getByName(String sName) {
		return get(sName);
	}
	
	public Set getEntrySet() {
		return entrySet();
	}
}
