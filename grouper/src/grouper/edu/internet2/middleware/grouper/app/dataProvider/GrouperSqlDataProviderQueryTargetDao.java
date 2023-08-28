package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

public class GrouperSqlDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

  @Override
  public List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex) {
    GrouperSqlDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperSqlDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId()).sql(grouperDataProviderQueryConfig.getProviderQuerySqlQuery()).selectList(Object[].class));

    GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId(), grouperDataProviderQueryConfig.getProviderQuerySqlQuery());

    this.getGrouperDataProviderQuery().getGrouperDataProviderSync().getGrouperDataEngine().getQueryConfigIdToTableMetadata().put(grouperDataProviderQueryConfig.getConfigId(), tableMetadata);
    
    List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
    for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
      lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
    }
    
    return rows;
  }
}
