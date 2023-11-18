/**
 * Copyright 2018 Internet2
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
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#renameGroup(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void renameGroup(String oldGroupName, String newGroupName,
      ChangeLogEntry changeLogEntry) {
  
    String event =  this.getConsumerName() + " rename group " + oldGroupName + " to " + newGroupName;
    addEvent(event);
    LOG.debug(event);

  
  }

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeMovedGroup(java.lang.String, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void removeMovedGroup(String oldGroupName, ChangeLogEntry changeLogEntry) {
    String event =  this.getConsumerName() + " remove moved group " + oldGroupName;
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
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void addGroup(Group group, ChangeLogEntry changeLogEntry) {
    // final String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
    String event =  this.getConsumerName() + " add group " + group.getName();
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addGroupAndMemberships(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void addGroupAndMemberships(Group group, ChangeLogEntry changeLogEntry) {
    // changeLogEntry type is attributeAssign_addAttributeAssign on group or folder
    String event = this.getConsumerName() + " add group " + group.getName() + " and memberships";
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#updateGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void updateGroup(Group group, ChangeLogEntry changeLogEntry) {
    String event = this.getConsumerName() + " update group " + group.getName();
    addEvent(event);
    LOG.debug(event);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeGroup(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void removeGroup(Group group, ChangeLogEntry changeLogEntry) {
    String event = this.getConsumerName() + " remove group " + group.getName();
    addEvent(event);
    LOG.debug(event + " per change log entry {}", changeLogEntry.getSequenceNumber());
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeDeletedGroup(edu.internet2.middleware.grouper.pit.PITGroup, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void removeDeletedGroup(PITGroup pitGroup, ChangeLogEntry changeLogEntry) {
    String event = this.getConsumerName() + " remove deleted group " + pitGroup.getName();
    addEvent(event);
    LOG.debug(event + " per change log entry {}", changeLogEntry.getSequenceNumber());
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#addMembership(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void addMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry) {
//    final String groupName = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
//    final String subjectId = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);

    String event = this.getConsumerName() + " add subject " + subject.getId() + " to group " + group.getName();
    addEvent(event);
    LOG.debug(event);
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBaseImpl#removeMembership(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.changeLog.ChangeLogEntry)
   */
  @Override
  protected void removeMembership(Subject subject, Group group,
      ChangeLogEntry changeLogEntry) {
//    final String groupName = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
//    final String subjectId = changeLogEntry
//        .retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
    String event = this.getConsumerName() + " remove subject " + subject.getId() + " from group " + group.getName();
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