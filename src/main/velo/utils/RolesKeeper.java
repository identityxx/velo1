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
package velo.utils;

import velo.entity.Role;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RolesKeeper {

    private static final Role[] EMPTY = {};

    private Role[] roles = EMPTY;
    
    public Role[] getRoles() {
        return roles;
    }
    
    public void addRole(Role role) {
        List roleList = new ArrayList(Arrays.asList(roles));
        roleList.add(role);
        roles = (Role[]) roleList.toArray(EMPTY);
    }
}
