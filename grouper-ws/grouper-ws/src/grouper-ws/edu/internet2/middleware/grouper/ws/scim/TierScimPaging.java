/****
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

/**
 * <pre>
 * Server side limits and paging for SCIM list (search) responses: GET /v2/Groups?filter=... and
 * GET /v2/Memberships?filter=...
 *
 * Before this class the caller chose itemsPerPage with no upper bound, the group searches ran
 * unpaged (every group the caller can VIEW, then the full member list of each converted group),
 * and startIndex was refused.  Now:
 * - itemsPerPage is clamped to ws.scim.maxResultsPerPage (grouper-ws.properties, default 1000,
 *   the old MAX_RESULTS_PER_PAGE default when itemsPerPage was not sent)
 * - startIndex (1-based, RFC 7644 3.4.2.4) is supported, so clients can page instead of asking
 *   for everything at once
 * - the group searches are paged in the database (sorted by name so pages are stable)
 *
 * One version of this class serves every release (2.6.19 to 7.x): it only uses APIs that exist
 * unchanged in all of them, and it is plain Java 8.
 * </pre>
 */
public class TierScimPaging {

  /** config key in grouper-ws.properties: most results in one SCIM list response */
  public static final String MAX_RESULTS_PER_PAGE_CONFIG = "ws.scim.maxResultsPerPage";

  /** default for ws.scim.maxResultsPerPage, the old default page size */
  public static final int MAX_RESULTS_PER_PAGE_DEFAULT = 1000;

  /** the group column the paged group searches sort on (every search aliases the group theGroup) */
  private static final String GROUP_SORT_COLUMN = "theGroup.nameDb";

  /** 1-based index of the first result, from the startIndex query param */
  private final int startIndex;

  /** most results on this page, from itemsPerPage (SCIM "count"), already clamped */
  private final int count;

  /**
   * @param theStartIndex startIndex query param (null or blank means 1)
   * @param itemsPerPage itemsPerPage query param (null means the configured max)
   * @throws IllegalArgumentException if startIndex is not an integer (the providers answer 400)
   */
  public TierScimPaging(String theStartIndex, Integer itemsPerPage) {
    this.startIndex = parseStartIndex(theStartIndex);
    this.count = clampCount(itemsPerPage);
  }

  /**
   * @return the configured most results per SCIM list response (at least 1)
   */
  public static int maxResultsPerPage() {
    int max = GrouperWsConfig.retrieveConfig().propertyValueInt(MAX_RESULTS_PER_PAGE_CONFIG,
        MAX_RESULTS_PER_PAGE_DEFAULT);
    // a zero or negative setting would make SCIM searches return nothing; treat it as unset.
    // keep count + 1 (one row to see if there is a next page) an int
    if (max < 1) {
      return MAX_RESULTS_PER_PAGE_DEFAULT;
    }
    return Math.min(max, Integer.MAX_VALUE / 2);
  }

  /**
   * itemsPerPage clamped to [0, max].  RFC 7644: a negative count is treated as 0
   * @param itemsPerPage null means the max
   * @return the count
   */
  public static int clampCount(Integer itemsPerPage) {
    int max = maxResultsPerPage();
    if (itemsPerPage == null) {
      return max;
    }
    if (itemsPerPage < 0) {
      return 0;
    }
    return Math.min(itemsPerPage, max);
  }

  /**
   * RFC 7644: a startIndex less than 1 is treated as 1
   * @param theStartIndex
   * @return the 1-based start index
   */
  public static int parseStartIndex(String theStartIndex) {
    if (GrouperUtil.isBlank(theStartIndex)) {
      return 1;
    }
    long value;
    try {
      value = Long.parseLong(theStartIndex.trim());
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("startIndex must be an integer");
    }
    if (value < 1) {
      return 1;
    }
    // far past any real result set; keep it an int (and start + count) for the query paging
    return (int)Math.min(value, Integer.MAX_VALUE / 2);
  }

  /**
   * @return 1-based index of the first result
   */
  public int getStartIndex() {
    return this.startIndex;
  }

  /**
   * @return most results on this page
   */
  public int getCount() {
    return this.count;
  }

  /**
   * <pre>
   * Query options for a group search (the GroupFinder *Secure methods): sorted by name, from
   * startIndex, at most count + 1 rows.  The extra row only says whether there is a next page
   * (see totalResults), so no count(*) query is needed.  Do not call when count is 0: a
   * hibernate max results of 0 means no limit.
   * </pre>
   * @return the query options
   */
  public QueryOptions groupQueryOptions() {
    if (this.count < 1) {
      throw new IllegalStateException("count is 0, do not query");
    }
    QueryPaging queryPaging = new QueryPaging();
    queryPaging.setPageSize(this.count + 1);
    // 0-based first row, not a page number, so any startIndex works
    queryPaging.setPageStartIndexQueryByIndex(this.startIndex - 1);
    return new QueryOptions().sortAsc(GROUP_SORT_COLUMN).paging(queryPaging);
  }

  /**
   * groups in name order (the DAO post filter may return a set without order), nulls dropped
   * @param groups
   * @return a new sorted list
   */
  public static List<Group> sortByName(Collection<Group> groups) {
    List<Group> list = new ArrayList<Group>();
    for (Group group : GrouperUtil.nonNull(groups)) {
      if (group != null) {
        list.add(group);
      }
    }
    Collections.sort(list, new Comparator<Group>() {

      public int compare(Group o1, Group o2) {
        String name1 = o1.getName() == null ? "" : o1.getName();
        String name2 = o2.getName() == null ? "" : o2.getName();
        return name1.compareTo(name2);
      }
    });
    return list;
  }

  /**
   * groups of one database page (found with groupQueryOptions) in name order, trimmed to count
   * @param groups found with groupQueryOptions
   * @return at most count groups
   */
  public List<Group> groupPage(Collection<Group> groups) {
    List<Group> list = sortByName(groups);
    return list.size() > this.count ? new ArrayList<Group>(list.subList(0, this.count)) : list;
  }

  /**
   * <pre>
   * totalResults for a page found with groupQueryOptions.  The total is not counted (that would
   * be another query over every match); this is exact on the last page and, when a next page
   * exists, one more than the results up to here, so a client that pages while
   * startIndex &lt;= totalResults asks for the next page and stops after the last one.
   * </pre>
   * @param fetched how many rows the database page returned (up to count + 1)
   * @return totalResults
   */
  public int groupTotalResults(int fetched) {
    int onPage = Math.min(fetched, this.count);
    int total = this.startIndex - 1 + onPage;
    if (fetched > this.count) {
      total++;
    }
    return total;
  }

  /**
   * <pre>
   * One page of a result list that was already loaded (and bounded) elsewhere, e.g. the
   * membership search, whose size is limited by ws.getMemberships.maxResultSize.  The caller
   * sorts the list first so pages are stable.
   * </pre>
   * @param all every result, in a stable order
   * @param <T> result type
   * @return the results from startIndex, at most count
   */
  public <T> List<T> page(List<T> all) {
    List<T> list = GrouperUtil.nonNull(all);
    int from = this.startIndex - 1;
    if (from >= list.size() || this.count < 1) {
      return new ArrayList<T>();
    }
    int to = (int)Math.min((long)from + this.count, list.size());
    return new ArrayList<T>(list.subList(from, to));
  }

}
