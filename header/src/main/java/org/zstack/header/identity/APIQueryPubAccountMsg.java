package org.zstack.header.identity;

import org.zstack.header.query.APIQueryMessage;
import org.zstack.header.query.AutoQuery;

/**
 * Created by frank on 7/14/2015.
 */
@AutoQuery(replyClass = APIQueryPubAccountReply.class, inventoryClass = PubAccountInventory.class)
public class APIQueryPubAccountMsg extends APIQueryMessage {
}
