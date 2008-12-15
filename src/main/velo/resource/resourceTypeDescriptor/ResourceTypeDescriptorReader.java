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
package velo.resource.resourceTypeDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.xml.sax.SAXException;

/**
 * @author Asaf Shakarchi
 * 
 * Parse an XML Target System Descriptor and initialize a corresponding object
 * to handle its attributes easily Abstract class, intialize must values for all
 * target system types, Inherited by more specific target system types
 * descriptors
 * 
 */
public class ResourceTypeDescriptorReader {

	private static Logger logger = Logger.getLogger(ResourceTypeDescriptorReader.class.getName());

	private Digester digester = new Digester();

	private ResourceTypeDescriptor resourceTypeDescriptor = new ResourceTypeDescriptor();

	/**
	 * Constructor
	 */
	public ResourceTypeDescriptorReader() {
		basicInitialization();
		initialization();
	}

	/**
	 * Basic initialization of the reader
	 * Mainly declare all Digester rules and execute them (parse the conf files) 
	 */
	public void basicInitialization() {
		try {
			// Prime the digester stack with an object for rules to operate on
			// getDigester().push(getResourceDescriptor());
			getDigester().push(getResourceTypeDescriptor());

			logger.fine("Initializing basic XML mapping properties...");
			getDigester().setValidating(false);

			getDigester().setRules(new ExtendedBaseRules());

			// We pushed already a TSDescriptor object, this will override the
			// object Digester works on!
			// d.addObjectCreate( "target-system-descriptor",
			// resourceDescriptor.class );

			getDigester().addObjectCreate("target-system-type-descriptor/actions-classes",
					java.util.HashMap.class);
			getDigester().addRule("target-system-type-descriptor/actions-classes/?",
					new ActionsRule(digester));
			getDigester().addSetNext("target-system-type-descriptor/actions-classes",
					"setActionsClasses");

			/**
			 * By default, Digester will look for classes using the same
			 * Classloader that loaded the Digester class. This means that if
			 * the commons-digester.jar file is in some container-level dir
			 * (rather than in WEB-INF/lib or similar) then it can't see that
			 * class. Option 1: put commons-digester.jar in the WEB-INF/lib (or
			 * equivalent) of your project. This is the cleanest solution, but
			 * it does require that "child-first" classloading is selected.
			 * Option 2: Tell digester to use the context classloader: Digester
			 * d = new Digester(); d.setUseContextClassLoader(true); Option 3:
			 * Set the classloader explicitly: Digester d = new Digester();
			 * d.setClassLoader(Thread.currentThread.getContextClassLoader()) //
			 * this is equivalent to the above, assuming that the current //
			 * class is from the webapp/ejb //
			 * d.setClassLoader(this.getClass().getClassLoader());
			 */
			// getDigester().setClassLoader(this.getClass().getClassLoader());
			getDigester().setUseContextClassLoader(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * An initialization method
	 */
	public void initialization() {

	}

	/**
	 * Parse the xml file
	 * @param fileName The file name to parse
	 */
	public void parseAndSetXmlFile(String fileName) throws IOException,SAXException {
		try {
			File inputFile = new File(fileName);
			// resourceDescriptor = (resourceDescriptor)digester.parse(
			// inputFile );
			getDigester().parse(inputFile);
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (SAXException saxe) {
			throw saxe;
		}
	}

	/**
	 * The digester object to set.
	 * @param digester
	 */
	protected void setDigester(Digester digester) {
		this.digester = digester;
	}

	/**
	 * @return Returns the digester object.
	 */
	protected Digester getDigester() {
		return digester;
	}

	/**
	 * The resourceTypeDescriptor to set.
	 * 
	 * @param resourceTypeDescriptor
	 */
	public void setResourceTypeDescriptor(
			ResourceTypeDescriptor resourceTypeDescriptor) {
		this.resourceTypeDescriptor = resourceTypeDescriptor;
	}

	/**
	 * The resourceTypeDescriptor to get.
	 * 
	 * @return Returns the resourceTypeDescriptor.
	 */
	public ResourceTypeDescriptor getResourceTypeDescriptor() {
		return resourceTypeDescriptor;
	}
}
