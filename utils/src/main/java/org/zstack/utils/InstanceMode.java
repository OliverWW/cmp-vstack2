package org.zstack.utils;

public class InstanceMode {
	private String uuid;
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	private String cpuNum;
	private String image;
	private String memory;
	public String getMemory() {
		return memory;
	}
	public void setMemory(String memory) {
		this.memory = memory;
	}
	private String diskSize;
	public String getCpuNum() {
		return cpuNum;
	}
	public void setCpuNum(String cpuNum) {
		this.cpuNum = cpuNum;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDiskSize() {
		return diskSize;
	}
	public void setDiskSize(String diskSize) {
		this.diskSize = diskSize;
	}
	 
}
