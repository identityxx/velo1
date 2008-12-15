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
//@!@clean
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;

import velo.common.SysConf;
import velo.encryption.EncryptionUtils;
import velo.exceptions.DecryptionException;
import velo.exceptions.EncryptionException;
import velo.tools.FileUtils;

/**
 * An entity that represents a Resource Administrator
 *
 * @author Asaf Shakarchi
 */
@Name("resourceAdmin")
@SequenceGenerator(name="ResourceAdminIdSeq",sequenceName="RESOURCE_ADMIN_ID_SEQ")
@Table(name="VL_RESOURCE_ADMIN")
@Entity
public class ResourceAdmin extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1987305452306162223L;
    
    private long resourceAdminId;
    
    private Resource resource;
    
    private String userName;
    
    private String password;
    
    private int priority;
    

    public void setResourceAdminId(long resourceAdminId) {
        this.resourceAdminId = resourceAdminId;
    }
    

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="ResourceAdminIdSeq")
    //@GeneratedValue //JB
    @Column(name="RESOURCE_ADMIN_ID")
    public long getResourceAdminId() {
        return resourceAdminId;
    }
    

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    @ManyToOne(optional=false)
    @JoinColumn(name="RESOURCE_ID", nullable=false)
    public Resource getResource() {
        return resource;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Column(name="USER_NAME", nullable=false)
    @Length(min=2, max=50) @NotNull //seam
    public String getUserName() {
        return userName;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Column(name="PASSWORD", nullable=false)
    @Length(min=1, max=100) @NotNull //seam
    public String getPassword() {
        return password;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    @Column(name="PRIORITY", nullable=false)
    //@Min(1) @Range(max=100) //// a numeric between 1 and 100
    public int getPriority() {
        return priority;
    }
    
    
    
    
    
    
    //Transients
    public void encryptPassword() throws EncryptionException {
        String fileName = SysConf.getSysConf().getString("system.directory.system_conf") + "/" + "keys" + "/" + SysConf.getSysConf().getString("system.files.targets_principals_encryption_key");
        try {
            String key = FileUtils.readLine(fileName);
            setPassword(EncryptionUtils.encrypt(getPassword(),key));
            //TODO: EncryptionUtils does not handle exceptions so well, encapsulate all possible exception.
        } catch (Exception ex) {
            throw new EncryptionException("Could not encrypt password due to: " + ex);
        }
    }
    
    @Transient
    public String getDecryptedPassword() throws DecryptionException {
        String fileName = SysConf.getSysConf().getString("system.directory.system_conf") + "/" + "keys" + "/" + SysConf.getSysConf().getString("system.files.targets_principals_encryption_key");
        
        try {
            String key = FileUtils.readLine(fileName);
            return EncryptionUtils.decrypt(getPassword(),key);
            //TODO: EncryptionUtils does not handle exceptions so well, encapsulate all possible exception.
        } catch (Exception ex) {
            throw new DecryptionException("Could not decrypt password due to: " + ex);
        }
    }
}
