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
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.pit.GrouperPIT;
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
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class SyncPITTables {

  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
  /** Whether or not to log details */
  private boolean logDetails = true;
  
  /** Whether or not to create a report for GrouperReport */
  private boolean createReport = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncPITTables.class);
  
  /** detailed output for grouper report */
  private StringBuilder report = new StringBuilder();
  
  /**
   * Whether or not to print out results of what's being done.  Defaults to true.
   * @param showResults
   * @return SyncPITTables
   */
  public SyncPITTables showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates.  Defaults to true.
   * @param saveUpdates
   * @return SyncPITTables
   */
  public SyncPITTables saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }
  
  /**
   * Whether or not to log details.  Defaults to true.
   * @param logDetails
   * @return SyncPITTables
   */
  public SyncPITTables logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }
  
  /**
   * Whether or not to create a report.  Defaults to false.
   * @param createReport
   * @return SyncPITTables
   */
  public SyncPITTables createReport(boolean createReport) {
    this.createReport = createReport;
    return this;
  }
  
  /**
   * No longer applicable.  Changes get sent to the temp change log.
   * @param sendNotifications
   * @return SyncPITTables
   * @deprecated
   */
  public SyncPITTables sendFlattenedNotifications(boolean sendNotifications) {
    return this;
  }
  
  /**
   * No longer applicable.  Changes get sent to the temp change log.
   * @param sendNotifications
   * @return SyncPITTables
   * @deprecated
   */
  public SyncPITTables sendPermissionNotifications(boolean sendNotifications) {
    return this;
  }
  
  /**
   * Sync all point in time tables
   * @return the number of updates made
   */
  public long syncAllPITTables() {
    return (long)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        long count = 0;

        clearReport();
        
        int tempChangeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
        showStatus("Number of entries in grouper_change_log_entry_temp: " + tempChangeLogCount);
        if (tempChangeLogCount > 0) {
          showStatus("For best results, run loaderRunOneJob(\"CHANGE_LOG_changeLogTempToChangeLog\") first.");
        }
        
        count += processMissingActivePITFields();
        count += processMissingActivePITMembers();
        count += processMissingActivePITStems();
        count += processMissingActivePITGroups();
        count += processMissingActivePITRoleSets();
        count += processMissingActivePITAttributeDefs();
        count += processMissingActivePITAttributeDefNames();
        count += processMissingActivePITAttributeDefNameSets();
        count += processMissingActivePITAttributeAssignActions();
        count += processMissingActivePITAttributeAssignActionSets();
        count += processMissingActivePITGroupSets();
        count += processMissingActivePITMemberships();
        count += processMissingActivePITAttributeAssigns();
        count += processMissingActivePITAttributeAssignValues();
        count += processMissingActivePITConfigs();
        
        count += processMissingInactivePITAttributeAssignValues();
        count += processMissingInactivePITAttributeAssigns();
        count += processMissingInactivePITMemberships();
        count += processMissingInactivePITGroupSets();
        count += processMissingInactivePITAttributeAssignActionSets();
        count += processMissingInactivePITAttributeAssignActions();
        count += processMissingInactivePITAttributeDefNameSets();
        count += processMissingInactivePITAttributeDefNames();
        count += processMissingInactivePITAttributeDefs();
        count += processMissingInactivePITRoleSets();
        count += processMissingInactivePITGroups();
        count += processMissingInactivePITStems();
        count += processMissingInactivePITMembers();
        count += processMissingInactivePITFields();
        count += processMissingInactivePITConfigs();
        return count;
      }
    });


  }
 
  /**
   * @return detailed output of the sync
   */
  public String getDetailedOutput() {
    return report.toString();
  }
  
  /**
   * clear report
   */
  public void clearReport() {
    report = new StringBuilder();
  }

  /**
   * Add missing point in time memberships.
   * @return the number of missing point in time memberships
   */
  public long processMissingActivePITMemberships() {
    showStatus("\n\nSearching for missing active point in time memberships");
    
    long totalProcessed = 0;

    Set<Membership> mships = GrouperDAOFactory.getFactory().getPITMembership().findMissingActivePITMemberships();
    showStatus("Found " + mships.size() + " missing active point in time memberships");

    for (Membership mship : mships) {
      
      logDetail("Found missing point in time membership with ownerId: " + mship.getOwnerId() + 
          ", memberId: " + mship.getMemberUuid() + ", fieldId: " + mship.getFieldId());
            
      if (saveUpdates) {
        mship.addMembershipAddChangeLog(mship.getContextId());
      }
      
      totalProcessed++;
    }
    
    if (mships.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time attribute assign.
   * @return the number of missing point in time attribute assigns
   */
  public long processMissingActivePITAttributeAssigns() {
    showStatus("\n\nSearching for missing active point in time attribute assigns");
    
    long totalProcessed = 0;

    Set<AttributeAssign> assigns = GrouperDAOFactory.getFactory().getPITAttributeAssign().findMissingActivePITAttributeAssigns();
    showStatus("Found " + assigns.size() + " missing active point in time attribute assigns");

    // sort to avoid foreign key issues
    LinkedHashSet<AttributeAssign> assignsSorted = new LinkedHashSet<AttributeAssign>();
    for (AttributeAssign assign : assigns) {
      if (assign.getOwnerAttributeAssignId() == null) {
        assignsSorted.add(assign);
      }
    }
    
    for (AttributeAssign assign : assigns) {
      if (assign.getOwnerAttributeAssignId() != null) {
        assignsSorted.add(assign);
      }
    }
    
    for (AttributeAssign assign : assignsSorted) {
      
      logDetail("Found missing point in time attribute assign with id: " + assign.getId());
            
      if (saveUpdates) {
        
        String ownerId1 = null;
        String ownerId2 = null;
        
        if (AttributeAssignType.group.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerGroupId();
        } else if (AttributeAssignType.stem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerStemId();
        } else if (AttributeAssignType.member.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerMemberId();
        } else if (AttributeAssignType.attr_def.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerAttributeDefId();
        } else if (AttributeAssignType.any_mem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerGroupId();
          ownerId2 = assign.getOwnerMemberId();
        } else if (AttributeAssignType.imm_mem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerMembershipId();

          PITMembership pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(assign.getOwnerMembershipId(), false);
          if (pitOwner1 == null) {
            // assignment might be disabled..
            logDetail("Skipping " + assign.getId() + " since active owner was not found in point in time.");
            continue;
          }
        } else {
          // this must be an attribute assign of an attribute assign.  foreign keys will make sure we're right.
          ownerId1 = assign.getOwnerAttributeAssignId();

          PITAttributeAssign pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign.getOwnerAttributeAssignId(), false);
          if (pitOwner1 == null) {
            // assignment might be disabled..
            logDetail("Skipping " + assign.getId() + " since active owner was not found in point in time.");
            continue;
          }
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.id.name(), assign.getId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameId.name(), assign.getAttributeDefNameId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeAssignActionId.name(), assign.getAttributeAssignActionId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.assignType.name(), assign.getAttributeAssignTypeDb(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId1.name(), ownerId1,
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.ownerId2.name(), ownerId2,
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.attributeDefNameName.name(), assign.getAttributeDefName().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.action.name(), assign.getAttributeAssignAction().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ADD.disallowed.name(), assign.getDisallowedDb());
        
        if (!StringUtils.isEmpty(assign.getContextId())) {
          changeLogEntry.setContextId(assign.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (assigns.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time attribute assign values.
   * @return the number of missing point in time attribute assign values
   */
  public long processMissingActivePITAttributeAssignValues() {
    showStatus("\n\nSearching for missing active point in time attribute assign values");
    
    long totalProcessed = 0;

    Set<AttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findMissingActivePITAttributeAssignValues();
    showStatus("Found " + values.size() + " missing active point in time attribute assign values");

    for (AttributeAssignValue value : values) {
      
      logDetail("Found missing point in time attribute assign value with id: " + value.getId() + ", value: " + value.getValueFriendly());
            
      if (saveUpdates) {
        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(value.getAttributeAssignId(), false);
        if (pitAttributeAssign == null) {
          // assignment might be disabled..
          logDetail("Skipping " + value.getId() + " since active assignment was not found in point in time.");
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.id.name(), value.getId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeAssignId.name(), value.getAttributeAssignId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameId.name(), value.getAttributeAssign().getAttributeDefNameId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName.name(), value.getAttributeAssign().getAttributeDefName().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.value.name(), value.valueString(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.valueType.name(), value.getAttributeAssign().getAttributeDef().getValueType().name());;

        if (!StringUtils.isEmpty(value.getContextId())) {
          changeLogEntry.setContextId(value.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (values.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time attribute defs.
   * @return the number of missing point in time attribute defs
   */
  public long processMissingActivePITAttributeDefs() {
    showStatus("\n\nSearching for missing active point in time attribute defs");
    
    long totalProcessed = 0;

    Set<AttributeDef> attrs = GrouperDAOFactory.getFactory().getPITAttributeDef().findMissingActivePITAttributeDefs();
    showStatus("Found " + attrs.size() + " missing active point in time attribute defs");

    for (AttributeDef attr : attrs) {
      
      logDetail("Found missing point in time attribute def with id: " + attr.getId() + ", name: " + attr.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name and/or stemId
        PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attr.getId(), false);
        if (pitAttributeDef != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(attr.getStemId(), true);
          pitAttributeDef.setNameDb(attr.getName());
          pitAttributeDef.setStemId(pitStem.getId());
          if (!StringUtils.isEmpty(attr.getContextId())) {
            pitAttributeDef.setContextId(attr.getContextId());
          } else {
            pitAttributeDef.setContextId(null);
          }
          pitAttributeDef.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_ADD, 
            ChangeLogLabels.ATTRIBUTE_DEF_ADD.id.name(), attr.getUuid(), 
            ChangeLogLabels.ATTRIBUTE_DEF_ADD.name.name(), attr.getName(), 
            ChangeLogLabels.ATTRIBUTE_DEF_ADD.stemId.name(), attr.getStemId(),
            ChangeLogLabels.ATTRIBUTE_DEF_ADD.description.name(), attr.getDescription(),
            ChangeLogLabels.ATTRIBUTE_DEF_ADD.attributeDefType.name(), attr.getAttributeDefTypeDb());
        
        if (!StringUtils.isEmpty(attr.getContextId())) {
          changeLogEntry.setContextId(attr.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (attrs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time attribute def names.
   * @return the number of missing point in time attribute def names
   */
  public long processMissingActivePITAttributeDefNames() {
    showStatus("\n\nSearching for missing active point in time attribute def names");
    
    long totalProcessed = 0;

    Set<AttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findMissingActivePITAttributeDefNames();
    showStatus("Found " + attrs.size() + " missing active point in time attribute def names");

    for (AttributeDefName attr : attrs) {
      
      logDetail("Found missing point in time attribute def name with id: " + attr.getId() + ", name: " + attr.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name
        PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attr.getId(), false);
        if (pitAttributeDefName != null) {
          pitAttributeDefName.setNameDb(attr.getNameDb());
          if (!StringUtils.isEmpty(attr.getContextId())) {
            pitAttributeDefName.setContextId(attr.getContextId());
          } else {
            pitAttributeDefName.setContextId(null);
          }
          pitAttributeDefName.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_ADD, 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.id.name(), attr.getId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.name.name(), attr.getName(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.stemId.name(), attr.getStemId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.description.name(), attr.getDescription(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_ADD.attributeDefId.name(), attr.getAttributeDefId());
        
        if (!StringUtils.isEmpty(attr.getContextId())) {
          changeLogEntry.setContextId(attr.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (attrs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time attribute def name sets.
   * @return the number of missing point in time attribute def name sets
   */
  public long processMissingActivePITAttributeDefNameSets() {
    showStatus("\n\nSearching for missing active point in time attribute def name sets");
    
    long totalProcessed = 0;

    Set<AttributeDefNameSet> attrSets = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findMissingActivePITAttributeDefNameSets();
    showStatus("Found " + attrSets.size() + " missing active point in time attribute def name sets");

    for (AttributeDefNameSet attrSet : attrSets) {
      
      logDetail("Found missing point in time attribute def name set with id: " + attrSet.getId());
            
      if (saveUpdates) {

        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_ADD, 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.id.name(), attrSet.getId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.type.name(), attrSet.getTypeDb(),
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.ifHasAttributeDefNameId.name(), attrSet.getIfHasAttributeDefNameId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.thenHasAttributeDefNameId.name(), attrSet.getThenHasAttributeDefNameId(),
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.parentAttrDefNameSetId.name(), attrSet.getParentAttrDefNameSetId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_ADD.depth.name(), "" + attrSet.getDepth());
        
        if (!StringUtils.isEmpty(attrSet.getContextId())) {
          changeLogEntry.setContextId(attrSet.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (attrSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time groups.
   * @return the number of missing point in time groups
   */
  public long processMissingActivePITGroups() {
    showStatus("\n\nSearching for missing active point in time groups");
    
    long totalProcessed = 0;

    Set<Group> groups = GrouperDAOFactory.getFactory().getPITGroup().findMissingActivePITGroups();
    showStatus("Found " + groups.size() + " missing active point in time groups");

    for (Group group : groups) {
      
      logDetail("Found missing point in time group with id: " + group.getId() + ", name: " + group.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name and/or stemId
        PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(group.getId(), false);
        if (pitGroup != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(group.getParentUuid(), true);
          pitGroup.setNameDb(group.getName());  
          pitGroup.setStemId(pitStem.getId());
          if (!StringUtils.isEmpty(group.getContextId())) {
            pitGroup.setContextId(group.getContextId());
          } else {
            pitGroup.setContextId(null);
          }
          pitGroup.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry;
        if (group.getTypeOfGroup() == TypeOfGroup.entity) {
          changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ENTITY_ADD, 
              ChangeLogLabels.ENTITY_ADD.id.name(), 
              group.getUuid(), ChangeLogLabels.ENTITY_ADD.name.name(), 
              group.getName(), ChangeLogLabels.ENTITY_ADD.parentStemId.name(), group.getParentUuid(),
              ChangeLogLabels.ENTITY_ADD.displayName.name(), group.getDisplayName(),
              ChangeLogLabels.ENTITY_ADD.description.name(), group.getDescription());

        } else {
          changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_ADD, 
              ChangeLogLabels.GROUP_ADD.id.name(), 
              group.getUuid(), ChangeLogLabels.GROUP_ADD.name.name(), 
              group.getName(), ChangeLogLabels.GROUP_ADD.parentStemId.name(), group.getParentUuid(),
              ChangeLogLabels.GROUP_ADD.displayName.name(), group.getDisplayName(),
              ChangeLogLabels.GROUP_ADD.description.name(), group.getDescription(),
              ChangeLogLabels.GROUP_ADD.idIndex.name(), "" + group.getIdIndex());

        }
        if (!StringUtils.isEmpty(group.getContextId())) {
          changeLogEntry.setContextId(group.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (groups.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time group sets.
   * 
   * @return the number of missing point in time group sets
   */
  public long processMissingActivePITGroupSets() {
    showStatus("\n\nSearching for missing active point in time group sets");
    
    long totalProcessed = 0;

    List<GroupSet> groupSets = new LinkedList<GroupSet>(GrouperDAOFactory.getFactory().getPITGroupSet().findMissingActivePITGroupSets(null));
    showStatus("Found " + groupSets.size() + " missing active point in time group sets");

    Collections.sort(groupSets, new Comparator<GroupSet>() {

      public int compare(GroupSet o1, GroupSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    for (GroupSet groupSet : groupSets) {
      
      logDetail("Found missing point in time group set with id: " + groupSet.getId());
            
      if (saveUpdates) {
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_SET_ADD, 
            ChangeLogLabels.GROUP_SET_ADD.id.name(), groupSet.getId(), 
            ChangeLogLabels.GROUP_SET_ADD.ownerGroupId.name(), groupSet.getOwnerGroupId(), 
            ChangeLogLabels.GROUP_SET_ADD.ownerStemId.name(), groupSet.getOwnerStemId(), 
            ChangeLogLabels.GROUP_SET_ADD.ownerAttributeDefId.name(), groupSet.getOwnerAttrDefId(), 
            ChangeLogLabels.GROUP_SET_ADD.memberGroupId.name(), groupSet.getMemberGroupId(), 
            ChangeLogLabels.GROUP_SET_ADD.memberStemId.name(), groupSet.getMemberStemId(), 
            ChangeLogLabels.GROUP_SET_ADD.memberAttributeDefId.name(), groupSet.getMemberAttrDefId(), 
            ChangeLogLabels.GROUP_SET_ADD.fieldId.name(), groupSet.getFieldId(), 
            ChangeLogLabels.GROUP_SET_ADD.memberFieldId.name(), groupSet.getMemberFieldId(), 
            ChangeLogLabels.GROUP_SET_ADD.parentGroupSetId.name(), groupSet.getParentId(), 
            ChangeLogLabels.GROUP_SET_ADD.depth.name(), "" + groupSet.getDepth());
        
        if (!StringUtils.isEmpty(groupSet.getContextId())) {
          changeLogEntry.setContextId(groupSet.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (groupSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time role sets.
   * @return the number of missing point in time role sets
   */
  public long processMissingActivePITRoleSets() {
    showStatus("\n\nSearching for missing active point in time role sets");
    
    long totalProcessed = 0;

    Set<RoleSet> roleSets = GrouperDAOFactory.getFactory().getPITRoleSet().findMissingActivePITRoleSets();
    showStatus("Found " + roleSets.size() + " missing active point in time role sets");

    for (RoleSet roleSet : roleSets) {
      
      logDetail("Found missing point in time role set with id: " + roleSet.getId());
            
      if (saveUpdates) {        

        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ROLE_SET_ADD, 
            ChangeLogLabels.ROLE_SET_ADD.id.name(), roleSet.getId(), 
            ChangeLogLabels.ROLE_SET_ADD.type.name(), roleSet.getTypeDb(),
            ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId.name(), roleSet.getIfHasRoleId(), 
            ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId.name(), roleSet.getThenHasRoleId(),
            ChangeLogLabels.ROLE_SET_ADD.parentRoleSetId.name(), roleSet.getParentRoleSetId(), 
            ChangeLogLabels.ROLE_SET_ADD.depth.name(), "" + roleSet.getDepth());
        
        if (!StringUtils.isEmpty(roleSet.getContextId())) {
          changeLogEntry.setContextId(roleSet.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (roleSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time fields.
   * @return the number of missing point in time fields
   */
  public long processMissingActivePITFields() {
    showStatus("\n\nSearching for missing active point in time fields");
    
    long totalProcessed = 0;

    Set<Field> fields = GrouperDAOFactory.getFactory().getPITField().findMissingActivePITFields();
    showStatus("Found " + fields.size() + " missing active point in time fields");

    for (Field field : fields) {
      
      logDetail("Found missing point in time field with id: " + field.getUuid() + ", name: " + field.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name and/or type
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(field.getUuid(), false);
        if (pitField != null) {
          pitField.setNameDb(field.getName());
          pitField.setTypeDb(field.getTypeString());
          if (!StringUtils.isEmpty(field.getContextId())) {
            pitField.setContextId(field.getContextId());
          } else {
            pitField.setContextId(null);
          }
          pitField.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_FIELD_ADD, 
            ChangeLogLabels.GROUP_FIELD_ADD.id.name(), 
            field.getUuid(), ChangeLogLabels.GROUP_FIELD_ADD.name.name(), 
            field.getName(), null, 
            null,
            null, 
            null,
            ChangeLogLabels.GROUP_FIELD_ADD.type.name(), field.getTypeString()
        );
        
        if (!StringUtils.isEmpty(field.getContextId())) {
          changeLogEntry.setContextId(field.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (fields.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time members.
   * @return the number of missing point in time members
   */
  public long processMissingActivePITMembers() {
    showStatus("\n\nSearching for missing active point in time members");
    
    long totalProcessed = 0;

    Set<Member> members = GrouperDAOFactory.getFactory().getPITMember().findMissingActivePITMembers();
    showStatus("Found " + members.size() + " missing active point in time members");

    for (Member member : members) {
      
      logDetail("Found missing point in time member with id: " + member.getUuid() + ", subject id: " + member.getSubjectId());
            
      if (saveUpdates) {

        // note that we may just need to update the subjectId, subjectSourceId, and/or subjectTypeId
        PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(member.getUuid(), false);
        if (pitMember != null) {
          pitMember.setSubjectId(member.getSubjectIdDb());
          pitMember.setSubjectSourceId(member.getSubjectSourceIdDb());
          pitMember.setSubjectTypeId(member.getSubjectTypeId());
          pitMember.setSubjectIdentifier0(member.getSubjectIdentifier0());
          if (!StringUtils.isEmpty(member.getContextId())) {
            pitMember.setContextId(member.getContextId());
          } else {
            pitMember.setContextId(null);
          }
          pitMember.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBER_ADD, 
            ChangeLogLabels.MEMBER_ADD.id.name(), member.getUuid(), 
            ChangeLogLabels.MEMBER_ADD.subjectId.name(), member.getSubjectIdDb(), 
            ChangeLogLabels.MEMBER_ADD.subjectSourceId.name(), member.getSubjectSourceIdDb(),
            ChangeLogLabels.MEMBER_ADD.subjectTypeId.name(), member.getSubjectTypeId(),
            ChangeLogLabels.MEMBER_ADD.subjectIdentifier0.name(), member.getSubjectIdentifier0(),
            ChangeLogLabels.MEMBER_ADD.subjectIdentifier1.name(), member.getSubjectIdentifier1(),
            ChangeLogLabels.MEMBER_ADD.subjectIdentifier2.name(), member.getSubjectIdentifier2(),
            ChangeLogLabels.MEMBER_ADD.email0.name(), member.getEmail0());

        if (!StringUtils.isEmpty(member.getContextId())) {
          changeLogEntry.setContextId(member.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (members.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time grouper configs.
   * @return the number of missing point in time configs
   */
  public long processMissingActivePITConfigs() {
    showStatus("\n\nSearching for missing active point in time configs");
    
    long totalProcessed = 0;

    Set<GrouperConfigHibernate> configs = GrouperDAOFactory.getFactory().getPITConfig().findMissingActivePITConfigs();
    showStatus("Found " + configs.size() + " missing active point in time configs");

    for (GrouperConfigHibernate config : configs) {
      
      logDetail("Found missing point in time config with id: " + config.getId());
            
      if (saveUpdates) {
        PITGrouperConfigHibernate pitConfig = GrouperDAOFactory.getFactory().getPITConfig().findBySourceIdActive(config.getId(), false);
        if (pitConfig == null) {
          GrouperConfigHibernate.createNewPITGrouperConfigHibernate(null, "T", config, null, null);
        }
      }
      
      totalProcessed++;
    }
    
    if (configs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time stems.
   * @return the number of missing point in time stems
   */
  public long processMissingActivePITStems() {
    showStatus("\n\nSearching for missing active point in time stems");
    
    long totalProcessed = 0;

    Set<Stem> stems = GrouperDAOFactory.getFactory().getPITStem().findMissingActivePITStems();
    
    // the root stem may be returned because its parent stem id is null.  if it is returned and exists in point in time, remove it from the set...
    Stem rootStem = StemFinder.findRootStem(GrouperSession.staticGrouperSession());
    if (stems.contains(rootStem) && GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(rootStem.getUuid(), false) != null) {
      stems.remove(rootStem);
    }
    
    showStatus("Found " + stems.size() + " missing active point in time stems");

    LinkedHashSet<Stem> stemsSorted = new LinkedHashSet<Stem>();
    if (stems.contains(rootStem)) {
      stemsSorted.add(rootStem);
    }
    
    stemsSorted.addAll(stems);
    
    for (Stem stem : stemsSorted) {
      
      logDetail("Found missing point in time stem with id: " + stem.getUuid() + ", name: " + stem.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name and/or parentStemId
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stem.getUuid(), false);
        if (pitStem != null) {
          pitStem.setNameDb(stem.getNameDb());
          pitStem.setParentStemId(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stem.getParentUuid(), true).getId());
          if (!StringUtils.isEmpty(stem.getContextId())) {
            pitStem.setContextId(stem.getContextId());
          } else {
            pitStem.setContextId(null);
          }
          pitStem.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        //change log into temp table
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.STEM_ADD, 
            ChangeLogLabels.STEM_ADD.id.name(), 
            stem.getUuid(), ChangeLogLabels.STEM_ADD.name.name(), 
            stem.getName(), ChangeLogLabels.STEM_ADD.parentStemId.name(), stem.getParentUuid(),
            ChangeLogLabels.STEM_ADD.displayName.name(), stem.getDisplayName(),
            ChangeLogLabels.STEM_ADD.description.name(), stem.getDescription());

        if (!StringUtils.isEmpty(stem.getContextId())) {
          changeLogEntry.setContextId(stem.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (stems.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time actions.
   * @return the number of missing point in time actions
   */
  public long processMissingActivePITAttributeAssignActions() {
    showStatus("\n\nSearching for missing active point in time actions");
    
    long totalProcessed = 0;

    Set<AttributeAssignAction> actions = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findMissingActivePITAttributeAssignActions();
    showStatus("Found " + actions.size() + " missing active point in time actions");

    for (AttributeAssignAction action : actions) {
      
      logDetail("Found missing point in time action with id: " + action.getId() + ", name: " + action.getName());
            
      if (saveUpdates) {
        // note that we may just need to update the name
        PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action.getId(), false);
        if (pitAttributeAssignAction != null) {
          pitAttributeAssignAction.setNameDb(action.getNameDb());
          if (!StringUtils.isEmpty(action.getContextId())) {
            pitAttributeAssignAction.setContextId(action.getContextId());
          } else {
            pitAttributeAssignAction.setContextId(null);
          }
          pitAttributeAssignAction.saveOrUpdate();
          totalProcessed++;
          continue;
        }
        
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.id.name(), action.getId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.name.name(), action.getName(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_ADD.attributeDefId.name(), action.getAttributeDefId());

        if (!StringUtils.isEmpty(action.getContextId())) {
          changeLogEntry.setContextId(action.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (actions.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * Add missing point in time action sets.
   * @return the number of missing point in time action sets
   */
  public long processMissingActivePITAttributeAssignActionSets() {
    showStatus("\n\nSearching for missing active point in time action sets");
    
    long totalProcessed = 0;

    Set<AttributeAssignActionSet> actionSets = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findMissingActivePITAttributeAssignActionSets();
    showStatus("Found " + actionSets.size() + " missing active point in time action sets");

    for (AttributeAssignActionSet actionSet : actionSets) {
      
      logDetail("Found missing point in time action set with id: " + actionSet.getId());
            
      if (saveUpdates) {

        ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.id.name(), actionSet.getId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.type.name(), actionSet.getTypeDb(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.ifHasAttrAssnActionId.name(), actionSet.getIfHasAttrAssignActionId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.thenHasAttrAssnActionId.name(), actionSet.getThenHasAttrAssignActionId(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.parentAttrAssignActionSetId.name(), actionSet.getParentAttrAssignActionSetId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_ADD.depth.name(), "" + actionSet.getDepth());
        
        if (!StringUtils.isEmpty(actionSet.getContextId())) {
          changeLogEntry.setContextId(actionSet.getContextId());
        }
        
        changeLogEntry.save();
      }
      
      totalProcessed++;
    }
    
    if (actionSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }

  /**
   * End point in time memberships that are currently active but should be inactive.
   * @return the number of point in time memberships to end
   */
  public long processMissingInactivePITMemberships() {
    showStatus("\n\nSearching for point in time memberships that should be inactive");
 
    long totalProcessed = 0;

    Set<PITMembership> mships = GrouperDAOFactory.getFactory().getPITMembership().findMissingInactivePITMemberships();
    showStatus("Found " + mships.size() + " active point in time memberships that should be inactive");

    for (PITMembership mship : mships) {
      
      logDetail("Found active point in time membership that should be inactive with id: " + mship.getId() + ", ownerId: " + mship.getOwnerId() + 
          ", memberId: " + mship.getMemberId() + ", fieldId: " + mship.getFieldId());
      
      if (saveUpdates) {
        PITField pitField = mship.getPITField();
        PITMember pitMember = mship.getPITMember();
        
        String subjectName = null;
        
        // get the subject name if the subject is a group
        if (pitMember.getSubjectTypeId().equals("group")) {
          Group memberGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(pitMember.getSubjectId(), false, null);
          if (memberGroup != null) {
            subjectName = memberGroup.getName();
          }
        }
        
        if (pitField.getType().equals("list")) {
          PITGroup pitGroup = mship.getOwnerPITGroup();
          
          new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, 
              ChangeLogLabels.MEMBERSHIP_DELETE.id.name(), mship.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), pitField.getName(), 
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), pitField.getSourceId(), 
              ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), pitMember.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), pitMember.getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), pitMember.getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), pitGroup.getSourceId(),
              //ChangeLogLabels.MEMBERSHIP_DELETE.membershipType.name(), this.getType(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectName.name(), subjectName,
              ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), pitGroup.getName(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectIdentifier0.name(), pitMember.getSubjectIdentifier0()).save();          
        } else if (pitField.getType().equals("access")) {
          PITGroup pitGroup = mship.getOwnerPITGroup();

          new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), mship.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), AccessPrivilege.listToPriv(pitField.getName()).getName(), 
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getSourceId(), 
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), pitMember.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), pitMember.getSubjectId(),
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), pitMember.getSubjectSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.ACCESS.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_GROUP,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), pitGroup.getSourceId(),
              //ChangeLogLabels.PRIVILEGE_DELETE.membershipType.name(), this.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), pitGroup.getName()).save();
        } else if (pitField.getType().equals("naming")) {
          PITStem pitStem = mship.getOwnerPITStem();
          
          new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), mship.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), NamingPrivilege.listToPriv(pitField.getName()).getName(), 
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getSourceId(), 
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), pitMember.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), pitMember.getSubjectId(),
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), pitMember.getSubjectSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.NAMING.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_STEM,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), pitStem.getSourceId(),
              //ChangeLogLabels.PRIVILEGE_DELETE.membershipType.name(), this.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), pitStem.getName()).save();
        } else if (pitField.getType().equals("attributeDef")) {
          PITAttributeDef pitAttributeDef = mship.getOwnerPITAttributeDef();
          
          new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), mship.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), AttributeDefPrivilege.listToPriv(pitField.getName()).getName(), 
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getSourceId(), 
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), pitMember.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), pitMember.getSubjectId(),
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), pitMember.getSubjectSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.ATTRIBUTE_DEF.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_ATTRIBUTE_DEF,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), pitAttributeDef.getSourceId(),
              //ChangeLogLabels.PRIVILEGE_DELETE.membershipType.name(), this.getType(),
              ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), pitAttributeDef.getName()).save();
        } else {
          throw new RuntimeException("unexpected field type: " + pitField.getType());
        }
      }
      
      totalProcessed++;
    }
    
    if (mships.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time attribute assigns that are currently active but should be inactive.
   * @return the number of point in time attribute assigns to end
   */
  public long processMissingInactivePITAttributeAssigns() {
    showStatus("\n\nSearching for point in time attribute assigns that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeAssign> assigns = GrouperDAOFactory.getFactory().getPITAttributeAssign().findMissingInactivePITAttributeAssigns();
    showStatus("Found " + assigns.size() + " active point in time attribute assigns that should be inactive");

    for (PITAttributeAssign assign : assigns) {
      
      logDetail("Found active point in time attribute assign that should be inactive with id: " + assign.getId());
      
      if (saveUpdates) {
        
        String ownerId1 = null;
        String ownerId2 = null;
        
        if (AttributeAssignType.group.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITGroup().getSourceId();
        } else if (AttributeAssignType.stem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITStem().getSourceId();
        } else if (AttributeAssignType.member.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITMember().getSourceId();
        } else if (AttributeAssignType.attr_def.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITAttributeDef().getSourceId();
        } else if (AttributeAssignType.any_mem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITGroup().getSourceId();
          ownerId2 = assign.getOwnerPITMember().getSourceId();
        } else if (AttributeAssignType.imm_mem.name().equals(assign.getAttributeAssignTypeDb())) {
          ownerId1 = assign.getOwnerPITMembership().getSourceId();
        } else if (assign.getOwnerAttributeAssignId() != null) {
          ownerId1 = assign.getOwnerPITAttributeAssign().getSourceId();
        } else {
          throw new RuntimeException("Unexpected ownerType: " + assign.getAttributeAssignTypeDb());
        }
        
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.id.name(), assign.getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameId.name(), assign.getPITAttributeDefName().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeAssignActionId.name(), assign.getPITAttributeAssignAction().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.assignType.name(), assign.getAttributeAssignTypeDb(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId1.name(), ownerId1,
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.ownerId2.name(), ownerId2,
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.attributeDefNameName.name(), assign.getPITAttributeDefName().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.action.name(), assign.getPITAttributeAssignAction().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_DELETE.disallowed.name(), assign.getDisallowedDb()).save();        
      }
      
      totalProcessed++;
    }
    
    if (assigns.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time attribute assign values that are currently active but should be inactive.
   * @return the number of point in time attribute assign values to end
   */
  public long processMissingInactivePITAttributeAssignValues() {
    showStatus("\n\nSearching for point in time attribute assign values that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findMissingInactivePITAttributeAssignValues();
    showStatus("Found " + values.size() + " active point in time attribute assign values that should be inactive");

    for (PITAttributeAssignValue value : values) {
      
      logDetail("Found active point in time attribute assign value that should be inactive with id: " + value.getId());
      
      if (saveUpdates) {
        PITAttributeDef pitAttributeDef = value.getPITAttributeAssign().getPITAttributeDefName().getPITAttributeDef();
        AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(pitAttributeDef.getSourceId(), false);
        
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.id.name(), value.getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeAssignId.name(), value.getPITAttributeAssign().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameId.name(), value.getPITAttributeAssign().getPITAttributeDefName().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName.name(), value.getPITAttributeAssign().getPITAttributeDefName().getName(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.value.name(), value.getValueString(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.valueType.name(), attributeDef == null ? null : attributeDef.getValueType().name()).save();
      }
      
      totalProcessed++;
    }
    
    if (values.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time attribute defs that are currently active but should be inactive.
   * @return the number of point in time attribute defs to end
   */
  public long processMissingInactivePITAttributeDefs() {
    showStatus("\n\nSearching for point in time attribute defs that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeDef> attrs = GrouperDAOFactory.getFactory().getPITAttributeDef().findMissingInactivePITAttributeDefs();
    showStatus("Found " + attrs.size() + " active point in time attribute defs that should be inactive");

    for (PITAttributeDef attr : attrs) {
      
      logDetail("Found active point in time attribute def that should be inactive with id: " + attr.getId() + ", name: " + attr.getName());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_DELETE, 
            ChangeLogLabels.ATTRIBUTE_DEF_DELETE.id.name(), attr.getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_DELETE.name.name(), attr.getName(), 
            ChangeLogLabels.ATTRIBUTE_DEF_DELETE.stemId.name(), attr.getPITStem().getSourceId(),
            //ChangeLogLabels.ATTRIBUTE_DEF_DELETE.description.name(), attr.getDescription(),
            ChangeLogLabels.ATTRIBUTE_DEF_DELETE.attributeDefType.name(), attr.getAttributeDefTypeDb()).save();   
      }
      
      totalProcessed++;
    }
    
    if (attrs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time attribute def names that are currently active but should be inactive.
   * @return the number of point in time attribute def names to end
   */
  public long processMissingInactivePITAttributeDefNames() {
    showStatus("\n\nSearching for point in time attribute def names that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeDefName> attrs = GrouperDAOFactory.getFactory().getPITAttributeDefName().findMissingInactivePITAttributeDefNames();
    showStatus("Found " + attrs.size() + " active point in time attribute def names that should be inactive");

    for (PITAttributeDefName attr : attrs) {
      
      logDetail("Found active point in time attribute def name that should be inactive with id: " + attr.getId() + ", name: " + attr.getName());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_DELETE, 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.id.name(), attr.getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.name.name(), attr.getName(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.stemId.name(), attr.getPITStem().getSourceId(), 
            //ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.description.name(), this.getDescription(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_DELETE.attributeDefId.name(), attr.getPITAttributeDef().getSourceId()).save();       
      }
      
      totalProcessed++;
    }
    
    if (attrs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time attribute def name sets that are currently active but should be inactive.
   * @return the number of point in time attribute def name sets to end
   */
  public long processMissingInactivePITAttributeDefNameSets() {
    showStatus("\n\nSearching for point in time attribute def name sets that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeDefNameSet> attrSets = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findMissingInactivePITAttributeDefNameSets();
    showStatus("Found " + attrSets.size() + " active point in time attribute def name sets that should be inactive");

    for (PITAttributeDefNameSet attrSet : attrSets) {
      
      logDetail("Found active point in time attribute def name set that should be inactive with id: " + attrSet.getId());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_DEF_NAME_SET_DELETE, 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.id.name(), attrSet.getSourceId(), 
            //ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.type.name(), attrSet.getTypeDb(),
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.ifHasAttributeDefNameId.name(), attrSet.getIfHasPITAttributeDefName().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.thenHasAttributeDefNameId.name(), attrSet.getThenHasPITAttributeDefName().getSourceId(),
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.parentAttrDefNameSetId.name(), attrSet.getParentPITAttributeDefNameSet().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_DEF_NAME_SET_DELETE.depth.name(), "" + attrSet.getDepth()).save();
      }
      
      totalProcessed++;
    }
    
    if (attrSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time groups that are currently active but should be inactive.
   * @return the number of point in time groups to end
   */
  public long processMissingInactivePITGroups() {
    showStatus("\n\nSearching for point in time groups that should be inactive");
 
    long totalProcessed = 0;

    Set<PITGroup> groups = GrouperDAOFactory.getFactory().getPITGroup().findMissingInactivePITGroups();
    showStatus("Found " + groups.size() + " active point in time groups that should be inactive");

    for (PITGroup group : groups) {
      
      logDetail("Found active point in time group that should be inactive with id: " + group.getId() + ", name: " + group.getName());
      
      if (saveUpdates) {
        // we don't know if this is an entity or group so just update here directly???
        
        group.setEndTimeDb(System.currentTimeMillis() * 1000);
        group.setActiveDb("F");
        group.setContextId(null);
        
        group.saveOrUpdate();     
      }
      
      totalProcessed++;
    }
    
    if (groups.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time group sets that are currently active but should be inactive.
   * @return the number of point in time group sets to end
   */
  public long processMissingInactivePITGroupSets() {
    showStatus("\n\nSearching for point in time group sets that should be inactive");
 
    long totalProcessed = 0;

    Set<PITGroupSet> groupSets = GrouperDAOFactory.getFactory().getPITGroupSet().findMissingInactivePITGroupSets();
    showStatus("Found " + groupSets.size() + " active point in time group sets that should be inactive");

    for (PITGroupSet groupSet : groupSets) {
      
      logDetail("Found active point in time group set that should be inactive with id: " + groupSet.getId());
      
      if (saveUpdates) {
        
        PITGroup ownerPITGroup = groupSet.getOwnerPITGroup();
        PITStem ownerPITStem = groupSet.getOwnerPITStem();
        PITAttributeDef ownerPITAttributeDef = groupSet.getOwnerPITAttributeDef();
        
        PITGroup memberPITGroup = groupSet.getMemberPITGroup();
        PITStem memberPITStem = groupSet.getMemberPITStem();
        PITAttributeDef memberPITAttributeDef = groupSet.getMemberPITAttributeDef();
                
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_SET_DELETE, 
            ChangeLogLabels.GROUP_SET_DELETE.id.name(), groupSet.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.ownerGroupId.name(), ownerPITGroup == null ? null : ownerPITGroup.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.ownerStemId.name(), ownerPITStem == null ? null : ownerPITStem.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.ownerAttributeDefId.name(), ownerPITAttributeDef == null ? null : ownerPITAttributeDef.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.memberGroupId.name(), memberPITGroup == null ? null : memberPITGroup.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.memberStemId.name(), memberPITStem == null ? null : memberPITStem.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.memberAttributeDefId.name(), memberPITAttributeDef == null ? null : memberPITAttributeDef.getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.fieldId.name(), groupSet.getPITField().getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.memberFieldId.name(), groupSet.getMemberPITField().getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.parentGroupSetId.name(), groupSet.getParentPITGroupSet().getSourceId(), 
            ChangeLogLabels.GROUP_SET_DELETE.depth.name(), "" + groupSet.getDepth()).save();
      }
      
      totalProcessed++;
    }
    
    if (groupSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time role sets that are currently active but should be inactive.
   * @return the number of point in time role sets to end
   */
  public long processMissingInactivePITRoleSets() {
    showStatus("\n\nSearching for point in time role sets that should be inactive");
 
    long totalProcessed = 0;

    Set<PITRoleSet> roleSets = GrouperDAOFactory.getFactory().getPITRoleSet().findMissingInactivePITRoleSets();
    showStatus("Found " + roleSets.size() + " active point in time role sets that should be inactive");

    for (PITRoleSet roleSet : roleSets) {
      
      logDetail("Found active point in time role set that should be inactive with id: " + roleSet.getId());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ROLE_SET_DELETE, 
            ChangeLogLabels.ROLE_SET_DELETE.id.name(), roleSet.getSourceId(), 
            //ChangeLogLabels.ROLE_SET_DELETE.type.name(), this.getTypeDb(),
            ChangeLogLabels.ROLE_SET_DELETE.ifHasRoleId.name(), roleSet.getIfHasPITRole().getSourceId(), 
            ChangeLogLabels.ROLE_SET_DELETE.thenHasRoleId.name(), roleSet.getThenHasPITRole().getSourceId(),
            ChangeLogLabels.ROLE_SET_DELETE.parentRoleSetId.name(), roleSet.getParentPITRoleSet().getSourceId(), 
            ChangeLogLabels.ROLE_SET_DELETE.depth.name(), "" + roleSet.getDepth()).save();
      }
      
      totalProcessed++;
    }
    
    if (roleSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time fields that are currently active but should be inactive.
   * @return the number of point in time fields to end
   */
  public long processMissingInactivePITFields() {
    showStatus("\n\nSearching for point in time fields that should be inactive");
 
    long totalProcessed = 0;

    Set<PITField> fields = GrouperDAOFactory.getFactory().getPITField().findMissingInactivePITFields();
    showStatus("Found " + fields.size() + " active point in time fields that should be inactive");

    for (PITField field : fields) {
      
      logDetail("Found active point in time field that should be inactive with id: " + field.getId() + ", name: " + field.getName());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_FIELD_DELETE, 
            ChangeLogLabels.GROUP_FIELD_DELETE.id.name(), 
            field.getSourceId(), ChangeLogLabels.GROUP_FIELD_DELETE.name.name(), 
            field.getName(), null, 
            null,
            null, 
            null,
            ChangeLogLabels.GROUP_FIELD_DELETE.type.name(), field.getType()
        ).save();    
      }
      
      totalProcessed++;
    }
    
    if (fields.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time members that are currently active but should be inactive.
   * @return the number of point in time members to end
   */
  public long processMissingInactivePITMembers() {
    showStatus("\n\nSearching for point in time members that should be inactive");
 
    long totalProcessed = 0;

    Set<PITMember> members = GrouperDAOFactory.getFactory().getPITMember().findMissingInactivePITMembers();
    showStatus("Found " + members.size() + " active point in time members that should be inactive");

    for (PITMember member : members) {
      
      logDetail("Found active point in time member that should be inactive with id: " + member.getId() + ", subject id: " + member.getSubjectId());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBER_DELETE, 
            ChangeLogLabels.MEMBER_DELETE.id.name(), member.getSourceId(), 
            ChangeLogLabels.MEMBER_DELETE.subjectId.name(), member.getSubjectId(), 
            ChangeLogLabels.MEMBER_DELETE.subjectSourceId.name(), member.getSubjectSourceId(),
            ChangeLogLabels.MEMBER_DELETE.subjectTypeId.name(), member.getSubjectTypeId(),
            ChangeLogLabels.MEMBER_DELETE.subjectIdentifier0.name(), member.getSubjectIdentifier0()
            //ChangeLogLabels.MEMBER_DELETE.subjectIdentifier1.name(), this.getSubjectIdentifier1(),
            //ChangeLogLabels.MEMBER_DELETE.subjectIdentifier2.name(), this.getSubjectIdentifier2(),
            //ChangeLogLabels.MEMBER_DELETE.email0.name(), this.getEmail0()
            ).save();  
      }
      
      totalProcessed++;
    }
    
    if (members.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time configs that are currently active but should be inactive.
   * @return the number of point in time configs to end
   */
  public long processMissingInactivePITConfigs() {
    showStatus("\n\nSearching for point in time configs that should be inactive");
 
    long totalProcessed = 0;

    Set<PITGrouperConfigHibernate> pitConfigs = GrouperDAOFactory.getFactory().getPITConfig().findMissingInactivePITConfigs();
    showStatus("Found " + pitConfigs.size() + " active point in time configs that should be inactive");

    for (PITGrouperConfigHibernate pitConfig : pitConfigs) {
      
      logDetail("Found active point in time config that should be inactive with id: " + pitConfig.getId());
      
      if (saveUpdates) {
        pitConfig.setEndTimeDb(System.currentTimeMillis() * 1000);
        pitConfig.setActiveDb("F");
        pitConfig.setContextId(null);
        
        pitConfig.saveOrUpdate();     
      }
      
      totalProcessed++;
    }
    
    if (pitConfigs.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time stems that are currently active but should be inactive.
   * @return the number of point in time stems to end
   */
  public long processMissingInactivePITStems() {
    showStatus("\n\nSearching for point in time stems that should be inactive");
 
    long totalProcessed = 0;

    Set<PITStem> stems = GrouperDAOFactory.getFactory().getPITStem().findMissingInactivePITStems();
    showStatus("Found " + stems.size() + " active point in time stems that should be inactive");

    for (PITStem stem : stems) {
      
      logDetail("Found active point in time stem that should be inactive with id: " + stem.getId() + ", name: " + stem.getName());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.STEM_DELETE, 
            ChangeLogLabels.STEM_DELETE.id.name(), 
            stem.getSourceId(), ChangeLogLabels.STEM_DELETE.name.name(), 
            stem.getName(), ChangeLogLabels.STEM_DELETE.parentStemId.name(), stem.getParentPITStem().getSourceId()
            //ChangeLogLabels.STEM_DELETE.displayName.name(), this.getDisplayName(),
            //ChangeLogLabels.STEM_DELETE.description.name(), this.getDescription()
            ).save();  
      }
      
      totalProcessed++;
    }
    
    if (stems.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time actions that are currently active but should be inactive.
   * @return the number of point in time actions to end
   */
  public long processMissingInactivePITAttributeAssignActions() {
    showStatus("\n\nSearching for point in time actions that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeAssignAction> actions = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findMissingInactivePITAttributeAssignActions();
    showStatus("Found " + actions.size() + " active point in time actions that should be inactive");

    for (PITAttributeAssignAction action : actions) {
      
      logDetail("Found active point in time action that should be inactive with id: " + action.getId() + ", name: " + action.getName());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.id.name(), action.getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.name.name(), action.getName(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_DELETE.attributeDefId.name(), action.getPITAttributeDef().getSourceId()).save();     
      }
      
      totalProcessed++;
    }
    
    if (actions.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  /**
   * End point in time action sets that are currently active but should be inactive.
   * @return the number of point in time action sets to end
   */
  public long processMissingInactivePITAttributeAssignActionSets() {
    showStatus("\n\nSearching for point in time action sets that should be inactive");
 
    long totalProcessed = 0;

    Set<PITAttributeAssignActionSet> actionSets = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findMissingInactivePITAttributeAssignActionSets();
    showStatus("Found " + actionSets.size() + " active point in time action sets that should be inactive");

    for (PITAttributeAssignActionSet actionSet : actionSets) {
      
      logDetail("Found active point in time action set that should be inactive with id: " + actionSet.getId());
      
      if (saveUpdates) {
        new ChangeLogEntry(true, ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE, 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.id.name(), actionSet.getSourceId(), 
            //ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.type.name(), this.getTypeDb(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.ifHasAttrAssnActionId.name(), actionSet.getIfHasPITAttributeAssignAction().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.thenHasAttrAssnActionId.name(), actionSet.getThenHasPITAttributeAssignAction().getSourceId(),
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.parentAttrAssignActionSetId.name(), actionSet.getParentPITAttributeAssignActionSet().getSourceId(), 
            ChangeLogLabels.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE.depth.name(), "" + actionSet.getDepth()).save();
      }
      
      totalProcessed++;
    }
    
    if (actionSets.size() > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }

  private void showStatus(String message) {
    if (showResults) {
      println(message);
    }
  }
  
  private void logDetail(String detail) {
    if (logDetails) {
      LOG.info(detail);
    }
    
    if (createReport && report != null) {
      report.append(detail + "\n");
    }
  }
  
  /**
   * Find and delete active entries in point in time tables that are duplicates.  
   * This will keep the oldest entry based on the start time.
   * @return number of duplicates
   */
  public long processAllDuplicates() {
    
    return (long)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        long count = 0;
        clearReport();
        
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITField());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITMember());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITConfig());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITStem());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITGroup());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITRoleSet());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeDef());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeDefName());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeDefNameSet());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeAssignAction());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITGroupSet());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITMembership());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeAssign());
        count+= processDuplicates(GrouperDAOFactory.getFactory().getPITAttributeAssignValue());
        return count;
      }
    });
  }
  
  /**
   * @param dao
   * @return number of duplicates
   */
  public long processDuplicates(GrouperDAO dao) {

    String className = dao.getClass().getSimpleName();
    long errorCount = 0;
    
    showStatus("\n\n" + className + ": Searching for point in time duplicates");

    Set<String> sourceIds = (Set<String>)GrouperUtil.callMethod(dao.getClass(), dao, "findActiveDuplicates", null, null, false, false);
    showStatus("Found " + sourceIds.size() + " entries that have duplicates in point in time.");

    for (String sourceId : sourceIds) {
      Set<GrouperPIT> objectsSet = (Set<GrouperPIT>)GrouperUtil.callMethod(dao.getClass(), dao, "findBySourceId", new Class[] {String.class, boolean.class}, new Object[] {sourceId, true}, false, false);
      List<GrouperPIT> objectsList = new ArrayList<GrouperPIT>();
      for (GrouperPIT object : objectsSet) {
        if (object.isActive()) {
          objectsList.add(object);
        }
      }
      
      if (objectsList.size() < 2) {
        throw new RuntimeException("Found fewer than expected entries with sourceId=" + sourceId + ", DAO=" + className);
      }
      
      Collections.sort(objectsList, new Comparator<GrouperPIT>() {

        public int compare(GrouperPIT o1, GrouperPIT o2) {
          return ((Long)o1.getStartTimeDb()).compareTo(o2.getStartTimeDb());
        }
      });

      // remove the first object from the list -- should be the oldest or tied for the oldest
      objectsList.remove(0);
      
      // try deleting the rest .. hopefully we won't have foreign key issues....
      for (GrouperPIT object : objectsList) {
        String id  = (String)GrouperUtil.callMethod(object.getClass(), object, "getId", null, null, false, false);
        
        logDetail(className + ": Found duplicate PIT record with sourceId=" + sourceId + ", id=" + id);
        showStatus("Found duplicate PIT record with sourceId=" + sourceId + ", id=" + id);

        if (saveUpdates) {
          try {
            GrouperUtil.callMethod(dao.getClass(), dao, "delete", new Class[] {String.class}, new Object[] {id}, false, false);
          } catch (Exception e) {
            LOG.error(className + ": Failed to delete PIT record with sourceId=" + sourceId + ", id=" + id, e);
            errorCount++;
          }
        }
      }
    }
    
    if (sourceIds.size() > 0 && saveUpdates) {
      if (errorCount == 0) {
        showStatus("Done making updates");
      } else {
        showStatus("Done making updates but there were " + errorCount + " errors.  See logs for details.");
      }
    }
    
    return sourceIds.size();
  }
  
  /**
   * print a line
   * @param string
   */
  private void println(String string) {
    if (this.captureOutput) {
      this.output.append(string).append("\n");
    } else {
      System.out.println(string);
    }
  }

  private StringBuilder output = new StringBuilder();
  
  public String getOutput() {
    GrouperUtil.assertion(captureOutput, "Output is not being captured, call syncPitTables.captureOutput(true)");
    return this.output.toString();
  }
  
  private boolean captureOutput = false;
  
  public void captureOutput(boolean b) {
    this.captureOutput = true;
    
  }

  /**
   * output and report
   * @return
   */
  public String getFullOutput() {
    StringBuilder result = new StringBuilder();
    String theOutput = this.getOutput();
    if (!StringUtils.isBlank(theOutput)) {
      result.append(theOutput.trim());
    }
    if (this.report != null && this.report.length() > 0) {
      if (result.length() > 0) {
        result.append("\n");
      }
      result.append(this.report);
    }
    return result.toString();
  }
}
