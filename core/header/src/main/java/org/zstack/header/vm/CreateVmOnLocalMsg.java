package org.zstack.header.vm;

import org.zstack.header.core.ApiTimeout;
import org.zstack.header.host.HostMessage;
import org.zstack.header.message.NeedReplyMessage;

@ApiTimeout(apiClasses = { APICreateVmInstanceMsg.class })
public class CreateVmOnLocalMsg extends NeedReplyMessage implements HostMessage {
	private VmPubInstanceSpec vmSpec;

	private String name;
	private String uuid;

	private String accesskeyKEY;
	private String accesskeyID;
	private String image;
	private String cloudType;
	private String instanceOfferingUuid;
	private String region;
	
 
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCloudType() {
		return cloudType;
	}

	public void setCloudType(String cloudType) {
		this.cloudType = cloudType;
	}

	public String getInstanceOfferingUuid() {
		return instanceOfferingUuid;
	}

	public void setInstanceOfferingUuid(String instanceOfferingUuid) {
		this.instanceOfferingUuid = instanceOfferingUuid;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccesskeyKEY() {
		return accesskeyKEY;
	}

	public void setAccesskeyKEY(String accesskeyKEY) {
		this.accesskeyKEY = accesskeyKEY;
	}

	public String getAccesskeyID() {
		return accesskeyID;
	}

	public void setAccesskeyID(String accesskeyID) {
		this.accesskeyID = accesskeyID;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getHostUuid() {
		return vmSpec.getUuid();
	}

	public VmPubInstanceSpec getVmSpec() {
		return vmSpec;
	}

	public void setVmSpec(VmPubInstanceSpec vmSpec) {
		this.vmSpec = vmSpec;
	}
}
