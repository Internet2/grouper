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
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;

/**
 * <pre>
 * Validates the sort columns of a QuerySort before ByHql concatenates them into HQL.
 *
 * ByHql appends each QuerySortField column to the HQL text twice: in the "order by" clause,
 * and (for cursor based paging) in the where clause as "column &gt; :lastCursorField".  Some
 * callers (web service find/get operations and their lite forms, the MCP find_groups tool)
 * pass a caller supplied sortString straight through, so without a check a caller can put a
 * subquery or an "or" in the sort and read data past the privilege filter.
 *
 * Every sort column Grouper itself sends is a plain HQL property path: an identifier
 * (name, uuid, lastUpdated, sortString0) or a dotted path (theGroup.nameDb, m.subjectIdDb,
 * ms.createTimeLong), or a comma separated list of those.  The direction is never part of
 * the column (QuerySort.sortString adds asc / desc itself), so the column must be exactly
 * property paths and nothing else.
 *
 * This class only uses java.util.regex and QuerySort / QuerySortField, so the same file works
 * on every Grouper release.
 * </pre>
 */
public class HqlSortColumnValidator {

  /**
   * an HQL property path: identifier, optionally followed by .identifier any number of times.
   * No spaces, parens, quotes, operators, commas or keywords can match
   */
  private static final Pattern VALID_SORT_COLUMN = Pattern.compile(
      "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$");

  /**
   * dont echo a whole injection payload back in the exception message
   */
  private static final int MAX_COLUMN_IN_MESSAGE = 40;

  /**
   * no instances
   */
  private HqlSortColumnValidator() {
  }

  /**
   * see if a sort column is a plain HQL property path.  Surrounding whitespace is allowed
   * since it cannot change the meaning of the HQL.  A comma separated list of property paths
   * is also allowed: QuerySort normally splits "sourceId, subjectId" into two fields, but
   * QuerySort.insertSortToBeginning and QuerySortField.setColumn do not split, and a list of
   * plain property paths is just as safe in the order by
   * @param column the sort column
   * @return true if the column is safe to concatenate into HQL
   */
  public static boolean isValidSortColumn(String column) {
    if (column == null) {
      return false;
    }
    // -1 keeps trailing empty pieces, so "name," is rejected rather than silently allowed
    String[] pieces = column.split(",", -1);
    for (String piece : pieces) {
      if (!VALID_SORT_COLUMN.matcher(piece.trim()).matches()) {
        return false;
      }
    }
    return true;
  }

  /**
   * throw if a sort column is not a plain HQL property path
   * @param column the sort column
   * @throws IllegalArgumentException if the column is not valid
   */
  public static void assertValidSortColumn(String column) {
    if (!isValidSortColumn(column)) {
      throw new IllegalArgumentException("Invalid sort field, must be a field name like "
          + "name or theGroup.nameDb: '" + abbreviate(column) + "'");
    }
  }

  /**
   * throw if any sort column of the query sort is not a plain HQL property path.  Null or
   * empty sorts are fine (nothing is concatenated)
   * @param querySort the sort, may be null
   * @throws IllegalArgumentException if any column is not valid
   */
  public static void assertValidQuerySort(QuerySort querySort) {
    if (querySort == null) {
      return;
    }
    List<QuerySortField> querySortFields = querySort.getQuerySortFields();
    if (querySortFields == null) {
      return;
    }
    for (QuerySortField querySortField : querySortFields) {
      // a null field would only be a programming error, but dont let it NPE the check
      assertValidSortColumn(querySortField == null ? null : querySortField.getColumn());
    }
  }

  /**
   * shorten a rejected value for the error message
   * @param column the value
   * @return the shortened value
   */
  private static String abbreviate(String column) {
    if (column == null) {
      return "null";
    }
    if (column.length() <= MAX_COLUMN_IN_MESSAGE) {
      return column;
    }
    return column.substring(0, MAX_COLUMN_IN_MESSAGE) + "...";
  }
}
