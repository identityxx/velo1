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
package velo.tools;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

/**
 *
 * @author Asaf Shakarchi
 */
public class ProcessReport {

    private String subject;
    private List<HtmlDataTable> tables = new ArrayList<HtmlDataTable>();
    
    public ProcessReport() {
    }

    
    public static void main(String[] args) {
        
        ProcessReport pr = new ProcessReport();
        
        HtmlDataTable hdt = new HtmlDataTable();
        hdt.setTitle("Rofl");
        HtmlColumn hc = new HtmlColumn();
        hc.setParent(hdt);
        HtmlOutputText hot = new HtmlOutputText();
        hot.setValue("asdfasdf");
        hot.setParent(hc);
        
        System.out.println(FacesContext.getCurrentInstance());
        //hdt.decode(FacesContext.getCurrentInstance())
        System.out.println("Rendered type: " + hdt.getRendererType());
        
        
    }
    
}
