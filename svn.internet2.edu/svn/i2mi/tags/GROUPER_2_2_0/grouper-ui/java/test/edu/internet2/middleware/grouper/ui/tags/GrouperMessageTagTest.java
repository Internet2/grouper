/*******************************************************************************
 * Copyright 2012 Internet2
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
