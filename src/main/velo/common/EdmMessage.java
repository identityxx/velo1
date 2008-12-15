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

/**
 * A class that represents an Edm Message
 * @author Shakarchi Asaf
 */
public class EdmMessage {
	public enum EdmMessageType { INFO, WARNING, SEVERE, CRITICAL }
	
	private String description;
	private String summary;
	private EdmMessageType type;
	
	
	public EdmMessage(EdmMessageType type,String summary,String description) {
		setType(type);
		setSummary(summary);
		setDescription(description);
	}
	
	/**
	 * @param type The type to set.
	 */
	public void setType(EdmMessageType type) {
		this.type = type;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}


	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}


	/**
	 * @return Returns the type.
	 */
	public EdmMessageType getType() {
		return type;
	}
	
	public String toString() {
		return "["+getType().toString()+"][" + getSummary() + "] [" + getDescription() + "]";
	}
}
