/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;


/**
 * test rule definitions
 * @author mchyzer
 *
 */
public class RuleHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleHookTest("testHook"));
  }
  
  /**
   * 
   * @param name
   */
  public RuleHookTest(String name) {
    super(name);
  }

  /**
   * test hook
   */
  public void testHook() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave(grouperSession).assignName("test:testGroup")
      .assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:testGroup2")
      .assignCreateParentStemsIfNotExist(true).save();
    AttributeAssign attributeAssign = group.getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "jdbc");
    String validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertTrue(validReason, !"T".equals(validReason));
    
    //lets make it valid
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectTestHelper.SUBJ0_ID);
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), group2.getName());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.test.name());
    
    validReason = attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
    
    assertTrue(!StringUtils.isBlank(validReason));
    assertEquals("T", validReason);
    
    
  }
}
