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
package velo.resource.resourceDescriptor;

import java.util.Map;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Digester;

/**
 * Attributes for each specific(unique attributes) typed system
 * @author asaf
 *
 */
public class SpecificAttributesRule extends Rule {
	Digester _digester;

    /**
     * Constructor
     * @param digester The Digester object to set
     */
    public SpecificAttributesRule(Digester digester) {
        super();
        _digester = digester;
    }

   public void body(String namespace, String name, String text)
    throws Exception {
	   //Expecting the current digester object in stack to be the created HashMap (See rules)
	   Map currStackMap = (Map)digester.peek();
	   //Add the current attribute to the map
	   currStackMap.put(name,text);
    }

}