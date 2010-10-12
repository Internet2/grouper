package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * attribute on an external subject, configured in the grouper.properties
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class ExternalSubjectAttribute extends GrouperAPI implements GrouperHasContext, 
    Hib3GrouperVersioned {

  /** subject uuid foreign key */
  private String subjectUuid;
  
  /** system name of the attributes */
  private String attributeSystemName;

  /** value of attribute */
  private String attributeValue;
  
  /** uuid of the attribute */
  private String uuid;

  /** contextId links to audit tables */
  private String contextId;

  /** time created */
  private long createTime = System.currentTimeMillis();

  /** who created this */
  private String creatorMemberId;

  /** who last modified this */
  private String modifierMemberId;

  /** when last modified */
  private long modifyTime = System.currentTimeMillis(); 

  /** column name for create time */
  public static final String COLUMN_CREATE_TIME = "create_time";

  /** column name for creator member id */
  public static final String COLUMN_CREATOR_MEMBER_ID = "creator_member_id";

  /** column name for modifier member id */
  public static final String COLUMN_MODIFIER_MEMBER_ID = "modifier_member_id";

  /** column name for modify time */
  public static final String COLUMN_MODIFY_TIME = "modify_time";

  /** table name for external subject attributes */
  public static final String TABLE_GROUPER_EXT_SUBJ_ATTR = "grouper_ext_subj_attr";
  
  /** column name for context id */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column name for attribute system name */
  public static final String COLUMN_ATTRIBUTE_SYSTEM_NAME = "attribute_system_name";
  
  /** column name for attribute value */
  public static final String COLUMN_ATTRIBUTE_VALUE = "attribute_value";
  
  /** column name for subject uuid */
  public static final String COLUMN_SUBJECT_UUID = "subject_uuid";
  
  /** column name for uuid */
  public static final String COLUMN_UUID = "uuid";

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeSystemName */
  public static final String FIELD_ATTRIBUTE_SYSTEM_NAME = "attributeSystemName";

  /** constant for field name for: attributeValue */
  public static final String FIELD_ATTRIBUTE_VALUE = "attributeValue";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorMemberId */
  public static final String FIELD_CREATOR_MEMBER_ID = "creatorMemberId";

  /** constant for field name for: modifierMemberId */
  public static final String FIELD_MODIFIER_MEMBER_ID = "modifierMemberId";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: subjectUuid */
  public static final String FIELD_SUBJECT_UUID = "subjectUuid";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_SYSTEM_NAME, FIELD_ATTRIBUTE_VALUE, FIELD_CONTEXT_ID, FIELD_CREATE_TIME, 
      FIELD_CREATOR_MEMBER_ID, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, FIELD_SUBJECT_UUID, 
      FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_SYSTEM_NAME, FIELD_ATTRIBUTE_VALUE, FIELD_CONTEXT_ID, FIELD_CREATE_TIME, 
      FIELD_CREATOR_MEMBER_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, 
      FIELD_SUBJECT_UUID, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /**
   * subject uuid foreign key to subject table
   * @return the uuid
   */
  public String getSubjectUuid() {
    return this.subjectUuid;
  }

  /**
   * subject uuid foreign key to subject table
   * @param subjectUuid1
   */
  public void setSubjectUuid(String subjectUuid1) {
    this.subjectUuid = subjectUuid1;
  }

  /**
   * system name of the attributes
   * @return system name
   */
  public String getAttributeSystemName() {
    return this.attributeSystemName;
  }

  /**
   * system name of the attributes
   * @param attributeSystemName1
   */
  public void setAttributeSystemName(String attributeSystemName1) {
    this.attributeSystemName = attributeSystemName1;
  }

  /**
   * value of attribute
   * @return value 
   */
  public String getAttributeValue() {
    return this.attributeValue;
  }

  /**
   * value of the attribute
   * @param attributeValue1
   */
  public void setAttributeValue(String attributeValue1) {
    this.attributeValue = attributeValue1;
  }

  /**
   * uuid of the attribute
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attribute
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  @Override
  public ExternalSubjectAttribute clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * contextId links to audit tables
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * contextId links to audit tables
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * Get creation time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreateTimeLong());
  }

  /**
   * @return create time
   */
  public long getCreateTimeLong() {
    return this.createTime;
  }

  /**
   * @return creator
   */
  public String getCreatorMemberId() {
    return this.creatorMemberId;
  }

  /**
   * 
   * @return the modifier member id
   */
  public String getModifierMemberId() {
    return this.modifierMemberId;
  }

  /**
   * Get modify time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getModifyTime() {
    return new Date(this.getModifyTimeLong());
  }

  /**
   * @return modify time
   */
  public long getModifyTimeLong() {
    return this.modifyTime;
  }

  /**
   * create time
   * @param createTime1 
   */
  public void setCreateTimeLong(long createTime1) {
    this.createTime = createTime1;
  
  }

  /**
   * member id of creator
   * @param creatorMemberId1
   */
  public void setCreatorMemberId(String creatorMemberId1) {
    this.creatorMemberId = creatorMemberId1;
  
  }

  /**
   * member id of modifier
   * @param modifierMemberId1
   */
  public void setModifierMemberId(String modifierMemberId1) {
    this.modifierMemberId = modifierMemberId1;
  
  }

  /**
   * last time modified
   * @param modifyTime1 
   */
  public void setModifyTimeLong(long modifyTime1) {
    this.modifyTime = modifyTime1;
  
  }
  
}
