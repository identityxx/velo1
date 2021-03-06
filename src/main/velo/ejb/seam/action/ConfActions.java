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
package velo.ejb.seam.action;

import javax.ejb.Local;

import org.apache.commons.configuration.Configuration;

@Local
public interface ConfActions {
	public void refreshSysConf();
	public void saveSysConf();
	public String importInitialData();
	public Configuration getVeloConfig();
	public void generateResourcePrincipalsEncryptionKey();
	//public void generateUsersLocalPasswordsEncryptionKey();
	public String importTestData();
	
	
	//Sync default data
	public void syncReadyActions();
	public void syncSystemEvents();
	public void syncReconcileEvents();
	
	
	
	public void destroy();
	
	
	
	public void test();
}
