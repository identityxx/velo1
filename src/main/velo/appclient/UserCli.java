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

import velo.ejb.interfaces.UserManagerRemote;
import velo.entity.User;
import velo.exceptions.NoResultFoundException;
import velo.exceptions.OperationException;

public class UserCli extends CliUtil {
	
	private UserManagerRemote userm;
	
	public UserCli(UserManagerRemote userm) {
		this.userm = userm;
	}
	
	public void buildOptions() {
		Options options = new Options();
		
		//Option help = new Option( "H","help",false,"print this message" );
		
		//Option disable = new Option ("DL","disable",false,"Disable a user");
		
		options.addOption( OptionBuilder.withLongOpt( "username" )
				.withDescription( "The username to perform the action on")
				.isRequired()
				.withValueSeparator( '=' )
				.hasArg()
				.create("U") );
		
		options.addOption( OptionBuilder.withLongOpt( "action" )
				.withDescription( "The action to perform on the specified user, action can be: [disable|reset_pass]")
				.isRequired()
				.withValueSeparator( '=' )
				.hasArg()
				.create("A") );
		
		//options.addOption( help );
		//options.addOption(disable);
		
		setOptions(options);
	}
	
	
	public void start(String[] args) {
		//create the parser
		CommandLineParser parser = new PosixParser();
		try {
			System.out.println(getOptions());
			// parse the command line arguments
			CommandLine line = parser.parse( getOptions(), args );
			String username = line.getOptionValue("U");
			String action = line.getOptionValue("A");
			
			if (! ( (action.equals("disable")) || (action.equals("reset_pass")) ) ) {
				throw new ParseException("Action must be one of the following: [disable|reset_pass]");
			}
			
			System.out.println("Trying to execute action: '" + action + "' for user name: '" + username + "'");
			
			try {
				User user = userm.findUserByName(username);
				System.out.println("Found User ID " + user.getUserId() + ", with name: " + user.getName());
				
				if (action.equals("disable")) {
					
					System.out.println("Size of accounts to disable: " + user.getAccounts().size());
					System.out.println("Disabling...");
/*					try {
						userm.disableUser(user);
						System.out.println("Successfully disabled all accounts of user name: " + user.getName());
					}
					catch (OperationException oe) {
						System.err.println(oe.getMessage());
						System.out.println("Failed to perform DISABLE process for one or more accounts of User name: " + user.getName() + ", detailed failure message: " + oe.getMessage());
					}
*/					
					System.out.println("Ended Disable User process...");
				}
				
				
				if (action.equals("reset_pass")) {
					System.out.println("Size of accounts to reset passwords for: " + user.getAccounts().size());
					System.out.println("Reseting Passwords...");
					/*
					if (userm.resetPasswordForUser(user)) {
					System.out.println("Successfully reset password for all accounts of user name: " + user.getName());
					}
					else {
					System.out.println("Failed to perform RESET PASSWORD process for one or more accounts of User name: " + user.getName() + ", see server logs for more information...");
					}
					 */
					System.out.println("Ended Reset Password User process...");
				}
			}
			catch (NoResultFoundException nrfe) {
				System.err.println("Error: Cannot perform action: '" + action + "' over User name: '" + username + "' since the specified user name was not found in repository!");
			}
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Execution failed, following parameters are must: " + exp.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "usercli", getOptions(), true );
		}
	}	
}
