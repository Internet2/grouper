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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpRecipe;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * behavioural tests for the recipe MCP tool: who sees which recipes, what an administrator may
 * do that a delegated editor may not, and what happens to the pointers when the tool is denied.
 *
 * <p>These drive GrouperMcpRecipeTool rather than the servlet, so they exercise the visibility
 * and authorization decisions without standing up a web layer.</p>
 */
public class GrouperMcpRecipeToolTest extends GrouperTest {

  public GrouperMcpRecipeToolTest() {
  }

  public GrouperMcpRecipeToolTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpRecipeToolTest("testDelegatedEditChangesContent"));
    TestRunner.run(GrouperMcpRecipeToolTest.class);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** in the audience of recipeOne */
  private Subject subjectInAudience;

  /** in no recipe's audience, and not an administrator */
  private Subject subjectOutsider;

  /** in groupNameCanAdminInMcp, but in no recipe's audience */
  private Subject subjectAdmin;

  /** owns recipeOne's wording through groupNameCanEdit */
  private Subject subjectEditor;

  @Override
  protected void setUp() {
    super.setUp();

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    this.subjectInAudience = SubjectTestHelper.SUBJ0;
    this.subjectOutsider = SubjectTestHelper.SUBJ1;
    this.subjectAdmin = SubjectTestHelper.SUBJ2;
    this.subjectEditor = SubjectTestHelper.SUBJ3;

    Group audienceGroup = createGroup("test:recipeAudience");
    audienceGroup.addMember(this.subjectInAudience, false);

    Group editorGroup = createGroup("test:recipeEditors");
    editorGroup.addMember(this.subjectEditor, false);

    Group adminGroup = createGroup("test:recipeMcpAdmins");
    adminGroup.addMember(this.subjectAdmin, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_GROUP_CAN_ADMIN_IN_MCP, "test:recipeMcpAdmins");

    // enabled, and reaching test:recipeAudience
    configureRecipe("recipeOne", "recipe-one", "Use the policy template, not group_save",
        "test:recipeAudience", "test:recipeEditors", "group_save", true);

    // enabled, but reaching a group nobody in this test is in
    configureRecipe("recipeTwo", "recipe-two", "Something for another audience",
        "test:recipeOtherAudience", null, "group_delete", true);

    // disabled, so it reaches nobody at all
    configureRecipe("recipeThree", "recipe-three", "Turned off pending a rewrite",
        "test:recipeAudience", null, "group_save", false);

    GrouperMcpRecipe.clearCache();
  }

  @Override
  protected void tearDown() {
    GrouperMcpRecipe.clearCache();
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * @param groupName the group to create
   * @return the group
   */
  private Group createGroup(String groupName) {
    return new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit(groupName)
        .assignName(groupName)
        .assignCreateParentStemsIfNotExist(true)
        .save();
  }

  /**
   * write one recipe into the configuration
   * @param configId the config id
   * @param name what a client asks for it by
   * @param summary the one line rule
   * @param groupNameCanUse who may read it
   * @param groupNameCanEdit who owns its wording, or null
   * @param toolNames comma separated tools it is about
   * @param enabled whether it is on
   */
  private void configureRecipe(String configId, String name, String summary,
      String groupNameCanUse, String groupNameCanEdit, String toolNames, boolean enabled) {

    String prefix = GrouperMcpRecipe.CONFIG_PREFIX + configId + ".";

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "name", name);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "summary", summary);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "body",
        "The detail behind " + name + ".");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "groupNameCanUse",
        groupNameCanUse);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "enabled",
        enabled ? "true" : "false");

    if (groupNameCanEdit != null) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "groupNameCanEdit",
          groupNameCanEdit);
    }

    if (toolNames != null) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "toolNames", toolNames);
    }
  }

  /**
   * @param action the action to invoke
   * @return arguments carrying just that action
   */
  private ObjectNode arguments(String action) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", action);
    return arguments;
  }

  /**
   * run the tool the way the servlet runs it, inside a session for the authenticated subject.
   *
   * <p>GrouperMcpServlet starts that session and dispatches inside callbackGrouperSession, so a
   * tool may assume one is open.  It matters for any action which writes: config writes record
   * an audit entry, and stamping an audit entry needs an open session, so calling execute bare
   * fails with "There is no open GrouperSession detected" rather than anything about recipes.
   * Read-only actions happen to work without one, which is why only the writing test noticed.</p>
   *
   * @param arguments the tool arguments
   * @param authUser the authenticated user
   * @param hasReadwriteAccess whether the caller has MCP readwrite
   * @return the tool result
   */
  private ObjectNode executeAsAuthUser(final ObjectNode arguments,
      final GrouperMcpAuthUser authUser, final boolean hasReadwriteAccess) {

    // start(subject, false) does not put the session on the thread local, so the callback is
    // what makes staticGrouperSession find it.  the same pair the servlet uses
    GrouperSession grouperSession = GrouperSession.start(authUser.getSubject(), false);

    try {

      return (ObjectNode) GrouperSession.callbackGrouperSession(grouperSession,
          new GrouperSessionHandler() {

            public Object callback(GrouperSession theGrouperSession) {
              return GrouperMcpRecipeTool.execute(arguments, authUser, hasReadwriteAccess);
            }
          });

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the payload of a successful result, which the tool returns as text
   * @param result the tool result
   * @return the parsed payload
   */
  private JsonNode payload(ObjectNode result) {
    assertFalse("expected a success result, got: " + result.toString(),
        result.get("isError").asBoolean());
    try {
      return objectMapper.readTree(result.get("content").get(0).get("text").asText());
    } catch (Exception e) {
      throw new RuntimeException("could not parse the tool result payload", e);
    }
  }

  /**
   * @param result the tool result
   * @return the error message
   */
  private String errorMessage(ObjectNode result) {
    assertTrue("expected an error result, got: " + result.toString(),
        result.get("isError").asBoolean());
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * @param recipes the recipes array from a list payload
   * @param name the name to look for
   * @return that recipe, or null
   */
  private JsonNode findRecipe(JsonNode recipes, String name) {
    for (JsonNode recipe : recipes) {
      if (name.equals(recipe.get("name").asText())) {
        return recipe;
      }
    }
    return null;
  }

  /**
   * somebody in a recipe's audience sees it, and does not see the other audience's recipe or
   * the disabled one
   */
  public void testListFiltersByAudience() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectInAudience);

    JsonNode recipes = payload(GrouperMcpRecipeTool.execute(arguments("list"), authUser, false))
        .get("recipes");

    assertEquals("only the enabled recipe for this audience", 1, recipes.size());
    assertEquals("recipe-one", recipes.get(0).get("name").asText());
  }

  /**
   * somebody in no recipe's audience is offered no tool at all
   */
  public void testToolDefinitionNullForOutsider() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectOutsider);

    assertNull("no recipes reach this subject, so no tool",
        GrouperMcpRecipeTool.toolDefinition(authUser, false));
  }

  /**
   * an MCP administrator gets the tool even when no recipe's audience includes them, since they
   * can read and rewrite every recipe
   */
  public void testToolDefinitionForAdminNotInAnyAudience() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    assertNotNull("an administrator administers recipes they are not an audience for",
        GrouperMcpRecipeTool.toolDefinition(authUser, true));
  }

  /**
   * an administrator lists every recipe, including other audiences and the disabled ones, and
   * the disabled ones say so
   */
  public void testListAsAdminIncludesOtherAudiences() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    JsonNode recipes = payload(GrouperMcpRecipeTool.execute(arguments("list"), authUser, true))
        .get("recipes");

    assertEquals("every recipe, whatever its audience or state", 3, recipes.size());

    assertNotNull("another audience's recipe is listed", findRecipe(recipes, "recipe-two"));

    JsonNode disabled = findRecipe(recipes, "recipe-three");
    assertNotNull("the disabled recipe is listed", disabled);
    assertFalse("and is marked as off, so it does not read as guidance in force",
        disabled.get("enabled").asBoolean());

    JsonNode enabled = findRecipe(recipes, "recipe-one");
    assertFalse("an enabled recipe carries no enabled flag", enabled.has("enabled"));
    assertTrue("an administrator may edit anything", enabled.get("canEdit").asBoolean());
  }

  /**
   * a readonly session does not get an administrator's wider view, since list, get and the tool
   * definition all require readwrite on top of the administration group
   */
  public void testAdminWithoutReadwriteSeesOnlyOwnAudience() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    JsonNode recipes = payload(GrouperMcpRecipeTool.execute(arguments("list"), authUser, false))
        .get("recipes");

    assertEquals("no readwrite, so no administration and no audience of their own",
        0, recipes.size());

    ObjectNode getArguments = arguments("get");
    getArguments.put("name", "recipe-two");

    assertTrue("and get does not reach past their audience either",
        errorMessage(GrouperMcpRecipeTool.execute(getArguments, authUser, false))
            .contains("no recipe named"));
  }

  /**
   * a recipe which is not for this user reads the same as one which does not exist, so the tool
   * cannot be used to find out which recipes other people have
   */
  public void testGetOutsideAudienceIsIndistinguishableFromMissing() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectInAudience);

    ObjectNode otherAudience = arguments("get");
    otherAudience.put("name", "recipe-two");

    ObjectNode noSuchRecipe = arguments("get");
    noSuchRecipe.put("name", "recipe-does-not-exist");

    assertEquals("the two answers must not differ",
        errorMessage(GrouperMcpRecipeTool.execute(noSuchRecipe, authUser, false))
            .replace("recipe-does-not-exist", "X"),
        errorMessage(GrouperMcpRecipeTool.execute(otherAudience, authUser, false))
            .replace("recipe-two", "X"));
  }

  /**
   * an administrator may read a recipe whose audience they are not in
   */
  public void testGetAsAdminReachesOtherAudiences() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    ObjectNode getArguments = arguments("get");
    getArguments.put("name", "recipe-two");

    assertEquals("recipe-two",
        payload(GrouperMcpRecipeTool.execute(getArguments, authUser, true)).get("name").asText());
  }

  /**
   * editing over MCP is off by default, so a delegated editor is refused and told why
   */
  public void testDelegatedEditRefusedWhenNotAllowedInMcp() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP, "false");
    GrouperMcpRecipe.clearCache();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectEditor);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("summary", "a new summary");

    assertTrue("the refusal names the config which turns it on",
        errorMessage(GrouperMcpRecipeTool.execute(updateArguments, authUser, true))
            .contains(GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP));
  }

  /**
   * a delegated editor may change the wording of a recipe their group owns
   */
  public void testDelegatedEditChangesContent() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP, "true");
    GrouperMcpRecipe.clearCache();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectEditor);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("summary", "a new summary");

    payload(executeAsAuthUser(updateArguments, authUser, true));

    // the tool writes to database config, but this test seeded the recipe through
    // propertiesOverrideMap, which the cascade lays over everything else it reads.  the seed
    // would mask the write and this would assert against its own setup.  drop the seed for the
    // one key under test, so what surfaces is what was actually persisted
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .remove(GrouperMcpRecipe.CONFIG_PREFIX + "recipeOne.summary");

    ConfigPropertiesCascadeBase.clearCache();
    GrouperMcpRecipe.clearCache();

    assertEquals("a new summary",
        GrouperMcpRecipe.retrieveAllRecipes(true).get("recipe-one").getSummary());
  }

  /**
   * a delegated editor is refused the fields which decide who a recipe reaches, rather than
   * having them quietly ignored
   */
  public void testDelegatedEditRefusedControlFields() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP, "true");
    GrouperMcpRecipe.clearCache();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectEditor);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("groupNameCanUse", "test:somewhereElse");

    assertTrue("refused, not ignored",
        errorMessage(GrouperMcpRecipeTool.execute(updateArguments, authUser, true))
            .contains("groupNameCanUse"));

    GrouperMcpRecipe.clearCache();

    assertEquals("and the audience is unchanged", "test:recipeAudience",
        GrouperMcpRecipe.retrieveAllRecipes(true).get("recipe-one").getGroupNameCanUse());
  }

  /**
   * somebody who owns no recipe cannot edit one, even with editing turned on
   */
  public void testUpdateRefusedForSubjectWhoOwnsNothing() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP, "true");
    GrouperMcpRecipe.clearCache();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectInAudience);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("summary", "a new summary");

    assertTrue("being in the audience is not owning the wording",
        errorMessage(GrouperMcpRecipeTool.execute(updateArguments, authUser, true))
            .contains("allowed to edit"));
  }

  /**
   * a readonly session cannot write, whatever else it is
   */
  public void testUpdateRefusedWithoutReadwrite() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpRecipe.CONFIG_ALLOW_EDIT_IN_MCP, "true");
    GrouperMcpRecipe.clearCache();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("summary", "a new summary");

    assertTrue("readwrite is required to write",
        errorMessage(GrouperMcpRecipeTool.execute(updateArguments, authUser, false))
            .contains("readwrite"));
  }

  /**
   * renaming a recipe onto another recipe's name is refused, since the name is what a client
   * asks for a recipe by
   */
  public void testUpdateRefusesDuplicateName() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(this.subjectAdmin);

    ObjectNode updateArguments = arguments("update");
    updateArguments.put("name", "recipe-one");
    updateArguments.put("newName", "recipe-two");

    assertTrue("the collision is reported",
        errorMessage(GrouperMcpRecipeTool.execute(updateArguments, authUser, true))
            .contains("already a recipe named"));
  }

  /**
   * a recipe naming a tool puts a pointer on that tool's description, for somebody the recipe
   * reaches
   */
  public void testPointersAppendedToNamedTool() {

    ArrayNode tools = toolsArray("group_save", "group_delete", "recipe");

    GrouperMcpRecipeTool.appendRecipePointers(tools,
        new GrouperMcpAuthUser(this.subjectInAudience));

    assertTrue("the tool the recipe names carries a pointer",
        description(tools, "group_save").contains("recipe-one"));
    assertFalse("a tool it does not name is untouched",
        description(tools, "group_delete").contains("recipe-one"));
  }

  /**
   * no pointers for somebody no recipe reaches
   */
  public void testNoPointersForOutsider() {

    ArrayNode tools = toolsArray("group_save", "recipe");

    GrouperMcpRecipeTool.appendRecipePointers(tools,
        new GrouperMcpAuthUser(this.subjectOutsider));

    assertEquals("untouched", "", description(tools, "group_save"));
  }

  /**
   * when the recipe tool is not on the finished list, nothing points at it.  this is what the
   * deployer's grouper.mcp.tools.deny leaves behind: the other tools are still advertised, and
   * would otherwise carry pointers to a tool the client was never offered
   */
  public void testNoPointersWhenRecipeToolNotOnList() {

    ArrayNode tools = toolsArray("group_save");

    GrouperMcpRecipeTool.appendRecipePointers(tools,
        new GrouperMcpAuthUser(this.subjectInAudience));

    assertEquals("nothing points at a tool which is not on the list",
        "", description(tools, "group_save"));
  }

  /**
   * the same, driven through the deny configuration rather than by leaving the tool out, so the
   * two halves of the deny path are both covered
   */
  public void testNoPointersWhenRecipeToolDeniedByConfig() {

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.mcp.tools.deny", "recipe");

    ArrayNode tools = toolsArray("group_save", "recipe");

    // the servlet would not have added a denied tool, so take it back off the way it would be
    // missing in the finished list
    ArrayNode withoutRecipe = objectMapper.createArrayNode();
    for (JsonNode tool : tools) {
      if (!"recipe".equals(tool.get("name").asText())) {
        withoutRecipe.add(tool);
      }
    }

    GrouperMcpRecipeTool.appendRecipePointers(withoutRecipe,
        new GrouperMcpAuthUser(this.subjectInAudience));

    assertEquals("a denied recipe tool leaves no dangling instructions",
        "", description(withoutRecipe, "group_save"));

    // and the failure hook, which points at the tool by name rather than by list
    ObjectNode errorResult = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", "something failed");
    content.add(textContent);
    errorResult.set("content", content);

    GrouperMcpRecipeTool.appendRecipesToError(errorResult, "group_save",
        new GrouperMcpAuthUser(this.subjectInAudience));

    assertFalse("the failure carries the recipe but does not send anyone to a denied tool",
        errorResult.get("content").get(0).get("text").asText().contains("recipe tool"));
  }

  /**
   * @param toolNames the tools to put on the list
   * @return a tools array shaped like the one tools/list builds
   */
  private ArrayNode toolsArray(String... toolNames) {

    ArrayNode tools = objectMapper.createArrayNode();

    for (String toolName : toolNames) {
      ObjectNode tool = objectMapper.createObjectNode();
      tool.put("name", toolName);
      tool.put("description", "");
      tools.add(tool);
    }

    return tools;
  }

  /**
   * @param tools the tools array
   * @param toolName the tool to look for
   * @return that tool's description
   */
  private String description(ArrayNode tools, String toolName) {
    for (JsonNode tool : tools) {
      if (toolName.equals(tool.get("name").asText())) {
        return tool.get("description").asText();
      }
    }
    throw new RuntimeException("no tool named '" + toolName + "' on the list");
  }

}
