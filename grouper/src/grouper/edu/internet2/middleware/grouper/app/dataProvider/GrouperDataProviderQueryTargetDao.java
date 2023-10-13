package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public abstract class GrouperDataProviderQueryTargetDao {
  
  private GrouperDataProviderQuery grouperDataProviderQuery;
  private GrouperDataProviderChangeLogQuery grouperDataProviderChangeLogQuery;

  
  public GrouperDataProviderQuery getGrouperDataProviderQuery() {
    return grouperDataProviderQuery;
  }

  
  public void setGrouperDataProviderQuery(GrouperDataProviderQuery grouperDataProviderQuery) {
    this.grouperDataProviderQuery = grouperDataProviderQuery;
  }
  
  public GrouperDataProviderChangeLogQuery getGrouperDataProviderChangeLogQuery() {
    return grouperDataProviderChangeLogQuery;
  }

  
  public void setGrouperDataProviderChangeLogQuery(GrouperDataProviderChangeLogQuery grouperDataProviderChangeLogQuery) {
    this.grouperDataProviderChangeLogQuery = grouperDataProviderChangeLogQuery;
  }

  public abstract List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex);
  
  public abstract List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex);
}
