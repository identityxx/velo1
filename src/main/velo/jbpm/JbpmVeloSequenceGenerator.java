package velo.jbpm;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.Type;


public class JbpmVeloSequenceGenerator extends SequenceGenerator {
	/**
	    * The longest allowable sequence name length: 30 for oracle.
	    */
	   protected int SEQUENCE_NAME_LENGTH = 30;
	   
	   /**
	    * Additional parameters for the DDL of the sequence.
	    */
	   protected static final String DDL_PARAMETERS = "nocache";
	   private static final Log log = LogFactory.getLog(JbpmVeloSequenceGenerator.class);
	   
	   /**
	    * The sequence parameter
	    */
	   //public static final String SEQUENCE = "sequence";

	   /**
	    * The parameters parameter, appended to the create sequence DDL.
	    * For example (Oracle): <tt>INCREMENT BY 1 START WITH 1 MAXVALUE 100 NOCACHE</tt>.
	    */
	   //public static final String PARAMETERSa = "INCREMENT BY 1 START WITH 1 MAXVALUE 100 NOCACHE";
	   
	    /**
	     * If the parameters do not contain a {@link SequenceGenerator#SEQUENCE} name, we
	     * assign one based on the table name.
	     */
	    public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
	       params.setProperty(PARAMETERS, DDL_PARAMETERS);
	        if(params.getProperty(SEQUENCE) == null || params.getProperty(SEQUENCE).length() == 0) {
	            String tableName = params.getProperty(PersistentIdentifierGenerator.TABLE);
	            if(tableName != null) {
	               String tmpSeqName = "S_" + tableName;
	                String seqName;
	                if(tmpSeqName.length() <= SEQUENCE_NAME_LENGTH){
	                   seqName = tmpSeqName;
	                }
	                else{
	                   seqName = tmpSeqName.substring(0, SEQUENCE_NAME_LENGTH);
	                   log.warn(tmpSeqName + " truncated to " + seqName
	                         + ". This may collide with another sequence already in the database.");
	                }
	                params.setProperty(SEQUENCE, seqName);
	            }
	        }
	        super.configure(type, params, dialect);
	        
	    } 
}
