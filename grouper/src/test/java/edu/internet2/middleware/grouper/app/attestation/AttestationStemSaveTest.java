package edu.internet2.middleware.grouper.app.attestation;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.textui.TestRunner;

public class AttestationStemSaveTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttestationStemSaveTest("testSaveAttestationAttributesOnStemByStemEmailGroup"));
  }
  
  /**
   * @param name
   */
  public AttestationStemSaveTest(String name) {
    super(name);
  }
  
  public void testSaveAttestationAttributesOnStemByStem() {
    
   Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    
   AttributeAssign attributeAssign = new AttestationStemSave()
     .assignStem(stem)
     .addEmailAddress("test@example.com")
     .assignAttestationType(AttestationType.report)
     .assignDaysBeforeToRemind(5)
     .assignDaysUntilRecertify(10)
     .assignSendEmail(true)
     .save();
   
   assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
   assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
   assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
   assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
   assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
   
   assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
   assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
    
  }
  
  public void testSaveAttestationAttributesOnStemByStemId() {
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
     
    AttributeAssign attributeAssign = new AttestationStemSave()
      .assignStemId(stem.getId())
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.report)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .save();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
     
   }
  
  public void testSaveAttestationAttributesOnStemByStemName() {
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
     
    AttributeAssign attributeAssign = new AttestationStemSave()
      .assignStemName(stem.getName())
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.report)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .save();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
     
   }
  
  public void testSaveAttestationAttributesStemNotFound() {
    
    try {
      new AttestationStemSave()
          .assignStemId("non_existent_stemId")
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
     
  }
  
  public void testDeleteAttestationAttributesOnStem() {
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");
    
    AttributeAssign attributeAssign = new AttestationStemSave()
      .assignStemName(stem.getName())
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.report)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .save();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());

    int auditEntriesSizeBeforeDelete = HibernateSession.byHqlStatic()
         .createQuery("from AuditEntry").list(AuditEntry.class).size();
     
   assertTrue(auditEntriesSizeBeforeDelete > 0);
     
   //now delete the assignment
   attributeAssign = new AttestationStemSave()
       .assignStem(stem)
       .assignSaveMode(SaveMode.DELETE)
       .save();
   
   assertEquals(0, stem.getAttributeDelegate().getAttributeAssigns().size());
   
   int auditEntriesSizeAfterDelete = HibernateSession.byHqlStatic()
       .createQuery("from AuditEntry").list(AuditEntry.class).size();
   
   assertTrue(auditEntriesSizeAfterDelete > auditEntriesSizeBeforeDelete);
  }
  
  public void testSaveAttestationAttributesOnStemReplaceAllExistingSettings() {
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeAssign attributeAssign = new AttestationStemSave()
      .assignStem(stem)
      .addEmailAddress("test@example.com")
      .assignAttestationType(AttestationType.report)
      .assignDaysBeforeToRemind(5)
      .assignDaysUntilRecertify(10)
      .assignSendEmail(true)
      .save();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
     
     // now replace all settings
     attributeAssign = new AttestationStemSave()
         .assignStemName(stem.getName())
         .addEmailAddress("test1@example.com")
         .assignAttestationType(AttestationType.report)
         .save();
     
     assertEquals("test1@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
     assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
     assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
     
     assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
     assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
    
  }
  
  public void testSaveAttestationSubjectHasAdminOnTheStem() {
    //subject has admin on the stem and not running as root; it should work
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN);
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeAssign attributeAssign = new AttestationStemSave()
        .assignStem(stem)
        .addEmailAddress("test@example.com")
        .assignAttestationType(AttestationType.report)
        .assignDaysBeforeToRemind(5)
        .assignDaysUntilRecertify(10)
        .assignSendEmail(true)
        .save();
    
    GrouperSession.startRootSession();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
  }
  
  public void testSaveAttestationSubjectDoesNotHaveAdminOnTheStemButRunningAsRoot() {
    // subject doesn't have admin on the stem and running as root; it should work
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeAssign attributeAssign = new AttestationStemSave()
        .assignStem(stem)
        .addEmailAddress("test@example.com")
        .assignAttestationType(AttestationType.report)
        .assignDaysBeforeToRemind(5)
        .assignDaysUntilRecertify(10)
        .assignSendEmail(true)
        .assignRunAsRoot(true)
        .save();
    
    GrouperSession.startRootSession();
    
    assertEquals("test@example.com", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName()));
    assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
    assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("report", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
    
    assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
    assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
  }
  
  public void testSaveAttestationSubjectDoesNotHaveAdminOnTheStem() {
    //subject doesn't have admin on the stem and not running as root; it should not work
    
    Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    try {
      new AttestationStemSave()
          .assignStem(stem)
          .addEmailAddress("test@example.com")
          .assignAttestationType(AttestationType.report)
          .assignDaysBeforeToRemind(5)
          .assignDaysUntilRecertify(10)
          .assignSendEmail(true)
          .save();
      fail();
    } catch(Exception e) {
      assertTrue(true);
    }
    
    GrouperSession.startRootSession();
    assertEquals(0, stem.getAttributeDelegate().getAttributeAssigns().size());
    
  }

  public void testSaveAttestationAttributesOnStemByStemEmailGroup() {
    
   Stem stem = new StemSave().assignName("test:test1").assignCreateParentStemsIfNotExist(true).save();
   Group group = new GroupSave().assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();

   AttributeAssign attributeAssign = new AttestationStemSave()
     .assignStem(stem)
     .assignEmailGroup(group)
     .assignAttestationType(AttestationType.group)
     .assignDaysBeforeToRemind(5)
     .assignDaysUntilRecertify(10)
     .assignSendEmail(true)
     .save();
  
   assertEquals(group.getId(), attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailGroupId().getName()));
   assertEquals("5", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysBeforeToRemind().getName()));
   assertEquals("10", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
   assertEquals("group", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
   assertEquals("true", attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));
  
   assertEquals(1, stem.getAttributeDelegate().getAttributeAssigns().size());
   assertEquals(stem.getId(), attributeAssign.getOwnerStemId());
    
  }

}
