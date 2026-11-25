package edu.internet2.middleware.grouper.xml.importXml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObjectIdValidator;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.xml.export.XmlExportMain;
import edu.internet2.middleware.grouper.xml.export.XmlExportUtils;

/**
 * SECURITY: ids are printed in UI pages, links and scripts as trusted values, so an XML import
 * gives a NEW object a generated id when the id in the file is outside the allowed pattern (letters,
 * digits, colon, underscore, dot, dash), and translates the references to it in the rest of the file.
 * Before the fix the object was inserted with the id from the file.
 */
public class XmlImportUnsafeIdTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new XmlImportUnsafeIdTest("testImportReplacesUnsafeGroupId"));
  }

  /**
   * @param name
   */
  public XmlImportUnsafeIdTest(String name) {
    super(name);
  }

  /**
   * an allowed id is kept and not translated; an unsafe one is replaced by a generated id that is
   * recorded in the translation map
   */
  public void testSafeImportedId() {
    Map<String, String> uuidTranslation = new HashMap<String, String>();

    assertEquals("abc123", XmlExportUtils.safeImportedId("abc123", uuidTranslation));
    assertEquals("123e4567-e89b-12d3-a456-426614174000",
        XmlExportUtils.safeImportedId("123e4567-e89b-12d3-a456-426614174000", uuidTranslation));
    assertEquals(0, uuidTranslation.size());

    String unsafeId = "bad\"id<b>";
    String newId = XmlExportUtils.safeImportedId(unsafeId, uuidTranslation);
    assertFalse(unsafeId.equals(newId));
    assertTrue(newId, GrouperObjectIdValidator.isValidNewId(newId));
    assertEquals(newId, uuidTranslation.get(unsafeId));
  }

  /**
   * export a group with a member, change the group id in the file to an unsafe one (as an older
   * Grouper could have), reset, import: the group gets a generated id and keeps its member
   */
  public void testImportReplacesUnsafeGroupId() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:unsafeIdImportGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    group.addMember(SubjectTestHelper.SUBJ0);
    String originalId = group.getId();

    StringWriter writer = new StringWriter();
    new XmlExportMain().writeAllTables(writer, "a string");
    GrouperSession.stopQuietly(grouperSession);

    // the same id everywhere in the file (group, memberships, the group's member row, ...), as XML
    String unsafeId = "bad\"id<b>";
    String xml = writer.toString().replace(originalId, "bad&quot;id&lt;b&gt;");
    assertFalse("the id should be in the export", xml.equals(writer.toString()));

    RegistryReset.reset();
    GrouperTest.initGroupsAndAttributes();

    grouperSession = GrouperSession.startRootSession();
    try {
      new XmlImportMain().processXml(xml);

      Group importedGroup = GroupFinder.findByName(grouperSession, "test:unsafeIdImportGroup", true);
      assertFalse("the unsafe id must not be used", unsafeId.equals(importedGroup.getId()));
      assertTrue(importedGroup.getId(), GrouperObjectIdValidator.isValidNewId(importedGroup.getId()));
      assertNull(GroupFinder.findByUuid(grouperSession, unsafeId, false));
      assertTrue("references in the file should follow the new id", importedGroup.hasMember(SubjectTestHelper.SUBJ0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
