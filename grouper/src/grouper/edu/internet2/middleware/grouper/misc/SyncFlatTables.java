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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.flat.FlatStem;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class SyncFlatTables {
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
  /** Whether or not to log details */
  private boolean logDetails = true;
  
  /** Whether or not to send notifications */
  private boolean sendNotifications = true;
  
  /** Whether or not to create a report for GrouperReport */
  private boolean createReport = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncFlatTables.class);

  /** total count for current phase */
  private long totalCount = 0;
  
  /** processed count for current phase */
  private long processedCount = 0;
  
  /** if we're done processing a phase */
  private boolean donePhase = false;
  
  /** start time of script */
  private long startTime = 0;
  
  /** status thread */
  Thread statusThread = null;
  
  /** detailed output for grouper report */
  private StringBuilder report = null;
  
  /**
   * Whether or not to print out results of what's being done.  Defaults to true.
   * @param showResults
   * @return SyncFlatTables
   */
  public SyncFlatTables showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates.  Defaults to true.
   * @param saveUpdates
   * @return SyncFlatTables
   */
  public SyncFlatTables saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }
  
  /**
   * Whether or not to log details.  Defaults to true.
   * @param logDetails
   * @return AddMissingGroupSets
   */
  public SyncFlatTables logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }
  
  /**
   * Whether or not to create a report.  Defaults to false.
   * @param createReport
   * @return AddMissingGroupSets
   */
  public SyncFlatTables createReport(boolean createReport) {
    this.createReport = createReport;
    return this;
  }
  
  /**
   * Whether or not to send notifications.  Defaults to true.
   * @param sendNotifications
   * @return SyncFlatTables
   */
  public SyncFlatTables sendNotifications(boolean sendNotifications) {
    this.sendNotifications = sendNotifications;
    return this;
  }

  /**
   * Sync all flat tables
   * @return the number of updates made
   */
  public int syncAllFlatTables() {
        
    clearReport();
    
    int count = 0;
    
    count += addMissingFlatGroups();
    count += addMissingFlatStems();
    count += addMissingFlatAttributeDefs();
    count += addMissingFlatMemberships();
    count += removeBadFlatMemberships();
    count += removeBadFlatGroups();
    count += removeBadFlatStems();
    count += removeBadFlatAttributeDefs();
    
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
   * add missing flat attr defs
   * @return the number of inserted flat attr defs
   */
  public int addMissingFlatAttributeDefs() {
    showStatus("\n\nSearching for missing flat attribute defs");
    Set<AttributeDef> attrDefs = GrouperDAOFactory.getFactory().getFlatAttributeDef().findMissingFlatAttributeDefs();
    totalCount = attrDefs.size();
    showStatus("Found " + totalCount + " missing flat attribute defs");
    
    Set<FlatAttributeDef> batch = new LinkedHashSet<FlatAttributeDef>();
    int batchSize = getBatchSize();
    
    try {
      reset();
      Iterator<AttributeDef> iter = attrDefs.iterator();
      
      while (iter.hasNext()) {
        AttributeDef attrDef = iter.next();
        FlatAttributeDef flatAttrDef = new FlatAttributeDef();
        flatAttrDef.setId(attrDef.getUuid());
        batch.add(flatAttrDef);
        logDetail("Found missing flat attribute def with name: " + attrDef.getName());
        
        if (batch.size() % batchSize == 0 || !iter.hasNext()) {
          if (saveUpdates) {
            GrouperDAOFactory.getFactory().getFlatAttributeDef().saveBatch(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (attrDefs.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
  
      return attrDefs.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * add missing flat stems
   * @return the number of inserted flat stems
   */
  public int addMissingFlatStems() {
    showStatus("\n\nSearching for missing flat stems");
    Set<Stem> stems = GrouperDAOFactory.getFactory().getFlatStem().findMissingFlatStems();
    totalCount = stems.size();
    showStatus("Found " + totalCount + " missing flat stems");
    
    Set<FlatStem> batch = new LinkedHashSet<FlatStem>();
    int batchSize = getBatchSize();
    
    try {
      reset();
      Iterator<Stem> iter = stems.iterator();
      
      while (iter.hasNext()) {
        Stem stem = iter.next();
        FlatStem flatStem = new FlatStem();
        flatStem.setId(stem.getUuid());
        batch.add(flatStem);
        logDetail("Found missing flat stem with name: " + stem.getName());
        
        if (batch.size() % batchSize == 0 || !iter.hasNext()) {
          if (saveUpdates) {
            GrouperDAOFactory.getFactory().getFlatStem().saveBatch(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (stems.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
  
      return stems.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * Add missing flat memberships either by adding them directly to the table
   * or by adding a changelog event depending on whether sendNotifications(boolean) is set.
   * @return the number of missing flat memberships
   */
  public int addMissingFlatMemberships() {
    showStatus("\n\nSearching for missing flat memberships");
    Set<Membership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findMissingFlatMemberships();
    totalCount = mships.size();
    showStatus("Found " + totalCount + " missing flat memberships");
    
    Set<FlatMembership> flatMembershipBatch = new LinkedHashSet<FlatMembership>();
    Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
    int batchSize = getBatchSize();
    
    try {
      reset();
      Iterator<Membership> iter = mships.iterator();
      
      while (iter.hasNext()) {
        Membership mship = iter.next();
        
        logDetail("Found missing flat membership with ownerId: " + mship.getOwnerId() + 
            ", memberId: " + mship.getMemberUuid() + ", fieldId: " + mship.getFieldId());
        
        Field field = FieldFinder.findById(mship.getFieldId(), true);
        
        // if we're not sending notifications, just add to the flat memberships table
        if (saveUpdates && !sendNotifications) {
          FlatMembership flatMship = new FlatMembership();
          flatMship.setId(GrouperUuid.getUuid());
          flatMship.setFieldId(mship.getFieldId());
          flatMship.setMemberId(mship.getMemberUuid());
          
          if (field.isAttributeDefListField()) {
            flatMship.setOwnerAttrDefId(mship.getOwnerId());
          } else if (field.isGroupListField()) {
            flatMship.setOwnerGroupId(mship.getOwnerId());
          } else if (field.isStemListField()) {
            flatMship.setOwnerStemId(mship.getOwnerId());
          } else {
            throw new RuntimeException("Cannot determine if field is for a group, stem, or attr def: " + field.getUuid());
          }
    
          flatMembershipBatch.add(flatMship);
          
          if (flatMembershipBatch.size() % batchSize == 0 || !iter.hasNext()) {
            GrouperDAOFactory.getFactory().getFlatMembership().saveBatch(flatMembershipBatch);            
            flatMembershipBatch.clear();
          }
        }
        
        // if we're sending notifications, add to the temp changelog instead
        if (saveUpdates && sendNotifications) {
          
          Member member = mship.getMember();
          
          if (mship.getOwnerAttrDefId() != null) {
            AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(
                mship.getOwnerAttrDefId(), false);
            if (attributeDef != null) {
              ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_ADD, 
                  ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), AttributeDefPrivilege.listToPriv(field.getName()).getName(), 
                  ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), field.getUuid(), 
                  ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), member.getUuid(),
                  ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), member.getSubjectId(),
                  ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), member.getSubjectSourceId(),
                  ChangeLogLabels.PRIVILEGE_ADD.privilegeType.name(), FieldType.ATTRIBUTE_DEF.getType(),
                  ChangeLogLabels.PRIVILEGE_ADD.ownerType.name(), Membership.OWNER_TYPE_ATTRIBUTE_DEF,
                  ChangeLogLabels.PRIVILEGE_ADD.ownerId.name(), mship.getOwnerAttrDefId(),
                  ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), attributeDef.getName());
              changeLogEntryBatch.add(changeLogEntry);
            }  
          } else if (mship.getOwnerGroupId() != null) {
            Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(
                mship.getOwnerGroupId(), false, new QueryOptions().secondLevelCache(false));
            if (group != null) {
              if (field.getTypeString().equals(FieldType.LIST.getType())) {
                ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBERSHIP_ADD, 
                    ChangeLogLabels.MEMBERSHIP_ADD.fieldName.name(), field.getName(), 
                    ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name(), field.getUuid(), 
                    ChangeLogLabels.MEMBERSHIP_ADD.memberId.name(), member.getUuid(),
                    ChangeLogLabels.MEMBERSHIP_ADD.subjectId.name(), member.getSubjectId(),
                    ChangeLogLabels.MEMBERSHIP_ADD.sourceId.name(), member.getSubjectSourceId(),
                    ChangeLogLabels.MEMBERSHIP_ADD.groupId.name(), mship.getOwnerGroupId(),
                    ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), group.getName());
                changeLogEntryBatch.add(changeLogEntry);
              } else if (field.getTypeString().equals(FieldType.ACCESS.getType())) {
                ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_ADD, 
                    ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), AccessPrivilege.listToPriv(field.getName()).getName(), 
                    ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), field.getUuid(), 
                    ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), member.getUuid(),
                    ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), member.getSubjectId(),
                    ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), member.getSubjectSourceId(),
                    ChangeLogLabels.PRIVILEGE_ADD.privilegeType.name(), FieldType.ACCESS.getType(),
                    ChangeLogLabels.PRIVILEGE_ADD.ownerType.name(), Membership.OWNER_TYPE_GROUP,
                    ChangeLogLabels.PRIVILEGE_ADD.ownerId.name(), mship.getOwnerGroupId(),
                    ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), group.getName());
                changeLogEntryBatch.add(changeLogEntry);
              } else {
                throw new RuntimeException("Field type for group membership is not list or access.  Field id is: " + field.getUuid());
              }
            }
          } else if (mship.getOwnerStemId() != null) {
            Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(
                mship.getOwnerStemId(), false, new QueryOptions().secondLevelCache(false));
            if (stem != null) {
              ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_ADD, 
                  ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), NamingPrivilege.listToPriv(field.getName()).getName(), 
                  ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), field.getUuid(), 
                  ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), member.getUuid(),
                  ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), member.getSubjectId(),
                  ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), member.getSubjectSourceId(),
                  ChangeLogLabels.PRIVILEGE_ADD.privilegeType.name(), FieldType.NAMING.getType(),
                  ChangeLogLabels.PRIVILEGE_ADD.ownerType.name(), Membership.OWNER_TYPE_STEM,
                  ChangeLogLabels.PRIVILEGE_ADD.ownerId.name(), mship.getOwnerStemId(),
                  ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), stem.getName());
              changeLogEntryBatch.add(changeLogEntry);
            }
          } else {
            throw new RuntimeException("Cannot determine if membership is for a group, stem, or attr def: " + mship.getUuid());
          }
          
          if (changeLogEntryBatch.size() % batchSize == 0 || !iter.hasNext()) {
            GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, true);            
            changeLogEntryBatch.clear();
          }
        }
        
        processedCount++;
      }
      
      // make sure all changes get made
      if (changeLogEntryBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, true);
        changeLogEntryBatch.clear();
      }
      
      // make sure all changes get made
      if (flatMembershipBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getFlatMembership().saveBatch(flatMembershipBatch);
        flatMembershipBatch.clear();
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
  
      return mships.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * add missing flat groups
   * @return the number of inserted flat groups
   */
  public int addMissingFlatGroups() {
    showStatus("\n\nSearching for missing flat groups");
    Set<Group> groups = GrouperDAOFactory.getFactory().getFlatGroup().findMissingFlatGroups();
    totalCount = groups.size();
    showStatus("Found " + totalCount + " missing flat groups");
    
    Set<FlatGroup> batch = new LinkedHashSet<FlatGroup>();
    int batchSize = getBatchSize();
    
    try {
      reset();
      Iterator<Group> iter = groups.iterator();
      
      while (iter.hasNext()) {
        Group group = iter.next();
        FlatGroup flatGroup = new FlatGroup();
        flatGroup.setId(group.getUuid());
        batch.add(flatGroup);
        logDetail("Found missing flat group with name: " + group.getName());
  
        if (batch.size() % batchSize == 0 || !iter.hasNext()) {
          if (saveUpdates) {
            GrouperDAOFactory.getFactory().getFlatGroup().saveBatch(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (groups.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
  
      return groups.size();
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * remove bad flat attr defs
   * @return the number of removed flat attr defs
   */
  public int removeBadFlatAttributeDefs() {
    showStatus("\n\nSearching for bad flat attribute defs");
    Set<FlatAttributeDef> attrDefs = GrouperDAOFactory.getFactory().getFlatAttributeDef().findBadFlatAttributeDefs();
    totalCount = attrDefs.size();
    showStatus("Found " + totalCount + " bad flat attribute defs");
    
    try {
      reset();
      Iterator<FlatAttributeDef> iter = attrDefs.iterator();
      while (iter.hasNext()) {
        FlatAttributeDef attrDef = iter.next();
        logDetail("Found bad flat attribute def with id: " + attrDef.getId());
        
        if (saveUpdates) {
          attrDef.delete();
        }
        
        processedCount++;
      }
      
      if (attrDefs.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
      
      return attrDefs.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * remove bad flat stems
   * @return the number of removed flat stems
   */
  public int removeBadFlatStems() {
    showStatus("\n\nSearching for bad flat stems");
    Set<FlatStem> stems = GrouperDAOFactory.getFactory().getFlatStem().findBadFlatStems();
    totalCount = stems.size();
    showStatus("Found " + totalCount + " bad flat stems");
    
    try {
      reset();
      Iterator<FlatStem> iter = stems.iterator();
      while (iter.hasNext()) {
        FlatStem stem = iter.next();
        logDetail("Found bad flat stem with id: " + stem.getId());
        
        if (saveUpdates) {
          stem.delete();
        }
        
        processedCount++;
      }
      
      if (stems.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
      
      return stems.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * remove bad flat groups
   * @return the number of removed flat groups
   */
  public int removeBadFlatGroups() {
    showStatus("\n\nSearching for bad flat groups");
    Set<FlatGroup> groups = GrouperDAOFactory.getFactory().getFlatGroup().findBadFlatGroups();
    totalCount = groups.size();
    showStatus("Found " + totalCount + " bad flat groups");
    
    try {
      reset();
      Iterator<FlatGroup> iter = groups.iterator();
      while (iter.hasNext()) {
        FlatGroup group = iter.next();
        logDetail("Found bad flat group with id: " + group.getId());
        
        if (saveUpdates) {
          group.delete();
        }
        
        processedCount++;
      }
      
      if (groups.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
      
      return groups.size();
    } finally {
      stopStatusThread();
    }
  }

  /**
   * Remove bad flat memberships either by removing them directly in the table
   * or by adding a changelog event depending on whether sendNotifications(boolean) is set.
   * @return the number of bad flat memberships
   */
  public int removeBadFlatMemberships() {
    showStatus("\n\nSearching for bad flat memberships");
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findBadFlatMemberships();
    totalCount = mships.size();
    showStatus("Found " + totalCount + " bad flat memberships");
    
    Set<ChangeLogEntry> batch = new LinkedHashSet<ChangeLogEntry>();
    int batchSize = getBatchSize();
    
    try {
      reset();
      Iterator<FlatMembership> iter = mships.iterator();
      while (iter.hasNext()) {
        FlatMembership mship = iter.next();
        
        logDetail("Found bad flat membership with id: " + mship.getId() + ", ownerId: " + mship.getOwnerId() + 
            ", memberId: " + mship.getMemberId() + ", fieldId: " + mship.getFieldId());
        
        // if we're not sending notifications, then just delete the flat membership.
        if (saveUpdates && !sendNotifications) {
          mship.delete();
        }
        
        // if we're sending notifications, then just add to the temp changelog
        if (saveUpdates && sendNotifications) {
          Field field = FieldFinder.findById(mship.getFieldId(), true);
          Member member = mship.getMember();
          
          if (mship.getOwnerAttrDefId() != null) {
            AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(
                mship.getOwnerAttrDefId(), false);
            if (attributeDef == null) {
              // should we be sending notifications for the delete after the object is deleted??
              mship.delete();
            } else {
            
             ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
                  ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), AttributeDefPrivilege.listToPriv(field.getName()).getName(), 
                  ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), field.getUuid(), 
                  ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), member.getUuid(),
                  ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), member.getSubjectId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), member.getSubjectSourceId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.ATTRIBUTE_DEF.getType(),
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_ATTRIBUTE_DEF,
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), mship.getOwnerAttrDefId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), attributeDef.getName());
             batch.add(changeLogEntry);
            }
            
          } else if (mship.getOwnerGroupId() != null) {
            Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(
                mship.getOwnerGroupId(), false, new QueryOptions().secondLevelCache(false));
            if (group == null) {
              // should we be sending notifications for the delete after the object is deleted??
              mship.delete();
            } else {
              if (field.getTypeString().equals(FieldType.LIST.getType())) {
                ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, 
                    ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), field.getName(), 
                    ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), field.getUuid(), 
                    ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), member.getUuid(),
                    ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), member.getSubjectId(),
                    ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), member.getSubjectSourceId(),
                    ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), mship.getOwnerGroupId(),
                    ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), group.getName());
                batch.add(changeLogEntry);
              } else if (field.getTypeString().equals(FieldType.ACCESS.getType())) {
                ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
                    ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), AccessPrivilege.listToPriv(field.getName()).getName(), 
                    ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), field.getUuid(), 
                    ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), member.getUuid(),
                    ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), member.getSubjectId(),
                    ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), member.getSubjectSourceId(),
                    ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.ACCESS.getType(),
                    ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_GROUP,
                    ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), mship.getOwnerGroupId(),
                    ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), group.getName());
                batch.add(changeLogEntry);
              } else {
                throw new RuntimeException("Field type for group membership is not list or access.  Field id is: " + field.getUuid());
              }
            }
            
          } else if (mship.getOwnerStemId() != null) {
            Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(
                mship.getOwnerStemId(), false, new QueryOptions().secondLevelCache(false));
            if (stem == null) {
              // should we be sending notifications for the delete after the object is deleted??
              mship.delete();
            } else {
            
              ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, 
                  ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), NamingPrivilege.listToPriv(field.getName()).getName(), 
                  ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), field.getUuid(), 
                  ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), member.getUuid(),
                  ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), member.getSubjectId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), member.getSubjectSourceId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), FieldType.NAMING.getType(),
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), Membership.OWNER_TYPE_STEM,
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), mship.getOwnerStemId(),
                  ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), stem.getName());
              batch.add(changeLogEntry);
            }
          } else {
            throw new RuntimeException("Cannot determine if flat membership is for a group, stem, or attr def: " + mship.getId());
          }
          
          if (batch.size() > 0 && batch.size() % batchSize == 0) {
            GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(batch, true);
            batch.clear();
          }
        }
        
        processedCount++;
      }
      
      // make sure all changes get made
      if (batch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(batch, true);
        batch.clear();
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
      
      return mships.size();
    } finally {
      stopStatusThread();
    }
  }
  
  private int getBatchSize() {
    int size = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
    if (size <= 0) {
      size = 1;
    }

    return size;
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
  
  private void reset() {
    processedCount = 0;
    donePhase = false;
    startTime = System.currentTimeMillis();
    
    // status thread
    statusThread = new Thread(new Runnable() {
      
      public void run() {
        SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        
        while (true) {

          // sleep 30 seconds between status messages
          for (int i = 0; i < 30; i++) {
            
            if (donePhase) {
              return;
            }
            
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ie) {
              // ignore this
            }
          }
          if (donePhase) {
            return;
          }
          
          if (showResults) {
            
            // print results
            long currentTotalCount = totalCount;              
            long currentProcessedCount = processedCount;
            
            if (currentTotalCount != 0) {
              long now = System.currentTimeMillis();
              long endTime = 0;
              double percent = 0;
              
              if (currentProcessedCount > 0) {
                percent = ((double)currentProcessedCount * 100D) / currentTotalCount;
                
                if (percent > 1) {
                  endTime = startTime + (long)((now - startTime) * (100D / percent));
                }
              }
              
              System.out.print(format.format(new Date(now)) + ": Processed " + currentProcessedCount + " of " + currentTotalCount + " (" + Math.round(percent)  + "%) of current phase.  ");
              
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
    donePhase = true;
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
