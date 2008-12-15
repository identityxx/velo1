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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import velo.utils.DataImporter;
import velo.ejb.interfaces.RoleManagerRemote;
import velo.exceptions.OperationException;
import org.apache.commons.cli.HelpFormatter;
import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.UserManagerRemote;
import velo.importer.AccountsToUsers;
import velo.importer.GroupsToRoles;
import velo.importer.UsersToRoles;
import java.io.File;

class ImportCli extends CliUtil {
    
    private RoleManagerRemote roleManager;
    private AccountManagerRemote accountManager;
    private UserManagerRemote userManager;
    
    public ImportCli(RoleManagerRemote roleManager, AccountManagerRemote accountManager, UserManagerRemote userManager) {
        this.roleManager = roleManager;
        this.accountManager = accountManager;
        this.userManager = userManager;
    }
    
    public void buildOptions() {
        Options options = new Options();
        
        //Option help = new Option( "H","help",false,"print this message" );
        
        //Option disable = new Option ("DL","disable",false,"Disable a user");
        
        options.addOption( OptionBuilder.withLongOpt( "type" )
            .withDescription( "The type of the data to import")
            .isRequired()
            .withValueSeparator( '=' )
            .hasArg()
            .create("T") );
        
        options.addOption( OptionBuilder.withLongOpt( "file" )
            .withDescription( "The XML file name contains the data to import")
            .isRequired()
            .withValueSeparator( '=' )
            .hasArg()
            .create("F") );
        
        setOptions(options);
    }
    
    
    public void start(String[] args) {
        //create the parser
        CommandLineParser parser = new PosixParser();
        try {
            System.out.println(getOptions());
            // parse the command line arguments
            CommandLine line = parser.parse( getOptions(), args );
            
            String commandName = line.getOptionValue("T");
            String fileName = line.getOptionValue("F");
            
            File f = new File(fileName);
            if (!f.isFile()) {
                System.err.println("Failed to perform operation, file named '" + fileName + "' was not found!");
                System.exit(1);
            }
            
            String[] allowedCommands = {"roles", "accountstousers", "groupstoroles", "userstoroles"};

            boolean isValidCommand = false;
            //for (int i=0;i<=allowedCommands.length;i++) {
            for (String currCmd : allowedCommands) {
                System.out.println("Specified Command: '" + commandName.toLowerCase() + ", curr allowed command: '" + currCmd + "'");
                if (currCmd.toLowerCase().equals(commandName.toLowerCase())) {
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
            if (commandName.toLowerCase().equals("roles")) {
                System.out.println("Importing Roles, please wait...");
                DataImporter di = new DataImporter(roleManager);
                di.execute(fileName);
            }
            else if (commandName.toLowerCase().equals("accountstousers")) {
                AccountsToUsers atu = new AccountsToUsers(accountManager);
                atu.execute(fileName);
            }
            else if (commandName.toLowerCase().equals("groupstoroles")) {
                GroupsToRoles gtr = new GroupsToRoles(roleManager);
                gtr.execute(fileName);
            }
            else if (commandName.toLowerCase().equals("userstoroles")) {
                UsersToRoles utr = new UsersToRoles(userManager);
                utr.execute(fileName);
            }
            else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "importer", getOptions(), true );
            }
        } catch (OperationException ex) {
            System.err.println("Execution faile due to: " + ex.getMessage());
        } 
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println("Execution failed, following parameters are must: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "importer", getOptions(), true );
        }
    }
    
}
