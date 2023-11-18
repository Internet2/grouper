/**
 * 
 */
package edu.internet2.middleware.grouper.util;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class GrouperEmailTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperEmailTest("testAllowGrouperConfig"));
  }
  
  /**
   * @param name
   */
  public GrouperEmailTest(String name) {
    super(name);
  }

  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /**
   * 
   */
  public void testAllowGrouperConfig() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    assertEquals(0, grouperConfigHibernates.size());

    try {
      GrouperEmail.addAllowEmailToGroup("a@b.c");
      fail();
    } catch (Exception e) {
      // good
    }

    assertTrue(GrouperEmail.addAllowEmailToGroup("something@grouper"));
    
    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernates.iterator().next();
    
    assertEquals("something@grouper", grouperConfigHibernate.retrieveValue());

    assertFalse(GrouperEmail.addAllowEmailToGroup("something@grouper"));
    
    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    grouperConfigHibernate = grouperConfigHibernates.iterator().next();
    
    assertEquals("something@grouper", grouperConfigHibernate.retrieveValue());
    
    assertTrue(GrouperEmail.addAllowEmailToGroup("something1@grouper"));
    
    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    grouperConfigHibernate = grouperConfigHibernates.iterator().next();
    
    assertEquals("something@grouper,something1@grouper", grouperConfigHibernate.retrieveValue());

    assertFalse(GrouperEmail.removeAllowEmailToGroup("something2@grouper"));

    assertTrue(GrouperEmail.removeAllowEmailToGroup("something@grouper"));
      
    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    grouperConfigHibernate = grouperConfigHibernates.iterator().next();
    
    assertEquals("something1@grouper", grouperConfigHibernate.retrieveValue());

    assertTrue(GrouperEmail.removeAllowEmailToGroup("something1@grouper"));
    
    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(
        ConfigFileName.GROUPER_PROPERTIES, null, "mail.smtp.groupUuidAndNameEmailDereferenceAllow");
    grouperConfigHibernate = grouperConfigHibernates.iterator().next();
    
    assertTrue(StringUtils.isBlank(grouperConfigHibernate.retrieveValue()));

  }
  
  /**
   * 
   */
  public void testSimpleEmail() {
    new GrouperEmail().setTo("mchyzer@whatever").setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }
  
  /**
   * 
   */
  public void testGroupEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupToSendTo(group).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }
  /**
   * 
   */
  public void testGroupIdEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupUuidToSendTo(group.getId(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }

  /**
   * 
   */
  public void testGroupIdEmailDereference() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    try {
      new GrouperEmail().setTo("mchyzer@yahoo.whatever," + group.getId() + "@grouper").setBody("email body").setSubject("email subject").send();
      fail("Shouldnt get here");
    } catch (Exception e) {
      // good
    }
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.groupUuidAndNameEmailDereferenceAllow", group.getId() + "@grouper");
    
    new GrouperEmail().setTo("mchyzer@yahoo.whatever," + group.getId() + "@grouper").setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@yahoo.whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

    
    //  # comma separated group name and uuid's to be allow email addresses to dereference.
    //  # for instance: a:b:c@grouper, def345@grouper<br />
    //  # If a configuration enters in one of those email addresses, and it is in this allow list, then
    //  # dereference the group and member and send email to their individual email addresses.  Note that 
    //  # groups in this list can have their members discovered so treat their membership as non private.
    //  # using the uuid@grouper gives a little bit of obscurity since the uuid of the group needs to be known
    //  # is it is still security by obscurity which is not true security.  There is a max of 100 members it will
    //  # send to
    //  # {valueType: "string", multiple: true}
    //  mail.smtp.groupUuidAndNameEmailDereferenceAllow =

    
  }

  /**
   * 
   */
  public void testGroupNameEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupNameToSendTo(group.getName(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }
  
  /**
   * 
   */
  public void testGroupEmailSecurity() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    try {
      new GrouperEmail().setTo("mchyzer@whatever").addGroupNameToSendTo(group.getName(), true).setBody("email body").setSubject("email subject").send();
      assertTrue(false);
    } catch (Exception e) {
      //good
    }
    new GrouperEmail().setTo("mchyzer@whatever").assignRunAsRoot(true).addGroupNameToSendTo(group.getName(), false).setBody("email body").setSubject("email subject").send();

    assertEquals("mchyzer@whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

    GrouperSession.stopQuietly(grouperSession);
    

  }
  
  /**
   * 
   */
  public void testGroupEmailSecurity2() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    GrouperSession.stopQuietly(grouperSession);
    
    GrouperSession.start(SubjectTestHelper.SUBJ2);
    new GrouperEmail().setTo("mchyzer@whatever").addGroupNameToSendTo(group.getName(), true).setBody("email body").setSubject("email subject").send();

    
    assertEquals("mchyzer@whatever,test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

    GrouperSession.stopQuietly(grouperSession);
    


  }
  

  
  /**
   * 
   */
  public void testHtmlEmail() {
    new GrouperEmail().setTo("mchyzer@whatever").setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/html; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }


  /**
   * 
   */
  public void testEmailToSubject() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addSubjectToSendTo(subject).setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/html; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }

  /**
   * 
   */
  public void testEmailToSubjectId() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addSubjectIdToSendTo(subject.getSourceId(), subject.getId()).setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/html; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }

  /**
   * 
   */
  public void testEmailToSubjectIdentifier() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addSubjectIdentifierToSendTo(subject.getSourceId(), "id.test.subject.0").setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/html; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());

  }

  /**
   * 
   */
  public void testEmailCcSubject() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectToCc(subject).setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testEmailCcSubjectId() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectIdToCc(subject.getSourceId(), subject.getId()).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testEmailCcSubjectIdentifier() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectIdentifierToCc(subject.getSourceId(), "id.test.subject.0").setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testEmailBccSubject() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectToBcc(subject).setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testEmailBccSubjectId() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectIdToBcc(subject.getSourceId(), subject.getId()).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testEmailBccSubjectIdentifier() {
    Subject subject = SubjectTestHelper.SUBJ0;
    new GrouperEmail().addEmailAddressToSendTo("mchyzer@whatever").addSubjectIdentifierToBcc(subject.getSourceId(), "id.test.subject.0").setBody("<h1>email body</h1>").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("<h1>email body</h1>", GrouperEmail.testingEmails().get(0).getBody());

  }

  /**
   * 
   */
  public void testGroupCcEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupToCc(group).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }


  /**
   * 
   */
  public void testGroupNameCcEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupNameToCc(group.getName(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }


  /**
   * 
   */
  public void testGroupIdCcEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupUuidToCc(group.getId(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getCc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }

  /**
   * 
   */
  public void testGroupBccEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupToBcc(group).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }

  /**
   * 
   */
  public void testGroupIdBccEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupUuidToBcc(group.getId(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }

  /**
   * 
   */
  public void testGroupNameBccEmail() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ1);
    
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    new GrouperEmail().setTo("mchyzer@whatever").addGroupNameToBcc(group.getName(), true).setBody("email body").setSubject("email subject").send();
    
    assertEquals("mchyzer@whatever", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("test.subject.0@somewhere.someSchool.edu,test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getBcc());
    assertEquals("email subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("email body", GrouperEmail.testingEmails().get(0).getBody());
    assertEquals("text/plain; charset=utf-8", GrouperEmail.testingEmails().get(0).getEmailContentType());
  
  }


}
