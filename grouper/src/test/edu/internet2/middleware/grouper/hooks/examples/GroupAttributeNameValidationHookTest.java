/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationHookTest.java,v 1.2 2009-03-24 17:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.regex.Pattern;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.FieldHooksImpl;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class GroupAttributeNameValidationHookTest extends GrouperTest {

  /**
   * edu stem 
   */
  private Stem edu;
  /**
   */
  private GrouperSession grouperSession = null;
  /**
   * edu stem 
   */
  private GroupType groupType;
  /**
   */
  private Privilege read = AccessPrivilege.READ;
  /**
   * root stem 
   */
  private Stem root;
  /**
   */
  private Privilege write = AccessPrivilege.UPDATE;

  /**
   * @param name
   */
  public GroupAttributeNameValidationHookTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupAttributeNameValidationHookTest("testBuiltInAttributeValidator"));
    //TestRunner.run(new GroupAttributeNameValidationHookTest(""));
    //TestRunner.run(GroupAttributeNameValidationHookTest.class);
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.FIELD.getPropertyFileKey(), 
        FieldHooksImpl.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.FIELD.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();

      grouperSession     = SessionHelper.getRootSession();
      groupType = GroupType.createType(grouperSession, "testType");
      root  = StemHelper.findRootStem(grouperSession);
      edu   = StemHelper.addChildStem(root, "edu", "education");
  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testBuiltInAttributeValidator() throws Exception {
  
    this.groupType.addAttribute(grouperSession, 
        GroupAttributeNameValidationHook.TEST_ATTRIBUTE_NAME, read, write, true);
  
    Group group = StemHelper.addChildGroup(this.edu, "test1", "the test1");
  
    group.addType(this.groupType);
    
    try {
      //put this in try/catch in case storing on setters
      //add the attribute
      group.setAttribute(GroupAttributeNameValidationHook.TEST_ATTRIBUTE_NAME, "whatever");
      
      fail("Should veto this invalid name");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
    }
    
    group = StemHelper.addChildGroup(this.edu, "test2", "the test2");
  
    group.addType(this.groupType);
    
    //add the attribute
    group.setAttribute(GroupAttributeNameValidationHook.TEST_ATTRIBUTE_NAME, GroupAttributeNameValidationHook.TEST_PATTERN);
        
    //this should not veto
  
    //lets test description:
    group = StemHelper.addChildGroup(this.edu, "test1", "the test1");
  
    try {
      try {
        
        GroupAttributeNameValidationHook.attributeNamePatterns.put(Group.FIELD_DESCRIPTION, Pattern.compile("^" + GroupAttributeNameValidationHook.TEST_PATTERN + "$"));
        GroupAttributeNameValidationHook.attributeNameRegexes.put(Group.FIELD_DESCRIPTION, "^" + GroupAttributeNameValidationHook.TEST_PATTERN + "$");
        GroupAttributeNameValidationHook.attributeNameVetoMessages.put(Group.FIELD_DESCRIPTION, "Attribute description cannot have the value: '$attributeValue$'");
        
        //put this in try/catch in case storing on setters
        //add the attribute
        group.setDescription("whatever");
        
        group.store();
        fail("Should veto this invalid name");
      } catch (HookVeto hv) {
        //this is a success, it is supposed to veto  
      }
      
      group.setDescription(GroupAttributeNameValidationHook.TEST_PATTERN);
      
      group.store();
    
      //should be fine
    } finally {
      GroupAttributeNameValidationHook.attributeNamePatterns.remove(Group.FIELD_DESCRIPTION);
      GroupAttributeNameValidationHook.attributeNameRegexes.remove(Group.FIELD_DESCRIPTION);
      GroupAttributeNameValidationHook.attributeNameVetoMessages.remove(Group.FIELD_DESCRIPTION);
      
    }
  
  }

}
