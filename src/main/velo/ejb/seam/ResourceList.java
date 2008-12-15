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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.Resource;

@Name("resourceList")
public class ResourceList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(resource.uniqueName) like concat(lower(#{resourceList.resource.uniqueName}),'%')",
			"lower(resource.displayName) like concat(lower(#{resourceList.resource.displayName}),'%')",
			"lower(resource.description) like concat(lower(#{resourceList.resource.description}),'%')",};

	private Resource resource = new Resource();

	private static final String EJBQL = "select resource from Resource resource";
	
	
	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Resource getResource() {
		return resource;
	}

	@Factory("resourceListString")
	public Map<String,String> getResourceListString() {
		Map<String,String> resMap = new HashMap<String,String>();
		List<Resource> res = getResultList();
		for (Resource currRes : res) {
			resMap.put(currRes.getUniqueName(), currRes.getDisplayName());
		}
		
		return resMap;
	}
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(EJBQL);
    }
	

}
