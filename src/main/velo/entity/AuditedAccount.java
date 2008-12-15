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
package velo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Table(name="VL_AUDITED_ACCOUNT")
@Entity
@SequenceGenerator(name="AuditedAccountIdSeq",sequenceName="AUDITED_ACCOUNT_ID_SEQ")

@NamedQueries( {
		@NamedQuery(name = "auditedAccount.isExists", query = "SELECT count(aa) FROM AuditedAccount aa WHERE UPPER(aa.name) = :accountName AND aa.resource.uniqueName = :resourceUniqueName"),
		@NamedQuery(name = "auditedAccount.searchByString", query = "SELECT object(aa) from AuditedAccount aa WHERE UPPER(aa.name) like :searchString")
})
public class AuditedAccount extends AccountSkeletal {
    private long auditedAccountId;
    
    public AuditedAccount() {
    }
    
    public AuditedAccount(String name, Resource ts) {
        setResource(ts);
        setName(name);
        setCreationDate(new Date());
    }
    
    /**
     Get the audited account ID
     @return auditedAccount ID
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="AuditedAccountIdSeq")
    //@GeneratedValue //JB
    @Column(name="AUDITED_ACCOUNT_ID")
    public long getAuditedAccountId() {
        //WTF IS THAT? java.sql.Timestamp t = new java.sql.Timestamp(accountId);
        return auditedAccountId;
    }
    
    /**
     Set audited account ID
     @param auditedAccountId Audited Account ID to set
     */
    public void setAuditedAccountId(long auditedAccountId) {
        this.auditedAccountId = auditedAccountId;
    }
    
    public void copyValues(Object entity) {
    	//TODO: Implement
    }
}