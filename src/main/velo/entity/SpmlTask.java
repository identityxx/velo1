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
import java.io.Serializable;
import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jboss.seam.annotations.Name;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.XMLMarshaller;
import org.openspml.v2.msg.XMLUnmarshaller;
import org.openspml.v2.util.xml.ReflectiveDOMXMLUnmarshaller;
import org.openspml.v2.util.xml.UnknownSpml2TypeException;

/**
 * An entity that represents a target system attribute.
 *
 * @author Asaf Shakarchi
 */

//Seam annotations
@Name("spmlTask")
@Entity
@DiscriminatorValue("SPML")
public class SpmlTask extends ResourceTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public SpmlTask() {
    	
    }

    public static SpmlTask factory(String resourceUniqueName, String description) {
    	SpmlTask spmlTask = new SpmlTask();
    	spmlTask.setCreationDate(new Date());
    	spmlTask.setExpectedExecutionDate(new Date());
    	spmlTask.setDescription(description);
    	spmlTask.setResourceUniqueName(resourceUniqueName);
    	spmlTask.setStatus(TaskStatus.PENDING);
    	
    	return spmlTask;
    }
    
    
    

	
	
	
	
	
	
	
    //Transients
    protected XMLUnmarshaller unmarshaller = null;
    protected XMLMarshaller marshaller = null;
    
    
    @Transient
    public XMLUnmarshaller getUnmarshaller() {
    	if (unmarshaller == null) {
    		unmarshaller = new ReflectiveDOMXMLUnmarshaller();
    	}
    	
    	return unmarshaller;
    }
    
    @Transient
    public org.openspml.v2.msg.spml.Request getSpmlRequest() throws UnknownSpml2TypeException{
    	//Expecting the body of the task to be an SPML content
    	Marshallable req = getUnmarshaller().unmarshall(getBody());
    	
    	if (!(req instanceof org.openspml.v2.msg.spml.Request)) {
			throw new UnknownSpml2TypeException("Spml marshallable object is not of a Request type!");
		}
    	
    	org.openspml.v2.msg.spml.Request request = (org.openspml.v2.msg.spml.Request)req;
    	return request;
    }

	
}