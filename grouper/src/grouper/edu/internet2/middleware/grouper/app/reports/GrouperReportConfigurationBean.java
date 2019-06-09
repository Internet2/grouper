/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 *
 */
public class GrouperReportConfigurationBean {

  /**
   * 
   */
  public GrouperReportConfigurationBean() {
  }

  /**
   * report type e.g. SQL
   */
  private ReportConfigType reportConfigType;

  
  /**
   * report type e.g. SQL
   * @return the reportConfigType
   */
  public ReportConfigType getReportConfigType() {
    return this.reportConfigType;
  }

  
  /**
   * report type e.g. SQL
   * @param reportConfigType1 the reportConfigType to set
   */
  public void setReportConfigType(ReportConfigType reportConfigType1) {
    this.reportConfigType = reportConfigType1;
  }

  /**
   * format of report e.g. CSV
   */
  private ReportConfigFormat reportConfigFormat;

  /**
   * format of report e.g. CSV
   * @return the reportConfigFormat
   */
  public ReportConfigFormat getReportConfigFormat() {
    return this.reportConfigFormat;
  }
  
  /**
   * format of report e.g. CSV
   * @param reportConfigFormat1 the reportConfigFormat to set
   */
  public void setReportConfigFormat(ReportConfigFormat reportConfigFormat1) {
    this.reportConfigFormat = reportConfigFormat1;
  }

  /**
   * attribute assignment id of the marker attribute on the group/folder
   */
  private String attributeAssignmentMarkerId;
  
  /**
   * attribute assignment id of the marker attribute on the group/folder
   * @return the attributeAssignmentMarkerId
   */
  public String getAttributeAssignmentMarkerId() {
    return this.attributeAssignmentMarkerId;
  }
  
  /**
   * attribute assignment id of the marker attribute on the group/folder
   * @param attributeAssignmentMarkerId1 the attributeAssignmentMarkerId to set
   */
  public void setAttributeAssignmentMarkerId(String attributeAssignmentMarkerId1) {
    this.attributeAssignmentMarkerId = attributeAssignmentMarkerId1;
  }

  /**
   * Name of report. No two reports in the same owner should have the same name
   */
  private String reportConfigName;
  
  /**
   * @return the reportConfigName
   */
  public String getReportConfigName() {
    return this.reportConfigName;
  }
  
  /**
   * @param reportConfigName the reportConfigName to set
   */
  public void setReportConfigName(String reportConfigName) {
    this.reportConfigName = reportConfigName;
  }
  
  /**
   * e.g. usersOfMyService_$$timestamp$$.csv
   * $$timestamp$$ translates to current time in this format: yyyy_mm_dd_hh24_mi_ss
   */
  private String reportConfigFilename;

  /**
   * e.g. usersOfMyService_$$timestamp$$.csv
   * $$timestamp$$ translates to current time in this format: yyyy_mm_dd_hh24_mi_ss
   * @return the reportConfigFilename
   */
  public String getReportConfigFilename() {
    return this.reportConfigFilename;
  }
  
  /**
   * @param reportConfigFilename1 the reportConfigFilename to set
   */
  public void setReportConfigFilename(String reportConfigFilename1) {
    this.reportConfigFilename = reportConfigFilename1;
  }

  /**
   * reportConfigValueDef  SQL for the report. The columns must be named in the SQL (e.g. not select *) and generally this comes from a view
   */
  private String reportConfigQuery;
  
  /**
   * reportConfigValueDef  SQL for the report. The columns must be named in the SQL (e.g. not select *) and generally this comes from a view
   * @return the reportConfigQuery
   */
  public String getReportConfigQuery() {
    return this.reportConfigQuery;
  }
  
  /**
   * reportConfigValueDef  SQL for the report. The columns must be named in the SQL (e.g. not select *) and generally this comes from a view
   * @param reportConfigQuery the reportConfigQuery to set
   */
  public void setReportConfigQuery(String reportConfigQuery) {
    this.reportConfigQuery = reportConfigQuery;
  }
  
  /**
   * description about the report
   */
  private String reportConfigDescription;


  /**
   * description about the report
   * @return reportConfigDescription
   */
  public String getReportConfigDescription() {
    return this.reportConfigDescription;
  }


  /**
   * description about the report
   * @param reportConfigDescription
   */
  public void setReportConfigDescription(String reportConfigDescription) {
    this.reportConfigDescription = reportConfigDescription;
  }
  
  
  /**
   * GroupId of people who can view this report. Grouper admins can view any report (blank means admin only)
   */
  private String reportConfigViewersGroupId;


  /**
   * GroupId of people who can view this report. Grouper admins can view any report (blank means admin only)
   * @return reportConfigViewersGroupId
   */
  public String getReportConfigViewersGroupId() {
    return this.reportConfigViewersGroupId;
  }


  /**
   * GroupId of people who can view this report. Grouper admins can view any report (blank means admin only)
   * @param reportConfigViewersGroupId
   */
  public void setReportConfigViewersGroupId(String reportConfigViewersGroupId) {
    this.reportConfigViewersGroupId = reportConfigViewersGroupId;
  }
  
  /**
   * Quartz cron-like schedule to generate the report
   */
  private String reportConfigQuartzCron;


  /**
   * Quartz cron-like schedule to generate the report
   * @return reportConfigQuartzCron
   */
  public String getReportConfigQuartzCron() {
    return this.reportConfigQuartzCron;
  }


  /**
   * Quartz cron-like schedule to generate the report
   * @param reportConfigQuartzCron
   */
  public void setReportConfigQuartzCron(String reportConfigQuartzCron) {
    this.reportConfigQuartzCron = reportConfigQuartzCron;
  }
  
  /**
   * true/false if email should be sent
   */
  private boolean reportConfigSendEmail = true;


  /**
   * true/false if email should be sent
   * @return reportConfigSendEmail
   */
  public boolean isReportConfigSendEmail() {
    return this.reportConfigSendEmail;
  }


  /**
   * true/false if email should be sent
   * @param reportConfigSendEmail
   */
  public void setReportConfigSendEmail(boolean reportConfigSendEmail) {
    this.reportConfigSendEmail = reportConfigSendEmail;
  }
  
  /**
   * subject for email (optional, will be generated from report name if blank)
   */
  private String reportConfigEmailSubject;


  /**
   * subject for email (optional, will be generated from report name if blank)
   * @return reportConfigEmailSubject
   */
  public String getReportConfigEmailSubject() {
    return this.reportConfigEmailSubject;
  }


  /**
   * subject for email (optional, will be generated from report name if blank)
   * @param reportConfigEmailSubject
   */
  public void setReportConfigEmailSubject(String reportConfigEmailSubject) {
    this.reportConfigEmailSubject = reportConfigEmailSubject;
  }
  
  
  /**
   * optional, will be generated by a grouper default if blank
   */
  private String reportConfigEmailBody;


  /**
   * optional, will be generated by a grouper default if blank
   * @return reportConfigEmailBody
   */
  public String getReportConfigEmailBody() {
    return this.reportConfigEmailBody;
  }

  /**
   * optional, will be generated by a grouper default if blank
   * @param reportConfigEmailBody
   */
  public void setReportConfigEmailBody(String reportConfigEmailBody) {
    this.reportConfigEmailBody = reportConfigEmailBody;
  }
  
  
  /**
   * true/false if report viewers should get email (if reportConfigSendEmail is true)
   */
  private boolean reportConfigSendEmailToViewers;


  /**
   * true/false if report viewers should get email (if reportConfigSendEmail is true)
   * @return reportConfigSendEmailToViewers
   */
  public boolean isReportConfigSendEmailToViewers() {
    return this.reportConfigSendEmailToViewers;
  }


  /**
   * true/false if report viewers should get email (if reportConfigSendEmail is true)
   * @param reportConfigSendEmailToViewers
   */
  public void setReportConfigSendEmailToViewers(boolean reportConfigSendEmailToViewers) {
    this.reportConfigSendEmailToViewers = reportConfigSendEmailToViewers;
  }
  
  
  /**
   * if reportConfigSendEmail is true, and reportConfigSendEmailToViewers is false), this is the groupId where members are retrieved from
   */
  private String reportConfigSendEmailToGroupId;


  /**
   * if reportConfigSendEmail is true, and reportConfigSendEmailToViewers is false), this is the groupId where members are retrieved from
   * @return reportConfigSendEmailToGroupId
   */
  public String getReportConfigSendEmailToGroupId() {
    return this.reportConfigSendEmailToGroupId;
  }


  /**
   * if reportConfigSendEmail is true, and reportConfigSendEmailToViewers is false), this is the groupId where members are retrieved from
   * @param reportConfigSendEmailToGroupId
   */
  public void setReportConfigSendEmailToGroupId(String reportConfigSendEmailToGroupId) {
    this.reportConfigSendEmailToGroupId = reportConfigSendEmailToGroupId;
  }
  
  
  /**
   * should the config be enabled
   */
  private boolean reportConfigEnabled = true;


  /**
   * should the config be enabled
   * @return reportConfigEnabled
   */
  public boolean isReportConfigEnabled() {
    return this.reportConfigEnabled;
  }


  /**
   * should the config be enabled
   * @param reportConfigEnabled
   */
  public void setReportConfigEnabled(boolean reportConfigEnabled) {
    this.reportConfigEnabled = reportConfigEnabled;
  }
  
  /**
   * can given subject read this report config
   * @param subject
   * @return
   */
  public boolean isCanRead(Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    String groupId = this.getReportConfigViewersGroupId();
    GrouperSession rootSession = GrouperSession.startRootSession();
    Group group = GroupFinder.findByUuid(rootSession, groupId, false);
    if (group == null) {
      return false;
    }
    
    Subject everyEntitySubject = SubjectFinder.findAllSubject();
    Member everyEntityMember = MemberFinder.findBySubject(rootSession, everyEntitySubject, false);
    if (group.getMembers().contains(everyEntityMember)) {
      return true;
    }
    
    Member member = MemberFinder.findBySubject(rootSession, subject, false);
    if (group.getMembers().contains(member)) {
      return true;
    }
    
    return false;
  }
  
  
}
