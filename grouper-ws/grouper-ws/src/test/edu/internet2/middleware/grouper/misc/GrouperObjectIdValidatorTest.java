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
package edu.internet2.middleware.grouper.misc;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.coresoap.WsStem;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * Security (GRP-7583): a uuid that a caller chooses for a NEW
 * group, folder, attribute definition or attribute definition name must be a safe identifier
 * (letters, digits, colon, underscore, dot, dash, at most 40 characters).  Before the fix any
 * string was stored, e.g. markup that then rendered in the UI.
 * <p>
 * Each save type (GroupSave, StemSave, AttributeDefSave, AttributeDefNameSave, the Stem API that
 * takes a uuid, and the WS group and folder to-save classes) refuses a bad uuid and creates
 * nothing, and accepts a Grouper generated id and a dashed UUID.  An existing object whose id
 * does not conform (made through an internal path) can still be saved by that id.
 * </p>
 * Before the fix every "refused" test fails (the object is created with the bad id).
 */
public class GrouperObjectIdValidatorTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperObjectIdValidatorTest("testGroupSave"));
  }

  /**
   *
   */
  public GrouperObjectIdValidatorTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperObjectIdValidatorTest(String name) {
    super(name);
  }

  /** markup in an id, the stored XSS case from the report */
  private static final String BAD_ID = "x\"><img src=x onerror=alert(1)>";

  /** a canonical dashed UUID, allowed */
  private static final String DASHED_ID = "123e4567-e89b-42d3-a456-426614174000";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** the folder test objects go in */
  private Stem topStem;

  /**
   * root session and folder
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    this.topStem = new StemSave(this.rootSession).assignName("uuidTest").save();
  }

  /**
   * stop the session
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * true if the throwable or one of its causes is of the type
   * @param t
   * @param type
   * @return true if found
   */
  private static boolean causedBy(Throwable t, Class<? extends Throwable> type) {
    for (Throwable cause = t; cause != null; cause = cause.getCause()) {
      if (type.isInstance(cause)) {
        return true;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    return false;
  }

  /**
   * assert that the runnable is refused by the id validation
   * @param label
   * @param runnable
   */
  private static void assertRefused(String label, Runnable runnable) {
    try {
      runnable.run();
    } catch (RuntimeException re) {
      assertTrue(label + ": refused for the wrong reason: " + re,
          causedBy(re, IllegalArgumentException.class) || causedBy(re, WsInvalidQueryException.class));
      return;
    }
    fail(label + ": a bad uuid was accepted");
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * the allowed set
   */
  public void testValidator() {
    assertTrue(GrouperObjectIdValidator.isValidNewId(null));
    assertTrue(GrouperObjectIdValidator.isValidNewId(""));
    assertTrue(GrouperObjectIdValidator.isValidNewId(GrouperUuid.getUuid()));
    assertTrue(GrouperObjectIdValidator.isValidNewId(DASHED_ID));
    assertTrue(GrouperObjectIdValidator.isValidNewId("legacy:Id_1.2-3"));
    assertTrue(GrouperObjectIdValidator.isValidNewId("1234567890123456789012345678901234567890"));

    assertFalse(GrouperObjectIdValidator.isValidNewId(BAD_ID));
    assertFalse(GrouperObjectIdValidator.isValidNewId("' or '1'='1"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a b"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a/b"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a?b=c"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a#b"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a&b"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a\nb"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("a\\b"));
    assertFalse(GrouperObjectIdValidator.isValidNewId("12345678901234567890123456789012345678901"));

    // the message does not echo the refused value
    assertFalse(GrouperObjectIdValidator.invalidIdMessage("group", BAD_ID).contains("<img"));
  }

  /**
   * GroupSave: bad uuid refused and nothing created; generated and dashed ids allowed
   */
  public void testGroupSave() {
    assertRefused("GroupSave", new Runnable() {
      public void run() {
        new GroupSave(GrouperObjectIdValidatorTest.this.rootSession).assignName("uuidTest:badGroup")
            .assignUuid(BAD_ID).save();
      }
    });
    assertNull(GroupFinder.findByName(this.rootSession, "uuidTest:badGroup", false));

    Group dashed = new GroupSave(this.rootSession).assignName("uuidTest:dashedGroup").assignUuid(DASHED_ID).save();
    assertEquals(DASHED_ID, dashed.getUuid());
    Group generated = new GroupSave(this.rootSession).assignName("uuidTest:generatedGroup").save();
    assertTrue(GrouperObjectIdValidator.isValidNewId(generated.getUuid()));
  }

  /**
   * a group whose id does not conform (made by an internal path, like data from before the fix)
   * can still be found and saved by that id
   */
  public void testExistingOddIdStillSavable() {
    String oddId = "odd id from before";
    Group odd = this.topStem.internal_addChildGroup("oddGroup", "oddGroup", oddId);
    assertEquals(oddId, odd.getUuid());

    Group saved = new GroupSave(this.rootSession).assignName("uuidTest:oddGroup").assignUuid(oddId)
        .assignDescription("updated").save();
    assertEquals(oddId, saved.getUuid());
    assertEquals("updated", GroupFinder.findByUuid(this.rootSession, oddId, true).getDescription());
  }

  /**
   * StemSave and the Stem API that takes a uuid
   */
  public void testStemSave() {
    assertRefused("StemSave", new Runnable() {
      public void run() {
        new StemSave(GrouperObjectIdValidatorTest.this.rootSession).assignName("uuidTest:badStem")
            .assignUuid(BAD_ID).save();
      }
    });
    assertRefused("Stem.addChildStem", new Runnable() {
      public void run() {
        GrouperObjectIdValidatorTest.this.topStem.addChildStem("badStem2", "badStem2", BAD_ID, true);
      }
    });
    assertNull(StemFinder.findByName(this.rootSession, "uuidTest:badStem", false));
    assertNull(StemFinder.findByName(this.rootSession, "uuidTest:badStem2", false));

    Stem dashed = new StemSave(this.rootSession).assignName("uuidTest:dashedStem").assignUuid(DASHED_ID).save();
    assertEquals(DASHED_ID, dashed.getUuid());
  }

  /**
   * AttributeDefSave
   */
  public void testAttributeDefSave() {
    assertRefused("AttributeDefSave", new Runnable() {
      public void run() {
        new AttributeDefSave(GrouperObjectIdValidatorTest.this.rootSession).assignName("uuidTest:badDef")
            .assignId(BAD_ID).assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
      }
    });
    assertNull(AttributeDefFinder.findByName("uuidTest:badDef", false, new QueryOptions().secondLevelCache(false)));

    AttributeDef dashed = new AttributeDefSave(this.rootSession).assignName("uuidTest:dashedDef")
        .assignId(DASHED_ID).assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
    assertEquals(DASHED_ID, dashed.getId());
  }

  /**
   * AttributeDefNameSave and the Stem API that takes a uuid
   */
  public void testAttributeDefNameSave() {
    final AttributeDef attributeDef = new AttributeDefSave(this.rootSession).assignName("uuidTest:def")
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();

    assertRefused("AttributeDefNameSave", new Runnable() {
      public void run() {
        new AttributeDefNameSave(GrouperObjectIdValidatorTest.this.rootSession, attributeDef)
            .assignName("uuidTest:badName").assignUuid(BAD_ID).save();
      }
    });
    assertRefused("Stem.addChildAttributeDefName", new Runnable() {
      public void run() {
        GrouperObjectIdValidatorTest.this.topStem.addChildAttributeDefName(attributeDef, "badName2", "badName2", BAD_ID);
      }
    });
    assertNull(AttributeDefNameFinder.findByName("uuidTest:badName", false));
    assertNull(AttributeDefNameFinder.findByName("uuidTest:badName2", false));

    AttributeDefName dashed = new AttributeDefNameSave(this.rootSession, attributeDef)
        .assignName("uuidTest:dashedName").assignUuid(DASHED_ID).save();
    assertEquals(DASHED_ID, dashed.getId());
  }

  /**
   * WS group and folder saves refuse a bad uuid for a new object (INVALID_QUERY) and accept a
   * good one
   */
  public void testWsToSave() {
    assertRefused("WsGroupToSave", new Runnable() {
      public void run() {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("uuidTest:wsBadGroup");
        wsGroup.setDisplayExtension("wsBadGroup");
        wsGroup.setUuid(BAD_ID);
        WsGroupToSave wsGroupToSave = new WsGroupToSave();
        wsGroupToSave.setWsGroup(wsGroup);
        wsGroupToSave.setWsGroupLookup(new WsGroupLookup("uuidTest:wsBadGroup", null));
        wsGroupToSave.setSaveMode("INSERT_OR_UPDATE");
        wsGroupToSave.save(GrouperObjectIdValidatorTest.this.rootSession, null);
      }
    });
    assertNull(GroupFinder.findByName(this.rootSession, "uuidTest:wsBadGroup", false));

    assertRefused("WsStemToSave", new Runnable() {
      public void run() {
        WsStem wsStem = new WsStem();
        wsStem.setName("uuidTest:wsBadStem");
        wsStem.setDisplayExtension("wsBadStem");
        wsStem.setUuid(BAD_ID);
        WsStemToSave wsStemToSave = new WsStemToSave();
        wsStemToSave.setWsStem(wsStem);
        wsStemToSave.setWsStemLookup(new WsStemLookup("uuidTest:wsBadStem", null));
        wsStemToSave.setSaveMode("INSERT_OR_UPDATE");
        wsStemToSave.save(GrouperObjectIdValidatorTest.this.rootSession);
      }
    });
    assertNull(StemFinder.findByName(this.rootSession, "uuidTest:wsBadStem", false));

    WsGroup wsGroup = new WsGroup();
    wsGroup.setName("uuidTest:wsGoodGroup");
    wsGroup.setDisplayExtension("wsGoodGroup");
    wsGroup.setUuid(DASHED_ID);
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
    wsGroupToSave.setWsGroup(wsGroup);
    wsGroupToSave.setWsGroupLookup(new WsGroupLookup("uuidTest:wsGoodGroup", null));
    wsGroupToSave.setSaveMode("INSERT_OR_UPDATE");
    Group group = wsGroupToSave.save(this.rootSession, null);
    assertEquals(DASHED_ID, group.getUuid());
  }

  /**
   * with createParentStemsIfNotExist, a refused uuid for a new group leaves no parent folders (the uuid is
   * checked before they are created; there is no transaction around the save).  A good uuid still creates
   * them (control)
   */
  public void testGroupSaveRefusedUuidCreatesNoParentFolders() {
    assertRefused("GroupSave with new parent folders", new Runnable() {
      public void run() {
        new GroupSave(GrouperObjectIdValidatorTest.this.rootSession).assignName("uuidTest:newParentA:newParentB:badGroup")
            .assignUuid(BAD_ID).assignCreateParentStemsIfNotExist(true).save();
      }
    });
    assertNull("no parent folder left behind", StemFinder.findByName(this.rootSession, "uuidTest:newParentA", false));

    Group group = new GroupSave(this.rootSession).assignName("uuidTest:newParentA:newParentB:goodGroup")
        .assignUuid(DASHED_ID).assignCreateParentStemsIfNotExist(true).save();
    assertEquals(DASHED_ID, group.getUuid());
    assertNotNull(StemFinder.findByName(this.rootSession, "uuidTest:newParentA:newParentB", false));
  }

  /**
   * with createParentStemsIfNotExist, a refused uuid for a new folder leaves no parent folders.  A good uuid
   * still creates them (control)
   */
  public void testStemSaveRefusedUuidCreatesNoParentFolders() {
    assertRefused("StemSave with new parent folders", new Runnable() {
      public void run() {
        new StemSave(GrouperObjectIdValidatorTest.this.rootSession).assignName("uuidTest:newParentC:newParentD:badFolder")
            .assignUuid(BAD_ID).assignCreateParentStemsIfNotExist(true).save();
      }
    });
    assertNull("no parent folder left behind", StemFinder.findByName(this.rootSession, "uuidTest:newParentC", false));

    Stem stem = new StemSave(this.rootSession).assignName("uuidTest:newParentC:newParentD:goodFolder")
        .assignUuid(DASHED_ID).assignCreateParentStemsIfNotExist(true).save();
    assertEquals(DASHED_ID, stem.getUuid());
    assertNotNull(StemFinder.findByName(this.rootSession, "uuidTest:newParentC:newParentD", false));
  }

}
