package org.zstack.header.identity;

import org.zstack.header.message.APIEvent;

public class APICreatePubAccountEvent extends APIEvent {
    private PubAccountInventory inventory;
    
    public APICreatePubAccountEvent(String apiId) {
        super(apiId);
    }
    
    public APICreatePubAccountEvent() {
        super(null);
    }

    public PubAccountInventory getInventory() {
        return inventory;
    }

    public void setInventory(PubAccountInventory inventory) {
        this.inventory = inventory;
    }
}
