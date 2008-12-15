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
package velo.uiComponents;



//public class RolesFoldersTree extends TreeDataModel implements TreeNode {
public class RolesFoldersTree {
    /*
    private static final long serialVersionUID = 1L;
    private Map rolesFolders = null;
    private Object state1;
    private Object state2;
    private Collection<velo.entity.RolesFolder> rolesFoldersEntities;
    
    public RolesFoldersTree(Collection<velo.entity.RolesFolder> rolesFoldersEntities) {
        this.rolesFoldersEntities = rolesFoldersEntities;
    }
    
    private Map getRolesFolders() {
        if (this.rolesFolders==null) {
            initData();
        }
        
        return this.rolesFolders;
    }
    
    
    public void addRolesFolder(RolesFolder rolesFolder) {
        TreeNodeImpl tn = new TreeNodeImpl();
        tn.setParent(this);
        tn.setData(rolesFolder.getName());
        
        addChild(rolesFolder.getRolesFolderId(),tn);
    }
    
    public void addChild(Object identifier, TreeNode child) {
        getRolesFolders().put(identifier, child);
    }
    
    public TreeNode getChild(Object id) {
        return (TreeNode) getRolesFolders().get(id);
    }
    
    
    public Iterator getChildren() {
        return getRolesFolders().entrySet().iterator();
    }
    
    public Object getData() {
        return this;
    }
    
    public TreeNode getParent() {
        return null;
    }
    
    public boolean isLeaf() {
        return getRolesFolders().isEmpty();
    }
    
    public void removeChild(Object id) {
        getRolesFolders().remove(id);
    }
    
    public void setData(Object data) {
    }
    
    public void setParent(TreeNode parent) {
    }
    
    public String getType() {
        return "tree";
    }
    
    private void initData() {
        System.out.println("****INITING ROLES FOLDERS TREE WITH FOLDERS AMOUNT: '" + rolesFoldersEntities.size() + "' ****");
        rolesFolders = new java.util.HashMap();
        
        for (velo.entity.RolesFolder currRolesFolderEntity : rolesFoldersEntities) {
            velo.uiComponents.RolesFolder currRF = new velo.uiComponents.RolesFolder(currRolesFolderEntity.getRolesFolderId());
            currRF.setName(currRolesFolderEntity.getName());
            currRF.setParent(this);
            
            for (velo.entity.Role currRoleEntity : currRolesFolderEntity.getRoles()) {
                Role currRole = new Role(currRoleEntity.getRoleId());
                currRole.setName(currRoleEntity.getName());
                currRole.setParent(currRF);
                currRole.roleEntity = currRoleEntity;
                currRF.addChild(currRole.getRoleId(), currRole);
            }
            
            rolesFolders.put(currRF.getRolesFolderId(),currRF);
        }
    }
    
    public Object getState1() {
        return state1;
    }
    
    public void setState1(Object state1) {
        this.state1 = state1;
    }
    
    public Object getState2() {
        return state2;
    }
    
    public void setState2(Object state2) {
        this.state2 = state2;
    }
    */
}
