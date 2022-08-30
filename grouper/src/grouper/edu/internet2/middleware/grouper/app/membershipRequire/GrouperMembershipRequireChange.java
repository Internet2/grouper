package edu.internet2.middleware.grouper.app.membershipRequire;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

/**
 * record in database which is log of a change due to membership requirement
 * @author mchyzer
 *
 */
@GcPersistableClass(tableName="grouper_mship_req_change", defaultFieldPersist=GcPersist.doPersist)
public class GrouperMembershipRequireChange {

  /**
   * attribute uuid of attribute assigned
   */
  private String attributeDefNameId;
  
  /**
   * attribute uuid of attribute assigned
   * @return attribute id
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute uuid of attribute assigned
   * @param attributeId1
   */
  public void setAttributeDefNameId(String attributeId1) {
    this.attributeDefNameId = attributeId1;
  }

  /**
   * group uuid of the require group
   */
  private String requireGroupId;

  /**
   * group uuid of the require group
   * @return group uuid
   */
  public String getRequireGroupId() {
    return this.requireGroupId;
  }

  /**
   * group uuid of the require group
   * @param requireGroupId1
   */
  public void setRequireGroupId(String requireGroupId1) {
    this.requireGroupId = requireGroupId1;
  }

  /**
   * configId of this config bean
   */
  private String configId;

  /**
   * configId of this config bean
   * @return config id
   */
  public String getConfigId() {
    return this.configId;
  }

  /**
   * configId of this config bean
   * @param configId1
   */
  public void setConfigId(String configId1) {
    this.configId = configId1;
  }
  

  /**
   * 
   */
  public GrouperMembershipRequireChange() {
  }

  /**
   * integer id for this
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private Long id;

  /**
   * integer id for this
   * @return id
   */
  public Long getId() {
    return this.id;
  }

  /**
   * integer id for this
   * @param id1
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * uuid of grouper members table
   */
  private String memberId;
  
  /**
   * uuid of grouper members table
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * uuid of grouper members table
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * uuid of the group table
   */
  private String groupId;

  /**
   * uuid of the group table
   * @return group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * uuid of the group table
   * @param groupId1
   */
  public void setGroupId(String groupId1) {
    this.groupId = groupId1;
  }

  /**
   * timestamp of when the event occurred
   */
  private Timestamp theTimestamp;

  /**
   * timestamp of when the event occurred
   * @return timestamp
   */
  public Timestamp getTheTimestamp() {
    return this.theTimestamp;
  }

  /**
   * timestamp of when the event occurred
   * @param theTimestamp1
   */
  public void setTheTimestamp(Timestamp theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }

  /**
   * H = hook, C = change log consumer, F = full sync
   */
  private String engine;

  /**
   * H = hook, C = change log consumer, F = full sync
   * @return engine
   */
  public String getEngineDb() {
    return this.engine;
  }

  /**
   * H = hook, C = change log consumer, F = full sync
   * @return engine
   */
  public MembershipRequireEngineEnum getEngine() {
    return MembershipRequireEngineEnum.valueOfIgnoreCase(this.engine, false);
  }

  /**
   * H = hook, C = change log consumer, F = full sync
   * @param engine1 
   */
  public void setEngine(MembershipRequireEngineEnum engine1) {
    this.engine = engine1 == null ? null : engine1.getDbCode();
  }
  
  /**
   * H = hook, C = change log consumer, F = full sync
   * @param engineDb1
   */
  public void setEngineDb(String engineDb1) {
    this.engine = engineDb1;
  }
  
  /**
   * store this record
   */
  public void store() {
    GrouperMembershipRequireChangeDao.store(this);
  }

  /**
   * prepare to store
   */
  public void storePrepare() {

    if (this.theTimestamp == null) {
      this.theTimestamp = new Timestamp(System.currentTimeMillis());
    }
    
    if (this.id == null) {
      this.id = TableIndex.reserveId(TableIndexType.membershipRequire);
    }
    
  }
  
}
