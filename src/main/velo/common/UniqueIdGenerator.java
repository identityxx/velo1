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

import java.rmi.server.UID;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UniqueIdGenerator {
	
	public static int generalUniqueNumberId() {
		try {
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			//generate a random number
			return prng.nextInt();
		}
		catch ( NoSuchAlgorithmException ex ) {
			return 0;
		}
	}
	
	public static long generateUniqueIdByTimeInMillis() {
		Long current = System.currentTimeMillis();
		return current;
	}
	
	public static String generateUniqueIdByUID() {
		UID userId = new UID();
		return userId.toString();
	}
}
