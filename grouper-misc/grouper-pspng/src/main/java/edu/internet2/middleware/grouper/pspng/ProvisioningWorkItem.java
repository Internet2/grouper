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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabel;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;
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
  protected Subject subject;
  protected Group group;

  /**
   * A place where information can be cached between the start/provision/finish 
   * phases of a provisioning batch
   */
  protected Map<String, Object> provisioningData = new HashMap<String,Object>();
  
  
  /**
   * Create a work item that just holds the group and subject, 
   * not a changelog item.
   * @param group
   * @param subject
   */
  public ProvisioningWorkItem(String action, Group group, Subject subject) {
    this.action = action;
    this.work = null;
    this.group=group;
    this.subject=subject;
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
  
  
  public Group getGroup(Provisioner provisioner) {
    if ( group != null )
      return group;
    else if ( getChangelogEntry() == null )
      return null;
    
    ChangeLogLabel groupKey;
    if ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD ))
      groupKey = ChangeLogLabels.GROUP_ADD.name;
    else if  ( getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE) )
      groupKey = ChangeLogLabels.GROUP_DELETE.name;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) )
      groupKey = ChangeLogLabels.MEMBERSHIP_ADD.groupName;
    else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) )
      groupKey = ChangeLogLabels.MEMBERSHIP_DELETE.groupName;
    else {
      LOG.debug("Not a supported change for finding group (not GROUP_ADD, GROUP_DELETE, MEMBERSHIP_ADD, nor MEMBERSHIP_DELETE): {}",  this);
      return null;
    }

    group = provisioner.getGroup(getChangelogEntry().retrieveValueForLabel(groupKey));
    return group;
  }
  
  
  
  public Subject getSubject(Provisioner provisioner) {
    if ( subject != null )
      return subject;
    else if ( getChangelogEntry() == null )
      return null;
    
    final ChangeLogLabel subjectSourceIdKey, subjectIdKey;
    
    if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD) ) {
      subjectSourceIdKey = ChangeLogLabels.MEMBERSHIP_ADD.sourceId;
      subjectIdKey = ChangeLogLabels.MEMBERSHIP_ADD.subjectId;
    } else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE) ) {
      subjectSourceIdKey = ChangeLogLabels.MEMBERSHIP_DELETE.sourceId;
      subjectIdKey = ChangeLogLabels.MEMBERSHIP_DELETE.subjectId;
    } else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_ADD) ) {
      subjectSourceIdKey = ChangeLogLabels.MEMBER_ADD.subjectSourceId;
      subjectIdKey = ChangeLogLabels.MEMBER_ADD.subjectId;
    } else if (  getChangelogEntry().equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBER_DELETE) ) {
      subjectSourceIdKey = ChangeLogLabels.MEMBER_DELETE.subjectSourceId;
      subjectIdKey = ChangeLogLabels.MEMBER_DELETE.subjectId;
    } else 
    {
      LOG.debug("Not a supported change for finding subject (not MEMBERSHIP_ADD nor MEMBERSHIP_DELETE): {}",  this);
      return null;
    }
    
    final String subjectId = getChangelogEntry().retrieveValueForLabel(subjectIdKey);
    final String sourceId = getChangelogEntry().retrieveValueForLabel(subjectSourceIdKey);

    subject = provisioner.getSubject(subjectId, sourceId);
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
      tsb.append("clog", new ToStringBuilder(work)
                    .append("type", work.getChangeLogType())
                    .append("seq", work.getSequenceNumber())
                    .append("s02", work.getString02())
                    .toString());
    
    return tsb.toString();
  }
}
