/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.scim;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import org.apache.wink.client.ClientWebException;

/**
 *
 * @author davel
 */
public class ScimEmitterTest extends GrouperTest {
  
  public ScimEmitterTest(String testName) {
    super(testName);
	//RegistryInitializeSchema.initializeSchemaForTests();
  }
  
  @Override
  protected void setUp() {  
	//super.setUp();

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
    GrouperSession sess = GrouperSession.startRootSession();
	
	Group g = GroupFinder.findByName(sess, "scim:test:group1", true);
	System.out.println("group is "+g.getName());
    ScimEmitter instance = new ScimEmitter();
	
    String expResult = "scim:test:group1";
	try{
		String result = instance.createGroup(g);
		System.out.println(result);
		assertEquals(expResult, result);
	}catch(ClientWebException cwe){
		System.err.println(cwe.getMessage());
	}
    // TODO review the generated test code and remove the default call to fail.
    
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
  public void testGetGroup() throws Exception {
    System.out.println("getGroup");
    Group group = GroupFinder.findByName(GrouperSession.startRootSession(), "scim:test:group1", true);
    ScimEmitter instance = new ScimEmitter();
    org.wso2.charon.core.objects.Group expResult = null;
    org.wso2.charon.core.objects.Group result = instance.getGroup(group);
    assertEquals(expResult.getDisplayName(), result.getDisplayName());
    // TODO review the generated test code and remove the default call to fail.
    
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
