/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.misc.SaveResultType;

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
    AttributeDef attributeDef = AttributeDefFinder.findByName("etc:attribute:loaderLdap:grouperLoaderLdapValueDef", false);
    if (attributeDef != null) {  
      AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef)
        .assignName("etc:attribute:loaderLdap:grouperLoaderLdapType").assignCreateParentStemsIfNotExist(true)
        .assignDescription("This holds the type of job from the GrouperLoaderType enum, currently the only "
            + "valid values are LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES. Simple is a group "
            + "loaded from LDAP filter which returns subject ids or identifiers.  Group list is an LDAP "
            + "filter which returns group objects, and the group objects have a list of subjects.  Groups "
            + "from attributes is an LDAP filter that returns subjects which have a multi-valued attribute "
            + "e.g. affiliations where groups will be created based on subject who have each attribute value  ")
            .assignDisplayName("etc:attribute:loaderLdap:Grouper loader LDAP type");  
      AttributeDefName attributeDefName = attributeDefNameSave.save();  
      
      if (attributeDefNameSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
        System.out.println("Made change for attributeDefName: " + attributeDefName.getName()); 
      }   
    } else { 
      System.out.println("ERROR: cant find attributeDef: 'etc:attribute:loaderLdap:grouperLoaderLdapValueDef'"); 
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
