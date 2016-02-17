/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtilsMapping;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
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

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
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
    return new Date(this.getCreateTimeDb());
  }

  /**
   * @return create time
   */
  public long getCreateTimeDb() {
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
    return new Date(this.getModifyTimeDb());
  }

  /**
   * @return modify time
   */
  public long getModifyTimeDb() {
    return this.modifyTime;
  }

  /**
   * create time
   * @param createTime1 
   */
  public void setCreateTimeDb(long createTime1) {
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
  public void setModifyTimeDb(long modifyTime1) {
    this.modifyTime = modifyTime1;
  
  }

  /**
   * delete this object from the DB.
   * @param externalSubject 
   */
  void delete(final ExternalSubject externalSubject) {    
    
    assertValidAttribute(this.getAttributeSystemName());

    HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
  
          ExternalSubjectAttributeStorageController.delete( ExternalSubjectAttribute.this );
            
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = null;
            
            auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJ_ATTR_DELETE, "id", 
                ExternalSubjectAttribute.this.getUuid(), "name", ExternalSubjectAttribute.this.getAttributeSystemName(), 
                "identifier", externalSubject.getIdentifier(), "value", ExternalSubjectAttribute.this.getAttributeValue());
            auditEntry.setDescription("Deleted external subject attribute: " + ExternalSubjectAttribute.this.getAttributeSystemName());
            auditEntry.saveOrUpdate(true);
          }
  
          return null;
        }
      });
    
  }

  /**
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    if (StringUtils.isBlank(this.getUuid())) {
      this.setUuid(GrouperUuid.getUuid());
    }
    
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );
  
    this.setCreatorMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setCreateTimeDb( System.currentTimeMillis() );
  }

  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );
  
  }

  /**
   * assert that the attribute name is valid
   * @param attributeName
   */
  public static void assertValidAttribute(String attributeName) {
    validAttribute(attributeName, true);
  }

  /**
   * assert that the attribute name is valid
   * @param attributeName
   * @param errorOnNotfound
   * @return if found
   */
  public static boolean validAttribute(String attributeName, boolean errorOnNotfound) {
    //make sure attribute name is ok
    boolean foundAttribute = false;
    for (ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean : 
        GrouperUtil.nonNull(ExternalSubjectConfig.externalSubjectConfigBean().getExternalSubjectAttributeConfigBeans())) {
      if (StringUtils.equals(attributeName, externalSubjectAttributeConfigBean.getSystemName())) {
        foundAttribute = true;
        break;
      }
    }
    if (!foundAttribute) {
      if (errorOnNotfound) {
        throw new RuntimeException("Invalid attribute name: " + attributeName 
            + ", not found in grouper.properties");
      }
      return false;
    }
    return true;
  }
  
  /**
   * store this object to the DB.
   * @param externalSubject reference back to owner
   */
  public void store(final ExternalSubject externalSubject) {    

    if (StringUtils.isBlank(this.getAttributeSystemName())) {
      throw new RuntimeException("Attribute system name cannot be blank: " + this);
    }
    
    assertValidAttribute(this.getAttributeSystemName());

    HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
      new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
  
          ExternalSubjectAttribute.this.setSubjectUuid(externalSubject.getUuid());
          
          boolean isInsert = HibUtilsMapping.isInsert(ExternalSubjectAttribute.this);
          
          ExternalSubjectAttributeStorageController.saveOrUpdate( ExternalSubjectAttribute.this );
            
          if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
            AuditEntry auditEntry = null;
            
            if (isInsert) {
              auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJ_ATTR_ADD, "id", 
                  ExternalSubjectAttribute.this.getUuid(), "name", ExternalSubjectAttribute.this.getAttributeSystemName(), "identifier", externalSubject.getIdentifier(),
                  "value", ExternalSubjectAttribute.this.getAttributeValue());
              auditEntry.setDescription("Added external subject attr: " + externalSubject.getIdentifier() + ", attr: " + ExternalSubjectAttribute.this.getAttributeSystemName());
            } else {
              auditEntry = new AuditEntry(AuditTypeBuiltin.EXTERNAL_SUBJ_ATTR_UPDATE, "id", 
                  ExternalSubjectAttribute.this.getUuid(), "name", ExternalSubjectAttribute.this.getAttributeSystemName(), "identifier", externalSubject.getIdentifier(),
                  "value", ExternalSubjectAttribute.this.getAttributeValue());
              auditEntry.setDescription("Updated external subject attr: " + externalSubject.getIdentifier() + ", attr: " + ExternalSubjectAttribute.this.getAttributeSystemName());
              
            }
            auditEntry.saveOrUpdate(true);
          }
  
          return null;
        }
      });
    
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    try {
      if (this.attributeSystemName != null) {
        result.append("attributeSystemName: ").append(this.attributeSystemName).append(", ");
      }
      if (this.attributeValue != null) {
        result.append("attributeValue: ").append(this.attributeValue).append(", ");
      }
      if (this.uuid != null) {
        result.append("uuid: ").append(this.uuid).append(", ");
      }
      if (this.subjectUuid != null) {
        result.append("subjectUuid: ").append(this.subjectUuid).append(", ");
      }
    } catch (Exception e) {
      //ignore, we did the best we could
    }
    return result.toString();
  }
  
}
