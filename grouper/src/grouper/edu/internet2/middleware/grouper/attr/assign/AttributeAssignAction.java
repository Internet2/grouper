/**
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
 */
/**
 * @author mchyzer
 * $Id: AttributeAssignAction.java,v 1.3 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignAction;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;


/**
 *
 */
@SuppressWarnings("serial")
public class AttributeAssignAction extends GrouperAPI 
  implements GrouperHasContext, Hib3GrouperVersioned, GrouperSetElement, XmlImportable<AttributeAssignAction> {

  /**
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    this.lastUpdatedDb = System.currentTimeMillis();
       
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.name.name(), this.getName(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId.name(), this.getAttributeDefId()).save();
  }
  
  /**
   * @see GrouperAPI#onPreDelete(HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.name.name(), this.getName(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.attributeDefId.name(), this.getAttributeDefId()).save();
  }

  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_ATTRIBUTE_DEF_ID)) {
      throw new RuntimeException("cannot update attributeDefId");
    }
    
    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id.name(), this.getId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.name.name(), this.getName(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.attributeDefId.name(), this.getAttributeDefId()),
        GrouperUtil.toList("name"),
        GrouperUtil.toList(
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.name.name())); 
  }

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_NAME);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DB_VERSION, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  
  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_ID = "attribute_def_id";
  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";
  /** column */
  public static final String COLUMN_ID = "id";
  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";
  /** column */
  public static final String COLUMN_NAME = "name";

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeDefName.class);
  /** name of the groups attribute assign action table in the db */
  public static final String TABLE_GROUPER_ATTR_ASSIGN_ACTION = "grouper_attr_assign_action";
  /** context id of the transaction */
  private String contextId;
  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;
  /** id of this attribute def name */
  private String id;
  
  /** id of the attribute def that this action is possible for */
  private String attributeDefId;
  
  /**set delegate */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignActionSetDelegate attributeAssignActionSetDelegate;
  
  
  /**
   * @return the attributeAssignActionSetDelegate
   */
  public AttributeAssignActionSetDelegate getAttributeAssignActionSetDelegate() {
    if (this.attributeAssignActionSetDelegate == null) {
      this.attributeAssignActionSetDelegate = new AttributeAssignActionSetDelegate(this);
    }
    return this.attributeAssignActionSetDelegate;
  }


  /**
   * id of the attribute def that this action is possible for
   * @return the attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * attribute def that this action is possible for
   * @return the attributeDef
   */
  public AttributeDef getAttributeDef() {
    return GrouperDAOFactory.getFactory().getAttributeDef().findById(this.attributeDefId, true);
  }

  
  /**
   * id of the attribute def that this action is possible for
   * @param attributeDefId1 the attributeDefId to set
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;
  /**
   * name of attribute, e.g. school:community:students:expireDate 
   */
  private String name;

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

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeAssignAction clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * save or update this object
   */
  public void delete() {
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              hibernateSession.setCachingEnabled(false);
              
              //we need to find all sets related to this node, and delete them (in reverse order since
              //parents might point back)
              Set<AttributeAssignActionSet> attributeAssignActionSets = GrouperDAOFactory.getFactory()
                .getAttributeAssignActionSet()
                .findByIfThenHasAttributeAssignActionId(AttributeAssignAction.this.id, 
                    AttributeAssignAction.this.id);

              List<AttributeAssignActionSet> attributeAssignActionSetsList = 
                new ArrayList<AttributeAssignActionSet>(attributeAssignActionSets);
              
              //sort in reverse depth
              Collections.sort(attributeAssignActionSetsList, new Comparator<AttributeAssignActionSet>() {
                
                /**
                 * compare two items
                 * @param first first item
                 * @param second item
                 * @return -1, 0, 1
                 */
                public int compare(AttributeAssignActionSet first, AttributeAssignActionSet second) {
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
              
              Collections.reverse(attributeAssignActionSetsList);
              
              for(AttributeAssignActionSet attributeAssignActionSet : attributeAssignActionSetsList) {
                //I believe mysql has problems deleting self referential foreign keys
                attributeAssignActionSet.setParentAttrAssignActionSetId(null);
                attributeAssignActionSet.saveOrUpdate();
                attributeAssignActionSet.delete();
              }
              
              //delete assignments that use this action
              Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
                .getAttributeAssign().findByActionId(AttributeAssignAction.this.getId());
              
              for (AttributeAssign attributeAssign : attributeAssigns) {
                attributeAssign.delete();
              }
              
              GrouperDAOFactory.getFactory().getAttributeAssignAction().delete(AttributeAssignAction.this);
              
              hibernateSession.misc().flush();
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem attributeAssignAction: " + GrouperUtil.toStringSafe(this)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

    
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
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
   * id of this attribute def name
   * @return id
   */
  public String getId() {
    return id;
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
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * 
   * @return the name
   */
  public String getNameDb() {
    return name;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
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
   * id of this attribute def name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
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
   * 
   * @param name1
   */
  public void setName(@SuppressWarnings("unused") String name1) {
    throw new RuntimeException("Dont call this method");
  }

  /**
   * 
   * @param name1
   */
  public void setNameDb(String name1) {
    this.name = name1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "name", this.name)
      .append( "id", this.getId() )
      .toString();
  }

  /**
   * save save this object (insert), assign uuid, etc
   */
  public void save() {
    
    if (StringUtils.isBlank(this.id)) {
      this.id = GrouperUuid.getUuid();
    }
    
    try {
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              hibernateSession.setCachingEnabled(false);

              GrouperDAOFactory.getFactory().getAttributeAssignAction().saveOrUpdate(AttributeAssignAction.this);
              
              //we need a record in the set table to link this with itself
              AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
              attributeAssignActionSet.setId(GrouperUuid.getUuid());
              attributeAssignActionSet.setDepth(0);
              attributeAssignActionSet.setIfHasAttrAssignActionId(AttributeAssignAction.this.getId());
              attributeAssignActionSet.setThenHasAttrAssignActionId(AttributeAssignAction.this.getId());
              attributeAssignActionSet.setType(AttributeAssignActionType.self);
              attributeAssignActionSet.setParentAttrAssignActionSetId(attributeAssignActionSet.getId());
              attributeAssignActionSet.saveOrUpdate();
              
              hibernateSession.misc().flush();
              return null;
            }
        
      });
    } catch (GrouperDAOException e) {
      String error = "Problem attributeAssignAction: " + GrouperUtil.toStringSafe(this)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * update this object (update to DB)
   */
  public void update() {
    GrouperDAOFactory.getFactory().getAttributeAssignAction().saveOrUpdate(this);
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeAssignAction existingRecord) {

    existingRecord.setAttributeDefId(this.attributeDefId);
    existingRecord.setId(this.getId());
    existingRecord.setNameDb(this.getNameDb());
    
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeAssignAction other) {
    if (!StringUtils.equals(this.attributeDefId, other.attributeDefId)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.name, other.name)) {
      return true;
    }
    return false;
  }


  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeAssignAction other) {
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
  public AttributeAssignAction xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(this.id, this.attributeDefId, this.name, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeAssignAction xmlSaveBusinessProperties(AttributeAssignAction existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      AttributeDef attributeDef = AttributeDefFinder.findById(this.attributeDefId, true);
      existingRecord = attributeDef.getAttributeDefActionDelegate().internal_addAction(this.name, this.id);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.update();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeAssignAction().saveUpdateProperties(this);
  }


  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeAssignAction xmlToExportAttributeAssignAction(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }

    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = new XmlExportAttributeAssignAction();
    
    xmlExportAttributeAssignAction.setAttributeDefId(this.getAttributeDefId());
    xmlExportAttributeAssignAction.setContextId(this.getContextId());
    xmlExportAttributeAssignAction.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeAssignAction.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeAssignAction.setName(this.getNameDb());
    xmlExportAttributeAssignAction.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeAssignAction.setUuid(this.getId());
    
    return xmlExportAttributeAssignAction;
    
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

    stringWriter.write("AttributeAssignAction: " + this.getId() + ", " + this.getName());

//    XmlExportUtils.toStringAttributeAssignAction(null, stringWriter, this.getId(), false);
    
    return stringWriter.toString();
    
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public AttributeAssignAction dbVersion() {
    return (AttributeAssignAction)this.dbVersion;
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
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AttributeAssignAction)) {
      return false;
    }
    return StringUtils.equals(this.getName(), ( (AttributeAssignAction) other ).getName() )
      && StringUtils.equals(this.getAttributeDefId(), ( (AttributeAssignAction) other ).getAttributeDefId() );
  } // public boolean equals(other)

  /**
   * @return hashcode
   * @since   1.2.0
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getName() )
      .append( this.getAttributeDefId() )
      .toHashCode();
  } // public int hashCode()


}
