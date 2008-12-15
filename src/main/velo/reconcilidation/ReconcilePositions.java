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
package velo.reconcilidation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import velo.ejb.interfaces.PositionManagerLocal;
import velo.entity.Position;
import velo.exceptions.LoadingObjectsException;
import velo.exceptions.OperationException;
import velo.reconcilidation.storage.PositionsMap;

public class ReconcilePositions {
	private static Logger log = Logger.getLogger(ReconcileAccounts.class.getName());
	
	PositionsMap activePositions = new PositionsMap();
	
	public void perform(File file) throws OperationException {
		log.info("Performing Positions Reconciliation process");
		
		log.debug("Determining whether XML file is writable or not");
		if (!file.isFile()) {
			throw new OperationException("Could not sync positions as XML file was not found with name '" + file.getAbsolutePath() + "'");
		}
		
		try {
			log.debug("Loading active positions from XML, please wait...");
			activePositions.load(file);
			log.debug("Loaded active positions with amount '" + activePositions.size());
		}catch (LoadingObjectsException e) {
			throw new OperationException("Could not sync positions(data could not be loaded from XML file): " + e.getMessage());
		}
		
		

		try {
			log.debug("Loading all positions from repository, please wait...");
			InitialContext ic = new InitialContext();
			PositionManagerLocal posManager = (PositionManagerLocal) ic.lookup("velo/PositionBean/local");
			List<Position> positionsInRep = posManager.loadAllPositions();
			log.debug("Loaded all positions from repository with amount '" + positionsInRep.size() + "'");
		
		List<Position> positionsToAdd = new ArrayList<Position>();
		List<Position> positionsToDelete = new ArrayList<Position>();
		
		log.debug("iterate over the positions in repository, check whether the position exist in the active positions");
		for (Position currPosInRep : positionsInRep) {
			log.trace("Checking whether position name '" + currPosInRep.getUniqueIdentifier() + "' exists in active list or not...");
			
			String positionKey = currPosInRep.getUniqueIdentifier().toUpperCase();
			if (activePositions.containsKey(positionKey)) {
				log.trace("Position '" + positionKey + "' found in active list, there's nothing to do...");
				
				activePositions.remove(positionKey);
			} else {
				log.trace("current iterated Position from repository was not found in active list, will delete the position from repository at the end of the process.");
				positionsToDelete.add(currPosInRep);
			}
		}
		
		
		log.debug("Left active positions(that does not exist in repository) with amount: '" + activePositions.size() + "'");
		positionsToAdd.addAll(activePositions.values());
		
		log.info("Positions Sync was finished with positions amount to add ==='" + positionsToAdd.size() + "'===, positions to delete ==='" + positionsToDelete.size() + "===");
		
		posManager.persistPositions(positionsToAdd);
		posManager.removePositions(positionsToDelete);
		
		} catch (NamingException e) {
			throw new OperationException("Could not sync positions(could not load local position manager): " + e.getMessage());
		}
	}
	
}
