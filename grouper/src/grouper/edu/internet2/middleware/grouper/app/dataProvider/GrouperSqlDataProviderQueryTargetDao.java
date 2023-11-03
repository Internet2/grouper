package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperSqlDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

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
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId());
      
      int batchSize = 200;
      List<Member> membersList = new ArrayList<Member>(members);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(membersList.size(), batchSize, true);
      for (int i=0;i<numberOfBatches;i++) {
        List<Member> batchMembers = GrouperUtil.batchList(membersList, batchSize, i);
        
        StringBuilder sql = new StringBuilder("select * from (" + grouperDataProviderQueryConfig.getProviderQuerySqlQuery() + ") innerQuery where ");
        sql.append(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute() + " in (");
        GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMembers));
        sql.append(")");
        
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
  public List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex) {
    GrouperSqlDataProviderChangeLogQueryConfig grouperDataProviderChangeLogQueryConfig = (GrouperSqlDataProviderChangeLogQueryConfig)this.getGrouperDataProviderChangeLogQuery().retrieveGrouperDataProviderChangeLogQueryConfig();

    List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlConfigId()).sql(grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlQuery()).selectList(Object[].class));

    GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlConfigId(), grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySqlQuery());

    this.getGrouperDataProviderChangeLogQuery().getGrouperDataProviderSync().getGrouperDataEngine().getQueryConfigIdToTableMetadata().put(grouperDataProviderChangeLogQueryConfig.getConfigId(), tableMetadata);
    
    List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
      lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
    }
    
    return rows;
  }
}
