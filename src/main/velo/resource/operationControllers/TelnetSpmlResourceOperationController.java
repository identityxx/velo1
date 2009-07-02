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
package velo.resource.operationControllers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.DeleteRequest;
import org.openspml.v2.msg.spml.ModifyRequest;
import org.openspml.v2.msg.spmlsuspend.ResumeRequest;
import org.openspml.v2.msg.spmlsuspend.SuspendRequest;
import org.sadun.util.OperationTimedoutException;

import velo.action.ResourceOperation;
import velo.adapters.GenericTelnetAdapterNew;
import velo.collections.Accounts;
import velo.collections.ResourceGroups;
import velo.contexts.OperationContext;
import velo.entity.Resource;
import velo.entity.ResourceTask;
import velo.entity.SpmlTask;
import velo.exceptions.AdapterException;
import velo.exceptions.OperationException;
import velo.resource.general.ScriptedResourceCommand;
import velo.resource.general.ScriptedResourceCommands;

public class TelnetSpmlResourceOperationController extends SpmlResourceOperationController {
	private static Logger log = Logger.getLogger(TelnetSpmlResourceOperationController.class.getName());
	GenericTelnetAdapterNew adapter;
	
	public Accounts listAllIdentities(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		return null;
	}
	
	public TelnetSpmlResourceOperationController() {

	}

	public TelnetSpmlResourceOperationController(Resource resource) {
		super(resource);
	}
	
	public void init(OperationContext context) {
		ScriptedResourceCommands<ScriptedResourceCommand> cmds = new ScriptedResourceCommands<ScriptedResourceCommand>();
		context.addVar("cmds", cmds);
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,SuspendRequest request) throws OperationException {
		performOperation(ro);
		
		log.debug("Sucessfully ended invocation of SuspendRequest operation...");
	}
			
		
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,ResumeRequest request) throws OperationException {
		performOperation(ro);
		
		log.debug("Sucessfully ended invocation of ResumeRequest operation...");
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro,DeleteRequest request) throws OperationException {
		performOperation(ro);

		log.debug("Sucessfully ended invocation of DeleteRequest operation...");
	}

	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, AddRequest request) throws OperationException {
		performOperation(ro);
		
		log.debug("Sucessfully ended invocation of AddRequest operation...");
	}

	
	public void performOperation(SpmlTask spmlTask, ResourceOperation ro, ModifyRequest request) throws OperationException {
		performOperation(ro);
		
		log.debug("Sucessfully ended invocation of ModifyRequest operation...");
	}
	
	
	@Override
	//TODO: Implement!
	public Accounts listIdentitiesIncrementally(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		return null;
	}

	@Override
	//TODO: Implement!
	public Accounts listIdentitiesFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	@Override
	//TODO: Implement!
	public ResourceGroups listGroupsFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	@Override
	//TODO: Implement!
	public ResourceGroups listGroupMembershipFull(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		throw new OperationException("Not supported yet");
	}
	
	
	
	
	private void performOperation(ResourceOperation ro) throws OperationException {
		try {
			ScriptedResourceCommands<ScriptedResourceCommand> cmds = (ScriptedResourceCommands<ScriptedResourceCommand>)ro.getContext().get("cmds");
		
			//Connect to the system, also performing login
			getAdapter().connect();
			
			try {
				performCommands(cmds);
			} catch (AdapterException e) {
				throw new OperationException(e.toString());
			}
		
			getAdapter().disconnect();
		} catch (AdapterException e) {
			throw new OperationException(e.toString());
		}
	}
	
	
	
	protected void performCommands(ScriptedResourceCommands<ScriptedResourceCommand> cmds) throws AdapterException {
		log.trace("Dump of telnet commands: " + cmds);
		
		for (ScriptedResourceCommand currCmd : cmds) {
			if (!currCmd.isSendEmpty()) {
				log.trace("Sending command: '" + currCmd.getSend() + "'");
				try {
					String cmd = currCmd.getSend() + "\r";
					getAdapter().getWriter().println(cmd);
				} catch (AdapterException ae) {
					throw new AdapterException("An exception has occured while sending command: '" + currCmd.getSend() + "': " + ae.toString());
				}
			} else {
				log.trace("Nothing to send, skipping sending data...");
			}
			
			if (!currCmd.isWaitForEmpty()) {
				log.trace("Waiting for Data: " + currCmd.getWaitFor());
				try {
					getAdapter().getConsumer().consumeInputUntilStringFound(currCmd.getWaitFor());
				} catch (IOException e) {
					throw new AdapterException("An exception has occured while waiting for data: '" + currCmd.getWaitFor() + "': " + e.toString());
				} catch (OperationTimedoutException e) {
					throw new AdapterException("An exception has occured while waiting for data: '" + currCmd.getWaitFor() + "': " + e.toString());
				}
			} else {
				log.trace("Nothing to wait for, skipping waiting for any data...");
			}
		}
	}
	
	@Deprecated
	public void resourceFetchActiveDataOffline(ResourceOperation ro, ResourceTask resourceTask) throws OperationException {
		
	}
	
	
	
	
	

	// TODO: Support adapters via pools
	private GenericTelnetAdapterNew getAdapter() throws AdapterException {
		if (this.adapter == null) {
			adapter = new GenericTelnetAdapterNew();
			adapter.setResource(getResource());
		}

		/*
		if (!adapter.isConnected()) {
			adapter.connect();
		}
		*/
		
		return this.adapter;
	}
}
