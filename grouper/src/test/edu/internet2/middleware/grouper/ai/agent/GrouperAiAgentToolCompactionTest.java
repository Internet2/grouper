/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.mcp.GrouperTool;
import edu.internet2.middleware.grouper.mcp.GrouperToolRegistry;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpRecipeTool;

/**
 * the tools the AI agent offers compact, tool_describe answered from the full definitions, and the
 * check for arguments a tool does not take.  no database needed
 */
public class GrouperAiAgentToolCompactionTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperAiAgentToolCompactionTest.class);
  }

  /**
   * @param name
   */
  public GrouperAiAgentToolCompactionTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param json JSON text, with single quotes for double
   * @return the parsed JSON
   */
  private static JsonNode json(String json) {
    try {
      return objectMapper.readTree(json.replace('\'', '"'));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return full definitions of group_find, group_get_members, entity_whoami, which has no
   * parameters, and a tool with no summary
   */
  private static ArrayNode fullDefinitions() {
    return (ArrayNode)json("["
        + "{'name':'group_find','description':'Find groups, at length.',"
        + "'inputSchema':{'type':'object','properties':{'queryFilterType':{'type':'string'},"
        + "'groupName':{'type':'string'}}}},"
        + "{'name':'group_get_members','description':'Get members, at length.',"
        + "'inputSchema':{'type':'object','properties':{'groupName':{'type':'string'},"
        + "'privilegeListName':{'type':'string'}}}},"
        + "{'name':'entity_whoami','description':'Who am I.','inputSchema':{'type':'object'}},"
        + "{'name':'noSummaryTool','description':'No summary is written for this.',"
        + "'inputSchema':{'type':'object','properties':{'a':{'type':'string'}}}}"
        + "]");
  }

  /**
   * @param toolDefinitions definitions
   * @param name a tool name
   * @return its definition, or null
   */
  private static JsonNode find(ArrayNode toolDefinitions, String name) {
    for (JsonNode toolDefinition : toolDefinitions) {
      if (name.equals(toolDefinition.path("name").asText())) {
        return toolDefinition;
      }
    }
    return null;
  }

  /**
   * @param toolNames names
   * @return tool_describe arguments
   */
  private static ObjectNode describeArguments(String... toolNames) {
    ObjectNode arguments = objectMapper.createObjectNode();
    ArrayNode tools = arguments.putArray("tools");
    for (String toolName : toolNames) {
      tools.add(toolName);
    }
    return arguments;
  }

  /**
   * @param result a tool result
   * @return its text
   */
  private static String text(ObjectNode result) {
    return result.path("content").get(0).path("text").asText();
  }

  /**
   * @param result a tool_describe result which worked
   * @return its text, parsed
   */
  private static JsonNode describeResult(ObjectNode result) {
    try {
      return objectMapper.readTree(text(result));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * the description becomes the summary, followed by the configured part of the full description
   * when the tool has one and then any recipe pointers, the schema is empty, and the other fields
   * of the definition stay, in order
   */
  public void testCompactToolDefinition() {

    ObjectNode fullDefinition = (ObjectNode)json("{'name':'group_add_member',"
        + "'description':'A long description of adding members.',"
        + "'inputSchema':{'type':'object','properties':{'replaceAllExisting':{'type':'boolean',"
        + "'description':'long'}}},'annotations':{'destructiveHint':false}}");

    ObjectNode compact = GrouperAiAgentToolCompaction.compactToolDefinition("group_add_member", fullDefinition);

    assertEquals("group_add_member", compact.path("name").asText());
    assertEquals(GrouperAiAgentToolSummaries.summary("group_add_member") + " "
        + GrouperAiAgentToolCompaction.DESCRIBE_FIRST, compact.path("description").asText());
    assertEquals(json("{'type':'object','properties':{}}"), compact.path("inputSchema"));
    assertFalse(compact.path("annotations").path("destructiveHint").asBoolean(true));

    // the full definition is not changed
    assertEquals("A long description of adding members.", fullDefinition.path("description").asText());
    assertTrue(fullDefinition.path("inputSchema").path("properties").has("replaceAllExisting"));

    // the institution's configured part of a description is kept
    ObjectNode recipeDefinition = (ObjectNode)json("{'name':'recipe',"
        + "'description':'How to use recipes, at length. " + GrouperMcpRecipeTool.AVAILABLE_RECIPES
        + "policyGroups - how to make one.','inputSchema':{'type':'object'}}");
    assertEquals(GrouperAiAgentToolSummaries.summary("recipe") + " " + GrouperAiAgentToolCompaction.DESCRIBE_FIRST
        + " " + GrouperMcpRecipeTool.AVAILABLE_RECIPES + "policyGroups - how to make one.",
        GrouperAiAgentToolCompaction.compactToolDefinition("recipe", recipeDefinition).path("description").asText());

    // and left out when the description does not have it
    recipeDefinition.put("description", "No list here.");
    assertEquals(GrouperAiAgentToolSummaries.summary("recipe") + " " + GrouperAiAgentToolCompaction.DESCRIBE_FIRST,
        GrouperAiAgentToolCompaction.compactToolDefinition("recipe", recipeDefinition).path("description").asText());

    // recipe pointers are this institution's routing, so they are kept after the summary
    String pointer = GrouperMcpRecipeTool.RECIPE_POINTER_START + "Use the template. You MUST read it "
        + "with the recipe tool, action 'get', name 'policy', before using this tool for that case, and "
        + "follow it.";
    String further = GrouperMcpRecipeTool.FURTHER_RECIPES_START + 2 + GrouperMcpRecipeTool.FURTHER_RECIPES_END;
    ObjectNode pointed = (ObjectNode)json("{'name':'group_save','inputSchema':{'type':'object'}}");
    pointed.put("description", "Save a group, at length." + pointer + further);
    assertEquals(GrouperAiAgentToolSummaries.summary("group_save") + " " + GrouperAiAgentToolCompaction.DESCRIBE_FIRST
        + pointer + further,
        GrouperAiAgentToolCompaction.compactToolDefinition("group_save", pointed).path("description").asText());

    // with no pointer shown, only the note about further recipes
    pointed.put("description", "Save a group, at length." + further);
    assertEquals(GrouperAiAgentToolSummaries.summary("group_save") + " " + GrouperAiAgentToolCompaction.DESCRIBE_FIRST
        + further,
        GrouperAiAgentToolCompaction.compactToolDefinition("group_save", pointed).path("description").asText());
  }

  /**
   * every advertised tool has a summary, so none is offered in full for want of one
   */
  public void testEveryToolHasSummary() {
    for (GrouperTool grouperTool : GrouperToolRegistry.advertisedTools()) {
      String summary = GrouperAiAgentToolSummaries.summary(grouperTool.name());
      assertTrue(grouperTool.name(), summary != null && summary.trim().length() > 0);
    }
  }

  /**
   * compact: every tool with a summary and parameters is offered compact, tool_describe is added,
   * and the tools in alwaysFull, one with no summary and one with no parameters are offered as they
   * are.  not compact, or nothing compact: the list as it came, with no tool_describe
   */
  public void testToolsOffered() {

    ArrayNode fullDefinitions = fullDefinitions();

    GrouperAiAgentToolCompaction compaction = new GrouperAiAgentToolCompaction(fullDefinitions, true, null);
    ArrayNode offered = compaction.getToolDefinitionsToOffer();

    assertEquals(fullDefinitions.size() + 1, offered.size());
    assertEquals(GrouperAiAgentToolCompaction.TOOL_DESCRIBE, offered.get(offered.size() - 1).path("name").asText());
    assertEquals(GrouperAiAgentToolCompaction.toolDescribeDefinition(), find(offered, GrouperAiAgentToolCompaction.TOOL_DESCRIBE));
    assertTrue(compaction.isToolDescribe(GrouperAiAgentToolCompaction.TOOL_DESCRIBE));

    assertEquals(GrouperAiAgentToolCompaction.compactToolDefinition("group_find", (ObjectNode)find(fullDefinitions, "group_find")),
        find(offered, "group_find"));
    assertEquals(0, find(offered, "group_get_members").path("inputSchema").path("properties").size());
    assertEquals(find(fullDefinitions, "noSummaryTool"), find(offered, "noSummaryTool"));
    // nothing to describe, so it needs no describe step
    assertEquals(find(fullDefinitions, "entity_whoami"), find(offered, "entity_whoami"));

    // the full definitions are not changed
    assertEquals("Find groups, at length.", find(fullDefinitions, "group_find").path("description").asText());

    // the tools named in alwaysFull are offered as they are
    ArrayNode configured = new GrouperAiAgentToolCompaction(fullDefinitions, true, " group_find , noSuchTool")
        .getToolDefinitionsToOffer();
    assertEquals(find(fullDefinitions, "group_find"), find(configured, "group_find"));
    assertFalse(find(fullDefinitions, "group_get_members").equals(find(configured, "group_get_members")));
    assertNotNull(find(configured, GrouperAiAgentToolCompaction.TOOL_DESCRIBE));

    // not compact: the list as it came, and tool_describe is not answered
    GrouperAiAgentToolCompaction notCompact = new GrouperAiAgentToolCompaction(fullDefinitions, false, null);
    assertSame(fullDefinitions, notCompact.getToolDefinitionsToOffer());
    assertFalse(notCompact.isToolDescribe(GrouperAiAgentToolCompaction.TOOL_DESCRIBE));

    // every tool in alwaysFull: nothing compact, so nothing to describe
    GrouperAiAgentToolCompaction allFull = new GrouperAiAgentToolCompaction(fullDefinitions, true,
        "group_find,group_get_members");
    assertSame(fullDefinitions, allFull.getToolDefinitionsToOffer());
    assertFalse(allFull.isToolDescribe(GrouperAiAgentToolCompaction.TOOL_DESCRIBE));

    // no tools
    assertNull(new GrouperAiAgentToolCompaction(null, true, null).getToolDefinitionsToOffer());
  }

  /**
   * kept while the user approves changes: which tools are compact comes from the list the model was
   * offered, not from the configuration, which may have changed since
   */
  public void testOfferedEarlier() {

    ArrayNode fullDefinitions = fullDefinitions();
    ArrayNode offeredCompact = new GrouperAiAgentToolCompaction(fullDefinitions, true, "group_find")
        .getToolDefinitionsToOffer();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("memberType", "indirect");

    GrouperAiAgentToolCompaction fromCompact = new GrouperAiAgentToolCompaction(fullDefinitions, offeredCompact);
    assertSame(offeredCompact, fromCompact.getToolDefinitionsToOffer());
    assertTrue(fromCompact.isToolDescribe(GrouperAiAgentToolCompaction.TOOL_DESCRIBE));
    assertFalse(fromCompact.describe(describeArguments("group_find")).path("isError").asBoolean());
    String error = fromCompact.unknownArgumentsError("group_get_members", arguments);
    assertTrue(error, error.contains("Call tool_describe"));
    // offered in full then
    error = fromCompact.unknownArgumentsError("group_find", arguments);
    assertTrue(error, error.endsWith("Its parameters are in its definition in the tool list."));

    // offered in full: no tool_describe and no check, as before
    GrouperAiAgentToolCompaction fromFull = new GrouperAiAgentToolCompaction(fullDefinitions, fullDefinitions);
    assertFalse(fromFull.isToolDescribe(GrouperAiAgentToolCompaction.TOOL_DESCRIBE));
    assertNull(fromFull.unknownArgumentsError("group_get_members", arguments));

    assertNull(new GrouperAiAgentToolCompaction(fullDefinitions, null).getToolDefinitionsToOffer());
  }

  /**
   * tool_describe returns the full definitions of tools offered, each once, and reports a tool
   * which was not offered the same as a misspelled one
   */
  public void testDescribe() {

    ArrayNode fullDefinitions = fullDefinitions();
    GrouperAiAgentToolCompaction compaction = new GrouperAiAgentToolCompaction(fullDefinitions, true, null);

    ObjectNode result = compaction.describe(describeArguments("group_find", "notATool", "group_find", "sql_select"));
    assertFalse(text(result), result.path("isError").asBoolean());
    JsonNode resultNode = describeResult(result);

    assertEquals(1, resultNode.path("tools").size());
    assertEquals(find(fullDefinitions, "group_find"), resultNode.path("tools").get(0));
    assertEquals(2, resultNode.path("unknownTools").size());
    assertEquals("notATool", resultNode.path("unknownTools").get(0).asText());
    assertEquals("sql_select", resultNode.path("unknownTools").get(1).asText());

    // an unknown name is echoed back cut short
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < GrouperAiAgentToolCompaction.MAX_UNKNOWN_TOOL_NAME_LENGTH * 2; i++) {
      longName.append('x');
    }
    JsonNode longNode = describeResult(compaction.describe(describeArguments(longName.toString())));
    assertTrue(longNode.path("unknownTools").get(0).asText().length()
        <= GrouperAiAgentToolCompaction.MAX_UNKNOWN_TOOL_NAME_LENGTH);

    // no tools named is an error, and so is a blank string, rather than a failure
    ObjectNode noTools = compaction.describe(objectMapper.createObjectNode());
    assertTrue(text(noTools), noTools.path("isError").asBoolean());
    ObjectNode blankTools = objectMapper.createObjectNode();
    blankTools.put("tools", "  ");
    ObjectNode blankResult = compaction.describe(blankTools);
    assertTrue(text(blankResult), blankResult.path("isError").asBoolean());
    assertTrue(text(blankResult), text(blankResult).contains("tools is required"));
    assertTrue(compaction.describe(null).path("isError").asBoolean());

    // the names as a comma separated string, or as a JSON array inside a string, are read too
    assertEquals("[group_save, group_find]", GrouperAiAgentToolCompaction.parseToolNames(
        objectMapper.getNodeFactory().textNode("group_save, group_find")).toString());
    assertEquals("[group_save, group_find]", GrouperAiAgentToolCompaction.parseToolNames(
        objectMapper.getNodeFactory().textNode("[\"group_save\",\"group_find\"]")).toString());

    // more names than there could be tools, which would only be echoed back
    String[] tooManyNames = new String[GrouperAiAgentToolCompaction.MAX_TOOL_NAMES + 1];
    for (int i = 0; i < tooManyNames.length; i++) {
      tooManyNames[i] = "notATool" + i;
    }
    ObjectNode tooManyResult = compaction.describe(describeArguments(tooManyNames));
    assertTrue(text(tooManyResult), tooManyResult.path("isError").asBoolean());
    assertFalse(text(tooManyResult), text(tooManyResult).contains("notATool0"));
  }

  /**
   * a call with an argument the tool does not list is refused, pointing at tool_describe for a
   * tool offered compact and at the tool list for one offered in full.  nothing to refuse for a
   * tool which was not offered, one with no parameters, a JSON null, or fieldName on the
   * membership tools
   */
  public void testUnknownArguments() {

    GrouperAiAgentToolCompaction compaction = new GrouperAiAgentToolCompaction(fullDefinitions(), true, null);

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "a:b");
    arguments.put("memberType", "indirect");

    String error = compaction.unknownArgumentsError("group_get_members", arguments);
    assertEquals("Not run: group_get_members has no parameter named 'memberType'.  Call tool_describe with "
        + "[\"group_get_members\"] for its parameters, then call it again.", error);

    // offered in full, so its parameters are in the tool list
    error = new GrouperAiAgentToolCompaction(fullDefinitions(), true, "group_get_members")
        .unknownArgumentsError("group_get_members", arguments);
    assertEquals("Not run: group_get_members has no parameter named 'memberType'.  Its parameters are in "
        + "its definition in the tool list.", error);
    // not compact: not checked, as before
    assertNull(new GrouperAiAgentToolCompaction(fullDefinitions(), false, null)
        .unknownArgumentsError("group_get_members", arguments));

    // the legacy name of privilegeListName on the membership tools, but not on others
    ObjectNode fieldName = objectMapper.createObjectNode();
    fieldName.put("groupName", "a:b");
    fieldName.put("fieldName", "members");
    assertNull(compaction.unknownArgumentsError("group_get_members", fieldName));
    assertNotNull(compaction.unknownArgumentsError("group_find", fieldName));

    // a JSON null is read as not given
    ObjectNode nullArguments = objectMapper.createObjectNode();
    nullArguments.put("groupName", "a:b");
    nullArguments.putNull("memberType");
    assertNull(compaction.unknownArgumentsError("group_get_members", nullArguments));

    // a tool with no parameters ignores them, so a placeholder a model sends is not refused
    ObjectNode placeholder = objectMapper.createObjectNode();
    placeholder.put("random_string", "x");
    assertNull(compaction.unknownArgumentsError("entity_whoami", placeholder));

    // a tool which was not offered is left to the tool layer, which refuses it
    assertNull(compaction.unknownArgumentsError("notATool", arguments));
    assertNull(compaction.unknownArgumentsError("group_get_members", null));

    // many unknown arguments: only the first few are named, so the error is not as large as the call
    ObjectNode junk = objectMapper.createObjectNode();
    junk.put("groupName", "a:b");
    for (int i = 0; i < GrouperAiAgentToolCompaction.MAX_UNKNOWN_ARGUMENTS_LISTED + 5; i++) {
      junk.put("junk" + i, "x");
    }
    error = compaction.unknownArgumentsError("group_get_members", junk);
    assertTrue(error, error.contains("'junk0'"));
    assertFalse(error, error.contains("'junk" + GrouperAiAgentToolCompaction.MAX_UNKNOWN_ARGUMENTS_LISTED + "'"));
    assertTrue(error, error.contains("and 5 more"));
  }

  /**
   * the arguments let through though a schema does not list them, pinned so a change is made on
   * purpose: checked against what every tool reads when this was written.  a tool which comes to
   * read an argument its schema does not list has to be added, here and in UNLISTED_ARGUMENTS
   */
  public void testUnlistedArguments() {
    assertEquals("{entity_get=[fieldName], entity_get_groups=[fieldName], group_get_members=[fieldName], "
        + "group_has_member=[fieldName], memberships_get=[fieldName], recipe=[enabled]}",
        new TreeMap<String, Set<String>>(GrouperAiAgentToolCompaction.UNLISTED_ARGUMENTS).toString());
  }

}
