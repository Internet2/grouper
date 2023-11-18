package edu.internet2.middleware.grouper.app.attestation;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import junit.textui.TestRunner;

public class AttestationGroupSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttestationGroupSaveTest("testDeleteAttestationAttributesOnGroupByGroupEmailGroup"));
  }
  
  /**
   * @param name
   */
  public AttestationGroupSaveTest(String name) {
    super(name);
  }
  
  public void testSaveAttestationAttributesGroupNotFound() {
    
    boolean exceptionThrown = false;
    
    try {
      new AttestationGroupSave()
          .assignGroupId("non_existent_groupId")
          .addEmailAddress("test@example.com")
          .assignAttestationType(AttestationType.group)
          .assignDaysBeforeToRemind(5)
          .assignDaysUntilRecertify(10)
          .assignSendEmail(true)
          .save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }

    assertTrue(exceptionThrown);
    
  }
  
  public void testDeleteAttestationAttributesOnGroupByGroup() {
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroup(group)
       .addEmailAddress("test@example.com")
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .save();
     
     assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
     
     int auditEntriesSizeBeforeDelete = HibernateSession.byHqlStatic()
         .createQuery("from AuditEntry").list(AuditEntry.class).size();
     
     assertTrue(auditEntriesSizeBeforeDelete > 0);
     
     //now delete the assignment
     attributeAssign = new AttestationGroupSave()
         .assignGroup(group)
         .assignSaveMode(SaveMode.DELETE)
         .save();
     
     assertEquals(0, group.getAttributeDelegate().getAttributeAssigns().size());
     
     int auditEntriesSizeAfterDelete = HibernateSession.byHqlStatic()
         .createQuery("from AuditEntry").list(AuditEntry.class).size();
     
     assertTrue(auditEntriesSizeAfterDelete > auditEntriesSizeBeforeDelete);
  }
  
  public void testDeleteAttestationAttributesOnGroupByGroupEmailGroup() {
    
    
    Group group = new GroupSave().assignName("d:e:f").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroup(group)
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .assignEmailGroup(group2)
       .save();
     
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     assertEquals(group2.getId(), attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailGroupId().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
     
     int auditEntriesSizeBeforeDelete = HibernateSession.byHqlStatic()
         .createQuery("from AuditEntry").list(AuditEntry.class).size();
     
     assertTrue(auditEntriesSizeBeforeDelete > 0);
     
     //now delete the assignment
     attributeAssign = new AttestationGroupSave()
         .assignGroup(group)
         .assignSaveMode(SaveMode.DELETE)
         .save();
     
     assertEquals(0, group.getAttributeDelegate().getAttributeAssigns().size());
     
     int auditEntriesSizeAfterDelete = HibernateSession.byHqlStatic()
         .createQuery("from AuditEntry").list(AuditEntry.class).size();
     
     assertTrue(auditEntriesSizeAfterDelete > auditEntriesSizeBeforeDelete);
  }
  
  public void testSaveAttestationAttributesOnGroupByGroup() {
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroup(group)
       .addEmailAddress("test@example.com")
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .save();
     
     assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
    
  }
  
  public void testSaveAttestationAttributesOnGroupByGroupId() {
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroupId(group.getId())
       .addEmailAddress("test@example.com")
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .save();
     
     assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
    
  }
  
  public void testSaveAttestationAttributesOnGroupByGroupName() {
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroupName(group.getName())
       .addEmailAddress("test@example.com")
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .save();
     
     assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
    
  }
  
  public void testSaveAttestationSubjectDoesNotHaveUpdateOnTheGroupButRunningAsRoot() {
    // subject doesn't have update on the group and running as root; it should work
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeAssign attributeAssign = new AttestationGroupSave()
      .assignGroup(group)
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.group)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .assignRunAsRoot(true)
      .save();
    
    GrouperSession.startRootSession();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
  }
  
  
  public void testSaveAttestationSubjectDoesNotHaveUpdateOnTheGroup() {
    //subject doesn't have update on the group and not running as root; it should not work
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    try {
      new AttestationGroupSave()
          .assignGroup(group)
          .addEmailAddress("test@example.com")
          .assignAttestationType(AttestationType.group)
          .assignDaysBeforeToRemind(5)
          .assignDaysUntilRecertify(10)
          .assignSendEmail(true)
          .save();
      fail();
    } catch(Exception e) {
      assertTrue(true);
    }
    
    GrouperSession.startRootSession();
    assertEquals(0, group.getAttributeDelegate().getAttributeAssigns().size());
  }
  
  public void testSaveAttestationSubjectHasUpdateOnTheGroup() {
    //subject has update on the group and not running as root; it should work
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeAssign attributeAssign = new AttestationGroupSave()
      .assignGroup(group)
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.group)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .save();
    
    GrouperSession.startRootSession();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
  }
  
  
  public void testSaveAttestationAttributesOnGroupReplaceAllExistingSettings() {
    
    Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
    
     AttributeAssign attributeAssign = new AttestationGroupSave()
       .assignGroupName(group.getName())
       .addEmailAddress("test@example.com")
       .assignAttestationType(AttestationType.group)
       .assignDaysBeforeToRemind(5)
       .assignDaysUntilRecertify(10)
       .assignSendEmail(true)
       .save();
     
     assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
     
     // now replace all settings
     attributeAssign = new AttestationGroupSave()
         .assignGroupName(group.getName())
         .addEmailAddress("test1@example.com")
         .assignAttestationType(AttestationType.report)
         .save();
     
     assertEquals("test1@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, group.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(group.getId(), attributeAssign.getOwnerGroupId());
    
  }
  
  
}
