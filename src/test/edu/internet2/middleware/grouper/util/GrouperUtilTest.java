/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.3 2008-03-24 20:15:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperUtilTest extends TestCase {
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperUtilTest("testIndentJson"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }
  
  /**
   * test replacing of strings
   */
  public void testReplace() {
    //here is a test case with 3 possible replacements, one will match "Groups"
    String groupsString = "<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups are many " +
    "people or groups');\">Groups</span>";
    List<String> keysList = GrouperUtil.toList("groups", "Groups", "the hierarchy");
    List<String> valuesList = GrouperUtil.toList("<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups " +
        "are many people or groups');\">groups</span>", 
        groupsString, "<span class=\"tooltip\" " +
            "onmouseover=\"grouperTooltip('A hierarchy is where each node has " +
            "zero or one parents and zero or many children  A hierarchy is where " +
            "each node has zero or one parents and zero or many children  A hierarchy " +
            "is where each node has zero or one parents and zero or many children  A " +
            "hierarchy is where each node has zero or one parents and zero or many " +
            "children ');\">the hierarchy</span>");
    String result = GrouperUtil.replace("All Groups", 
        keysList,
        valuesList, false, true);

    assertEquals(result, "All " + groupsString);
    
    //at this point the keys and values should have one fewer item
    assertEquals(2, keysList.size());
    assertEquals(2, valuesList.size());
  }

  /**
   * test indent
   */
  public void testIndent() {
    String indented = GrouperUtil.indent("<a><b whatever=\"whatever\"><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>", true);
    String expected = "<a>\n  <b whatever=\"whatever\">\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);
  }
  
  /**
   * test indent
   */
  public void testIndentDoctype() {
    String xml = "<?xml version='1.0' encoding='iso-8859-1'?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><a><b whatever=\"whatever\"><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>";
    String indented = GrouperUtil.indent(xml, true);
    String expected = "<?xml version='1.0' encoding='iso-8859-1'?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<a>\n  <b whatever=\"whatever\">\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);
  }

  /**
   * indent json
   */
  public void testIndentJson() {
    String json = "{\"a\":{\"b\\\"b\":{\"c\\\\\":\"d\"},\"e\":\"f\",\"g\":[\"h\":\"i\"]}}";
    String indented = GrouperUtil.indent(json, true);
    String expected = "{\n  \"a\":{\n    \"b\\\"b\":{\n      \"c\\\\\":\"d\"\n    },\n    \"e\":\"f\",\n    \"g\":[\n      \"h\":\"i\"\n    ]\n  }\n}";
    assertEquals("\n\nExpected:\n" + expected + "\n\nresult:\n" + indented + "\n\n", expected, indented);
  }
  
  /**
   * @param name
   */
  public GrouperUtilTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testBatching() {
    List<Integer> list = GrouperUtil.toList(0, 1, 2, 3, 4);
    assertEquals("3 batches, 0 and 1, 2 and 3, and 4", 3, GrouperUtil.batchNumberOfBatches(list, 2));
    List<Integer> listBatch = GrouperUtil.batchList(list, 2, 0);
    assertEquals(2, listBatch.size());
    assertEquals(0, (int)listBatch.get(0));
    assertEquals(1, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 1);
    assertEquals(2, listBatch.size());
    assertEquals(2, (int)listBatch.get(0));
    assertEquals(3, (int)listBatch.get(1));
    
    listBatch = GrouperUtil.batchList(list, 2, 2);
    assertEquals(1, listBatch.size());
    assertEquals(4, (int)listBatch.get(0));
  }
  
}
