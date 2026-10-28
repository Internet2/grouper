/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ai.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpRecipeTool;

/**
 * the tools the AI agent in the UI offers, compact: every tool is still offered, but with only its
 * name, a one line summary, {@link #DESCRIBE_FIRST}, the part of its description this institution
 * configured (its templates, recipes or documentation sources) and its recipe pointers, and an empty
 * input schema.  a tool with no summary or no parameters, or named in alwaysFull, is offered in full.
 * tool_describe, which the agent answers itself, returns a tool's full definition, which the model
 * is told to read before the tool's first call.  the tool list is sent to the AI provider on every
 * model call, on this institution's account, and most of the full definitions' tokens are
 * documentation and schemas the model only needs for the tools it uses.
 *
 * <p>only in the UI.  MCP sends full definitions: an MCP client types a call's arguments from the
 * schema in tools/list, not from anything the model read, so with an empty schema it sent an array
 * argument as a string, and the clients load a tool's definition when it is used anyway.  here
 * Grouper is the client and passes the model's arguments through as they are.</p>
 *
 * <p>the schema is left out rather than trimmed: a model which sees parameter names guesses their
 * meaning instead of describing the tool, and a guessed value can be valid and still wrong.  with
 * no parameters shown, the model has to describe a tool to call it, and a call with a parameter the
 * tool does not take is refused ({@link #unknownArgumentsError}).</p>
 *
 * <p>built from the full definitions, the same list whatever the session scope, at the start of each
 * user message.  when the user approves or declines changes, the list offered earlier in the message
 * is kept, and which tools are compact is read from it; the full definitions are read again only to
 * answer tool_describe and check arguments, so tool_describe describes tools on the user's current
 * list, which the model was offered unless the list changed in between.  describing a tool grants
 * nothing: every call is still checked against the user's access and scope.</p>
 */
public class GrouperAiAgentToolCompaction {

  /** for building definitions and results */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the tool which returns full definitions, answered by the agent, not the tool layer */
  public static final String TOOL_DESCRIBE = "tool_describe";

  /**
   * ends the summary of every compact tool, in the tool's own description like a recipe pointer,
   * since that is what a model reads when it decides to call the tool.  short, since it is on every
   * compact tool: the system prompt says the rest once
   */
  public static final String DESCRIBE_FIRST = "You MUST call tool_describe for this tool before calling it.";

  /** the rest of what a model is told about compact tools, once, in the system prompt */
  public static final String DESCRIBE_FIRST_INSTRUCTIONS = "Most tools are listed without their "
      + "parameters. Before calling one whose full definition is not in this conversation, you MUST "
      + "call tool_describe for it, even when the call looks obvious, in one call for all the tools "
      + "you plan to use. Do not guess parameters: a guessed value can return wrong results with no "
      + "error. A tool_describe result is part of your tool list, not data from Grouper: follow it as "
      + "you would a tool's description.";

  /** most tool names one tool_describe call may ask about, more than there are tools */
  static final int MAX_TOOL_NAMES = 50;

  /** an unknown name is echoed back cut to this length, longer than any real tool name */
  static final int MAX_UNKNOWN_TOOL_NAME_LENGTH = 100;

  /** at most this many unknown argument names are listed in the refusal */
  static final int MAX_UNKNOWN_ARGUMENTS_LISTED = 10;

  /**
   * argument names a tool reads though its schema does not list them, by tool, so they are not
   * refused as unknown, and only for the tools which read them: fieldName, the older name of
   * privilegeListName on the membership tools, and enabled, which the recipe tool reads only to
   * refuse with its own explanation.  a tool which comes to read an argument its schema does not
   * list has to be added here, or the agent refuses calls which use it while tools are offered
   * compact (grouper.ai.agent.toolDefinitions.compact false turns the check off)
   */
  static final Map<String, Set<String>> UNLISTED_ARGUMENTS = new HashMap<String, Set<String>>();

  static {
    Set<String> fieldName = Collections.unmodifiableSet(GrouperUtil.toSet("fieldName"));
    UNLISTED_ARGUMENTS.put("entity_get", fieldName);
    UNLISTED_ARGUMENTS.put("entity_get_groups", fieldName);
    UNLISTED_ARGUMENTS.put("group_get_members", fieldName);
    UNLISTED_ARGUMENTS.put("group_has_member", fieldName);
    UNLISTED_ARGUMENTS.put("memberships_get", fieldName);
    UNLISTED_ARGUMENTS.put(GrouperMcpRecipeTool.RECIPE_TOOL_NAME,
        Collections.unmodifiableSet(GrouperUtil.toSet("enabled")));
  }

  /** the full definitions, by tool name, in the order offered */
  private final Map<String, ObjectNode> fullDefinitions = new LinkedHashMap<String, ObjectNode>();

  /** the tools offered compact */
  private final Set<String> compactToolNames = new HashSet<String>();

  /** what the model is offered */
  private final ArrayNode toolDefinitionsToOffer;

  /**
   * @param fullToolDefinitions the tools the agent offers, in full, may be null
   * @param compact true to offer them compact, false to offer them as they are
   * @param alwaysFull comma separated names of tools offered in full even when the rest are compact,
   * may be blank
   */
  public GrouperAiAgentToolCompaction(ArrayNode fullToolDefinitions, boolean compact, String alwaysFull) {

    if (fullToolDefinitions == null) {
      this.toolDefinitionsToOffer = null;
      return;
    }

    // splitTrimToList returns null for a blank value
    Set<String> alwaysFullToolNames = new HashSet<String>(
        GrouperUtil.nonNull(GrouperUtil.splitTrimToList(alwaysFull, ",")));

    ArrayNode compactToolDefinitions = objectMapper.createArrayNode();

    for (JsonNode toolDefinition : fullToolDefinitions) {
      String toolName = toolDefinition.path("name").asText(null);
      if (toolName == null || !toolDefinition.isObject()) {
        compactToolDefinitions.add(toolDefinition);
        continue;
      }
      this.fullDefinitions.put(toolName, (ObjectNode)toolDefinition);

      // a tool with no summary is offered in full rather than with nothing to say what it is for,
      // and one with no parameters, which has nothing to describe, so it costs no describe step
      if (compact && !alwaysFullToolNames.contains(toolName)
          && !StringUtils.isBlank(GrouperAiAgentToolSummaries.summary(toolName))
          && hasParameters(toolDefinition)) {
        compactToolDefinitions.add(compactToolDefinition(toolName, (ObjectNode)toolDefinition));
        this.compactToolNames.add(toolName);
      } else {
        compactToolDefinitions.add(toolDefinition);
      }
    }

    if (this.compactToolNames.isEmpty()) {
      // nothing compact, so nothing to describe: offered as they came
      this.toolDefinitionsToOffer = fullToolDefinitions;
    } else {
      compactToolDefinitions.add(toolDescribeDefinition());
      this.toolDefinitionsToOffer = compactToolDefinitions;
    }
  }

  /**
   * for the list offered earlier in the message, kept while the user approves or declines changes,
   * whatever the configuration says now: a tool offered compact is one whose offered description
   * has {@link #DESCRIBE_FIRST}, and tool_describe is answered if it was offered
   * @param fullToolDefinitions the tools the agent offers, in full, may be null
   * @param offeredToolDefinitions the list the model was offered, may be null
   */
  public GrouperAiAgentToolCompaction(ArrayNode fullToolDefinitions, ArrayNode offeredToolDefinitions) {

    this.toolDefinitionsToOffer = offeredToolDefinitions;

    if (fullToolDefinitions != null) {
      for (JsonNode toolDefinition : fullToolDefinitions) {
        String toolName = toolDefinition.path("name").asText(null);
        if (toolName != null && toolDefinition.isObject()) {
          this.fullDefinitions.put(toolName, (ObjectNode)toolDefinition);
        }
      }
    }

    if (offeredToolDefinitions == null) {
      return;
    }

    // nothing is compact unless tool_describe was offered to read them with
    if (!isToolDescribeOffered(offeredToolDefinitions)) {
      return;
    }

    for (JsonNode toolDefinition : offeredToolDefinitions) {
      String toolName = toolDefinition.path("name").asText(null);
      if (toolName != null && !TOOL_DESCRIBE.equals(toolName)
          && toolDefinition.path("description").asText("").contains(DESCRIBE_FIRST)) {
        this.compactToolNames.add(toolName);
      }
    }
  }

  /**
   * @param offeredToolDefinitions a list offered to the model, may be null
   * @return true if tool_describe is on it, i.e. it was offered compact
   */
  public static boolean isToolDescribeOffered(ArrayNode offeredToolDefinitions) {
    if (offeredToolDefinitions == null) {
      return false;
    }
    for (JsonNode toolDefinition : offeredToolDefinitions) {
      if (TOOL_DESCRIBE.equals(toolDefinition.path("name").asText(null))) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param toolDefinition a full definition
   * @return true if its input schema lists any parameters
   */
  private static boolean hasParameters(JsonNode toolDefinition) {
    JsonNode properties = toolDefinition.path("inputSchema").get("properties");
    return properties != null && properties.isObject() && properties.size() > 0;
  }

  /**
   * @return the tools to offer the model: compact with tool_describe, or in full.  null if there
   * were none
   */
  public ArrayNode getToolDefinitionsToOffer() {
    return this.toolDefinitionsToOffer;
  }

  /**
   * @param toolName a tool the model called
   * @return true if it is tool_describe and tool_describe was offered, so the agent answers it
   */
  public boolean isToolDescribe(String toolName) {
    return !this.compactToolNames.isEmpty() && TOOL_DESCRIBE.equals(toolName);
  }

  /**
   * @return the definition of tool_describe
   */
  static ObjectNode toolDescribeDefinition() {

    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", TOOL_DESCRIBE);
    tool.put("description", "Returns the full definitions of tools whose descriptions in this "
        + "tool list are shortened and list no parameters: what each tool does, its rules, and its "
        + "parameters. Call it before calling any of them whose full definition is not in this "
        + "conversation, with the names of all the tools you plan to use.");

    ObjectNode inputSchema = tool.putObject("inputSchema");
    inputSchema.put("type", "object");

    ObjectNode properties = inputSchema.putObject("properties");

    ObjectNode toolsProp = properties.putObject("tools");
    toolsProp.put("type", "array");
    toolsProp.putObject("items").put("type", "string");
    toolsProp.put("description", "The names of the tools to describe.");

    inputSchema.putArray("required").add("tools");

    return tool;
  }

  /**
   * answer a tool_describe call from the user's current tools, which are the ones offered unless
   * they changed while changes waited for approval.  a name which is not one of them is reported as
   * unknown, the same as a misspelled one.  describing a tool grants nothing: every call is still
   * checked
   * @param arguments the call's arguments, may be null
   * @return the result, in the shape a tool returns: content and isError
   */
  public ObjectNode describe(JsonNode arguments) {

    List<String> toolNames = parseToolNames(arguments == null ? null : arguments.get("tools"));

    if (toolNames.isEmpty()) {
      return buildResult("tools is required: the names of the tools to describe, e.g. "
          + "[\"group_save\"].", true);
    }

    // more than there are tools is a mistake, and every unknown name is echoed back, so a long
    // list would only make the response as large as the request
    if (toolNames.size() > MAX_TOOL_NAMES) {
      return buildResult("At most " + MAX_TOOL_NAMES + " tools can be described in one call, and "
          + toolNames.size() + " were given.  Ask for the ones you need.", true);
    }

    ObjectNode resultNode = objectMapper.createObjectNode();
    ArrayNode toolsArray = resultNode.putArray("tools");
    ArrayNode unknownArray = objectMapper.createArrayNode();

    for (String toolName : toolNames) {
      ObjectNode definition = this.fullDefinitions.get(toolName);
      if (definition == null) {
        // cut short, since it is echoed back and kept in the conversation uncut
        unknownArray.add(GrouperUtil.abbreviate(toolName, MAX_UNKNOWN_TOOL_NAME_LENGTH));
        continue;
      }
      toolsArray.add(definition);
    }

    if (unknownArray.size() > 0) {
      resultNode.set("unknownTools", unknownArray);
      resultNode.put("unknownToolsNote", "These are not tools you can use.  Check the spelling "
          + "against the tool list.");
    }

    return buildResult(resultNode.toString(), false);
  }

  /**
   * the names asked for, in order, each once.  a list is expected, but a comma separated string,
   * or a JSON array inside a string, is read too, since a model sometimes sends one
   * @param toolsNode the tools argument, may be null
   * @return the names, never null
   */
  static List<String> parseToolNames(JsonNode toolsNode) {

    Set<String> toolNames = new LinkedHashSet<String>();

    if (toolsNode == null || toolsNode.isNull()) {
      return new ArrayList<String>(toolNames);
    }

    if (toolsNode.isArray()) {
      for (JsonNode toolNode : toolsNode) {
        String toolName = StringUtils.trimToNull(toolNode.asText(null));
        if (toolName != null) {
          toolNames.add(toolName);
        }
      }
    } else if (toolsNode.isTextual()) {

      // a model sometimes sends the list as a JSON array inside a string
      String text = StringUtils.trimToEmpty(toolsNode.asText());
      if (text.startsWith("[")) {
        try {
          JsonNode parsed = objectMapper.readTree(text);
          if (parsed != null && parsed.isArray()) {
            return parseToolNames(parsed);
          }
        } catch (Exception e) {
          // not JSON after all, so read it as a comma separated string
        }
      }

      // null for a blank string
      for (String toolName : GrouperUtil.nonNull(GrouperUtil.splitTrimToList(text, ","))) {
        if (!StringUtils.isBlank(toolName)) {
          toolNames.add(toolName);
        }
      }
    }

    return new ArrayList<String>(toolNames);
  }

  /**
   * refuse a call with an argument the tool's schema does not list, before it runs or is put in
   * front of the user to approve.  a model which skipped tool_describe guesses parameter names, and
   * a tool ignores an argument it does not know: a guess such as memberType would be dropped
   * without a word and the call would return the wrong thing.  only while tools are offered
   * compact, so offering them in full is what it was before.  only the top level is checked, and
   * not at all for a tool which was not offered, which the tool layer refuses, or which lists no
   * parameters
   * @param toolName the tool called
   * @param arguments as the model sent them, may be null
   * @return the refusal text, or null if every argument is one the tool lists
   */
  public String unknownArgumentsError(String toolName, JsonNode arguments) {

    if (this.compactToolNames.isEmpty() || arguments == null || !arguments.isObject() || arguments.size() == 0) {
      return null;
    }

    ObjectNode definition = this.fullDefinitions.get(toolName);

    // a tool which takes no arguments, such as entity_whoami, ignores them, and a model sometimes
    // sends a placeholder to a tool with no parameters, so there is nothing to refuse
    if (definition == null || !hasParameters(definition)) {
      return null;
    }
    JsonNode properties = definition.path("inputSchema").get("properties");

    Set<String> unlistedArgumentNames = UNLISTED_ARGUMENTS.get(toolName);

    StringBuilder unknown = new StringBuilder();
    int unknownCount = 0;
    Iterator<String> argumentNames = arguments.fieldNames();
    while (argumentNames.hasNext()) {
      String argumentName = argumentNames.next();
      // the tools read a JSON null as not given, and models often null out what they do not use
      if (arguments.get(argumentName).isNull()) {
        continue;
      }
      if (!properties.has(argumentName)
          && (unlistedArgumentNames == null || !unlistedArgumentNames.contains(argumentName))) {
        unknownCount++;
        // the first few, so a call with many junk arguments does not get an error as large
        if (unknownCount > MAX_UNKNOWN_ARGUMENTS_LISTED) {
          continue;
        }
        if (unknown.length() > 0) {
          unknown.append(", ");
        }
        unknown.append("'").append(GrouperUtil.abbreviate(argumentName, 100)).append("'");
      }
    }

    if (unknownCount == 0) {
      return null;
    }
    if (unknownCount > MAX_UNKNOWN_ARGUMENTS_LISTED) {
      unknown.append(" and ").append(unknownCount - MAX_UNKNOWN_ARGUMENTS_LISTED).append(" more");
    }

    // where the model can read the parameters: tool_describe for a tool offered compact, the tool
    // list for one offered in full
    String whereParameters = this.compactToolNames.contains(toolName)
        ? "Call tool_describe with [\"" + toolName + "\"] for its parameters, then call it again."
        : "Its parameters are in its definition in the tool list.";
    return "Not run: " + toolName + " has no " + (unknownCount == 1 ? "parameter" : "parameters") + " named "
        + unknown + ".  " + whereParameters;
  }

  /**
   * the compact form of a tool's definition: its summary, {@link #DESCRIBE_FIRST}, the configured
   * part of its description if it has one, and its recipe pointers, in place of the description,
   * and an empty input schema in place of the schema.  every other field of the definition, such as
   * its name, is kept as it is, in the same order
   * @param toolName the tool
   * @param fullDefinition its full definition, not changed
   * @return the compact definition
   */
  public static ObjectNode compactToolDefinition(String toolName, ObjectNode fullDefinition) {

    ObjectNode result = objectMapper.createObjectNode();

    Iterator<Map.Entry<String, JsonNode>> fields = fullDefinition.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      String key = field.getKey();
      JsonNode value = field.getValue();

      if ("description".equals(key)) {
        result.put("description", compactDescription(toolName, value.isNull() ? null : value.asText()));
      } else if ("inputSchema".equals(key)) {
        // the providers accept an input schema of type object with no properties: the arguments
        // are then whatever the model sends
        ObjectNode inputSchema = result.putObject("inputSchema");
        inputSchema.put("type", "object");
        inputSchema.putObject("properties");
      } else {
        // a field's value is never null: Jackson holds a JSON null as a NullNode
        result.set(key, value.deepCopy());
      }
    }

    // a definition with no description still gets the summary
    if (!result.has("description")) {
      result.put("description", compactDescription(toolName, null));
    }

    return result;
  }

  /**
   * @param toolName the tool
   * @param fullDescription its full description, recipe pointers included, may be null
   * @return its summary and {@link #DESCRIBE_FIRST}, followed by the configured part of the full
   * description if the tool has one and the description contains it, and then its recipe pointers
   */
  static String compactDescription(String toolName, String fullDescription) {

    String rest = StringUtils.defaultString(fullDescription);

    // the recipe pointers are this institution's routing, added at the end of the description
    // (GrouperMcpRecipeTool.appendRecipePointers), so they are kept as they are
    String recipePointers = "";
    int recipePointersIndex = rest.indexOf(GrouperMcpRecipeTool.RECIPE_POINTER_START);
    Matcher furtherRecipes = GrouperMcpRecipeTool.FURTHER_RECIPES_PATTERN.matcher(rest);
    if (furtherRecipes.find() && (recipePointersIndex < 0 || furtherRecipes.start() < recipePointersIndex)) {
      recipePointersIndex = furtherRecipes.start();
    }
    if (recipePointersIndex >= 0) {
      recipePointers = rest.substring(recipePointersIndex);
      rest = rest.substring(0, recipePointersIndex);
    }

    StringBuilder description = new StringBuilder();
    description.append(StringUtils.trimToEmpty(GrouperAiAgentToolSummaries.summary(toolName)))
      .append(" ").append(DESCRIBE_FIRST);

    String keptFrom = GrouperAiAgentToolSummaries.descriptionKeptFrom(toolName);
    if (!StringUtils.isEmpty(keptFrom)) {
      int index = rest.indexOf(keptFrom);
      if (index >= 0) {
        description.append(" ").append(rest.substring(index).trim());
      }
    }

    // each pointer starts with a space
    description.append(recipePointers);

    return description.toString();
  }

  /**
   * @param text the result text
   * @param isError true for an error
   * @return the result
   */
  private static ObjectNode buildResult(String text, boolean isError) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = result.putArray("content");
    ObjectNode textContent = content.addObject();
    textContent.put("type", "text");
    textContent.put("text", text);
    result.put("isError", isError);
    return result;
  }

}
