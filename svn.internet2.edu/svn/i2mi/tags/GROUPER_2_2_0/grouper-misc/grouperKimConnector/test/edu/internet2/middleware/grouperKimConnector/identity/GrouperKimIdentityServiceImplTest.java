/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import java.util.Map;

import junit.textui.TestRunner;

import org.apache.commons.lang.ArrayUtils;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNamePrincipalNameInfo;

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
    TestRunner.run(new GrouperKimIdentityServiceImplTest("testGetDefaultNamesForPrincipalIds"));
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
    assertTrue(attributeNamesString + ", NOTE, for this to work, you need this in grouper-ws.properties: "
        + " ws.subject.result.detail.attribute.names = loginid", ArrayUtils.contains(attributeNameArray, "loginid"));
    
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
  public void testGetDefaultNamesForEntityIds() {
    
    
    
    Map<String, KimEntityNameInfo> kimNameMap = new GrouperKimIdentityServiceImpl()
      .getDefaultNamesForEntityIds(GrouperClientUtils.toList("test.subject.0", "test.subject.1"));
  
    KimEntityNameInfo kimEntityNameInfo0 = kimNameMap.get("test.subject.0");
    
    assertEquals("name.test.subject.0", kimEntityNameInfo0.getFormattedName());
    
    KimEntityNameInfo kimEntityNameInfo1 = kimNameMap.get("test.subject.1");

    assertEquals("name.test.subject.1", kimEntityNameInfo1.getFormattedName());
  }
  
  /**
   * 
   */
  public void testGetDefaultNamesForPrincipalIds() {
    
    
    
    Map<String, KimEntityNamePrincipalNameInfo> kimNameMap = new GrouperKimIdentityServiceImpl()
      .getDefaultNamesForPrincipalIds(GrouperClientUtils.toList("id.test.subject.0", "id.test.subject.1"));
  
    KimEntityNamePrincipalNameInfo kimEntityNamePrincipalNameInfo0 = kimNameMap.get("id.test.subject.0");
    
    assertEquals("id.test.subject.0", kimEntityNamePrincipalNameInfo0.getPrincipalName());
    
    KimEntityNamePrincipalNameInfo kimEntityNamePrincipalNameInfo1 = kimNameMap.get("id.test.subject.1");

    assertEquals("id.test.subject.1", kimEntityNamePrincipalNameInfo1.getPrincipalName());
  }
  
}
