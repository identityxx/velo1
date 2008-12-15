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
package velo.patterns;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.configuration.Configuration;

import velo.common.SysConf;

/**
 * Factory a logger for velo components
 * Usually create a logger that logs to files
 * @author Asaf Shakarchi
 *
 */
public class FactoryLogger {
	
	/**
	 * A system configuration object 
	 */
	public static Configuration sysConf = SysConf.getSysConf();
	private static String logDir = sysConf.getString("system.directory.log_dir");
	
	/**
	 * Factory a logger by a log file name
	 * @param logName The log file name to factory a logger object for
	 * @return A logger object for the specified log file name
	 */
	public static Logger loggerFactory(String logName) {
		Logger logger = Logger.getLogger(logName);
		//Create a new handler that uses the simple formatter
		try {
			FileHandler fh = new FileHandler(getLogDir() + logName + ".txt");
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
		} catch (IOException e) {
		}
		
		return logger;
	}


	/**
	 * Set the log dir to the class
	 * @param logDir The logDir to set.
	 */
	public static void setLogDir(String logDir) {
		//logDir = logDir;
	}


	/**
	 * Get the log dir from the class
	 * @return Returns the logDir.
	 */
	public static String getLogDir() {
		return logDir;
	}
}
