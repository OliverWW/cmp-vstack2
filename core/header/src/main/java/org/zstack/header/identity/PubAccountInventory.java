package org.zstack.header.identity;

import org.zstack.header.configuration.PythonClassInventory;

import org.zstack.header.query.ExpandedQueries;
import org.zstack.header.query.ExpandedQuery;
import org.zstack.header.search.Inventory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.metamodel.SingularAttribute;

@Inventory(mappingVOClass =PubAccountVO.class)
 
@PythonClassInventory
public class PubAccountInventory {
	private String uuid;
	private String cloudType;
	private String description;
    private String username;
    private String accesskeyID;
    private String accesskeyKey;
    private String token;
	private Timestamp createDate;
    private Timestamp lastOpDate;
    
    public static PubAccountInventory valueOf(PubAccountVO vo) {
    	PubAccountInventory inv = new PubAccountInventory();
        inv.setUuid(vo.getUuid());
        inv.setUsername(vo.getUsername());
        inv.setAccesskeyID(vo.getAccesskeyID());
        inv.setAccesskeyKey(vo.getAccesskeyKey());
        inv.setToken(vo.getToken());
        inv.setDescription(vo.getDescription());
        inv.setCloudType(vo.getCloudType().toString());
        inv.setCreateDate(vo.getCreateDate());
        inv.setLastOpDate(vo.getLastOpDate());
        return inv;
    }
    
    public static List<PubAccountInventory> valueOf(Collection<PubAccountVO> vos) {
        List<PubAccountInventory> lst = new ArrayList<PubAccountInventory>(vos.size());
        for (PubAccountVO vo : vos) {
            lst.add(PubAccountInventory.valueOf(vo));
        }
        return lst;
    }

     
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCloudType() {
		return cloudType;
	}

	public void setCloudType(String cloudType) {
		this.cloudType = cloudType;
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

	 
     
}
