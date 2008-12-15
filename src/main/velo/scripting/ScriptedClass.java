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


/**
 * 
 * Base class to all classes that are 
 * scriptable but do not inherit DataObject
 *
 * Each scriptable function should be accessed (in java)
 * with invokeMethod, and not a direct function call.
 * 
 * @see Scriptable
 * @deprecated
 * 
 * @author Rani sharim
 */
@Deprecated
public class ScriptedClass implements Scriptable 
{
	
	/**
	 *  Calls a method on the ScriptedObject
	 *  
	 *  @param name the name of the function to call
	 *              on the object.
	 *  @param args the arguments to pass to the fucntion
	 *              when calling it.
	 *              
	 * @return An object containing the result of
	 *         the funcion.
	 */
	public Object invokeMethod(String name,Object args)
	{
		Object temp = new Object();
		return temp;
	}
}
