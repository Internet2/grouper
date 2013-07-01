/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * @author mchyzer
 *
 */
public class UserDataListTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UserDataListTest("testReplaceWithSubset"));
  }

  /**
   * 
   */
  public UserDataListTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public UserDataListTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testReplaceWithSubset() {
    
    UserDataObject userDataObject0 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis());
    UserDataObject userDataObject1 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+1);
    UserDataObject userDataObject2 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+2);
    UserDataObject userDataObject3 = new UserDataObject(userDataObject0.getUuid(), System.currentTimeMillis()+3);
    
    UserDataObject[] list = new UserDataObject[]{userDataObject0, userDataObject1, userDataObject2, userDataObject3};

    UserDataList userDataList = new UserDataList(list);

    Set<String> subset = GrouperUtil.toSet(userDataObject0.getUuid(), userDataObject1.getUuid());
    
    assertTrue(userDataList.replaceUserDataObjectsWithSubset(subset, 5));
    
    //lets check it out
    assertEquals(2, userDataList.getList().length);
    assertEquals(userDataObject0.getUuid(), userDataList.getList()[0].getUuid());
    assertEquals(userDataObject0.getTimestamp(), userDataList.getList()[0].getTimestamp());

    assertEquals(userDataObject1.getUuid(), userDataList.getList()[1].getUuid());
    assertEquals(userDataObject1.getTimestamp(), userDataList.getList()[1].getTimestamp());

    //no changes
    assertFalse(userDataList.replaceUserDataObjectsWithSubset(subset, 5));

    
    assertTrue(userDataList.replaceUserDataObjectsWithSubset(subset, 1));
    assertEquals(1, userDataList.getList().length);
    assertEquals(userDataObject0.getUuid(), userDataList.getList()[0].getUuid());
    assertEquals(userDataObject0.getTimestamp(), userDataList.getList()[0].getTimestamp());
    
  }
  
  /**
   * 
   */
  public void testJsonMarshal() {
    
    UserDataObject userDataObject0 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis());
    UserDataObject userDataObject1 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+1);
    UserDataObject userDataObject2 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+2);
    
    UserDataObject[] list = new UserDataObject[]{userDataObject0, userDataObject1, userDataObject2};

    UserDataList userDataList = new UserDataList(list);
    
    //convert to json:
    String json = userDataList.jsonConvertTo();
    
    //this should be generic and not tied to the object types
    assertFalse(json, json.toLowerCase().contains("user"));
    
    // this is 3 objects: {"list":[{"timestamp":1363829748082,"uuid":"f7994071e2cf4cb4b29b71016cb90a08"},{"timestamp":1363829748083,"uuid":"c92a4c8c4e7f473daf98c318e20f1355"},{"timestamp":1363829748084,"uuid":"63d66988e2a64ed38258f0a71486d21c"}]}
    // which is size 230.  So 30 objects can comfortably fit in 4k of space
    //System.out.println(json);
    //System.out.println(GrouperUtil.indent(json, true));
    
    //lets make sure it comes back ok
    userDataList = UserDataList.jsonMarshalFrom(json);
    
    assertEquals(3, GrouperUtil.length(userDataList.getList()));
    
    assertEquals(json, userDataObject0.getUuid(), userDataList.getList()[0].getUuid());
    assertEquals(json, userDataObject1.getUuid(), userDataList.getList()[1].getUuid());
    assertEquals(json, userDataObject1.getTimestamp(), userDataList.getList()[1].getTimestamp());
    assertEquals(json, userDataObject2.getTimestamp(), userDataList.getList()[2].getTimestamp());
    
    //make sure no extraneous data or missing data messes it up
    String string = "{\"list\":[{\"timestamp\":1363827648860,\"abcd\":\"qwert\"}]}";

    userDataList = UserDataList.jsonMarshalFrom(string);
    
    assertEquals(1, GrouperUtil.length(userDataList));
    assertEquals(new Long(1363827648860L), userDataList.getList()[0].getTimestamp());
    assertNull(userDataList.getList()[0].getUuid());

    list = new UserDataObject[20];
    userDataList = new UserDataList(list);
    
    for (int i=0;i<20;i++) {
      UserDataObject userDataObject = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis());
      list[i] = userDataObject;
    }
    
    //see how long 100 conversions takes
    //100 conversions of array of size 3 takes 180ms
    //100 conversions of array of size 20 takes 554ms
    long start = System.nanoTime();

    for (int i=0;i<100;i++) {
    
      userDataList = new UserDataList(list);

      //convert to json:
      json = userDataList.jsonConvertTo();
      
      userDataList = UserDataList.jsonMarshalFrom(json);
      
      assertEquals(20, GrouperUtil.length(userDataList.getList()));
    }
    
    //System.out.println("100 conversions there and back took: " + ((System.nanoTime()-start) / 1000000L) + "ms");
  }

}
