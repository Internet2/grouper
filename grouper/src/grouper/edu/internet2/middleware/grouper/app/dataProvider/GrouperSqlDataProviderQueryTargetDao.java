package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperSqlDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

  /**
   * {@inheritDoc}
   * wraps the configured query as a subquery, projects only the subject id column,
   * and selects distinct lowercased subject ids, ordered by the database:
   * SELECT DISTINCT LOWER(col) FROM (SELECT col FROM (...) innerQuery) outerQuery ORDER BY 1
   */
  @Override
  public List<String> selectDistinctSubjectIds() {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();
    String sql = "SELECT DISTINCT LOWER(" + subjectIdAttribute + ") FROM (SELECT " + subjectIdAttribute + " FROM (" + grouperDataProviderQueryConfig.getProviderQuerySqlQuery() + ") innerQuery) outerQuery ORDER BY 1";

    return new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId()).sql(sql).selectList(String.class);
  }

  /**
   * {@inheritDoc}
   * wraps the configured query as a subquery and filters with
   * WHERE LOWER(col) >= ? AND LOWER(col) <= ? using bind variables for the range bounds
   */
  @Override
  public List<Object[]> selectDataBySubjectIdRange(Map<String, Integer> lowerColumnNameToZeroIndex, String fromSubjectIdLower, String toSubjectIdLower) {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();
    String sql = "SELECT * FROM (" + grouperDataProviderQueryConfig.getProviderQuerySqlQuery() + ") innerQuery WHERE LOWER(" + subjectIdAttribute + ") >= ? AND LOWER(" + subjectIdAttribute + ") <= ?";

    List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId())
        .sql(sql).addBindVar(fromSubjectIdLower).addBindVar(toSubjectIdLower).selectList(Object[].class));

    retrieveMetadata(lowerColumnNameToZeroIndex);
    return rows;
  }

  /**
   * {@inheritDoc}
   * wraps the configured query as a subquery and filters with
   * WHERE LOWER(col) IN (?,?,...). batches in groups of 800 to stay within
   * database bind variable limits (e.g. Oracle's 1000 limit)
   */
  @Override
  public List<Object[]> selectDataBySubjectIds(Map<String, Integer> lowerColumnNameToZeroIndex, List<String> subjectIdsLower) {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    List<Object[]> rows = new ArrayList<Object[]>();

    if (subjectIdsLower.size() > 0) {
      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();
      int batchSize = 800;

      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectIdsLower.size(), batchSize, true);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> batchSubjectIds = GrouperUtil.batchList(subjectIdsLower, batchSize, i);

        StringBuilder sql = new StringBuilder("SELECT * FROM (" + grouperDataProviderQueryConfig.getProviderQuerySqlQuery() + ") innerQuery WHERE LOWER(" + subjectIdAttribute + ") IN (");
        GrouperClientUtils.appendQuestions(sql, batchSubjectIds.size());
        sql.append(")");

        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId());
        for (String subjectId : batchSubjectIds) {
          gcDbAccess.addBindVar(subjectId);
        }

        rows.addAll(GrouperUtil.nonNull(gcDbAccess.sql(sql.toString()).selectList(Object[].class)));
      }
    }

    retrieveMetadata(lowerColumnNameToZeroIndex);
    return rows;
  }

  @Override
  public List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex) {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId()).sql(grouperDataProviderQueryConfig.getProviderQuerySqlQuery()).selectList(Object[].class));
    
    retrieveMetadata(lowerColumnNameToZeroIndex);
    return rows;
  }
  
  @Override
  public List<Object[]> selectDataByMembers(Map<String, Integer> lowerColumnNameToZeroIndex, Set<Member> members) {
    
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    List<Object[]> rows = new ArrayList<Object[]>();
    
    if (members.size() > 0) {      
      int batchSize = 200;
      List<Member> membersList = new ArrayList<Member>(members);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(membersList.size(), batchSize, true);
      for (int i=0;i<numberOfBatches;i++) {
        List<Member> batchMembers = GrouperUtil.batchList(membersList, batchSize, i);
        
        StringBuilder sql = new StringBuilder("select * from (" + grouperDataProviderQueryConfig.getProviderQuerySqlQuery() + ") innerQuery where ");
        sql.append(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute() + " in (");
        GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMembers));
        sql.append(")");
        
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId());

        for (Member member : batchMembers) {
          if ("subjectIdentifier".equals(grouperDataProviderQueryConfig.getProviderQuerySubjectIdType())) {
            // we probably shouldn't assume this is subjectIdentifier0???
            gcDbAccess.addBindVar(member.getSubjectIdentifier0());
          } else {
            gcDbAccess.addBindVar(member.getSubjectId());
          }
        }
        
        rows.addAll(GrouperUtil.nonNull(gcDbAccess.sql(sql.toString()).selectList(Object[].class)));
      }
    }
    
    retrieveMetadata(lowerColumnNameToZeroIndex);
    return rows;
  }
  
  private void retrieveMetadata(Map<String, Integer> lowerColumnNameToZeroIndex) {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId(), grouperDataProviderQueryConfig.getProviderQuerySqlQuery());

    this.getGrouperDataProviderQuery().getGrouperDataProviderSync().getGrouperDataEngine().getQueryConfigIdToTableMetadata().put(grouperDataProviderQueryConfig.getConfigId(), tableMetadata);
    
    List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
      lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
    }
  }
  
  @Override
  public List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex, Timestamp changesFromTimestamp, Timestamp changesToTimestamp) {
    if (changesToTimestamp == null) {
      throw new RuntimeException("changesToTimestamp is null!");
    }
    
    GrouperSqlDataProviderChangeLogQueryConfig grouperDataProviderChangeLogQueryConfig = (GrouperSqlDataProviderChangeLogQueryConfig)this.getGrouperDataProviderChangeLogQuery().retrieveGrouperDataProviderChangeLogQueryConfig();
    String timestampColumnName = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQueryTimestampAttribute();    
    Boolean timestampColumnIsNumeric = null;
    
    GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlConfigId(), grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlQuery());

    this.getGrouperDataProviderChangeLogQuery().getGrouperDataProviderSync().getGrouperDataEngine().getQueryConfigIdToTableMetadata().put(grouperDataProviderChangeLogQueryConfig.getConfigId(), tableMetadata);
    
    List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
      lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
      
      if (timestampColumnName.toLowerCase().equals(columnMetadata.getColumnName().toLowerCase())) {
        if (columnMetadata.getColumnType() == ColumnType.NUMERIC) {
          timestampColumnIsNumeric = true;
        } else if (columnMetadata.getColumnType() == ColumnType.TIMESTAMP) {
          timestampColumnIsNumeric = false;
        } else {
          throw new RuntimeException("Unexpected column type of " + columnMetadata.getColumnType() + " for " + timestampColumnName + ". Expecting numeric or timestamp.");
        }
      }
    }
    
    if (timestampColumnIsNumeric == null) {
      throw new RuntimeException("Didn't find column " + timestampColumnName + " in metadata, select that column!");
    }
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlConfigId());
        
    StringBuilder sql = new StringBuilder("select * from (" + grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlQuery() + ") innerQuery where ");
    
    if (changesFromTimestamp != null) {
      sql.append(timestampColumnName + " > ? and ");
      if (timestampColumnIsNumeric) {
        gcDbAccess.addBindVar(changesFromTimestamp.getTime());
      } else {
        gcDbAccess.addBindVar(changesFromTimestamp);
      }
    }
    
    sql.append(timestampColumnName + " <= ? ");
    if (timestampColumnIsNumeric) {
      gcDbAccess.addBindVar(changesToTimestamp.getTime());
    } else {
      gcDbAccess.addBindVar(changesToTimestamp);
    }

    List<Object[]> rows = GrouperUtil.nonNull(gcDbAccess.sql(sql.toString()).selectList(Object[].class));
    
    return rows;
  }
}
