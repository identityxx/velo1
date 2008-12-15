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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import org.codehaus.groovy.control.CompilationFailedException;

import velo.exceptions.ScriptLoadingException;

@Deprecated
public class GroovyScripting {
	
	public GroovyObject factoryGroovyObject(String scriptCode) throws ScriptLoadingException {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		
		try {
			Class groovyClass = loader.parseClass(scriptCode);
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			
			return groovyObject;
		}
		catch (CompilationFailedException e) {
			throw new ScriptLoadingException(e);
		}
		catch (IllegalAccessException e) {
			throw new ScriptLoadingException(e); 
		}
		catch (InstantiationException e) {
			throw new ScriptLoadingException(e);
		}
		//it is a user code that being compiled and cannot be trusted
		catch (Exception e) {
			throw new ScriptLoadingException(e);
		}
	}
}

