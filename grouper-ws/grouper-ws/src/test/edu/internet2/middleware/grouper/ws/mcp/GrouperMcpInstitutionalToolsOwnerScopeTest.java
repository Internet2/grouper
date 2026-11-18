/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemMove;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * Security issue 49 (mcpInstitutionalToolOwnerScopeBypass): a GSH template that can write (not
 * mcpReadonly) gets the protected-resource and OAuth read-write scope checks on its owner group
 * or folder, whether the caller names it or it comes from the template's default, on the object
 * the name resolves to.
 *
 * <p>The template prints "ran on" and its owner, so a run is visible in the output.  Before the
 * fix the out of scope owners run.</p>
 */
public class GrouperMcpInstitutionalToolsOwnerScopeTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** in the consent */
  private static final String SANDBOX = "test:mcpOwner:sandbox";

  /** not in the consent */
  private static final String FINANCE = "test:mcpOwner:finance";

  /** */
  private Stem financeStem;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpInstitutionalToolsOwnerScopeTest("testStemOwnerOutOfScopeDenied"));
  }

  /**
   * @param name
   */
  public GrouperMcpInstitutionalToolsOwnerScopeTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    Stem sandboxStem = new StemSave(rootSession).assignName(SANDBOX).assignCreateParentStemsIfNotExist(true).save();
    this.financeStem = new StemSave(rootSession).assignName(FINANCE).assignCreateParentStemsIfNotExist(true).save();
    sandboxStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    this.financeStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    Group sandboxGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(SANDBOX + ":g1").save();
    Group financeGroup = new GroupSave(rootSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(FINANCE + ":g1").save();
    sandboxGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    financeGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    configureTemplate("testOwnerFolderTpl", true, false);
    configureTemplate("testOwnerGroupTpl", false, true);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * a template that can write, run on a folder or a group owner, which prints its owner
   * @param configId id
   * @param showOnFolders runs on folders
   * @param showOnGroups runs on groups
   */
  private static void configureTemplate(String configId, boolean showOnFolders, boolean showOnGroups) {
    String configPrefix = "grouperGshTemplate." + configId + ".";
    java.util.Map<String, String> override = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    override.put(configPrefix + "enabled", "true");
    override.put(configPrefix + "templateType", "gsh");
    override.put(configPrefix + "templateVersion", "V1");
    override.put(configPrefix + "templateName", "Owner Template");
    override.put(configPrefix + "templateDescription", "Template that acts on its owner");
    override.put(configPrefix + "securityRunType", "everyone");
    override.put(configPrefix + "mcpEnabled", "true");
    // mcpReadonly defaults to false: the template can write
    override.put(configPrefix + "runAsType", "GrouperSystem");
    override.put(configPrefix + "showOnFolders", showOnFolders ? "true" : "false");
    override.put(configPrefix + "showOnGroups", showOnGroups ? "true" : "false");
    // required on 7.9.x when the template shows on folders / groups: everywhere, so the scope check decides
    if (showOnFolders) {
      override.put(configPrefix + "folderShowType", "allFolders");
    }
    if (showOnGroups) {
      override.put(configPrefix + "groupShowType", "allGroups");
    }
    override.put(configPrefix + "allowWsFromNoOwner", "false");
    override.put(configPrefix + "numberOfInputs", "0");
    override.put(configPrefix + "gshTemplate", showOnFolders
        ? "gsh_builtin_gshTemplateOutput.addOutputLine(\"ran on \" + gsh_builtin_ownerStemName);"
        : "gsh_builtin_gshTemplateOutput.addOutputLine(\"ran on \" + gsh_builtin_ownerGroupName);");
  }

  /**
   * SUBJ0 as an OAuth caller with read-write consent only on the sandbox folder
   * @return the auth user
   */
  private static GrouperMcpAuthUser sandboxUser() {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteFolders(Arrays.asList(SANDBOX));
    return authUser;
  }

  /**
   * run a template as SUBJ0
   * @param authUser caller
   * @param arguments tool arguments
   * @return result
   */
  private static ObjectNode run(GrouperMcpAuthUser authUser, ObjectNode arguments) {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpInstitutionalTools.execute(arguments, authUser, true);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * @param ownerStemName owner
   * @return execute arguments for the folder template
   */
  private static ObjectNode folderArguments(String ownerStemName) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "execute");
    arguments.put("configId", "testOwnerFolderTpl");
    arguments.put("ownerType", "stem");
    arguments.put("ownerStemName", ownerStemName);
    return arguments;
  }

  /**
   * the write-up's example: an owner folder outside the consent is refused and the template does
   * not run.  Before the fix it runs
   */
  public void testStemOwnerOutOfScopeDenied() {
    ObjectNode result = run(sandboxUser(), folderArguments(FINANCE));
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(result.toString(), result.toString().contains("ran on"));
  }

  /**
   * control: an owner folder in the consent runs
   */
  public void testStemOwnerInScopeAllowed() {
    ObjectNode result = run(sandboxUser(), folderArguments(SANDBOX));
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertTrue(result.toString(), result.toString().contains("ran on " + SANDBOX));
  }

  /**
   * an owner group outside the consent is refused
   */
  public void testGroupOwnerOutOfScopeDenied() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "execute");
    arguments.put("configId", "testOwnerGroupTpl");
    arguments.put("ownerType", "group");
    arguments.put("ownerGroupName", FINANCE + ":g1");
    ObjectNode result = run(sandboxUser(), arguments);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(result.toString(), result.toString().contains("ran on"));
  }

  /**
   * a default owner from the template config, outside the consent, is refused too
   */
  public void testDefaultOwnerOutOfScopeDenied() {
    String configPrefix = "grouperGshTemplate.testOwnerFolderTpl.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runButtonGroupOrFolder", "folder");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "defaultRunButtonFolderUuidOrName",
        this.financeStem.getUuid());

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "execute");
    arguments.put("configId", "testOwnerFolderTpl");
    ObjectNode result = run(sandboxUser(), arguments);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(result.toString(), result.toString().contains("ran on"));
  }

  /**
   * an owner given by the old name of a folder moved out of the consent is refused (issue 54's
   * pattern: the gate uses the resolved folder)
   */
  public void testStemOwnerByAlternateNameDenied() {
    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    Stem moved = new StemSave(rootSession).assignName(SANDBOX + ":movedAway").save();
    moved.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    new StemMove(moved, this.financeStem).assignAlternateName(true).save();

    ObjectNode result = run(sandboxUser(), folderArguments(SANDBOX + ":movedAway"));
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(result.toString(), result.toString().contains("ran on"));
  }

  /**
   * an owner under etc is refused for any caller, including a wheel caller with no OAuth scope
   */
  public void testProtectedOwnerDenied() {
    new StemSave(GrouperSession.staticGrouperSession()).assignName("etc:mcpOwnerProtected")
        .assignCreateParentStemsIfNotExist(true).save();
    GrouperMcpAuthUser rootUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());
    ObjectNode result = GrouperMcpInstitutionalTools.execute(folderArguments("etc:mcpOwnerProtected"), rootUser, true);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertFalse(result.toString(), result.toString().contains("ran on"));
  }

  /**
   * a caller with no OAuth scope (web service credentials) is not limited by a scope, so an
   * owner anywhere that is not protected still runs
   */
  public void testNonOAuthOwnerAllowed() {
    GrouperMcpAuthUser wsUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    ObjectNode result = run(wsUser, folderArguments(FINANCE));
    assertFalse(result.toString(), result.get("isError").asBoolean());
    assertTrue(result.toString(), result.toString().contains("ran on " + FINANCE));
  }
}
