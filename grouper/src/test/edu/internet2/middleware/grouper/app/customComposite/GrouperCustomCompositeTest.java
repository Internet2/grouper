/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.app.customComposite;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.ConfigFile;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * custom composites saved through database config, as the custom composites screen does
 *
 * @author shilen
 */
public class GrouperCustomCompositeTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperCustomCompositeTest("testSaveDeleteKeepsOtherIndexes"));
  }

  /**
   * @param name
   */
  public GrouperCustomCompositeTest(String name) {
    super(name);
  }

  /**
   *
   */
  public GrouperCustomCompositeTest() {
    super();
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSession();
    deleteTestConfig();
    new GroupSave().assignName("test:customCompositeGroup").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:customCompositeGroup2").assignCreateParentStemsIfNotExist(true).save();
  }

  @Override
  protected void tearDown() {
    deleteTestConfig();
    super.tearDown();
  }

  /**
   * remove what these tests put in database config
   */
  private static void deleteTestConfig() {
    new GcDbAccess().sql("delete from grouper_config where config_key like 'grouper.membership.customComposite.%' or config_key like 'testCc%'").executeSql();
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * add two, delete the first: the second keeps its index and the first leaves a gap
   */
  public void testSaveDeleteKeepsOtherIndexes() {

    Map<String, String> errors = GrouperCustomComposite.save(null, "testCcMinusGroup", "Members not in the group",
        "complement", "test:customCompositeGroup");
    assertEquals(errors.toString(), 0, errors.size());

    errors = GrouperCustomComposite.save(null, "testCcIntersectGroup2", "Members also in group 2",
        "intersection", "test:customCompositeGroup2");
    assertEquals(errors.toString(), 0, errors.size());

    GrouperCustomComposite first = GrouperCustomComposite.retrieve(0);
    assertEquals("testCcMinusGroup", first.getUiKey());
    assertEquals("complement", first.getCompositeType());
    assertEquals("test:customCompositeGroup", first.getGroupName());
    assertEquals("Members not in the group", first.getLabel());
    assertEquals(GrouperConfigSourceType.database, first.getConfigSource());
    assertEquals(GrouperConfigSourceType.database, first.getLabelSource());
    assertEquals("Members not in the group", GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertyValueString("testCcMinusGroup"));

    assertNull(GrouperCustomComposite.delete(0));

    assertNull(GrouperCustomComposite.retrieve(0));
    assertFalse(GrouperUiApiTextConfig.retrieveTextConfigEnUs().containsKey("testCcMinusGroup"));

    GrouperCustomComposite second = GrouperCustomComposite.retrieve(1);
    assertTrue(second.isComplete());
    assertEquals("testCcIntersectGroup2", second.getUiKey());

    assertEquals(1, GrouperCustomComposite.retrieveIndexes().size());
    assertEquals(2, GrouperCustomComposite.nextIndex());

    // editing keeps the uiKey, even if another is sent
    errors = GrouperCustomComposite.save(1, "somethingElse", "Members also in group 2, edited",
        "complement", "test:customCompositeGroup");
    assertEquals(errors.toString(), 0, errors.size());
    second = GrouperCustomComposite.retrieve(1);
    assertEquals("testCcIntersectGroup2", second.getUiKey());
    assertEquals("complement", second.getCompositeType());
    assertEquals("Members also in group 2, edited", second.getLabel());
  }

  /**
   * indexes after a gap are still read, a type in another case is read as lower case, and an index
   * too long for an int is ignored rather than failing
   */
  public void testGapCaseAndLongIndex() {
    for (int index : new int[] {0, 2}) {
      GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.uiKey." + index, "testCcGap" + index);
      GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.groupName." + index, "test:customCompositeGroup");
    }
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.compositeType.0", "complement");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.compositeType.2", "INTERSECTION");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.uiKey.99999999999", "testCcTooLong");

    assertEquals(GrouperUtil.toSet(0, 2), GrouperCustomComposite.retrieveIndexes());
    assertTrue(GrouperCustomComposite.retrieve(0).isComplete());
    assertNull(GrouperCustomComposite.retrieve(1));
    assertEquals("intersection", GrouperCustomComposite.retrieve(2).getCompositeType());
    assertNull(GrouperCustomComposite.parseIndex("99999999999"));
    assertEquals(Integer.valueOf(3), GrouperCustomComposite.parseIndex("3"));
  }

  /**
   * an index with only some of its keys is incomplete and not offered
   */
  public void testIncomplete() {
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.uiKey.5", "testCcIncomplete");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membership.customComposite.compositeType.5", "complement");

    GrouperCustomComposite incomplete = GrouperCustomComposite.retrieve(5);
    assertNotNull(incomplete);
    assertFalse(incomplete.isComplete());
    assertNull(GrouperCustomComposite.parseIndex("abc"));
    assertTrue(GrouperCustomComposite.retrieveIndexes().contains(5));
  }

  /**
   * validation, including a uiKey which would overwrite existing UI text
   */
  public void testValidation() {

    Map<String, String> errors = GrouperCustomComposite.save(null, "bad.key", null, "union", "test:doesNotExist");
    assertTrue(errors.toString(), errors.containsKey("uiKey"));
    assertTrue(errors.toString(), errors.containsKey("label"));
    assertTrue(errors.toString(), errors.containsKey("compositeType"));
    assertTrue(errors.toString(), errors.containsKey("groupName"));

    // an existing UI text key
    errors = GrouperCustomComposite.save(null, "myServicesHomeBreadcrumb", "label", "complement", "test:customCompositeGroup");
    assertEquals(GrouperCustomComposite.text("customCompositeErrorUiKeyTextExists"), errors.get("uiKey"));
    assertEquals(0, GrouperCustomComposite.retrieveIndexes().size());

    // a uiKey which would make the label be stored as a secret
    errors = GrouperCustomComposite.save(null, "testCcMinusPassholders", "label", "complement", "test:customCompositeGroup");
    assertEquals(GrouperCustomComposite.text("customCompositeErrorPasswordRelatedWords"), errors.get("uiKey"));

    // a uiKey another composite uses
    assertEquals(0, GrouperCustomComposite.save(null, "testCcOne", "One", "complement", "test:customCompositeGroup").size());
    errors = GrouperCustomComposite.save(null, "testCcOne", "One again", "complement", "test:customCompositeGroup");
    assertEquals(GrouperCustomComposite.text("customCompositeErrorUiKeyInUse"), errors.get("uiKey"));
  }

  /**
   * a composite set in a config file (here a test override) cannot be changed or deleted
   */
  public void testConfigFileIsReadOnly() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.uiKey.3", "testCcFromFile");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.compositeType.3", "complement");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.groupName.3", "test:customCompositeGroup");

    try {
      GrouperCustomComposite fromFile = GrouperCustomComposite.retrieve(3);
      assertEquals(GrouperConfigSourceType.configFile, fromFile.getConfigSource());
      assertFalse(fromFile.isEditable());

      Map<String, String> errors = GrouperCustomComposite.save(3, null, "label", "intersection", "test:customCompositeGroup");
      assertEquals(GrouperCustomComposite.text("customCompositeErrorReadOnly"), errors.get(""));
      assertEquals(GrouperCustomComposite.text("customCompositeErrorReadOnly"), GrouperCustomComposite.delete(3));

      // a new one goes after it
      assertEquals(4, GrouperCustomComposite.nextIndex());
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.uiKey.3");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.compositeType.3");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.groupName.3");
    }
  }

  /**
   * a database value which overrides a value in a config file counts as set in the file: deleting the
   * database value would bring the file value back while the screen said it was deleted
   */
  public void testDatabaseValueOverFileIsConfigFile() {

    // a key from a grouper.properties file on disk in this environment (not the base, not the database)
    String fileKey = null;
    String fileValue = null;
    List<ConfigFile> configFiles = GrouperConfig.retrieveConfig().internalRetrieveConfigFiles();
    for (int i = 1; i < GrouperUtil.length(configFiles) && fileKey == null; i++) {
      ConfigFile configFile = configFiles.get(i);
      if (configFile.getProperties() == null || Strings.CI.startsWith(configFile.getOriginalConfig(), "database")) {
        continue;
      }
      for (Object key : configFile.getProperties().keySet()) {
        String keyString = (String)key;
        // not one already in the database, which the test would delete at the end, and one with a value,
        // since storing a blank value is refused
        if (!keyString.endsWith(".elConfig") && !GrouperConfigHibernate.containsPasswordRelatedWords(keyString)
            && !StringUtils.isBlank(configFile.getProperties().getProperty(keyString))
            && new GcDbAccess().sql("select count(*) from grouper_config where config_key = ?")
              .addBindVar(keyString).select(int.class) == 0) {
          fileKey = keyString;
          fileValue = configFile.getProperties().getProperty(keyString);
          break;
        }
      }
    }
    if (fileKey == null) {
      // no grouper.properties file with keys in this environment
      return;
    }

    assertEquals(GrouperConfigSourceType.configFile, GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(), fileKey));

    // the same value, so nothing else changes while it is in the database
    GrouperCustomComposite.storeConfig("grouper.properties", fileKey, fileValue);
    try {
      // the value really is in the database too
      assertEquals(fileKey, 1, (int)new GcDbAccess().sql("select count(*) from grouper_config where config_key = ?")
          .addBindVar(fileKey).select(int.class));
      ConfigPropertiesCascadeBase.clearCache();
      assertEquals(GrouperConfigSourceType.configFile, GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(), fileKey));

      // a key only in the database is in the database
      GrouperCustomComposite.storeConfig("grouper.properties", "testCcOnlyInDatabase", "value");
      ConfigPropertiesCascadeBase.clearCache();
      assertEquals(GrouperConfigSourceType.database, GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(), "testCcOnlyInDatabase"));
    } finally {
      GrouperCustomComposite.deleteConfig("grouper.properties", fileKey);
      ConfigPropertiesCascadeBase.clearCache();
    }
  }

  /**
   * the startup config check reports a misspelled custom composite key, but not a gap in the indexes
   */
  public void testMisspelledKeys() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.uiKey.5", "testCcFive");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.compositeType.5", "complement");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.groupName.5", "test:customCompositeGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.customComposite.uikey.7", "testCcMisspelled");
    try {
      Set<String> misspelledKeys = GrouperCheckConfig.retrieveCustomCompositeKeysMisspelled();
      assertTrue(misspelledKeys.toString(), misspelledKeys.contains("grouper.membership.customComposite.uikey.7"));
      assertFalse(misspelledKeys.toString(), misspelledKeys.contains("grouper.membership.customComposite.uiKey.5"));
      assertFalse(misspelledKeys.toString(), misspelledKeys.contains("grouper.membership.customComposite.compositeType.5"));
      assertFalse(misspelledKeys.toString(), misspelledKeys.contains("grouper.membership.customComposite.groupName.5"));
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.uiKey.5");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.compositeType.5");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.groupName.5");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membership.customComposite.uikey.7");
    }
  }

  /**
   * only a Grouper system administrator can save or delete
   */
  public void testNotWheelRefused() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      try {
        GrouperCustomComposite.save(null, "testCcNotAllowed", "label", "complement", "test:customCompositeGroup");
        fail("should not be allowed");
      } catch (RuntimeException re) {
        // good
      }
      try {
        GrouperCustomComposite.delete(0);
        fail("should not be allowed");
      } catch (RuntimeException re) {
        // good
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertEquals(0, GrouperCustomComposite.retrieveIndexes().size());
  }
}
