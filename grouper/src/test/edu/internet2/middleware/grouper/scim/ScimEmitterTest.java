/*
 * Copyright 2014 Internet2.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.scim;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;

/**
 *
 * @author davel
 */
public class ScimEmitterTest extends GrouperTest {
  
  public ScimEmitterTest(String testName) {
    super(testName);
    GrouperTest.assertEnoughMemory();
    
    GrouperTest.testing = true;

    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.JUNIT, false, true);

    GrouperTest.setupTests();
	
	GrouperConfig conf = GrouperConfig.retrieveConfig();
	conf.propertiesOverrideMap().put("scim.endpoint", "https://scim.endpoint/charonDemoApp/scim");
    conf.propertiesOverrideMap().put("scim.user", "admin");
    conf.propertiesOverrideMap().put("scim.password", "scimT3st!");
            
  }
  
  @Override
  protected void setUp() {
    super.setUp();

            
  }
  
  @Override
  protected void tearDown() {
    super.tearDown();
  }

  /**
   * Test of createGroup method, of class ScimEmitter.
   */
  public void testCreateGroup() {
    System.out.println("createGroup");
    Group group = null;
    ScimEmitter instance = new ScimEmitter();
    String expResult = "";
    String result = instance.createGroup(group);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of updateGroup method, of class ScimEmitter.
   */
  public void testUpdateGroup() {
    System.out.println("updateGroup");
    Group group = null;
    ScimEmitter instance = new ScimEmitter();
    String expResult = "";
    String result = instance.updateGroup(group);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getGroup method, of class ScimEmitter.
   */
  public void testGetGroup() {
    System.out.println("getGroup");
    Group group = null;
    ScimEmitter instance = new ScimEmitter();
    org.wso2.charon.core.objects.Group expResult = null;
    org.wso2.charon.core.objects.Group result = instance.getGroup(group);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of deleteGroup method, of class ScimEmitter.
   */
  public void testDeleteGroup() {
    System.out.println("deleteGroup");
    Group group = null;
    ScimEmitter instance = new ScimEmitter();
    String expResult = "";
    String result = instance.deleteGroup(group);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
}
