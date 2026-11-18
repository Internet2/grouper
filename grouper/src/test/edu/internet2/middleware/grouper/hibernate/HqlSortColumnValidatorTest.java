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

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security advisory C2 (ws sortString HQL injection): ByHql concatenated every QuerySortField
 * column into the HQL unvalidated, in the order by and in the cursor paging where clause.
 * Web service find/get operations and the MCP find_groups tool pass a caller supplied
 * sortString through, so a caller could put a subquery in the sort (a one bit oracle that
 * ignores privileges) or an "or" in the cursor where clause (rows past the privilege filter).
 *
 * The fix validates every sort column in ByHql.attachQueryInfo with HqlSortColumnValidator.
 * This test fails to compile before the fix (the validator does not exist).
 * </pre>
 */
public class HqlSortColumnValidatorTest extends GrouperTest {

  /**
   * the confirmed oracle payload from the security report, shortened (no commas, since QuerySort
   * splits on commas)
   */
  private static final String ORACLE_PAYLOAD = "case when (select count(g.uuid) from Group g "
      + "where g.nameDb = 'test:secret') > 0 then theGroup.nameDb else theGroup.uuid end";

  /**
   * a cursor paging payload: "&lt;privilege filter&gt; and 1=1 or theGroup.nameDb &gt; :c"
   * would return rows the caller cannot see
   */
  private static final String CURSOR_OR_PAYLOAD = "1=1 or theGroup.nameDb";

  /**
   *
   */
  public HqlSortColumnValidatorTest() {
    super();
  }

  /**
   * @param name
   */
  public HqlSortColumnValidatorTest(String name) {
    super(name);
  }

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HqlSortColumnValidatorTest("testSortInjectionThroughGroupFinder"));
  }

  /**
   * every sort column Grouper itself sends (UI, WS, MCP, GSH, DAOs, documented WS values)
   * must still be accepted, so the fix only rejects injection
   */
  public void testGrouperSortColumnsAccepted() {

    String[] grouperColumns = new String[] {
        // WS documented values, MCP find_groups values and the grouperClient default
        "name", "displayName", "extension", "displayExtension", "uuid", "subjectId", "sourceId",
        "sortString0", "sortString4", "m.id", "description",
        // after Hib3*DAO.massageSortFields renames
        "theGroup.nameDb", "theGroup.displayNameDb", "theGroup.extensionDb",
        "theGroup.displayExtensionDb", "ns.displayNameDb", "theAttributeDef.nameDb",
        "theAttributeDefName.displayNameDb", "theAttributeDefName.description",
        "m.subjectIdDb", "m.subjectSourceIdDb", "m.sortString0", "m.subjectTypeId",
        // other literals in the code base
        "id", "lastUpdated", "lastUpdatedDb", "createdOnDb", "startedTime", "startTimeDb",
        "inTargetStart", "inTargetEnd", "primaryEmail", "thePITGroup.nameDb", "theGroup.id",
        "theMember.subjectIdDb", "ss.ifHasStemId", "pea.roleDisplayName", "subject_id",
        "attributeSystemName", "parentThenHasName", "depth", "ifHasRoleName", "col1",
        // multi column sorts that a non splitting method (insertSortToBeginning) could pass whole
        "sourceId, subjectId", "ms.createTimeLong, ms.immediateMembershipId",
        // surrounding whitespace cannot change the HQL
        " name ",
    };
    for (String column : grouperColumns) {
      assertTrue("Grouper sort column must be accepted: '" + column + "'",
          HqlSortColumnValidator.isValidSortColumn(column));
      HqlSortColumnValidator.assertValidSortColumn(column);
    }

    // QuerySort as built by QueryOptions.create and the WS
    HqlSortColumnValidator.assertValidQuerySort(null);
    HqlSortColumnValidator.assertValidQuerySort(new QuerySort("sourceId, subjectId", true));
    HqlSortColumnValidator.assertValidQuerySort(QuerySort.desc("theGroup.nameDb"));
  }

  /**
   * expressions, keywords, operators and direction inside the column are rejected
   */
  public void testInjectionColumnsRejected() {

    String[] badColumns = new String[] {
        ORACLE_PAYLOAD,
        CURSOR_OR_PAYLOAD,
        "(select 1 from Group)",
        "nameDb desc",
        "name asc",
        "name;",
        "name--",
        "lower(name)",
        "theGroup.nameDb, (select 1 from Group)",
        "name,",
        "1name",
        "theGroup..nameDb",
        "theGroup.",
        "'name'",
        "",
        "   ",
        null,
    };
    for (String column : badColumns) {
      assertFalse("sort column must be rejected: '" + column + "'",
          HqlSortColumnValidator.isValidSortColumn(column));
      try {
        HqlSortColumnValidator.assertValidSortColumn(column);
        fail("expected IllegalArgumentException for '" + column + "'");
      } catch (IllegalArgumentException iae) {
        // expected, and the message must not echo a long payload in full
        assertTrue(iae.getMessage(), iae.getMessage().length() < 200);
      }
    }

    try {
      HqlSortColumnValidator.assertValidQuerySort(QuerySort.asc(ORACLE_PAYLOAD));
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  /**
   * through the real path: a non-privileged subject finds groups with an injected sort, with
   * and without cursor paging.  The query must fail and never return the hidden group;
   * plain sorts still work
   */
  public void testSortInjectionThroughGroupFinder() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    new GroupSave(rootSession).assignName("test:visible")
        .assignCreateParentStemsIfNotExist(true).save();
    Group secret = new GroupSave(rootSession).assignName("test:secret")
        .assignCreateParentStemsIfNotExist(true).save();

    // GrouperTest grants GrouperAll READ and VIEW on create, so take both away from the secret
    secret.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    secret.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

    Stem testStem = StemFinder.findByName(rootSession, "test", true);
    String testStemId = testStem.getId();
    GrouperSession.stopQuietly(rootSession);

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {

      // control: plain sort works and the secret group is filtered.  GroupFinder only applies a
      // privilege filter when privileges are assigned, otherwise it returns every group
      Set<Group> groups = new GroupFinder().assignParentStemId(testStemId)
          .assignStemScope(Scope.ONE).assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().sortAsc("name")).findGroups();
      assertEquals(1, groups.size());
      assertEquals("test:visible", groups.iterator().next().getName());

      // control: cursor paging on a plain column works and the secret group is filtered
      groups = new GroupFinder().assignParentStemId(testStemId).assignStemScope(Scope.ONE)
          .assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().sortAsc("theGroup.nameDb")
              .pagingCursor(10, "test:", false, false)).findGroups();
      assertEquals(1, groups.size());
      assertEquals("test:visible", groups.iterator().next().getName());

      // oracle payload in the order by
      assertInjectionRejected(testStemId, new QueryOptions().sortAsc(ORACLE_PAYLOAD));

      // "or" in the cursor paging where clause, which would widen past the privilege filter
      assertInjectionRejected(testStemId, new QueryOptions().sortAsc(CURSOR_OR_PAYLOAD)
          .pagingCursor(10, "test:", false, false));

      // direction smuggled into the column
      assertInjectionRejected(testStemId, new QueryOptions().sortAsc("theGroup.nameDb desc"));

    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
  }

  /**
   * run the find with the given options and make sure it throws the validator error and
   * never returns the secret group
   * @param testStemId
   * @param queryOptions
   */
  private void assertInjectionRejected(String testStemId, QueryOptions queryOptions) {
    Set<Group> groups = null;
    try {
      groups = new GroupFinder().assignParentStemId(testStemId).assignStemScope(Scope.ONE)
          .assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES)
          .assignQueryOptions(queryOptions).findGroups();
    } catch (RuntimeException re) {
      // the validator error may be wrapped by the DAO layer, so look through the causes
      Throwable throwable = re;
      while (throwable != null && !(throwable instanceof IllegalArgumentException)) {
        throwable = throwable.getCause();
      }
      assertNotNull("expected the sort validator to reject the query, got: " + re, throwable);
      assertTrue(throwable.getMessage(), throwable.getMessage().contains("Invalid sort field"));
      return;
    }
    for (Group group : groups) {
      assertFalse("injected sort leaked a hidden group", "test:secret".equals(group.getName()));
    }
    fail("injected sort must be rejected, returned " + groups.size() + " groups");
  }

}
