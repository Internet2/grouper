/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.mcp;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * the cap on how many rows a list tool returns in one call.  no database needed
 */
public class GrouperToolExecutorPageSizeTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperToolExecutorPageSizeTest.class);
  }

  /**
   * @param name
   */
  public GrouperToolExecutorPageSizeTest(String name) {
    super(name);
  }

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * @param json JSON text
   * @return the parsed arguments
   */
  private static JsonNode json(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * a pageSize over the limit is lowered, and the caller's arguments are not changed
   */
  public void testOverLimitLowered() {
    JsonNode arguments = json("{\"groupName\":\"a:b\",\"pageSize\":50000}");
    Integer[] limitedTo = new Integer[1];

    JsonNode result = GrouperToolExecutor.applyPageSizeLimit(50, 200, arguments, limitedTo);

    assertEquals(200, result.get("pageSize").asInt());
    assertEquals("a:b", result.get("groupName").asText());
    assertEquals(Integer.valueOf(200), limitedTo[0]);
    assertEquals(50000, arguments.get("pageSize").asInt());
  }

  /**
   * a pageSize within the limit, or a missing one where the tool's own default is within it, is
   * left alone and nothing is noted
   */
  public void testWithinLimitLeftAlone() {
    Integer[] limitedTo = new Integer[1];

    JsonNode arguments = json("{\"pageSize\":100}");
    assertSame(arguments, GrouperToolExecutor.applyPageSizeLimit(50, 200, arguments, limitedTo));

    arguments = json("{\"groupName\":\"a:b\"}");
    assertSame(arguments, GrouperToolExecutor.applyPageSizeLimit(50, 200, arguments, limitedTo));

    assertNull(limitedTo[0]);
  }

  /**
   * a missing pageSize is set to the limit when the tool would otherwise return more, or
   * everything
   */
  public void testMissingSetWhenToolReturnsMore() {

    Integer[] limitedTo = new Integer[1];
    JsonNode result = GrouperToolExecutor.applyPageSizeLimit(0, 200, json("{\"subjectId\":\"x\"}"), limitedTo);
    assertEquals(200, result.get("pageSize").asInt());
    assertEquals(Integer.valueOf(200), limitedTo[0]);

    limitedTo = new Integer[1];
    result = GrouperToolExecutor.applyPageSizeLimit(500, 200, null, limitedTo);
    assertEquals(200, result.get("pageSize").asInt());
    assertEquals(Integer.valueOf(200), limitedTo[0]);
  }

  /**
   * no limit when it is 0 or less, which is the default for MCP
   */
  public void testNoLimit() {
    Integer[] limitedTo = new Integer[1];

    JsonNode arguments = json("{\"pageSize\":50000}");
    assertSame(arguments, GrouperToolExecutor.applyPageSizeLimit(50, 0, arguments, limitedTo));

    arguments = json("{\"pageSize\":0}");
    assertSame(arguments, GrouperToolExecutor.applyPageSizeLimit(50, -1, arguments, limitedTo));

    assertNull(limitedTo[0]);
  }

  /**
   * a pageSize below 1 is capped rather than passed on, since the tools do not refuse it and 0
   * reaches the database as no limit.  so is one which is not a number, or too large for an int,
   * which the tools read as 0 or less, and an explicit null, which some tools read as 0
   */
  public void testBadValuesCapped() {

    String[] badValues = new String[] {"0", "-5", "\"lots\"", "4294967296", "-4294967296", "null"};

    for (String badValue : badValues) {
      Integer[] limitedTo = new Integer[1];
      JsonNode arguments = json("{\"groupName\":\"a:b\",\"pageSize\":" + badValue + "}");

      JsonNode result = GrouperToolExecutor.applyPageSizeLimit(50, 200, arguments, limitedTo);

      assertEquals(badValue, 200, result.get("pageSize").asInt());
      assertEquals(badValue, "a:b", result.get("groupName").asText());
      assertEquals(badValue, Integer.valueOf(200), limitedTo[0]);
    }

    // a number given as text is read as the tools read it, so it is not a bad value
    Integer[] limitedTo = new Integer[1];
    JsonNode arguments = json("{\"pageSize\":\"100\"}");
    assertSame(arguments, GrouperToolExecutor.applyPageSizeLimit(50, 200, arguments, limitedTo));
    assertNull(limitedTo[0]);
  }

  /**
   * the capped note is only added when a list in the result is as long as the cap, wherever the
   * tool put it, or when the result cannot be read
   */
  public void testPossiblyFullPage() {

    // a full page, at the top or under a name
    assertTrue(GrouperToolExecutor.isPossiblyFullPage("[1,2,3]", 3));
    assertTrue(GrouperToolExecutor.isPossiblyFullPage("{\"pageSize\":3,\"groups\":[{},{},{}]}", 3));
    assertTrue(GrouperToolExecutor.isPossiblyFullPage("{\"a\":{\"rows\":[1,2,3]}}", 3));

    // short, empty, or one object
    assertFalse(GrouperToolExecutor.isPossiblyFullPage("{\"pageSize\":3,\"groups\":[{},{}]}", 3));
    assertFalse(GrouperToolExecutor.isPossiblyFullPage("{\"pageSize\":3,\"groups\":[]}", 3));
    assertFalse(GrouperToolExecutor.isPossiblyFullPage("{\"name\":\"a:b\"}", 3));
    assertFalse(GrouperToolExecutor.isPossiblyFullPage(null, 3));

    // cannot tell, so the note stays
    assertTrue(GrouperToolExecutor.isPossiblyFullPage("not json", 3));
    assertTrue(GrouperToolExecutor.isPossiblyFullPage("42", 3));
  }

}
