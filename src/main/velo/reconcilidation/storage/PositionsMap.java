package velo.reconcilidation.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import velo.entity.Account;
import velo.entity.Position;
import velo.exceptions.LoadingObjectsException;
import velo.exceptions.ValidationException;

public class PositionsMap extends HashMap<String,Position> {
	private static transient Logger log = Logger.getLogger(PositionsMap.class.getName());
	
	public void load(File f) throws LoadingObjectsException {
		Digester d = createDigester();
		
		//SyncSourceAccountList list = new SyncSourceAccountList();
		d.push(this);
		
		addRules(d);
		
		try {
			d.parse(f);
		}catch (IOException e) {
			throw new LoadingObjectsException(e);
		} catch (SAXException e) {
			throw new LoadingObjectsException(e);
		}
	}
	
	
	public void load(InputStream is) throws LoadingObjectsException {
		Digester d = createDigester();
		
		//SyncSourceAccountList list = new SyncSourceAccountList();
		d.push(this);
		
		addRules(d);
		
		try {
			d.parse(is);
		}catch (IOException e) {
			throw new LoadingObjectsException(e);
		} catch (SAXException e) {
			throw new LoadingObjectsException(e);
		}
	}
	
	public Digester createDigester() {
		Digester d = new Digester();
		return d;
	}
	
	public void addRules(Digester d) {
		d.setValidating(false);
        d.setRules(new ExtendedBaseRules());
        
        d.addObjectCreate("positions/position", Position.class);
        d.addSetProperties("positions/position");
        d.addSetNext("positions/position", "add");
        
        //does not work
        //d.addCallMethod("positions/position/uniqueIdentifier", "setUniqueIdentifier",0);
        //d.addCallMethod("positions/position/org-unit", "setUniqueOrgUnitId",0);
        //d.addCallMethod("positions/position/displayName", "setDisplayName",0);
        //d.addCallMethod("positions/position/active", "setActive",0);

	}
	
	//ADDING KEY AS UPPERCASE!
	public void add(Position position) {
		try {
			position.setCreationDate(new Date());
			position.isCorrectlyImported();
			put(position.getUniqueIdentifier().toUpperCase(),position);
		}catch (ValidationException e) {
			log.warn("Could not import active position: " + e.getMessage());
		}
	}
	
	/*
	public static void main(String[] args) throws Exception {
		PositionsMap a = new PositionsMap();
		File f = new File("c:/temp/velo/velo_ws/data/positions/positions.xml");
		
		a.load(f);
	}
	*/
}
