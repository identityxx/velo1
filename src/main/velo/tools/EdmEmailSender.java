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
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import velo.common.SysConf;

public class EdmEmailSender extends EmailSender {
    
    private static Logger logger = Logger.getLogger(EdmEmailSender.class.getName());
    
    private final static String smtpServer = SysConf.getSysConf().getString("email-conf.smtp-server-primary.host");
    private final static String fromAddress =  SysConf.getSysConf().getString("email-conf.system-sender-address");
    
    @Override
    public void init() {
        setHostName(smtpServer);
    }
    
    public void addHtmlEmail(String recipient, String subject, String body) throws EmailException {
        addHtmlEmail(fromAddress, recipient, subject, body);
    }
    
    
    public Email factoryEmail(String subject, String body) throws EmailException {
    	HtmlEmail he = new HtmlEmail();
    	he.setSubject(subject);
    	he.setHtmlMsg(body);
    	he.setCharset("UTF-8");
    	he.setHostName(getHostName());
    	he.setFrom(fromAddress);
    	
    	return he;
    }
       
    
    
    /*DEBUG
    private void lala() {
        setHostName("localhost");
    }
    
    public static void main(String[] args) throws Exception {
        EdmEmailSender ees = new EdmEmailSender();
        ees.lala();
        ees.addHtmlEmail("rofl@rofl.com", "asaf@mydomain.com", "asdfsadf", "asdfasdf!!!");
        ees.send();
    }
     */
}
