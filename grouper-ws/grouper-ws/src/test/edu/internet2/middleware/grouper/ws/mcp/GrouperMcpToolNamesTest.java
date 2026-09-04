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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.mcp.GrouperMcpToolNames;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * GrouperMcpToolNames lives in the API module so that the recipe configuration and the recipes
 * screen can validate against it, since neither can see the classes in this module.  That means
 * it can drift away from the tools this module actually has, and a recipe pointing at a tool
 * which no longer exists fails silently: its pointer is added to nothing.
 *
 * <p>Two things keep it honest.  addToolIfAllowed refuses to advertise a tool which is not in
 * the list, so a new tool which nobody registered fails the first time a tool list is built.
 * This test covers the other direction, a name left in the list after its tool was removed.</p>
 *
 * <p>This reads source files and touches nothing else, so it extends TestCase rather than
 * GrouperTest: a check this cheap should not need a database to be up in order to run.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpToolNamesTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpToolNamesTest("testToolNamesMatchTheTools"));
    TestRunner.run(GrouperMcpToolNamesTest.class);
  }

  /**
   * @param name
   */
  public GrouperMcpToolNamesTest(String name) {
    super(name);
  }

  /** finds the name each tool definition gives itself, written as a literal */
  private static final Pattern TOOL_NAME_PATTERN =
      Pattern.compile("\\.put\\(\"name\", \"([a-z_]+)\"\\)");

  /**
   * finds a tool which names itself from a constant instead of a literal, e.g.
   * <code>tool.put("name", RECIPE_TOOL_NAME)</code>, which a tool does when its name is
   * referenced in several places.  the constant is then resolved against its declaration in the
   * same file.
   *
   * <p>Deliberately only the <code>_TOOL_NAME</code> suffix.  A put of "name" is not on its own
   * a tool: GrouperMcpServlet builds the initialize response with
   * <code>serverInfo.put("name", SERVER_NAME)</code>, which names the server rather than a
   * tool.</p>
   */
  private static final Pattern TOOL_NAME_CONSTANT_PATTERN =
      Pattern.compile("\\.put\\(\"name\", ([A-Z][A-Z0-9_]*_TOOL_NAME)\\)");

  /** finds the tool names the servlet dispatches */
  private static final Pattern DISPATCH_PATTERN =
      Pattern.compile("case \"([a-z_]+)\":");

  /**
   * every advertised tool name is registered, and every registered name is a tool which exists
   */
  public void testToolNamesMatchTheTools() {

    File mcpDirectory = mcpSourceDirectory();

    Set<String> advertisedToolNames = new LinkedHashSet<String>();

    for (File javaFile : GrouperUtil.nonNull(mcpDirectory.listFiles(), File.class)) {

      if (!javaFile.getName().endsWith(".java")) {
        continue;
      }

      String source = GrouperUtil.readFileIntoString(javaFile);

      // only the classes which define a tool.  otherwise any future put("name", "something")
      // elsewhere in this package would be read as a tool and fail this test for no reason
      if (!source.contains("toolDefinition")) {
        continue;
      }

      Matcher matcher = TOOL_NAME_PATTERN.matcher(source);

      while (matcher.find()) {
        advertisedToolNames.add(matcher.group(1));
      }

      // a tool whose name is referenced in several places declares it as a constant and puts
      // that, rather than repeating the literal.  resolve the constant against its declaration
      // in the same file, so naming style does not decide whether this test can see a tool
      Matcher constantMatcher = TOOL_NAME_CONSTANT_PATTERN.matcher(source);

      while (constantMatcher.find()) {

        String constantName = constantMatcher.group(1);

        Matcher valueMatcher = Pattern.compile(
            "\\b" + Pattern.quote(constantName) + "\\s*=\\s*\"([a-z_]+)\"").matcher(source);

        assertTrue("Tool definition in " + javaFile.getName() + " names itself from constant '"
            + constantName + "', but that constant is not declared with a string literal in the "
            + "same file, so this test cannot tell which tool it is", valueMatcher.find());

        advertisedToolNames.add(valueMatcher.group(1));
      }
    }

    assertTrue("Found no tool definitions to check, so this test is not testing anything",
        advertisedToolNames.size() > 20);

    List<String> notRegistered = new ArrayList<String>();
    for (String advertisedToolName : advertisedToolNames) {
      if (!GrouperMcpToolNames.isToolName(advertisedToolName)) {
        notRegistered.add(advertisedToolName);
      }
    }

    assertEquals("These tools exist but are not in GrouperMcpToolNames, so a recipe cannot "
        + "point at them: " + notRegistered, 0, notRegistered.size());

    List<String> noSuchTool = new ArrayList<String>();
    for (String registeredToolName : GrouperMcpToolNames.toolNames()) {
      if (!advertisedToolNames.contains(registeredToolName)) {
        noSuchTool.add(registeredToolName);
      }
    }

    assertEquals("These names are in GrouperMcpToolNames but no tool defines them, so a recipe "
        + "pointing at one would silently do nothing: " + noSuchTool, 0, noSuchTool.size());
  }

  /**
   * every registered tool name is answered by the servlet.  a tool which is advertised and then
   * refused as unknown when it is called is worse than one which was never advertised
   */
  public void testEveryToolNameIsDispatched() {

    File servletFile = new File(mcpSourceDirectory(), "GrouperMcpServlet.java");

    assertTrue("Cannot find " + servletFile.getAbsolutePath(), servletFile.exists());

    Set<String> dispatchedToolNames = new LinkedHashSet<String>();

    Matcher matcher = DISPATCH_PATTERN.matcher(GrouperUtil.readFileIntoString(servletFile));

    while (matcher.find()) {
      dispatchedToolNames.add(matcher.group(1));
    }

    List<String> notDispatched = new ArrayList<String>();
    for (String registeredToolName : GrouperMcpToolNames.toolNames()) {
      if (!dispatchedToolNames.contains(registeredToolName)) {
        notDispatched.add(registeredToolName);
      }
    }

    // the other direction is not checked: the dispatch is deliberately a superset, since
    // sql_select_count is still answered for older clients without being advertised
    assertEquals("These names are in GrouperMcpToolNames but the servlet does not dispatch them: "
        + notDispatched, 0, notDispatched.size());
  }

  /**
   * find the MCP source, wherever the test was launched from.  the working directory is the
   * grouper-ws module under some runners and the repository root under others, so this walks up
   * from wherever it started looking for either shape rather than assuming one of them
   * @return the directory holding the MCP source
   */
  private static File mcpSourceDirectory() {

    String moduleRelativePath = "src/grouper-ws/edu/internet2/middleware/grouper/ws/mcp";
    String repositoryRelativePath = "grouper-ws/grouper-ws/" + moduleRelativePath;

    File directory = new File(".").getAbsoluteFile();

    while (directory != null) {

      File candidate = new File(directory, moduleRelativePath);

      if (candidate.exists()) {
        return candidate;
      }

      candidate = new File(directory, repositoryRelativePath);

      if (candidate.exists()) {
        return candidate;
      }

      directory = directory.getParentFile();
    }

    // failing rather than passing quietly: a check which cannot see the source proves nothing,
    // and one which says so is easier to fix than one which silently stops guarding anything
    fail("Cannot find the MCP source from " + new File(".").getAbsolutePath()
        + ", looking up the tree for '" + moduleRelativePath + "' or '" + repositoryRelativePath
        + "', so this test cannot check the tool names");

    return null;
  }

}
