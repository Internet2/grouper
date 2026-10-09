/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * every tool says how its result is bounded, and the ones which are not bounded are a known list.
 * no database needed
 */
public class GrouperToolResultBoundTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperToolResultBoundTest.class);
  }

  /**
   * @param name
   */
  public GrouperToolResultBoundTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @return every registered tool, advertised or not
   */
  private static List<GrouperTool> allTools() {
    List<GrouperTool> result = new ArrayList<GrouperTool>(GrouperToolRegistry.advertisedTools());
    // kept working for older clients but not advertised
    result.add(GrouperToolRegistry.find("sql_select_count"));
    return result;
  }

  /**
   * every tool declares a bound.  the interface has no default, so this mostly catches a tool
   * returning null
   */
  public void testEveryToolDeclaresResultBound() {
    for (GrouperTool grouperTool : allTools()) {
      assertNotNull(grouperTool.name(), grouperTool.resultBound());
    }
  }

  /**
   * the tools which can return as much as the data has.  adding one should be a decision, made
   * with a comment on the tool saying why it cannot be bounded, so a new one fails here until it
   * is added to this list
   */
  public void testNotBoundedToolsAreKnown() {

    Set<String> notBounded = new TreeSet<String>();
    for (GrouperTool grouperTool : allTools()) {
      if (grouperTool.resultBound() == GrouperToolResultBound.notBounded) {
        notBounded.add(grouperTool.name());
      }
    }

    Set<String> expected = new TreeSet<String>();
    expected.add("attribute_assignment_get");
    expected.add("privilege_get");
    expected.add("sql_get_schema");

    assertEquals(expected, notBounded);
  }

  /**
   * a tool which says it pages says how, so the executor can cap it, and a tool which pages says so
   */
  public void testPagedToolsDeclarePageSize() {

    // folder_find pages only the searches which can match many folders
    ObjectNode folderFindArguments = objectMapper.createObjectNode();
    folderFindArguments.put("stemQueryFilterType", "FIND_BY_PARENT_STEM_NAME");

    for (GrouperTool grouperTool : allTools()) {
      JsonNode arguments = "folder_find".equals(grouperTool.name())
          ? folderFindArguments : objectMapper.createObjectNode();
      boolean paged = grouperTool.resultBound() == GrouperToolResultBound.paged;
      Integer pageSizeWhenNotGiven = grouperTool.pageSizeWhenNotGiven(arguments);
      assertEquals(grouperTool.name(), paged, pageSizeWhenNotGiven != null);
    }

    // and folder_find does not page an exact name lookup
    ObjectNode exactArguments = objectMapper.createObjectNode();
    exactArguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME");
    assertNull(GrouperToolRegistry.find("folder_find").pageSizeWhenNotGiven(exactArguments));
  }

}
