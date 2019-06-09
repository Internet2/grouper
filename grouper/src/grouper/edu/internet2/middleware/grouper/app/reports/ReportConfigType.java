/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.List;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * how data is retrieved
 */
public enum ReportConfigType {

  /** report that gets data from SQL */
  SQL {
    /**
     * 
     * @param grouperReportConfigurationBean
     * @return the grouper report data
     */
    public GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean) {
      
      GrouperReportData grouperReportData = new GrouperReportData();

      //lets parse the query:
      String headersString = grouperReportConfigurationBean.getReportConfigQuery();
      headersString = headersString.substring("select ".length());
      
      headersString = headersString.substring(0, headersString.toLowerCase().indexOf(" from "));
      
      List<String> headers = GrouperUtil.splitTrimToList(headersString, ",");
      
      List<String[]> data = HibernateSession.bySqlStatic().listSelect(String[].class, grouperReportConfigurationBean.getReportConfigQuery(), null, null);

      grouperReportData.setHeaders(headers);
      grouperReportData.setData(data);
      return grouperReportData;
      
    }
    

  };
  
  /**
   * get the data from a report
   * @param grouperReportConfigurationBean
   * @return the data
   */
  public abstract GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionIfBlank 
   * @return the enum or null or exception if not found
   */
  public static ReportConfigType valueOfIgnoreCase(String string, boolean exceptionIfBlank) {
    return GrouperUtil.enumValueOfIgnoreCase(ReportConfigType.class,string, exceptionIfBlank, true );
  }

  
}
