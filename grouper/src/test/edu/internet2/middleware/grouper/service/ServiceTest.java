package edu.internet2.middleware.grouper.service;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

/**
 * 
 * @author mchyzer
 *
 */
public class ServiceTest extends GrouperTest {

  /**
   * 
   * @param name
   */
  public ServiceTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ServiceTest("testServicePrivileges"));
  }

  /**
   * 
   */
  public void testServicePrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef jiraServiceDef = new AttributeDefSave(grouperSession)
      .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
      .assignName("apps:jira:jiraServiceDefinition").assignToAttributeDef(true)
      .assignToGroup(true).assignToStem(true).save();
    
    AttributeDefName jiraService = new AttributeDefNameSave(grouperSession, jiraServiceDef)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("apps:jira:jiraService").assignDisplayExtension("Central IT production Jira issue tracker").save();
    
    //jira group
    Group jiraGroup = new GroupSave(grouperSession)
      .assignName("apps:jira:groups:admins").assignCreateParentStemsIfNotExist(true).save();
    
    //the jira group has the jira service tag
    jiraGroup.getAttributeDelegate().assignAttribute(jiraService);
    
    AttributeDef confluenceServiceDef = new AttributeDefSave(grouperSession)
      .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
      .assignName("apps:confluence:confluenceServiceDefinition").assignToAttributeDef(true)
      .assignToGroup(true).assignToStem(true).save();
    
    AttributeDefName confluenceService = new AttributeDefNameSave(grouperSession, confluenceServiceDef)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("apps:confluence:confluenceService").assignDisplayExtension("Central IT production Confluence wiki").save();

    Group confluenceGroup = new GroupSave(grouperSession)
      .assignName("apps:confluence:groups:editors").assignCreateParentStemsIfNotExist(true).save();
    
    //the confluence folder has the confluence service tag
    Stem confluenceFolder = StemFinder.findByName(grouperSession, "apps:confluence", true);
    confluenceFolder.getAttributeDelegate().assignAttribute(confluenceService);
    
    AttributeDef directoryServiceDef = new AttributeDefSave(grouperSession)
      .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
      .assignName("apps:directory:directoryServiceDefinition").assignToAttributeDef(true)
      .assignToGroup(true).assignToStem(true).save();
    
    AttributeDefName directoryService = new AttributeDefNameSave(grouperSession, directoryServiceDef)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("apps:directory:directoryService").assignDisplayExtension("MySchool directory").save();
    
    //the directory is public
    directoryServiceDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), 
        AttributeDefPrivilege.ATTR_VIEW, false);
    
    //################
    // test subject 0 can view confluence
    confluenceServiceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // test subject 0 can see the directory and confluence services
    
    
    //################
    // test subject 1 can view jira
    jiraServiceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_VIEW, false);

    // test subject 1 can see the directory and jira services
    
    //################
    // test subject 2 can view confluence and jira
    confluenceServiceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_VIEW, false);
    jiraServiceDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_VIEW, false);
    
    // test subject 2 can see the directory, confluence, and jira services
    
    //################
    // test subject 3 can not view either confluence or jira
    
    // test subject 3 can see the directory service
    
    //################
    // test subject 4 can admin a confluence group
    confluenceGroup.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN);
    
    // test subject 4 can see the directory and confluence services
    
    //################
    // test subject 5 can update a jira group
    jiraGroup.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    
    // test subject 5 can see the directory and jira services
    
    //################
    // test subject 6 can read a confluence group and update a jira group
    confluenceGroup.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.READ);
    jiraGroup.grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.UPDATE);

    // test subject 6 can see the directory, confluence, and jira services
    
    //################
    // test subject 7 is a member of a confluence group
    confluenceGroup.addMember(SubjectTestHelper.SUBJ7, false);
    
    // test subject 7 can see the directory and confluence services
    
    //################
    // test subject 8 is a member of a jira group
    jiraGroup.addMember(SubjectTestHelper.SUBJ8, false);
    
    // test subject 8 can see the directory and jira services
    
    //################
    // test subject 9 is a member of a confluence group and a jira group
    confluenceGroup.addMember(SubjectTestHelper.SUBJ9, false);
    jiraGroup.addMember(SubjectTestHelper.SUBJ9, false);
    
    // test subject 9 can see the directory, confluence, and jira services
    
    
    
  }

  /**
   * 
   */
  public void testCreateService() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef jiraServiceDef = new AttributeDefSave(grouperSession)
      .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
      .assignName("apps:jira:jiraServiceDefinition").assignToAttributeDef(true)
      .assignToGroup(true).assignToStem(true).save();
    
    AttributeDefName jiraService = new AttributeDefNameSave(grouperSession, jiraServiceDef)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("apps:jira:jiraService").assignDisplayExtension("Central IT production Jira issue tracker").save();
  
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignMultiAssignable(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create multi-assignable");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignMultiValued(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create multi-valued");
    } catch (Exception e) {
      //good
    }

    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignValueType(AttributeDefValueType.string).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create non-marker");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToMember(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to member");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToMemberAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to member assign");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToGroupAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to group assn");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToStemAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to stem assn");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToAttributeDefAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to attr def assn");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToImmMembership(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to imm membership");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToImmMembershipAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to imm membership assn");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToEffMembership(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to eff membership");
    } catch (Exception e) {
      //good
    }
    
    try {
      new AttributeDefSave(grouperSession)
        .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.service)
        .assignName("apps:jira:jira2").assignToEffMembershipAssn(true).assignToAttributeDef(true).assignToGroup(true).assignToStem(true).save();
      fail("Shouldnt be able to create assignable to eff membership assn");
    } catch (Exception e) {
      //good
    }
    

    GrouperSession.stopQuietly(grouperSession);
  }
  
}
