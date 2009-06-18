/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * definition of an attribute
 * @author mchyzer
 *
 */
public class AttributeDef extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDef clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /** id of this attribute def */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /** type that this attribute value is assignable to */
  private AttributeDefAssignableTo assignableTo;
  
  /** stem that this attribute is in */
  private String stemId;

  /**
   * time in millis when this attribute was last modified
   */
  private long modifyTime = 0;

  /**
   * time in millis when this attribute was created
   */
  private long createTime = 0;

  /**
   * member id of the member who created this 
   */
  private String creatorId;

  /**
   * description of attribute, friendly description, e.g. in sentence form, 
   * about what the attribute is about 
   */
  private String description;

  /**
   * extension of attribute expireTime
   */
  private String extension;

  /**
   * member id of the person who last modified this attribute
   */
  private String modifierId;

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
   * @return
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
   * group that can see that the attribute def exists
   * "public" for public
   * @return group
   */
  public String getSecurityUpdateGroup() {
    return securityUpdateGroup;
  }

  /**
   * group that can see that the attribute def exists
   * "public" for public
   * @param securityUpdateGroup1
   */
  public void setSecurityUpdateGroup(String securityUpdateGroup1) {
    this.securityUpdateGroup = securityUpdateGroup1;
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
   * type that this attribute value is assignable to AttributeDefAssignTo
   * @return attribute value
   */
  public String getAssignableToDb() {
    return this.assignableTo == null ? null : this.assignableTo.name();
  }
  
  /**
   * if this attribute can be assigned to the same verb to the same object more than once
   */
  private boolean multiAssignable;
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   */
  private boolean multiValued;

  /** 
   * group that can read data from this attribute (blank for default behavior)
   * "public" for public
   */
  private String securityReadGroup;
  
  /** 
   * group that can write data from this attribute (blank for default behavior)
   * "public" for public
   */
  private String securityUpdateGroup;
  
  /**
   * group that can admin data from this attribute (blank for default behavior).
   * "public" for public
   */
  private String securityAdminGroup;
  
  /**
   * group that can see that the attribute def exists
   * "public" for public
   */
  private String securityViewGroup;
  
  /**
   * comma separated list of verbs for this attribute.  at least there needs to be one.
   * default is "assign"  verbs must contain only alphanumeric or underscore, case sensitive
   * e.g. read,write,admin
   */
  private String verbs = "assign";
  
  /**
   * type of the value,  int, double, string, marker
   */
  private AttributeDefValueType valueType;
  
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
   * comma separated list of verbs for this attribute.  at least there needs to be one.
   * default is "assign"  verbs must contain only alphanumeric or underscore, case sensitive
   * e.g. read, write, admin
   * @return the verbs
   */
  public String getVerbs() {
    if (StringUtils.isBlank(this.verbs)) {
      this.verbs = "assign";
    }
    return verbs;
  }

  /**
   * get the verbs for this attribute def in an array
   * @return the array of verbs
   */
  public String[] getVerbsArray() {
    return GrouperUtil.splitTrim(this.verbs, ",");
  }
  
  /**
   * comma separated list of verbs for this attribute.  at least there needs to be one.
   * default is "assign" verbs must contain only alphanumeric or underscore, case sensitive
   * e.g. read,write,admin
   * @param verbs
   */
  public void setVerbs(String verbs) {
    this.verbs = verbs;
    if (StringUtils.isBlank(this.verbs)) {
      this.verbs = "assign";
    }
  }

  /**
   * if this attribute can be assigned to the same verb to the same object more than once
   * @return if multiassignable
   */
  public boolean isMultiAssignable() {
    return multiAssignable;
  }
  
  /**
   * if this attribute can be assigned to the same verb to the same object more than once
   * convert to string for hibernate
   * @return the string value
   */
  public String getMultiAssignableDb() {
    return this.multiAssignable ? "T" : "F";
  }

  /**
   * if this attribute can be assigned to the same verb to the same object more than once
   * convert to string for hibernate
   * @param multiAssignableDb
   */
  public void setMultiAssignableDb(String multiAssignableDb) {
    this.multiAssignable = GrouperUtil.booleanValue(multiAssignableDb, false);
  }
  
  /**
   * if this attribute can be assigned to the same verb to the same object more than once
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
   * @return
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
    this.multiValued = GrouperUtil.booleanValue(this.multiValued, false);
  }
  
  /**
   * if more than one value (same type) can be assigned to the attribute assignment
   * @param multiValued1
   */
  public void setMultiValued(boolean multiValued1) {
    this.multiValued = multiValued1;
  }

  /**
   * type that this attribute value is assignable to AttributeDefAssignTo
   * @param assignableToString
   */
  public void setAssignableToDb(String assignableToString) {
    this.assignableTo = AttributeDefAssignableTo.valueOfIgnoreCase(assignableToString, 
        false);
  }
  
  /**
   * type that this attribute value is assignable to
   * @return the type
   */
  public AttributeDefAssignableTo getAssignableTo() {
    return assignableTo;
  }

  /**
   * type that this attribute value is assignable to
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

  
  public long getModifyTime() {
    return modifyTime;
  }

  
  public void setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
  }

  
  /**
   * time in millis when this attribute was created
   * @return the create time
   */
  public long getCreateTime() {
    return createTime;
  }

  /**
   * time in millis when this attribute was created
   * @param createTime1
   */
  public void setCreateTime(long createTime1) {
    this.createTime = createTime1;
  }

  /**
   * member id of the member who created this 
   * @return the creator id
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * member id of the member who created this 
   * @param creatorId1
   */
  public void setCreatorId(String creatorId1) {
    this.creatorId = creatorId1;
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
   * @param extension1
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * member id of the person who last modified this attribute
   * @return modifier id
   */
  public String getModifierId() {
    return modifierId;
  }

  /**
   * member id of the person who last modified this attribute
   * @param modifierId1
   */
  public void setModifierId(String modifierId1) {
    this.modifierId = modifierId1;
  }

  /**
   * group that can read data from this attribute (blank for default behavior)
   * @return security read group
   */
  public String getSecurityReadGroup() {
    return securityReadGroup;
  }

  /**
   * group that can read data from this attribute (blank for default behavior)
   * @param securityReadGroup1
   */
  public void setSecurityReadGroup(String securityReadGroup1) {
    this.securityReadGroup = securityReadGroup1;
  }

  /**
   * group that can admin this attribute def (blank for default behavior)
   * "public" for public
   * @return group
   */
  public String getSecurityAdminGroup() {
    return this.securityAdminGroup;
  }

  /**
   * group that can admin this attribute def (blank for default behavior)
   * "public" for public
   * @param securityAdminGroup1
   */
  public void setSecurityAdminGroup(String securityAdminGroup1) {
    this.securityAdminGroup = securityAdminGroup1;
  }

  /**
   * group that can see that the attribute def exists
   * "public" for public
   * @return the security view group
   */
  public String getSecurityViewGroup() {
    return this.securityViewGroup;
  }

  /**
   * group that can see that the attribute def exists
   * "public" for public
   * @param securityViewGroup1
   */
  public void setSecurityViewGroup(String securityViewGroup1) {
    this.securityViewGroup = securityViewGroup1;
  }
  
}
