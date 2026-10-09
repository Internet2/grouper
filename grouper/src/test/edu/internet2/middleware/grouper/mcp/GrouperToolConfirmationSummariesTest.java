/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * what a user is shown before a write tool runs.  no database needed
 */
public class GrouperToolConfirmationSummariesTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperToolConfirmationSummariesTest.class);
  }

  /**
   * @param name
   */
  public GrouperToolConfirmationSummariesTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param json JSON text
   * @return the parsed arguments
   */
  private static JsonNode json(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * every call a tool counts as a write has a summary, so the user is never asked to approve
   * raw JSON.  a write tool added later without one fails here
   */
  public void testEveryWriteHasSummary() {

    ObjectNode noArguments = objectMapper.createObjectNode();
    ObjectNode update = objectMapper.createObjectNode();
    update.put("action", "update");

    int writes = 0;
    for (GrouperTool grouperTool : GrouperToolRegistry.advertisedTools()) {
      for (JsonNode arguments : new JsonNode[] {noArguments, update}) {
        if (grouperTool.category(arguments).isWrite()) {
          writes++;
          assertNotNull(grouperTool.name() + " " + arguments, grouperTool.confirmationSummary(arguments));
        } else {
          assertNull(grouperTool.name() + " " + arguments, grouperTool.confirmationSummary(arguments));
        }
      }
    }
    assertTrue(writes > 0);
  }

  /**
   * adding members, and replacing all of them, which has to stand out
   */
  public void testGroupAddMember() {

    assertEquals("Add 2 subjects: 'jsmith', '12345' (source 'ldap', as 'subjectId') to group 'a:b' "
        + "(also given: disabledTime: '2026/12/31 00:00:00.000')",
        GrouperToolConfirmationSummaries.groupAddMember(json("{\"groupName\":\"a:b\","
            + "\"subjects\":[{\"subjectIdOrIdentifier\":\"jsmith\"},"
            + "{\"subjectIdOrIdentifier\":\"12345\",\"sourceId\":\"ldap\",\"subjectIdType\":\"subjectId\"}],"
            + "\"disabledTime\":\"2026/12/31 00:00:00.000\"}")));

    assertEquals("Replace ALL existing members of the 'admins' list of group 'a:b' with 1 subject: 'jsmith'",
        GrouperToolConfirmationSummaries.groupAddMember(json("{\"groupName\":\"a:b\",\"fieldName\":\"admins\","
            + "\"replaceAllExisting\":true,\"subjects\":[{\"subjectIdOrIdentifier\":\"jsmith\"}]}")));

    // read the way the tool reads it
    assertTrue(GrouperToolConfirmationSummaries.groupAddMember(json("{\"groupName\":\"a:b\","
        + "\"replaceAllExisting\":\"true\",\"subjects\":[]}")).startsWith("Replace ALL"));
  }

  /**
   * a privilege call without allowed is a revoke.  the privilege type is part of the headline,
   * not an extra
   */
  public void testPrivilegeAssign() {

    assertEquals("Grant the 'update' access privilege on group 'a:b' to 'jsmith'",
        GrouperToolConfirmationSummaries.privilegeAssign(json("{\"groupName\":\"a:b\","
            + "\"subjectIdOrIdentifier\":\"jsmith\",\"privilegeType\":\"access\",\"privilegeName\":\"update\","
            + "\"allowed\":true}")));

    assertEquals("Revoke the 'stemAdmin' naming privilege on folder 'a' from 'jsmith'",
        GrouperToolConfirmationSummaries.privilegeAssign(json("{\"stemName\":\"a\","
            + "\"subjectIdOrIdentifier\":\"jsmith\",\"privilegeType\":\"naming\",\"privilegeName\":\"stemAdmin\"}")));

    // no privilege type given
    assertEquals("Grant the 'read' privilege on group 'a:b' to 'jsmith'",
        GrouperToolConfirmationSummaries.privilegeAssign(json("{\"groupName\":\"a:b\","
            + "\"subjectIdOrIdentifier\":\"jsmith\",\"privilegeName\":\"read\",\"allowed\":true}")));

    // a blank one is shown, not hidden
    assertEquals("Grant the 'read' '' privilege on group 'a:b' to 'jsmith'",
        GrouperToolConfirmationSummaries.privilegeAssign(json("{\"groupName\":\"a:b\","
            + "\"subjectIdOrIdentifier\":\"jsmith\",\"privilegeType\":\"\",\"privilegeName\":\"read\",\"allowed\":true}")));

    // anything but access or naming is quoted and escaped, so it cannot pass for part of the sentence
    assertEquals("Grant the 'read' 'access privilege on group \\'c:d\\' to \\'nobody\\'.\\n' privilege "
        + "on group 'a:b' to 'jsmith'",
        GrouperToolConfirmationSummaries.privilegeAssign(json("{\"groupName\":\"a:b\","
            + "\"subjectIdOrIdentifier\":\"jsmith\",\"privilegeType\":\"access privilege on group 'c:d' "
            + "to 'nobody'.\\n\",\"privilegeName\":\"read\",\"allowed\":true}")));
  }

  /**
   * group changes name the action and show everything else given
   */
  public void testGroupSave() {

    assertEquals("Rename group 'a:b' (also given: newExtension: 'c', setAlternateNameIfRename: true)",
        GrouperToolConfirmationSummaries.groupSave(json("{\"action\":\"renameGroup\",\"groupName\":\"a:b\","
            + "\"newExtension\":\"c\",\"setAlternateNameIfRename\":true}")));

    assertEquals("Change group 'a:b' with action 'somethingNew'",
        GrouperToolConfirmationSummaries.groupSave(json("{\"action\":\"somethingNew\",\"groupName\":\"a:b\"}")));
  }

  /**
   * text from the model cannot close a quote, start a new line, reorder what is shown, or pass an
   * argument name off as the summary's own words
   */
  public void testSpoofingEscaped() {

    // a quote in a value stays inside the value
    assertEquals("Delete group 'a:b\\' is kept, this deletes \\'x'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b' is kept, this deletes 'x\"}")));

    // a new line in a value
    String summary = GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b\\n\\nOK to approve\"}"));
    assertEquals("Delete group 'a:b\\n\\nOK to approve'", summary);
    assertEquals(-1, summary.indexOf('\n'));

    // a right to left override and a zero width space
    assertEquals("Delete group 'a\\u202eb\\u200bc'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a\\u202eb\\u200bc\"}")));

    // an argument name the tool ignores, written to read like part of the summary
    summary = GrouperToolConfirmationSummaries.groupAddMember(json("{\"groupName\":\"a:b\",\"replaceAllExisting\":true,"
        + "\"subjects\":[{\"subjectIdOrIdentifier\":\"jsmith\"}],"
        + "\"\\n\\nNote: existing members are kept\":1}"));
    assertEquals("Replace ALL existing members of group 'a:b' with 1 subject: 'jsmith' "
        + "(also given: '\\n\\nNote: existing members are kept': 1)", summary);
    assertEquals(-1, summary.indexOf('\n'));

    // non text values are JSON, with invisible characters escaped too
    assertEquals("Delete group 'a:b' (also given: extra: [\"x\\u202e\"])",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b\",\"extra\":[\"x\\u202e\"]}")));

    // an invisible tag character above U+FFFF, shown as its two surrogate halves
    assertEquals("Delete group 'a:b\\udb40\\udc41'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b\\udb40\\udc41\"}")));

    // half a surrogate pair on its own
    assertEquals("Delete group 'a:b\\ud800'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b\\ud800\"}")));

    // an ordinary character above U+FFFF is left alone
    String emoji = new String(Character.toChars(0x1F600));
    assertEquals("Delete group 'a:b" + emoji + "'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b" + emoji + "\"}")));
  }

  /**
   * the rest of the write tools
   */
  public void testOtherWrites() {

    assertEquals("Remove 1 subject: 'jsmith' from group 'a:b'",
        GrouperToolConfirmationSummaries.groupRemoveMember(json("{\"groupName\":\"a:b\","
            + "\"subjects\":[{\"subjectIdOrIdentifier\":\"jsmith\"}]}")));

    assertEquals("Delete group 'a:b'",
        GrouperToolConfirmationSummaries.groupDelete(json("{\"groupName\":\"a:b\"}")));

    assertEquals("Delete folder (not given)",
        GrouperToolConfirmationSummaries.folderDelete(json("{}")));

    assertEquals("Run daemon job 'CHANGE_LOG_consumer_x' now",
        GrouperToolConfirmationSummaries.adminDaemonJobRun(json("{\"jobName\":\"CHANGE_LOG_consumer_x\"}")));

    assertEquals("Update recipe 'Adding people', which changes guidance for everybody who uses it "
        + "(also given: body: 'new text')",
        GrouperToolConfirmationSummaries.recipeUpdate(json("{\"action\":\"update\",\"name\":\"Adding people\","
            + "\"body\":\"new text\"}")));

    assertEquals("Run institutional tool 'addStudentWorker', a script which can make changes "
        + "(also given: ownerType: 'group', ownerGroupName: 'a:b', inputs: {\"netId\":\"jsmith\"})",
        GrouperToolConfirmationSummaries.institutionalToolExecute(json("{\"action\":\"execute\","
            + "\"configId\":\"addStudentWorker\",\"ownerType\":\"group\",\"ownerGroupName\":\"a:b\","
            + "\"inputs\":{\"netId\":\"jsmith\"}}")));

    assertEquals("Change attribute assignments: 'assign_attr' attribute 'a:attr' "
        + "(also given: attributeAssignType: 'group', ownerGroupName: 'a:b', values: [\"x\"])",
        GrouperToolConfirmationSummaries.attributeAssignmentSave(json("{\"attributeAssignType\":\"group\","
            + "\"attributeAssignOperation\":\"assign_attr\",\"attributeDefNameName\":\"a:attr\","
            + "\"ownerGroupName\":\"a:b\",\"values\":[\"x\"]}")));
  }

}
