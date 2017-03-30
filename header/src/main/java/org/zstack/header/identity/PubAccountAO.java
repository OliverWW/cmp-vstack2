package org.zstack.header.identity;

import org.zstack.header.vo.Index;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 */
@MappedSuperclass
public class PubAccountAO {
	
	@Id
    @Column
    private String uuid;
	@Column
	private String cloudType;
	@Column
	    private String description;
    @Column
    private String username;
    @Column
    private String accesskeyID;
    @Column
    private String accesskeyKey;
    @Column
	private String token;
    @Column
    private Timestamp createDate;
    @Column
    private Timestamp lastOpDate;
    
	
	public String getCloudType() {
		return cloudType;
	}

	public void setCloudType(String cloudType) {
		this.cloudType = cloudType;
	}

    public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@PreUpdate
    private void preUpdate() {
        lastOpDate = null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getLastOpDate() {
        return lastOpDate;
    }

    public void setLastOpDate(Timestamp lastOpDate) {
        this.lastOpDate = lastOpDate;
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
 
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
