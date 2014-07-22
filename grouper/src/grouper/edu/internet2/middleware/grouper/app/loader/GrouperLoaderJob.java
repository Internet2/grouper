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
/*
 * @author mchyzer
 * $Id: GrouperLoaderJob.java,v 1.9 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.StatefulJob;
import org.quartz.Trigger;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.hooks.LoaderHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLoaderBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * class which will run a loader job
 * implements StatefulJob so multiple dont run at once
 */
public class GrouperLoaderJob implements Job, StatefulJob {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext context) throws JobExecutionException {
    long startTime = System.currentTimeMillis();

    Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
    
    Group group = null;
    AttributeDef attributeDef = null;
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      String jobName = context.getJobDetail().getName();
  
      hib3GrouploaderLog.setJobName(jobName);
      
      String grouperLoaderQuartzCronFromOwner = null;
      String grouperLoaderTypeFromOwner = null;
      String grouperLoaderScheduleTypeFromOwner = null;
      Integer grouperLoaderPriorityFromOwner = null;
      Integer grouperLoaderIntervalSecondsFromOwner = null;
      
      //job name is GrouperLoaderType__groupname__uuid
      GrouperLoaderType grouperLoaderType = GrouperLoaderType.typeForThisName(jobName);
      if (grouperLoaderType.equals(GrouperLoaderType.SQL_GROUP_LIST) || 
          grouperLoaderType.equals(GrouperLoaderType.SQL_SIMPLE)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
        
        if (uuidIndexStart >= 0) {
          String grouperLoaderGroupUuid = null;
          grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
          hib3GrouploaderLog.setGroupUuid(grouperLoaderGroupUuid);

          
          group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid, true);
          grouperLoaderQuartzCronFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
          grouperLoaderScheduleTypeFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
          grouperLoaderTypeFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNull(group, 
              GrouperLoader.GROUPER_LOADER_TYPE);
          grouperLoaderPriorityFromOwner = GrouperUtil.intObjectValue(
              GrouperLoaderType.attributeValueOrDefaultOrNull(group,
              GrouperLoader.GROUPER_LOADER_PRIORITY), true);
          
          //lets reset the job name in case the name has changed
          jobName = grouperLoaderTypeFromOwner + "__" + group.getName() + "__" + group.getUuid();
          hib3GrouploaderLog.setJobName(jobName);
          
        }
      }
      
      if (grouperLoaderType.equals(GrouperLoaderType.ATTR_SQL_SIMPLE)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
        
        if (uuidIndexStart >= 0) {
          String grouperLoaderAttrDefUuid = null;
          grouperLoaderAttrDefUuid = jobName.substring(uuidIndexStart+2, jobName.length());
          hib3GrouploaderLog.setGroupUuid(grouperLoaderAttrDefUuid);

          
          attributeDef = AttributeDefFinder.findById(grouperLoaderAttrDefUuid, true);
          
          grouperLoaderQuartzCronFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(attributeDef, 
              GrouperLoader.ATTRIBUTE_LOADER_QUARTZ_CRON);
          grouperLoaderScheduleTypeFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(attributeDef, 
              GrouperLoader.ATTRIBUTE_LOADER_SCHEDULE_TYPE);
          grouperLoaderTypeFromOwner = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(attributeDef, 
              GrouperLoader.ATTRIBUTE_LOADER_TYPE);
          grouperLoaderIntervalSecondsFromOwner = GrouperUtil.intObjectValue(
              GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(attributeDef,
              GrouperLoader.ATTRIBUTE_LOADER_INTERVAL_SECONDS), true);
          grouperLoaderPriorityFromOwner = GrouperUtil.intObjectValue(
              GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(attributeDef,
              GrouperLoader.ATTRIBUTE_LOADER_PRIORITY), true);
          
          //lets reset the job name in case the name has changed
          jobName = grouperLoaderTypeFromOwner + "__" + attributeDef.getName() + "__" + attributeDef.getId();
          hib3GrouploaderLog.setJobName(jobName);
          
        }
      }
      
      if (grouperLoaderType.equals(GrouperLoaderType.LDAP_SIMPLE)
          || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUP_LIST)
          || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES)) {
        
        int uuidIndexStart = jobName.lastIndexOf("__");
        
        if (uuidIndexStart >= 0) {
          String grouperLoaderGroupUuid = null;
          grouperLoaderGroupUuid = jobName.substring(uuidIndexStart+2, jobName.length());
          hib3GrouploaderLog.setGroupUuid(grouperLoaderGroupUuid);

          
          group = GroupFinder.findByUuid(grouperSession, grouperLoaderGroupUuid, true);
          
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
          
          AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(AttributeDef.ACTION_DEFAULT, attributeDefName, false, true);
          
          grouperLoaderQuartzCronFromOwner = attributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapQuartzCronName());
          
          grouperLoaderTypeFromOwner = attributeAssign.getAttributeValueDelegate().retrieveValueString(LoaderLdapUtils.grouperLoaderLdapTypeName());
          grouperLoaderPriorityFromOwner = GrouperUtil.intObjectValue(attributeAssign.getAttributeValueDelegate()
              .retrieveValueInteger(LoaderLdapUtils.grouperLoaderLdapPriorityName()), true);
          
          //lets reset the job name in case the name has changed
          jobName = grouperLoaderTypeFromOwner + "__" + group.getName() + "__" + group.getId();
          hib3GrouploaderLog.setJobName(jobName);
          
        }
      }
      
      //switch the job type?
      if (!StringUtils.isBlank(grouperLoaderTypeFromOwner)) {
        GrouperLoaderType grouperLoaderTypeFromGroupEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeFromOwner, true);
        if (!grouperLoaderTypeFromGroupEnum.equals(grouperLoaderType)) {
          LOG.debug("Grouper loader type has changed to " + grouperLoaderTypeFromGroupEnum
              + " from " + grouperLoaderType + ", for job: " + jobName);
          grouperLoaderType = grouperLoaderTypeFromGroupEnum;
        }
      }
      
      Trigger trigger = context.getTrigger();
      String grouperLoaderQuartzCron = null;
      String grouperLoaderScheduleType = null;
      if (trigger instanceof CronTrigger) {
        grouperLoaderQuartzCron = ((CronTrigger)trigger).getCronExpression();
        grouperLoaderScheduleType = GrouperLoaderScheduleType.CRON.name();
      }
      Integer grouperLoaderIntervalSeconds = null;
      if (trigger instanceof SimpleTrigger) {
        grouperLoaderIntervalSeconds = (int)(((SimpleTrigger)trigger).getRepeatInterval()/1000);
        grouperLoaderScheduleType = GrouperLoaderScheduleType.START_TO_START_INTERVAL.name();
      }
      
      boolean scheduleChange = false;
      
      if (!StringUtils.isBlank(grouperLoaderScheduleTypeFromOwner)
          && !StringUtils.equalsIgnoreCase(grouperLoaderScheduleTypeFromOwner, grouperLoaderScheduleType)) {
        LOG.warn("Detected a grouper loader schedule change in job: " + jobName 
            + ", scheduleType from: " + grouperLoaderScheduleTypeFromOwner + ", to: " + grouperLoaderScheduleType);

        scheduleChange = true;
      }
      if (!StringUtils.isBlank(grouperLoaderQuartzCronFromOwner)
          && !StringUtils.equals(grouperLoaderQuartzCronFromOwner, grouperLoaderQuartzCron)) {

        LOG.warn("Detected a grouper loader schedule change in job: " + jobName 
            + ", quartzCron from: " + grouperLoaderQuartzCronFromOwner + ", to: " + grouperLoaderQuartzCron);

        scheduleChange = true;
      }
      if (grouperLoaderIntervalSecondsFromOwner != null
          && !ObjectUtils.equals(grouperLoaderIntervalSecondsFromOwner, grouperLoaderIntervalSeconds)) {

        LOG.warn("Detected a grouper loader schedule change in job: " + jobName 
            + ", intervalSeconds from: " + grouperLoaderIntervalSecondsFromOwner + ", to: " + grouperLoaderIntervalSeconds);

        scheduleChange = true;
      }
      if (grouperLoaderPriorityFromOwner != null && 
          !ObjectUtils.equals(grouperLoaderPriorityFromOwner, trigger.getPriority())) {

        LOG.warn("Detected a grouper loader schedule change in job: " + jobName 
            + ", priority from: " + grouperLoaderPriorityFromOwner + ", to: " + trigger.getPriority());

        scheduleChange = true;
      }
      
      //see if the runtime settings have changed
      if (scheduleChange) {
        
        GrouperLoaderScheduleType grouperLoaderScheduleTypeEnumFromOwner = null;
        
        //if there is a cron string, then it must be cron
        if (StringUtils.isBlank(grouperLoaderScheduleTypeFromOwner) && !StringUtils.isBlank(grouperLoaderQuartzCronFromOwner)) {
          grouperLoaderScheduleTypeEnumFromOwner = GrouperLoaderScheduleType.CRON;
          
          //if it is an LDAP job, then it must be cron
        } else if (StringUtils.isBlank(grouperLoaderQuartzCronFromOwner) && (grouperLoaderType.equals(GrouperLoaderType.LDAP_SIMPLE)
            || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUP_LIST)
            || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES))) {
          grouperLoaderScheduleTypeEnumFromOwner = GrouperLoaderScheduleType.CRON;
          
          //else parse the schedule type, and it is required
        } else {
          grouperLoaderScheduleTypeEnumFromOwner = GrouperLoaderScheduleType
              .valueOfIgnoreCase(grouperLoaderScheduleTypeFromOwner, true);
        }
        
        if (grouperLoaderScheduleTypeEnumFromOwner.equals(GrouperLoaderScheduleType.START_TO_START_INTERVAL)) {
          if (grouperLoaderIntervalSecondsFromOwner == null) {
            grouperLoaderIntervalSecondsFromOwner = 60*60*24;
          }
        }
        if (grouperLoaderScheduleTypeEnumFromOwner.equals(GrouperLoaderScheduleType.CRON)) {
          if (StringUtils.isBlank(grouperLoaderQuartzCronFromOwner)) {
            throw new RuntimeException("Cron cant be blank if cron schedule: " + jobName);
          }
        }
      }
      
      if (grouperLoaderType != null) {
        hib3GrouploaderLog.setJobType(grouperLoaderType.name());
      }
      if (!StringUtils.isBlank(grouperLoaderScheduleTypeFromOwner)) {
        hib3GrouploaderLog.setJobScheduleType(grouperLoaderScheduleTypeFromOwner);
      }
      hib3GrouploaderLog.setJobScheduleIntervalSeconds(grouperLoaderIntervalSecondsFromOwner);
      
      if (grouperLoaderPriorityFromOwner != null || trigger != null) {
        
        hib3GrouploaderLog.setJobSchedulePriority(grouperLoaderPriorityFromOwner != null ? grouperLoaderPriorityFromOwner 
            : trigger.getPriority());
      }

      hib3GrouploaderLog.setJobScheduleQuartzCron(!StringUtils.isBlank(grouperLoaderQuartzCronFromOwner) ? 
          grouperLoaderQuartzCronFromOwner : grouperLoaderQuartzCron );
      
      if (scheduleChange) {
        LOG.warn("Detected a grouper loader schedule change in job: " + jobName + ", to: " 
            + grouperLoaderScheduleTypeFromOwner + ", cron: " + grouperLoaderQuartzCronFromOwner
            + ", interval: " + grouperLoaderIntervalSecondsFromOwner);
        
        GrouperLoaderType.scheduleJob(jobName, true, grouperLoaderScheduleTypeFromOwner, grouperLoaderQuartzCronFromOwner,
            grouperLoaderIntervalSecondsFromOwner, grouperLoaderPriorityFromOwner);
        
      }
      
      if (grouperLoaderType.equals(GrouperLoaderType.ATTR_SQL_SIMPLE)) {
        
        runJobAttrDef(hib3GrouploaderLog, attributeDef, grouperSession);
      } else if (grouperLoaderType.equals(GrouperLoaderType.LDAP_SIMPLE)
        || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUP_LIST)
        || grouperLoaderType.equals(GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES)) {
        
        runJobLdap(hib3GrouploaderLog, group, grouperSession);
      } else {
        //all other jobs go through here
        runJob(hib3GrouploaderLog, group, grouperSession);
      }
    } catch (Exception e) {
      LOG.error("Error running up job", e);
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      storeLogInDb(hib3GrouploaderLog, false, startTime);
      throw jobExecutionException;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * run a job (either from quartz or outside)
   * @param hib3GrouploaderLog will get information, most importantly the job name
   * @param jobGroup if a group job, this is the group object
   * @param grouperSession 
   */
  public static void runJob(Hib3GrouperLoaderLog hib3GrouploaderLog, Group jobGroup, GrouperSession grouperSession) {
    long startTime = System.currentTimeMillis();
    boolean throwExceptionsInFinally = true;
    String jobName = null;
    try {
      
      jobName = hib3GrouploaderLog.getJobName();
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.typeForThisName(jobName);
      
      List<Group> andGroups = new ArrayList<Group>();
  
      //find the groups whose membership we "and" with the dynamic group
      //CH 20110925: why is this not retrieved from the jobGroup???
      String grouperLoaderAndGroupNames = hib3GrouploaderLog.getAndGroupNames();
      if (!StringUtils.isBlank(grouperLoaderAndGroupNames)) {
        
        hib3GrouploaderLog.setAndGroupNames(grouperLoaderAndGroupNames);
        
        //there are groups to and with, get the list
        String[] groupNames = GrouperUtil.splitTrim(grouperLoaderAndGroupNames, ",");
        
        
        for (String groupName : groupNames) {
          Group group = GroupFinder.findByName(grouperSession, groupName, true);
          andGroups.add(group);
        }
      }
      
      //log that we are starting a job
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      
      hib3GrouploaderLog.store();
      
      String grouperLoaderDbName = "grouper";
      
      String grouperLoaderQuery = null;
      String groupName = null;
      
      String groupTypesString = null;
      String groupLikeString = null;
      String groupQuery = null;
      
      if (jobGroup != null) {
        grouperLoaderDbName = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DB_NAME);
        grouperLoaderQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUERY);
        groupTypesString = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_TYPES);
        groupLikeString = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE);
        groupQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_QUERY);
        groupName = jobGroup.getName();
        hib3GrouploaderLog.setGroupUuid(jobGroup.getUuid());
      }
      
      GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(grouperLoaderDbName);
      
      List<GroupType> groupTypes = null;
      if (!StringUtils.isBlank(groupTypesString)) {
        String[] groupTypeArray = GrouperUtil.splitTrim(groupTypesString, ",");
        groupTypes = new ArrayList<GroupType>();
        for (String groupType : groupTypeArray) {
          //this better find the type!
          groupTypes.add(GroupTypeFinder.find(groupType, true));
        }
      }
      
      LoaderJobBean loaderJobBean = new LoaderJobBean(grouperLoaderTypeEnum, groupName, grouperLoaderDb, grouperLoaderQuery, 
          hib3GrouploaderLog, grouperSession, andGroups, groupTypes, groupLikeString, groupQuery, startTime);
      
      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_PRE_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_PRE_RUN);
  
      //based on type, run query from the db and sync members
      grouperLoaderTypeEnum.runJob(loaderJobBean);
  
      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_POST_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_POST_RUN);
      
    } catch (Exception t) {
      LOG.error("Error on job: " + jobName, t);
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(t));
      throwExceptionsInFinally = false;
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t.getMessage(), t);
    } finally {
      
      storeLogInDb(hib3GrouploaderLog, throwExceptionsInFinally, startTime);
    }
    
  }

  /**
   * run a job (either from quartz or outside)
   * @param hib3GrouploaderLog will get information, most importantly the job name
   * @param jobGroup group that this ldap job is about
   * @param grouperSession 
   */
  public static void runJobLdap(Hib3GrouperLoaderLog hib3GrouploaderLog, Group jobGroup, GrouperSession grouperSession) {
    long startTime = System.currentTimeMillis();
    boolean throwExceptionsInFinally = true;
    String jobName = null;
    try {
      
      jobName = hib3GrouploaderLog.getJobName();
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.typeForThisName(jobName);
      
      //log that we are starting a job
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      
      hib3GrouploaderLog.store();
      
      String grouperLoaderLdapType = null;
      String grouperLoaderLdapServerId = null;
      String grouperLoaderLdapFilter = null;
      String grouperLoaderLdapSubjectAttribute = null;
      String grouperLoaderLdapSearchDn = null;
      String grouperLoaderLdapSourceId = null;
      String grouperLoaderLdapSubjectIdType = null;
      String grouperLoaderLdapSearchScope = null;
      String grouperLoaderLdapAndGroups = null;
      String grouperLoaderLdapGroupAttributeName = null;
      String grouperLoaderLdapAttributeFilterExpression = null;
      String grouperLoaderLdapExtraAttributes = null;
      String grouperLoaderLdapErrorUnresolvable = null;
      String grouperLoaderLdapGroupNameExpression = null;
      String grouperLoaderLdapGroupDisplayExtensionExpression = null;
      String grouperLoaderLdapGroupDescriptionExpression = null;
      String grouperLoaderLdapSubjectExpression = null;
      String groupTypesString = null;
      String grouperLoaderLdapGroupReaders = null;
      String grouperLoaderLdapGroupViewers = null;
      String grouperLoaderLdapGroupAdmins = null;
      String grouperLoaderLdapGroupUpdaters = null;
      String grouperLoaderLdapGroupOptins = null;
      String grouperLoaderLdapGroupOptouts = null;
      String grouperLoaderLdapGroupAttrReaders = null;
      String grouperLoaderLdapGroupAttrUpdaters = null;
      String grouperLoaderLdapGroupsLike = null;
      
      AttributeDefName grouperLoaderLdapTypeAttributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(LoaderLdapUtils.grouperLoaderLdapName(), false);
      AttributeAssign attributeAssign = grouperLoaderLdapTypeAttributeDefName == null ? null : 
        jobGroup.getAttributeDelegate().retrieveAssignment(
          null, grouperLoaderLdapTypeAttributeDefName, false, false);

      grouperLoaderLdapType = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapTypeName());
      grouperLoaderLdapServerId = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapServerIdName());
      grouperLoaderLdapFilter = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapFilterName());
      grouperLoaderLdapSubjectAttribute = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName());
      grouperLoaderLdapSearchDn = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSearchDnName());
      grouperLoaderLdapSourceId = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSourceIdName());
      grouperLoaderLdapSubjectIdType = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName());
      grouperLoaderLdapSearchScope = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSearchScopeName());
      grouperLoaderLdapAndGroups = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapAndGroupsName());
      grouperLoaderLdapGroupAttributeName = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupAttributeName());
      grouperLoaderLdapAttributeFilterExpression = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName());
      grouperLoaderLdapExtraAttributes = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapExtraAttributesName());
      grouperLoaderLdapErrorUnresolvable = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapErrorUnresolvableName());
      grouperLoaderLdapGroupNameExpression = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName());
      grouperLoaderLdapGroupDisplayExtensionExpression = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName());
      grouperLoaderLdapGroupDescriptionExpression = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName());
      grouperLoaderLdapSubjectExpression = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName());
      grouperLoaderLdapGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupsLikeName());
      groupTypesString = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupTypesName());
      grouperLoaderLdapGroupOptins = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapOptinsName());
      grouperLoaderLdapGroupOptouts = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapOptoutsName());
      grouperLoaderLdapGroupAttrReaders = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName());
      grouperLoaderLdapGroupAttrUpdaters = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName());
      grouperLoaderLdapGroupViewers = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapViewersName());
      grouperLoaderLdapGroupReaders = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapReadersName());
      grouperLoaderLdapGroupAdmins = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapAdminsName());
      grouperLoaderLdapGroupUpdaters = GrouperLoaderType.attributeValueOrDefaultOrNull(attributeAssign, LoaderLdapUtils.grouperLoaderLdapUpdatersName());
      
      String groupName = jobGroup.getName();
      hib3GrouploaderLog.setGroupUuid(jobGroup.getUuid());
      
      List<Group> andGroups = new ArrayList<Group>();
      
      //find the groups whose membership we "and" with the dynamic group
      if (!StringUtils.isBlank(grouperLoaderLdapAndGroups)) {
        
        hib3GrouploaderLog.setAndGroupNames(grouperLoaderLdapAndGroups);
        
        //there are groups to and with, get the list
        String[] groupNames = GrouperUtil.splitTrim(grouperLoaderLdapAndGroups, ",");
        
        for (String andGroupName : groupNames) {
          Group group = GroupFinder.findByName(grouperSession, andGroupName, true);
          andGroups.add(group);
        }
      }
      
      List<GroupType> groupTypes = null;
      if (!StringUtils.isBlank(groupTypesString)) {
        String[] groupTypeArray = GrouperUtil.splitTrim(groupTypesString, ",");
        groupTypes = new ArrayList<GroupType>();
        for (String groupType : groupTypeArray) {
          //this better find the type!
          groupTypes.add(GroupTypeFinder.find(groupType, true));
        }
      }

      LoaderJobBean loaderJobBean = new LoaderJobBean(grouperLoaderLdapType, grouperLoaderLdapServerId, 
          grouperLoaderLdapFilter, 
          grouperLoaderLdapSubjectAttribute, grouperLoaderLdapSearchDn, grouperLoaderLdapSourceId, 
          grouperLoaderLdapSubjectIdType, 
          grouperLoaderLdapSearchScope, startTime, grouperLoaderTypeEnum, groupName, hib3GrouploaderLog, 
          grouperSession, andGroups, grouperLoaderLdapGroupAttributeName, grouperLoaderLdapExtraAttributes,
          grouperLoaderLdapErrorUnresolvable, grouperLoaderLdapGroupNameExpression,
          grouperLoaderLdapGroupDisplayExtensionExpression, grouperLoaderLdapGroupDescriptionExpression,
          grouperLoaderLdapSubjectExpression, groupTypes, grouperLoaderLdapGroupReaders, 
          grouperLoaderLdapGroupViewers, grouperLoaderLdapGroupAdmins, grouperLoaderLdapGroupUpdaters, 
          grouperLoaderLdapGroupOptins, grouperLoaderLdapGroupOptouts, grouperLoaderLdapGroupsLike, 
          grouperLoaderLdapAttributeFilterExpression, grouperLoaderLdapGroupAttrReaders, 
          grouperLoaderLdapGroupAttrUpdaters);

      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_PRE_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_PRE_RUN);

      //based on type, run query from the db and sync members
      grouperLoaderTypeEnum.runJob(loaderJobBean);

      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_POST_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_POST_RUN);
      
    } catch (Exception t) {
      LOG.error("Error on job: " + jobName, t);
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(t));
      throwExceptionsInFinally = false;
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t.getMessage(), t);
    } finally {
      
      storeLogInDb(hib3GrouploaderLog, throwExceptionsInFinally, startTime);
    }
  }

  /**
   * run a job (either from quartz or outside)
   * @param hib3GrouploaderLog will get information, most importantly the job name
   * @param jobAttributeDef if a attributeDef job, this is the attributeDef object
   * @param grouperSession 
   */
  public static void runJobAttrDef(Hib3GrouperLoaderLog hib3GrouploaderLog, AttributeDef jobAttributeDef, GrouperSession grouperSession) {
    long startTime = System.currentTimeMillis();
    boolean throwExceptionsInFinally = true;
    String jobName = null;
    try {
      
      jobName = hib3GrouploaderLog.getJobName();
      
      GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.typeForThisName(jobName);
      
      //log that we are starting a job
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      
      hib3GrouploaderLog.store();
      
      String attributeLoaderDbName = "grouper";
      
      String attributeLoaderActionQuery = null;
      String attributeDefName = null;
      
      String attributeLoaderAttrsLike = null;
      String attributeLoaderActionSetQuery = null;
      String attributeLoaderAttrQuery = null;
      String attributeLoaderAttrSetQuery = null;
      
      //why are we checking for null?
      if (jobAttributeDef != null) {
        attributeLoaderActionQuery = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_ACTION_QUERY);
        attributeLoaderActionSetQuery = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_ACTION_SET_QUERY);
        attributeLoaderAttrQuery = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTR_QUERY);
        attributeLoaderAttrSetQuery = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTR_SET_QUERY);
        attributeLoaderAttrsLike = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_ATTRS_LIKE);
        attributeLoaderDbName = GrouperLoaderType.attributeValueOrDefaultOrNullAttrDef(jobAttributeDef, GrouperLoader.ATTRIBUTE_LOADER_DB_NAME);
        attributeDefName = jobAttributeDef.getName();
        hib3GrouploaderLog.setGroupUuid(jobAttributeDef.getUuid());
      }
      
      GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(attributeLoaderDbName);
            
      LoaderJobBean loaderJobBean = new LoaderJobBean(grouperLoaderTypeEnum, attributeDefName, grouperLoaderDb,  
          hib3GrouploaderLog, grouperSession, attributeLoaderAttrQuery, attributeLoaderAttrSetQuery, 
          attributeLoaderAttrsLike, attributeLoaderActionQuery, attributeLoaderActionSetQuery, startTime);
      
      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_PRE_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_PRE_RUN);

      //based on type, run query from the db and sync members
      grouperLoaderTypeEnum.runJob(loaderJobBean);

      //call hooks if registered
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LOADER, 
          LoaderHooks.METHOD_LOADER_POST_RUN, HooksLoaderBean.class, loaderJobBean, 
          LoaderJobBean.class, VetoTypeGrouper.LOADER_POST_RUN);
      
    } catch (Exception t) {
      LOG.error("Error on job: " + jobName, t);
      
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouploaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(t));
      throwExceptionsInFinally = false;
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t.getMessage(), t);
    } finally {
      
      storeLogInDb(hib3GrouploaderLog, throwExceptionsInFinally, startTime);
    }
    
  }

  /**
   * @param hib3GrouploaderLog
   * @param throwException 
   * @param startTime
   */
  private static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouploaderLog,
      boolean throwException, long startTime) {
    //store this safely
    try {
      
      long endTime = System.currentTimeMillis();
      hib3GrouploaderLog.setEndedTime(new Timestamp(endTime));
      hib3GrouploaderLog.setMillis((int)(endTime-startTime));
      
      hib3GrouploaderLog.store();
      
    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }
  
}
