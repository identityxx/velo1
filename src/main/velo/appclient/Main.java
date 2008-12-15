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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import velo.ejb.interfaces.AccessGuardianRemote;
import velo.ejb.interfaces.AccountManagerRemote;
import velo.ejb.interfaces.ConfManagerRemote;
import velo.ejb.interfaces.PositionManagerRemote;
import velo.ejb.interfaces.ReconcileManagerRemote;
import velo.ejb.interfaces.RequestManagerRemote;
import velo.ejb.interfaces.ResourceManagerRemote;
import velo.ejb.interfaces.RoleManagerRemote;
import velo.ejb.interfaces.TaskManagerRemote;
import velo.ejb.interfaces.UserManagerRemote;

public class Main { // appclient main class must be public

	private static ResourceManagerRemote resourceManager;
	private static ReconcileManagerRemote recm;
	private static RoleManagerRemote roleManager;
	private static AccountManagerRemote accm;
	private static UserManagerRemote userm;
	private static AccessGuardianRemote accessGuardianManager;
	private static PositionManagerRemote positionManagerRemote;
	private static InitialContext initialContext;

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Parameters are wrong...");
			System.exit(1);
		}

		if (args[0].toLowerCase().equals("reconcilecli")) {
			System.out.println("Executed reconcile CLI utility");
			ReconcileCli rc = new ReconcileCli(getResourceManager(), getReconcileManager());
			rc.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("reconcileuserscli")) {
			System.out.println("Executed Reconcile Uesrs CLI utility");
			ReconcileUsersCli recuc = new ReconcileUsersCli(recm);
			recuc.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("reconcileidentityattributes")) {
			ReconcileIdentityAttributes recIA = new ReconcileIdentityAttributes(getReconcileManager());
			recIA.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("usercli")) {
			System.out.println("Executed User CLI utility");
			UserCli userCli = new UserCli(userm);
			userCli.start(prepareArgs(args));
		} else if (args[0].toLowerCase().toLowerCase().equals("accountcli")) {
			System.out.println("Executed Account CLI utility");
			AccountCli accountCli = new AccountCli(accm);
			accountCli.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("sysconf")) {
			System.out.println("Executed System Conf CLI utility");
			ConfCli confCli = new ConfCli(getConfBean(), getRequestBean(),
					getTaskBean());
			confCli.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("pos_rec_cli")) {
				System.out.println("Executed Position Reconciliation CLI utility");
				PositionReconciliationCli posRecCli = new PositionReconciliationCli(getPositionManager());
				posRecCli.start(prepareArgs(args));
		} else if (args[0].toLowerCase().equals("importer")) {
			System.out.println("Executed Importer CLI utility");
			ImportCli importCli = new ImportCli(roleManager, accm, userm);
			importCli.start(prepareArgs(args));
		} else if (args[0].toLowerCase().toLowerCase().equals("scannerscli")) {
			System.out.println("Executed Scanners CLI utility");
			ScannersCli scannersCli = new ScannersCli(getAccessGuardianManager());
			scannersCli.start(prepareArgs(args));
		} else {
			System.err
					.println("There is no such utility name in this client application...");
		}
	}

	public static String[] prepareArgs(String[] args) {
		String[] newArgs = new String[args.length - 1];
		int j = 0;
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				newArgs[j] = args[i];
				j++;
			}
		}

		return newArgs;
	}

	public static ConfManagerRemote getConfBean() {
		try {
			ConfManagerRemote confManager = (ConfManagerRemote) getInitialContext()
					.lookup("velo/ConfBean/remote");
			return confManager;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static RequestManagerRemote getRequestBean() {
		try {
			RequestManagerRemote requestManager = (RequestManagerRemote) getInitialContext()
					.lookup("velo/RequestBean/remote");
			return requestManager;

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ResourceManagerRemote getResourceManager() {
		try {
			ResourceManagerRemote rm = (ResourceManagerRemote) getInitialContext()
					.lookup("velo/ResourceBean/remote");
			return rm;

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ReconcileManagerRemote getReconcileManager() {
		try {
			ReconcileManagerRemote rm = (ReconcileManagerRemote) getInitialContext()
					.lookup("velo/ReconcileBean/remote");
			return rm;

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static TaskManagerRemote getTaskBean() {
		try {
			TaskManagerRemote taskManager = (TaskManagerRemote) getInitialContext()
					.lookup("velo/TaskBean/remote");
			return taskManager;

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static AccessGuardianRemote getAccessGuardianManager() {
		try {
			AccessGuardianRemote accessGuardianBean = (AccessGuardianRemote) getInitialContext()
					.lookup("velo/AccessGuardianBean/remote");
			return accessGuardianBean;

		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	public static PositionManagerRemote getPositionManager() {
		try {
			PositionManagerRemote posBean = (PositionManagerRemote) getInitialContext()
					.lookup("velo/PositionBean/remote");
			return posBean;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static InitialContext getInitialContext() throws NamingException {
		if (initialContext == null) {
			try {
				Hashtable environment = new Hashtable();
		        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
		        environment.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		        environment.put(Context.PROVIDER_URL, "jnp://localhost:1099"); // remote machine IP
		        
				InitialContext ie = new InitialContext(environment);

				return ie;
			} catch (NamingException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return initialContext;
		}
	}
}
