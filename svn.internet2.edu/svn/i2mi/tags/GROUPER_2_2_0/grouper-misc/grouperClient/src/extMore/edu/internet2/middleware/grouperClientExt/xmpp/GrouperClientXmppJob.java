/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * bean that holds config data for grouper client xmpp job
 */
public class GrouperClientXmppJob {
  
  /** if we should allow incremental not in group names list */
  private boolean allowIncrementalNotInGroupNamesList = false;
  
  /**
   * if we should allow incremental not in group names list
   * @return if we should allow incremental not in group names list
   */
  public boolean isAllowIncrementalNotInGroupNamesList() {
    return this.allowIncrementalNotInGroupNamesList;
  }

  /**
   * if we should allow incremental not in group names list
   * @param allowIncrementalNotInGroupNamesList1
   */
  public void setAllowIncrementalNotInGroupNamesList(
      boolean allowIncrementalNotInGroupNamesList1) {
    this.allowIncrementalNotInGroupNamesList = allowIncrementalNotInGroupNamesList1;
  }

  /** cached xmpp jobs */
  private static List<GrouperClientXmppJob> xmppJobs = null;
  
  
  /**
   * name of job
   * @return the jobName
   */
  public String getJobName() {
    return this.jobName;
  }

  /**
   * name of job
   * @param jobName1 the jobName to set
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  /** event action on each event */
  private XmppJobEventAction eventAction = null;
  
  /**
   * event action on each event
   * @return the eventAction
   */
  public XmppJobEventAction getEventAction() {
    return this.eventAction;
  }
  
  /**
   * event action on each event
   * @param eventAction1 the eventAction to set
   */
  public void setEventAction(XmppJobEventAction eventAction1) {
    this.eventAction = eventAction1;
  }

  /**
   * retrieve a job by name
   * @param name
   * @param exceptionIfNotFound 
   * @return the job
   */
  public static GrouperClientXmppJob retrieveJob(String name, boolean exceptionIfNotFound) {
    for (GrouperClientXmppJob grouperClientXmppJob : retrieveXmppJobs()) {
      if (GrouperClientUtils.equals(name, grouperClientXmppJob.getJobName())) {
        return grouperClientXmppJob;
      }
    }
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find job with name: " + name);
    }
    return null;
  }

  /**
   * retrieve the cached xmpp jobs
   * @return jobs
   */
  public static List<GrouperClientXmppJob> retrieveXmppJobs() {
    if (xmppJobs == null) {
      
      List<GrouperClientXmppJob> theXmppJobs = new ArrayList<GrouperClientXmppJob>();
      
      Pattern pattern = Pattern.compile("^grouperClient\\.xmpp\\.job\\.(.+)\\.handlerClass$");
      
      //note, the overrides arent included here...
      Properties properties = GrouperClientConfig.retrieveConfig().properties();
      for (Object keyObject : properties.keySet()) {
        String key = (String)keyObject;
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
          String jobName = matcher.group(1);
          GrouperClientXmppJob xmppJob = new GrouperClientXmppJob();
          xmppJob.setJobName(jobName);
          {
            String handlerClass = (String)properties.get(key);
            xmppJob.setHandlerClass(handlerClass);
          }
          
          xmppJob.setAllowIncrementalNotInGroupNamesList(GrouperClientUtils.propertiesValueBoolean(properties, 
              "grouperClient.xmpp.job." + jobName + ".allowIncrementalNotInGroupNamesList", false));
          
          {
            String groupNamesString = (String)properties.get("grouperClient.xmpp.job." + jobName + ".groupNames");
            if (!GrouperClientUtils.isBlank(groupNamesString)) {
              Set<String> groupNamesSet = new HashSet<String>(GrouperClientUtils.splitTrimToList(groupNamesString, ","));
              xmppJob.setGroupNames(groupNamesSet);
            }
          }
          {
            String subjectAttributeNamesString = (String)properties.get("grouperClient.xmpp.job." + jobName + ".subjectAttributeNames");
            if (!GrouperClientUtils.isBlank(subjectAttributeNamesString)) {
              List<String> subjectAttributeNamesList = GrouperClientUtils.splitTrimToList(subjectAttributeNamesString, ",");
              xmppJob.setSubjectAttributeNames(subjectAttributeNamesList);
            }
          }
          {
            String elfilterString = (String)properties.get("grouperClient.xmpp.job." + jobName + ".elfilter");
            if (!GrouperClientUtils.isBlank(elfilterString)) {
              xmppJob.setElfilter(elfilterString);
            }
          }
          {
            //default to incremental
            String eventActionString = (String)properties.get("grouperClient.xmpp.job." + jobName + ".eventAction");
            eventActionString = GrouperClientUtils.defaultIfBlank(eventActionString, XmppJobEventAction.incremental.name());
            xmppJob.setEventAction(XmppJobEventAction.valueOf(eventActionString));
          }
          {
            //default to incremental
            String fullRefreshQuartzCronString = (String)properties.get("grouperClient.xmpp.job." + jobName + ".fullRefreshQuartzCronString");
            
            //lets do full refresh by default once per day sometime between 4 and 8 am.
            int minutes = new Random().nextInt(60);
            int hours = 4 + new Random().nextInt(4);
            
            fullRefreshQuartzCronString = GrouperClientUtils.defaultIfBlank(
                fullRefreshQuartzCronString, "0 " + minutes + " " + hours + " * * ?");
            xmppJob.setFullRefreshQuartzCronString(fullRefreshQuartzCronString);
          }
          {
            //only needed for file handler              grouperClient.xmpp.job.myJobName.fileHandler.targetFile
            String targetFile = (String)properties.get("grouperClient.xmpp.job." + jobName + ".fileHandler.targetFile");
            
            xmppJob.setTargetFile(targetFile);
          }
          {
            //only needed for file handler
            String filePrefix = (String)properties.get("grouperClient.xmpp.job." + jobName + ".fileHandler.filePrefix");
            
            xmppJob.setFilePrefix(filePrefix);
          }
          {
            //only needed for file handler
            String iteratorEl = (String)properties.get("grouperClient.xmpp.job." + jobName + ".fileHandler.iteratorEl");
            
            xmppJob.setIteratorEl(iteratorEl);
          }
          {
            //only needed for file handler
            String fileSuffix = (String)properties.get("grouperClient.xmpp.job." + jobName + ".fileHandler.fileSuffix");
            
            xmppJob.setFileSuffix(fileSuffix);
          }
          theXmppJobs.add(xmppJob);
        }
      }
      
      xmppJobs = theXmppJobs;
      
    }
    return xmppJobs;
  }
  
  /** subject attribute names for full refresh */
  private List<String> subjectAttributeNames = null;

  /**
   * subject attribute names for full refresh
   * @return the subjectAttributeNames
   */
  public List<String> getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * subject attribute names for full refresh
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(List<String> subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * group names which trigger notifications
   */
  private Set<String> groupNames = null;

  /**
   * group names which trigger notifications
   * @return group names which trigger notifications
   */
  public Set<String> getGroupNames() {
    return groupNames;
  }

  /**
   * group names which trigger notifications
   * @param groupNames
   */
  public void setGroupNames(Set<String> groupNames) {
    this.groupNames = groupNames;
  }

  /** name of job */
  private String jobName = null;
  
  /** class that handles events on this job */
  private String handlerClass = null;
  
  /** enum for action when event happens */
  public static enum XmppJobEventAction {

    /** incremental will use the message and memory list */
    incremental,
    
    /** when a message comes in, reload the membership list */
    reload_group;
  }

  /** how often a full refresh should occur regardless of events, quartz
   * cron like string, e.g. on the 8am each day: 0 0 8 * * ? */
  private String fullRefreshQuartzCronString;

  /** file to write for file handler jobs */
  private String targetFile;

  /** prefix to put at the beginning of file (before users) */
  private String filePrefix;
  
  /** expression language for each user, e.g.  ${subject['pennname']}$space$
   * note: $newline$ is also ok for new lines.  */
  private String iteratorEl;
  
  /** elfilter that decides if the event is worth processsing */
  private String elfilter;
  
  /**
   * elfilter that decides if the event is worth processsing
   * @return elfilter
   */
  public String getElfilter() {
    return elfilter;
  }

  /**
   * elfilter that decides if the event is worth processsing
   * @param elfilter1
   */
  public void setElfilter(String elfilter1) {
    this.elfilter = elfilter1;
  }

  /** suffix to put at the end of file (after users) */
  private String fileSuffix;

  /**
   * class that handles events on this job
   * @return the handlerClass
   */
  public String getHandlerClass() {
    return this.handlerClass;
  }

  /**
   * class that handles events on this job
   * @param handlerClass1 the handlerClass to set
   */
  public void setHandlerClass(String handlerClass1) {
    this.handlerClass = handlerClass1;
  }
  
  /**
   * how often a full refresh should occur regardless of events, quartz
   * cron like string, e.g. on the 8am each day: 0 0 8 * * ?
   * @return the fullRefreshQuartzCronString
   */
  public String getFullRefreshQuartzCronString() {
    return this.fullRefreshQuartzCronString;
  }



  
  /**
   * how often a full refresh should occur regardless of events, quartz
   * cron like string, e.g. on the 8am each day: 0 0 8 * * ?
   * @param fullRefreshQuartzCronString1 the fullRefreshQuartzCronString to set
   */
  public void setFullRefreshQuartzCronString(String fullRefreshQuartzCronString1) {
    this.fullRefreshQuartzCronString = fullRefreshQuartzCronString1;
  }



  
  /**
   * file to write for file handler jobs
   * @return the targetFile
   */
  public String getTargetFile() {
    return this.targetFile;
  }



  
  /**
   * file to write for file handler jobs
   * @param targetFile1 the targetFile to set
   */
  public void setTargetFile(String targetFile1) {
    this.targetFile = targetFile1;
  }



  
  /**
   * prefix to put at the beginning of file (before users)
   * @return the filePrefix
   */
  public String getFilePrefix() {
    return this.filePrefix;
  }



  
  /**
   * prefix to put at the beginning of file (before users)
   * @param filePrefix1 the filePrefix to set
   */
  public void setFilePrefix(String filePrefix1) {
    this.filePrefix = filePrefix1;
  }



  
  /**
   * expression language for each user, e.g.  ${subject['pennname']}$space$
   * note: $newline$ is also ok for new lines.
   * @return the iteratorEl
   */
  public String getIteratorEl() {
    return this.iteratorEl;
  }



  
  /**
   * expression language for each user, e.g.  ${subject['pennname']}$space$
   * note: $newline$ is also ok for new lines.
   * @param iteratorEl1 the iteratorEl to set
   */
  public void setIteratorEl(String iteratorEl1) {
    this.iteratorEl = iteratorEl1;
  }



  
  /**
   * suffix to put at the end of file (after users)
   * @return the fileSuffix
   */
  public String getFileSuffix() {
    return this.fileSuffix;
  }



  
  /**
   * suffix to put at the end of file (after users)
   * @param fileSuffix1 the fileSuffix to set
   */
  public void setFileSuffix(String fileSuffix1) {
    this.fileSuffix = fileSuffix1;
  }
}
