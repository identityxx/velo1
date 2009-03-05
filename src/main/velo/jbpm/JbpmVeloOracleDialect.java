package velo.jbpm;

import org.hibernate.dialect.OracleDialect;

public class JbpmVeloOracleDialect extends OracleDialect {
	public Class getNativeIdentifierGeneratorClass() {
	    return JbpmVeloSequenceGenerator.class;
	}
}
