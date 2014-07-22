/**
 * Copyright 2012 Internet2
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
package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.shibboleth.filter.Filter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

/**
 * Test for {@link StemDataConnector}.
 */
public class StemDataConnectorTest extends BaseDataConnectorTest {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(StemDataConnectorTest.class);

  /** Path to attribute resolver configuration. */
  public static final String RESOLVER_CONFIG = TEST_PATH + "StemDataConnectorTest-resolver.xml";

  /**
   * 
   * Constructor
   * 
   * @param name
   */
  public StemDataConnectorTest(String name) {
    super(name);
  }

  /**
   * Run tests.
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(StemDataConnectorTest.class);
    // TestRunner.run(new StemDataConnectorTest("testGetAllIdentifiers"));
  }

  /**
   * 
   * Assert that the attributes returned from the data connector match the provided attributes.
   * 
   * @param dataConnectorName the data connector name
   * @param group the group
   * @param correctMap the correct attributes
   */
  private void runResolveTest(String dataConnectorName, Stem stem, AttributeMap correctMap) {
    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean(dataConnectorName);
      AttributeMap currentMap = new AttributeMap(sdc.resolve(getShibContext(stem.getName())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("correct\n{}", correctMap);
        LOG.debug("current\n{}", currentMap);
      }
      assertEquals(correctMap, currentMap);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testRootStem() {
    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testAll");
      Map<String, BaseAttribute> map = sdc.resolve(getShibContext("root"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testStemNotFound() {
    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testAll");
      Map<String, BaseAttribute> map = sdc.resolve(getShibContext("notfound"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testAllParentStem() {
    runResolveTest("testAll", parentStem, correctAttributesParentStem);
  }

  public void testAllChildStem() {
    runResolveTest("testAll", childStem, correctAttributesChildStem);
  }

  public void testAttributeDef() {
    parentStem.getAttributeValueDelegate().assignValuesString("parentStem:mailAlternateAddress",
        GrouperUtil.toSet("foo@memphis.edu", "bar@memphis.edu"), true);

    correctAttributesParentStem.setAttribute("parentStem:mailAlternateAddress", "foo@memphis.edu", "bar@memphis.edu");

    runResolveTest("testAll", parentStem, correctAttributesParentStem);
  }

  public void testFilterStemNameExact() {

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemNameExact");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      assertEquals(1, stems.size());

      assertTrue(stems.contains(parentStem));
      assertFalse(stems.contains(childStem));

      assertTrue(filter.matches(parentStem));
      assertFalse(filter.matches(childStem));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemNameExactRoot() {

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemNameExactRoot");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      assertEquals(1, stems.size());

      Stem root = StemFinder.findRootStem(grouperSession);

      assertTrue(stems.contains(root));
      assertFalse(stems.contains(parentStem));
      assertFalse(stems.contains(childStem));

      assertTrue(filter.matches(root));
      assertFalse(filter.matches(parentStem));
      assertFalse(filter.matches(childStem));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemInStemSUB() {

    Stem anotherChildStem = StemHelper.addChildStem(childStem, "childStem2", "Child Stem 2");

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemInStemSUB");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      assertEquals(2, stems.size());

      assertFalse(stems.contains(parentStem));
      assertTrue(stems.contains(childStem));
      assertTrue(stems.contains(anotherChildStem));

      assertFalse(filter.matches(parentStem));
      assertTrue(filter.matches(childStem));
      assertTrue(filter.matches(anotherChildStem));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemInStemSUBRoot() {

    Stem anotherChildStem = StemHelper.addChildStem(childStem, "childStem2", "Child Stem 2");

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemInStemSUBRoot");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      Stem etc = StemFinder.findByName(grouperSession, "etc", true);
      Set<Stem> etcChildStems = etc.getChildStems(Scope.SUB);

      // parent, child, anotherChildStem, etc, and etc children
      assertEquals(3 + 1 + etcChildStems.size(), stems.size());

      assertTrue(stems.contains(parentStem));
      assertTrue(stems.contains(childStem));
      assertTrue(stems.contains(anotherChildStem));

      assertTrue(stems.contains(etc));
      for (Stem stem : etcChildStems) {
        assertTrue(stems.contains(stem));
      }

      assertTrue(filter.matches(parentStem));
      assertTrue(filter.matches(childStem));
      assertTrue(filter.matches(anotherChildStem));

      assertTrue(filter.matches(etc));
      for (Stem stem : etcChildStems) {
        assertTrue(filter.matches(stem));
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemInStemONE() {

    Stem anotherChildStem = StemHelper.addChildStem(childStem, "childStem2", "Child Stem 2");

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemInStemONE");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      assertEquals(1, stems.size());

      assertFalse(stems.contains(parentStem));
      assertTrue(stems.contains(childStem));
      assertFalse(stems.contains(anotherChildStem));

      assertFalse(filter.matches(parentStem));
      assertTrue(filter.matches(childStem));
      assertFalse(filter.matches(anotherChildStem));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterStemInStemONERoot() {

    Stem anotherChildStem = StemHelper.addChildStem(childStem, "childStem2", "Child Stem 2");

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterStemInStemONERoot");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      Stem etc = StemFinder.findByName(grouperSession, "etc", true);

      assertEquals(2, stems.size());

      assertTrue(stems.contains(etc));
      assertTrue(stems.contains(parentStem));
      assertFalse(stems.contains(childStem));
      assertFalse(stems.contains(anotherChildStem));

      assertTrue(filter.matches(etc));
      assertTrue(filter.matches(parentStem));
      assertFalse(filter.matches(childStem));
      assertFalse(filter.matches(anotherChildStem));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testFilterMinusEtc() {

    Stem anotherChildStem = StemHelper.addChildStem(childStem, "childStem2", "Child Stem 2");

    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testFilterMinusEtc");

      Filter filter = sdc.getFilter();

      Set<Stem> stems = filter.getResults(grouperSession);

      assertEquals(3, stems.size());

      assertTrue(stems.contains(parentStem));
      assertTrue(stems.contains(childStem));
      assertTrue(stems.contains(anotherChildStem));

      assertTrue(filter.matches(parentStem));
      assertTrue(filter.matches(childStem));
      assertTrue(filter.matches(anotherChildStem));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
