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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.flat.FlatStem;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 */
public class SyncFlatTables {
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
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
  
  /**
   * Whether or not to print out results of what's being done
   * @param showResults
   * @return SyncFlatTables
   */
  public SyncFlatTables showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates
   * @param saveUpdates
   * @return SyncFlatTables
   */
  public SyncFlatTables saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }

  /**
   * Sync all flat tables
   * @return the number of updates made
   */
  public int syncAllFlatTables() {
    
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
   * add missing flat attr defs
   * @return the number of inserted flat attr defs
   */
  public int addMissingFlatAttributeDefs() {
    try {
      reset();
      showStatus("\n\nSearching for missing flat attribute defs");
      Set<AttributeDef> attrDefs = GrouperDAOFactory.getFactory().getFlatAttributeDef().findMissingFlatAttributeDefs();
      totalCount = attrDefs.size();
      showStatus("Found " + totalCount + " missing flat attribute defs");
      Iterator<AttributeDef> iter = attrDefs.iterator();
      
      Set<FlatAttributeDef> batch = new LinkedHashSet<FlatAttributeDef>();
      int batchSize = getBatchSize();
      
      while (iter.hasNext()) {
        AttributeDef attrDef = iter.next();
        FlatAttributeDef flatAttrDef = new FlatAttributeDef();
        flatAttrDef.setId(attrDef.getUuid());
        batch.add(flatAttrDef);
        LOG.info("Adding flat attribute def with name: " + attrDef.getName());
        
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

  /**
   * add missing flat stems
   * @return the number of inserted flat stems
   */
  public int addMissingFlatStems() {
    try {
      reset();
      showStatus("\n\nSearching for missing flat stems");
      Set<Stem> stems = GrouperDAOFactory.getFactory().getFlatStem().findMissingFlatStems();
      totalCount = stems.size();
      showStatus("Found " + totalCount + " missing flat stems");
      Iterator<Stem> iter = stems.iterator();
      
      Set<FlatStem> batch = new LinkedHashSet<FlatStem>();
      int batchSize = getBatchSize();
      
      while (iter.hasNext()) {
        Stem stem = iter.next();
        FlatStem flatStem = new FlatStem();
        flatStem.setId(stem.getUuid());
        batch.add(flatStem);
        LOG.info("Adding flat stem with name: " + stem.getName());
        
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

  /**
   * add missing flat memberships
   * @return the number of inserted flat memberships
   */
  public int addMissingFlatMemberships() {
    try {
      reset();
      showStatus("\n\nSearching for missing flat memberships");
      Set<Membership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findMissingFlatMemberships();
      totalCount = mships.size();
      showStatus("Found " + totalCount + " missing flat memberships");
      Iterator<Membership> iter = mships.iterator();
      
      Set<FlatMembership> batch = new LinkedHashSet<FlatMembership>();
      int batchSize = getBatchSize();
      
      while (iter.hasNext()) {
        Membership mship = iter.next();
        Field field = FieldFinder.findById(mship.getFieldId(), true);
        
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
  
        batch.add(flatMship);
        LOG.info("Adding flat membership with id: " + flatMship.getId() + ", ownerId: " + flatMship.getOwnerId() + 
            ", memberId: " + flatMship.getMemberId() + ", fieldId: " + flatMship.getFieldId());
        
        if (batch.size() % batchSize == 0 || !iter.hasNext()) {
          if (saveUpdates) {
            GrouperDAOFactory.getFactory().getFlatMembership().saveBatch(batch);
          }
          batch.clear();
        }
        
        processedCount++;
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
  
      return mships.size();
    } finally {
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

  /**
   * add missing flat groups
   * @return the number of inserted flat groups
   */
  public int addMissingFlatGroups() {
    try {
      reset();
      showStatus("\n\nSearching for missing flat groups");
      Set<Group> groups = GrouperDAOFactory.getFactory().getFlatGroup().findMissingFlatGroups();
      totalCount = groups.size();
      showStatus("Found " + totalCount + " missing flat groups");
      Iterator<Group> iter = groups.iterator();
      
      Set<FlatGroup> batch = new LinkedHashSet<FlatGroup>();
      int batchSize = getBatchSize();
      
      while (iter.hasNext()) {
        Group group = iter.next();
        FlatGroup flatGroup = new FlatGroup();
        flatGroup.setId(group.getUuid());
        batch.add(flatGroup);
        LOG.info("Adding flat group with name: " + group.getName());
  
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
  
  /**
   * remove bad flat attr defs
   * @return the number of removed flat attr defs
   */
  public int removeBadFlatAttributeDefs() {
    try {
      reset();
      showStatus("\n\nSearching for bad flat attribute defs");
      Set<FlatAttributeDef> attrDefs = GrouperDAOFactory.getFactory().getFlatAttributeDef().findBadFlatAttributeDefs();
      totalCount = attrDefs.size();
      showStatus("Found " + totalCount + " bad flat attribute defs");
      Iterator<FlatAttributeDef> iter = attrDefs.iterator();
      while (iter.hasNext()) {
        FlatAttributeDef attrDef = iter.next();
        LOG.info("Deleting flat attribute def with id: " + attrDef.getId());
        
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

  /**
   * remove bad flat stems
   * @return the number of removed flat stems
   */
  public int removeBadFlatStems() {
    try {
      reset();
      showStatus("\n\nSearching for bad flat stems");
      Set<FlatStem> stems = GrouperDAOFactory.getFactory().getFlatStem().findBadFlatStems();
      totalCount = stems.size();
      showStatus("Found " + totalCount + " bad flat stems");
      Iterator<FlatStem> iter = stems.iterator();
      while (iter.hasNext()) {
        FlatStem stem = iter.next();
        LOG.info("Deleting flat stem with id: " + stem.getId());
        
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

  /**
   * remove bad flat groups
   * @return the number of removed flat groups
   */
  public int removeBadFlatGroups() {
    try {
      reset();
      showStatus("\n\nSearching for bad flat groups");
      Set<FlatGroup> groups = GrouperDAOFactory.getFactory().getFlatGroup().findBadFlatGroups();
      totalCount = groups.size();
      showStatus("Found " + totalCount + " bad flat groups");
      Iterator<FlatGroup> iter = groups.iterator();
      while (iter.hasNext()) {
        FlatGroup group = iter.next();
        LOG.info("Deleting flat group with id: " + group.getId());
        
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

  /**
   * remove bad flat memberships
   * @return the number of removed flat memberships
   */
  public int removeBadFlatMemberships() {
    try {
      reset();
      showStatus("\n\nSearching for bad flat memberships");
      Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findBadFlatMemberships();
      totalCount = mships.size();
      showStatus("Found " + totalCount + " bad flat memberships");
      Iterator<FlatMembership> iter = mships.iterator();
      while (iter.hasNext()) {
        FlatMembership mship = iter.next();
        LOG.info("Deleting flat membership with id: " + mship.getId() + ", ownerId: " + mship.getOwnerId() + 
            ", memberId: " + mship.getMemberId() + ", fieldId: " + mship.getFieldId());
        
        if (saveUpdates) {
          mship.delete();
        }
        
        processedCount++;
      }
      
      if (mships.size() > 0 && saveUpdates) {
        showStatus("Done making " + totalCount + " updates");
      }
      
      return mships.size();
    } finally {
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
  
  private void reset() {
    totalCount = 0;
    processedCount = 0;
    donePhase = false;
    startTime = System.currentTimeMillis();
    
    // status thread
    statusThread = new Thread(new Runnable() {
      
      public void run() {
        SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");
        
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
              int percent = 0;
              
              if (currentProcessedCount > 0) {
                percent = (int)Math.round(((double)currentProcessedCount * 100D) / currentTotalCount);
                endTime = startTime + (long)((now - startTime) * (100D / percent));
              }
              
              System.out.print("Processed " + currentProcessedCount + " of " + currentTotalCount + " (" + percent  + "%) of current phase.  ");
              
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
}
