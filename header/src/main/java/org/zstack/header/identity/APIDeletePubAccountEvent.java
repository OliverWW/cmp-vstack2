package org.zstack.header.identity;

import org.zstack.header.message.APIEvent;

/**
 * Created by frank on 7/15/2015.
 */
public class APIDeletePubAccountEvent extends APIEvent {
    public APIDeletePubAccountEvent() {
    }

    public APIDeletePubAccountEvent(String apiId) {
        super(apiId);
    }
}
