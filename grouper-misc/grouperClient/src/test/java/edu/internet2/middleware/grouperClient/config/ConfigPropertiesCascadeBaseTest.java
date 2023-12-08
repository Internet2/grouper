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
package edu.internet2.middleware.grouperClient.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author mchyzer
 */
public class ConfigPropertiesCascadeBaseTest extends TestCase {

  File tmpCustomfolder = new File("/tmp/grouperClient_test");
  File tmpCustomExampleProps = new File(tmpCustomfolder, "testCascadeConfig.properties");
  File tmpCustomExampleProps2 = new File(tmpCustomfolder, "testCascadeConfig2.properties");

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ConfigPropertiesCascadeBaseTest("testReferToOtherProperties2"));
  }
  
  /**
   * 
   * @param name
   */
  public ConfigPropertiesCascadeBaseTest(String name) {
    super(name);
  }

  private void createTempProperties() {
    try {
      tmpCustomfolder.mkdir();

      InputStream in = this.getClass().getClassLoader().getResourceAsStream("testCascadeConfig.properties");
      byte[] buffer = new byte[in.available()];
      in.read(buffer);
      new FileOutputStream(tmpCustomExampleProps).write(buffer);

      InputStream in2 = this.getClass().getClassLoader().getResourceAsStream("testCascadeConfig2.properties");
      buffer = new byte[in2.available()];
      in2.read(buffer);
      new FileOutputStream(tmpCustomExampleProps2).write(buffer);
    } catch (IOException e) {
      ;
    }
  }

  private void deleteTempProperties() {
    tmpCustomExampleProps.delete();
    tmpCustomExampleProps2.delete();
    tmpCustomfolder.delete();
  }

  /**
   * testCascadeConfig.properties
   */
  public void testReferToOtherProperties() {

    //try it with EL
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever", "someproperty");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever1.elConfig", 
        "${edu.internet2.middleware.grouperClient.util.GrouperClientConfig.retrieveConfig().propertyValueString(\"somethingWhatever\")}_lastPart");
    
    assertEquals("someproperty_lastPart", GrouperClientConfig.retrieveConfig().propertyValueStringRequired("somethingWhatever1"));
    
  }

  /**
   * 
   */
  public void testReferToOtherProperties2() {
    //try it with local reference
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever", "someproperty");
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever8", 
        "$$somethingWhatever$$_lastPart2");
    
    assertEquals("someproperty_lastPart2", GrouperClientConfig.retrieveConfig().propertyValueStringRequired("somethingWhatever8"));

    //mix and match
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever9.elConfig", 
        "${edu.internet2.middleware.grouperClient.util.GrouperClientConfig.retrieveConfig().propertyValueString(\"somethingWhatever\")}_$$somethingWhatever$$_lastPart");
    
    assertEquals("someproperty_someproperty_lastPart", GrouperClientConfig.retrieveConfig().propertyValueStringRequired("somethingWhatever9"));

    
  }  

  

  /**
   * testCascadeConfig.properties
   */
  public void testEnvironmentVariables() {
    Map<String, String> env = System.getenv();
    String javaHome = env.get("JAVA_HOME");
    
    if (StringUtils.isBlank(javaHome)) {
      fail("Sorry, but you need JAVA_HOME set for this test to work!");
    }

    //some.config.1.elConfig = ${elUtils.append('a', 'b')}
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever3.elConfig", "${java.lang.System.getenv().get('JAVA_HOME')}");
    
    assertEquals(javaHome, GrouperClientConfig.retrieveConfig().propertyValueStringRequired("somethingWhatever3"));
  }
  
  /**
   * testCascadeConfig.properties
   */
  public void testEnvironmentVariables2() {
    Map<String, String> env = System.getenv();
    String javaHome = env.get("JAVA_HOME");
    
    if (StringUtils.isBlank(javaHome)) {
      fail("Sorry, but you need JAVA_HOME set for this test to work!");
    }

    //some.config.1.elConfig = ${elUtils.append('a', 'b')}
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().put("somethingWhatever5.elConfig", "${elUtils.environmentVariable('JAVA_HOME')}");
    
    assertEquals(javaHome, GrouperClientConfig.retrieveConfig().propertyValueStringRequired("somethingWhatever5"));
  }
  
  /**
   * testCascadeConfig.properties
   */
  public void testOverrideHasHierarchy() {
    
    //example:
    //test1 = something
    //test2 = somethingElse
    
    //override:
    //test2 = somethingElse2
    //test3 = yet another something

    createTempProperties();

    assertEquals("something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test1"));
    assertEquals("somethingElse2", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test2"));
    assertEquals("yet another something", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test3"));
    assertEquals("abc", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("test4", "abc"));
    assertNull(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("test4"));
    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueStringRequired("test4");
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    assertEquals(4, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueIntRequired("someInt"));
    
    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueIntRequired("someInt2");
      fail("Should catch bad type");
    } catch (Exception e) {
      //good
    }
    
    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueIntRequired("someInt123");
      fail("Doesnt exist");
    } catch (Exception e) {
      //good
    }
    
    assertEquals(-1, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueInt("someInt123", -1));
    assertNull(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueInt("someInt123"));

    //
    assertEquals(true, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueBooleanRequired("someBool"));
    
    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueBooleanRequired("someBool2");
      fail("Should catch bad type");
    } catch (Exception e) {
      //good
    }

    try {
      ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueBooleanRequired("someBool123");
      fail("Doesnt exist");
    } catch (Exception e) {
      //good
    }
    
    assertEquals(true, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueBoolean("someBool123", true));
    assertNull(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueBoolean("someBool123"));
    
    //
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("someBool"));
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("someBool2"));

    //test override map
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesOverrideMap().put("somethingNotExisting", "this is the value");
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesOverrideMap().put("somethingNotExistingNull", null);
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesOverrideMap().put("something", "qwer");
    
    assertEquals("this is the value", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("somethingNotExisting"));
    assertEquals("qwer", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("something"));
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("somethingNotExisting"));
    assertEquals(null, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("somethingNotExistingNull"));
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("somethingNotExistingNull"));
    
    //test threadlocal override map
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesThreadLocalOverrideMap().put("somethingNotExistingAgain", "this is the other value");
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesThreadLocalOverrideMap().put("somethingNotExistingAgainNull", null);
    ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertiesThreadLocalOverrideMap().put("something", "zxcv");
    
    assertEquals("this is the other value", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("somethingNotExistingAgain"));
    assertEquals("zxcv", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("something"));
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("somethingNotExistingAgain"));
    assertEquals(null, ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("somethingNotExistingAgainNull"));
    assertTrue(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().containsKey("somethingNotExistingAgainNull"));
    
    final String[] errorMessage = new String[1];

    //try a different thread and dont see those values
    Thread thread = new Thread(new Runnable() {

      /**
       * 
       */
      @Override
      public void run() {
        try {
          assertNull(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("somethingNotExistingAgain"));
          assertEquals("qwer", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("something"));
        } catch (Throwable t) {
          errorMessage[0] = GrouperClientUtils.getFullStackTrace(t);
        }
      }
      
    });
    
    thread.start();
    
    try {
      thread.join();
    } catch (Exception e) {
      
    }

    if (!GrouperClientUtils.isBlank(errorMessage[0])) {
      fail(errorMessage[0]);
    }
    
    //wait for the thread to catch up
    //propertyNames
    Set<String> propertyNames = ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyNames();
    Properties properties = ConfigPropertiesOverrideHasHierarchy.retrieveConfig().properties();
    
    assertTrue(propertyNames.contains("test1"));
    assertTrue(propertyNames.contains("test2"));
    assertFalse(propertyNames.contains("test4"));
    assertTrue(propertyNames.contains("somethingNotExisting"));
    assertTrue(propertyNames.contains("somethingNotExistingNull"));
    assertTrue(propertyNames.contains("somethingNotExistingAgain"));
    assertTrue(propertyNames.contains("somethingNotExistingAgainNull"));
    assertTrue(propertyNames.contains("something"));
    
    //EL properties
    //some.config.1.elConfig = ${elUtils.append('a', 'b')}
    assertEquals("ab", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("some.config.1"));

    assertTrue(propertyNames.contains("some.config.1"));
    assertFalse(propertyNames.contains("some.config.1.elConfig"));

    assertTrue(properties.containsKey("some.config.1"));
    assertFalse(properties.containsKey("some.config.1.elConfig"));

    assertEquals("ab", properties.get("some.config.1"));
    
    //EL static method
    //some.config.2.elConfig = ${edu.internet2.middleware.grouperClient.config.SomeTestElClass.someMethod('start', ' middle')}
    
    assertEquals("start middle something else", ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("some.config.2"));

    deleteTempProperties();
    
  }
  
  /**
   * testCascadeConfig2.properties
   */
  public void testOriginalHasHierarchy() {
    
    //base:
    //test1 = somethingA
    //test2 = somethingElseA

    //test2 = somethingElse2A
    //test3 = yet another somethingA

    createTempProperties();

    assertEquals("somethingA", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test1"));
    assertEquals("somethingElse2A", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test2"));
    assertEquals("yet another somethingA", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test3"));
    assertNull(ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueString("test4", null));
    try {
      ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueStringRequired("test4");
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    assertEquals("somethingNotThere", ConfigPropertiesOriginalHasHierarchy.retrieveConfig().propertyValueString("somethingWhateversdfsdf", "somethingNotThere"));

    deleteTempProperties();
  }
  
  /**
   * 
   */
  public void testAutoReload() {

    createTempProperties();
    for (int i=0;i<60;i++) {
      
      System.out.println("Last reloaded: " + new Date(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getCreatedTime()) + ", last checked: " + new Date(ConfigPropertiesOverrideHasHierarchy.retrieveConfig().getLastCheckedTime()));
      System.out.println("Property: testAutoReload: " + ConfigPropertiesOverrideHasHierarchy.retrieveConfig().propertyValueString("testAutoReload", null));
      GrouperClientUtils.sleep(1000);
    }
    deleteTempProperties();
  }

    /**
     *
     */
    public void testHooks() {
      createTempProperties();
      assertFalse("non-existing single class", ConfigPropertiesHooks.retrieveConfig().assertPropertyValueClass("grouper.group.hooks1", Object.class, true));
      assertFalse("non-existing multiple classes", ConfigPropertiesHooks.retrieveConfig().assertPropertyValueClass("grouper.group.hooks2", Object.class, true));
      assertTrue("Existing matching type class", ConfigPropertiesHooks.retrieveConfig().assertPropertyValueClass("grouper.group.hooks3", Object.class, true));
      assertTrue("Existing matching type classes", ConfigPropertiesHooks.retrieveConfig().assertPropertyValueClass("grouper.group.hooks4", Object.class, true));
      assertTrue("Existing matching type classes with whitespace in config", ConfigPropertiesHooks.retrieveConfig().assertPropertyValueClass("grouper.group.hooks5", Object.class, true));
      deleteTempProperties();
    }
  
}
