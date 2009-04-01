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

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import velo.entity.Resource;
import velo.entity.Role;



@Name("roleList")
public class RoleList extends EntityQuery {
	
	public enum HasPositionsAssociatedStatus {
		ANY, ASSOCIATED, NOT_ASSOCIATED
	}
	
	private static final String[] RESTRICTIONS = {
			"lower(role.name) like concat(lower(#{roleList.role.name}),'%')",
			"lower(role.description) like concat(lower(#{roleList.role.description}),'%')",
			"lower(role.info1) like concat('%',lower(#{roleList.role.info1}),'%')",
			"lower(role.info2) like concat('%',lower(#{roleList.role.info2}),'%')",
			"lower(role.info3) like concat('%',lower(#{roleList.role.info3}),'%')",
			"role.disabled = #{roleList.role.disabled}",
			"lower(resources.uniqueName) like concat (lower( #{roleList.resourceUniqueName}),'%')"};
			
	private Role role = new Role();

	private String resourceUniqueName;
	
	private HasPositionsAssociatedStatus hasPositionsAssociated;
	
	//private final  String ejbQlBasicQuery  = "select role from Role role, IN(role.resources) resources";
	private final  String ejbQlBasicQuery  = "SELECT DISTINCT role FROM Role role LEFT JOIN role.resources resources";
	
	private String ejbQlQueryString;
	
	
	/**
	 * @return the ejbQlQueryString
	 */
	public String getEjbQlQueryString() {
		return ejbQlQueryString;
	}

	/**
	 * @param ejbQlQueryString the ejbQlQueryString to set
	 */
	public void setEjbQlQueryString(String ejbQlQueryString) {
		this.ejbQlQueryString = ejbQlQueryString;
	}

	


	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Role getRole() {
		return role;
	}

	public List roleSearchByNameAutoComplete(Object suggest) {
        String pref = (String)suggest;
        getRole().setName(pref);
        
        List roles = getResultList();
        
        return roles;
    }
	
	/**
	 * @return the hasPositionsAssociated
	 */
	public HasPositionsAssociatedStatus getHasPositionsAssociated() {
		return hasPositionsAssociated;
	}

	/**
	 * @param hasPositionsAssociated the hasPositionsAssociated to set
	 */
	public void setHasPositionsAssociated(HasPositionsAssociatedStatus hasPositionsAssociated) {
		this.hasPositionsAssociated = hasPositionsAssociated;
			if (hasPositionsAssociated == HasPositionsAssociatedStatus.ASSOCIATED || hasPositionsAssociated == HasPositionsAssociatedStatus.NOT_ASSOCIATED)
					addHasPositionsAssociatedRestriction();
		
	}
	
	
	public void addHasPositionsAssociatedRestriction() {
		
		
		if (this.hasPositionsAssociated == HasPositionsAssociatedStatus.ASSOCIATED){
			setEjbQlQueryString(" where role.positionRoles IS NOT EMPTY");
			
			
		}
		else if(this.hasPositionsAssociated == HasPositionsAssociatedStatus.NOT_ASSOCIATED) {
			setEjbQlQueryString(" where role.positionRoles IS EMPTY");
		}
		
		
	}
	
	@Factory("hasPositionsAssociatedStatuses")
	public HasPositionsAssociatedStatus[] getHasPositionsAssociatedStatuses() {
		return HasPositionsAssociatedStatus.values();
	}
	
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
	}

	/**
	 * @return the ejbQlBasicQuery
	 */
	public String getEjbQlBasicQuery() {
		return ejbQlBasicQuery;
	}
	
	
	
	public String getEjbql() {
		String basicQuery = getEjbQlBasicQuery();
		if(getEjbQlQueryString() == null)
			return basicQuery;
		else
			return basicQuery+getEjbQlQueryString();
	}
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	
    	String basicQuery = getEjbQlBasicQuery();
    	if(getEjbQlQueryString() == null)
    		setEjbql(basicQuery);
		else
			setEjbql(basicQuery+getEjbQlQueryString());
    }

}
