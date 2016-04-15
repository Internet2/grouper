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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  
  /**
   * Create a work item that just holds the groupName without the overhead of  
   * a changelog item.
   * 
   * This is used when Provisioner code is used in FullSync processes.
   * @param group
   * @param subject
   */
  public ProvisioningWorkItem(String action, GrouperGroupInfo group) {
    this.action = action;
    this.work = null;
    if ( group != null )
      this.groupName=group.getName();
    else
      this.groupName=null;
  }
  
  
  public ProvisioningWorkItem(ChangeLogEntry work) {
    this.work=work;
    this.action = work.getChangeLogType().toString();
  }
  
  public ChangeLogEntry getChangelogEntry() {
    return work;
  }
  
  private void setStatus(String status, String statusMessageFormat, Object... statusMessageArgs)
  {
    this.status=status;
    this.statusMessage = String.format(statusMessageFormat, statusMessageArgs);
    
    if ( success )
      LOG.info("Work item was successful: {}", this);
    else
      LOG.error("Work item failed: {}", this);
  }
  
  public void markAsSuccess(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = true;
    setStatus("done", statusMessageFormat, statusMessageArgs);
  }
  
  public void markAsFailure(String statusMessageFormat, Object... statusMessageArgs)
  {
    success = false;
    setStatus("failed", statusMessageFormat, statusMessageArgs);
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
    if ( groupName != null )
      return groupName;
    else if ( getChangelogEntry() == null )
      return null;
    
    ChangeLogLabel groupNameKey;
    if ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
      groupNameKey = ChangeLogLabels.GROUP_ADD.name;
    else if  ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE) )
      groupNameKey = ChangeLogLabels.GROUP_DELETE.name;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) )
      groupNameKey = ChangeLogLabels.MEMBERSHIP_ADD.groupName;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) )
      groupNameKey = ChangeLogLabels.MEMBERSHIP_DELETE.groupName;
    else {
      LOG.debug("Not a supported change for finding group (not GROUP_ADD, GROUP_DELETE, MEMBERSHIP_ADD, nor MEMBERSHIP_DELETE): {}",  this);
      return null;
    }

    groupName = getChangelogEntry().retrieveValueForLabel(groupNameKey);
    return groupName;
  }
  
  public GrouperGroupInfo getGroupInfo(Provisioner provisioner) {
	  String groupName = getGroupName();
	  if ( groupName == null )
		  return null;
	  
	  return provisioner.getGroupInfo(groupName);
  }
  
  
  
  public String getSubjectId() {
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
      LOG.info("Not a supported change for finding subject (not MEMBERSHIP_ADD nor MEMBERSHIP_DELETE): {}",  this);
      return null;
    }
    
    final String subjectId = getChangelogEntry().retrieveValueForLabel(subjectIdKey);

    return subjectId;
  }
  
  public String getSubjectSourceId() {
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
      LOG.info("Not a supported change for finding subject (not MEMBERSHIP_ADD nor MEMBERSHIP_DELETE): {}",  this);
      return null;
    }
    
    final String sourceId = getChangelogEntry().retrieveValueForLabel(subjectSourceIdKey);

    return sourceId;
  }
  
  public Subject getSubject(Provisioner provisioner) {
    if ( getChangelogEntry() == null )
      return null;
    
    final String subjectId = getSubjectId();
    final String sourceId = getSubjectSourceId();

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
    else
      tsb.append("clog", String.format("clog #%d / %s", work.getSequenceNumber(), work.getChangeLogType()));
    
    return tsb.toString();
  }


  public String getMdcLabel() {
    if ( work != null )
      return String.format("%d/", work.getSequenceNumber());
    else
      return String.format("%s/", action);
  }
}
