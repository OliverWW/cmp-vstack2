package org.zstack.header.vm;

import org.zstack.header.message.APIReply;
import org.zstack.utils.InstanceMode;
import org.zstack.utils.PubCloud;

import java.util.List;

/**
 *@apiResult
 *
 * api reply for message :ref:`APIGetHypervisorTypesMsg`
 *
 *@since 0.1.0
 *
 *@example
 *
 * {
"org.zstack.header.host.APIGetHypervisorTypesReply": {
"hypervisorTypes": [
"KVM",
"Simulator"
],
"success": true
}
}
 */
public class APIQueryPubVmInstanceOfferingReply extends APIReply {
    /**
     * @desc a list of hypervisor types supported by zstack
     * @choices
     * - KVM
     * - Simulator
     */
    private List<InstanceMode> instanceOffering;
    private List<String> images;

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public List<InstanceMode> getInstanceOffering() {
		return instanceOffering;
	}

	public void setInstanceOffering(List<InstanceMode> instanceOffering) {
		this.instanceOffering = instanceOffering;
	}
 

   

	 
}
