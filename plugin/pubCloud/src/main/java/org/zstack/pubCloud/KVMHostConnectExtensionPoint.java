package org.zstack.pubCloud;

import org.zstack.header.core.workflow.Flow;

public interface KVMHostConnectExtensionPoint {
    Flow createKvmHostConnectingFlow(KVMHostConnectedContext context);
}
