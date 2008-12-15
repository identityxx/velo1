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

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.ResourceGroup;

@Name("resourceGroupList")
public class ResourceGroupList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(resourceGroup.uniqueId) like concat(lower(#{resourceGroupList.resourceGroup.uniqueId}),'%')",
			"lower(resourceGroup.displayName) like concat(lower(#{resourceGroupList.resourceGroup.displayName}),'%')",
			"lower(resourceGroup.description) like concat(lower(#{resourceGroupList.resourceGroup.description}),'%')",
			"lower(resourceGroup.type) like concat(lower(#{resourceGroupList.resourceGroup.type}),'%')",
			"lower(resourceGroup.resource.uniqueName) like lower(#{resourceGroupList.resourceUniqueName})"};

	private ResourceGroup resourceGroup = new ResourceGroup();

	private String resourceUniqueName;
	
	
	private static final String EJBQL = "select resourceGroup from ResourceGroup resourceGroup";
	
	
	/**
	 * @return the resourceUniqueName
	 */
	public String getResourceUniqueName() {
		return resourceUniqueName;
	}

	/**
	 * @param resourceUniqueName the resourceUniqueName to set
	 */
	public void setResourceUniqueName(String resourceUniqueName) {
		this.resourceUniqueName = resourceUniqueName;
		System.out.println("The resourceUniqueName field got value  " +  resourceUniqueName);
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public ResourceGroup getResourceGroup() {
		return resourceGroup;
	}

	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
}
