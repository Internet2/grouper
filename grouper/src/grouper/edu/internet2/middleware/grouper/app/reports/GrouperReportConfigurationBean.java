/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;


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

  
  
}
