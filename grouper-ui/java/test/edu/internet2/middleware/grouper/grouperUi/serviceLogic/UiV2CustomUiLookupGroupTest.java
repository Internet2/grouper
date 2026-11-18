package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import junit.textui.TestRunner;

/**
 * The custom UI screens look up the group from the request in a root session. Before the fix any
 * logged-in user could pass the uuid of a group with no custom UI, and the error the UI showed
 * ("No custom ui config id found for group: <name>") revealed the full group name; a bad uuid gave
 * a different error, which revealed whether the group exists. The lookup must only return groups
 * with an enabled custom UI and fail with the same, name-free message otherwise.
 */
public class UiV2CustomUiLookupGroupTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2CustomUiLookupGroupTest("testLookupOnlyGroupsWithEnabledCustomUi"));
  }

  /**
   *
   */
  public UiV2CustomUiLookupGroupTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2CustomUiLookupGroupTest(String name) {
    super(name);
  }

  /**
   * call the helper and return the exception message, or fail if it did not throw
   * @param rootSession
   * @param groupId
   * @return the message
   */
  private static String lookupErrorMessage(GrouperSession rootSession, String groupId) {
    try {
      UiV2CustomUi.retrieveCustomUiGroupOrFail(rootSession, groupId);
    } catch (RuntimeException re) {
      return re.getMessage();
    }
    fail("Expected lookup of " + groupId + " to be refused");
    return null;
  }

  /**
   * only groups with an enabled custom UI are returned; other groups and unknown uuids fail
   * with the same message that holds only the supplied id
   */
  public void testLookupOnlyGroupsWithEnabledCustomUi() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    try {
      // a group with no custom UI, and one with a custom UI configured
      Group plainGroup = new GroupSave(rootSession).assignName("test:secretPlainGroup")
          .assignCreateParentStemsIfNotExist(true).save();
      Group customUiGroup = new GroupSave(rootSession).assignName("test:secretCustomUiGroup")
          .assignCreateParentStemsIfNotExist(true).save();

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.testLookup.groupUUIDOrName", customUiGroup.getId());
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.testLookup.enabled", "true");

      // the configured group is returned
      Group found = UiV2CustomUi.retrieveCustomUiGroupOrFail(rootSession, customUiGroup.getId());
      assertEquals(customUiGroup.getId(), found.getId());

      // a group with no custom UI is refused, and the message does not hold its name
      String plainMessage = lookupErrorMessage(rootSession, plainGroup.getId());
      assertFalse(plainMessage, plainMessage.contains(plainGroup.getName()));
      assertFalse(plainMessage, plainMessage.contains("secretPlainGroup"));

      // an unknown uuid is refused with the same message (other than the id), so it is not an existence oracle
      String randomId = GrouperUuid.getUuid();
      String randomMessage = lookupErrorMessage(rootSession, randomId);
      assertEquals(plainMessage.replace(plainGroup.getId(), "X"), randomMessage.replace(randomId, "X"));

      // a configured but disabled custom UI is refused too
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperCustomUI.testLookup.enabled", "false");
      String disabledMessage = lookupErrorMessage(rootSession, customUiGroup.getId());
      assertFalse(disabledMessage, disabledMessage.contains("secretCustomUiGroup"));
      assertEquals(plainMessage.replace(plainGroup.getId(), "X"), disabledMessage.replace(customUiGroup.getId(), "X"));

      // blank id is refused
      lookupErrorMessage(rootSession, "");

    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperCustomUI.testLookup.groupUUIDOrName");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperCustomUI.testLookup.enabled");
      GrouperSession.stopQuietly(rootSession);
    }
  }

}
