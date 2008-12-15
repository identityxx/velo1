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
package velo.adapters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.sadun.util.OperationTimedoutException;
import org.sadun.util.TelnetInputStreamConsumer;
import org.sadun.util.UnixLoginHandler;
import org.sadun.util.UnixLoginHandler.LoginIncorrectException;

import velo.entity.ResourceAdmin;
import velo.exceptions.AdapterException;
import velo.exceptions.DecryptionException;

public class GenericTelnetAdapterNew extends ResourceAdapter {
	private static Logger log = Logger.getLogger(GenericTelnetAdapterNew.class.getName());
	
	private PrintWriter writer;
	private UnixLoginHandler endPoint;
	private TelnetInputStreamConsumer consumer;
	
	//Params
    private String host;
    private Integer port;
    private String terminalType;
    //Set timeout as default to 30 seconds.
    private Integer timeout = 30000;
    private String loginPrompt;
    private String incorrectLoginPrompt;
    //private boolean sendInitialCRLF;
    
    
	public boolean connect() throws AdapterException {
		log.debug("Connecting by \'" +
                velo.adapters.GenericTelnetAdapterNew.class.getName() +
                "\', to resource: \'" +
                getResource().getDisplayName() + "\'");
        
        //Load confiugration parameters if neccessary
        if (!isInitiatedStandalone()) {
            host = getResourceDescriptor().getString("specific.host");
            port = getResourceDescriptor().getInt("specific.port");
            terminalType = getResourceDescriptor().getString("specific.terminal_type");
            timeout = getResourceDescriptor().getInt("specific.timeout");
            loginPrompt = getResourceDescriptor().getString("specific.loginPrompt");
            incorrectLoginPrompt = getResourceDescriptor().getString("specific.incorrectLoginPrompt");
            //sendInitialCRLF = getResourceDescriptor().getBoolean("specific.loginPrompt");
        }
        
        if (log.isTraceEnabled()) {
        	log.trace("Connecting to host: " + host);
        	log.trace("Connecting to port: " + port);
        	log.trace("Connecting with terminal type: " + terminalType);
        	log.trace("Setting timeout (in MS): " + timeout);
        	log.trace("Setting login prompt: " + loginPrompt);
        	log.trace("Setting Incorrect login prompt: " + incorrectLoginPrompt);
        	//log.trace("Send Initial CRLF?: " + sendInitialCRLF);
        }
        
        try {
        	// Create a socket to the host
        	Socket socket = new Socket(host, port);
        	// Create a UnixLoginHandler object over the socket to login to the host
        	endPoint = new UnixLoginHandler(socket);
        	endPoint.setTimeout(timeout);
        	endPoint.setLoginPromptSequence(loginPrompt);
        	endPoint.setLoginIncorrectSequence(incorrectLoginPrompt);
        	//endPoint.setSendInitialCRLF(sendInitialCRLF);
        	//set login credentials
        	ResourceAdmin ra = getResource().getFirstResourceAdmin();
        	if (ra == null) {
        		throw new AdapterException("Could not find any admin for resource '" + getResource().getDisplayName() + "'");
        	}
        	
        	log.trace("Connecting with admin: '" + ra.getUserName() + "'");
        	// Create a TelnetInputStreamConsumer object over the socket by logging in to the host
        	consumer = endPoint.doLogin(ra.getUserName(),ra.getDecryptedPassword());
        	
        	//this.endPoint.setLoginIncorrectSequence(seq);
        	//this.endPoint.setSendInitialCRLF(sendInitialCRLF);
        
        	writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
        	throw new AdapterException("An IO error occured during login sequence: " + e.getMessage());
        } catch (OperationTimedoutException e) {
        	throw new AdapterException("A timeout occured during login sequence");
        } catch (LoginIncorrectException e) {
        	throw new AdapterException("Incorrect login.");
        } catch (DecryptionException e) {
        	throw new AdapterException("Decryption password exception: " + e.getMessage());
        }
        
        
        return true;
        
        
	}
	
	public boolean disconnect() {
		endPoint.disconnect();
		return true;
	}
	
	public TelnetInputStreamConsumer getConsumer() {
		return this.consumer;
	}
	
	public PrintWriter getWriter() {
		return this.writer;
	}
}
