/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * the one list of what Grouper can be asked to do.
 *
 * <p>this replaces a switch in the MCP servlet which mapped a name to a class, and a second
 * switch elsewhere which mapped a name to a category.  a front door now looks a tool up here,
 * which means a tool can be run from plain Java with no servlet anywhere in the call stack, and
 * means the UI and MCP cannot drift apart on what a tool is or what it is allowed to do.</p>
 */
public class GrouperToolRegistry {

  /** tools by name, in registration order so that a tool list reads sensibly */
  private static final Map<String, GrouperTool> tools = new LinkedHashMap<String, GrouperTool>();

  /**
   * add a tool.  registering the same name twice is a mistake worth hearing about rather than
   * something to resolve silently.  only for GrouperToolRegistration: readers do not lock, so
   * adding a tool once requests are being served is not safe
   * @param grouperTool the tool
   */
  static synchronized void register(GrouperTool grouperTool) {

    String name = grouperTool.name();

    // GrouperMcpToolNames is the list of names Grouper advertises, so a name kept working only
    // for older clients is deliberately not in it
    if (grouperTool.advertised() && !GrouperMcpToolNames.isToolName(name)) {
      throw new RuntimeException("Tool '" + name + "' is not in GrouperMcpToolNames, add it there "
          + "so that every part of Grouper agrees on what tools exist");
    }

    GrouperTool existing = tools.get(name);
    if (existing != null) {
      throw new RuntimeException("Tool '" + name + "' is already registered by "
          + existing.getClass().getName());
    }

    tools.put(name, grouperTool);
  }

  /**
   * @param name the tool name
   * @return the tool, or null if there is no tool by that name
   */
  public static GrouperTool find(String name) {
    GrouperToolRegistration.registerIfNeeded();
    return tools.get(name);
  }

  /**
   * the tools a caller should be offered, which leaves out names kept working only for older
   * clients
   * @return the tools
   */
  public static Collection<GrouperTool> advertisedTools() {

    GrouperToolRegistration.registerIfNeeded();

    Map<String, GrouperTool> result = new LinkedHashMap<String, GrouperTool>();

    for (GrouperTool grouperTool : tools.values()) {
      if (grouperTool.advertised()) {
        result.put(grouperTool.name(), grouperTool);
      }
    }

    return result.values();
  }

  /**
   * empty the registry.  only for GrouperToolRegistration, which calls it when registration fails
   * part way and resets its own flag at the same time.  calling this alone would leave the
   * registry empty for good, since registration would not run again
   */
  static synchronized void clear() {
    tools.clear();
  }

}
