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

package velo.importer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Shakarchi Asaf
 */

public class RoleCreationUnit {
    
    private String roleName;
    
    private Map <String, String> positions = new HashMap <String, String>();
    
    public  List<String> positionsDisplayNamesAsList  = new ArrayList<String>();
    
    public List<String> positionsIdsAsList  = new ArrayList<String>();
    
    private Set<RoleResourceAttributeUnit> roleResourceAttributeUnits  = new HashSet<RoleResourceAttributeUnit>();
    
    private String resourceUniqueId;
    
    private boolean isToBeCreated;
    /**
	 * @return the isToBeCreated
	 */
	public boolean isToBeCreated() {
		
		return isToBeCreated;
	}




	/**
	 * @param isToBeCreated the isToBeCreated to set
	 */
	public void setToBeCreated(boolean isToBeCreated) {
		this.isToBeCreated = isToBeCreated;
	}




	/**
	 * @return the resourceUniqueId
	 */
	public String getResourceUniqueId() {
		return resourceUniqueId;
	}




	/**
	 * @param resourceUniqueId the resourceUniqueId to set
	 */
	public void setResourceUniqueId(String resourceUniqueId) {
		this.resourceUniqueId = resourceUniqueId;
	}




	public RoleCreationUnit() {
    }


	
	
	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}




	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}




	



	/**
	 * @return the roleResourceAttributeUnits
	 */
	public Set<RoleResourceAttributeUnit> getRoleResourceAttributeUnits() {
		System.out.println("The roleResourceAttributeUnits is "+roleResourceAttributeUnits.toString());
		return roleResourceAttributeUnits; 
	}




	/**
	 * @param roleResourceAttributeUnits the roleResourceAttributeUnits to set
	 */
	public void setRoleResourceAttributeUnits(
			Set<RoleResourceAttributeUnit> roleResourceAttributeUnits) {
		this.roleResourceAttributeUnits = roleResourceAttributeUnits;
	}


	
	public void addRoleResourceAttributeUnit(RoleResourceAttributeUnit rrau) {
		 getRoleResourceAttributeUnits().add(rrau);
	 }


	 public void addPosition(String positionUniqueId, String positionDisplayName){
		 getPositions().put(positionUniqueId, positionDisplayName);
		 getPositionsDisplayNamesAsList().add(positionDisplayName);
		 getPositionsIdsAsList().add(positionUniqueId);
	 }
	 
	 



	/**
	 * @return the positions
	 */
	public Map<String, String> getPositions() {
		return positions;
	}




	/**
	 * @param positions the positions to set
	 */
	public void setPositions(Map<String, String> positions) {
		this.positions = positions;
	}

	
	public List<String> getPositionsDisplayNamesAsList(){
	  	return positionsDisplayNamesAsList;
	    	
	    	
		
	}
	
	public List<String> getPositionsIdsAsList(){
	   	return positionsIdsAsList;
	    	
	    	
		
	}
}
