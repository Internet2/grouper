/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * definition of an attribute name (is linked with an attribute def)
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class AttributeDefName extends GrouperAPI 
    implements GrouperHasContext, Hib3GrouperVersioned, GrouperSetElement {

  /** name of the groups attribute def name table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF_NAME = "grouper_attribute_def_name";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_DESCRIPTION = "description";

  /** column */
  public static final String COLUMN_EXTENSION = "extension";

  /** column */
  public static final String COLUMN_NAME = "name";

  /** column */
  public static final String COLUMN_DISPLAY_EXTENSION = "display_extension";

  /** column */
  public static final String COLUMN_DISPLAY_NAME = "display_name";

  /** column */
  public static final String COLUMN_STEM_ID = "stem_id";

  /** column */
  public static final String COLUMN_ID = "id";


  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: displayExtension */
  public static final String FIELD_DISPLAY_EXTENSION = "displayExtension";

  /** constant for field name for: displayName */
  public static final String FIELD_DISPLAY_NAME = "displayName";

  /** constant for field name for: extension */
  public static final String FIELD_EXTENSION = "extension";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_NAME, FIELD_STEM_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_NAME, FIELD_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeDefName clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /** id of this attribute def name */
  private String id;

  /** id of this attribute def  */
  private String attributeDefId;

  /** context id of the transaction */
  private String contextId;

  /** stem that this attribute is in */
  private String stemId;

  /**
   * name of attribute, e.g. school:community:students:expireDate 
   */
  private String name;

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
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

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
   * id of this attribute def name
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * 
   * @param name1
   */
  public void setName(@SuppressWarnings("unused") String name1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * 
   * @return the name
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
  public void setDisplayExtension(@SuppressWarnings("unused") String displayExtension1) {
    throw new RuntimeException("Dont call this method");
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
  public void setDisplayName(@SuppressWarnings("unused") String displayName1) {
    throw new RuntimeException("Dont call this method");
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
  public void setExtension(@SuppressWarnings("unused") String extension1) {
    throw new RuntimeException("Dont call this method");
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
  public void setExtensionDb(String extension1) {
    this.extension = extension1;
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
   * displayExtension of attribute, e.g. Expire Date
   * @return display extension
   */
  public String getDisplayExtensionDb() {
    return displayExtension;
  }

  /**
   * displayExtension of attribute, e.g. Expire Date
   * @param displayExtension1
   */
  public void setDisplayExtensionDb(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @return display name
   */
  public String getDisplayNameDb() {
    return displayName;
  }

  /**
   * displayName of attribute, e.g. My School:Community Groups:Expire Date 
   * @param displayName1
   */
  public void setDisplayNameDb(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * attribute definition that this is related to
   * @return the attribute def id
   */
  public String getAttributeDefId() {
    return attributeDefId;
  }

  /**
   * attribute def id that this is related to
   * @param attributeDefId
   */
  public void setAttributeDefId(String attributeDefId) {
    this.attributeDefId = attributeDefId;
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeDefName.class);

  /**
   * 
   * @param newAttributeDefName
   * @return true if added, false if already there
   */
  public boolean addToAttributeDefNameSet(AttributeDefName newAttributeDefName) {
    return GrouperSetEnum.ATTRIBUTE_SET.addToGrouperSet(this, newAttributeDefName);
  }

  /**
   * 
   * @param attributeDefNameToRemove
   * @return true if removed, false if already removed
   */
  public boolean removeFromAttributeDefNameSet(AttributeDefName attributeDefNameToRemove) {
    return GrouperSetEnum.ATTRIBUTE_SET.removeFromGrouperSet(this, attributeDefNameToRemove);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetElement#__getId()
   */
  public String __getId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSetElement#__getName()
   */
  public String __getName() {
    return this.getName();
  }
  
  /**
   * save or update this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeDefName().delete(this);
  }

  
}
