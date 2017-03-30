package org.zstack.pubCloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;
import org.zstack.compute.host.HostGlobalConfig;
import org.zstack.core.CoreGlobalProperty;
import org.zstack.core.ansible.AnsibleFacade;
import org.zstack.core.cloudbus.*;
import org.zstack.core.componentloader.PluginRegistry;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.db.SimpleQuery;
import org.zstack.core.thread.AsyncThread;
import org.zstack.header.AbstractService;
import org.zstack.header.Component;
import org.zstack.header.host.*;
import org.zstack.header.managementnode.ManagementNodeReadyExtensionPoint;
import org.zstack.header.message.Message;
import org.zstack.header.message.MessageReply;
import org.zstack.header.message.NeedReplyMessage;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.rest.SyncHttpCallHandler;
import org.zstack.header.volume.MaxDataVolumeNumberExtensionPoint;
import org.zstack.pubCloud.ECSAgentCommands.ReconnectMeCmd;
import org.zstack.utils.CollectionUtils;
import org.zstack.utils.Utils;
import org.zstack.utils.function.Function;
import org.zstack.utils.logging.CLogger;
import org.zstack.xen.XenHostVO;

import java.util.ArrayList;
import java.util.List;

public class LocalHostFactory extends AbstractService implements HypervisorFactory, Component,
        ManagementNodeReadyExtensionPoint, MaxDataVolumeNumberExtensionPoint {
    private static final CLogger logger = Utils.getLogger(LocalHostFactory.class);
    public static final HypervisorType hypervisorType = new HypervisorType(PubCloudConstant.KVM_HYPERVISOR_TYPE);

    private List<KVMHostConnectExtensionPoint> connectExtensions = new ArrayList<KVMHostConnectExtensionPoint>();
    private int maxDataVolumeNum;

   

    @Autowired
    private DatabaseFacade dbf;
    @Autowired
    private PluginRegistry pluginRgty;
    @Autowired
    private AnsibleFacade asf;
    @Autowired
    private ResourceDestinationMaker destMaker;
    @Autowired
    private CloudBus bus;
    @Autowired
    private RESTFacade restf;

   

//    @Override
//    public Host getHost(HostVO vo) {
//        KVMHostVO kvo = dbf.findByUuid(vo.getUuid(), KVMHostVO.class);
//        KVMHostContext context = getHostContext(vo.getUuid());
//        if (context == null) {
//            context = createHostContext(kvo);
//        }
//        return new ECSHost(kvo, context);
//    }

    private List<String> getHostManagedByUs() {
//        int qun = 10000;
//        long amount = dbf.count(HostVO.class);
//        int times = (int)(amount / qun) + (amount % qun != 0 ? 1 : 0);
//        List<String> hostUuids = new ArrayList<String>();
//        int start = 0;
//        for (int i=0; i<times; i++) {
//            SimpleQuery<KVMHostVO> q = dbf.createQuery(KVMHostVO.class);
//            q.select(HostVO_.uuid);
//            // disconnected host will be handled by HostManager
//            q.add(HostVO_.status, SimpleQuery.Op.EQ, HostStatus.Connected);
//            q.setLimit(qun);
//            q.setStart(start);
//            List<String> lst = q.listValue();
//            start += qun;
//            for (String huuid : lst) {
//                if (!destMaker.isManagedByUs(huuid)) {
//                    continue;
//                }
//                hostUuids.add(huuid);
//            }
//        }

        return null;
    }
 

//    @Override
//    public HostInventory getHostInventory(HostVO vo) {
//        KVMHostVO kvo = vo instanceof KVMHostVO ? (KVMHostVO) vo : dbf.findByUuid(vo.getUuid(), KVMHostVO.class);
//        return KVMHostInventory.valueOf(kvo);
//    }

//    @Override
//    public HostInventory getHostInventory(String uuid) {
//        KVMHostVO vo = dbf.findByUuid(uuid, KVMHostVO.class);
//        return vo == null ? null : KVMHostInventory.valueOf(vo);
//    }

//   s


    private void deployAnsibleModule() {
        if (CoreGlobalProperty.UNIT_TEST_ON) {
            return;
        }

        asf.deployModule(PubCloudConstant.ANSIBLE_MODULE_PATH, PubCloudConstant.ANSIBLE_PLAYBOOK_NAME);
    }

    @Override
    public boolean start() {
        deployAnsibleModule();
//        populateExtensions();

//        maxDataVolumeNum = KVMGlobalConfig.MAX_DATA_VOLUME_NUM.value(int.class);
//        KVMGlobalConfig.MAX_DATA_VOLUME_NUM.installUpdateExtension(new GlobalConfigUpdateExtensionPoint() {
//            @Override
//            public void updateGlobalConfig(GlobalConfig oldConfig, GlobalConfig newConfig) {
//                maxDataVolumeNum = newConfig.value(int.class);
//            }
//        });
//        KVMGlobalConfig.RESERVED_MEMORY_CAPACITY.installValidateExtension(new GlobalConfigValidatorExtensionPoint() {
//            @Override
//            public void validateGlobalConfig(String category, String name, String oldValue, String value) throws GlobalConfigException {
//                if (!SizeUtils.isSizeString(value)) {
//                    throw new GlobalConfigException(String.format("%s only allows a size string. A size string is a number with suffix 'T/t/G/g/M/m/K/k/B/b' or without suffix, but got %s",
//                            KVMGlobalConfig.RESERVED_MEMORY_CAPACITY.getCanonicalName(), value));
//                }
//            }
//        });

        restf.registerSyncHttpCallHandler(PubCloudConstant.KVM_RECONNECT_ME, ReconnectMeCmd.class, new SyncHttpCallHandler<ReconnectMeCmd>() {
            @Override
            public String handleSyncHttpCall(ReconnectMeCmd cmd) {
                //TODO
                logger.warn(String.format("the kvm host[uuid:%s] asks the mgmt server to reconnect it for %s", cmd.hostUuid, cmd.reason));
                ReconnectHostMsg msg = new ReconnectHostMsg();
                msg.setHostUuid(cmd.hostUuid);
                bus.makeTargetServiceIdByResourceUuid(msg, HostConstant.SERVICE_ID, cmd.hostUuid);
                bus.send(msg);
                return null;
            }
        });

        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    public List<KVMHostConnectExtensionPoint> getConnectExtensions() {
        return connectExtensions;
    }

    ECSHostContext createHostContext() {
        UriComponentsBuilder ub = UriComponentsBuilder.newInstance();
        ub.scheme(PubCloudGlobalProperty.AGENT_URL_SCHEME);
        ub.host("127.0.0.1");
        ub.port(PubCloudGlobalProperty.AGENT_PORT);
        if (!"".equals(PubCloudGlobalProperty.AGENT_URL_ROOT_PATH)) {
            ub.path(PubCloudGlobalProperty.AGENT_URL_ROOT_PATH);
        }
        String baseUrl = ub.build().toUriString();

        ECSHostContext context = new ECSHostContext();
//        context.setInventory(KVMHostInventory.valueOf(vo));
        context.setBaseUrl(baseUrl);
        return context;
    }

    public ECSHostContext getHostContext(String hostUuid) {
//    	HostVO kvo = dbf.findByUuid(hostUuid, HostVO.class);
        return createHostContext();
    }

    @Override
    public String getHypervisorTypeForMaxDataVolumeNumberExtension() {
        return PubCloudConstant.KVM_HYPERVISOR_TYPE;
    }

    @Override
    public int getMaxDataVolumeNumber() {
        return maxDataVolumeNum;
    }

    @Override
    @AsyncThread
    public void managementNodeReady() {
        if (CoreGlobalProperty.UNIT_TEST_ON) {
            return;
        }

        if (!asf.isModuleChanged(PubCloudConstant.ANSIBLE_PLAYBOOK_NAME)) {
            return;
        }

        // KVM hosts need to deploy new agent
        // connect hosts even if they are ConnectionState is Connected

        List<String> hostUuids = getHostManagedByUs();
        if (hostUuids.isEmpty()) {
            return;
        }

        logger.debug(String.format("need to connect kvm hosts because kvm agent changed, uuids:%s", hostUuids));

        List<ConnectHostMsg> msgs = new ArrayList<ConnectHostMsg>();
        for (String huuid : hostUuids) {
            ConnectHostMsg msg = new ConnectHostMsg();
            msg.setNewAdd(false);
            msg.setUuid(huuid);
            bus.makeTargetServiceIdByResourceUuid(msg, HostConstant.SERVICE_ID, huuid);
            msgs.add(msg);
        }

        bus.send(msgs, HostGlobalConfig.HOST_LOAD_PARALLELISM_DEGREE.value(Integer.class), new CloudBusSteppingCallback() {
            @Override
            public void run(NeedReplyMessage msg, MessageReply reply) {
                ConnectHostMsg cmsg = (ConnectHostMsg)msg;
                if (!reply.isSuccess()) {
                    logger.warn(String.format("failed to connect kvm host[uuid:%s], %s", cmsg.getHostUuid(), reply.getError()));
                } else {
                    logger.debug(String.format("successfully to connect kvm host[uuid:%s]", cmsg.getHostUuid()));
                }
            }
        });
    }

    @Override
    @MessageSafe
    public void handleMessage(Message msg) {
        if (msg instanceof APIKvmRunShellMsg) {
            handle((APIKvmRunShellMsg) msg);
        } else {
            bus.dealWithUnknownMessage(msg);
        }
    }

    private void handle(final APIKvmRunShellMsg msg) {
        final APIKvmRunShellEvent evt = new APIKvmRunShellEvent(msg.getId());

        final List<KvmRunShellMsg> kmsgs = CollectionUtils.transformToList(msg.getHostUuids(), new Function<KvmRunShellMsg, String>() {
            @Override
            public KvmRunShellMsg call(String arg) {
                KvmRunShellMsg kmsg = new KvmRunShellMsg();
                kmsg.setHostUuid(arg);
                kmsg.setScript(msg.getScript());
                bus.makeTargetServiceIdByResourceUuid(kmsg, HostConstant.SERVICE_ID, arg);
                return kmsg;
            }
        });

        bus.send(kmsgs, new CloudBusListCallBack(msg) {
            @Override
            public void run(List<MessageReply> replies) {
                for (MessageReply r : replies) {
                    String hostUuid = kmsgs.get(replies.indexOf(r)).getHostUuid();

                    APIKvmRunShellEvent.ShellResult result = new APIKvmRunShellEvent.ShellResult();
                    if (!r.isSuccess()) {
                        result.setErrorCode(r.getError());
                    } else {
                        KvmRunShellReply kr = r.castReply();
                        result.setReturnCode(kr.getReturnCode());
                        result.setStderr(kr.getStderr());
                        result.setStdout(kr.getStdout());
                    }

                    evt.getInventory().put(hostUuid, result);
                }

                bus.publish(evt);
            }
        });
    }

    @Override
    public String getId() {
        return bus.makeLocalServiceId(PubCloudConstant.SERVICE_ID);
    }

//	@Override
	public HostVO createHost(HostVO vo, AddHostMessage msg) {
		// TODO Auto-generated method stub
		 APIAddLocalHostMsg amsg = (APIAddLocalHostMsg) msg;
		 HostVO kvo = new XenHostVO(vo);
//	        kvo.setUsername(amsg.getUsername());
//	        kvo.setPassword(amsg.getPassword());
//	        kvo.setPort(amsg.getSshPort());
	        return kvo;
	}

	 

	@Override
	public Host getHost(HostVO vo) {
        	ECSHostContext  context = createHostContext( );
        return   new ECSHost(vo, context);
	}

	@Override
	public HypervisorType getHypervisorType() {
		// TODO Auto-generated method stub
		return hypervisorType;
	}

	@Override
	public HostInventory getHostInventory(HostVO vo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HostInventory getHostInventory(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HostVO createHost(HostVO vo, APIAddHostMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}
}
