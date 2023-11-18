/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;


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

      String dbExternalSystemConfigId = StringUtils.defaultIfBlank(grouperReportConfigurationBean.getSqlConfig(), "grouper");
      GrouperReportData grouperReportData = new GrouperReportData();
      String sql = grouperReportConfigurationBean.getReportConfigQuery();
      
      try {
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbExternalSystemConfigId);
        
        List<Object[]> results = gcDbAccess.sql(sql).selectList(Object[].class);
        
        GcTableSyncTableMetadata metadataFromDatabase = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(dbExternalSystemConfigId, sql);
        
        // set headers from metadata
        List<GcTableSyncColumnMetadata> retrieveColumnMetadataOrdered = metadataFromDatabase.retrieveColumnMetadataOrdered();
        
        int columnCount = retrieveColumnMetadataOrdered.size();
        ArrayList<String> cols = new ArrayList<>();
        for (int index = 1; index <= columnCount; index++) {
          cols.add(retrieveColumnMetadataOrdered.get(index-1).getColumnName());
        }
        grouperReportData.setHeaders(cols);

        // load rows as list of String[]
        ArrayList<String[]> resultList = new ArrayList<>();
        
        for (Object[] result: results) {
          String[] row = new String[columnCount];
          for (int i = 0; i < columnCount; i++) {
            row[i] = GrouperUtil.stringValue(result[i]);
          }
          resultList.add(row);
        }
        grouperReportData.setData(resultList);
      } catch (Exception e) {
        throw new RuntimeException("Problem with query in listSelect: " + sql, e);
      }

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
