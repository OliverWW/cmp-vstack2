package org.zstack.pubCloud;

import org.zstack.core.validation.ConditionalValidation;
import org.zstack.header.configuration.InstanceOffering;
import org.zstack.header.configuration.InstanceOfferingInventory;
import org.zstack.header.core.ApiTimeout;
import org.zstack.header.core.validation.Validation;
import org.zstack.header.storage.snapshot.APIDeleteVolumeSnapshotMsg;
import org.zstack.header.vm.APICreateVmInstanceMsg;
import org.zstack.header.vm.ECSNode;
import org.zstack.header.vm.VmBootDevice;
import org.zstack.header.volume.APICreateVolumeSnapshotMsg;
import org.zstack.network.securitygroup.SecurityGroupRuleTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ECSAgentCommands {
	public static enum BootDev {
		hd(VmBootDevice.HardDisk),
		cdrom(VmBootDevice.CdRom);

        private VmBootDevice device;

        private BootDev(VmBootDevice dev) {
            device = dev;
        }

        public VmBootDevice toVmBootDevice() {
            return device;
        }
	}
	
    public static class AgentResponse implements ConditionalValidation {
        private boolean success = true;
        private String error;
        public boolean isSuccess() {
            return success;
        }
        public void setSuccess(boolean success) {
            this.success = success;
        }
        public String getError() {
            return error;
        }
        public void setError(String error) {
            this.error = error;
        }

        @Override
        public boolean needValidation() {
            return success;
        }
    }
    
    public static class AgentCommand {
    }

    public static class CheckVmStateCmd extends AgentCommand {
        public List<String> vmUuids;
        public String hostUuid;
    }

    public static class CheckVmStateRsp extends AgentResponse {
        public Map<String, String> states;
    }

    public static class DetachNicCommand extends AgentCommand {
        private String vmUuid;
        private NicTO nic;

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }

        public NicTO getNic() {
            return nic;
        }

        public void setNic(NicTO nic) {
            this.nic = nic;
        }
    }

    public static class DetachNicRsp extends AgentResponse {

    }

    public static class AttachNicCommand extends AgentCommand {
        private String vmUuid;
        private NicTO nic;
        private Map addons = new HashMap();

        public Map getAddons() {
            return addons;
        }

        public void setAddons(Map addons) {
            this.addons = addons;
        }

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }

        public NicTO getNic() {
            return nic;
        }

        public void setNic(NicTO nic) {
            this.nic = nic;
        }
    }

    public static class AttachNicResponse extends AgentResponse {

    }

    public static class ConnectCmd extends AgentCommand {
        private String hostUuid;
        private String sendCommandUrl;

        public String getSendCommandUrl() {
            return sendCommandUrl;
        }

        public void setSendCommandUrl(String sendCommandUrl) {
            this.sendCommandUrl = sendCommandUrl;
        }

        public String getHostUuid() {
            return hostUuid;
        }
        public void setHostUuid(String hostUuid) {
            this.hostUuid = hostUuid;
        }
    }
    
    public static class ConnectResponse extends AgentResponse {
        private String libvirtVersion;
        private String qemuVersion;

        public String getLibvirtVersion() {
            return libvirtVersion;
        }

        public void setLibvirtVersion(String libvirtVersion) {
            this.libvirtVersion = libvirtVersion;
        }

        public String getQemuVersion() {
            return qemuVersion;
        }

        public void setQemuVersion(String qemuVersion) {
            this.qemuVersion = qemuVersion;
        }
    }
    
    public static class PingCmd extends AgentCommand {
        public String hostUuid;
    }
    
    public static class PingResponse extends AgentResponse {
        private String hostUuid;
        public String getHostUuid() {
            return hostUuid;
        }
        public void setHostUuid(String hostUuid) {
            this.hostUuid = hostUuid;
        }
    }
    
    public static class CheckPhysicalNetworkInterfaceCmd extends AgentCommand {
        private List<String> interfaceNames = new ArrayList<String>(2);

        public CheckPhysicalNetworkInterfaceCmd addInterfaceName(String name) {
            interfaceNames.add(name);
            return this;
        }
        public List<String> getInterfaceNames() {
            return interfaceNames;
        }

        public void setInterfaceNames(List<String> interfaceNames) {
            this.interfaceNames = interfaceNames;
        }
    }
    
    public static class CheckPhysicalNetworkInterfaceResponse extends AgentResponse {
        private List<String> failedInterfaceNames;
        public List<String> getFailedInterfaceNames() {
            if (failedInterfaceNames == null) {
                failedInterfaceNames = new ArrayList<String>(0);
            }
            return failedInterfaceNames;
        }

        public void setFailedInterfaceNames(List<String> failedInterfaceNames) {
            this.failedInterfaceNames = failedInterfaceNames;
        }
    }

    public static class HostFactCmd extends AgentCommand {
    }

    public static class HostFactResponse extends AgentResponse {
        private String qemuImgVersion;
        private String libvirtVersion;
        private String hvmCpuFlag;

        public String getHvmCpuFlag() {
            return hvmCpuFlag;
        }

        public void setHvmCpuFlag(String hvmCpuFlag) {
            this.hvmCpuFlag = hvmCpuFlag;
        }

        public String getLibvirtVersion() {
            return libvirtVersion;
        }

        public void setLibvirtVersion(String libvirtVersion) {
            this.libvirtVersion = libvirtVersion;
        }

        public String getQemuImgVersion() {
            return qemuImgVersion;
        }

        public void setQemuImgVersion(String qemuImgVersion) {
            this.qemuImgVersion = qemuImgVersion;
        }
    }
    
    public static class HostCapacityCmd extends AgentCommand {
    }
    
    public static class HostCapacityResponse extends AgentResponse {
        private long cpuNum;
        private long cpuSpeed;
        private long usedCpu;
        private long totalMemory;
        private long usedMemory;

        public long getCpuNum() {
            return cpuNum;
        }
        public void setCpuNum(long cpuNum) {
            this.cpuNum = cpuNum;
        }
        public long getCpuSpeed() {
            return cpuSpeed;
        }
        public void setCpuSpeed(long cpuSpeed) {
            this.cpuSpeed = cpuSpeed;
        }
        public long getUsedCpu() {
            return usedCpu;
        }
        public void setUsedCpu(long usedCpu) {
            this.usedCpu = usedCpu;
        }
        public long getTotalMemory() {
            return totalMemory;
        }
        public void setTotalMemory(long totalMemory) {
            this.totalMemory = totalMemory;
        }
        public long getUsedMemory() {
            return usedMemory;
        }
        public void setUsedMemory(long usedMemory) {
            this.usedMemory = usedMemory;
        }
    }
    
    public static class CreateBridgeCmd extends AgentCommand {
        private String physicalInterfaceName;
        private String bridgeName;
        public String getPhysicalInterfaceName() {
            return physicalInterfaceName;
        }
        public void setPhysicalInterfaceName(String physicalInterfaceName) {
            this.physicalInterfaceName = physicalInterfaceName;
        }
        public String getBridgeName() {
            return bridgeName;
        }
        public void setBridgeName(String bridgeName) {
            this.bridgeName = bridgeName;
        }
    }

    public static class CreateBridgeResponse extends AgentResponse {
    }

    public static class CheckBridgeCmd extends AgentCommand {
        private String physicalInterfaceName;
        private String bridgeName;
        public String getPhysicalInterfaceName() {
            return physicalInterfaceName;
        }
        public void setPhysicalInterfaceName(String physicalInterfaceName) {
            this.physicalInterfaceName = physicalInterfaceName;
        }
        public String getBridgeName() {
            return bridgeName;
        }
        public void setBridgeName(String bridgeName) {
            this.bridgeName = bridgeName;
        }
    }
    public static class CheckBridgeResponse extends AgentResponse {
    }

    public static class CheckVlanBridgeCmd extends CheckBridgeCmd {
        private int vlan;

        public int getVlan() {
            return vlan;
        }

        public void setVlan(int vlan) {
            this.vlan = vlan;
        }
    }
    public static class CheckVlanBridgeResponse extends CheckBridgeResponse {
    }

    public static class CreateVlanBridgeCmd extends CreateBridgeCmd {
        private int vlan;

        public int getVlan() {
            return vlan;
        }

        public void setVlan(int vlan) {
            this.vlan = vlan;
        }
    }
    public static class CreateVlanBridgeResponse extends CreateBridgeResponse {
    }
    
    public static class NicTO {
    	private String mac;
    	private String bridgeName;
        private String uuid;
    	private String nicInternalName;
    	private int deviceId;
    	private String metaData;
        private Boolean useVirtio;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Boolean getUseVirtio() {
            return useVirtio;
        }

        public void setUseVirtio(Boolean useVirtio) {
            this.useVirtio = useVirtio;
        }

        public String getMac() {
			return mac;
		}
		public void setMac(String mac) {
			this.mac = mac;
		}
		public String getBridgeName() {
			return bridgeName;
		}
		public void setBridgeName(String bridgeName) {
			this.bridgeName = bridgeName;
		}
		public int getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(int deviceId) {
			this.deviceId = deviceId;
		}
        public String getMetaData() {
            return metaData;
        }
        public void setMetaData(String metaData) {
            this.metaData = metaData;
        }
        public String getNicInternalName() {
            return nicInternalName;
        }
        public void setNicInternalName(String nicInternalName) {
            this.nicInternalName = nicInternalName;
        }
        
    }
    
    public static class VolumeTO {
        public static final String FILE = "file";
        public static final String ISCSI = "iscsi";
        public static final String CEPH = "ceph";
        public static final String FUSIONSTOR = "fusionstor";

        private String installPath;
        private int deviceId;
        private String deviceType = FILE;
        private String volumeUuid;
        private boolean useVirtio;
        private String cacheMode = "none";

        public VolumeTO() {
        }

        public VolumeTO(VolumeTO other) {
            this.installPath = other.installPath;
            this.deviceId = other.deviceId;
            this.deviceType = other.deviceType;
            this.volumeUuid = other.volumeUuid;
            this.useVirtio = other.useVirtio;
            this.cacheMode = other.cacheMode;
        }

        public boolean isUseVirtio() {
            return useVirtio;
        }

        public void setUseVirtio(boolean useVirtio) {
            this.useVirtio = useVirtio;
        }

        public String getVolumeUuid() {
            return volumeUuid;
        }

        public void setVolumeUuid(String volumeUuid) {
            this.volumeUuid = volumeUuid;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getInstallPath() {
            return installPath;
        }
        public void setInstallPath(String installPath) {
            this.installPath = installPath;
        }
        public int getDeviceId() {
            return deviceId;
        }
        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        public String getCacheMode() {
            return cacheMode;
        }

        public void setCacheMode(String cacheMode) {
            this.cacheMode = cacheMode;
        }
    }
    
    public static class DetachDataVolumeCmd extends AgentCommand {
        private VolumeTO volume;
        private String vmInstanceUuid;
        public VolumeTO getVolume() {
            return volume;
        }
        public void setVolume(VolumeTO volume) {
            this.volume = volume;
        }
        public String getVmUuid() {
            return vmInstanceUuid;
        }
        public void setVmUuid(String vmInstanceUuid) {
            this.vmInstanceUuid = vmInstanceUuid;
        }
    }
    
    public static class DetachDataVolumeResponse extends AgentResponse {
    }
    
    public static class AttachDataVolumeCmd extends AgentCommand {
        private VolumeTO volume;
        private String vmInstanceUuid;
        private Map<String, Object> addons;

        public Map<String, Object> getAddons() {
            if (addons == null) {
                addons = new HashMap<String, Object>();
            }
            return addons;
        }

        public void setAddons(Map<String, Object> addons) {
            this.addons = addons;
        }

        public VolumeTO getVolume() {
            return volume;
        }

        public void setVolume(VolumeTO volume) {
            this.volume = volume;
        }

        public String getVmUuid() {
            return vmInstanceUuid;
        }

        public void setVmUuid(String vmInstanceUuid) {
            this.vmInstanceUuid = vmInstanceUuid;
        }
    }
    
    public static class AttachDataVolumeResponse extends AgentResponse {
    }

    public static class IsoTO {
        private String path;
        private String imageUuid;

        public IsoTO() {
        }

        public IsoTO(IsoTO other) {
            this.path = other.path;
            this.imageUuid = other.imageUuid;
        }


        public String getImageUuid() {
            return imageUuid;
        }

        public void setImageUuid(String imageUuid) {
            this.imageUuid = imageUuid;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class HardenVmConsoleCmd extends AgentCommand {
        public String vmUuid;
        public Long vmInternalId;
        public String hostManagementIp;
    }

    public static class DeleteVmConsoleFirewallCmd extends AgentCommand {
        public String vmUuid;
        public Long vmInternalId;
        public String hostManagementIp;
    }

    @ApiTimeout(apiClasses = {APICreateVmInstanceMsg.class})
    public static class StartVmCmd extends AgentCommand {
    	
		private String hostManagementIp;
         private String name;
        private String vmInstanceUuid;
        private String access_key_id;
        private String access_key_secret;
        private String region;
        private String image;
        private String size;
        private String auth;
        private String ex_security_group_id;
        private String ex_description;
        private String ex_internet_charge_type;
        private int ex_internet_max_bandwidth_out;
        private int ex_internet_max_bandwidth_in;
        
        private String ex_hostname;
        private String ex_io_optimized;
        private String ex_data_disks;
        private String ex_vswitch_id;
        private String ex_private_ip_address;
        private String ex_client_token;
        
        private Map ex_system_disk = new HashMap();

       
        
        public void setEx_system_disk(Map ex_system_disk) {
			this.ex_system_disk = ex_system_disk;
		}
		public String getHostManagementIp() {
     			return hostManagementIp;
     		}
     		public void setHostManagementIp(String hostManagementIp) {
     			this.hostManagementIp = hostManagementIp;
     		}
     		public String getName() {
     			return name;
     		}
     		public void setName(String name) {
     			this.name = name;
     		}
     		public String getVmInstanceUuid() {
     			return vmInstanceUuid;
     		}
     		public void setVmInstanceUuid(String vmInstanceUuid) {
     			this.vmInstanceUuid = vmInstanceUuid;
     		}
     		public String getAccess_key_id() {
     			return access_key_id;
     		}
     		public void setAccess_key_id(String access_key_id) {
     			this.access_key_id = access_key_id;
     		}
     		public String getAccess_key_secret() {
     			return access_key_secret;
     		}
     		public void setAccess_key_secret(String access_key_secret) {
     			this.access_key_secret = access_key_secret;
     		}
     		public String getRegion() {
     			return region ;
     		}
     		public void setRegion(String region) {
     			this.region = region;
     		}
     		public String getImage() {
     			return image;
     		}
     		public void setImage(String image) {
     			this.image = image;
     		}
     		public String getSize() {
     			return size;
     		}
     		public void setSize(String size) {
     			this.size = size;
     		}
     		public String getAuth() {
     			return auth;
     		}
     		public void setAuth(String auth) {
     			this.auth = auth;
     		}
     		public String getEx_security_group_id() {
     			return ex_security_group_id ;
     		}
     		public void setEx_security_group_id(String ex_security_group_id) {
     			this.ex_security_group_id = ex_security_group_id;
     		}
     		public String getEx_description() {
     			return ex_description;
     		}
     		public void setEx_description(String ex_description) {
     			this.ex_description = ex_description;
     		}
     		public String getEx_internet_charge_type() {
     			return ex_internet_charge_type;
     		}
     		public void setEx_internet_charge_type(String ex_internet_charge_type) {
     			this.ex_internet_charge_type = ex_internet_charge_type;
     		}
     		public int getEx_internet_max_bandwidth_out() {
     			return ex_internet_max_bandwidth_out;
     		}
     		public void setEx_internet_max_bandwidth_out(int ex_internet_max_bandwidth_out) {
     			this.ex_internet_max_bandwidth_out = ex_internet_max_bandwidth_out;
     		}
     		public int getEx_internet_max_bandwidth_in() {
     			return ex_internet_max_bandwidth_in;
     		}
     		public void setEx_internet_max_bandwidth_in(int ex_internet_max_bandwidth_in) {
     			this.ex_internet_max_bandwidth_in = ex_internet_max_bandwidth_in;
     		}
     		public String getEx_hostname() {
     			return ex_hostname;
     		}
     		public void setEx_hostname(String ex_hostname) {
     			this.ex_hostname = ex_hostname;
     		}
     		public String getEx_io_optimized() {
     			return ex_io_optimized;
     		}
     		public void setEx_io_optimized(String ex_io_optimized) {
     			this.ex_io_optimized = ex_io_optimized;
     		}
     		 
     		public String getEx_data_disks() {
     			return ex_data_disks;
     		}
     		public void setEx_data_disks(String ex_data_disks) {
     			this.ex_data_disks = ex_data_disks;
     		}
     		public String getEx_vswitch_id() {
     			return ex_vswitch_id;
     		}
     		public void setEx_vswitch_id(String ex_vswitch_id) {
     			this.ex_vswitch_id = ex_vswitch_id;
     		}
     		public String getEx_private_ip_address() {
     			return ex_private_ip_address;
     		}
     		public void setEx_private_ip_address(String ex_private_ip_address) {
     			this.ex_private_ip_address = ex_private_ip_address;
     		}
     		public String getEx_client_token() {
     			return ex_client_token;
     		}
     		public void setEx_client_token(String ex_client_token) {
     			this.ex_client_token = ex_client_token;
     		}
    }
    public static class StartVmPubResponse extends AgentResponse {
    	private String vmUuid;

		public String getVmUuid() {
			return vmUuid;
		}

		public void setVmUuid(String vmUuid) {
			this.vmUuid = vmUuid;
		}
 
    	
    }
    public static class OnlineChangeCpuMemoryCmd extends AgentCommand{
        private String vmUuid;
        private int cpuNum;
        private long memorySize;

        public void setVmUuid(String vmUuid){
            this.vmUuid = vmUuid;
        }
        public String getVmUuid(){
            return  vmUuid;
        }

        public void setCpuNum(int cpuNum){
            this.cpuNum = cpuNum;
        }

        public int getCpuNum(){
            return cpuNum;
        }

        public void setMemorySize(long memorySize){
            this.memorySize = memorySize;
        }

        public long getMemorySize(){
            return memorySize;
        }
    }
    public static class OnlineChangeCpuMemoryResponse extends AgentResponse{
        private int cpuNum;
        private long memorySize;

        public void setCpuNum(int cpuNum){
            this.cpuNum = cpuNum;
        }

        public int getCpuNum(){
            return cpuNum;
        }

        public void setMemorySize(long memorySize){
            this.memorySize = memorySize;
        }

        public long getMemorySize(){
            return memorySize;
        }
    }
    public static class GetVncPortCmd extends AgentCommand {
    	private String vmUuid;

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }
    }
    public static class GetVncPortResponse extends AgentResponse {
    	private int port;
        private String protocol;
		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
		
		public String getProtocol() {
                        return protocol;
                }

        public void setProtocol(String protocol) {
                        this.protocol = protocol;
                }
    }
    
    
    
    public static class GetPubVmCmd extends AgentCommand {
    	private String vmUuid;
    	 private String access_key_id;
         private String access_key_secret;
         
        public String getAccess_key_id() {
			return access_key_id;
		}

		public void setAccess_key_id(String access_key_id) {
			this.access_key_id = access_key_id;
		}

		public String getAccess_key_secret() {
			return access_key_secret;
		}

		public void setAccess_key_secret(String access_key_secret) {
			this.access_key_secret = access_key_secret;
		}

		public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }
    }
    
    public static class GetPubVmResponse extends AgentResponse {

    	private List<ECSNode> nodes;
    	
    	public List<ECSNode> getVms() {
    		return nodes;
    	}

    	public void setVms(List<ECSNode> nodes) {
    		this.nodes = nodes;
    	}

    	public GetPubVmResponse(){
    		nodes = new ArrayList<ECSNode>();
    	}
    }
    
    public static class StopVmCmd extends AgentCommand {
    	private String uuid;
        private String type;
        private String vmUuid;
        private String access_key_id;
        private String access_key_secret;
        private String region;
        private String force;
        
        
        
        public String getAccess_key_id() {
			return access_key_id;
		}
		public void setAccess_key_id(String access_key_id) {
			this.access_key_id = access_key_id;
		}
		public String getAccess_key_secret() {
			return access_key_secret;
		}
		public void setAccess_key_secret(String access_key_secret) {
			this.access_key_secret = access_key_secret;
		}
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public String getForce() {
			return force;
		}
		public void setForce(String force) {
			this.force = force;
		}
		
    	public String getVmUuid() {
			return vmUuid;
		}
		public void setVmUuid(String vmUuid) {
			this.vmUuid = vmUuid;
		}
		private long timeout;

		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public long getTimeout() {
			return timeout;
		}
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }
    public static class StopVmResponse extends AgentResponse {
    }
    
    public static class RebootVmCmd extends AgentCommand {
    	private String uuid;
        private String type;
        private String vmUuid;
        private String access_key_id;
        private String access_key_secret;
        private String region;
        private String force;
        
        
        
        public String getAccess_key_id() {
			return access_key_id;
		}
		public void setAccess_key_id(String access_key_id) {
			this.access_key_id = access_key_id;
		}
		public String getAccess_key_secret() {
			return access_key_secret;
		}
		public void setAccess_key_secret(String access_key_secret) {
			this.access_key_secret = access_key_secret;
		}
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public String getForce() {
			return force;
		}
		public void setForce(String force) {
			this.force = force;
		}
		
    	public String getVmUuid() {
			return vmUuid;
		}
		public void setVmUuid(String vmUuid) {
			this.vmUuid = vmUuid;
		}
		private long timeout;

		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public long getTimeout() {
			return timeout;
		}
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }
    public static class RebootVmResponse extends AgentResponse {
    }
    
    public static class DestroyVmCmd extends AgentCommand {
    	private String uuid;
        private String type;
        private String vmUuid;
        private String access_key_id;
        private String access_key_secret;
        private String region;
        private String force;
        
        
        
        public String getAccess_key_id() {
			return access_key_id;
		}
		public void setAccess_key_id(String access_key_id) {
			this.access_key_id = access_key_id;
		}
		public String getAccess_key_secret() {
			return access_key_secret;
		}
		public void setAccess_key_secret(String access_key_secret) {
			this.access_key_secret = access_key_secret;
		}
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public String getForce() {
			return force;
		}
		public void setForce(String force) {
			this.force = force;
		}
		
    	public String getVmUuid() {
			return vmUuid;
		}
		public void setVmUuid(String vmUuid) {
			this.vmUuid = vmUuid;
		}
		private long timeout;

		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public long getTimeout() {
			return timeout;
		}
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }
    public static class DestroyVmResponse extends AgentResponse {
    }
    
    
    public static class VmSyncCmd extends AgentCommand {
    }
    
    public static class VmSyncResponse extends AgentResponse {
        private HashMap<String, String> states;

        public HashMap<String, String> getStates() {
            return states;
        }
        public void setStates(HashMap<String, String> states) {
            this.states = states;
        }
    }
    
    public static class RefreshAllRulesOnHostCmd extends AgentCommand {
        private List<SecurityGroupRuleTO> ruleTOs;

        public List<SecurityGroupRuleTO> getRuleTOs() {
            return ruleTOs;
        }

        public void setRuleTOs(List<SecurityGroupRuleTO> ruleTOs) {
            this.ruleTOs = ruleTOs;
        }
    }
    
    public static class RefreshAllRulesOnHostResponse extends AgentResponse {
    }

    public static class CleanupUnusedRulesOnHostCmd extends AgentCommand {
    }
    public static class CleanupUnusedRulesOnHostResponse extends AgentResponse {
    }
    
    
    public static class ApplySecurityGroupRuleCmd extends AgentCommand {
        private List<SecurityGroupRuleTO> ruleTOs;

        public List<SecurityGroupRuleTO> getRuleTOs() {
            return ruleTOs;
        }

        public void setRuleTOs(List<SecurityGroupRuleTO> ruleTOs) {
            this.ruleTOs = ruleTOs;
        }
    }
    
    public static class ApplySecurityGroupRuleResponse extends AgentResponse {
    }

    public static class MigrateVmCmd extends AgentCommand {
        private String vmUuid;
        private String destHostIp;
        private String storageMigrationPolicy;
        private String srcHostIp;

        public String getSrcHostIp() {
            return srcHostIp;
        }

        public void setSrcHostIp(String srcHostIp) {
            this.srcHostIp = srcHostIp;
        }

        public String getStorageMigrationPolicy() {
            return storageMigrationPolicy;
        }

        public void setStorageMigrationPolicy(String storageMigrationPolicy) {
            this.storageMigrationPolicy = storageMigrationPolicy;
        }

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }

        public String getDestHostIp() {
            return destHostIp;
        }

        public void setDestHostIp(String destHostIp) {
            this.destHostIp = destHostIp;
        }
    }

    public static class MigrateVmResponse extends AgentResponse {
    }

    public static class MergeSnapshotRsp extends AgentResponse {
    }

    @ApiTimeout(apiClasses = {APIDeleteVolumeSnapshotMsg.class})
    public static class MergeSnapshotCmd extends AgentCommand {
        private String vmUuid;
        private int deviceId;
        private String srcPath;
        private String destPath;
        private boolean fullRebase;

        public boolean isFullRebase() {
            return fullRebase;
        }

        public void setFullRebase(boolean fullRebase) {
            this.fullRebase = fullRebase;
        }

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }

        public int getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        public String getSrcPath() {
            return srcPath;
        }

        public void setSrcPath(String srcPath) {
            this.srcPath = srcPath;
        }

        public String getDestPath() {
            return destPath;
        }

        public void setDestPath(String destPath) {
            this.destPath = destPath;
        }
    }

    @ApiTimeout(apiClasses = {APICreateVolumeSnapshotMsg.class})
    public static class TakeSnapshotCmd extends AgentCommand {
        private String vmUuid;
        private String volumeUuid;
        private int deviceId;
        private String installPath;
        private boolean fullSnapshot;
        private String volumeInstallPath;

        public String getVolumeUuid() {
            return volumeUuid;
        }

        public void setVolumeUuid(String volumeUuid) {
            this.volumeUuid = volumeUuid;
        }

        public String getVolumeInstallPath() {
            return volumeInstallPath;
        }

        public void setVolumeInstallPath(String volumeInstallPath) {
            this.volumeInstallPath = volumeInstallPath;
        }

        public boolean isFullSnapshot() {
            return fullSnapshot;
        }

        public void setFullSnapshot(boolean fullSnapshot) {
            this.fullSnapshot = fullSnapshot;
        }

        public String getVmUuid() {
            return vmUuid;
        }

        public void setVmUuid(String vmUuid) {
            this.vmUuid = vmUuid;
        }

        public int getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        public String getInstallPath() {
            return installPath;
        }

        public void setInstallPath(String installPath) {
            this.installPath = installPath;
        }
    }

    public static class TakeSnapshotResponse extends AgentResponse {
        @Validation
        private String newVolumeInstallPath;
        @Validation
        private String snapshotInstallPath;
        @Validation(notZero = true)
        private long size;

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getSnapshotInstallPath() {
            return snapshotInstallPath;
        }

        public void setSnapshotInstallPath(String snapshotInstallPath) {
            this.snapshotInstallPath = snapshotInstallPath;
        }

        public String getNewVolumeInstallPath() {
            return newVolumeInstallPath;
        }

        public void setNewVolumeInstallPath(String newVolumeInstallPath) {
            this.newVolumeInstallPath = newVolumeInstallPath;
        }
    }

    public static class LogoutIscsiTargetCmd extends AgentCommand {
        private String hostname;
        private int port;
        private String target;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    public static class LogoutIscsiTargetRsp extends AgentResponse {
    }

    public static class LoginIscsiTargetCmd extends AgentCommand {
        private String hostname;
        private int port;
        private String target;
        private String chapUsername;
        private String chapPassword;

        public String getChapUsername() {
            return chapUsername;
        }

        public void setChapUsername(String chapUsername) {
            this.chapUsername = chapUsername;
        }

        public String getChapPassword() {
            return chapPassword;
        }

        public void setChapPassword(String chapPassword) {
            this.chapPassword = chapPassword;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    public static class LoginIscsiTargetRsp extends AgentResponse {
    }

    public static class AttachIsoCmd extends AgentCommand {
        public IsoTO iso;
        public String vmUuid;
    }

    public static class AttachIsoRsp extends AgentResponse {
    }

    public static class DetachIsoCmd extends AgentCommand {
        public String vmUuid;
        public String isoUuid;
    }

    public static class DetachIsoRsp extends AgentResponse {

    }

    public static class ReportVmStateCmd {
        public String hostUuid;
        public String vmUuid;
        public String vmState;
    }

    public static class ReconnectMeCmd {
        public String hostUuid;
        public String reason;
    }
}
