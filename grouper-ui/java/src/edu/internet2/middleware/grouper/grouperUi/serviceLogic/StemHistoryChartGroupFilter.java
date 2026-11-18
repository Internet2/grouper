/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Limits the folder membership history chart (UiV2Stem.viewHistoryChartResults, graph and
 * CSV export) to the descendant groups whose memberships the logged in user can READ.</p>
 *
 * <p>Why: the chart is open to anyone who can see the folder, and its SQL over
 * grouper_pit_memberships_all_v took every group whose name starts with the folder name.  That
 * showed membership add and remove counts and times of private groups the user cannot READ
 * (the group chart needs READ on the group).  Users who can READ every group (wheel, root,
 * read-only root) keep the unfiltered query, which also counts deleted groups.  Everyone else
 * gets the groups currently under the folder that they can READ; history of deleted groups is
 * left out for them because there is no group left to check READ on.</p>
 *
 * <p>The chart queries end with "where ... g.name like ?" plus optional more conditions; this
 * class appends "and g.source_id in (...)" in batches of 100 ids (pit group source_id is the
 * group id) and adds the results of the batches together.</p>
 */
public class StemHistoryChartGroupFilter {

  /** ids per IN clause */
  private static final int BATCH_SIZE = 100;

  /** ids of the readable groups, or null if the user can read every group (no filter) */
  private List<String> readableGroupIds;

  /**
   * @param stem folder of the chart
   * @param subject logged in subject, must be the subject of the current grouper session
   */
  public StemHistoryChartGroupFilter(Stem stem, Subject subject) {
    if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
      this.readableGroupIds = null;
      return;
    }
    // GroupFinder filters by READ (or ADMIN) for the session subject
    Set<Group> groups = new GroupFinder().assignParentStemId(stem.getId()).assignStemScope(Scope.SUB)
        .assignPrivileges(AccessPrivilege.READ_PRIVILEGES).findGroups();
    this.readableGroupIds = new ArrayList<String>();
    for (Group group : GrouperUtil.nonNull(groups)) {
      this.readableGroupIds.add(group.getId());
    }
  }

  /**
   * @return true if the chart is limited to some groups (the user cannot read every group)
   */
  public boolean isFiltered() {
    return this.readableGroupIds != null;
  }

  /**
   * run a chart query that returns rows
   * @param sql query whose last condition is on the pit group "g" (binds: stem scope, then extraBindVars)
   * @param stemScopeString e.g. a:b:%
   * @param extraBindVars bind vars after the stem scope
   * @return the rows of all batches
   */
  public List<Object[]> selectRows(String sql, String stemScopeString, Object... extraBindVars) {
    if (this.readableGroupIds == null) {
      return newQuery(sql, stemScopeString, extraBindVars, null).selectList(Object[].class);
    }
    List<Object[]> result = new ArrayList<Object[]>();
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(this.readableGroupIds, BATCH_SIZE);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> batch = GrouperUtil.batchList(this.readableGroupIds, BATCH_SIZE, i);
      result.addAll(newQuery(sql, stemScopeString, extraBindVars, batch).selectList(Object[].class));
    }
    return result;
  }

  /**
   * run the earliest-activity query (a single MIN value)
   * @param sql query whose last condition is on the pit group "g" (one bind: stem scope)
   * @param stemScopeString e.g. a:b:%
   * @return the smallest value of all batches, or null if no rows
   */
  public Long selectMin(String sql, String stemScopeString) {
    if (this.readableGroupIds == null) {
      return newQuery(sql, stemScopeString, null, null).select(Long.class);
    }
    Long result = null;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(this.readableGroupIds, BATCH_SIZE);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> batch = GrouperUtil.batchList(this.readableGroupIds, BATCH_SIZE, i);
      Long value = newQuery(sql, stemScopeString, null, batch).select(Long.class);
      if (value != null && (result == null || value < result)) {
        result = value;
      }
    }
    return result;
  }

  /**
   * @param sql
   * @param stemScopeString
   * @param extraBindVars
   * @param groupIdBatch null for no group id filter
   * @return the query with bind vars
   */
  private static GcDbAccess newQuery(String sql, String stemScopeString, Object[] extraBindVars, List<String> groupIdBatch) {
    StringBuilder fullSql = new StringBuilder(sql);
    if (groupIdBatch != null) {
      fullSql.append(" and g.source_id in (");
      for (int i = 0; i < groupIdBatch.size(); i++) {
        fullSql.append(i == 0 ? "?" : ", ?");
      }
      fullSql.append(")");
    }
    GcDbAccess gcDbAccess = new GcDbAccess().sql(fullSql.toString()).addBindVar(stemScopeString);
    if (extraBindVars != null) {
      for (Object bindVar : extraBindVars) {
        gcDbAccess.addBindVar(bindVar);
      }
    }
    if (groupIdBatch != null) {
      for (String groupId : groupIdBatch) {
        gcDbAccess.addBindVar(groupId);
      }
    }
    return gcDbAccess;
  }
}
