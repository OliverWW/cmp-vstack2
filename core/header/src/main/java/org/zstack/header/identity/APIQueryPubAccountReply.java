package org.zstack.header.identity;

import org.zstack.header.query.APIQueryReply;

import java.util.List;

/**
 * Created by frank on 7/14/2015.
 */
public class APIQueryPubAccountReply extends APIQueryReply {
    private List<PubAccountInventory> inventories;

    public List<PubAccountInventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<PubAccountInventory> inventories) {
        this.inventories = inventories;
    }
}
