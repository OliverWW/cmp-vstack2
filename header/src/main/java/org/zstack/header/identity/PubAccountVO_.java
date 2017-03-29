package org.zstack.header.identity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.sql.Timestamp;

@StaticMetamodel(PubAccountVO.class)
public class PubAccountVO_ {
    public static volatile SingularAttribute<PubAccountVO, String> uuid;
    public static volatile SingularAttribute<PubAccountVO, PubCloudType> cloudType;
    public static volatile SingularAttribute<PubAccountVO, String> description;
    public static volatile SingularAttribute<PubAccountVO, String> username;
    public static volatile SingularAttribute<PubAccountVO, String> accesskeyID;
    public static volatile SingularAttribute<PubAccountVO, String> accesskeyKey;
    public static volatile SingularAttribute<PubAccountVO, String> token;
    public static volatile SingularAttribute<PubAccountVO, Timestamp> createDate;
    public static volatile SingularAttribute<PubAccountVO, Timestamp> lastOpDate;
    
}
