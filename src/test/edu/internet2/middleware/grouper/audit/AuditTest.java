/*
 * @author mchyzer
 * $Id: AuditTest.java,v 1.6 2009-02-10 05:23:45 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import java.util.List;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupTypeTupleDAO;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AuditTest extends GrouperTest {
  /**
   * @param name
   */
  public AuditTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AuditTest("testTypeTuples"));
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("grouper.env.name", "testEnv");
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    ApiConfig.testConfig.remove("grouper.env.name");

  }

  /**
   * @throws Exception 
   * 
   */
  public void testTypes() throws Exception {
    
    //delete all audit records
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationNanos should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2, one for select, one for insert: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());

    assertEquals("Context id's should match", auditEntry.getContextId(), groupType.getContextId());
    
    assertEquals("engine should be junit", GrouperEngineBuiltin.JUNIT.getGrouperEngine(), auditEntry.getGrouperEngine());

    assertNotNull("createdOn should exist", auditEntry.getCreatedOn());

    assertEquals("testEnv", auditEntry.getEnvName());
    
    assertNotNull(auditEntry.getLastUpdated());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    assertTrue("grouper version is blank", StringUtils.isNotBlank(auditEntry.getGrouperVersion()));

    assertTrue("server host is blank", StringUtils.isNotBlank(auditEntry.getServerHost()));

    assertTrue("server user name is blank", StringUtils.isNotBlank(auditEntry.getServerUserName()));

    GroupType.createType(grouperSession, "test1", false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");

    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    groupType.delete(grouperSession);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOn").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("durationNanos should exist", auditEntry2.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 1, one for delete: " + auditEntry2.getQueryCount(), 2 <= auditEntry2.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry2.getContextId(), groupType.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    
  }

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession = SessionHelper.getRootSession();
  
  /** root stem */
  private Stem root;

  /**
   * @throws Exception 
   */
  public void testTypeTuples() throws Exception {
    GroupType groupType = GroupType.createType(grouperSession, "test1");

    Group group = StemHelper.addChildGroup(this.edu, "test1", "the test1");
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");
    
    group.addType(groupType);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));

    GroupTypeTuple groupTypeTuple = Hib3GroupTypeTupleDAO.findByGroupAndType(group, groupType);
    
    assertEquals("Context id's should match", auditEntry.getContextId(), groupTypeTuple.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    group.addType(groupType, false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");

    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different
    GrouperUtil.sleep(1000);
    
    group.deleteType(groupType);

    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOn").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);

    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testFields() throws Exception {

    //add a type
    GrouperSession grouperSession = SessionHelper.getRootSession();
    GroupType groupType = GroupType.createType(grouperSession, "test1");
    
    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    Field field = groupType.addAttribute(grouperSession, "test1attr", AccessPrivilege.READ, AccessPrivilege.ADMIN,true);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationNanos should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2, one for select, one for insert: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), field.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry.getDescription()));
    
    groupType.addAttribute(grouperSession, "test1attr", AccessPrivilege.READ, AccessPrivilege.ADMIN,true, false);
    
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Shouldnt have changed since type didnt change", auditCount+1, newAuditCount);
    
    //make sure date is different on mysql
    GrouperUtil.sleep(1000);

    //try an update
    field = groupType.addOrUpdateAttribute(grouperSession, "test1attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, true);
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly two audits", auditCount+2, newAuditCount);
  
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOn").list(AuditEntry.class);
    AuditEntry auditEntry2 = auditEntries.get(1);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry2.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry2.getContextId()));
    
    assertTrue("durationNanos should exist", auditEntry2.getDurationMicroseconds() > 0);
  
    assertEquals("Context id's should match", auditEntry2.getContextId(), field.getContextId());
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry2.getDescription()));
    
    //make sure date is different on mysql
    GrouperUtil.sleep(1000);

    //try a delete
    groupType.deleteField(grouperSession, "test1attr");
  
    newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
  
    assertEquals("Should have added exactly three audits", auditCount+3, newAuditCount);
  
     auditEntries = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry order by createdOn").list(AuditEntry.class);
    AuditEntry auditEntry3 = auditEntries.get(2);
  
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry3.getContextId()));
    
    assertTrue("contextIds should be different", !StringUtils.equals(auditEntry.getContextId(), auditEntry3.getContextId()));
    
    assertTrue("description is blank", StringUtils.isNotBlank(auditEntry3.getDescription()));
    
    
  }
}
