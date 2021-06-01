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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3StemDAO;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen
 */
public class SyncStemSets {
  
  /** Whether or not to print out results of what's being done */
  private boolean showResults = true;
  
  /** Whether or not to actually save updates */
  private boolean saveUpdates = true;
  
  /** Whether or not to log details */
  private boolean logDetails = false;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SyncStemSets.class);
  
  /** total count for current phase */
  private long totalCount = 0;
  
  /** total count overall */
  private long overallTotalCount = 0;
  
  
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
   * @return SyncStemSets
   */
  public SyncStemSets showResults(boolean showResults) {
    this.showResults = showResults;
    return this;
  }
  
  /**
   * Whether or not to actually save updates
   * @param saveUpdates
   * @return SyncStemSets
   */
  public SyncStemSets saveUpdates(boolean saveUpdates) {
    this.saveUpdates = saveUpdates;
    return this;
  }

  /**
   * Whether or not to log details
   * @param logDetails
   * @return SyncStemSets
   */
  public SyncStemSets logDetails(boolean logDetails) {
    this.logDetails = logDetails;
    return this;
  }

  /**
   * Full sync
   * @return the number of stems with stemSet issues
   */
  public long fullSync() {
        
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    long stemCount = HibernateSession.byHqlStatic().createQuery("select count(*) from Stem").uniqueResult(Long.class);
    
    // if we have a lot of stems and a few stem sets, let's clear out the stem sets so we don't spend time in the second phase.
    if (stemCount > 100 && stemSetCount < 100) {

      if (GrouperDdlUtils.isMysql() || GrouperDdlUtils.isHsql()) {
        //do this since mysql cant handle self-referential foreign keys
        // restrict this only to mysql since in oracle this might cause unique constraint violations
        HibernateSession.byHqlStatic().createQuery("update StemSet set parentStemSetId = null").executeUpdate();
      }
      
      HibernateSession.byHqlStatic().createQuery("delete from StemSet").executeUpdate();
      stemSetCount = 0;
    }
    
    long count = addMissingSelfStemSets();
    
    // no need to do this if there were no stem sets to begin with...
    if (stemSetCount > 0) {
      count = count + fixExistingStemSets();
    }
    
    return count;
  }
  
  /**
   * Find issues with existing stem sets
   * @return the number of stems with stemSet issues
   */
  public long fixExistingStemSets() {
    showStatus("\n\nSearching for all stems to find existing stem sets with issues");

    // order by name so parent stems get fixed before child stems
    Set<Object[]> stemsAndParents = HibernateSession.byHqlStatic().createQuery("select uuid, parentUuid from Stem as ns order by name").listSet(Object[].class);
    Map<String, String> stemsToParents = new LinkedHashMap<String, String>();
    for (Object[] stemAndParent : stemsAndParents) {
      stemsToParents.put((String)stemAndParent[0], (String)stemAndParent[1]);
    }
    
    totalCount = stemsToParents.size();
    overallTotalCount += totalCount;
    showStatus("Found " + totalCount + " stems");
    
    long totalIssues = 0;
    
    // get root stem
    Stem root = GrouperDAOFactory.getFactory().getStem().findByName(Stem.ROOT_INT, true);

    try {
      reset();
      
      List<String> stemIds = new ArrayList<String>(stemsToParents.keySet());
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(stemIds.size(), 100);
      for (int j = 0; j < numberOfBatches; j++) {
        List<String> currentBatch = GrouperUtil.batchList(stemIds, 100, j);
        List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemIds(currentBatch));

        Map<String, List<StemSet>> ifHasStemsToStemSets = new LinkedHashMap<String, List<StemSet>>();
        for (StemSet stemSet :stemSets) {
          if (!ifHasStemsToStemSets.containsKey(stemSet.getIfHasStemId())) {
            ifHasStemsToStemSets.put(stemSet.getIfHasStemId(), new ArrayList<StemSet>());
          }
          
          ifHasStemsToStemSets.get(stemSet.getIfHasStemId()).add(stemSet);
        }
        
        for (String stemId : currentBatch) {
          
          boolean issues = false;
          
          List<StemSet> currentBatchStemSets = ifHasStemsToStemSets.get(stemId);
          
          if (currentBatchStemSets == null || currentBatchStemSets.size() == 0) {
            // guess it was deleted?
            processedCount++;
            continue;
          }
          
          // sort by depth
          Collections.sort(currentBatchStemSets, new Comparator<StemSet>() {

            public int compare(StemSet o1, StemSet o2) {
              return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
            }
          });
          
          String lastThenHas = null;
          for (int i = 0; i < currentBatchStemSets.size(); i++) {
            StemSet stemSet = currentBatchStemSets.get(i);
            
            if (i == 0) {
              // depth 0 should have same stemId for thenHas
              if (!stemSet.getIfHasStemId().equals(stemSet.getThenHasStemId())) {
                issues = true;
                break;
              }
            } else {
              if (stemsToParents.get(lastThenHas) == null || !stemsToParents.get(lastThenHas).equals(stemSet.getThenHasStemId())) {
                issues = true;
                break;
              }
            }
            
            lastThenHas = stemSet.getThenHasStemId();
          }
          
          // if we didn't reach the root
          if (!issues && !root.getUuid().equals(lastThenHas)) {
            issues = true;
          }
          
          if (issues) {
            totalIssues++;
            
            Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, true);
            
            if (saveUpdates) {
              logDetail("Fixing stem sets for: " + stem.getName());
              GrouperDAOFactory.getFactory().getStemSet().deleteByIfHasStemId(stemId);
              new Hib3StemDAO().createStemSetsForStem(stem.getUuid(), stem.getParentUuid());          
            } else {
              logDetail("Would be fixing stem sets for: " + stem.getName());
            }
          }
          
          processedCount++; 
        }
      }
      
      if (saveUpdates) {
        showStatus("Done making " + totalIssues + " updates");
      } else {
        showStatus("Would have done making " + totalIssues + " updates"); 
      }
      
      return totalIssues;
    } finally {
      stopStatusThread();
    }
  }
  
  /**
   * Add missing self stem sets
   * @return the number of stems with missing self stem sets
   */
  public long addMissingSelfStemSets() {
    showStatus("Searching for missing self stemSets");
    Set<Object[]> stemIdsAndParentIdsAndNames = GrouperDAOFactory.getFactory().getStemSet().findMissingSelfStemSets();
    totalCount = stemIdsAndParentIdsAndNames.size();
    overallTotalCount += totalCount;
    showStatus("Found " + totalCount + " missing self stemSets");

    // order by depth
    List<Set<Object[]>> stemIdsAndParentIdsAndNamesByDepth = new ArrayList<Set<Object[]>>();
    for (int i = 0; i < 150; i++) {
      stemIdsAndParentIdsAndNamesByDepth.add(new LinkedHashSet<Object[]>());
    }
    
    for (Object[] stemIdAndParentIdAndName : stemIdsAndParentIdsAndNames) {
      String name = (String)stemIdAndParentIdAndName[2];
      int depth = name.equals(":") ? 0 : (StringUtils.countMatches(name, ":") + 1);
      stemIdsAndParentIdsAndNamesByDepth.get(depth).add(stemIdAndParentIdAndName);
    }
    
    boolean useThreads = GrouperConfig.retrieveConfig().propertyValueBoolean("stemSet.sync.useThreads", true);
    int groupThreadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("stemSet.sync.threadPoolSize", 20);
    
    try {
      reset();
      
      if (stemIdsAndParentIdsAndNames.size() > 0) {
        
        for (int i = 0; i < 150; i++) {
          
          Set<Object[]> currStemIdsAndParentIdsAndNames = stemIdsAndParentIdsAndNamesByDepth.get(i);

          List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
          List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
          
          for (Object[] stemIdAndParentIdAndName : currStemIdsAndParentIdsAndNames) {
            
            final String stemId = (String)stemIdAndParentIdAndName[0];
            final String parentId = (String)stemIdAndParentIdAndName[1];
            
            GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("createStemSetsForStem") {
              
              @Override
              public Void callLogic() {
                if (saveUpdates) {
                  logDetail("Adding stemSets for stem: " + stemId);
                  new Hib3StemDAO().createStemSetsForStem(stemId, parentId);          
                } else {
                  logDetail("Would be adding stemSets for stem: " + stemId);
                }
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
            
            processedCount++;
          }
          
          GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
          GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
        }
        
        if (saveUpdates) {
          showStatus("Done making " + totalCount + " updates");
        } else {
          showStatus("Would have done making " + totalCount + " updates"); 
        }
      }
      
      return totalCount;
    } finally {
      stopStatusThread();
    }
  }

  /**
   * 
   * @return
   */
  public long getUpdateCount() {
    return overallTotalCount;
  }

  
  public long getProcessedCount() {
    return processedCount;
  }

  private void showStatus(String message) {
    if (showResults) {
      println(message);
    }
  }
  
  private int detailCount = 0;
  
  private void logDetail(String detail) {
    if (logDetails) {
      LOG.info(detail);
    }
    if (captureOutput) {
      detailCount++;
      if (detailCount < 4000) {
        this.output.append(detail).append("\n");
      }
      if (detailCount == 4000) {
        this.output.append("Too many details, stopping to log details\n");
      }
    }
  }
  
  /**
   * print
   * @param string
   */
  private void print(String string) {
    if (this.captureOutput) {
      this.output.append(string);
    } else {
      System.out.print(string);
    }
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
              
              print(format.format(new Date(now)) + ": Processed " + currentProcessedCount + " of " + currentTotalCount + " (" + Math.round(percent) + "%) of current phase.  ");
              
              if (endTime != 0) {
                print("Estimated completion time: " + estFormat.format(new Date(endTime)) + ".");
              }
              
              print("\n");
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