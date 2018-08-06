package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/

import java.util.*;
import java.util.logging.Level;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.subject.Subject;


/**
 * This is a wrapper for the event-triggering data item used to drive PSP.
 * Besides insulating PSP code from whether it is being triggered by Grouper's
 * ChangeLog process or by a Messaging process, this wrapper also keeps track
 * of the status and additional data associated with this trigger event.
 * 
 * For instance, LDAP-based provisioners store the ldap modifications that are
 * necessary to process a trigger event within the events themselves. At the end
 * of a batch of events, all the ldap modifications are pulled together into as
 * few ldap modification operations as possible.
 *  
 * 
 * @author Bert Bee-Lindgren
 *
 */
public class ProvisioningWorkItem {
  final private static Logger LOG = LoggerFactory.getLogger(Provisioner.class);

  protected final ChangeLogEntry work;
  
  protected Boolean success=null;
  protected String status=null;
  protected String statusMessage=null;
  
  protected String action;
  protected String groupName;

  /**
   * A place where information can be cached between the start/provision/finish 
   * phases of a provisioning batch
   */
  protected Map<String, Object> provisioningData = new HashMap<String,Object>();
  
  protected final static List<ChangeLogTypeBuiltin> groupOrStemChangingActions = new ArrayList<ChangeLogTypeBuiltin>();
  
  static {
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_ADD);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_DELETE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_ADD);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_SET_DELETE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ACTION_UPDATE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_ADD);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_DELETE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.GROUP_FIELD_ADD);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.GROUP_FIELD_DELETE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.GROUP_UPDATE);
    groupOrStemChangingActions.add(ChangeLogTypeBuiltin.STEM_UPDATE);
  }

  
  /**
   * Create a work item that just holds the groupName without the overhead of  
   * a changelog item.
   * 
   * This is used when Provisioner code is used in FullSync processes.
   * @param action
   * @param group
   */
  public ProvisioningWorkItem(String action, GrouperGroupInfo group) {
    this(null);
    this.action = action;
    if ( group != null )
      this.groupName=group.getName();
    else
      this.groupName=null;
  }
  
  
  public ProvisioningWorkItem(ChangeLogEntry work) {
    this.work=work;
    
    if ( work != null ) 
      this.action = work.getChangeLogType().toString();
  }

  
  public ChangeLogEntry getChangelogEntry() {
    return work;
  }
  
  /**
   * Update the status of a work item.
   * 
   * @param logLevel A java.util.logging Level (INFO, WARNING, SEVERE (aka error)). This is 
   * used because slf4j 1.6.1 does not define such constants
   * @param status
   * @param statusMessageFormat
   * @param statusMessageArgs
   */
  private void setStatus(Level logLevel, String status, String statusMessageFormat, Object... statusMessageArgs)
  {
    this.status=status;
    this.statusMessage = String.format(statusMessageFormat, statusMessageArgs);
    
    String msg;
    if ( success ) {
      msg = "Work item handled: {}";
    }
    else {
      msg = "Work item not handled; {}";
    }
      
    // Convert the j.u.logging constants into slf4j log methods
    if ( logLevel == Level.INFO ) {
      LOG.info(msg, this);
    }
    else if ( logLevel == Level.WARNING ) {
      LOG.warn(msg, this);
    }
    else if ( logLevel == Level.SEVERE ) {
      LOG.error(msg, this);
    }
    else {
      LOG.debug(msg, this);
    }
  }
  
  public void markAsSkippedAndWarn(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = true;
    setStatus(Level.WARNING, "done", statusMessageFormat, statusMessageArgs);
  }
  
  public void markAsSuccess(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = true;
    setStatus(Level.INFO, "done", statusMessageFormat, statusMessageArgs);
  }
  
  public void markAsFailure(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = false;
    setStatus(Level.SEVERE, "failed", statusMessageFormat, statusMessageArgs);
  }
  
  /** 
   * Has this work item been processed?
   * @return true when this item should not be processed further
   */
  public boolean hasBeenProcessed() {
    // success ivar remains null until work item is processed and marked as success/failure
    return success != null;
  }
  
  
  public String getGroupName() {
    return getGroupName(true);
  }


  private String getGroupName(boolean logIfWrongEventType) {
    if ( groupName != null )
      return groupName;
    else if ( getChangelogEntry() == null )
      return null;
    
    ChangeLogLabel groupNameKey;
    if ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
      groupNameKey = ChangeLogLabels.GROUP_ADD.name;
    else if  ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE) )
      groupNameKey = ChangeLogLabels.GROUP_DELETE.name;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE) )
      groupNameKey = ChangeLogLabels.GROUP_UPDATE.name;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) )
      groupNameKey = ChangeLogLabels.MEMBERSHIP_ADD.groupName;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) )
      groupNameKey = ChangeLogLabels.MEMBERSHIP_DELETE.groupName;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN) )
      groupNameKey = ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN) )
      groupNameKey = ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName;
    else {
      if ( logIfWrongEventType ) {
        LOG.debug("Not a supported change for finding group ({} is not any of these: {}): {}",
          new Object[] {
          getChangelogEntry().getChangeLogType(),
          Arrays.asList(
            ChangeLogTypeBuiltin.GROUP_ADD,
            ChangeLogTypeBuiltin.GROUP_DELETE,
            ChangeLogTypeBuiltin.GROUP_UPDATE,
            ChangeLogTypeBuiltin.MEMBERSHIP_ADD,
            ChangeLogTypeBuiltin.MEMBERSHIP_DELETE,
            ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN,
            ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN),
          work != null ? work.getSequenceNumber() : "action="+action});
      }
      return null;
    }

    groupName = getChangelogEntry().retrieveValueForLabel(groupNameKey);
    return groupName;
  }
  
  public GrouperGroupInfo getGroupInfo(Provisioner provisioner) {
	  String groupName = getGroupName(true);
	  if ( groupName == null )
		  return null;
	  
	  return provisioner.getGroupInfo(groupName);
  }
  
  
  
  public String getSubjectId() {
    return getSubjectId(true);
  }


  private String getSubjectId(boolean logIfWrongEventType) {
    if ( getChangelogEntry() == null )
      return null;
    
    final ChangeLogLabel subjectIdKey;
    
    if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) )
      subjectIdKey = ChangeLogLabels.MEMBERSHIP_ADD.subjectId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) )
      subjectIdKey = ChangeLogLabels.MEMBERSHIP_DELETE.subjectId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_ADD) )
      subjectIdKey = ChangeLogLabels.MEMBER_ADD.subjectId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_DELETE) )
      subjectIdKey = ChangeLogLabels.MEMBER_DELETE.subjectId;
    else {
      if ( logIfWrongEventType ) {
        LOG.debug("Not a supported change for finding subject id ({} is not {}, {}, {}, nor {}): {}",  
          new Object[] {
          getChangelogEntry().getChangeLogType(),
          ChangeLogTypeBuiltin.MEMBER_ADD, ChangeLogTypeBuiltin.MEMBER_DELETE, 
          ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, 
          work != null ? work.getSequenceNumber() : "action="+action});
      }
      return null;
    }
    
    final String subjectId = getChangelogEntry().retrieveValueForLabel(subjectIdKey);

    return subjectId;
  }
  
  public String getSubjectSourceId() {
    return getSubjectSourceId(true);
  }


  private String getSubjectSourceId(boolean logIfWrongEventType) {
    if ( getChangelogEntry() == null )
      return null;
    
    final ChangeLogLabel subjectSourceIdKey;
    
    if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) )
      subjectSourceIdKey = ChangeLogLabels.MEMBERSHIP_ADD.sourceId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) )
      subjectSourceIdKey = ChangeLogLabels.MEMBERSHIP_DELETE.sourceId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_ADD) )
      subjectSourceIdKey = ChangeLogLabels.MEMBER_ADD.subjectSourceId;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_DELETE) )
      subjectSourceIdKey = ChangeLogLabels.MEMBER_DELETE.subjectSourceId;
    else {
      if ( logIfWrongEventType ) {
        LOG.debug("Not a supported change for finding subject source id ({} is not {}, {}, {}, nor {}): {}",  
          new Object[] {
          getChangelogEntry().getChangeLogType(),
          ChangeLogTypeBuiltin.MEMBER_ADD, ChangeLogTypeBuiltin.MEMBER_DELETE, 
          ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, 
          work != null ? work.getSequenceNumber() : "action="+action});
      }
      return null;
    }
    
    final String sourceId = getChangelogEntry().retrieveValueForLabel(subjectSourceIdKey);

    return sourceId;
  }
  
  public Subject getSubject(Provisioner provisioner) {
    if ( getChangelogEntry() == null )
      return null;
    
    final String subjectId = getSubjectId(true);
    final String sourceId = getSubjectSourceId(true);

    if ( subjectId == null || sourceId == null )
      return null;
    
    Subject subject = provisioner.getSubject(subjectId, sourceId);
    return subject;
  }
  
  public void putProvisioningData(String key, Object value)
  {
    provisioningData.put(key, value);
  }
  
  public Object getProvisioningDataValue(String key)
  {
    return provisioningData.get(key);
  }

  public void addValueToProvisioningData(String key, Object value) {
    List<Object> valueArray = (List) provisioningData.get(key);
    
    if ( valueArray == null )
    {
      synchronized (provisioningData) {
        // Double check to see if another thread might have created the array
        if ( !provisioningData.containsKey(key) )
          provisioningData.put(key, new ArrayList<Object>());
        
        valueArray = (List) provisioningData.get(key);
      }
    }
    
    valueArray.add(value);
  }
  
  public List<Object> getProvisioningDataValues(String key) {
    return (List<Object>) provisioningData.get(key);
  }

  public boolean wasSuccessful() {
    if ( success == null )
      return false;
    
    return success;
  }
  
  public boolean wasError() {
    return !wasSuccessful();
  }

  public String getStatusMessage() {
    return statusMessage;
  }
  
  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this)
    .append("successful", success)
    .append("msg", statusMessage);
    
    if ( work == null )
      tsb.append("action", action);
    else {
      String groupName = getGroupName(false);
      String subjectId = getSubjectId(false);
      String subjectSourceId = getSubjectSourceId(false);
      
      tsb.append("clog", String.format("clog #%d / %s", work.getSequenceNumber(), work.getChangeLogType()));
      if ( groupName != null )
        tsb.append("group", groupName);
      if ( subjectId != null )
        tsb.append("subject", String.format("%s@%s", subjectId, subjectSourceId));
    }
    return tsb.toString();
  }


  public String getMdcLabel() {
    if ( work != null )
      return String.format("%d/", work.getSequenceNumber());
    else
      return String.format("%s/", action);
  }


  public boolean isChangingGroupOrStemInformation() {
    if ( work == null )
      return false;
    
    for ( ChangeLogTypeBuiltin changelogType: groupOrStemChangingActions ) {
      if ( work.equalsCategoryAndAction(changelogType) )
        return true;
    }
    
    return false;
  }

  /**
   * Some changes (eg, labeling a folder for syncing) can have a large effect and are best handled with
   * a complete sync of all groups.
   * @return true if this work item should initiate a full sync of all groups
   */
  public boolean shouldBeHandledBySyncingAllGroups(Provisioner provisioner) {
    if ( getChangelogEntry() == null ) {
      return false;
    }

    String attributeName;

    if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD) ) {
      attributeName = getChangelogEntry().retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_ADD.attributeDefNameName);
    }
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE) ) {
      attributeName = getChangelogEntry().retrieveValueForLabel(ChangeLogLabels.ATTRIBUTE_ASSIGN_VALUE_DELETE.attributeDefNameName);
    }
    else {
      return false;
    }

    if ( attributeName == null ) {
      return false;
    }

    if ( provisioner.getConfig().attributesUsedInGroupSelectionExpression.contains(attributeName) ) {
      LOG.info("{}: Performing full-sync of all groups for work item {}", provisioner.getName(), this);
      return true;
    }

    return false;
  }
}
