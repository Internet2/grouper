package edu.internet2.middleware.grouper.changeLog.consumer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.subject.Subject;

/**
 * Example change log consumer based on ChangeLogConsumerBaseImpl. ChangeLogConsumerBaseImpl handles
 * the mapping of change log event to methods, the processing loop, and exception handling.
 */
public class PrintChangeLogConsumer extends ChangeLogConsumerBaseImpl {

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#renameGroup(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void renameGroup(String oldGroupName, String newGroupName,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
  
    String event =  consumer.getConsumerName() + " rename group " + oldGroupName + " to " + newGroupName;
    addEvent(event);
    LOG.debug(event);

  
  }

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeMovedGroup(java.lang.String, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void removeMovedGroup(String oldGroupName, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    String event =  consumer.getConsumerName() + " remove moved group " + oldGroupName;
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * list used for testing
   */
  public static List<String> eventsProcessed = new ArrayList<String>();
  
  /**
   * 
   * @param event
   */
  private void addEvent(String event) {
    //dont let this cause a memory error
    synchronized(PrintChangeLogConsumer.class) {
      if (eventsProcessed.size() > 2000) {
        eventsProcessed.clear();
      }
      eventsProcessed.add(event);
    }
  }
  
  /**
   * 
   */
  private static final Logger LOG = LoggerFactory.getLogger(PrintChangeLogConsumer.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void addGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    // final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
    String event =  consumer.getConsumerName() + " add group " + group.getName();
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addGroupAndMemberships(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void addGroupAndMemberships(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    // changeLogEntry type is attributeAssign_addAttributeAssign on group or folder
    String event = consumer.getConsumerName() + " add group " + group.getName() + " and memberships";
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#updateGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void updateGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    String event = consumer.getConsumerName() + " update group " + group.getName();
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void removeGroup(Group group, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    String event = consumer.getConsumerName() + " remove group " + group.getName();
    addEvent(event);
    LOG.debug(event + " per change log entry {}", changeLogEntry.getSequenceNumber());
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeDeletedGroup(edu.internet2.middleware.grouper.pit.PITGroup, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry,
      ChangeLogConsumerBaseImpl consumer) {
    String event = consumer.getConsumerName() + " remove deleted group " + pitGroup.getName();
    addEvent(event);
    LOG.debug(event + " per change log entry {}", changeLogEntry.getSequenceNumber());
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addMembership(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void addMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
//    final String groupName = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
//    final String subjectId = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);

    String event = consumer.getConsumerName() + " add subject " + subject.getId() + " to group " + group.getName();
    addEvent(event);
    LOG.debug(event);
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeMembership(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry, edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl)
   */
  @Override
  protected void removeMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry, ChangeLogConsumerBaseImpl consumer) {
//    final String groupName = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
//    final String subjectId = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
    String event = consumer.getConsumerName() + " remove subject " + subject.getId() + " from group " + group.getName();
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#isFullSyncRunning(java.lang.String)
   */
  @Override
  protected boolean isFullSyncRunning(String consumerName) {
    // TODO stub out fullsycn and check to see if running
    return false;
  }

}