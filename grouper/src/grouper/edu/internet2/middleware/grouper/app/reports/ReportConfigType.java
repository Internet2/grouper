/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyRuntime;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh;
import edu.internet2.middleware.grouper.app.gsh.GrouperGroovysh.GrouperGroovyResult;
import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateReport;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompiledDispatch;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.subject.LazySource;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;


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

        // GRP-7031: compiled-Java report template — when a gshTemplateConfigId is
        // configured, resolve the GrouperTemplateReport from the registry and call
        // runReport with the runtime already on the ThreadLocal; skip the inline
        // Groovy path.
        String gshTemplateConfigId = grouperReportConfigurationBean.getGshTemplateConfigId();
        if (!StringUtils.isBlank(gshTemplateConfigId)) {
          GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(gshTemplateConfigId);
          gshTemplateConfig.populateConfiguration();
          GrouperTemplateReport grouperTemplateReport = GshTemplateCompiledDispatch.instantiate(
              gshTemplateConfigId, gshTemplateConfig, GrouperTemplateReport.class);
          grouperTemplateReport.runReport(gshReportRuntime);
          GrouperDaemonUtils.stopProcessingIfJobPaused();
          return grouperReportData;
        }

        String gshScript = grouperReportConfigurationBean.getReportConfigScript();
              
        StringBuilder scriptToRun = new StringBuilder();
        scriptToRun.append("GrouperSession gsh_builtin_grouperSession = GrouperGroovyRuntime.retrieveGrouperGroovyRuntime().getGrouperSession();\n");
  
        scriptToRun.append("GshReportRuntime gsh_builtin_gshReportRuntime = GshReportRuntime.retrieveGshReportRuntime();\n");
  
        scriptToRun.append("String gsh_builtin_ownerStemName = gsh_builtin_gshReportRuntime.getOwnerStemName();\n");
        scriptToRun.append("String gsh_builtin_ownerGroupName = gsh_builtin_gshReportRuntime.getOwnerGroupName();\n");
  
        GrouperGroovyInput grouperGroovyInput = new GrouperGroovyInput();
        grouperGroovyInput.assignScriptPrependHeaders(GrouperUtil.whitespaceCountNewLines(gshScript.toString()));
        scriptToRun.append(gshScript);

        // keep a handle of the runtime
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
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();

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
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        GcTableSyncTableMetadata metadataFromDatabase = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(dbExternalSystemConfigId, sql);
        GrouperDaemonUtils.stopProcessingIfJobPaused();

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
  },
  /**
   * report that exports the members of a group.  This backs the self-service "export members"
   * feature and reuses the reporting engine for async run, encrypted storage, and delivery.
   * The export parameters are carried on the (system-managed, hidden) report config in existing
   * fields to avoid new attribute definitions:
   *   reportConfigQuery  = export mode, either "all" or "ids"
   *   reportConfigScript = for "all" mode: the sort field on the first line, then the comma
   *                        separated header/subject-field list on the second line
   *   sqlConfig          = the requesting subject as sourceId::subjectId (used to email the link)
   */
  GROUP_MEMBERS_EXPORT {

    public GrouperReportData retrieveReportDataByConfig(GrouperReportConfigurationBean grouperReportConfigurationBean, GrouperReportInstance grouperReportInstance) {

      String attributeAssignId = grouperReportConfigurationBean.getAttributeAssignmentMarkerId();
      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      Group group = attributeAssign.getOwnerGroup();
      if (group == null) {
        throw new RuntimeException("GROUP_MEMBERS_EXPORT report config must be assigned to a group. attributeAssignId: " + attributeAssignId);
      }

      boolean exportAll = StringUtils.equalsIgnoreCase("all", grouperReportConfigurationBean.getReportConfigQuery());

      Set<Member> members = group.getMembers();

      GrouperReportData grouperReportData = new GrouperReportData();

      if (exportAll) {

        String sortField = null;
        String headersCommaSeparated = null;
        String script = grouperReportConfigurationBean.getReportConfigScript();
        String[] scriptLines = GrouperUtil.splitTrim(script, "\n");
        if (scriptLines != null && scriptLines.length > 0) {
          sortField = scriptLines[0];
        }
        if (scriptLines != null && scriptLines.length > 1) {
          headersCommaSeparated = scriptLines[1];
        }

        String[] headers = GrouperUtil.splitTrim(headersCommaSeparated, ",");

        boolean[] isAttribute = new boolean[headers.length];
        int sortCol = 0;
        int sourceIdCol = -1;
        for (int i = 0; i < headers.length; i++) {
          isAttribute[i] = !GROUP_MEMBERS_EXPORT_NON_ATTRIBUTE_COLS.contains(headers[i].toLowerCase());
          if (StringUtils.equalsIgnoreCase(headers[i], sortField)) {
            sortCol = i;
          } else if (StringUtils.equalsIgnoreCase("sourceId", headers[i])) {
            sourceIdCol = i;
          }
        }

        Member.resolveSubjects(members, true);

        List<String[]> memberData = new ArrayList<String[]>();
        for (Member member : members) {
          memberData.add(groupMembersExportAllRow(member, headers, isAttribute));
        }
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        final int SORT_COL = sortCol;
        final int SOURCE_ID_COL = sourceIdCol;
        Collections.sort(memberData, new Comparator<String[]>() {
          public int compare(String[] o1, String[] o2) {
            if (SOURCE_ID_COL != -1 && !StringUtils.equals(o1[SOURCE_ID_COL], o2[SOURCE_ID_COL])) {
              return groupMembersExportCompare(o1[SOURCE_ID_COL], o2[SOURCE_ID_COL]);
            }
            return groupMembersExportCompare(o1[SORT_COL], o2[SORT_COL]);
          }
        });

        List<String> headerList = new ArrayList<String>();
        for (String header : headers) {
          headerList.add(header);
        }
        grouperReportData.setHeaders(headerList);
        grouperReportData.setData(memberData);

      } else {

        List<String[]> memberData = new ArrayList<String[]>();
        for (Member member : members) {
          memberData.add(new String[]{member.getSubjectSourceId(), member.getSubjectId()});
        }
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        Collections.sort(memberData, new Comparator<String[]>() {
          public int compare(String[] o1, String[] o2) {
            if (!StringUtils.equals(o1[0], o2[0])) {
              return groupMembersExportCompare(o1[0], o2[0]);
            }
            return groupMembersExportCompare(o1[1], o2[1]);
          }
        });

        grouperReportData.setHeaders(Arrays.asList("sourceId", "entityId"));
        grouperReportData.setData(memberData);
      }

      return grouperReportData;
    }
  };

  /**
   * logger
   */
  private static final Log LOG = GrouperUtil.getLog(ReportConfigType.class);

  /**
   * columns (lower case) that are not subject attributes for the group members export
   */
  private static final Set<String> GROUP_MEMBERS_EXPORT_NON_ATTRIBUTE_COLS = GrouperUtil.toSet(
      "subjectid", "entityid", "sourceid", "memberid", "name", "description", "screenlabel");

  /**
   * null safe case-insensitive compare used to sort group members export rows
   * @param first
   * @param second
   * @return compare result
   */
  static int groupMembersExportCompare(String first, String second) {
    if (StringUtils.equals(first, second)) {
      return 0;
    }
    if (first == null) {
      return -1;
    }
    if (second == null) {
      return 1;
    }
    return first.compareToIgnoreCase(second);
  }

  /**
   * build a single csv row of subject fields for a member (group members export, "all" mode).
   * Mirrors the legacy UI export but runs in core, so screenLabel falls back to the subject name.
   * @param member
   * @param headers
   * @param isAttribute which header indexes are subject attributes
   * @return the row of values matching the headers
   */
  static String[] groupMembersExportAllRow(Member member, String[] headers, boolean[] isAttribute) {

    String[] result = new String[headers.length];

    for (int i = 0; i < headers.length; i++) {
      String header = headers[i];
      if ("subjectId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectId();
      } else if ("entityId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectId();
      } else if ("sourceId".equalsIgnoreCase(header)) {
        result[i] = member.getSubjectSourceId();
      } else if ("memberId".equalsIgnoreCase(header)) {
        result[i] = member.getUuid();
      }
    }

    try {
      Subject subject = member.getSubject();
      Source source = subject.getSource();
      Map<String, String> exportLabelToAttributeName = null;
      if (source instanceof LazySource) {
        source = ((LazySource)source).getSource();
      }
      if (source instanceof BaseSourceAdapter) {
        exportLabelToAttributeName = ((BaseSourceAdapter)source).exportLabelToAttributeName();
      }
      for (int i = 0; i < headers.length; i++) {
        String header = headers[i];
        if ("name".equalsIgnoreCase(header)) {
          result[i] = subject.getName();
        } else if ("description".equalsIgnoreCase(header)) {
          result[i] = subject.getDescription();
        } else if ("screenLabel".equalsIgnoreCase(header)) {
          result[i] = subject.getName();
        } else if (isAttribute[i]) {
          String attributeName = header;
          if (exportLabelToAttributeName != null && exportLabelToAttributeName.containsKey(attributeName)) {
            attributeName = exportLabelToAttributeName.get(attributeName);
          }
          result[i] = subject.getAttributeValueOrCommaSeparated(attributeName);
        }
      }
    } catch (Exception e) {
      LOG.error("Error resolving subject for group members export, memberId: " + member.getUuid()
          + ", subjectId: " + member.getSubjectId(), e);
    }

    return result;
  }

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
