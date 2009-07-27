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
import velo.ejb.interfaces.ResourceManagerRemote;
import velo.entity.Resource;
import velo.exceptions.OperationException;

public class ReconcileCli extends CliUtil {
	
	private static ResourceManagerRemote resourceManager;
	private static ReconcileManagerRemote recm;
	
	public ReconcileCli(ResourceManagerRemote resourceManager , ReconcileManagerRemote recm) {
		setRecm(recm);
		setResourceManager(resourceManager);
	}
	
	/**
	 * @param recm The recm to set.
	 */
	private static void setRecm(ReconcileManagerRemote recm) {
		ReconcileCli.recm = recm;
	}

	/**
	 * @return Returns the recm.
	 */
	private static ReconcileManagerRemote getRecm() {
		return recm;
	}

	public void buildOptions() {
		Options options = new Options();
		
		/*
		Option help = new Option( "H","help",false,"print this message" );
		Option projecthelp = new Option("PH","projecthelp", false,"print project help information" );
		Option version = new Option("version", "print the version information and exit" );
		Option quiet = new Option( "quiet", "be extra quiet" );
		Option verbose = new Option( "V","verbose", false,"be extra verbose" );
		Option debug = new Option( "D","debug", false,"print debugging information" );
		*/
		options.addOption( OptionBuilder.withLongOpt( "resource" )
				.withDescription( "Reconcile with the specified resource name" )
				.withValueSeparator( '=' )
				.isRequired()
				.hasArg()
				.create("R") );
		
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription( "Type the of the reconciliation: [identities / groups / group_membership]")
				.withValueSeparator( '=' )
				.isRequired()
				.hasArg()
				.create("T") );
		
		//.withDescription( "Type the of the reconciliation: [full / without-fetch-active-data / only-fetch-active-data ]")
		options.addOption( OptionBuilder.withLongOpt( "mode" )
				.withDescription( "Type the of the reconciliation: [full / incremental]")
				.withValueSeparator( '=' )
				.isRequired()
				.hasArg()
				.create("M") );
		
		setOptions(options);
	}
	
	
	/**
	 * @param rmr The rmr to set.
	 */
	public static void setResourceManager(ResourceManagerRemote resourceManager) {
		ReconcileCli.resourceManager = resourceManager;
	}

	/**
	 * @return Returns the rmr.
	 */
	public static ResourceManagerRemote getResourceManager() {
		return resourceManager;
	}

	public void start(String[] args) {
		//create the parser
		CommandLineParser parser = new PosixParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse( getOptions(), args );
			
			String resName = line.getOptionValue("R");
			String recType = line.getOptionValue("T");
			String mode = line.getOptionValue("M");
			System.out.println("Trying to execute Reconcile Process for resource name: '" + resName + "'");

			Resource loadedResource = getResourceManager().findResource(resName);
			
			if (loadedResource == null) {
				System.err.println("Resource with unique name '" + resName + "' does not exist!");
				return;
			}
			
			try {
				Boolean full = null;
				
				if (mode.equals("full")) {
					full = true; 
				} else if (mode.equals("incremental") ) {
					full = false;
				} else {
					System.err.println("Mode should be full/incremental.");
					return;
				}
				
				
				if (recType.equals("identities")) {
					if (mode.equals("full")) {
						getRecm().reconcileIdentitiesFull(loadedResource.getUniqueName(),true);
					}else {
						getRecm().reconcileIdentitiesIncrementally(loadedResource.getUniqueName(),true);
					}
				}else if (recType.equals("groups")) {
					getRecm().reconcileGroupsFull(loadedResource.getUniqueName(),true);
				}else if (recType.equals("group_membership")) {
					if (mode.equals("full")) {
						getRecm().reconcileGroupMembershipFull(loadedResource.getUniqueName(),true);
					}else {
						getRecm().reconcileGroupMembershipIncremental(loadedResource.getUniqueName(),true);
					}
				} else {
					System.err.println("Type should be identities/groups/group_membership");
					return;
				}

			} catch (OperationException e) {
					System.err.println( "Failed to execute: " + e.getMessage());
					return;
				}
				
				System.out.println("Sucessfully requrested reconciliation for Resource Name " + resName);
				System.out.println("Please follow internal logs for detailed information.");
			//}
		/*
			catch(ReconcileProcessException rpe) {
				System.err.println("Failed to reconcile, exception is: " + rpe.getMessage());
			}
		*/
			/*JB?!?!
			catch (TaskCreationException tce) {
				System.err.println("Failed to reconcile, exception is: " + tce.getMessage());
			}
			*/
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Failed to execute, these parameters are must: " + exp.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "reconcile", getOptions(), true );
		}
	}
}