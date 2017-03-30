package org.zstack.header.vm;

import org.zstack.header.tag.AutoDeleteTag;
import org.zstack.header.vo.EO;

import java.util.List;

import javax.persistence.*;
 
public class ECSNode   {
	
	private String id;
	private String name;
	private String state;
	private List<String> public_ips;
	private List<String> private_ips;
	private String driver;
	private String configuration;
	private String node;
	

	
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	} 
 
	public String getName() {
		return name;
	}
	 
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<String> getPublic_ips() {
		return public_ips;
	}
	public void setPublic_ips(List<String> public_ips) {
		this.public_ips = public_ips;
	}
	public List<String> getPrivate_ips() {
		return private_ips;
	}
	public void setPrivate_ips(List<String> private_ips) {
		this.private_ips = private_ips;
	}
	 
	
}
