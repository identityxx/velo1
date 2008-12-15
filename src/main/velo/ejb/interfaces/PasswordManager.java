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

import java.util.List;

import velo.entity.PasswordPolicy;
import velo.entity.PasswordPolicyContainer;
import velo.exceptions.NoResultFoundException;

/**
 * A Password Manager interface for all EJB exposed methods
 * 
 * @author Asaf Shakarchi
 */
public interface PasswordManager {
	
	/**
	 * Persist a PasswordPolicy entity in the database
	 * @param pp The PasswordPolicy entity to persist
	 */
	@Deprecated
	public void persistPasswordPolicyEntity(PasswordPolicy pp);
	
	
	/**
	 * Read all Password Policies 
	 * @return A collection of PasswordPolicies fetched from the database
	 */
	@Deprecated
	public List<PasswordPolicy> loadAllPasswordPolicies();
	
	
	/**
	 * Find a PasswordPolicy by unique name
	 * @param uniqueName The unique name of the password policy to find
	 * @return A loaded PasswordPolicy entity
	 * @throws NoResultFoundException
	 */
	@Deprecated
	public PasswordPolicy findPasswordPolicyByUniqueName(String uniqueName) throws NoResultFoundException;
	
	
	/**
	 * Whether or not a password policy exists by its unique name
	 * @param uniqueName The unique name to check by whether or not a PasswordPolicy exists in DB
	 * @return true/false upon existance/non-existance
	 */
	@Deprecated
	public boolean isPasswordPolicyExistsByUniqueName(String uniqueName);
	
	
	/**
	 * Delete a password policy entity from the database
	 * @param pp The PasswordPolicy entity to delete
	 */
	@Deprecated
	public void deletePasswordPolicy(PasswordPolicy pp);
	
	
	/**
	 * Merge an existed PasswordPolicy entity in the database
	 * @param pp The password policy entity to merge
	 */
	@Deprecated
	public void mergePasswordPolicy(PasswordPolicy pp);
	
	
	/**
	 * Persist a PasswordPolicyContainer entity in the database
	 * @param pp The PasswordPolicyContainer entity to persist
	 */
	@Deprecated
	public void persistPasswordPolicyContainerEntity(PasswordPolicyContainer pp);

	/**
	 * Load all Password Policy containers from the repository
	 * @return A collection of Password Policy Containers fetched from the database
	 */
	@Deprecated
	public List<PasswordPolicyContainer> loadAllPasswordPolicyContainers();

	/**
	 * Find a Password Policy Containers by unique name
	 * @param uniqueName The unique name of the password policy to find
	 * @return A loaded PasswordPolicyContainer entity
	 * @throws NoResultFoundException
	 */
	@Deprecated
	public PasswordPolicyContainer findPasswordPolicyContainerByUniqueName(String uniqueName) throws NoResultFoundException;
	
	
	/**
	 * Whether or not a password policy container exists by its unique name
	 * @param uniqueName The unique name to check by whether or not a PasswordPolicyContainer exists in repository
	 * @return true/false upon existance/non-existance
	 */
	@Deprecated
	public boolean isPasswordPolicyContainerExistsByUniqueName(String uniqueName);
	
	
	/**
	 * Delete a password policy container entity from the database
	 * @param pp The PasswordPolicyContainer entity to delete
	 */
	@Deprecated
	public void deletePasswordPolicyContainer(PasswordPolicyContainer pp);
	
	
	/**
	 * Merge an existed PasswordPolicyContainer entity in the database
	 * @param pp The password policy container entity to merge
	 */
	@Deprecated
	public void mergePasswordPolicyContainer(PasswordPolicyContainer pp);
	
	
}
