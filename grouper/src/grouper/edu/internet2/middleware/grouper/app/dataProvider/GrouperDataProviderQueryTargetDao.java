package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;

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
  
  public abstract List<Object[]> selectDataByMembers(Map<String, Integer> lowerColumnNameToZeroIndex, Set<Member> members);
  
  public abstract List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex);
}
