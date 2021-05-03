package edu.internet2.middleware.grouper.app.loader;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class NotificationDaemonTest extends GrouperTest {

  /**
     * 
     * @param args
     */
  public static void main(String[] args) {

    TestRunner.run(
        new NotificationDaemonTest("testSendSummary"));
  }

  public NotificationDaemonTest() {

  }

  /**
   * @param name
   */
  public NotificationDaemonTest(String name) {
    super(name);
  }

  public void testSendNotifications() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.class", "edu.internet2.middleware.grouper.app.loader.NotificationDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.quartzCron", "0 03 5 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailType", "notification");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.populationType", "groupMembership");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.eligibilityGroupName", "test:testGroupEligible");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryOnlyIfRecordsExist", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryToGroupName", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSubjectTemplate", "subject");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailBodyTemplate", "hello ${subject_name}__NEWLINE__${column_subject_id}bye");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.subjectSourceId", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListDbConnection", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListGroupName", "test:testGroupMembers");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListQuery", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.lastSentGroupName", "test:testGroupLastSent");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.bccsCommaSeparated", "mchyzer@yahoo.com");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.sendToBccOnly", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroupMembers = new GroupSave(grouperSession).assignName("test:testGroupMembers").assignCreateParentStemsIfNotExist(true).save();
    testGroupMembers.addMember(SubjectTestHelper.SUBJ0);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ1);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ2);

    Group testGroupEligible = new GroupSave(grouperSession).assignName("test:testGroupEligible").assignCreateParentStemsIfNotExist(true).save();
    testGroupEligible.addMember(SubjectTestHelper.SUBJ0);
    testGroupEligible.addMember(SubjectTestHelper.SUBJ1);
    testGroupEligible.addMember(SubjectTestHelper.SUBJ8);

    Group testGroupLastSent = new GroupSave(grouperSession).assignName("test:testGroupLastSent").assignCreateParentStemsIfNotExist(true).save();

//    AttributeDef gotEmailDef = new AttributeDefSave(grouperSession).assignName("test:gotEmailDef").assignAttributeDefType(AttributeDefType.attr)
//        .assignToImmMembership(true).assignValueType(AttributeDefValueType.string).save();
//    
//    AttributeDefName gotEmailDefName = new AttributeDefNameSave(grouperSession, gotEmailDef).assignName("test:gotEmail").save();
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");

    assertEquals(2, GrouperUtil.length(GrouperEmail.testingEmails()));
    
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("hello my name is test.subject.0\ntest.subject.0bye", GrouperEmail.testingEmails().get(0).getBody());
    
    assertEquals("test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(1).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(1).getSubject());
    assertEquals("hello my name is test.subject.1\ntest.subject.1bye", GrouperEmail.testingEmails().get(1).getBody());

    assertEquals(2, GrouperUtil.length(testGroupLastSent.getMembers()));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ1));

    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member0).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member1).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));

    testGroupMembers.addMember(SubjectTestHelper.SUBJ8, false);

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");

    assertEquals(3, GrouperUtil.length(GrouperEmail.testingEmails()));

    assertEquals("test.subject.8@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(2).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(2).getSubject());
    assertEquals("hello my name is test.subject.8\ntest.subject.8bye", GrouperEmail.testingEmails().get(2).getBody());

    Member member8 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member8).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));

  }

  public void testSendNotificationsNoEligibility() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.class", "edu.internet2.middleware.grouper.app.loader.NotificationDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.quartzCron", "0 03 5 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailType", "notification");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.populationType", "groupMembership");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.eligibilityGroupName", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryOnlyIfRecordsExist", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryToGroupName", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSubjectTemplate", "subject");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailBodyTemplate", "hello ${subject_name}__NEWLINE__${column_subject_id}bye");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.subjectSourceId", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListDbConnection", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListGroupName", "test:testGroupMembers");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListQuery", "");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.lastSentAttributeDefName", "test:gotEmail");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.lastSentGroupName", "test:testGroupLastSent");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.bccsCommaSeparated", "mchyzer@yahoo.com");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.sendToBccOnly", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroupMembers = new GroupSave(grouperSession).assignName("test:testGroupMembers").assignCreateParentStemsIfNotExist(true).save();
    testGroupMembers.addMember(SubjectTestHelper.SUBJ0);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ1);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ2);

    Group testGroupLastSent = new GroupSave(grouperSession).assignName("test:testGroupLastSent").assignCreateParentStemsIfNotExist(true).save();

//    AttributeDef gotEmailDef = new AttributeDefSave(grouperSession).assignName("test:gotEmailDef").assignAttributeDefType(AttributeDefType.attr)
//        .assignToImmMembership(true).assignValueType(AttributeDefValueType.string).save();
//    
//    AttributeDefName gotEmailDefName = new AttributeDefNameSave(grouperSession, gotEmailDef).assignName("test:gotEmail").save();
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");

    assertEquals(3, GrouperUtil.length(GrouperEmail.testingEmails()));
    
    assertEquals("test.subject.0@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals("hello my name is test.subject.0\ntest.subject.0bye", GrouperEmail.testingEmails().get(0).getBody());
    
    assertEquals("test.subject.1@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(1).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(1).getSubject());
    assertEquals("hello my name is test.subject.1\ntest.subject.1bye", GrouperEmail.testingEmails().get(1).getBody());
    
    assertEquals("test.subject.2@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(2).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(2).getSubject());
    assertEquals("hello my name is test.subject.2\ntest.subject.2bye", GrouperEmail.testingEmails().get(2).getBody());

    assertEquals(3, GrouperUtil.length(testGroupLastSent.getMembers()));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ2));

    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member0).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member1).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member2).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));

    testGroupMembers.addMember(SubjectTestHelper.SUBJ8, false);

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");

    assertEquals(4, GrouperUtil.length(GrouperEmail.testingEmails()));

    assertEquals("test.subject.8@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(3).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(3).getSubject());
    assertEquals("hello my name is test.subject.8\ntest.subject.8bye", GrouperEmail.testingEmails().get(3).getBody());

    Member member8 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ8, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member8).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));

  }

  public void testSendSummary() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", "testing");
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.class", "edu.internet2.middleware.grouper.app.loader.NotificationDaemon");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.quartzCron", "0 03 5 * * ?");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailType", "summary");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.populationType", "sqlQuery");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.eligibilityGroupName", "test:testGroupEligible");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryOnlyIfRecordsExist", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSummaryToGroupName", "test:emailSummaryToGroup");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailSubjectTemplate", "subject");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailBodyTemplate", 

//        hello my name is test.subject.6
//
//        Record subject ID: test.subject.0
//        Record subject ID: test.subject.1
//      bye
        
        "hello ${subject_name},__NEWLINE__Here is the list of records:__NEWLINE__$$ for (var recordMap : listOfRecordMaps) {__NEWLINE__  Record subject ID: ${recordMap.get('subject_id')} is__NEWLINE__$$}__NEWLINE__bye");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.subjectSourceId", "jdbc");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListDbConnection", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListGroupName", "");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.emailListQuery", 
        "select subject_id from grouper_memberships_lw_v where group_name = 'test:testGroupMembers' and list_name = 'members' order by subject_id");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.lastSentGroupName", "test:testGroupLastSent");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.bccsCommaSeparated", "mchyzer@yahoo.com");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob.notificationTest.sendToBccOnly", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroupMembers = new GroupSave(grouperSession).assignName("test:testGroupMembers").assignCreateParentStemsIfNotExist(true).save();
    testGroupMembers.addMember(SubjectTestHelper.SUBJ0);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ1);
    testGroupMembers.addMember(SubjectTestHelper.SUBJ2);
  
    Group testGroupEligible = new GroupSave(grouperSession).assignName("test:testGroupEligible").assignCreateParentStemsIfNotExist(true).save();
    testGroupEligible.addMember(SubjectTestHelper.SUBJ0);
    testGroupEligible.addMember(SubjectTestHelper.SUBJ1);
    testGroupEligible.addMember(SubjectTestHelper.SUBJ8);
  
    Group emailSummaryToGroup = new GroupSave(grouperSession).assignName("test:emailSummaryToGroup").assignCreateParentStemsIfNotExist(true).save();
    emailSummaryToGroup.addMember(SubjectTestHelper.SUBJ6);
    emailSummaryToGroup.addMember(SubjectTestHelper.SUBJ7);
    
    Group testGroupLastSent = new GroupSave(grouperSession).assignName("test:testGroupLastSent").assignCreateParentStemsIfNotExist(true).save();

//    AttributeDef gotEmailDef = new AttributeDefSave(grouperSession).assignName("test:gotEmailDef").assignAttributeDefType(AttributeDefType.attr)
//        .assignToImmMembership(true).assignValueType(AttributeDefValueType.string).save();
//    
//    AttributeDefName gotEmailDefName = new AttributeDefNameSave(grouperSession, gotEmailDef).assignName("test:gotEmail").save();
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");
  
    assertEquals(2, GrouperUtil.length(GrouperEmail.testingEmails()));
    
    assertEquals("test.subject.6@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(0).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(0).getSubject());
    assertEquals(GrouperEmail.testingEmails().get(0).getBody(), 
        "hello my name is test.subject.6,\nHere is the list of records:\n  Record subject ID: test.subject.0 is\n  "
        + "Record subject ID: test.subject.1 is\nbye", GrouperEmail.testingEmails().get(0).getBody());
    
    assertEquals("test.subject.7@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(1).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(1).getSubject());
    assertEquals("hello my name is test.subject.7,\nHere is the list of records:\n  Record subject ID: test.subject.0 is\n  "
        + "Record subject ID: test.subject.1 is\nbye", GrouperEmail.testingEmails().get(1).getBody());
    
    assertEquals(2, GrouperUtil.length(testGroupLastSent.getMembers()));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ6));
    assertTrue(testGroupLastSent.hasMember(SubjectTestHelper.SUBJ7));

    String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    Member member6 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ6, false);
    Member member7 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member6).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member7).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));

    testGroupMembers.addMember(SubjectTestHelper.SUBJ8, false);
  
    emailSummaryToGroup.addMember(SubjectTestHelper.SUBJ9, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_notificationTest");
  
    assertEquals(3, GrouperUtil.length(GrouperEmail.testingEmails()));
  
    assertEquals("test.subject.9@somewhere.someSchool.edu", GrouperEmail.testingEmails().get(2).getTo());
    assertEquals("subject", GrouperEmail.testingEmails().get(2).getSubject());
    assertEquals("hello my name is test.subject.9,\nHere is the list of records:\n  Record subject ID: test.subject.0 is\n  "
        + "Record subject ID: test.subject.1 is\n  Record subject ID: test.subject.8 is\nbye", GrouperEmail.testingEmails().get(2).getBody());
  
    Member member9 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ9, false);
    assertEquals(date, testGroupLastSent.getAttributeValueDelegateMembership(member9).retrieveValueString(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT));
  
  }

}
