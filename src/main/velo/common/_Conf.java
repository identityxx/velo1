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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * An abstract class of managing a configuration file
 * 
 * @author Asaf Shakarchi
 */
public abstract class _Conf {

	private String confFileName;
	
	/**
	 * Get the configuration file name
	 * @return Configuration File name as a string 
	 */
	public String getConfFileName() {
		return confFileName;
	}
	
	/**
	 * Set the configuration file name
	 * @param confFileName The configuration file name as a string 
	 */
	public void setConfFileName(String confFileName) {
		this.confFileName = confFileName;
	}
	
	/**
	 * @param propertyName The name of the property to retrieve
	 * @return The value of the property
	 * @throws FileNotFoundException throwed if the conf file was not found
	 * @throws IOException throwed if there was a problem to load the XML file
	 */
	public String getPropertyByName(String propertyName) throws FileNotFoundException, IOException {
		
	    Properties prop = new Properties();
	    try {
	    FileInputStream fis =
	        new FileInputStream(getConfFileName());
	    prop.loadFromXML(fis);
	    }
	    catch (FileNotFoundException fnfe) {
	    	throw fnfe;
	    }
	    catch (IOException ioe) {
	    	throw ioe;
	    }
	
	    return prop.getProperty(propertyName);
	}
	
}
