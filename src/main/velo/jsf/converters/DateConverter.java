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
package velo.jsf.converters;

import javax.faces.convert.DateTimeConverter;

import org.jboss.seam.annotations.Name;

//a workaround for the 1 day subtract with GMT+1 (and as it seems +2 too)
//from seam 2.1.x must name this as component and in facelets do converter="#{dateTimeConverter}" direct access to class does not work anymore
@Name("dateTimeConverter")
public class DateConverter extends DateTimeConverter {
    public static final String pattern = "dd/MM/yyyy";
	    
    public DateConverter(){
        this.setPattern(pattern);
    }
}
