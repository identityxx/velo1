package velo.importer;

public class ImportRoleToRolesFolder {

	private String roleName;
	private String rolesFolderName;
	
	public ImportRoleToRolesFolder(String roleName, String rolesFolderName){
		this.roleName = roleName;
		this.rolesFolderName = rolesFolderName;		
	}
	
	
	public String getRoleName() {
		return roleName;
	}
	
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	public String getRolesFolderName() {
		return rolesFolderName;
	}
	
	public void setRolesFolderName(String rolesFolderName) {
		this.rolesFolderName = rolesFolderName;
	}
	
	
	
}
