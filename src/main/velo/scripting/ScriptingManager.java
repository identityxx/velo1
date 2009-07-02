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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.time.StopWatch;

import velo.contexts.OperationContext;
import velo.exceptions.FactoryException;

/**
 * @author Asaf Shakarchi
 *
 * A manager class for handing scripting objects factory, execution and tools
 * Which focuses on JSR 223 standard. 
 */
public class ScriptingManager {
	ScriptEngineManager sem;
	private static Map<String,ScriptEngine> engines = new HashMap<String,ScriptEngine>();
	private static ScriptEngineManager engineManager = new ScriptEngineManager();
	
	//TODO: holy crap, is it safe? if not we are doomed, this is a singleton of each script engine
	//The scary thing is that when invoking CompiledScript.eval it use the context of the engine 
	//as each compiledScript holds a reference to the ScriptEngine it was created from.
	public static ScriptEngine getScriptEngine(String engineName) throws FactoryException {
		if (engines.containsKey(engineName)) {
			return engines.get(engineName);
		} else {
			ScriptEngine engine = engineManager.getEngineByName(engineName);
			if (engine == null) throw new FactoryException("Could not find engine with name '" + engineName + "'");
			engines.put(engineName,engine);
			
			return engine;
		}
	}
	/*
	public ScriptEngineManager getScriptEngineManager() {
		if (sem == null) {
			ScriptEngineManager localSem = new ScriptEngineManager();
			this.sem = localSem;
			
			return sem;
		} else {
			return sem;
		}
	}
	*/
	
	public static void main(String[] args) throws FactoryException {
		//ScriptingManager sm = new ScriptingManager();
		//ScriptEngine se = sm.getScriptEngine("groovy");
		ScriptEngine se = ScriptingManager.getScriptEngine("groovy");

		OperationContext oc = new OperationContext();
		se.put("name", "moshe");
		se.put("cntx", oc);
		//String a = "<?xml version=\"1.0\"?><j:jelly trim=\"false\" xmlns:j=\"jelly:core\" xmlns:x=\"jelly:xml\" xmlns:html=\"jelly:html\"><html><head><title>${name}'s Page</title></head></html></j:jelly>";
		//String a = "def a = new Date(); println(a.getClass().getName()); def myArr = new Date[1]; myArr[0] = a; println(myArr.getClass().getName()); cntx.addVar('myArr',myArr);";
		String a = "def a = new Date(); a.getClass().getName(); def myArr = new Date[1]; myArr[0] = a; myArr.getClass().getName(); cntx.addVar('myArr',myArr);";


		//Sadly it seems that invoking via se.eval and script.eval almost return the same times, thought it should be much more effecient :/
		try {
			//groovy
			//se.eval("println(name);");
			StopWatch sw = new StopWatch();
			sw.start();
			
			for (int i=0;i<100000;i++) {
				se.eval(a);
			}
			sw.stop();
			System.out.println("time in seconds: '" + sw.getTime()/1000 + "'");
			sw.reset();
			
			sw.start();
			CompiledScript script = ((Compilable)se).compile(a);
			for (int i=0;i<100000;i++) {
				script.eval();
			}
			sw.stop();
			System.out.println("time in seconds: '" + sw.getTime()/1000 + "'");
			
			OperationContext getOc = (OperationContext)se.get("cntx");
			Date[] arrOfDate = (Date[])getOc.get("myArr");
			//System.out.println(arrOfDate);
			
		}catch (ScriptException e) {
			System.out.println("ERROR: " + e);
		}
	}
}
