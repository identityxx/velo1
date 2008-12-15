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
 * A base class of all Exceptions
 * 
 * @author Asaf Shakarchi
 */
public class EdentityException extends Exception {
	private static final long serialVersionUID = 1987305452306161213L;

	/**
	 * Empty Constructor
	 * 
	 */
	public EdentityException() {
		super("Edm Error Occured");
	}

	/**
	 * Constructor of the exception
	 * @param msg A message that describes the exception
	 */
	public EdentityException(String msg) {
		super(msg);
	}

	/*
	 public String getMessage() {
	 // Get this exception's message.
	 String msg = super.getMessage();
	 return msg;
	 }
	 */
}
