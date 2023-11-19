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
package edu.internet2.middleware.grouper.changeLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheMembershipDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * convert the temp objects to regular objects
 * @author mchyzer
 *
 */
public class ChangeLogTempToEntity {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ChangeLogTempToEntity.class);
  
  private static ChangeLogEntry lastTempChangeLogProcessingIfIndividual = null;
      
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
    int changeLogTempToChangeLogQuerySize = GrouperLoaderConfig.retrieveConfig().propertyValueIntRequired("changeLog.changeLogTempToChangeLogQuerySize");
    if (changeLogTempToChangeLogQuerySize <= 0) {
      changeLogTempToChangeLogQuerySize = 1;
    }
    
    int totalCount = 0;
    while (true) {
      try {
        lastTempChangeLogProcessingIfIndividual = null;
        int currentCount = convertRecordsOnePage(hib3GrouperLoaderLog, changeLogTempToChangeLogQuerySize);
        totalCount += currentCount;
        
        if (currentCount != changeLogTempToChangeLogQuerySize) {
          break;
        }
      } catch (Exception e) {
        if (changeLogTempToChangeLogQuerySize > 1) {
          LOG.warn("Error while processing temp change log, trying individually now", e);
          changeLogTempToChangeLogQuerySize = 1;
        } else {
          if (lastTempChangeLogProcessingIfIndividual == null) {
            LOG.error("Error processing temp change log with query size = 1", e);
          } else {
            LOG.error("Error processing the following individual temp change log entry with id=" + lastTempChangeLogProcessingIfIndividual.getId(), e);
          }
          
          throw e;
        }
      }
    }
    
    return totalCount;
  }
  
  private static int convertRecordsOnePage(Hib3GrouperLoaderLog hib3GrouperLoaderLog, int changeLogTempToChangeLogQuerySize) {
    
    final boolean includeNonFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeNonFlattenedMemberships", false);
    final boolean includeNonFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeNonFlattenedPrivileges", false);
    final int tooManyChangeLogUpdatesSize = GrouperLoaderConfig.retrieveConfig().propertyValueIntRequired("changeLog.tooManyChangeLogUpdatesSize");
    
    ChangeLogEntry.clearNextSequenceNumberCache();
        
    //first select the temp records
    final List<ChangeLogEntry> tempChangeLogEntryList = HibernateSession.byHqlStatic().createQuery("from ChangeLogEntryTemp order by createdOnDb")
      .options(new QueryOptions().paging(changeLogTempToChangeLogQuerySize, 1, false)).list(ChangeLogEntry.class);
    
    int tempChangeLogEntryListOrigSize = tempChangeLogEntryList.size();
    
    if (changeLogTempToChangeLogQuerySize == 1 && tempChangeLogEntryListOrigSize == 1) {
      lastTempChangeLogProcessingIfIndividual = tempChangeLogEntryList.get(0);
    }
        
    int totalCountActuallyProcessed = 0;
    while (true) {
      int currentCountActuallyProcessed = (Integer)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
                  
              List<ChangeLogEntry> tempChangeLogEntryListProcessed = new ArrayList<ChangeLogEntry>();
              
              List<ChangeLogEntry> changeLogEntriesToSave = new ArrayList<ChangeLogEntry>();
  
              for (int i = 0; i < tempChangeLogEntryList.size(); i++) {
                
                ChangeLogEntry CHANGE_LOG_ENTRY = tempChangeLogEntryList.get(i);
                List<ChangeLogEntry> currentTempChangeLogEntriesBatch = new ArrayList<ChangeLogEntry>();
                currentTempChangeLogEntriesBatch.add(CHANGE_LOG_ENTRY);
                
                if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
                  
                  for (int j = i + 1; j < tempChangeLogEntryList.size(); j++) {
                    ChangeLogEntry NEXT_CHANGE_LOG_ENTRY = tempChangeLogEntryList.get(j);
                    if (NEXT_CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
                      currentTempChangeLogEntriesBatch.add(NEXT_CHANGE_LOG_ENTRY);
                      i++;
                    } else {
                      break;
                    }
                  }
                  
                  int numberProcessed = ChangeLogTempToEntity.processMembershipAdd(currentTempChangeLogEntriesBatch, changeLogEntriesToSave);
                  currentTempChangeLogEntriesBatch.subList(numberProcessed, currentTempChangeLogEntriesBatch.size()).clear();
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
  
                  for (int j = i + 1; j < tempChangeLogEntryList.size(); j++) {
                    ChangeLogEntry NEXT_CHANGE_LOG_ENTRY = tempChangeLogEntryList.get(j);
                    if (NEXT_CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                      currentTempChangeLogEntriesBatch.add(NEXT_CHANGE_LOG_ENTRY);
                      i++;
                    } else {
                      break;
                    }
                  }
                  
                  int numberProcessed = ChangeLogTempToEntity.processMembershipDelete(currentTempChangeLogEntriesBatch, changeLogEntriesToSave);
                  currentTempChangeLogEntriesBatch.subList(numberProcessed, currentTempChangeLogEntriesBatch.size()).clear();
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)
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
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD)) {
                  ChangeLogTempToEntity.processPrivilegeAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                  ChangeLogTempToEntity.processPrivilegeDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD)) {
                  ChangeLogTempToEntity.processAttributeAssignAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE)) {
                  ChangeLogTempToEntity.processAttributeAssignDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
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
                  ChangeLogTempToEntity.processAttributeAssignActionSetAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE)) {
                  ChangeLogTempToEntity.processAttributeAssignActionSetDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD)) {
                  ChangeLogTempToEntity.processAttributeDefNameSetAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE)) {
                  ChangeLogTempToEntity.processAttributeDefNameSetDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_ADD)) {
                  ChangeLogTempToEntity.processRoleSetAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ROLE_SET_DELETE)) {
                  ChangeLogTempToEntity.processRoleSetDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD)) {
                  ChangeLogTempToEntity.processAttributeAssignValueAdd(CHANGE_LOG_ENTRY);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE)) {
                  ChangeLogTempToEntity.processAttributeAssignValueDelete(CHANGE_LOG_ENTRY);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_SET_ADD)) {
                  ChangeLogTempToEntity.processGroupSetAdd(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                } else if (CHANGE_LOG_ENTRY.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_SET_DELETE)) {
                  ChangeLogTempToEntity.processGroupSetDelete(CHANGE_LOG_ENTRY, changeLogEntriesToSave);
                }
                
                for (ChangeLogEntry currentChangeLogEntry : currentTempChangeLogEntriesBatch) {
                  if (currentChangeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) ||
                      currentChangeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
                    
                    if (includeNonFlattenedMemberships) {
                      //insert into the non temp table
                      currentChangeLogEntry.setTempObject(false);
                      changeLogEntriesToSave.add(currentChangeLogEntry);
                    }
                  } else if (currentChangeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_ADD) ||
                      currentChangeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.PRIVILEGE_DELETE)) {
                    
                    if (includeNonFlattenedPrivileges) {
                      //insert into the non temp table
                      currentChangeLogEntry.setTempObject(false);
                      changeLogEntriesToSave.add(currentChangeLogEntry);
                    }
                  } else {
                    
                    //insert into the non temp table
                    currentChangeLogEntry.setTempObject(false);
                    changeLogEntriesToSave.add(currentChangeLogEntry);
                  }
                }
                
                tempChangeLogEntryListProcessed.addAll(currentTempChangeLogEntriesBatch);
                
                if (changeLogEntriesToSave.size() > tooManyChangeLogUpdatesSize) {
                  break;
                }
              }
              
              if (changeLogEntriesToSave.size() > 0) {
                // save to real change log
                int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 200);
                if (batchSize <= 0) {
                  batchSize = 1;
                }
                
                int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(changeLogEntriesToSave), batchSize);
                for (int i = 0; i < numberOfBatches; i++) {
                  List<ChangeLogEntry> currentBatch = GrouperUtil.batchList(changeLogEntriesToSave, batchSize, i);
                  GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(new LinkedHashSet<ChangeLogEntry>(currentBatch), false);
                }
                
                // save to the cached data tables
                Map<MultiKey, MultiKey> cachedDataAdds = new LinkedHashMap<MultiKey, MultiKey>();
                Collection<MultiKey> cachedDataDeletes = new LinkedHashSet<MultiKey>();
                String membershipAddChangeLogTypeId = ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId();
                String membershipDeleteChangeLogTypeId = ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId();
                String privilegeAddChangeLogTypeId = ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId();
                String privilegeDeleteChangeLogTypeId = ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId();
                for (ChangeLogEntry changeLogEntry : changeLogEntriesToSave) {
                  if (membershipAddChangeLogTypeId.equals(changeLogEntry.getChangeLogTypeId())) {
                    if ("flattened".equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.membershipType))) {
                      String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
                      String fieldName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName);
                      String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId);
                      String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
                      long createdOnLong = changeLogEntry.getCreatedOn().getTime();
                      
                      MultiKey key = new MultiKey(groupName, fieldName, sourceId, subjectId);
                      MultiKey value = new MultiKey(groupName, fieldName, sourceId, subjectId, createdOnLong);
                      if (cachedDataDeletes.contains(key)) {
                        cachedDataDeletes.remove(key);
                      } else {
                        cachedDataAdds.put(key, value);
                      }
                    }
                  } else if (membershipDeleteChangeLogTypeId.equals(changeLogEntry.getChangeLogTypeId())) {
                    if ("flattened".equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType))) {
                      String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
                      String fieldName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName);
                      String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
                      String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
                      MultiKey key = new MultiKey(groupName, fieldName, sourceId, subjectId);
                      if (cachedDataAdds.containsKey(key)) {
                        cachedDataAdds.remove(key);
                      } else {
                        cachedDataDeletes.add(key);
                      }
                    }
                  } else if (privilegeAddChangeLogTypeId.equals(changeLogEntry.getChangeLogTypeId())) {
                    if ("flattened".equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.membershipType)) && Membership.OWNER_TYPE_GROUP.equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType))) {
                      String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName);
                      String fieldName = AccessPrivilege.privToList(Privilege.getInstance(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName), true));
                      String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId);
                      String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId);
                      long createdOnLong = changeLogEntry.getCreatedOn().getTime();
                      
                      MultiKey key = new MultiKey(groupName, fieldName, sourceId, subjectId);
                      MultiKey value = new MultiKey(groupName, fieldName, sourceId, subjectId, createdOnLong);
                      if (cachedDataDeletes.contains(key)) {
                        cachedDataDeletes.remove(key);
                      } else {
                        cachedDataAdds.put(key, value);
                      }
                    }
                  } else if (privilegeDeleteChangeLogTypeId.equals(changeLogEntry.getChangeLogTypeId())) {
                    if ("flattened".equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.membershipType)) && Membership.OWNER_TYPE_GROUP.equals(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType))) {
                      String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName);
                      String fieldName = AccessPrivilege.privToList(Privilege.getInstance(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName), true));
                      String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId);
                      String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId);
                      MultiKey key = new MultiKey(groupName, fieldName, sourceId, subjectId);
                      if (cachedDataAdds.containsKey(key)) {
                        cachedDataAdds.remove(key);
                      } else {
                        cachedDataDeletes.add(key);
                      }
                    }
                  }
                }
                
                SqlCacheMembershipDao.insertSqlCacheMembershipsIfCacheable(cachedDataAdds.values());
                SqlCacheMembershipDao.deleteSqlCacheMembershipsIfCacheable(cachedDataDeletes);
              }
              
              // up to 1000 bind vars seem to be ok for oracle, mysql, and postgres
              if (tempChangeLogEntryListProcessed.size() > 0) {
                //delete from the temp
                int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(tempChangeLogEntryListProcessed), 1000);
                for (int j = 0; j < numberOfBatches; j++) {
                  List<ChangeLogEntry> currentBatch = GrouperUtil.batchList(tempChangeLogEntryListProcessed, 1000, j);
                  
                  List<Object> idsToDelete = new ArrayList<Object>();
                  List<Type> types = new ArrayList<Type>();
                  StringBuilder queryInClause = new StringBuilder();
                  for (int i = 0; i < currentBatch.size(); i++) {
                    ChangeLogEntry changeLogEntry = currentBatch.get(i);
                    idsToDelete.add(changeLogEntry.getId());
                    types.add(StringType.INSTANCE);
                    queryInClause.append("?");
      
                    if (i < currentBatch.size() - 1) {
                      queryInClause.append(", ");
                    }
                  }
      
                  int count = HibernateSession.bySqlStatic().executeSql("delete from grouper_change_log_entry_temp where id in (" + queryInClause.toString() + ")", idsToDelete, types);
                  if (count != currentBatch.size()) {
                    throw new RuntimeException("Bad count of " + count + " when deleting temp change log entries, expected " + tempChangeLogEntryListProcessed.size() + ".");
                  }
                }  
              }
          
              return tempChangeLogEntryListProcessed.size();
            }
      });
      
      totalCountActuallyProcessed += currentCountActuallyProcessed;
      
      if (totalCountActuallyProcessed == tempChangeLogEntryListOrigSize) {
        break;
      }
      
      tempChangeLogEntryList.subList(0, currentCountActuallyProcessed).clear();
    }
    
    int count = tempChangeLogEntryListOrigSize;

    if (count > 0 && hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addTotalCount(count);
      hib3GrouperLoaderLog.store();
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
    
    String internalIdString = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.internalId);
    
    PITGroup existing = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }

    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.parentStemId), true, false);
    if (pitStem == null) {
      LOG.warn("Skipping change since stem couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITGroup pitGroup = new PITGroup();
    pitGroup.setId(GrouperUuid.getUuid());
    pitGroup.setSourceId(id);
    pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name));  
    pitGroup.setStemId(pitStem.getId());
    pitGroup.setContextId(contextId);
    pitGroup.setActiveDb("T");
    pitGroup.setStartTimeDb(time);
    
    if (!StringUtils.isEmpty(internalIdString)) {
      pitGroup.setSourceInternalId(Long.parseLong(internalIdString));
    }
    
    pitGroup.saveOrUpdate();
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
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id), false);
      if (pitGroup == null) {
        return;
      }
      pitGroup.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name));
      pitGroup.setContextId(contextId);
      pitGroup.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged).equals("parentStemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_UPDATE.parentStemId.name());

      PITStem pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.parentStemId), true, false);
      if (pitParentStem == null) {
        LOG.warn("Skipping change since stem couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.id), false);
      if (pitGroup == null) {
        return;
      }
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
    
    Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(id, false);
    if (group != null) {
      // group may have just been disabled so do nothing here
      return;
    }

    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(id, false);
    if (pitGroup == null) {
      return;
    }
        
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
      PITStem pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(parentStemId, true, false);
      if (pitParentStem == null) {
        LOG.warn("Skipping change since parent stem couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      parentStemId = pitParentStem.getId();
    }
    
    pitStem.setId(GrouperUuid.getUuid());
    pitStem.setSourceId(id);
    pitStem.setNameDb(name);
    pitStem.setParentStemId(parentStemId);
    pitStem.setContextId(contextId);
    pitStem.setActiveDb("T");
    pitStem.setStartTimeDb(time);
    
    pitStem.saveOrUpdate();
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
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id), false);
      if (pitStem == null) {
        return;
      }
      pitStem.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.name));
      pitStem.setContextId(contextId);
      pitStem.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.propertyChanged).equals("parentStemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.STEM_UPDATE.parentStemId.name());
      
      PITStem pitParentStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.parentStemId), true, false);
      if (pitParentStem == null) {
        LOG.warn("Skipping change since parent stem couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.STEM_UPDATE.id), false);
      if (pitStem == null) {
        return;
      }
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
    
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_ADD.stemId), true, false);
    if (pitStem == null) {
      LOG.warn("Skipping change since stem couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
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
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id), false);
      if (pitAttributeDef == null) {
        LOG.warn("Skipping change since attr def couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeDef.setNameDb(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.name));
      pitAttributeDef.setContextId(contextId);
      pitAttributeDef.saveOrUpdate();
    } else if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.propertyChanged).equals("stemId")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.stemId.name());

      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.stemId), true, false);
      if (pitStem == null) {
        LOG.warn("Skipping change since stem couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_UPDATE.id), false);
      if (pitAttributeDef == null) {
        LOG.warn("Skipping change since attr def couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
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
    
    String internalIdString = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_FIELD_ADD.internalId);

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
    
    if (!StringUtils.isEmpty(internalIdString)) {
      pitField.setSourceInternalId(Long.parseLong(internalIdString));
    }
    
    pitField.saveOrUpdate();
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

    // String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId);
    // String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    // add group sets
    // GrouperDAOFactory.getFactory().getPITGroupSet().insertSelfPITGroupSetsByOwner(groupId, changeLogEntry.getCreatedOnDb(), contextId, true);
  }
  
  /**
   * @param changeLogEntry
   */
  private static void processGroupTypeUnassign(ChangeLogEntry changeLogEntry) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId.name());

    /*
    String typeId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId);
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId);
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    Long endTime = changeLogEntry.getCreatedOnDb();

    @SuppressWarnings("deprecation")
    GroupType groupType = GroupTypeFinder.findByUuid(typeId, false);
    if (groupType == null) {
      return;
    }
    
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
    */
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
    String subjectIdentifier0 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.subjectIdentifier0);
    if (GrouperUtil.isEmpty(subjectIdentifier0)) {
      subjectIdentifier0 = null;
    }
    
    Long time = changeLogEntry.getCreatedOnDb();
    
    String internalIdString = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_ADD.internalId);

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
    pitMember.setSubjectIdentifier0(subjectIdentifier0);
    pitMember.setActiveDb("T");
    pitMember.setStartTimeDb(time);
    
    if (!StringUtils.isEmpty(internalIdString)) {
      pitMember.setSourceInternalId(Long.parseLong(internalIdString));
    }
    
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
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectTypeId") ||
        changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectIdentifier0")) {
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectSourceId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBER_UPDATE.subjectTypeId.name());

      PITMember pitMember = null;
      if (changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged).equals("subjectIdentifier0")) {
        // because of the odd way that member attributes are updated (in a separate transaction), it's possible that the object was previously deleted.
        pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(
            changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id), false);
      } else {
        pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(
            changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id), false);
      }

      if (pitMember == null) {
        return;
      }
      
      pitMember.setSubjectId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      pitMember.setSubjectSourceId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      pitMember.setSubjectTypeId(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      String subjectIdentifier0 = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0);
      if (GrouperUtil.isEmpty(subjectIdentifier0)) {
        subjectIdentifier0 = null;
      }
      pitMember.setSubjectIdentifier0(subjectIdentifier0);
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
   * @param changeLogEntriesToSave
   * @return number processed
   */
  private static int processMembershipAdd(List<ChangeLogEntry> changeLogEntries, List<ChangeLogEntry> changeLogEntriesToSave) {
    
    final int tooManyChangeLogUpdatesSize = GrouperLoaderConfig.retrieveConfig().propertyValueIntRequired("changeLog.tooManyChangeLogUpdatesSize");

    Set<String> ids = new LinkedHashSet<String>();
    Set<String> groupIds = new LinkedHashSet<String>();
    Set<String> fieldIds = new LinkedHashSet<String>();
    Set<String> memberIds = new LinkedHashSet<String>();

    for (ChangeLogEntry changeLogEntry : changeLogEntries) {
      LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
      
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.id.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.groupId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_ADD.memberId.name());
      
      ids.add(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.id));
      groupIds.add(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
      fieldIds.add(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
      memberIds.add(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    }
    
    Map<String, PITMembership> pitMembershipsActiveMap = new LinkedHashMap<String, PITMembership>();
    Map<String, Set<PITMembership>> pitMembershipsAllMap = new LinkedHashMap<String, Set<PITMembership>>();

    Set<PITMembership> pitMembershipsAll = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIds(ids);
    for (PITMembership pitMembership : pitMembershipsAll) {
      String currSourceId = pitMembership.getSourceId();
      if (!pitMembershipsAllMap.containsKey(currSourceId)) {
        pitMembershipsAllMap.put(currSourceId, new LinkedHashSet<PITMembership>());
      }
      pitMembershipsAllMap.get(currSourceId).add(pitMembership);
      
      if (pitMembership.isActive()) {
        pitMembershipsActiveMap.put(currSourceId, pitMembership);
      }
    }
    
    Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdsActive(groupIds);
    Map<String, PITGroup> pitGroupsMap = new LinkedHashMap<String, PITGroup>();
    for (PITGroup pitGroup : pitGroups) {
      pitGroupsMap.put(pitGroup.getSourceId(), pitGroup);
    }
    
    Set<PITField> pitFields = GrouperDAOFactory.getFactory().getPITField().findBySourceIdsActive(fieldIds);
    Map<String, PITField> pitFieldsMap = new LinkedHashMap<String, PITField>();
    for (PITField pitField : pitFields) {
      pitFieldsMap.put(pitField.getSourceId(), pitField);
    }
    
    Set<PITMember> pitMembers = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdsActive(memberIds);
    Map<String, PITMember> pitMembersMap = new LinkedHashMap<String, PITMember>();
    for (PITMember pitMember : pitMembers) {
      pitMembersMap.put(pitMember.getSourceId(), pitMember);
    }

    int count = 0;
    for (ChangeLogEntry changeLogEntry : changeLogEntries) {
      count++;
      
      String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.id);
      String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId);
      String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId);
      String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId);
      Long time = changeLogEntry.getCreatedOnDb();
      
      PITMembership existing = pitMembershipsActiveMap.get(id);
      if (existing != null) {
        LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
        continue;
      }
      
      PITGroup pitGroup = pitGroupsMap.get(groupId);
      if (pitGroup == null) {
        // out of order in change log?
        pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupId, true, false);
        if (pitGroup == null) {
          LOG.warn("Skipping change since group couldn't be found: " + changeLogEntry.toStringDeep());
          continue;
        }
      }
      
      PITField pitField = pitFieldsMap.get(fieldId);
      if (pitField == null) {
        throw new RuntimeException("Could not find pit field: " + fieldId);
      }
      
      PITMember pitMember = pitMembersMap.get(memberId);
      if (pitMember == null) {
        // out of order in change log?
        pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(memberId, true, false);
        if (pitMember == null) {
          LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
          continue;
        }
      }
            
      PITMembership pitMembership = new PITMembership();
      pitMembership.setId(GrouperUuid.getUuid());
      pitMembership.setSourceId(id);
      pitMembership.setOwnerGroupId(pitGroup.getId());
      pitMembership.setMemberId(pitMember.getId());
      pitMembership.setMember(pitMember);
      pitMembership.setFieldId(pitField.getId());
      pitMembership.setActiveDb("T");
      pitMembership.setStartTimeDb(time);
      
      if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
        pitMembership.setContextId(changeLogEntry.getContextId());
      }
      
      boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);
      boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
      boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
      boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
      pitMembership.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
      pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
      pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
      pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
      
      pitMembership.setSaveChangeLogUpdates(false);
      pitMembership.save(pitMembershipsAllMap.get(id));
      changeLogEntriesToSave.addAll(pitMembership.getChangeLogUpdates());
      pitMembership.clearChangeLogUpdates();
      
      if (changeLogEntriesToSave.size() > tooManyChangeLogUpdatesSize) {
        return count;
      }
    }
    
    return count;
  }
  
  /**
   * If a membership gets deleted, then the membership needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   * @return number processed
   */
  private static int processMembershipDelete(List<ChangeLogEntry> changeLogEntries, List<ChangeLogEntry> changeLogEntriesToSave) {
    
    final int tooManyChangeLogUpdatesSize = GrouperLoaderConfig.retrieveConfig().propertyValueIntRequired("changeLog.tooManyChangeLogUpdatesSize");

    Set<String> ids = new LinkedHashSet<String>();
    Set<String> pitMemberIds = new LinkedHashSet<String>();

    for (ChangeLogEntry changeLogEntry : changeLogEntries) {
      LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
      assertNotEmpty(changeLogEntry, ChangeLogLabels.MEMBERSHIP_DELETE.id.name());      
      ids.add(changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id));
    }

    Map<String, PITMembership> pitMembershipsActiveMap = new LinkedHashMap<String, PITMembership>();
    Set<PITMembership> pitMembershipsActive = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdsActive(ids);
    for (PITMembership pitMembership : pitMembershipsActive) {
      String currSourceId = pitMembership.getSourceId();
      pitMembershipsActiveMap.put(currSourceId, pitMembership);
      
      pitMemberIds.add(pitMembership.getMemberId());
    }
    
    Set<PITMember> pitMembers = GrouperDAOFactory.getFactory().getPITMember().findByIds(pitMemberIds);
    Map<String, PITMember> pitMembersMap = new LinkedHashMap<String, PITMember>();
    for (PITMember pitMember : pitMembers) {
      pitMembersMap.put(pitMember.getId(), pitMember);
    }
    
    int count = 0;
    for (ChangeLogEntry changeLogEntry : changeLogEntries) {
      count++;
      
      String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.id);
      Long time = changeLogEntry.getCreatedOnDb();
  
      PITMembership pitMembership = pitMembershipsActiveMap.get(id);
      if (pitMembership == null) {
        continue;
      }
      
      pitMembership.setEndTimeDb(time);
      pitMembership.setActiveDb("F");
      pitMembership.setMember(pitMembersMap.get(pitMembership.getMemberId()));
  
      if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
        pitMembership.setContextId(changeLogEntry.getContextId());
      }
      
      boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);
      boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
      boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
      boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
      pitMembership.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
      pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
      pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
      pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
      
      pitMembership.setSaveChangeLogUpdates(false);
      pitMembership.update();
      changeLogEntriesToSave.addAll(pitMembership.getChangeLogUpdates());
      pitMembership.clearChangeLogUpdates();
      
      if (changeLogEntriesToSave.size() > tooManyChangeLogUpdatesSize) {
        return count;
      }
    }
    
    return count;
  }
  
  /**
   * If an access, naming, or attr def privilege gets added, the privilege needs to
   * get added to the PIT table.
   * @param changeLogEntry
   * @param changeLogEntriesToSave
   */
  private static void processPrivilegeAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(memberId, true, false);
    if (pitMember == null) {
      LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITMembership pitMembership = new PITMembership();
    pitMembership.setId(GrouperUuid.getUuid());
    pitMembership.setSourceId(id);
    pitMembership.setMemberId(pitMember.getId());
    pitMembership.setFieldId(pitField.getId());
    pitMembership.setActiveDb("T");
    pitMembership.setStartTimeDb(time);
    
    if (ownerType.equals(Membership.OWNER_TYPE_GROUP)) {
      PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId, true, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitMembership.setOwnerGroupId(pitOwner.getId());
    } else if (ownerType.equals(Membership.OWNER_TYPE_STEM)) {
      PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(ownerId, true, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitMembership.setOwnerStemId(pitOwner.getId());
    } else if (ownerType.equals(Membership.OWNER_TYPE_ATTRIBUTE_DEF)) {
      PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(ownerId, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitMembership.setOwnerAttrDefId(pitOwner.getId());
    } else {
      throw new RuntimeException("unexpected ownerType: " + ownerType);
    }
    
    if (!GrouperUtil.isEmpty(changeLogEntry.getContextId())) {
      pitMembership.setContextId(changeLogEntry.getContextId());
    }
    
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitMembership.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(false);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    pitMembership.setSaveChangeLogUpdates(false);

    pitMembership.save();
    changeLogEntriesToSave.addAll(pitMembership.getChangeLogUpdates());
    pitMembership.clearChangeLogUpdates();
  }
  
  
  /**
   * If an access, naming, or attr def privilege gets deleted, the privilege needs to
   * get deleted from the PIT table.
   * @param changeLogEntry
   * @param changeLogEntriesToSave
   */
  private static void processPrivilegeDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitMembership.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(false);
    pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    pitMembership.setSaveChangeLogUpdates(false);

    pitMembership.update();
    changeLogEntriesToSave.addAll(pitMembership.getChangeLogUpdates());
    pitMembership.clearChangeLogUpdates();
  }
  
  /**
   * If an attribute assign gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeAssignAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefNameId, false);
    if (pitAttributeDefName == null) {
      LOG.warn("Skipping change since attr def name couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(actionId, false);
    if (pitAttributeAssignAction == null) {
      LOG.warn("Skipping change since action couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
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
      PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId1, true, false);
      if (pitOwner1 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
    } else if (AttributeAssignType.stem.name().equals(assignType)) {
      PITStem pitOwner1 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(ownerId1, true, false);
      if (pitOwner1 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssign.setOwnerStemId(pitOwner1.getId());
    } else if (AttributeAssignType.member.name().equals(assignType)) {
      PITMember pitOwner1 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(ownerId1, true, false);
      if (pitOwner1 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssign.setOwnerMemberId(pitOwner1.getId());
    } else if (AttributeAssignType.attr_def.name().equals(assignType)) {
      PITAttributeDef pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(ownerId1, false);
      if (pitOwner1 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssign.setOwnerAttributeDefId(pitOwner1.getId());
    } else if (AttributeAssignType.any_mem.name().equals(assignType)) {
      PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerId1, true, false);
      if (pitOwner1 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      PITMember pitOwner2 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(ownerId2, true, false);
      if (pitOwner2 == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
      pitAttributeAssign.setOwnerMemberId(pitOwner2.getId());
    } else if (AttributeAssignType.imm_mem.name().equals(assignType)) {
      PITMembership pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(ownerId1, false);
      if (pitOwner1 == null) {
        // it may be disabled..
        pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(ownerId1, false);
        if (pitOwner1 == null) {
          // maybe we need to add a disabled pit membership
          Membership owner1 = GrouperDAOFactory.getFactory().getMembership().findByUuid(ownerId1, false, false);
          if (owner1 == null) {
            LOG.warn("Skipping change since unable to find membership in either PIT and non-PIT tables: " + changeLogEntry.toStringDeep());
            return;
          }
          
          pitOwner1 = new PITMembership();
          pitOwner1.setId(GrouperUuid.getUuid());
          pitOwner1.setSourceId(owner1.getImmediateMembershipId());
          PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(owner1.getMemberUuid(), true, false);
          if (pitMember == null) {
            LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
            return;
          }
          pitOwner1.setMemberId(pitMember.getId());
          pitOwner1.setFieldId(GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(owner1.getFieldId(), true).getId());
          pitOwner1.setActiveDb("T");
          pitOwner1.setStartTimeDb(owner1.getCreateTimeLong() * 1000L);
          if (!owner1.isEnabled()) {
            pitOwner1.setActiveDb("F");
            pitOwner1.setEndTimeDb(owner1.getCreateTimeLong() * 1000L);
          }
          if (owner1.getOwnerGroupId() != null) {
            PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(owner1.getOwnerGroupId(), true, false);
            if (pitOwner == null) {
              LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
              return;
            }
            pitOwner1.setOwnerGroupId(pitOwner.getId());
          } else if (owner1.getOwnerStemId() != null) {
            PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(owner1.getOwnerStemId(), true, false);
            if (pitOwner == null) {
              LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
              return;
            }
            pitOwner1.setOwnerStemId(pitOwner.getId());
          } else if (owner1.getOwnerAttrDefId() != null) {
            PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(owner1.getOwnerAttrDefId(), false);
            if (pitOwner == null) {
              LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
              return;
            }
            pitOwner1.setOwnerAttrDefId(pitOwner.getId());
          } else {
            throw new RuntimeException("unexpected owner: " + owner1);
          }
          pitOwner1.setContextId(owner1.getContextId());
          pitOwner1.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(false);
          pitOwner1.setFlatMembershipNotificationsOnSaveOrUpdate(false);
          pitOwner1.setFlatPrivilegeNotificationsOnSaveOrUpdate(false);
          pitOwner1.save();
        }
      }
      pitAttributeAssign.setOwnerMembershipId(pitOwner1.getId());
    } else {
      // this must be an attribute assign of an attribute assign.  foreign keys will make sure we're right.
      PITAttributeAssign pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(ownerId1, false);
      if (pitOwner1 == null) {
        // it may be disabled..
        pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(ownerId1, false);
        
        if (pitOwner1 == null) {
          LOG.warn("Skipping change pit owner not found: " + changeLogEntry.toStringDeep());
          return;
        }
      }
      pitAttributeAssign.setOwnerAttributeAssignId(pitOwner1.getId());
    }
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeAssign.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeAssign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);

    pitAttributeAssign.setSaveChangeLogUpdates(false);

    pitAttributeAssign.save();
    changeLogEntriesToSave.addAll(pitAttributeAssign.getChangeLogUpdates());
    pitAttributeAssign.clearChangeLogUpdates();
  }
  
  /**
   * If an attribute assign gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeAssign.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeAssign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssign.setSaveChangeLogUpdates(false);

    pitAttributeAssign.update();
    changeLogEntriesToSave.addAll(pitAttributeAssign.getChangeLogUpdates());
    pitAttributeAssign.clearChangeLogUpdates();
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
      pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(attributeAssignId, false);
      
      if (pitAttributeAssign == null) {
        LOG.warn("Skipping change since pitAttributeAssign not found: " + changeLogEntry.toStringDeep());
        return;
      }
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
    
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDefId, false);
    if (pitAttributeDef == null) {
      LOG.warn("Skipping change since attr def couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stemId, true, false);
    if (pitStem == null) {
      LOG.warn("Skipping change since stem couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
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
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_DEF_NAME_UPDATE.id), false);
      if (pitAttributeDefName == null) {
        LOG.warn("Skipping change since attr def name couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
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
    
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDefId, false);
    if (pitAttributeDef == null) {
      LOG.warn("Skipping change since attr def couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    
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
          changeLogEntry.retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_UPDATE.id), false);
      if (pitAttributeAssignAction == null) {
        return;
      }
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
  private static void processAttributeAssignActionSetAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    PITAttributeAssignAction pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(ifHas, false);
    if (pitIfHas == null) {
      LOG.warn("Skipping change since ifHas couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITAttributeAssignAction pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(thenHas, false);
    if (pitThenHas == null) {
      LOG.warn("Skipping change since thenHas group couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITAttributeAssignActionSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(parent, false);
    
    PITAttributeAssignActionSet pitAttributeAssignActionSet = new PITAttributeAssignActionSet();
    pitAttributeAssignActionSet.setId(GrouperUuid.getUuid());
    pitAttributeAssignActionSet.setSourceId(id);
    pitAttributeAssignActionSet.setDepth(Integer.parseInt(depth));
    pitAttributeAssignActionSet.setIfHasAttrAssignActionId(pitIfHas.getId());
    pitAttributeAssignActionSet.setThenHasAttrAssignActionId(pitThenHas.getId());
    
    if (Integer.parseInt(depth) == 0) {
      pitAttributeAssignActionSet.setParentAttrAssignActionSetId(pitAttributeAssignActionSet.getId());
    } else {
      if (pitParent == null) {
        LOG.warn("Skipping change since parent couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeAssignActionSet.setParentAttrAssignActionSetId(pitParent.getId()); 
    }    
    
    pitAttributeAssignActionSet.setActiveDb("T");
    pitAttributeAssignActionSet.setStartTimeDb(time);
    pitAttributeAssignActionSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeAssignActionSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeAssignActionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssignActionSet.setSaveChangeLogUpdates(false);

    pitAttributeAssignActionSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitAttributeAssignActionSet.getChangeLogUpdates());
    pitAttributeAssignActionSet.clearChangeLogUpdates();
  }
  
  /**
   * If an attribute assign action set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeAssignActionSetDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeAssignActionSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeAssignActionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeAssignActionSet.setSaveChangeLogUpdates(false);

    pitAttributeAssignActionSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitAttributeAssignActionSet.getChangeLogUpdates());
    pitAttributeAssignActionSet.clearChangeLogUpdates();
  }
  
  /**
   * If an attribute def name set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    PITAttributeDefName pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(ifHas, false);
    if (pitIfHas == null) {
      LOG.warn("Skipping change since ifHas couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITAttributeDefName pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(thenHas, false);
    if (pitThenHas == null) {
      LOG.warn("Skipping change since thenHas couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITAttributeDefNameSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(parent, false);

    PITAttributeDefNameSet pitAttributeDefNameSet = new PITAttributeDefNameSet();
    pitAttributeDefNameSet.setId(GrouperUuid.getUuid());
    pitAttributeDefNameSet.setSourceId(id);
    pitAttributeDefNameSet.setDepth(Integer.parseInt(depth));
    pitAttributeDefNameSet.setIfHasAttributeDefNameId(pitIfHas.getId());
    pitAttributeDefNameSet.setThenHasAttributeDefNameId(pitThenHas.getId());
    
    if (Integer.parseInt(depth) == 0) {
      pitAttributeDefNameSet.setParentAttrDefNameSetId(pitAttributeDefNameSet.getId());
    } else {
      if (pitParent == null) {
        LOG.warn("Skipping change since parent couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitAttributeDefNameSet.setParentAttrDefNameSetId(pitParent.getId());
    }
    
    pitAttributeDefNameSet.setActiveDb("T");
    pitAttributeDefNameSet.setStartTimeDb(time);
    pitAttributeDefNameSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeDefNameSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeDefNameSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeDefNameSet.setSaveChangeLogUpdates(false);

    pitAttributeDefNameSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitAttributeDefNameSet.getChangeLogUpdates());
    pitAttributeDefNameSet.clearChangeLogUpdates();
  }
  
  /**
   * If an attribute def name set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processAttributeDefNameSetDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitAttributeDefNameSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitAttributeDefNameSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitAttributeDefNameSet.setSaveChangeLogUpdates(false);

    pitAttributeDefNameSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitAttributeDefNameSet.getChangeLogUpdates());
    pitAttributeDefNameSet.clearChangeLogUpdates();
  }
  
  /**
   * If a role set set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processRoleSetAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    PITGroup pitIfHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ifHas, true, false);
    if (pitIfHas == null) {
      LOG.warn("Skipping change since ifHas group couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITGroup pitThenHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(thenHas, true, false);
    if (pitThenHas == null) {
      LOG.warn("Skipping change since thenHas group couldn't be found: " + changeLogEntry.toStringDeep());
      return;
    }
    PITRoleSet pitParent = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(parent, false);

    PITRoleSet pitRoleSet = new PITRoleSet();
    pitRoleSet.setId(GrouperUuid.getUuid());
    pitRoleSet.setSourceId(id);
    pitRoleSet.setDepth(Integer.parseInt(depth));
    pitRoleSet.setIfHasRoleId(pitIfHas.getId());
    pitRoleSet.setThenHasRoleId(pitThenHas.getId());
    
    if (Integer.parseInt(depth) == 0) {
      pitRoleSet.setParentRoleSetId(pitRoleSet.getId());
    } else {
      if (pitParent == null) {
        LOG.warn("Skipping change since parent couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitRoleSet.setParentRoleSetId(pitParent.getId());
    }
    
    pitRoleSet.setActiveDb("T");
    pitRoleSet.setStartTimeDb(time);
    pitRoleSet.setContextId(contextId);

    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitRoleSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitRoleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitRoleSet.setSaveChangeLogUpdates(false);

    pitRoleSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitRoleSet.getChangeLogUpdates());
    pitRoleSet.clearChangeLogUpdates();
  }
  
  /**
   * If a group set gets added, insert into pit table.
   * @param changeLogEntry
   */
  private static void processGroupSetAdd(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());

    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_ADD.id.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_ADD.depth.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_ADD.fieldId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_ADD.memberFieldId.name());
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_ADD.parentGroupSetId.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.id);
    String depth = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.depth);
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.fieldId);
    String memberFieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.memberFieldId);
    String parentGroupSetId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.parentGroupSetId);
    String ownerGroupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.ownerGroupId);
    String ownerStemId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.ownerStemId);
    String ownerAttributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.ownerAttributeDefId);
    String memberGroupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.memberGroupId);
    String memberStemId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.memberStemId);
    String memberAttributeDefId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_ADD.memberAttributeDefId);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();
    
    PITGroupSet existing = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(id, false);
    if (existing != null) {
      LOG.warn("Skipping change since already in PIT: " + changeLogEntry.toStringDeep());
      return;
    }

    
    PITGroupSet pitGroupSet = new PITGroupSet();
    pitGroupSet.setId(GrouperUuid.getUuid());
    pitGroupSet.setSourceId(id);
    pitGroupSet.setDepth(Integer.parseInt(depth));
    pitGroupSet.setActiveDb("T");
    pitGroupSet.setStartTimeDb(time);
    pitGroupSet.setContextId(contextId);
    
    if (!StringUtils.isEmpty(ownerGroupId)) {
      PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(ownerGroupId, true, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setOwnerGroupId(pitOwner.getId());
    } else if (!StringUtils.isEmpty(ownerStemId)) {
      PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(ownerStemId, true, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setOwnerStemId(pitOwner.getId());
    } else if (!StringUtils.isEmpty(ownerAttributeDefId)) {
      PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(ownerAttributeDefId, false);
      if (pitOwner == null) {
        LOG.warn("Skipping change since owner couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setOwnerAttrDefId(pitOwner.getId());
    } else {
      throw new RuntimeException("missing owner in change log for group set id " + id);
    }
    
    if (!StringUtils.isEmpty(memberGroupId)) {
      PITGroup pitMember = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(memberGroupId, true, false);
      if (pitMember == null) {
        LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setMemberGroupId(pitMember.getId());
    } else if (!StringUtils.isEmpty(memberStemId)) {
      PITStem pitMember = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(memberStemId, true, false);
      if (pitMember == null) {
        LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setMemberStemId(pitMember.getId());
    } else if (!StringUtils.isEmpty(memberAttributeDefId)) {
      PITAttributeDef pitMember = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(memberAttributeDefId, false);
      if (pitMember == null) {
        LOG.warn("Skipping change since member couldn't be found: " + changeLogEntry.toStringDeep());
        return;
      }
      pitGroupSet.setMemberAttrDefId(pitMember.getId());
    } else {
      throw new RuntimeException("missing member in change log for group set id " + id);
    }
    
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldId, true);
    pitGroupSet.setFieldId(pitField.getId());
    
    PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(memberFieldId, true);
    pitGroupSet.setMemberFieldId(pitMemberField.getId());

    if (Integer.parseInt(depth) == 0) {
      pitGroupSet.setParentId(pitGroupSet.getId());
    } else {
      PITGroupSet pitParent = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(parentGroupSetId, false);
      
      if (pitParent == null) {
        // missing the parent, skip and let the pit sync correct it.
        // this could happen if change log entries are out of order.
        // or if upgrading from a pre-2.6.17 release where (for example) a container not upgraded yet creates a group and the group is deleted before pit sync runs
        return;
      }
      
      pitGroupSet.setParentId(pitParent.getId());
    }

    if (pitGroupSet.getDepth() > 0) {
      boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);
      boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
      boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
      pitGroupSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
      pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
      pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    }
    
    pitGroupSet.setSaveChangeLogUpdates(false);

    pitGroupSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitGroupSet.getChangeLogUpdates());
    pitGroupSet.clearChangeLogUpdates();
  }
  
  /**
   * If a role set set gets delete, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processRoleSetDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
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
    
    boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);
    boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
    pitRoleSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
    pitRoleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
    
    pitRoleSet.setSaveChangeLogUpdates(false);

    pitRoleSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitRoleSet.getChangeLogUpdates());
    pitRoleSet.clearChangeLogUpdates();
  }
  
  /**
   * If a group set gets deleted, add end time to pit row.
   * @param changeLogEntry
   */
  private static void processGroupSetDelete(ChangeLogEntry changeLogEntry, List<ChangeLogEntry> changeLogEntriesToSave) {
    
    LOG.debug("Processing change: " + changeLogEntry.toStringDeep());
    
    assertNotEmpty(changeLogEntry, ChangeLogLabels.GROUP_SET_DELETE.id.name());

    String id = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_SET_DELETE.id);
    Long time = changeLogEntry.getCreatedOnDb();
    String contextId = GrouperUtil.isEmpty(changeLogEntry.getContextId()) ? null : changeLogEntry.getContextId();

    PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(id, false);
    if (pitGroupSet == null) {
      return;
    }
    
    pitGroupSet.setEndTimeDb(time);
    pitGroupSet.setActiveDb("F");
    pitGroupSet.setContextId(contextId);
    
    if (pitGroupSet.getDepth() > 0) {
      boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);
      boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
      boolean includeSubjectsWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeSubjectsWithPermissionChanges", false);
      pitGroupSet.setNotificationsForSubjectsWithPermissionChangesOnSaveOrUpdate(includeSubjectsWithPermissionChanges);
      pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
      pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
    }
    
    pitGroupSet.setSaveChangeLogUpdates(false);

    pitGroupSet.saveOrUpdate();
    changeLogEntriesToSave.addAll(pitGroupSet.getChangeLogUpdates());
    pitGroupSet.clearChangeLogUpdates();
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
