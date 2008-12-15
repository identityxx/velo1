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

import velo.entity.Position;

@Name("positionList")
public class PositionList extends EntityQuery {

	public enum HasRolesAssociatedStatus {
		ANY, ASSOCIATED, NOT_ASSOCIATED
	}
		
	private final static String[] RESTRICTIONS = {"lower(position.displayName) like concat(lower(#{positionList.position.displayName}),'%')",
												  "lower(position.uniqueOrgUnitId) like concat(lower(#{positionList.position.uniqueOrgUnitId}),'%')",
												  "position.disabled = #{positionList.position.disabled}",
												  "position.creationDate >= #{positionList.position.fromCreationDate}",
												  "position.creationDate <= #{positionList.position.toCreationDate}"};
												
	
	
	private Position position = new Position();
	
	private HasRolesAssociatedStatus hasRolesAssociated;

	private final  String ejbQlBasicQuery = "select position from Position position";
	
	private String ejbQlQueryString;
	
	
	
	
	@Override
	public String getEjbql() {
		String basicQuery = getEjbQlBasicQuery();
		if(getEjbQlQueryString() == null)
			return basicQuery;
		else
			return basicQuery+getEjbQlQueryString();
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Position getPosition() {
		return position;
	}

	
	public void addHasRolesAssociatedRestriction() {
		
				
		if (this.hasRolesAssociated == HasRolesAssociatedStatus.ASSOCIATED){
			setEjbQlQueryString(" where position.positionRoles IS NOT EMPTY");
			
		}
		else if(this.hasRolesAssociated == HasRolesAssociatedStatus.NOT_ASSOCIATED) {
			setEjbQlQueryString(" where position.positionRoles IS EMPTY");
		}
		
		
	}
	
	

	/**
	 * @return the hasRolesAssociated
	 */
	public HasRolesAssociatedStatus getHasRolesAssociated() {
		return hasRolesAssociated;
	}

	/**
	 * @param hasRolesAssociated the hasRolesAssociated to set
	 */
	public void setHasRolesAssociated(HasRolesAssociatedStatus hasRolesAssociated) {
		this.hasRolesAssociated = hasRolesAssociated;
			if (hasRolesAssociated == HasRolesAssociatedStatus.ASSOCIATED || hasRolesAssociated == HasRolesAssociatedStatus.NOT_ASSOCIATED) 
				addHasRolesAssociatedRestriction();
		
	}
	
	
	@Factory("hasRolesAssociatedStatuses")
	public HasRolesAssociatedStatus[] getHasRolesAssociatedStatuses() {
		return HasRolesAssociatedStatus.values();
	}

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

	/**
	 * @return the ejbQlBasicQuery
	 */
	public String getEjbQlBasicQuery() {
		return ejbQlBasicQuery;
	}
	
	
	
	@PostConstruct
    public void initialize() {
    	setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    	setEjbql(getEjbql());
    }
}
