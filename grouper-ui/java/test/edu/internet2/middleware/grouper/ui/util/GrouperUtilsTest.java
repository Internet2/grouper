/**
 * 
 */
package edu.internet2.middleware.grouper.ui.util;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author mchyzer
 * 
 */
public class GrouperUtilsTest extends TestCase {
	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		TestRunner.run(GrouperUtilsTest.class);
	}

	/**
	 * test replacing of strings
	 */
	public void testReplace() {
		//here is a test case with 3 possible replacements, one will match "Groups"
		String groupsString = "<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups are many " +
		"people or groups');\">Groups</span>";
		List<String> keysList = GrouperUtils.toList("groups", "Groups", "the hierarchy");
		List<String> valuesList = GrouperUtils.toList("<span class=\"tooltip\" onmouseover=\"grouperTooltip('Groups " +
				"are many people or groups');\">groups</span>", 
				groupsString, "<span class=\"tooltip\" " +
						"onmouseover=\"grouperTooltip('A hierarchy is where each node has " +
						"zero or one parents and zero or many children  A hierarchy is where " +
						"each node has zero or one parents and zero or many children  A hierarchy " +
						"is where each node has zero or one parents and zero or many children  A " +
						"hierarchy is where each node has zero or one parents and zero or many " +
						"children ');\">the hierarchy</span>");
		String result = GrouperUtils.replace("All Groups", 
				keysList,
				valuesList, false, true);

		assertEquals(result, "All " + groupsString);
		
		//at this point the keys and values should have one fewer item
		assertEquals(2, keysList.size());
		assertEquals(2, valuesList.size());
	}
}
