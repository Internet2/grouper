/**
 * Copyright 2014 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefName;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;
import edu.internet2.middleware.subject.Subject;


/**
 * definition of an attribute name (is linked with an attribute def)
 * @author mchyzer
 */
@SuppressWarnings("serial")
public class AttributeDefName extends GrouperAPI 
    implements GrouperHasContext, Hib3GrouperVersioned, GrouperSetElement, 
    XmlImportable<AttributeDefName>, Comparable<GrouperObject>, GrouperObject {

  /**
   * 
   */
  public static final String VALIDATION_NAME_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY = "nameOfAttributeDefNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_DISPLAY_NAME_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY = "displayNameOfAttributeDefNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_DECRIPTION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY = "decriptionOfAttributeDefNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_EXTENSION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY = "extensionOfAttributeDefNameTooLong";

  /**
   * 
   */
  public static final String VALIDATION_DISPLAY_EXTENSION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY = "displayExtensionOfAttributeDefNameTooLong";

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

  /** unique number for this attributeDefName */
  public static final String COLUMN_ID_INDEX = "id_index";

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

  /** constant for field name for: idIndex */
  public static final String FIELD_ID_INDEX = "idIndex";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DESCRIPTION, 
      FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, FIELD_ID, FIELD_ID_INDEX, 
      FIELD_LAST_UPDATED_DB, FIELD_NAME, FIELD_STEM_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DB_VERSION, 
      FIELD_DESCRIPTION, FIELD_DISPLAY_EXTENSION, FIELD_DISPLAY_NAME, FIELD_EXTENSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_ID_INDEX, FIELD_LAST_UPDATED_DB, FIELD_NAME, 
      FIELD_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * cache of multikey (attributeDefId, sourceId, subjectId) to true or false if allowed to admin attributeDefId
   */
  static GrouperCache<MultiKey, Boolean> canAdminAttributeDef = null;
  
  /**
   * 
   * @return
   */
  static GrouperCache<MultiKey, Boolean> canAdminAttributeDef() {
    if (canAdminAttributeDef == null) {
      canAdminAttributeDef = new GrouperCache<MultiKey, Boolean>(
          AttributeDefName.class.getName() + ".CanAdminAttributeDef", 5000, false, 5, 5, false);
    }
    return canAdminAttributeDef;
  }

  /**
   * make sure this attribute def can admin from grouper session
   */
  public void assertCanAdminAttributeDefStatic() {
    
    Subject subject = GrouperSession.staticGrouperSession().getSubject();
    MultiKey cacheKey = new MultiKey(this.attributeDefId, subject.getSourceId(), subject.getId());
    Boolean result = canAdminAttributeDef().get(cacheKey);
    AttributeDef attributeDef = null;
    
    //if not in cache, calculate
    if (result == null) {
      attributeDef = this.getAttributeDef();
      result = attributeDef.getPrivilegeDelegate().canAttrAdmin(subject);
      
      //add back to cache since wasnt cached
      canAdminAttributeDef().put(cacheKey, result);
    }
    
    //false means cant admin
    if (!result) {
      
      attributeDef = attributeDef == null ? this.getAttributeDef() : attributeDef;
      
      throw new InsufficientPrivilegeException(GrouperUtil
          .subjectToString(subject)
          + " is not attrAdmin on attributeDef: " + attributeDef.getName() 
          + ", concerning attributeDefName: " + this.getName());
    }
    
  }



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
   * stem that this attribute is in
   * @return the stem id
   */
  public Stem getStem() {
    return this.stemId == null ? null : StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.stemId, true);
  }

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
   * this method makes this class more compatible with Group and Stem
   * @return the parent stem id
   */
  public String getParentUuid() {
    return this.stemId;
  }

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
   * @return id
   */
  public String getUuid() {
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
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

        hibernateSession.setCachingEnabled(false);
        
        //make sure subject is allowed to do this
        Subject subject = GrouperSession.staticGrouperSession().getSubject();
        AttributeDef attributeDef2 = AttributeDefName.this.getAttributeDef();
        if (!attributeDef2.getPrivilegeDelegate().canAttrAdmin(subject)) {
          throw new InsufficientPrivilegeException(GrouperUtil
              .subjectToString(subject)
              + " is not attrAdmin on attributeDef: " + attributeDef2.getName());
        }
        
        //we need to find all sets related to this node, and delete them (in reverse order since
        //parents might point back)
        Set<AttributeDefNameSet> attributeDefNameSets = GrouperDAOFactory.getFactory()
          .getAttributeDefNameSet()
          .findByIfThenHasAttributeDefNameId(AttributeDefName.this.id, 
              AttributeDefName.this.id);

        List<AttributeDefNameSet> attributeDefNameSetsList = 
          new ArrayList<AttributeDefNameSet>(attributeDefNameSets);
        
        //sort in reverse depth
        Collections.sort(attributeDefNameSetsList, new Comparator<AttributeDefNameSet>() {
          
          /**
           * compare two items
           * @param first first item
           * @param second item
           * @return -1, 0, 1
           */
          public int compare(AttributeDefNameSet first, AttributeDefNameSet second) {
            if (first == second) {
              return 0;
            }
            if (first == null) {
              return -1;
            }
            if (second == null) {
              return 1;
            }
            return ((Integer)first.getDepth()).compareTo(second.getDepth());
          }
        });
        
        Collections.reverse(attributeDefNameSetsList);
        
        for(AttributeDefNameSet attributeDefNameSet : attributeDefNameSetsList) {
          //I believe mysql has problems deleting self referential foreign keys
          attributeDefNameSet.setParentAttrDefNameSetId(null);
          attributeDefNameSet.saveOrUpdate();
          attributeDefNameSet.delete();
        }

        //delete any attributes on this stem, this is done as root
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByAttributeDefNameId(AttributeDefName.this.getId());
            
            //delete all assignments
            for(AttributeAssign attributeAssign : attributeAssigns) {
              attributeAssign.delete();
            }
            
            return null;
          }
        });

        GrouperDAOFactory.getFactory().getAttributeDefName().delete(AttributeDefName.this);
        
        if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE, "id", 
              AttributeDefName.this.getId(), "name", AttributeDefName.this.getName(), 
              "displayName", AttributeDefName.this.getDisplayName(),
              "description", AttributeDefName.this.getDescription(),
              "parentStemId", AttributeDefName.this.getStemId(), 
              "parentAttributeDefId", attributeDef2.getId(),
              "parentAttributeDefName", attributeDef2.getName());
          
          auditEntry.setDescription("Deleted attributeDefName: " + AttributeDefName.this.getName());
          auditEntry.saveOrUpdate(true);
        }

        
        return null;
      }
    });
    
  }

  /**
   * delegate logic about attribute def name sets to this object
   */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDefNameSetDelegate attributeDefNameSetDelegate;

  /** id of the group as a unique integer */
  private Long idIndex;
  
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
    // Bypass privilege checks.  If the attributedef name is loaded it is viewable.
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
    if (!StringUtils.equals(StringUtils.trimToNull(this.description), StringUtils.trimToNull(other.description))) {
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
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public AttributeDefName xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(this.id, this.name, false,
        new QueryOptions().secondLevelCache(false));
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
      if (this.idIndex != null) {
        existingRecord.assignIdIndex(this.idIndex);
      }
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
    xmlExportAttributeDefName.setIdIndex(this.getIdIndex());
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

  /**
   * store this group (update) to database
   */
  public void store() {
    
    validate();

    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            //make sure subject is allowed to do this
            Subject subject = GrouperSession.staticGrouperSession().getSubject();
            AttributeDef attributeDef2 = AttributeDefName.this.getAttributeDef();
            if (!attributeDef2.getPrivilegeDelegate().canAttrAdmin(subject)) {
              throw new InsufficientPrivilegeException(GrouperUtil
                  .subjectToString(subject)
                  + " is not attrAdmin on attributeDef: " + attributeDef2.getName());
            }
            
            String differences = GrouperUtil.dbVersionDescribeDifferences(AttributeDefName.this.dbVersion(), 
                AttributeDefName.this, AttributeDefName.this.dbVersion() != null ? AttributeDefName.this.dbVersionDifferentFields() : AttributeDefName.CLONE_FIELDS);

            GrouperDAOFactory.getFactory().getAttributeDefName().saveOrUpdate(AttributeDefName.this);
            
            if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
              AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE, "id", 
                  AttributeDefName.this.getId(), "name", AttributeDefName.this.getName(), 
                  "displayName", AttributeDefName.this.getDisplayName(),
                  "description", AttributeDefName.this.getDescription(),
                  "parentStemId", AttributeDefName.this.getStemId(), 
                  "parentAttributeDefId", attributeDef2.getId(),
                  "parentAttributeDefName", attributeDef2.getName());
              
              auditEntry.setDescription("Updated attributeDefName: " + AttributeDefName.this.getName() + ", " + differences);
              auditEntry.saveOrUpdate(true);
            }
            return null;
          }
        });
  }



  /**
   * 
   */
  public void validate() {
    //lets validate
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
    //        AttributeDefName.COLUMN_NAME, Types.VARCHAR, ddlVersionBean.isSqlServer() ? "900" : "1024", false, true);
    boolean sqlServer = GrouperDdlUtils.isSQLServer();
    int maxNameLength = sqlServer ? 900 : 1024;
    maxNameLength = GrouperConfig.retrieveConfig().propertyValueInt("grouper.nameOfAttributeDefName.maxSize", maxNameLength);

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
    //        AttributeDefName.COLUMN_EXTENSION, Types.VARCHAR, "255", false, true);
    if (GrouperUtil.lengthAscii(this.getExtension()) > 255) {
      throw new GrouperValidationException("Extension of attributeDefName too long: " + GrouperUtil.lengthAscii(this.getExtension()), 
          VALIDATION_EXTENSION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY, 255, GrouperUtil.lengthAscii(this.getExtension()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
    //        AttributeDefName.COLUMN_DISPLAY_EXTENSION, Types.VARCHAR, "128", false, true);
    if (GrouperUtil.lengthAscii(this.getDisplayExtension()) > 128) {
      throw new GrouperValidationException("Display extension of attributeDefName too long: " + GrouperUtil.lengthAscii(this.getDisplayExtension()), 
          VALIDATION_DISPLAY_EXTENSION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY, 128, GrouperUtil.lengthAscii(this.getDisplayExtension()));
    }
    
    if (GrouperUtil.lengthAscii(this.getName()) > maxNameLength) {
      throw new GrouperValidationException("Name of attributeDefName too long: " + GrouperUtil.lengthAscii(this.getName()), 
          VALIDATION_NAME_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY, maxNameLength, GrouperUtil.lengthAscii(this.getName()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
    //        AttributeDefName.COLUMN_DISPLAY_NAME, Types.VARCHAR, ddlVersionBean.isSqlServer() ? "900" : "1024", false, true);
    if (GrouperUtil.lengthAscii(this.getDisplayName()) > maxNameLength) {
      throw new GrouperValidationException("Display name of attributeDefName too long: " + GrouperUtil.lengthAscii(this.getDisplayName()), 
          VALIDATION_DISPLAY_NAME_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY, maxNameLength, GrouperUtil.lengthAscii(this.getDisplayName()));
    }

    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(attributeDefNameTable,
    //        AttributeDefName.COLUMN_DESCRIPTION, Types.VARCHAR, "1024", false, false);
    if (GrouperUtil.lengthAscii(this.getDescription()) > 1024) {
      throw new GrouperValidationException("Description of attributeDefName too long: " + GrouperUtil.lengthAscii(this.getDescription()), 
          VALIDATION_DECRIPTION_OF_ATTRIBUTE_DEF_NAME_TOO_LONG_KEY, 1024, GrouperUtil.lengthAscii(this.getDescription()));
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
  
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_DELETE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_DELETE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_DELETE, false, true);
  
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type
      
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_DELETE, HooksGroupTypeBean.class,
          groupType, GroupType.class);

      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_DELETE, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_DELETE, false, true);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
  
    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_INSERT, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_INSERT, true, false);
  
    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_INSERT, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class);
  
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type

      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_INSERT, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_INSERT, true, false);

      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_INSERT, HooksGroupTypeBean.class,
          groupType, GroupType.class);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    
    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_UPDATE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class);
  
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_POST_UPDATE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_UPDATE, true, false);
  
  
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type
      
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_UPDATE, HooksGroupTypeBean.class,
          groupType, GroupType.class);

      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_POST_UPDATE, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_UPDATE, true, false);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
  
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type
      
      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_PRE_DELETE, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_DELETE, false, false);
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_PRE_DELETE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_DELETE, false, false);
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE, 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.name.name(), this.getName(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.stemId.name(), this.getStemId(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.description.name(), this.getDescription(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.attributeDefId.name(), this.getAttributeDefId()).save();
    
    Hib3AttributeDefNameDAO.attributeDefNameCacheRemove(this);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    this.lastUpdatedDb = System.currentTimeMillis();
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }

    if (this.idIndex == null) {
      this.idIndex = TableIndex.reserveId(TableIndexType.attributeDefName);
    }

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_PRE_INSERT, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_INSERT, false, false);
    
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type
    
      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_PRE_INSERT, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_INSERT, false, false);
    }
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD, 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name.name(), this.getName(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId.name(), this.getStemId(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.description.name(), this.getDescription(), 
        ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId.name(), this.getAttributeDefId()).save();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_DEF_ID)) {
      throw new RuntimeException("cannot update attributeDefId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_STEM_ID)) {
      throw new RuntimeException("cannot update stemId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_NAME) || this.dbVersionDifferentFields().contains(FIELD_EXTENSION)) {
      // don't allow renames for legacy attributes
      String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
      String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
      String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
      String customListPrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.customList.prefix");
      
      String oldName = this.dbVersion().getNameDb();
      if (oldName.startsWith(stemName + ":" + groupTypePrefix) ||
          oldName.startsWith(stemName + ":" + attributePrefix) ||
          oldName.startsWith(stemName + ":" + customListPrefix)) {
        throw new RuntimeException("cannot update name for legacy attributes");
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.ATTRIBUTE_DEF_NAME, 
        AttributeDefNameHooks.METHOD_ATTRIBUTE_DEF_NAME_PRE_UPDATE, HooksAttributeDefNameBean.class, 
        this, AttributeDefName.class, VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_UPDATE, false, false);
  
    
    GroupType groupType = GroupType.internal_getGroupType(this, false);
    if (groupType != null) {
      // this is a legacy group type
      
      GrouperHooksUtils.callHooksIfRegistered(groupType, GrouperHookType.GROUP_TYPE,
          GroupTypeHooks.METHOD_GROUP_TYPE_PRE_UPDATE, HooksGroupTypeBean.class,
          groupType, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_UPDATE, false, false);
    }
    
    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id.name(), this.getId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name.name(), this.getName(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.stemId.name(), this.getStemId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.description.name(), this.getDescription(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.attributeDefId.name(), this.getAttributeDefId()),
        GrouperUtil.toList("name", "description"),
        GrouperUtil.toList(
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name.name(),
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.description.name())); 
    
    Hib3AttributeDefNameDAO.attributeDefNameCacheRemove(this);

  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public AttributeDefName dbVersion() {
    return (AttributeDefName)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }

  /**
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(GrouperObject that) {
    if (that==null) {
      return 1;
    }
    String thisName = StringUtils.defaultString(this.getName());
    String thatName = StringUtils.defaultString(that.getName());
    return thisName.compareTo(thatName);
  }



  /**
   * id of the group as a unique integer
   * @return id
   */
  public Long getIdIndex() {
    return this.idIndex;
  }



  /**
   * id of the group as a unique integer
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
  }
  
  /**
   * assign different id index
   * @param theIdIndex
   * @return if it was changed
   */
  public boolean assignIdIndex(final long theIdIndex) {

    TableIndex.assertCanAssignIdIndex();

    boolean needsSave = false;
    synchronized (TableIndexType.attributeDefName) {

      //ok, if the index is not in use (not, it could be reserved... hmmm)
      AttributeDefName tempAttributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdIndex(theIdIndex, false, null);
      if (tempAttributeDefName == null) {
        
        this.setIdIndex(theIdIndex);
        TableIndex.clearReservedId(TableIndexType.attributeDefName, theIdIndex);
        needsSave = true;
        
        //do a new session so we don hold on too long
        HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            //now we might need to increment the index
            TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().findByType(TableIndexType.attributeDefName);
            if (tableIndex != null && tableIndex.getLastIndexReserved() < theIdIndex) {
              tableIndex.setLastIndexReserved(theIdIndex);
              tableIndex.saveOrUpdate();
            }
            return null;
          }
        });
      }      
    }
    return needsSave;
  }

  /**
   * set this for caching
   * @param attributeDef1
   */
  public void internalSetAttributeDef(AttributeDef attributeDef1) {
    
    if (attributeDef1 != null) {
      if (!StringUtils.equals(this.attributeDefId, attributeDef1.getId())) {
        throw new RuntimeException("Why does the attributeDef id " 
            + this.attributeDefId + " not equal the param id: " + attributeDef1.getId());
      }
    }
    
    this.attributeDef = attributeDef1;
  }
  
  /**
   * @param exceptionIfNotLegacyAttribute
   * @return legacy attribute name
   */
  public String getLegacyAttributeName(boolean exceptionIfNotLegacyAttribute) {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
    
    String prefix = stemName + ":" + attributePrefix;
    if (this.name.startsWith(prefix)) {
      return this.name.substring(prefix.length());
    } 

    if (exceptionIfNotLegacyAttribute) {
      throw new RuntimeException("Not legacy attribute");
    }
      
    return null;
  }
  
  /**
   * @param exceptionIfNotLegacyGroupType
   * @return legacy attribute name
   */
  public String getLegacyGroupTypeName(boolean exceptionIfNotLegacyGroupType) {
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String groupTypePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.groupType.prefix");
    
    String prefix = stemName + ":" + groupTypePrefix;
    if (this.name.startsWith(prefix)) {
      return this.name.substring(prefix.length());
    } 

    if (exceptionIfNotLegacyGroupType) {
      throw new RuntimeException("Not legacy group type");
    }
      
    return null;
  }
  
  /**
   * @see GrouperObject#matchesLowerSearchStrings(Set)
   */
  @Override
  public boolean matchesLowerSearchStrings(Set<String> filterStrings) {

    if (GrouperUtil.length(filterStrings) == 0) {
      return true;
    }

    String lowerId = this.getId().toLowerCase();
    String lowerName = StringUtils.defaultString(this.getName()).toLowerCase();
    String lowerDisplayName = StringUtils.defaultString(this.getDisplayName()).toLowerCase();
    String lowerDescription = StringUtils.defaultString(this.getDescription()).toLowerCase();
    
    for (String filterString : GrouperUtil.nonNull(filterStrings)) {
      
      //if all dont match, return false
      if (!lowerId.contains(filterString)
          && !lowerName.contains(filterString)
          && !lowerDisplayName.contains(filterString)
          && !lowerDescription.contains(filterString)) {
        return false;
      }
      
    }
    return true;
  }



  /**
   * @see edu.internet2.middleware.grouper.misc.GrouperObject#getParentStem()
   */
  public Stem getParentStem() {
    return this.getStem();
  }

}
