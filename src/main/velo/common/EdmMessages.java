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
package velo.common;

import java.util.ArrayList;
import java.util.List;
import velo.common.EdmMessage.EdmMessageType;

/**
 * A class to hold message and easily display them
 * 
 * @author Shakarchi Asaf
 */
public class EdmMessages {
	
	/**
	 * A list to hold messages
	 */
	List<EdmMessage> messages = new ArrayList<EdmMessage>();
	
	public static EdmMessages instance() {
		EdmMessages em = new EdmMessages();
		return em;
	}
	
	public void add(EdmMessageType type, String summary, String description) {
		EdmMessage em = new EdmMessage(type,summary,description);
		messages.add(em);
	}
	
	public void add(String type, String summary, String description) {
		EdmMessageType msgType = EdmMessageType.INFO; 
		
		if (type.toUpperCase().equals("INFO")) {
			msgType = EdmMessageType.INFO;
		}
		else if (type.toUpperCase().equals("WARNING")) {
			msgType = EdmMessageType.WARNING;
		}
		else if (type.toUpperCase().equals("SEVERE")) {
			msgType = EdmMessageType.SEVERE;
		}
		
		EdmMessage em = new EdmMessage(msgType,summary,description);
		messages.add(em);
	}
	
	public void add(String summary, String description) {
		EdmMessage em = new EdmMessage(EdmMessageType.INFO,summary,description);
		messages.add(em);
	}
	public void add(String msg) {
		EdmMessage em = new EdmMessage(EdmMessageType.INFO,msg,"");
		messages.add(em);
	}
	
	public void info(String summary, String description) {
		add(EdmMessageType.INFO,summary,description);
	}
	
	public void warning(String summary, String description) {
		add(EdmMessageType.WARNING,summary,description);
	}
	
	public void severe(String summary, String description) {
		add(EdmMessageType.SEVERE,summary,description);
	}
	
	public void info(String msg) {
		add(EdmMessageType.INFO,msg,"");
	}
	
	public void warning(String msg) {
		add(EdmMessageType.WARNING,msg,"");
	}
	
	public void severe(String msg) {
		add(EdmMessageType.SEVERE,msg,"");
	}
	
	public void clear() {
		messages.clear();
	}
	
	public String getFirstMessage() {
		if (messages.size() >= 1) {
			return messages.get(0).toString();
		}
		else {
			return null;
		}
	}
	
	public String getLastMessage() {
		if (messages.size() >= 0) {
			return messages.get(messages.size()-1).toString();
		}
		else {
			return null;
		}
	}
	
	public boolean isEmpty() {
		return messages.isEmpty();
	}
	
	public String getMessagesAsString() {
		StringBuilder msgsAsString = new StringBuilder();
		
		int msgId = 0;
		for (EdmMessage currMsg : messages) {
			msgId++;
			msgsAsString.append("("+msgId+") " + currMsg.toString());
		}
		
		return msgsAsString.toString();
	}
	
	public List<EdmMessage> getMessages() {
		return messages;
	}
	
	public String toString() {
		return getMessagesAsString();
	}
}
