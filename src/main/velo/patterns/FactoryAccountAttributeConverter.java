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

import velo.common.SysConf;
import velo.converters.AccountAttributeConverterInterface;
import velo.entity.ResourceAttributeBase;
import velo.entity.ResourceAttribute;
import velo.entity.User;
import velo.entity.UserIdentityAttribute;
import velo.exceptions.ScriptLoadingException;
import velo.scripting.ScriptFactory;

/**
 * @author Asaf Shakarchi A class for factoring a converter scripted class
 */
public class FactoryAccountAttributeConverter extends ScriptFactory {
    
    public static AccountAttributeConverterInterface factoryConverter(ResourceAttribute tsa) throws ScriptLoadingException {
        ScriptFactory sf = new ScriptFactory();
        String scriptResourceName = buildScriptResource(tsa);

        Object scriptObj = sf.factoryScriptableObjectByResourceName(scriptResourceName);
        
        AccountAttributeConverterInterface scriptedConverter = (AccountAttributeConverterInterface) scriptObj;
        
        scriptedConverter.setResource(tsa.getResource());
        scriptedConverter.setResourceAttribute(tsa);

        return scriptedConverter;
    }
    
    
    /**
     * @param tsa The resourceAttribute entity to factory the converter for
     * @param user The user entity to factory the converter to
     * @param sourceUserAttribute The source user attribute to factory the converter for
     * @return A factored AccountAttributeConverter object casted to its interface
     * @throws ScriptLoadingException
     */
    public static AccountAttributeConverterInterface factoryConverter(
            ResourceAttributeBase ra, User user,
            UserIdentityAttribute sourceUserAttribute)
            throws ScriptLoadingException {
        
        
//JB       AccountAttributeConverterInterface scriptObj = factoryConverter(ra);
        // Set user/Attribute entities into the factored scripted converter
//JB        scriptObj.setUser(user);
        
        //Of course only if there is a mapping to an IA set the source UserIdentityAttribute
        //System.out.println("*********************: " + tsa.getIdentityAttribute());
        if (ra.getIdentityAttribute() != null) {
            // Important to set a CLONE of the sourceAttribute (see description
            // of 'clone()' function for more information in Attribute class)
//JB            scriptObj.setSourceUserAttribute(sourceUserAttribute.clone());
        }
        
        
//JB        return scriptObj; 
        //jb!
        return null;
        
        
        //OLD
        // resourceActionInterface actionScript =
        // (resourceActionInterface) obj;
        
        // Try to set the recieved resource into the scripted action
        // actionScript.setResource(resource);
        
        // return (Action)actionScript;
    }
    
    
    /**
     * Build a script resource structure based on a specific resourceAttribute
     * @param tsa
     * @return A string with the full scriptResource structure
     * @deprecated
     */
    //TODO: Move this method to somewhere generic and common method for all
    // factories
    public static String buildScriptResource(ResourceAttribute tsa) {
        String scriptResourceName = SysConf.getSysConf().getString(
                "system.directory.user_workspace_dir")
                + "/"
                + "targets_scripts"
                + "/"
                + tsa.getResource().getUniqueName().toLowerCase()
                + "/"
                + "converters" + "/" + tsa.getConverterClassName();
        return scriptResourceName;
    }
    
    
    
    
    public static void destroyConverter(AccountAttributeConverterInterface aaci) {
        aaci.setResource(null);
        aaci.setUser(null);
        aaci.setResourceAttribute(null);
        aaci.setSourceUserAttribute(null);
        aaci = null;
    }
    
    public static AccountAttributeConverterInterface initConverterReferences(AccountAttributeConverterInterface aaci, ResourceAttribute tsa, User user,UserIdentityAttribute sourceUserAttribute) {
        aaci.setResource(tsa.getResource());
        aaci.setUser(user);
        aaci.setResourceAttribute(tsa);
        if (tsa.getIdentityAttribute() != null) {
            // Important to set a CLONE of the sourceAttribute (see description
            // of 'clone()' function for more information in Attribute class)
            aaci.setSourceUserAttribute(sourceUserAttribute.clone());
        }
        
        return aaci;
    }
}
