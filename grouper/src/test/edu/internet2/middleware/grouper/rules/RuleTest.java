/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;


/**
 *
 */
public class RuleTest extends GrouperTest {

  /**
   * 
   */
  public RuleTest() {
    super();
    
  }

  /**
   * @param name
   */
  public RuleTest(String name) {
    super(name);
    
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleTest("whatver"));
  }

  /**
   * 
   */
  public void testRuleLonghand() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").save();
    
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_ACT_AS_SUBJECT_SOURCE_ID, "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_ACT_AS_SUBJECT_ID, "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_CHECK_OWNER_NAME, "stem:b");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_CHECK_TYPE, 
        RuleCheckType.membershipRemove.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_IF_CONDITION_ENUM, 
        RuleConditionEnum.thisGroupHasImmediateMember.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.RULE_THEN_EL, 
        "${ruleUtils.removeMember(thisGroupId, memberId}");
    
    groupB.addMember(SubjectTestHelper.SUBJ0);

    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(SubjectTestHelper.SUBJ0);

    groupB.addMember(SubjectTestHelper.SUBJ0);
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    
    groupB.deleteMember(SubjectTestHelper.SUBJ0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(SubjectTestHelper.SUBJ0));
    
  }
  
}
