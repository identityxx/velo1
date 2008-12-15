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
package velo.events;

import java.util.Collection;

import velo.actions.Action;
import velo.exceptions.InitException;

/**
 * A class that represents an instance of an EventResponseDefinition
 *
 * @author Asaf Shakarchi
 */
public abstract class EventResponse extends Action {
    private boolean persistenceModified = false;
    
    private boolean persistence;
    
    public EventResponse() {
        
    }

    public void initEventResponse() throws InitException {
        
    }

    //TODO: Implement this as default in Action, it is very annoying, not all actions have results.
    public Collection getActionResult() {
        return null;
    }
    
    public boolean isPersistence() {
        return persistence;
    }
    
    public void setPersistence(boolean persistence) {
        setPersistenceModified(true);
        this.persistence = persistence;
    }

    public boolean isPersistenceModified() {
        return persistenceModified;
    }

    public void setPersistenceModified(boolean persistenceModified) {
        this.persistenceModified = persistenceModified;
    }
}
