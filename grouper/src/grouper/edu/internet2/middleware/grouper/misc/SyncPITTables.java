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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
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
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
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
  
  /** Whether or not to send flattened notifications */
  private boolean sendFlattenedNotifications = true;

  /** Whether or not to send permission notifications */
  private boolean sendPermissionNotifications = true;
  
  /** Whether or not to create a report for GrouperReport */
  private boolean createReport = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncPITTables.class);
  
  /** detailed output for grouper report */
  private StringBuilder report = new StringBuilder();
  
  /** whether or not to send flattened notifications for memberships */
  private boolean includeFlattenedMemberships = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedMemberships", true);
  
  /** whether or not to send flattened notifications for privileges */
  private boolean includeFlattenedPrivileges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeFlattenedPrivileges", true);
  
  /** whether there will be notifications for roles with permission changes */ 
  private boolean includeRolesWithPermissionChanges = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.includeRolesWithPermissionChanges", false);

  /** max query size for queries that have a max **/
  private static final int MAX_QUERY_SIZE = 100000;
  
  /** total count for current phase */
  private long statusThreadTotalCount = 0;
  
  /** processed count for current phase */
  private long statusThreadProcessedCount = 0;
  
  /** if we're done processing a phase */
  private boolean statusThreadDonePhase = false;
  
  /** start time of script */
  private long statusThreadStartTime = 0;
  
  /** status thread */
  Thread statusThread = null;
  
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
   * @return AddMissingGroupSets
   */
  public SyncPITTables logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }
  
  /**
   * Whether or not to create a report.  Defaults to false.
   * @param createReport
   * @return AddMissingGroupSets
   */
  public SyncPITTables createReport(boolean createReport) {
    this.createReport = createReport;
    return this;
  }
  
  /**
   * Whether or not to send flattened notifications for memberships and privileges.  
   * If true, notifications will be based on configuration.  If false, notifications will not be sent
   * regardless of configuration.  Defaults to true.
   * @param sendNotifications
   * @return SyncPITTables
   */
  public SyncPITTables sendFlattenedNotifications(boolean sendNotifications) {
    this.sendFlattenedNotifications = sendNotifications;
    return this;
  }
  
  /**
   * Whether or not to send notifications for permissions.  
   * If true, notifications will be based on configuration.  If false, notifications will not be sent
   * regardless of configuration.  Defaults to true.
   * @param sendNotifications
   * @return SyncPITTables
   */
  public SyncPITTables sendPermissionNotifications(boolean sendNotifications) {
    this.sendPermissionNotifications = sendNotifications;
    return this;
  }

  /**
   * Sync all point in time tables
   * @return the number of updates made
   */
  public long syncAllPITTables() {

    GrouperSession session = null;
    long count = 0;

    try {
      session = GrouperSession.startRootSession();
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
      count += processMissingActivePITGroupSetsSecondPass();
      count += processMissingActivePITAttributeAssigns();
      count += processMissingActivePITAttributeAssignValues();
      
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
    } finally {
      GrouperSession.stopQuietly(session);
    }
    
    return count;
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
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(mship.getFieldId(), true);
        PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(mship.getMemberUuid(), true);
        
        PITMembership pitMembership = new PITMembership();
        pitMembership.setId(GrouperUuid.getUuid());
        pitMembership.setSourceId(mship.getImmediateMembershipId());
        pitMembership.setMemberId(pitMember.getId());
        pitMembership.setFieldId(pitField.getId());
        pitMembership.setActiveDb("T");
        pitMembership.setStartTimeDb(System.currentTimeMillis() * 1000);

        if (mship.getOwnerGroupId() != null) {
          pitMembership.setOwnerGroupId(GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(mship.getOwnerGroupId(), true).getId());
        } else if (mship.getOwnerStemId() != null) {
          pitMembership.setOwnerStemId(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(mship.getOwnerStemId(), true).getId());
        } else if (mship.getOwnerAttrDefId() != null) {
          pitMembership.setOwnerAttrDefId(GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(mship.getOwnerAttrDefId(), true).getId());
        } else {
          throw new RuntimeException("Unexpected -- Membership with id " + mship.getUuid() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
        }
        
        if (!GrouperUtil.isEmpty(mship.getContextId())) {
          pitMembership.setContextId(mship.getContextId());
        }
        
        if (sendFlattenedNotifications) {
          pitMembership.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
          pitMembership.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
        }
        
        if (sendPermissionNotifications) {
          pitMembership.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }

        pitMembership.save();
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
        PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(assign.getAttributeDefNameId(), true);
        PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(assign.getAttributeAssignActionId(), true);
        
        PITAttributeAssign pitAttributeAssign = new PITAttributeAssign();
        pitAttributeAssign.setId(GrouperUuid.getUuid());
        pitAttributeAssign.setSourceId(assign.getId());
        pitAttributeAssign.setAttributeDefNameId(pitAttributeDefName.getId());
        pitAttributeAssign.setAttributeAssignActionId(pitAttributeAssignAction.getId());
        pitAttributeAssign.setAttributeAssignTypeDb(assign.getAttributeAssignTypeDb());
        pitAttributeAssign.setDisallowedDb(assign.getDisallowedDb());
        pitAttributeAssign.setActiveDb("T");
        pitAttributeAssign.setStartTimeDb(System.currentTimeMillis() * 1000);
        
        if (AttributeAssignType.group.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(assign.getOwnerGroupId(), true);
          pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
        } else if (AttributeAssignType.stem.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITStem pitOwner1 = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(assign.getOwnerStemId(), true);
          pitAttributeAssign.setOwnerStemId(pitOwner1.getId());
        } else if (AttributeAssignType.member.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITMember pitOwner1 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(assign.getOwnerMemberId(), true);
          pitAttributeAssign.setOwnerMemberId(pitOwner1.getId());
        } else if (AttributeAssignType.attr_def.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITAttributeDef pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(assign.getOwnerAttributeDefId(), true);
          pitAttributeAssign.setOwnerAttributeDefId(pitOwner1.getId());
        } else if (AttributeAssignType.any_mem.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(assign.getOwnerGroupId(), true);
          PITMember pitOwner2 = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(assign.getOwnerMemberId(), true);
          pitAttributeAssign.setOwnerGroupId(pitOwner1.getId());
          pitAttributeAssign.setOwnerMemberId(pitOwner2.getId());
        } else if (AttributeAssignType.imm_mem.name().equals(pitAttributeAssign.getAttributeAssignTypeDb())) {
          PITMembership pitOwner1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(assign.getOwnerMembershipId(), false);
          if (pitOwner1 == null) {
            // assignment must be disabled..
            logDetail("Skipping " + assign.getId() + " since active owner was not found in point in time.");
            continue;
          }
          pitAttributeAssign.setOwnerMembershipId(pitOwner1.getId());
        } else {
          // this must be an attribute assign of an attribute assign.  foreign keys will make sure we're right.
          PITAttributeAssign pitOwner1 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign.getOwnerAttributeAssignId(), false);
          if (pitOwner1 == null) {
            // assignment must be disabled..
            logDetail("Skipping " + assign.getId() + " since active owner was not found in point in time.");
            continue;
          }
          pitAttributeAssign.setOwnerAttributeAssignId(pitOwner1.getId());
        }
        
        if (!GrouperUtil.isEmpty(assign.getContextId())) {
          pitAttributeAssign.setContextId(assign.getContextId());
        }
                
        if (sendPermissionNotifications) {
          pitAttributeAssign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }

        pitAttributeAssign.save();
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
          // assignment must be disabled..
          logDetail("Skipping " + value.getId() + " since active assignment was not found in point in time.");
          continue;
        }
        
        PITAttributeAssignValue pitAttributeAssignValue = new PITAttributeAssignValue();
        pitAttributeAssignValue.setId(GrouperUuid.getUuid());
        pitAttributeAssignValue.setSourceId(value.getId());
        pitAttributeAssignValue.setAttributeAssignId(pitAttributeAssign.getId());
        pitAttributeAssignValue.setActiveDb("T");
        pitAttributeAssignValue.setStartTimeDb(System.currentTimeMillis() * 1000);
        
        pitAttributeAssignValue.setValueString(value.getValueString());
        pitAttributeAssignValue.setValueInteger(value.getValueInteger());
        pitAttributeAssignValue.setValueMemberId(value.getValueMemberId());
        pitAttributeAssignValue.setValueFloating(value.getValueFloating());
        
        if (!GrouperUtil.isEmpty(value.getContextId())) {
          pitAttributeAssignValue.setContextId(value.getContextId());
        }

        pitAttributeAssignValue.save();
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
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(attr.getStemId(), true);

        // note that we may just need to update the name and/or stemId
        PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attr.getId(), false);
        if (pitAttributeDef == null) {
          pitAttributeDef = new PITAttributeDef();
          pitAttributeDef.setId(GrouperUuid.getUuid());
          pitAttributeDef.setSourceId(attr.getUuid());
          pitAttributeDef.setAttributeDefTypeDb(attr.getAttributeDefTypeDb());
          pitAttributeDef.setActiveDb("T");
          pitAttributeDef.setStartTimeDb(System.currentTimeMillis() * 1000);
        }

        pitAttributeDef.setNameDb(attr.getName());
        pitAttributeDef.setStemId(pitStem.getId());
        
        if (!GrouperUtil.isEmpty(attr.getContextId())) {
          pitAttributeDef.setContextId(attr.getContextId());
        } else {
          pitAttributeDef.setContextId(null);
        }
        
        pitAttributeDef.saveOrUpdate();
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
        PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attr.getAttributeDefId(), true);
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(attr.getStemId(), true);
        
        // note that we may just need to update the name
        PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attr.getId(), false);
        if (pitAttributeDefName == null) {
          pitAttributeDefName = new PITAttributeDefName();
          pitAttributeDefName.setId(GrouperUuid.getUuid());
          pitAttributeDefName.setSourceId(attr.getId());
          pitAttributeDefName.setAttributeDefId(pitAttributeDef.getId());
          pitAttributeDefName.setStemId(pitStem.getId());
          pitAttributeDefName.setActiveDb("T");
          pitAttributeDefName.setStartTimeDb(System.currentTimeMillis() * 1000);
        }

        pitAttributeDefName.setNameDb(attr.getNameDb());

        if (!GrouperUtil.isEmpty(attr.getContextId())) {
          pitAttributeDefName.setContextId(attr.getContextId());
        } else {
          pitAttributeDefName.setContextId(null);
        }
        
        pitAttributeDefName.saveOrUpdate();
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
        PITAttributeDefName pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attrSet.getIfHasAttributeDefNameId(), true);
        PITAttributeDefName pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attrSet.getThenHasAttributeDefNameId(), true);
        PITAttributeDefNameSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(attrSet.getParentAttrDefNameSetId(), false);

        PITAttributeDefNameSet pitAttributeDefNameSet = new PITAttributeDefNameSet();
        pitAttributeDefNameSet.setId(GrouperUuid.getUuid());
        pitAttributeDefNameSet.setSourceId(attrSet.getId());
        pitAttributeDefNameSet.setDepth(attrSet.getDepth());
        pitAttributeDefNameSet.setIfHasAttributeDefNameId(pitIfHas.getId());
        pitAttributeDefNameSet.setThenHasAttributeDefNameId(pitThenHas.getId());
        pitAttributeDefNameSet.setParentAttrDefNameSetId(attrSet.getDepth() == 0 ? pitAttributeDefNameSet.getId() : pitParent.getId());
        pitAttributeDefNameSet.setActiveDb("T");
        pitAttributeDefNameSet.setStartTimeDb(System.currentTimeMillis() * 1000);

        if (!GrouperUtil.isEmpty(attrSet.getContextId())) {
          pitAttributeDefNameSet.setContextId(attrSet.getContextId());
        }
        
        if (sendPermissionNotifications) {
          pitAttributeDefNameSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        pitAttributeDefNameSet.saveOrUpdate();
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
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(group.getParentUuid(), true);

        // note that we may just need to update the name and/or stemId
        PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(group.getId(), false);
        if (pitGroup == null) {
          pitGroup = new PITGroup();
          pitGroup.setId(GrouperUuid.getUuid());
          pitGroup.setSourceId(group.getUuid());
          pitGroup.setActiveDb("T");
          pitGroup.setStartTimeDb(System.currentTimeMillis() * 1000);
        }
        
        pitGroup.setNameDb(group.getName());  
        pitGroup.setStemId(pitStem.getId());

        if (!GrouperUtil.isEmpty(group.getContextId())) {
          pitGroup.setContextId(group.getContextId());
        } else {
          pitGroup.setContextId(null);
        }
        
        pitGroup.saveOrUpdate();
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
   * @return the number of missing point in time group sets
   */
  public long processMissingActivePITGroupSets() {
    
    long totalProcessed = 0;

    int batchSize = getBatchSize();

    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("pit.sync.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("pit.sync.threadPoolSize", 20);
    
    while (true) {
      showStatus("\n\nSearching for missing active point in time group sets");

      List<GroupSet> groupSets = new ArrayList<GroupSet>(GrouperDAOFactory.getFactory().getPITGroupSet().findMissingActivePITGroupSets(new QueryOptions().paging(MAX_QUERY_SIZE, 1, false)));
      if (groupSets.size() == MAX_QUERY_SIZE) {
        showStatus("Found " + groupSets.size() + " missing active point in time group sets.  (Note there are probably more since the maximum results willing to be returned at one time is " + MAX_QUERY_SIZE + " as well.)");
      } else {
        showStatus("Found " + groupSets.size() + " missing active point in time group sets");
      }

      boolean moreToProcess = saveUpdates && groupSets.size() == MAX_QUERY_SIZE;
      statusThreadTotalCount = groupSets.size();
      
      try {
        reset();
      
        List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
        List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
        
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupSets, batchSize);
        for (int batchNumber = 0; batchNumber < numberOfBatches; batchNumber++) {
          final List<GroupSet> groupSetBatch = GrouperUtil.batchList(groupSets, batchSize, batchNumber);
  
          GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("processMissingActivePITGroupSetsBatch") {
            
            @Override
            public Void callLogic() {
              processMissingActivePITGroupSetsBatch(groupSetBatch);
              return null;
            }
          };
         
  
          if (!useThreads){
            grouperCallable.callLogic();
          } else {
            GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable);
            futures.add(future);          
            GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);
          }
          
          totalProcessed = totalProcessed + groupSetBatch.size();
          statusThreadProcessedCount = statusThreadProcessedCount + groupSetBatch.size();
        }
        
        GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
        GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
        
        if (!moreToProcess) {
          break;
        }
      } finally {
        stopStatusThread();
      }
    }
    
    if (totalProcessed > 0 && saveUpdates) {
      showStatus("Done making " + totalProcessed + " updates");
    }
    
    return totalProcessed;
  }
  
  private void processMissingActivePITGroupSetsBatch(List<GroupSet> groupSets) {
    Set<PITGroupSet> batch = new LinkedHashSet<PITGroupSet>();

    for (GroupSet groupSet : groupSets) {
      
      logDetail("Found missing point in time group set with id: " + groupSet.getId());
          
      if (saveUpdates) {
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getFieldId(), true);
        PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getMemberFieldId(), true);
        PITGroupSet pitParent = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getParentId(), false);

        PITGroupSet pitGroupSet = new PITGroupSet();
        pitGroupSet.setId(GrouperUuid.getUuid());
        pitGroupSet.setSourceId(groupSet.getId());
        pitGroupSet.setDepth(groupSet.getDepth());
        pitGroupSet.setParentId(groupSet.getDepth() == 0 ? pitGroupSet.getId() : pitParent.getId());
        pitGroupSet.setFieldId(pitField.getId());
        pitGroupSet.setMemberFieldId(pitMemberField.getId());
        pitGroupSet.setActiveDb("T");
        pitGroupSet.setStartTimeDb(System.currentTimeMillis() * 1000);

        if (groupSet.getOwnerGroupId() != null) {
          PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerGroupId(pitOwner.getId());
        } else if (groupSet.getOwnerStemId() != null) {
          PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerStemId(pitOwner.getId());
        } else if (groupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerAttrDefId(pitOwner.getId());
        } else {
          throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
        }
      
        if (groupSet.getMemberGroupId() != null) {
          PITGroup pitMember = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberGroupId(pitMember.getId());
        } else if (groupSet.getMemberStemId() != null) {
          PITStem pitMember = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberStemId(pitMember.getId());
        } else if (groupSet.getMemberAttrDefId() != null) {
          PITAttributeDef pitMember = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberAttrDefId(pitMember.getId());
        } else {
          throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an memberGroupId, memberStemId, or memberAttrDefId.");
        }
      
        if (!GrouperUtil.isEmpty(groupSet.getContextId())) {
          pitGroupSet.setContextId(groupSet.getContextId());
        }
      
        if (sendFlattenedNotifications) {
          pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
          pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
        }
      
        batch.add(pitGroupSet);
      }
    }
    
    if (batch.size() > 0) {
      GrouperDAOFactory.getFactory().getPITGroupSet().saveBatch(batch);
    }
  }
  
  /**
   * Add missing point in time group sets. (Second pass looking for issues with effective groupSets.)
   * 
   * @return the number of missing point in time group sets
   */
  public long processMissingActivePITGroupSetsSecondPass() {
    showStatus("\n\nSearching for missing active point in time group sets (second pass)");
    
    long totalProcessed = 0;

    List<GroupSet> groupSets = new LinkedList<GroupSet>(GrouperDAOFactory.getFactory().getPITGroupSet().findMissingActivePITGroupSetsSecondPass());
    showStatus("Found " + groupSets.size() + " missing active point in time group sets");

    Collections.sort(groupSets, new Comparator<GroupSet>() {

      public int compare(GroupSet o1, GroupSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    for (GroupSet groupSet : groupSets) {
      
      logDetail("Found missing point in time group set with id: " + groupSet.getId());
            
      if (saveUpdates) {
        
        // it's possible this was already taken care of... check
        PITGroupSet check = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getId(), false);
        if (check != null) {
          continue;
        }
        
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getFieldId(), true);
        PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getMemberFieldId(), true);
        PITGroupSet pitParent = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getParentId(), true);

        PITGroupSet pitGroupSet = new PITGroupSet();
        pitGroupSet.setId(GrouperUuid.getUuid());
        pitGroupSet.setSourceId(groupSet.getId());
        pitGroupSet.setDepth(groupSet.getDepth());
        pitGroupSet.setParentId(pitParent.getId());
        pitGroupSet.setFieldId(pitField.getId());
        pitGroupSet.setMemberFieldId(pitMemberField.getId());
        pitGroupSet.setActiveDb("T");
        pitGroupSet.setStartTimeDb(System.currentTimeMillis() * 1000);

        if (groupSet.getOwnerGroupId() != null) {
          PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerGroupId(pitOwner.getId());
        } else if (groupSet.getOwnerStemId() != null) {
          PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerStemId(pitOwner.getId());
        } else if (groupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getOwnerId(), true);
          pitGroupSet.setOwnerAttrDefId(pitOwner.getId());
        } else {
          throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
        }
        
        if (groupSet.getMemberGroupId() != null) {
          PITGroup pitMember = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberGroupId(pitMember.getId());
        } else if (groupSet.getMemberStemId() != null) {
          PITStem pitMember = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberStemId(pitMember.getId());
        } else if (groupSet.getMemberAttrDefId() != null) {
          PITAttributeDef pitMember = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getMemberId(), true);
          pitGroupSet.setMemberAttrDefId(pitMember.getId());
        } else {
          throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an memberGroupId, memberStemId, or memberAttrDefId.");
        }
        
        if (!GrouperUtil.isEmpty(groupSet.getContextId())) {
          pitGroupSet.setContextId(groupSet.getContextId());
        }
        
        if (sendFlattenedNotifications) {
          pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
          pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
        }
        
        pitGroupSet.saveOrUpdate();
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
        PITGroup pitIfHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(roleSet.getIfHasRoleId(), true);
        PITGroup pitThenHas = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(roleSet.getThenHasRoleId(), true);
        PITRoleSet pitParent = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(roleSet.getParentRoleSetId(), false);

        PITRoleSet pitRoleSet = new PITRoleSet();
        pitRoleSet.setId(GrouperUuid.getUuid());
        pitRoleSet.setSourceId(roleSet.getId());
        pitRoleSet.setDepth(roleSet.getDepth());
        pitRoleSet.setIfHasRoleId(pitIfHas.getId());
        pitRoleSet.setThenHasRoleId(pitThenHas.getId());
        pitRoleSet.setParentRoleSetId(roleSet.getDepth() == 0 ? pitRoleSet.getId() : pitParent.getId());
        pitRoleSet.setActiveDb("T");
        pitRoleSet.setStartTimeDb(System.currentTimeMillis() * 1000);

        if (!GrouperUtil.isEmpty(roleSet.getContextId())) {
          pitRoleSet.setContextId(roleSet.getContextId());
        }
        
        if (sendPermissionNotifications) {
          pitRoleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        pitRoleSet.saveOrUpdate();
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
        if (pitField == null) {
          pitField = new PITField();
          pitField.setId(GrouperUuid.getUuid());
          pitField.setSourceId(field.getUuid());
          pitField.setActiveDb("T");
          pitField.setStartTimeDb(System.currentTimeMillis() * 1000);
        }
        
        pitField.setNameDb(field.getName());
        pitField.setTypeDb(field.getTypeString());
        
        if (!GrouperUtil.isEmpty(field.getContextId())) {
          pitField.setContextId(field.getContextId());
        } else {
          pitField.setContextId(null);
        }
        
        pitField.saveOrUpdate();
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
        if (pitMember == null) {
          pitMember = new PITMember();
          pitMember.setId(GrouperUuid.getUuid());
          pitMember.setSourceId(member.getUuid());
          pitMember.setActiveDb("T");
          pitMember.setStartTimeDb(System.currentTimeMillis() * 1000);
        }
        
        pitMember.setSubjectId(member.getSubjectIdDb());
        pitMember.setSubjectSourceId(member.getSubjectSourceIdDb());
        pitMember.setSubjectTypeId(member.getSubjectTypeId());
        
        if (!GrouperUtil.isEmpty(member.getContextId())) {
          pitMember.setContextId(member.getContextId());
        } else {
          pitMember.setContextId(null);
        }
        
        pitMember.saveOrUpdate();
      }
      
      totalProcessed++;
    }
    
    if (members.size() > 0 && saveUpdates) {
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
        if (pitStem == null) {
          pitStem = new PITStem();
          pitStem.setId(GrouperUuid.getUuid());
          pitStem.setSourceId(stem.getUuid());
          pitStem.setActiveDb("T");
          pitStem.setStartTimeDb(System.currentTimeMillis() * 1000);
        }

        pitStem.setNameDb(stem.getNameDb());
        
        if (stem.getParentUuid() != null) {
          pitStem.setParentStemId(GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(stem.getParentUuid(), true).getId());
        }
        
        if (!GrouperUtil.isEmpty(stem.getContextId())) {
          pitStem.setContextId(stem.getContextId());
        } else {
          pitStem.setContextId(null);
        }
        
        pitStem.saveOrUpdate();
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
        PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(action.getAttributeDefId(), true);

        // note that we may just need to update the name
        PITAttributeAssignAction pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action.getId(), false);
        if (pitAttributeAssignAction == null) {
          pitAttributeAssignAction = new PITAttributeAssignAction();
          pitAttributeAssignAction.setId(GrouperUuid.getUuid());
          pitAttributeAssignAction.setSourceId(action.getId());
          pitAttributeAssignAction.setAttributeDefId(pitAttributeDef.getId());
          pitAttributeAssignAction.setActiveDb("T");
          pitAttributeAssignAction.setStartTimeDb(System.currentTimeMillis() * 1000); 
        }

        pitAttributeAssignAction.setNameDb(action.getNameDb());

        if (!GrouperUtil.isEmpty(action.getContextId())) {
          pitAttributeAssignAction.setContextId(action.getContextId());
        } else {
          pitAttributeAssignAction.setContextId(null);
        }
        
        pitAttributeAssignAction.saveOrUpdate();
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
        PITAttributeAssignAction pitIfHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(actionSet.getIfHasAttrAssignActionId(), true);
        PITAttributeAssignAction pitThenHas = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(actionSet.getThenHasAttrAssignActionId(), true);
        PITAttributeAssignActionSet pitParent = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(actionSet.getParentAttrAssignActionSetId(), false);
        
        PITAttributeAssignActionSet pitAttributeAssignActionSet = new PITAttributeAssignActionSet();
        pitAttributeAssignActionSet.setId(GrouperUuid.getUuid());
        pitAttributeAssignActionSet.setSourceId(actionSet.getId());
        pitAttributeAssignActionSet.setDepth(actionSet.getDepth());
        pitAttributeAssignActionSet.setIfHasAttrAssignActionId(pitIfHas.getId());
        pitAttributeAssignActionSet.setThenHasAttrAssignActionId(pitThenHas.getId());
        pitAttributeAssignActionSet.setParentAttrAssignActionSetId(actionSet.getDepth() == 0 ? pitAttributeAssignActionSet.getId() : pitParent.getId());
        pitAttributeAssignActionSet.setActiveDb("T");
        pitAttributeAssignActionSet.setStartTimeDb(System.currentTimeMillis() * 1000);
        
        if (!GrouperUtil.isEmpty(actionSet.getContextId())) {
          pitAttributeAssignActionSet.setContextId(actionSet.getContextId());
        }
                
        if (sendPermissionNotifications) {
          pitAttributeAssignActionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }

        pitAttributeAssignActionSet.saveOrUpdate();
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
        mship.setEndTimeDb(System.currentTimeMillis() * 1000);
        mship.setActiveDb("F");
        mship.setContextId(null);

        if (sendFlattenedNotifications) {
          mship.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
          mship.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
        }
        
        if (sendPermissionNotifications) {
          mship.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        mship.update();
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
        assign.setEndTimeDb(System.currentTimeMillis() * 1000);
        assign.setActiveDb("F");
        assign.setContextId(null);

        if (sendPermissionNotifications) {
          assign.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        assign.update();
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
        value.setEndTimeDb(System.currentTimeMillis() * 1000);
        value.setActiveDb("F");
        value.setContextId(null);

        value.update();
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
        attr.setEndTimeDb(System.currentTimeMillis() * 1000);
        attr.setActiveDb("F");
        attr.setContextId(null);
        
        attr.saveOrUpdate();        
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
        attr.setEndTimeDb(System.currentTimeMillis() * 1000);
        attr.setActiveDb("F");
        attr.setContextId(null);
        
        attr.saveOrUpdate();        
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
        attrSet.setEndTimeDb(System.currentTimeMillis() * 1000);
        attrSet.setActiveDb("F");
        attrSet.setContextId(null);
        
        if (sendPermissionNotifications) {
          attrSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        attrSet.saveOrUpdate();        
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
        groupSet.setEndTimeDb(System.currentTimeMillis() * 1000);
        groupSet.setActiveDb("F");
        groupSet.setContextId(null);
        
        
        if (sendFlattenedNotifications) {
          groupSet.setFlatMembershipNotificationsOnSaveOrUpdate(includeFlattenedMemberships);
          groupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(includeFlattenedPrivileges);
        }
        
        groupSet.saveOrUpdate();     
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
        roleSet.setEndTimeDb(System.currentTimeMillis() * 1000);
        roleSet.setActiveDb("F");
        roleSet.setContextId(null);
        
        if (sendPermissionNotifications) {
          roleSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        roleSet.saveOrUpdate();     
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
        field.setEndTimeDb(System.currentTimeMillis() * 1000);
        field.setActiveDb("F");
        field.setContextId(null);
        
        field.saveOrUpdate();     
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
        member.setEndTimeDb(System.currentTimeMillis() * 1000);
        member.setActiveDb("F");
        member.setContextId(null);
        
        member.saveOrUpdate();     
      }
      
      totalProcessed++;
    }
    
    if (members.size() > 0 && saveUpdates) {
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
        stem.setEndTimeDb(System.currentTimeMillis() * 1000);
        stem.setActiveDb("F");
        stem.setContextId(null);
        
        stem.saveOrUpdate();     
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
        action.setEndTimeDb(System.currentTimeMillis() * 1000);
        action.setActiveDb("F");
        action.setContextId(null);
        
        action.saveOrUpdate();     
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
        actionSet.setEndTimeDb(System.currentTimeMillis() * 1000);
        actionSet.setActiveDb("F");
        actionSet.setContextId(null);

        if (sendPermissionNotifications) {
          actionSet.setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(includeRolesWithPermissionChanges);
        }
        
        actionSet.saveOrUpdate();
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
      System.out.println(message);
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
    
    GrouperSession session = null;
    long count = 0;

    try {
      session = GrouperSession.startRootSession();
      clearReport();
      
      count+= processDuplicates(GrouperDAOFactory.getFactory().getPITField());
      count+= processDuplicates(GrouperDAOFactory.getFactory().getPITMember());
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
    } finally {
      GrouperSession.stopQuietly(session);
    }
    
    return count;
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

  private int getBatchSize() {
    int size = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
    if (size <= 0) {
      size = 1;
    }
    
    return size;
  }
  
  private void reset() {
    statusThreadProcessedCount = 0;
    statusThreadDonePhase = false;
    statusThreadStartTime = System.currentTimeMillis();
    
    // status thread
    statusThread = new Thread(new Runnable() {
      
      public void run() {
        SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        while (true) {

          // sleep 30 seconds between status messages
          for (int i = 0; i < 30; i++) {
            
            if (statusThreadDonePhase) {
              return;
            }
            
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ie) {
              // ignore this
            }
          }
          if (statusThreadDonePhase) {
            return;
          }
          
          if (showResults) {
            
            // print results
            long currentTotalCount = statusThreadTotalCount;              
            long currentProcessedCount = statusThreadProcessedCount;
            
            if (currentTotalCount != 0) {
              long now = System.currentTimeMillis();
              long endTime = 0;
              double percent = 0;
              
              if (currentProcessedCount > 0) {
                percent = ((double)currentProcessedCount * 100D) / currentTotalCount;
                
                if (percent > 1) {
                  endTime = statusThreadStartTime + (long)((now - statusThreadStartTime) * (100D / percent));
                }
              }
              
              System.out.print(format.format(new Date(now)) + ": Processed " + currentProcessedCount + " of " + currentTotalCount + " (" + Math.round(percent) + "%) of current phase.  ");
              
              if (endTime != 0) {
                System.out.print("Estimated completion time: " + estFormat.format(new Date(endTime)) + ".");
              }
              
              System.out.print("\n");
            }
          }
        }          
      }
    });
    
    statusThread.start();
  }
  
  private void stopStatusThread() {
    statusThreadDonePhase = true;
    if (statusThread != null) {
      try {
        statusThread.join(2000);
      } catch (InterruptedException ie) {
        // ignore this
      }
      
      statusThread = null;
    }
  }
}
