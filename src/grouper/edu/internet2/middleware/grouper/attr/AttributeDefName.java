/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * definition of an attribute
 * @author mchyzer
 *
 */
public class AttributeDefName extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {

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
  public AttributeDefName clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /** id of this attribute def */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /** stem that this attribute is in */
  private String stemId;

  /**
   * time in millis when this attribute was last modified
   */
  private long modifyTime = 0;

  /**
   * name of attribute, e.g. school:community:students:expireDate 
   */
  private String name;

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
   * displayExtension of attribute, e.g. Expire Date
   */
  private String displayExtension;

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   */
  private String displayName;

  /**
   * extension of attribute expireTime
   */
  private String extension;

  /**
   * member id of the person who last modified this attribute
   */
  private String modifierId;

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

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
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
   * displayExtension of attribute, e.g. Expire Date
   * @return display extension
   */
  public String getDisplayExtension() {
    return displayExtension;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
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
  
}
