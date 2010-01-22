/**
 * 
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class GrouperMessageTagTest extends TestCase {

	  /**
	   * Method main.
	   * @param args String[]
	   */
	  public static void main(String[] args) {
	    TestRunner.run(GrouperMessageTagTest.class);
	  }
	  
	  /**
	   * try a substitute call where a prefix happens later in the properties
	   */
	  public void testSubstitutePrefix() {
		  Map<String, String> map = new LinkedHashMap<String, String>();
		  map.put("b", "whatever4");
		  map.put("ab", "whatever3");
		  map.put("abc", "whatever");
		  map.put("abcd", "whatever2");
		  map = GrouperMessageTag.sortMapBySubstringFirst(map);
		  Iterator<String> iterator = map.keySet().iterator();
		  String key1 = iterator.next();
		  assertEquals(key1, "abcd");
		  String key2 = iterator.next();
		  assertEquals(key2, "abc");
		  String key3 = iterator.next();
		  assertEquals(key3, "ab");
		  String key4 = iterator.next();
		  assertEquals(key4, "b");
	  }
	
}
