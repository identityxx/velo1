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
/*
 * RolesFolder.java
 *
 * Created on 1 אפריל 2007, 18:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package velo.uiComponents;





/**
 *
 * @author Administrator
 */
//public class RolesFolder extends TreeDataModel implements TreeNode {
public class RolesFolder {
    
	/*
    public long rolesFolderId;
    private Map roles = new HashMap();
    private String name;
    private RolesFolder parentRolesFolder;
    private RolesFoldersTree rolesFoldersTree;
    
    
    // Creates a new instance of RolesFolder
    public RolesFolder(long rolesFolderId) {
        this.rolesFolderId = rolesFolderId;
    }
    
    public void addRole(Role role) {
        addChild(Long.toString(role.getRoleId()), role);
        role.setParent(this);
    }
    
    public void addChild(Object identifier, TreeNode child) {
        roles.put(identifier, child);
    }
    
    public TreeNode getChild(Object id) {
        return (TreeNode) roles.get(id);
    }
    
    public Iterator getChildren() {
        return roles.entrySet().iterator();
    }
    
    public Object getData() {
        return this;
    }
    
    public TreeNode getParent() {
        if (parentRolesFolder != null) {
            return parentRolesFolder;
        } else {
            return rolesFoldersTree;
        }
    }
    
    public boolean isLeaf() {
        return roles.isEmpty();
    }
    
    public void removeChild(Object id) {
        roles.remove(id);
    }
    
    public void setData(Object data) {
    }
    
    public void setParent(TreeNode parent) {
        rolesFoldersTree = (RolesFoldersTree) parent;
    }
    
    public long getRolesFolderId() {
        return rolesFolderId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public RolesFoldersTree getRolesFoldersTree() {
        return rolesFoldersTree;
    }
    
    public void setRolesFoldersTree(RolesFoldersTree rolesFoldersTree) {
        this.rolesFoldersTree = rolesFoldersTree;
    }
    
    public String getType() {
        return "roles_folder";
    }
    */
}
