/*
 * @author mchyzer
 * $Id: PrintTest.java,v 1.1 2009-06-10 05:31:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog.consumer;

import java.util.List;

import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;


/**
 * just print out some of the events
 */
public class PrintTest extends ChangeLogConsumerBase {

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    
    long currentId = -1;

    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        currentId = changeLogEntry.getSequenceNumber();

        //if this is a group type add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_ADD)) {
          
          //print the name from the entry
          System.out.println("Group type add, name: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_ADD.name));
        }
        
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_DELETE)) {
          
          //print the name from the entry
          System.out.println("Group type delete, name: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_TYPE_DELETE.name));
        }
        
        //if this is a group add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
          
          //print the name from the entry
          System.out.println("Group add, name: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name));
        }
        
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          
          //print the name from the entry
          System.out.println("Group delete, name: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name));
        }
        
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
          
          //print the name from the entry
          System.out.println("Group update, name: " 
              + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name)
              + ", property: " + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyChanged)
              + ", from: '" + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyOldValue)
              + "', to: '" + changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.propertyNewValue) + "'");
        }
        
        //we successfully processed this record
      }
    } catch (Exception e) {
      changeLogProcessorMetadata.registerProblem(e, "Error processing record", currentId);
      //we made it to this -1
      return currentId-1;
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldnt process any records");
    }
    
    return currentId;
  }

}
