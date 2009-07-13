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

//import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.ScopeType.SESSION;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;

/**
 * System Configuration manager Define all system confs of the system
 *
 * @author Asaf Shakarchi
 *
 */
@Scope(SESSION)
@Name("sysConfManager")
public class SysConf {
	private final static String VELO_SERVER_VERSION = "1.4GAb";
	private final static String veloINIConfFileName = "velo_config.ini";
	private final static String veloXMLConfFileName = "velo_config.xml";
	private final static String VELO_CONF_DIR = "conf";
	private final static String VELO_SYS_DIR = "velo_sys";
	public final static String VELO_KEYS_DIR = "keys";
	public final static String VELO_WORKSPACE_DIR = "velo_ws";
	public static Configuration staticConfig = null;


	//private static ConfigurationFactory factory;

	@In(required=false) @Out(scope = SESSION)
	private Configuration config;

	public static String getVeloXMLConfFileName() {
		return getVeloHomeDir() + "/" + VELO_SYS_DIR + "/" + VELO_CONF_DIR + "/" + veloXMLConfFileName;
	}

	public static String getVeloINIConfFileName() {
		return getVeloHomeDir() + "/" + VELO_SYS_DIR + "/" + VELO_CONF_DIR + "/" + veloINIConfFileName;
	}

	public static String getVeloHomeDir() {
		return System.getProperty("veloHomeDir");
	}

	public static String getVeloSysConfDir() {
		return System.getProperty("veloHomeDir") + "/" + VELO_SYS_DIR;
	}


	public static String getVeloWorkspaceDir() {
		return System.getProperty("veloHomeDir") + "/" + VELO_WORKSPACE_DIR;
	}

	/**
	 * Returns a Configuration object for managing system configuration properties
	 * @return Configuration object
	 */
	public static Configuration getSysConf() {
		if (staticConfig == null) {
			try {
				System.out.println("(!)Factoring Global Configuration Object...");
				//URL iniConf = SysConf.class.getClassLoader().getResource(veloINIConfFileName);
				//URL xmlConf = SysConf.class.getClassLoader().getResource(veloXMLConfFileName);

				//String xmlConfFile = System.getProperty("veloSysDir") + "/conf/" + veloXMLConfFileName;
				//String iniConfFile = System.getProperty("veloSysDir") + "/conf/" + veloINIConfFileName;


				/*
            if ( (iniConf == null) || (xmlConf == null) ) {
                System.err.println("Could not load configuration files, dying...");
                return null;
            }
				 */

				//01-jul-07(Asaf), Removed root conf, it is just another file that is accessible by the user and is not neccessary
				//Only got two conf files, added them by code
				//factory = new ConfigurationFactory(sysconf.getFile());
				//Configuration config = factory.getConfiguration();

				CompositeConfiguration config = new CompositeConfiguration();
				config.setDelimiterParsingDisabled(true);
				config.setListDelimiter("|".charAt(0));
				config.setDelimiter("|".charAt(0));

				//for tests
				//config.addConfiguration(new PropertiesConfiguration("c:/temp/velo/velo_sys/conf/velo_config.ini"));
				//config.addConfiguration(new XMLConfiguration("c:/temp/velo/velo_sys/conf/velo_config.xml"));

				String iniConfFile = getVeloINIConfFileName();
				String xmlConfFile = getVeloXMLConfFileName();


				config.addConfiguration(new PropertiesConfiguration(iniConfFile));
				XMLConfiguration xmlConf = new XMLConfiguration(xmlConfFile);
				xmlConf.setDelimiterParsingDisabled(true);
				xmlConf.setListDelimiter("|".charAt(0));
				xmlConf.setDelimiter("|".charAt(0));
				config.addConfiguration(xmlConf);


				//TODO: Fix this for JBOSS
				//config.addConfiguration(new PropertiesConfiguration(iniConf.getFile()));
				//config.addConfiguration(new XMLConfiguration(xmlConf.getFile()));

				return config;
			} catch (ConfigurationException ce) {
				System.out.println("Could not read ROOT configuration file due to: " + ce.getMessage());
				return null;
			}
		}
		
		
		return staticConfig;

	}

	@Factory("globalConf")
	public Configuration getGlobalConf() {
		if (config == null) {
			this.config = SysConf.getSysConf();
		}

		return config;
	}

	public static String getConfProperty(String propertyName) {
		return SysConf.getSysConf().getString(propertyName);
	}

	/*
    public static String getResourceTypeDescriptorFileName(ResourceType tst) {
    	String fileToSeek = getSysConf().getString("system.directory.system_conf") + "/conf/" + tst.getUniqueName().toLowerCase() + ".xml";
    	//URL u = SysConf.class.getClassLoader().getResource(fileToSeek);

    	//return u.getFile();
    	return fileToSeek;
    }
	 */

	/*tests!
    public static void main(String arg[]) {
        System.out.println("FROM XML: " + getSysConf().getString("system.directory.system_conf"));
        System.out.println("FROM INI: " + getSysConf().getString("scripts.file_extension"));
    }
	 **/


	public String getVeloServerVersion() {
		return VELO_SERVER_VERSION;
	}
}
