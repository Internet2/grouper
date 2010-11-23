/*
 * @author mchyzer
 * $Id: GrouperUtilTest.java,v 1.16 2009-11-09 03:12:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.helper.SessionHelper;


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
    TestRunner.run(new GrouperUtilTest("testMail"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }
 
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUtilTest.class);

  /**
   * 
   */
  public void testAppendIfNotBlankString() {
    
  }

  /**
   * 
   */
  public void testMail() {
    
    String testEmailAddress = GrouperConfig.getProperty("mail.test.address");
    
    new GrouperEmail().setBody("test body").setSubject("test subject").setTo(testEmailAddress).send();
    
  }

  /**
   * test log next exception
   */
  public void testLogNextException() {
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever", new Throwable()), 100);
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever"), 100);
    SQLException sqlException = new SQLException("whatever", new Throwable());
    SQLException nextException = new SQLException("THE NEXT EXCEPTION");
    sqlException.setNextException(nextException);
    GrouperUtil.logErrorNextException(LOG, new Throwable("whatever", sqlException), 100);
  }
  
  /**
   * yyyy/mm/dd
   * yyyy-mm-dd
   * dd-mon-yyyy
   * yyyy/mm/dd hh:mm:ss
   * dd-mon-yyyy hh:mm:ss
   * yyyy/mm/dd hh:mm:ss.SSS
   * dd-mon-yyyy hh:mm:ss.SSS
   */
  public void testStringToDate2() {
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("2001/02/03"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("2001-2-3"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("03-Feb-2001"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 00:00:00.000"), GrouperUtil.stringToDate2("3-FEB-2001"));

    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.000"), GrouperUtil.stringToDate2("2001-2-3  4:05_06"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.000"), GrouperUtil.stringToDate2("3_feb, 2001 04 5 6"));

    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.007"), GrouperUtil.stringToDate2("2001-2-3  4:05_06.7"));
    assertEquals(GrouperUtil.dateValue("2001/02/03 04:05:06.007"), GrouperUtil.stringToDate2("3_feb, 2001 04 5 6.007"));
  }
  
  
  /**
   * 
   */
  public void testMatchSqlString() {
    //  * e.g. if the input is a:b:%, and the input is: a:b:test:that, then it returns true
    assertTrue(GrouperUtil.matchSqlString("a:b:%", "a:b:test:that"));
    assertFalse(GrouperUtil.matchSqlString("a:b:%", "a:c:test:that"));
    assertTrue(GrouperUtil.matchSqlString("a_b:%", "a:b:test:that"));
    assertTrue(GrouperUtil.matchSqlString("a%b:%", "aasdfasdfb:test:that"));
  }

  
  /**
   * see that properties can be retrieved from url
   * note, this is called atestUrlProperties since the URL might not be setup
   */
  public void testUrlProperties() {
    
    //put test.properties at a url with these contents:
    //test=testProp
    //test2=hey<b> < what
    
    String url = "https://medley.isc-seo.upenn.edu/docroot/misc/grouperUnit.properties";
    //different url in cache, but same principal
    String url2 = url + "?";
    
    Properties properties = null;
    
    int propertiesFromUrlHttpCount = GrouperUtil.propertiesFromUrlHttpCount;
    int propertiesFromUrlFailsafeGetCount = GrouperUtil.propertiesFromUrlFailsafeGetCount;
    
    properties = GrouperUtil.propertiesFromUrl(url, false, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+1, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //go again, no cache:
    properties = GrouperUtil.propertiesFromUrl(url, false, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+2, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //lets cache
    properties = GrouperUtil.propertiesFromUrl(url, true, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+3, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //should get from cache
    properties = GrouperUtil.propertiesFromUrl(url, true, false, null);
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> < what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+3, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //transform
    properties = GrouperUtil.propertiesFromUrl(url, false, false, new GrouperHtmlFilter());
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+4, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //failsafe
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());
    
    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));
    
    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    GrouperUtil.propertiesFromUrlFailForTest = true;
    
    //try a failure when not caching
    try {
      properties = GrouperUtil.propertiesFromUrl(url, false, false, new GrouperHtmlFilter());
      fail();
    } catch (Exception e) {
      //good
    }
    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount, GrouperUtil.propertiesFromUrlFailsafeGetCount);
    
    //try a failure when caching (second level)
    GrouperUtil.propertiesFromUrlFailForTest = true;
    GrouperUtil.propertiesFromUrlCache.clear();
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());

    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));

    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount+1, GrouperUtil.propertiesFromUrlFailsafeGetCount);

    //should be in cache
    properties = GrouperUtil.propertiesFromUrl(url2, true, true, new GrouperHtmlFilter());

    assertEquals("testProp", properties.get("test"));
    assertEquals("hey<b> &lt; what", properties.get("test2"));

    assertEquals(propertiesFromUrlHttpCount+5, GrouperUtil.propertiesFromUrlHttpCount);
    assertEquals(propertiesFromUrlFailsafeGetCount+1, GrouperUtil.propertiesFromUrlFailsafeGetCount);
    
    
    
  }
  
  /**
   * 
   */
  public void testFormatNumberWithCommas() {
    assertEquals("123", GrouperUtil.formatNumberWithCommas(123L));
    assertEquals("1,234", GrouperUtil.formatNumberWithCommas(1234L));
    assertEquals("12,345", GrouperUtil.formatNumberWithCommas(12345L));
    assertEquals("123,456", GrouperUtil.formatNumberWithCommas(123456L));
    assertEquals("1,234,567", GrouperUtil.formatNumberWithCommas(1234567L));
    assertEquals("123,456,789,123,456,789", GrouperUtil.formatNumberWithCommas(123456789123456789L));

  }
  
  /**
   * test encrypt sha
   */
  public void testEncryptSha() {
    assertEquals("9yAXSF+/ZCNJm6+bJA2qFPXwlaE=", GrouperUtil.encryptSha("This is a string"));
  }
  
  /**
   * 
   */
  public void testParentStemNames() {
    Set<String> stemNames = GrouperUtil.findParentStemNames("a:b:c:d");
    assertEquals(4, stemNames.size());
    assertTrue(stemNames.contains(":"));
    assertTrue(stemNames.contains("a"));
    assertTrue(stemNames.contains("a:b"));
    assertTrue(stemNames.contains("a:b:c"));
    
  }

  /**
   * 
   */
  public void testParentStemNames2() {
    
    Group group1 = new Group();
    group1.setNameDb("a:b:c:d");
    Group group2 = new Group();
    group2.setNameDb("a:d:r");
    
    Set<String> stemNames = GrouperUtil.findParentStemNames(GrouperUtil.toSet(group1, group2));
    assertEquals(5, stemNames.size());
    assertTrue(stemNames.contains(":"));
    assertTrue(stemNames.contains("a"));
    assertTrue(stemNames.contains("a:b"));
    assertTrue(stemNames.contains("a:b:c"));
    assertTrue(stemNames.contains("a:d"));
    
  }
  
  /**
   * 
   */
  public void splitTrim() {
    String string = "a:b :::: b:c";
    String[] stringArray = GrouperUtil.splitTrim(string, "::::");
    assertEquals(2, stringArray.length);
    assertEquals("a:b", stringArray[0]);
    assertEquals("b:c", stringArray[1]);
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
    GrouperUtil.assignField(groupFrom, Group.FIELD_CREATOR_UUID, "abc");
    
    Group groupTo = new Group();
    GrouperUtil.cloneFields(groupFrom, groupTo, 
        GrouperUtil.toSet(Group.FIELD_CREATOR_UUID, Group.FIELD_CREATE_TIME));
    
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

    indented = GrouperUtil.indent("<a><!-- comment --><!-- another comment --><b whatever=\"whatever\"><!-- hey --><c>hey</c><d><e>there</e><f /><g / ><h></h></d></b></a>", true);
    expected = "<a>\n  <!-- comment -->\n  <!-- another comment -->\n  <b whatever=\"whatever\">\n    <!-- hey -->\n    <c>hey</c>\n    <d>\n      <e>there</e>\n      <f />\n      <g / >\n      <h></h>\n    </d>\n  </b>\n</a>";
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
 
  /**
   * 
   * @param group
   * @return string
   */
  public static String transformGroup(Group group) {
    return "hey: " + group.getNameDb();
  }
  
  /**
   * 
   */
  public void testSubstituteExpressionLanguage() {
    Group group = new Group();
    group.setNameDb("someName");
    Map<String, Object> substituteMap = new HashMap<String, Object>();
    substituteMap.put("group", group);
    String nameDb = null;
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${group.nameDb} ${group.nameDb}", substituteMap);
    assertEquals("someName someName", nameDb);
    
    try {
      nameDb = GrouperUtil.substituteExpressionLanguage("${java.lang.System.currentTimeMillis()}", substituteMap);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    nameDb = GrouperUtil.substituteExpressionLanguage("${java.lang.System.currentTimeMillis()}", substituteMap, true);
    assertTrue(Long.parseLong(nameDb) > 0);
    nameDb = GrouperUtil.substituteExpressionLanguage("${edu.internet2.middleware.grouper.util.GrouperUtilTest.transformGroup(group)}", substituteMap, true);
    assertEquals("hey: someName", nameDb);

    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { 'hello'; } } ${group.nameDb}", substituteMap);
    assertEquals("hello someName", nameDb);
    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { if (true){ 'hello'; }}} ${group.nameDb}", substituteMap);
    assertEquals("hello someName", nameDb);
    nameDb = GrouperUtil.substituteExpressionLanguage("${if (true) { if (true){ 'hello'; }}} ${if (true) { if (true){ 'hello'; }}} ${group.nameDb}", substituteMap);
    assertEquals("hello hello someName", nameDb);
  }
  
}
