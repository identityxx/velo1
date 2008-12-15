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
import velo.exceptions.OperationException;
import org.apache.commons.cli.HelpFormatter;
import velo.ejb.interfaces.AccessGuardianRemote;

class ScannersCli extends CliUtil {
    
    private AccessGuardianRemote accessGuardian;
    
    public ScannersCli(AccessGuardianRemote accessGuardian) {
        this.accessGuardian = accessGuardian;
    }
    
    public void buildOptions() {
        Options options = new Options();
        
        options.addOption( OptionBuilder.withLongOpt( "scanner" )
            .withDescription( "The scanner to perform the action on")
            .isRequired()
            .withValueSeparator( '=' )
            .hasArg()
            .create("S") );
        
        options.addOption( OptionBuilder.withLongOpt( "action" )
            .withDescription( "The action to perform on the specified scanner(start/stop)")
            .isRequired()
            .withValueSeparator( '=' )
            .hasArg()
            .create("A") );
        
        setOptions(options);
    }
    
    
    public void start(String[] args) {
        //create the parser
        CommandLineParser parser = new PosixParser();
        try {
            System.out.println(getOptions());
            // parse the command line arguments
            CommandLine line = parser.parse( getOptions(), args );
            
            String scannerName = line.getOptionValue("S");
            String actionName = line.getOptionValue("A");
            
            String[] availableScanners = {"access_guardian_scanner", "tasks_scanner", "requests_scanner"};

            boolean isValidCommand = false;
            //for (int i=0;i<=allowedCommands.length;i++) {
            for (String currCmd : availableScanners) {
                System.out.println("Specified Command: '" + scannerName.toLowerCase() + ", curr allowed command: '" + currCmd + "'");
                if (currCmd.toLowerCase().equals(scannerName.toLowerCase())) {
                    isValidCommand = true;
                    break;
                }
            }
            
            
            //Make sure the action is valid
            if (!isValidCommand) {
                throw new ParseException("The specified command ('" + scannerName + "') is not valid!");
            }
            
            System.out.println("Executing command name: " + scannerName);
            //Create Initial Data command
            if (scannerName.toLowerCase().equals("access_guardian_scanner")) {
                System.out.println("Performing action '" + actionName + "' over scanner '" + scannerName + "'");
                
                
                boolean isActivated = accessGuardian.isAccessGuardianScannerActivate();
                if (actionName.equalsIgnoreCase("start")) {
                	if (isActivated) {
                		System.out.println("Access Guardian scanner is already activated...");
                	} else {
                		accessGuardian.changeAccessGuardianScannerMode();
                	}
                } else if (actionName.equalsIgnoreCase("stop")) {
                	if (!isActivated) {
                		System.out.println("Access Guardian scanner is already NOT activated...");
                	} else {
                		accessGuardian.changeAccessGuardianScannerMode();
                	}
                }
            }
            else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "scannerCli", getOptions(), true );
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
