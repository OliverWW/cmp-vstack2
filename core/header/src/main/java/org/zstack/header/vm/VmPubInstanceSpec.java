package org.zstack.header.vm;

import org.zstack.header.configuration.DiskOfferingInventory;
import org.zstack.header.host.HostInventory;
import org.zstack.header.image.ImageBackupStorageRefInventory;
import org.zstack.header.image.ImageInventory;
import org.zstack.header.message.Message;
import org.zstack.header.network.l3.L3NetworkInventory;
import org.zstack.header.network.service.NetworkServiceL3NetworkRefInventory;
import org.zstack.header.storage.primary.PrimaryStorageInventory;
import org.zstack.header.vm.VmInstanceConstant.VmOperation;
import org.zstack.header.volume.VolumeInventory;
import org.zstack.utils.JsonWrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VmPubInstanceSpec implements Serializable {
  
	 private String name;
	 private String uuid;
	    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

		private String accesskeyKEY;
	    private String accesskeyID;

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

     
}
