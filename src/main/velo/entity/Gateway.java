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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="VL_GATEWAY")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="GATEWAY_TYPE")
@SequenceGenerator(name="GatewayIdSeq",sequenceName="GATEWAY_ID_SEQ")
public class Gateway extends ExtBasicEntity {
	
	private Long gatewayId;
	private String hostName;
	private Integer port = 8000;
	private List<Resource> resources;
	
	public enum GatewayType {
    	WIN_GATEWAY, LINUX_GATEWAY, GENERIC
	}
	
	
	/**
	 * @return the gatewayId
	 */
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO,generator="GatewayIdSeq")
    @Column(name="GATEWAY_ID")
	public Long getGatewayId() {
		return gatewayId;
	}

	/**
	 * @param gatewayId the gatewayId to set
	 */
	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}
	
	/**
	 * @return the host
	 */
	@Column(name = "HOSTNAME", nullable = false, unique = false)
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * @return the port
	 */
	@Column(name = "PORT", nullable = false, unique = false)
	public Integer getPort() {
		return port;
	}
	
	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	
	@OneToMany(mappedBy = "gateway", fetch = FetchType.LAZY)
	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@Transient
	public GatewayType getType() {
		return GatewayType.GENERIC;
	}

}
