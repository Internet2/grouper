/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class ChangeLogHelper {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderType.class);

  /**
   * 
   * @param consumerName name of configured consumer, or another name that is not configured (e.g. ldappcng)
   * @param hib3GrouploaderLog send an instance of this in so it can be logged to the DB...
   * @param changeLogConsumerBase is the instance that should handle the requests
   */
  public static void processRecords(String consumerName, Hib3GrouperLoaderLog hib3GrouploaderLog, ChangeLogConsumerBase changeLogConsumerBase) {
    ChangeLogConsumer changeLogConsumer = GrouperDAOFactory.getFactory().getChangeLogConsumer().findByName(consumerName, false);
    boolean error = false;
    
    //if this is a new job
    if (changeLogConsumer == null) {
      changeLogConsumer = new ChangeLogConsumer();
      changeLogConsumer.setName(consumerName);
      GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
    }
    
    //if the sequence number is not set
    if (changeLogConsumer.getLastSequenceProcessed() == null) {
      changeLogConsumer.setLastSequenceProcessed(GrouperUtil.defaultIfNull(ChangeLogEntry.maxSequenceNumber(true), 0l));
      GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
    }
    
    
    //lets only do 100k records at a time
    for (int i=0;i<1000;i++) {
      
      ChangeLogProcessorMetadata changeLogProcessorMetadata = new ChangeLogProcessorMetadata();
      changeLogProcessorMetadata.setHib3GrouperLoaderLog(hib3GrouploaderLog);
      changeLogProcessorMetadata.setConsumerName(consumerName);
      
      //lets get 100 records
      List<ChangeLogEntry> changeLogEntryList = GrouperDAOFactory.getFactory().getChangeLogEntry()
        .retrieveBatch(changeLogConsumer.getLastSequenceProcessed(), 100);
      
      if (changeLogEntryList.size() == 0) {
        break;
      }
      
      //pass this to the consumer
      long lastProcessed = -1;
      try {
        lastProcessed = changeLogConsumerBase.processChangeLogEntries(changeLogEntryList, changeLogProcessorMetadata);
      } catch (Exception e) {
        LOG.error("Error", e);
        hib3GrouploaderLog.appendJobMessage("Error: " 
            + ExceptionUtils.getFullStackTrace(e));
        error = true;
      }
      changeLogConsumer.setLastSequenceProcessed(lastProcessed);
      GrouperDAOFactory.getFactory().getChangeLogConsumer().saveOrUpdate(changeLogConsumer);
      
      long lastSequenceInBatch = changeLogEntryList.get(changeLogEntryList.size()-1).getSequenceNumber();

      
      if (changeLogProcessorMetadata.isHadProblem()) {
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
        LOG.error(errorString);
        hib3GrouploaderLog.appendJobMessage(errorString);
        //didnt get all the way through
        error = true;
      }
      
      if (error) {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.CONFIG_ERROR.name());
        break;
      }
      
      hib3GrouploaderLog.addTotalCount(changeLogEntryList.size());

      if (changeLogEntryList.size() < 100) {
        break;
      }
      hib3GrouploaderLog.store();
    }

    if (!error) {

      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
    }

  }
  
}
