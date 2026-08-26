---
name: grouper-mcp-edit
description: |
  Guide for creating, editing, and maintaining Grouper MCP (Model Context Protocol) tools and their
  associated tests, servlet registration, configuration, and wiki documentation. Use this skill
  whenever working on MCP tools in the Grouper codebase - including creating new tools, modifying
  existing ones, adding tests, registering tools in the servlet, adding configuration properties,
  or updating the MCP wiki documentation HTML files. Trigger on any mention of MCP tools, MCP servlet,
  MCP tests, or MCP documentation in the Grouper project context. Also trigger when the user mentions
  tool names like group_find, group_add_member, folder_delete, etc. in the context of Grouper development.
---

# Grouper MCP Tool Development Guide

This skill walks you through the complete workflow for creating or modifying an MCP tool in the
Grouper codebase. Every MCP tool touches several files in a predictable pattern. Follow these
steps in order so nothing gets missed.

## File Locations

All MCP source files live under a single package:

- **Tool classes**: `grouper-ws/grouper-ws/src/grouper-ws/edu/internet2/middleware/grouper/ws/mcp/`
- **Test classes**: `grouper-ws/grouper-ws/src/test/edu/internet2/middleware/grouper/ws/mcp/`
- **Configuration**: `grouper/conf/grouper.base.properties` (search for `# MCP` section)
- **Wiki docs**: `grouper/temp/mcpDocs/` (HTML files)

## Code Documentation Standards

Good javadoc and inline comments are important in this codebase. Every tool class should have:

- **Class-level javadoc**: Describe what the tool does, how it works at a high level, any security
  considerations (e.g., which privileges are required), and `@author` tag.
- **Method-level javadoc**: Every public/package method needs javadoc with `@param` and `@return` tags.
  Private helper methods should have at least a one-line javadoc comment.
- **Inline comments**: Add comments for non-obvious logic — security checks, why a particular API
  is used, what each group of WS parameters means (especially when passing many nulls to a service
  method). Use inline comments on complex parameter lists to label what each argument is for.

Look at the existing tool classes (e.g., `GrouperMcpAddMember.java`, `GrouperMcpFindGroups.java`)
for the expected level of documentation.

## Step 1: Create the Tool Class

Create `GrouperMcp[ToolName].java` in the MCP package. Tools are plain Java classes with
**no inheritance** - just two required static methods.

### Required Structure

```java
package edu.internet2.middleware.grouper.ws.mcp;

// imports...
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.logging.Log;

public class GrouperMcp[ToolName] {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcp[ToolName].class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * return the MCP tool definition for [tool_name]
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() { ... }

  /**
   * execute the [tool_name] tool
   * @param arguments the tool arguments from the MCP request
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) { ... }

  private static ObjectNode buildSuccessResult(String text) { ... }
  private static ObjectNode buildErrorResult(String errorMessage) { ... }
}
```

### toolDefinition() Method

Build a Jackson ObjectNode describing the tool for the MCP protocol:

```java
public static ObjectNode toolDefinition() {
  ObjectNode tool = objectMapper.createObjectNode();
  tool.put("name", "tool_name_in_snake_case");
  tool.put("description", "Human-readable description of what the tool does.");

  ObjectNode inputSchema = objectMapper.createObjectNode();
  inputSchema.put("type", "object");

  ObjectNode properties = objectMapper.createObjectNode();

  // For each parameter:
  ObjectNode paramProp = objectMapper.createObjectNode();
  paramProp.put("type", "string");  // or "integer", "boolean", "array"
  paramProp.put("description", "What this parameter does.");
  properties.set("paramName", paramProp);

  // For enum parameters:
  ArrayNode enumValues = objectMapper.createArrayNode();
  enumValues.add("VALUE_ONE");
  enumValues.add("VALUE_TWO");
  paramProp.set("enum", enumValues);

  inputSchema.set("properties", properties);

  // Required parameters:
  ArrayNode required = objectMapper.createArrayNode();
  required.add("paramName");
  inputSchema.set("required", required);

  tool.set("inputSchema", inputSchema);
  return tool;
}
```

### execute() Method

The execute method receives parsed JSON arguments and the authenticated user. It should:

1. Parse and validate arguments from the JsonNode
2. For **readwrite** tools: check protected resources and OAuth scope
3. Delegate to existing Grouper service logic (typically `GrouperServiceLogic.*`)
4. Return results via `buildSuccessResult()` or `buildErrorResult()`

#### Argument Parsing Pattern

Always handle null/missing arguments defensively:

```java
String paramValue = arguments != null && arguments.has("paramName")
    ? arguments.get("paramName").asText() : null;
boolean boolParam = arguments != null && arguments.has("boolParam")
    && arguments.get("boolParam").asBoolean(false);
int intParam = arguments != null && arguments.has("intParam")
    ? arguments.get("intParam").asInt(50) : 50;
```

#### Readwrite Tool Security Checks

Readwrite tools must check two things before proceeding:

```java
// 1. Block modifications to protected system groups/stems
if (GrouperMcpProtectedResources.isProtectedGroupName(groupName)) {
  return buildErrorResult(
      GrouperMcpProtectedResources.buildProtectedGroupError(groupName));
}

// 2. Check OAuth readwrite scope restrictions
if (authUser.isOAuthAuthenticated()) {
  if (!authUser.isGroupInReadwriteScope(groupName)) {
    return buildErrorResult(
        authUser.buildReadwriteScopeDeniedError("group", groupName));
  }
}
```

For subjects, use `authUser.isSubjectInReadwriteScope(subjectValue)`.
For stems, use `GrouperMcpProtectedResources.isProtectedStemName()` and
`authUser.isStemInReadwriteScope()`.

#### Result Format

The MCP protocol expects this exact JSON structure:

```java
private static ObjectNode buildSuccessResult(String text) {
  ObjectNode result = objectMapper.createObjectNode();
  ArrayNode content = objectMapper.createArrayNode();
  ObjectNode textContent = objectMapper.createObjectNode();
  textContent.put("type", "text");
  textContent.put("text", text);
  content.add(textContent);
  result.set("content", content);
  result.put("isError", false);
  return result;
}

private static ObjectNode buildErrorResult(String errorMessage) {
  ObjectNode result = objectMapper.createObjectNode();
  ArrayNode content = objectMapper.createArrayNode();
  ObjectNode textContent = objectMapper.createObjectNode();
  textContent.put("type", "text");
  textContent.put("text", errorMessage);
  content.add(textContent);
  result.set("content", content);
  result.put("isError", true);
  return result;
}
```

#### Error Handling in execute()

Wrap the main logic in try/catch. Return errors as result objects, never throw:

```java
try {
  // ... main logic ...
} catch (Exception e) {
  LOG.error("Error doing X", e);
  return buildErrorResult("Error doing X: " + e.getMessage()
      + "\n\n" + GrouperUtil.getFullStackTrace(e));
}
```

## Step 2: Register in GrouperMcpServlet

Registration requires changes in **two** methods of `GrouperMcpServlet.java`:

### 2a. handleToolsList()

Add the tool definition to the appropriate access-level block. Tools are organized
alphabetically within each block:

```java
// In handleToolsList():

// For readonly tools:
if (hasReadonlyAccess(authUser)) {
  addToolIfAllowed(toolsArray, GrouperMcpNewTool.toolDefinition());  // alphabetical order
}

// For readwrite tools:
if (hasReadwriteAccess(authUser)) {
  addToolIfAllowed(toolsArray, GrouperMcpNewTool.toolDefinition());
}

// For SQL tools:
if (hasSqlReadonlyAccess(authUser)) { ... }

// For admin readonly tools:
if (hasAdminReadonlyAccess(authUser)) { ... }

// For admin readwrite tools:
if (hasAdminReadwriteAccess(authUser)) { ... }
```

### 2b. dispatchToolCall()

Add a case to the switch statement. Match the access check to the tool's category.
Cases are organized alphabetically within each access-level section:

```java
// For readonly tools:
case "new_tool_name":
  if (!hasReadonlyAccess(authUser)) {
    return buildMcpErrorResult("Access denied: user is not authorized for new_tool_name. "
        + "Membership in the MCP readonly or readwrite group is required.");
  }
  return GrouperMcpNewTool.execute(arguments, authUser);

// For readwrite tools:
case "new_tool_name":
  if (!hasReadwriteAccess(authUser)) {
    return buildMcpErrorResult("Access denied: user is not authorized for new_tool_name. "
        + "Membership in the MCP readwrite group is required.");
  }
  return GrouperMcpNewTool.execute(arguments, authUser);
```

### 2c. GrouperMcpToolLog.getToolCategory()

Add the tool name to the `getToolCategory()` switch statement in
`grouper/src/grouper/edu/internet2/middleware/grouper/mcp/GrouperMcpToolLog.java`.
This maps the tool name to its category for audit logging and rate limiting.
Add the case alphabetically within the correct category block:

```java
// In getToolCategory():
// readonly tools:
case "new_tool_name":
  return CATEGORY_READONLY;

// readwrite tools:
case "new_tool_name":
  return CATEGORY_READWRITE;

// sql tools:
case "new_tool_name":
  return CATEGORY_SQL;

// admin readonly:
case "new_tool_name":
  return CATEGORY_ADMIN_READONLY;

// admin readwrite:
case "new_tool_name":
  return CATEGORY_ADMIN_READWRITE;
```

If you skip this step, the tool will default to `CATEGORY_READONLY`, which means
readwrite tools will have incorrect throttle limits in the audit log.

## Step 3: Write the Unit Test

Create `GrouperMcp[ToolName]Test.java` in the test MCP package.

### Test Class Structure

```java
package edu.internet2.middleware.grouper.ws.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

public class GrouperMcp[ToolName]Test extends GrouperTest {

  public GrouperMcp[ToolName]Test() { }

  public GrouperMcp[ToolName]Test(String name) { super(name); }

  public static void main(String[] args) {
    TestRunner.run(new GrouperMcp[ToolName]Test("testMethodName"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("groups.create.grant.all.view", "false");
    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }
}
```

### Test Method Pattern

Each test method should:
1. Set up test data (groups, stems, members, etc.)
2. Grant appropriate privileges to the test subject
3. Start a GrouperSession as the test subject
4. Create a `GrouperMcpAuthUser` for that subject
5. Build arguments as an ObjectNode
6. Call the tool's `execute()` method
7. Assert on `isError` and parse the result text as JSON

```java
public void testBasicOperation() {
  // 1. Create test data
  Group group = new GroupSave(GrouperSession.staticGrouperSession())
      .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:mcpTestGroup")
      .assignName("test:mcpTestGroup")
      .assignCreateParentStemsIfNotExist(true)
      .assignDescription("test group").save();

  // 2. Grant privileges
  group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

  // 3-4. Session and auth user
  GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
  try {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

    // 5. Build arguments
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("paramName", "value");

    // 6. Execute
    ObjectNode result = GrouperMcpNewTool.execute(arguments, authUser);

    // 7. Assert
    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    JsonNode responseNode = objectMapper.readTree(text);
    // ... assertions on responseNode ...

  } catch (Exception e) {
    fail("Unexpected exception: " + e.getMessage());
  } finally {
    GrouperSession.stopQuietly(session);
  }
}
```

### Must-Have Test Cases

1. **testToolDefinition()** - Verify the tool definition has correct name, description, schema, required fields
2. **testBasicOperation()** - Happy path with valid inputs
3. **testMissingRequiredParam()** - Verify error when required parameters are missing
4. **testNullArguments()** - Verify graceful handling of null arguments
5. For readwrite tools: **testProtectedResource()** - Verify protected resources are blocked

### Register the Test

Add the test class to `AllMcpTests.java`:

```java
suite.addTestSuite(GrouperMcp[ToolName]Test.class);
```

## Step 4: Add Configuration Properties (if needed)

If the tool needs configuration, add properties to the MCP section of
`grouper/conf/grouper.base.properties`. Search for `# MCP` to find the section.

Follow the existing naming convention: `grouper.mcp.[category].[configId].[property]`

## Step 5: Update Wiki Documentation

The wiki documentation lives in `grouper/temp/mcpDocs/`. These are HTML files that serve as
user-facing documentation. The documentation should be non-technical and focused on what
the tool does and how to use it, not on implementation details.

Read `references/doc-patterns.md` for the exact HTML patterns and which files to update.

### Files to Update

For a new tool, update:
1. **mcpWikiUserGuide.html** - Add the tool to the table of contents and add a tool section
2. **mcpWikiAdminGuide.html** - If the tool has admin-specific configuration

For admin/external-system tools, also update:
3. **mcpWikiAdminExternalSystems.html** - If the tool involves external system integration

## Access Level Reference

| Access Level | Authorization Group Config | OAuth Consent Scope | Tools |
|---|---|---|---|
| readonly | `grouper.mcp.users.readonly` | `grouper_readonly` | Search, find, get, has_member |
| readwrite | `grouper.mcp.users.readwrite` | `grouper_readwrite` | Add/remove member, save, delete, assign |
| sql | `grouper.mcp.users.canRunSqlReadonly` | `grouper_sql_readonly` | sql_select, sql_get_schema |
| admin_readonly | `grouper.mcp.users.adminReadonly` | `grouper_admin_readonly` | Config search, daemon info, LDAP |
| admin_readwrite | `grouper.mcp.users.adminReadWrite` | `grouper_admin_readwrite` | Run daemon jobs |

## Tool Name Conventions

- Tool names use **snake_case**: `group_find`, `group_add_member`, `folder_delete`
- Class names use **PascalCase** with `GrouperMcp` prefix: `GrouperMcpFindGroups`, `GrouperMcpAddMember`
- The class name doesn't have to exactly mirror the tool name, but should be close
- Test class names append `Test`: `GrouperMcpFindGroupsTest`

## Delegation Pattern

Tools should delegate to existing Grouper service logic rather than implementing operations
directly. This ensures consistency with the WS layer and respects existing security checks:

```java
// Use GrouperServiceLogic for WS-equivalent operations
WsAddMemberResults wsResults = GrouperServiceLogic.addMember(
    GrouperVersion.currentVersion(),
    wsGroupLookup,
    subjectLookups,
    // ... other params
);
```

For operations not covered by `GrouperServiceLogic`, use the core Grouper API directly
(e.g., `GroupFinder`, `GroupSave`, `StemFinder`, etc.).

## Checklist

When creating or modifying an MCP tool, verify:

- [ ] Class-level javadoc describes what the tool does, privileges needed, and has `@author` tag
- [ ] All public methods have javadoc with `@param` and `@return` tags
- [ ] Inline comments explain non-obvious logic, security checks, and complex parameter lists
- [ ] Tool class has `toolDefinition()` and `execute()` static methods
- [ ] Tool name is snake_case in the definition
- [ ] Required parameters are listed in `inputSchema.required`
- [ ] Arguments are parsed defensively (null checks)
- [ ] Readwrite tools check `GrouperMcpProtectedResources` and OAuth scope
- [ ] Results use `buildSuccessResult()`/`buildErrorResult()` format
- [ ] Errors are caught and returned as error results (no thrown exceptions)
- [ ] Tool is registered in `handleToolsList()` under the correct access level
- [ ] Tool is registered in `dispatchToolCall()` with matching access check
- [ ] Tool name added to `GrouperMcpToolLog.getToolCategory()` under correct category
- [ ] Test class extends `GrouperTest` with proper setUp/tearDown
- [ ] Test class is added to `AllMcpTests.suite()`
- [ ] Wiki user guide is updated with tool documentation (table of contents AND tool section)
- [ ] Configuration properties added to `grouper.base.properties` if needed
