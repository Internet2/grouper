package edu.internet2.middleware.grouper.ui.util;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.textui.TestRunner;

/**
 * the custom composites offered on the members screen: the indexes can have gaps, an incomplete one
 * is skipped, and a user only sees one whose group they can read
 *
 * @author shilen
 */
public class GrouperUiUtilsCustomCompositeTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperUiUtilsCustomCompositeTest("testGapsIncompleteAndRead"));
  }

  /**
   * @param name
   */
  public GrouperUiUtilsCustomCompositeTest(String name) {
    super(name);
  }

  /**
   *
   */
  public GrouperUiUtilsCustomCompositeTest() {
    super();
  }

  /**
   * index 5 is complete and readable, there is no 6, 7 has only its uiKey, and 8's group cannot be read
   */
  public void testGapsIncompleteAndRead() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession.startRootSession();
    Group readableGroup = new GroupSave().assignName("test:ccReadable").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:ccNotReadable").assignCreateParentStemsIfNotExist(true).save();
    readableGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    String[] keys = new String[] {
        "grouper.membership.customComposite.uiKey.5", "grouper.membership.customComposite.compositeType.5",
        "grouper.membership.customComposite.groupName.5", "grouper.membership.customComposite.uiKey.7",
        "grouper.membership.customComposite.uiKey.8", "grouper.membership.customComposite.compositeType.8",
        "grouper.membership.customComposite.groupName.8"};
    String[] values = new String[] {"testCcReadable", "intersection", "test:ccReadable", "testCcIncomplete",
        "testCcNotReadable", "complement", "test:ccNotReadable"};
    for (int i = 0; i < keys.length; i++) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keys[i], values[i]);
    }
    try {
      // the setup is seen: the composite's index, and the user's READ on its group
      assertTrue(GrouperCustomComposite.retrieveIndexes().toString(), GrouperCustomComposite.retrieveIndexes().contains(5));
      assertTrue(readableGroup.canHavePrivilege(SubjectTestHelper.SUBJ0, AccessPrivilege.READ.getName(), false));

      Map<Integer, String> uiKeys = GrouperUiUtils.getCustomCompositeUiKeys(SubjectTestHelper.SUBJ0);
      assertEquals("testCcReadable", uiKeys.get(5));
      assertFalse(uiKeys.toString(), uiKeys.containsKey(7));
      assertFalse(uiKeys.toString(), uiKeys.containsKey(8));

      // a sysadmin can read both groups, and still does not get the incomplete one
      uiKeys = GrouperUiUtils.getCustomCompositeUiKeys(SubjectTestHelper.SUBJR);
      assertEquals("testCcReadable", uiKeys.get(5));
      assertEquals("testCcNotReadable", uiKeys.get(8));
      assertFalse(uiKeys.toString(), uiKeys.containsKey(7));
    } finally {
      for (String key : keys) {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(key);
      }
    }
  }
}
