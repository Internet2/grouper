/**
 * Copyright 2012 Internet2
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
 * $Id: Hib3GrouperLoaderLog.java,v 1.7 2009-02-09 05:33:30 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * maps to the grouper ddl table
 */
public class Hib3GrouperLoaderLog implements HibGrouperLifecycle {
  
  /**
   * 
   */
  public static final String TABLE_GROUPER_LOADER_LOG = "grouper_loader_log";

  /**
   * 
   */
  public static final String COLUMN_CONTEXT_ID = "context_id";


  /**
   * default constructor
   */
  public Hib3GrouperLoaderLog() {
    //blank
  }
  
  /** uuid for the row so hib knows insert vs update */
  private String id;
  
  /** job that ran in loader, might have group name in it */
  private String jobName;
  
  /** STARTED, SUCCESS, ERROR, WARNING, GrouerLoaderStatus */
  private String status;
  
  /** when the job started */
  private Timestamp startedTime;

  /** when the record was last updated */
  private Timestamp lastUpdated;

  /** when the job ended (if it is SUCCESS or ERROR) */
  private Timestamp endedTime;
  
  /** how long the job took */
  private Integer millis;
  
  /** if this job gets and loads data, this is the get data part */
  private Integer millisGetData;
  
  /** if this job gets and loads data, this is the load data part */
  private Integer millisLoadData;
  
  /** enum value from GrouperLoaderJobType */
  private String jobType;
  
  /** enum value from GrouperLoaderJobScheduleType */
  private String jobScheduleType;
  
  /** job description (more info than fields in this class */
  private String jobDescription;
  
  /** could be an error or success message.  might include partial stacktraces */
  private StringBuilder jobMessage;
  
  /** host that the loader is running on */
  private String host;
  
  /** if this is a group related job, then this is the group uuid */
  private String groupUuid;
  
  /** quartz cron setting string */ 
  private String jobScheduleQuartzCron;
 
  /** if the schedule is periodic, then this is the seconds in between */
  private Integer jobScheduleIntervalSeconds;
  
  /** if the quartz threadpool is exhausted, and many jobs are up for scheduling, then
   * the highest priority will win.  Default is 5 if not entered.
   */
  private Integer jobSchedulePriority;

  /**
   * number of subjects which arent resolvable via source
   */
  private Integer unresolvableSubjectCount = 0;
  
  /** number of records inserted */
  private Integer insertCount = 0;

  /** number of records updated */
  private Integer updateCount = 0;

  /** number of records deleted */
  private Integer deleteCount = 0;

  /** number of records total (e.g. size of group) */
  private Integer totalCount = 0;
  
  /** if this is a subjob of another job, then put the parent job name here */
  private String parentJobName;
  
  /** if this is a subjob of another job, then put the parent job id here */
  private String parentJobId;
  
  /** if this is anded with other groups, these are the names */
  private String andGroupNames;
  
  /**
   * if this is anded with other groups, these are the names
   * @return the andGroupNames
   */
  public String getAndGroupNames() {
    return this.andGroupNames;
  }
  
  /**
   * if this is anded with other groups, these are the names
   * @param andGroupNames1 the andGroupNames to set
   */
  public void setAndGroupNames(String andGroupNames1) {
    this.andGroupNames = andGroupNames1;
  }

  /**
   * add to totalCount
   * @param add
   */
  public void addTotalCount(Integer add) {
    this.totalCount = GrouperUtil.defaultIfNull(this.totalCount, 0);
    add = GrouperUtil.defaultIfNull(add, 0);
    this.totalCount += add;
  }
  
  /**
   * add to deleteCount
   * @param add
   */
  public void addDeleteCount(Integer add) {
    this.deleteCount = GrouperUtil.defaultIfNull(this.deleteCount, 0);
    add = GrouperUtil.defaultIfNull(add, 0);
    this.deleteCount += add;
  }
  
  /**
   * add to updateCount
   * @param add
   */
  public void addUpdateCount(Integer add) {
    this.updateCount = GrouperUtil.defaultIfNull(this.updateCount, 0);
    add = GrouperUtil.defaultIfNull(add, 0);
    this.updateCount += add;
  }
  
  /**
   * add to insertCount
   * @param add
   */
  public void addInsertCount(Integer add) {
    this.insertCount = GrouperUtil.defaultIfNull(this.insertCount, 0);
    add = GrouperUtil.defaultIfNull(add, 0);
    this.insertCount += add;
  }
  
  /**
   * add to unresolvable subjectCount
   * @param add
   */
  public void addUnresolvableSubjectCount(int add) {
    this.unresolvableSubjectCount = GrouperUtil.defaultIfNull(this.unresolvableSubjectCount, 0);
    this.unresolvableSubjectCount += add;
  }
  
  /**
   * if this is a subjob of another job, then put the parent job name here
   * @return the parentJobName
   */
  public String getParentJobName() {
    return this.parentJobName;
  }



  
  /**
   * if this is a subjob of another job, then put the parent job name here
   * @param parentJobName1 the parentJobName to set
   */
  public void setParentJobName(String parentJobName1) {
    this.parentJobName = parentJobName1;
  }



  
  /**
   * if this is a subjob of another job, then put the parent job id here
   * @return the parentJobId
   */
  public String getParentJobId() {
    return this.parentJobId;
  }



  
  /**
   * if this is a subjob of another job, then put the parent job id here
   * @param parentJobId1 the parentJobId to set
   */
  public void setParentJobId(String parentJobId1) {
    this.parentJobId = parentJobId1;
  }



  /**
   * number of records total (e.g. size of group)
   * @return the totalCount
   */
  public Integer getTotalCount() {
    return this.totalCount;
  }


  
  /**
   * number of records total (e.g. size of group)
   * @param totalCount1 the totalCount to set
   */
  public void setTotalCount(Integer totalCount1) {
    this.totalCount = totalCount1;
  }


  /**
   * number of records inserted
   * @return the insertCount
   */
  public Integer getInsertCount() {
    return this.insertCount;
  }

  
  /**
   * number of records inserted
   * @param insertCount1 the insertCount to set
   */
  public void setInsertCount(Integer insertCount1) {
    this.insertCount = insertCount1;
  }

  
  /**
   * number of records updated
   * @return the updateCount
   */
  public Integer getUpdateCount() {
    return this.updateCount;
  }

  
  /**
   * number of records updated
   * @param updateCount1 the updateCount to set
   */
  public void setUpdateCount(Integer updateCount1) {
    this.updateCount = updateCount1;
  }

  
  /**
   * number of records deleted
   * @return the deleteCount
   */
  public Integer getDeleteCount() {
    return this.deleteCount;
  }

  
  /**
   * number of records deleted
   * @param deleteCount1 the deleteCount to set
   */
  public void setDeleteCount(Integer deleteCount1) {
    this.deleteCount = deleteCount1;
  }

  /**
   * number of subjects which arent resolvable via source
   * @return the unresolvableSubjectCount
   */
  public Integer getUnresolvableSubjectCount() {
    return this.unresolvableSubjectCount;
  }

  /**
   * number of subjects which arent resolvable via source
   * @param unresolvableSubjectCount1 the unresolvableSubjectCount to set
   */
  public void setUnresolvableSubjectCount(Integer unresolvableSubjectCount1) {
    this.unresolvableSubjectCount = unresolvableSubjectCount1;
  }

  /**
   * increment number of subjects which arent resolvable via source
   */
  public void incrementUnresolvableSubjectCount() {
    //make null safe, null means 0
    if (this.unresolvableSubjectCount == null) {
      this.unresolvableSubjectCount = 1;
    } else {
      this.unresolvableSubjectCount++;
    }
  }

  /**
   * uuid for the row so hib knows insert vs update
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * uuid for the row so hib knows insert vs update
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * job that ran in loader, might have group name in it
   * @return the jobName
   */
  public String getJobName() {
    return this.jobName;
  }

  
  /**
   * job that ran in loader, might have group name in it
   * @param jobName1 the jobName to set
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  
  /**
   * STARTED, SUCCESS, ERROR, WARNING, GrouerLoaderStatus
   * @return the status
   */
  public String getStatus() {
    return this.status;
  }

  
  /**
   * STARTED, SUCCESS, ERROR, WARNING, GrouerLoaderStatus
   * @param status1 the status to set
   */
  public void setStatus(String status1) {
    this.status = status1;
  }

  
  /**
   * when the job started
   * @return the startedTime
   */
  public Timestamp getStartedTime() {
    return this.startedTime;
  }

  
  /**
   * when the job started
   * @param startedTime1 the startedTime to set
   */
  public void setStartedTime(Timestamp startedTime1) {
    this.startedTime = startedTime1;
  }

  
  /**
   * when the job ended (if it is SUCCESS or ERROR)
   * @return the endedTime
   */
  public Timestamp getEndedTime() {
    return this.endedTime;
  }

  
  /**
   * when the job ended (if it is SUCCESS or ERROR)
   * @param endedTime1 the endedTime to set
   */
  public void setEndedTime(Timestamp endedTime1) {
    this.endedTime = endedTime1;
  }

  
  /**
   * how long the job took
   * @return the millis
   */
  public Integer getMillis() {
    return this.millis;
  }

  
  /**
   * how long the job took
   * @param millis1 the millis to set
   */
  public void setMillis(Integer millis1) {
    this.millis = millis1;
  }

  
  /**
   * if this job gets and loads data, this is the get data part
   * @return the millisGetData
   */
  public Integer getMillisGetData() {
    return this.millisGetData;
  }

  
  /**
   * if this job gets and loads data, this is the get data part
   * @param millisGetData1 the millisGetData to set
   */
  public void setMillisGetData(Integer millisGetData1) {
    this.millisGetData = millisGetData1;
  }

  
  /**
   * if this job gets and loads data, this is the load data part
   * @return the millisLoadData
   */
  public Integer getMillisLoadData() {
    return this.millisLoadData;
  }

  
  /**
   * if this job gets and loads data, this is the load data part
   * @param millisLoadData1 the millisLoadData to set
   */
  public void setMillisLoadData(Integer millisLoadData1) {
    this.millisLoadData = millisLoadData1;
  }

  
  /**
   * enum value from GrouperLoaderJobType
   * @return the jobType
   */
  public String getJobType() {
    return this.jobType;
  }

  
  /**
   * enum value from GrouperLoaderJobType
   * @param jobType1 the jobType to set
   */
  public void setJobType(String jobType1) {
    this.jobType = jobType1;
  }

  
  /**
   * enum value from GrouperLoaderJobScheduleType
   * @return the jobScheduleType
   */
  public String getJobScheduleType() {
    return this.jobScheduleType;
  }

  
  /**
   * enum value from GrouperLoaderJobScheduleType
   * @param jobScheduleType1 the jobScheduleType to set
   */
  public void setJobScheduleType(String jobScheduleType1) {
    this.jobScheduleType = jobScheduleType1;
  }

  
  /**
   * job description (more info than fields in this class
   * @return the jobDescription
   */
  public String getJobDescription() {
    return this.jobDescription;
  }

  
  /**
   * job description (more info than fields in this class
   * @param jobDescription1 the jobDescription to set
   */
  public void setJobDescription(String jobDescription1) {
    this.jobDescription = jobDescription1;
  }

  
  /**
   * could be an error or success message.  might include partial stacktraces
   * @return the jobMessage
   */
  public String getJobMessage() {
    return this.jobMessage == null ? null : this.jobMessage.toString();
  }

  
  /**
   * could be an error or success message.  might include partial stacktraces
   * @param messageFragment
   */
  public void appendJobMessage(String messageFragment) {
    if (this.jobMessage == null) {
      this.jobMessage = new StringBuilder();
    }
    this.jobMessage.append(messageFragment);
  }
  
  /**
   * could be an error or success message.  might include partial stacktraces.
   * 
   * insert at beginning
   * @param messageFragment
   */
  public void insertJobMessage(String messageFragment) {
    if (this.jobMessage == null) {
      this.jobMessage = new StringBuilder();
    }
    this.jobMessage.insert(0, messageFragment);
  }
  
  /**
   * could be an error or success message.  might include partial stacktraces
   * @param jobMessage1 the jobMessage to set
   */
  public void setJobMessage(String jobMessage1) {
    this.jobMessage = jobMessage1 == null ? null : new StringBuilder(jobMessage1);
  }

  
  /**
   * host that the loader is running on
   * @return the host
   */
  public String getHost() {
    return this.host;
  }

  
  /**
   * host that the loader is running on
   * @param host1 the host to set
   */
  public void setHost(String host1) {
    this.host = host1;
  }

  /**
   * get group name from job name 
   * @return group name
   */
  public String getGroupNameFromJobName() {
    //LDAP_SIMPLE__someStem:myLdapGroup__aa8f3a245d1947509d347fee0f6a80b2
    
    String jobName = this.getJobName();
    if (StringUtils.isBlank(jobName)) {
      return null;
    }

    int firstIndex = jobName.indexOf("__") + 2;
    int lastIndex = jobName.lastIndexOf("__");
    if (firstIndex < 0 || lastIndex == 0) {
      return null;
    }
    return jobName.substring(firstIndex, lastIndex);
    
  }
  
  
  /**
   * if this is a group related job, then this is the group uuid
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }

  
  /**
   * if this is a group related job, then this is the group uuid
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }

  
  /**
   * quartz cron setting string
   * @return the jobScheduleQuartzCron
   */
  public String getJobScheduleQuartzCron() {
    return this.jobScheduleQuartzCron;
  }

  
  /**
   * quartz cron setting string
   * @param jobScheduleQuartzCron1 the jobScheduleQuartzCron to set
   */
  public void setJobScheduleQuartzCron(String jobScheduleQuartzCron1) {
    this.jobScheduleQuartzCron = jobScheduleQuartzCron1;
  }

  
  /**
   * if the schedule is periodic, then this is the seconds in between
   * @return the jobScheduleIntervalSeconds
   */
  public Integer getJobScheduleIntervalSeconds() {
    return this.jobScheduleIntervalSeconds;
  }

  
  /**
   * if the schedule is periodic, then this is the seconds in between
   * @param jobScheduleIntervalSeconds1 the jobScheduleIntervalSeconds to set
   */
  public void setJobScheduleIntervalSeconds(Integer jobScheduleIntervalSeconds1) {
    this.jobScheduleIntervalSeconds = jobScheduleIntervalSeconds1;
  }

  
  /**
   * if the quartz threadpool is exhausted, and many jobs are up for scheduling, then
   * the highest priority will win.  Default is 5 if not entered.
   * @return the priority
   */
  public Integer getJobSchedulePriority() {
    return this.jobSchedulePriority;
  }

  
  /**
   * if the quartz threadpool is exhausted, and many jobs are up for scheduling, then
   * the highest priority will win.  Default is 5 if not entered.
   * @param priority1 the priority to set
   */
  public void setJobSchedulePriority(Integer priority1) {
    this.jobSchedulePriority = priority1;
  }
  
  /**
   * make sure this object will fit in the DB
   */
  public void truncate() {
    this.jobName = GrouperUtil.truncateAscii(this.jobName, 512);
    this.jobType = GrouperUtil.truncateAscii(this.jobType, 128);
    this.jobScheduleType = GrouperUtil.truncateAscii(this.jobScheduleType, 128);
    this.jobDescription = GrouperUtil.truncateAscii(this.jobDescription, 4000);
    this.setJobMessage(GrouperUtil.truncateAscii(this.getJobMessage(), 4000));
    this.host = GrouperUtil.truncateAscii(this.host, 128);
    this.groupUuid = GrouperUtil.truncateAscii(this.groupUuid, 128);
    this.jobScheduleQuartzCron = GrouperUtil.truncateAscii(this.jobScheduleQuartzCron, 128);
    this.parentJobName = GrouperUtil.truncateAscii(this.parentJobName, 512);
    this.parentJobId = GrouperUtil.truncateAscii(this.parentJobId, 128);
  }
  
  /**
   * truncate the fields if needed and store to db
   */
  public void store() {
    
    this.truncate();
    
    //if dry run dont do this
    if (!GrouperLoader.isDryRun()) {
    
      //do this in autonomous transaction
      HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, 
          new HibernateHandler() {
  
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
          HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
          hibernateSession.byObject().saveOrUpdate(Hib3GrouperLoaderLog.this);
          return null;
        }
        
      });
    }
  }

  /**
   * when the record was last updated
   * @return the lastUpdated
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * when the record was last updated
   * @param lastUpdated1 the lastUpdated to set
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostSave(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreDelete(HibernateSession hibernateSession) {
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreSave(HibernateSession hibernateSession) {
    this.lastUpdated = new Timestamp(System.currentTimeMillis());
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPreUpdate(HibernateSession hibernateSession) {
    this.lastUpdated = new Timestamp(System.currentTimeMillis());
  }
}
