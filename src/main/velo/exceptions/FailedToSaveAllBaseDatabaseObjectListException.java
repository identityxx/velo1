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
package velo.exceptions;

/**
 * @deprecated
 * 
 * @author Asaf Shakarchi
 */
public class FailedToSaveAllBaseDatabaseObjectListException extends EdmException{
	
	private static final long serialVersionUID = 1987305452306161213L;
	
	/**
	 * Constructor of the exception
	 */
	public FailedToSaveAllBaseDatabaseObjectListException() {
		super("Failed to save all the baseDatabaseObjectList to the database!");
	}
}