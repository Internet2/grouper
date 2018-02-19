/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperDaemonDeleteOldRecordsTest extends GrouperTest {

  /**
   * 
   */
  public GrouperDaemonDeleteOldRecordsTest() {
  }

  /**
   * @param name
   */
  public GrouperDaemonDeleteOldRecordsTest(String name) {
    super(name);

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonDeleteOldRecordsTest("testDeleteOldDeletedPointInTime"));
  }

  /**
   * 
   */
  public void testDeleteOldAuditEntryNoLoggedInUser() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //clear out
    AuditTypeDAO auditTypeDao = GrouperDAOFactory.getFactory().getAuditType();
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    final AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
    auditType.setId(GrouperUuid.getUuid());
        
    auditTypeDao.saveOrUpdate(auditType);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true);
    
    Calendar sixDaysAgo = new GregorianCalendar();
    sixDaysAgo.setTimeInMillis(System.currentTimeMillis());
    sixDaysAgo.add(Calendar.DAY_OF_YEAR, -6);
    
    Calendar eightDaysAgo = new GregorianCalendar();
    eightDaysAgo.setTimeInMillis(System.currentTimeMillis());
    eightDaysAgo.add(Calendar.DAY_OF_YEAR, -8);
    
    AuditEntry auditEntryLoggedIn1 = new AuditEntry();
    auditEntryLoggedIn1.setAuditTypeId(auditType.getId());
    auditEntryLoggedIn1.setDescription("whatever");
    auditEntryLoggedIn1.setId(GrouperUuid.getUuid());
    auditEntryLoggedIn1.setString01("something");
    auditEntryLoggedIn1.setLoggedInMemberId(member.getId());
    auditEntryLoggedIn1.setCreatedOnDb(sixDaysAgo.getTimeInMillis());
    auditEntryLoggedIn1.saveOrUpdate(false);
    
    AuditEntry auditEntryLoggedIn2 = new AuditEntry();
    auditEntryLoggedIn2.setAuditTypeId(auditType.getId());
    auditEntryLoggedIn2.setDescription("whatever");
    auditEntryLoggedIn2.setId(GrouperUuid.getUuid());
    auditEntryLoggedIn2.setString01("something");
    auditEntryLoggedIn2.setLoggedInMemberId(member.getId());
    auditEntryLoggedIn2.setCreatedOnDb(eightDaysAgo.getTimeInMillis());
    auditEntryLoggedIn2.saveOrUpdate(false);
    
    AuditEntry auditEntryNotLoggedIn1 = new AuditEntry();
    auditEntryNotLoggedIn1.setAuditTypeId(auditType.getId());
    auditEntryNotLoggedIn1.setDescription("whatever");
    auditEntryNotLoggedIn1.setId(GrouperUuid.getUuid());
    auditEntryNotLoggedIn1.setString01("something");
    auditEntryNotLoggedIn1.setCreatedOnDb(sixDaysAgo.getTimeInMillis());
    auditEntryNotLoggedIn1.saveOrUpdate(false);
    
    AuditEntry auditEntryNotLoggedIn2 = new AuditEntry();
    auditEntryNotLoggedIn2.setAuditTypeId(auditType.getId());
    auditEntryNotLoggedIn2.setDescription("whatever");
    auditEntryNotLoggedIn2.setId(GrouperUuid.getUuid());
    auditEntryNotLoggedIn2.setString01("something");
    auditEntryNotLoggedIn2.setCreatedOnDb(eightDaysAgo.getTimeInMillis());
    auditEntryNotLoggedIn2.saveOrUpdate(false);
    
    // loader.retain.db.audit_entry_no_logged_in_user.days=1825
    // try turned off
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry_no_logged_in_user.days", "-1");
    assertEquals(-1, GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 9 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry_no_logged_in_user.days", "9");
    assertEquals(0, GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 7 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry_no_logged_in_user.days", "7");
    assertEquals(1, GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 5 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry_no_logged_in_user.days", "5");
    assertEquals(1, GrouperDaemonDeleteOldRecords.deleteOldAuditEntryNoLoggedInUser());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    //clear out
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");

    GrouperSession.stopQuietly(grouperSession);
  }
  
  /**
   * 
   */
  public void testDeleteOldAuditEntry() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //clear out
    AuditTypeDAO auditTypeDao = GrouperDAOFactory.getFactory().getAuditType();
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");
    
    final AuditType auditType = new AuditType("a", "b", null, "s1", "s2");
    auditType.setId(GrouperUuid.getUuid());
        
    auditTypeDao.saveOrUpdate(auditType);
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true);
    
    Calendar sixDaysAgo = new GregorianCalendar();
    sixDaysAgo.setTimeInMillis(System.currentTimeMillis());
    sixDaysAgo.add(Calendar.DAY_OF_YEAR, -6);
    
    Calendar eightDaysAgo = new GregorianCalendar();
    eightDaysAgo.setTimeInMillis(System.currentTimeMillis());
    eightDaysAgo.add(Calendar.DAY_OF_YEAR, -8);
    
    AuditEntry auditEntryLoggedIn1 = new AuditEntry();
    auditEntryLoggedIn1.setAuditTypeId(auditType.getId());
    auditEntryLoggedIn1.setDescription("whatever");
    auditEntryLoggedIn1.setId(GrouperUuid.getUuid());
    auditEntryLoggedIn1.setString01("something");
    auditEntryLoggedIn1.setLoggedInMemberId(member.getId());
    auditEntryLoggedIn1.setCreatedOnDb(sixDaysAgo.getTimeInMillis());
    auditEntryLoggedIn1.saveOrUpdate(false);
    
    AuditEntry auditEntryLoggedIn2 = new AuditEntry();
    auditEntryLoggedIn2.setAuditTypeId(auditType.getId());
    auditEntryLoggedIn2.setDescription("whatever");
    auditEntryLoggedIn2.setId(GrouperUuid.getUuid());
    auditEntryLoggedIn2.setString01("something");
    auditEntryLoggedIn2.setLoggedInMemberId(member.getId());
    auditEntryLoggedIn2.setCreatedOnDb(eightDaysAgo.getTimeInMillis());
    auditEntryLoggedIn2.saveOrUpdate(false);
    
    AuditEntry auditEntryNotLoggedIn1 = new AuditEntry();
    auditEntryNotLoggedIn1.setAuditTypeId(auditType.getId());
    auditEntryNotLoggedIn1.setDescription("whatever");
    auditEntryNotLoggedIn1.setId(GrouperUuid.getUuid());
    auditEntryNotLoggedIn1.setString01("something");
    auditEntryNotLoggedIn1.setCreatedOnDb(sixDaysAgo.getTimeInMillis());
    auditEntryNotLoggedIn1.saveOrUpdate(false);
    
    AuditEntry auditEntryNotLoggedIn2 = new AuditEntry();
    auditEntryNotLoggedIn2.setAuditTypeId(auditType.getId());
    auditEntryNotLoggedIn2.setDescription("whatever");
    auditEntryNotLoggedIn2.setId(GrouperUuid.getUuid());
    auditEntryNotLoggedIn2.setString01("something");
    auditEntryNotLoggedIn2.setCreatedOnDb(eightDaysAgo.getTimeInMillis());
    auditEntryNotLoggedIn2.saveOrUpdate(false);
    
    //  ############################################
    //  ## Some think its ok to remove all audit entries over 10 (or X) years, but will default this 
    //  ## to never since even at large institutions there aren't that many records.  
    //  ## These are audits for things people do on the UI or WS generally (as a different to records with no logged in user) 
    //  ############################################
    //  
    //  # number of days to retain db rows in grouper_audit_entry.  -1 is forever.  suggested is -1 or ten years: 3650
    //  loader.retain.db.audit_entry.days=-1
    // try turned off
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "-1");
    assertEquals(-1, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 9 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "9");
    assertEquals(0, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 7 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "7");
    assertEquals(2, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
    
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    // try 5 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "5");
    assertEquals(2, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
    
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));

    //clear out
    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");

    GrouperSession.stopQuietly(grouperSession);

    
  }
  
  /**
   * 
   */
  public void testDeleteOldDeletedPointInTime() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //clear out
    Group group1 = new GroupSave(grouperSession).assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    
    group1.delete();

    Group group2 = new GroupSave(grouperSession).assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    group2.delete();

    //run the temp to change log
    ChangeLogTempToEntity.convertRecords();

    long sixDaysAgoMillisTime1000 = -1;
    
    {
      Calendar sixDaysAgo = new GregorianCalendar();
      sixDaysAgo.setTimeInMillis(System.currentTimeMillis());
      sixDaysAgo.add(Calendar.DAY_OF_YEAR, -6);
      sixDaysAgoMillisTime1000 = sixDaysAgo.getTimeInMillis()*1000;
    }
    
    long eightDaysAgoMillisTime1000 = -1;

    {
      Calendar eightDaysAgo = new GregorianCalendar();
      eightDaysAgo.setTimeInMillis(System.currentTimeMillis());
      eightDaysAgo.add(Calendar.DAY_OF_YEAR, -8);
      eightDaysAgoMillisTime1000 = eightDaysAgo.getTimeInMillis()*1000;
    }
    
    long tenDaysAgoMillisTime1000 = -1;

    {
      Calendar tenDaysAgo = new GregorianCalendar();
      tenDaysAgo.setTimeInMillis(System.currentTimeMillis());
      tenDaysAgo.add(Calendar.DAY_OF_YEAR, -10);
      tenDaysAgoMillisTime1000 = tenDaysAgo.getTimeInMillis()*1000;
    }
    
    Set<PITGroup> pitGroups1 = GrouperDAOFactory.getFactory().getPITGroup().findByName(group1.getName(), true);
    assertEquals(1, GrouperUtil.length(pitGroups1));
    PITGroup pitGroup1 = pitGroups1.iterator().next();
    pitGroup1.setStartTimeDb(tenDaysAgoMillisTime1000);
    pitGroup1.setEndTimeDb(sixDaysAgoMillisTime1000);
    pitGroup1.saveOrUpdate();
    
    Set<PITGroup> pitGroups2 = GrouperDAOFactory.getFactory().getPITGroup().findByName(group2.getName(), true);
    assertEquals(1, GrouperUtil.length(pitGroups2));
    PITGroup pitGroup2 = pitGroups2.iterator().next();
    pitGroup2.setStartTimeDb(tenDaysAgoMillisTime1000);
    pitGroup2.setEndTimeDb(eightDaysAgoMillisTime1000);
    pitGroup2.saveOrUpdate();

    //  ############################################
    //  ## After you delete an object in grouper, it is still in point in time.  So if you want to know who was in a group a year ago, you need this info
    //  ## However, after some time it might be ok to let it go.  So the default is 5 years
    //  ############################################
    //
    //  # number of days to retain db rows for point in time deleted objects.  -1 is forever.  suggested is 365.  default is five years: 1825
    //  loader.retain.db.point_in_time_deleted_objects.days=1825
    
    // try turned off
    int pitGroupsCountOrig = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_groups");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.point_in_time_deleted_objects.days", "-1");
    assertEquals(-1, GrouperDaemonDeleteOldRecords.deleteOldDeletedPointInTimeObjects());

    int pitGroupsCountNew = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_groups");
    assertEquals(pitGroupsCountOrig, pitGroupsCountNew);
    
    pitGroups1 = GrouperDAOFactory.getFactory().getPITGroup().findByName(group1.getName(), true);
    assertEquals(1, GrouperUtil.length(pitGroups1));
    
    pitGroups2 = GrouperDAOFactory.getFactory().getPITGroup().findByName(group2.getName(), true);
    assertEquals(1, GrouperUtil.length(pitGroups2));
    
    
    // try 9 days ago
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "9");
    assertEquals(0, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
//    
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));
//
//    // try 7 days ago
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "7");
//    assertEquals(2, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
//    
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
//    assertNotNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));
//
//    // try 5 days ago
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("loader.retain.db.audit_entry.days", "5");
//    assertEquals(2, GrouperDaemonDeleteOldRecords.deleteOldAuditEntry());
//    
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn1.getId(), false));
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryLoggedIn2.getId(), false));
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn1.getId(), false));
//    assertNull(GrouperDAOFactory.getFactory().getAuditEntry().findById(auditEntryNotLoggedIn2.getId(), false));
//
//    //clear out
//    auditTypeDao.deleteEntriesAndTypesByCategoryAndAction("a", "b");

    GrouperSession.stopQuietly(grouperSession);

  }

  
}
