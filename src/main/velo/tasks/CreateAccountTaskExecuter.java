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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.RoleManagerRemote;
import velo.entity.Task;

public class CreateAccountTaskExecuter extends BasicTaskExecuter {
    AccountManagerRemote am;
    RoleManagerRemote rm;
    
    public CreateAccountTaskExecuter() {
        try {
            Context ic = new InitialContext();
            AccountManagerRemote amRemote = (AccountManagerRemote) ic.lookup("velo/AccountBean/remote");
            
            am = amRemote;
            
            
            RoleManagerRemote rmRemote = (RoleManagerRemote) ic.lookup("velo/RoleBean/remote");
            
            rm = rmRemote;
            
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }
    
    public boolean execute(Task task) {
        //If needed, keep the task script before execution (which after success might delete it)
        String taskScript = null;
        //TODO: FIX!!!!!!
        /*
        if (task.getTaskDefinition().isScripted()) {
            taskScript = task.getScript();
        }
        //Keep serialized task, needed later after 'executeActionInTask'(which aftrer success might delete it)
        String serializedTask = task.getSerializedTask();
        
        
        if (executeActionInTask(task)) {
            try {
                XStream xstream = new XStream(new DomDriver());
                if (task.getTaskDefinition().isScripted()) {
                    ScriptFactory sf = new ScriptFactory();
                    ClassLoader parent = getClass().getClassLoader();
                    GroovyClassLoader loader = new GroovyClassLoader(parent);
                    //Class groovyClass = loader.parseClass(task.getScript());
                    Class groovyClass = loader.parseClass(taskScript);
                    
                    xstream.setClassLoader(loader);
                    //ActionInterface action = (ActionInterface)xstream.fromXML(task.getSerializedTask());
                }
                
                //resourceAccountActionInterface action = (resourceAccountActionInterface)xstream.fromXML(task.getSerializedTask());
                ResourceAccountActionInterface action = (ResourceAccountActionInterface)xstream.fromXML(serializedTask);
                
                CreateAccountTools at = (CreateAccountTools)action.getTools();
                
                Account account = new Account();
                account.setUser(at.getUser());
                account.setName(at.getAccountId());
                account.setResource(at.getResource());
                //CANCLED -> NOT NEEDED, SINCE WE ADD THE ACCOUNT TO THE USER_ROLE(With cascade.PERSIST) WHICH WILL PERSIST THE ACCOUNT TOO!
                am.createAccount(account);
                
                //If a UesrRole was set then we need to update the UserRole that the account was created
                                
                                //if (at.getUserRole() != null) {
                                //        UserRole loadedUserRole = rm.findUserRoleByRoleAndUser(at.getUserRole().getUser(),at.getUserRole().getRole());
                                //        loadedUserRole.getAccounts().add(account);
                                //        rm.updateUserRole(loadedUserRole);
                                //}
                
                
                //Now, after everything was correcly done, then indicate a success of the task execution
                tm.indicateTaskExecutionSuccess(task);
                
                return true;
            } catch (ClassCastException cce) {
                tm.indicateTaskExecutionFailure(task);
                return false;
            } catch (CompilationFailedException cfe) {
                tm.indicateTaskExecutionFailure(task);
                return false;
            }
        } else {
            tm.indicateTaskExecutionFailure(task);
            return false;
        }
        */
        return true;
    }
}
