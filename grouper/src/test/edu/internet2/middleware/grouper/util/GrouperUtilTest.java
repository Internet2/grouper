/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.11.2.2 2009-10-18 15:50:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;


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
    TestRunner.run(new GrouperUtilTest("testStringSuffix"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }
  
  /**
   * test strip suffix
   */
  public void testStringSuffix() {
    assertEquals("whatever", GrouperUtil.stripSuffix("whatever", "what"));
    assertEquals("whatever", GrouperUtil.stripSuffix("whatever", "eve"));
    assertEquals(null, GrouperUtil.stripSuffix(null, null));
    assertEquals("a", GrouperUtil.stripSuffix("a", null));
    assertEquals(null, GrouperUtil.stripSuffix(null, "a"));
    assertEquals("what", GrouperUtil.stripSuffix("whatever", "ever"));
  }
  
  /**
   * 
   */
  public void testConvertMillisToFriendlyString() {
    assertEquals("30ms", GrouperUtil.convertMillisToFriendlyString(30L));
    assertEquals("4s, 30ms", GrouperUtil.convertMillisToFriendlyString(4030L));
    assertEquals("5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(304030L));
    assertEquals("6h, 5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(21904030L));
    assertEquals("7d, 6h, 5m, 4s, 30ms", GrouperUtil.convertMillisToFriendlyString(626704030L));
  }
  
  /**
   * 
   */
  public void testFixHibernateConnectionUrl() {
    Properties properties = new Properties();
    String hibKey = "hibernate.connection.url";
    properties.put(hibKey, "jdbc:hsqldb:file:whatever");
    String oldGrouperHome = GrouperUtil.grouperHome;
    try {
      GrouperUtil.grouperHome = "/something";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something" + File.separator + "whatever", properties.get(hibKey));
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something" + File.separator + "whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:file:whatever");
      GrouperUtil.grouperHome = "/something/";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/something/whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:file:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:/whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:file:\\whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:\\whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:file:c:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:file:c:/whatever", properties.get(hibKey));
      
  
      //########################
      //try without file
      properties.put(hibKey, "jdbc:hsqldb:whatever");
      GrouperUtil.grouperHome = "/something";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something" + File.separator + "whatever", properties.get(hibKey));
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something" + File.separator + "whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:whatever");
      GrouperUtil.grouperHome = "/something/";
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/something/whatever", properties.get(hibKey));
  
      properties.put(hibKey, "jdbc:hsqldb:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:/whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:\\whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:\\whatever", properties.get(hibKey));
      
      properties.put(hibKey, "jdbc:hsqldb:c:/whatever");
      GrouperUtil.fixHibernateConnectionUrl(properties);
      assertEquals("jdbc:hsqldb:c:/whatever", properties.get(hibKey));
    } finally {
      GrouperUtil.grouperHome = oldGrouperHome;
    }
  }
  
  /**
   * test utility method
   */
  public void testArgAfter() {
    assertEquals("arg1", GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg0"));
    assertEquals("arg1", GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1", "b"}, "arg0"));
    assertNull(GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg1"));
    try {
      GrouperUtil.argAfter(new String[]{"a", "arg0", "arg1"}, "arg2");
      fail("Should throw exception");
    } catch (Exception e) {
      //good
    }
  }
  
  /**
   * 
   * @throws AttributeNotFoundException
   */
  public void testCopyObjectFields() throws AttributeNotFoundException {
    SessionHelper.getRootSession();

    Group groupFrom = new Group();
    Map attributes = new HashMap();
    attributes.put("a", "b");
    GrouperUtil.assignField(groupFrom, Group.FIELD_ATTRIBUTES, attributes);
    GrouperUtil.assignField(groupFrom, Group.FIELD_CREATOR_UUID, "abc");
    
    Group groupTo = new Group();
    GrouperUtil.cloneFields(groupFrom, groupTo, 
        GrouperUtil.toSet(Group.FIELD_ATTRIBUTES, Group.FIELD_CREATOR_UUID, Group.FIELD_CREATE_TIME));
    
    assertEquals("b", (String)groupTo.getAttributesDb().get("a"));
    assertEquals("abc", groupTo.getCreatorUuid());
    assertEquals(0, groupTo.getCreateTimeLong());
    
  }
  
  /**
   * see if testing for equal maps work
   */
  public void testMapEquals() {
    
    Map<String, String> first = null;
    Map<String, String> second = null;
    
    assertTrue("nulls should be equal", GrouperUtil.mapEquals(first, second));
    
    first = new HashMap<String, String>();
    
    assertTrue("null is equal to empty", GrouperUtil.mapEquals(first, second));
    
    first.put("key1", "value1");
    
    assertFalse("null is not empty to map with size", GrouperUtil.mapEquals(first, second));
    assertTrue("map is equal to self", GrouperUtil.mapEquals(first, first));
    
    second = new HashMap<String, String>();
    second.put("key1", "value2");
    
    assertFalse("not equal if same size, different values", GrouperUtil.mapEquals(first, second));
    
    second.put("key1", "value1");
    
    assertTrue("equal if same size, same keys/values", GrouperUtil.mapEquals(first, second));
    
    first.put("key2", "value2");
    assertFalse("not equal if different size", GrouperUtil.mapEquals(first, second));
    assertFalse("not equal if different size", GrouperUtil.mapEquals(second, first));
    
    second.put("key2", "value2");
    assertTrue("equal if same size, same keys/values", GrouperUtil.mapEquals(first, second));
    
    second.put("key3", "value2");
    second.remove("key2");
    assertFalse("not equal if same size, different keys", GrouperUtil.mapEquals(first, second));
    assertFalse("not equal if same size, different keys(reversed)", GrouperUtil.mapEquals(second, first));
  }
  
  /**
   * see if testing for equal maps work
   */
  public void testMapDifferences() {
    
    Map<String, String> first = null;
    Map<String, String> second = null;
    Set<String> differences = new LinkedHashSet<String>();
    
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");

    assertTrue("nulls should be equal", differences.size() == 0);
    
    first = new LinkedHashMap<String, String>();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    
    assertEquals("null is equal to empty", 0, differences.size());
    
    first.put("key1", "value1");
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    
    assertEquals("First should have a size of 1", 1, first.size());
    
    assertEquals("null is not equal to map with size", 1, differences.size());
    assertEquals("null is not equal to map with size", (String)differences.toArray()[0],"attribute__key1");

    differences.clear();
    //try reversed
    GrouperUtil.mapDifferences(second, first, differences, "attribute__");

    assertEquals("null is not equal to map with size(reversed)", 1, differences.size());
    assertEquals("null is not equal to map with size(reversed)", (String)differences.toArray()[0],"attribute__key1");
    
    differences.clear();
    GrouperUtil.mapDifferences(first, first, differences, "attribute__");
    assertEquals("map equal to self", 0, differences.size());
    
    GrouperUtil.mapDifferences(first, new HashMap<String, String>(first), differences, "attribute__");
    assertEquals("map equal to clone self", 0, differences.size());
    
    second = new LinkedHashMap<String, String>();
    second.put("key1", "value2");
    
    GrouperUtil.mapDifferences(first, second, differences, null);
    
    assertEquals("Second should still have size 1", 1, second.size());
    
    assertEquals("not equal if same size, different values", 1, differences.size());
    assertEquals("not equal if same size, different values", "key1", (String)differences.toArray()[0]);
    
    second.put("key1", "value1");
    differences.clear();
    GrouperUtil.mapDifferences(first, first, differences, "attribute__");
    
    assertEquals("equal if same size, same keys/values", 0, differences.size());
    
    first.put("key2", "value2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if different size", 1, differences.size());
    assertEquals("not equal if different size", "attribute__key2", (String)differences.toArray()[0]);
    
    second.put("key2", "value2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("equal if same size, same keys/values", 0, differences.size());
    
    second.put("key3", "value2");
    second.remove("key2");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if same size, different keys", 2, differences.size());
    assertEquals("not equal if same size, different keys", "attribute__key2", (String)differences.toArray()[0]);
    assertEquals("not equal if same size, different keys", "attribute__key3", (String)differences.toArray()[1]);

    first.put("key4", "value4");
    differences.clear();
    GrouperUtil.mapDifferences(first, second, differences, "attribute__");
    assertEquals("not equal if different size, different keys", 3, differences.size());
    assertEquals("not equal if same size, different keys.  first map keys first", "attribute__key2", (String)differences.toArray()[0]);
    assertEquals("not equal if same size, different keys.  first map keys first", "attribute__key4", (String)differences.toArray()[1]);
    assertEquals("not equal if same size, different keys.  missing from first map keys second", "attribute__key3", (String)differences.toArray()[2]);
    
  }
  
  /**
   * make sure exceptions inject
   */
  public void testInjectException() {
    Exception e = new Exception();
    GrouperUtil.injectInException(e, "hey");
    assertEquals("hey", e.getMessage());
    GrouperUtil.injectInException(e, "there");
    assertTrue(e.getMessage().contains("hey"));
    assertTrue(e.getMessage().contains("there"));
    
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
