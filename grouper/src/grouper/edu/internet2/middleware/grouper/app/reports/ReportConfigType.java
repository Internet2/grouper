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

import org.hibernate.internal.SessionImpl;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * how data is retrieved
 */
public enum ReportConfigType {

  /** report that gets data from GSH */
  GSH {
    /**
     * 
     * @param grouperReportConfigurationBean
     * @return the grouper report data
     */
    public GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean, GrouperReportInstance grouperReportInstance) {
  
      GshReportRuntime gshReportRuntime = new GshReportRuntime();
      
      String attributeAssignId = grouperReportConfigurationBean.getAttributeAssignmentMarkerId();
      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      {
        Group group = attributeAssign.getOwnerGroup();
        if (group != null) {
          gshReportRuntime.setOwnerGroup(group);
          gshReportRuntime.setOwnerGroupName(group.getName());
        }
      }      
      
      {
        Stem stem = attributeAssign.getOwnerStem();
        if (stem != null) {
          gshReportRuntime.setOwnerStem(stem);
          gshReportRuntime.setOwnerStemName(stem.getName());
        }
      }      
      
      GshReportRuntime.assignThreadLocalGshReportRuntime(gshReportRuntime);
      
      try {
        GrouperReportData grouperReportData = new GrouperReportData();
        gshReportRuntime.setGrouperReportData(grouperReportData);
        grouperReportData.setFile(grouperReportInstance.getReportFileUnencrypted());

        String gshScript = grouperReportConfigurationBean.getReportConfigScript();
              
        StringBuilder scriptToRun = new StringBuilder();
        scriptToRun.append("GrouperSession gsh_builtin_grouperSession = GrouperGroovyRuntime.retrieveGrouperGroovyRuntime().getGrouperSession();\n");
  
        scriptToRun.append("GshReportRuntime gsh_builtin_gshReportRuntime = GshReportRuntime.retrieveGshReportRuntime();\n");
  
        scriptToRun.append("String gsh_builtin_ownerStemName = gsh_builtin_gshReportRuntime.getOwnerStemName();\n");
        scriptToRun.append("String gsh_builtin_ownerGroupName = gsh_builtin_gshReportRuntime.getOwnerGroupName();\n");
  
  
        scriptToRun.append(gshScript);

        // keep a handle of the runtime
        GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();
        GrouperGroovyRuntime grouperGroovyRuntime = new GrouperGroovyRuntime();

        grouperGroovyInput.assignGrouperGroovyRuntime(grouperGroovyRuntime);

  
        grouperGroovyInput.assignScript(scriptToRun.toString());
  
        GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();

        GrouperGroovysh.runScript(grouperGroovyInput, grouperGroovyResult);

        if (grouperGroovyResult.getException() != null) {
          throw grouperGroovyResult.getException();
        }

        if (GrouperUtil.intValue(grouperGroovyResult.getResultCode(), -1) != 0) {
          throw new RuntimeException("GSH script result code not 0: " + grouperGroovyResult.getResultCode());
        }
        
        return grouperReportData;
      } finally {
        GshReportRuntime.removeThreadLocalGshReportRuntime();
      }
    }
  },
  /** report that gets data from SQL */
  SQL {
    /**
     * 
     * @param grouperReportConfigurationBean
     * @return the grouper report data
     */
    public GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean, GrouperReportInstance grouperReportInstance) {

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
  public abstract GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean, GrouperReportInstance grouperReportInstance);
  
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
