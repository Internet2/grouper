package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
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

  /**
   * select distinct lowercased subject ids from the source, ordered.
   * for SQL this uses ORDER BY in the query.
   * for LDAP this sorts in Java after retrieval.
   * @return ordered list of distinct lowercased subject ids
   */
  public abstract List<String> selectDistinctSubjectIds();

  /**
   * select data for subjects whose lowercased subject id falls within the given range (inclusive).
   * @param lowerColumnNameToZeroIndex
   * @param fromSubjectIdLower lower bound (inclusive), lowercased
   * @param toSubjectIdLower upper bound (inclusive), lowercased
   * @return
   */
  public abstract List<Object[]> selectDataBySubjectIdRange(Map<String, Integer> lowerColumnNameToZeroIndex, String fromSubjectIdLower, String toSubjectIdLower);

  /**
   * select data for specific lowercased subject ids. batches internally to avoid
   * exceeding bind variable limits.
   * @param lowerColumnNameToZeroIndex
   * @param subjectIdsLower lowercased subject ids to look up
   * @return
   */
  public abstract List<Object[]> selectDataBySubjectIds(Map<String, Integer> lowerColumnNameToZeroIndex, List<String> subjectIdsLower);

  public abstract List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex);

  public abstract List<Object[]> selectDataByMembers(Map<String, Integer> lowerColumnNameToZeroIndex, Set<Member> members);
  
  /**
   * @param lowerColumnNameToZeroIndex
   * @param changesFromTimestamp get changes after this timestamp, could be null
   * @param changesToTimestamp get changes to and including this timestamp
   * @return
   */
  public abstract List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex, Timestamp changesFromTimestamp, Timestamp changesToTimestamp);
}
