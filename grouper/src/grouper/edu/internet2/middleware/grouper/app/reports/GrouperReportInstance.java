/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperReportInstance {
  
  public static final String STATUS_SUCCESS = "SUCCESS";
  public static final String STATUS_ERROR = "ERROR";

  /**
   * 
   */
  public GrouperReportInstance() {
  }

  /**
   * grouper report configuration
   */
  private GrouperReportConfigurationBean grouperReportConfigurationBean;
  
  /**
   * grouper report configuration
   * @return the grouperReportConfigurationBean
   */
  public GrouperReportConfigurationBean getGrouperReportConfigurationBean() {
    return this.grouperReportConfigurationBean;
  }
  
  /**
   * grouper report configuration
   * @param grouperReportConfigurationBean the grouperReportConfigurationBean to set
   */
  public void setGrouperReportConfigurationBean(
      GrouperReportConfigurationBean grouperReportConfigurationBean) {
    this.grouperReportConfigurationBean = grouperReportConfigurationBean;
  }

  /**
   * if not set will default to now
   */
  private long reportDateMillis = System.currentTimeMillis();

  
  /**
   * if not set will default to now
   * @return the reportDateMillis
   */
  public long getReportDateMillis() {
    return this.reportDateMillis;
  }

  
  /**
   * if not set will default to now
   * @param reportDateMillis1 the reportDateMillis to set
   */
  public void setReportDateMillis(long reportDateMillis1) {
    this.reportDateMillis = reportDateMillis1;
  }

  /**
   * file where unencrypted report is
   */
  private File reportFileUnencrypted;


  
  /**
   * file where unencrypted report is
   * @return the reportFileUnencrypted
   */
  public File getReportFileUnencrypted() {
    return this.reportFileUnencrypted;
  }


  
  /**
   * file where unencrypted report is
   * @param reportFileUnencrypted the reportFileUnencrypted to set
   */
  public void setReportFileUnencrypted(File reportFileUnencrypted) {
    this.reportFileUnencrypted = reportFileUnencrypted;
  }
  
  private String reportInstanceStatus;
  
  private long reportElapsedMillis;
  
  private String reportInstanceConfigMarkerAssignmentId;
  
  private long reportInstanceMillisSince1970;
  
  private long reportInstanceSizeBytes;
  
  private String reportInstanceFilePointer;
  
  private long reportInstanceDownloadCount;
  
  private String reportInstanceEncryptionKey;
  
  private long reportInstanceRows;
  
  private String reportInstanceEmailToSubjects;
  
  private String reportInstanceEmailToSubjectsError;
  
  private String reportInstanceFileName;
  
  private String attributeAssignId;
  
  
  public String getReportInstanceStatus() {
    return reportInstanceStatus;
  }

  
  public void setReportInstanceStatus(String reportInstanceStatus) {
    this.reportInstanceStatus = reportInstanceStatus;
  }

  
  public long getReportElapsedMillis() {
    return reportElapsedMillis;
  }

  
  public void setReportElapsedMillis(long reportElapsedMillis) {
    this.reportElapsedMillis = reportElapsedMillis;
  }

  
  public String getReportInstanceConfigMarkerAssignmentId() {
    return reportInstanceConfigMarkerAssignmentId;
  }

  
  public void setReportInstanceConfigMarkerAssignmentId(
      String reportInstanceConfigMarkerAssignmentId) {
    this.reportInstanceConfigMarkerAssignmentId = reportInstanceConfigMarkerAssignmentId;
  }

  
  public long getReportInstanceMillisSince1970() {
    return reportInstanceMillisSince1970;
  }

  
  public void setReportInstanceMillisSince1970(long reportInstanceMillisSince1970) {
    this.reportInstanceMillisSince1970 = reportInstanceMillisSince1970;
  }

  
  public long getReportInstanceSizeBytes() {
    return reportInstanceSizeBytes;
  }

  
  public void setReportInstanceSizeBytes(long reportInstanceSizeBytes) {
    this.reportInstanceSizeBytes = reportInstanceSizeBytes;
  }

  
  public String getReportInstanceFilePointer() {
    return reportInstanceFilePointer;
  }

  
  public void setReportInstanceFilePointer(String reportInstanceFilePointer) {
    this.reportInstanceFilePointer = reportInstanceFilePointer;
  }

  
  public long getReportInstanceDownloadCount() {
    return reportInstanceDownloadCount;
  }

  
  public void setReportInstanceDownloadCount(long reportInstanceDownloadCount) {
    this.reportInstanceDownloadCount = reportInstanceDownloadCount;
  }
  
  public String getReportInstanceEncryptionKey() {
    return reportInstanceEncryptionKey;
  }

  public void setReportInstanceEncryptionKey(String reportInstanceEncryptionKey) {
    this.reportInstanceEncryptionKey = reportInstanceEncryptionKey;
  }

  
  public long getReportInstanceRows() {
    return reportInstanceRows;
  }

  
  public void setReportInstanceRows(long reportInstanceRows) {
    this.reportInstanceRows = reportInstanceRows;
  }

  
  public String getReportInstanceEmailToSubjects() {
    return reportInstanceEmailToSubjects;
  }

  
  public void setReportInstanceEmailToSubjects(String reportInstanceEmailToSubjects) {
    this.reportInstanceEmailToSubjects = reportInstanceEmailToSubjects;
  }

  
  public String getReportInstanceEmailToSubjectsError() {
    return reportInstanceEmailToSubjectsError;
  }

  
  public void setReportInstanceEmailToSubjectsError(String reportInstanceEmailToSubjectsError) {
    this.reportInstanceEmailToSubjectsError = reportInstanceEmailToSubjectsError;
  }
  
  public String getReportInstanceFileName() {
    return reportInstanceFileName;
  }

  
  public void setReportInstanceFileName(String reportInstanceFileName) {
    this.reportInstanceFileName = reportInstanceFileName;
  }
  
  
  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  
  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }

  /**
   * create an empty grouper report file
   */
  public void createEmptyReportFile() {
    
    GrouperReportConfigurationBean grouperReportConfigurationBean = this.getGrouperReportConfigurationBean();
    
    //reports/YYYY/MM/DD/group__$groupName$__$groupId$__report__$reportInstanceId$.csv
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    String tempFileParentDirPath = GrouperUtil.tmpDir() + "reports" + File.separator + calendar.get(Calendar.YEAR) + File.separator 
        + StringUtils.leftPad(""+(calendar.get(Calendar.MONTH)+1), 2, '0') + File.separator + StringUtils.leftPad(""+calendar.get(Calendar.DAY_OF_MONTH), 2, '0')
        + File.separator + GrouperUtil.uniqueId();

    //make sure the dir exists
    GrouperUtil.mkdirs(new File(tempFileParentDirPath));
    
    // $$timestamp$$ translates to current time in this format: yyyy_mm_dd_hh24_mi_ss
    String filename = grouperReportConfigurationBean.getReportConfigFilename();

    filename = StringUtils.replace(filename, "$$timestamp$$", new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date(this.getReportDateMillis())));
    
    final File theFile = new File(tempFileParentDirPath + File.separator + filename);
    GrouperUtil.fileCreateNewFile(theFile);

    this.setReportFileUnencrypted(theFile);
        
  }
  
  public boolean isReportStoredInS3() {
    //TODO do proper url check and then check for s3
    return this.getReportInstanceFilePointer().contains("amazonaws");
  }
  
}
