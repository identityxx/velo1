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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;

import velo.exceptions.AdapterException;

public class GenericHttpClientAdapter extends ResourceAdapter {
	private static Logger logger = Logger.getLogger(GenericHttpClientAdapter.class.getName());
	private HttpClient httpClient;

	public boolean connect() throws AdapterException {
		logger.fine("Connecting by '" + GenericHttpClientAdapter.class.getName() + "', to Resource: '" + getResource().getDisplayName() + "'");
		String host = getResourceDescriptor().getString("specific.host");
		Integer port = getResourceDescriptor().getInt("specific.port");
		String protocol = getResourceDescriptor().getString("specific.protocol");

		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Connecting to host: " + host);
			logger.finest("Connecting with host: " + port);
			logger.finest("Connecting with protocol: " + protocol);
		}

		HttpClient client = new HttpClient();
		client.getHostConfiguration().setHost(host, port, protocol);
		//Set by default coockie policy to 'browser_compatibility'
//needed?		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		
		setConnected(true);
		setHttpClient(client);

		return true;
	}

	public boolean disconnect() {
		setConnected(false);
		return true;
	}

	/**
	 * @param httpClient the httpClient to set
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * @return the httpClient
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	
	public int executeMethod(HttpMethod pm) throws AdapterException {
		try {
			return getHttpClient().executeMethod(pm);
		}
		catch (HttpException he) {
			throw new AdapterException(he.getMessage());
		}
		catch (IOException ioe) {
			throw new AdapterException(ioe.getMessage());
		}
	}

	public PostMethod factoryPostMethod(String uri) throws IllegalArgumentException,IllegalStateException {
		PostMethod pm = new PostMethod(uri);
		return pm;
	}
}
