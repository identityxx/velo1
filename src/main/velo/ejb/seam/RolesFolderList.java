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
package velo.ejb.seam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.RolesFolder;
import velo.uiComponents.RolesFoldersTree;

@Name("rolesFolderList")
public class RolesFolderList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(rolesFolder.uniqueName) like concat(lower(#{rolesFolderList.rolesFolder.uniqueName}),'%')",
			"lower(rolesFolder.description) like concat(lower(#{rolesFolderList.rolesFolder.description}),'%')",};
	
	private String[] extraRestricions;
	
	//private RolesFoldersTree rolesFoldersTree;
	private RolesFolder rolesFolder = new RolesFolder();

	
	private static final String EJBQL = "select rolesFolder from RolesFolder rolesFolder";
	

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public RolesFolder getRolesFolder() {
		return rolesFolder;
	}
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }

	/*
	@Override
	public List<String> getRestrictions() {
		List<String> a = new ArrayList<String>();
		a.addAll(Arrays.asList(RESTRICTIONS));
		
		if ( (getExtraRestricions() != null) && (getExtraRestricions().length > 0) ) {
			a.addAll(Arrays.asList(getExtraRestricions()));
		}
		
		return a;
	}
	*/

	
	/*
	public String[] getExtraRestricions() {
		return extraRestricions;
	}

	public void setExtraRestricions(String[] extraRestricions) {
		this.extraRestricions = extraRestricions;
	}
	
	
	
	public List<RolesFolder> getRolesFolders(RolesFolder excluded) {
		if (excluded.getRolesFolderId() != null) {
			return getEntityManager().createQuery("select rolesFolder from RolesFolder rolesFolder WHERE rolesFolder.rolesFolderId != :excludedId").setParameter("excludedId", excluded.getRolesFolderId()).getResultList();
		} else {
			return getResultList();
		}
	}
	*/
	
	//public RolesFoldersTree getRolesFoldersTree() {
		/*
		if (rolesFoldersTree == null) {
			RolesFoldersTree rolesFoldersTree = new RolesFoldersTree(roleManager.loadRolesFolders(true));
			return rolesFoldersTree;
		}
		
		else {
			return rolesFoldersTree;
		}
		*/
    //}
}
