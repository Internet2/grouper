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
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.List;

import com.opensymphony.module.propertyset.PropertySet;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperProfileProviderTest extends TestCase {

  /**
   * 
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = false;
  }

  /** profile provider */
  private GrouperProfileProvider grouperProfileProvider = new GrouperProfileProvider();
  

  /**
   * 
   * @param name
   */
  public GrouperProfileProviderTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProfileProviderTest("testHandles"));
  }

  /**
   * 
   */
  public void testGetPropertySet() {
    
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    PropertySet propertySet = this.grouperProfileProvider.getPropertySet("whataslkfdjasldkfj");
    
    assertNull(propertySet);
        
    assertTrue(cacheMisses < GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);

    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;

    propertySet = this.grouperProfileProvider.getPropertySet("whataslkfdjasldkfj");
    
    assertNull(propertySet);
        
    assertEquals(cacheMisses, GrouperProfileProvider.cacheMisses);
    assertTrue(cacheHits < GrouperProfileProvider.cacheHits);

    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;
    
    propertySet = this.grouperProfileProvider.getPropertySet(GrouperAccessProviderTest.TEST_USERNAME);
    
    assertNotNull(propertySet);

    assertTrue(cacheHits < GrouperProfileProvider.cacheHits);
  
    assertEquals(GrouperClientUtils.propertiesValue("atlassian.test.email", true), propertySet.getString("email"));
    assertEquals(GrouperClientUtils.propertiesValue("atlassian.test.name", true), propertySet.getString("fullName"));

    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;
    
    //this should look again since not in list
    propertySet = this.grouperProfileProvider.getPropertySet("lkjrwetnsdf");
    
    assertNull(propertySet);
        
    assertTrue(cacheMisses < GrouperProfileProvider.cacheMisses);
    assertTrue(cacheHits <= GrouperProfileProvider.cacheHits);
    
    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperProfileProvider().flushCaches();
    
    assertNotNull(this.grouperProfileProvider.getPropertySet(GrouperAccessProviderTest.TEST_USERNAME));
    assertTrue(GrouperProfileProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperProfileProvider.cacheMisses;
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    
    assertNotNull(this.grouperProfileProvider.getPropertySet(GrouperAccessProviderTest.TEST_USERNAME));
    
    assertTrue(GrouperProfileProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperProfileProvider.cacheFailsafeHits);

    
  }
  
  /**
   * make sure these throw exceptions
   */
  public void testCreateRemoveStore() {
    
    assertFalse(this.grouperProfileProvider.create(GrouperAccessProviderTest.TEST_USERNAME));
    
    assertFalse(this.grouperProfileProvider.handles("whatever"));

    assertTrue(this.grouperProfileProvider.create("whatever"));

    assertTrue(this.grouperProfileProvider.handles("whatever"));
    
    assertFalse(this.grouperProfileProvider.create("whatever"));
    
    assertEquals(this.grouperProfileProvider.getPropertySet("whatever").getString("fullName"), "whatever");

    try {
      this.grouperProfileProvider.remove("whatever");
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

    try {
      this.grouperProfileProvider.store("whatever", null);
      fail("Should fail");
    } catch (Exception e) {
      //good
    }

  }

  /**
   * 
   */
  public void testList() {
    
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    List<String> userIds = this.grouperProfileProvider.list();
    
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);
    
    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    userIds = this.grouperProfileProvider.list();
    
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);
    
    assertTrue(GrouperClientUtils.length(userIds) > 0);
    assertTrue(userIds.contains(GrouperAccessProviderTest.TEST_USERNAME));

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperProfileProvider().flushCaches();
    
    assertTrue(this.grouperProfileProvider.list().contains(GrouperAccessProviderTest.TEST_USERNAME));

    assertTrue(GrouperProfileProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperProfileProvider.cacheMisses;
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperProfileProvider.list().contains(GrouperAccessProviderTest.TEST_USERNAME));
    
    assertTrue(GrouperProfileProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperProfileProvider.cacheFailsafeHits);
    
  }
  
  /**
   * 
   */
  public void testHandles() {

    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);

    assertFalse(this.grouperProfileProvider.handles("whataslkfdjasldkfj"));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperProfileProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 4, GrouperProfileProvider.cacheHits);

    this.grouperProfileProvider.flushCaches();

    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);

    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperProfileProvider.cacheHits);

    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperProfileProvider().flushCaches();
    
    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));

    assertTrue(GrouperProfileProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperProfileProvider.cacheMisses;
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperProfileProvider.handles(GrouperAccessProviderTest.TEST_USERNAME));
    
    assertTrue(GrouperProfileProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperProfileProvider.cacheFailsafeHits);
  }

  /**
   * 
   */
  public void testLoad() {
  
    this.grouperProfileProvider.flushCaches();
    
    long cacheHits = GrouperProfileProvider.cacheHits;
    long cacheMisses = GrouperProfileProvider.cacheMisses;
    
    assertFalse(this.grouperProfileProvider.load("whataslkfdjasldkfj", null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits, GrouperProfileProvider.cacheHits);
  
    assertFalse(this.grouperProfileProvider.load("whataslkfdjasldkfj", null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 2, GrouperProfileProvider.cacheHits);
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 2, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 4, GrouperProfileProvider.cacheHits);
  
    this.grouperProfileProvider.flushCaches();
  
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheMisses = GrouperProfileProvider.cacheMisses;
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 1, GrouperProfileProvider.cacheHits);
  
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
  
    assertEquals(cacheMisses + 1, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheHits + 3, GrouperProfileProvider.cacheHits);
  
    //lets make some exceptions happen
    long cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = true;
    new GrouperProfileProvider().flushCaches();
    
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));

    assertTrue(GrouperProfileProvider.cacheFailsafeHits > cacheFailsafeHits);
    
    cacheMisses = GrouperProfileProvider.cacheMisses;
    cacheHits = GrouperProfileProvider.cacheHits;
    cacheFailsafeHits = GrouperProfileProvider.cacheFailsafeHits;
    
    assertTrue(this.grouperProfileProvider.load(GrouperAccessProviderTest.TEST_USERNAME, null));
    
    assertTrue(GrouperProfileProvider.cacheHits > cacheHits);
    assertEquals(cacheMisses, GrouperProfileProvider.cacheMisses);
    assertEquals(cacheFailsafeHits, GrouperProfileProvider.cacheFailsafeHits);
  }

  /**
   * 
   */
  @Override
  protected void tearDown() throws Exception {
    
    super.tearDown();
    GrouperProfileProvider.failOnGrouperForTestingFailsafeCache = false;
  }

}
