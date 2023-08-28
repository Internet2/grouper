package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public abstract class GrouperDataProviderQueryTargetDao {
  
  private GrouperDataProviderQuery grouperDataProviderQuery;

  
  public GrouperDataProviderQuery getGrouperDataProviderQuery() {
    return grouperDataProviderQuery;
  }

  
  public void setGrouperDataProviderQuery(GrouperDataProviderQuery grouperDataProviderQuery) {
    this.grouperDataProviderQuery = grouperDataProviderQuery;
  }

  public abstract List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex);
}
