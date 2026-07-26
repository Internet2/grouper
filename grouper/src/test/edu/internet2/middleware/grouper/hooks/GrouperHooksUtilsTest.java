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
package edu.internet2.middleware.grouper.hooks;

import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.examples.GrouperAttributeAssignValueRulesConfigHook;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookMethodAndObject;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.RuleUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


/**
 * GRP-7178: one built in hook registration which throws must not stop the
 * registrations which come after it in fireHooksInitHooksIfNotFiredAlready()
 */
public class GrouperHooksUtilsTest extends GrouperTest {

  /**
   * @param name
   */
  public GrouperHooksUtilsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperHooksUtilsTest("testBadBuiltInHookConfigDoesNotDisableOtherHooks"));
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("stem.attribute.validator.regex.0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("stem.attribute.validator.vetoMessage.0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("stem.attribute.validator.attributeName.0");
    GrouperHooksUtils.reloadHooks();
    RuleEngine.clearRuleEngineCache();
    super.tearDown();
  }

  /**
   * a grouper.properties misconfiguration which makes one built in hook registration throw
   * must not silently disable the built in hooks which are registered after it
   */
  public void testBadBuiltInHookConfigDoesNotDisableOtherHooks() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // regex/vetoMessage with no attributeName makes
    // StemAttributeNameValidationHook.registerHookIfNecessary() throw
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.attribute.validator.attributeName.0", null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.attribute.validator.regex.0", "^[a-zA-Z0-9]+$");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.attribute.validator.vetoMessage.0",
        "Stem ID '$attributeValue$' is invalid");

    // reset the once only flag, the hook type caches, and the per hook registered statics
    // so the registration sequence genuinely runs again with the bad config in place
    GrouperHooksUtils.reloadHooks();
    RuleEngine.clearRuleEngineCache();

    // (a) hooks init must still register the hooks which come after the one which throws
    List<GrouperHookMethodAndObject> attributeAssignValueHooks = GrouperHookType.hooksInstances(
        GrouperHookType.ATTRIBUTE_ASSIGN_VALUE,
        AttributeAssignValueHooks.METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_INSERT,
        HooksAttributeAssignValueBean.class);

    assertTrue("GrouperAttributeAssignValueRulesConfigHook must be registered even though "
        + "StemAttributeNameValidationHook registration failed",
        containsHook(attributeAssignValueHooks, GrouperAttributeAssignValueRulesConfigHook.class));

    // (b) end to end, the rules engine still computes the ruleValid attribute value
    Stem stem = new StemSave(grouperSession).assignName("test:testGrp7178")
        .assignCreateParentStemsIfNotExist(true).save();

    AttributeAssign attributeAssign = RuleApi.inheritFolderPrivileges(stem, Scope.SUB,
        SubjectTestHelper.SUBJ0, GrouperUtil.toSet(NamingPrivilege.CREATE));

    assertEquals("T", attributeAssign.getAttributeValueDelegate()
        .retrieveValueString(RuleUtils.ruleValidName()));
  }

  /**
   * @param hooks
   * @param hookClass
   * @return true if one of the hook instances is of this class
   */
  private static boolean containsHook(List<GrouperHookMethodAndObject> hooks, Class<?> hookClass) {
    for (GrouperHookMethodAndObject hook : GrouperUtil.nonNull(hooks)) {
      if (hookClass.isInstance(hook.getHookLogicInstance())) {
        return true;
      }
    }
    return false;
  }

}
