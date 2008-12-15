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
package velo.tasks;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.actions.ActionInterface;
import velo.ejb.interfaces.TaskManagerRemote;
import velo.entity.Task;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class BasicTaskExecuter implements TaskExecuter {
    protected TaskManagerRemote tm;
    
    public BasicTaskExecuter() {
        try {
            /////////////Context ic = new InitialContext();
        	   Hashtable environment = new Hashtable();
               environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
               environment.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
               environment.put(Context.PROVIDER_URL, "jnp://localhost:1099"); // remote machine IP
               Context ic = new InitialContext(environment);
        	
            //gfTaskManagerRemote tsLocal = (TaskManagerRemote) ic.lookup(TaskManagerRemote.class.getName());
            TaskManagerRemote tsLocal = (TaskManagerRemote) ic.lookup("velo/TaskBean/remote");
            
            tm = tsLocal;
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }
    
    public boolean executeActionInTask(Task task) {
        //JB boolean status = tm.sendTaskToJms(task);
        boolean status = true;
        return status;
    }
    
    public ActionInterface getActionFromTask(Task task) {
        XStream xstream = new XStream(new DomDriver());
        //TODO: FUCK, FIX
        //try {
        	/*
            if (task.getTaskDefinition().isScripted()) {
                ScriptFactory sf = new ScriptFactory();
                ClassLoader parent = getClass().getClassLoader();
                GroovyClassLoader loader = new GroovyClassLoader(parent);
                Class groovyClass = loader.parseClass(task.getScript());
                
                xstream.setClassLoader(loader);
                //ActionInterface action = (ActionInterface)xstream.fromXML(task.getSerializedTask());
            }
            
            ActionInterface action = (ActionInterface) xstream.fromXML(task.getSerializedTask());
            
            return action;
        } catch (ClassCastException cce) {
            String errMsg ="A ClassCastException exception was occured, detailed message: " + cce.getMessage();
            System.out.println(errMsg);
            task.addLog("ERROR", "ClassCastException", errMsg);
            tm.indicateTaskExecutionFailure(task);
            return null;
        } catch (CompilationFailedException cfe) {
            String errMsg ="A CompilationFailedException exception was occured, detailed message: " + cfe.getMessage();
            System.out.println(errMsg);
            task.addLog("ERROR", "CompilationFailedException", errMsg);
            tm.indicateTaskExecutionFailure(task);
            return null;
        }
        */
        return null;
    }
}
