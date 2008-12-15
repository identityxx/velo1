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
package velo.query;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Shakarchi Asaf
 */
public class QueryBuilder {
    
    public QueryBuilder() {
        
    }
    
    
    public static org.skife.sql.QueryBuilder select() {
        return org.skife.sql.QueryBuilder.select();
    }
    
    public String constructQueryByFields(org.skife.sql.QueryBuilder qb, Map<String,String> searchParameters) {
        qb.toString();
        return null;
    }
    
    
    public static boolean isFieldSpecified(Object field) {
        if (field instanceof String) {
            String f = (String)field;
            if (f != null) {
                if (f.length() > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (field instanceof Long) {
            Long f = (Long)field;
            if (f != null) {
                return true;
            } else {
                return false;
            }
        } else if (field instanceof Boolean) {
            Boolean f = (Boolean)field;
            return f;
        } else if (field instanceof Date) {
            return true;
        } else {
            if (field == null) {
                return false;
            }
            else {
                return true;
            }
        }
    }
}
