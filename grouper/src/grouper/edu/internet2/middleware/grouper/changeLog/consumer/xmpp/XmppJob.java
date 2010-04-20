/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog.consumer.xmpp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean that holds config data for xmpp job
 */
public class XmppJob {
  
  /** cached xmpp jobs */
  private static List<XmppJob> xmppJobs = null;
  
  /** name of job */
  private String jobName = null;
  
  
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


  /**
   * retrieve the cached xmpp jobs
   * @return jobs
   */
  public static List<XmppJob> retrieveXmppJobs() {
    if (xmppJobs == null) {
      List<XmppJob> theXmppJobs = new ArrayList<XmppJob>();
      
      Pattern pattern = Pattern.compile("^xmpp\\.job\\.(.+)\\.sendToXmppJabberIds$");
      
      Properties properties = GrouperLoaderConfig.properties();
      for (Object keyObject : properties.keySet()) {
        String key = (String)keyObject;
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
          String jobName = matcher.group(1);
          XmppJob xmppJob = new XmppJob();
          xmppJob.setJobName(jobName);
          {
            String jabberIdsString = (String)properties.get(key);
            Set<String> jabberIdsSet = GrouperUtil.splitTrimToSet(jabberIdsString, ",");
            xmppJob.setSendToXmppJabberIds(jabberIdsSet);
          }          
          {
            String groupNamesString = (String)properties.get("xmpp.job." + jobName + ".groupNames");
            if (!StringUtils.isBlank(groupNamesString)) {
              Set<String> groupNamesSet = GrouperUtil.splitTrimToSet(groupNamesString, ",");
              xmppJob.setGroupNames(groupNamesSet);
            }
          }
          {
            String groupRegex = (String)properties.get("xmpp.job." + jobName + ".groupRegex");
            if (!StringUtils.isBlank(groupRegex)) {
              xmppJob.setGroupRegex(groupRegex);
            }
          }
          {
            String subjectAttributeNamesString = (String)properties.get("xmpp.job." + jobName + ".subjectAttributeNames");
            if (!StringUtils.isBlank(subjectAttributeNamesString)) {
              String[] subjectAttributeNames = GrouperUtil.splitTrim(subjectAttributeNamesString, ",");
              xmppJob.setSubjectAttributeNames(subjectAttributeNames);
            }
          }
          theXmppJobs.add(xmppJob);
        }
      }
      
      xmppJobs = theXmppJobs;
      
    }
    return xmppJobs;
  }
  
  /** where to send notifications */
  private Set<String> sendToXmppJabberIds = null;
  
  /** subject attribute names to send to xmpp */
  private String[] subjectAttributeNames = null;
  
  /** regex for group names to match */
  private String groupRegex = null;
  
  /** keep a regex pattern here so we dont have to compile it so often */
  private Pattern pattern = null;
  
  /**
   * group names which trigger notifications
   */
  private Set<String> groupNames = null;

  
  /**
   * where to send notifications
   * @return the sentToXmppJabberIds
   */
  public Set<String> getSendToXmppJabberIds() {
    return this.sendToXmppJabberIds;
  }

  
  /**
   * where to send notifications
   * @param sentToXmppJabberIds1 the sentToXmppJabberIds to set
   */
  public void setSendToXmppJabberIds(Set<String> sentToXmppJabberIds1) {
    this.sendToXmppJabberIds = sentToXmppJabberIds1;
  }

  
  /**
   * subject attribute names to send to xmpp
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  
  /**
   * subject attribute names to send to xmpp
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  
  /**
   * lazy load the regex for group names
   * @return the pattern
   */
  public Pattern retrievePattern() {
    if (this.pattern == null && !StringUtils.isBlank(this.groupRegex)) {
      this.pattern = Pattern.compile(this.groupRegex);
    }
    return this.pattern;
  }
  
  /**
   * regex for group names to match
   * @return the groupRegex
   */
  public String getGroupRegex() {
    return this.groupRegex;
  }


  
  /**
   * regex for group names to match
   * @param groupRegex1 the groupRegex to set
   */
  public void setGroupRegex(String groupRegex1) {
    this.groupRegex = groupRegex1;
  }


  /**
   * group names which trigger notifications
   * @return the groupNames
   */
  public Set<String> getGroupNames() {
    return this.groupNames;
  }

  
  /**
   * group names which trigger notifications
   * @param groupNames1 the groupNames to set
   */
  public void setGroupNames(Set<String> groupNames1) {
    this.groupNames = groupNames1;
  }
}
