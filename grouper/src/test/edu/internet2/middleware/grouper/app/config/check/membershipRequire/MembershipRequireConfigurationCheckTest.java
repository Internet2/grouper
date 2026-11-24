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
package edu.internet2.middleware.grouper.app.config.check.membershipRequire;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * the configuration review check for membership requirements and custom composites which are
 * configured but do not work
 *
 * @author shilen
 */
public class MembershipRequireConfigurationCheckTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipRequireConfigurationCheckTest("testBrokenRequirementAndIncompleteComposite"));
  }

  /**
   * @param name
   */
  public MembershipRequireConfigurationCheckTest(String name) {
    super(name);
  }

  /**
   *
   */
  public MembershipRequireConfigurationCheckTest() {
    super();
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSession();
  }

  /** every override this test set, removed in tearDown */
  private List<String> overrideKeys = new ArrayList<String>();

  /**
   * set a grouper.properties override which tearDown removes
   * @param key the key
   * @param value the value
   */
  private void override(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
    this.overrideKeys.add(key);
  }

  @Override
  protected void tearDown() {
    for (String key : this.overrideKeys) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
    super.tearDown();
  }

  /**
   * the finding for a property name, or null
   * @param results the findings
   * @param propertyName the property name
   * @return the finding
   */
  private static ConfigurationCheckResult findResult(List<ConfigurationCheckResult> results, String propertyName) {
    for (ConfigurationCheckResult result : results) {
      if (propertyName.equals(result.getPropertyName())) {
        return result;
      }
    }
    return null;
  }

  /**
   * a working requirement and complete composites with a real group and type have no findings, and
   * composites whose group does not exist or whose type is not valid are warnings
   */
  public void testWorkingAndBrokenComposites() {

    new GroupSave().assignName("test:configCheckRequireGroup").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName("test:configCheckDef")
        .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true).save();
    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDef).assignName("test:configCheckAttribute")
        .assignCreateParentStemsIfNotExist(true).save();

    override("grouper.membershipRequirement.configCheckWorks.uiKey", "configCheckWorks");
    override("grouper.membershipRequirement.configCheckWorks.attributeName", "test:configCheckAttribute");
    override("grouper.membershipRequirement.configCheckWorks.requireGroupName", "test:configCheckRequireGroup");

    for (int index : new int[] {78, 79, 80}) {
      override("grouper.membership.customComposite.uiKey." + index, "configCheckComposite" + index);
    }
    override("grouper.membership.customComposite.compositeType.78", "complement");
    override("grouper.membership.customComposite.groupName.78", "test:configCheckRequireGroup");
    override("grouper.membership.customComposite.compositeType.79", "complement");
    override("grouper.membership.customComposite.groupName.79", "test:configCheckDoesNotExist");
    override("grouper.membership.customComposite.compositeType.80", "union");
    override("grouper.membership.customComposite.groupName.80", "test:configCheckRequireGroup");

    List<ConfigurationCheckResult> results = new MembershipRequireConfigurationCheck().checkConfiguration();

    assertNull(results.toString(), findResult(results, "grouper.membershipRequirement.configCheckWorks.*"));
    assertNull(results.toString(), findResult(results, "grouper.membership.customComposite.*.78"));
    assertEquals(ConfigurationCheckSeverity.WARNING, findResult(results, "grouper.membership.customComposite.*.79").getSeverity());
    assertEquals(ConfigurationCheckSeverity.WARNING, findResult(results, "grouper.membership.customComposite.*.80").getSeverity());
  }

  /**
   * a requirement whose attribute and group do not exist is an error, an index with only its uiKey a
   * warning.  only findings for this test's config are looked at, so other config does not matter
   */
  public void testBrokenRequirementAndIncompleteComposite() {

    override("grouper.membershipRequirement.configCheckTest.uiKey", "configCheckTest");
    override("grouper.membershipRequirement.configCheckTest.attributeName", "test:doesNotExist:attribute");
    override("grouper.membershipRequirement.configCheckTest.requireGroupName", "test:doesNotExist:group");
    override("grouper.membership.customComposite.uiKey.77", "configCheckTestComposite");

    List<ConfigurationCheckResult> results = new MembershipRequireConfigurationCheck().checkConfiguration();

    ConfigurationCheckResult requirementResult = findResult(results, "grouper.membershipRequirement.configCheckTest.*");
    ConfigurationCheckResult compositeResult = findResult(results, "grouper.membership.customComposite.*.77");

    assertNotNull(results.toString(), requirementResult);
    assertEquals(ConfigurationCheckSeverity.ERROR, requirementResult.getSeverity());
    assertTrue(requirementResult.getMessage(), requirementResult.getMessage().contains("configCheckTest"));
    // the message is English, not UI text
    assertTrue(requirementResult.getMessage(), requirementResult.getMessage().contains(
        "Its attribute test:doesNotExist:attribute does not exist."));
    assertTrue(requirementResult.getMessage(), requirementResult.getMessage().contains(
        "Its required group test:doesNotExist:group does not exist."));

    assertNotNull(results.toString(), compositeResult);
    assertEquals(ConfigurationCheckSeverity.WARNING, compositeResult.getSeverity());
  }
}
