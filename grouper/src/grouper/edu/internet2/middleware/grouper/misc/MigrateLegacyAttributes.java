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
package edu.internet2.middleware.grouper.misc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen
 */
public class MigrateLegacyAttributes {
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = false;
  
  /** Whether or not to log details */
  private boolean logDetails = true;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MigrateLegacyAttributes.class);
  
  /** total count for current phase */
  private long totalCount = 0;
  
  /** processed count for current phase */
  private long processedCount = 0;
  
  /** if we're done processing a phase */
  private boolean donePhase = false;
  
  /** start time of script */
  private long startTime = 0;
  
  /** keep track of count of ones already migrated for each phase */
  private long alreadyDone = 0;
  
  /** keep track of the count of ones needing migration for each phase */
  private long needsMigration = 0;
  
  /** status thread */
  Thread statusThread = null;
  
  /**
   * Whether or not to print out results of what's being done
   * @param showResults
   * @return MigrateLegacyAttributes
   */
  public MigrateLegacyAttributes showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates
   * @param saveUpdates
   * @return MigrateLegacyAttributes
   */
  public MigrateLegacyAttributes saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }

  /**
   * Whether or not to log details
   * @param logDetails
   * @return MigrateLegacyAttributes
   */
  public MigrateLegacyAttributes logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }

  /**
   * Full migration
   * @return number of objects migrated
   */
  public long fullMigration() {
        
    long count = 0;
    
    count += migrateGroupTypes();
    count += migrateAttributes();
    count += migrateLists();
    count += migrateGroupTypeAssignments();
    count += migrateAttributeAssignments();
    
    return count;
  }
  
  /**
   * Migrate group types
   * @return the number of group types migrated or would need migration
   */
  @SuppressWarnings("deprecation")
  public long migrateGroupTypes() {
    showStatus("\n\nSearching for legacy group types");
    String sql = "select id, name from grouper_types_legacy where name not in ('base', 'naming', 'attributeDef')";
    List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, sql, null);
    totalCount = results.size();
    
    showStatus("Found " + totalCount + " legacy group types");

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      reset();
      
      alreadyDone = 0;
      needsMigration = 0;
      
      for (String[] values : results) {
        
      String id = values[0];
      String name = values[1];
      
        GroupType type = GroupTypeFinder.findByUuid(id, false);
        if (type != null) {
          alreadyDone++;
        } else {
          needsMigration++;
          
          if (saveUpdates) {
            logDetail("Migrating groupType with id=" + id + ", name=" + name);
            type = GroupType.internal_createType(grouperSession, name, true, null, id);
            type.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            type.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          } else {
            logDetail("Would be migrating groupType with id=" + id + ", name=" + name);
          }
        }

        processedCount++;
      }
      
      if (saveUpdates) {
        showStatus("Done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      } else {
        showStatus("Would have done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      }
      
      return needsMigration;
    } finally {
      stopStatusThread();
      
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Migrate attributes
   * @return the number of attributes migrated or would need migration
   */
  @SuppressWarnings("deprecation")
  public long migrateAttributes() {
    showStatus("\n\nSearching for legacy attributes");
    String sql = "select grouptype_uuid, name from grouper_fields_legacy where type='attribute'";
    List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, sql, null);
    totalCount = results.size();
    
    showStatus("Found " + totalCount + " legacy attributes");

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      reset();
      
      alreadyDone = 0;
      needsMigration = 0;
      
      for (String[] values : results) {
        
        String groupTypeId = values[0];
        String name = values[1];
        
        String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
        String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
        GroupType type = GroupTypeFinder.findByUuid(groupTypeId, false);
        if (type != null && AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + name, false) != null) {
          alreadyDone++;
        } else {
          needsMigration++;
          
          if (saveUpdates) {
            logDetail("Migrating attribute with name=" + name);
            type.addAttribute(grouperSession, name, true);
            type.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            type.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          } else {
            logDetail("Would be migrating attribute with name=" + name);
          }
        }
        
        processedCount++;
      }
      
      if (saveUpdates) {
        showStatus("Done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      } else {
        showStatus("Would have done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      }
      
      return needsMigration;
    } finally {
      stopStatusThread();
      
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * Migrate lists
   * @return the number of custom lists migrated or would need migration
   */
  @SuppressWarnings("deprecation")
  public long migrateLists() {
    showStatus("\n\nSearching for lists");
    String sql = "select grouptype_uuid, name from grouper_fields_legacy where type='list' and name not in ('members')";
    List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, sql, null);
    totalCount = results.size();
    
    showStatus("Found " + totalCount + " lists");

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      reset();
      
      alreadyDone = 0;
      needsMigration = 0;
      
      for (String[] values : results) {
        
        String groupTypeId = values[0];
        String name = values[1];
        
        Field field = FieldFinder.find(name, true);
        
        GroupType type = GroupTypeFinder.findByUuid(groupTypeId, false);

        if (type != null && type.internal_getAttributeDefNameForCustomLists() != null &&
            type.getAttributeDefName().getAttributeDef().getAttributeValueDelegate().findValue(type.internal_getAttributeDefNameForCustomLists().getName(), field.getUuid()) != null) {
          alreadyDone++;
        } else {
          needsMigration++;
          
          if (saveUpdates) {
            logDetail("Migrating list with name=" + name);
            type.internal_addList(grouperSession, name, field.getReadPriv(), field.getWritePriv(), field.getUuid(), false);
            type.internal_getAttributeDefForCustomLists().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
          } else {
            logDetail("Would be migrating list with name=" + name);
          }
        }

        processedCount++;
      }
      
      if (saveUpdates) {
        showStatus("Done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      } else {
        showStatus("Would have done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      }
      
      return needsMigration;
    } finally {
      stopStatusThread();
      
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Migrate group type assignments
   * @return the number of group type assignments migrated or would need migration
   */
  @SuppressWarnings("deprecation")
  public long migrateGroupTypeAssignments() {
    showStatus("\n\nSearching for group type assignments");
    
    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("legacyAttributeMigration.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("legacyAttributeMigration.threadPoolSize", 20);
  
    String baseGroupTypeId = HibernateSession.bySqlStatic().select(String.class, "select id from grouper_types_legacy where name='base'", null);

    String sql = "select id, group_uuid, type_uuid from grouper_groups_types_legacy where type_uuid not in ('" + baseGroupTypeId + "')";
    List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, sql, null);
    totalCount = results.size();
    
    showStatus("Found " + totalCount + " group type assignments");

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      reset();
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
      
      alreadyDone = 0;
      needsMigration = 0;
      
      for (final String[] values : results) {
        
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("migrateGroupTypeAssignments") {
          
          @Override
          public Void callLogic() {

            String groupTypeAssignmentId = values[0];
            String groupId = values[1];
            String typeId = values[2];
                    
            GroupType type = GroupTypeFinder.findByUuid(typeId, false);
            Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);

            if (group == null) {
              LOG.warn("Skipping groupType assignment for groupId=" + groupId + " and typeId=" + typeId + " since group could not be found.");
            } else if (type != null && group.hasType(type)) {
              incrementNumberDone();
            } else {
              incrementNumberNeedingMigration();
              
              if (saveUpdates) {
                logDetail("Migrating group type assignment.  Group=" + group.getName() + ", type="  + type.getName());
                group.internal_addType(type, groupTypeAssignmentId, true);
              } else {
                logDetail("Would be migrating group type assignment.  Group=" + group.getName() + ", type="  + typeId);
              }
            }
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

        processedCount++;
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (saveUpdates) {
        showStatus("Done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      } else {
        showStatus("Would have done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      }
      
      return needsMigration;
    } finally {
      stopStatusThread();
      
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * Migrate attribute assignments
   * @return the number of attribute assignments migrated or would need migration
   */
  @SuppressWarnings("deprecation")
  public long migrateAttributeAssignments() {
    showStatus("\n\nSearching for attribute assignments");
    
    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("legacyAttributeMigration.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("legacyAttributeMigration.threadPoolSize", 20);
    
    // cache attributes first
    String attributeSql = "select id, name from grouper_fields_legacy where type='attribute'";
    List<String[]> attributeResults = HibernateSession.bySqlStatic().listSelect(String[].class, attributeSql, null);
    final Map<String, String> fieldIdToNameMap = new HashMap<String, String>();
    for (String[] values : attributeResults) {
      String fieldId = values[0];
      String fieldName = values[1];
      fieldIdToNameMap.put(fieldId, fieldName);
    }
    
    String sql = "select id, group_id, field_id, value from grouper_attributes_legacy";
    List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, sql, null);
    totalCount = results.size();
    
    showStatus("Found " + totalCount + " attribute assignments");

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      reset();
      
      List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
      List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
      
      alreadyDone = 0;
      needsMigration = 0;
      
      for (final String[] values : results) {
        
        GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("migrateAttributeAssignments") {
          
          @Override
          public Void callLogic() {
            String attributeAssignmentId = values[0];
            String groupId = values[1];
            String attributeName = fieldIdToNameMap.get(values[2]);
            if (attributeName == null) {
              throw new RuntimeException("Unexpected.");
            }
            String attributeValue = values[3];
                    
            Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, false);

            if (group == null) {
              LOG.warn("Skipping attribute assignment for groupId=" + groupId + " and attributeAssignmentId=" + attributeAssignmentId + " since group could not be found.");
            } else if (attributeValue.equals(group.getAttributesDb().get(attributeName))) {
              incrementNumberDone();
            } else {
              incrementNumberNeedingMigration();
              
              if (saveUpdates) {
                logDetail("Migrating attribute assignment.  Group=" + group.getName() + ", attribute="  + attributeName);
                group.internal_setAttribute(attributeName, attributeValue, false, attributeAssignmentId);
              } else {
                logDetail("Would be migrating attribute assignment.  Group=" + group.getName() + ", attribute="  + attributeName);
              }
            }
            
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
        
        processedCount++;
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      
      if (saveUpdates) {
        showStatus("Done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      } else {
        showStatus("Would have done making " + needsMigration + " updates.  Number previously migrated = " + alreadyDone + ".");
      }
      
      return needsMigration;
    } finally {
      stopStatusThread();
      
      GrouperSession.stopQuietly(grouperSession);
    }
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
  
  private synchronized void incrementNumberDone() {
    alreadyDone++;
  }
  
  private synchronized void incrementNumberNeedingMigration() {
    needsMigration++;
  }
}
