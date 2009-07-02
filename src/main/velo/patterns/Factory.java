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
package velo.patterns;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import velo.exceptions.FactoryException;

/**
 * @author Rani sharim
 * 
 * Base class to a Factory patten class.
 * 
 * A factory class gets a class name and creates
 * an instance of that class.
 *
 */
public class Factory implements Serializable{
	private static Logger log = Logger.getLogger(Factory.class.getName());
	
	/**
	 * @param fullyQualifiedClassName The fully qualified class name to factory an object for
	 * @return A factored object for the specified class name 
	 * @throws FactoryException
	 */
	public static Object factoryInstance(String fullyQualifiedClassName) throws FactoryException {
		try {
			log.trace("Factoring object for class name: " + fullyQualifiedClassName);
			return Class.forName(fullyQualifiedClassName).newInstance();
			//return Class.forName(packagePath).newInstance()
		}
		catch (ClassNotFoundException cnfe) {
			throw new FactoryException(cnfe.getMessage());
		}
		catch (IllegalAccessException iae) {
			throw new FactoryException(iae.getMessage());
		}
		catch (InstantiationException ie) {
			throw new FactoryException(ie.getMessage());
		}
	}
	
	public static Object factoryInstance(String fullyQualifiedClassName, Class ofInstance) throws FactoryException {
		Object obj;
		
		try {
			obj = factoryInstance(fullyQualifiedClassName);
			if (!ofInstance.isInstance(obj)) {
				throw new FactoryException("Object was initiated but is not an instance of type '" + ofInstance.getName() + "'");
			}
			
			return obj;
		} catch (FactoryException e) {
			throw new FactoryException("Could not factory class: " + e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @param className The class name of the class we want
	 *                   to create an instance of.
	 * @param packagePath The package or path of the 
	 *                     required class
	 * @return A new instance of the requierd class, or 
	 *         null if we cant create it for soem reason.
	 */
	@Deprecated
	public static Class factory(String className,String packagePath) {
		try {
			String fullClassName = className+"."+packagePath;
			return Class.forName(fullClassName);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param className The class name to factory a class for
	 * @return A factored Class
	 * @throws ClassNotFoundException
	 */
	@Deprecated
	public static Class factory(String className) throws ClassNotFoundException{
		try {
			return Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			throw (e);
		}
	}
	
        
        
        public static Object factoryInstance(Class className) throws FactoryException {
		try {
			log.trace("Factoring object for class name: " + className.getName());
			return className.newInstance();
			//return Class.forName(packagePath).newInstance()
		}
		catch (IllegalAccessException iae) {
			throw new FactoryException(iae.getMessage());
		}
		catch (InstantiationException ie) {
			throw new FactoryException(ie.getMessage());
		}
	}
        
        
	
	/**
	 * Factory an object for a certain class name, support none-empty constructors
	 * @param className The name of the class to factory an object for
	 * @param classes An array with Constructors
	 * @param objects An array with objects corresponds to the Constructors array
	 * @return A factored object
	 * @throws FactoryException
	 */
    @Deprecated
	public static Object factory(String className, Class[] classes, Object[] objects) throws FactoryException {
		try {
			Class cls = factory(className);
			Constructor constructor = cls.getConstructor(classes);
			Object obj = constructor.newInstance(objects);
			return obj;
		}
		catch(NoSuchMethodException nsme) {
			throw new FactoryException("NoSuchMethodException occured while trying to factory a constructor, failed with message: " + nsme.getMessage());
		}
		//Note: I had a situation when Invocation exception occured since the parent's parent's constructor had one statement which was null, then the message was 'NullException', this was very hard to trace, so I decided to write a note here
		catch(InvocationTargetException ite) {
			throw new FactoryException("InvocationTargetException occured while trying to factory a constructor, failed with message: " + ite.getCause());
		}
		catch(IllegalAccessException iae) {
			throw new FactoryException("IllegalAccessException occured while trying to factory a constructor, failed with message: " + iae.getMessage());
		}
		catch (InstantiationException ie) {
			throw new FactoryException("InstantiationException occured while trying to factory a constructor, failed with message: " + ie.getMessage());
		}
		catch (ClassNotFoundException cnfe) {
			throw new FactoryException(cnfe.getMessage());
		}
	}
    
    
    public static void main(String[] args) {
    	try {
			Object o = Factory.factoryInstance("velo.actions.readyActions.RemoveAccount", velo.entity.ReadyAction.class);
			System.out.println("O class '" + o.getClass().getName() + "'");
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
