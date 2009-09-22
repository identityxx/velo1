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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import velo.ejb.interfaces.AccountManagerLocal;
import velo.ejb.interfaces.PositionManagerLocal;
import velo.ejb.interfaces.ResourceAttributeManagerLocal;
import velo.ejb.interfaces.ResourceManagerLocal;
import velo.ejb.interfaces.RoleManagerLocal;
import velo.entity.Position;
import velo.entity.PositionRole;
import velo.entity.PositionRolePK;
import velo.entity.Resource;
import velo.entity.Role;
import velo.entity.RolesFolder;
import velo.exceptions.OperationException;
import velo.exceptions.PersistEntityException;
import velo.importer.AccountsList;
import velo.importer.AccountsToUsersList;
import velo.importer.ImportAccount;
import velo.importer.ImportAccountToUser;
import velo.importer.ImportRoleToRolesFolder;
import velo.importer.RoleCreationUnit;
import velo.importer.RoleCreationUnitList;
import velo.importer.RolesToPosition;
import velo.importer.RolesToPositionList;
import velo.importer.RolesToRolesFolderList;
import velo.importer.UsersToPosition;
import velo.importer.UsersToPositionList;
@Stateful
@Scope(ScopeType.SESSION)
@Name("importActions")
public class ImportActionsBean implements ImportActions {

	@Logger
	private Log log;

	@EJB
	public AccountManagerLocal accountManager;
	
	@EJB
	public PositionManagerLocal positionManager;
	
	
	@EJB
	public RoleManagerLocal roleManager;
	
	
	@EJB
	public ResourceManagerLocal resourceManager;
	
	
	@EJB
	public ResourceAttributeManagerLocal resourceAttributeManager;
	
	
	@In
	FacesMessages facesMessages;
	
	
	@PersistenceContext
    public EntityManager em;
	
	
	
	private byte[] uploadedFile;
	private String contentType;
	private String fileName;
	private String spreadSheetName;
	private boolean createAccounts;
	
	private AccountsToUsersList importAccountsToUsersList;
	private RolesToPositionList importRolesToPositionList;
	private UsersToPositionList importUsersToPositionList;
	private RoleCreationUnitList importNewRolesList;
	private AccountsList importAccountsList;
	private RolesToRolesFolderList importRolesToRolesFolderList;
	
	/**
	 * @return the uploadedFile
	 */
	public byte[] getUploadedFile() {
		return uploadedFile;
	}

	/**
	 * @param uploadedFile the uploadedFile to set
	 */
	public void setUploadedFile(byte[] uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * @return the spreadSheetName
	 */
	public String getSpreadSheetName() {
		return spreadSheetName;
	}

	/**
	 * @param spreadSheetName the spreadSheetName to set
	 */
	public void setSpreadSheetName(String spreadSheetName) {
		this.spreadSheetName = spreadSheetName;
	}
	
	
	public String showAccountsToUsersAssoications() {
		try {
			if (getUploadedFile() != null) {
				importAccountsToUsersList = new AccountsToUsersList();
				//importAccountsToUsersList.importFromXls(new String(getUploadedFile()), getSpreadSheetName());
				//importAccountsToUsersList.importFromCsv(new String(getUploadedFile()));
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());
				importAccountsToUsersList.importFromXls(fileContent, getSpreadSheetName());

				FacesMessages.instance().add("Successfully loaded '"+ importAccountsToUsersList.size()+ "' rows.");

				return "/admin/ImportAccountsToUsersAssociationsTable.xhtml";
			} else {
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
			}
		} catch (Exception e) {
			log.debug("Could not handle Users to Position Association process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}

	
	
	public String performImportAccountsToUsers() {
		if (importAccountsToUsersList.size() > 0) {
			for (ImportAccountToUser currAccountToUser : importAccountsToUsersList) {
				
				if (createAccounts){
					log.trace("Creating Account name '#0', on resource '#1' and associating it to user #2", currAccountToUser.getAccountName(), currAccountToUser.getResourceName(), currAccountToUser.getUserName());
					accountManager.persistAccount(currAccountToUser.getAccountName(), currAccountToUser.getResourceName(), currAccountToUser.getUserName());
				}
				else{
					log.trace("Associating Account name '#0', on resource '#1' to User '#2'", currAccountToUser.getAccountName(), currAccountToUser.getResourceName(), currAccountToUser.getUserName());
					try {
						accountManager.associateAccountToUser(currAccountToUser.getAccountName(), currAccountToUser.getResourceName(), currAccountToUser.getUserName());
					} catch (OperationException ex) {
						FacesMessages.instance().add(FacesMessage.SEVERITY_WARN, ex.getMessage());
					}
				}
				
	        }

			importAccountsToUsersList.clear();
		}
		
		FacesMessages.instance().add("Finished association!");
		return "/admin/Home.seam";
	}
						
	
	public String showUsersToPositionAssociations() {
		try {
			if (getUploadedFile() != null) {
				log.debug("Getting uploaded xml file "+ getUploadedFile().toString() + "; Byte array size is " + getUploadedFile().length);
				importUsersToPositionList = new UsersToPositionList();
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());
				
				log.debug("Calling importFromXML method");
				log.debug("Printing importUsersToPositionList object "+ importUsersToPositionList.toString());
				importUsersToPositionList.parseImportedXmlFile(fileContent);
				
				
				FacesMessages.instance().add("Successfully loaded '" + importUsersToPositionList.size()+ "' rows.");
				
				return "/admin/ImportUsersToPositionAssociationsTable.xhtml";
			} else {
				
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
				
			
			}
			
		} catch (Exception e) {
			log.debug("Could not handle Users to Position Association process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}
	
	
	public String performImportUsersToPosition() {
		if (importUsersToPositionList.size() > 0) {
			for (UsersToPosition currUsersToPosition : importUsersToPositionList) {
				for (String userName : currUsersToPosition.getUsersNames()) {
					try {
						positionManager.associatePositionToUser(currUsersToPosition.getPositionUniqueId(),currUsersToPosition.getPositionDisplayName(), userName);
					} catch (OperationException ex) {
							FacesMessages.instance().add(FacesMessage.SEVERITY_WARN, ex.getMessage());
					  }
				}		
			}

			importUsersToPositionList.clear();
		}
		
		FacesMessages.instance().add("Finished association!");
		return "/admin/Home.seam";
	}
	
	
	public String showRolesToPositionAssociations() {
		try {
			if (getUploadedFile() != null) {
				importRolesToPositionList = new RolesToPositionList();
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());
				importRolesToPositionList.parseImportedXmlFile(fileContent);
				
				
				FacesMessages.instance().add("Successfully loaded '" + importRolesToPositionList.size()+ "' rows.");
				
				return "/admin/ImportRolesToPositionAssociationsTable.xhtml";
			} else {
				
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
			
			}
			
		} catch (Exception e) {
			log.debug("Could not handle Roles to Position Association process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}
	
	
	public String performImportRolesToPosition() {
		if (importRolesToPositionList.size() > 0) {
			for (RolesToPosition currRolesToPosition : importRolesToPositionList) {
				for (String roleName : currRolesToPosition.getRolesNames()) {
					try {
						positionManager.associatePositionToRole(currRolesToPosition.getPositionUniqueId(), currRolesToPosition.getPositionDisplayName(), roleName);
					} catch (OperationException ex) {
							FacesMessages.instance().add(FacesMessage.SEVERITY_WARN, ex.getMessage());
					  }
				}		
			}

			importRolesToPositionList.clear();
		}
		
		FacesMessages.instance().add("Finished association!");
		return "/admin/Home.seam";
	}
	
	
	
	public String showNewRolesAssociations() {
		try {
			if (getUploadedFile() != null) {
				importNewRolesList = new RoleCreationUnitList();
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());
				importNewRolesList.parseImportedXmlFile(fileContent);
				
				
				FacesMessages.instance().add("Successfully loaded '" + importNewRolesList.size()+ "' rows.");
				
				return "/admin/ImportNewRolesTable.xhtml";
			} else {
				
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
			
			}
			
		} catch (Exception e) {
			log.debug("Could not handle new roles creation  process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}

	
	public String performImportNewRoles() {
		
		if (importNewRolesList.size() > 0) {
			for (RoleCreationUnit currRoleCreationUnit : importNewRolesList) {
				String roleName = currRoleCreationUnit.getRoleName();
				//TODO: add description property to xml
				String roleDescription = roleName; 
				log.trace("Iterating over role named " + roleName);
				boolean isRoleToBeCreated = currRoleCreationUnit.isToBeCreated();
				String resourceUniqueId = currRoleCreationUnit.getResourceUniqueId();
				log.trace("The resource associated with the role  - " + resourceUniqueId);
				Resource resource = resourceManager.findResource(resourceUniqueId);
				List <Resource> resourceList = new ArrayList <Resource>();
					if(resource != null) {
						resourceList.add(resource);
					}	
				
				Role existingRole = roleManager.findRole(roleName);
				
				if(existingRole == null && isRoleToBeCreated) {
					Role iteratedRole = new Role();
					log.trace("New role was created: " + iteratedRole.toString());
					
					iteratedRole.setResources(resourceList);
					iteratedRole.setName(roleName);
					iteratedRole.setDescription(roleDescription);
					Date creationDate =  new Date();
					iteratedRole.setCreationDate(creationDate);
					//Add the ROleResourceAttribute(s)
					//Since adding of RoleResourceAttributes makes some mess, I've decided to create roles without it 
					//and to bind them manually/in any other way afterwards
					
					
				/*	Set<RoleResourceAttributeUnit> roleResourceAttributeUnits = currRoleCreationUnit.getRoleResourceAttributeUnits();		
					for(RoleResourceAttributeUnit currRRAU : roleResourceAttributeUnits) {
						log.trace("Iterating over RoleResourceAttributeUnits List; iterated attribute is " +currRRAU.getResourceAttributeName());
						ResourceAttribute ra = resource.findAttributeByName(currRRAU.getResourceAttributeName());
						if(ra != null) {
							//RoleResourceAttributeAsTextual roleResourceAttribute;
							//ResourceAttribute ra = resourceAttributeManager.findResourceAttributeByName(currRRAU.getResourceAttributeName(), resource);
							log.trace("The iterated resource attribute is found in repository : "+ ra.toString());
							RoleResourceAttribute roleResourceAttribute = new RoleResourceAttributeAsTextual(iteratedRole, ra);
							//log.trace("Persisting roleResourceAttribute " + roleResourceAttribute.toString() + " without adding it to role's collection of roleResourceAttributes");
							iteratedRole.getRoleResourceAttributes().add(roleResourceAttribute);
							
							//em.persist(roleResourceAttribute);
							log.trace("The iterated role now contains following  role resource attributes: " + iteratedRole.getRoleResourceAttributes().toString());
							
							
						}
					}
					
					*/
					
					//Add the Positions
					
					Iterator <Map.Entry<String,String>> it = currRoleCreationUnit.getPositions().entrySet().iterator();
				    
					
					while (it.hasNext()) {
						Map.Entry <String,String> me = (Map.Entry <String,String>)it.next();
						String positionId = me.getKey();
						String positionDisplayName = me.getValue();
						log.trace("Iterated over positions list");
						Position position = positionManager.findPosition(positionId);
						if(position != null) {
							log.trace("Found non null position " + positionId);
							PositionRole pr = new PositionRole(iteratedRole, position);
							log.trace("New position role has been created :" + pr.toString() + ", with role "+pr.getRole().toString());
							PositionRolePK prpk = pr.getPrimaryKey();
							log.trace("A primary key for a newly created positionRole entity: " + prpk.toString());
							iteratedRole.getPositionRoles().add(pr);
							log.trace("Associated found position to the iterated role");
						}
						
						else {
							FacesMessages.instance().add("Position " + positionDisplayName + " was not found in repository; skipping it");
						}
						
					}
					
					
					log.trace("Iterating over positions of role" + roleName + " is done");
					log.trace("The positions collection for role " + roleName + " is: " + iteratedRole.getPositionRoles().toString());
					
					try {
						
						
						log.trace("Persisting role named " + roleName);
						roleManager.createRole(iteratedRole);
						} 
					catch (PersistEntityException ex) {
						FacesMessages.instance().add(FacesMessage.SEVERITY_WARN, ex.getMessage());
					}
				}
				
				
				
				else {
					//TODO: add the logic for EXISTING role
					FacesMessages.instance().add("Since role named " + roleName + " already exists, it won't be created");
				}
					
			}		
			

			importNewRolesList.clear();
		}
		
		FacesMessages.instance().add("Finished association!");
		return "/admin/Home.seam";
	}
	
	

	
	
	
	
	public String showAccounts() {
		try {
			if (getUploadedFile() != null) {
				importAccountsList = new AccountsList();
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());
				importAccountsList.importFromXls(fileContent, getSpreadSheetName());

				FacesMessages.instance().add("Successfully loaded '"+ importAccountsList.size()+ "' rows.");

				return "/admin/ImportAccountsTable.xhtml";
			} else {
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
			}
		} catch (Exception e) {
			log.debug("Could not handle Resource Account Import process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}

	
	
	public String performImportAccounts() {
		if (importAccountsList.size() > 0) {
			for (ImportAccount currAccount : importAccountsList) {
				log.trace("Adding account name #0 to resource #1", currAccount.getAccountName(), currAccount.getResourceName());
                accountManager.persistAccount(currAccount.getAccountName(), currAccount.getResourceName(), null);
	        }

			importAccountsList.clear();
		}
		
		FacesMessages.instance().add("Finished resource accounts creation!");
		return "/admin/Home.seam";
	}	
	
	
	
	public String showRolesToRolesFolder() {
		try {
			if (getUploadedFile() != null) {
				importRolesToRolesFolderList = new RolesToRolesFolderList();
				ByteArrayInputStream fileContent = new ByteArrayInputStream(getUploadedFile());

				importRolesToRolesFolderList.importFromXls(fileContent, getSpreadSheetName());

				FacesMessages.instance().add("Successfully loaded '"+ importRolesToRolesFolderList.size()+ "' rows.");

				return "/admin/ImportRolesToRolesFolderTable.xhtml";
			} else {
				FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,
						"Failed to upload file, received data is null.");

				return null;
			}
		} catch (Exception e) {
			log.debug("Could not handle Resource Account Import process due to: "+ e.getMessage());
			FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());

			return null;
		}
	}

	
	
	public String performImportRolesToRolesFolder() {
		if (importRolesToRolesFolderList.size() > 0) {
			for (ImportRoleToRolesFolder currImportRole : importRolesToRolesFolderList) {
				log.trace("Creating role name #0 in roles folder #1", currImportRole.getRoleName(), currImportRole.getRolesFolderName());
				try{
					Role role = new Role();
					role.setName(currImportRole.getRoleName());
					role.setDescription(currImportRole.getRoleName());
					
					RolesFolder rf = roleManager.findRolesFolder(currImportRole.getRolesFolderName());
					if (rf != null){
						List<RolesFolder> rfList = new ArrayList<RolesFolder>();
						rfList.add(rf);
						role.setRolesFolders(rfList);
						Date creationDate = new Date();
						role.setCreationDate(creationDate);
						roleManager.createRole(role);
					}
					else
						log.debug("Can't create the role #0 in the roles folder #1 since roles folder does not exist!", currImportRole.getRoleName(), currImportRole.getRolesFolderName());
				}
				catch(PersistEntityException e){
					log.debug("Could not handle role Import process due to: "+ e.getMessage());
					FacesMessages.instance().add(FacesMessage.SEVERITY_ERROR,e.getMessage());
				}			
			}

			importRolesToRolesFolderList.clear();
		}
		
		FacesMessages.instance().add("Finished roles creation!");
		return "/admin/Home.seam";
	}		
	
	
	
	
	
	//accessors
	/**
	 * @return the importAccountsToUsersList
	 */
	public AccountsToUsersList getImportAccountsToUsersList() {
		return importAccountsToUsersList;
	}
	
	
	public UsersToPositionList getImportUsersToPositionList() {
		return importUsersToPositionList;
	}
	
	public RolesToPositionList getImportRolesToPositionList() {
		return importRolesToPositionList;
	}
	
	
	

	/**
	 * @return the importNewRolesList
	 */
	public RoleCreationUnitList getImportNewRolesList() {
		return importNewRolesList;
	}

	public AccountsList getImportAccountsList() {
		return importAccountsList;
	}

	public RolesToRolesFolderList getImportRolesToRolesFolderList() {
		return importRolesToRolesFolderList;
	}
	
	public boolean isCreateAccounts() {
		return createAccounts;
	}

	public void setCreateAccounts(boolean createAccounts) {
		this.createAccounts = createAccounts;
	}

	@Destroy
	@Remove
	public void destroy() {
	}

}
