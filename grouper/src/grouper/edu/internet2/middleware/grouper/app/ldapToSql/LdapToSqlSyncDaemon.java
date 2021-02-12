package edu.internet2.middleware.grouper.app.ldapToSql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class LdapToSqlSyncDaemon extends OtherJobBase {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(LdapToSqlSyncDaemon.class);

  
  public LdapToSqlSyncDaemon() {
  }

  private OtherJobInput otherJobInput = null;

  private Map<String, Object> debugMap = null;

  private GrouperSession grouperSession = null;
  
  private String jobName = null;
  
  private String dbConnection = null;
  
  private String baseDn = null;

  private String filter = null;

  private String ldapConnection = null;
  
  private int numberOfColumns = -1;
  
  private String searchScope = null;
  
  private String tableName = null;
  
  private Set<String> extraAttributes = new HashSet<String>();
  
  private List<LdapToSqlSyncColumn> ldapToSqlSyncColumns = new ArrayList<LdapToSqlSyncColumn>();
  
  @Override
  public OtherJobOutput run(OtherJobInput theOtherJobInput) {
    
    this.otherJobInput = theOtherJobInput;
    
    debugMap = new LinkedHashMap<String, Object>();
    
    grouperSession = GrouperSession.startRootSession();
    
    jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());
    
    // notification, summary
    this.dbConnection = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlDbConnection");
    debugMap.put("dbConnection", this.dbConnection);
    
    this.baseDn = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlBaseDn");
    debugMap.put("baseDn", this.baseDn);
    
    this.filter = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlFilter");
    debugMap.put("filter", this.filter);
    
    this.ldapConnection = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlLdapConnection");
    debugMap.put("ldapConnection", this.ldapConnection);
    
    this.numberOfColumns = GrouperUtil.intValue(GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlNumberOfAttributes"));
    debugMap.put("numberOfColumns", this.numberOfColumns);
    
    this.searchScope = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlSearchScope");
    debugMap.put("searchScope", this.searchScope);
    
    this.tableName = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlTableName");
    debugMap.put("tableName", this.tableName);
    
    String extraAttributesString = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".ldapSqlExtraAttributes");
    debugMap.put("extraAttributes", extraAttributesString);
    if (!StringUtils.isBlank(extraAttributesString)) {
      this.extraAttributes = GrouperUtil.splitTrimToSet(extraAttributesString, ",");
    }
    
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.", "dn");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.sqlColumn", "the_dn");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.0.uniqueKey", "true");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.1.ldapName", "mail");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.1.sqlColumn", "mail");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.2.sqlColumn", "description");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.2.translation", "${ldapAttribute__givenname + ', ' + ldapAttribute__uid}");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.3.sqlColumn", "some_int");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.ldapSqlAttribute.3.translation", "${123}");
    //  GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.ldapToSqlSingleValued.", "uid,givenName");
    for (int i=0;i<numberOfColumns;i++) {
      
      LdapToSqlSyncColumn ldapToSqlSyncColumn = new LdapToSqlSyncColumn();
      this.ldapToSqlSyncColumns.add(ldapToSqlSyncColumn);
      
      String sqlColumn = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlAttribute." + i + ".sqlColumn");
      ldapToSqlSyncColumn.setSqlColumn(sqlColumn);

      String ldapName = GrouperLoaderConfig
          .retrieveConfig().propertyValueString("otherJob." + jobName + ".ldapSqlAttribute." + i + ".ldapName");
      if (!StringUtils.isBlank(ldapName)) {
        ldapToSqlSyncColumn.setLdapName(ldapName);
      }
      
      String translation = GrouperLoaderConfig
          .retrieveConfig().propertyValueString("otherJob." + jobName + ".ldapSqlAttribute." + i + ".translation");
      if (!StringUtils.isBlank(translation)) {
        ldapToSqlSyncColumn.setTranslation(translation);
      }
      
      if (StringUtils.isBlank(ldapName) == StringUtils.isBlank(translation)) {
        throw new RuntimeException("ldapName and translation are mutually exclusive!!! '" + ldapName + "', '" + translation + "'");
      }

      boolean uniqueKey = GrouperLoaderConfig
          .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".ldapSqlAttribute." + i + ".uniqueKey", false);
      ldapToSqlSyncColumn.setUniqueKey(uniqueKey);

    }

    
    
    otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    return null;
  }
  
  private GcTableSyncTableBean dataBean;
  
  private void retrieveDataFromDatabase() {
    
    final GcTableSync gcTableSync = new GcTableSync();
    
    dataBean = new GcTableSyncTableBean(gcTableSync);
    dataBean.configureMetadata(this.dbConnection, this.tableName);

    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.dbConnection);
    
    String sql = "select " + dataBean.getTableMetadata().columnListAll() + " from " + dataBean.getTableMetadata().getTableName();

    
    List<Object[]> results = gcDbAccess.sql(sql).selectList(Object[].class);

    GcTableSyncTableMetadata gcTableSyncTableMetadata = dataBean.getTableMetadata();

    GcTableSyncTableData gcTableSyncTableData = new GcTableSyncTableData();
    gcTableSyncTableData.init(dataBean, gcTableSyncTableMetadata.lookupColumns(dataBean.getTableMetadata().columnListAll()), results);
    
//    return gcTableSyncTableData;
//  } catch (RuntimeException re) {
//    GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' connectionName: " + connectionName + ", query '" + sql + "', " + GrouperClientUtils.toStringForLog(bindVars));
//    throw re;
//  } finally {
//    //temporarily store as micros, then divide in the end
//    logIncrement(debugMap, queryLogLabel + "Millis", (long)((System.nanoTime() - start)/1000));
//  }

  }
  
}
