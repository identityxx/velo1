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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;

import org.codehaus.groovy.control.CompilationFailedException;

import velo.exceptions.ScriptLoadingException;
import velo.patterns.Factory;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * A ScriptFactory manager to factory scripted classes
 * 
 * @author Asaf Shakarchi
 */
@Deprecated
public class ScriptFactory extends Factory implements Serializable { 
	
	//private static Logger logger = FactoryLogger.loggerFactory(ScriptFactory.class.getName());
	
	public static String getScriptContent(String fileName) throws ScriptLoadingException {
		String thisLine;
		
		try {
			FileInputStream fin =  new FileInputStream(fileName);
			BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
			StringBuffer sb = new StringBuffer();
			while ((thisLine = myInput.readLine()) != null) {
				sb.append(thisLine + "\n");
			}
			
			return sb.toString();
		}
		catch (FileNotFoundException fnfe) {
			throw new ScriptLoadingException(fnfe.getMessage());
		}
		catch (IOException ioe) {
			throw new ScriptLoadingException(ioe.getMessage());
		}
		
	}
	
	public Class factoryScriptedClass(String clsString) throws ScriptLoadingException {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		
		try {
			Class groovyClass = loader.parseClass(clsString);
			
			return groovyClass;
		}
		catch(CompilationFailedException cfe) {
			////logger.warning("Failed to compile the script file named: " + scriptFileName);
			////logger.warning("Failure message is: " + cfe.getMessage());
			throw new ScriptLoadingException("Failed to compile the scripted class, failure message is: " + cfe.getMessage());
		}
		
	}
	
	
	
	/**
	 * Factory a scriptable Groovy class
	 * @param scriptFileName The name of the script to factory an object for
	 * @return A GroovyObject factored by this method
	 * @throws ScriptLoadingException
	 */
	public GroovyObject factoryScriptableGroovyObjectByScriptFileName(String scriptFileName) throws ScriptLoadingException {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		//Class groovyClass = loader.parseClass(new File("d:/eclipse_workspaces/groovy/src/Test.gsc"));
		try {
			Class groovyClass = loader.parseClass(new File(scriptFileName));
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			return groovyObject;
		}
		catch(CompilationFailedException cfe) {
			////logger.warning("Failed to compile the script file named: " + scriptFileName);
			////logger.warning("Failure message is: " + cfe.getMessage());
			throw new ScriptLoadingException("Failed to compile the script file named: " + scriptFileName + ", failure message is: " + cfe);
		}
		catch (IOException ioe) {
			////logger.warning("An IO exception has occured while trying to handle file name: " + scriptFileName);
			throw new ScriptLoadingException("An IO exception has occured while trying to handle file name: " + scriptFileName);
			
		}
		catch (IllegalAccessException iae) {
			////logger.warning("An illegal access exception has occured while trying to factory a scriptable class with file name: " + scriptFileName + ", Failure message is: " + iae.getMessage());
			throw new ScriptLoadingException("An illegal access exception has occured while trying to factory a scriptable class with file name: " + scriptFileName + ", Failure message is: " + iae);
		}
		catch (InstantiationException ie) {
			////logger.warning(ie.getMessage());
			throw new ScriptLoadingException(ie.getMessage());
		}
		
		//lets call some method on an instance
		//Object[] args = {};
		//groovyObject.invokeMethod("sayHello", args);
	}
	
	
	
	
	
	/**
	 * Factory a groovy script by resource name
	 * @param scriptedClassName The script class name
	 * @return A scripted loaded object
	 * @throws ScriptLoadingException
	 */
	public Object factoryScriptableObjectByResourceName(String scriptedClassName) throws ScriptLoadingException {
		//GroovyClassLoader gcl = new GroovyClassLoader();
		//ClassLoader parent = getClass().getClassLoader();
		//GroovyClassLoader loader = new GroovyClassLoader(parent);

		/*Just a test to see if URL find a resource (expecting '/' instead of '.'
		scriptedClassName = "/targets_scripts/qflow/actions/QflowListAccounts.groovy";
		URL resourceURL = getClass().getResource(scriptedClassName);
		System.out.println("Parsing script resouce name: " + resourceURL);
		*/
		//logger.info("Trying to factory a groovy script with resource name: " + scriptedClassName);
		try {
			//String scriptedClassName1 = "d:/velo_workspace/targets_scripts/qflow/actions/qflow_list_accounts.groovy";
			//logger.fine("Parsing script resource name: " + scriptedClassName);
			URL resourceURL = getClass().getResource(scriptedClassName);
			//System.out.println("Parsing script resouce name!!!: " + resourceURL);
			
			//System.out.println(resourceActionInterface.class);
			
			
			//Load the specified CLASS (The class of course must be in the classpath!)
			//Class objClass = gcl.loadClass("targets_scripts.qflow.actions.QflowListAccounts");
			GroovyClassLoader gcl = new GroovyClassLoader();
			//System.out.println("Sys class loader: " + GroovyClassLoader.getSystemClassLoader().getClass().getName());
			//System.out.println("GCL is: " + gcl);
			//Class objClass = gcl.loadClass(scriptedClassName1);
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader gcc = new GroovyClassLoader(parent);
			File f = new File(scriptedClassName);
			//logger.finest("File obj: " + f);
			Class parsedClass = gcc.parseClass(f);
			//logger.finest("Parsed Class is: " + parsedClass);
			Object scriptObject = parsedClass.newInstance();
			//logger.info("Successfully loaded scriptable class name: " + scriptedClassName);
			//Initialize from the gotten class a new instance
			//Object scriptObject = objClass.newInstance(); 
			
			//logger.fine("Loaded scripted class by groovy loader: " + scriptObject);

			//Parsing script is not needed, since the scripts are in classpath, we use loadClass instead
			//Class clazz = gcl.parseClass("targets_scripts.qflow.actions.QflowCreateAccount.groovy");
			//System.out.println("Successfully parsed scriptable class name: " + classNameToLoad);
			//Object scriptObject = clazz.newInstance();
			
			/*Display the methods of the scripted class instance			
			Method[] m = scriptObject.getClass().getMethods();
			for (int i=0;i<m.length;i++) {
				System.out.println(m[i]);
			}
			*/

			return scriptObject;
		}
		catch (IllegalAccessException iae) {
			throw new ScriptLoadingException("An illegal access exception has occured while trying to factory a scriptable class with file name: " + scriptedClassName + ", Failure message is: " + iae.getMessage());
		}
		catch (InstantiationException ie) {
			throw new ScriptLoadingException("An Instantiation exception has occured while trying to factory a scriptable class with file name: " + scriptedClassName + ", Failure message is: " + ie.getMessage());
		}
		/*
		catch(ClassNotFoundException cnfe) {
			//NOTE: This can happen if the script has a wrong 'class' declaration (meaning the class extends or implments a NOT FOUND class or interface!)
			
			if (cnfe.getException() != null) {
				//throw new ScriptLoadingException("Exception occured while trying to parse script, WITH MESSAGE: " + cnfe.getMessage() + ", details: " + cnfe.getException().getMessage());
			}
			else{
				throw new ScriptLoadingException("Exception occured while trying to parse script, WITH MESSAGE: " + cnfe.getMessage());
			}
			//System.out.println("ClassNotFoundExceptio occured with message: " + cnfe.getMessage());
			//System.out.println("Exception details: " + cnfe.getException().getMessage());
		}
		*/
		catch (IOException ioe) {
			////logger.warning("An IO exception has occured while trying to handle, error message: " + ioe.getMessage());
			throw new ScriptLoadingException("An IO exception has occured while trying to handle, error message: " + ioe.getMessage());
		}
		catch (CompilationFailedException cfe) {
			throw new ScriptLoadingException("Script loading exception occured, with message: " + cfe);
		}
		catch (AbstractMethodError ame) {
			throw new ScriptLoadingException("Script loading exception occured, with message: " + ame);
		}
		catch (java.lang.NoSuchMethodError nsme) {
			throw new ScriptLoadingException("Script loading exception occured, with message: " + nsme);
		}
	}
	
	/*
	public static void main(String[] args) {
		ScriptableObjectsFactory sob = new ScriptableObjectsFactory();
		sob.factoryScriptableObjectByResourceName("asdf");
	}
	*/
	
	
	
	public GroovyObject factoryScriptableObject(String classCode) throws ScriptLoadingException {
		//logger.info("-STARTED- to factory a scriptable object");
		
		try {
			//String scriptedClassName1 = "d:/velo_workspace/targets_scripts/qflow/actions/qflow_list_accounts.groovy";
			////logger.finest("-START- OF dump of scriptable code");
			////logger.finest(classCode);
			////logger.finest("-END- OF dump of scriptable code");
			
			
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Class groovyClass = loader.parseClass(classCode);
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			return groovyObject;
		}
		catch (IllegalAccessException iae) {
			throw new ScriptLoadingException("An illegal access exception has occured while trying to factory a scriptable object, failure message is: " + iae);
		}
		catch (InstantiationException ie) {
			throw new ScriptLoadingException("An Instantiation exception has occured while trying to factory a scriptable object, failure message is: " + ie);
		}
		catch (CompilationFailedException cfe) {
			throw new ScriptLoadingException("Script compilation failure occured: " + cfe);
		}
		catch (AbstractMethodError ame) {
			throw new ScriptLoadingException("Script loading exception occured: " + ame);
		}
		catch (java.lang.NoSuchMethodError nsme) {
			throw new ScriptLoadingException("Script loading exception occured: " + nsme);
		}
                //Happend to me a NPE once (even in loading, not sure exaclty why, but it's better to catch any exceptions when dealing with scripts)
                catch (Exception e) {
                    throw new ScriptLoadingException("Script loading exception(generic) has occured,error message: " + e);
                }
	}
	
	
}
