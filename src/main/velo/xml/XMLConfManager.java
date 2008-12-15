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
package velo.xml;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Asaf Shakarchi
 */
public class XMLConfManager implements Serializable {

    XMLConfiguration config;


    public XMLConfManager() {
    }

    public void init(String fileName) throws ConfigurationException {
        config = new XMLConfiguration();
        config.setFileName(fileName);
        config.load();
    }
    
    public void initViaContent(String xmlContent) throws ConfigurationException {
    	config = new XMLConfiguration();
    	Reader reader = new StringReader(xmlContent);
    	config.load(reader);
    }


    public XMLConfiguration getConfig() {
        return config;
    }

    public void setConfig(XMLConfiguration config) {
        this.config = config;
    }

    /*
    public static String getFullXmlTagPath(XMLConfiguration.Node node) {
        String orgNodeName = node.getName();
        String tagPath = new String();
        
        int i=0;
        while (node.getParent().getName() != null) {
            XMLConfiguration.Node currNode = node.getParent();
            
            if (i>0) {
                tagPath=currNode.getName() + "." + tagPath;
                node = currNode;
            }
            else {
                tagPath+=node.getName();
            }
            
            i++;
        }

        System.out.println("FULL TAG NAME OF NODE '" +  orgNodeName + "' is: " + tagPath + "'");
        return tagPath;
    }
     * */
    
    /*
    public static void main(String[] args) throws Exception {
        XMLConfManager xc = new XMLConfManager();
        xc.init("c:/test.xml");

        
        
        HierarchicalConfiguration hc = xc.config.configurationAt("system.directory");
        hc.setExpressionEngine(new XPathExpressionEngine());

        Iterator a = hc.getRootNode().getChildren().listIterator();
        while (a.hasNext()) {
            XMLConfiguration.Node conf = (XMLConfiguration.Node)a.next();

            System.out.println("Node name: '" + conf.getName() + "'");
            //System.out.println("Node full path: '" + getFullXmlTagPath(conf));
            //XMLConfiguration.Node desc = (XMLConfiguration.Node)conf.getAttributes("description");
            //does not work: System.out.println(conf.getAttributes().contains("description"));
            List attrList = conf.getAttributes();
            Iterator attrListIt = attrList.iterator();
            while (attrListIt.hasNext()) {
               XMLConfiguration.Node attr = (XMLConfiguration.Node)attrListIt.next();
            }
        }
        
        System.exit(1);
        List f = hc.getList("*");
        System.out.println(f.size());
        Iterator iii = f.iterator();
        while (iii.hasNext()) {
            System.out.println(iii.next());
        }
        System.exit(1);
        //not good, returns a key not only per child but per attributes of each child
        //Iterator keys = hc.getKeys()
        List childs = hc.getRoot().getChildren();

//        /keys iterator
//        while (it.hasNext()) {
//        String key = (String)it.next();
//        String description = key + "[@description]";
//        System.out.println("Key: " + key + ", value: " + hc.getString(key) + ", description = '" + hc.getString(description) + "'");
//        }
         

//        Iterator childsIt = childs.iterator();
//        while (childsIt.hasNext()) {
//            XMLConfiguration.Node conf = (XMLConfiguration.Node)childsIt.next();
//            System.out.println("Curr child name: " + conf.getName() + ", description: " + conf.g);
        //}
    }
     **/
}
