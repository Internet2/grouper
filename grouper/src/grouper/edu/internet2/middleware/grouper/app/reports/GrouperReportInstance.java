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

import edu.internet2.middleware.grouper.util.GrouperUtil;


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
  private Long reportDateMillis = System.currentTimeMillis();

  
  /**
   * if not set will default to now
   * @return the reportDateMillis
   */
  public Long getReportDateMillis() {
    return this.reportDateMillis;
  }

  
  /**
   * if not set will default to now
   * @param reportDateMillis1 the reportDateMillis to set
   */
  public void setReportDateMillis(Long reportDateMillis1) {
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
  
  /**
   * report instance status: SUCCESS or ERROR
   */
  private String reportInstanceStatus;
  
  /**
   * millis took to generate the report
   */
  private Long reportElapsedMillis;
  
  /**
   * id of report config marker that has this instance
   */
  private String reportInstanceConfigMarkerAssignmentId;
  
  /**
   * time when the report is generated
   */
  private Long reportInstanceMillisSince1970;
  
  /**
   * size of report in bytes
   */
  private Long reportInstanceSizeBytes;
  
  /**
   * pointer to the location of the file
   */
  private String reportInstanceFilePointer;
  
  /**
   * how many times the file has been downloaded
   */
  private Long reportInstanceDownloadCount;
  
  /**
   * encryption key with which the report has been encrypted
   */
  private String reportInstanceEncryptionKey;
  
  /**
   * number of rows in the report
   */
  private Long reportInstanceRows;
  
  /**
   * subjects who were sent report link via email 
   */
  private String reportInstanceEmailToSubjects;
  
  /**
   * subjects who were configured to receive but system couldn't send email to
   */
  private String reportInstanceEmailToSubjectsError;
  
  /**
   * file name of the report
   */
  private String reportInstanceFileName;
  
  /**
   * attribute assign id
   */
  private String attributeAssignId;
  
  /**
   * report instance status: SUCCESS or ERROR
   * @return
   */
  public String getReportInstanceStatus() {
    return reportInstanceStatus;
  }

  /**
   * report instance status: SUCCESS or ERROR
   * @param reportInstanceStatus
   */
  public void setReportInstanceStatus(String reportInstanceStatus) {
    this.reportInstanceStatus = reportInstanceStatus;
  }

  /**
   * millis took to generate the report
   * @return
   */
  public Long getReportElapsedMillis() {
    return reportElapsedMillis;
  }

  /**
   * millis took to generate the report
   * @param reportElapsedMillis
   */
  public void setReportElapsedMillis(Long reportElapsedMillis) {
    this.reportElapsedMillis = reportElapsedMillis;
  }

  /**
   * id of report config marker that has this instance
   * @return
   */
  public String getReportInstanceConfigMarkerAssignmentId() {
    return reportInstanceConfigMarkerAssignmentId;
  }

  /**
   * id of report config marker that has this instance
   * @param reportInstanceConfigMarkerAssignmentId
   */
  public void setReportInstanceConfigMarkerAssignmentId(
      String reportInstanceConfigMarkerAssignmentId) {
    this.reportInstanceConfigMarkerAssignmentId = reportInstanceConfigMarkerAssignmentId;
  }

  /**
   * time when the report is generated
   * @return
   */
  public Long getReportInstanceMillisSince1970() {
    return reportInstanceMillisSince1970;
  }

  /**
   * time when the report is generated
   * @param reportInstanceMillisSince1970
   */
  public void setReportInstanceMillisSince1970(Long reportInstanceMillisSince1970) {
    this.reportInstanceMillisSince1970 = reportInstanceMillisSince1970;
  }

  /**
   * size of report in bytes
   * @return
   */
  public Long getReportInstanceSizeBytes() {
    return reportInstanceSizeBytes;
  }

  /**
   * size of report in bytes
   * @param reportInstanceSizeBytes
   */
  public void setReportInstanceSizeBytes(Long reportInstanceSizeBytes) {
    this.reportInstanceSizeBytes = reportInstanceSizeBytes;
  }

  /**
   * pointer to the location of the file
   * @return
   */
  public String getReportInstanceFilePointer() {
    return reportInstanceFilePointer;
  }

  /**
   * pointer to the location of the file
   * @param reportInstanceFilePointer
   */
  public void setReportInstanceFilePointer(String reportInstanceFilePointer) {
    this.reportInstanceFilePointer = reportInstanceFilePointer;
  }

  /**
   * how many times the file has been downloaded
   * @return
   */
  public Long getReportInstanceDownloadCount() {
    return reportInstanceDownloadCount;
  }

  /**
   * how many times the file has been downloaded
   * @param reportInstanceDownloadCount
   */
  public void setReportInstanceDownloadCount(Long reportInstanceDownloadCount) {
    this.reportInstanceDownloadCount = reportInstanceDownloadCount;
  }
  
  /**
   * encryption key with which the report has been encrypted
   * @return
   */
  public String getReportInstanceEncryptionKey() {
    return reportInstanceEncryptionKey;
  }

  /**
   * encryption key with which the report has been encrypted
   * @param reportInstanceEncryptionKey
   */
  public void setReportInstanceEncryptionKey(String reportInstanceEncryptionKey) {
    this.reportInstanceEncryptionKey = reportInstanceEncryptionKey;
  }

  /**
   * number of rows in the report
   * @return
   */
  public Long getReportInstanceRows() {
    return reportInstanceRows;
  }

  /**
   * number of rows in the report
   * @param reportInstanceRows
   */
  public void setReportInstanceRows(Long reportInstanceRows) {
    this.reportInstanceRows = reportInstanceRows;
  }

  /**
   * subjects who were sent report link via email
   * @return
   */
  public String getReportInstanceEmailToSubjects() {
    return reportInstanceEmailToSubjects;
  }

  /**
   * subjects who were sent report link via email
   * @param reportInstanceEmailToSubjects
   */
  public void setReportInstanceEmailToSubjects(String reportInstanceEmailToSubjects) {
    this.reportInstanceEmailToSubjects = reportInstanceEmailToSubjects;
  }

  /**
   * subjects who were configured to receive but system couldn't send email to
   * @return
   */
  public String getReportInstanceEmailToSubjectsError() {
    return reportInstanceEmailToSubjectsError;
  }

  /**
   * subjects who were configured to receive but system couldn't send email to
   * @param reportInstanceEmailToSubjectsError
   */
  public void setReportInstanceEmailToSubjectsError(String reportInstanceEmailToSubjectsError) {
    this.reportInstanceEmailToSubjectsError = reportInstanceEmailToSubjectsError;
  }
  
  /**
   * file name of the report
   * @return
   */
  public String getReportInstanceFileName() {
    return reportInstanceFileName;
  }

  /**
   * file name of the report
   * @param reportInstanceFileName
   */
  public void setReportInstanceFileName(String reportInstanceFileName) {
    this.reportInstanceFileName = reportInstanceFileName;
  }
  
  /**
   * attribute assign id
   * @return
   */
  public String getAttributeAssignId() {
    return attributeAssignId;
  }
  
  /**
   * attribute assign id
   * @param attributeAssignId
   */
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
    
    String tempFileParentDirPath = GrouperUtil.tmpDir(true) + "grouperReports" + File.separator + calendar.get(Calendar.YEAR) + File.separator 
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
    return this.getReportInstanceFilePointer().startsWith("https://");
  }
  
}
