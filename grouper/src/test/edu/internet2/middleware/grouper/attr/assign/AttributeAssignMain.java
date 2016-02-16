/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.subject.Subject;

/**
 *
 */
public class AttributeAssignMain {

  /**
   * 
   */
  public AttributeAssignMain() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    //attributeAssignExample();
    GrouperSession grouperSession = GrouperSession.startRootSession();

    long gshTotalObjectCount = 0L;
    long gshTotalChangeCount = 0L;
    long gshTotalErrorCount = 0L;
    System.out.println("Hello0");
    Set attributeAssignIdsAlreadyUsed = new HashSet();
    boolean problemWithAttributeAssign = false;
    AttributeAssignSave attributeAssignSave = new AttributeAssignSave(grouperSession);
    attributeAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
    attributeAssignSave.assignAttributeAssignType(AttributeAssignType.imm_mem);
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
        "etc:attribute:userData:grouperUserData", false);
    if (attributeDefName == null) {
      gshTotalErrorCount++;
      System.out
          .println("Error: cant find attributeDefName: etc:attribute:userData:grouperUserData");
      problemWithAttributeAssign = true;
    }
    attributeAssignSave.assignAttributeDefName(attributeDefName);
    attributeAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
    Group ownerGroup = GroupFinder.findByName(grouperSession,
        "etc:grouperUi:grouperUiUserData", false);
    if (ownerGroup == null) {
      gshTotalErrorCount++;
      System.out.println("Error: cant find group: etc:grouperUi:grouperUiUserData");
      problemWithAttributeAssign = true;
    }
    attributeAssignSave.assignOwnerGroup(ownerGroup);
    Subject ownerSubject = SubjectFinder.findByIdAndSource(
        "5557e047c3214621819bd21c3b91d763", "grouperExternal", false);
    if (ownerSubject == null) {
      gshTotalErrorCount++;
      System.out
          .println("Error: cant find subject: grouperExternal: 5557e047c3214621819bd21c3b91d763");
      problemWithAttributeAssign = true;
    }
    if (ownerSubject != null) {
      Member ownerMember = MemberFinder.findBySubject(grouperSession, ownerSubject, true);
      attributeAssignSave.assignOwnerMember(ownerMember);
    }
    AttributeAssignSave attributeAssignOnAssignSave = new AttributeAssignSave(
        grouperSession);
    attributeAssignOnAssignSave
        .assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
    attributeAssignOnAssignSave
        .assignAttributeAssignType(AttributeAssignType.imm_mem_asgn);
    attributeDefName = AttributeDefNameFinder.findByName(
        "etc:attribute:userData:grouperUserDataRecentGroups", false);
    if (attributeDefName == null) {
      gshTotalErrorCount++;
      System.out
          .println("Error: cant find attributeDefName: etc:attribute:userData:grouperUserDataRecentGroups");
      problemWithAttributeAssign = true;
    }
    attributeAssignOnAssignSave.assignAttributeDefName(attributeDefName);
    attributeAssignOnAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue
        .setValueString("{&quot;list&quot;:[{&quot;timestamp&quot;:1453992092662,&quot;uuid&quot;:&quot;01d1ec77e56f4f63933b95887d67de30&quot;},{&quot;timestamp&quot;:1453991965490,&quot;uuid&quot;:&quot;8a860d07dadb46b5bcc65544a6e89350&quot;},{&quot;timestamp&quot;:1447190902786,&quot;uuid&quot;:&quot;9d25e467-b127-4229-880b-ba65d0506c02&quot;}]}");
    attributeAssignOnAssignSave.addAttributeAssignValue(attributeAssignValue);
    attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);
    gshTotalObjectCount += 3;
    if (!problemWithAttributeAssign) {
      AttributeAssign attributeAssign = attributeAssignSave.save();
      if (attributeAssignSave.getChangesCount() > 0) {
        gshTotalChangeCount += attributeAssignSave.getChangesCount();
        System.out.println("Made " + attributeAssignSave.getChangesCount()
            + " changes for attribute assign: " + attributeAssign.toString());
      }
    }

  }

  /**
   * 
   */
  private static void attributeAssignExample() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Set attributeAssignIdsAlreadyUsed = new HashSet();
    {
      boolean problemWithAttributeAssign = false;
      AttributeAssignSave attributeAssignSave = new AttributeAssignSave(grouperSession);
      attributeAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
      attributeAssignSave.assignAttributeAssignType(AttributeAssignType.group);
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
          "test:attributeDefName0", false);
      if (attributeDefName == null) {
        System.out.println("Error: cant find attributeDefName: test:attributeDefName0");
        problemWithAttributeAssign = true;
      }
      attributeAssignSave.assignAttributeDefName(attributeDefName);
      Group ownerGroup = GroupFinder.findByName(grouperSession, "test:group0", false);
      if (ownerGroup == null) {
        System.out.println("Error: cant find group: test:group0");
        problemWithAttributeAssign = true;
      }
      attributeAssignSave.assignOwnerGroup(ownerGroup);
      attributeAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
      if (!problemWithAttributeAssign) {
        AttributeAssign attributeAssign = attributeAssignSave.save();
        System.out.println("Made " + attributeAssignSave.getChangesCount() + " changes for attribute assign: " + attributeAssign.toString());
      }
    }
    {
      boolean problemWithAttributeAssign = false;
      AttributeAssignSave attributeAssignSave = new AttributeAssignSave(grouperSession);
      attributeAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
      attributeAssignSave.assignAttributeAssignType(AttributeAssignType.group);
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
          "test:attributeDefName0", false);
      if (attributeDefName == null) {
        System.out.println("Error: cant find attributeDefName: test:attributeDefName0");
        problemWithAttributeAssign = true;
      }
      attributeAssignSave.assignAttributeDefName(attributeDefName);
      Group ownerGroup = GroupFinder.findByName(grouperSession, "test:group0", false);
      if (ownerGroup == null) {
        System.out.println("Error: cant find group: test:group0");
        problemWithAttributeAssign = true;
      }
      attributeAssignSave.assignOwnerGroup(ownerGroup);
      attributeAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
      if (!problemWithAttributeAssign) {
        AttributeAssign attributeAssign = attributeAssignSave.save();
        System.out.println("Made " + attributeAssignSave.getChangesCount() + " changes for attribute assign: " + attributeAssign.toString());
      }
    }
  }

}
