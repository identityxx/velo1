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
package velo.ejb.interfaces;

import java.util.Map;

import javax.ejb.Local;

import velo.entity.EmailTemplate;
import velo.exceptions.EmailNotificationException;

/**
 * A local Adapter Bean Interface
 * 
 * @author Asaf Shakarchi
 */
@Local
public interface EmailManagerLocal {
	public EmailTemplate findEmailTemplate(String name);
	public void sendEmailsToApproversGroup(String agUniqueName, String emailTemplateName, Map<String,Object> varsMap) throws EmailNotificationException;
	public void sendEmailToUser(String userName, String emailTemplateName, Map<String,Object> varsMap) throws EmailNotificationException;
}
