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

import velo.entity.ApproversGroup;
import velo.entity.Resource;
import velo.entity.ResourceAttribute;
import velo.entity.ResourceGroup;
import velo.entity.RoleApproversGroup;
import velo.entity.RoleResourceAttribute;

@Local
public interface RoleActions {
	public String performModifyGroupsInRole();
	public String modifyGroupsInRole();
	public void modifyApproversGroupsInRole();
	
	
	
	public void addGroupToRevoke(ResourceGroup group);
	public void removeGroupToRevoke(ResourceGroup group);
	public void addGroupToAssign(ResourceGroup group);
	public void removeGroupToAssign(ResourceGroup group);
	
	
	
	public void addRoleApproversGroupToDelete(RoleApproversGroup rag);
	public void removeRoleApproversGroupToDelete(RoleApproversGroup rag);
	public void addApproversGroupToAssign(ApproversGroup ag);
	public void removeApproversGroupToAssign(ApproversGroup ag);
	
	
	
	public void addRoleResourceAttributesToDelete(RoleResourceAttribute rra);
	public void removeRoleResourceAttributesToDelete(RoleResourceAttribute rra);
	public void addTextualResourceAttributeToAssign(ResourceAttribute rra);
	public void addRuleResourceAttributeToAssign(ResourceAttribute rra);
	public void removeResourceAttributeToAssign(RoleResourceAttribute rra);
	public void modifyResourceAttributesInRole();
	
	
	
	public Resource getSelectedResource();
	public void setSelectedResource(Resource selectedResource);
	public String changeRoleStatus();
	
	
	public void destroy();
}