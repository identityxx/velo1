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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;



public class RequestsTree implements TreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3530085227471752526L;
	private Map inbox = null;
	private Object state1;
	private Object state2;

	private Map<String,TreeNode> getInbox() {
		if (this.inbox==null) {
			initData();
		}
		return this.inbox;
	}
	
	public void addRequestType(String requestType) {
		TreeNodeImpl tn = new TreeNodeImpl();
		tn.setParent(this);
		tn.setData(requestType);
		
		addChild(requestType,tn);
	}
	
	public void addChild(Object identifier, TreeNode child) {
		getInbox().put(identifier.toString(), child);
	}

	public TreeNode getChild(Object id) {
		return (TreeNode) getInbox().get(id);
	}

	public Iterator getChildren() {
		return getInbox().entrySet().iterator();
	}

	public Object getData() {
		return this;
	}

	public TreeNode getParent() {
		return null;
	}

	public boolean isLeaf() {
		return getInbox().isEmpty();
	}

	public void removeChild(Object id) {
		getInbox().remove(id);
	}

	public void setData(Object data) {
	}

	public void setParent(TreeNode parent) {
	}

	public String getType() {
		return "library";
	}
	
	
	private void initData() {
		inbox = new HashMap();
		
		//TODO: Remove this static stuff
		List<String> requestTypes = new ArrayList<String>();
		requestTypes.add("Create Users");
		requestTypes.add("Delete Users");
		requestTypes.add("Enable Users");
		requestTypes.add("Disable Users");

		for (String currReqTypeName : requestTypes) {
			TreeNodeImpl currTreeNode = new TreeNodeImpl();
			currTreeNode.setParent(this);
			currTreeNode.setData(currReqTypeName);
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
}
