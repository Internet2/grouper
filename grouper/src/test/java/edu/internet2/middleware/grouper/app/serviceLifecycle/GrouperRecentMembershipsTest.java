package edu.internet2.middleware.grouper.app.serviceLifecycle;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperRecentMembershipsTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperRecentMembershipsTest("testUpgradeTask"));
  }
  
  public GrouperRecentMembershipsTest(String name) {
    super(name);
  }

  public void testUpgradeTask() {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
        
        AttributeDef grouperRecentMembershipsValueDef = AttributeDefFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_VALUE_DEF, true);

        AttributeDefName grouperRecentMembershipsDays = new AttributeDefNameSave(grouperSession, grouperRecentMembershipsValueDef).assignName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsDays").save();

        AttributeDefName grouperRecentMembershipsGroupName = new AttributeDefNameSave(grouperSession, grouperRecentMembershipsValueDef).assignName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsGroupName").save();

        AttributeDefName grouperRecentMembershipsGroupUuidFrom = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);

        AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);

        AttributeDefName grouperRecentMembershipsIncludeCurrent = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);

        Group testGroupFrom = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupFrom").save();
        Group testGroupTo = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupTo").save();

        AttributeAssign attributeAssign = testGroupFrom.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker).getAttributeAssign();
        attributeAssign.getAttributeValueDelegate().assignValue(grouperRecentMembershipsDays.getName(), "5");
        attributeAssign.getAttributeValueDelegate().assignValue(grouperRecentMembershipsGroupName.getName(), testGroupTo.getName());
        attributeAssign.getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeCurrent.getName(), "true");

        GrouperRecentMemberships.upgradeFromV2_5_29_to_V2_5_30();
        
        assertFalse(testGroupFrom.getAttributeDelegate().hasAttribute(grouperRecentMembershipsMarker));
        
        assertTrue(testGroupTo.getAttributeDelegate().hasAttribute(grouperRecentMembershipsMarker));

        attributeAssign = testGroupTo.getAttributeDelegate().retrieveAssignment("assign", grouperRecentMembershipsMarker, false, true);

        assertEquals(testGroupFrom.getId(), attributeAssign.getAttributeValueDelegate().retrieveValueString(grouperRecentMembershipsGroupUuidFrom.getName()));
        assertEquals("T", attributeAssign.getAttributeValueDelegate().retrieveValueString(grouperRecentMembershipsIncludeCurrent.getName()));
        assertEquals((Object)(5*24*60L*60*1000000L), attributeAssign.getAttributeValueDelegate().retrieveValueInteger(grouperRecentMembershipsMicros.getName()));
        
        GrouperCacheUtils.clearAllCaches();
        grouperRecentMembershipsDays = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsDays", false);
       
        assertNull(grouperRecentMembershipsDays);
        
        grouperRecentMembershipsGroupName = AttributeDefNameFinder.findByName(
            GrouperRecentMemberships.recentMembershipsStemName() + ":grouperRecentMembershipsGroupName", false);
        
        assertNull(grouperRecentMembershipsGroupName);

        return null;
      }
    });
    

  }
  
  /**
   * 
   */
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
    if (runChangeLog) {
      ChangeLogTempToEntity.convertRecords();
    }
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(true), "OTHER_JOB_timeDaemon");

    if (runConsumer) {

      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(true), "CHANGE_LOG_consumer_recentMemberships");
      
      Timestamp lastSuccess = new GcDbAccess().sql("select max(ended_time) from grouper_loader_log where job_name = ?")
          .addBindVar("CHANGE_LOG_consumer_recentMemberships").select(Timestamp.class);

      Hib3GrouperLoaderLog hib3GrouploaderLog = HibernateSession.byHqlStatic().createQuery("select theLoaderLog from Hib3GrouperLoaderLog theLoaderLog " +
          "where theLoaderLog.jobName = :theJobName and theLoaderLog.endedTime = :theEndedTime")
          .setString("theJobName", "CHANGE_LOG_consumer_recentMemberships")
          .setTimestamp("theEndedTime", lastSuccess)
          .uniqueResult(Hib3GrouperLoaderLog.class);
      
      
//      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
//      hib3GrouploaderLog.setStartedTime(new Timestamp(System.currentTimeMillis()));
//      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
//      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_recentMemberships");
//      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
//      ChangeLogHelper.processRecords("recentMemberships", hib3GrouploaderLog, new EsbConsumer());
//      hib3GrouploaderLog.setEndedTime(new Timestamp(System.currentTimeMillis()));
//      hib3GrouploaderLog.store();
      // sometimes the loader attributes are updated after deleted by job... so wait a tad
      GrouperUtil.sleep(2000);
      
      return hib3GrouploaderLog;
    }
    
    return null;
  }

  public void testRecentMembershipsNotIncludeEligible() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group2daySource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2daySource").save();
    Group group4daySource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4daySource").save();
    Group group2daySourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2daySourceSub").save();
    Group group4daySourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4daySourceSub").save();

    
    group2daySource.addMember(group2daySourceSub.toSubject());
    group4daySource.addMember(group4daySourceSub.toSubject());

    runJobs(true, false);

    long micros9daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*9));
    long micros8daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*8));
    long micros7daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*7));
    long micros3daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*3));
    long micros1daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*1));

    // the groups have to have been members for a while
    {
      String group2daySourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(group2daySourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(group2daySourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String group2daySourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group2daySource.getName()).select(String.class);
      String group2daySourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group2daySourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(group2daySourcePitGroupId)
        .addBindVar(group2daySourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    {
      String group4daySourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(group4daySourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(group4daySourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String group4daySourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group4daySource.getName()).select(String.class);
      String group4daySourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group4daySourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(group4daySourcePitGroupId)
        .addBindVar(group4daySourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    
    // subj 0 is in both groups for 1 week
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ0);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    String subject0pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.0'").select(String.class);
    int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
        .addBindVar(micros7daysAgo).addBindVar(subject0pitMemberId).executeSql();
    assertEquals(2, rows);  

    // subj 1 was in both groups a week ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ1);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    String subject1pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.1'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros7daysAgo).addBindVar(subject1pitMemberId).executeSql();
    assertEquals(2, rows);  

    // subj 2 was in both groups 1 day ago, and is still in both groups
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    String subject2pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.2'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject2pitMemberId).executeSql();
    assertEquals(2, rows);  
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);

    // subj 3 was in both groups three days ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ3);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    String subject3pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.3'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros3daysAgo).addBindVar(subject3pitMemberId).executeSql();
    assertEquals(2, rows);  


    // subj 4 was in both groups one days ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ4);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    String subject4pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.4'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject4pitMemberId).executeSql();
    assertEquals(2, rows);  

    int fullSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount;
    int incrementalSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount;
    
    runJobs(true, true);
    
    assertEquals(fullSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperRecentMembershipsGroupUuidFrom = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperRecentMembershipsIncludeEligible = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    Group theGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2dayRecentMemberships").save();
    AttributeAssignResult attributeAssignResult = theGroup.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), "" + (2 * 24L * 60 * 60 * 1000 * 1000L));
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsGroupUuidFrom.getName(), group2daySource.getId());
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "F");
    
    theGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4dayRecentMemberships").save();
    attributeAssignResult = theGroup.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), "" + (4 * 24L * 60 * 60 * 1000 * 1000L));
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsGroupUuidFrom.getName(), group4daySource.getId());
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "F");
    
    runJobs(true, true);

    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    Group group2dayRecentMemberships = GroupFinder.findByName(grouperSession, "test:group2dayRecentMemberships", true);
    Group group4dayRecentMemberships = GroupFinder.findByName(grouperSession, "test:group4dayRecentMemberships", true);
    
    assertEquals(2, group4dayRecentMemberships.getMembers().size());
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    assertEquals(1, group2dayRecentMemberships.getMembers().size());
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);

    runJobs(true, true);

    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
    
    assertEquals(3, group4dayRecentMemberships.getMembers().size());
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    assertEquals(2, group2dayRecentMemberships.getMembers().size());
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
  }

  public void testRecentMembershipsIncludeEligible() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group2daySource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2daySource").save();
    Group group4daySource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4daySource").save();
    Group group2daySourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2daySourceSub").save();
    Group group4daySourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4daySourceSub").save();

    Group group2dayRecentMemberships = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group2dayRecentMemberships").save();
    Group group4dayRecentMemberships = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group4dayRecentMemberships").save();
    
    group2daySource.addMember(group2daySourceSub.toSubject());
    group4daySource.addMember(group4daySourceSub.toSubject());

    runJobs(true, false);

    long micros9daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*9));
    long micros8daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*8));
    long micros7daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*7));
    long micros3daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*3));
    long micros1daysAgo = 1000*(System.currentTimeMillis()-(1000*60*60*24*1));

    // the groups have to have been members for a while
    {
      String group2daySourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(group2daySourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(group2daySourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String group2daySourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group2daySource.getName()).select(String.class);
      String group2daySourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group2daySourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(group2daySourcePitGroupId)
        .addBindVar(group2daySourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    {
      String group4daySourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(group4daySourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(group4daySourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String group4daySourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group4daySource.getName()).select(String.class);
      String group4daySourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(group4daySourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(group4daySourcePitGroupId)
        .addBindVar(group4daySourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    
    // subj 0 is in both groups for 1 week
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ0);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    String subject0pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.0'").select(String.class);
    int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
        .addBindVar(micros7daysAgo).addBindVar(subject0pitMemberId).executeSql();
    assertEquals(2, rows);  

    // subj 1 was in both groups a week ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ1);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    String subject1pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.1'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros7daysAgo).addBindVar(subject1pitMemberId).executeSql();
    assertEquals(2, rows);  

    // subj 2 was in both groups 1 day ago, and is still in both groups
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    String subject2pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.2'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject2pitMemberId).executeSql();
    assertEquals(2, rows);  
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);

    // subj 3 was in both groups three days ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ3);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    String subject3pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.3'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros3daysAgo).addBindVar(subject3pitMemberId).executeSql();
    assertEquals(2, rows);  


    // subj 4 was in both groups one days ago
    group2daySourceSub.addMember(SubjectTestHelper.SUBJ4);
    group4daySourceSub.addMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    String subject4pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.4'").select(String.class);
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject4pitMemberId).executeSql();
    assertEquals(2, rows);  

    int fullSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount;
    int incrementalSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount;
    
    runJobs(true, true);
    
    assertEquals(fullSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperRecentMembershipsUuidFrom = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperRecentMembershipsIncludeEligible = AttributeDefNameFinder.findByName(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    AttributeAssignResult attributeAssignResult = group2dayRecentMemberships.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), Long.toString(2L * 24 * 60 * 60 * 1000 * 1000L));
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsUuidFrom.getName(), group2daySource.getId());
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "T");
    
    attributeAssignResult = group4dayRecentMemberships.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), Long.toString(4L * 24 * 60 * 60 * 1000 * 1000L));
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsUuidFrom.getName(), group4daySource.getId());
    attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "T");
    
    runJobs(true, true);

    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    assertEquals(4, group4dayRecentMemberships.getMembers().size());
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    assertEquals(3, group2dayRecentMemberships.getMembers().size());
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    group2daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    group4daySourceSub.deleteMember(SubjectTestHelper.SUBJ2);

    runJobs(true, true);

    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
    
    assertEquals(4, group4dayRecentMemberships.getMembers().size());
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ3));
    assertTrue(group4dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
    assertEquals(3, group2dayRecentMemberships.getMembers().size());
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group2dayRecentMemberships.hasMember(SubjectTestHelper.SUBJ4));
    
  }

  public void testRecentMembershipsRange() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupSource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSource").save();
    Group groupSourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSourceSub").save();
  
    groupSource.addMember(groupSourceSub.toSubject());
  
    runJobs(true, false);
  
    long micros9daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*9.5)));
    long micros8daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*8.5)));
    long micros7daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*7.5)));
    long micros3daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*3.5)));
    long micros1daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*1.5)));
  
    // the groups have to have been members for a while
    {
      String groupSourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(groupSourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(groupSourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String groupSourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSource.getName()).select(String.class);
      String groupSourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(groupSourcePitGroupId)
        .addBindVar(groupSourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    
    // subj 0 is in both groups for 7.5 days
    groupSourceSub.addMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    String subject0pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.0'").select(String.class);
    int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
        .addBindVar(micros7daysAgo).addBindVar(subject0pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 1 was in both groups 8.5 - 7.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    String subject1pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.1'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros7daysAgo).addBindVar(subject1pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 2 was in both groups 8.5 - 1.5 days ago, and is still in both groups since in sub
    groupSourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    String subject2pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.2'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject2pitMemberId).executeSql();
    assertEquals(1, rows);  
    groupSourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
  
    // subj 3 was in both groups 8.5-3.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    String subject3pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.3'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros3daysAgo).addBindVar(subject3pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 4 was in both groups 8.5 - 1.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    String subject4pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.4'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject4pitMemberId).executeSql();
    assertEquals(1, rows);  

    int fullSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount;
    int incrementalSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount;
    
    runJobs(true, true);
    
    assertEquals(fullSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperRecentMembershipsDays = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperRecentMembershipsGroupUuidFrom = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperRecentMembershipsIncludeEligible = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    for (int i=1;i<=12;i++) {
      
      Group theGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group" + i + "dayRecentMemberships").save();

      
      AttributeAssignResult attributeAssignResult = theGroup.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
      
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsDays.getName(), "" + (i * 24L * 60 * 60 * 1000 * 1000L));
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsGroupUuidFrom.getName(), groupSource.getId());
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "T");
    }
        
    runJobs(true, true);
  
    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    Group[] groupsRecentMemberships = new Group[13];
    for (int i=1;i<=12;i++) {

      groupsRecentMemberships[i] = GroupFinder.findByName(grouperSession, "test:group" + i + "dayRecentMemberships", false);

    }
    // currently 2020/06/06 16:44 PM
    // my name is test.subject.0    2020/05/30 04:43 AM - ?
    // my name is test.subject.1    2020/05/29 04:43 AM - 2020/05/30 04:43 AM
    // my name is test.subject.2    2020/05/29 04:43 AM - 2020/06/05 04:43 AM
    // my name is test.subject.2    2020/06/06 16:43 PM - ?
    // my name is test.subject.3    2020/05/29 04:43 AM - 2020/06/03 04:43 AM
    // my name is test.subject.4    2020/05/29 04:43 AM - 2020/06/05 04:43 AM
    
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, true == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ0)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=8) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ1)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, true == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ2)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=4) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ3)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=2) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ4)));
    }
    
    
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ2);
  
    runJobs(true, true);
  
    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);

    
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, true == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ0)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=8) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ1)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=1) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ2)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=4) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ3)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=2) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ4)));
    }
    
  }

  public void testRecentMembershipsRangeNotIncludeEligible() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupSource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSource").save();
    Group groupSourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSourceSub").save();
  
    groupSource.addMember(groupSourceSub.toSubject());
  
    runJobs(true, false);
  
    long micros9daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*9.5)));
    long micros8daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*8.5)));
    long micros7daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*7.5)));
    long micros3daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*3.5)));
    long micros1daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*1.5)));
  
    // the groups have to have been members for a while
    {
      String groupSourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(groupSourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(groupSourceSubMemberId).executeSql();
      assertEquals(1, rows);  
      String groupSourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSource.getName()).select(String.class);
      String groupSourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(groupSourcePitGroupId)
        .addBindVar(groupSourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    
    // subj 0 is in both groups for 7.5 days
    groupSourceSub.addMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    String subject0pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.0'").select(String.class);
    int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
        .addBindVar(micros7daysAgo).addBindVar(subject0pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 1 was in both groups 8.5 - 7.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    String subject1pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.1'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros7daysAgo).addBindVar(subject1pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 2 was in both groups 8.5 - 1.5 days ago, and is still in both groups since in sub
    groupSourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    String subject2pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.2'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject2pitMemberId).executeSql();
    assertEquals(1, rows);  
    groupSourceSub.addMember(SubjectTestHelper.SUBJ2);
    runJobs(true, false);
  
    // subj 3 was in both groups 8.5-3.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    String subject3pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.3'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ3);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros3daysAgo).addBindVar(subject3pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    // subj 4 was in both groups 8.5 - 1.5 days ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    String subject4pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.4'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ4);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros8daysAgo).addBindVar(micros1daysAgo).addBindVar(subject4pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    int fullSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount;
    int incrementalSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount;
    
    runJobs(true, true);
    
    assertEquals(fullSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperRecentMembershipsFromGroupUuid = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperRecentMembershipsIncludeEligible = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    for (int i=1;i<=12;i++) {
      
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group" + i + "dayRecentMemberships").save();
      
      AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), "" + (i * 24L * 60 * 60 * 1000 * 1000L));
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsFromGroupUuid.getName(), groupSource.getId());
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "F");
    }
        
    runJobs(true, true);
  
    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    Group[] groupsRecentMemberships = new Group[13];
    for (int i=1;i<=12;i++) {
  
      groupsRecentMemberships[i] = GroupFinder.findByName(grouperSession, "test:group" + i + "dayRecentMemberships", false);
  
    }
    runJobs(true, true);

    // currently 2020/06/06 16:44 PM
    // my name is test.subject.0    2020/05/30 04:43 AM - ?
    // my name is test.subject.1    2020/05/29 04:43 AM - 2020/05/30 04:43 AM
    // my name is test.subject.2    2020/05/29 04:43 AM - 2020/06/05 04:43 AM
    // my name is test.subject.2    2020/06/06 16:43 PM - ?
    // my name is test.subject.3    2020/05/29 04:43 AM - 2020/06/03 04:43 AM
    // my name is test.subject.4    2020/05/29 04:43 AM - 2020/06/05 04:43 AM
    
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, false == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ0)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=8) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ1)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, false == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ2)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=4) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ3)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=2) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ4)));
    }
    
    
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ2);
  
    runJobs(true, true);
  
    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, false == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ0)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=8) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ1)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=1) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ2)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=4) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ3)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=2) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ4)));
    }
    
  }

  public void testRecentMembershipsEdgeCase() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupSource = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSource").save();
    Group groupSourceSub = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:groupSourceSub").save();
  
    groupSource.addMember(groupSourceSub.toSubject());
  
    runJobs(true, false);
  
    long micros9daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*9.5)));
    long micros3daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*3.5)));
    long microsLess1daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*0.99)));
    long microsMore1daysAgo = (long)(1000*(System.currentTimeMillis()-(1000*60*60*24*1.01)));
  
    // the groups have to have been members for a while
    {
      runJobs(true, false);
      String groupSourceSubMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = ?").addBindVar(groupSourceSub.getId()).select(String.class);
      int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ? where member_id = ?")
          .addBindVar(micros9daysAgo).addBindVar(groupSourceSubMemberId).executeSql();
      runJobs(true, false);
      assertEquals(1, rows);  
      String groupSourcePitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSource.getName()).select(String.class);
      String groupSourceSubPitGroupId = new GcDbAccess().sql("select id from grouper_pit_groups where name = ?").addBindVar(groupSourceSub.getName()).select(String.class);
      rows = new GcDbAccess().sql("update grouper_pit_group_set set start_time = ? where owner_id in (?,?)").addBindVar(micros9daysAgo).addBindVar(groupSourcePitGroupId)
        .addBindVar(groupSourceSubPitGroupId).executeSql();
      // rows for all fields and the relationship
      assertTrue(rows + "", rows > 10);
    }
    
    // subj 0 is in sub group less than one day ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    String subject0pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.0'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ0);
    runJobs(true, false);
    int rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros3daysAgo).addBindVar(microsLess1daysAgo).addBindVar(subject0pitMemberId).executeSql();

    assertEquals(1, rows);  
  
    // subj 1 was in sub group more than one day ago
    groupSourceSub.addMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    String subject1pitMemberId = new GcDbAccess().sql("select id from grouper_pit_members where subject_id = 'test.subject.1'").select(String.class);
    groupSourceSub.deleteMember(SubjectTestHelper.SUBJ1);
    runJobs(true, false);
    rows = new GcDbAccess().sql("update grouper_pit_memberships set start_time = ?, end_time = ? where member_id = ?")
        .addBindVar(micros3daysAgo).addBindVar(microsMore1daysAgo).addBindVar(subject1pitMemberId).executeSql();
    assertEquals(1, rows);  
  
    int fullSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount;
    int incrementalSyncCount = GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount;
    
    runJobs(true, true);
        
    assertEquals(fullSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    AttributeDefName grouperRecentMembershipsMarker = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER, true);
    AttributeDefName grouperRecentMembershipsMicros = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, true);
    AttributeDefName grouperRecentMembershipsFromGroupUuid = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, true);
    AttributeDefName grouperRecentMembershipsIncludeEligible = AttributeDefNameFinder.findByName(GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, true);
    
    for (int i=1;i<=12;i++) {
      
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group" + i + "dayRecentMemberships").save();
      
      AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(grouperRecentMembershipsMarker);
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsMicros.getName(), "" + (i * 24L * 60 * 60 * 1000 * 1000L));
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsFromGroupUuid.getName(), groupSource.getId());
      attributeAssignResult.getAttributeAssign().getAttributeValueDelegate().assignValue(grouperRecentMembershipsIncludeEligible.getName(), "F");
    }
        
    // recent memberships should survive rename
    new GroupSave(grouperSession).assignGroupNameToEdit(groupSource.getName()).assignName("test:groupSourceRename").assignSetAlternateNameIfRename(false).save();

    runJobs(true, true);
  
    assertEquals(fullSyncCount+1, GrouperRecentMembershipsChangeLogConsumer.test_fullSyncCount);
    assertEquals(incrementalSyncCount, GrouperRecentMembershipsChangeLogConsumer.test_incrementalSyncCount);
  
    Group[] groupsRecentMemberships = new Group[13];
    for (int i=1;i<=12;i++) {
  
      groupsRecentMemberships[i] = GroupFinder.findByName(grouperSession, "test:group" + i + "dayRecentMemberships", false);
  
    }
    
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=1) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ0)));
    }
    for (int i=1;i<=12;i++) {
      assertTrue("" + i, (i>=2) == (groupsRecentMemberships[i] != null && groupsRecentMemberships[i].hasMember(SubjectTestHelper.SUBJ1)));
    }
    
  }

}
