/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

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
    TestRunner.run(new UserDataListTest("testJsonMarshal"));
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
  public void testJsonMarshal() {
    
    UserDataObject userDataObject0 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis());
    UserDataObject userDataObject1 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+1);
    UserDataObject userDataObject2 = new UserDataObject(GrouperUuid.getUuid(), System.currentTimeMillis()+2);
    
    UserDataObject[] list = new UserDataObject[]{userDataObject0, userDataObject1, userDataObject2};

    UserDataList userDataList = new UserDataList(list);
    
    //convert to json:
    String json = GrouperUtil.jsonConvertTo(userDataList, false);
    
    //this should be generic and not tied to the object types
    assertFalse(json, json.toLowerCase().contains("user"));
    
    // this is 3 objects: {"list":[{"theTimestamp":1363829748082,"uuid":"f7994071e2cf4cb4b29b71016cb90a08"},{"theTimestamp":1363829748083,"uuid":"c92a4c8c4e7f473daf98c318e20f1355"},{"theTimestamp":1363829748084,"uuid":"63d66988e2a64ed38258f0a71486d21c"}]}
    // which is size 230.  So 30 objects can comfortably fit in 4k of space
    //System.out.println(json);
    //System.out.println(GrouperUtil.indent(json, true));
    
    //lets make sure it comes back ok
    userDataList = GrouperUtil.jsonConvertFrom(json, UserDataList.class);
    
    assertEquals(3, GrouperUtil.length(userDataList.getList()));
    
    assertEquals(userDataObject0.getUuid(), userDataList.getList()[0].getUuid());
    assertEquals(userDataObject1.getUuid(), userDataList.getList()[1].getUuid());
    assertEquals(userDataObject1.getTheTimestamp(), userDataList.getList()[1].getTheTimestamp());
    assertEquals(userDataObject2.getTheTimestamp(), userDataList.getList()[2].getTheTimestamp());
    
    //make sure no extraneous data or missing data messes it up
    String string = "{\"list\":[{\"theTimestamp\":1363827648860,\"abcd\":\"qwert\"}]}";

    userDataList = GrouperUtil.jsonConvertFrom(string, UserDataList.class);
    
    assertEquals(1, GrouperUtil.length(userDataList));
    assertEquals(1363827648860L, userDataList.getList()[0].getTheTimestamp());
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
      json = GrouperUtil.jsonConvertTo(userDataList, false);
      
      userDataList = GrouperUtil.jsonConvertFrom(json, UserDataList.class);
      
      assertEquals(20, GrouperUtil.length(userDataList.getList()));
    }
    
    System.out.println("100 conversions there and back took: " + ((System.nanoTime()-start) / 1000000L) + "ms");
  }

}
