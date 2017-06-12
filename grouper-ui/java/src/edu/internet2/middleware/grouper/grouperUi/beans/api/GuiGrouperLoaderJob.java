package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

/**
 * grouper loader job
 * @author mchyzer
 *
 */
public class GuiGrouperLoaderJob implements Serializable, Comparable<GuiGrouperLoaderJob> {

  /**
   * 
   */
  private static final long serialVersionUID = -5225233958485408940L;
  
  /**
   * job name of loader job
   */
  private String jobName;

  /**
   * job name of loader job
   * @return the job name
   */
  public String getJobName() {
    return this.jobName;
  }

  /**
   * job name of loader job
   * @param jobName1
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  /**
   * description of the source for a tooltip
   */
  private String sourceDescription;
  
  /**
   * description of the source for a tooltip
   * @return source description
   */
  public String getSourceDescription() {
    return this.sourceDescription;
  }

  /**
   * description of the source for a tooltip
   * @param sourceDescription1
   */
  public void setSourceDescription(String sourceDescription1) {
    this.sourceDescription = sourceDescription1;
  }

  /**
   * SQL query or LDAP filter
   */
  private String query;

  /**
   * SQL query or LDAP filter
   * @return
   */
  public String getQuery() {
    return query;
  }

  /**
   * SQL query or LDAP filter
   * @param query1
   */
  public void setQuery(String query1) {
    this.query = query1;
  }

  /**
   * which SQL or LDAP source this is hitting
   */
  private String source;
  
  /**
   * which SQL or LDAP source this is hitting
   * @return the source
   */
  public String getSource() {
    return this.source;
  }

  /**
   * which SQL or LDAP source this is hitting
   * @param source1
   */
  public void setSource(String source1) {
    this.source = source1;
  }

  /**
   * GrouperLoaderType string e.g. SQL_SIMPLE
   */
  private String type;
  
  /**
   * GrouperLoaderType string e.g. SQL_SIMPLE
   * @return type
   */
  public String getType() {
    return this.type;
  }

  /**
   * GrouperLoaderType string e.g. SQL_SIMPLE
   * @param type1
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * number of changes in the last run (or recent runs)
   */
  private int changes = -1;
  
  /**
   * number of changes in the last run (or recent runs)
   * @return changes
   */
  public int getChanges() {
    return this.changes;
  }

  /**
   * number of changes in the last run (or recent runs)
   * @param changes1
   */
  public void setChanges(int changes1) {
    this.changes = changes1;
  }

  /**
   * number of memberships this job manages
   */
  private int count = -1;
  
  /**
   * number of memberships this job manages
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * number of memberships this job manages
   * @param count1
   */
  public void setCount(int count1) {
    this.count = count1;
  }

  /**
   * description of the status, for more info
   * e.g. Found a success on 2017/01/01 8:09:10 in grouper_loader_log for job name: someJob which is within the threshold of 60 minutes
   */
  private String statusDescription;
  
  /**
   * description of the status, for more info
   * e.g. Found a success on 2017/01/01 8:09:10 in grouper_loader_log for job name: someJob which is within the threshold of 60 minutes
   * @return the description
   */
  public String getStatusDescription() {
    return this.statusDescription;
  }

  /**
   * description of the status, for more info
   * e.g. Found a success on 2017/01/01 8:09:10 in grouper_loader_log for job name: someJob which is within the threshold of 60 minutes
   * @param statusDescription1
   */
  public void setStatusDescription(String statusDescription1) {
    this.statusDescription = statusDescription1;
  }

  /**
   * SUCCESS, ERROR, WARNING
   */
  private String status;

  /**
   * SUCCESS, ERROR, WARNING
   * @return status
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * SUCCESS, ERROR, WARNING
   * @param status1
   */
  public void setStatus(String status1) {
    this.status = status1;
  }

  /**
   * gui group where this job is defined
   */
  private GuiGroup guiGroup;

  /**
   * gui group where this job is defined
   * @return gui group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * gui group where this job is defined
   * @param guiGroup1
   */
  public void setGuiGroup(GuiGroup guiGroup1) {
    this.guiGroup = guiGroup1;
  }

  /**
   * to show an odered list of jobs
   */
  @Override
  public int compareTo(GuiGrouperLoaderJob o) {
    if (o==null) {
      return -1;
    }
    if (this.guiGroup == o.guiGroup) {
      return 0;
    }
    if (o.guiGroup == null) {
      return -1;
    }
    if (this.guiGroup == null) {
      return 1;
    }
    return this.guiGroup.getGroup().compareTo(o.guiGroup.getGroup());
  }

  
  
}
