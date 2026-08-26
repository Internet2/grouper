# Wiki Documentation Patterns

The MCP wiki documentation lives in `grouper/temp/mcpDocs/`. These HTML files are
non-technical, user-facing documentation. They should explain what tools do and how
to use them without exposing implementation details like Java class names, method
signatures, or internal architecture.

## HTML Style

All doc files share the same CSS style block at the top. Copy it from any existing file.
Key classes: `.note` (yellow), `.warning` (red), `.info` (blue).

## mcpWikiUserGuide.html

This is the main file to update when adding a new tool.

### 1. Add to Table of Contents

Find the `<ul>` under "Available MCP tools" and add the tool link in **alphabetical order**:

```html
<li><a href="#tool-new_tool_name">new_tool_name</a></li>
```

### 2. Add Tool Section

Add an `<h3>` section for the tool, placed **alphabetically** among the existing tool
sections. Follow this template:

#### For a Readonly Tool

```html
<h3 id="tool-new_tool_name">new_tool_name</h3>

<p>[Plain-language description of what the tool does and when you'd use it.]
Requires membership in the MCP readonly or readwrite group.
For OAuth users, the <code>readonly</code> (or <code>readwrite</code>) consent scope
must also be granted.</p>

<table>
  <tr>
    <th>Parameter</th>
    <th>Type</th>
    <th>Required</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>paramName</code></td>
    <td>string</td>
    <td>Yes</td>
    <td>[What this parameter does, in plain language.]</td>
  </tr>
  <!-- more parameters... -->
</table>

<p><strong>Response:</strong> [Describe what the response contains in plain language.
Mention key fields the user will see.]</p>
```

#### For a Readwrite Tool

```html
<h3 id="tool-new_tool_name">new_tool_name</h3>

<p>[Plain-language description of what the tool does.]
Requires membership in the MCP readwrite group.
For OAuth users, the <code>readwrite</code> consent scope must also be granted.
Note: system groups and stems under the Grouper built-in objects stem (default
<code>etc</code>) are protected and cannot be modified via MCP.</p>

<table>
  <tr>
    <th>Parameter</th>
    <th>Type</th>
    <th>Required</th>
    <th>Description</th>
  </tr>
  <!-- parameters... -->
</table>

<p><strong>Response:</strong> [Describe the response.]</p>
```

#### For an Admin Tool

```html
<h3 id="tool-new_tool_name">new_tool_name</h3>

<p>[Plain-language description.]
Requires membership in the MCP admin readonly (or admin readwrite) group.
For OAuth users, the <code>admin_readonly</code> (or <code>admin_readwrite</code>)
consent scope must also be granted.</p>

<!-- parameters table and response description... -->
```

### Writing Style for Documentation

- Write for Grouper administrators and power users, not developers
- Explain what the tool does in plain terms
- Don't mention Java classes, method names, or internal implementation details
- Use `code` formatting for parameter names, group names, and configuration values
- Parameter descriptions should explain what the parameter controls, not how it's
  implemented internally
- Include practical examples like `stem1:stem2:groupName` for group name format
- For enum parameters, list all valid values with brief explanations
- Mention the required access level (readonly, readwrite, admin, etc.)
- Mention OAuth consent scope requirements
- For readwrite tools, mention that system groups/stems are protected

## mcpWikiAdminGuide.html

Update this file if the tool has configuration properties that a Grouper deployer
(system administrator) would need to set up. Add configuration documentation in
the relevant section.

## mcpWikiAdminExternalSystems.html

Update this file only if the tool integrates with external systems (LDAP directories,
external databases, etc.).

## mcpWikiOverview.html

Usually does not need updating for individual tools. Only update if the tool represents
a new category of functionality.

## mcpWikiTechnical.html

Update this file if the tool changes the MCP protocol behavior, authentication flow,
or adds new capabilities. Individual tool additions typically don't require changes here.
