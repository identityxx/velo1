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

import velo.ejb.interfaces.AccountManagerRemote;
import velo.entity.Account;
import velo.exceptions.TaskCreationException;

public class AccountCli extends CliUtil {
    
    private AccountManagerRemote am;
    
    public AccountCli(AccountManagerRemote am) {
        this.am = am;
    }
    
    public void buildOptions() {
        Options options = new Options();
        
        //Option help = new Option( "H","help",false,"print this message" );
        
        //Option disable = new Option ("DL","disable",false,"Disable a user");
        
        options.addOption( OptionBuilder.withLongOpt( "account-name" )
                .withDescription( "The account name to perform the action on")
                .isRequired()
                .withValueSeparator( '=' )
                .hasArg()
                .create("A") );
        
        options.addOption( OptionBuilder.withLongOpt( "target-system" )
                .withDescription( "The name of the Target System the account is related to")
                .isRequired()
                .withValueSeparator( '=' )
                .hasArg()
                .create("T") );
        
        
        options.addOption( OptionBuilder.withLongOpt( "action" )
                .withDescription( "The action to perform on the specified account, action can be: [disable|enable|delete|reset-password]")
                .isRequired()
                .withValueSeparator( '=' )
                .hasArg()
                .create("O") );
        
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
            
            String accountName = line.getOptionValue("A");
            String action = line.getOptionValue("O");
            String tsName = line.getOptionValue("T");
            
            //Make sure the action is valid
            if (! ((action.toLowerCase().equals("disable")) || (action.toLowerCase().equals("delete")) || (action.toLowerCase().equals("enable")) || (action.toLowerCase().equals("reset_pass"))) ) {
                throw new ParseException("Action must be one of the following: [disable|enable|delete|reset_pass]");
            }
            
            System.out.println("Trying to execute action: '" + action + "' for account name: '" + accountName + "'" + ", on target system name: " + tsName);
            
            //Handle -DISABLE- action
            if (action.equals("disable")) {
                System.out.println("Disabling account name: " + accountName);
                try {
                    Account account = am.findAccount(accountName, tsName);
                    am.disableAccount(account, null, null,null);
                } catch (TaskCreationException tce) {
                    System.err.println("Could not DISABLE account name: '" + accountName + "', on Target System: '" + tsName + "', failure message is: " + tce.getMessage());
                    System.exit(1);
                } /*JB!!! catch (NoResultFoundException nrfe) {
                    System.err.println("Could not DISABLE account name: '" + accountName + "', on Target System: '" + tsName + "', account name was not found!");
                    System.exit(1);
                }*/
            }
            
            //Handle -ENABLE- action
            if (action.equals("enable")) {
                System.out.println("Enable account name: " + accountName);
                try {
                    Account account = am.findAccount(accountName, tsName);
                    am.enableAccount(account,null,null);
                } catch (TaskCreationException tce) {
                    System.err.println("Could not ENABLE account name: '" + accountName + "', on Target System: '" + tsName + "', failure message is: " + tce.getMessage());
                    System.exit(1);
                } /*JB!!! catch (NoResultFoundException nrfe) {
                    System.err.println("Could not ENABLE account name: '" + accountName + "', on Target System: '" + tsName + "', account name was not found!");
                    System.exit(1);
                }*/
            }
            
            //Handle -DELETE- action
            if (action.equals("delete")) {
                System.out.println("Enable account name: " + accountName);
                //try {
                    Account account = am.findAccount(accountName, tsName);
                    //am.deleteAccount(account,null,null,null);
                ////jb!!!} /*catch (TaskCreationException tce) {
                    //System.err.println("Could not DELETE account name: '" + accountName + "', on Target System: '" + tsName + "', failure message is: " + tce.getMessage());
                    //System.exit(1);
                //} /*jb!!! catch (NoResultFoundException nrfe) {
                    System.err.println("Could not DELETE account name: '" + accountName + "', on Target System: '" + tsName + "', account name was not found!");
                    System.exit(1);
                //}
            }
            
                        /*
                         
                        //Handle -RESET PASSWORD- action
                        if (action.equals("reset_pass")) {
                                System.out.println("Reset Password account name: " + accountName);
                                Account account = am.findAccountByName(accountName, tsName);
                                if (am.accountResetPassword(account)) {
                                        System.out.println("Successfully reset password account!");
                                }
                                else {
                                        System.err.println("Failed to reset password for account, see server log for details...");
                                }
                        }
                         */
            
            
        } catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println("Execution failed, following parameters are must: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "usercli", getOptions(), true );
        }
    }
    
}
