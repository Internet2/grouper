/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    String provisionTarget = "ad";
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //HibernateSession.bySqlStatic().listSelect(String.class, "SELECT DISTINCT gaaa.value_string FROM grouper_attribute_assign_value gaaa, grouper_attribute_assign gaa, grouper_attribute_def_name gadn WHERE gaaa.attribute_assign_id = gaa.id AND gaa.attribute_def_name_id = gadn.id AND gadn.extension IN ('provision_to', 'do_not_provision_to')", null, null);
    Set stemsToProvisionToSet = HibernateSession.byHqlStatic().createQuery("select s from Stem s, AttributeAssign aa, AttributeDefName adn, AttributeAssignValue aav where s.id = aa.ownerStemId and aav.attributeAssignId = aa.id and aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'stem' and aa.enabledDb = 'T' and adn.extensionDb = 'provision_to' and aav.valueString = '" + provisionTarget + "'").listSet(Stem.class);
    for (Object stemObject : stemsToProvisionToSet) { Stem stem = (Stem)stemObject; System.out.println("provision_to assigned to stem: " + stem.getName());  }
    Set stemsToNotProvisionToSet = HibernateSession.byHqlStatic().createQuery("select s from Stem s, AttributeAssign aa, AttributeDefName adn, AttributeAssignValue aav where s.id = aa.ownerStemId and aav.attributeAssignId = aa.id and aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'stem' and aa.enabledDb = 'T' and adn.extensionDb = 'do_not_provision_to' and aav.valueString = '" + provisionTarget + "'").listSet(Stem.class);
    for (Object stemObject : stemsToNotProvisionToSet) { Stem stem = (Stem)stemObject; System.out.println("do_not_provision_to assigned to stem: " + stem.getName());  }
    Set groupsToProvisionToSet = HibernateSession.byHqlStatic().createQuery("select g from Group g, AttributeAssign aa, AttributeDefName adn, AttributeAssignValue aav where g.id = aa.ownerGroupId and aav.attributeAssignId = aa.id and aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'group' and aa.enabledDb = 'T' and adn.extensionDb = 'provision_to' and aav.valueString = '" + provisionTarget + "'").listSet(Stem.class);
    for (Object groupObject : groupsToProvisionToSet) { Group group = (Group)groupObject; System.out.println("provision_to assigned to group: " + group.getName());  }
    Set groupsToNotProvisionToSet = HibernateSession.byHqlStatic().createQuery("select g from Group g, AttributeAssign aa, AttributeDefName adn, AttributeAssignValue aav where g.id = aa.ownerGroupId and aav.attributeAssignId = aa.id and aa.attributeDefNameId = adn.id and aa.attributeAssignTypeDb = 'group' and aa.enabledDb = 'T' and adn.extensionDb = 'do_not_provision_to' and aav.valueString = '" + provisionTarget + "'").listSet(Stem.class);
    for (Object groupObject : groupsToNotProvisionToSet) { Group group = (Group)groupObject; System.out.println("do_not_provision_to assigned to group: " + group.getName());  }
    Set allGroups = new LinkedHashSet();
    Set allGroupsToProvision = new TreeSet();
    allGroupsToProvision.addAll(groupsToProvisionToSet);

    Set stemNamesToNotProvisionTo = new HashSet();
    Set stemNamesToProvisionTo = new HashSet();
    
    for (Object stemToProvision : stemsToProvisionToSet) { stemNamesToProvisionTo.add(((Stem)stemToProvision).getName()); }
    for (Object stemNotToProvision : stemsToNotProvisionToSet) { stemNamesToNotProvisionTo.add(((Stem)stemNotToProvision).getName()); }

    // go through stems to provision
    for (Object stemToProvision : stemsToProvisionToSet) { allGroups.addAll(((Stem)stemToProvision).getChildGroups(edu.internet2.middleware.grouper.Stem.Scope.SUB)); }
    
    // go through all groups
    Map groupToPaths = new HashMap();
    for (Object groupObject : allGroups) { Group group = (Group)groupObject; if (allGroupsToProvision.contains(group)) {continue;} if (groupsToNotProvisionToSet.contains(group)) {continue;} List paths = new ArrayList(); groupToPaths.put(group, paths); String currentName = group.getName(); paths.add(currentName);  while(true) { currentName = GrouperUtil.parentStemNameFromName(currentName);  if (GrouperUtil.isBlank(currentName)) {break;} paths.add(currentName);  }   }
    
    //go through all group paths
    for (Object groupObject : groupToPaths.keySet()) {Group group = (Group)groupObject; List paths = (List)groupToPaths.get(group); for (Object pathObject : paths) { String path = (String)pathObject; if (stemNamesToProvisionTo.contains(path)) { allGroupsToProvision.add(group); break; } if (stemNamesToNotProvisionTo.contains(path)) { break; } } }
    
    for (Object groupObject : allGroupsToProvision) { Group group = (Group)groupObject; System.out.println("configured to provision to: " + provisionTarget + ": " + group.getName()); }
    
//    GrouperSession grouperSession = GrouperSession.startRootSession();
//
//    long gshTotalObjectCount = 0L;
//    long gshTotalChangeCount = 0L;
//    long gshTotalErrorCount = 0L;
//    System.out.println("Hello0");
//    Set attributeAssignIdsAlreadyUsed = new HashSet();
//    boolean problemWithAttributeAssign = false;
//    AttributeAssignSave attributeAssignSave = new AttributeAssignSave(grouperSession);
//    attributeAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
//    attributeAssignSave.assignAttributeAssignType(AttributeAssignType.imm_mem);
//    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(
//        "etc:attribute:userData:grouperUserData", false);
//    if (attributeDefName == null) {
//      gshTotalErrorCount++;
//      System.out
//          .println("Error: cant find attributeDefName: etc:attribute:userData:grouperUserData");
//      problemWithAttributeAssign = true;
//    }
//    attributeAssignSave.assignAttributeDefName(attributeDefName);
//    attributeAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
//    Group ownerGroup = GroupFinder.findByName(grouperSession,
//        "etc:grouperUi:grouperUiUserData", false);
//    if (ownerGroup == null) {
//      gshTotalErrorCount++;
//      System.out.println("Error: cant find group: etc:grouperUi:grouperUiUserData");
//      problemWithAttributeAssign = true;
//    }
//    attributeAssignSave.assignOwnerGroup(ownerGroup);
//    Subject ownerSubject = SubjectFinder.findByIdAndSource(
//        "5557e047c3214621819bd21c3b91d763", "grouperExternal", false);
//    if (ownerSubject == null) {
//      gshTotalErrorCount++;
//      System.out
//          .println("Error: cant find subject: grouperExternal: 5557e047c3214621819bd21c3b91d763");
//      problemWithAttributeAssign = true;
//    }
//    if (ownerSubject != null) {
//      Member ownerMember = MemberFinder.findBySubject(grouperSession, ownerSubject, true);
//      attributeAssignSave.assignOwnerMember(ownerMember);
//    }
//    AttributeAssignSave attributeAssignOnAssignSave = new AttributeAssignSave(
//        grouperSession);
//    attributeAssignOnAssignSave
//        .assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed);
//    attributeAssignOnAssignSave
//        .assignAttributeAssignType(AttributeAssignType.imm_mem_asgn);
//    attributeDefName = AttributeDefNameFinder.findByName(
//        "etc:attribute:userData:grouperUserDataRecentGroups", false);
//    if (attributeDefName == null) {
//      gshTotalErrorCount++;
//      System.out
//          .println("Error: cant find attributeDefName: etc:attribute:userData:grouperUserDataRecentGroups");
//      problemWithAttributeAssign = true;
//    }
//    attributeAssignOnAssignSave.assignAttributeDefName(attributeDefName);
//    attributeAssignOnAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
//    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
//    attributeAssignValue
//        .setValueString("{&quot;list&quot;:[{&quot;timestamp&quot;:1453992092662,&quot;uuid&quot;:&quot;01d1ec77e56f4f63933b95887d67de30&quot;},{&quot;timestamp&quot;:1453991965490,&quot;uuid&quot;:&quot;8a860d07dadb46b5bcc65544a6e89350&quot;},{&quot;timestamp&quot;:1447190902786,&quot;uuid&quot;:&quot;9d25e467-b127-4229-880b-ba65d0506c02&quot;}]}");
//    attributeAssignOnAssignSave.addAttributeAssignValue(attributeAssignValue);
//    attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave);
//    gshTotalObjectCount += 3;
//    if (!problemWithAttributeAssign) {
//      AttributeAssign attributeAssign = attributeAssignSave.save();
//      if (attributeAssignSave.getChangesCount() > 0) {
//        gshTotalChangeCount += attributeAssignSave.getChangesCount();
//        System.out.println("Made " + attributeAssignSave.getChangesCount()
//            + " changes for attribute assign: " + attributeAssign.toString());
//      }
//    }

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
