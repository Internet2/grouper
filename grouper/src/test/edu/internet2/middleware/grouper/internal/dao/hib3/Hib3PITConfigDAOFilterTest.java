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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security advisory C2 (configuration history filter HQL injection): the UI config history
 * search box value was concatenated into five HQL string literals in
 * Hib3PITConfigDAO.findPITConfigs, so a quote escaped the like operand and the caller
 * controlled the where clause (tautologies, cross entity exists subqueries).
 *
 * The fix binds the filter as one named parameter. Before the fix: the single quote search
 * throws an HQL parser exception and the tautology returns a full page.
 * </pre>
 */
public class Hib3PITConfigDAOFilterTest extends GrouperTest {

  /** config key whose value has nothing special */
  private static final String KEY_A = "junit.pitFilter.a";

  /** config key whose value has a single quote */
  private static final String KEY_B = "junit.pitFilter.b";

  /** tautology from the security report */
  private static final String TAUTOLOGY = "') or 1=1 or lower(pit.configKey) like lower('";

  /** cross entity true predicate from the security report */
  private static final String EXISTS_TRUE = "') and 1=0 or exists (select 1 from GrouperConfigHibernate cfg) "
      + "or lower(pit.configKey) like lower('NO_MATCH_7f3e91a2";

  /** cross entity false predicate from the security report */
  private static final String EXISTS_FALSE = "') and 1=0 or exists (select 1 from GrouperConfigHibernate cfg "
      + "where cfg.id = '00000000000000000000000000000000') or lower(pit.configKey) like lower('NO_MATCH_7f3e91a2";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Hib3PITConfigDAOFilterTest("testFilterIsBound"));
  }

  /**
   * @param name
   */
  public Hib3PITConfigDAOFilterTest(String name) {
    super(name);
  }

  /**
   * save two db config values so there are PIT config rows to search
   */
  @Override
  protected void setUp() {
    super.setUp();
    // GrouperDbConfig.store needs an open session on 4.x (GrouperDbConfig.seeIfAllowed); harmless later
    GrouperSession.startRootSession();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(KEY_A).value("alpha").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(KEY_B).value("o'brien").store();
  }

  /**
   * ordinary searches still work, injection payloads are plain search text
   */
  public void testFilterIsBound() {

    // ordinary key search, case insensitive
    assertTrue(containsKey(find("junit.pitFilter.a"), KEY_A));
    assertTrue(containsKey(find("JUNIT.PITFILTER.A"), KEY_A));
    assertFalse(containsKey(find("junit.pitFilter.a"), KEY_B));

    // value search with a quote: data, no exception
    List<PITGrouperConfigHibernate> quoteResults = find("o'brien");
    assertTrue(containsKey(quoteResults, KEY_B));
    assertFalse(containsKey(quoteResults, KEY_A));

    // no such text: no rows (baseline)
    assertEquals(0, find("NO_SUCH_CONFIG_HISTORY_VALUE_7f3e91a2").size());

    // payloads are search text now, nothing has that text, so no rows
    assertEquals(0, find(TAUTOLOGY).size());
    assertEquals(0, find(EXISTS_TRUE).size());
    assertEquals(0, find(EXISTS_FALSE).size());

    // blank filter still returns the default page
    assertTrue(find("").size() > 0);
    assertTrue(find(null).size() > 0);
  }

  /**
   * @param filter
   * @return the PIT configs for a filter
   */
  private static List<PITGrouperConfigHibernate> find(String filter) {
    return GrouperDAOFactory.getFactory().getPITConfig().findPITConfigs(null, filter);
  }

  /**
   * @param pitConfigs
   * @param configKey
   * @return true if a row has that key
   */
  private static boolean containsKey(List<PITGrouperConfigHibernate> pitConfigs, String configKey) {
    for (PITGrouperConfigHibernate pitConfig : pitConfigs) {
      if (configKey.equals(pitConfig.getConfigKey())) {
        return true;
      }
    }
    return false;
  }
}
