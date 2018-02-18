/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.dao.AuditTypeDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


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
    TestRunner.run(new GrouperDaemonDeleteOldRecordsTest("testDeleteOldAuditEntry"));
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

    
  }
}
