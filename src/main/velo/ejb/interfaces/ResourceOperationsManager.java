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
import java.util.Set;

import javax.ejb.Local;

import org.openspml.v2.msg.spmlsuspend.SuspendRequest;

import velo.entity.Account;
import velo.entity.Attribute;
import velo.entity.Resource;
import velo.entity.ResourceGroup;
import velo.entity.ResourceTask;
import velo.entity.Role;
import velo.entity.SpmlTask;
import velo.entity.User;
import velo.exceptions.OperationException;
import velo.exceptions.UserAuthenticationException;

public interface ResourceOperationsManager {
	
	//Create SPML requests
	public void createSuspendAccountRequest(Account account) throws OperationException;
	public SpmlTask createSuspendAccountRequestTask(Account account) throws OperationException;
	public void createResumeAccountRequest(Account account) throws OperationException;
	public SpmlTask createResumeAccountRequestTask(Account account) throws OperationException;
	public SpmlTask createDeleteAccountRequestTask(Account account, Set<Role> excludedProtectedRoleList, boolean forceDelete) throws OperationException;
	public void createDeleteAccountRequest(Account account, Set<Role> excludedProtectedRoleList, boolean forceDelete) throws OperationException;
	//public SpmlTask createModifyAccountRequestForUserTask(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke) throws OperationException;
	public SpmlTask createModifyAccountRequestForUserTask(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException;
	//public void createModifyAccountRequestForUser(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke) throws OperationException;
	public void createModifyAccountRequestForUser(Account account, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException;
	public void createModifyAccountRequestForUser(String accountName, String resourceUniqueName, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify) throws OperationException;
	public void createModifyAccountRequestForUser(String accountName, String resourceUniqueName, Set<ResourceGroup> memberOfGroupsToAdd, Set<ResourceGroup> memberOfGroupsToRevoke, List<Attribute> attrsToModify, String taskDescription) throws OperationException;
	
	//public SpmlTask createAddAccountRequestForUserTask(Resource resource, User user, Set<ResourceGroup> memberOfGroups) throws OperationException;
	public SpmlTask createAddAccountRequestForUserTask(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Account virtualAccount) throws OperationException;
	public SpmlTask createAddAccountRequestForUserTask(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Set<Role> roles) throws OperationException;
	public void createAddAccountRequestForUser(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Account virtualAccount) throws OperationException;
	public void createAddAccountRequestForUser(Resource resource, User user, Set<ResourceGroup> memberOfGroups, Set<Role> roles) throws OperationException;
	
	public void performResourceTask(ResourceTask resourceTask) throws OperationException;
	public void performSpmlTask(SpmlTask spmlTask) throws OperationException;
	public void performResourceFetchActiveDataOffline(ResourceTask resourceTask, Resource resource) throws OperationException;
	public void perform(Resource resource, SpmlTask spmlTask, SuspendRequest request) throws OperationException;
	
	public void authenticate(Resource resource,String userName, String password) throws UserAuthenticationException;
	//public boolean testConnectivity(Resource resource) throws OperationException; 
	
	//public void addAccountAction(Resource resource, SpmlAddRequest spmlRequest) throws OperationException;
	public void testConnectivity(Resource resource) throws OperationException;
}
