/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import java.util.Arrays;
import java.util.Map;

import junit.textui.TestRunner;

import org.apache.commons.lang.ArrayUtils;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * NOTE, for this to work, you need this in grouper-ws.properties:
 * ws.subject.result.detail.attribute.names = lfname
 */
public class GrouperKimIdentityServiceImplTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /**
   * 
   */
  public GrouperKimIdentityServiceImplTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GrouperKimIdentityServiceImplTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //TestRunner.run(GrouperKimIdentityServiceImplTest.class);
    TestRunner.run(new GrouperKimIdentityServiceImplTest("testGetDirectGroupIdsForPrincipal"));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    // dont do this, it deletes types
    // super.setUp();

    //sanity test this, if you dont have this in grouper-ws.properties, the tests wont work!
    String attributeNamesString = GrouperWsConfig.getPropertyString("ws.subject.result.detail.attribute.names");
    String[] attributeNameArray = GrouperUtil.nonNull(GrouperUtil.splitTrim(attributeNamesString, ","), String.class);
    assertTrue(attributeNamesString + ", NOTE, for this to work, you need this in grouper-ws.properties: "
      + " ws.subject.result.detail.attribute.names = lfname", ArrayUtils.contains(attributeNameArray, "lfname"));
    
    this.grouperSession = GrouperSession.startRootSession();
  
    GrouperClientUtils.grouperClientOverrideMap().put("kuali.identity.source.id.0", "jdbc");
    GrouperClientUtils.grouperClientOverrideMap().put("kuali.identity.source.nameAttribute.0", "lfname");
    
    String wsUserLabel = GrouperClientUtils.propertiesValue(
        "grouperClient.webService.user.label", true);
    String wsUserString = GrouperClientUtils.propertiesValue(
        "grouperClient.webService." + wsUserLabel, true);

    RestClientSettings.resetData(wsUserString, false);
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * 
   */
  public void testGetDirectGroupIdsForPrincipal() {
    
    
    
    Map<String, KimEntityNameInfo> kimNameMap = new GrouperKimIdentityServiceImpl()
      .getDefaultNamesForEntityIds(GrouperClientUtils.toList("test.subject.0", "test.subject.1"));
  
    KimEntityNameInfo kimEntityNameInfo0 = kimNameMap.get("test.subject.0");
    
    assertEquals("name.test.subject.0", kimEntityNameInfo0.getFormattedName());
    
    KimEntityNameInfo kimEntityNameInfo1 = kimNameMap.get("test.subject.1");

    assertEquals("name.test.subject.1", kimEntityNameInfo1.getFormattedName());
  }
  
}
