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
package velo.entity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.StAXWriter;

@Table(name = "VL_XML_OBJECT")
@Entity
@SequenceGenerator(name="XmlObjectIdSeq",sequenceName="XML_OBJECT_ID_SEQ")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE")
public abstract class XmlObject extends BaseEntity {
	private static Logger log = Logger.getLogger(XmlObject.class.getName());
	
	protected String BINDING_REPOSITORY_FOLDER = "jibx-bindings/";
	
	protected Object object;
	private Class clazz;
	
	private Long xmlObjectId;
	private String xml;
	
	
	public XmlObject() {
		setCreationDate(new Date());
	}
	
	
	/**
	 * @return the xmlObjectId
	 */
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="XmlObjectIdSeq")
    @Column(name="XML_OBJECT_ID")
	public Long getXmlObjectId() {
		return xmlObjectId;
	}
	/**
	 * @param xmlObjectId the xmlObjectId to set
	 */
	public void setXmlObjectId(Long xmlObjectId) {
		this.xmlObjectId = xmlObjectId;
	}
	/**
	 * @return the xml
	 */
	@Column(name="XML")
	@Lob
	public String getXml() {
		return xml;
	}
	/**
	 * @param xml the xml to set
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}

	@Transient
	public abstract String getBindingFilename();
	
	@Transient
	public File getBindingFile() {
		String fileName = BINDING_REPOSITORY_FOLDER + getBindingFilename();
		URL myURL = this.getClass().getClassLoader().getResource(fileName);
		
		if (myURL != null){
			File f = new File(myURL.getFile());
			
			return f;
		} else {
			log.error("JIBX Binding file with URL '" + fileName + "' was not found!");
			return null;
		}
	}
	
	
	public Object unmarshal() {
		try {
			log.trace("Unmarshaling XML object of class '" + getClazz().getName() + "'");
			IBindingFactory bfact = BindingDirectory.getFactory(getClazz());
			log.trace("Factored a binding factory...");
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			log.trace("Created an UnMarshalling Context...");
			
			//Object obj = uctx.unmarshalDocument
			//(new FileInputStream("src/jibx/data.xml"), null);
			
			
			/*
			File bindingFile = getBindingFile();
			if (bindingFile == null) {
				return null;
			}
			*/
			
			//FileInputStream f = new FileInputStream(getBindingFile());
			StringReader reader = new StringReader(getXml());
			Object obj = uctx.unmarshalDocument(reader, null);
			//StringReader sreader = new StringReader(getBindingContent());
			
			
			//Object obj = uctx.unmarshalDocument(sreader);
			log.trace("Successfully unmarshalled object: " + obj);
			
			return obj;
		}catch (JiBXException e) {
			log.error(e.toString());
			return null;
		}/* catch (FileNotFoundException e) {
			log.error(e.toString());
			return null;
		}
		*/
	}
	
	public String marshal() {
	    try {
	    	log.trace("Marshaling object has started.");
	    	log.trace("BindingFactory of class '" + getClazz().getName() + "'");
	    	IBindingFactory bfact = BindingDirectory.getFactory(getClazz());
			IMarshallingContext mctx = bfact.createMarshallingContext();
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    
            XMLOutputFactory ofact = XMLOutputFactory.newInstance();
            XMLStreamWriter wrtr = ofact.createXMLStreamWriter(bos, "UTF-8");
            mctx.setXmlWriter(new StAXWriter(bfact.getNamespaces(), wrtr));
            mctx.marshalDocument(getObject());
            
            log.trace("Marshalled XML: " + bos.toString());
            return bos.toString();
        } catch (XMLStreamException e) {
        	log.error(e.toString());
            //throw new JiBXException("Error creating writer", e);
        	return null;
        } catch (JiBXException e) {
        	log.error(e.toString());
        	return null;
        }
	}
	
	public void update() {
		setXml(marshal());
	}
	
	/**
	 * @return the object
	 */
	@Transient
	public Object getObject() {
		return object;
	}
	
	/**
	 * @param object the object to set
	 */
	public void setObject(Object object) {
		this.object = object;
	}
	
	
	/**
	 * @return the clazz
	 */
	@Transient
	public Class getClazz() {
		return clazz;
	}
	/**
	 * @param clazz the clazz to set
	 */
	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}
	
	
	@Transient
	public Object getData() {
		System.out.println("!!!!!!!!!!!!!!");
		return unmarshal();
	}
	
	
	
}
