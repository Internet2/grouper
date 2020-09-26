/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.hibernate.internal.SessionImpl;


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

      GrouperReportData grouperReportData = (GrouperReportData)HibernateSession.callbackHibernateSession(
              GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {

                  HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
                  PreparedStatement preparedStatement = null;
                  GrouperReportData grouperReportData = new GrouperReportData();
                  String sql = grouperReportConfigurationBean.getReportConfigQuery();

                  try {
                    //we dont close this connection or anything since could be pooled
                    Connection connection = ((SessionImpl) hibernateSession.getSession()).connection();
                    preparedStatement = connection.prepareStatement(sql);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

                    // set headers from metadata
                    int columnCount = resultSetMetaData.getColumnCount();
                    ArrayList<String> cols = new ArrayList<>();
                    for (int index = 1; index <= columnCount; index++) {
                      cols.add(resultSetMetaData.getColumnName(index));
                    }
                    grouperReportData.setHeaders(cols);

                    // load rows as list of String[]
                    ArrayList<String[]> resultList = new ArrayList<>();
                    while (resultSet.next()) {
                      String[] row = new String[columnCount];
                      for (int i = 0; i < columnCount; i++) {
                        row[i] = resultSet.getString(i + 1);
                      }
                      resultList.add(row);
                    }
                    grouperReportData.setData(resultList);

                    return grouperReportData;
                  } catch (Exception e) {
                    throw new RuntimeException("Problem with query in listSelect: " + sql, e);
                  } finally {
                    GrouperUtil.closeQuietly(preparedStatement);
                  }
                }
              });

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
