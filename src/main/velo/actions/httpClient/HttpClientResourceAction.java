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
package velo.actions.httpClient;

import java.util.logging.Logger;

import velo.actions.ResourceAction;
import velo.adapters.GenericTelnetAdapter;
import velo.entity.Resource;
import velo.exceptions.AdapterException;
import velo.query.Query;

/**
 The base call of all actions that are based on a HTTP Client adapter type.
 
 @author Asaf Shakarchi
 */
abstract public class HttpClientResourceAction extends ResourceAction {
    
    /**
     A reference to a query object, used in the actions code.
     */
    Query query = new Query();
    
    private static Logger logger = Logger.getLogger(HttpClientResourceAction.class.getName());
    
    /**
     Set the Resource into the class
     @param resource The Resource entity to set
     */
    public HttpClientResourceAction(Resource resource) {
        super(resource);
    }
    
    /**
     Empty constructor
     Used for easy factory by reflection
     */
    public HttpClientResourceAction() {
        
    }
    
    /**
     Replaces the old query object with a new query object.
     
     @param query the new query object to set inside the action.
     */
    public void setQuery(Query query) {
        this.query = query;
    }
    
    /**
     Get the specific adapter for this adpater actions type
     @return GenericHttpClientAdapter a GenericHttpClientAdapter object
     */
    public GenericTelnetAdapter getAdapter() throws AdapterException {
        return (GenericTelnetAdapter) super.getResourceAdapter();
    }
    
    //public boolean execute() throws ActionFailureException {
    
    public boolean preActionOperation() {
        try {
            getAdapter().connect();
            return true;
        } catch (AdapterException ae) {
            getMsgs().add(ae.getMessage());
            return false;
        }
    }
    
    public boolean readUntil( String pattern ) throws AdapterException {
        try {
            return getAdapter().readUntil(pattern);
        } catch (AdapterException ex) {
            getMsgs().add(ex.toString());
            throw ex;
        }
    }
    
    public void write( String value) throws AdapterException {
        try {
            getAdapter().write(value);
        } catch (AdapterException ex) {
            getMsgs().add(ex.toString());
            throw ex;
        }
    }
}