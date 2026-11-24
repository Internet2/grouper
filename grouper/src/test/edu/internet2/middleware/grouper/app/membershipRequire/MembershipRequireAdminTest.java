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
package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * membership requirements saved through database config, as the custom composites screen does
 *
 * @author shilen
 */
public class MembershipRequireAdminTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipRequireAdminTest("testCreateWithNewDefinition"));
  }

  /**
   * @param name
   */
  public MembershipRequireAdminTest(String name) {
    super(name);
  }

  /**
   *
   */
  public MembershipRequireAdminTest() {
    super();
  }

  /** a group the requirement is applied to */
  private Group policyGroup;

  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSession();
    deleteTestConfig();

    new GroupSave().assignName("test:requireGroup").assignCreateParentStemsIfNotExist(true).save();
    this.policyGroup = new GroupSave().assignName("test:policyGroup").assignCreateParentStemsIfNotExist(true).save();
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
    new GcDbAccess().sql("delete from grouper_config where config_key like 'grouper.membershipRequirement.%' "
        + "or config_key like 'groupScreen.attribute.%' "
        + "or config_key like 'testMr%' or config_key like 'veto.membershipVeto.customComposite.testMr%'").executeSql();
    ConfigPropertiesCascadeBase.clearCache();
    MembershipRequireEngine.clearCaches();
  }

  /**
   * save a requirement for test:requireGroup with a new attribute definition and name
   * @param configId the config id
   * @param requireGroupName the require group
   * @param attributeSuffix makes the attribute and definition names unique
   * @return errors
   */
  private static Map<String, String> saveRequirement(String configId, String requireGroupName, String attributeSuffix) {
    return MembershipRequireAdmin.save(true, configId, "testMr" + configId,
        "Users must be in " + requireGroupName, requireGroupName, true, null,
        MembershipRequireAdmin.AttributeOption.newNameAndDef, "test:attr:requirement" + attributeSuffix,
        "test:attr:requirementDef" + attributeSuffix, false, null, null, null);
  }

  /**
   * creating with a new definition sets it up as a marker for groups and folders, and everything is
   * read back as saved
   */
  public void testCreateWithNewDefinition() {

    Map<String, String> errors = saveRequirement("one", "test:requireGroup", "One");
    assertEquals(errors.toString(), 0, errors.size());

    AttributeDef attributeDef = AttributeDefFinder.findByName("test:attr:requirementDefOne", true);
    assertTrue(MembershipRequireAdmin.isSuitableAttributeDef(attributeDef));
    assertNotNull(AttributeDefNameFinder.findByName("test:attr:requirementOne", false));

    MembershipRequireAdmin membershipRequireAdmin = MembershipRequireAdmin.retrieve("one");
    assertEquals("testMrone", membershipRequireAdmin.getUiKey());
    assertEquals("test:attr:requirementOne", membershipRequireAdmin.getAttributeName());
    assertEquals("test:requireGroup", membershipRequireAdmin.getRequireGroupName());
    assertTrue(membershipRequireAdmin.isHookEnable());
    assertEquals("Users must be in test:requireGroup", membershipRequireAdmin.getVetoMessage());
    assertEquals(0, membershipRequireAdmin.retrieveProblems().size());
    assertEquals("Users must be in test:requireGroup",
        GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertyValueString(MembershipRequireAdmin.VETO_TEXT_PREFIX + "testMrone"));

    // the engine sees it
    boolean found = false;
    for (MembershipRequireConfigBean membershipRequireConfigBean : MembershipRequireEngine.membershipRequireConfigBeans()) {
      if ("one".equals(membershipRequireConfigBean.getConfigId())) {
        found = true;
      }
    }
    assertTrue(found);

    // blank sourceIdsToIgnore means the global setting
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.sourceIdsToIgnore", "g:isa");
    try {
      assertNull(MembershipRequireAdmin.retrieve("one").getSourceIdsToIgnore());
      assertEquals("g:isa", MembershipRequireAdmin.retrieve("one").getSourceIdsToIgnoreEffective());
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.sourceIdsToIgnore");
    }
  }

  /**
   * the attribute can be put on the group screen, which is read back, changed, turned off, and
   * removed with the requirement
   */
  public void testGroupScreen() {

    Map<String, String> errors = MembershipRequireAdmin.save(true, "one", "testMrone", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:requirementOne", "test:attr:requirementDefOne", true, null, null, "abc");
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorGroupScreenLabelRequired"), errors.get("groupScreenLabel"));
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorGroupScreenIndexInvalid"), errors.get("groupScreenIndex"));

    errors = MembershipRequireAdmin.save(true, "one", "testMrone", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:requirementOne", "test:attr:requirementDefOne", true, "Require one", "Members must be in one", "5");
    assertEquals(errors.toString(), 0, errors.size());

    assertEquals("test:attr:requirementOne", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.attributeName"));
    MembershipRequireAdmin membershipRequireAdmin = MembershipRequireAdmin.retrieve("one");
    assertTrue(membershipRequireAdmin.isShownOnGroupScreen());
    assertEquals("Require one", membershipRequireAdmin.getGroupScreenLabel());
    assertEquals("Members must be in one", membershipRequireAdmin.getGroupScreenDescription());
    assertEquals("5", membershipRequireAdmin.getGroupScreenIndex());

    // edit: blank order removes it, unticking removes the entry
    errors = MembershipRequireAdmin.save(false, "one", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, true, "Require one", null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertNull(MembershipRequireAdmin.retrieve("one").getGroupScreenIndex());

    errors = MembershipRequireAdmin.save(false, "one", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, false, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertFalse(MembershipRequireAdmin.retrieve("one").isShownOnGroupScreen());
    assertNull(GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.label"));

    // deleting the requirement takes it off the group screen
    errors = MembershipRequireAdmin.save(false, "one", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, true, "Require one", null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertNull(MembershipRequireAdmin.delete("one"));
    assertNull(GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.attributeName"));
  }

  /**
   * two requirements on one attribute share its group screen entry: unticking or deleting one leaves
   * the entry for the other, and the attribute cannot be changed on edit
   */
  public void testGroupScreenSharedAttribute() {

    assertEquals(0, MembershipRequireAdmin.save(true, "one", "testMrone", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:requirementOne", "test:attr:requirementDefOne", true, "Require one", null, null).size());

    // a second requirement on the same attribute finds the same entry.  adding leaves it as it is (the
    // add form could not show its values), even with other values in the form
    Map<String, String> errors = MembershipRequireAdmin.save(true, "two", "testMrtwo", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, true, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertEquals("Require one", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.label"));
    assertNull(GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.two.attributeName"));
    assertTrue(MembershipRequireAdmin.retrieve("two").isShownOnGroupScreen());

    // editing either requirement changes the shared entry
    errors = MembershipRequireAdmin.save(false, "two", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, true, "Require shared", null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertEquals("Require shared", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.label"));

    // unticking on one requirement leaves the entry, which the other uses
    errors = MembershipRequireAdmin.save(false, "two", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, false, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertEquals("test:attr:requirementOne", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.attributeName"));

    // deleting one requirement leaves the entry, which the other still uses
    assertNull(MembershipRequireAdmin.delete("one"));
    assertEquals("test:attr:requirementOne", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.attributeName"));

    // the attribute is fixed once set: an edit asking for another attribute keeps this one
    errors = MembershipRequireAdmin.save(false, "two", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:requirementTwo", "test:attr:requirementDefTwo", true, "Require shared", null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertEquals("test:attr:requirementOne", MembershipRequireAdmin.retrieve("two").getAttributeName());
    assertNull(AttributeDefFinder.findByName("test:attr:requirementDefTwo", false));

    // now nothing else uses the attribute, so deleting the last requirement removes its entry
    assertNull(MembershipRequireAdmin.delete("two"));
    assertNull(GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.one.attributeName"));
  }

  /**
   * a requirement which keeps an attribute the group screen cannot show (multi-assignable, set up
   * before this screen) cannot be put on the group screen
   */
  public void testGroupScreenAttributeNotSupported() {

    AttributeDef multiAssignableDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:attr:multiAssignableDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true)
        .assignMultiAssignable(true).save();
    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), multiAssignableDef).assignName("test:attr:multiAssignable")
        .assignCreateParentStemsIfNotExist(true).save();

    // a requirement configured by hand on that attribute
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.old.uiKey", "testMrold");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.old.requireGroupName", "test:requireGroup");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.old.attributeName", "test:attr:multiAssignable");

    Map<String, String> errors = MembershipRequireAdmin.save(false, "old", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:multiAssignable", null, true, "Old", null, null);
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorGroupScreenAttributeNotSupported"), errors.get("showOnGroupScreen"));

    // without the box it still saves
    errors = MembershipRequireAdmin.save(false, "old", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:multiAssignable", null, false, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());

    // nor a string attribute, which the group screen would show as a text field, not a checkbox
    AttributeDef stringDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:attr:stringDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.string)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true).save();
    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), stringDef).assignName("test:attr:stringAttribute")
        .assignCreateParentStemsIfNotExist(true).save();
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.str.uiKey", "testMrstr");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.str.requireGroupName", "test:requireGroup");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.str.attributeName", "test:attr:stringAttribute");

    errors = MembershipRequireAdmin.save(false, "str", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:stringAttribute", null, true, "Str", null, null);
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorGroupScreenAttributeNotSupported"), errors.get("showOnGroupScreen"));
  }

  /**
   * a requirement whose attribute has since been deleted can still be edited: the attribute cannot be
   * changed, and the problems list reports it
   */
  public void testEditWithDeletedAttribute() {
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.gone.uiKey", "testMrgone");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.gone.requireGroupName", "test:requireGroup");
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.gone.attributeName", "test:attr:doesNotExist");

    assertTrue(MembershipRequireAdmin.retrieve("gone").retrieveProblems().size() > 0);

    Map<String, String> errors = MembershipRequireAdmin.save(false, "gone", null, "veto",
        "test:requireGroup", false, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:doesNotExist", null, false, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());
    assertFalse(MembershipRequireAdmin.retrieve("gone").isHookEnable());

    // but it cannot be put on the group screen, which would show nothing for it
    errors = MembershipRequireAdmin.save(false, "gone", null, "veto",
        "test:requireGroup", false, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:doesNotExist", null, true, "Gone", null, null);
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorAttributeNotFound"), errors.get("showOnGroupScreen"));
  }

  /**
   * the configuration module has the five fields from the metadata in grouper.base.properties, reads a
   * saved requirement's values, and does not take the settings for all requirements for a requirement
   */
  public void testConfigurationModule() {
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.hookEnable", "true");
    try {
      MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
      membershipRequireConfiguration.setConfigId("one");
      assertEquals(GrouperUtil.toSet("uiKey", "attributeName", "requireGroupName", "hookEnable", "sourceIdsToIgnore"),
          membershipRequireConfiguration.retrieveAttributes().keySet());
      assertEquals("testMrone", membershipRequireConfiguration.retrieveValue("uiKey"));
      assertEquals("test:attr:requirementOne", membershipRequireConfiguration.retrieveValue("attributeName"));
      assertEquals("test:requireGroup", membershipRequireConfiguration.retrieveValue("requireGroupName"));

      assertTrue(membershipRequireConfiguration.retrieveConfigurationConfigIds().contains("one"));
      assertFalse(membershipRequireConfiguration.retrieveConfigurationConfigIds().contains("hookEnable"));
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.hookEnable");
    }

    // an incomplete requirement shows blanks for its unset keys, not the example values from
    // grouper.base.properties, and only the uiKey (which is set) is read only
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.partial.uiKey", "testMrpartial");
    MembershipRequireConfiguration partialConfiguration = new MembershipRequireConfiguration();
    partialConfiguration.setConfigId("partial");
    partialConfiguration.markFieldsReadOnly(MembershipRequireAdmin.retrieve("partial"));
    assertEquals("testMrpartial", partialConfiguration.retrieveValue("uiKey"));
    assertNull(partialConfiguration.retrieveValue("attributeName"));
    assertNull(partialConfiguration.retrieveValue("requireGroupName"));
    assertNull(partialConfiguration.retrieveValue("sourceIdsToIgnore"));
    assertEquals("true", partialConfiguration.retrieveValue("hookEnable"));
    assertTrue(partialConfiguration.retrieveAttributes().get("uiKey").isReadOnly());
    assertFalse(partialConfiguration.retrieveAttributes().get("attributeName").isReadOnly());
    assertFalse(partialConfiguration.retrieveAttributes().get("requireGroupName").isReadOnly());

    // the requirement's keys are written through the module: a cleared exempt sources is removed
    assertEquals(0, MembershipRequireAdmin.save(false, "one", null, "veto",
        "test:requireGroup", true, "g:isa", MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, false, null, null, null).size());
    assertEquals("g:isa", MembershipRequireAdmin.retrieve("one").getSourceIdsToIgnore());
    assertEquals(0, MembershipRequireAdmin.save(false, "one", null, "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
        "test:attr:requirementOne", null, false, null, null, null).size());
    assertNull(MembershipRequireAdmin.retrieve("one").getSourceIdsToIgnore());
  }

  /**
   * a missing veto message is a warning, not a problem: the engine still runs the requirement
   */
  public void testMissingTextIsWarning() {
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());
    GrouperCustomComposite.deleteConfig(GrouperCustomComposite.TEXT_PROPERTIES, MembershipRequireAdmin.VETO_TEXT_PREFIX + "testMrone");

    MembershipRequireAdmin membershipRequireAdmin = MembershipRequireAdmin.retrieve("one");
    assertEquals(0, membershipRequireAdmin.retrieveProblems().size());
    assertEquals(1, membershipRequireAdmin.retrieveWarnings().size());

    MembershipRequireEngine.clearCaches();
    boolean found = false;
    for (MembershipRequireConfigBean membershipRequireConfigBean : MembershipRequireEngine.membershipRequireConfigBeans()) {
      if ("one".equals(membershipRequireConfigBean.getConfigId())) {
        found = true;
      }
    }
    assertTrue(found);
  }

  /**
   * an incomplete requirement is listed with its problems, and the engine skips it
   */
  public void testIncompleteHasProblems() {
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, "grouper.membershipRequirement.partial.uiKey", "testMrpartial");
    MembershipRequireAdmin partial = MembershipRequireAdmin.retrieve("partial");
    assertNotNull(partial);
    assertTrue(partial.retrieveProblems().size() > 0);
    MembershipRequireEngine.clearCaches();
    for (MembershipRequireConfigBean membershipRequireConfigBean : MembershipRequireEngine.membershipRequireConfigBeans()) {
      assertFalse("partial".equals(membershipRequireConfigBean.getConfigId()));
    }
  }

  /**
   * validation of the attribute choices and other fields
   */
  public void testValidation() {

    // an attribute definition which cannot be assigned to folders is not suitable
    AttributeDef groupOnlyDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:attr:groupOnlyDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
    assertFalse(MembershipRequireAdmin.isSuitableAttributeDef(groupOnlyDef));

    // nor is one which is multi-assignable
    AttributeDef multiAssignableDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:attr:multiAssignableDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true)
        .assignMultiAssignable(true).save();
    assertFalse(MembershipRequireAdmin.isSuitableAttributeDef(multiAssignableDef));

    Map<String, String> errors = MembershipRequireAdmin.save(true, "bad.id", "testMrbad", "veto",
        "test:doesNotExist", true, "notASource", MembershipRequireAdmin.AttributeOption.newNameInExistingDef,
        "test:attr:newName", "test:attr:groupOnlyDef", false, null, null, null);
    assertTrue(errors.toString(), errors.containsKey("configId"));
    assertTrue(errors.toString(), errors.containsKey("requireGroupName"));
    assertTrue(errors.toString(), errors.containsKey("sourceIdsToIgnore"));
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorAttributeDefNotSuitable"), errors.get("attributeDefName"));

    // nothing was created when the form had errors
    assertNull(AttributeDefNameFinder.findByName("test:attr:newName", false));

    // a uiKey whose veto message key is already UI text
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.TEXT_PROPERTIES, MembershipRequireAdmin.VETO_TEXT_PREFIX + "testMrTaken", "taken");
    errors = MembershipRequireAdmin.save(true, "good", "testMrTaken", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:newName", "test:attr:newDef", false, null, null, null);
    assertEquals(GrouperCustomComposite.text("customCompositeErrorOtherTextExists"), errors.get("uiKey"));
    assertNull(AttributeDefFinder.findByName("test:attr:newDef", false));

    // a requirement does not write a text under the uiKey itself, so an existing UI text key is fine.
    // a test override, so the veto message saved for it is removed with the other test config
    GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertiesOverrideMap().put("testMrTextKey", "some UI text");
    try {
      errors = MembershipRequireAdmin.save(true, "good", "testMrTextKey", "veto",
          "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
          "test:attr:newName", "test:attr:newDef", false, null, null, null);
      assertEquals(errors.toString(), 0, errors.size());
      assertNotNull(AttributeDefFinder.findByName("test:attr:newDef", false));
    } finally {
      GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertiesOverrideMap().remove("testMrTextKey");
    }

    // the form's default attribute name, the folder with nothing after the colon, is refused before
    // the new definition is created
    errors = MembershipRequireAdmin.save(true, "blankName", "testMrblankName", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameAndDef,
        "test:attr:", "test:attr:blankNameDef", false, null, null, null);
    assertEquals(GrouperCustomComposite.text("membershipRequirementErrorNameInvalid"), errors.get("attributeName"));
    assertNull(AttributeDefFinder.findByName("test:attr:blankNameDef", false));

    // a config id which would make the requirement's config be stored as a secret
    errors = saveRequirement("requirePassport", "test:requireGroup", "Passport");
    assertEquals(GrouperCustomComposite.text("customCompositeErrorPasswordRelatedWords"), errors.get("configId"));

    // config id in use
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());
    errors = saveRequirement("one", "test:requireGroup", "OneAgain");
    assertTrue(errors.toString(), errors.containsKey("configId"));
  }

  /**
   * delete removes config and texts but leaves the attribute and its assignment
   */
  public void testDeleteKeepsAttribute() {
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:attr:requirementOne", true);
    this.policyGroup.getAttributeDelegate().assignAttribute(attributeDefName);

    assertNull(MembershipRequireAdmin.delete("one"));

    assertNull(MembershipRequireAdmin.retrieve("one"));
    assertFalse(GrouperUiApiTextConfig.retrieveTextConfigEnUs().containsKey(MembershipRequireAdmin.VETO_TEXT_PREFIX + "testMrone"));
    assertNotNull(AttributeDefNameFinder.findByName("test:attr:requirementOne", false));
    assertTrue(this.policyGroup.getAttributeDelegate().hasAttribute(attributeDefName));
  }

  /**
   * a requirement set in a config file (here a test override) cannot be changed or deleted, and its
   * configuration module fields are all read only
   */
  public void testConfigFileIsReadOnly() {
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());
    MembershipRequireAdmin.delete("one");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.fromFile.uiKey", "testMrfromFile");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.fromFile.attributeName", "test:attr:requirementOne");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.fromFile.requireGroupName", "test:requireGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membershipRequirement.fromFile.hookEnable", "true");
    try {
      MembershipRequireAdmin fromFile = MembershipRequireAdmin.retrieve("fromFile");
      assertFalse(fromFile.isEditable());

      Map<String, String> errors = MembershipRequireAdmin.save(false, "fromFile", null, "veto",
          "test:requireGroup", false, null, MembershipRequireAdmin.AttributeOption.existing,
          "test:attr:requirementOne", null, false, null, null, null);
      assertEquals(GrouperCustomComposite.text("membershipRequirementErrorReadOnly"), errors.get(""));
      assertEquals(GrouperCustomComposite.text("membershipRequirementErrorReadOnly"), MembershipRequireAdmin.delete("fromFile"));

      MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
      membershipRequireConfiguration.setConfigId("fromFile");
      membershipRequireConfiguration.markFieldsReadOnly(fromFile);
      for (GrouperConfigurationModuleAttribute attribute : membershipRequireConfiguration.retrieveAttributes().values()) {
        assertTrue(attribute.getConfigSuffix(), attribute.isReadOnly());
      }
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.fromFile.uiKey");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.fromFile.attributeName");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.fromFile.requireGroupName");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.membershipRequirement.fromFile.hookEnable");
    }
  }

  /**
   * a requirement in the database whose veto message and group screen entry are set in config files
   * (here test overrides): saving and deleting it leaves both as they are, and writes neither to the
   * database
   */
  public void testFileVetoMessageAndGroupScreenKept() {
    assertEquals(0, saveRequirement("one", "test:requireGroup", "One").size());

    String vetoKey = MembershipRequireAdmin.VETO_TEXT_PREFIX + "testMrone";
    GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertiesOverrideMap().put(vetoKey, "Veto from file");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupScreen.attribute.testMrFile.attributeName", "test:attr:requirementOne");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupScreen.attribute.testMrFile.label", "Label from file");
    try {
      MembershipRequireAdmin membershipRequireAdmin = MembershipRequireAdmin.retrieve("one");
      assertTrue(membershipRequireAdmin.isEditable());
      assertTrue(membershipRequireAdmin.isShownOnGroupScreen());

      Map<String, String> errors = MembershipRequireAdmin.save(false, "one", null, "Veto from the screen",
          "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.existing,
          "test:attr:requirementOne", null, true, "Label from the screen", null, null);
      assertEquals(errors.toString(), 0, errors.size());
      assertEquals("Veto from file", GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertyValueString(vetoKey));
      assertEquals("Label from file", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.testMrFile.label"));

      assertNull(MembershipRequireAdmin.delete("one"));
      assertNull(MembershipRequireAdmin.retrieve("one"));
      assertEquals("Label from file", GrouperConfig.retrieveConfig().propertyValueString("groupScreen.attribute.testMrFile.label"));

      // nothing for the group screen was written to the database
      assertEquals(0, (int)new GcDbAccess().sql("select count(*) from grouper_config where config_key like 'groupScreen.attribute.%'")
          .select(int.class));
    } finally {
      GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertiesOverrideMap().remove(vetoKey);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groupScreen.attribute.testMrFile.attributeName");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groupScreen.attribute.testMrFile.label");
    }
  }

  /**
   * a new attribute in a definition which already exists
   */
  public void testNewNameInExistingDef() {
    AttributeDef existingDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:attr:existingDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true)
        .assignMultiAssignable(false).assignMultiValued(false).save();

    Map<String, String> errors = MembershipRequireAdmin.save(true, "inDef", "testMrinDef", "veto",
        "test:requireGroup", true, null, MembershipRequireAdmin.AttributeOption.newNameInExistingDef,
        "test:attr:inExistingDef", "test:attr:existingDef", false, null, null, null);
    assertEquals(errors.toString(), 0, errors.size());

    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:attr:inExistingDef", false);
    assertNotNull(attributeDefName);
    assertEquals(existingDef.getId(), attributeDefName.getAttributeDefId());
    assertEquals("test:attr:inExistingDef", MembershipRequireAdmin.retrieve("inDef").getAttributeName());
    assertEquals(0, MembershipRequireAdmin.retrieve("inDef").retrieveProblems().size());
  }

  /**
   * only a Grouper system administrator can save or delete
   */
  public void testNotWheelRefused() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      try {
        saveRequirement("one", "test:requireGroup", "One");
        fail("should not be allowed");
      } catch (RuntimeException re) {
        // good
      }
      try {
        MembershipRequireAdmin.delete("one");
        fail("should not be allowed");
      } catch (RuntimeException re) {
        // good
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      // the checks below look objects up, which needs a session
      GrouperSession.startRootSession();
    }
    assertNull(AttributeDefFinder.findByName("test:attr:requirementDefOne", false));
    assertNull(MembershipRequireAdmin.retrieve("one"));
  }
}
