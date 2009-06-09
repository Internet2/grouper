/**
 * 
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;


/**
 * extend this class and register in the grouper-loader.properties to be a change log consumer
 * @author mchyzer
 *
 */
public abstract class ChangeLogConsumerBase {

  /**
   * process the change logs
   * @param changeLogEntryList
   * @param changeLogProcessorMetadata
   * @return which sequence number it got up to (which sequence number was the last one processed)
   */
  public abstract long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList, 
      ChangeLogProcessorMetadata changeLogProcessorMetadata);
  
}
