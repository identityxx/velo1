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
package velo.wf;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A TreeMappedList of comments within a workflow process
 * 
 * 
 * @author Asaf Shakarchi
 */
public class WfComments extends ArrayList<WfComment> implements Serializable {
	private static final long serialVersionUID = 1987305452306161213L;
	
	public String displayAllAsClearText() {
		StringBuffer txt = new StringBuffer();
		
		for (WfComment currWC : this) {
			txt.append(currWC.getDate() + " - [" + currWC.getFullName() + "] - " + currWC.getComment());
		}
		
		return txt.toString();
	}
}
