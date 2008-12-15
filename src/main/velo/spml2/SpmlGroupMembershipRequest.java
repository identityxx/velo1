package velo.spml2;

import org.openspml.v2.msg.spml.BatchableRequest;
import org.openspml.v2.msg.spml.ExecutionMode;
import org.openspml.v2.msg.spml.PSOIdentifier;

public class SpmlGroupMembershipRequest extends BatchableRequest {
	private PSOIdentifier m_psoID = null; // required

	protected SpmlGroupMembershipRequest() {
		super();
	}

	protected SpmlGroupMembershipRequest(String requestId,
			ExecutionMode executionMode, PSOIdentifier psoID) {
		super(requestId, executionMode);
		assert (psoID != null);
		m_psoID = psoID;
	}

	public PSOIdentifier getPsoID() {
		return m_psoID;
	}

	public void setPsoID(PSOIdentifier psoID) {
		assert (psoID != null);
		m_psoID = psoID;
	}

	/*
	public PrefixAndNamespaceTuple[] getNamespacesInfo() {
		return PrefixAndNamespaceTuple.concatNamespacesInfo(super.getNamespacesInfo(),NamespaceDefinitions.getMarshallableNamespacesInfo());
	}
	*/

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SpmlGroupMembershipRequest))
			return false;
		if (!super.equals(o))
			return false;

		final SpmlGroupMembershipRequest basicPasswordRequest = (SpmlGroupMembershipRequest) o;

		if (!m_psoID.equals(basicPasswordRequest.m_psoID))
			return false;

		return true;
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 29 * result + m_psoID.hashCode();
		return result;
	}
}
