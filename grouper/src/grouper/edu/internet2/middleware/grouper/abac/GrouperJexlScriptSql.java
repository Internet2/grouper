package edu.internet2.middleware.grouper.abac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperJexlScriptSql {

  private Set<Long> groupInternalIds = new HashSet<Long>();
  private Set<Long> attributeInternalIds = new HashSet<Long>();
  private Set<Long> rowInternalIds = new HashSet<Long>();
  private String whereClause;
  
  private Map<MultiKey, SqlCacheGroup> allSqlCacheGroupsForCurrentJexl = new HashMap<>();
  
  public Set<Long> getGroupInternalIds() {
    return groupInternalIds;
  }
  
  public void setGroupInternalIds(Set<Long> groupInternalIds) {
    this.groupInternalIds = groupInternalIds;
  }
  
  public Set<Long> getAttributeInternalIds() {
    return attributeInternalIds;
  }
  
  public void setAttributeInternalIds(Set<Long> attributeInternalIds) {
    this.attributeInternalIds = attributeInternalIds;
  }
  
  public Set<Long> getRowInternalIds() {
    return rowInternalIds;
  }
  
  public void setRowInternalIds(Set<Long> rowInternalIds) {
    this.rowInternalIds = rowInternalIds;
  }

  
  public String getWhereClause() {
    return whereClause;
  }

  
  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
  }
  
  public Map<MultiKey, SqlCacheGroup> getAllSqlCacheGroupsForCurrentJexl() {
    return allSqlCacheGroupsForCurrentJexl;
  }
  
  public void setAllSqlCacheGroupsForCurrentJexl(
      Map<MultiKey, SqlCacheGroup> allSqlCacheGroupsForCurrentJexl) {
    this.allSqlCacheGroupsForCurrentJexl = allSqlCacheGroupsForCurrentJexl;
  }

}
