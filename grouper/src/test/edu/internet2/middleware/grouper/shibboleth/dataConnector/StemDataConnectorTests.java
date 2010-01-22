package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import java.util.Map;

import junit.textui.TestRunner;

import org.slf4j.Logger;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class StemDataConnectorTests extends BaseDataConnectorTest {

  private static final Logger LOG = GrouperUtil.getLogger(StemDataConnectorTests.class);

  public static final String RESOLVER_CONFIG = TEST_PATH + "StemDataConnectorTests-resolver.xml";

  public StemDataConnectorTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(StemDataConnectorTests.class);
    // TestRunner.run(new StemDataConnectorTests("testRootStem"));
  }
  
  private void runResolveTest(String groupDataConnectorName, Stem stem, AttributeMap correctMap) {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean(groupDataConnectorName);
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
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
      StemDataConnector sdc = (StemDataConnector) gContext.getBean("testAll");
      Map<String, BaseAttribute> map = sdc.resolve(getShibContext("root"));
      assertTrue(map.isEmpty());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void testStemNotFound() {
    try {
      GenericApplicationContext gContext = PSPUtil.createSpringContext(RESOLVER_CONFIG);
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
  
}
