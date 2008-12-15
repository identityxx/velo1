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

/*Import Java's SQL support*/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.wrappers.StringTrimmedResultSet;
import org.apache.log4j.Logger;

import velo.common.EdmMessages;
import velo.entity.Resource;
import velo.entity.ResourceAdmin;
import velo.exceptions.AdapterCommandExecutionException;
import velo.exceptions.AdapterException;
import velo.exceptions.DecryptionException;
import velo.exceptions.JdbcDbTypeNotFoundException;
import velo.exceptions.JdbcDriverNotFoundException;
import velo.query.Query;
import velo.query.SingleQuery;
import velo.resource.resourceDescriptor.ResourceDescriptor;
/**
 * A class used to wrap a connection to a database
 * that can be accessed with jdbc.
 *
 * @author Rani sharim
 */
public class JdbcAdapter extends QueryBasedAdapter {
    private static final long serialVersionUID = 1987305452306161213L;
    private static Logger log = Logger.getLogger(JdbcAdapter.class.getName());
    
    /**
     * Holds the JDBC Connection object
     */
    Connection jdbcConnection = null;
    
    /**
     * Holds the JDBC Statement object
     */
    Statement statement = null;
    
    /**
     * The name of the JDBC Driver
     */
    String driverName = null;
    
    /**
     * The DB Type as JDBC expect to be specified on the JDBC connection string url
     */
    String dbType = null;
    
    //Must have an empty constructor per adapter.
    public JdbcAdapter() {
        
    }
    
    public JdbcAdapter(Resource resource) {
		super(resource);
	}
    
        /*
         * Constructor
         * @param resource The Resource entity object the Adapter works with
         * @throws ResourceDescriptorException
         */
        /*
        public JdbcAdapter(Resource resource) throws ResourceDescriptorException {
                super(resource);
                //Cast the ResourceDescriptorSpecificAttributes object to JDBC Specific Object (Assuming resourceDescriptor was already set by first parent!)
                //		ResourceDescriptorJdbcAttributes tsdja = (ResourceDescriptorJdbcAttributes)getResourceDescriptor().getResourceSpecificAttributes();
                //		setResourceDescriptorJdbcAttributes(tsdja);
        }
         */
    
    public boolean connect() throws AdapterException {
        log.debug("Connecting via JDBC Adapter to Resource named: '" + getResource().getDisplayName() + "'");
        //Must init the adapter, mostly initialize the JDBC driver
        init();
        
        EdmMessages ems = new EdmMessages();
        //Rewind the list and loop through each of the admins until a successful connection
        //If the end of the list is reached, then raise an exception
        
        //logger.info("Connecting to Resource name: " + getResource().getDisplayName() + ", via Adapter class name: " + this.getClass().getName());
        ems.info("Connecting to Resource name: "
            + getResource().getDisplayName()
            + ", via Adapter class name: " + this.getClass().getName()
            );
        
        /////logger.info(ems.getLastMessage());
        ResourceDescriptor rd = getResourceDescriptor();
        if (rd == null) {
        	throw new AdapterException("Could not get resource descriptor!");
        }
        
        //Get hostname/dbname from the ResourceCollection
        String dbName = getResourceDescriptor()
        .getString("specific.dbName");
        String hostName = getResourceDescriptor()
        .getString("specific.host");
        String dbType = getResourceDescriptor()
        .getString("specific.dbType");
        String hostPort = getResourceDescriptor().getString("specific.port");
        Integer queryTimeout = getResourceDescriptor().getInt("specific.query-timeout");
        
        
        if (log.isTraceEnabled()) {
            log.trace("Connecting to host: '" + hostName + ", to DB: '" + dbName + "', with dbType: '" + dbType + "', to port: '" + hostPort + "', with query execution timeout in seconds set to: '" + queryTimeout + "'");
        }
        
        if (queryTimeout>2) {
            setQueryTimeout(queryTimeout);
        } else {
            //Default is 30 seconds
            setQueryTimeout(30);
        }
        
        ems.info("Db name is: " + dbName + ", hostName is: " + hostName + "," + "Port: " + hostPort + ", DB Type: " + dbType);
        
        /////logger.info(ems.getLastMessage());
        
        //Build the Properties object
        Properties properties = new Properties();
        //dbName/port are already set by the JDBC URL TEMPLATE
        //properties.put("databaseName",dbName);
        //properties.put("port",hostPort);
        
        //	Build the JDBC connection string
        String urlConnectionString = buildUrlConnectionString(dbName, hostName,hostPort);
        
        //Holds the JDBC Connection
        Connection jdbcConnection;
        
        //Set the bConnectionEstablished by default to false
        setConnected(false);
        
        // Get the iterator of the admin list
        Iterator<ResourceAdmin> tsAdminIterator = getResource()
        .getResourceAdmins().iterator();
        
        //Make sure we got admins!
        if (getResource().getResourceAdmins().size() < 1) {
            ems.info("Could not connect to Target System, cannot find any administrators for Resource name: "
                + getResource().getDisplayName());
            
            /////logger.warning(ems.getLastMessage());
            throw new AdapterException(
                "Could not connect to Target System, cannot find any administrators for Resource name: "
                + getResource().getDisplayName());
        }
        
        //While not the end of the admin list is reached or while the connection estalbished do:
        while (tsAdminIterator.hasNext()) {
            //Get the current TS Admin object
            ResourceAdmin tsAdmin = tsAdminIterator.next();
            
            //Try to connect with the current iterated ts admin
            //LOL//logger.fine("Constructed URL connection string is: " + urlConnectionString);
            
            //LOL//logger.fine("Iterating admin list, trying currently to connect with admin username: " + tsAdmin.getUserName());
            
            properties.put("user", tsAdmin.getUserName());
            try {
                properties.put("password", tsAdmin.getDecryptedPassword());
            } catch (DecryptionException de) {
                throw new AdapterException("Could not connect to target due to: " + de);
            }
            
            try {
                //COmmented, using DbUtil's way jdbcConnection = DriverManager.getConnection (urlConnectionString, properties);
                jdbcConnection = DriverManager.getConnection(urlConnectionString, properties);
                
                
                setJdbcConnection(jdbcConnection);
            } catch (SQLException jdbcException) {
                ems.warning("Couldnt connect with Admin name: "
                    + tsAdmin.getUserName()
                    + ", continuing with next one (if available), error message is: " + jdbcException.getMessage());
                /////logger.warning(ems.getLastMessage());
                //Just continue to the next admin
                //LOL//logger.warning("Failed to connect with admin user name: " + tsAdmin.getUserName() + "with error message: " + jdbcException.getMessage()+ ", continuing to the next admin...");
                
                //If there are NO admins left then throw an exception with an error that a connection could not get established
                if (!tsAdminIterator.hasNext()) {
                    throw new AdapterException(
                        "Could not establish connection with Resource name: "
                        + getResource().getDisplayName() + ", due to connectivity problem or wrong credentials, long trailed connection session log: " + ems.toString());
                } else {
                    continue;
                }
            }
            
            //Try to check whether the connection is open or not, if connection established
            //Then set bConnectionEstablished to true
            try {
                if (!getJdbcConnection().isClosed()) {
                    //Set Connected to true
                    setConnected(true);
                } else {
                    //LOL//logger.warning("DB found as closed!");
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
            
            //LOL//logger.info("Successfully connected to database name: " + dbName);
            return true;
            
        }
        return false;
    }
    
    public boolean disconnect() {
        try {
            if (!getJdbcConnection().isClosed()) {
                try {
                    getJdbcConnection().close();
                    //	LOG: "Database connection terminated"
                    return true;
                }
                //Catch exception of close()
                catch (Exception e) {
                    //LOL//logger.severe(e.getMessage());
                    return false;
                }
                
            } else {
                return false;
            }
        }
        // Catch exception of isClosed()
        catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }
    
    /**
     * Load the JDBC driver before connecting to the system
     * @return true/false upon success/failure
     */
    public boolean loadDriver() {
        try {
            //logger.info("Found drivername for TS: " + (String) getResourceDescriptor().getSpecificAttributes().get("driverName"));
            
            //Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            //Class.forName ("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance ();
            //Commented, currently using Commons.DbUtil Class.forName (getResourceDescriptorJdbcAttributes().getJdbcDriverName()).newInstance();
            DbUtils.loadDriver(getResourceDescriptor().getString("specific.driverName"));
            
            return true;
        } catch (Exception e) {
            /////logger.severe(e.getMessage());
            //LOG: Log the message
            return false;
        }
    }
    
    
    public void runQuery(Query queryManager) throws AdapterException {
        //try {
    	//currently does nothing
        //query.buildQuery();
        
        log.debug("Execution of JdbcAdapter->runQuery() method started...");
        //logger.info("Executing Query: " + query.getQueryString() + ", of type: " + query.getActionType());
        
        //System.out.println("Query ACTION TYPE CLASS: " + query.getActionType().getClass().getName());
        //System.out.println("ACTIONOPTIONS.INSERT TYPE CLASS: " + ActionOptions.INSERT.getClass().getName());
        
        List lMap = null;
        for (SingleQuery currSQ : queryManager.getQueries()) {
        switch(currSQ.getQueryType()) {
            case INSERT: case UPDATE:
                log.debug("An Insert/Update query type is being executed...");
                try {
                    //logger.info("Jdbc query execution for 'INSERT'/'UPDATE' action type STARTED.");
                    
                    log.debug("Executing query, please wait...");
                    
                    //for (SingleQuery currSQ : queryManager.getQueries()) {
                        //Statement st = getJdbcConnection().createStatement();
//                        Statement st = factoryStatement();
                    	PreparedStatement stmt = currSQ.factoryPreparedStatement(getJdbcConnection());
                        
                        log.trace("Executing query against the system...");
                        //qRunner.update(getJdbcConnection(), query.getQueryString());
                        //st.executeQuery(query.getQueryString());
                        
                        //Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or an SQL statement that returns nothing, such as an SQL DDL statement.
                        stmt.executeUpdate();
                        
                        //logger.info("Jdbc query execution for 'INSERT' action type ENDED.");
                        log.debug("Successfully finished executing an 'Update/Insert' query!");
                        
                        //Close the statement
                        stmt.close();
//                    }
                } catch (SQLException e) {
                    //logger.warning("Unable to INSERT record, failed with message: " + e.getMessage());
                    throw new AdapterException(
                        "Unable to INSERT record, failed with message: " + e.getMessage());
                }
                break;
            case SELECT:
                try {
                    log.debug("Jdbc query execution for 'SELECT' action type STARTED.");
                    //qRunner = new QueryRunner();
                    //List lMap = (List)qRunner.query(getJdbcConnection(),query.getQueryString(), new MapListHandler());
                    
                    //Statement st = getJdbcConnection().createStatement();
//                    Statement st = factoryStatement();
                    PreparedStatement stmt = currSQ.factoryPreparedStatement(getJdbcConnection());
                    log.debug("Executing query...");
                    ResultSet rs = (ResultSet) stmt.executeQuery();
                    
                    rs = StringTrimmedResultSet.wrap(rs);
                    MapListHandler mlh = new MapListHandler();
                    
                    if (lMap == null) {
                    	lMap = (List) mlh.handle(rs);
                    } else {
                    	lMap.addAll((List)mlh.handle(rs));
                    }
                    
                                                /*
                                                try {
                                                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/sniffit.txt"), "UTF-8"));
                                                 
                                                        for (int i = 0; i < lMap.size(); i++) {
                                                                Map currCol = (Map) lMap.get(i);
                                                                System.out.println(currCol);
                                                                String firstName = (String) currCol.get("firstName");
                                                                String lastName = (String) currCol.get("lastName");
                                                 
                                                                System.out.println("FirstName: " + firstName);
                                                                System.out.println("LastName: " + lastName);
                                                                writer.write(firstName + "\n\n" + lastName);
                                                        }
                                                        writer.close();
                                                 
                                                 
                                                }
                                                catch (FileNotFoundException fnfe) {
                                                        fnfe.printStackTrace();
                                                }
                                                catch (UnsupportedEncodingException uee) {
                                                        uee.printStackTrace();
                                                }
                                                catch (IOException ioe) {
                                                        ioe.printStackTrace();
                                                }
                                                 */
                    
                    
                    //	Set the result to the adapter
                    setResult(lMap);
                    //logger.info("Jdbc query execution for 'SELECT' action type ENDED.");
                    //Return success
                    
                    //Close the statement
                    stmt.close();
                    log.debug("Successfully finished executing a -SELECT- query!");
                } catch (SQLException e) {
                    //logger.warning("Unable to INSERT/UPDATE record" + e.getMessage());
                    throw new AdapterException(
                        "Unable to INSERT/UPDATE record" + e.getMessage());
                }
                //Debugging
                //
                // Iterator mapIterator = lMap.iterator();
                // while (mapIterator.hasNext()) {
                // System.out.println(mapIterator.next());
                // }
                break;
            case DELETE:
                try {
                	log.debug("A Delete query type is being executed...");
                    //logger.info("Jdbc query execution for 'DELETE' action type STARTED.");
                    //Statement st = getJdbcConnection().createStatement();
                    //st.setQueryTimeout(getQueryTimeout());
                    //qRunner.update(getJdbcConnection(), query.getQueryString());
                    //st.execute(query.getQueryString());
//                    for (String queryString : query.getQueries()) {
                    	log.debug("Executing query...");
                    	PreparedStatement stmt = currSQ.factoryPreparedStatement(getJdbcConnection());
                    	
//                        Statement st = factoryStatement();
                        //Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement or an SQL statement that returns nothing, such as an SQL DDL statement.
//                        st.executeUpdate(queryString);
                    	stmt.executeUpdate();
                        //qRunner.update(getJdbcConnection(), query.getQueryString());
                        //logger.info("Jdbc query execution for 'DELETE' action type ENDED.");
                        //Return failure
                        //Close the statement
                        stmt.close();
                    log.debug("Successfully finished executing a -DELETE- query!");
                } catch (SQLException e) {
                    //logger.warning("Unable to DELETE record: " + e.getMessage());
                    throw new AdapterException(
                        "Unable to DELETE record: " + e.getMessage());
                }
                break;
            default:
                throw new AdapterException("Jdbc query execution FAILED, must set Action type before execution!");
        }
        }
    }
    
    /**
     * Perform a query, return true/false upon result
     * @param query The query object with the query to execute
     * @return true/false upon result
     * @throws AdapterCommandExecutionException
     */
    public boolean isTrueQuery(Query queryManager) throws AdapterCommandExecutionException {
        
    	if (!queryManager.isHasQueries()) {
    		throw new AdapterCommandExecutionException("Could not find any queries to execute...");
    	}
    	
        
        //logger.info("Executing isTrueQuery action, checking whether the query returns true/false");
        //logger.info("Executing query: " + query.getQueryString());
        
        //Before executing, make sure that the connection is not null
        if (getJdbcConnection() == null) {
            throw new AdapterCommandExecutionException(
                "Jdbc query execution FAILED, connection object is null.");
        }
        
        ResultSetHandler rsh = new ResultSetHandler() {
            public Boolean handle(ResultSet rs) throws SQLException {
                if (!rs.next()) {
                    return false;
                }
                
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                Object[] result = new Object[cols];
                
                for (int i = 0; i < cols; i++) {
                    result[i] = rs.getObject(i + 1);
                }
                return true;
            }
        };
        
        QueryRunner qRunner = new QueryRunner();
        
        try {
        	SingleQuery sq = queryManager.getQueries().iterator().next(); 
        	boolean result = (Boolean) qRunner.query(sq.getQueryString(), sq.getParams(), rsh);
//            boolean result = (Boolean) qRunner.query(getJdbcConnection(), queryManager.getQueries().iterator().next().getQueryString(), rsh);
            
            if (result) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException se) {
            String message = "Failed to perform 'isTrueQuery', failed with message: " + se.getMessage();
            //logger.warning(message);
            
            //Since we cannot thorugh an exception (since true/false must be returned) lets log into the adapter messages the failure message
            getMsgs().warning(message);
            return false;
        }
    }
    
    /**
     * @deprecated use runQuery() instead
     */
    public boolean runCommandString(String sCmdString) {
        try {
                        /*
                         Statement stmt = getJdbcConnection().createStatement();
                         //System.out.println(stmt);
                         stmt.executeQuery(sCmdString);
                         //Set the statement to the class
                         setStatement(stmt);
                         //Return the statement after execution
                         return true;
                         */
            
            QueryRunner qRunner = new QueryRunner();
            //List lMap = (List) qRunner.query(getJdbcConnection(),
            //      "SELECT UserId, FirstName FROM dbo.qflow.qfUser WHERE UserId<100");
            //new String[] { "1", "2" }, new MapListHandler());
            List lMap = (List) qRunner.query(getJdbcConnection(), sCmdString,
                new MapListHandler());
            
                        /*Tests
                         for (int i = 0; i < lMap.size(); i++) {
                         Map vals = (Map) lMap.get(i);
                         
                         System.out.println("\tId >>" + vals.get("userId"));
                         System.out.println("\tName >>" + vals.get("userName"));
                         }
                         */
            
            return true;
            
        }
        //Catch exception if something went wrong while creating/executing the query
        catch (SQLException se) {
            //logger.warning("SQL Exception has occured: " + se.getMessage());
            return false;
        }
    }
    
    /**
     * @deprecated
     * @return true/false
     */
    public boolean setLastCommandResultSetFromConnector() {
        return false;
    }
    
    /**
     * @deprecated
     * @return true/false
     */
    public boolean setBLastCommandSuccessfullFromConnector() {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * Get the driver name to load as expected by Init
     * @return The Driver Name
     * @throws JdbcDriverNotFoundException - When no driver name found
     */
    public String getDriverName() throws JdbcDriverNotFoundException {
        if (this.driverName != null) {
            return this.driverName;
        } else {
            //Throw a 'Drivername not found' exception
            throw new JdbcDriverNotFoundException();
        }
    }
    
    /**
     * Get the DB type (required by the ConnectionString)
     * @return The DB Type string
     * @throws JdbcDbTypeNotFoundException - Where no DB Type found
     */
    public String getDbType() throws JdbcDbTypeNotFoundException {
        if (this.dbType != null) {
            return this.dbType;
        } else {
            //Throw a 'Drivertype not found' exception
            throw new JdbcDbTypeNotFoundException();
        }
    }
    
    /**
     * Return a connection string url as expected by JDBC
     * @param dbName The DB Type as expected by jdbC (I.e Mysql,Oracle, etc...)
     * @param hostName The hostname of the Database to connect to
     * @param hostPort The port of the DB hostname
     * @return The connection string url as expected by JDBC
     */
    public String buildUrlConnectionString(String dbName, String hostName,
        String hostPort) throws AdapterException {
        //Get the jdbc URL template from the TargetDescriptors
        String jdbcTemplateURL = (String) getResourceDescriptor()
        .getString("specific.urlTemplate");
        if (jdbcTemplateURL == null) {
        	throw new AdapterException("URL Template was not found in resource configuration");
        }
        
        jdbcTemplateURL = jdbcTemplateURL.replaceFirst("%d", dbName);
        jdbcTemplateURL = jdbcTemplateURL.replaceFirst("%h", hostName);
        jdbcTemplateURL = jdbcTemplateURL.replaceFirst("%p", hostPort);
        /////logger.info("Generated JDBC connection URL string is: " + jdbcTemplateURL);
        
        return jdbcTemplateURL;
    }
    
    /**
     * Set the JDBC "Connection" object to the class
     * @param jdbcConnection - The JDBC Connection object to set
     */
    public void setJdbcConnection(Connection jdbcConnection) {
        //logger.fine("Connection is: " + jdbcConnection);
        this.jdbcConnection = jdbcConnection;
    }
    
    /**
     * Get the JDBC "Connection" object from the class
     * @return The JDBC Connection object, returns null if not exist
     */
    public Connection getJdbcConnection() {
                /*
                 if (this.jdbcConnection != null) {
                 return this.jdbcConnection;
                 }
                 else {
                 return null;
                 }
                 */
        return this.jdbcConnection;
    }
    
        /*
         * Set a statement to the class
         * @param stmt - The new statement to set
         * @return true/false upon success failure
         
         public boolean setStatement(Statement stmt)
         {
         this.statement = stmt;
         if (this.statement != null) {
         return true;
         }
         else {
         return false;
         }
         }
         
         public Statement getStatement() {
         if (this.statement != null) {
         return this.statement;
         }
         else {
         //LOG: no statement was found
         return null;
         }
         }
         */
    
    
    public boolean init() {
        boolean Validated = true;
        if (loadDriver()) {
            //LOG: Successfully loaded JDBC driver
        } else {
            //LOG: Failed to load JDBC driver
            Validated = false;
        }
        
        //IF initialize was validated, then return true, otherwise return false
        if (Validated) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public Statement factoryStatement() throws SQLException {
        Statement st = getJdbcConnection().createStatement();
        st.setQueryTimeout(getQueryTimeout());
        
        return st;
    }
    
}//eoc
