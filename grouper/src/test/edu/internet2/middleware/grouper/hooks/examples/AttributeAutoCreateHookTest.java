package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperGracePeriod;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;


public class AttributeAutoCreateHookTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new AttributeAutoCreateHookTest("testAutoAssignAttributeIds"));
  }
  
  public AttributeAutoCreateHookTest(String name) {
    super(name);
  }

  public void testAutoAssignAttributeIds() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group2daySource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2daySource").save();

    AttributeDefName grouperGracePeriodMarker = AttributeDefNameFinder.findByName(GrouperGracePeriod.gracePeriodStemName() + ":" + GrouperGracePeriod.GROUPER_GRACE_PERIOD_MARKER, true);
    AttributeDefName grouperGracePeriodDays = AttributeDefNameFinder.findByName(GrouperGracePeriod.gracePeriodStemName() + ":" + GrouperGracePeriod.GROUPER_GRACE_PERIOD_ATTR_DAYS, true);
    AttributeDefName grouperGracePeriodGroupName = AttributeDefNameFinder.findByName(GrouperGracePeriod.gracePeriodStemName() + ":" + GrouperGracePeriod.GROUPER_GRACE_PERIOD_ATTR_GROUP_NAME, true);
    AttributeDefName grouperGracePeriodIncludeEligible = AttributeDefNameFinder.findByName(GrouperGracePeriod.gracePeriodStemName() + ":" + GrouperGracePeriod.GROUPER_GRACE_PERIOD_ATTR_INCLUDE_ELIGIBLE, true);
    
    AttributeAssignResult attributeAssignResult = group2daySource.getAttributeDelegate().assignAttribute(grouperGracePeriodMarker);
    
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodDays));
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodGroupName));
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodIncludeEligible));

  }

}
