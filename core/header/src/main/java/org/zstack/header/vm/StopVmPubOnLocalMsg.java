package org.zstack.header.vm;

import org.zstack.header.host.HostMessage;
import org.zstack.header.message.NeedReplyMessage;

public class StopVmPubOnLocalMsg extends NeedReplyMessage implements HostMessage {
    private VmInstanceInventory vmInventory;
    public String getAccess_key_id() {
		return access_key_id;
	}

	public void setAccess_key_id(String access_key_id) {
		this.access_key_id = access_key_id;
	}

	public String getAccess_key_secret() {
		return access_key_secret;
	}

	public void setAccess_key_secret(String access_key_secret) {
		this.access_key_secret = access_key_secret;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getForce() {
		return force;
	}

	public void setForce(String force) {
		this.force = force;
	}
	private String type;
    private String vMUuid;
    
    private String access_key_id;
    private String access_key_secret;
    private String region;
    private String force;

    public String getvMUuid() {
		return vMUuid;
	}

	public void setvMUuid(String vMUuid) {
		this.vMUuid = vMUuid;
	}

	public VmInstanceInventory getVmInventory() {
        return vmInventory;
    }

    public void setVmInventory(VmInstanceInventory vmInventory) {
        this.vmInventory = vmInventory;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }
    @Override
    public String getHostUuid() {
        return vmInventory.getHostUuid();
    }
}
