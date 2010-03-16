/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefName;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;


/**
 * definition of an attribute name (is linked with an attribute def)
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class AttributeDefName extends GrouperAPI 
    implements GrouperHasContext, Hib3GrouperVersioned, GrouperSetElement, XmlImportable<AttributeDefName> {

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

  /** cache the attributeDef */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private transient AttributeDef attributeDef;

  /**
   * get the attribute def
   * @return the attribute def
   */
  public AttributeDef getAttributeDef() {
    if (this.attributeDef == null) {
      this.attributeDef = AttributeDefFinder.findById(this.attributeDefId, true);
    }
    return this.attributeDef;
  }
  
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
    return this.stemId;
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
    return this.id;
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
    return this.name;
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
    return this.name;
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
    return this.description;
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
    return this.displayExtension;
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
    return this.displayName;
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
    return this.extension;
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
    return this.extension;
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
    return this.displayExtension;
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
    return this.displayName;
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
    return this.attributeDefId;
  }

  /**
   * attribute def id that this is related to
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
    this.attributeDef = null;
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeDefName.class);

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

  /**
   * delegate logic about attribute def name sets to this object
   */
  private AttributeDefNameSetDelegate attributeDefNameSetDelegate;
  
  /**
   * delegate logic about attribute def name sets to this object 
   * @return the delegate
   */
  public AttributeDefNameSetDelegate getAttributeDefNameSetDelegate() {
    if (this.attributeDefNameSetDelegate == null) {
      this.attributeDefNameSetDelegate = new AttributeDefNameSetDelegate(this);
    }
    return this.attributeDefNameSetDelegate;
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

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AttributeDefName)) {
      return false;
    }
    return StringUtils.equals(this.getName(), ( (AttributeDefName) other ).getName() );
  } // public boolean equals(other)

  /**
   * @return hashcode
   * @since   1.2.0
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getName() )
      .toHashCode();
  } // public int hashCode()

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeDefName existingRecord) {
    existingRecord.setAttributeDefId(this.getAttributeDefId());
    existingRecord.setDescription(this.getDescription());
    existingRecord.setDisplayExtensionDb(this.getDisplayExtensionDb());
    existingRecord.setDisplayNameDb(this.getDisplayNameDb());
    existingRecord.setExtensionDb(this.getExtensionDb());
    existingRecord.setId(this.getId());
    existingRecord.setNameDb(this.getNameDb());
    existingRecord.setStemId(this.getStemId());
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeDefName other) {
    if (!StringUtils.equals(this.attributeDefId, other.attributeDefId)) {
      return true;
    }
    if (!StringUtils.equals(this.description, other.description)) {
      return true;
    }
    if (!StringUtils.equals(this.displayExtension, other.displayExtension)) {
      return true;
    }
    if (!StringUtils.equals(this.displayName, other.displayName)) {
      return true;
    }
    if (!StringUtils.equals(this.extension, other.extension)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.name, other.name)) {
      return true;
    }
    if (!StringUtils.equals(this.stemId, other.stemId)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeDefName other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (this.createdOnDb != other.createdOnDb) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    if (this.lastUpdatedDb != other.lastUpdatedDb) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public AttributeDefName xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(this.id, this.name, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeDefName xmlSaveBusinessProperties(AttributeDefName existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      Stem parent = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.stemId, true);
      existingRecord = parent.internal_addChildAttributeDefName(GrouperSession.staticGrouperSession(), 
          this.getAttributeDef(), this.extension, this.displayExtension, this.id, this.description);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    GrouperDAOFactory.getFactory().getAttributeDefName().saveOrUpdate(existingRecord);
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeDefName().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeDefName xmlToExportAttributeDefName(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    XmlExportAttributeDefName xmlExportAttributeDefName  = new XmlExportAttributeDefName();
    
    xmlExportAttributeDefName.setAttributeDefId(this.getAttributeDefId());
    xmlExportAttributeDefName.setContextId(this.getContextId());
    xmlExportAttributeDefName.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeDefName.setDescription(this.getDescription());
    xmlExportAttributeDefName.setDisplayExtension(this.getDisplayExtension());
    xmlExportAttributeDefName.setDisplayName(this.getDisplayName());
    xmlExportAttributeDefName.setExtension(this.getExtension());
    xmlExportAttributeDefName.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeDefName.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeDefName.setName(this.getName());
    xmlExportAttributeDefName.setParentStem(this.getStemId());
    xmlExportAttributeDefName.setUuid(this.getId());

    return xmlExportAttributeDefName;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AttributeDefName: " + this.getId() + ", " + this.getName());

//    XmlExportUtils.toStringAttributeDefName(null, stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

}
