/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAttributeDefDelegate;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * definition of an attribute
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeDef extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, Owner {

  /** default action */
  public static final String ACTION_DEFAULT = "assign";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AttributeDef.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF = "grouper_attribute_def";

  /** actions col in db */
  public static final String COLUMN_ACTIONS = "actions";

  /** assignable_to col in db */
  public static final String COLUMN_ASSIGNABLE_TO = "assignable_to";

  /** if the attribute def is public */
  public static final String COLUMN_ATTRIBUTE_DEF_PUBLIC = "attribute_def_public";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_TYPE = "attribute_def_type";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_CREATOR_ID = "creator_id";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_DESCRIPTION = "description";

  /** column */
  public static final String COLUMN_EXTENSION = "extension";

  /** column */
  public static final String COLUMN_NAME = "name";

  /** column */
  public static final String COLUMN_MULTI_ASSIGNABLE = "multi_assignable";

  /** column */
  public static final String COLUMN_MULTI_VALUED = "multi_valued";

  /** column */
  public static final String COLUMN_STEM_ID = "stem_id";

  /** column */
  public static final String COLUMN_VALUE_TYPE = "value_type";

  /** column */
  public static final String COLUMN_ID = "id";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: assignableTo */
  public static final String FIELD_ASSIGNABLE_TO = "assignableTo";

  /** constant for field name for: attributeDefPublic */
  public static final String FIELD_ATTRIBUTE_DEF_PUBLIC = "attributeDefPublic";

  /** constant for field name for: attributeDefType */
  public static final String FIELD_ATTRIBUTE_DEF_TYPE = "attributeDefType";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: extension */
  public static final String FIELD_EXTENSION = "extension";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: multiAssignable */
  public static final String FIELD_MULTI_ASSIGNABLE = "multiAssignable";

  /** constant for field name for: multiValued */
  public static final String FIELD_MULTI_VALUED = "multiValued";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /** constant for field name for: valueType */
  public static final String FIELD_VALUE_TYPE = "valueType";

  /** constant for field name for: actions */
  public static final String FIELD_ACTIONS = "actions";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ASSIGNABLE_TO, FIELD_ATTRIBUTE_DEF_PUBLIC, FIELD_ATTRIBUTE_DEF_TYPE, FIELD_CONTEXT_ID, 
      FIELD_CREATE_TIME, FIELD_DESCRIPTION, FIELD_EXTENSION, 
      FIELD_ID, FIELD_MODIFY_TIME, FIELD_MULTI_ASSIGNABLE, 
      FIELD_MULTI_VALUED, FIELD_STEM_ID, FIELD_VALUE_TYPE, FIELD_ACTIONS);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ASSIGNABLE_TO, FIELD_ATTRIBUTE_DEF_PUBLIC, FIELD_ATTRIBUTE_DEF_TYPE, FIELD_CONTEXT_ID, 
      FIELD_CREATE_TIME, FIELD_DESCRIPTION, FIELD_EXTENSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_MODIFY_TIME, 
      FIELD_MULTI_ASSIGNABLE, FIELD_MULTI_VALUED, FIELD_STEM_ID, FIELD_VALUE_TYPE, 
      FIELD_ACTIONS);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDef clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAttributeDefDelegate attributeAssignAttributeDefDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignAttributeDefDelegate getAttributeDelegate() {
    if (this.attributeAssignAttributeDefDelegate == null) {
      this.attributeAssignAttributeDefDelegate = new AttributeAssignAttributeDefDelegate(this);
    }
    return this.attributeAssignAttributeDefDelegate;
  }


  /** id of this attribute def */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /** 
   * AttributeDefAssignableTo type that this attribute value is assignable to,
   * group, stem, membership, member, groupAttribute, 
   * stemAttribute, membershipAttribute, memberAttribute
   */
  private AttributeDefAssignableTo assignableTo;
  
  /** stem that this attribute is in */
  private String stemId;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * memberId of who created this
   */
  private String creatorId;
  
  
  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return this.creatorId;
  }

  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    this.creatorId = GrouperSession.staticGrouperSession(true).getMemberUuid();
  }

  /**
   * @param creatorId1 the creatorId to set
   */
  public void setCreatorId(String creatorId1) {
    this.creatorId = creatorId1;
  }

  /**
   * store this group (update) to database
   */
  public void store() {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //make sure subject is allowed to do this
            Subject subject = GrouperSession.staticGrouperSession().getSubject();
            if (!AttributeDef.this.getPrivilegeDelegate().canAttrAdmin(subject)) {
              throw new InsufficientPrivilegeException(GrouperUtil
                  .subjectToString(subject)
                  + " is not attrAdmin on attributeDef: " + AttributeDef.this.getName());
            }
            
            GrouperDAOFactory.getFactory().getAttributeDef().saveOrUpdate(AttributeDef.this);
            return null;
          }
        });
  }
  
  /** delegate privilege calls to another class to separate logic */
  private AttributeDefPrivilegeDelegate attributeDefPrivilegeDelegate = null;

  /**
   * privilege delegate to handle security on this attribute def
   * @return the delegate
   */
  public AttributeDefPrivilegeDelegate getPrivilegeDelegate() {
    if (this.attributeDefPrivilegeDelegate == null) {
      this.attributeDefPrivilegeDelegate = new AttributeDefPrivilegeDelegate(this);
    }
    return this.attributeDefPrivilegeDelegate;
  }
  
  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   */
  private String description;

  /**
   * extension of attribute
   */
  private String extension;

  /**
   * name of attribute
   */
  private String name;

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   */
  private boolean attributeDefPublic = false;
  
  /**
   * type of this attribute (e.g. attribute or privilege)
   */
  private AttributeDefType attributeDefType;
  
  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @return if public
   */
  public boolean isAttributeDefPublic() {
    return this.attributeDefPublic;
  }

  /**
   * hibernate mapped method for if this attribute def is public
   * @return true if public, false if not (default false)
   */
  public String getAttributeDefPublicDb() {
    return this.attributeDefPublic ? "T" : "F";
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param theAttributeDefPublicDb
   */
  public void setAttributeDefPublicDb(String theAttributeDefPublicDb) {
    this.attributeDefPublic = GrouperUtil.booleanValue(theAttributeDefPublicDb, false);
  }
  
  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param attributeDefPublic1
   */
  public void setAttributeDefPublic(boolean attributeDefPublic1) {
    this.attributeDefPublic = attributeDefPublic1;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @return attribute def type
   */
  public AttributeDefType getAttributeDefType() {
    return this.attributeDefType;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @param attributeDefType
   */
  public void setAttributeDefType(AttributeDefType attributeDefType) {
    this.attributeDefType = attributeDefType;
  }

  /**
   * type of this attribute (e.g. attribute or privilege)
   * @return the attribute def type
   */
  public String getAttributeDefTypeDb() {
    return this.attributeDefType == null ? null : this.attributeDefType.name();
  }

  /**
   * type of this attribute (e.g. attr or priv or limit)
   * @param theAttributeDefType
   */
  public void setAttributeDefTypeDb(String theAttributeDefType) {
    this.attributeDefType = AttributeDefType.valueOfIgnoreCase(theAttributeDefType, false);
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @return if public
   */
  public boolean isAttributeDefIsPublic() {
    return attributeDefPublic;
  }

  /**
   * if the attribute def is public, otherwise you just see it in this stem and substem
   * @param attributeDefIsPublic1
   */
  public void setAttributeDefIsPublic(boolean attributeDefIsPublic1) {
    this.attributeDefPublic = attributeDefIsPublic1;
  }

  /**
   * stem that this attribute is in
   * @return the stem id
   */
  public String getStemId() {
    return stemId;
  }

  /**
   * stem that this attribute is in
   * @param stemId1
   */
  public void setStemId(String stemId1) {
    this.stemId = stemId1;
  }

  /**
   * AttributeDefAssignableTo type that this attribute value is assignable to,
   * group, stem, membership, member, groupAttribute, 
   * stemAttribute, membershipAttribute, memberAttribute
   * @return attribute value
   */
  public String getAssignableToDb() {
    return this.assignableTo == null ? null : this.assignableTo.name();
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   */
  private boolean multiAssignable;
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   */
  private boolean multiValued;

  /**
   * comma separated list of actions for this attribute.  at least there needs to be one.
   * default is "assign"  actions must contain only alphanumeric or underscore, case sensitive
   * e.g. read,write,admin
   */
  private String actions = ACTION_DEFAULT;
  
  /**
   * type of the value,  int, double, string, marker
   */
  private AttributeDefValueType valueType = AttributeDefValueType.marker;
  
  /**
   * type of the value,  int, double, string, marker
   * @return the type
   */
  public AttributeDefValueType getValueType() {
    return valueType;
  }

  /**
   * type of the value,  int, double, string, marker
   * @param valueType1
   */
  public void setValueType(AttributeDefValueType valueType1) {
    this.valueType = valueType1;
  }

  /**
   * type of the value,  int, double, string, marker
   * @return the type
   */
  public String getValueTypeDb() {
    return this.valueType == null ? null : this.valueType.toString();
  }

  /**
   * type of the value,  int, double, string, marker
   * @param valueType1
   */
  public void setValueTypeDb(String valueType1) {
    this.valueType = AttributeDefValueType.valueOfIgnoreCase(valueType1, false);
  }

  /**
   * comma separated list of actions for this attribute.  at least there needs to be one.
   * default is "assign"  actions must contain only alphanumeric or underscore, case sensitive
   * e.g. read, write, admin
   * @return the actions
   */
  public String getActions() {
    if (StringUtils.isBlank(this.actions)) {
      this.actions = ACTION_DEFAULT;
    }
    return actions;
  }

  /**
   * get the actions for this attribute def in an array
   * @return the array of actions
   */
  public String[] getActionsArray() {
    return GrouperUtil.splitTrim(this.actions, ",");
  }
  
  /**
   * comma separated list of actions for this attribute.  at least there needs to be one.
   * default is "assign" actions must contain only alphanumeric or underscore, case sensitive
   * e.g. read,write,admin
   * @param actions
   */
  public void setActions(String actions) {
    this.actions = actions;
    if (StringUtils.isBlank(this.actions)) {
      this.actions = ACTION_DEFAULT;
    }
  }

  /**
   * set of allowed actions
   */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private Set<String> allowedActionsSet = null;

  /**
   * get (and cache) the allowed actions
   * @return the set of strings
   */
  public Set<String> allowedActions() {
    if (this.allowedActionsSet == null) {
      if (StringUtils.isBlank(this.actions)) {
        throw new RuntimeException("actions cant be null: " + this);
      }
      String[] actionsArray = GrouperUtil.splitTrim(this.actions, ",");
      this.allowedActionsSet = GrouperUtil.toSet(actionsArray);
    }
    return this.allowedActionsSet;
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * @return if multiassignable
   */
  public boolean isMultiAssignable() {
    return multiAssignable;
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * convert to string for hibernate
   * @return the string value
   */
  public String getMultiAssignableDb() {
    return this.multiAssignable ? "T" : "F";
  }

  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * convert to string for hibernate
   * @param multiAssignableDb
   */
  public void setMultiAssignableDb(String multiAssignableDb) {
    this.multiAssignable = GrouperUtil.booleanValue(multiAssignableDb, false);
  }
  
  /**
   * if this attribute can be assigned to the same action to the same object more than once
   * @param multiAssignable1
   */
  public void setMultiAssignable(boolean multiAssignable1) {
    this.multiAssignable = multiAssignable1;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * @return boolean
   */
  public boolean isMultiValued() {
    return multiValued;
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * convert to String for hibernate
   * @return if multivalued
   */
  public String getMultiValuedDb() {
    return this.multiValued ? "T" : "F";
  }

  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * convert to String for hibernate
   * @param multiValuedDb
   */
  public void setMultiValuedDb(String multiValuedDb) {
    this.multiValued = GrouperUtil.booleanValue(multiValuedDb, false);
  }
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * @param multiValued1
   */
  public void setMultiValued(boolean multiValued1) {
    this.multiValued = multiValued1;
  }

  /**
   * AttributeDefAssignableTo type that this attribute value is assignable to,
   * group, stem, membership, member, groupAttribute, 
   * stemAttribute, membershipAttribute, memberAttribute
   * @param assignableToString
   */
  public void setAssignableToDb(String assignableToString) {
    this.assignableTo = AttributeDefAssignableTo.valueOfIgnoreCase(assignableToString, 
        false);
  }
  
  /**
   * AttributeDefAssignableTo type that this attribute value is assignable to,
   * group, stem, membership, member, groupAttribute, 
   * stemAttribute, membershipAttribute, memberAttribute
   * @return the type
   */
  public AttributeDefAssignableTo getAssignableTo() {
    return assignableTo;
  }

  /**
   * AttributeDefAssignableTo type that this attribute value is assignable to,
   * group, stem, membership, member, groupAttribute, 
   * stemAttribute, membershipAttribute, memberAttribute
   * @param assignableTo1
   */
  public void setAssignableTo(AttributeDefAssignableTo assignableTo1) {
    this.assignableTo = assignableTo1;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * id of this attribute def
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }
  
  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtension() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @return extension
   */
  public String getExtensionDb() {
    return extension;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * extension of attribute expireTime
   * @param extension1
   */
  public void setExtensionDb(String extension1) {
    this.extension = extension1;
  }

  /**
   * 
   * @return the name for hibernate
   */
  public String getNameDb() {
    return name;
  }

  /**
   * 
   * @param name1
   */
  public void setNameDb(String name1) {
    this.name = name1;
  }
  

  /**
   * Get group name.
   * @return  Group name.
   * @throws  GrouperException
   */
  public String getName() throws GrouperException  {

    if (StringUtils.isBlank(this.name)) {
      LOG.error( "attributeDef is blank");
      throw new GrouperException("attributeDef is blank");
    }
    return this.name;
  } // public String getName()

  /**
   * Set attributeDef <i>name</i>.  This should not be called
   * @param   value   Set <i>extension</i> to this value.
   */
  public void setName(String value) {
    throw new InsufficientPrivilegeException("group name is system maintained: " + this.name + ", " + value);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AttributeDef)) {
      return false;
    }
    return StringUtils.equals(this.id, ((AttributeDef)obj).id);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return StringUtils.defaultString(this.id).hashCode();
  }


  /**
   * @see edu.internet2.middleware.grouper.misc.Owner#getUuid()
   */
  public String getUuid() {
    return this.getId();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "name", this.name)
      .append( "uuid", this.getId() )
      .toString();
  }

}
