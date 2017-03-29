package org.zstack.header.identity;

import java.sql.Timestamp;

import javax.persistence.metamodel.SingularAttribute;

import org.zstack.header.message.APICreateMessage;
import org.zstack.header.message.APIParam;

public class APICreatePubAccountMsg extends APICreateMessage {
    @APIParam(maxLength = 255)
    private String username;
    @APIParam(maxLength = 255)
    private String password;
    @APIParam(maxLength = 255)
    private String token;
    @APIParam(maxLength = 255)
    private String accesskeyID;
    @APIParam(maxLength = 255)
    private String accesskeyKey;
    @APIParam(maxLength = 255, required = false)
    private String cloudType;
    @APIParam(maxLength = 2048, required = false)
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAccesskeyID() {
		return accesskeyID;
	}

	public void setAccesskeyID(String accesskeyID) {
		this.accesskeyID = accesskeyID;
	}

	public String getAccesskeyKey() {
		return accesskeyKey;
	}

	public void setAccesskeyKey(String accesskeyKey) {
		this.accesskeyKey = accesskeyKey;
	}

	public String getCloudType() {
		return cloudType;
	}

	public void setCloudType(String cloudType) {
		this.cloudType = cloudType;
	}

	public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}


