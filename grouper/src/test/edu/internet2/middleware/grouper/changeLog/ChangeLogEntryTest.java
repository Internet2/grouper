/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


/**
 *
 */
public class ChangeLogEntryTest extends GrouperTest {

  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ChangeLogEntryTest("testJson"));
  }

  /**
   * 
   */
  public ChangeLogEntryTest() {
    super();
    
  }

  /**
   * @param name
   */
  public ChangeLogEntryTest(String name) {
    super(name);
    
  }
  
  /**
   * to / from json
   */
  public void testJson() {
    
    ChangeLogEntry changeLogEntry = new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_ADD, 
        ChangeLogLabels.GROUP_ADD.id.name(), 
        "id", ChangeLogLabels.GROUP_ADD.name.name(), 
        "name", ChangeLogLabels.GROUP_ADD.parentStemId.name(), "parentStemId",
        ChangeLogLabels.GROUP_ADD.displayName.name(), "displayName",
        ChangeLogLabels.GROUP_ADD.description.name(), "description");
    String json = changeLogEntry.toJson(true);
    ChangeLogEntry newEntry = ChangeLogEntry.fromJsonToCollection(json).iterator().next();
    assertEquals(changeLogEntry.retrieveValueForLabel("name"), newEntry.retrieveValueForLabel("name"));
    String newJson = newEntry.toJson(true);
    assertEquals(json, newJson);
  }
  
}
