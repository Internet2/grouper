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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen
 */
public class AddMissingGroupSets {

  /** */
  private Set<String> compositeOwnerIds = new HashSet<String>();
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
  /** Whether or not to log details */
  private boolean logDetails = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AddMissingGroupSets.class);
  
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
  
  /**
   * Whether or not to print out results of what's being done
   * @param showResults
   * @return AddMissingGroupSets
   */
  public AddMissingGroupSets showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates
   * @param saveUpdates
   * @return AddMissingGroupSets
   */
  public AddMissingGroupSets saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }

  /**
   * Whether or not to log details
   * @param logDetails
   * @return AddMissingGroupSets
   */
  public AddMissingGroupSets logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }

  /**
   * Add all missing group sets
   */
  public void addAllMissingGroupSets() {
        
    addMissingSelfGroupSetsForAttrDefs();

    addMissingSelfGroupSetsForGroups();
    
    addMissingSelfGroupSetsForGroupsWithCustomFields();
    
    addMissingSelfGroupSetsForStems();
    
    addMissingImmediateGroupSetsForAttrDefOwners();

    addMissingImmediateGroupSetsForGroupOwners();
    
    addMissingImmediateGroupSetsForStemOwners();
  }
  
  /**
   * Add missing self group sets for groups
   */
  public void addMissingSelfGroupSetsForGroups() {
    showStatus("\n\nSearching for all composite groups to cache for later use");
    cacheCompositeOwners();
    
    showStatus("Searching for missing self groupSets for groups");
    Set<Object[]> groupsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForGroups();
    totalCount = groupsAndFields.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("groupSet.sync.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("groupSet.sync.threadPoolSize", 20);
    
    try {
      reset();
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
      
      Iterator<Object[]> groupsAndFieldsIter = groupsAndFields.iterator();
      while (groupsAndFieldsIter.hasNext()) {
        Object[] groupAndField = groupsAndFieldsIter.next();
        Group group = (Group)groupAndField[0];
        Field field = (Field)groupAndField[1];
      
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(group.getCreatorUuid());
        groupSet.setCreateTime(group.getCreateTimeLong());
        groupSet.setDepth(0);
        groupSet.setMemberGroupId(group.getUuid());
        groupSet.setOwnerGroupId(group.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        
        // if the default list and the group is a composite, set the groupSet type to composite
        if (Group.getDefaultList().equals(field) && compositeOwnerIds.contains(group.getUuid())) {
          groupSet.setType(MembershipType.COMPOSITE.getTypeString());
        }
        
        batch.add(groupSet);
        logDetail("Adding self groupSet for " + group.getName() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !groupsAndFieldsIter.hasNext()) {
          if (saveUpdates) {
            final Set<GroupSet> theBatch = new LinkedHashSet<GroupSet>(batch);
            
            GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("addMissingSelfGroupSetsForGroups") {
              
              @Override
              public Void callLogic() {
                GrouperDAOFactory.getFactory().getGroupSet().saveBatch(theBatch);
                return null;
              }
            };
            
            if (!useThreads){
              grouperCallable.callLogic();
            } else {
              GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
              futures.add(future);          
              GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);
            }
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (groupsAndFields.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * Add missing self group sets for groups with custom fields
   */
  public void addMissingSelfGroupSetsForGroupsWithCustomFields() {
    showStatus("Searching for missing self groupSets for groups with custom fields");
    Set<Object[]> groupsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForGroupsWithCustomFields();
    totalCount = groupsAndFields.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    try {
      reset();
    Iterator<Object[]> groupsAndFieldsIter = groupsAndFields.iterator();
    while (groupsAndFieldsIter.hasNext()) {
      Object[] groupAndField = groupsAndFieldsIter.next();
      Group group = (Group)groupAndField[0];
      Field field = (Field)groupAndField[1];
      
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(group.getCreatorUuid());
        groupSet.setCreateTime(group.getCreateTimeLong());
        groupSet.setDepth(0);
        groupSet.setMemberGroupId(group.getUuid());
        groupSet.setOwnerGroupId(group.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        
        batch.add(groupSet);
        logDetail("Adding self groupSet for " + group.getName() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !groupsAndFieldsIter.hasNext()) {
          if (saveUpdates) {
            GrouperDAOFactory.getFactory().getGroupSet().saveBatch(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (groupsAndFields.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * Add missing self group sets for stems
   */
  public void addMissingSelfGroupSetsForStems() {
    showStatus("\n\nSearching for missing self groupSets for stems");
    Set<Object[]> stemsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForStems();
    totalCount = stemsAndFields.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("groupSet.sync.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("groupSet.sync.threadPoolSize", 20);
    
    try {
      reset();
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
      
      Iterator<Object[]> stemsAndFieldsIter = stemsAndFields.iterator();
      while (stemsAndFieldsIter.hasNext()) {
        Object[] stemAndField = stemsAndFieldsIter.next();
        Stem stem = (Stem)stemAndField[0];
        Field field = (Field)stemAndField[1];
      
        String stemName = null;
        if (stem.isRootStem()) {
          stemName = "{rootStem}";
        } else {
          stemName = stem.getName();
        }
      
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(stem.getCreatorUuid());
        groupSet.setCreateTime(stem.getCreateTimeLong());
        groupSet.setDepth(0);
        groupSet.setMemberStemId(stem.getUuid());
        groupSet.setOwnerStemId(stem.getUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        
        batch.add(groupSet);
        logDetail("Adding self groupSet for " + stemName + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !stemsAndFieldsIter.hasNext()) {
          if (saveUpdates) {
            final Set<GroupSet> theBatch = new LinkedHashSet<GroupSet>(batch);
            
            GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("addMissingSelfGroupSetsForStems") {
              
              @Override
              public Void callLogic() {
                GrouperDAOFactory.getFactory().getGroupSet().saveBatch(theBatch);
                return null;
              }
            };
            
            if (!useThreads){
              grouperCallable.callLogic();
            } else {
              GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
              futures.add(future);          
              GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);
            }
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (stemsAndFields.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a group
   */
  public void addMissingImmediateGroupSetsForGroupOwners() {
    showStatus("\n\nSearching for missing immediate groupSets where the owner is a group");
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForGroupOwners();
    totalCount = mships.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    try {
      reset();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
        GroupSet immediateGroupSet = new GroupSet();
        immediateGroupSet.setId(GrouperUuid.getUuid());
        immediateGroupSet.setCreatorId(mship.getCreatorUuid());
        immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
        immediateGroupSet.setDepth(1);
        immediateGroupSet.setFieldId(field.getUuid());
        immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
        immediateGroupSet.setType(MembershipType.EFFECTIVE.getTypeString());
        immediateGroupSet.setOwnerGroupId(mship.getOwnerGroupId());
        immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
            .findSelfGroup(mship.getOwnerGroupId(), mship.getFieldId()).getId());
        
        batch.add(immediateGroupSet);
        logDetail("Adding groupSet for ownerGroupId = " + mship.getOwnerGroupId() + 
            ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !mshipsIter.hasNext()) {
          if (saveUpdates) {
            // We're not doing batch inserts here because the onPostSave 
            // of one groupSet insert might insert another groupSet in a child 
            // transaction that the parent transaction needs to know about
            // for the next batch insert.
            GrouperDAOFactory.getFactory().getGroupSet().save(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * Add missing group sets for immediate memberships where the owner is a stem
   */
  public void addMissingImmediateGroupSetsForStemOwners() {
    showStatus("\n\nSearching for missing immediate groupSets where the owner is a stem");
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForStemOwners();
    totalCount = mships.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    try {
      reset();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
        GroupSet immediateGroupSet = new GroupSet();
        immediateGroupSet.setId(GrouperUuid.getUuid());
        immediateGroupSet.setCreatorId(mship.getCreatorUuid());
        immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
        immediateGroupSet.setDepth(1);
        immediateGroupSet.setFieldId(field.getUuid());
        immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
        immediateGroupSet.setType(MembershipType.EFFECTIVE.getTypeString());
        immediateGroupSet.setOwnerStemId(mship.getOwnerStemId());
        immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
            .findSelfStem(mship.getOwnerStemId(), mship.getFieldId()).getId());
        
        batch.add(immediateGroupSet);
        logDetail("Adding groupSet for ownerStemId = " + mship.getOwnerStemId() + 
            ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !mshipsIter.hasNext()) {
          if (saveUpdates) {
            // We're not doing batch inserts here because the onPostSave 
            // of one groupSet insert might insert another groupSet in a child 
            // transaction that the parent transaction needs to know about
            // for the next batch insert.
            GrouperDAOFactory.getFactory().getGroupSet().save(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * cache the composite owners
   */
  private void cacheCompositeOwners() {
    Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().getAllComposites();
    Iterator<Composite> compositesIter = composites.iterator();
    
    while (compositesIter.hasNext()) {
      Composite c = compositesIter.next();
      compositeOwnerIds.add(c.getFactorOwnerUuid());
    }
  }

  /**
   * Add missing group sets for immediate memberships where the owner is a stem
   */
  public void addMissingImmediateGroupSetsForAttrDefOwners() {
    showStatus("\n\nSearching for missing immediate groupSets where the owner is an attribute def");
    Set<Membership> mships = GrouperDAOFactory.getFactory().getMembership().findMissingImmediateGroupSetsForAttrDefOwners();
    totalCount = mships.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    try {
      reset();
    Iterator<Membership> mshipsIter = mships.iterator();
    
    while (mshipsIter.hasNext()) {
      Membership mship = mshipsIter.next();
      Field field = FieldFinder.findById(mship.getFieldId(), true);
      
      GroupSet immediateGroupSet = new GroupSet();
      immediateGroupSet.setId(GrouperUuid.getUuid());
      immediateGroupSet.setCreatorId(mship.getCreatorUuid());
      immediateGroupSet.setCreateTime(mship.getCreateTimeLong());
      immediateGroupSet.setDepth(1);
      immediateGroupSet.setFieldId(field.getUuid());
      immediateGroupSet.setMemberGroupId(mship.getMemberSubjectId());
      immediateGroupSet.setType(MembershipType.EFFECTIVE.getTypeString());
      immediateGroupSet.setOwnerAttrDefId(mship.getOwnerAttrDefId());
      immediateGroupSet.setParentId(GrouperDAOFactory.getFactory().getGroupSet()
          .findSelfStem(mship.getOwnerAttrDefId(), mship.getFieldId()).getId());
      
        batch.add(immediateGroupSet);
        logDetail("Adding groupSet for ownerAttrDefId = " + mship.getOwnerStemId() + 
          ", memberGroupId = " + mship.getMemberSubjectId() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !mshipsIter.hasNext()) {
          if (saveUpdates) {
            // We're not doing batch inserts here because the onPostSave 
            // of one groupSet insert might insert another groupSet in a child 
            // transaction that the parent transaction needs to know about
            // for the next batch insert.
            GrouperDAOFactory.getFactory().getGroupSet().save(batch);      
          }
          batch.clear();
    }
        
        processedCount++;
  }

      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
    } finally {
      stopStatusThread();
    }
  }

  /**
   * Add missing self group sets for stems
   */
  public void addMissingSelfGroupSetsForAttrDefs() {
    showStatus("\n\nSearching for missing self groupSets for attribute defs");
    Set<Object[]> attrDefsAndFields = GrouperDAOFactory.getFactory().getGroupSet().findMissingSelfGroupSetsForAttrDefs();
    totalCount = attrDefsAndFields.size();
    showStatus("Found " + totalCount + " missing groupSets");
    
    Set<GroupSet> batch = new LinkedHashSet<GroupSet>();
    int batchSize = getBatchSize();

    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("groupSet.sync.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("groupSet.sync.threadPoolSize", 20);
    
    try {
      reset();
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
      
      Iterator<Object[]> attrDefsAndFieldsIter = attrDefsAndFields.iterator();
      while (attrDefsAndFieldsIter.hasNext()) {
        Object[] attrDefAndField = attrDefsAndFieldsIter.next();
        AttributeDef attributeDef = (AttributeDef)attrDefAndField[0];
        Field field = (Field)attrDefAndField[1];
      
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(attributeDef.getCreatorId());
        groupSet.setCreateTime(attributeDef.getCreatedOnDb());
        groupSet.setDepth(0);
        groupSet.setMemberAttrDefId(attributeDef.getId());
        groupSet.setOwnerAttrDefId(attributeDef.getId());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
      
        batch.add(groupSet);
        logDetail("Adding self groupSet for " + attributeDef.getName() + " for field " + field.getTypeString() + " / " + field.getName());

        if (batch.size() % batchSize == 0 || !attrDefsAndFieldsIter.hasNext()) {
          if (saveUpdates) {
            final Set<GroupSet> theBatch = new LinkedHashSet<GroupSet>(batch);
            
            GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("addMissingSelfGroupSetsForAttrDefs") {
              
              @Override
              public Void callLogic() {
                GrouperDAOFactory.getFactory().getGroupSet().saveBatch(theBatch);
                return null;
              }
            };
            
            if (!useThreads){
              grouperCallable.callLogic();
            } else {
              GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
              futures.add(future);          
              GrouperFuture.waitForJob(futures, groupThreadPoolSize, callablesWithProblems);
            }
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (attrDefsAndFields.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
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
