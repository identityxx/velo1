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

import velo.ejb.interfaces.ReconcileManagerRemote;
import velo.exceptions.ReconcileUsersException;

public class ReconcileUsersCli extends CliUtil {
	
	private static ReconcileManagerRemote recm;
	
	public ReconcileUsersCli(ReconcileManagerRemote recm) {
		setRecm(recm);
	}
	
	/**
	 * @param recm The recm to set.
	 */
	private static void setRecm(ReconcileManagerRemote recm) {
		ReconcileUsersCli.recm = recm;
	}

	/**
	 * @return Returns the recm.
	 */
	private static ReconcileManagerRemote getRecm() {
		return recm;
	}

	public void buildOptions() {
		Options options = new Options();
		
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription( "Type the of the reconciliation: [full / without-fetch-active-data]")
				.withValueSeparator( '=' )
				.isRequired()
				.hasArg()
				.create("S") );
		
		setOptions(options);
	}
	
	
	public void start(String[] args) {
		//create the parser
		CommandLineParser parser = new PosixParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse( getOptions(), args );
			
			String type = line.getOptionValue("S");
			System.out.println("Executing Reconcile Users Process");
				
			try {
				if (type.toLowerCase().equals("full")) {
					getRecm().reconcileUsers(true);
				}
				else if (type.toLowerCase().equals("without-fetch-active-data")){
					getRecm().reconcileUsers(false);
				}
				else {
					System.err.println( "Failed to execute,  The specified parameters were wrong.");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp( "reconcile", getOptions(), true );
				}
				
				System.out.println("Sucessfully requrested users reconciliation");
				System.out.println("Please follow internal logs for detailed information.");
			}
			catch (ReconcileUsersException rue) {
				System.err.println("Failed to reconcile users, exception is: " + rue.getMessage());
			}
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Failed to execute,  These parameters are must: " + exp.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "reconcile", getOptions(), true );
		}
	}
}