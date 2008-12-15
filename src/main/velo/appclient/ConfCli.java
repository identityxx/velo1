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
package velo.appclient;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import velo.ejb.interfaces.ConfManagerRemote;
import velo.ejb.interfaces.RequestManagerRemote;
import velo.ejb.interfaces.TaskManagerRemote;
import velo.exceptions.OperationException;

public class ConfCli extends CliUtil {
    
    private ConfManagerRemote confManager;
    private RequestManagerRemote requestManager;
    private TaskManagerRemote taskManager;
    
    public ConfCli(ConfManagerRemote confManager, RequestManagerRemote requestManager, TaskManagerRemote taskManager) {
        this.confManager = confManager;
        this.requestManager = requestManager;
        this.taskManager = taskManager;
    }
    
    public void buildOptions() {
        Options options = new Options();
        
        //Option help = new Option( "H","help",false,"print this message" );
        
        //Option disable = new Option ("DL","disable",false,"Disable a user");
        
        options.addOption( OptionBuilder.withLongOpt( "command" )
                .withDescription( "The command to execute, Possible commands are: \n- createInitialData\n- generateTargetsPrincipalsEncryptionKey\n- generateUsersInRepositoryEncryptionKey\n- initRequestsScanner\n- initTasksScanner")
                .isRequired()
                .withValueSeparator( '=' )
                .hasArg()
                .create("C") );
        
        setOptions(options);
    }
    
    
    public void start(String[] args) {
        //create the parser
        CommandLineParser parser = new PosixParser();
        try {
            //System.out.println(getOptions());
            // parse the command line arguments
            CommandLine line = parser.parse( getOptions(), args );
            
            String commandName = line.getOptionValue("C");
            
            String[] allowedCommands = {"createInitialData", "generateTargetsPrincipalsEncryptionKey","generateUsersInRepositoryEncryptionKey", "initRequestsScanner", "initTasksScanner", "syncCapabilities", "syncTaskDefinitions"};
            
            boolean isValidCommand = false;
            for (int i=0;i<allowedCommands.length;i++) {
                if (commandName.equals(allowedCommands[i])) {
                    isValidCommand = true;
                    break;
                }
            }
            
            //Make sure the action is valid
            if (!isValidCommand) {
                throw new ParseException("The specified command ('" + commandName + "') is not valid!");
            }
            
            System.out.println("Executing command name: " + commandName);
            //Create Initial Data command
            if (commandName.equals("createInitialData")) {
                confManager.persistInitialtData();
                System.out.println("Successfully imported initial data to the repository!");
                System.exit(1);
            }
            
            //Generate targets principals encryption key
            if (commandName.equals("generateTargetsPrincipalsEncryptionKey")) {
                try {
                    confManager.generateTargetsPrincipalsEncryptionKey();
                } catch (OperationException oe) {
                    System.err.println("An operation exception has occured: " + oe);
                    System.exit(1);
                }
            }
            
            if (commandName.toLowerCase().equals("initrequestsscanner")) {
                try {
                    requestManager.createTimerScanner();
                } catch (OperationException oe) {
                    System.err.println("An operation exception has occured: " + oe);
                    System.exit(1);
                }
            }
            
            if (commandName.toLowerCase().equals("inittasksscanner")) {
                try {
                    taskManager.createTimerScanner();
                } catch (OperationException oe) {
                    System.err.println("An operation exception has occured: " + oe);
                    System.exit(1);
                }
            }
            
            if (commandName.toLowerCase().equals("synccapabilities")) {
                //confManager.syncCapabilities();
            }
            
            if (commandName.toLowerCase().equals("synctaskdefinitions")) {
                //confManager.syncTaskDefinitions();
            }
            
        } catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println("Execution failed, following parameters are must: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "usercli", getOptions(), true );
        }
    }
    
}
