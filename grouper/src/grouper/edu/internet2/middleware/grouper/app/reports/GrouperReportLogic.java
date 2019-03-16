/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;



/**
 *
 */
public class GrouperReportLogic {

  /**
   * 
   */
  public GrouperReportLogic() {
  }
  

  /**
   * run report
   * @param grouperReportConfigurationBean
   */
  @SuppressWarnings("cast")
  public static void runReport(GrouperReportConfigurationBean grouperReportConfigurationBean) {
    
    // get report data
    GrouperReportData grouperReportData = grouperReportConfigurationBean.getReportConfigType().retrieveReportDataByConfig(grouperReportConfigurationBean);

    GrouperReportInstance grouperReportInstance = new GrouperReportInstance();

    // now the file is in the report instance, remember to delete it and parent folders
    grouperReportConfigurationBean.getReportConfigFormat().formatReport(grouperReportData, grouperReportInstance);

    //TODO need to encrypt the file
    
    //TODO need to store to storage and delete from filesystem
    
    //TODO need to email this out
    
  }
}
