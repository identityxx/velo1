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
package velo.ejb.interfaces;

import javax.ejb.Local;
import javax.ejb.Timer;

import velo.exceptions.OperationException;

/**
 * An Access Guardian local interface
 * 
 * @author Asaf Shakarchi
 */
@Local
public interface AccessGuardianLocal {
	
    //SCANNER
	public void scanRolesToRevoke(Timer timer);
    public void changeAccessGuardianScannerMode() throws OperationException;
    public boolean isAccessGuardianScannerActivate();
    
    /**
     * Create a timer scanner with the specified interval duration in seconds.
     * @param initialDuration - The initial (start) time of the first execution in SECONDS
     * @param intervalDuration - The interval (after the initial execution ends) for the next exeucitons in SECONDS
     */
    public void createTimerScanner(long initialDuration, long intervalDuration);	
}