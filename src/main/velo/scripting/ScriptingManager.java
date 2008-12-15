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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
	
	public ScriptEngine getScriptEngine(String engineName) throws FactoryException {
		ScriptEngine engine = getScriptEngineManager().getEngineByName(engineName);
		
		if (engine == null) {
			throw new FactoryException("Could not find engine with name '" + engineName + "'");
		}
		
		return engine;
	}
	
	public ScriptEngineManager getScriptEngineManager() {
		if (sem == null) {
			ScriptEngineManager localSem = new ScriptEngineManager();
			this.sem = localSem;
			
			return sem;
		} else {
			return sem;
		}
	}
	
	public static void main(String[] args) throws FactoryException {
		ScriptingManager sm = new ScriptingManager();
		ScriptEngine se = sm.getScriptEngine("groovy");

		OperationContext oc = new OperationContext();
		se.put("name", "moshe");
		se.put("cntx", oc);
		//String a = "<?xml version=\"1.0\"?><j:jelly trim=\"false\" xmlns:j=\"jelly:core\" xmlns:x=\"jelly:xml\" xmlns:html=\"jelly:html\"><html><head><title>${name}'s Page</title></head></html></j:jelly>";
		String a = "def a = new Date(); println(a.getClass().getName()); def myArr = new Date[1]; myArr[0] = a; println(myArr.getClass().getName()); cntx.addVar('myArr',myArr);";
		
		try {
			//groovy
			//se.eval("println(name);");
			se.eval(a);
			
			OperationContext getOc = (OperationContext)se.get("cntx");
			Date[] arrOfDate = (Date[])getOc.get("myArr");
			System.out.println(arrOfDate);
			
		}catch (ScriptException e) {
			System.out.println("ERROR: " + e);
		}
	}
}
