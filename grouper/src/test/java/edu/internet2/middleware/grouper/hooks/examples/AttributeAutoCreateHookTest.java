package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
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

    AttributeDefName grouperGracePeriodMarker = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperGracePeriodDays = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperGracePeriodGroupName = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperGracePeriodIncludeEligible = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    AttributeAssignResult attributeAssignResult = group2daySource.getAttributeDelegate().assignAttribute(grouperGracePeriodMarker);
    
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodDays));
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodGroupName));
    assertTrue(attributeAssignResult.getAttributeAssign().getAttributeDelegate().hasAttribute(grouperGracePeriodIncludeEligible));

  }

}
