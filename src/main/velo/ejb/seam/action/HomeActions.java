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

import java.io.File;
import java.util.List;

import javax.ejb.Local;

import velo.ejb.seam.RequestList;
import velo.ejb.seam.TaskList;
import velo.ejb.seam.UserList;
import velo.ejb.seam.WorkflowProcessList;
import velo.entity.Task;
import velo.entity.User;

@Local
public interface HomeActions {
	public byte[] getChart();
	public TaskList getFailedTaskList();
	
	@Deprecated
	public RequestList getLastFailedRequests();
	@Deprecated
	public RequestList getLastRequestsWaitingForApproval();
	@Deprecated
	public RequestList getLastApprovedRequests();
	
	public WorkflowProcessList getLastProcessList();
	public WorkflowProcessList getLastSuspendedProcessList();
	
	public UserList getLastCreatedUsers();
	
	public List<Task> getLastCreatedUsersAsList();
	
	
	//@WebRemote
	public String getArray();
	
	public void fun();
	
	//public byte[] getLogo();
	public File getLogo();
	
	public void test();
	public void destroy();
}