package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Map;

import junit.textui.TestRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class FindStemByNameDataConnectorTest extends BaseDataConnectorTest {

  private static final Logger LOG = LoggerFactory.getLogger(FindStemByNameDataConnectorTest.class);

  public static final String RESOLVER_CONFIG = TEST_PATH + "StemDataConnectorTest-resolver.xml";

  public FindStemByNameDataConnectorTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(FindStemByNameDataConnectorTest.class);
    // TestRunner.run(new FindStemByNameDataConnectorTest("testAttributeDef"));
  }
  
  private void runResolveTest(String groupDataConnectorName, Stem stem, AttributeMap correctMap) {
    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      FindStemByNameDataConnector sdc = (FindStemByNameDataConnector) gContext.getBean(groupDataConnectorName);
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
      FindStemByNameDataConnector sdc = (FindStemByNameDataConnector) gContext.getBean("testAll");
      Map<String, BaseAttribute> map = sdc.resolve(getShibContext("root"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testStemNotFound() {
    try {
      GenericApplicationContext gContext = BaseDataConnectorTest.createSpringContext(RESOLVER_CONFIG);
      FindStemByNameDataConnector sdc = (FindStemByNameDataConnector) gContext.getBean("testAll");
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
  
}
