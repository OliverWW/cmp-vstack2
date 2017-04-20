package org.zstack.compute.vm;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.util.UriComponentsBuilder;
import org.zstack.core.ansible.AnsibleGlobalProperty;
import org.zstack.core.ansible.AnsibleRunner;
import org.zstack.core.ansible.SshFileMd5Checker;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.CloudBusCallBack;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.errorcode.ErrorFacade;
import org.zstack.core.logging.Log;
import org.zstack.header.allocator.*;
import org.zstack.header.configuration.DiskOfferingInventory;
import org.zstack.header.configuration.DiskOfferingVO;
import org.zstack.header.core.Completion;
import org.zstack.header.core.workflow.Flow;
import org.zstack.header.core.workflow.FlowRollback;
import org.zstack.header.core.workflow.FlowTrigger;
import org.zstack.header.core.workflow.NoRollbackFlow;
import org.zstack.header.errorcode.ErrorCode;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.host.HostInventory;
import org.zstack.header.identity.PubAccountEO;
import org.zstack.header.image.ImageConstant.ImageMediaType;
import org.zstack.header.image.ImageInventory;
import org.zstack.header.message.MessageReply;
import org.zstack.header.network.l3.L3NetworkInventory;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.vm.VmInstanceConstant;
import org.zstack.header.vm.VmInstanceConstant.VmOperation;
import org.zstack.header.vm.VmInstanceSpec;
import org.zstack.header.vm.VmInstanceVO;
import org.zstack.utils.CollectionUtils;
import org.zstack.utils.StringBind;
import org.zstack.utils.function.Function;
import org.zstack.utils.path.PathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Configurable(preConstruction = true, autowire = Autowire.BY_TYPE)
public class VmDeployProxytFlow implements Flow {
    @Autowired
    protected DatabaseFacade dbf;
    @Autowired
    protected CloudBus bus;
    @Autowired
    protected ErrorFacade errf;
    @Autowired
    private RESTFacade restf;

     
    
    @Override
    public void run(final FlowTrigger trigger, Map data) {
        String srcPath = PathUtil.findFileOnClassPath(String.format("ansible/aliyun/%s","aliyunagent-1.6.tar.gz"), true).getAbsolutePath();
        String destPath = String.format("/var/lib/zstack/aliyun/package/%s", "aliyunagent-1.6.tar.gz");
        SshFileMd5Checker checker = new SshFileMd5Checker();
        PubAccountEO eo = dbf.findByColumName(PubAccountEO.class, "cloudType", "LOCAL");
        checker.setUsername(eo.getUsername());
        checker.setPassword(eo.getPassword());
        checker.setSshPort(22);
        checker.setTargetIp("127.0.0.1");
        checker.addSrcDestPair(SshFileMd5Checker.ZSTACKLIB_SRC_PATH, String.format("/var/lib/zstack/pubCloud/package/%s", AnsibleGlobalProperty.ZSTACKLIB_PACKAGE_NAME));
        checker.addSrcDestPair(srcPath, destPath);

        AnsibleRunner runner = new AnsibleRunner();
        runner.installChecker(checker);
        runner.setAgentPort(7072);
        runner.setTargetIp("127.0.0.1");
        runner.setPlayBookName("aliyun");
        runner.setUsername(eo.getUsername());
        runner.setPassword(eo.getPassword());
        runner.setSshPort(22);
       
        runner.putArgument("pkg_pubCloud", "aliyunagent-1.6.tar.gz");
        String ManagementIP = "133.133.133.127";
        runner.putArgument("hostname", String.format("%s.zstack.org",ManagementIP.replaceAll("\\.", "-")));
//
//        UriComponentsBuilder ub = UriComponentsBuilder.fromHttpUrl(restf.getBaseUrl());
//        ub.path(new StringBind(PubCloudConstant.KVM_ANSIBLE_LOG_PATH_FROMAT).bind("uuid", self.getUuid()).toString());
//        String postUrl = ub.build().toString();
//
//        runner.putArgument("post_url", postUrl);
//        runner.run(new Completion(trigger) {
//            @Override
//            public void success() {
//            	trigger.next();
//            }
//
//            @Override
//            public void fail(ErrorCode errorCode) {
//            	trigger.fail(errorCode);
//            }
//        });
    }
    	
    	
    

    @Override
    public void rollback(FlowRollback chain, Map data) {
        VmInstanceSpec spec = (VmInstanceSpec) data.get(VmInstanceConstant.Params.VmInstanceSpec.toString());
        HostInventory host = spec.getDestHost();
        if (host != null) {
            ReturnHostCapacityMsg msg = new ReturnHostCapacityMsg();
            msg.setCpuCapacity(spec.getVmInventory().getCpuNum());
            msg.setMemoryCapacity(spec.getVmInventory().getMemorySize());
            msg.setHostUuid(host.getUuid());
            msg.setServiceId(bus.makeLocalServiceId(HostAllocatorConstant.SERVICE_ID));
            bus.send(msg);
        }
        chain.rollback();
    }
}
