package org.zstack.pubCloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.zstack.compute.host.HostBase;
import org.zstack.compute.host.HostSystemTags;
import org.zstack.compute.vm.VmSystemTags;
import org.zstack.core.CoreGlobalProperty;
import org.zstack.core.MessageCommandRecorder;
import org.zstack.core.Platform;
import org.zstack.core.ansible.AnsibleConstant;
import org.zstack.core.ansible.AnsibleGlobalProperty;
import org.zstack.core.ansible.AnsibleRunner;
import org.zstack.core.ansible.SshFileMd5Checker;
import org.zstack.core.db.SimpleQuery;
import org.zstack.core.db.SimpleQuery.Op;
import org.zstack.core.errorcode.ErrorFacade;
import org.zstack.core.logging.Log;
import org.zstack.core.thread.ChainTask;
import org.zstack.core.thread.SyncTaskChain;
import org.zstack.core.workflow.FlowChainBuilder;
import org.zstack.core.workflow.ShareFlow;
import org.zstack.header.core.AsyncLatch;
import org.zstack.header.configuration.InstanceOfferingInventory;
import org.zstack.header.core.Completion;
import org.zstack.header.core.NoErrorCompletion;
import org.zstack.header.core.workflow.*;
import org.zstack.header.errorcode.ErrorCode;
import org.zstack.header.errorcode.OperationFailureException;
import org.zstack.header.errorcode.SysErrors;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.host.*;
import org.zstack.header.host.MigrateVmOnHypervisorMsg.StorageMigrationPolicy;
import org.zstack.header.identity.PubAccountEO;
import org.zstack.header.image.ImagePlatform;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.Message;
import org.zstack.header.message.NeedReplyMessage;
import org.zstack.header.network.l2.*;
import org.zstack.header.rest.JsonAsyncRESTCallback;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.storage.snapshot.VolumeSnapshotInventory;
import org.zstack.header.vm.*;
import org.zstack.header.volume.VolumeInventory;
import org.zstack.pubCloud.ECSAgentCommands.BootDev;
import org.zstack.pubCloud.ECSAgentCommands.CheckPhysicalNetworkInterfaceCmd;
import org.zstack.pubCloud.ECSAgentCommands.CheckPhysicalNetworkInterfaceResponse;
import org.zstack.pubCloud.ECSAgentCommands.ConnectCmd;
import org.zstack.pubCloud.ECSAgentCommands.ConnectResponse;
import org.zstack.pubCloud.ECSAgentCommands.DestroyVmCmd;
import org.zstack.pubCloud.ECSAgentCommands.RebootVmCmd;
import org.zstack.pubCloud.ECSAgentCommands.RebootVmResponse;
import org.zstack.pubCloud.ECSAgentCommands.StartVmCmd;
import org.zstack.pubCloud.ECSAgentCommands.StartVmPubResponse;
import org.zstack.pubCloud.ECSAgentCommands.StopVmCmd;
import org.zstack.pubCloud.ECSAgentCommands.StopVmResponse;
import org.zstack.pubCloud.ECSAgentCommands.VolumeTO;
import org.zstack.utils.*;
import org.zstack.utils.gson.JSONList;
import org.zstack.utils.gson.JSONObjectUtil;
import org.zstack.utils.logging.CLogger;
import org.zstack.utils.path.PathUtil;
import org.zstack.utils.ssh.Ssh;
import org.zstack.utils.ssh.SshResult;

import javax.persistence.TypedQuery;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.zstack.utils.CollectionDSL.e;
import static org.zstack.utils.CollectionDSL.map;

public class ECSHost extends HostBase implements Host {
	private static final CLogger logger = Utils.getLogger(ECSHost.class);

	@Autowired
	private RESTFacade restf;

	@Autowired
	private ErrorFacade errf;

	private ECSHostContext context;
	@Autowired
	private LocalHostFactory factory;
	// ///////////////////// REST URL //////////////////////////
	private String baseUrl;
	private String connectPath;
	private String pingPath;
	private String checkPhysicalNetworkInterfacePath;
	private String startVmPath;
	private String getPubVmPath;
	private String stopVmPath;
	private String rebootVmPath;
	private String destroyVmPath;
	private String attachDataVolumePath;
	private String detachDataVolumePath;
	private String echoPath;
	private String attachNicPath;
	private String detachNicPath;
	private String migrateVmPath;
	private String snapshotPath;
	private String mergeSnapshotPath;
	private String hostFactPath;
	private String attachIsoPath;
	private String detachIsoPath;
	private String checkVmStatePath;
	private String getConsolePortPath;
	private String changeCpuMemoryPath;
	private String deleteConsoleFirewall;

	private String agentPackageName = PubCloudGlobalProperty.AGENT_PACKAGE_NAME;

	public ECSHost(HostVO ecsvo, ECSHostContext context2) {
		super(ecsvo);

		this.context = context2;
		baseUrl = context2.getBaseUrl();

		UriComponentsBuilder ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_CONNECT_PATH);
		connectPath = ub.build().toUriString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_PING_PATH);
		pingPath = ub.build().toUriString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_CHECK_PHYSICAL_NETWORK_INTERFACE_PATH);
		checkPhysicalNetworkInterfacePath = ub.build().toUriString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_START_VM_PATH);
		startVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_GET_PUB_VM_PATH);
		getPubVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_STOP_VM_PATH);
		stopVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_REBOOT_VM_PATH);
		rebootVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_DESTROY_VM_PATH);
		destroyVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_ATTACH_VOLUME);
		attachDataVolumePath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_DETACH_VOLUME);
		detachDataVolumePath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_ECHO_PATH);
		echoPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_ATTACH_NIC_PATH);
		attachNicPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_DETACH_NIC_PATH);
		detachNicPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_MIGRATE_VM_PATH);
		migrateVmPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_TAKE_VOLUME_SNAPSHOT_PATH);
		snapshotPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_MERGE_SNAPSHOT_PATH);
		mergeSnapshotPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_HOST_FACT_PATH);
		hostFactPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_ATTACH_ISO_PATH);
		attachIsoPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_DETACH_ISO_PATH);
		detachIsoPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_VM_CHECK_STATE);
		checkVmStatePath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_GET_VNC_PORT_PATH);
		getConsolePortPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_VM_CHANGE_CPUMEMORY);
		changeCpuMemoryPath = ub.build().toString();

		ub = UriComponentsBuilder.fromHttpUrl(baseUrl);
		ub.path(PubCloudConstant.KVM_DELETE_CONSOLE_FIREWALL_PATH);
		deleteConsoleFirewall = ub.build().toString();
	}

	@Override
	protected void handleApiMessage(APIMessage msg) {
		super.handleApiMessage(msg);
	}

	@Override
	protected void handleLocalMessage(Message msg) {
		if (msg instanceof CheckNetworkPhysicalInterfaceMsg) {
			handle((CheckNetworkPhysicalInterfaceMsg) msg);
		} else if (msg instanceof CreateVmOnLocalMsg) {
			handle((CreateVmOnLocalMsg) msg);
		}   else if (msg instanceof StopVmPubOnLocalMsg) {
			handle((StopVmPubOnLocalMsg) msg);
		} else if (msg instanceof RebootVmPubOnLocalMsg) {
			handle((RebootVmPubOnLocalMsg) msg);
		} else if (msg instanceof DeleteVmPubOnLocalMsg) {
			handle((DeleteVmPubOnLocalMsg) msg);
		} else if (msg instanceof StartVmPubOnLocalMsg) {
			handle((StartVmPubOnLocalMsg) msg);
		} else {
			super.handleLocalMessage(msg);
		}
	}

//	private void directlyDestroy(final VmDirectlyDestroyOnHypervisorMsg msg, final NoErrorCompletion completion) {
//		checkStatus();
//
//		final VmDirectlyDestroyOnHypervisorReply reply = new VmDirectlyDestroyOnHypervisorReply();
//		DestroyVmCmd cmd = new DestroyVmCmd();
//		cmd.setUuid(msg.getVmUuid());
//		restf.asyncJsonPost(destroyVmPath, cmd, new JsonAsyncRESTCallback<DestroyVmResponse>(msg, completion) {
//			@Override
//			public void fail(ErrorCode err) {
//				reply.setError(err);
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public void success(DestroyVmResponse ret) {
//				if (!ret.isSuccess()) {
//					reply.setError(
//							errf.instantiateErrorCode(HostErrors.FAILED_TO_DESTROY_VM_ON_HYPERVISOR, ret.getError()));
//				}
//
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public Class<DestroyVmResponse> getReturnClass() {
//				return DestroyVmResponse.class;
//			}
//		});
//	}

//	private void handle(final VmDirectlyDestroyOnHypervisorMsg msg) {
//		thdf.chainSubmit(new ChainTask(msg) {
//			@Override
//			public String getSyncSignature() {
//				return id;
//			}
//
//			@Override
//			public void run(final SyncTaskChain chain) {
//				directlyDestroy(msg, new NoErrorCompletion(chain) {
//					@Override
//					public void done() {
//						chain.next();
//					}
//				});
//			}
//
//			@Override
//			public String getName() {
//				return String.format("directly-delete-vm-%s-msg-on-kvm-%s", msg.getVmUuid(), self.getUuid());
//			}
//		});
//	}

	private SshResult runShell(String script) {
		Ssh ssh = new Ssh();
		ssh.setHostname(self.getManagementIp());
		ssh.setPort(22);
		PubAccountEO eo = dbf.findByColumName(PubAccountEO.class, "cloudType", "LOCAL");
		ssh.setUsername(eo.getUsername());
		ssh.setPassword(eo.getPassword());
		ssh.shell(script);
		return ssh.runAndClose();
	}

//	private void handle(final OnlineChangeVmCpuMemoryMsg msg) {
//		final OnlineChangeVmCpuMemoryReply reply = new OnlineChangeVmCpuMemoryReply();
//
//		OnlineChangeCpuMemoryCmd cmd = new OnlineChangeCpuMemoryCmd();
//		cmd.setVmUuid(msg.getVmInstanceUuid());
//		cmd.setCpuNum(msg.getInstanceOfferingInventory().getCpuNum());
//		cmd.setMemorySize(msg.getInstanceOfferingInventory().getMemorySize());
//		restf.asyncJsonPost(changeCpuMemoryPath, cmd, new JsonAsyncRESTCallback<OnlineChangeCpuMemoryResponse>(msg) {
//			@Override
//			public void fail(ErrorCode err) {
//				reply.setError(err);
//				bus.reply(msg, reply);
//			}
//
//			@Override
//			public void success(OnlineChangeCpuMemoryResponse ret) {
//				if (!ret.isSuccess()) {
//					reply.setError(errf.stringToOperationError(ret.getError()));
//				} else {
//					InstanceOfferingInventory inventory = new InstanceOfferingInventory();
//					inventory.setCpuNum(ret.getCpuNum());
//					inventory.setMemorySize(ret.getMemorySize());
//					reply.setInstanceOfferingInventory(inventory);
//
//				}
//				bus.reply(msg, reply);
//			}
//
//			@Override
//			public Class<OnlineChangeCpuMemoryResponse> getReturnClass() {
//				return OnlineChangeCpuMemoryResponse.class;
//			}
//		});
//	}

//	private void handle(final GetVmConsoleAddressFromHostMsg msg) {
//		final GetVmConsoleAddressFromHostReply reply = new GetVmConsoleAddressFromHostReply();
//
//		GetVncPortCmd cmd = new GetVncPortCmd();
//		cmd.setVmUuid(msg.getVmInstanceUuid());
//		restf.asyncJsonPost(getConsolePortPath, cmd, new JsonAsyncRESTCallback<GetVncPortResponse>() {
//			@Override
//			public void fail(ErrorCode err) {
//				reply.setError(err);
//				bus.reply(msg, reply);
//			}
//
//			@Override
//			public void success(GetVncPortResponse ret) {
//				if (!ret.isSuccess()) {
//					reply.setError(errf.stringToOperationError(ret.getError()));
//				} else {
//					reply.setHostIp(self.getManagementIp());
//					reply.setProtocol(ret.getProtocol());
//					reply.setPort(ret.getPort());
//				}
//				bus.reply(msg, reply);
//			}
//
//			@Override
//			public Class<GetVncPortResponse> getReturnClass() {
//				return GetVncPortResponse.class;
//			}
//		});
//	}

	private void handle(final KVMHostSyncHttpCallMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				executeSyncHttpCall(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("execute-sync-http-call-on-kvm-host-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private void executeSyncHttpCall(KVMHostSyncHttpCallMsg msg, NoErrorCompletion completion) {
		if (!msg.isNoStatusCheck()) {
			checkStatus();
		}
		String url = buildUrl(msg.getPath());
		MessageCommandRecorder.record(msg.getCommandClassName());
		LinkedHashMap rsp = restf.syncJsonPost(url, msg.getCommand(), LinkedHashMap.class);
		KVMHostSyncHttpCallReply reply = new KVMHostSyncHttpCallReply();
		reply.setResponse(rsp);
		bus.reply(msg, reply);
		completion.done();
	}

	private void handle(final KVMHostAsyncHttpCallMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				executeAsyncHttpCall(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("execute-async-http-call-on-kvm-host-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private String buildUrl(String path) {
		UriComponentsBuilder ub = UriComponentsBuilder.newInstance();
		ub.scheme(PubCloudGlobalProperty.AGENT_URL_SCHEME);
		ub.host(self.getManagementIp());
		ub.port(PubCloudGlobalProperty.AGENT_PORT);
		if (!"".equals(PubCloudGlobalProperty.AGENT_URL_ROOT_PATH)) {
			ub.path(PubCloudGlobalProperty.AGENT_URL_ROOT_PATH);
		}
		ub.path(path);
		return ub.build().toUriString();
	}

	private void executeAsyncHttpCall(final KVMHostAsyncHttpCallMsg msg, final NoErrorCompletion completion) {
		if (!msg.isNoStatusCheck()) {
			checkStatus();
		}

		String url = buildUrl(msg.getPath());
		MessageCommandRecorder.record(msg.getCommandClassName());
		restf.asyncJsonPost(url, msg.getCommand(), new JsonAsyncRESTCallback<LinkedHashMap>(msg, completion) {
			@Override
			public void fail(ErrorCode err) {
				KVMHostAsyncHttpCallReply reply = new KVMHostAsyncHttpCallReply();
				if (err.isError(SysErrors.HTTP_ERROR, SysErrors.IO_ERROR)) {
					reply.setError(errf.instantiateErrorCode(HostErrors.OPERATION_FAILURE_GC_ELIGIBLE,
							"cannot do the operation on the KVM host", err));
				} else {
					reply.setError(reply.getError());
				}

				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public void success(LinkedHashMap ret) {
				KVMHostAsyncHttpCallReply reply = new KVMHostAsyncHttpCallReply();
				reply.setResponse(ret);
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public Class<LinkedHashMap> getReturnClass() {
				return LinkedHashMap.class;
			}
		}, TimeUnit.SECONDS, msg.getCommandTimeout());
	}
//
//	private void handle(final MergeVolumeSnapshotOnKvmMsg msg) {
//		thdf.chainSubmit(new ChainTask(msg) {
//			@Override
//			public String getSyncSignature() {
//				return id;
//			}
//
//			@Override
//			public void run(final SyncTaskChain chain) {
//				mergeVolumeSnapshot(msg, new NoErrorCompletion(chain) {
//					@Override
//					public void done() {
//						chain.next();
//					}
//				});
//			}
//
//			@Override
//			public String getName() {
//				return String.format("merge-volume-snapshot-on-kvm-%s", self.getUuid());
//			}
//
//			@Override
//			protected int getSyncLevel() {
//				return getHostSyncLevel();
//			}
//		});
//	}

//	private void mergeVolumeSnapshot(final MergeVolumeSnapshotOnKvmMsg msg, final NoErrorCompletion completion) {
//		checkStateAndStatus();
//
//		final MergeVolumeSnapshotOnKvmReply reply = new MergeVolumeSnapshotOnKvmReply();
//
//		VolumeInventory volume = msg.getTo();
//
//		if (volume.getVmInstanceUuid() != null) {
//			SimpleQuery<VmInstanceVO> q = dbf.createQuery(VmInstanceVO.class);
//			q.select(VmInstanceVO_.state);
//			q.add(VmInstanceVO_.uuid, Op.EQ, volume.getVmInstanceUuid());
//			VmInstanceState state = q.findValue();
//			if (state != VmInstanceState.Stopped && state != VmInstanceState.Running) {
//				throw new OperationFailureException(errf.stringToOperationError(String.format(
//						"cannot do volume snapshot merge when vm[uuid:%s] is in state of %s. The operation is only allowed when vm is Running or Stopped",
//						volume.getUuid(), state)));
//			}
//
//			if (state == VmInstanceState.Running) {
//				String libvirtVersion = KVMSystemTags.LIBVIRT_VERSION.getTokenByResourceUuid(self.getUuid(),
//						KVMSystemTags.LIBVIRT_VERSION_TOKEN);
//				if (new VersionComparator(PubCloudConstant.MIN_LIBVIRT_LIVE_BLOCK_COMMIT_VERSION)
//						.compare(libvirtVersion) > 0) {
//					throw new OperationFailureException(errf.stringToOperationError(String.format(
//							"live volume snapshot merge needs libvirt version greater than %s, current libvirt version is %s. Please stop vm and redo the operation or detach the volume if it's data volume",
//							PubCloudConstant.MIN_LIBVIRT_LIVE_BLOCK_COMMIT_VERSION, libvirtVersion)));
//				}
//			}
//		}
//
//		VolumeSnapshotInventory snapshot = msg.getFrom();
//		MergeSnapshotCmd cmd = new MergeSnapshotCmd();
//		cmd.setFullRebase(msg.isFullRebase());
//		cmd.setDestPath(volume.getInstallPath());
//		cmd.setSrcPath(snapshot.getPrimaryStorageInstallPath());
//		cmd.setVmUuid(volume.getVmInstanceUuid());
//		cmd.setDeviceId(volume.getDeviceId());
//		restf.asyncJsonPost(mergeSnapshotPath, cmd, new JsonAsyncRESTCallback<MergeSnapshotRsp>(msg, completion) {
//			@Override
//			public void fail(ErrorCode err) {
//				reply.setError(err);
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public void success(MergeSnapshotRsp ret) {
//				if (!ret.isSuccess()) {
//					reply.setError(errf.stringToOperationError(ret.getError()));
//				}
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public Class<MergeSnapshotRsp> getReturnClass() {
//				return MergeSnapshotRsp.class;
//			}
//		});
//	}
//
//	private void handle(final TakeSnapshotOnHypervisorMsg msg) {
//		thdf.chainSubmit(new ChainTask(msg) {
//			@Override
//			public String getSyncSignature() {
//				return id;
//			}
//
//			@Override
//			public void run(final SyncTaskChain chain) {
//				takeSnapshot(msg, new NoErrorCompletion(chain) {
//					@Override
//					public void done() {
//						chain.next();
//					}
//				});
//			}
//
//			@Override
//			public String getName() {
//				return String.format("take-snapshot-on-kvm-%s", self.getUuid());
//			}
//
//			@Override
//			protected int getSyncLevel() {
//				return getHostSyncLevel();
//			}
//		});
//	}

//	private void takeSnapshot(final TakeSnapshotOnHypervisorMsg msg, final NoErrorCompletion completion) {
//		checkStateAndStatus();
//
//		final TakeSnapshotOnHypervisorReply reply = new TakeSnapshotOnHypervisorReply();
//		TakeSnapshotCmd cmd = new TakeSnapshotCmd();
//
//		if (msg.getVmUuid() != null) {
//			SimpleQuery<VmInstanceVO> q = dbf.createQuery(VmInstanceVO.class);
//			q.select(VmInstanceVO_.state);
//			q.add(VmInstanceVO_.uuid, SimpleQuery.Op.EQ, msg.getVmUuid());
//			VmInstanceState vmState = q.findValue();
//			if (vmState != VmInstanceState.Running && vmState != VmInstanceState.Stopped) {
//				throw new OperationFailureException(errf.stringToOperationError(String
//						.format("vm[uuid:%s] is not Running or Stopped, current state[%s]", msg.getVmUuid(), vmState)));
//			}
//
//			if (!HostSystemTags.LIVE_SNAPSHOT.hasTag(self.getUuid())) {
//				if (vmState != VmInstanceState.Stopped) {
//					throw new OperationFailureException(errf.instantiateErrorCode(SysErrors.NO_CAPABILITY_ERROR,
//							String.format(
//									"kvm host[uuid:%s, name:%s, ip:%s] doesn't not support live snapshot. please stop vm[uuid:%s] and try again",
//									self.getUuid(), self.getName(), self.getManagementIp(), msg.getVmUuid())));
//				}
//			}
//
//			cmd.setVolumeUuid(msg.getVolume().getUuid());
//			cmd.setVmUuid(msg.getVmUuid());
//			cmd.setDeviceId(msg.getVolume().getDeviceId());
//		}
//
//		cmd.setVolumeInstallPath(msg.getVolume().getInstallPath());
//		cmd.setInstallPath(msg.getInstallPath());
//		cmd.setFullSnapshot(msg.isFullSnapshot());
//		restf.asyncJsonPost(snapshotPath, cmd, new JsonAsyncRESTCallback<TakeSnapshotResponse>(msg, completion) {
//			@Override
//			public void fail(ErrorCode err) {
//				reply.setError(err);
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public void success(TakeSnapshotResponse ret) {
//				if (ret.isSuccess()) {
//					reply.setNewVolumeInstallPath(ret.getNewVolumeInstallPath());
//					reply.setSnapshotInstallPath(ret.getSnapshotInstallPath());
//					reply.setSize(ret.getSize());
//				} else {
//					reply.setError(errf.stringToOperationError(ret.getError()));
//				}
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public Class<TakeSnapshotResponse> getReturnClass() {
//				return TakeSnapshotResponse.class;
//			}
//		});
//	}

//	private void migrateVm(final Iterator<MigrateStruct> it, final Completion completion) {
//		final String hostIp;
//		final String vmUuid;
//		final StorageMigrationPolicy storageMigrationPolicy;
//		synchronized (it) {
//			if (!it.hasNext()) {
//				completion.success();
//				return;
//			}
//
//			MigrateStruct s = it.next();
//			vmUuid = s.vmUuid;
//			hostIp = s.dstHostIp;
//			storageMigrationPolicy = s.storageMigrationPolicy;
//		}
//
//		SimpleQuery<VmInstanceVO> q = dbf.createQuery(VmInstanceVO.class);
//		q.select(VmInstanceVO_.internalId);
//		q.add(VmInstanceVO_.uuid, Op.EQ, vmUuid);
//		final Long vmInternalId = q.findValue();
//
//		FlowChain chain = FlowChainBuilder.newShareFlowChain();
//		chain.setName(String.format("migrate-vm-%s-on-kvm-host-%s", vmUuid, self.getUuid()));
//		chain.then(new ShareFlow() {
//			@Override
//			public void setup() {
//				flow(new NoRollbackFlow() {
//					String __name__ = "migrate-vm";
//
//					@Override
//					public void run(final FlowTrigger trigger, Map data) {
//						MigrateVmCmd cmd = new MigrateVmCmd();
//						cmd.setDestHostIp(hostIp);
//						cmd.setSrcHostIp(self.getManagementIp());
//						cmd.setStorageMigrationPolicy(
//								storageMigrationPolicy == null ? null : storageMigrationPolicy.toString());
//						cmd.setVmUuid(vmUuid);
//						restf.asyncJsonPost(migrateVmPath, cmd, new JsonAsyncRESTCallback<MigrateVmResponse>(trigger) {
//							@Override
//							public void fail(ErrorCode err) {
//								completion.fail(err);
//							}
//
//							@Override
//							public void success(MigrateVmResponse ret) {
//								if (!ret.isSuccess()) {
//									ErrorCode err = errf.instantiateErrorCode(
//											HostErrors.FAILED_TO_MIGRATE_VM_ON_HYPERVISOR,
//											String.format(
//													"failed to migrate vm[uuid:%s] from kvm host[uuid:%s, ip:%s] to dest host[ip:%s], %s",
//													vmUuid, self.getUuid(), self.getManagementIp(), hostIp,
//													ret.getError()));
//
//									trigger.fail(err);
//								} else {
//									String info = String.format(
//											"successfully migrated vm[uuid:%s] from kvm host[uuid:%s, ip:%s] to dest host[ip:%s]",
//											vmUuid, self.getUuid(), self.getManagementIp(), hostIp);
//									logger.debug(info);
//
//									trigger.next();
//								}
//							}
//
//							@Override
//							public Class<MigrateVmResponse> getReturnClass() {
//								return MigrateVmResponse.class;
//							}
//						});
//					}
//				});
//
//				flow(new NoRollbackFlow() {
//					String __name__ = "harden-vm-console-on-dst-host";
//
//					@Override
//					public void run(final FlowTrigger trigger, Map data) {
//						HardenVmConsoleCmd cmd = new HardenVmConsoleCmd();
//						cmd.vmInternalId = vmInternalId;
//						cmd.vmUuid = vmUuid;
//						cmd.hostManagementIp = hostIp;
//
//						UriComponentsBuilder ub = UriComponentsBuilder.newInstance();
//						ub.scheme(PubCloudGlobalProperty.AGENT_URL_SCHEME);
//						ub.host(hostIp);
//						ub.port(PubCloudGlobalProperty.AGENT_PORT);
//						ub.path(PubCloudConstant.KVM_HARDEN_CONSOLE_PATH);
//						String url = ub.build().toString();
//
//						restf.asyncJsonPost(url, cmd, new JsonAsyncRESTCallback<AgentResponse>(trigger) {
//							@Override
//							public void fail(ErrorCode err) {
//								// TODO
//								logger.warn(String.format("failed to harden VM[uuid:%s]'s console, %s", vmUuid, err));
//								trigger.next();
//							}
//
//							@Override
//							public void success(AgentResponse ret) {
//								if (!ret.isSuccess()) {
//									// TODO
//									logger.warn(String.format("failed to harden VM[uuid:%s]'s console, %s", vmUuid,
//											ret.getError()));
//								}
//
//								trigger.next();
//							}
//
//							@Override
//							public Class<AgentResponse> getReturnClass() {
//								return AgentResponse.class;
//							}
//						});
//					}
//				});
//
//				flow(new NoRollbackFlow() {
//					String __name__ = "delete-vm-console-firewall-on-source-host";
//
//					@Override
//					public void run(final FlowTrigger trigger, Map data) {
//						DeleteVmConsoleFirewallCmd cmd = new DeleteVmConsoleFirewallCmd();
//						cmd.vmInternalId = vmInternalId;
//						cmd.vmUuid = vmUuid;
//						cmd.hostManagementIp = self.getManagementIp();
//
//						restf.asyncJsonPost(deleteConsoleFirewall, cmd,
//								new JsonAsyncRESTCallback<AgentResponse>(trigger) {
//									@Override
//									public void fail(ErrorCode err) {
//										// TODO
//										logger.warn(String.format(
//												"failed to delete console firewall rule for the vm[uuid:%s] on"
//														+ " the source host[uuid:%s, ip:%s], %s",
//												vmUuid, self.getUuid(), self.getManagementIp(), err));
//										trigger.next();
//									}
//
//									@Override
//									public void success(AgentResponse ret) {
//										if (!ret.isSuccess()) {
//											logger.warn(String.format(
//													"failed to delete console firewall rule for the vm[uuid:%s] on"
//															+ " the source host[uuid:%s, ip:%s], %s",
//													vmUuid, self.getUuid(), self.getManagementIp(), ret.getError()));
//										}
//
//										trigger.next();
//									}
//
//									@Override
//									public Class<AgentResponse> getReturnClass() {
//										return AgentResponse.class;
//									}
//								});
//					}
//				});
//
//				done(new FlowDoneHandler(completion) {
//					@Override
//					public void handle(Map data) {
//						String info = String.format(
//								"successfully migrated vm[uuid:%s] from kvm host[uuid:%s, ip:%s] to dest host[ip:%s]",
//								vmUuid, self.getUuid(), self.getManagementIp(), hostIp);
//						logger.debug(info);
//
//						migrateVm(it, completion);
//					}
//				});
//
//				error(new FlowErrorHandler(completion) {
//					@Override
//					public void handle(ErrorCode errCode, Map data) {
//						completion.fail(errCode);
//					}
//				});
//			}
//		}).start();
//	}

//	private void handle(final MigrateVmOnHypervisorMsg msg) {
//		thdf.chainSubmit(new ChainTask(msg) {
//			@Override
//			public String getSyncSignature() {
//				return id;
//			}
//
//			@Override
//			public void run(final SyncTaskChain chain) {
//				migrateVm(msg, new NoErrorCompletion(chain) {
//					@Override
//					public void done() {
//						chain.next();
//					}
//				});
//			}
//
//			@Override
//			public String getName() {
//				return String.format("migrate-vm-on-kvm-%s", self.getUuid());
//			}
//
//			@Override
//			protected int getSyncLevel() {
//				return getHostSyncLevel();
//			}
//		});
//	}
//
//	class MigrateStruct {
//		String vmUuid;
//		String dstHostIp;
//		StorageMigrationPolicy storageMigrationPolicy;
//	}

//	private void migrateVm(final MigrateVmOnHypervisorMsg msg, final NoErrorCompletion completion) {
//		checkStatus();
//
//		List<MigrateStruct> lst = new ArrayList<MigrateStruct>();
//		MigrateStruct s = new MigrateStruct();
//		s.vmUuid = msg.getVmInventory().getUuid();
//		s.dstHostIp = msg.getDestHostInventory().getManagementIp();
//		s.storageMigrationPolicy = msg.getStorageMigrationPolicy();
//		lst.add(s);
//		final MigrateVmOnHypervisorReply reply = new MigrateVmOnHypervisorReply();
//		migrateVm(lst.iterator(), new Completion(msg, completion) {
//			@Override
//			public void success() {
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public void fail(ErrorCode errorCode) {
//				reply.setError(errorCode);
//				bus.reply(msg, reply);
//				completion.done();
//			}
//		});
//	}

	private void handle(final DeleteVmPubOnLocalMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				destroyVm(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("destroy-vm-on-kvm-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private void handle(final StartVmPubOnLocalMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				startVm(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("destroy-vm-on-kvm-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private void destroyVm(final DeleteVmPubOnLocalMsg msg, final NoErrorCompletion completion) {

		DestroyVmCmd cmd = new DestroyVmCmd();
		cmd.setUuid(msg.getvMUuid());
		cmd.setVmUuid(msg.getvMUuid());
		cmd.setTimeout(120);
		cmd.setAccess_key_id(msg.getAccess_key_id());
		cmd.setAccess_key_secret(msg.getAccess_key_secret());
		cmd.setRegion(msg.getRegion());
		cmd.setForce("False");

		restf.asyncJsonPost(destroyVmPath, cmd, new JsonAsyncRESTCallback<StopVmResponse>(msg, completion) {
			@Override
			public void fail(ErrorCode err) {
				DestroyVmOnHypervisorReply reply = new DestroyVmOnHypervisorReply();
				if (err.isError(SysErrors.IO_ERROR, SysErrors.HTTP_ERROR)) {
					err = errf.instantiateErrorCode(HostErrors.OPERATION_FAILURE_GC_ELIGIBLE, "unable to stop a vm",
							err);
				}

				reply.setError(err);
				// extEmitter.stopVmOnKvmFailed(KVMHostInventory.valueOf(getSelf()),
				// vminv, err);
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public void success(StopVmResponse ret) {
				StopVmOnHypervisorReply reply = new StopVmOnHypervisorReply();
				if (!ret.isSuccess()) {
					String err = String.format("unable to stop vm[uuid:%s ] on kvm host[uuid:%s, ip:%s], because %s",
							msg.getId(), self.getUuid(), self.getManagementIp(), ret.getError());
					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_STOP_VM_ON_HYPERVISOR, err));
					logger.warn(err);
					// extEmitter.stopVmOnKvmFailed(KVMHostInventory.valueOf(getSelf()),
					// vminv, reply.getError());
				} else {
					// extEmitter.stopVmOnKvmSuccess(KVMHostInventory.valueOf(getSelf()),
					// vminv);
				}
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public Class<StopVmResponse> getReturnClass() {
				return StopVmResponse.class;
			}

		});

	}

	private void handle(final RebootVmPubOnLocalMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				rebootVmPub(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("reboot-vm-on-kvm-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});

	}

	private List<String> toKvmBootDev(List<String> order) {
		List<String> ret = new ArrayList<String>();
		for (String o : order) {
			if (VmBootDevice.HardDisk.toString().equals(o)) {
				ret.add(BootDev.hd.toString());
			} else if (VmBootDevice.CdRom.toString().equals(o)) {
				ret.add(BootDev.cdrom.toString());
			} else {
				throw new CloudRuntimeException(String.format("unknown boot device[%s]", o));
			}
		}

		return ret;
	}

	private void rebootVmPub(final RebootVmPubOnLocalMsg msg, final NoErrorCompletion completion) {

		RebootVmCmd cmd = new RebootVmCmd();
		long timeout = TimeUnit.MILLISECONDS.toSeconds(msg.getTimeout());
		cmd.setUuid(msg.getvMUuid());
		cmd.setVmUuid(msg.getvMUuid());
		cmd.setTimeout(120);
		cmd.setAccess_key_id(msg.getAccess_key_id());
		cmd.setAccess_key_secret(msg.getAccess_key_secret());
		cmd.setRegion(msg.getRegion());
		cmd.setForce("False");
		cmd.setTimeout(timeout);
		restf.asyncJsonPost(rebootVmPath, cmd, new JsonAsyncRESTCallback<RebootVmResponse>(msg, completion) {
			@Override
			public void fail(ErrorCode err) {
				RebootVmPubOnLocalReply reply = new RebootVmPubOnLocalReply();
				reply.setError(err);
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public void success(RebootVmResponse ret) {
				RebootVmPubOnLocalReply reply = new RebootVmPubOnLocalReply();
				if (!ret.isSuccess()) {
					String err = String.format("unable to reboot vm[uuid:%s] on kvm host[uuid:%s, ip:%s], because %s",
							cmd.getUuid(), self.getUuid(), self.getManagementIp(), ret.getError());
					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_REBOOT_VM_ON_HYPERVISOR, err));
				}
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public Class<RebootVmResponse> getReturnClass() {
				return RebootVmResponse.class;
			}

		});
	}

	private void handle(final StopVmPubOnLocalMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				stopVm(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("stop-vm-on-kvm-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private void stopVm(final StopVmPubOnLocalMsg msg, final NoErrorCompletion completion) {
		StopVmCmd cmd = new StopVmCmd();
		cmd.setUuid(msg.getId());
		cmd.setVmUuid(msg.getvMUuid());
		cmd.setTimeout(120);
		cmd.setAccess_key_id(msg.getAccess_key_id());
		cmd.setAccess_key_secret(msg.getAccess_key_secret());
		cmd.setRegion(msg.getRegion());
		cmd.setForce("False");

		restf.asyncJsonPost(stopVmPath, cmd, new JsonAsyncRESTCallback<StopVmResponse>(msg, completion) {
			@Override
			public void fail(ErrorCode err) {
				StopVmOnHypervisorReply reply = new StopVmOnHypervisorReply();
				if (err.isError(SysErrors.IO_ERROR, SysErrors.HTTP_ERROR)) {
					err = errf.instantiateErrorCode(HostErrors.OPERATION_FAILURE_GC_ELIGIBLE, "unable to stop a vm",
							err);
				}
				reply.setError(err);
				// extEmitter.stopVmOnKvmFailed(KVMHostInventory.valueOf(getSelf()),
				// vminv, err);
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public void success(StopVmResponse ret) {
				StopVmOnHypervisorReply reply = new StopVmOnHypervisorReply();
				if (!ret.isSuccess()) {
					String err = String.format("unable to stop vm[uuid:%s ] on kvm host[uuid:%s, ip:%s], because %s",
							msg.getId(), self.getUuid(), self.getManagementIp(), ret.getError());
					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_STOP_VM_ON_HYPERVISOR, err));
					logger.warn(err);
					// extEmitter.stopVmOnKvmFailed(KVMHostInventory.valueOf(getSelf()),
					// vminv, reply.getError());
				} else {
					// extEmitter.stopVmOnKvmSuccess(KVMHostInventory.valueOf(getSelf()),
					// vminv);
				}
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public Class<StopVmResponse> getReturnClass() {
				return StopVmResponse.class;
			}

		});
	}

//	private void handle(final GetPubVmInstanceListMsg msg) {
//
//		thdf.chainSubmit(new ChainTask(msg) {
//			@Override
//			public String getSyncSignature() {
//				return id;
//			}
//
//			@Override
//			public void run(final SyncTaskChain chain) {
////				getPubVmList(msg, msg, new NoErrorCompletion(chain) {
////					@Override
////					public void done() {
////						chain.next();
////					}
////				});
//			}
//
//			@Override
//			public String getName() {
//				return String.format("start-vm-on-kvm-%s", self.getUuid());
//			}
//
//			@Override
//			protected int getSyncLevel() {
//				return getHostSyncLevel();
//			}
//		});
//	}

	private void handle(final CreateVmOnLocalMsg msg) {

		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				createVm(msg, msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("start-vm-on-kvm-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	@Transactional
	private L2NetworkInventory getL2NetworkTypeFromL3NetworkUuid(String l3NetworkUuid) {
		String sql = "select l2 from L2NetworkVO l2 where l2.uuid = (select l3.l2NetworkUuid from L3NetworkVO l3 where l3.uuid = :l3NetworkUuid)";
		TypedQuery<L2NetworkVO> query = dbf.getEntityManager().createQuery(sql, L2NetworkVO.class);
		query.setParameter("l3NetworkUuid", l3NetworkUuid);
		L2NetworkVO l2vo = query.getSingleResult();
		return L2NetworkInventory.valueOf(l2vo);
	}

	private String getVolumeTOType(VolumeInventory vol) {
		return vol.getInstallPath().startsWith("iscsi") ? VolumeTO.ISCSI : VolumeTO.FILE;
	}

	private void createVm(final CreateVmOnLocalMsg spec, final NeedReplyMessage msg,
			final NoErrorCompletion completion) {
		final StartVmCmd cmd = new StartVmCmd();
		cmd.setAccess_key_id(spec.getAccesskeyID());
		cmd.setAccess_key_secret(spec.getAccesskeyKEY());
		cmd.setName(spec.getName());
		cmd.setRegion(spec.getRegion());
		cmd.setImage(spec.getImage());
		cmd.setAuth("P@$$w0rd");
		cmd.setEx_security_group_id("sg-2ze6ujdztjgsp3idbfl2");
		cmd.setEx_internet_max_bandwidth_out(1);
		cmd.setEx_internet_charge_type("PayByTraffic");
		// cmd.setSize("ecs.t1.small");
		cmd.setSize(spec.getInstanceOfferingUuid());
		Map sys_disk = new HashMap();
		sys_disk.put("category", "cloud_efficiency");
		cmd.setEx_system_disk(sys_disk);
		restf.asyncJsonPost(startVmPath, cmd, new JsonAsyncRESTCallback<StartVmPubResponse>(msg, completion) {
			@Override
			public void fail(ErrorCode err) {
				StartVmOnHypervisorReply reply = new StartVmOnHypervisorReply();
				reply.setError(err);
				reply.setSuccess(false);
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public void success(StartVmPubResponse ret) {
				StartVmOnPubReply reply = new StartVmOnPubReply();
				if (ret.isSuccess()) {
					String info = String.format("successfully start vm[uuid:%s name:%s] on kvm host[uuid:%s, ip:%s]",
							spec.getUuid(), spec.getName(), self.getUuid(), self.getManagementIp());
					logger.debug(info);
				} else {
					String err = String.format(
							"failed to start vm[uuid:%s name:%s] on kvm host[uuid:%s, ip:%s], because %s",
							spec.getUuid(), spec.getName(), self.getUuid(), self.getManagementIp(), ret.getError());
					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_START_VM_ON_HYPERVISOR, err));
					logger.warn(err);
				}
				reply.setVmUuid(ret.getVmUuid());
				bus.reply(msg, reply);
				completion.done();
			}

			@Override
			public Class<StartVmPubResponse> getReturnClass() {
				return StartVmPubResponse.class;
			}
		});
	}

//	public List<PubVmInstanceEO> getPubVmVOFromVms(List<ECSNode> vms) {
//		List<PubVmInstanceEO> pubVo = new ArrayList<PubVmInstanceEO>();
//		for (ECSNode ecs : vms) {
//			PubVmInstanceEO vo = new PubVmInstanceEO();
//			vo.setPubID(ecs.getId());
//			vo.setHostname(ecs.getName());
//			vo.setCloudType("ECS");
//			vo.setUuid(ecs.getId());
//			vo.setState(ecs.getState());
//			String otherInfo = "Public IP : " + ecs.getPublic_ips() + "  " + "Private IP : " + ecs.getPrivate_ips()
//					+ "   ";
//			vo.setOtherInfo(otherInfo);
//			pubVo.add(vo);
//		}
//		return pubVo;
//
//	}
//
//	public PubVmInstanceEO isContain(String pubID, List<PubVmInstanceEO> vms) {
//		for (PubVmInstanceEO eo : vms) {
//			if (eo.getPubID().equals(pubID)) {
//				List<PubVmInstanceEO> eos = dbf.listByColumName(PubVmInstanceEO.class, "pubID", pubID);
//				return eos.get(0);
//
//			}
//		}
//		return null;
//
//	}
//
//	private void getPubVmList(final GetPubVmInstanceListMsg spec, final NeedReplyMessage msg,
//			final NoErrorCompletion completion) {
//		final GetPubVmCmd cmd = new GetPubVmCmd();
//		restf.asyncJsonPost(getPubVmPath, cmd, new JsonAsyncRESTCallback<String>(msg, completion) {
//			@Override
//			public void fail(ErrorCode err) {
//				GetPubVmInstanceListReply reply = new GetPubVmInstanceListReply();
//				reply.setError(err);
//				reply.setSuccess(false);
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public void success(String ret) {
//				JSONList<ECSNode> re = JSONList.fromJson(ret, ECSNode.class);
//				GetPubVmInstanceListReply reply = new GetPubVmInstanceListReply();
//				reply.setCloudType("ECS");
//				if (re.isSuccess()) {
//					String info = String.format("successfully Get Vm Lists");
//					logger.debug(info);
//					List<PubVmInstanceEO> oldEO = dbf.listByColumName(PubVmInstanceEO.class, "cloudType", "ECS");
//					List<PubVmInstanceEO> newPriderEOs = getPubVmVOFromVms(re.getNodes());
//					if (oldEO.size() == 0 && newPriderEOs.size() != 0) {
//						dbf.persistCollection(newPriderEOs);
//					} 
//					if (oldEO.size() != 0 && newPriderEOs.size() == 0) {
//						dbf.removeByColumName(PubVmInstanceEO.class, "cloudType", "ECS");
//					} 
//					if (oldEO.size() != 0 && newPriderEOs.size() != 0) {
//						List<PubVmInstanceEO> newDB = new ArrayList<PubVmInstanceEO>();
//						for (PubVmInstanceEO oldep : oldEO) {
//							PubVmInstanceEO tmp = isContain(oldep.getPubID(), newPriderEOs); // 通过ID比对是否在里面
//							if (tmp != null) {
//								tmp.setMemorySize(oldep.getMemorySize());
//								tmp.setCpuInfo(oldep.getCpuInfo());
//								newDB.add(tmp);
//							}
//						}
//						dbf.removeByColumName(PubVmInstanceEO.class, "cloudType", "ECS");
//						dbf.persistCollection(newDB);
//					} 
//
//				} else {
//					String err = String.format("failed to Get Vm Lists");
//					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_START_VM_ON_HYPERVISOR, err));
//					logger.warn(err);
//				}
//				bus.reply(msg, reply);
//				completion.done();
//			}
//
//			@Override
//			public Class<String> getReturnClass() {
//				return String.class;
//			}
//		});
//	}

	private void startVm(final StartVmPubOnLocalMsg spec, final NoErrorCompletion completion) {
		final StartVmCmd cmd = new StartVmCmd();
		cmd.setAccess_key_id(spec.getAccess_key_id());
		cmd.setAccess_key_secret(spec.getAccess_key_secret());
		cmd.setRegion(spec.getRegion());
		cmd.setImage(spec.getImage());
		cmd.setAuth("P@$$w0rd");
		cmd.setEx_security_group_id("sg-2ze56hvvjveewzm12jar");
		cmd.setEx_internet_max_bandwidth_out(1);
		cmd.setEx_internet_charge_type("PayByTraffic");
		cmd.setVmInstanceUuid(spec.getvMUuid());
		// cmd.setSize("ecs.t1.small");
		cmd.setSize(spec.getInstanceOfferingUuid());
		Map sys_disk = new HashMap();
		sys_disk.put("category", "cloud_efficiency");
		cmd.setEx_system_disk(sys_disk);
		restf.asyncJsonPost(startVmPath, cmd, new JsonAsyncRESTCallback<StartVmPubResponse>(spec, completion) {
			@Override
			public void fail(ErrorCode err) {
				StartVmOnHypervisorReply reply = new StartVmOnHypervisorReply();
				reply.setError(err);
				reply.setSuccess(false);
				bus.reply(spec, reply);
				completion.done();
			}

			@Override
			public void success(StartVmPubResponse ret) {
				StartVmOnPubReply reply = new StartVmOnPubReply();
				if (ret.isSuccess()) {
					String info = String.format("successfully start vm[uuid:%s ] on kvm host[uuid:%s, ip:%s]",
							spec.getvMUuid(), self.getUuid(), self.getManagementIp());
					logger.debug(info);
				} else {
					String err = String.format("failed to start vm[uuid:%s  ] on kvm host[uuid:%s, ip:%s], because %s",
							spec.getvMUuid(), self.getUuid(), self.getManagementIp(), ret.getError());
					reply.setError(errf.instantiateErrorCode(HostErrors.FAILED_TO_START_VM_ON_HYPERVISOR, err));
					logger.warn(err);
				}
				reply.setVmUuid(ret.getVmUuid());
				bus.reply(spec, reply);
				completion.done();
			}

			@Override
			public Class<StartVmPubResponse> getReturnClass() {
				return StartVmPubResponse.class;
			}
		});
	}

	// private void handle(final StartVmOnHypervisorMsg msg) {
	// thdf.chainSubmit(new ChainTask(msg) {
	// @Override
	// public String getSyncSignature() {
	// return id;
	// }
	//
	// @Override
	// public void run(final SyncTaskChain chain) {
	// startVm(msg.getVmSpec(), msg, new NoErrorCompletion(chain) {
	// @Override
	// public void done() {
	// chain.next();
	// }
	// });
	// }
	//
	// @Override
	// public String getName() {
	// return String.format("start-vm-on-kvm-%s", self.getUuid());
	// }
	//
	// @Override
	// protected int getSyncLevel() {
	// return getHostSyncLevel();
	// }
	// });
	// }

	private void handle(final CheckNetworkPhysicalInterfaceMsg msg) {
		thdf.chainSubmit(new ChainTask(msg) {
			@Override
			public String getSyncSignature() {
				return id;
			}

			@Override
			public void run(final SyncTaskChain chain) {
				checkPhysicalInterface(msg, new NoErrorCompletion(chain) {
					@Override
					public void done() {
						chain.next();
					}
				});
			}

			@Override
			public String getName() {
				return String.format("check-network-physical-interface-on-host-%s", self.getUuid());
			}

			@Override
			protected int getSyncLevel() {
				return getHostSyncLevel();
			}
		});
	}

	private void checkPhysicalInterface(CheckNetworkPhysicalInterfaceMsg msg, NoErrorCompletion completion) {
		checkState();
		CheckPhysicalNetworkInterfaceCmd cmd = new CheckPhysicalNetworkInterfaceCmd();
		cmd.addInterfaceName(msg.getPhysicalInterface());
		CheckNetworkPhysicalInterfaceReply reply = new CheckNetworkPhysicalInterfaceReply();
		CheckPhysicalNetworkInterfaceResponse rsp = restf.syncJsonPost(checkPhysicalNetworkInterfacePath, cmd,
				CheckPhysicalNetworkInterfaceResponse.class);
		if (!rsp.isSuccess()) {
			String err = rsp.getFailedInterfaceNames().isEmpty() ? rsp.getError()
					: String.format(
							"%s, failed to check physical network interfaces[names : %s] on kvm host[uuid:%s, ip:%s]",
							rsp.getError(), msg.getPhysicalInterface(), context.getInventory().getUuid(),
							context.getInventory().getManagementIp());
			reply.setError(errf.stringToOperationError(err));
			logger.warn(err);
		}
		bus.reply(msg, reply);
		completion.done();
	}

	@Override
	public void handleMessage(Message msg) {
		try {
			if (msg instanceof APIMessage) {
				handleApiMessage((APIMessage) msg);
			} else {
				handleLocalMessage(msg);
			}
		} catch (Exception e) {
			bus.logExceptionWithMessageDump(msg, e);
			bus.replyErrorByMessageType(msg, e);
		}
	}

	@Override
	public void changeStateHook(HostState current, HostStateEvent stateEvent, HostState next) {
	}

	@Override
	public void deleteHook() {
	}

	@Override
	protected HostInventory getSelfInventory() {
		return KVMHostInventory.valueOf(getSelf());
	}

	@Override
	protected int getVmMigrateQuantity() {
		return PubCloudGlobalConfig.VM_MIGRATION_QUANTITY.value(Integer.class);
	}

	private ErrorCode connectToAgent() {
		ErrorCode errCode = null;
		try {
			ConnectCmd cmd = new ConnectCmd();
			cmd.setHostUuid(self.getUuid());
			cmd.setSendCommandUrl(restf.getSendCommandUrl());
			ConnectResponse rsp = restf.syncJsonPost(connectPath, cmd, ConnectResponse.class);
			if (!rsp.isSuccess()) {
				String err = String.format("unable to connect to kvm host[uuid:%s, ip:%s, url:%s], because %s",
						self.getUuid(), self.getManagementIp(), connectPath, rsp.getError());
				errCode = errf.stringToOperationError(err);
			} else {
				VersionComparator libvirtVersion = new VersionComparator(rsp.getLibvirtVersion());
				VersionComparator qemuVersion = new VersionComparator(rsp.getQemuVersion());
				boolean liveSnapshot = libvirtVersion.compare(PubCloudConstant.MIN_LIBVIRT_LIVESNAPSHOT_VERSION) >= 0
						&& qemuVersion.compare(PubCloudConstant.MIN_QEMU_LIVESNAPSHOT_VERSION) >= 0;

				String hostOS = HostSystemTags.OS_DISTRIBUTION.getTokenByResourceUuid(self.getUuid(),
						HostSystemTags.OS_DISTRIBUTION_TOKEN);
				// liveSnapshot = liveSnapshot && (!"CentOS".equals(hostOS) ||
				// KVMGlobalConfig.ALLOW_LIVE_SNAPSHOT_ON_REDHAT.value(Boolean.class));

				if (liveSnapshot) {
					logger.debug(String.format(
							"kvm host[OS:%s, uuid:%s, name:%s, ip:%s] supports live snapshot with libvirt[version:%s], qemu[version:%s]",
							hostOS, self.getUuid(), self.getName(), self.getManagementIp(), rsp.getLibvirtVersion(),
							rsp.getQemuVersion()));
					HostSystemTags.LIVE_SNAPSHOT.reCreateInherentTag(self.getUuid());
				} else {
					HostSystemTags.LIVE_SNAPSHOT.deleteInherentTag(self.getUuid());
				}
			}
		} catch (RestClientException e) {
			String err = String.format("unable to connect to kvm host[uuid:%s, ip:%s, url:%s], because %s",
					self.getUuid(), self.getManagementIp(), connectPath, e.getMessage());
			errCode = errf.stringToOperationError(err);
		} catch (Throwable t) {
			logger.warn(t.getMessage(), t);
			errCode = errf.throwableToInternalError(t);
		}

		return errCode;
	}

	private HostVO getSelf() {
		return (HostVO) self;
	}

	private void continueConnect(final boolean newAdded, final Completion completion) {

		ErrorCode errCode = connectToAgent();
		if (errCode != null) {
			throw new OperationFailureException(errCode);
		}

		FlowChain chain = FlowChainBuilder.newSimpleFlowChain();
		chain.setName(String.format("continue-connecting-kvm-host-%s-%s", self.getManagementIp(), self.getUuid()));
		for (KVMHostConnectExtensionPoint extp : factory.getConnectExtensions()) {
			KVMHostConnectedContext ctx = new KVMHostConnectedContext();
			ctx.setInventory((KVMHostInventory) getSelfInventory());
			ctx.setNewAddedHost(newAdded);

			chain.then(extp.createKvmHostConnectingFlow(ctx));
		}

		chain.done(new FlowDoneHandler(completion) {
			@Override
			public void handle(Map data) {
				completion.success();
			}
		}).error(new FlowErrorHandler(completion) {
			@Override
			public void handle(ErrorCode errCode, Map data) {
				String err = String.format("connection error for KVM host[uuid:%s, ip:%s]", self.getUuid(),
						self.getManagementIp());
				completion.fail(errf.instantiateErrorCode(HostErrors.CONNECTION_ERROR, err, errCode));
			}
		}).start();
	}

	private void createHostVersionSystemTags(String distro, String release, String version) {
		HostSystemTags.OS_DISTRIBUTION.createInherentTag(self.getUuid(),
				map(e(HostSystemTags.OS_DISTRIBUTION_TOKEN, distro)));
		HostSystemTags.OS_RELEASE.createInherentTag(self.getUuid(), map(e(HostSystemTags.OS_RELEASE_TOKEN, release)));
		HostSystemTags.OS_VERSION.createInherentTag(self.getUuid(), map(e(HostSystemTags.OS_VERSION_TOKEN, version)));
	}

	@Override
	protected int getHostSyncLevel() {
		return PubCloudGlobalConfig.HOST_SYNC_LEVEL.value(Integer.class);
	}

	@Override
	public void executeHostMessageHandlerHook(HostMessageHandlerExtensionPoint ext, Message msg) {
	}

	@Override
	protected HostVO updateHost(APIUpdateHostMsg msg) {
		if (!(msg instanceof APIUpdateKVMHostMsg)) {
			return super.updateHost(msg);
		}

		HostVO vo = (HostVO) super.updateHost(msg);
		vo = vo == null ? getSelf() : vo;
		//
		// APIUpdateKVMHostMsg umsg = (APIUpdateKVMHostMsg) msg;
		// if (umsg.getUsername() != null) {
		// vo.setUsername(umsg.getUsername());
		// }
		// if (umsg.getPassword() != null) {
		// vo.setPassword(umsg.getPassword());
		// }
		// if (umsg.getSshPort() != null && umsg.getSshPort() > 0 &&
		// umsg.getSshPort() <= 65535 ) {
		// vo.setPort(umsg.getSshPort());
		// }

		return vo;
	}

	@Override
	protected void pingHook(Completion completion) {
		// TODO Auto-generated method stub

	}

	public void connectHook(final ConnectHostInfo info, final Completion complete) {
		if (CoreGlobalProperty.UNIT_TEST_ON) {
			if (info.isNewAdded()) {
				createHostVersionSystemTags("zstack", "kvmSimulator", "0.1");
				KVMSystemTags.LIBVIRT_VERSION.createInherentTag(self.getUuid(),
						map(e(KVMSystemTags.LIBVIRT_VERSION_TOKEN, "1.2.9")));
				KVMSystemTags.QEMU_IMG_VERSION.createInherentTag(self.getUuid(),
						map(e(KVMSystemTags.QEMU_IMG_VERSION_TOKEN, "2.0.0")));
			}

			continueConnect(info.isNewAdded(), complete);
		} else {
			FlowChain chain = FlowChainBuilder.newShareFlowChain();
			chain.setName(String.format("run-ansible-for-ECS-%s", self.getUuid()));
			chain.then(new ShareFlow() {
				@Override
				public void setup() {
					if (info.isNewAdded()) {
						flow(new NoRollbackFlow() {
							String __name__ = "ping-DNS-check-list";

							@Override
							public void run(FlowTrigger trigger, Map data) {
								PubAccountEO eo = dbf.findByColumName(PubAccountEO.class, "cloudType", "LOCAL");
								String checkList = PubCloudGlobalConfig.HOST_DNS_CHECK_LIST.value();
								checkList = checkList.replaceAll(",", " ");
								SshResult ret = new Ssh().setHostname("127.0.0.1").setUsername(eo.getUsername())
										.setPassword(eo.getPassword()).setPort(22)
										.script("scripts/check-public-dns-name.sh", map(e("dnsCheckList", checkList)))
										.runAndClose();
								if (ret.isSshFailure()) {
									trigger.fail(errf.stringToOperationError(String.format(
											"unable to connect to KVM[ip:%s, username:%s, sshPort: %d, ] to do DNS check, please check if username/password is wrong; %s",
											self.getManagementIp(), "root", 22, ret.getExitErrorMessage())));
								} else if (ret.getReturnCode() != 0) {
									trigger.fail(errf.stringToOperationError(String.format(
											"failed to ping all DNS/IP in %s; please check /etc/resolv.conf to make sure your host is able to reach public internet, or change host.DNSCheckList if you have some special network setup",
											PubCloudGlobalConfig.HOST_DNS_CHECK_LIST.value())));
								} else {
									trigger.next();
								}
							}
						});
					}

					flow(new NoRollbackFlow() {
						String __name__ = "check-if-host-can-reach-management-node";

						@Override
						public void run(FlowTrigger trigger, Map data) {
							
							PubAccountEO eo = dbf.findByColumName(PubAccountEO.class, "cloudType", "LOCAL");
							new Log(self.getUuid()).log(LocalHostLabel.ADD_HOST_CHECK_PING_MGMT_NODE);
							SshResult ret2 = new Ssh().setHostname("127.0.0.1").setUsername(eo.getUsername())
									.setPassword(eo.getPassword()).setPort(22)
									.command(String.format("curl --connect-timeout 10 %s", restf.getCallbackUrl()))
									.runAndClose();

							if (ret2.isSshFailure()) {
								throw new OperationFailureException(errf.stringToOperationError(String.format(
										"unable to connect to KVM[ip:%s, username:%s, sshPort:%d] to check the management node connectivity,"
												+ "please check if username/password is wrong; %s",
										self.getManagementIp(), "root", 22, ret2.getExitErrorMessage())));
							} else if (ret2.getReturnCode() != 0) {
								throw new OperationFailureException(errf.stringToOperationError(String.format(
										"the KVM host[ip:%s] cannot access the management node's callback url. It seems"
												+ " that the KVM host cannot reach the management IP[%s]. %s %s",
										self.getManagementIp(), Platform.getManagementServerIp(), ret2.getStderr(),
										ret2.getExitErrorMessage())));
							}

							trigger.next();
						}
					});

					flow(new NoRollbackFlow() {
						String __name__ = "apply-ansible-playbook";

						@Override
						public void run(final FlowTrigger trigger, Map data) {
							new Log(self.getUuid()).log(LocalHostLabel.CALL_ANSIBLE);

							String srcPath = PathUtil
									.findFileOnClassPath(String.format("ansible/aliyun/%s", agentPackageName), true)
									.getAbsolutePath();
							String destPath = String.format("/var/lib/zstack/aliyun/package/%s", agentPackageName);
							PubAccountEO eo = dbf.findByColumName(PubAccountEO.class, "cloudType", "LOCAL");
							
							SshFileMd5Checker checker = new SshFileMd5Checker();
							checker.setUsername(eo.getUsername());
							checker.setPassword(eo.getPassword());
							checker.setSshPort(22);
							checker.setTargetIp("127.0.0.1");
							checker.addSrcDestPair(SshFileMd5Checker.ZSTACKLIB_SRC_PATH, String.format(
									"/var/lib/zstack/kvm/package/%s", AnsibleGlobalProperty.ZSTACKLIB_PACKAGE_NAME));
							checker.addSrcDestPair(srcPath, destPath);

							AnsibleRunner runner = new AnsibleRunner();
							runner.installChecker(checker);
							runner.setAgentPort(PubCloudGlobalProperty.AGENT_PORT);
							runner.setTargetIp("127.0.0.1");
							runner.setPlayBookName(PubCloudConstant.ANSIBLE_PLAYBOOK_NAME);
							runner.setUsername(eo.getUsername());
							runner.setPassword(eo.getPassword());
							runner.setSshPort(22);
							if (info.isNewAdded()) {
								runner.putArgument("init", "true");
								runner.setFullDeploy(true);
							}
							runner.putArgument("pkg_kvmagent", agentPackageName);
							runner.putArgument("hostname",
									String.format("%s.zstack.org", "127.0.0.1".replaceAll("\\.", "-")));

							UriComponentsBuilder ub = UriComponentsBuilder.fromHttpUrl(restf.getBaseUrl());
							ub.path(new StringBind(PubCloudConstant.KVM_ANSIBLE_LOG_PATH_FROMAT)
									.bind("uuid", self.getUuid()).toString());
							String postUrl = ub.build().toString();

							runner.putArgument("post_url", postUrl);
							runner.run(new Completion(trigger) {
								@Override
								public void success() {
									trigger.next();
								}

								@Override
								public void fail(ErrorCode errorCode) {
									trigger.fail(errorCode);
								}
							});
						}
					});

					flow(new NoRollbackFlow() {
						String __name__ = "echo-host";

						@Override
						public void run(final FlowTrigger trigger, Map data) {
							new Log(self.getUuid()).log(LocalHostLabel.ECHO_AGENT);

							restf.echo(echoPath, new Completion(trigger) {
								@Override
								public void success() {
									trigger.next();
								}

								@Override
								public void fail(ErrorCode errorCode) {
									trigger.fail(errorCode);
								}
							});
						}
					});

					if (info.isNewAdded()) {
						flow(new NoRollbackFlow() {
							String __name__ = "ansbile-get-kvm-host-facts";

							@Override
							public void run(FlowTrigger trigger, Map data) {
								String privKeyFile = PathUtil.findFileOnClassPath(AnsibleConstant.RSA_PRIVATE_KEY)
										.getAbsolutePath();
								ShellResult ret = ShellUtils.runAndReturn(
										String.format(
												"ansible -i %s --private-key %s -m setup -a filter=ansible_distribution* %s -e 'ansible_ssh_port=%d ansible_ssh_user=%s'",
												AnsibleConstant.INVENTORY_FILE, privKeyFile, "127.0.0.1", 22, "root"),
										AnsibleConstant.ROOT_DIR);
								if (!ret.isReturnCode(0)) {
									trigger.fail(errf.stringToOperationError(
											String.format("unable to get ecs host[uuid:%s, ip:%s] facts by ansible\n%s",
													self.getUuid(), self.getManagementIp(), ret.getExecutionLog())));

									return;
								}

								String[] pairs = ret.getStdout().split(">>");
								if (pairs.length != 2) {
									trigger.fail(errf.stringToOperationError(String
											.format("unrecognized ansible facts mediaType, %s", ret.getStdout())));
									return;
								}

								LinkedHashMap output = JSONObjectUtil.toObject(pairs[1], LinkedHashMap.class);
								LinkedHashMap facts = (LinkedHashMap) output.get("ansible_facts");
								if (facts == null) {
									trigger.fail(errf.stringToOperationError(String.format(
											"unrecognized ansible facts mediaType, cannot find field 'ansible_facts', %s",
											ret.getStdout())));
									return;
								}

								String distro = (String) facts.get("ansible_distribution");
								String release = (String) facts.get("ansible_distribution_release");
								String version = (String) facts.get("ansible_distribution_version");
								createHostVersionSystemTags(distro, release, version);
								trigger.next();
							}
						});
					}

					// flow(new NoRollbackFlow() {
					// String __name__ = "prepare-host-env";
					//
					// @Override
					// public void run(FlowTrigger trigger, Map data) {
					// new
					// Log(self.getUuid()).log(LocalHostLabel.PREPARE_FIREWALL);
					//
					// String script = "which iptables > /dev/null && iptables
					// -C FORWARD -j REJECT --reject-with icmp-host-prohibited >
					// /dev/null 2>&1 && iptables -D FORWARD -j REJECT
					// --reject-with icmp-host-prohibited > /dev/null 2>&1 ||
					// true";
					// runShell(script);
					// trigger.next();
					// }
					// });

					error(new FlowErrorHandler(complete) {
						@Override
						public void handle(ErrorCode errCode, Map data) {
							complete.fail(errCode);
						}
					});

					done(new FlowDoneHandler(complete) {
						@Override
						public void handle(Map data) {
							// continueConnect(info.isNewAdded(), complete);
							complete.success();
						}
					});
				}
			}).start();
		}
	}

}
