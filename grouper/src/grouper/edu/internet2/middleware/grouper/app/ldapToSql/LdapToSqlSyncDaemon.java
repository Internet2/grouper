package edu.internet2.middleware.grouper.app.ldapToSql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncConfiguration;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncRowData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

public class LdapToSqlSyncDaemon extends OtherJobBase {
  
  public static void main(String[] args) {
    Map<String, Object> elVariableMap = new HashMap<String, Object>();
    
    elVariableMap.put("ldapAttribute__uid", "jsmith");

    String script = "${ldapAttribute__uid + '@example.edu'}";
      
    String ldapValue = (String)GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    
    System.out.println(ldapValue);
    
    elVariableMap.put("ldapAttribute__somethingPacked", "{fruit=apple}:{color=red}:{flavor=sweet}");

    // int x=4;\nString y='hey';\ny
    //
    ldapValue = (String)GrouperUtil.substituteExpressionLanguageScript("${var theMatcher = java.util.regex.Pattern.compile('^.*\\{color=([^}]*)\\}.*$').matcher(ldapAttribute__somethingPacked);theMatcher.matches() ? theMatcher.group(1) : null}", elVariableMap, true, false, false);
    System.out.println("First: '" + ldapValue + "'");
    
    Pattern pattern = java.util.regex.Pattern.compile("^.*\\{color=([^}]*)\\}.*$");
    
    Matcher matcher = pattern.matcher((String)elVariableMap.get("ldapAttribute__somethingPacked"));
    
    String result = matcher.matches() ? matcher.group(1) : null; 
    System.out.println(result);
    script = "${java.util.regex.Matcher \nString result = matcher.matches() ? group(1) : null;}";
    
    ldapValue = (String)GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    System.out.println(ldapValue);
  }
  
  public LdapToSqlSyncDaemon() {
  }

  public static Map<String, Object> internalTestLastDebugMap = null;
  
  private OtherJobInput otherJobInput = null;

  private Map<String, Object> debugMap = null;

  private String jobName = null;
  
  private String dbConnection = null;
  
  private String baseDn = null;

  private String filter = null;

  private String ldapConnection = null;
  
  private int numberOfColumns = -1;
  
  private String searchScope = null;
  
  private String tableName = null;
  
  private Set<String> extraAttributes = new HashSet<String>();
  
  /**
   * map of lower case column name to sync column
   */
  private Map<String, LdapToSqlSyncColumn> ldapToSqlSyncColumns = new TreeMap<String, LdapToSqlSyncColumn>();

  private GcTableSync gcTableSync;

  private boolean hasMultiValuedTable;

  private String multiValuedTableName;

  private String multiValuedIdColumn;

  private String multiValuedAttributes;

  private GcTableSync gcTableSyncAttr;

  private GcTableSyncTableData gcTableSyncTableDataLdapAttr;
  
  @Override
  public OtherJobOutput run(OtherJobInput theOtherJobInput) {
    
    this.otherJobInput = theOtherJobInput;
    
    debugMap = new LinkedHashMap<String, Object>();
    
    internalTestLastDebugMap = debugMap;
    
    GrouperSession.startRootSession();
    
    jobName = otherJobInput.getJobName();

    this.gcTableSync = new GcTableSync();
    this.gcTableSyncAttr = new GcTableSync();

    configure();
    
    retrieveDataFromDatabase();
    retrieveDataFromLdap();
    
    long nowNanos = System.nanoTime();
    try { 
      convertLdapDataToDatabaseFormat();
    } finally {
      this.debugMap.put("convertDataMillis", (System.nanoTime()-nowNanos)/1000);
    }
        
    nowNanos = System.nanoTime();
    try { 
      GcTableSyncConfiguration gcTableSyncConfiguration = new GcTableSyncConfiguration();
      gcTableSync.setGcTableSyncConfiguration(gcTableSyncConfiguration);

      gcTableSync.setGcTableSyncOutput(new GcTableSyncOutput());
      
      GcTableSyncSubtype.fullSyncFull.syncData(this.debugMap, gcTableSync);
    } finally {
      this.debugMap.put("changeSqlMainTableMillis", (System.nanoTime()-nowNanos)/1000);
    }
    if (this.hasMultiValuedTable) {
      nowNanos = System.nanoTime();
      try { 
        GcTableSyncConfiguration gcTableSyncConfigurationAttr = new GcTableSyncConfiguration();
        gcTableSyncAttr.setGcTableSyncConfiguration(gcTableSyncConfigurationAttr);

        gcTableSyncAttr.setGcTableSyncOutput(new GcTableSyncOutput());
        
        
        Map<String, Object> debugMapAttr = new LinkedHashMap<String, Object>();
        GcTableSyncSubtype.fullSyncFull.syncData(debugMapAttr, gcTableSyncAttr);

        // merge the debug maps
        for (String key : debugMapAttr.keySet()) {
          
          Object newValue = debugMapAttr.get(key);
          Object existingValue = this.debugMap.get(key);
          
          if (existingValue instanceof Long) {
            this.debugMap.put(key, (Long)existingValue + GrouperUtil.longValue(newValue));
          } else if (existingValue instanceof Integer) {
            this.debugMap.put(key, (Integer)existingValue + GrouperUtil.intValue(newValue));
          } else if (this.debugMap.containsKey(key)) {
            this.debugMap.put(key + "_attr", newValue);
          } else {
            this.debugMap.put(key, newValue);
          }

          
        }
      
      } finally {
        this.debugMap.put("changeSqlAttributeTableMillis", (System.nanoTime()-nowNanos)/1000);
      }
    }
      
    //  dbConnection: grouper, baseDn: ou=Groups,dc=example,dc=edu, filter: (objectClass=groupOfUniqueNames), ldapConnection: personLdap, 
    //      numberOfColumns: 3, searchScope: SUBTREE_SCOPE, tableName: testgrouper_ldapsync, extraAttributes: null, ldapRecords: 1, 
    //      dbRows: 0, dbUniqueKeys: 0, deletesCount: 0, deletesMillis: 81, insertsCount: 1, insertsMillis: 1997, updatesCount: 0, updatesMillis: 4
    
    otherJobInput.getHib3GrouperLoaderLog().setInsertCount(GrouperUtil.intValue(this.debugMap.get("insertsCount"), 0));
    otherJobInput.getHib3GrouperLoaderLog().setDeleteCount(GrouperUtil.intValue(this.debugMap.get("deletesCount"), 0));
    otherJobInput.getHib3GrouperLoaderLog().setUpdateCount(GrouperUtil.intValue(this.debugMap.get("updatesCount"), 0));
    otherJobInput.getHib3GrouperLoaderLog().setTotalCount(GrouperUtil.intValue(this.debugMap.get("ldapRecords"), 0) + GrouperUtil.intValue(this.debugMap.get("ldapMultiValuedAttributes"), 0));
    
    // change micros to millis in the logs
    for (String label : debugMap.keySet()) {
      if (label.endsWith("Millis")) {
        Object value = debugMap.get(label);
        if (value instanceof Number) {
          long millis = ((Number)value).longValue()/1000;
          debugMap.put(label, millis);
        }
      }
    }

    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    return null;
  }


  private void convertLdapDataToDatabaseFormat() {
    
    this.gcTableSyncTableDataLdap = new GcTableSyncTableData();
    gcTableSync.getDataBeanFrom().setDataInitialQuery(this.gcTableSyncTableDataLdap);

    this.gcTableSyncTableDataLdap.setColumnMetadata(this.gcTableSyncTableDataSql.getColumnMetadata());

    this.gcTableSyncTableDataLdap.setGcTableSyncTableBean(this.gcTableSyncTableDataSql.getGcTableSyncTableBean());

    List<GcTableSyncRowData> gcTableSyncRowDatas = new ArrayList<GcTableSyncRowData>();
    
    for (Object[] ldapRawRow : GrouperUtil.nonNull(this.ldapData)) {
      
      GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
      gcTableSyncRowDatas.add(gcTableSyncRowData);
      
      gcTableSyncRowData.setGcTableSyncTableData(this.gcTableSyncTableDataLdap);
      
      Object[] rowData = new Object[this.gcTableSyncTableDataLdap.getColumnMetadata().size()];
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      
      for (int i=0;i<this.ldapAttributeNames.length;i++) {
        String attributeName = this.ldapAttributeNames[i];
        String attributeValue = (String)ldapRawRow[i];
        elVariableMap.put("ldapAttribute__" + attributeName, attributeValue);
      }
  
      for (int i=0;i<this.gcTableSyncTableDataLdap.getColumnMetadata().size();i++) {

        GcTableSyncColumnMetadata gcTableSyncColumnMetadata = this.gcTableSyncTableDataLdap.getColumnMetadata().get(i);
        String columnName = gcTableSyncColumnMetadata.getColumnName();
        
        LdapToSqlSyncColumn ldapToSqlSyncColumn = this.ldapToSqlSyncColumns.get(columnName.toLowerCase());
        
        String script = ldapToSqlSyncColumn.getTranslation();
        
        Object ldapValue = null;
        
        if (!StringUtils.isBlank(script)) {
        
          try {
            if (!script.contains("${")) {
              script = "${" + script + "}";
            }
            ldapValue = GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
            
          } catch (RuntimeException re) {
            GrouperUtil.injectInException(re, ", script: '" + script + "', ");
            GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
            throw re;
          }
        } else {
          
          int ldapValueIndex = this.ldapAttributeNameIndex.get(ldapToSqlSyncColumn.getLdapName());
          ldapValue = ldapRawRow[ldapValueIndex];
          
        }

        //now we need to typecast
        rowData[i] = gcTableSyncColumnMetadata.getColumnType().convertToType(ldapValue);
        
      }
      
      gcTableSyncRowData.setData(rowData);
    }
    
    
    this.gcTableSyncTableDataLdap.setRows(gcTableSyncRowDatas);

    if (this.hasMultiValuedTable) {
      this.gcTableSyncTableDataLdapAttr = new GcTableSyncTableData();
      gcTableSyncAttr.getDataBeanFrom().setDataInitialQuery(this.gcTableSyncTableDataLdapAttr);

      this.gcTableSyncTableDataLdapAttr.setColumnMetadata(this.gcTableSyncTableDataSqlAttr.getColumnMetadata());

      this.gcTableSyncTableDataLdapAttr.setGcTableSyncTableBean(this.gcTableSyncTableDataSqlAttr.getGcTableSyncTableBean());

      List<GcTableSyncRowData> gcTableSyncRowDatasAttr = new ArrayList<GcTableSyncRowData>();
      
      for (Object[] ldapRawRowAttr : GrouperUtil.nonNull(this.ldapDataAttr)) {
        
        GcTableSyncRowData gcTableSyncRowDataAttr = new GcTableSyncRowData();
        gcTableSyncRowDatasAttr.add(gcTableSyncRowDataAttr);
        
        gcTableSyncRowDataAttr.setGcTableSyncTableData(this.gcTableSyncTableDataLdapAttr);
        
        Object[] rowData = new Object[3];

        for (int i=0;i<this.gcTableSyncTableDataLdapAttr.getColumnMetadata().size();i++) {

          GcTableSyncColumnMetadata gcTableSyncColumnMetadataAttr = this.gcTableSyncTableDataLdapAttr.getColumnMetadata().get(i);
          String columnName = gcTableSyncColumnMetadataAttr.getColumnName();
          if (StringUtils.equalsIgnoreCase("ldap_id", columnName)) {
            rowData[i] = ldapRawRowAttr[0];
          } else if (StringUtils.equalsIgnoreCase("attribute_name", columnName)) {
            rowData[i] = ldapRawRowAttr[1];
          } else if (StringUtils.equalsIgnoreCase("attribute_value", columnName)) {
            rowData[i] = ldapRawRowAttr[2];
          } else {
            throw new RuntimeException("Invalid column name: '" + columnName + "'");
          }
        }
        gcTableSyncRowDataAttr.setData(rowData);
      }
      
      
      this.gcTableSyncTableDataLdapAttr.setRows(gcTableSyncRowDatasAttr);

    }
    
  }

  private void configure() {
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

    this.hasMultiValuedTable = GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".ldapSqlHasMultiValuedTable", false);
    debugMap.put("hasMultiValuedTable", this.hasMultiValuedTable);

    if (this.hasMultiValuedTable) {
      this.multiValuedTableName = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlMultiValuedTableName");
      debugMap.put("multiValuedTableName", this.multiValuedTableName);
  
      this.multiValuedIdColumn = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlIdColumn");
      debugMap.put("multiValuedIdColumn", this.multiValuedIdColumn);
  
      this.multiValuedAttributes = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlMultiValuedAttributes");
      debugMap.put("multiValuedAttributes", this.multiValuedAttributes);
    }
        
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
    
    boolean foundMultiValuedIdColumn = false;
    
    StringBuilder columnNames = new StringBuilder();
    
    for (int i=0;i<numberOfColumns;i++) {
      
      LdapToSqlSyncColumn ldapToSqlSyncColumn = new LdapToSqlSyncColumn();
      
      String sqlColumn = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".ldapSqlAttribute." + i + ".sqlColumn");
      ldapToSqlSyncColumn.setSqlColumn(sqlColumn);

      this.ldapToSqlSyncColumns.put(sqlColumn.toLowerCase(), ldapToSqlSyncColumn);

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

      if (StringUtils.equalsIgnoreCase(this.multiValuedIdColumn, sqlColumn)) {
        foundMultiValuedIdColumn = true;
      }

      if (i>0) {
        columnNames.append(", ");
      }
      columnNames.append(sqlColumn);
      
    }
    
    if (!StringUtils.isBlank(this.multiValuedIdColumn) && !foundMultiValuedIdColumn) {
      throw new RuntimeException("Multi-valued ID column '" + this.multiValuedIdColumn + "' is not in list of main SQL table columns! " + columnNames);
    }
    
  }
  
  private GcTableSyncTableBean gcTableSyncTableBeanSql;

  /**
   * ldap attribute names
   */
  private String[] ldapAttributeNames;

  /**
   * ldap attribute names
   */
  private Map<String, Integer> ldapAttributeNameIndex = new HashMap<String, Integer>();

  /**
   * ldap data from filter
   */
  private ArrayList<Object[]> ldapData;

  private GcTableSyncTableData gcTableSyncTableDataSql;
  private GcTableSyncTableData gcTableSyncTableDataLdap;

  private TreeSet<String> uniqueKeyColumnNames;

  private GcTableSyncTableBean gcTableSyncTableBeanSqlAttr;

  private GcTableSyncTableData gcTableSyncTableDataSqlAttr;

  private ArrayList<Object[]> ldapDataAttr;
  
  private void retrieveDataFromDatabase() {
    long nowNanos = System.nanoTime();
    
    try {
      gcTableSyncTableBeanSql = new GcTableSyncTableBean(gcTableSync);
      gcTableSyncTableBeanSql.configureMetadata(this.dbConnection, this.tableName);
      this.gcTableSync.setDataBeanTo(gcTableSyncTableBeanSql);
  
      Set<String> databaseColumnNames = new TreeSet<String>();
      for (LdapToSqlSyncColumn ldapToSqlSyncColumn : GrouperUtil.nonNull(this.ldapToSqlSyncColumns).values()) {
        databaseColumnNames.add(ldapToSqlSyncColumn.getSqlColumn());
      }
  
      this.uniqueKeyColumnNames = new TreeSet<String>();
      for (LdapToSqlSyncColumn ldapToSqlSyncColumn : GrouperUtil.nonNull(this.ldapToSqlSyncColumns).values()) {
        if (ldapToSqlSyncColumn.isUniqueKey()) {
          this.uniqueKeyColumnNames.add(ldapToSqlSyncColumn.getSqlColumn());
        }
      }
  
      GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanSql.getTableMetadata();
  
      gcTableSyncTableMetadata.assignColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
      gcTableSyncTableMetadata.assignPrimaryKeyColumns(GrouperUtil.join(this.uniqueKeyColumnNames.iterator(), ','));
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.dbConnection);
      
      String sql = "select " + gcTableSyncTableMetadata.columnListAll() + " from " + gcTableSyncTableMetadata.getTableName();
      
      List<Object[]> results = gcDbAccess.sql(sql).selectList(Object[].class);
  
      this.debugMap.put("dbRows", GrouperUtil.length(results));
  
      this.gcTableSyncTableDataSql = new GcTableSyncTableData();
      this.gcTableSyncTableDataSql.init(this.gcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(this.gcTableSyncTableBeanSql.getTableMetadata().columnListAll()), results);
      this.gcTableSyncTableDataSql.indexData();
  
      gcTableSyncTableBeanSql.setDataInitialQuery(this.gcTableSyncTableDataSql);
      gcTableSyncTableBeanSql.setGcTableSync(gcTableSync);
  
      
      this.debugMap.put("dbUniqueKeys", this.gcTableSyncTableDataSql.allPrimaryKeys().size());
      
      if (this.hasMultiValuedTable) {
        this.gcTableSyncTableBeanSqlAttr = new GcTableSyncTableBean(gcTableSyncAttr);
        this.gcTableSyncTableBeanSqlAttr.configureMetadata(this.dbConnection, this.multiValuedTableName);
        this.gcTableSyncAttr.setDataBeanTo(this.gcTableSyncTableBeanSqlAttr);
  
        databaseColumnNames = new TreeSet<String>();
        databaseColumnNames.add("ldap_id");
        databaseColumnNames.add("attribute_name");
        databaseColumnNames.add("attribute_value");
  
        GcTableSyncTableMetadata gcTableSyncTableMetadataAttr = gcTableSyncTableBeanSqlAttr.getTableMetadata();
  
        gcTableSyncTableMetadataAttr.assignColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
        gcTableSyncTableMetadataAttr.assignPrimaryKeyColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
        
        gcDbAccess = new GcDbAccess().connectionName(this.dbConnection);
        
        sql = "select " + gcTableSyncTableMetadataAttr.columnListAll() + " from " + gcTableSyncTableMetadataAttr.getTableName();
        
        results = gcDbAccess.sql(sql).selectList(Object[].class);
  
        this.debugMap.put("dbRows", GrouperUtil.length(results));
  
        this.gcTableSyncTableDataSqlAttr = new GcTableSyncTableData();
        this.gcTableSyncTableDataSqlAttr.init(this.gcTableSyncTableBeanSqlAttr, gcTableSyncTableMetadataAttr.lookupColumns(this.gcTableSyncTableBeanSqlAttr.getTableMetadata().columnListAll()), results);
        this.gcTableSyncTableDataSqlAttr.indexData();
  
        gcTableSyncTableBeanSqlAttr.setDataInitialQuery(this.gcTableSyncTableDataSqlAttr);
        gcTableSyncTableBeanSqlAttr.setGcTableSync(gcTableSyncAttr);
  
        
        this.debugMap.put("dbMultiValuedAttributes", this.gcTableSyncTableDataSqlAttr.allPrimaryKeys().size());
        
  
      }
    } finally {
      this.debugMap.put("retrieveDataSqlMillis", (System.nanoTime()-nowNanos)/1000);
    }
    
  }

  private void retrieveDataFromLdap() {
    
    long nowNanos = System.nanoTime();
    try {
      LdapSession ldapSession = LdapSessionUtils.ldapSession();
      
      Set<String> ldapAttributeNamesSet = new TreeSet<String>();
      
      ldapAttributeNamesSet.addAll(GrouperUtil.nonNull(this.extraAttributes));
      
      Set<String> attributesInMainTable = new HashSet<String>(ldapAttributeNamesSet);
  
      LdapToSqlSyncColumn multiValuedIdColumnObject = null;
      
      for (LdapToSqlSyncColumn ldapToSqlSyncColumn : GrouperUtil.nonNull(this.ldapToSqlSyncColumns).values()) {
        if (!StringUtils.isBlank(ldapToSqlSyncColumn.getLdapName())) {
          ldapAttributeNamesSet.add(ldapToSqlSyncColumn.getLdapName());
          attributesInMainTable.add(ldapToSqlSyncColumn.getLdapName());
          
          if (this.hasMultiValuedTable && StringUtils.equalsIgnoreCase(ldapToSqlSyncColumn.getSqlColumn(), this.multiValuedIdColumn)) {
            multiValuedIdColumnObject = ldapToSqlSyncColumn;
          }
          
        }
      }
  
      Set<String> multiValuedAttributesSet = null;
      if (this.hasMultiValuedTable) {
        multiValuedAttributesSet = GrouperUtil.splitTrimToSet(this.multiValuedAttributes, ",");
        ldapAttributeNamesSet.addAll(multiValuedAttributesSet);
      }
      
      this.ldapAttributeNames = GrouperUtil.toArray(ldapAttributeNamesSet, String.class);
      this.ldapAttributeNameIndex = new HashMap<String, Integer>();
  
      for (int i=0;i<GrouperUtil.length(this.ldapAttributeNames);i++) {
        this.ldapAttributeNameIndex.put(this.ldapAttributeNames[i], i);
      }
      
      List<LdapEntry> result = ldapSession.list(this.ldapConnection, this.baseDn, LdapSearchScope.valueOfIgnoreCase(this.searchScope, true), this.filter, this.ldapAttributeNames, null);
  
      this.ldapData = new ArrayList<Object[]>();
  
      this.ldapDataAttr = new ArrayList<Object[]>();
  
      for (LdapEntry ldapEntry : GrouperUtil.nonNull(result)) {
        Object[] values = new Object[this.ldapAttributeNames.length];
        for (int i=0;i<this.ldapAttributeNames.length;i++) {
          
          if (!this.hasMultiValuedTable || attributesInMainTable.contains(this.ldapAttributeNames[i])) {
            if (StringUtils.equalsIgnoreCase("dn", this.ldapAttributeNames[i])) {
              values[i] = ldapEntry.getDn();
            } else {
              Collection<String> stringValues = ldapEntry.getAttribute(this.ldapAttributeNames[i]).getStringValues();
              if (stringValues == null || stringValues.size() == 0) {
                values[i] = null;
              } else if (stringValues.size() == 1) {
                values[i] = stringValues.iterator().next();
              } else {
                stringValues = new TreeSet<String>(stringValues);
                values[i] = GrouperUtil.join(stringValues.iterator(), ',');
              }
            }
          }
        }
        this.ldapData.add(values);
      }
      this.debugMap.put("ldapRecords", GrouperUtil.length(this.ldapData));
      
      GcTableSyncTableBean gcTableSyncTableBeanFrom = new GcTableSyncTableBean();
      gcTableSync.setDataBeanFrom(gcTableSyncTableBeanFrom);
      gcTableSyncTableBeanFrom.setTableMetadata(this.gcTableSyncTableBeanSql.getTableMetadata());
      gcTableSyncTableBeanFrom.setGcTableSync(gcTableSync);
  
      // lets do attributes
      if (this.hasMultiValuedTable) {
        
        if (multiValuedIdColumnObject == null) {
          throw new RuntimeException("Cannot find multi-valued ID column: '" + this.multiValuedIdColumn + "', configure that column and cannot be a translation, must be direct LDAP attribute");
        }
        
        for (LdapEntry ldapEntry : GrouperUtil.nonNull(result)) {
  
          String id = null;
          
          try {
            if (StringUtils.equalsIgnoreCase("dn", multiValuedIdColumnObject.getLdapName())) {
              id = ldapEntry.getDn();
            } else {
              id = GrouperUtil.collectionPopOne(ldapEntry.getAttribute(multiValuedIdColumnObject.getLdapName()).getStringValues(), true);
            }
          } catch (RuntimeException e) {
            GrouperUtil.injectInException(e, "dn is: " + ldapEntry.getDn() + ", attr: " + multiValuedIdColumnObject.getLdapName() + ", values: " 
                + GrouperUtil.toStringForLog(ldapEntry.getAttribute(multiValuedIdColumnObject.getLdapName()).getStringValues(), 4000));
            throw e;
          }
  
          for (int i=0;i<this.ldapAttributeNames.length;i++) {
  
            if (multiValuedAttributesSet.contains(this.ldapAttributeNames[i])) {
              
              Collection<String> values = null;
              
              if (StringUtils.equalsIgnoreCase("dn", this.ldapAttributeNames[i])) {
                this.ldapDataAttr.add(new Object[] {id, this.ldapAttributeNames[i], ldapEntry.getDn()});
              } else {
                values = ldapEntry.getAttribute(this.ldapAttributeNames[i]).getStringValues();
                if (GrouperUtil.length(values) == 0) {
                  this.ldapDataAttr.add(new Object[] {id, this.ldapAttributeNames[i], null});
                } else {
                  for (String value : values) {
                    this.ldapDataAttr.add(new Object[] {id, this.ldapAttributeNames[i], value});
                  }
                }
              }
            }
          }
        }
        
        this.debugMap.put("ldapMultiValuedAttributes", GrouperUtil.length(this.ldapDataAttr));
        
        GcTableSyncTableBean gcTableSyncTableBeanFromAttr = new GcTableSyncTableBean();
        gcTableSyncAttr.setDataBeanFrom(gcTableSyncTableBeanFromAttr);
        gcTableSyncTableBeanFromAttr.setTableMetadata(this.gcTableSyncTableBeanSqlAttr.getTableMetadata());
        gcTableSyncTableBeanFromAttr.setGcTableSync(gcTableSyncAttr);
  
      }
    } finally {
      this.debugMap.put("retrieveDataLdapMillis", (System.nanoTime()-nowNanos)/1000);
    }
  }
  
}
