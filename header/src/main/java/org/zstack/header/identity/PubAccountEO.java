package org.zstack.header.identity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 */
@Entity
@Table
public class PubAccountEO extends PubAccountAO{
    @Column
    private String deleted;
    
    @Column
    private String password;

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
	

    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
