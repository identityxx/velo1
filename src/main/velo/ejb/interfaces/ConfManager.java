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
package velo.ejb.interfaces;

import velo.exceptions.OperationException;


/**
 * A Conf Manager interface for all EJB exposed methods
 *
 * @author Asaf Shakarchi
 */
public interface ConfManager {
    
    //public void syncCapabilities();
    //public void syncTaskDefinitions();

	
	void generateResourcePrincipalsEncryptionKey() throws OperationException;
	//public void generateUsersLocalPasswordsEncryptionKey() throws OperationException;
	
	
	
	
	//individual imports
	public void importReconcileEvents();
	public void importReadyActions();
	public void importSystemEvents();
	
	
	
	public void syncResourceTypes();
	public boolean isInitialDataImported();
    public void persistInitialtData();
    public void persistTestData();
    
    
	@Deprecated
    public void syncActionLanguages() throws OperationException;
	@Deprecated
    public void syncResourceTypeAttributes() throws OperationException;
	@Deprecated
    public void syncResourceActionDefinitions();
    
}
