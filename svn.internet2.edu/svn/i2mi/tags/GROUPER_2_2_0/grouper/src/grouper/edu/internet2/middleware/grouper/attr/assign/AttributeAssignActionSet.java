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
package edu.internet2.middleware.grouper.attr.assign;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.grouperSet.GrouperSet;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignActionSet;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

//select gg.name, gadn.name
//from grouper_attribute_assign gaa, grouper_attribute_def_name_set gadns, grouper_groups gg, 
//grouper_attribute_def_name gadn
//where gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id
//and gadn.id = gadns.then_has_attribute_def_name_id


//select gaa.id attribute_assign_id, gaa.owner_group_id, gaa.owner_membership_id, gadn.name, gadn.id attribute_def_name_id
//from grouper_attribute_assign gaa, grouper_attribute_def_name_set gadns,
//grouper_attribute_def_name gadn
//where gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id
//and gadn.id = gadns.then_has_attribute_def_name_id;

/**
 * <pre>
 * Make a directed graph of attribute assign actions.  e.g. "admin" implies "read" and "write".
 * 
 * RegistryReset.internal_resetRegistryAndAddTestSubjects();
 * exit;
 * 
 * grouperSession = GrouperSession.startRootSession();
 * root = StemFinder.findRootStem(this.grouperSession);
 * 
 * top = this.root.addChildStem("top", "top display name");
 * -or-
 * top = StemFinder.findByName(grouperSession, "top");
 * 
 * role = top.addChildRole("role", "role");
 * 
 * //make a permission definition 
 * permissionDef = top.addChildAttributeDef("permissionDef", AttributeDefType.perm);
 * //make a permission name
 * permissionName = top.addChildAttributeDefName(permissionDef, "permission", "permission");
 * 
 * //set the list of allowed actions for this permission definition
 * permissionDef.getAttributeDefActionDelegate().configureActionList("admin,read,write");
 * admin = permissionDef.getAttributeDefActionDelegate().allowedAction("admin", true);
 * read = permissionDef.getAttributeDefActionDelegate().allowedAction("read", true);
 * write = permissionDef.getAttributeDefActionDelegate().allowedAction("write", true);
 * 
 * //if someone has admin, then they have read or write
 * admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read);
 * admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(write);
 * 
 * //assign admin permission to a role
 * role.getPermissionRoleDelegate().assignRolePermission("admin", permissionName);
 * 
 * //assign the role to a user
 * role.addMember(SubjectFinder.findById("test.subject.0"));
 * 
 * //see what permissions that user has (true is returned to all)
 * GrouperDAOFactory.getFactory().getPermissionEntry().hasPermissionBySubjectIdSourceIdActionAttributeDefName("test.subject.0", "jdbc", "admin", "top:permission");
 * GrouperDAOFactory.getFactory().getPermissionEntry().hasPermissionBySubjectIdSourceIdActionAttributeDefName("test.subject.0", "jdbc", "read", "top:permission");
 * GrouperDAOFactory.getFactory().getPermissionEntry().hasPermissionBySubjectIdSourceIdActionAttributeDefName("test.subject.0", "jdbc", "write", "top:permission");
 * 
 * </pre>
 * @author mchyzer $Id: AttributeAssignActionSet.java,v 1.2 2009-10-26 04:52:17 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeAssignActionSet extends GrouperAPI 
    implements Hib3GrouperVersioned, GrouperSet, XmlImportable<AttributeAssignActionSet> {
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "AttributeAssignActionSet: " + this.id;
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssignActionSet.class);

  /** name of the groups attribute assign action set */
  public static final String TABLE_GROUPER_ATTR_ASSIGN_ACTION_SET = "grouper_attr_assign_action_set";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_DEPTH = "depth";

  /** column */
  public static final String COLUMN_IF_HAS_ATTR_ASSN_ACTION_ID = "if_has_attr_assn_action_id";

  /** column */
  public static final String COLUMN_THEN_HAS_ATTR_ASSN_ACTION_ID = "then_has_attr_assn_action_id";

  /** column */
  public static final String COLUMN_PARENT_ATTR_ASSN_ACTION_ID = "parent_attr_assn_action_id";

  /** column */
  public static final String COLUMN_TYPE = "type";



  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttributeAssignActionId */
  public static final String FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID = "ifHasAttrAssignActionId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: parentAttrAssignActionSetId */
  public static final String FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID = "parentAttrAssignActionSetId";

  /** constant for field name for: thenHasAttributeAssignActionId */
  public static final String FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID = "thenHasAttrAssignActionId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID,  FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID, FIELD_TYPE, FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID,  
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** membership type -- immediate, or effective */
  private AttributeAssignActionType type = AttributeAssignActionType.immediate;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentAttrAssignActionSetId;

  /** attribute def name id of the parent */
  private String thenHasAttrAssignActionId;
  
  /** attribute def name id of the child */
  private String ifHasAttrAssignActionId;

  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * find an attribute def name set, better be here
   * @param attributeDefNameSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @param exceptionIfNull 
   * @return the def name set
   */
  public static AttributeAssignActionSet findInCollection(
      Collection<AttributeAssignActionSet> attributeDefNameSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (AttributeAssignActionSet attributeDefNameSet : GrouperUtil.nonNull(attributeDefNameSets)) {
      if (StringUtils.equals(ifHasId, attributeDefNameSet.getIfHasAttrAssignActionId())
          && StringUtils.equals(thenHasId, attributeDefNameSet.getThenHasAttrAssignActionId())
          && depth == attributeDefNameSet.getDepth()) {
        return attributeDefNameSet;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find attribute def name set with id: " + ifHasId + ", " + thenHasId + ", " + depth);
    }
    return null;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof AttributeAssignActionSet)) {
      return false;
    }
    
    AttributeAssignActionSet that = (AttributeAssignActionSet) other;
    return new EqualsBuilder()
      .append(this.parentAttrAssignActionSetId, that.parentAttrAssignActionSetId)
      .append(this.thenHasAttrAssignActionId, that.thenHasAttrAssignActionId)
      .append(this.ifHasAttrAssignActionId, that.ifHasAttrAssignActionId)
      .isEquals();

  }
  
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.parentAttrAssignActionSetId)
      .append(this.thenHasAttrAssignActionId)
      .append(this.ifHasAttrAssignActionId)
      .toHashCode();
  }
  
  /**
   * 
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeAssignActionSet getParentAttributeDefSet() {
    if (this.depth == 0) {
      return this;
    }
    
    AttributeAssignActionSet parent = GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .findById(this.getParentAttrAssignActionSetId(), true) ;
    return parent;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeAssignAction getIfHasAttributeAssignAction() {
    AttributeAssignAction ifHasAttributeAssignAction = 
      GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(this.getIfHasAttrAssignActionId(), true) ;
    return ifHasAttributeAssignAction;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeAssignAction getThenHasAttributeAssignAction() {
    AttributeAssignAction thenHasAttributeAssignAction = 
      GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(this.getThenHasAttrAssignActionId(), true) ;
    return thenHasAttributeAssignAction;
  }
  
  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @return parent id
   */
  public String getParentAttrAssignActionSetId() {
    return parentAttrAssignActionSetId;
  }

  
  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @param parentId1
   */
  public void setParentAttrAssignActionSetId(String parentId1) {
    this.parentAttrAssignActionSetId = parentId1;
  }

  
  /**
   * @return attribute def id for the owner
   */
  public String getThenHasAttrAssignActionId() {
    return this.thenHasAttrAssignActionId;
  }

  /**
   * Set attribute def id for the owner
   * @param ownerAttributeDefId
   */
  public void setThenHasAttrAssignActionId(String ownerAttributeDefId) {
    this.thenHasAttrAssignActionId = ownerAttributeDefId;
  }

  /**
   * @return member attribute def name id for the child
   */
  public String getIfHasAttrAssignActionId() {
    return this.ifHasAttrAssignActionId;
  }

  
  /**
   * Set attribute def name id for the child
   * @param memberAttributeAssignActionId
   */
  public void setIfHasAttrAssignActionId(String memberAttributeAssignActionId) {
    this.ifHasAttrAssignActionId = memberAttributeAssignActionId;
  }
  
  
  /**
   * @return membership type (immediate, effective, or composite)
   */
  public AttributeAssignActionType getType() {
    return this.type;
  }

  /**
   * get string value of type for hibernate
   * @return type
   */
  public String getTypeDb() {
    return this.type == null ? null : this.type.name();
  }
  
  /**
   * set group set assignment type
   * @param type1
   */
  public void setType(AttributeAssignActionType type1) {
    this.type = type1;
  }

  /**
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = AttributeAssignActionType.valueOfIgnoreCase(type1, false);
  }

  /**
   * @return depth
   */
  public int getDepth() {
    return depth;
  }

  /**
   * set depth
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
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
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getAttributeAssignActionSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeAssignActionSet().delete(this);
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
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
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getId()
   */
  public String __getId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElementId()
   */
  public String __getIfHasElementId() {
    return this.getIfHasAttrAssignActionId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElementId()
   */
  public String __getThenHasElementId() {
    return this.getThenHasAttrAssignActionId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getDepth()
   */
  public int __getDepth() {
    return this.getDepth();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElement()
   */
  public GrouperSetElement __getIfHasElement() {
    return this.getIfHasAttributeAssignAction();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElement()
   */
  public GrouperSetElement __getThenHasElement() {
    return this.getThenHasAttributeAssignAction();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__setParentGrouperSetId(java.lang.String)
   */
  public void __setParentGrouperSetId(String grouperSetId) {
    this.setParentAttrAssignActionSetId(grouperSetId);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSet()
   */
  public GrouperSet __getParentGrouperSet() {
    return this.getParentAttributeDefSet();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSetId()
   */
  public String __getParentGrouperSetId() {
    return this.getParentAttrAssignActionSetId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeAssignActionSet existingRecord) {
    existingRecord.setDepth(this.depth);
    existingRecord.setId(this.id);
    existingRecord.setIfHasAttrAssignActionId(this.ifHasAttrAssignActionId);
    existingRecord.setThenHasAttrAssignActionId(this.thenHasAttrAssignActionId);
    existingRecord.setType(this.type);

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeAssignActionSet other) {
    if (this.depth != other.depth) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.ifHasAttrAssignActionId, other.ifHasAttrAssignActionId)) {
      return true;
    }
    if (!StringUtils.equals(this.thenHasAttrAssignActionId, other.thenHasAttrAssignActionId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.type, other.type)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeAssignActionSet other) {
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
  public AttributeAssignActionSet xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByUuidOrKey(this.id, this.ifHasAttrAssignActionId, this.thenHasAttrAssignActionId, this.parentAttrAssignActionSetId, this.depth, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeAssignActionSet xmlSaveBusinessProperties(AttributeAssignActionSet existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      
      if (this.depth != 1) {
        throw new RuntimeException("Why are we doing a depth not equal to 1????");
      }
      
      AttributeAssignAction ifHasAttributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findById(this.ifHasAttrAssignActionId, true);
      AttributeAssignAction thenHasAttributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findById(this.thenHasAttrAssignActionId, true);
      
      ifHasAttributeAssignAction.getAttributeAssignActionSetDelegate().internal_addToAttributeAssignActionSet(thenHasAttributeAssignAction, this.id);
      
      existingRecord = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfThenImmediate(
          this.ifHasAttrAssignActionId, this.thenHasAttrAssignActionId, true);
    }
    //basically the set should not be updated.... the id's wont match since the self referential records arent
    //exported/imported
    //    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //    //if its an insert or update, then do the rest of the fields
    //    existingRecord.saveOrUpdate();
    return existingRecord;

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeAssignActionSet().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeAssignActionSet xmlToExportAttributeAssignActionSet(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = new XmlExportAttributeAssignActionSet();
    
    xmlExportAttributeAssignActionSet.setContextId(this.getContextId());
    xmlExportAttributeAssignActionSet.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeAssignActionSet.setDepth(this.getDepth());
    xmlExportAttributeAssignActionSet.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeAssignActionSet.setIfHasAttributeAssignActionId(this.getIfHasAttrAssignActionId());
    xmlExportAttributeAssignActionSet.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeAssignActionSet.setThenHasAttributeAssignActionId(this.getThenHasAttrAssignActionId());
    xmlExportAttributeAssignActionSet.setType(this.getTypeDb());
    xmlExportAttributeAssignActionSet.setUuid(this.getId());

    return xmlExportAttributeAssignActionSet;
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
    
    stringWriter.write("AttributeAssignActionSet: " + this.getId());

//    XmlExportUtils.toStringAttributeAssignActionSet(stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

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
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.type.name(), this.getTypeDb(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId.name(), this.getIfHasAttrAssignActionId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId.name(), this.getThenHasAttrAssignActionId(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.parentAttrAssignActionSetId.name(), this.getParentAttrAssignActionSetId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.depth.name(), "" + this.getDepth()).save();
  }
  
  /**
   * @see GrouperAPI#onPreDelete(HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE, 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id.name(), this.getId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.type.name(), this.getTypeDb(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.ifHasAttrAssnActionId.name(), this.getIfHasAttrAssignActionId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.thenHasAttrAssnActionId.name(), this.getThenHasAttrAssignActionId(),
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.parentAttrAssignActionSetId.name(), this.getParentAttrAssignActionSetId(), 
        ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.depth.name(), "" + this.getDepth()).save();
  }

  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_DEPTH)) {
      throw new RuntimeException("cannot update depth");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_IF_HAS_ATTR_ASSIGN_ACTION_ID)) {
      throw new RuntimeException("cannot update ifHasAttrAssignActionId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_THEN_HAS_ATTR_ASSIGN_ACTION_ID)) {
      throw new RuntimeException("cannot update thenHasAttrAssignActionId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID) && parentAttrAssignActionSetId != null) {
      throw new RuntimeException("cannot update parentAttrAssignActionSetId");
    }
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public AttributeAssignActionSet dbVersion() {
    return (AttributeAssignActionSet)this.dbVersion;
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
}
