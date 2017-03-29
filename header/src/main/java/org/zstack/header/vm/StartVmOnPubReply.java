package org.zstack.header.vm;

import org.zstack.header.message.MessageReply;

public class StartVmOnPubReply extends MessageReply {
 private String vmUuid;

public String getVmUuid() {
	return vmUuid;
}

public void setVmUuid(String vmUuid) {
	this.vmUuid = vmUuid;
}
 
}
