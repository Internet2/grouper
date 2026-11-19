/**
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
 */
package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import junit.textui.TestRunner;

/**
 * Security advisory M7 (reportInstanceOwnerBindingAuthorization): the report detail and download
 * screens (UiV2GrouperReport) take a group or folder and a report instance attribute assign id as
 * two request parameters.  GrouperReportInstanceOwnerBinding must return the instance only when
 * the instance assignment and its report config marker assignment are both on exactly that group
 * or folder, and null (one generic not found) for everything else.
 *
 * <p>Before the fix the screens used GrouperReportInstanceService.getReportInstance(id), which
 * finds any instance in the registry, so this test does not compile without the fix (the class
 * is new).  Same file for every patched release.</p>
 */
public class GrouperReportInstanceOwnerBindingTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperReportInstanceOwnerBindingTest("testInstanceMustBelongToRequestedOwner"));
  }

  /**
   * @param name
   */
  public GrouperReportInstanceOwnerBindingTest(String name) {
    super(name);
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperReporting.enable");
    super.tearDown();
  }

  /**
   * instance on the victim group: found for the victim group only; never for a decoy group, the
   * folder, a wrong attribute or a bogus id; an instance whose config is on another owner is refused
   */
  public void testInstanceMustBelongToRequestedOwner() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Stem stem = new StemSave(rootSession).assignName("secPatch137").assignCreateParentStemsIfNotExist(true).save();
      Group victim = new GroupSave(rootSession).assignName("secPatch137:victim").save();
      Group decoy = new GroupSave(rootSession).assignName("secPatch137:decoy").save();

      AttributeAssign victimConfig = saveReportConfig(victim, "victim config");
      AttributeAssign victimInstance = saveReportInstance(victim, victimConfig.getId());

      // ---- the real owner gets its instance
      GrouperReportInstance found = GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(victimInstance.getId(), victim);
      assertNotNull("instance on its own group", found);
      assertEquals(victimInstance.getId(), found.getAttributeAssignId());
      assertEquals("secPatch137.csv", found.getReportInstanceFileName());

      // ---- the attack: the victim's instance paired with a group the caller can VIEW
      assertNull("instance paired with a decoy group",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(victimInstance.getId(), decoy));

      // ---- a group instance on the folder endpoint
      assertNull("group instance paired with a folder",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(victimInstance.getId(), stem));

      // ---- the config marker id is not a report instance
      assertNull("config marker id as instance id",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(victimConfig.getId(), victim));

      // ---- bogus and blank ids: same answer as not allowed
      assertNull("bogus id", GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner("abc123notAnId", victim));
      assertNull("blank id", GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner("", victim));
      assertNull("null owner", GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(victimInstance.getId(), null));

      // ---- an instance on the decoy that points at the victim's config: the config owner must match too
      AttributeAssign crossInstance = saveReportInstance(decoy, victimConfig.getId());
      assertNull("instance on decoy with the victim's config, asked for the decoy",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(crossInstance.getId(), decoy));
      assertNull("instance on decoy with the victim's config, asked for the victim",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(crossInstance.getId(), victim));

      // ---- folder owner: positive control and a group in the folder as decoy
      AttributeAssign stemConfig = saveReportConfig(stem, "folder config");
      AttributeAssign stemInstance = saveReportInstance(stem, stemConfig.getId());
      assertNotNull("instance on its own folder",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(stemInstance.getId(), stem));
      assertNull("folder instance paired with a group",
          GrouperReportInstanceOwnerBinding.retrieveReportInstanceForOwner(stemInstance.getId(), decoy));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * report config marker with a name (the other config attributes are optional for the lookup)
   * @param owner group or folder
   * @param name report config name
   * @return the marker assignment
   */
  private static AttributeAssign saveReportConfig(GrouperObject owner, String name) {
    AttributeAssign attributeAssign = owner instanceof Group
        ? ((Group)owner).getAttributeDelegate().addAttribute(GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign()
        : ((Stem)owner).getAttributeDelegate().addAttribute(GrouperReportConfigAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_NAME, name);
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_TYPE, "SQL");
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportConfigAttributeNames.GROUPER_REPORT_CONFIG_FORMAT, "CSV");
    return attributeAssign;
  }

  /**
   * report instance pointing at a config marker
   * @param owner group or folder the instance is assigned to
   * @param configMarkerAssignId
   * @return the instance assignment
   */
  private static AttributeAssign saveReportInstance(GrouperObject owner, String configMarkerAssignId) {
    AttributeAssign attributeAssign = owner instanceof Group
        ? ((Group)owner).getAttributeDelegate().addAttribute(GrouperReportInstanceAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign()
        : ((Stem)owner).getAttributeDelegate().addAttribute(GrouperReportInstanceAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_STATUS, GrouperReportInstance.STATUS_SUCCESS);
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, configMarkerAssignId);
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970, String.valueOf(System.currentTimeMillis()));
    attributeAssign.getAttributeValueDelegate().assignValue(
        reportConfigStemName() + ":" + GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_NAME, "secPatch137.csv");
    return attributeAssign;
  }
}
