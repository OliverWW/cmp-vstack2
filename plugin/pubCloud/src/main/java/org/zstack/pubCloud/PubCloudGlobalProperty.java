package org.zstack.pubCloud;

import org.zstack.core.GlobalProperty;
import org.zstack.core.GlobalPropertyDefinition;

/**
 */
@GlobalPropertyDefinition
public class PubCloudGlobalProperty {
    @GlobalProperty(name="pubCloudAgent.agentPackageName", defaultValue = "aliyunagent-1.6.tar.gz")
    public static String AGENT_PACKAGE_NAME;
    @GlobalProperty(name="pubCloudAgent.agentUrlRootPath", defaultValue = "")
    public static String AGENT_URL_ROOT_PATH;
    @GlobalProperty(name="pubCloudAgent.agentUrlScheme", defaultValue = "http")
    public static String AGENT_URL_SCHEME;
    @GlobalProperty(name="pubCloudAgent.port", defaultValue = "7072")
    public static int AGENT_PORT;
    @GlobalProperty(name="pubCloudServer.port", defaultValue = "10001")
    public static int AGENT_SERVER_PORT;
}
