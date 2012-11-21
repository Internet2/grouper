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
package edu.internet2.middleware.grouper.changeLog;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * convert the temp objects to regular objects
 * @author mchyzer
 *
 */
public class ChangeLogTempToEntity {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ChangeLogTempToEntity.class);
  
  /**
   * convert the temps to regulars, assign id's
   * hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords() {
    return convertRecords(null);
  }
  
  /**
   * convert the temps to regulars, assign id's
   * @param hib3GrouperLoaderLog is the log object to post updates, can be null
   * @return the number of records converted
   */
  public static int convertRecords(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    
    final boolean includeNonFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeNonFlattenedMemberships", false);
    final boolean includeNonFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeNonFlattenedPrivileges", false);
    
    int count = 0;
    
    //first select the temp records
    List<ChangeLogEntry> changeLogEntryList = HibernateSession.byHqlStatic().createQuery("from ChangeLogEntryTemp order by createdOnDb")
      .options(new QueryOptions().paging(1000, 1, false)).list(ChangeLogEntry.class);
    
    //note: this is not in a transaction, though the inner one is
    for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
      
      final ChangeLogEntry CHANGE_LOG_ENTRY = changeLogEntry;
      
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              
              if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)
                  || CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_ADD)) {
                ChangeLogTempToEntity.processGroupAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)
                  || CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_UPDATE)) {
                ChangeLogTempToEntity.processGroupUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)
                  || CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ENTITY_DELETE)) {
                ChangeLogTempToEntity.processGroupDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_ADD)) {
                ChangeLogTempToEntity.processStemAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_UPDATE)) {
                ChangeLogTempToEntity.processStemUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.STEM_DELETE)) {
                ChangeLogTempToEntity.processStemDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD)) {
                ChangeLogTempToEntity.processAttributeDefAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_UPDATE)) {
                ChangeLogTempToEntity.processAttributeDefUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_ADD)) {
                ChangeLogTempToEntity.processFieldAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE)) {
                ChangeLogTempToEntity.processFieldUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_FIELD_DELETE)) {
                ChangeLogTempToEntity.processFieldDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN)) {
                ChangeLogTempToEntity.processGroupTypeAssign(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN)) {
                ChangeLogTempToEntity.processGroupTypeUnassign(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_ADD)) {
                ChangeLogTempToEntity.processMemberAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_UPDATE)) {
                ChangeLogTempToEntity.processMemberUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_DELETE)) {
                ChangeLogTempToEntity.processMemberDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
                ChangeLogTempToEntity.processMembershipAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                ChangeLogTempToEntity.processMembershipDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD)) {
                ChangeLogTempToEntity.processPrivilegeAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                ChangeLogTempToEntity.processPrivilegeDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD)) {
                ChangeLogTempToEntity.processAttributeDefNameAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_UPDATE)) {
                ChangeLogTempToEntity.processAttributeDefNameUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefNameDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignActionAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_UPDATE)) {
                ChangeLogTempToEntity.processAttributeAssignActionUpdate(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignActionDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignActionSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignActionSetDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD)) {
                ChangeLogTempToEntity.processAttributeDefNameSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE)) {
                ChangeLogTempToEntity.processAttributeDefNameSetDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_ADD)) {
                ChangeLogTempToEntity.processRoleSetAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_DELETE)) {
                ChangeLogTempToEntity.processRoleSetDelete(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)) {
                ChangeLogTempToEntity.processAttributeAssignValueAdd(CHANGE_LOG_ENTRY);
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE)) {
                ChangeLogTempToEntity.processAttributeAssignValueDelete(CHANGE_LOG_ENTRY);
              }
              
              if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) ||
                  CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                
                if (includeNonFlattenedMemberships) {
                  //insert into the non temp table
                  CHANGE_LOG_ENTRY.setTempObject(false);
                  CHANGE_LOG_ENTRY.save();
                }
              } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD) ||
                  CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                
                if (includeNonFlattenedPrivileges) {
                  //insert into the non temp table
                  CHANGE_LOG_ENTRY.setTempObject(false);
                  CHANGE_LOG_ENTRY.save();
                }
              } else {
                
                //insert into the non temp table
                CHANGE_LOG_ENTRY.setTempObject(false);
                CHANGE_LOG_ENTRY.save();
              }
              
              //delete from the temp
              //using sql since hibernate would try to otherwise batch this delete (since the table is not versioned I think),
              //in which case some database (like Oracle) do not return the number of affected rows.
              int count = HibernateSession.bySqlStatic().executeSql("delete from grouper_change_log_entry_temp where id = ?", 
                  GrouperUtil.toList((Object)CHANGE_LOG_ENTRY.getId()));
              if (count != 1) {
                throw new RuntimeException("Bad count of " + count + " when deleting temp change log entry: " + CHANGE_LOG_ENTRY.toStringDeep());
              }
              
              return null;
            }
        
      });
    }
    
    count += changeLogEntryList.size();

    if (count > 0 && hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addTotalCount(count);
      hib3GrouperLoaderLog.store();
    }
    
    if (changeLogEntryList.size() == 1000) {
      count += convertRecords(hib3GrouperLoaderLog);
    }
    
    return count;
  }
  
  /**
   * If a group gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processGroupAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_ADD.name.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_ADD.parentStemId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long time = changeLogEntry.getCreatedOnDb();
    
    PITGroup existing = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }

    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.parentStemId), true);
    
    PITGroup pitGroup = new PITGroup();
    pitGroup.setId(GrouperUuid.getUuid());
    pitGroup.setSourceId(id);
    pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name));  
    pitGroup.setStemId(pitStem.getId());
    pitGroup.setContextId(contextId);
    pitGroup.setActiveDb("T");
    pitGroup.setStartTimeDb(time);
    
    pitGroup.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If a group gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processGroupUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged).equals("name")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.name.name());

      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id), true);
      pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      pitGroup.setContextId(contextId);
      pitGroup.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged).equals("parentStemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.parentStemId.name());

      PITStem pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.parentStemId), true);
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id), true);
      pitGroup.setStemId(pitParentStem.getId());
      pitGroup.setContextId(contextId);
      pitGroup.saveOrUpdate();
    }
  }
  
  /**
   * Need to update end time of group and also end time of groupSets
   * @param changeLogEntry
   */
  private static void processGroupDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();

    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(id, false);
    if (pitGroup == null) {
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByPITOwner(pitGroup.getId(), endTime, contextId);
    
    pitGroup.setEndTimeDb(endTime);
    pitGroup.setActiveDb("F");
    pitGroup.setContextId(contextId);
    pitGroup.saveOrUpdate();
  }
  
  /**
   * If a stem gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processStemAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_ADD.id.name());

    PITStem pitStem = new PITStem();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.id);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.name);
    String parentStemId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_ADD.parentStemId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long time = changeLogEntry.getCreatedOnDb();

    PITStem existing = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    if (GrouperUtil.isEmpty(name)) {
      // is this the root stem??
      name = null;
      parentStemId = null;
      Stem root = GrouperDAOFactory.getFactory().getStem().findByName(Stem.ROOT_INT, true, null);
      if (root.getUuid().equals(id)) {
        name = Stem.ROOT_INT;
      }
    }
    
    if (parentStemId != null) {
      parentStemId = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(parentStemId, true).getId();
    }
    
    pitStem.setId(GrouperUuid.getUuid());
    pitStem.setSourceId(id);
    pitStem.setNameDb(name);
    pitStem.setParentStemId(parentStemId);
    pitStem.setContextId(contextId);
    pitStem.setActiveDb("T");
    pitStem.setStartTimeDb(time);
    
    pitStem.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If a stem gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processStemUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged).equals("name")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.name.name());

      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id), true);
      pitStem.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      pitStem.setContextId(contextId);
      pitStem.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged).equals("parentStemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.parentStemId.name());
      
      PITStem pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.parentStemId), true);
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id), true);
      pitStem.setParentStemId(pitParentStem.getId());
      pitStem.setContextId(contextId);
      pitStem.saveOrUpdate();
    }
  }
  
  /**
   * Need to update groupSets
   * @param changeLogEntry
   */
  private static void processStemDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(id, false);
    if (pitStem == null) {
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByPITOwner(pitStem.getId(), endTime, contextId);
    
    pitStem.setEndTimeDb(endTime);
    pitStem.setActiveDb("F");
    pitStem.setContextId(contextId);
    pitStem.saveOrUpdate();
  }
  
  /**
   * If an attribute def gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_ADD.name.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_ADD.stemId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_ADD.attributeDefType.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long time = changeLogEntry.getCreatedOnDb();

    PITAttributeDef existing = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.stemId), true);
    PITAttributeDef pitAttributeDef = new PITAttributeDef();
    pitAttributeDef.setId(GrouperUuid.getUuid());
    pitAttributeDef.setSourceId(id);
    pitAttributeDef.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.name));
    pitAttributeDef.setStemId(pitStem.getId());
    pitAttributeDef.setAttributeDefTypeDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.attributeDefType));
    pitAttributeDef.setContextId(contextId);
    pitAttributeDef.setActiveDb("T");
    pitAttributeDef.setStartTimeDb(time);

    pitAttributeDef.saveOrUpdate();
    
    // Add PIT group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByOwner(id, changeLogEntry.getCreatedOnDb(), contextId, false);
  }
  
  /**
   * If an attribute def gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged).equals("name")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name.name());

      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id), true);
      pitAttributeDef.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name));
      pitAttributeDef.setContextId(contextId);
      pitAttributeDef.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged).equals("stemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.stemId.name());

      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.stemId), true);
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id), true);
      pitAttributeDef.setStemId(pitStem.getId());
      pitAttributeDef.setContextId(contextId);
      pitAttributeDef.saveOrUpdate();
    }
  }
  
  /**
   * Need to update end time of attribute def and also groupSets
   * @param changeLogEntry
   */
  private static void processAttributeDefDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();

    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(id, false);
    if (pitAttributeDef == null) {
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByPITOwner(pitAttributeDef.getId(), endTime, contextId);
    
    pitAttributeDef.setEndTimeDb(endTime);
    pitAttributeDef.setActiveDb("F");
    pitAttributeDef.setContextId(contextId);
    pitAttributeDef.saveOrUpdate();
  }
  
  /**
   * If a field gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processFieldAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_ADD.name.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_ADD.type.name());

    PITField pitField = new PITField();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.id);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.name);
    String type = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.type);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long time = changeLogEntry.getCreatedOnDb();

    PITField existing = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    pitField.setId(GrouperUuid.getUuid());
    pitField.setSourceId(id);
    pitField.setNameDb(name);
    pitField.setTypeDb(type);
    pitField.setContextId(contextId);
    pitField.setActiveDb("T");
    pitField.setStartTimeDb(time);
    
    pitField.saveOrUpdate();
    
    // might have to add PIT group sets...
    Field field = FieldFinder.findById(id, false);
    if (field != null && field.isGroupListField()) {      
      GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByField(id, changeLogEntry.getCreatedOnDb(), contextId);
    }
  }
  
  private static void processFieldDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
    
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(id, false);
    if (pitField == null) {
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByPITField(pitField.getId(), endTime, contextId);
    
    pitField.setEndTimeDb(endTime);
    pitField.setActiveDb("F");
    pitField.setContextId(contextId);
    pitField.saveOrUpdate();
  }
  
  /**
   * If a field gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processFieldUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged).equals("name") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.propertyChanged).equals("type")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_UPDATE.name.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_FIELD_UPDATE.type.name());

      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.id), true);
      pitField.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.name));
      pitField.setTypeDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_UPDATE.type));
      pitField.setContextId(contextId);
      pitField.saveOrUpdate();
    }
  }
  
  /**
   * @param changeLogEntry
   */
  private static void processGroupTypeAssign(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId.name());

    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    // add group sets
    GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByOwner(groupId, changeLogEntry.getCreatedOnDb(), contextId, true);
  }
  
  /**
   * @param changeLogEntry
   */
  private static void processGroupTypeUnassign(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId.name());

    String typeId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId);
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();

    // remove group sets
    Set<Field> fields = FieldFinder.findAllByGroupType(typeId);
    Iterator<Field> iter = fields.iterator();
    
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupId, false);
    if (pitGroup == null) {
      return;
    }
    
    while (iter.hasNext()) {
      Field field = iter.next();
      if (field.isGroupListField()) {
        
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(field.getUuid(), false);
        if (pitField != null) {
          GrouperDAOFactory.getFactory().getPITGroupSet().updateEndTimeByPITOwnerAndPITField(pitGroup.getId(), pitField.getId(), endTime, contextId);
        }
      }
    }
  }
  
  /**
   * If a member gets added, we need to add it to the PIT table.
   * @param changeLogEntry
   */
  private static void processMemberAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_ADD.subjectId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_ADD.subjectSourceId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_ADD.subjectTypeId.name());

    PITMember pitMember = new PITMember();
    
    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.id);
    String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectId);
    String subjectSourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectSourceId);
    String subjectTypeId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectTypeId);
    Long time = changeLogEntry.getCreatedOnDb();

    PITMember existing = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    pitMember.setId(GrouperUuid.getUuid());
    pitMember.setSourceId(id);
    pitMember.setSubjectId(subjectId);
    pitMember.setSubjectSourceId(subjectSourceId);
    pitMember.setSubjectTypeId(subjectTypeId);
    pitMember.setActiveDb("T");
    pitMember.setStartTimeDb(time);

    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMember.setContextId(changeLogEntry.getContextId());
    }
    
    pitMember.saveOrUpdate();
  }
  
  /**
   * If a member gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processMemberUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectId") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectSourceId") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectTypeId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectSourceId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectTypeId.name());
      
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id), true);
      pitMember.setSubjectId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      pitMember.setSubjectSourceId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      pitMember.setSubjectTypeId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      pitMember.setContextId(contextId);
      pitMember.saveOrUpdate();
    }
  }
  
  private static void processMemberDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_DELETE.id);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();
        
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(id, false);
    if (pitMember == null) {
      return;
    }
    
    pitMember.setEndTimeDb(endTime);
    pitMember.setActiveDb("F");
    pitMember.setContextId(contextId);
    pitMember.saveOrUpdate();
  }
 
  /**
   * If a membership gets added, then the membership needs to
   * get added to the PIT table.
   * @param changeLogEntry
   */
  private static void processMembershipAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.groupId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.memberId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.id);
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId);
    String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId);
    Long time = changeLogEntry.getCreatedOnDb();
    
    PITMembership existing = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupId, true);
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldId, true);
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(memberId, true);
    
    PITMembership pitMembership = new PITMembership();
    pitMembership.setId(GrouperUuid.getUuid());
    pitMembership.setSourceId(id);
    pitMembership.setOwnerGroupId(pitGroup.getId());
    pitMembership.setMemberId(pitMember.getId());
    pitMembership.setFieldId(pitField.getId());
    pitMembership.setActiveDb("T");
    pitMembership.setStartTimeDb(time);
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.save();
  }
  
  /**
   * If a membership gets deleted, then the membership needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   */
  private static void processMembershipDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();

    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(id, false);
    if (pitMembership == null) {
      return;
    }
    
    pitMembership.setEndTimeDb(time);
    pitMembership.setActiveDb("F");

    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.update();
  }
  
  /**
   * If an access, naming, or attr def privilege gets added, the privilege needs to
   * get added to the PIT table.
   * @param changeLogEntry
   */
  private static void processPrivilegeAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.ownerType.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.ownerId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.fieldId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_ADD.memberId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.id);
    String ownerType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType);
    String ownerId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId);
    String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId);    
    Long time = changeLogEntry.getCreatedOnDb();
    
    PITMembership existing = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldId, true);
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(memberId, true);
    
    PITMembership pitMembership = new PITMembership();
    pitMembership.setId(GrouperUuid.getUuid());
    pitMembership.setSourceId(id);
    pitMembership.setMemberId(pitMember.getId());
    pitMembership.setFieldId(pitField.getId());
    pitMembership.setActiveDb("T");
    pitMembership.setStartTimeDb(time);
    
    if (ownerType.equals(Membership.OWNER_TYPE_GROUP)) {
      PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId, true);
      pitMembership.setOwnerGroupId(pitOwner.getId());
    } else if (ownerType.equals(Membership.OWNER_TYPE_STEM)) {
      PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(ownerId, true);
      pitMembership.setOwnerStemId(pitOwner.getId());
    } else if (ownerType.equals(Membership.OWNER_TYPE_ATTRIBUTE_DEF)) {
      PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(ownerId, true);
      pitMembership.setOwnerAttrDefId(pitOwner.getId());
    } else {
      throw new RuntimeException("unexpected ownerType: " + ownerType);
    }
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.save();
  }
  
  
  /**
   * If an access, naming, or attr def privilege gets deleted, the privilege needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   */
  private static void processPrivilegeDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.PRIVILEGE_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();

    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(id, false);
    if (pitMembership == null) {
      return;
    }
    
    pitMembership.setEndTimeDb(time);
    pitMembership.setActiveDb("F");
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedMemberships = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedMemberships", true);
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    
    pitMembership.update();
  }
  
  /**
   * If an attribute assign gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id);
    String attributeDefNameId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId);
    String actionId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId);
    String assignType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType);
    String ownerId1 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1);    
    String ownerId2 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2);    
    String disallowedDb = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed);    
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssign existing = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefNameId, true);
    PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(actionId, true);
    
    PITAttributeAssign pitAttributeAssign = new PITAttributeAssign();
    pitAttributeAssign.setId(GrouperUuid.getUuid());
    pitAttributeAssign.setSourceId(id);
    pitAttributeAssign.setAttributeDefNameId(pitAttributeDefName.getId());
    pitAttributeAssign.setAttributeAssignActionId(pitAttributeAssignAction.getId());
    pitAttributeAssign.setAttributeAssignTypeDb(assignType);
    pitAttributeAssign.setActiveDb("T");
    pitAttributeAssign.setStartTimeDb(time);
    pitAttributeAssign.setContextId(contextId);
    pitAttributeAssign.setDisallowedDb(disallowedDb);
    
    if (AttributeAssignType.group.name().equals(assignType)) {
      PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId1, true);
      pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
    } else if (AttributeAssignType.stem.name().equals(assignType)) {
      PITStem pitOwner1 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(ownerId1, true);
      pitAttributeAssign.setOwnerStemId(pitOwner1.getId());
    } else if (AttributeAssignType.member.name().equals(assignType)) {
      PITMember pitOwner1 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(ownerId1, true);
      pitAttributeAssign.setOwnerMemberId(pitOwner1.getId());
    } else if (AttributeAssignType.attr_def.name().equals(assignType)) {
      PITAttributeDef pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(ownerId1, true);
      pitAttributeAssign.setOwnerAttributeDefId(pitOwner1.getId());
    } else if (AttributeAssignType.any_mem.name().equals(assignType)) {
      PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId1, true);
      PITMember pitOwner2 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(ownerId2, true);
      pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
      pitAttributeAssign.setOwnerMemberId(pitOwner2.getId());
    } else if (AttributeAssignType.imm_mem.name().equals(assignType)) {
      PITMembership pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(ownerId1, false);
      if (pitOwner1 == null) {
        // it may be disabled..
        pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(ownerId1, true);
      }
      pitAttributeAssign.setOwnerMembershipId(pitOwner1.getId());
    } else {
      // this must be an attribute assign of an attribute assign.  foreign keys will make sure we're right.
      PITAttributeAssign pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(ownerId1, false);
      if (pitOwner1 == null) {
        // it may be disabled..
        pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(ownerId1, true);
      }
      pitAttributeAssign.setOwnerAttributeAssignId(pitOwner1.getId());
    }
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeAssign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);

    pitAttributeAssign.save();
  }
  
  /**
   * If an attribute assign gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(id, false);
    if (pitAttributeAssign == null) {
      return;
    }
    
    pitAttributeAssign.setEndTimeDb(time);
    pitAttributeAssign.setActiveDb("F");
    pitAttributeAssign.setContextId(contextId);
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeAssign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssign.update();
  }
  
  /**
   * If an attribute assign value gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignValueAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id);
    String attributeAssignId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId);
    String value = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value);
    String valueType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType);
  
    PITAttributeAssignValue existing = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(attributeAssignId, false);
    if (pitAttributeAssign == null) {
      // it may be disabled..
      pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(attributeAssignId, true);
    }
    
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssignValue pitAttributeAssignValue = new PITAttributeAssignValue();
    pitAttributeAssignValue.setId(GrouperUuid.getUuid());
    pitAttributeAssignValue.setSourceId(id);
    pitAttributeAssignValue.setAttributeAssignId(pitAttributeAssign.getId());
    pitAttributeAssignValue.setActiveDb("T");
    pitAttributeAssignValue.setStartTimeDb(time);
    pitAttributeAssignValue.setContextId(contextId);
    
    if (AttributeDefValueType.string.name().equals(valueType)) {
      pitAttributeAssignValue.setValueString(value);
    } else if (AttributeDefValueType.integer.name().equals(valueType) || AttributeDefValueType.timestamp.name().equals(valueType)) {
      pitAttributeAssignValue.setValueInteger(GrouperUtil.longValue(value));
    } else if (AttributeDefValueType.memberId.name().equals(valueType)) {
      pitAttributeAssignValue.setValueMemberId(value);
    } else if (AttributeDefValueType.floating.name().equals(valueType)) {
      pitAttributeAssignValue.setValueFloating(GrouperUtil.doubleValue(value));
    } else {
      throw new RuntimeException("unexpected valueType: " + valueType);
    }

    pitAttributeAssignValue.save();
  }
  
  /**
   * If an attribute assign value gets deleted, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignValueDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssignValue pitAttributeAssignValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdActive(id, false);
    if (pitAttributeAssignValue == null) {
      return;
    }
    
    pitAttributeAssignValue.setEndTimeDb(time);
    pitAttributeAssignValue.setActiveDb("F");
    pitAttributeAssignValue.setContextId(contextId);
    
    pitAttributeAssignValue.update();
  }
  
  /**
   * If an attribute def name gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id);
    String attributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId);
    String stemId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeDefName existing = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDefId, true);
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stemId, true);
    
    PITAttributeDefName pitAttributeDefName = new PITAttributeDefName();
    pitAttributeDefName.setId(GrouperUuid.getUuid());
    pitAttributeDefName.setSourceId(id);
    pitAttributeDefName.setAttributeDefId(pitAttributeDef.getId());
    pitAttributeDefName.setStemId(pitStem.getId());
    pitAttributeDefName.setNameDb(name);
    pitAttributeDefName.setActiveDb("T");
    pitAttributeDefName.setStartTimeDb(time);
    pitAttributeDefName.setContextId(contextId);

    pitAttributeDefName.saveOrUpdate();
  }
  
  /**
   * If an attribute def name gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
     
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.propertyChanged).equals("name")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name.name());

      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id), true);
      pitAttributeDefName.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.name));
      pitAttributeDefName.setContextId(contextId);
      pitAttributeDefName.saveOrUpdate();
    }
  }
  
  /**
   * If an attribute def name gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(id, false);
    if (pitAttributeDefName == null) {
      return;
    }
    
    pitAttributeDefName.setEndTimeDb(time);
    pitAttributeDefName.setActiveDb("F");
    pitAttributeDefName.setContextId(contextId);
    
    pitAttributeDefName.saveOrUpdate();
  }

  /**
   * If an attribute assign action gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id);
    String attributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId);
    String name = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.name);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssignAction existing = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDefId, true);

    PITAttributeAssignAction pitAttributeAssignAction = new PITAttributeAssignAction();
    pitAttributeAssignAction.setId(GrouperUuid.getUuid());
    pitAttributeAssignAction.setSourceId(id);
    pitAttributeAssignAction.setAttributeDefId(pitAttributeDef.getId());
    pitAttributeAssignAction.setNameDb(name);
    pitAttributeAssignAction.setActiveDb("T");
    pitAttributeAssignAction.setStartTimeDb(time);
    pitAttributeAssignAction.setContextId(contextId);

    pitAttributeAssignAction.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action gets updated, we may need to update the PIT table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionUpdate(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.propertyChanged).equals("name")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id.name());

      PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id), true);
      pitAttributeAssignAction.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.name));
      pitAttributeAssignAction.setContextId(contextId);
      pitAttributeAssignAction.saveOrUpdate();
    }
  }
  
  /**
   * If an attribute assign action gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(id, false);
    if (pitAttributeAssignAction == null) {
      return;
    }
    
    pitAttributeAssignAction.setEndTimeDb(time);
    pitAttributeAssignAction.setActiveDb("F");
    pitAttributeAssignAction.setContextId(contextId);
    
    pitAttributeAssignAction.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionSetAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.depth.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.parentAttrAssignActionSetId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.parentAttrAssignActionSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeAssignActionSet existing = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeAssignAction pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(ifHas, true);
    PITAttributeAssignAction pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(thenHas, true);
    PITAttributeAssignActionSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(parent, false);
    
    PITAttributeAssignActionSet pitAttributeAssignActionSet = new PITAttributeAssignActionSet();
    pitAttributeAssignActionSet.setId(GrouperUuid.getUuid());
    pitAttributeAssignActionSet.setSourceId(id);
    pitAttributeAssignActionSet.setDepth(Integer.parseInt(depth));
    pitAttributeAssignActionSet.setIfHasAttrAssignActionId(pitIfHas.getId());
    pitAttributeAssignActionSet.setThenHasAttrAssignActionId(pitThenHas.getId());
    pitAttributeAssignActionSet.setParentAttrAssignActionSetId(Integer.parseInt(depth) == 0 ? pitAttributeAssignActionSet.getId() : pitParent.getId());
    pitAttributeAssignActionSet.setActiveDb("T");
    pitAttributeAssignActionSet.setStartTimeDb(time);
    pitAttributeAssignActionSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeAssignActionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssignActionSet.saveOrUpdate();
  }
  
  /**
   * If an attribute assign action set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionSetDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeAssignActionSet pitAttributeAssignActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(id, false);
    if (pitAttributeAssignActionSet == null) {
      return;
    }
    
    pitAttributeAssignActionSet.setEndTimeDb(time);
    pitAttributeAssignActionSet.setActiveDb("F");
    pitAttributeAssignActionSet.setContextId(contextId);
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeAssignActionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssignActionSet.saveOrUpdate();
  }
  
  /**
   * If an attribute def name set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.depth.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.parentAttrDefNameSetId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.parentAttrDefNameSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITAttributeDefNameSet existing = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITAttributeDefName pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(ifHas, true);
    PITAttributeDefName pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(thenHas, true);
    PITAttributeDefNameSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(parent, false);

    PITAttributeDefNameSet pitAttributeDefNameSet = new PITAttributeDefNameSet();
    pitAttributeDefNameSet.setId(GrouperUuid.getUuid());
    pitAttributeDefNameSet.setSourceId(id);
    pitAttributeDefNameSet.setDepth(Integer.parseInt(depth));
    pitAttributeDefNameSet.setIfHasAttributeDefNameId(pitIfHas.getId());
    pitAttributeDefNameSet.setThenHasAttributeDefNameId(pitThenHas.getId());
    pitAttributeDefNameSet.setParentAttrDefNameSetId(Integer.parseInt(depth) == 0 ? pitAttributeDefNameSet.getId() : pitParent.getId());
    pitAttributeDefNameSet.setActiveDb("T");
    pitAttributeDefNameSet.setStartTimeDb(time);
    pitAttributeDefNameSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeDefNameSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeDefNameSet.saveOrUpdate();
  }
  
  /**
   * If an attribute def name set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(id, false);
    if (pitAttributeDefNameSet == null) {
      return;
    }
    
    pitAttributeDefNameSet.setEndTimeDb(time);
    pitAttributeDefNameSet.setActiveDb("F");
    pitAttributeDefNameSet.setContextId(contextId);
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitAttributeDefNameSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeDefNameSet.saveOrUpdate();
  }
  
  /**
   * If a role set set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processRoleSetAdd(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_ADD.depth.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_ADD.parentRoleSetId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.depth);
    String ifHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId);
    String thenHas = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId);
    String parent = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_ADD.parentRoleSetId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITRoleSet existing = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITGroup pitIfHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ifHas, true);
    PITGroup pitThenHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(thenHas, true);
    PITRoleSet pitParent = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(parent, false);

    PITRoleSet pitRoleSet = new PITRoleSet();
    pitRoleSet.setId(GrouperUuid.getUuid());
    pitRoleSet.setSourceId(id);
    pitRoleSet.setDepth(Integer.parseInt(depth));
    pitRoleSet.setIfHasRoleId(pitIfHas.getId());
    pitRoleSet.setThenHasRoleId(pitThenHas.getId());
    pitRoleSet.setParentRoleSetId(Integer.parseInt(depth) == 0 ? pitRoleSet.getId() : pitParent.getId());
    pitRoleSet.setActiveDb("T");
    pitRoleSet.setStartTimeDb(time);
    pitRoleSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitRoleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitRoleSet.saveOrUpdate();
  }
  
  /**
   * If a role set set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processRoleSetDelete(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.ROLE_SET_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ROLE_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(id, false);
    if (pitRoleSet == null) {
      return;
    }
    
    pitRoleSet.setEndTimeDb(time);
    pitRoleSet.setActiveDb("F");
    pitRoleSet.setContextId(contextId);
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.getPropertyBoolean("changeLog.includeRolesWithPermissionChanges", false);
    pitRoleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitRoleSet.saveOrUpdate();
  }
  
  /**
   * Verify that field is not empty
   * @param s
   */
  private static void assertNotEmpty(ChangeLogEntry changeLogEntry, String label) {
    String value = changeLogEntry.retrieveValueForLabel(label);
    if (GrouperUtil.isEmpty(value)) {
      throw new RuntimeException(label + " is empty for change log entry: " + changeLogEntry.toStringDeep());
    }
  }
}
