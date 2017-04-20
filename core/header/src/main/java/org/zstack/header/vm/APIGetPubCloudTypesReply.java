package org.zstack.header.vm;

import org.zstack.header.message.APIReply;

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
public class APIGetPubCloudTypesReply extends APIReply {
    /**
     * @desc a list of hypervisor types supported by zstack
     * @choices
     * - KVM
     * - Simulator
     */
    private List<String> cloudTypes;

    public List<String> getCloudTypes() {
		return cloudTypes;
	}

	public void setCloudTypes(List<String> cloudTypes) {
		this.cloudTypes = cloudTypes;
	}

	 
}
