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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * BlindSSLSocketFactoryTest
 *  Simple test to show an Active Directory (LDAP) without verifying the server's certificate.
 *  Credit to: blog.platinumsolutions.com
 *  
 * @author Mike McKinney, Platinum Solutions, Inc.
 */
public class BlindSSLSocketFactory extends SocketFactory {
	private static SocketFactory blindFactory = null;
	
	/**
	 * Builds an all trusting "blind" ssl socket factory.
	 */
	static {
		// create a trust manager that will purposefully fall down on the
		// job
		TrustManager[] blindTrustMan = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() { return null; }
			public void checkClientTrusted(X509Certificate[] c, String a) { }
			public void checkServerTrusted(X509Certificate[] c, String a) { }
		} };

		// create our "blind" ssl socket factory with our lazy trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, blindTrustMan, new java.security.SecureRandom());
			blindFactory = sc.getSocketFactory();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see javax.net.SocketFactory#getDefault()
	 */
	public static SocketFactory getDefault() {
		return new BlindSSLSocketFactory();
	}


	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
	 */
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return blindFactory.createSocket(arg0, arg1);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
	 *      java.net.InetAddress, int)
	 */
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	/**
	 * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
	 *      java.net.InetAddress, int)
	 */
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		return blindFactory.createSocket(arg0, arg1, arg2, arg3);
	}

	/**
	 * Our test...
	 * @param args
	 */
	/*
	public static void main(String[] args) {
		// do you want to validate the server's SSL cert?
		//   if you have invalid certs, true will produce errors
		boolean validateCert = false; 
		
		// ****************************************************>
		// LDAPS CONNECTION
		// ****************************************************>
		System.out.println("Testing LDAPS connection with validateCert: " + validateCert);
		Hashtable env = new Hashtable();
		
		// complete URL of Active Directory/LDAP server running SSL with invalid cert
		String url = "ldaps://192.168.5.3:636";
		// domain is the Active Directory domain i.e. "yourdomain.com"
		String domain = "MYAD";
		// the sAMAccountName (i.e. jsmith)
		String login = "CN=Administrator,CN=Users,DC=myad,DC=com";
		String password = "password";
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		//env.put(Context.SECURITY_PRINCIPAL, login + "@" + domain);
		env.put(Context.SECURITY_PRINCIPAL, login);
		env.put(Context.SECURITY_CREDENTIALS, password);

		if (url.startsWith("ldaps") && !validateCert) {
			env.put("java.naming.ldap.factory.socket", BlindSSLSocketFactoryTest.class.getName());
		}

		try {
			LdapContext ctx = new InitialLdapContext(env, null);
			System.out.println("Successfull bind to " + url + "!");
			
			
			String userName = "CN=Albert Einstein,CN=Users,DC=myad,DC=com";
			Attributes attrs = new BasicAttributes(true);
			attrs.put("objectClass","user");
			attrs.put("samAccountName","AlbertE");
			attrs.put("cn","Albert Einstein");
			attrs.put("sn","Einstein");
			attrs.put("displayName","Albert Einstein");
			attrs.put("userPrincipalName","aeinstein@orange.co.il");
			attrs.put("mail","albertMail@rofl.com");
			attrs.put("telephoneNumber", "999-9223-1111");
			attrs.put("description", "The DESC of the user:)");
			
			
			int UF_ACCOUNTDISABLE = 0x0002;
			int UF_PASSWD_NOTREQD = 0x0020;
			int UF_PASSWD_CANT_CHANGE = 0x0040;
			int UF_NORMAL_ACCOUNT = 0x0200;
			int UF_DONT_EXPIRE_PASSWD = 0x10000;
			int UF_PASSWORD_EXPIRED = 0x800000;
		
		
			attrs.put("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED+ UF_ACCOUNTDISABLE));
			// Create the context
			Context result = ctx.createSubcontext(userName, attrs);
			
			ModificationItem[] mods = new ModificationItem[2];
			String newQuotedPassword = "\"Password2000\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
			mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl",Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
		
			// Perform the update
			ctx.modifyAttributes(userName, mods);
			System.out.println("Set password & updated userccountControl");
			
		} catch (AuthenticationException e) {
			System.out.println("The credentials could not be validated!");
		} catch (NamingException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}

		// ****************************************************>
		
		// ****************************************************>
		// HTTPS CONNECTION
		// ****************************************************>
		/*
		System.out.println("Testing HTTPS connection with validateCert: " + validateCert);
		// host name of server running SSL with invalid cert
		String host = "<SSL WEB HOST>";
		int port = 443;// modify this if other than default port.
		
		try {
		    SocketFactory sslFactory;
			if (validateCert) {
				sslFactory = SSLSocketFactory.getDefault();
			} else {
				sslFactory = BlindSSLSocketFactoryTest.getDefault();
			}
			
		    SSLSocket s = (SSLSocket) sslFactory.createSocket(host, port);
		    OutputStream out = s.getOutputStream();
		    
		    out.write("GET / HTTP/1.0\n\r\n\r".getBytes());
		    out.flush();
		    System.out.println("Successfull connection to " + host + ":" + port + "!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ****************************************************>
		 */
	//}
}