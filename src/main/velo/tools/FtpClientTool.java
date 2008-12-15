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
import java.io.IOException;
//import sun.net.ftp.FtpClient;

public class FtpClientTool {
	//private FtpClient ftpClient;
	
	/**
	 * Connect and login to a remote host
	 * @param remoteHost The remote host to connect to 
	 * @param post The port of the ftp server to connect to
	 * @return true/false upon success/failure of the connection process
	 */
	public boolean connect(String remoteHost,int port) throws IOException{
		//setFtpClient(new FtpClient(remoteHost, port));
		
		return true;
	}
	
	public boolean login(String userName,String password) throws IOException {
		//getFtpClient().login(userName,password);
		return true;
	}
	
        /*
	public FtpClient connectAndLogin(String remoteHost, int port, String userName, String password) throws IOException {
		connect(remoteHost,port);
		login(userName,password);
		
		return getFtpClient();
	}
        */
        
	/**
	 * @param ftpClient The ftpClient to set.
	 */
        /*
	public void setFtpClient(FtpClient ftpClient) {
		this.ftpClient = ftpClient;
	}
         **/

	/**
	 * @return Returns the ftpClient.
	 */
        /*
	public FtpClient getFtpClient() {
		return ftpClient;
	}
         **/
	
}
