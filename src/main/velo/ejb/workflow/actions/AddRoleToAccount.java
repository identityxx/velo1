package velo.ejb.workflow.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class AddRoleToAccount implements ActionHandler {
	private static Log log = LogFactory.getLog(AddRoleToAccount.class);
	public String blob;
	
	public AddRoleToAccount()
	{
		log.debug("Default constructor");
		blob = "default";
	}
	
	public AddRoleToAccount(String xml)
	{
		this();
		
		log.debug("Config constructor");
		
		configure(xml);
	}
	
	public void configure(String xml)
	{
		log.debug("Parse: " + xml.trim());
	}
	
	public void setBlob(String blob)
	{
		log.debug("Setter");
		this.blob = blob;
	}

	public void execute(ExecutionContext executionContext) throws Exception
	{
		log.debug("Execute: " + blob);
	}
}
