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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;

import velo.exceptions.AdapterException;

public class GenericTelnetAdapter extends ResourceAdapter {
    private static Logger logger = Logger.getLogger(GenericTelnetAdapter.class.getName());
    private TelnetClient telnetClient;
    private InputStream in;
    private PrintStream out;
    
    //Params
    private String host;
    private Integer port;
    private String terminalType;
    //Set timeout as default to 30 seconds.
    private Integer timeout = 30000;
    
    public GenericTelnetAdapter() {
        
    }
    
    //Used mostly for testing
    public GenericTelnetAdapter(String host, Integer port, String terminalType) {
        this.host = host;
        this.port = port;
        this.terminalType = terminalType;
    }
    
    public boolean connect() throws AdapterException {
        try     {
            logger.debug("Connecting by \'" +
                    velo.adapters.GenericTelnetAdapter.class.getName() +
                    "\', to resource: \'" +
                    getResource().getDisplayName() + "\'");
            
            //Load confiugration parameters if neccessary
            if (!isInitiatedStandalone()) {
                host = getResourceDescriptor().getString("specific.host");
                port = getResourceDescriptor().getInt("specific.port");
                terminalType = getResourceDescriptor().getString("specific.terminal_type");
                timeout = getResourceDescriptor().getInt("specific.timeout");
            }
            
            if (logger.isTraceEnabled()) {
                logger.trace("Connecting to host: " + host);
                logger.trace("Connecting to port: " + port);
                logger.trace("Connecting with terminal type: " + terminalType);
                logger.trace("Setting timeout (in MS): " + timeout);
            }
            
            telnetClient = new org.apache.commons.net.telnet.TelnetClient(terminalType);
            telnetClient.connect(host, port);
            
            //Set timeout
            telnetClient.setSoTimeout(timeout);
            
            in = telnetClient.getInputStream();
            out = new java.io.PrintStream(telnetClient.getOutputStream());
            setConnected(true);
            setTelnetClient(telnetClient);
            return true;
        } catch (SocketException ex) {
        	logger.warn(ex.toString());
            throw new AdapterException(ex);
        } catch (IOException ex) {
        	logger.warn(ex.toString());
            throw new AdapterException(ex);
        }
    }
    
    public boolean disconnect() {
        setConnected(false);
        try {
            getTelnetClient().disconnect();
        } catch (IOException ex) {
            logger.error("Could not disconnect adapter: " + ex);
            return false;
        }
        
        return true;
    }
    
    /**
     * @param telnetClient the telnetClient to set
     */
    public void setTelnetClient(TelnetClient telnetClient) {
        this.telnetClient = telnetClient;
    }
    
    /**
     * @return the telnetClient
     */
    public TelnetClient getTelnetClient() {
        return telnetClient;
    }
    
    
    /**
     * Read buffer until the pattern was matched or a timeout has occured
     * @param pattern
     * @return
     */
    public boolean readUntil( String pattern ) throws AdapterException {
        try {
            byte[] buff = new byte[1024];
            int ret_read = 0;
            
            do
            {
                ret_read = in.read(buff);
                if(ret_read > 0) {
                    String strBuff = new String(buff, 0, ret_read);
                    logger.trace(strBuff);
                    if (strBuff.contains(pattern)) {
                        return true;
                    }
                }
            }
            while (ret_read >= 0);
        } catch (IOException e) {
            logger.error("Error while reading socket:" + e);
            throw new AdapterException("Error while reading socket:" + e);
        }
        
        return false;
    }
    
    
    public void write( String value ) throws AdapterException {
        try {
            out.println( value );
            out.flush();
            logger.trace(value);
        } catch( Exception e ) {
            throw new AdapterException("Could not write value '" + value + "' through adapter: " + e);
        }
    }
}
