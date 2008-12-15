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
package velo.resource.general;

public class ScriptedResourceCommand {
	private String send;
	private String waitFor;
	
	
	public ScriptedResourceCommand(String send, String waitFor) {
		setSend(send);
		setWaitFor(waitFor);
	}
	
	/**
	 * @return the send
	 */
	public String getSend() {
		return send;
	}
	/**
	 * @param send the send to set
	 */
	public void setSend(String send) {
		this.send = send;
	}
	/**
	 * @return the waitFor
	 */
	public String getWaitFor() {
		return waitFor;
	}
	/**
	 * @param waitFor the waitFor to set
	 */
	public void setWaitFor(String waitFor) {
		this.waitFor = waitFor;
	}
	
	
	public boolean isWaitForEmpty() {
		if (getWaitFor() == null) {
			return true;
		}
		
		if (getWaitFor().length() < 1) {
			return true;
		}
		
		
		return false;
	}
	
	public boolean isSendEmpty() {
		if (getSend() == null) {
			return true;
		}
		
		if (getSend().length() < 1) {
			return true;
		}
		
		return false;
	}
}
