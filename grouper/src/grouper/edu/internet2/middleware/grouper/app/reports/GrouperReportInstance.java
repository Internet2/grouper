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
  
}
