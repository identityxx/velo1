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

import velo.entity.SelfServiceAccessRequest;
import velo.entity.User;

@Local
public interface SelfServiceAccessRequestActions {

	public SelfServiceAccessRequest getRequest();
	
	public void setRequest(SelfServiceAccessRequest request);
	
	public String accessIsRequestedForMyself();
	
	public String accessIsRequestedForOtherUser();
	public String setUserThatHasMyAccess(User user);
	
	public String submitRequest();
	
	
	//workflow tasks
	public void reviewWorkflowTask();
	public void approveWorkflowTask();
	public String rejectWorkflowTask();
	public void finalRequestApproval();
	public void finalRequestReject();
	public String[] assignPoolActors(Integer level);
	public String isThereApproversInLevel(Integer level);
	
	
	public void cancelRequest();
	public void readMyLastRequestedRequests();
	
	
	
	//accessors
	public String getApprovalTaskComment();
	public void setApprovalTaskComment(String approvalTaskComment);
	public void destroy();
	
}