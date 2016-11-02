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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class ChangeLogHelper {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(ChangeLogHelper.class);

  /**
   * example change log helper
   */
  static class TestChangeLogHelper extends ChangeLogConsumerBase {

    /**
     * 
     * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
     */
    @Override
    public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
        ChangeLogProcessorMetadata changeLogProcessorMetadata) {
      
      long currentId = -1;

      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        //try catch so we can track that we made some progress
        try {

          currentId = changeLogEntry.getSequenceNumber();
          
          if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {

            System.out.println("Member add, name: " 
                + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName)
                + ", " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));

          } else if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {

            System.out.println("Member delete, name: " 
                + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName)
                + ", " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));

          }
          
        } catch (Exception e) {
          //we unsuccessfully processed this record... decide whether to wait, throw, ignore, log, etc...
          LOG.error("problem with id: " + currentId, e);
          //continue
        }
      }

      return currentId;
    }
  };
  
  /**
   * main
   * @param args
   */
  public static void main(String args[]) {
    example();
  }
  
  /**
   * this is an unused example of calling the processRecords method.  Note, you might not have an anonymous inner
   * class there, you might just define a top level class which extends ChangeLogConsumerBase
   */
  @SuppressWarnings("unused")
  private static void example() {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //lets start on latest change log for this example...  you probably shouldnt do this in real life...
    {
      ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName("myCustomJob", false);
      if (changeLogConsumer == null) {
        changeLogConsumer = new ChangeLogConsumer();
        changeLogConsumer.setName("myCustomJob");
        GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
      }
      
      changeLogConsumer.setLastSequenceProcessed(GrouperUtil.defaultIfNull(ChangeLogEntry.maxSequenceNumber(true), 0l));
      GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
    }
    
    
    Group group = new GroupSave(grouperSession).assignName("a:b").assignCreateParentStemsIfNotExist(true).save();
    
    TestChangeLogHelper testChangeLogHelper = new TestChangeLogHelper();
    
    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    hib3GrouploaderLog.setHost(GrouperUtil.hostname());
    hib3GrouploaderLog.setJobName("myCustomJob");
    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
    hib3GrouploaderLog.store();
    
    try {

      group.addMember(SubjectFinder.findRootSubject(), false);
      
      GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
      processRecords("myCustomJob", hib3GrouploaderLog, testChangeLogHelper);
      
      group.addMember(SubjectFinder.findRootSubject(), false);
      group.addMember(SubjectFinder.findAllSubject(), false);
      group.deleteMember(SubjectFinder.findAllSubject());
      group.deleteMember(SubjectFinder.findRootSubject());

      GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
      
      processRecords("myCustomJob", hib3GrouploaderLog, testChangeLogHelper);

      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      
    } catch (Exception e) {
      LOG.error("Error processing records", e);
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
    }
    hib3GrouploaderLog.store();
  }
  
  /**
   * <pre>
   * call this method to process a batch of 100k (max) records of the change log... 
   * pass in a consumer name (nothing that people would use for a real change log consumer), that is used
   * to keep track of the last processed record, the loader log which will log process in the grouper loader
   * log table, and the processor which is the change log consumer base...
   * 
   * to test this, do your changes, e.g. add a member, delete a member, then call this:
   * 
   * GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
   * 
   * then call this method...  e.g. the static example() method in this class
   * 
   * 
   * </pre>
   * @param consumerName name of configured consumer, or another name that is not configured (e.g. ldappcng)
   * @param hib3GrouploaderLog send an instance of this in so it can be logged to the DB...
   * @param changeLogConsumerBase is the instance that should handle the requests
   */
  public static void processRecords(String consumerName, Hib3GrouperLoaderLog hib3GrouploaderLog, ChangeLogConsumerBase changeLogConsumerBase) {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (LOG.isDebugEnabled()) {
      debugMap.put("consumerName", consumerName);
    }
    
    try {

      ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName(consumerName, false);
      boolean error = false;
      
      //if this is a new job
      if (changeLogConsumer == null) {

        if (LOG.isDebugEnabled()) {
          debugMap.put("creating consumer row", true);
        }
        
        changeLogConsumer = new ChangeLogConsumer();
        changeLogConsumer.setName(consumerName);
        GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
      }
      
      //if the sequence number is not set
      if (changeLogConsumer.getLastSequenceProcessed() == null) {
        changeLogConsumer.setLastSequenceProcessed(GrouperUtil.defaultIfNull(ChangeLogEntry.maxSequenceNumber(true), 0l));
        GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
      }
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("last sequence processed", changeLogConsumer.getLastSequenceProcessed());
      }
      
      int batchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.changeLogConsumerBatchSize", 1000);

      for (int i=0;i<1000;i++) {
        
        ChangeLogProcessorMetadata changeLogProcessorMetadata = new ChangeLogProcessorMetadata();
        changeLogProcessorMetadata.setHib3GrouperLoaderLog(hib3GrouploaderLog);
        changeLogProcessorMetadata.setConsumerName(consumerName);
                
        List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
          .retrieveBatch(changeLogConsumer.getLastSequenceProcessed(), batchSize);

        if (LOG.isDebugEnabled()) {
          debugMap.put(i + ": number of records found to process", changeLogEntryList.size());
        }
        
        if (changeLogEntryList.size() == 0) {
          break;
        }
        
        //pass this to the consumer
        long lastProcessed = -1;
        try {
          lastProcessed = changeLogConsumerBase.processChangeLogEntries(changeLogEntryList, changeLogProcessorMetadata);
          
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": processed to record number", lastProcessed);
          }

        } catch (Exception e) {
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": error processing records", true);
          }
          LOG.error("Error", e);
          hib3GrouploaderLog.appendJobMessage("Error: " 
              + ExceptionUtils.getFullStackTrace(e));
          error = true;
        }
        if (lastProcessed != -1 && (changeLogConsumer.getLastSequenceProcessed() == null || changeLogConsumer.getLastSequenceProcessed() != lastProcessed)) {
          changeLogConsumer.setLastSequenceProcessed(lastProcessed);
          GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
        }
        
        long lastSequenceInBatch = changeLogEntryList.get(changeLogEntryList.size()-1).getSequenceNumber();
  
        
        if (changeLogProcessorMetadata.isHadProblem()) {
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": hadProblem", true + ", " + changeLogProcessorMetadata.getRecordProblemText());
          }
          String errorString = "Error: " 
              + changeLogProcessorMetadata.getRecordProblemText()
              + ", sequenceNumber: " + changeLogProcessorMetadata.getRecordExceptionSequence()
              + ", " + ExceptionUtils.getFullStackTrace(changeLogProcessorMetadata.getRecordException());
          LOG.error(errorString);
          hib3GrouploaderLog.appendJobMessage(errorString);
          error = true;
        }
        if (lastProcessed != lastSequenceInBatch) {
          String errorString = "Did not get all the way through the batch! " + lastProcessed
              + " != " + lastSequenceInBatch;
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": allThroughBatch", errorString);
          }
          LOG.error(errorString);
          hib3GrouploaderLog.appendJobMessage(errorString);
          //didnt get all the way through
          error = true;
        }
        
        if (error) {
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          break;
        }
        
        hib3GrouploaderLog.addTotalCount(changeLogEntryList.size());
        
        if (changeLogEntryList.size() < batchSize) {
          break;
        }
        hib3GrouploaderLog.store();
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("totalRecordsProcessed", hib3GrouploaderLog.getTotalCount());
      }
  
      if (!error) {
  
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }

    }
  }
  
}
