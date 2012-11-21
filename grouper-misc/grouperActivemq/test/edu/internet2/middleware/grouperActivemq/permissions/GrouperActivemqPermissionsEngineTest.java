/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperActivemqPermissionsEngineTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperActivemqPermissionsEngineTest("testHasPermissionHelper"));
  }
  
  /**
   * 
   * @param name
   */
  public GrouperActivemqPermissionsEngineTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testHasPermissionHelper() {
    
    Map<String, Set<GrouperActivemqPermission>> permissions = new HashMap<String, Set<GrouperActivemqPermission>>();
    
    permissions.put("test1", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessage, "testA")));
    permissions.put("test2", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.sendMessage, "testB")));
    permissions.put("test3", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessageInherit, "testC")));
    permissions.put("test4", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.sendMessageInherit, "testD")));
    permissions.put("test5", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.createDestination, "testE")));
    permissions.put("test6", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.deleteDestination, "testF")));
    permissions.put("test7", GrouperClientUtils.toSet(new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessage, "testG"),
        new GrouperActivemqPermission(GrouperActivemqPermissionAction.receiveMessageInherit, "testH:testI")));

    // test1
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.receiveMessage, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.receiveMessage, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.receiveMessage, "testA1"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.receiveMessage, "testA:2"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.createDestination, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.deleteDestination, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.receiveMessageInherit, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.sendMessage, "testA"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test1", GrouperActivemqPermissionAction.sendMessageInherit, "testA"));

    // test2
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.sendMessage, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.sendMessage, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.sendMessage, "testB1"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.sendMessage, "testB:2"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.createDestination, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.deleteDestination, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.sendMessageInherit, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.receiveMessage, "testB"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test2", GrouperActivemqPermissionAction.receiveMessageInherit, "testB"));

    // test3
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.receiveMessage, "testC"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.receiveMessage, "testC"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.receiveMessage, "testC1"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.receiveMessage, "testC:2"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.receiveMessage, "testC:2:3"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.createDestination, "testC"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.deleteDestination, "testC"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.receiveMessageInherit, "testC"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.sendMessage, "testC"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test3", GrouperActivemqPermissionAction.sendMessageInherit, "testC"));

    // test4
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.sendMessage, "testD"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.sendMessage, "testD"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.sendMessage, "testD1"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.sendMessage, "testD:2"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.createDestination, "testD"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.deleteDestination, "testD"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.sendMessageInherit, "testD"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.receiveMessage, "testD"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test4", GrouperActivemqPermissionAction.receiveMessageInherit, "testD"));

    // test5
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.sendMessage, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.sendMessage, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.sendMessage, "testE1"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.sendMessage, "testE:2"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.createDestination, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.deleteDestination, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.sendMessageInherit, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.receiveMessage, "testE"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test5", GrouperActivemqPermissionAction.receiveMessageInherit, "testE"));


    // test6
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.sendMessage, "testF"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.sendMessage, "testF"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.sendMessage, "testF1"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.sendMessage, "testF:2"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.createDestination, "testF"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.deleteDestination, "testF"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.sendMessageInherit, "testF"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.receiveMessage, "testF"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test6", GrouperActivemqPermissionAction.receiveMessageInherit, "testF"));

    // test7
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.sendMessage, "testG"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "testQ", GrouperActivemqPermissionAction.sendMessage, "testG"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.sendMessage, "testG1"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.sendMessage, "testG:2"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.createDestination, "testG"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.deleteDestination, "testG"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.sendMessageInherit, "testG"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.receiveMessage, "testG"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.receiveMessageInherit, "testG"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.receiveMessageInherit, "testH:testI"));
    assertFalse(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.receiveMessageInherit, "testH:testI:testJ"));
    assertTrue(GrouperActivemqPermissionsEngine.hasPermissionHelper(permissions, "test7", GrouperActivemqPermissionAction.receiveMessage, "testH:testI:testJ"));

  }
    
  
}
