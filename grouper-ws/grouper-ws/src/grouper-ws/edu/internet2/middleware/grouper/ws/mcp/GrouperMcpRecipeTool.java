/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.mcp.GrouperMcpRecipe;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolNames;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * MCP tool handler for recipes: institution-authored guidance about how a task should be done
 * here.  The client is given each recipe's name and summary in this tool's description, and
 * fetches a body only when it decides a recipe applies.
 *
 * <p>Note on how a client actually finds these.  A recipe listed only here has to win tool
 * selection against whichever tool directly matches the user's request, and it usually will not:
 * a request to save a group matches a tool called group_save long before it matches one called
 * recipe.  That is why a recipe which names tools in its toolNames also has a pointer appended
 * to those tools' own descriptions, which is where a client is demonstrably reading at the
 * moment it chooses.  This tool is where the client goes once pointed, and a browsing path for
 * the cases where it is already looking.</p>
 *
 * <p>Actions: list, get, and update.  Two kinds of caller may update.  A recipe's own editors,
 * the members of its groupNameCanEdit, may change its name, summary, and body, and only while
 * grouper.mcp.recipe.allowEditInMcp is on, which it is not by default; they are refused the
 * fields which decide who the recipe reaches and what it attaches to.  Members of
 * grouper.mcp.recipe.groupNameCanAdminInMcp may change any other field of any enabled recipe, and
 * may read any enabled recipe, since being able to rewrite something one cannot read is worse
 * than either consistent answer.  Creating and deleting recipes, and turning one on or off, stays
 * in the UI for both.</p>
 *
 * <p>Nothing here reaches a disabled recipe.  Not list, not get, not update, not the tool
 * description, and not for an administrator either: being one widens which recipes reach this
 * client past the audience their own groups put them in, and stops there.  Everything here which
 * names or returns a recipe is read by a client, and a client has no notion of administration, so
 * a recipe it can see is one it will fetch and follow; a recipe somebody turned off pending a
 * rewrite would go on being applied to real requests.  Nor is enabled a field update can write:
 * whether guidance steers every later request is a decision for a person on the recipes screen,
 * which is where a disabled recipe is seen, rewritten, and turned back on.  When every recipe is
 * disabled this tool is not advertised at all, the same as when none is configured.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpRecipeTool {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpRecipeTool.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * how many recipes are listed with their summaries in the tool description before it becomes
   * names only.  every client pays for this text on every connection, so it cannot grow without
   * limit, but a handful of summaries is what lets a client recognise a relevant recipe at all
   */
  private static final int DEFAULT_MAX_SUMMARIES_IN_DESCRIPTION = 10;

  /** config key of the summary cap */
  private static final String CONFIG_MAX_SUMMARIES = "grouper.mcp.recipe.maxSummariesInDescription";

  /**
   * how many recipes may add a pointer to one tool's description.  this is the surface which
   * grows in practice, because routing rules concentrate on the few tools which write, and past
   * two or three "there is a recipe for this" lines on one tool none of them is read carefully
   */
  private static final int DEFAULT_MAX_POINTERS_PER_TOOL = 3;

  /**
   * config key of the per tool cap.  read in two places on purpose: it caps the pointers added
   * to a tool's description and the recipes returned when that tool fails, so a deployer sets
   * how many recipes one tool is worth hearing about once rather than twice
   */
  private static final String CONFIG_MAX_POINTERS_PER_TOOL = "grouper.mcp.recipe.maxPointersPerTool";

  /**
   * how much of a recipe body is returned on a failed tool call.  a model which retries would
   * otherwise pull the whole recipe back on every attempt, and bodies have no length limit
   */
  private static final int DEFAULT_MAX_BODY_CHARS_ON_ERROR = 4000;

  /** config key of how much of a body a failed call carries */
  private static final String CONFIG_MAX_BODY_CHARS_ON_ERROR = "grouper.mcp.recipe.maxBodyCharsOnError";

  /**
   * the fields which decide who a recipe reaches, what it attaches to, and where it ranks.  a
   * recipe administrator may change these over MCP; a delegated editor is refused them.  listed
   * once so the refusal and the write cannot fall out of step.
   *
   * <p>enabled is deliberately not among them, and is not reachable from MCP at all.  Turning a
   * recipe on or off decides whether guidance steers every later request, which is a decision
   * for a person on the recipes screen rather than for a client acting on one.  Leaving it out
   * is also what keeps the rule without an exception: nothing here reaches a disabled recipe, so
   * there is no way to turn one off from here and then find it unreachable to turn back on.</p>
   */
  private static final String[] CONTROL_FIELD_NAMES = new String[] {
      "groupNameCanUse", "groupNameCanEdit", "toolNames", "priority" };

  /** the name this tool is advertised and dispatched under */
  public static final String RECIPE_TOOL_NAME = "recipe";

  /**
   * whether a tool of this name is on the finished tool list
   * @param toolsArray the tools being advertised
   * @param toolName the name to look for
   * @return true if it is there
   */
  private static boolean containsTool(ArrayNode toolsArray, String toolName) {

    for (JsonNode tool : toolsArray) {
      if (tool.has("name") && Strings.CS.equals(toolName, tool.get("name").asText())) {
        return true;
      }
    }

    return false;
  }

  /**
   * return the MCP tool definition for recipe, or null when this user can see no recipes, in
   * which case the tool is not advertised at all
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the tool definition, or null
   */
  public static ObjectNode toolDefinition(GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) {

    boolean canAdmin = hasReadwriteAccess && GrouperMcpRecipe.canAdminInMcp(authUser.getSubject());

    // an administrator gets the tool whenever there is any recipe at all, since they can read
    // and rewrite all of them.  without this, one who happens not to be in any recipe's
    // groupNameCanUse is offered no tool and has no way to reach the recipes they administer.
    // disabled ones stay out even for them: this description is read by the client, which has no
    // notion of administration and will act on any recipe named here
    Map<String, GrouperMcpRecipe> recipes = canAdmin
        ? GrouperMcpRecipe.retrieveAllRecipes()
        : GrouperMcpRecipe.retrieveRecipesCanUse(authUser.getSubject());

    // a deployment whose every recipe is turned off is a deployment with no recipes, as far as
    // anything here can tell.  the tool goes away with the last enabled one and comes back when
    // somebody re-enables one on the recipes screen
    if (recipes.isEmpty()) {
      return null;
    }

    // a delegated editor only counts when the deployer has turned that path on, otherwise the
    // action would be advertised to somebody who can only be refused by it
    boolean canEditAny = canAdmin
        || (hasReadwriteAccess && GrouperMcpRecipe.isAllowEditInMcp()
            && !GrouperMcpRecipe.retrieveRecipesCanEdit(authUser.getSubject()).isEmpty());

    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", RECIPE_TOOL_NAME);

    StringBuilder description = new StringBuilder();
    description.append("Institution-specific guidance on how tasks should be done in this Grouper. ");
    description.append("Consult a recipe before acting when one covers what you are about to do: it "
        + "describes the route this institution wants used, which is not always the tool which "
        + "looks like the closest match. ");
    description.append("Use action 'get' with the recipe name to read one in full. ");

    // what update offers differs by caller, and the schema below differs with it, so the prose
    // has to as well.  telling an administrator they may change only the wording would have them
    // not attempt a field the schema does offer them
    if (canAdmin) {
      description.append("Use action 'update' to change any recipe listed here, including who it "
          + "is for and which tools it applies to. Whether a recipe is turned on is not editable "
          + "here, and a recipe which is turned off is not listed here or readable here at all; "
          + "that is done on the recipes screen in the Grouper UI. ");
    } else if (canEditAny) {
      description.append("Use action 'update' to change the name, summary, or text of a recipe you "
          + "are allowed to edit. ");
    }

    int maxSummaries = GrouperConfig.retrieveConfig().propertyValueInt(
        CONFIG_MAX_SUMMARIES, DEFAULT_MAX_SUMMARIES_IN_DESCRIPTION);

    description.append("Available recipes: ");

    boolean first = true;
    int count = 0;

    // in the deployer's order, so that when there are more recipes than fit it is the ones they
    // ranked highest which get described rather than whichever sorted first by name
    for (GrouperMcpRecipe recipe : GrouperMcpRecipe.sortByPriority(recipes.values())) {

      if (!first) {
        description.append("; ");
      }

      description.append(recipe.getName());

      // past the cap only the names are listed.  a name with no summary is still worth listing,
      // since the client can then ask for it by name, whereas an unlisted recipe cannot be found
      if (count < maxSummaries) {
        description.append(" - ").append(recipe.getSummary());
      }

      first = false;
      count++;
    }

    description.append(".");

    tool.put("description", description.toString());

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");

    ObjectNode properties = objectMapper.createObjectNode();

    ObjectNode actionProp = objectMapper.createObjectNode();
    actionProp.put("type", "string");
    ArrayNode actionEnum = objectMapper.createArrayNode();
    actionEnum.add("list");
    actionEnum.add("get");
    if (canEditAny) {
      actionEnum.add("update");
    }
    actionProp.set("enum", actionEnum);
    actionProp.put("description", "'list' returns every recipe you can see with its summary. "
        + "'get' returns one recipe in full, by name."
        + (canEditAny ? " 'update' changes a recipe you are allowed to edit." : ""));
    properties.set("action", actionProp);

    ObjectNode nameProp = objectMapper.createObjectNode();
    nameProp.put("type", "string");
    nameProp.put("description", "The recipe name. Required for 'get' and 'update'.");
    properties.set("name", nameProp);

    if (canEditAny) {

      ObjectNode newNameProp = objectMapper.createObjectNode();
      newNameProp.put("type", "string");
      newNameProp.put("description", "For 'update': the new name, if it is being renamed. "
          + "Leave it out to keep the current name.");
      properties.set("newName", newNameProp);

      ObjectNode summaryProp = objectMapper.createObjectNode();
      summaryProp.put("type", "string");
      summaryProp.put("description", "For 'update': the new summary. Leave it out to keep the current one.");
      properties.set("summary", summaryProp);

      ObjectNode bodyProp = objectMapper.createObjectNode();
      bodyProp.put("type", "string");
      bodyProp.put("description", "For 'update': the new recipe text. Leave it out to keep the current one.");
      properties.set("body", bodyProp);

    }

    // the fields which decide who a recipe reaches and what it attaches to are only offered to a
    // recipe administrator.  a delegated editor which sends one anyway is refused, not ignored
    if (canAdmin) {

      ObjectNode groupCanUseProp = objectMapper.createObjectNode();
      groupCanUseProp.put("type", "string");
      groupCanUseProp.put("description", "For 'update': name of the group whose members can see "
          + "and read this recipe. Blank means nobody. Leave it out to keep the current one.");
      properties.set("groupNameCanUse", groupCanUseProp);

      ObjectNode groupCanEditProp = objectMapper.createObjectNode();
      groupCanEditProp.put("type", "string");
      groupCanEditProp.put("description", "For 'update': name of the group whose members can edit "
          + "this recipe's content. Blank means nobody. Leave it out to keep the current one.");
      properties.set("groupNameCanEdit", groupCanEditProp);

      ObjectNode toolNamesProp = objectMapper.createObjectNode();
      toolNamesProp.put("type", "string");
      toolNamesProp.put("description", "For 'update': comma separated MCP tool names this recipe "
          + "is about. Each one gets a pointer to this recipe added to its own description, for "
          + "everybody who can see the recipe. Leave it out to keep the current ones.");
      properties.set("toolNames", toolNamesProp);

      // no 'enabled' property.  a recipe is turned on and off on the recipes screen in the
      // Grouper UI, which is also the only place a disabled one can be seen or edited
      ObjectNode priorityProp = objectMapper.createObjectNode();
      priorityProp.put("type", "string");
      priorityProp.put("description", "For 'update': a whole number deciding which recipes are "
          + "described first when there are more than fit, and the order of pointers on a tool "
          + "several recipes are about. Lower is more important. Leave it out to keep the "
          + "current setting.");
      properties.set("priority", priorityProp);
    }

    inputSchema.set("properties", properties);

    ArrayNode required = objectMapper.createArrayNode();
    required.add("action");
    inputSchema.set("required", required);

    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * append a pointer to each visible recipe which names this tool, onto that tool's own
   * description.  this is the delivery path which does not depend on the client deciding to go
   * looking: it puts the rule in front of the client at the moment it is choosing this tool.
   * @param toolDefinition the tool definition to annotate, modified in place
   * @param authUser the authenticated user
   */
  public static void appendRecipePointers(ArrayNode toolsArray, GrouperMcpAuthUser authUser) {

    if (toolsArray == null || toolsArray.size() == 0 || authUser == null) {
      return;
    }

    // every pointer tells the client to call the recipe tool, so there is no point adding any
    // when that tool is not on the list.  it can be missing because the deployer denied it with
    // grouper.mcp.tools.allow/deny, or because this user has no recipes at all.  the finished
    // list is the one thing which knows either way, which is why this is asked here rather than
    // rechecking the configuration
    if (!containsTool(toolsArray, RECIPE_TOOL_NAME)) {
      return;
    }

    // the recipes are read once for the whole tool list.  doing it per tool would put a
    // membership query on every one of the couple of dozen tools, on every connection
    Map<String, GrouperMcpRecipe> recipes = GrouperMcpRecipe.retrieveRecipesCanUse(authUser.getSubject());

    if (recipes.isEmpty()) {
      return;
    }

    Map<String, List<GrouperMcpRecipe>> recipesByToolName = new LinkedHashMap<String, List<GrouperMcpRecipe>>();

    for (GrouperMcpRecipe recipe : recipes.values()) {
      for (String toolName : recipe.getToolNames()) {

        // the configuration screen rejects an unknown tool name, so reaching here means the tool
        // went away after the recipe was saved, most likely in an upgrade.  logged once rather
        // than on every connect, and the recipes screen shows the same thing to whoever can fix it
        if (!GrouperMcpToolNames.isToolName(toolName)) {
          GrouperMcpRecipe.logProblemOnce("MCP recipe '" + recipe.getName() + "' points at tool '"
              + toolName + "', which does not exist, so nothing is added to that tool.");
          continue;
        }

        List<GrouperMcpRecipe> recipesForTool = recipesByToolName.get(toolName);
        if (recipesForTool == null) {
          recipesForTool = new ArrayList<GrouperMcpRecipe>();
          recipesByToolName.put(toolName, recipesForTool);
        }
        recipesForTool.add(recipe);
      }
    }

    if (recipesByToolName.isEmpty()) {
      return;
    }

    int maxPointersPerTool = GrouperConfig.retrieveConfig().propertyValueInt(
        CONFIG_MAX_POINTERS_PER_TOOL, DEFAULT_MAX_POINTERS_PER_TOOL);

    for (JsonNode toolNode : toolsArray) {

      if (!(toolNode instanceof ObjectNode)) {
        continue;
      }

      ObjectNode toolDefinition = (ObjectNode) toolNode;

      JsonNode nameNode = toolDefinition.get("name");
      if (nameNode == null || nameNode.isNull()) {
        continue;
      }

      List<GrouperMcpRecipe> recipesForTool = recipesByToolName.get(nameNode.asText());

      if (recipesForTool == null || recipesForTool.isEmpty()) {
        continue;
      }

      JsonNode descriptionNode = toolDefinition.get("description");
      StringBuilder description = new StringBuilder(
          descriptionNode == null || descriptionNode.isNull() ? "" : descriptionNode.asText());

      int pointerCount = 0;

      // in priority order, and capped: the deployer decides which rule a model reads first on a
      // tool several recipes are about, and one tool's description does not grow without limit
      for (GrouperMcpRecipe recipe : GrouperMcpRecipe.sortByPriority(recipesForTool)) {

        if (pointerCount >= maxPointersPerTool) {
          description.append(" There are ").append(recipesForTool.size() - pointerCount)
              .append(" further recipes about this tool. List them with the recipe tool, action "
                  + "'list', and read any which apply before using this tool.");
          break;
        }

        description.append(" IMPORTANT: this institution has a recipe covering this tool - ")
            .append(recipe.getSummary())
            .append(" Read it with the recipe tool, action 'get', name '")
            .append(recipe.getName())
            .append("', before using this tool for that case.");

        pointerCount++;
      }

      toolDefinition.put("description", description.toString());
    }
  }

  /**
   * append the recipes for a tool onto that tool's error result, body included.
   *
   * <p>This is the delivery path which does not depend on the model choosing to read anything:
   * it is looking at the response to its own failed call.  The tool description pointer is what
   * tries to stop the wrong call being made; this is what routes the model once one has been.
   * Both are wanted, they do different jobs.</p>
   *
   * <p>It runs on every kind of tool failure rather than on privilege denials alone.  Privilege
   * errors cannot be told apart reliably from the result, and a recipe attached to a tool is
   * worth reading on any failure of it.  A tool with no matching recipe is left untouched.</p>
   *
   * <p>The body is capped.  A model which retries a failing call would otherwise pull the whole
   * recipe back on every attempt, and recipe bodies have no length limit.</p>
   *
   * @param result the tool result, modified in place, only when it carries recipes to add
   * @param toolName the tool which failed
   * @param authUser the authenticated user
   */
  public static void appendRecipesToError(ObjectNode result, String toolName,
      GrouperMcpAuthUser authUser) {

    if (result == null || authUser == null || StringUtils.isBlank(toolName)) {
      return;
    }

    // everything from here is wrapped, not just the lookup.  the call has already failed and
    // this is an extra which is trying to help, so nothing it does may turn a tool error the
    // model could act on into a server error it cannot
    try {

      appendRecipesToErrorHelper(result, toolName, authUser);

    } catch (Exception e) {
      LOG.error("Error adding recipes to the failed call of tool '" + toolName + "'", e);
    }
  }

  /**
   * build and append the recipe text for a failed call
   * @param result the tool result, modified in place
   * @param toolName the tool which failed
   * @param authUser the authenticated user
   */
  private static void appendRecipesToErrorHelper(ObjectNode result, String toolName,
      GrouperMcpAuthUser authUser) {

    List<GrouperMcpRecipe> recipes =
        GrouperMcpRecipe.retrieveRecipesForTool(toolName, authUser.getSubject());

    if (recipes.isEmpty()) {
      return;
    }

    // a deployer setting this to -1 meaning "no limit", which is the convention elsewhere in
    // this properties file, would otherwise index past the end of the body below
    int maxBodyChars = GrouperConfig.retrieveConfig().propertyValueInt(
        CONFIG_MAX_BODY_CHARS_ON_ERROR, DEFAULT_MAX_BODY_CHARS_ON_ERROR);

    if (maxBodyChars <= 0) {
      maxBodyChars = Integer.MAX_VALUE;
    }

    // capped and ordered the same way the tool descriptions are: several recipes on one write
    // tool is the normal case, and three full bodies in one error response helps nobody
    int maxRecipes = GrouperConfig.retrieveConfig().propertyValueInt(
        CONFIG_MAX_POINTERS_PER_TOOL, DEFAULT_MAX_POINTERS_PER_TOOL);

    // the two lines below send the reader to the recipe tool, which a deployer can deny with
    // grouper.mcp.tools.allow/deny.  when it is not there, the recipe text still helps and is
    // still carried, but pointing at a tool the client was never offered does not
    boolean recipeToolAvailable = GrouperMcpServlet.isToolAllowedByConfig(RECIPE_TOOL_NAME);

    StringBuilder addition = new StringBuilder();

    int recipeCount = 0;

    for (GrouperMcpRecipe recipe : GrouperMcpRecipe.sortByPriority(recipes)) {

      if (recipeCount++ >= maxRecipes) {
        addition.append("\n\nThere are ").append(recipes.size() - maxRecipes)
            .append(" further recipes about this tool.");
        if (recipeToolAvailable) {
          addition.append(" List them with the recipe tool, action 'list'.");
        }
        break;
      }

      addition.append("\n\nThis institution has a recipe for this tool. Read it before trying "
          + "again, and follow it rather than retrying the call which just failed.\n\n");
      addition.append(recipe.attributionLine()).append("\n\n");
      addition.append("Recipe '").append(recipe.getName()).append("': ")
          .append(recipe.getSummary()).append("\n\n");

      String body = recipe.getBody();

      if (body != null && body.length() > maxBodyChars) {
        addition.append(body, 0, maxBodyChars);
        if (recipeToolAvailable) {
          addition.append("\n\n[Recipe truncated. Call the recipe tool with action 'get' and name '")
              .append(recipe.getName()).append("' for the whole thing.]");
        } else {
          addition.append("\n\n[Recipe truncated.]");
        }
      } else {
        addition.append(body);
      }
    }

    // appended onto the existing error text rather than added as a second content item, so that
    // a client which only surfaces the first block still sees it
    JsonNode content = result.get("content");

    if (content != null && content.isArray() && content.size() > 0
        && content.get(0) instanceof ObjectNode) {

      ObjectNode firstContent = (ObjectNode) content.get(0);

      if (firstContent.has("text")) {
        firstContent.put("text", firstContent.get("text").asText() + addition.toString());
        return;
      }
    }

    ArrayNode newContent = content != null && content.isArray()
        ? (ArrayNode) content : objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", addition.toString().trim());
    newContent.add(textContent);
    result.set("content", newContent);
  }

  /**
   * execute the recipe tool
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser,
      boolean hasReadwriteAccess) {

    String action = arguments != null && arguments.has("action")
        ? arguments.get("action").asText() : null;

    if (StringUtils.isBlank(action)) {
      return buildErrorResult("action is required. Use 'list', 'get', or 'update'.");
    }

    try {

      if ("list".equals(action)) {
        return list(authUser, hasReadwriteAccess);
      }

      if ("get".equals(action)) {
        return get(arguments, authUser, hasReadwriteAccess);
      }

      if ("update".equals(action)) {
        return update(arguments, authUser, hasReadwriteAccess);
      }

      return buildErrorResult("Unknown action '" + action + "'. Use 'list', 'get', or 'update'.");

    } catch (Exception e) {
      LOG.error("Error executing recipe tool action '" + action + "'", e);
      return buildErrorResult("Error executing recipe action '" + action + "'");
    }
  }

  /**
   * list every recipe this user can see, with summaries
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the result
   */
  private static ObjectNode list(GrouperMcpAuthUser authUser, boolean hasReadwriteAccess) {

    Map<String, GrouperMcpRecipe> editable = GrouperMcpRecipe.retrieveRecipesCanEdit(authUser.getSubject());

    // canEdit has to mean what update will actually allow, or a client is told it may change
    // something it will then be refused, and an administrator is told it may not change
    // anything when in fact it may change everything
    boolean canAdmin = hasReadwriteAccess && GrouperMcpRecipe.canAdminInMcp(authUser.getSubject());
    boolean delegationOn = hasReadwriteAccess && GrouperMcpRecipe.isAllowEditInMcp();

    // an administrator sees every enabled recipe, including the ones whose audience they are not
    // in.  they can already read and rewrite any of them through get and update, so listing only
    // their own audience would leave them guessing names for the rest.  everybody else sees the
    // recipes their groups reach.  a disabled recipe is listed to neither: the caller here is a
    // client, and a name it can see is a name it can fetch and follow.  the recipes screen in
    // the UI is where a disabled one is administered
    Map<String, GrouperMcpRecipe> recipes = canAdmin
        ? GrouperMcpRecipe.retrieveAllRecipes()
        : GrouperMcpRecipe.retrieveRecipesCanUse(authUser.getSubject());

    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode recipesArray = objectMapper.createArrayNode();

    for (GrouperMcpRecipe recipe : recipes.values()) {

      ObjectNode recipeNode = objectMapper.createObjectNode();
      recipeNode.put("name", recipe.getName());
      recipeNode.put("summary", recipe.getSummary());
      recipeNode.put("canEdit",
          canAdmin || (delegationOn && editable.containsKey(recipe.getName())));

      if (!recipe.getToolNames().isEmpty()) {
        ArrayNode toolNamesArray = objectMapper.createArrayNode();
        for (String toolName : recipe.getToolNames()) {
          toolNamesArray.add(toolName);
        }
        recipeNode.set("appliesToTools", toolNamesArray);
      }

      recipesArray.add(recipeNode);
    }

    result.set("recipes", recipesArray);

    return buildSuccessResult(result);
  }

  /**
   * return one recipe in full
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @return the result
   */
  private static ObjectNode get(JsonNode arguments, GrouperMcpAuthUser authUser,
      boolean hasReadwriteAccess) {

    String name = argumentString(arguments, "name");

    if (StringUtils.isBlank(name)) {
      return buildErrorResult("name is required for action 'get'.");
    }

    GrouperMcpRecipe recipe = GrouperMcpRecipe.retrieveRecipesCanUse(authUser.getSubject()).get(name);

    // a member of groupNameCanAdminInMcp can update any recipe, so they can read any recipe too:
    // being able to rewrite something one cannot read is worse than either consistent answer.
    // this is the MCP administration group only, not the UI one.  readwrite is required on top
    // of it, the same way list and the tool definition require it, so that a readonly session
    // is not handed recipes outside its own audience.  it reaches past the audience and not past
    // enabled: a body returned here is guidance the client will follow, and a recipe which is
    // turned off is guidance nobody decided is in force
    if (recipe == null && hasReadwriteAccess
        && GrouperMcpRecipe.canAdminInMcp(authUser.getSubject())) {
      recipe = GrouperMcpRecipe.retrieveAllRecipes().get(name);
    }

    // a recipe which exists but is not for this user reads the same as one which does not
    // exist, so that the tool cannot be used to enumerate other people's recipes
    if (recipe == null) {
      return buildErrorResult("There is no recipe named '" + name + "' available to you.");
    }

    ObjectNode result = objectMapper.createObjectNode();
    result.put("name", recipe.getName());
    result.put("summary", recipe.getSummary());

    // the body arrives with a name on it rather than as an anonymous system instruction.  the
    // audience a recipe reaches is fixed, but the text itself is whatever was last written into
    // it, so a reader is told where it came from and who is behind it
    result.put("attribution", recipe.attributionLine());
    result.put("body", recipe.getBody());

    return buildSuccessResult(result);
  }

  /**
   * update the content of a recipe
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @param hasReadwriteAccess true if the user has MCP readwrite access
   * @return the result
   */
  private static ObjectNode update(JsonNode arguments, GrouperMcpAuthUser authUser,
      boolean hasReadwriteAccess) {

    // who may edit a recipe is decided by its own groupNameCanEdit, checked below, and not by
    // being a Grouper administrator: the point of that field is to hand a recipe to the team
    // which owns the process it describes, who are usually not administrators of anything.
    // readwrite is required on top of it only so that a readonly session cannot write
    if (!hasReadwriteAccess) {
      return buildErrorResult("Editing a recipe requires MCP readwrite access.");
    }

    String name = argumentString(arguments, "name");

    if (StringUtils.isBlank(name)) {
      return buildErrorResult("name is required for action 'update'.");
    }

    // two ways in.  an administrator may change any field of any recipe.  a delegated editor may
    // change only the content of a recipe their own group owns, and only while the deployer has
    // turned that path on, which is off by default
    boolean canAdmin = GrouperMcpRecipe.canAdminInMcp(authUser.getSubject());

    GrouperMcpRecipe recipe = canAdmin
        ? GrouperMcpRecipe.retrieveAllRecipes().get(name)
        : GrouperMcpRecipe.retrieveRecipesCanEdit(authUser.getSubject()).get(name);

    // retrieveRecipesCanEdit does include the disabled ones, because the delegated edit screen in
    // the UI is built from it and an owner has to be able to open a recipe they turned off.  MCP
    // does not get that: rewriting a disabled recipe from here would be editing something this
    // caller cannot read back through get and nobody can see the effect of.  filtered here rather
    // than by narrowing that method, which would take the recipe away from the screen too
    if (recipe != null && !recipe.isEnabled()) {
      recipe = null;
    }

    if (recipe == null) {
      return buildErrorResult("There is no recipe named '" + name + "' which you are allowed to edit.");
    }

    if (!canAdmin && !GrouperMcpRecipe.isAllowEditInMcp()) {
      return buildErrorResult("Editing recipes over MCP is turned off here ("
          + GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP + " is false). Edit this recipe in the "
          + "Grouper UI instead.");
    }

    // the fields which decide who a recipe reaches and which tools it attaches to are not
    // reachable from here for a delegated editor.  refused rather than ignored, so a caller
    // which thinks it changed something is told it did not
    // a field sent as null is not an attempt to change it, the same way an omitted one is not.
    // clients routinely null out the fields they are not using, and refusing those would make a
    // whole record echoed back unusable
    if (!canAdmin) {
      for (String controlField : CONTROL_FIELD_NAMES) {
        if (argumentString(arguments, controlField) != null) {
          return buildErrorResult("'" + controlField + "' cannot be changed here. You may change "
              + "only the name, summary, and text of this recipe.");
        }
      }
    }

    // enabled is refused to everybody, an administrator included, rather than dropped along with
    // the fields nobody offered.  a caller which sent it and got a success back would believe it
    // had taken this recipe out of force, and would have written the other fields it sent while
    // believing that
    if (argumentString(arguments, "enabled") != null) {
      return buildErrorResult("'enabled' cannot be changed here. Turning a recipe on or off is "
          + "done on the recipes screen in the Grouper UI, which is also the only place a recipe "
          + "which is turned off can be seen or edited.");
    }

    // only what was actually supplied is written.  filling the rest in from the recipe we read
    // would rewrite fields this caller never touched, out of a copy which can be up to the cache
    // TTL old, so a change somebody else made in the meantime would be silently lost
    Map<String, String> fieldValues = new LinkedHashMap<String, String>();

    String newName = argumentString(arguments, "newName");

    if (newName != null) {
      fieldValues.put("name", newName);
    }

    for (String contentField : new String[] { "summary", "body" }) {
      String value = argumentString(arguments, contentField);
      if (value != null) {
        fieldValues.put(contentField, value);
      }
    }

    // an administrator may also change who the recipe reaches, which tools it attaches to, and
    // whether it is on
    if (canAdmin) {
      for (String controlField : CONTROL_FIELD_NAMES) {
        String value = argumentString(arguments, controlField);
        if (value != null) {
          fieldValues.put(controlField, value);
        }
      }
    }

    if (fieldValues.isEmpty()) {
      return buildErrorResult("Nothing to update. Supply at least one of newName, summary, or body"
          + (canAdmin ? ", " + StringUtils.join(CONTROL_FIELD_NAMES, ", ") + "." : "."));
    }

    // renaming onto another recipe's name would make one of them unreachable by name
    String effectiveName = fieldValues.containsKey("name") ? fieldValues.get("name") : recipe.getName();

    // past the cache, which is node local: a name taken on another node inside the TTL would
    // otherwise look free here
    if (!Strings.CS.equals(effectiveName, recipe.getName())
        && GrouperMcpRecipe.retrieveAllRecipes(true, false).containsKey(effectiveName)) {
      return buildErrorResult("There is already a recipe named '" + effectiveName + "'.");
    }

    // the same rules the recipe screens apply, so writing through MCP cannot put a recipe into a
    // state the UI would have refused
    String validationError = GrouperMcpRecipe.validateFieldValues(recipe.getConfigId(), fieldValues);

    if (validationError != null) {
      return buildErrorResult(validationError);
    }

    GrouperMcpRecipe.updateRecipeFields(recipe.getConfigId(), fieldValues);

    ObjectNode result = objectMapper.createObjectNode();
    result.put("stored", true);
    result.put("configId", recipe.getConfigId());
    result.put("name", effectiveName);

    ArrayNode changedFields = objectMapper.createArrayNode();
    for (String changedField : fieldValues.keySet()) {
      changedFields.add(changedField);
    }
    result.set("changed", changedFields);

    return buildSuccessResult(result);
  }

  /**
   * read a string argument, or null when it is absent or json null
   * @param arguments the tool arguments
   * @param key the argument name
   * @return the value or null
   */
  private static String argumentString(JsonNode arguments, String key) {
    if (arguments == null || !arguments.has(key) || arguments.get(key).isNull()) {
      return null;
    }
    // trimmed, because every one of these is read back through trimToNull.  storing an untrimmed
    // value gains nothing and loses the name uniqueness check: a name with a trailing space does
    // not collide with anything in a map keyed on trimmed names, and then reads back as one
    // which does
    return StringUtils.trim(arguments.get(key).asText());
  }

  /**
   * build a successful MCP tool result carrying a json payload as text
   * @param payload the payload
   * @return the result
   */
  private static ObjectNode buildSuccessResult(ObjectNode payload) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", payload.toString());
    content.add(textContent);
    result.set("content", content);
    result.put("isError", false);
    return result;
  }

  /**
   * build an error MCP tool result
   * @param message the message
   * @return the result
   */
  private static ObjectNode buildErrorResult(String message) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", message);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", true);
    return result;
  }

}
