package org.zstack.header.identity;

import org.zstack.header.search.SqlTrigger;
import org.zstack.header.search.TriggerIndex;
import org.zstack.header.tag.AutoDeleteTag;
import org.zstack.header.vo.EO;
import org.zstack.header.vo.Index;
import org.zstack.header.zone.ZoneEO;

import javax.persistence.*;
import javax.persistence.metamodel.SingularAttribute;

import java.sql.Timestamp;

@Entity
@Table
@EO(EOClazz = PubAccountEO.class)
@AutoDeleteTag
public class PubAccountVO extends PubAccountAO{

}
