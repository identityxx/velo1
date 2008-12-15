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
import velo.ejb.interfaces.PositionManagerRemote;

class PositionReconciliationCli extends CliUtil {
    
    private PositionManagerRemote positionManager;
    
    public PositionReconciliationCli(PositionManagerRemote positionManager) {
        this.positionManager = positionManager;
    }
    
    public void buildOptions() {
        Options options = new Options();
        
        options.addOption( OptionBuilder.withLongOpt( "type" )
            .withDescription( "The position syncyronization type to perform")
            .isRequired()
            .withValueSeparator( '=' )
            .hasArg()
            .create("T") );
        
        
        setOptions(options);
    }
    
    
    public void start(String[] args) {
        //create the parser
        CommandLineParser parser = new PosixParser();
        try {
            System.out.println(getOptions());
            // parse the command line arguments
            CommandLine line = parser.parse( getOptions(), args );
            
            String type = line.getOptionValue("T");
            String[] availableTypes = {"only_sync"};

            boolean isValidCommand = false;
            for (String currType : availableTypes) {
                //System.out.println("Specified type: '" + type.toLowerCase() + ", curr allowed command: '" + currCmd + "'");
                if (currType.toLowerCase().equals(type.toLowerCase())) {
                    isValidCommand = true;
                    break;
                }
            }
            
            
            //Make sure the action is valid
            if (!isValidCommand) {
                throw new ParseException("The specified command ('" + type + "') is not valid!");
            } else {
            	//execute
            	try {
            		positionManager.reconcilePositions();
            	}catch (OperationException e) {
            		System.err.println("An error has occurd: " + e.getMessage());
            	}
            }
        } catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println("Execution failed, following parameters are must: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "posSyncCli", getOptions(), true );
        }
    }
    
}
